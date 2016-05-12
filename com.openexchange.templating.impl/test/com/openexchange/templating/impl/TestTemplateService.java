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

package com.openexchange.templating.impl;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.session.SimSession;
import com.openexchange.sim.SimBuilder;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplate.TemplateLevel;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 * This test will not work if started from the MainTestSuite as the folder test-resources is not found
 * TODO fix this in the future
 */
public class TestTemplateService extends TestCase {

    protected SimConfigurationService configService = null;
    protected TemplateServiceImpl templateService = null;
    private ServerSession session = null;
    private ServerSession sessionWithoutInfostore = null;

    private FolderObject privateTemplateFolder;
    private FolderObject globalTemplateFolder;

    @Mock
    private Context context;

    @Mock
    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        configService = new SimConfigurationService();
        configService.stringProperties.put("com.openexchange.templating.path", "test-resources");
        configService.stringProperties.put("com.openexchange.templating.usertemplating", "true");

        templateService = new TemplateServiceImpl(configService) {

            @Override
            protected String loadFromFileSystem(final String defaultTemplateName) {
                return "Test Content In File\n";
            }
        };

        privateTemplateFolder = new FolderObject();
        privateTemplateFolder.setFolderName("Templates");
        privateTemplateFolder.setObjectID(23);

        globalTemplateFolder = new FolderObject();
        globalTemplateFolder.setFolderName("Templates");
        globalTemplateFolder.setObjectID(13);

        final MutableUserConfiguration userConfig = new MutableUserConfiguration(new HashSet<String>(), 0, new int[0], null);
        userConfig.setInfostore(true);

        final MutableUserConfiguration noInfostore = new MutableUserConfiguration(new HashSet<String>(),0,new int[0],null);
        noInfostore.setInfostore(false);

        SimSession simSession = new SimSession(1, 1);
        session = new ServerSessionAdapter(simSession, context, new SimUser(1), userConfig, new UserPermissionBits(UserPermissionBits.INFOSTORE, 1, context));
        sessionWithoutInfostore = new ServerSessionAdapter(simSession, context, user, noInfostore, new UserPermissionBits(0, 1, context));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        configService = null;
        templateService = null;

        super.tearDown();
    }

    public void testLoadTemplate() throws Exception {
        final OXTemplate template = templateService.loadTemplate("test-template.tmpl");
        assertNotNull("OX-Template should not be null", template);
        assertEquals(TemplateLevel.SERVER, template.getLevel());
    }

    public void testLoadTemplateFromPrivateTemplateFolder() throws Exception {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(privateTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, privateTemplateFolder, "test-template.tmpl").andReturn("Template Content");

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final OXTemplate template = templateService.loadTemplate("test-template.tmpl", "default-template", session);

        assertNotNull(template);
        assertEquals(TemplateLevel.USER, template.getLevel());
        final StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Template Content", writer.toString().trim());

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testLoadTemplateFromGlobalTemplateFolder() throws Exception {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, globalTemplateFolder, "test-template.tmpl").andReturn("Template Content");

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final OXTemplate template = templateService.loadTemplate("test-template.tmpl", "default-template", session);
        assertNotNull(template);
        assertEquals(TemplateLevel.USER, template.getLevel());
        final StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Template Content", writer.toString().trim());

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testCreateCopyOfDefaultTemplateInPrivateTemplateFolder() throws Exception {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(privateTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, privateTemplateFolder, "new-template").andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(null);
        infostoreBuilder.expectCall("storeTemplateInFolder", session, privateTemplateFolder, "new-template", "Test Content In File\n");

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final OXTemplate template = templateService.loadTemplate("new-template", "test-template.tmpl", session);

        assertNotNull(template);
        assertEquals(TemplateLevel.USER, template.getLevel());
        final StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertTrue(writer.toString().contains("Test Content In File"));

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testCreatePrivateTemplateFolderAndCopyDefaultTemplate() throws Exception {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);
        oxfolderHelperBuilder.expectCall("createPrivateTemplateFolder", session).andReturn(privateTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, globalTemplateFolder, "new-template").andReturn(null);
        infostoreBuilder.expectCall("storeTemplateInFolder", session, privateTemplateFolder, "new-template", "Test Content In File\n");

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final OXTemplate template = templateService.loadTemplate("new-template", "test-template.tmpl", session);

        assertNotNull(template);
        assertEquals(TemplateLevel.USER, template.getLevel());
        final StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertTrue(writer.toString().contains("Test Content In File"));

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testGrabBasicTemplateAndDontRecreateIt() throws Exception {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, globalTemplateFolder, "test-template").andReturn(null);

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final OXTemplate template = templateService.loadTemplate("test-template", "test-template.tmpl", session);

        assertNotNull(template);
        assertEquals(TemplateLevel.SERVER, template.getLevel());

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testFallbackToDefaultTemplate() throws Exception {
        final boolean[] called = new boolean[]{false};
        templateService = new TemplateServiceImpl(configService) {
            @Override
            public OXTemplateImpl loadTemplate(final String templateName) {
                called[0] = true;
                return null;
            }
        };

        templateService.loadTemplate("", "test-template.tmpl", session);
        assertTrue(called[0]);

    }

    public void testDisableUserTemplatingPerConfiguration() throws Exception {
        configService.stringProperties.put("com.openexchange.templating.usertemplating", "false");

        final boolean[] called = new boolean[]{false};
        templateService = new TemplateServiceImpl(configService) {
            @Override
            public OXTemplateImpl loadTemplate(final String templateName) {
                called[0] = true;
                return null;
            }
        };

        templateService.loadTemplate("user-template", "test-template.tmpl", session);
        assertTrue(called[0]);

    }

    public void testDisableUserTemplatingWhenInfostoreIsDisabled() throws Exception {

        final boolean[] called = new boolean[]{false};
        templateService = new TemplateServiceImpl(configService) {
            @Override
            public OXTemplateImpl loadTemplate(final String templateName) {
                called[0] = true;
                return null;
            }
        };

        templateService.loadTemplate("user-template", "test-template.tmpl", sessionWithoutInfostore);
        assertTrue(called[0]);

    }

    public void testListBasicTemplates() {
        final List<String> names = templateService.getBasicTemplateNames();
        assertEquals(1, names.size());
        assertEquals("test-template.tmpl", names.get(0));
    }

    public void testListTemplates() throws OXException {
        final SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(privateTemplateFolder);

        final SimBuilder infostoreBuilder = new SimBuilder();
        final String[] filter = new String[0];
        infostoreBuilder.expectCall("getNames", session, globalTemplateFolder, filter).andReturn(Arrays.asList("global1" , "global2"));
        infostoreBuilder.expectCall("getNames", session, privateTemplateFolder, filter).andReturn(Arrays.asList("private1" , "private2"));

        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));

        final List<String> templateNames = templateService.getTemplateNames(session, filter);

        assertTrue(templateNames.contains("global1"));
        assertTrue(templateNames.contains("global2"));
        assertTrue(templateNames.contains("private1"));
        assertTrue(templateNames.contains("private2"));
        assertTrue(templateNames.contains("test-template.tmpl"));

        assertEquals(5, templateNames.size());

        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();

    }
}
