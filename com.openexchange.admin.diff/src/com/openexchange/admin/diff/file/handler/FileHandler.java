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
