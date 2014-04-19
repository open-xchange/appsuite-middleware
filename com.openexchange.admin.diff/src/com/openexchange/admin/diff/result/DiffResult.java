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

package com.openexchange.admin.diff.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Presents the results of diff processing
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class DiffResult {

    /**
     * Includes all stacktraces occurred during processing diffs
     */
    private List<String> processingErrors = new ArrayList<String>();

    /**
     * Includes configuration files missing in the installation
     */
    private List<String> missingFiles = new ArrayList<String>();

    /**
     * Includes configuration files that are duplicate within the installation TODO change to multimap to get info about duplicate files
     */
    private List<String> duplicateFiles = new ArrayList<String>();

    /**
     * Includes non configuration files within the installation
     */
    private List<String> nonConfigurationFiles = new ArrayList<String>();

    /**
     * Includes additional configuration files and its content
     */
    private Map<String, String> additionalFiles = new HashMap<String, String>();

    /**
     * Includes additional properties and its files
     */
    private Map<String, PropertyDiffResultSet> additionalProperties = new HashMap<String, PropertyDiffResultSet>();

    /**
     * Includes properties that are missing in a configuration file
     */
    private Map<String, String> missingProperties = new HashMap<String, String>();

    /**
     * Includes properties that are duplicate
     */
    private Map<String, String> duplicateProperties = new HashMap<String, String>();

    /**
     * Includes properties/configurations that have been changed
     */
    private Map<String, PropertyDiffResultSet> changedProperties = new HashMap<String, PropertyDiffResultSet>();

    /**
     * Gets the processingErrors
     * 
     * @return The processingErrors
     */
    public List<String> getProcessingErrors() {
        return processingErrors;
    }

    /**
     * Gets the missingFiles
     * 
     * @return The missingFiles
     */
    public List<String> getMissingFiles() {
        return missingFiles;
    }

    /**
     * Gets the duplicateFiles
     * 
     * @return The duplicateFiles
     */
    public List<String> getDuplicateFiles() {
        return duplicateFiles;
    }

    /**
     * Gets the nonConfigurationFiles
     * 
     * @return The nonConfigurationFiles
     */
    public List<String> getNonConfigurationFiles() {
        return nonConfigurationFiles;
    }

    /**
     * Gets the additionalFiles
     * 
     * @return The additionalFiles
     */
    public Map<String, String> getAdditionalFiles() {
        return additionalFiles;
    }

    /**
     * Gets the additionalProperties
     * 
     * @return The additionalProperties
     */
    public Map<String, PropertyDiffResultSet> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Gets the missingProperties
     * 
     * @return The missingProperties
     */
    public Map<String, String> getMissingProperties() {
        return missingProperties;
    }


    /**
     * Gets the duplicateProperties
     * 
     * @return The duplicateProperties
     */
    public Map<String, String> getDuplicateProperties() {
        return duplicateProperties;
    }

    /**
     * Gets the changedProperties
     * 
     * @return The changedProperties
     */
    public Map<String, PropertyDiffResultSet> getChangedProperties() {
        return changedProperties;
    }

    /**
     * Returns the current state of the diff processing.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("---------- Missing configuration files: " + missingFiles.size() + " ---------- \n");
        for (String missingFile : missingFiles) {
            builder.append(missingFile + "\n");
        }

        builder.append("---------- Missing properties: " + missingProperties.size() + " ---------- \n");
        for (Entry<String, String> missingProperty : missingProperties.entrySet()) {
            builder.append(missingProperty.getKey() + ": " + missingProperty.getValue() + "\n");
        }

        builder.append("---------- Additional configuration files: " + additionalFiles.size() + " ---------- \n");
        for (Entry<String, String> additionalFile : additionalFiles.entrySet()) {
            builder.append(additionalFile.getKey() + "\n");
            // TODO you may add the value of the additional files with additionalField.getValue()
        }

        builder.append("---------- Additional properties: " + additionalProperties.size() + " ---------- \n");
        for (Entry<String, PropertyDiffResultSet> additionalProperty : additionalProperties.entrySet()) {
            PropertyDiffResultSet propertyAdded = additionalProperty.getValue();
            builder.append(propertyAdded.getFileName() + ": " + propertyAdded.getProperty() + "=" + propertyAdded.getValue() + "\n");
        }

        builder.append("---------- Duplicate configuration files: " + duplicateFiles.size() + " ---------- \n");
        for (String duplicateFile : duplicateFiles) {
            builder.append(duplicateFile + "\n");
        }

        builder.append("---------- Duplicate properties: " + duplicateProperties.size() + " ---------- \n");
        for (Entry<String, String> duplicateProperty : duplicateProperties.entrySet()) {
            builder.append(duplicateProperty.getKey() + ": " + duplicateProperty.getValue() + "\n");
        }

        builder.append("---------- Non configuration files: " + nonConfigurationFiles.size() + " ---------- \n");
        for (String nonConfigurationFile : nonConfigurationFiles) {
            builder.append(nonConfigurationFile + "\n");
        }

        builder.append("---------- Changed properties: " + changedProperties.size() + " ---------- \n");
        for (Entry<String, PropertyDiffResultSet> changedProperty : changedProperties.entrySet()) {
            PropertyDiffResultSet propertyChanged = changedProperty.getValue();
            builder.append(propertyChanged.getFileName() + ": " + propertyChanged.getProperty() + "=" + propertyChanged.getValue() + "\n");
        }

        return builder.toString();
    }
}
