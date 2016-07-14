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
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(currentFile);
                    String fileContent = IOUtils.toString(fileReader);
                    fileReader.close();
                    fileReader = null;

                    ConfigurationFile configurationFile = new ConfigurationFile(currentFile.getName(), rootDirectory.getAbsolutePath(), FilenameUtils.getFullPath(FileProviderUtil.removeRootFolder(currentFile.getAbsolutePath(), rootDirectory.getAbsolutePath())), fileContent, isOriginal);
                    ConfFileHandler.addConfigurationFile(diffResult, configurationFile);
                } catch (FileNotFoundException e) {
                    diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
                } catch (IOException e) {
                    diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
                } finally {
                    if (null != fileReader) {
                        try { fileReader.close(); } catch (Exception e) { /*ignore*/ }
                    }
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
