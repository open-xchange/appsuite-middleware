/**
 * 
 */
package com.openexchange.configuration;

import com.openexchange.tools.conf.AbstractConfig;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AJAXConfig extends AbstractConfig {

    private static final TestConfig.Property KEY = TestConfig.Property.AJAX_PROPS;

    private static AJAXConfig singleton;

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws ConfigurationException {
        final String fileName = TestConfig.getProperty(KEY);
        if (null == fileName) {
            throw new ConfigurationException(ConfigurationException.Code
                .PROPERTY_MISSING, KEY.getPropertyName());
        }
        return fileName;
    }

    /**
     * Reads the configuration.
     * @throws ConfigurationException if reading configuration fails.
     */
    public static void init() throws ConfigurationException {
        TestConfig.init();
        if (null == singleton) {
            singleton = new AJAXConfig();
            singleton.loadPropertiesInternal();
        }
    }

    public static String getProperty(final Property key) {
        return singleton.getPropertyInternal(key.getPropertyName());
    }

    /**
     * Enumeration of all properties in the ajax.properties file.
     */
    public static enum Property {
        /**
         * Server host.
         */
        HOSTNAME("hostname"),
        /**
         * User login.
         */
        LOGIN("login"),
        /**
         * User password.
         */
        PASSWORD("password"),
        /**
         * http or https.
         */
        PROTOCOL("protocol"),
        /**
         * Second user login.
         */
        SECONDUSER("seconduser");

        /**
         * Name of the property in the ajax.properties file.
         */
        private String propertyName;

        /**
         * Default constructor.
         * @param propertyName Name of the property in the ajax.properties
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
