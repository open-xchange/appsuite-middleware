package com.openexchange.admin.diff.file.handler.impl;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
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

    // TODO test the PropertyHandler
    @Ignore
    @Test
    public void testGetDiff() {
        new DiffResult();

        List<ConfigurationFile> lOriginalFiles = new ArrayList<ConfigurationFile>();
        lOriginalFiles.add(new ConfigurationFile("file1.properties", "properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile1", true));
        lOriginalFiles.add(new ConfigurationFile("file2.properties", "properties", "/opt/open-xchange/bundles", "/jar!/conf", "valueFile2", true));

        List<ConfigurationFile> lInstalledFiles = new ArrayList<ConfigurationFile>();
        lInstalledFiles.add(new ConfigurationFile("file1.properties", "properties", "/opt/open-xchange/etc", "/jar!/conf", "valueFile1", false));

        propertyHandler.getPropertyDiffsPerFile(new DiffResult(), lOriginalFiles, lInstalledFiles);
    }
}
