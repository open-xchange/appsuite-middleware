package com.openexchange.admin.diff.file.type;

import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.diff.file.type.impl.CcfHandler;
import com.openexchange.admin.diff.file.type.impl.ConfHandler;
import com.openexchange.admin.diff.file.type.impl.NoConfigFileHandler;
import com.openexchange.admin.diff.file.type.impl.NoExtensionHandler;
import com.openexchange.admin.diff.file.type.impl.PerfmapHandler;
import com.openexchange.admin.diff.file.type.impl.PropertyHandler;
import com.openexchange.admin.diff.file.type.impl.ShHandler;
import com.openexchange.admin.diff.file.type.impl.TypesHandler;
import com.openexchange.admin.diff.file.type.impl.XmlHandler;
import com.openexchange.admin.diff.file.type.impl.YamlHandler;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link ConfFileHandlerTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class ConfFileHandlerTest {

    @Before
    public void setUp() throws Exception {
        MockUtils.injectValueIntoPrivateField(CcfHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(ConfHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(PropertyHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(NoExtensionHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(PerfmapHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(ShHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(YamlHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(TypesHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(XmlHandler.class, "instance", null);
        MockUtils.injectValueIntoPrivateField(NoConfigFileHandler.class, "instance", null);
    }

    @Test
    public void testAddConfigurationFile_noConfigurationFile_fileAdded() {
        String fileName = "aaaa.dfdsf";
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(NoConfigFileHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_ccfConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CCF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(CcfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_cnfConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CNF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(ConfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_confConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CONF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(ConfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_inConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension() + "." + ConfigurationFileTypes.IN.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get("aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension());
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_noExtensionConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.NO_EXTENSION.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(NoExtensionHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_perfmapConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PERFMAP.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(PerfmapHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_propertyConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_shConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(ShHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_typesConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.TYPES.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(TypesHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_xmlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.XML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(XmlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_yamlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YAML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(YamlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_ymlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, false);

        Map<String, String> installedFiles = (Map<String, String>) MockUtils.getValueFromField(YamlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(fileName);
        Assert.assertEquals(content, string);
    }


    @Test
    public void testAddConfigurationFile_ccfConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CCF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(CcfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_cnfConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CNF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(ConfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_confConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CONF.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(ConfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_inConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension() + "." + ConfigurationFileTypes.IN.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(ShHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get("aaaa." + ConfigurationFileTypes.SH.getFileExtension());
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_noExtensionConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.NO_EXTENSION.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(NoExtensionHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_perfmapConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PERFMAP.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(PerfmapHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_propertyConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_shConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(ShHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_typesConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.TYPES.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(TypesHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_xmlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.XML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(XmlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_yamlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YAML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(YamlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }

    @Test
    public void testAddConfigurationFile_ymlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YML.getFileExtension();
        String content = "content";

        ConfFileHandler.addConfigurationFile(fileName, content, true);

        Map<String, String> originalFiles = (Map<String, String>) MockUtils.getValueFromField(YamlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(fileName);
        Assert.assertEquals(content, string);
    }
}
