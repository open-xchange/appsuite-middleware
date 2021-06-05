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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.ConfFileHandler;
import com.openexchange.admin.diff.file.provider.util.FileProviderUtil;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link RecursiveFileProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class RecursiveFileProvider implements IConfigurationFileProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> readConfigurationFiles(DiffResult diffResult, File rootFolder, String[] fileExtension) {
        // read all file types and not those in ConfigurationFileTypes.CONFIGURATION_FILE_TYPE to get unexpected files
        Collection<File> listFiles = FileUtils.listFiles(rootFolder, fileExtension, true);

        if (listFiles != null) {
            return Collections.synchronizedList(new ArrayList<>(listFiles));
        }
        return Collections.synchronizedList(new ArrayList<File>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFilesToDiffQueue(DiffResult diffResult, File rootDirectory, List<File> filesToAdd, boolean isOriginal) {
        if (filesToAdd == null) {
            return;
        }

        for (File currentFile : filesToAdd) {
            try (FileReader fileReader = new FileReader(currentFile)) {
                String fileContent = IOUtils.toString(fileReader);
                ConfigurationFile configurationFile = new ConfigurationFile(currentFile.getName(), rootDirectory.getAbsolutePath(), FilenameUtils.getFullPath(FileProviderUtil.removeRootFolder(currentFile.getAbsolutePath(), rootDirectory.getAbsolutePath())), fileContent, isOriginal);
                ConfFileHandler.addConfigurationFile(diffResult, configurationFile);
            } catch (FileNotFoundException e) {
                diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
            }
        }
    }
}
