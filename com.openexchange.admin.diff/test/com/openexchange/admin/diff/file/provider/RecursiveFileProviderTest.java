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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.ConfFileHandler;
import com.openexchange.admin.diff.file.type.ConfigurationFileTypes;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link RecursiveFileProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class, ConfFileHandler.class })
public class RecursiveFileProviderTest {

    @InjectMocks
    private RecursiveFileProvider fileProvider;

    @Mock
    private File configurationFile;

    @Rule
    private final TemporaryFolder folder = new TemporaryFolder();

    List<File> configurationFiles = new ArrayList<File>();

    private File rootFolder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(ConfFileHandler.class);

        configurationFiles.add(configurationFile);

        rootFolder = Mockito.mock(File.class);
    }

    @Test
    public void testReadConfigurationFiles_listFilesNull_returnEmptyArray() {
        PowerMockito.when(FileUtils.listFiles(ArgumentMatchers.any(File.class), ArgumentMatchers.any(String[].class), ArgumentMatchers.anyBoolean())).thenReturn(null);

        List<File> readConfigurationFiles = fileProvider.readConfigurationFiles(new DiffResult(), rootFolder, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

        Assert.assertEquals(0, readConfigurationFiles.size());
    }

    @Test
    public void testReadConfigurationFiles_fileFound_fileInList() {
        PowerMockito.when(FileUtils.listFiles(ArgumentMatchers.any(File.class), ArgumentMatchers.any(String[].class), ArgumentMatchers.anyBoolean())).thenReturn(configurationFiles);

        List<File> readConfigurationFiles = fileProvider.readConfigurationFiles(new DiffResult(), rootFolder, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

        Assert.assertEquals(1, readConfigurationFiles.size());
    }

    @Test
    public void testAddFilesToDiffQueue_filesNull_noFileAddedToQueue() {
        fileProvider.addFilesToDiffQueue(new DiffResult(), rootFolder, null, true);

        PowerMockito.verifyStatic(ConfFileHandler.class, Mockito.never());
        ConfFileHandler.addConfigurationFile(ArgumentMatchers.any(DiffResult.class), ArgumentMatchers.any(ConfigurationFile.class));
    }

    @Test
    public void testAddFilesToDiffQueue_filesProvided_addedToQueue() throws IOException {
        File newFile = folder.newFile("file1");
        File newFile2 = folder.newFile("file2");
        List<File> files = new ArrayList<File>();
        files.add(newFile);
        files.add(newFile2);

        fileProvider.addFilesToDiffQueue(new DiffResult(), rootFolder, files, true);

        PowerMockito.verifyStatic(ConfFileHandler.class, Mockito.times(2));
        ConfFileHandler.addConfigurationFile(ArgumentMatchers.any(DiffResult.class), ArgumentMatchers.any(ConfigurationFile.class));
    }
}
