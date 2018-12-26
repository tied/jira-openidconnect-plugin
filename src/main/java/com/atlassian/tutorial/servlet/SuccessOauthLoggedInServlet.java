package com.atlassian.tutorial.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.tutorial.util.SessionConstants;
import com.auth0.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Scanned
public class SuccessOauthLoggedInServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SuccessOauthLoggedInServlet.class);
    private static final String SUCCESS_LOGIN_PAGE = "/templates/success-login.vm";

    @JiraImport
    private TemplateRenderer templateRenderer;

    public SuccessOauthLoggedInServlet(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String idToken = (String) SessionUtils.get(req, SessionConstants.ID_TOKEN);
        final String accessToken = (String) SessionUtils.get(req, SessionConstants.ACCESS_TOKEN);
        Map<String, Object> context = new HashMap<>();

        if (accessToken!= null) {
            context.put("userId", idToken);
        } else if (idToken  != null) {
            context.put("userId", accessToken);
        }

        log.info("Render template: {}", SUCCESS_LOGIN_PAGE);
        templateRenderer.render(SUCCESS_LOGIN_PAGE, context, resp.getWriter());
    }
}
