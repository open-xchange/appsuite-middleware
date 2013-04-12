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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.publish.microformats;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.context.SimContextService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.TemplatingHelper;

/**
 * {@link OXMFPublicationServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXMFPublicationServiceTest extends TestCase {

    private OXMFPublicationService publicationService;

    private final Publication oldPublication = new Publication();

    @Override
    public void setUp() throws Exception {
        publicationService = new OXMFPublicationService() {

            @Override
            public Publication loadInternally(final Context ctx, final int publicationId) throws OXException {
                return oldPublication;
            }
            @Override
            public Publication getPublication(final Context ctx, final String site) throws OXException {
                if(site.equals("existingSite")) {
                    final Publication publication = new Publication();
                    publication.setId(23);
                    publication.getConfiguration().put("siteName", "existingSite");
                    publication.setContext(new SimContext(1337));
                    return publication;
                }
                return null;
            }
        };
        publicationService.setRootURL("/publications/bananas");
        publicationService.setFolderType("bananas");
        publicationService.setTargetId("com.openexchange.publish.microformats.contacts.online");
        publicationService.setTargetDisplayName("Banana Publications!");

        publicationService.setTemplateService(new FindEverythingTemplateService());

        oldPublication.setTarget(publicationService.getTarget());

    }

    public void testModule() throws OXException {
        final PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("Module differs", "bananas", target.getModule());
    }

    public void testId() throws OXException {
        final PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("id differs", "com.openexchange.publish.microformats.contacts.online", target.getId());
    }

    public void testDisplayNameOfTarget() throws OXException {
        final PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("Display Name differs", "Banana Publications!", target.getDisplayName());
    }

    public void testModifyOutgoingShouldSetDisplayNameToSiteName() throws OXException{
        final Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName","expected");
        publicationService.modifyOutgoing(publication);
        assertEquals("Should be the siteName", "expected", publication.getDisplayName());
    }


    public void testFields() throws OXException {
        final PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);

        final DynamicFormDescription description = target.getFormDescription();


        assertNotNull(description.getField("siteName"));
        assertNotNull(description.getField("protected"));

        assertNotNull(description);
    }

    public void testAddSecretToConfigIfProtected() throws OXException {
        final Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeCreate(publication);

        assertSecret(publication);
    }

    public void testDontAddSecretToConfigIfNotProtected() throws OXException {
        final Publication publication = new Publication();
        publication.getConfiguration().put("protected", false);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeCreate(publication);

        assertNoSecret(publication);
    }

    public void testAddSecretToConfigWhenProtectionChangesToOn() throws OXException {
        oldPublication.getConfiguration().put("protected", false);

        final Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecret(publication);
    }

    public void testGenerateNewSecretOnlyIfProtectionStatusChanged() throws OXException {
        oldPublication.getConfiguration().put("protected", true);
        oldPublication.getConfiguration().put("secret", "172812951aefbda");

        final Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecret(publication);

        assertEquals("172812951aefbda", publication.getConfiguration().get("secret"));

    }

    public void testRemoveSecretFromConfigWhenProtectionChangesToOff() throws OXException {
        final Publication publication = new Publication();
        publication.getConfiguration().put("protected", false);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecretRemoved(publication);
    }

    public void testRemoveSecretFromConfigExternally() throws OXException {
        final Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");
        publication.getConfiguration().put("secret", "geheim");

        publicationService.modifyOutgoing(publication);

        assertTrue(publication.getConfiguration().get("secret") == null);
    }

    public void testGenerateURL() throws OXException {
        final Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName", "public");

        publicationService.modifyOutgoing(publication);

        assertNotNull(publication.getConfiguration().get("url"));
        assertEquals("/publications/bananas/1337/public", publication.getConfiguration().get("url"));

        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("secret", "abedfea108275720123abde");

        publicationService.modifyOutgoing(publication);

        assertEquals("/publications/bananas/1337/public?secret=abedfea108275720123abde", publication.getConfiguration().get("url"));

    }

    public void testResolveUrl() throws OXException {
        
        
        
        final Publication publication = new Publication();
        publication.setId(23);
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName", "existingSite");

        publicationService.modifyOutgoing(publication);

        assertNotNull(publication.getConfiguration().get("url"));
        assertEquals("/publications/bananas/1337/existingSite", publication.getConfiguration().get("url"));

        final SimContextService service = new SimContextService() {
            @Override
            public Context getContext(int contextId) throws OXException {
                return new SimContext(contextId);
            }
        };
        
        final Publication comparePublication = publicationService.resolveUrl(service, "/publications/bananas/1337/existingSite");
        assertNotNull("Returned publication of resolveUrl is null!",comparePublication);
        
        publicationService.modifyOutgoing(comparePublication);

        assertNotNull(comparePublication.getConfiguration().get("url"));
        assertEquals("/publications/bananas/1337/existingSite", comparePublication.getConfiguration().get("url"));
        
        assertEqualPublication(publication,comparePublication);
    }

    public void testUniqueSite() throws OXException {
        final Publication publication = new Publication();
        publication.setId(42);
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName", "existingSite");

        try {
            publicationService.modifyIncoming(publication);
            fail("Could create double site");
        } catch (final OXException x) {
            // Hooray
        }

    }

    public void testSubmittingUnchangingSiteNameIsAccepted() throws OXException {
        final Publication publication = new Publication();
        publication.setId(23);
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName", "existingSite");
        publicationService.modifyIncoming(publication);
    }

    public void testNormalizedSiteName() throws OXException {
        final Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("siteName","path/with//tooMany//slashes/");
        publicationService.modifyIncoming(publication);
        assertEquals("path/with/tooMany/slashes", publication.getConfiguration().get("siteName"));
    }

    public void assertSecret(final Publication publication) {
        assertTrue("Secret was unset!", publication.getConfiguration().containsKey("secret"));
    }

    public void assertNoSecret(final Publication publication) {
        assertFalse("Secret was set!", publication.getConfiguration().containsKey("secret"));
    }

    public void assertSecretRemoved(final Publication publication) {
        assertTrue("Secret was unset!", publication.getConfiguration().containsKey("secret"));
        assertTrue("Secret was not null explicitely!", publication.getConfiguration().get("secret") == null);
    }

    public void assertEqualPublication(final Publication publication, final Publication other){
        assertEquals(publication.getContext().getContextId(),other.getContext().getContextId());
        assertEquals(publication.getId(), other.getId());
    }

    private static final class FindEverythingTemplateService implements TemplateService {

        @Override
        public OXTemplate loadTemplate(final String templateName) {
            return null;
        }

        @Override
        public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session session) {
            return null;
        }

        @Override
        public List<String> getBasicTemplateNames(final String...filter) {
            return new ArrayList<String>(0);
        }

        @Override
        public List<String> getTemplateNames(final Session session, final String... filter) {
            return null;
        }

        @Override
        public OXTemplate loadTemplate(final String templateName, final OXTemplateExceptionHandler exceptionHandler) {
            return null;
        }

        @Override
        public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session session, final OXTemplateExceptionHandler exceptionHandler) {
            return null;
        }

        @Override
        public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session session, final boolean createCopy) {
            return null;
        }

        @Override
        public TemplatingHelper createHelper(final Object rootObject, final Session session, boolean createCopy) {
            return null;
        }
    }
}
