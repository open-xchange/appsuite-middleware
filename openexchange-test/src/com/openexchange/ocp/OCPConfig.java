/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ocp;

import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.TestConfig;
import com.openexchange.exception.OXException;
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
