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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.diff.file.type.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.PropertyDiffResultSet;

/**
 * Handler for .properties configuration files
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class PropertyHandler extends AbstractFileHandler {

    private volatile static PropertyHandler instance;

    private List<String> criticalProperties = new ArrayList<String>(Arrays.asList(new String[] {
        "com.openexchange.oauth.yahoo.apiKey",
        "com.openexchange.oauth.xing.consumerKey",
        "com.openexchange.socialplugin.linkedin.apikey",
        "com.openexchange.facebook.secretKey",
        "com.openexchange.oauth.xing.consumerSecret",
        "com.openexchange.oauth.xing.apiKey",
        "com.openexchange.oauth.yahoo.apiSecret",
        "com.openexchange.oauth.xing.apiSecret",
        "com.openexchange.socialplugin.linkedin.apisecret",
        "com.openexchange.facebook.apiKey",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    }));

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
    public DiffResult getDiff(DiffResult diffResult, Map<String, String> lOriginalFiles, Map<String, String> lInstalledFiles) {
        getPropertyDiffsPerFile(diffResult, new HashMap<String, String>(lOriginalFiles), new HashMap<String, String>(lInstalledFiles));

        return diffResult;
    }

    private void getPropertyDiffsPerFile(DiffResult diffResult, HashMap<String, String> lOriginalFiles, HashMap<String, String> lInstalledFiles) {
        Iterator<Entry<String, String>> it = lOriginalFiles.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, String> pairs = it.next();
            try {
                String originalFileContent = pairs.getValue();
                Properties originalProperty = new Properties();
                originalProperty.load(new StringReader(originalFileContent));

                String installedFileContent = lInstalledFiles.get(pairs.getKey());
                if (installedFileContent == null) {
                    // File not available in installation. See PropertyHandler.getFileDiffs(DiffResult, HashSet<String>, HashSet<String>)
                    continue;
                }
                Properties installedProperty = new Properties();
                installedProperty.load(new StringReader(installedFileContent));

                getDiffProperties(diffResult, pairs.getKey(), originalProperty, installedProperty);
            }catch (IOException e) {
                diffResult.getProcessingErrors().add(e.getLocalizedMessage());
            }
        }
    }

    private DiffResult getDiffProperties(DiffResult diffResult, String fileName, final Properties originalProperties, final Properties installedProperties) {

        for (String key : originalProperties.stringPropertyNames()) {
            String originalPropertyValue = originalProperties.getProperty(key);

            if (installedProperties.getProperty(key) == null) {
                diffResult.getMissingProperties().put(fileName, key);
                originalProperties.remove(key);
                continue;
            }

            String installedPropertyValue = installedProperties.getProperty(key);
            if (!originalPropertyValue.equalsIgnoreCase(installedPropertyValue)) {
                diffResult.getChangedProperties().put(key, new PropertyDiffResultSet(fileName, key, obscure(key, installedPropertyValue)));
            } else if (installedPropertyValue == null) {
                diffResult.getMissingProperties().put(fileName, key);
            }

            installedProperties.remove(key);
            originalProperties.remove(key);
        }

        if (!installedProperties.isEmpty()) {
            for (String key : installedProperties.stringPropertyNames()) {
                diffResult.getAdditionalProperties().put(fileName, new PropertyDiffResultSet(fileName, key, installedProperties.getProperty(key)));
            }
        }
        return diffResult;
    }

    /**
     * Obscures the given string if it is a key, password or an other critical value.
     * 
     * @param key - the key of the property to verify
     * @param propertyValue - the property to obscure
     * @return - the given string if not included in the criticalProperties List or ******** if it is critical
     */
    private String obscure(String key, String propertyValue) {
        if (criticalProperties.contains(key)) {
            propertyValue = "********";
        }
        return propertyValue;
    }
}
