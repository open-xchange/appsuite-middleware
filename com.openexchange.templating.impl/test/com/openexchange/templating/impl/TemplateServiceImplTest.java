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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.impl.OXFolderHelper;
import com.openexchange.templating.impl.OXInfostoreHelper;
import com.openexchange.templating.impl.OXTemplateImpl;
import com.openexchange.templating.impl.TemplateServiceImpl;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Unit tests for {@link TemplateServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerSessionAdapter.class })
public class TemplateServiceImplTest {

    /**
     * Instance under test
     */
    @InjectMocks
    private TemplateServiceImpl templateService = null;

    /**
     * Name of the default template
     */
    private final String defaultTemplateName = "infostore.tmpl";

    /**
     * Name of the user defined template
     */
    private final String userDefinedTemplateName = "infostore_user.tmpl";

    /**
     * Mock of the {@link ConfigurationService}
     */
    @Mock
    private ConfigurationService configService;

    @Mock
    private ServerSession serverSession;

    /**
     * Mock of the {@link OXFolderHelper}
     */
    @Mock
    private OXFolderHelper folders;

    /**
     * Mock of the {@link OXInfostoreHelper}
     */
    @Mock
    private OXInfostoreHelper infostore;

    /**
     * Mock of the {@link Session}
     */
    @Mock
    private Session session;

    /**
     * The dummy template content
     */
    private final String templateContent = "theContentOfTheTemplate";

