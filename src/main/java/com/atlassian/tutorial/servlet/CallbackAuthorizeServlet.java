package com.atlassian.tutorial.servlet;

import com.atlassian.tutorial.AuthenticationProvider;
import com.atlassian.tutorial.util.SessionConstants;
import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CallbackAuthorizeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CallbackAuthorizeServlet.class);

    private AuthenticationController authenticationController;
    private String redirectOnSuccess;
    private String redirectOnFail;

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.debug("Initialize {} servlet", this.getClass().getName());
        super.init(config);

        authenticationController = AuthenticationProvider.getInstance();
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
        try {
            Tokens tokens = authenticationController.handle(req);
            log.info("Token processed successfully: {}", tokens);

            SessionUtils.set(req, SessionConstants.ACCESS_TOKEN, tokens.getAccessToken());
            SessionUtils.set(req, SessionConstants.ID_TOKEN, tokens.getIdToken());

            log.info("Redirect on: {}", redirectOnSuccess);
            resp.sendRedirect(redirectOnSuccess);
        } catch (IdentityVerificationException e) {
            log.error("Token is not correct: {}");
            log.error("Code of error: {}", e.getCode());
            if (e.isAPIError()) {
                log.error("It is API error!");
            } else if (e.isJWTError()) {
                log.error("It is JWT error!");
            }
            e.printStackTrace();

            log.info("Redirect on: {}", redirectOnFail);
            resp.sendRedirect(redirectOnFail);
        }
    }


}
