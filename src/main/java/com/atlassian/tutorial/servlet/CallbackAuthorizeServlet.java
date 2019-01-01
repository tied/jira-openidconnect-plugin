package com.atlassian.tutorial.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.tutorial.auth.AuthenticationHandler;
import com.atlassian.tutorial.auth.AuthenticationProvider;
import com.atlassian.tutorial.util.SessionConstants;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.auth.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Scanned
public class CallbackAuthorizeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CallbackAuthorizeServlet.class);

    private AuthenticationProvider authenticationProvider;

    @Autowired
    public CallbackAuthorizeServlet(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    private String redirectOnSuccess;
    private String redirectOnFail;

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.debug("Initialize {} servlet", this.getClass().getName());
        super.init(config);

        redirectOnSuccess = "/jira/plugins/servlet/success";
        redirectOnFail = "/jira/plugins/servlet/oauth-login";
        log.debug("Initialization of {} servlet finished", this.getClass().getName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticationHandler authenticationHandler = authenticationProvider.getInstance();
        try {
            log.info("Handle callback authorize request");

            TokenHolder tokens = authenticationHandler.handle(req);
            log.info("Token processed successfully: {}", tokens);

            req.getSession().setAttribute(SessionConstants.ACCESS_TOKEN, tokens.getAccessToken());
            log.debug("Token {}: {}", SessionConstants.ACCESS_TOKEN, tokens.getAccessToken());

            req.getSession().setAttribute(SessionConstants.ID_TOKEN, tokens.getIdToken());
            log.debug("Token {}: {}", SessionConstants.ID_TOKEN, tokens.getIdToken());

            // todo: handle Auth0Exception
            UserInfo userInfo = authenticationHandler.getUserInfo(tokens.getAccessToken());

            Map<String, Object> userInfoValues = userInfo.getValues();
            log.info("User info: {}", userInfoValues);

            req.getSession().setAttribute(SessionConstants.USER_INFO, userInfoValues);

            log.info("Redirect on: {}", redirectOnSuccess);
            resp.sendRedirect(redirectOnSuccess);
        } catch (RuntimeException e) {
            // todo: check error handling
            log.error("Token is not correct: {}");
            e.printStackTrace();

            log.info("Redirect on: {}", redirectOnFail);
            resp.sendRedirect(redirectOnFail);
        }
    }


}