    /**
     * A temporary folder that could be used by each mock.
     */
    @Rule
    protected TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws OXException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ServerSessionAdapter.class);

        Mockito.when(this.configService.getProperty(TemplateServiceImpl.PATH_PROPERTY)).thenReturn("thePath");
        PowerMockito.when(ServerSessionAdapter.valueOf((Session) Matchers.anyObject())).thenReturn(serverSession);
    }

    @Test
    public void testIsAdminTemplate_noBasicTemplateDefined_returnFalse() {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>(0);
            }
        };

        boolean adminTemplate = this.templateService.isAdminTemplate(defaultTemplateName);

        Assert.assertFalse(adminTemplate);
    }

    @Test
    public void testIsAdminTemplate_defaultTemplateAvailable_returnTrue() {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>(Arrays.asList("contacts.tmpl", "contacts_hcard_censored.tmpl", "contacts_hcard_uncensored.tmpl", "contacts_oxmf_censored.tmpl", "contacts_oxmf_uncensored.tmpl", "iPhoneTemplate.tmpl", "infostore.tmpl", "infostore_gallery.tmpl"));
            }

            @Override
            protected boolean existsInFilesystem(final String templateName) {
                return true;
            }

        };

        boolean adminTemplate = this.templateService.isAdminTemplate(defaultTemplateName);

        Assert.assertTrue(adminTemplate);
    }

    @Test
    public void testIsAdminTemplate_userTemplateDesiredButNotAvailable_returnFalse() {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>(Arrays.asList("contacts.tmpl", "contacts_hcard_censored.tmpl", "contacts_hcard_uncensored.tmpl", "contacts_oxmf_censored.tmpl", "contacts_oxmf_uncensored.tmpl", "iPhoneTemplate.tmpl", "infostore.tmpl", "infostore_gallery.tmpl"));
            }
        };

        boolean adminTemplate = this.templateService.isAdminTemplate(this.userDefinedTemplateName);

        Assert.assertFalse(adminTemplate);
    }

    @Test
    public void testIsAdminTemplate_userTemplateDesiredAndAvailable_returnTrue() {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>(Arrays.asList("contacts.tmpl", "contacts_hcard_censored.tmpl", "contacts_hcard_uncensored.tmpl", "contacts_oxmf_censored.tmpl", "contacts_oxmf_uncensored.tmpl", "iPhoneTemplate.tmpl", "infostore.tmpl", "infostore_gallery.tmpl", userDefinedTemplateName));
            }

            @Override
            protected boolean existsInFilesystem(final String templateName) {
                return true;
            }
        };

        boolean adminTemplate = this.templateService.isAdminTemplate(this.userDefinedTemplateName);

        Assert.assertTrue(adminTemplate);
    }

    @Test
    public void testIsAdminTemplate_userTemplateAvailableButNotInFilesystem_returnEmptyTemplate() {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>(Arrays.asList("contacts.tmpl", "contacts_hcard_censored.tmpl", "contacts_hcard_uncensored.tmpl", "contacts_oxmf_censored.tmpl", "contacts_oxmf_uncensored.tmpl", "iPhoneTemplate.tmpl", "infostore.tmpl", "infostore_gallery.tmpl", userDefinedTemplateName));
            }

            @Override
            protected boolean existsInFilesystem(final String templateName) {
                return false;
            }
        };

        boolean adminTemplate = this.templateService.isAdminTemplate(this.userDefinedTemplateName);

        Assert.assertFalse(adminTemplate);
    }

    @Test(expected = OXException.class)
    public void testLoadTemplate_userTemplateNull_returnDefaultTemplate() throws OXException {
        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            public List<String> getBasicTemplateNames(final String... filter) {
                return new ArrayList<String>();
            }

        };
        this.templateService.setOXFolderHelper(folders);

        this.templateService.loadTemplate(null, this.defaultTemplateName, this.session, true);
    }

    @Test(expected = OXException.class)
    public void testLoadTemplate_userTemplateEmptyString_returnDefaultTemplate() throws OXException {
        this.templateService = new TemplateServiceImpl(this.configService);
        this.templateService.setOXFolderHelper(folders);

        this.templateService.loadTemplate("", this.defaultTemplateName, this.session, true);
    }

    @Test(expected = OXException.class)
    public void testLoadTemplate_defaultTemplateNull_returnDefaultTemplate() throws OXException {
        this.templateService = new TemplateServiceImpl(this.configService);
        this.templateService.setOXFolderHelper(folders);

        this.templateService.loadTemplate(this.userDefinedTemplateName, null, this.session, true);
    }

    @Test(expected = OXException.class)
    public void testLoadTemplate_defaultTemplateEmptyString_returnDefaultTemplate() throws OXException {
        this.templateService = new TemplateServiceImpl(this.configService);
        this.templateService.setOXFolderHelper(folders);

        this.templateService.loadTemplate(this.userDefinedTemplateName, "", this.session, true);
    }

    @Test(expected = OXException.class)
    public void testLoadTemplate_SessionNull_returnDefaultTemplate() throws OXException {
        this.templateService = new TemplateServiceImpl(this.configService);
        this.templateService.setOXFolderHelper(folders);

        this.templateService.loadTemplate(this.userDefinedTemplateName, this.defaultTemplateName, null, true);
    }

    @Test
    public void testLoadTemplate_NothingToFind_returnEmptyTemplate() throws OXException, IOException {
        folder.create();
        folder.newFolder();

        Mockito.when(this.configService.getProperty(TemplateServiceImpl.PATH_PROPERTY)).thenReturn(folder.getRoot().getAbsolutePath());

        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            protected boolean isAdminTemplate(String templateName) {
                return true;
            }

            @Override
            protected String loadFromFileSystem(final String defaultTemplateName) throws OXException {
                return "theContentOfTheTemplate";
            }
        };
        this.templateService.setOXFolderHelper(folders);
        this.templateService.setInfostoreHelper(infostore);
        Mockito.when(folders.getGlobalTemplateFolder((ServerSession) Matchers.any())).thenReturn(new FolderObject());

        OXTemplate template = this.templateService.loadTemplate(this.userDefinedTemplateName, this.defaultTemplateName, this.session, true);

        Assert.assertNotNull(template);
        Mockito.verify(folders, Mockito.times(1)).getPrivateTemplateFolder((ServerSession) Matchers.any());
    }

    @Test
    public void testLoadTemplate_loadFromFilesystem_returnTemplateContent() throws OXException, IOException {
        folder.create();
        folder.newFolder();

        Mockito.when(this.configService.getProperty(TemplateServiceImpl.PATH_PROPERTY)).thenReturn(folder.getRoot().getAbsolutePath());

        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            protected boolean isAdminTemplate(String templateName) {
                return true;
            }

            @Override
            protected String loadFromFileSystem(final String defaultTemplateName) throws OXException {
                return templateContent;
            }
        };
        this.templateService.setOXFolderHelper(folders);
        this.templateService.setInfostoreHelper(infostore);
        Mockito.when(folders.getGlobalTemplateFolder((ServerSession) Matchers.any())).thenReturn(new FolderObject());

        OXTemplateImpl template = (OXTemplateImpl) this.templateService.loadTemplate(this.userDefinedTemplateName, this.defaultTemplateName, this.session, true);

        Assert.assertEquals(templateContent.trim(), template.getTemplate().toString().trim());
    }

    @Test
    public void testLoadTemplate_loadFromFilesystem_returnCorrectTmplName() throws OXException, IOException {
        folder.create();
        folder.newFolder();

        Mockito.when(this.configService.getProperty(TemplateServiceImpl.PATH_PROPERTY)).thenReturn(folder.getRoot().getAbsolutePath());

        this.templateService = new TemplateServiceImpl(this.configService) {

            @Override
            protected boolean isAdminTemplate(String templateName) {
                return true;
            }

            @Override
            protected String loadFromFileSystem(final String defaultTemplateName) throws OXException {
                return templateContent;
            }
        };
        this.templateService.setOXFolderHelper(folders);
        this.templateService.setInfostoreHelper(infostore);
        Mockito.when(folders.getGlobalTemplateFolder((ServerSession) Matchers.any())).thenReturn(new FolderObject());

        OXTemplateImpl template = (OXTemplateImpl) this.templateService.loadTemplate(this.userDefinedTemplateName, this.defaultTemplateName, this.session, true);

        Assert.assertEquals(this.userDefinedTemplateName, template.getTemplate().getName().toString());
    }
}
