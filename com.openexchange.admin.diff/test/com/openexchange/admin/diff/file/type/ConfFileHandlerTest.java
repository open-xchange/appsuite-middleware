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

package com.openexchange.admin.diff.file.type;

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.ConfFileHandler;
import com.openexchange.admin.diff.file.handler.impl.CcfHandler;
import com.openexchange.admin.diff.file.handler.impl.ConfHandler;
import com.openexchange.admin.diff.file.handler.impl.NoConfigFileHandler;
import com.openexchange.admin.diff.file.handler.impl.NoExtensionHandler;
import com.openexchange.admin.diff.file.handler.impl.PerfmapHandler;
import com.openexchange.admin.diff.file.handler.impl.PropertyHandler;
import com.openexchange.admin.diff.file.handler.impl.ShHandler;
import com.openexchange.admin.diff.file.handler.impl.TypesHandler;
import com.openexchange.admin.diff.file.handler.impl.XmlHandler;
import com.openexchange.admin.diff.file.handler.impl.YamlHandler;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link ConfFileHandlerTest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
@SuppressWarnings("unchecked")
public class ConfFileHandlerTest {

    private ConfigurationFile configurationFile = null;

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
        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(NoConfigFileHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_ccfConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CCF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        CcfHandler.getInstance().addFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(CcfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);

    }

    @Test
    public void testAddConfigurationFile_cnfConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CNF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ConfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_confConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CONF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ConfHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_inConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension() + "." + ConfigurationFileTypes.IN.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(FilenameUtils.removeExtension(fileName), string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_noExtensionConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.NO_EXTENSION.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(NoExtensionHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_perfmapConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PERFMAP.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(PerfmapHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_propertyConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_shConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ShHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_typesConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.TYPES.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(TypesHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_xmlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.XML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(XmlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_yamlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YAML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(YamlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_ymlConfigurationFile_fileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, false);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> installedFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(YamlHandler.getInstance(), "installedFiles");
        Assert.assertEquals(1, installedFiles.size());
        String string = installedFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = installedFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_ccfConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CCF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(CcfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_cnfConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CNF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ConfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_confConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.CONF.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ConfHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_inConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension() + "." + ConfigurationFileTypes.IN.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ShHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(FilenameUtils.removeExtension(fileName), string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_noExtensionConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.NO_EXTENSION.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(NoExtensionHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_perfmapConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PERFMAP.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(PerfmapHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_propertyConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.PROPERTY.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(PropertyHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_shConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.SH.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(ShHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_typesConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.TYPES.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(TypesHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_xmlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.XML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(XmlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_yamlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YAML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(YamlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }

    @Test
    public void testAddConfigurationFile_ymlConfigurationFile_origFileAdded() {
        String fileName = "aaaa." + ConfigurationFileTypes.YML.getFileExtension();
        String content = "content";

        configurationFile = new ConfigurationFile(fileName, "/opt/open-xchange/bundles", "/jar!/conf", content, true);

        ConfFileHandler.addConfigurationFile(new DiffResult(), configurationFile);

        List<ConfigurationFile> originalFiles = (List<ConfigurationFile>) MockUtils.getValueFromField(YamlHandler.getInstance(), "originalFiles");
        Assert.assertEquals(1, originalFiles.size());
        String string = originalFiles.get(0).getName();
        Assert.assertEquals(fileName, string);
        String stringContent = originalFiles.get(0).getContent();
        Assert.assertEquals(content, stringContent);
    }
}
