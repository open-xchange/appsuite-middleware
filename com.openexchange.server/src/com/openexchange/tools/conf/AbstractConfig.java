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

package com.openexchange.tools.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * Class holding configuration options loaded from property files can extend this class to inherit usefull methods.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @deprecated use {@link ConfigurationService}.
 */
@Deprecated
public abstract class AbstractConfig {

    /**
     * Stores the configuration parameters of the configuration file.
     */
    private Properties props;

    /**
     * Default constructor.
     */
    protected AbstractConfig() {
        super();
    }

    /**
     * Checks if the properties contain a given key.
     *
     * @param key key to check for existance.
     * @return <code>true</code> only if the properties are loaded and the key exists.
     */
    protected final boolean containsPropertyInternal(final String key) {
        return props == null ? false : props.containsKey(key);
    }

    /**
     * Returns the value of the property with the specified key. This method returns <code>null</code> if the property is not found.
     *
     * @param key the property key.
     * @return the value of the property or <code>null</code> if the property is not found.
     */
    protected final String getPropertyInternal(final String key) {
        return getPropertyInternal(key, null);
    }

    /**
     * Returns the value of the property with the specified key. This method returns the def argument if the property is not defined.
     *
     * @param key the property name.
     * @param def default value if the property is not defined.
     * @return the property value or the default value if the property is not defined.
     */
    protected final String getPropertyInternal(final String key, final String def) {
        return props == null ? null : props.getProperty(key, def);
    }

    /**
     * Returns <code>true</code> if and only if the property named by the argument exists and is equal to the string <code>"true"</code>.
     * The test of this string is case insensitive.
     * <p>
     * If there is no property with the specified name, or if the specified name is empty or null, then <code>false</code> is returned.
     *
     * @param key the property name.
     * @return the <code>boolean</code> value of the property.
     */
    protected final boolean getBooleanInternal(final String key) {
        return getBooleanInternal(key, null);
    }

    /**
     * Returns the boolean value of the property. If the propery isn't set the def arguments is returned.
     *
     * @param key the property name.
     * @param def default value to return if the property isn't set.
     * @return the boolean value of the property.
     */
    protected final boolean getBooleanInternal(final String key, final boolean def) {
        return getBooleanInternal(key, Boolean.valueOf(def).toString());
    }

    /**
     * Returns the boolean value of the property. If the propery isn't set the def arguments is returned.
     *
     * @param key the property name.
     * @param def default value to return if the property isn't set.
     * @return the boolean value of the property.
     */
    protected final boolean getBooleanInternal(final String key, final String def) {
        String value = getPropertyInternal(key);
        if (null == value) {
            value = def;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * @return an iterator of the properties keys.
     */
    protected final Iterator<String> keyIterator() {
        final Iterator<Object> iter = props.keySet().iterator();
        return new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public String next() {
                return (String) iter.next();
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }

    /**
     * @return if system.properties must be loaded.
     */
    protected final boolean isPropertiesLoadInternal() {
        return (props != null);
    }

    /**
     * @return the name of the property file.
     * @throws ConfigurationException if determining the filename of the property file fails.
     */
    protected abstract String getPropertyFileName() throws OXException;

    /**
     * Loads the properties file by using the JVM system property defining the path to the system.properties configuration file.
     */
    protected final void loadPropertiesInternal() throws OXException {
        loadPropertiesInternal(getPropertyFileName());
    }

    /**
     * Loads the system.properties configuration file from the specified file.
     *
     * @param propFileName name of the file containing the system.properties.
     */
    protected final void loadPropertiesInternal(final String propFileName) throws OXException {
        if (null == propFileName) {
            throw ConfigurationExceptionCodes.NO_FILENAME.create();
        }
        final File propFile = new File(propFileName);
        if (!propFile.exists()) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsoluteFile());
        }
        if (!propFile.canRead()) {
            throw ConfigurationExceptionCodes.NOT_READABLE.create(propFile.getAbsoluteFile());
        }
        loadProperties(propFile);
    }

    /**
     * Loads the system.properties configuration file from the specified file.
     *
     * @param propFile file containing the system.properties.
     */
    protected final void loadProperties(final File propFile) throws OXException {
        props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propFile);
            props.load(fis);
        } catch (final FileNotFoundException e) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsolutePath(), e);
        } catch (final IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(propFile.getAbsolutePath(), e);
        } finally {
            Streams.close(fis);
        }
    }

    /**
     * Clears the properties.
     */
    protected final void clearProperties() {
        props = null;
    }
}
