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

    private String content = "this is the content of the file";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));

        noExtensionHandler.getFileDiffs(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(1, diffResult.getMissingFiles().size());
        Assert.assertEquals("file2.properties", diffResult.getMissingFiles().get(0).getName());
        Assert.assertEquals("/opt/open-xchange/bundles/jar!/conffile2.properties", diffResult.getMissingFiles().get(0).getFullFilePathWithExtension());
    }

    @Test
    public void testGetFileDiffs_additionalFileInInstallation_addToMap() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
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

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));
        lInstalledFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile2", true));

        noExtensionHandler.getFileDiffs(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getMissingFiles().size());
        Assert.assertEquals(0, diffResult.getAdditionalFiles().size());
    }
}
