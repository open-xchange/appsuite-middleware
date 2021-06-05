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
 * {@link PropertyHandlerTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class PropertyHandlerTest {

    @InjectMocks
    private PropertyHandler propertyHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPropertyDiffsPerFile_oneMoreFile_notMarkedForPropertyDiff() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile2=valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile1", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

    @Test
    public void testGetPropertyDiffsPerFile_propertyChanged_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile5", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(1, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

    @Test
    public void testGetPropertyDiffsPerFile_propertyMissing_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(1, diffResult.getMissingProperties().size());
    }

    @Test
    public void testGetPropertyDiffsPerFile_valueMissing_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile1\nkeyFile2=newProperty", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(1, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

}
