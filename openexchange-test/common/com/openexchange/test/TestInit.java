
package com.openexchange.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestInit {

    private static boolean testPropertiesLoaded = false;
    public static Properties testProps = null;

    public static void loadTestProperties() {
        if (!testPropertiesLoaded) {
            testProps = new Properties();
            try {
                String propfile = System.getProperty("test.propfile");
                if (null == propfile) {
                    propfile = "conf/test.properties";
                }
                testProps.load(new FileInputStream(propfile));
            } catch (final IOException e) {
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
