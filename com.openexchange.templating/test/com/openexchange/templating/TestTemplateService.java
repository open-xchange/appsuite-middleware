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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.templating;

import java.io.StringWriter;
import java.util.HashMap;
import junit.framework.TestCase;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.sim.SimBuilder;
import com.openexchange.templating.impl.OXFolderHelper;
import com.openexchange.templating.impl.OXInfostoreHelper;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TestTemplateService extends TestCase {
    
    protected SimConfigurationService configService = null;
    protected TemplateServiceImpl templateService = null;
    private ServerSession session = new ServerSessionAdapter((Session)null, (Context) null);
    private FolderObject privateTemplateFolder;
    private FolderObject globalTemplateFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        configService = new SimConfigurationService();
        configService.stringProperties.put("com.openexchange.templating.path", "test-resources");
        configService.stringProperties.put("com.openexchange.templating.usertemplating", "true");

        templateService = new TemplateServiceImpl(configService);
        

        TemplateErrorMessage.EXCEPTIONS.setApplicationId("com.openexchange.subscribe");
        TemplateErrorMessage.EXCEPTIONS.setComponent(new StringComponent("TMPL"));
    
        privateTemplateFolder = new FolderObject();
        privateTemplateFolder.setFolderName("Templates");
        privateTemplateFolder.setObjectID(23);
        
        globalTemplateFolder = new FolderObject();
        globalTemplateFolder.setFolderName("Templates");
        globalTemplateFolder.setObjectID(13);
    }

    @Override
    public void tearDown() throws Exception {
        configService = null;
        templateService = null;
        
        super.tearDown();
    }
    
    public void testLoadTemplate() throws Exception {
        OXTemplate template = templateService.loadTemplate("test-template");
        assertNotNull("OX-Template should not be null", template);
    }
    
    public void testLoadTemplateFromPrivateTemplateFolder() throws Exception {
        SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(privateTemplateFolder);
        
        SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, privateTemplateFolder, "test-template").andReturn("Template Content");
        
        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));
    
        OXTemplate template = templateService.loadTemplate("test-template", "default-template", session);
        
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Template Content", writer.toString());
        
        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }

    public void testLoadTemplateFromGlobalTemplateFolder() throws Exception {
        SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);
        
        SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, globalTemplateFolder, "test-template").andReturn("Template Content");
        
        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));
    
        OXTemplate template = templateService.loadTemplate("test-template", "default-template", session);
        
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Template Content", writer.toString());
        
        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }
    
    public void testCreateCopyOfDefaultTemplateInPrivateTemplateFolder() throws Exception {
        SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(privateTemplateFolder);
        
        SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, privateTemplateFolder, "new-template").andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(null);
        infostoreBuilder.expectCall("storeTemplateInFolder", session, privateTemplateFolder, "new-template", "Test Content In File");
        
        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));
    
        OXTemplate template = templateService.loadTemplate("new-template", "test-template", session);
        
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Test Content In File\n", writer.toString());
        
        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }
    
    public void testCreatePrivateTemplateFolderAndCopyDefaultTemplate() throws Exception {
        SimBuilder oxfolderHelperBuilder = new SimBuilder();
        oxfolderHelperBuilder.expectCall("getPrivateTemplateFolder", session).andReturn(null);
        oxfolderHelperBuilder.expectCall("getGlobalTemplateFolder", session).andReturn(globalTemplateFolder);
        oxfolderHelperBuilder.expectCall("createPrivateTemplateFolder", session).andReturn(privateTemplateFolder);
            
        SimBuilder infostoreBuilder = new SimBuilder();
        infostoreBuilder.expectCall("findTemplateInFolder", session, privateTemplateFolder, "new-template").andReturn(null);
        infostoreBuilder.expectCall("storeTemplateInFolder", session, privateTemplateFolder, "new-template", "Test Content In File");
        
        templateService.setOXFolderHelper(oxfolderHelperBuilder.getSim(OXFolderHelper.class));
        templateService.setInfostoreHelper(infostoreBuilder.getSim(OXInfostoreHelper.class));
    
        OXTemplate template = templateService.loadTemplate("new-template", "test-template", session);
        
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Test Content In File\n", writer.toString());
        
        oxfolderHelperBuilder.assertAllWereCalled();
        infostoreBuilder.assertAllWereCalled();
    }
    
    public void testFallbackToDefaultTemplate() throws Exception {
        OXTemplate template = templateService.loadTemplate("", "test-template", session);
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Test Content In File\n", writer.toString());
        
        template = templateService.loadTemplate(null, "test-template", session);
        assertNotNull(template);
        writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Test Content In File\n", writer.toString());

    }
    
    public void testDisableUserTemplatingPerConfiguration() throws Exception {
        configService.stringProperties.put("com.openexchange.templating.usertemplating", "false");

        OXTemplate template = templateService.loadTemplate("user-template", "test-template", session);
        assertNotNull(template);
        StringWriter writer = new StringWriter();
        template.process(new HashMap<Object, Object>(), writer);
        assertEquals("Test Content In File\n", writer.toString());

    }
}
