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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
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

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public List<File> readConfigurationFiles(DiffResult diffResult, File rootFolder, String[] fileExtension) {
        Collection<File> listFiles = FileUtils.listFiles(rootFolder, new AndFileFilter(new PrefixFileFilter("com.openexchange."), new SuffixFileFilter(".jar")), TrueFileFilter.TRUE);

        if (listFiles != null) {
            return Collections.synchronizedList(new ArrayList<File>(listFiles));
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

            JarFile jarFile;
            try {
                jarFile = new JarFile(currentFile);
                final Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    final String entryName = entry.getName();

                    // TODO check previously if 'isFile'
                    final int slashIdx = entryName.lastIndexOf('/');
                    if (slashIdx > 0) {
                        final String entryExt = entryName.substring(slashIdx + 1);

                        if (!"".equalsIgnoreCase(entryExt) && FilenameUtils.isExtension(entryExt, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE) && entryName.contains("conf/")) {
                            InputStream inputStream = jarFile.getInputStream(entry);
                            try {
                                String fileContent = IOUtils.toString(inputStream);
                                IOUtils.closeQuietly(inputStream);
                                inputStream = null;
                                String pathWithoutRootFolder = FileProviderUtil.removeRootFolder(currentFile.getAbsolutePath() + "!/" + entryName, rootDirectory.getAbsolutePath());
                                ConfigurationFile configurationFile = new ConfigurationFile(entryExt, rootDirectory.getAbsolutePath(), FilenameUtils.getFullPath(pathWithoutRootFolder), fileContent, isOriginal);
                                ConfFileHandler.addConfigurationFile(diffResult, configurationFile);
                            } finally {
                                if (null != inputStream) {
                                    IOUtils.closeQuietly(inputStream);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error adding configuration file to queue: " + e.getLocalizedMessage() + ". Please run with root.\n");
            }
        }
    }
}
