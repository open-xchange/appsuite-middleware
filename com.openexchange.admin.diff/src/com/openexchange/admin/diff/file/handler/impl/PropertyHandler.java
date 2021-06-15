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

package com.openexchange.admin.diff.file.handler.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.domain.PropertyDiff;
import com.openexchange.admin.diff.util.ConfigurationFileSearch;

/**
 * Handler for .properties configuration files
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class PropertyHandler extends AbstractFileHandler {

    private static final String KEY_REGEX = ".*consumerKey.*|.*apiKey.*|.*secretKey.*|.*consumerSecret.*|.*apiSecret.*|.*password.*|.*login$";

    private volatile static PropertyHandler instance;

    private final List<String> criticalProperties = new ArrayList<String>(Arrays.asList(new String[] {
        // Fill in some special property names which do
        // not match '.*consumerKey.*|.*apiKey.*|.*secretKey.*|.*consumerSecret.*|.*apiSecret.*'
        "writeProperty.2", "readProperty.2",
        "",
        "",
        "",
        "",
        ""
    }));

    protected final static String OBSCURED_PROPERTY = "********";

    private PropertyHandler() {
        ConfigDiff.register(this);
    }

    public static synchronized PropertyHandler getInstance() {
        if (instance == null) {
            synchronized (PropertyHandler.class) {
                if (instance == null) {
                    instance = new PropertyHandler();
                }
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        getPropertyDiffsPerFile(diffResult, Collections.synchronizedList(new ArrayList<ConfigurationFile>(lOriginalFiles)), Collections.synchronizedList(new ArrayList<ConfigurationFile>(lInstalledFiles)));

        return diffResult;
    }

    /**
     * Diff the properties from the given files and adds the differences to the provided DiffResult
     *
     * @param diffResult - the object that will be aerated with the results
     * @param lOriginalFiles - original files to diff
     * @param lInstalledFiles - installed files to diff
     */
    protected void getPropertyDiffsPerFile(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {

        for (ConfigurationFile origFile : lOriginalFiles) {
            try {
                String originalFileContent = origFile.getContent();
                Properties originalProperty = new Properties();
                loadPropertiesFrom(new StringReader(originalFileContent), originalProperty);

                final String fileName = origFile.getName();
                List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

                if (result.isEmpty()) {
                    // Missing in installation, but already tracked in file diff
                    continue;
                }

                Properties installedProperty = new Properties();
                loadPropertiesFrom(new StringReader(result.get(0).getContent()), installedProperty);

                getDiffProperties(diffResult, fileName, originalProperty, installedProperty);
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error while property diff per file: " + e.getLocalizedMessage() + "\n");
            }
        }
    }

    private void loadPropertiesFrom(Reader reader, Properties originalProperty) throws IOException {
        if (reader != null) {
            try {
                originalProperty.load(reader);
            } finally {
                reader.close();
            }
        }
    }

    /**
     * Diffs the given properties of one file and adds the diff to the given DiffResult.
     *
     * @param diffResult - the object that will be aerated with the results
     * @param fileName - name of the file the property is included in
     * @param originalProperties - properties of the original file
     * @param installedProperties - properties of the installed file
     */
    protected void getDiffProperties(DiffResult diffResult, String fileName, final Properties originalProperties, final Properties installedProperties) {

        for (String key : originalProperties.stringPropertyNames()) {
            String originalPropertyValue = originalProperties.getProperty(key);

            if (installedProperties.getProperty(key) == null) {
                diffResult.getMissingProperties().add(new PropertyDiff(fileName, key, null));
                originalProperties.remove(key);
                continue;
            }

            String installedPropertyValue = installedProperties.getProperty(key);
            if (!originalPropertyValue.equalsIgnoreCase(installedPropertyValue)) {
                diffResult.getChangedProperties().add(new PropertyDiff(fileName, key, obscure(key, installedPropertyValue)));
            } else if (installedPropertyValue == null) {
                diffResult.getMissingProperties().add(new PropertyDiff(fileName, key, null));
            }

            installedProperties.remove(key);
            originalProperties.remove(key);
        }

        if (!installedProperties.isEmpty()) {
            for (String key : installedProperties.stringPropertyNames()) {
                diffResult.getAdditionalProperties().add(new PropertyDiff(fileName, key, installedProperties.getProperty(key)));
            }
        }
    }

    /**
     * Obscures the given string if it is a key, password or an other critical value.
     *
     * @param key - the key of the property to verify
     * @param propertyValue - the property to obscure
     * @return - the given string if not included in the criticalProperties List or ******** if it is critical
     */
    protected String obscure(String key, String propertyValue) {
        if (key.toLowerCase().matches(KEY_REGEX.toLowerCase())) {
            return OBSCURED_PROPERTY;
        }
        if (criticalProperties.contains(key)) {
            return OBSCURED_PROPERTY;
        }
        return propertyValue;
    }
}
