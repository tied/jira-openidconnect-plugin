package com.atlassian.tutorial;

import com.auth0.AuthenticationController;

public abstract class AuthenticationProvider {

    // todo: set this parameters in the config file
    private static final String DOMAIN = "";
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";

    public static AuthenticationController getInstance() {
        return AuthenticationController.newBuilder(DOMAIN, CLIENT_ID, CLIENT_SECRET)
                .build();
    }

    public static String getDomain() {
        return DOMAIN;
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getClientSecret() {
        return CLIENT_SECRET;
    }
}
