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

package com.openexchange.admin.diff.file.provider;

import java.io.File;
import java.util.List;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * Provides configuration files and adds them to the diff queue.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public interface IConfigurationFileProvider {

    /**
     * Read the configuration files from the given rootFolder.
     * 
     * @param diffResult
     * @param rootFolder - the folder to start reading
     * @param fileExtension - the file extensions to look at
     * @return List with the configuration files read or an empty List if no file was found. Never returns null!
     */
    public List<File> readConfigurationFiles(DiffResult diffResult, File rootFolder, String[] fileExtension);

    /**
     * Adds the given files to the diff queue
     * 
     * @param diffResult
     * @param rootFolder - the folder to start reading
     * @param filesToAdd - the files to add to the queue
     * @param isOriginal - flag if the files to add are from original installation folder or if they are currently installed
     */
    public void addFilesToDiffQueue(DiffResult diffResult, File rootFolder, List<File> filesToAdd, boolean isOriginal);

}
