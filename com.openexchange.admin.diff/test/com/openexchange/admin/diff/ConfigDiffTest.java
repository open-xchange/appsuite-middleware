package com.openexchange.admin.diff;

import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.admin.diff.file.type.IConfFileHandler;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.test.mock.MockUtils;


/**
 * Unit tests for {@link ConfigDiff}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class ConfigDiffTest {

    @InjectMocks
    private ConfigDiff configDiff;

    @Mock
    private IConfFileHandler confFileHandler;

    private String originalFolderChanged = "/opt/open-xchange/bundlesChanged";

    private String installationFolderChanged = "/opt/open-xchange/etcChanged";

    private String originalFolder = "/opt/open-xchange/bundles";

    private String installationFolder = "/opt/open-xchange/etc";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(confFileHandler.getDiff((DiffResult) Matchers.any())).thenReturn(new DiffResult());
    }

    @Test
    public void testConstructor_originalFolderNull_setDefault() {
        configDiff = new ConfigDiff(null, installationFolder);

        Assert.assertEquals(configDiff.originalFolder, originalFolder);
    }

    @Test
    public void testConstructor_originalFolderBlank_setDefault() {
        configDiff = new ConfigDiff("", installationFolder);

        Assert.assertEquals(configDiff.originalFolder, originalFolder);
    }

    @Test
    public void testConstructor_installedFolderNull_setDefault() {
        configDiff = new ConfigDiff(originalFolder, null);

        Assert.assertEquals(configDiff.installationFolder, installationFolder);
    }

    @Test
    public void testConstructor_installedFolderBlank_setDefault() {
        configDiff = new ConfigDiff(originalFolder, "");

        Assert.assertEquals(configDiff.installationFolder, installationFolder);
    }

    @Test
    public void testConstructor_providedFolders_setProvidedFolders() {
        configDiff = new ConfigDiff(originalFolderChanged, installationFolderChanged);

        Assert.assertEquals(configDiff.originalFolder, originalFolderChanged);
        Assert.assertEquals(configDiff.installationFolder, installationFolderChanged);
    }

    @Test
    public void testRegisterHandler_handlerAdded_inList() {
        configDiff = new ConfigDiff(originalFolderChanged, installationFolderChanged);

        ConfigDiff.register(confFileHandler);

        Set valueFromField = (Set) MockUtils.getValueFromField(ConfigDiff.class, "handlers");
        Assert.assertEquals(1, valueFromField.size());
    }

    @Test
    public void testGetDiff_handlerRegistered_callHandler() {
        configDiff = new ConfigDiff(originalFolderChanged, installationFolderChanged);
        ConfigDiff.register(confFileHandler);
        DiffResult diffResult = new DiffResult();

        configDiff.getDiffs(diffResult);

        Mockito.verify(confFileHandler, Mockito.times(1)).getDiff(diffResult);
    }
}
