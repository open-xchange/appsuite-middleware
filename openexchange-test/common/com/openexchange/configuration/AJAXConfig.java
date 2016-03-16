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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.configuration;

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
                    singleton.loadPropertiesInternal();
                }
            }
        }
    }

    public static String getProperty(final Property key) {
        return singleton.getPropertyInternal(key.getPropertyName());
    }

    public static String getProperty(final Property key, final String fallBack) {
        String property;
        try {
            property = getProperty(key);
        } catch (final Exception e) {
            return fallBack;
        }
        return property;
    }

    /**
     * Enumeration of all properties in the ajax.properties file.
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
        RMI_HOST("rmihost"),
        /** Executor sleeps this amount of time after every request to prevent Apache problems */
        SLEEP("sleep"),
        /**
         * User login.
         */
        LOGIN("login"),
        /**
         * User password.
         */
        PASSWORD("password"),
        /**
         * Second user login.
         */
        SECONDUSER("seconduser"),
        /**
         * Third user login.
         */
        THIRDLOGIN("thirdlogin"),
        /**
         * Fourth user login.
         */
        FOURTHLOGIN("fourthlogin"),
        /**
         * OXAdmin login.
         */
        OXADMIN("oxadmin"),
        /**
         * Whether SP3 or SP4 data
         */
        IS_SP3("isSP3"),
        /**
         * Context name.
         */
        CONTEXTNAME("contextName"),

        /**
         * USER PARTICIPANTS
         */
        USER_PARTICIPANT1("user_participant1"),
        USER_PARTICIPANT2("user_participant2"),
        USER_PARTICIPANT3("user_participant3"),
        /**
         * OXADMINMASTER
         */
        OX_ADMIN_MASTER("oxadminmaster"),
        OX_ADMIN_MASTER_PWD("oxadminmaster_password"),
        /**
         * Resource Participants
         */
        RESOURCE_PARTICIPANT1("resource_participant1"),
        RESOURCE_PARTICIPANT2("resource_participant2"),
        RESOURCE_PARTICIPANT3("resource_participant3"),
        /**
         * Group Participant
         */
        GROUP_PARTICIPANT("group_participant"),

        /**
         * Echo header; see property "com.openexchange.servlet.echoHeaderName" in file 'server.properties'
         */
        ECHO_HEADER("echo_header");
        ;

        /**
         * Name of the property in the ajax.properties file.
         */
        private final String propertyName;

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
