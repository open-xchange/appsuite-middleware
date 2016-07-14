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

package com.openexchange.groupware.configuration;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;

/**
 * This class handles the configuration parameters read from the configuration
 * property file participant.properties. This are especially options for
 * participants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ParticipantConfig {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ParticipantConfig.class);

    /**
     * Singleton instance.
     */
    private static final ParticipantConfig SINGLETON = new ParticipantConfig();

    /**
     * Name of the properties file.
     */
    private static final String FILENAME = "participant.properties";

    /**
     * Field to store properties.
     */
    private final Properties props = new Properties();

    /**
     * Prevent instantiation.
     */
    private ParticipantConfig() {
        super();
    }

    /**
     * Gets the value of a property from the file.
     * @param key name of the property.
     * @return the value of the property.
     */
    public Boolean getProperty(final Property key) {
        logNotInitialized();
        return Boolean.valueOf(props.getProperty(key.name, key.value));
    }

    public Integer getIntProperty(final Property key) {
        logNotInitialized();
        return Integer.valueOf(props.getProperty(key.name, key.value));
    }

    /**
     * All properties of the participant properties file.
     */
    public enum Property {
        /**
         * Determines if external participants without email address are shown.
         */
        SHOW_WITHOUT_EMAIL("com.openexchange.participant.ShowWithoutEmail",
            Boolean.TRUE.toString()),
        /**
         * Determines if a search for all users, groups and resources is
         * triggered on opened participant dialog.
         */
        AUTO_SEARCH("com.openexchange.participant.autoSearch",
            Boolean.TRUE.toString());

        /**
         * Name of the property in the participant.properties file.
         */
        private String name;

        /**
         * Default value of the property.
         */
        private String value;

        /**
         * Default constructor.
         * @param keyName Name of the property in the participant.properties
         * file.
         * @param value Default value of the property.
         */
        private Property(final String keyName, final String value) {
            this.name = keyName;
            this.value = value;
        }
    }

    /**
     * @return the singleton instance.
     */
    public static ParticipantConfig getInstance() {
        return SINGLETON;
    }

    /**
     * @param configuration the configuration service.
     */
    public void initialize(final ConfigurationService configuration) {
        final Properties props = configuration.getFile(FILENAME);
        if (null == props) {
            LOG.info("Configuration file {} is missing. Using defaults.", FILENAME);
        } else {
            this.props.clear();
            this.props.putAll(props);
            LOG.info("Read configuration file {}.", FILENAME);
        }
    }

    private void logNotInitialized() {
        if (props.isEmpty()) {
            LOG.info("Configuration file {} not read. Using defaults.", FILENAME);
        }
    }
}
