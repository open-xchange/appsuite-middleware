package com.openexchange.admin.diff.file.type.impl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import com.openexchange.admin.diff.result.DiffResult;


/**
 * {@link PropertyHandlerTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
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
        DiffResult diffResult = new DiffResult();
        Map<String, String> lOriginalFiles = new HashMap<String, String>();
        lOriginalFiles.put("file1.properties", "valueFile1");
        lOriginalFiles.put("file2.properties", "valueFile2");
        Map<String, String> lInstalledFiles = new HashMap<String, String>();
        lInstalledFiles.put("file1.properties", "valueFile1");

        propertyHandler.getPropertyDiffsPerFile(diffResult, lOriginalFiles, lInstalledFiles);

    }

}
