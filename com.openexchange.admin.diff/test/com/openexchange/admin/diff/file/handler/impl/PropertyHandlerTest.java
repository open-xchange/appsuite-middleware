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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

     @Test
     public void testGetPropertyDiffsPerFile_oneMoreFile_notMarkedForPropertyDiff() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile2=valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile1", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

     @Test
     public void testGetPropertyDiffsPerFile_propertyChanged_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile5", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(1, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

     @Test
     public void testGetPropertyDiffsPerFile_propertyMissing_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(0, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(1, diffResult.getMissingProperties().size());
    }

     @Test
     public void testGetPropertyDiffsPerFile_valueMissing_markAsChanged() {
        DiffResult diffResult = new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/bundles", "/jar!/conf", "keyFile1=valueFile1", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "/opt/open-xchange/etc", "/jar!/conf", "keyFile1=valueFile1\nkeyFile2=newProperty", false));

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

        Assert.assertEquals(1, diffResult.getAdditionalProperties().size());
        Assert.assertEquals(0, diffResult.getChangedProperties().size());
        Assert.assertEquals(0, diffResult.getMissingProperties().size());
    }

}
