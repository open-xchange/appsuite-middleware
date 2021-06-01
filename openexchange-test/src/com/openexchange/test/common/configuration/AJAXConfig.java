/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.test.common.configuration;

import java.io.File;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.tools.conf.AbstractConfig;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AJAXConfig extends AbstractConfig {

    private static final TestConfig.Property KEY = TestConfig.Property.AJAX_PROPS;

    private static volatile AJAXConfig singleton;

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        final String fileName = TestConfig.getProperty(KEY);
        if (null == fileName) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(KEY.getPropertyName());
        }
        return fileName;
    }

    /**
     * Reads the configuration.
     * @throws OXException if reading configuration fails.
     */
    public static void init() throws OXException {
        TestConfig.init();
        if (null == singleton) {
            synchronized (AJAXConfig.class) {
                if (null == singleton) {
                    singleton = new AJAXConfig();
                    String propertyFileName = singleton.getPropertyFileName();
                    //Check for custom ajax properties
                    String customPropertyFileName = propertyFileName.replace(".properties", "-custom.properties");
                    final File customPropFile = new File(customPropertyFileName);
                    if (customPropFile.exists() && customPropFile.canRead()) {
                        singleton.loadPropertiesInternal(customPropertyFileName);
                    } else {
                        singleton.loadPropertiesInternal(propertyFileName);
                    }
                }
            }
        }
    }

    public static String getProperty(final Property key) {
        String property = tryToFindEnviromentVariable(key);
        if (property == null) {
            property = singleton.getPropertyInternal(key.getPropertyName());
        }
        return property;
    }

    public static String getProperty(final Property key, final String fallBack) {
        String property;
        try {
            property = tryToFindEnviromentVariable(key);
            if (property == null) {
                property = singleton.getPropertyInternal(key.getPropertyName(), fallBack);
            }
        } catch (Exception e) {
            return fallBack;
        }
        return property;
    }

    private static String tryToFindEnviromentVariable(Property key) {
        String envVar;
        try {
            envVar = System.getenv(key.getEnvVarName());
        } catch (@SuppressWarnings("unused") Exception e) {
            envVar = null;
        }
        return envVar;
    }

    /**
     * Enumeration of all properties in the ajax.properties file.
     * FIXME only required for unittests. do clean up their setup
     */
    public static enum Property {
        /**
         * http or https.
         */
        PROTOCOL("protocol"),
        /**
         * Server host.
         */
        HOSTNAME("hostname"),
        /**
         * The host for RMI calls
         */
        RMIHOST("rmihost"),
        /** Executor sleeps this amount of time after every request to prevent Apache problems */
        SLEEP("sleep"),
        /**
         * Whether SP3 or SP4 data
         */
        IS_SP3("isSP3"),
        /**
         * Echo header; see property "com.openexchange.servlet.echoHeaderName" in file 'server.properties'
         */
        ECHO_HEADER("echo_header"),

        /**
         * Directory which contains test files
         */
        TEST_MAIL_DIR("testMailDir"),

        MAIL_PORT("mailPort"),

        PATH_PREFIX("pathPrefix"),

        /**
         * The token endpoint of an oauth authentication server (e.g. keycloak)
         */
        OAUTH_TOKEN_ENDPOINT("oauthTokenEndpoint"),
        /**
         * The client id configured in the oauth authentication server
         */
        OAUTH_CLIENT_ID("oauthClientID"),
        /**
         * The client secret configured in the oauth authentication server
         */
        OAUTH_CLIENT_PASSWORD("oauthClientPassword"),

        /**
         * Whether newly created contexts should be deleted after usage or not
         */
        DELETE_CONTEXT_AFTER_USE("deleteContextAfterUse"),

        /**
         * The suffix of the context name
         */
        CONTEXT_NAME_SUFFIX("contextNameSuffix"),

        /**
         * Whether to use a random cid range or not
         */
        USE_RANDOM_CID_RANGE("useRandomCidRange"),

        /**
         * Whether to initially provision contexts before starting the first test
         */
        PRE_PROVISION_CONTEXTS("preProvisionContexts"),

        /**
         * The context admin login.
         */
        CONTEXT_ADMIN_USER("contextAdminUser"),

        /**
         * The context admin password.
         */
        CONTEXT_ADMIN_PASSWORD("contextAdminPassword"),

        /**
         * The default password for new users.
         */
        USER_PASSWORD("userPassword"),

        /**
         * A comma separated list with names for the users of a context.
         */
        USER_NAMES("userNames")

        ;

        /**
         * Name of the property in the ajax.properties file.
         */
        private final String propertyName;

        private static final String ENV_VAR_PREFIX = "ajax_properties__%1s";

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

        public String getEnvVarName() {
            return String.format(ENV_VAR_PREFIX, this.name());
        }
    }
}
