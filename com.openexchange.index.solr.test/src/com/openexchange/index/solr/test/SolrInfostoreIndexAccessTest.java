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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.index.solr.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.index.InfostoreUUID;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.StandardIndexDocument;


/**
 * {@link SolrInfostoreIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrInfostoreIndexAccessTest extends AbstractSolrIndexAccessTest {

    @Before
    public void setUpTest() throws OXException {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<DocumentMetadata> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, user.getId(), context.getId());
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.ALL_REQUEST)
                                    .build();
        indexAccess.deleteByQuery(params);
    }

    @Test
    public void testSimpleQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<DocumentMetadata> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, user.getId(), context.getId());

        DocumentMetadata d1 = createDocument(1, 1L, "First Document", "This is the first document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d1));

        DocumentMetadata d2 = createDocument(2, 1L, "Second Document", "This is the second document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d2));

        DocumentMetadata d3 = createDocument(3, 2L, "Third Document", "This is the third document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d3));

        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("First")
            .build();
        IndexResult<DocumentMetadata> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 1);
        checkDocuments(d1, r1.getResults().get(0).getObject());

        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, Collections.singleton("1"));
        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("Document")
            .setAccountFolders(Collections.singleton(af1))
            .build();
        IndexResult<DocumentMetadata> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 2);
    }

    @Test
    public void testGetQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<DocumentMetadata> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, user.getId(), context.getId());

        DocumentMetadata d1 = createDocument(1, 1L, "First Document", "This is the first document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d1));

        DocumentMetadata d2 = createDocument(2, 1L, "Second Document", "This is the second document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d2));

        DocumentMetadata d3 = createDocument(3, 2L, "Third Document", "This is the third document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d3));

        Set<String> allIds = new HashSet<String>();
        allIds.add(InfostoreUUID.newUUID(context.getId(), user.getId(), d1).toString());
        allIds.add(InfostoreUUID.newUUID(context.getId(), user.getId(), d2).toString());
        allIds.add(InfostoreUUID.newUUID(context.getId(), user.getId(), d3).toString());
        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.GET_REQUEST)
            .setIndexIds(allIds)
            .build();
        IndexResult<DocumentMetadata> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 3);

        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, Collections.singleton("1"));
        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.GET_REQUEST)
            .setAccountFolders(Collections.singleton(af1))
            .setIndexIds(allIds)
            .build();
        IndexResult<DocumentMetadata> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 2);
    }

    @Test
    public void testAllQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<DocumentMetadata> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, user.getId(), context.getId());

        DocumentMetadata d1 = createDocument(1, 1L, "First Document", "This is the first document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d1));

        DocumentMetadata d2 = createDocument(2, 1L, "Second Document", "This is the second document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d2));

        DocumentMetadata d3 = createDocument(3, 2L, "Third Document", "This is the third document");
        indexAccess.addDocument(new StandardIndexDocument<DocumentMetadata>(d3));

        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .build();
        IndexResult<DocumentMetadata> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 3);

        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, Collections.singleton("1"));
        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setAccountFolders(Collections.singleton(af1))
            .build();
        IndexResult<DocumentMetadata> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 2);
    }

    @Test
    public void testCustomQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<DocumentMetadata> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, user.getId(), context.getId());

        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.CUSTOM)
            .setSearchTerm(new Object())
            .build();
        try {
            indexAccess.query(q1, null);
        } catch (Exception e) {
            // TODO: Expect correct exception
        }
    }

    private void checkDocuments(DocumentMetadata expected, DocumentMetadata actual) {
        assertEquals("Wrong folder", expected.getFolderId(), actual.getFolderId());
        assertEquals("Wrong id", expected.getId(), actual.getId());
    }

    private DocumentMetadata createDocument(int id, long folderId, String title, String description) {
        DocumentMetadata d1 = new TestDocumentMetadata();
        d1.setId(id);
        d1.setFolderId(folderId);
        d1.setIsCurrentVersion(true);
        d1.setVersion(1);
        d1.setNumberOfVersions(1);
        d1.setCreatedBy(0);
        d1.setCreationDate(new Date());
        d1.setLastModified(d1.getCreationDate());
        d1.setModifiedBy(d1.getCreatedBy());
        d1.setTitle(title);
        d1.setDescription(description);

        return d1;
    }

}
