package com.atlassian.openid.connect.servlet;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugin.webfragment.conditions.cache.ConditionCacheKeys;
import com.atlassian.jira.plugin.webfragment.conditions.cache.RequestCachingConditionHelper;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Scanned
public class AdminServlet extends HttpServlet {

    private static final String ADMIN_TEMPLATE = "/templates/admin.vm";

    @JiraImport
    private GlobalPermissionManager permissionManager;

    @JiraImport
    private JiraAuthenticationContext authenticationContext;

    @JiraImport
    private TemplateRenderer templateRenderer;

    public AdminServlet(GlobalPermissionManager permissionManager, JiraAuthenticationContext authenticationContext,
                        TemplateRenderer templateRenderer) {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        boolean isSystemAdmin = RequestCachingConditionHelper.cacheConditionResultInRequest(ConditionCacheKeys.permission(GlobalPermissionKey.SYSTEM_ADMIN, user),
                () -> this.permissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user));
        if (!isSystemAdmin) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        templateRenderer.render(ADMIN_TEMPLATE, resp.getWriter());
    }

}
