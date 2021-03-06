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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
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
 * {@link JarFileProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class, ConfFileHandler.class })
public class JarFileProviderTest {

    @InjectMocks
    private JarFileProvider fileProvider;

    @Mock
    private File configurationFile;

    @Rule
    private final TemporaryFolder folder = new TemporaryFolder();

    List<File> configurationFiles = new ArrayList<>();

    private File rootFolder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(ConfFileHandler.class);

        configurationFiles.add(configurationFile);

        rootFolder = Mockito.mock(File.class);
    }

    @Test
    public void testReadConfigurationFiles_listFilesNull_returnEmptyArray() {
        PowerMockito.when(FileUtils.listFiles((File) ArgumentMatchers.any(), ArgumentMatchers.any(String[].class), ArgumentMatchers.anyBoolean())).thenReturn(null);

        List<File> readConfigurationFiles = fileProvider.readConfigurationFiles(new DiffResult(), rootFolder, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

        Assert.assertEquals(0, readConfigurationFiles.size());
    }

    @Test
    public void testReadConfigurationFiles_fileFound_fileInList() {
        PowerMockito.when(FileUtils.listFiles((File) ArgumentMatchers.any(), ArgumentMatchers.any(AndFileFilter.class), ArgumentMatchers.any(IOFileFilter.class))).thenReturn(configurationFiles);

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
    public void testAddFilesToDiffQueue_filesNotInConfFolder_noFileAddedToQueue() throws IOException {
        File newFile = folder.newFile("file1.jar");
        File newFile2 = folder.newFile("file2.jar");
        List<File> files = new ArrayList<>();
        files.add(newFile);
        files.add(newFile2);

        fileProvider.addFilesToDiffQueue(new DiffResult(), rootFolder, files, true);

        PowerMockito.verifyStatic(ConfFileHandler.class, Mockito.never());
        ConfFileHandler.addConfigurationFile(ArgumentMatchers.any(DiffResult.class), ArgumentMatchers.any(ConfigurationFile.class));
    }
}
