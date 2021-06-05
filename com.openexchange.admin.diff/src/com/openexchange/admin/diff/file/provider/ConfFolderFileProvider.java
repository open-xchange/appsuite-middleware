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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
 * Provides configuration files by recursive traversing the given folder and considering file extension.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ConfFolderFileProvider implements IConfigurationFileProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> readConfigurationFiles(DiffResult diffResult, File rootFolder, String[] fileExtension) {

        Collection<File> listFiles = Collections.synchronizedList(new ArrayList<File>());

        Collection<File> filesInRootFolder = FileUtils.listFiles(rootFolder, fileExtension, true);
        if ((filesInRootFolder != null) && (!filesInRootFolder.isEmpty())) {
            listFiles.addAll(filesInRootFolder);
        }

        List<File> filesWithoutExtension = getFilesWithoutExtension(rootFolder, Collections.synchronizedList(new ArrayList<File>()));
        if ((filesWithoutExtension != null) && (!filesWithoutExtension.isEmpty())) {
            listFiles.addAll(filesWithoutExtension);
        }

        return Collections.synchronizedList(new ArrayList<File>(listFiles));
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
            if (currentFile.getAbsolutePath().contains("/conf/")) {
                try (InputStreamReader reader = new InputStreamReader(new FileInputStream(currentFile), StandardCharsets.UTF_8)) {
                    String fileContent = IOUtils.toString(reader);

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

    /**
     * This method is a hack because FileUtils.listFiles(...) is not able to return files that have no extension.
     *
     * @param dir
     * @param listToFill
     * @return
     */
    public List<File> getFilesWithoutExtension(File dir, List<File> listToFill) {

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getFilesWithoutExtension(file, listToFill);
            } else {
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("")) {
                    listToFill.add(file);
                }
            }
        }

        return listToFill;
    }
}
