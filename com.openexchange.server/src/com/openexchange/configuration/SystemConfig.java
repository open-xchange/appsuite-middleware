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

import java.io.File;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * This class handles the configuration parameters of the system.properties
 * configuration file.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SystemConfig extends AbstractConfig implements Initialization, Reloadable {

    /**
     * Singleton instance.
     */
    private static final SystemConfig singleton = new SystemConfig();

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SystemConfig.class);

    /**
     * Key of the system property that contains the file name of the
     * system.properties configuration file.
     */
    private static final String KEY = "openexchange.propdir";

    /**
     * Returns the value of the property with the specified key. This method
     * returns <code>null</code> if the property is not found.
     *
     * @param key
     *            the property key.
     * @return the value of the property or <code>null</code> if the property
     *         is not found.
     */
    public static String getProperty(final String key) {
        return getProperty(key, null);
    }

    /**
     * Returns the value of the property with the specified key. This method
     * returns the default value argument if the property is not found.
     *
     * @param key
     *            the property key.
     * @param def
     *            a default value.
     * @return the value of the property or the default value argument if the
     *         property is not found.
     */
    public static String getProperty(final String key, final String def) {
        return singleton.getPropertyInternal(key, def);
    }

    /**
     * @param property
     *            wanted property.
     * @return the value of the property.
     */
    public static String getProperty(final Property property) {
        return getProperty(property.propertyName);
    }

    /**
     * @return if system.properties must be loaded.
     */
    public static boolean isPropertiesLoad() {
        return null != singleton && singleton.isPropertiesLoadInternal();
    }

    /**
     * @return the singleton instance.
     */
    public static SystemConfig getInstance() {
        return singleton;
    }

    /**
     * Prevent instantiation.
     */
    private SystemConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadConfiguration(final ConfigurationService configService) {
        try {
            stop();
            start();
        } catch (final OXException e) {
            LOG.warn("Could not reload system configuration.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws OXException {
        if (isPropertiesLoadInternal()) {
            LOG.error("Duplicate initialization of SystemConfig.");
            return;
        }
        loadPropertiesInternal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        clearProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        final String directory = System.getProperty(KEY);
        if (null == directory) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(KEY);
        }
        return directory + File.separator + "system.properties";
    }

    /**
     * Enumeration of all properties in the system.properties file.
     */
    public static enum Property {
        /**
         * Properties file for LDAP.
         */
        LDAP("LDAP"),
        /**
         * Defines if caching should be used.
         */
        CACHE("Cache"),
        /**
         * UserConfigurationStorage.
         */
        USER_CONF_STORAGE("UserConfigurationStorage"),
        /**
         * Spring XML file with overrides for WebDAV.
         */
        WebdavOverrides("WEBDAVOVERRIDES"),
        /**
         * Mime type file name; <code>mime.types</code>
         */
        MimeTypeFileName("MimeTypeFileName");

        /**
         * Name of the property in the server.properties file.
         */
        private String propertyName;

        /**
         * Default constructor.
         *
         * @param propertyName
         *            Name of the property in the server.properties file.
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

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("system.properties");
    }
}
