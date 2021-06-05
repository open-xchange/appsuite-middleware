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

package com.openexchange.admin.diff.file.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.admin.diff.file.provider.IConfigurationFileProvider;
import com.openexchange.admin.diff.file.type.ConfigurationFileTypes;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * Executes the given IConfigurationFileProvider to read configuration files and add them to the diff queue.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class FileHandler {

    /**
     * Walk through the given rootDirectory handled by the provided IConfigurationFileProvider. The read files will also be added to diff
     * working queue.
     *
     * @param diffResult - The {@link DiffResult} the processing results of the current handler will be attached.
     * @param rootDirectory - root directory to read from; is a valid directory, which can be read.
     * @param isOriginal - indicates if the provided files will be from original or installed configuration.
     * @param configurationFileProviders - providers that should be executed to gather all files.
     */
    public void readConfFiles(DiffResult diffResult, File rootDirectory, boolean isOriginal, IConfigurationFileProvider... configurationFileProviders) throws TooManyFilesException {
        if (configurationFileProviders == null) {
            return;
        }

        try {
            validateDirectory(rootDirectory);
        } catch (FileNotFoundException e) {
            diffResult.getProcessingErrors().add("Error in validating directory " + rootDirectory + "\n" + e.getLocalizedMessage() + "\n");
            return;
        }

        List<File> confFiles = new ArrayList<File>();

        for (IConfigurationFileProvider configurationFileProvider : configurationFileProviders) {
            confFiles = configurationFileProvider.readConfigurationFiles(diffResult, rootDirectory, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

            if (confFiles.size() > 10000) {
                String errorMessage = "Too many configuration files found (allowed <= 10000; found " + confFiles.size() + ") by file provider " + configurationFileProvider + " for further processing. Stop diff execution!!!";
                diffResult.getProcessingErrors().add(errorMessage);
                throw new TooManyFilesException(errorMessage);
            }
            configurationFileProvider.addFilesToDiffQueue(diffResult, rootDirectory, confFiles, isOriginal);
        }
    }

    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     *
     * @param directory - the directory that should be validated.
     */
    protected void validateDirectory(File directory) throws FileNotFoundException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!directory.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + directory);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + directory);
        }
        if (!directory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: " + directory);
        }
    }
}
