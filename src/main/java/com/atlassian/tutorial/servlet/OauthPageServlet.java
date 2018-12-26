package com.atlassian.tutorial.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.tutorial.AuthenticationProvider;
import com.auth0.AuthenticationController;
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
public class OauthPageServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(OauthPageServlet.class);

    @JiraImport
    private IssueService issueService;
    @JiraImport
    private ProjectService projectService;
    @JiraImport
    private SearchService searchService;
    @JiraImport
    private TemplateRenderer templateRenderer;
    @JiraImport
    private JiraAuthenticationContext authenticationContext;
    @JiraImport
    private ConstantsManager constantsManager;

    private static final String OAUTH_LOGIN_PAGE_TEMPLATE = "/templates/login-outh.vm";

    private AuthenticationController authenticationController;

    public OauthPageServlet(IssueService issueService, ProjectService projectService, SearchService searchService,
                            TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, ConstantsManager constantsManager) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;

        authenticationController = AuthenticationProvider.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> context = new HashMap<>();
        resp.setContentType("text/html;charset=utf-8");

        log.info("Render templage: {}", OAUTH_LOGIN_PAGE_TEMPLATE);
        templateRenderer.render(OAUTH_LOGIN_PAGE_TEMPLATE, context, resp.getWriter());
    }


    // todo: test this method
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String redirectUri = req.getScheme() + "://" + req.getServerName() + ":"
                + req.getServerPort() + "/jira/plugins/servlet/callback";
        log.info("Redirect uri: {}", redirectUri);
        String authorizeUrl = authenticationController.buildAuthorizeUrl(req, redirectUri)
                .withAudience(String.format("https://%s/userinfo", AuthenticationProvider.getDomain()))
                .build();

        log.info("Redirect on {} for authorization", authorizeUrl);
        resp.sendRedirect(authorizeUrl);
    }
}