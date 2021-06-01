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

package com.openexchange.tools.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;

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
        try {
            props = ConfigurationServices.loadPropertiesFrom(propFile, true);
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(e, propFile.getAbsolutePath());
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(e, propFile.getAbsolutePath());
        }
    }

    /**
     * Clears the properties.
     */
    protected final void clearProperties() {
        props = null;
    }
}
