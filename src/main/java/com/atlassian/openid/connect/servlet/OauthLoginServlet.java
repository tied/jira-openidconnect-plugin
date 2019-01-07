package com.atlassian.openid.connect.servlet;

import com.atlassian.openid.connect.auth.AuthenticationHandler;
import com.atlassian.openid.connect.auth.AuthenticationProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Scanned
public class OauthLoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(OauthLoginServlet.class);

    private static final String INIT_PARAM_CALLBACK_PATH = "callbackPath";

    private AuthenticationProvider authenticationProvider;

    private String callbackPath;

    @Autowired
    public OauthLoginServlet(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void init() throws ServletException {
        log.debug("Initializing {} servlet", this.getClass().getName());

        super.init();
        callbackPath = getServletConfig().getInitParameter(INIT_PARAM_CALLBACK_PATH);
        log.info("Callback servlet path init: {}", callbackPath);

        log.debug("Initialization of {} servlet finished", this.getClass().getName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    // todo: test this method
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String redirectUri = req.getScheme() + "://" + req.getServerName() + ":"
                + req.getServerPort() + callbackPath;
        log.info("Redirect uri: {}", redirectUri);

        AuthenticationHandler authHandler = authenticationProvider.getInstance();
        String authorizeUrl = authHandler.authorizeUrl(redirectUri)
                .withAudience(String.format("https://%s/userinfo", authHandler.getDomain()))
                .withScope("openid profile email")
                .build();

        log.info("Redirect on {} for authorization", authorizeUrl);
        resp.sendRedirect(authorizeUrl);
    }
}