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

package com.openexchange.admin.diff.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.domain.PropertyDiff;
import com.openexchange.admin.diff.util.ConfigurationFileByNameSorter;
import com.openexchange.admin.diff.util.PropertyDiffByFileNameSorter;

/**
 * Presents the results of diff processing
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DiffResult {

    /**
     * Includes all stacktraces occurred during processing diffs
     */
    private List<String> processingErrors = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Includes configuration files missing in the installation
     */
    private List<ConfigurationFile> missingFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * Includes configuration files that are duplicate within the installation
     */
    private List<ConfigurationFile> duplicateFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * Includes non configuration files within the installation
     */
    private List<ConfigurationFile> nonConfigurationFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * Includes additional configuration files and its content
     */
    private List<ConfigurationFile> additionalFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * Includes additional properties and its files
     */
    private List<PropertyDiff> additionalProperties = Collections.synchronizedList(new ArrayList<PropertyDiff>());

    /**
     * Includes properties that are missing in a configuration file
     */
    private List<PropertyDiff> missingProperties = Collections.synchronizedList(new ArrayList<PropertyDiff>());

    /**
     * Includes properties/configurations that have been changed
     */
    private List<PropertyDiff> changedProperties = Collections.synchronizedList(new ArrayList<PropertyDiff>());

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
    public List<ConfigurationFile> getMissingFiles() {
        return missingFiles;
    }

    /**
     * Gets the duplicateFiles
     *
     * @return The duplicateFiles
     */
    public List<ConfigurationFile> getDuplicateFiles() {
        return duplicateFiles;
    }

    /**
     * Gets the nonConfigurationFiles
     *
     * @return The nonConfigurationFiles
     */
    public List<ConfigurationFile> getNonConfigurationFiles() {
        return nonConfigurationFiles;
    }

    /**
     * Gets the additionalFiles
     *
     * @return The additionalFiles
     */
    public List<ConfigurationFile> getAdditionalFiles() {
        return additionalFiles;
    }

    /**
     * Gets the additionalProperties
     *
     * @return The additionalProperties
     */
    public List<PropertyDiff> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Gets the missingProperties
     *
     * @return The missingProperties
     */
    public List<PropertyDiff> getMissingProperties() {
        return missingProperties;
    }

    /**
     * Gets the changedProperties
     *
     * @return The changedProperties
     */
    public List<PropertyDiff> getChangedProperties() {
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

        builder.append("\n");

        builder.append("---------- Missing configuration files: " + missingFiles.size() + " ---------- \n");
        for (ConfigurationFile missingFile : missingFiles) {
            builder.append(missingFile.toString());
        }

        builder.append("\n");

        builder.append("---------- Missing properties: " + missingProperties.size() + " ---------- \n");
        for (PropertyDiff missingProperty : missingProperties) {
            builder.append(missingProperty.toString());
        }

        builder.append("\n");

        builder.append("---------- Additional configuration files: " + additionalFiles.size() + " ---------- \n");
        for (ConfigurationFile additionalFile : additionalFiles) {
            builder.append(additionalFile.toString());
        }

        builder.append("\n");

        builder.append("---------- Additional properties: " + additionalProperties.size() + " ---------- \n");
        for (PropertyDiff additionalProperty : additionalProperties) {
            builder.append(additionalProperty.toString());
        }

        builder.append("\n");

        builder.append("---------- Duplicate configuration files: " + duplicateFiles.size() + " ---------- \n");
        for (ConfigurationFile duplicateFile : duplicateFiles) {
            builder.append(duplicateFile.toString());
        }

        builder.append("\n");

        builder.append("---------- Non configuration files: " + nonConfigurationFiles.size() + " ---------- \n");
        for (ConfigurationFile nonConfigurationFile : nonConfigurationFiles) {
            builder.append(nonConfigurationFile.toString());
        }

        builder.append("\n");

        builder.append("---------- Changed properties: " + changedProperties.size() + " ---------- \n");
        for (PropertyDiff changedProperty : changedProperties) {
            builder.append(changedProperty.toString());
        }

        return builder.toString();
    }

    /**
     * Resets all collected diff information except the processing errors
     */
    public void reset() {
        this.additionalFiles.clear();
        this.additionalProperties.clear();
        this.changedProperties.clear();
        this.duplicateFiles.clear();
        this.missingFiles.clear();
        this.missingProperties.clear();
        this.nonConfigurationFiles.clear();
    }

    /**
     * Sort all maps based on keys for output
     */
    private void sortMaps() {
        sortFilesByFileName(this.additionalFiles);
        sortFilesByFileName(this.missingFiles);
        sortFilesByFileName(this.nonConfigurationFiles);
        sortFilesByFileName(this.duplicateFiles);

        sortProperties(this.additionalProperties);
        sortProperties(this.changedProperties);
        sortProperties(this.missingProperties);
    }

    /**
     * Sorts the given list of property diffs based on the file names the diff occurs in
     *
     * @param list with PropertyDiffs to sort
     */
    private void sortProperties(List<PropertyDiff> properties) {
        Collections.sort(properties, new PropertyDiffByFileNameSorter());
    }

    /**
     * Sorts the given list of configuration files based on the file names
     *
     * @param list with ConfigurationFiles to sort
     */
    private void sortFilesByFileName(List<ConfigurationFile> list) {
        Collections.sort(list, new ConfigurationFileByNameSorter());
    }
}
