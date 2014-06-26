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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.groupware.attach.index.ORTerm;
import com.openexchange.groupware.attach.index.ObjectIdTerm;
import com.openexchange.groupware.attach.index.SearchTerm;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.StandardIndexDocument;


/**
 * {@link SolrAttachmentIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentIndexAccessTest extends AbstractSolrIndexAccessTest {

    @Before
    public void setUpTest() throws OXException {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<Attachment> indexAccess = indexFacade.acquireIndexAccess(Types.ATTACHMENT, user.getId(), context.getId());
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.ALL_REQUEST)
                                    .build();
        indexAccess.deleteByQuery(params);
    }

    @Test
    public void testSimpleQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<Attachment> indexAccess = indexFacade.acquireIndexAccess(Types.ATTACHMENT, user.getId(), context.getId());

        Attachment a1 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "1",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample1.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a1));

        Attachment a2 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample2.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a2));

        Attachment a3 = createAttachment(
            Types.EMAIL,
            "acc2",
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a3));

        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("sample")
            .build();
        IndexResult<Attachment> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong size.", r1.getNumFound() == 3);

        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("sample")
            .setModule(Types.EMAIL)
            .build();
        IndexResult<Attachment> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong size.", r2.getNumFound() == 1);
        checkAttachments(a3, r2.getResults().get(0).getObject());

        Set<String> folders = new HashSet<String>();
        folders.add("1");
        folders.add("2");
        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, folders);
        QueryParameters q3 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("sample")
            .setAccountFolders(Collections.singleton(af1))
            .build();
        IndexResult<Attachment> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong size.", r3.getNumFound() == 2);

        Set<AccountFolders> afSet = new HashSet<AccountFolders>();
        afSet.add(af1);
        afSet.add(new AccountFolders(a3.getAccount()));
        QueryParameters q4 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.SIMPLE)
            .setSearchTerm("sample")
            .setAccountFolders(afSet)
            .build();
        IndexResult<Attachment> r4 = indexAccess.query(q4, null);
        assertTrue("Wrong size.", r4.getNumFound() == 3);
    }

    @Test
    public void testGetQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<Attachment> indexAccess = indexFacade.acquireIndexAccess(Types.ATTACHMENT, user.getId(), context.getId());

        Attachment a1 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "1",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample1.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a1));

        Attachment a2 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample2.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a2));

        Attachment a3 = createAttachment(
            Types.EMAIL,
            "acc2",
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a3));

        Set<String> allIds = new HashSet<String>();
        allIds.add(AttachmentUUID.newUUID(context.getId(), user.getId(), a1).toString());
        allIds.add(AttachmentUUID.newUUID(context.getId(), user.getId(), a2).toString());
        allIds.add(AttachmentUUID.newUUID(context.getId(), user.getId(), a3).toString());
        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.GET_REQUEST)
            .setIndexIds(allIds)
            .build();
        IndexResult<Attachment> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong size.", r1.getNumFound() == 3);

        Set<String> folders = new HashSet<String>();
        folders.add("1");
        folders.add("2");
        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, folders);
        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.GET_REQUEST)
            .setAccountFolders(Collections.singleton(af1))
            .setIndexIds(allIds)
            .build();
        IndexResult<Attachment> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong size.", r2.getNumFound() == 2);

        QueryParameters q3 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.GET_REQUEST)
            .setIndexIds(allIds)
            .setModule(Types.EMAIL)
            .build();
        IndexResult<Attachment> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong size.", r3.getNumFound() == 1);
        checkAttachments(a3, r3.getResults().get(0).getObject());
    }

    @Test
    public void testAllQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<Attachment> indexAccess = indexFacade.acquireIndexAccess(Types.ATTACHMENT, user.getId(), context.getId());

        Attachment a1 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "1",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample1.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a1));

        Attachment a2 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample2.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a2));

        Attachment a3 = createAttachment(
            Types.EMAIL,
            "acc2",
            "2",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a3));

        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .build();
        IndexResult<Attachment> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong size.", r1.getNumFound() == 3);

        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT);
        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setAccountFolders(Collections.singleton(af1))
            .build();
        IndexResult<Attachment> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong size.", r2.getNumFound() == 2);

        QueryParameters q3 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setModule(Types.EMAIL)
            .build();
        IndexResult<Attachment> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong size.", r3.getNumFound() == 1);
        checkAttachments(a3, r3.getResults().get(0).getObject());
    }

    @Test
    public void testCustomQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<Attachment> indexAccess = indexFacade.acquireIndexAccess(Types.ATTACHMENT, user.getId(), context.getId());

        Attachment a1 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "1",
            "1",
            String.valueOf(new Random().nextInt()),
            "sample1.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a1));

        Attachment a2 = createAttachment(
            Types.INFOSTORE,
            IndexConstants.DEFAULT_ACCOUNT,
            "2",
            "2",
            String.valueOf(new Random().nextInt()),
            "sample2.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a2));

        Attachment a3 = createAttachment(
            Types.EMAIL,
            "acc2",
            "2",
            "3",
            String.valueOf(new Random().nextInt()),
            "sample.txt");
        indexAccess.addDocument(new StandardIndexDocument<Attachment>(a3));

        SearchTerm<?> orTerm = new ORTerm(new SearchTerm<?>[] { new ObjectIdTerm("1"), new ObjectIdTerm("2"), new ObjectIdTerm("3") });
        QueryParameters q1 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.CUSTOM)
            .setSearchTerm(orTerm)
            .build();
        IndexResult<Attachment> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong size.", r1.getNumFound() == 3);

        QueryParameters q2 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.CUSTOM)
            .setSearchTerm(orTerm)
            .setModule(Types.EMAIL)
            .build();
        IndexResult<Attachment> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong size.", r2.getNumFound() == 1);
        checkAttachments(a3, r2.getResults().get(0).getObject());

        Set<String> folders = new HashSet<String>();
        folders.add("1");
        folders.add("2");
        AccountFolders af1 = new AccountFolders(IndexConstants.DEFAULT_ACCOUNT, folders);
        QueryParameters q3 = new QueryParameters.Builder()
            .setHandler(SearchHandlers.CUSTOM)
            .setAccountFolders(Collections.singleton(af1))
            .setSearchTerm(orTerm)
            .build();
        IndexResult<Attachment> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong size.", r3.getNumFound() == 2);
    }

    private void checkAttachments(Attachment expected, Attachment actual) {
        assertEquals("Wrong module.", expected.getModule(), actual.getModule());
        assertEquals("Wrong account.", expected.getAccount(), actual.getAccount());
        assertEquals("Wrong folder.", expected.getFolder(), actual.getFolder());
        assertEquals("Wrong object id.", expected.getObjectId(), actual.getObjectId());
        assertEquals("Wrong attachment id.", expected.getAttachmentId(), actual.getAttachmentId());
    }

    private Attachment createAttachment(int module, String account, String folder, String objectId, String attachmentId, String fileName) {
        Attachment attachment = new Attachment();
        attachment.setModule(module);
        attachment.setAccount(account);
        attachment.setFolder(folder);
        attachment.setObjectId(objectId);
        attachment.setAttachmentId(attachmentId);
        attachment.setFileName(fileName);
        attachment.setContent(TestDocuments.toInputStream(TestDocuments.DOC1));
        attachment.setFileSize(TestDocuments.DOC1.length);
        attachment.setMimeType("text/plain");

        return attachment;
    }

}
