package it.com.atlassian.openid.connect.util;

public final class TestConstants {

    public static final String AUTH_DOMAIN = PropertyLoader.getProperty("openidconnect.auth.domain");
    public static final String AUTH_HOST = PropertyLoader.getProperty("openidconnect.auth.host");
    public static final int AUTH_PORT = Integer.parseInt(PropertyLoader.getProperty("openidconnect.auth.port"));

    public static final String AUTH_CLIENT_ID = PropertyLoader.getProperty("openidconnect.auth.client-id");
    public static final String AUTH_CLIENT_SECRET = PropertyLoader.getProperty("openidconnect.auth.client-secret");

    public static final String JIRA_HOST = PropertyLoader.getJiraProperty("jira.host");
    public static final int JIRA_PORT = Integer.parseInt(PropertyLoader.getJiraProperty("jira.port"));
    public static final String JIRA_DOMAIN = String.format("http://%s:%d", TestConstants.JIRA_HOST, TestConstants.JIRA_PORT);

    public static final String ID_TOKEN = "id_token";
    public static final String ACCESS_TOKEN = "access_token";

    private TestConstants() {}

}
