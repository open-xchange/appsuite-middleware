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

package com.openexchange.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.openexchange.exception.OXException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimConfigurationService implements ConfigurationService {

    public final Map<String, String> stringProperties = new HashMap<String, String>();
    public final File configurationDirectory;

    public SimConfigurationService() {
        this("");
    }

    public SimConfigurationService(String dirName) {
        super();
        configurationDirectory = new File(dirName);
    }

    @Override
    public Filter getFilterFromProperty(String name) {
        // Nothing to do
        return null;
    }

    @Override
    public Map<String, String> getProperties(PropertyFilter filter) throws OXException {
        return Collections.emptyMap();
    }

    @Override
    public boolean getBoolProperty(final String name, final boolean defaultValue) {
        final String prop = stringProperties.get(name);
        if (null != prop) {
            return Boolean.parseBoolean(prop.trim());
        }
        return defaultValue;
    }

    @Override
    public Properties getFile(final String fileName) {
        // Nothing to do
        return null;
    }

    @Override
    public int getIntProperty(final String name, final int defaultValue) {
        final String prop = stringProperties.get(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop.trim());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return defaultValue;
    }

    @Override
    public Properties getPropertiesInFolder(final String folderName) {
        // Nothing to do
        return null;
    }

    @Override
    public String getProperty(final String name) {
        return stringProperties.get(name);
    }

    @Override
    public String getProperty(final String name, final String defaultValue) {
        return stringProperties.containsKey(name) ? stringProperties.get(name) : defaultValue;
    }

    @Override
    public Iterator<String> propertyNames() {
        return stringProperties.keySet().iterator();
    }

    @Override
    public int size() {
        // Nothing to do
        return 0;
    }

    @Override
    public File getFileByName(String fileName) {
        // Nothing to do
        return null;
    }

    @Override
    public File getDirectory(String directoryName) {
        return new File(configurationDirectory, directoryName);
    }

    @Override
    public String getText(final String fileName) {
        // Nothing to do
        return null;
    }

    @Override
    public Object getYaml(String filename) {
        // Nothing to do
        return null;
    }

    @Override
    public Map<String, Object> getYamlInFolder(String dirName) {
        // Nothing to do
        return null;
    }

    @Override
    public List<String> getProperty(String name, String defaultValue, String separator) {
        return Collections.emptyList();
    }

}
