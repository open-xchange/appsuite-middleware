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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * Configuration for the generic implementation of the config jump interface.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GenericImplConfig extends AbstractConfig {

    /**
     * Lock for initialization.
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * Singleton instance.
     */
    private static final GenericImplConfig SINGLETON = new GenericImplConfig();
    
    /**
     * Prevent instantiation
     */
    private GenericImplConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws ConfigurationException {
        return "configjump.properties";
    }

    /**
     * Gets the value of a property from the file.
     * @param key name of the property.
     * @return the value of the property.
     */
    public static String getProperty(final Property key) {
        return SINGLETON.getPropertyInternal(key.propertyName, key.defaultValue);
    }

    /**
     * Loads the configjump.properties.
     * @throws ConfigurationException if loading fails.
     */
    public static void init() throws ConfigurationException {
        LOCK.lock();
        try {
            if (!SINGLETON.isPropertiesLoadInternal()) {
                reinit();
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Reloads the configjump.properties.
     * @throws ConfigurationException if loading fails.
     */
    public static void reinit() throws ConfigurationException {
        SINGLETON.loadPropertiesInternal();
    }

    /**
     * All properties of the configjump properties file.
     */
    public enum Property {
        /**
         * URL to external configuration system.
         */
        URL("URL", null);

        /**
         * Name of the property in the configjump.properties file.
         */
        private String propertyName;

        /**
         * Default value of the property.
         */
        private String defaultValue;

        /**
         * Default constructor.
         * @param keyName Name of the property in the configjump.properties
         * file.
         * @param value Default value of the property.
         */
        private Property(final String keyName, final String value) {
            this.propertyName = keyName;
            this.defaultValue = value;
        }
    }
}
