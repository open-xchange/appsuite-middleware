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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.configuration;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * This class handles the configuration parameters read from the configuration
 * property file participant.properties. This are especially options for
 * participants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ParticipantConfig extends AbstractConfig {

    /**
     * Property key in the system.properties file.
     */
    private static final SystemConfig.Property KEY = SystemConfig.Property
        .PARTICIPANT;

    /**
     * Singleton instance.
     */
    private static final ParticipantConfig SINGLETON = new ParticipantConfig();

    /**
     * Prevent instantiation
     */
    private ParticipantConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws ConfigurationException {
        final String filename = SystemConfig.getProperty(KEY);
        if (null == filename) {
            throw new ConfigurationException(Code.PROPERTY_MISSING,
                KEY.getPropertyName());
        }
        return filename;
    }

    /**
     * Loads the participants.properties file.
     * @throws ConfigurationException if loading fails.
     */
    public static void init() throws ConfigurationException {
        SINGLETON.loadPropertiesInternal();
    }

    /**
     * Gets the value of a property from the file.
     * @param key name of the property.
     * @return the value of the property.
     */
    public static boolean getProperty(final Property key) {
        return SINGLETON.getBooleanInternal(key.propertyName, key.defaultValue);
    }

    /**
     * All properties of the participant properties file.
     */
    public enum Property {
        /**
         * Determines if external participants without email address are shown.
         */
        SHOW_WITHOUT_EMAIL("ShowWithoutEmail", Boolean.TRUE.toString());

        /**
         * Name of the property in the participant.properties file.
         */
        private String propertyName;

        /**
         * Default value of the property.
         */
        private String defaultValue;

        /**
         * Default constructor.
         * @param keyName Name of the property in the participant.properties
         * file.
         * @param value Default value of the property.
         */
        private Property(final String keyName, final String value) {
            this.propertyName = keyName;
            this.defaultValue = value;
        }
    }
}
