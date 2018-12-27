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
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.tutorial.util.SessionConstants;
import com.auth0.SessionUtils;
import com.auth0.json.auth.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private JiraAuthenticationContext jiraAuthenticationContext;

    public SuccessOauthLoggedInServlet(TemplateRenderer templateRenderer, UserManager userManager,
                                       UserSearchService userSearchService, JiraAuthenticationContext jiraAuthenticationContext) {
        this.templateRenderer = templateRenderer;
        this.userManager = userManager;
        this.userSearchService = userSearchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // todo: check user logged in or not
        final String idToken = (String) SessionUtils.get(req, SessionConstants.ID_TOKEN);
        // todo: remove tokens from logs
        log.debug("Token {}: {}", SessionConstants.ID_TOKEN, idToken);

        final String accessToken = (String) SessionUtils.get(req, SessionConstants.ACCESS_TOKEN);
        log.debug("Token {}: {}", SessionConstants.ACCESS_TOKEN, accessToken);

        final UserInfo userInfo = (UserInfo) SessionUtils.get(req, SessionConstants.USER_INFO);

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> userInfoParams = userInfo.getValues();
        context.put("userInfo", userInfoParams);
        context.put("userName", userInfoParams.get("nickname"));

        // todo: it can be only one user. need to think about this iterable
        Iterable<ApplicationUser> usersByEmail = userSearchService.findUsersByEmail((String) userInfoParams.get("email"));
        Iterator<ApplicationUser> iterator = usersByEmail.iterator();
        ApplicationUser currentUser;

        if (iterator.hasNext()) {
            currentUser = iterator.next();
            log.info("Found user in the system: {}", currentUser);

            log.info("Clear logged in current user {}", jiraAuthenticationContext.getLoggedInUser());
            jiraAuthenticationContext.clearLoggedInUser();

            log.info("Log in retrieved user {}", currentUser);
            jiraAuthenticationContext.setLoggedInUser(currentUser);
        } else {
            log.info("User with email {} not found", userInfoParams.get("email"));
            log.info("Creating new user with email {}", userInfoParams.get("email"));

            UserDetails userDetails = new UserDetails((String) userInfoParams.get("nickname"), (String) userInfoParams.get("name"));
            userDetails = userDetails.withEmail((String) userInfoParams.get("email"));
            userDetails = userDetails.withPassword("1");

            try {
                currentUser = userManager.createUser(userDetails);
                log.info("User created: {}", currentUser);
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
