package com.atlassian.openid.connect.auth;

import com.atlassian.openid.connect.config.AuthenticationInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.openid.connect.util.AuthenticationInfoChecker;
import com.auth0.client.auth.AuthAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class AuthenticationProvider {

    private final TransactionTemplate transactionTemplate;

    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public AuthenticationProvider(@JiraImport TransactionTemplate transactionTemplate,
                                  @JiraImport PluginSettingsFactory pluginSettingsFactory) {
        this.transactionTemplate = transactionTemplate;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public AuthenticationHandler getInstance() {
        return transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            AuthenticationInfo config = new AuthenticationInfo();
            config.setDomain((String) settings.get(AuthenticationInfo.class.getName() + ".domain"));
            config.setClientId((String) settings.get(AuthenticationInfo.class.getName() + ".clientId"));
            config.setClientSecret((String) settings.get(AuthenticationInfo.class.getName() + ".clientSecret"));

            AuthenticationInfoChecker.checkAuthenticationInfo(config);
            AuthAPI authAPI = new AuthAPI(config.getDomain(), config.getClientId(), config.getClientSecret());
            return new AuthenticationHandler(authAPI, config);
        });
    }


}