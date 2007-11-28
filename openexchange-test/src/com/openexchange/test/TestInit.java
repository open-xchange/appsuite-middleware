package com.openexchange.test;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class TestInit {
    private static boolean testPropertiesLoaded = false;
    public static Properties testProps = null;

    public static void loadTestProperties() {
        if (!testPropertiesLoaded) {
            testProps = new Properties();
            try {
                final String propfile = System.getProperty("test.propfile");
                if (null == propfile) {
                    throw new RuntimeException("Test properties file "
                            + "test.propfile is not defined as a JVM "
                            + "system property.");
                }
                testProps.load(new FileInputStream(propfile));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            testPropertiesLoaded = true;
        }
    }

    public static Properties getTestProperties() {
        if (!testPropertiesLoaded) {
            loadTestProperties();
        }
        return testProps;
    }

    public static String getTestProperty(final String key) {
        return getTestProperties().getProperty(key);
    }
}
