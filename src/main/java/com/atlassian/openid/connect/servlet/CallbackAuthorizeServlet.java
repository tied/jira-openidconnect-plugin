package com.atlassian.openid.connect.servlet;

import com.atlassian.openid.connect.auth.AuthenticationException;
import com.atlassian.openid.connect.auth.AuthenticationHandler;
import com.atlassian.openid.connect.auth.AuthenticationProvider;
import com.atlassian.openid.connect.model.Tokens;
import com.atlassian.openid.connect.util.SessionConstants;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.auth0.exception.Auth0Exception;
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

    private static final String INIT_PARAM_REDIRECT_ON_SUCCESS = "redirectOnSuccess";
    private static final String INIT_PARAM_REDIRECT_ON_FAIL = "redirectOnFail";

    private final AuthenticationProvider authenticationProvider;

    private String redirectOnSuccess;
    private String redirectOnFail;

    @Autowired
    public CallbackAuthorizeServlet(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.debug("Initializing {} servlet", this.getClass().getName());
        super.init(config);

        ServletConfig servletConfig = getServletConfig();
        redirectOnSuccess = servletConfig.getInitParameter(INIT_PARAM_REDIRECT_ON_SUCCESS);
        log.info("Redirect on success log in servlet path init: {}", redirectOnSuccess);

        redirectOnFail = servletConfig.getInitParameter(INIT_PARAM_REDIRECT_ON_FAIL);
        log.info("Redirect on fail log in servlet path init: {}", redirectOnFail);

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

            Tokens tokens = authenticationHandler.handle(req);
            log.info("Token processed successfully: {}", tokens);

            req.getSession().setAttribute(SessionConstants.ACCESS_TOKEN, tokens.getAccessToken());
            log.debug("Token {}: {}", SessionConstants.ACCESS_TOKEN, tokens.getAccessToken());

            req.getSession().setAttribute(SessionConstants.ID_TOKEN, tokens.getIdToken());
            log.debug("Token {}: {}", SessionConstants.ID_TOKEN, tokens.getIdToken());

            UserInfo userInfo = authenticationHandler.getUserInfo(tokens.getAccessToken());

            Map<String, Object> userInfoValues = userInfo.getValues();
            log.info("User info: {}", userInfoValues);

            req.getSession().setAttribute(SessionConstants.USER_INFO, userInfoValues);

            log.info("Redirect on: {}", redirectOnSuccess);
            resp.sendRedirect(redirectOnSuccess);
        } catch (AuthenticationException | Auth0Exception e) {
            log.error("Token is not correct: {}");
            e.printStackTrace();

            log.info("Redirect on: {}", redirectOnFail);
            resp.sendRedirect(redirectOnFail);
        }
    }

}
