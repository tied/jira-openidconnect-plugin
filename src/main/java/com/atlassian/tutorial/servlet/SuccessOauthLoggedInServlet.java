package com.atlassian.tutorial.servlet;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.tutorial.util.SessionConstants;
import com.auth0.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Scanned
public class SuccessOauthLoggedInServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SuccessOauthLoggedInServlet.class);
    private static final String SUCCESS_LOGIN_PAGE = "/templates/success-login.vm";

    @JiraImport
    private TemplateRenderer templateRenderer;

    @JiraImport
    private UserManager userManager;

    @JiraImport
    private UserSearchService userSearchService;

    @JiraImport
    private JiraAuthenticationContext authContext;

    public SuccessOauthLoggedInServlet(TemplateRenderer templateRenderer, UserManager userManager,
                                       UserSearchService userSearchService, JiraAuthenticationContext authContext) {
        this.templateRenderer = templateRenderer;
        this.userManager = userManager;
        this.userSearchService = userSearchService;
        this.authContext = authContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // todo: check user logged in or not
        final String idToken = (String) SessionUtils.get(req, SessionConstants.ID_TOKEN);
        // todo: remove tokens from logs
        log.debug("Token {}: {}", SessionConstants.ID_TOKEN, idToken);

        final String accessToken = (String) SessionUtils.get(req, SessionConstants.ACCESS_TOKEN);
        log.debug("Token {}: {}", SessionConstants.ACCESS_TOKEN, accessToken);

        final Map<String, Object> userInfoValues = (Map<String, Object>) SessionUtils.get(req, SessionConstants.USER_INFO);

        Map<String, Object> context = new HashMap<>();
        context.put("userInfo", userInfoValues);
        context.put("userName", userInfoValues.get("nickname"));

        // todo: it can be only one user. need to think about this iterable
        Iterable<ApplicationUser> usersByEmail = userSearchService.findUsersByEmail((String) userInfoValues.get("email"));
        Iterator<ApplicationUser> iterator = usersByEmail.iterator();
        ApplicationUser appUser;

        if (iterator.hasNext()) {
            appUser = iterator.next();
            log.info("Found user in the system: {}", appUser);

            authContext.setLoggedInUser(appUser);
            log.info("Log in retrieved user {}", appUser);

            HttpSession session = req.getSession();

            session.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            session.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
        } else {
            log.info("User with email {} not found", userInfoValues.get("email"));
            log.info("Creating new user with email {}", userInfoValues.get("email"));

            //todo: set default permission
            UserDetails userDetails = new UserDetails((String) userInfoValues.get("nickname"), (String) userInfoValues.get("name"));
            userDetails = userDetails.withEmail((String) userInfoValues.get("email"));
            userDetails = userDetails.withPassword("1");

            try {
                appUser = userManager.createUser(userDetails);
                log.info("User created: {}", appUser);
            } catch (CreateException e) {
                log.error("Cannot create user! {}", e);
                e.printStackTrace();
                return;
            } catch (PermissionException e) {
                log.error("Do not have enough permissions! {}", e);
                e.printStackTrace();
                return;
            }
        }

        log.info("Render template: {}", SUCCESS_LOGIN_PAGE);
        templateRenderer.render(SUCCESS_LOGIN_PAGE, context, resp.getWriter());
    }
}
