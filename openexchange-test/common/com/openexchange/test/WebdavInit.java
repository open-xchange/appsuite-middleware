package com.openexchange.test;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WebdavInit {
    private static boolean webdavPropertiesLoaded = false;
    public static Properties webdavProps = null;

    private static void loadWebdavProperties() {
        TestInit.loadTestProperties();
        webdavProps = new Properties();
        try {
            webdavProps.load(new FileInputStream(TestInit.getTestProperties()
                    .getProperty("webdavPropertiesFile")));
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        webdavPropertiesLoaded = true;
    }

    public static Properties getWebdavProperties() {
        if (!webdavPropertiesLoaded) {
            loadWebdavProperties();
        }
        return webdavProps;
    }
}
