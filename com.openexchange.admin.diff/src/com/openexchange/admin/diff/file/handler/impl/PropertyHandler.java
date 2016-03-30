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

package com.openexchange.admin.diff.file.handler.impl;

import java.io.IOException;
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
                originalProperty.load(new StringReader(originalFileContent));

                final String fileName = origFile.getName();
                List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

                if (result.isEmpty()) {
                    // Missing in installation, but already tracked in file diff
                    continue;
                }

                Properties installedProperty = new Properties();
                installedProperty.load(new StringReader(result.get(0).getContent()));

                getDiffProperties(diffResult, fileName, originalProperty, installedProperty);
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error while property diff per file: " + e.getLocalizedMessage() + "\n");
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
            propertyValue = OBSCURED_PROPERTY;
        } else if (criticalProperties.contains(key)) {
            propertyValue = OBSCURED_PROPERTY;
        }
        return propertyValue;
    }
}
