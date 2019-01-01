package com.atlassian.tutorial.auth;

import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.tutorial.config.AuthenticationInfo;
import com.atlassian.tutorial.config.AuthenticationInfoException;
import com.auth0.client.auth.AuthAPI;
import org.apache.commons.lang3.StringUtils;
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

            checkConfig(config);
            AuthAPI authAPI = new AuthAPI(config.getDomain(), config.getClientId(), config.getClientSecret());
            return new AuthenticationHandler(authAPI, config);
        });
    }

    private void checkConfig(AuthenticationInfo config) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(config.getDomain())) {
            sb.append("Authorization domain is not set up!").append(System.lineSeparator());
        }
        if (StringUtils.isBlank(config.getClientId())) {
            sb.append("Client id is not set up! ").append(System.lineSeparator());
        }
        if (StringUtils.isBlank(config.getClientSecret())) {
            sb.append("Client secret is not set up! ").append(System.lineSeparator());
        }
        if (sb.length() > 0) {
            throw new AuthenticationInfoException(sb.toString());
        }
    }

}