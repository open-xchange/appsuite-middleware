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
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import com.openexchange.admin.diff.file.provider.IConfigurationFileProvider;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link FileHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class FileHandlerTest {

    @Spy
    private FileHandler fileHandler;

    @Mock
    private IConfigurationFileProvider configurationFileProvider;

    private final boolean isOriginal = false;

    private File rootFolder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        rootFolder = Mockito.mock(File.class);
    }

    @Test
    public void testReadConfFiles_noConfigurationFileProvider_doNothing() throws IOException, TooManyFilesException {
        Mockito.doNothing().when(fileHandler).validateDirectory(ArgumentMatchers.any());

        fileHandler.readConfFiles(new DiffResult(), rootFolder, isOriginal);

        Mockito.verify(configurationFileProvider, Mockito.never()).addFilesToDiffQueue(ArgumentMatchers.<DiffResult> any(), ArgumentMatchers.any(File.class), ArgumentMatchers.anyList(), ArgumentMatchers.anyBoolean());
        Mockito.verify(configurationFileProvider, Mockito.never()).readConfigurationFiles(ArgumentMatchers.<DiffResult> any(), ArgumentMatchers.any(File.class), ArgumentMatchers.any(String[].class));
    }

    @Test
    public void testReadConfFiles_configurationFileProvider_readAndAddFiles() throws IOException, TooManyFilesException {
        Mockito.doNothing().when(fileHandler).validateDirectory(ArgumentMatchers.any());

        fileHandler.readConfFiles(new DiffResult(), rootFolder, isOriginal, configurationFileProvider);

        Mockito.verify(configurationFileProvider, Mockito.times(1)).addFilesToDiffQueue(ArgumentMatchers.<DiffResult> any(), ArgumentMatchers.any(File.class), ArgumentMatchers.anyList(), ArgumentMatchers.anyBoolean());
        Mockito.verify(configurationFileProvider, Mockito.times(1)).readConfigurationFiles(ArgumentMatchers.<DiffResult> any(), ArgumentMatchers.any(File.class), ArgumentMatchers.any(String[].class));
    }
}
