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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.ConfFileHandler;
import com.openexchange.admin.diff.file.provider.util.FileProviderUtil;
import com.openexchange.admin.diff.file.type.ConfigurationFileTypes;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * Provides configuration files from within a jar file.<br>
 * <br>
 * The JarFileProvider only opens the jar once within addFilesToDiffQueue(...). readConfigurationFiles(...) is responsible for getting all
 * eligible jars.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class JarFileProvider implements IConfigurationFileProvider {

    private static final String EMPTY_STRING = "";

    /**
     * Initializes a new {@link JarFileProvider}.
     */
    public JarFileProvider() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public List<File> readConfigurationFiles(DiffResult diffResult, File rootFolder, String[] fileExtension) {
        Collection<File> listFiles = FileUtils.listFiles(rootFolder, new AndFileFilter(new PrefixFileFilter("com.openexchange."), new SuffixFileFilter(".jar")), TrueFileFilter.TRUE);
        return Collections.synchronizedList(listFiles != null ? new ArrayList<>(listFiles) : new LinkedList<>());
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
            try (JarFile jarFile = new JarFile(currentFile)) {
                processJarEntries(rootDirectory, currentFile, jarFile, diffResult, isOriginal);
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
            }
        }
    }

    /**
     * Processes all JAR entries in the specified file
     * 
     * @param rootDirectory The folder to start reading
     * @param currentFile The current file
     * @param jarFile The JAR file
     * @param diffResult The diff result
     * @param isOriginal flag if the files to add are from original installation folder or if they are currently installed
     * @throws IOException If an I/O error is occurred
     */
    private void processJarEntries(File rootDirectory, File currentFile, JarFile jarFile, DiffResult diffResult, boolean isOriginal) throws IOException {
        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
            processJarEntry(rootDirectory, currentFile, jarFile, entries.nextElement(), diffResult, isOriginal);
        }
    }

    /**
     * Processes a single jar entry
     *
     * @param rootDirectory The folder to start reading
     * @param currentFile The current file
     * @param jarFile The JAR file
     * @param entry The JAR entry
     * @param diffResult The diff result
     * @param isOriginal flag if the files to add are from original installation folder or if they are currently installed
     * @throws IOException If an I/O error is occurred
     */
    private void processJarEntry(File rootDirectory, File currentFile, JarFile jarFile, JarEntry entry, DiffResult diffResult, boolean isOriginal) throws IOException {
        final String entryName = entry.getName();

        // TODO check previously if 'isFile'
        final int slashIdx = entryName.lastIndexOf('/');
        if (slashIdx <= 0) {
            return;
        }
        final String entryExt = entryName.substring(slashIdx + 1);
        if (EMPTY_STRING.equalsIgnoreCase(entryExt) || !FilenameUtils.isExtension(entryExt, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE) || !entryName.contains("conf/")) {
            return;
        }
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            String pathWithoutRootFolder = FileProviderUtil.removeRootFolder(currentFile.getAbsolutePath() + "!/" + entryName, rootDirectory.getAbsolutePath());
            ConfigurationFile configurationFile = new ConfigurationFile(entryExt, rootDirectory.getAbsolutePath(), FilenameUtils.getFullPath(pathWithoutRootFolder), fileContent, isOriginal);
            ConfFileHandler.addConfigurationFile(diffResult, configurationFile);
        }
    }
}
