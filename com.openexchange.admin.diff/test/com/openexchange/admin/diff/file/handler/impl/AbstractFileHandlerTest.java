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

package com.openexchange.admin.diff.file.handler.impl;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link AbstractFileHandlerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class AbstractFileHandlerTest {

    @InjectMocks
    private NoExtensionHandler noExtensionHandler;

    private String fileName = "myFile.properties";

    private String mpasswd = "mpasswd";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddFile_originalFile_fileAdded() {
        noExtensionHandler.addFile(new DiffResult(), new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));

        Assert.assertEquals(1, noExtensionHandler.originalFiles.size());
        Assert.assertEquals(0, noExtensionHandler.installedFiles.size());
    }

    @Test
    public void testAddFile_installedFile_fileAdded() {
        noExtensionHandler.addFile(new DiffResult(), new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", false));

        Assert.assertEquals(0, noExtensionHandler.originalFiles.size());
        Assert.assertEquals(1, noExtensionHandler.installedFiles.size());
    }

    @Test
    public void testAddFile_fileToIgnore_fileNotAdded() {
        noExtensionHandler.addFile(new DiffResult(), new ConfigurationFile(mpasswd, "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));

        Assert.assertEquals(0, noExtensionHandler.originalFiles.size());
        Assert.assertEquals(0, noExtensionHandler.installedFiles.size());
    }

    @Test
    public void testGetFileDiffs_fileInInstallationMissing_addToMap() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));

        noExtensionHandler.getFileDiffs(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(1, diffResult.getMissingFiles().size());
        Assert.assertEquals("file2.properties", diffResult.getMissingFiles().get(0).getName());
        Assert.assertEquals("/opt/open-xchange/bundles/jar!/conffile2.properties", diffResult.getMissingFiles().get(0).getFullFilePathWithExtension());
    }

    @Test
    public void testGetFileDiffs_additionalFileInInstallation_addToMap() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));
        lInstalledFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile2", true));

        noExtensionHandler.getFileDiffs(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getMissingFiles().size());
        Assert.assertEquals(1, diffResult.getAdditionalFiles().size());
        Assert.assertEquals("valueFile2", diffResult.getAdditionalFiles().get(0).getName(), "file2.properties");
    }

    @Test
    public void testGetFileDiffs_filesEqual_mapsEmpty() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));
        lInstalledFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile2", true));

        noExtensionHandler.getFileDiffs(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getMissingFiles().size());
        Assert.assertEquals(0, diffResult.getAdditionalFiles().size());
    }
}
