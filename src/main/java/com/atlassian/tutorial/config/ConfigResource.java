package com.atlassian.tutorial.config;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugin.webfragment.conditions.cache.ConditionCacheKeys;
import com.atlassian.jira.plugin.webfragment.conditions.cache.RequestCachingConditionHelper;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Scanned
public class ConfigResource {

    @JiraImport
    private PluginSettingsFactory pluginSettingsFactory;

    @JiraImport
    private TransactionTemplate transactionTemplate;

    @JiraImport
    private GlobalPermissionManager permissionManager;

    @JiraImport
    private JiraAuthenticationContext authenticationContext;

    public ConfigResource(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate,
                          GlobalPermissionManager permissionManager, JiraAuthenticationContext authenticationContext) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest request) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        boolean isSystemAdmin = RequestCachingConditionHelper.cacheConditionResultInRequest(ConditionCacheKeys.permission(GlobalPermissionKey.SYSTEM_ADMIN, user),
                () -> this.permissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user));
        if (!isSystemAdmin) {
            return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

        return Response.ok(transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            AuthenticationInfo config = new AuthenticationInfo();
            config.setDomain((String) settings.get(AuthenticationInfo.class.getName() + ".domain"));
            config.setClientId((String) settings.get(AuthenticationInfo.class.getName() + ".clientId"));
            config.setClientSecret((String) settings.get(AuthenticationInfo.class.getName() + ".clientSecret"));
            return config;
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final AuthenticationInfo config, @Context HttpServletRequest request) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
        boolean isSystemAdmin = RequestCachingConditionHelper.cacheConditionResultInRequest(ConditionCacheKeys.permission(GlobalPermissionKey.SYSTEM_ADMIN, user),
                () -> this.permissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user));
        if (!isSystemAdmin) {
            return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

        transactionTemplate.execute(() -> {
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            pluginSettings.put(AuthenticationInfo.class.getName() + ".domain", config.getDomain());
            pluginSettings.put(AuthenticationInfo.class.getName() + ".clientId", config.getClientId());
            pluginSettings.put(AuthenticationInfo.class.getName() + ".clientSecret", config.getClientSecret());
            return null;
        });
        return Response.noContent().build();
    }


}
