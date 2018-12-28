package com.atlassian.tutorial.auth;

import com.auth0.client.auth.AuthAPI;

public abstract class AuthenticationProvider {

    // todo: set this parameters in the config file
    private static final String DOMAIN = "";
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";

    public static AuthenticationHandler getInstance() {
        return new AuthenticationHandler(new AuthAPI(DOMAIN, CLIENT_ID, CLIENT_SECRET));
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
