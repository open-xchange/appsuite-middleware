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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.diff.util.CaseInsensitiveSorter;

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
     * Includes configuration files missing in the installation<br>
     * <br>
     * TODO change to data object (that must be defined) list to get to know where the file was found in the original installation
     */
    private List<String> missingFiles = new ArrayList<String>();

    /**
     * Includes configuration files that are duplicate within the installation
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
        sortMaps();

        StringBuilder builder = new StringBuilder();

        builder.append("---------- PROCESSING ERRORS WHILE EXECUTION: " + processingErrors.size() + " ---------- \n");
        for (String processingError : processingErrors) {
            builder.append(processingError + "\n");
        }

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
        }

        builder.append("---------- Additional properties: " + additionalProperties.size() + " ---------- \n");
        for (Entry<String, PropertyDiffResultSet> additionalProperty : additionalProperties.entrySet()) {
            PropertyDiffResultSet propertyAdded = additionalProperty.getValue();
            builder.append(propertyAdded.getFileName() + ": " + propertyAdded.getPropertyNameAndValue() + "\n");
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
            builder.append(propertyChanged.getFileName() + ": " + propertyChanged.getPropertyNameAndValue() + "\n");
        }

        return builder.toString();
    }

    /**
     * Sort all maps based on keys for output
     */
    private void sortMaps() {
        Map<String, String> sortedAdditionalFiles = sortByKeys(this.additionalFiles);
        this.additionalFiles = sortedAdditionalFiles;

        Map<String, PropertyDiffResultSet> sortedAdditionalProperties = sortByKeys(this.additionalProperties);
        this.additionalProperties = sortedAdditionalProperties;

        Map<String, PropertyDiffResultSet> sortedChangedProperties = sortByKeys(this.changedProperties);
        this.changedProperties = sortedChangedProperties;

        Collections.sort(this.duplicateFiles, new CaseInsensitiveSorter());

        Map<String, String> sortedDuplicateProperties = sortByKeys(this.duplicateProperties);
        this.duplicateProperties = sortedDuplicateProperties;

        Collections.sort(this.missingFiles, new CaseInsensitiveSorter());

        Map<String, String> sortedMissingProperties = sortByKeys(this.missingProperties);
        this.missingProperties = sortedMissingProperties;

        Collections.sort(this.nonConfigurationFiles, new CaseInsensitiveSorter());
        Collections.sort(this.processingErrors, new CaseInsensitiveSorter());
    }

    /**
     * Sorts the given Map based on the keys
     * 
     * @param map to sort
     * @return Sorted map
     */
    private static <K extends Comparable<?>, V extends Comparable<?>> Map<K, V> sortByKeys(Map<K, V> map) {
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.sort(keys, (Comparator) new CaseInsensitiveSorter());

        // LinkedHashMap will keep the keys in the order they are inserted
        // which is currently sorted on natural ordering
        Map<K, V> sortedMap = new LinkedHashMap<K, V>();
        for (K key : keys) {
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }
}
