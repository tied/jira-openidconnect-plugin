package it.com.atlassian.openid.connect.util;

import java.io.IOException;
import java.util.Properties;

public final class PropertyLoader {

    private static final String CONFIG_FILE_NAME = "config.properties";
    public static final String LOCALTEST_PROPERTIES = "localtest.properties";

    public static String getProperty(String name) {
        Properties properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream(CONFIG_FILE_NAME));
        } catch (IOException e) {
            return null;
        }

        return properties.getProperty(name);
    }

    public static String getJiraProperty(String name) {
        Properties properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream(LOCALTEST_PROPERTIES));
        } catch (IOException e) {
            return null;
        }

        return properties.getProperty(name);
    }

    private PropertyLoader() {}

}
