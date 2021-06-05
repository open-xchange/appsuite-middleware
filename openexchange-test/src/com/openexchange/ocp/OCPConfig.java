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

package com.openexchange.ocp;

import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.TestConfig;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * {@link OCPConfig}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
@SuppressWarnings("deprecation")
public class OCPConfig extends AbstractConfig {

    private static final TestConfig.Property KEY = TestConfig.Property.OCP_PROPS;

    private static volatile OCPConfig INSTANCE;

    /**
     * Initializes a new {@link OCPConfig}.
     */
    public OCPConfig() {
        super();
    }

    /**
     * Initialises the OCP configuration
     *
     * @throws OXException if reading the configuration fails
     */
    public static void init() throws OXException {
        TestConfig.init();
        if (null != INSTANCE) {
            return;
        }
        synchronized (AJAXConfig.class) {
            if (null != INSTANCE) {
                return;
            }
            INSTANCE = new OCPConfig();
            INSTANCE.loadPropertiesInternal();
        }
    }

    public static String getProperty(Property key) {
        return INSTANCE.getPropertyInternal(key.getPropertyName());
    }

    public static String getProperty(Property key, String fallBack) {
        return INSTANCE.getPropertyInternal(key.getPropertyName(), fallBack);
    }

    @Override
    protected String getPropertyFileName() throws OXException {
        String fileName = TestConfig.getProperty(KEY);
        if (null == fileName) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(KEY.getPropertyName());
        }
        return fileName;
    }

    public static enum Property {

        HOSTNAME("appsuite.server"),
        PROTOCOL("appsuite.protocol"),
        LOGIN("appsuite.login"),
        PASSWORD("appsuite.password"),
        BRAND("appsuite.brand"),
        BRAND_ADMIN_PASSWORD("appsuite.brand.admin.password"),
        SUB_BRAND("appsuite.sub_brand"),
        SUB_BRAND_ADMIN_PASSWORD("appsuite.sub_brand.admin.password"),
        DATABASE_URL("database.url"),
        DATABASE_USERNAME("database.username"),
        DATABASE_PASSWORD("database.password"),
        DATABASE_LOGIN_TABLE("database.login.table"),
        DATABASE_USER_DELTA_TABLE("database.user_delta.table"),
        ;

        /**
         * Name of the property in the ocp.properties file.
         */
        private final String propertyName;

        /**
         * Default constructor.
         * 
         * @param propertyName Name of the property in the ocp.properties file.
         */
        private Property(final String propertyName) {
            this.propertyName = propertyName;
        }

        /**
         * The property name
         * 
         * @return the propertyName
         */
        public String getPropertyName() {
            return propertyName;
        }
    }
}
