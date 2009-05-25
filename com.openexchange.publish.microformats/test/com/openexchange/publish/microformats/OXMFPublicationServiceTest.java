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

package com.openexchange.publish.microformats;

import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationTarget;
import junit.framework.TestCase;

/**
 * {@link OXMFPublicationServiceTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXMFPublicationServiceTest extends TestCase {

    private OXMFPublicationService publicationService;

    private final Publication oldPublication = new Publication();

    public void setUp() {
        publicationService = new OXMFPublicationService() {

            @Override
            public Publication load(Context ctx, int publicationId) throws PublicationException {
                return oldPublication;
            }
        };
        publicationService.setRootURL("/publications/bananas");
        publicationService.setFolderType("bananas");
        publicationService.setTargetId("com.openexchange.publish.microformats.contacts.online");
        publicationService.setTargetDisplayName("Banana Publications!");
    }

    public void testModule() throws PublicationException {
        PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("Module differs", "folder:bananas", target.getModule());
    }
    
    public void testId() throws PublicationException {
        PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("id differs", "com.openexchange.publish.microformats.contacts.online", target.getId());
    }
    
    public void testDisplayName() throws PublicationException {
        PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        assertEquals("Display Name differs", "Banana Publications!", target.getDisplayName());
    }

    public void testFields() throws PublicationException {
        PublicationTarget target = publicationService.getTarget();

        assertNotNull("Target was null", target);
        
        DynamicFormDescription description = target.getFormDescription();
        
        
        assertNotNull(description.getField("siteName"));
        assertNotNull(description.getField("protected"));
        
        assertNotNull(description);
    }

    public void testAddSecretToConfigIfProtected() throws PublicationException {
        Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeCreate(publication);

        assertSecret(publication);
    }

    public void testDontAddSecretToConfigIfNotProtected() throws PublicationException {
        Publication publication = new Publication();
        publication.getConfiguration().put("protected", false);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeCreate(publication);

        assertNoSecret(publication);
    }

    public void testAddSecretToConfigWhenProtectionChangesToOn() throws PublicationException {
        oldPublication.getConfiguration().put("protected", false);

        Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecret(publication);
    }

    public void testGenerateNewSecretOnlyIfProtectionStatusChanged() throws PublicationException {
        oldPublication.getConfiguration().put("protected", true);
        oldPublication.getConfiguration().put("secret", "172812951aefbda");

        Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecret(publication);

        assertEquals("172812951aefbda", publication.getConfiguration().get("secret"));

    }

    public void testRemoveSecretFromConfigWhenProtectionChangesToOff() throws PublicationException {
        Publication publication = new Publication();
        publication.getConfiguration().put("protected", false);
        publication.getConfiguration().put("siteName", "public");

        publicationService.beforeUpdate(publication);

        assertSecretRemoved(publication);
    }

    public void testRemoveSecretFromConfigExternally() throws PublicationException {
        Publication publication = new Publication();
        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("siteName", "public");
        publication.getConfiguration().put("secret", "geheim");

        publicationService.modifyOutgoing(publication);

        assertTrue(publication.getConfiguration().get("secret") == null);
    }

    public void testGenerateURL() throws PublicationException {
        Publication publication = new Publication();
        publication.getConfiguration().put("siteName", "public");

        publicationService.modifyOutgoing(publication);

        assertNotNull(publication.getUrl());
        assertEquals("/publications/bananas/public", publication.getUrl());

        publication.getConfiguration().put("protected", true);
        publication.getConfiguration().put("secret", "abedfea108275720123abde");

        publicationService.modifyOutgoing(publication);

        assertEquals("/publications/bananas/public?secret=abedfea108275720123abde", publication.getUrl());

    }

    public void assertSecret(Publication publication) {
        assertTrue("Secret was unset!", publication.getConfiguration().containsKey("secret"));
    }

    public void assertNoSecret(Publication publication) {
        assertFalse("Secret was set!", publication.getConfiguration().containsKey("secret"));
    }

    public void assertSecretRemoved(Publication publication) {
        assertTrue("Secret was unset!", publication.getConfiguration().containsKey("secret"));
        assertTrue("Secret was not null explicitely!", publication.getConfiguration().get("secret") == null);
    }

}
