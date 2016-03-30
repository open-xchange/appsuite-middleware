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

package com.openexchange.publish.online.infostore;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.Publication;

/**
 * {@link InfostoreDocumentPublicationServiceTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@RunWith(PowerMockRunner.class)
public class InfostoreDocumentPublicationServiceTest extends TestCase {

    @InjectMocks
    private InfostoreDocumentPublicationService publicationService;

    @Mock
    private IDBasedFileAccessFactory fileAccessFactory = null;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public void testAddSecretBeforeCreate() throws OXException {
        Publication publication = new Publication();
        publicationService.beforeCreate(publication);

        Object secret1 = publication.getConfiguration().get("secret");

        publicationService.beforeCreate(publication);

        Object secret2 = publication.getConfiguration().get("secret");

        assertNotNull("No secret added", secret1);
        assertNotNull("No secret added second time around", secret2);

        assertFalse(secret1.equals(secret2));
    }

    public void testAddURLToOutgoingPublications() throws OXException {

        Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("secret", "theSecret");
        publicationService.modifyOutgoing(publication);

        String url = (String) publication.getConfiguration().get("url");

        assertNotNull("No URL added", url);
        assertTrue("URL should contain prefix", url.contains(InfostoreDocumentPublicationService.PREFIX));
        assertTrue("URL should contain secret", url.contains("theSecret"));
        assertTrue("URL should contain cid", url.contains("1337"));

    }

    public void testRemoveSecretFromOutgoingPublications() throws OXException {

        Publication publication = new Publication();
        publication.setContext(new SimContext(1337));
        publication.getConfiguration().put("secret", "theSecret");
        publicationService.modifyOutgoing(publication);

        assertFalse("Secret still set", publication.getConfiguration().containsKey("secret"));

    }

    public void testGenerateURL() throws OXException {
        final Publication publication = new Publication();
        final SimContext simContext = new SimContext(1337);
        publication.setContext(simContext);
        publication.getConfiguration().put("secret", "theSecret");

        publicationService = new InfostoreDocumentPublicationService(this.fileAccessFactory) {

            @Override
            public Publication getPublication(Context ctx, String secret) throws OXException {
                if (simContext.getContextId() == 1337 && secret.equalsIgnoreCase("theSecret")) {
                    return publication;
                }
                return null;
            }
        };

        publicationService.modifyOutgoing(publication);

        assertNotNull(publication.getConfiguration().get("url"));
        assertEquals("/publications/documents/1337/theSecret", publication.getConfiguration().get("url"));

        final Publication comparePublication = publicationService.resolveUrl(simContext, "/publications/documents/1337/theSecret");
        assertNotNull("Returned publication of resolveUrl is null!", comparePublication);

        assertNotNull(comparePublication.getConfiguration().get("url"));
        assertEquals("/publications/documents/1337/theSecret", comparePublication.getConfiguration().get("url"));

        assertEqualPublication(publication, comparePublication);
    }

    public void assertEqualPublication(final Publication publication, final Publication other) {
        assertEquals(publication.getContext().getContextId(), other.getContext().getContextId());
        assertEquals(publication.getId(), other.getId());
    }
}
