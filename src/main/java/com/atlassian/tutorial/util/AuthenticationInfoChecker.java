package com.atlassian.tutorial.util;

import com.atlassian.tutorial.config.AuthenticationInfo;
import com.atlassian.tutorial.config.AuthenticationInfoException;
import org.apache.commons.lang3.StringUtils;

public final class AuthenticationInfoChecker {

    public static void checkAuthenticationInfo(AuthenticationInfo config) {
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

    private AuthenticationInfoChecker() {
    }

}
