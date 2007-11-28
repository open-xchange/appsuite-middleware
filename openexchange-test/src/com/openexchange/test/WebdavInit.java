package com.openexchange.test;


import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

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
        catch (IOException e) {
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
