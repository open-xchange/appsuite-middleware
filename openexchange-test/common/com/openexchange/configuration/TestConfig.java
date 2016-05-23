/**
 *
 */
package com.openexchange.configuration;

import com.openexchange.exception.OXException;
import com.openexchange.tools.conf.AbstractConfig;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TestConfig extends AbstractConfig {

    private static volatile boolean initialized = false;

    /**
     * Singleton.
     */
    private static TestConfig singleton;

    /**
     * Prevent instantiation
     */
    private TestConfig() {
        super();
    }

    /**
     * Key of the system property that contains the file name of the
     * system.properties configuration file.
     */
    private static final String KEY = "test.propfile";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        String fileName = System.getProperty(KEY);
        if (null == fileName) {
        	fileName = "conf/test.properties";
        }
    	return fileName;
    }

    /**
     * Reads the configuration.
     * @throws OXException if reading configuration fails.
     */
    public static void init() throws OXException {
        if (null == singleton) {
            synchronized (TestConfig.class) {
                if (null == singleton) {
                    singleton = new TestConfig();
                    singleton.loadPropertiesInternal();
                    initialized = true;
                }
            }
        }
    }

    public static String getProperty(final Property key) {
        if (!initialized) {
            try {
                init();
            } catch (final OXException e) {
                return null;
            }
        }
        return singleton.getPropertyInternal(key.getPropertyName());
    }

    /**
     * Enumeration of all properties in the test.properties file.
     */
    public static enum Property {
        /**
         * ajax.properties
         */
        AJAX_PROPS("ajaxPropertiesFile"),
        /**
         * mail.properties
         */
        MAIL_PROPS("mailPropertiesFile"),
        /**
         * pubsub.properties
         */
        PUBSUB_PROPS("pubsubPropertiesFile"),
        /**
         * webdav.properties
         */
        WEBDAV_PROPS("webdavPropertiesFile"),
        /**
         * xingtest.properties
         */
        XING_PROPS("xingPropertiesFile"),
        /**
         * googletest.properties
         */
        GOOGLE_PROPS("googlePropertiesFile"),

        TEST_DATA_DIR("testDataDir"),

        FILESTORE("filestoreId");

        /**
         * Name of the property in the test.properties file.
         */
        private String propertyName;

        /**
         * Default constructor.
         * @param propertyName Name of the property in the test.properties
         * file.
         */
        private Property(final String propertyName) {
            this.propertyName = propertyName;
        }

        /**
         * @return the propertyName
         */
        public String getPropertyName() {
            return propertyName;
        }
    }
}
