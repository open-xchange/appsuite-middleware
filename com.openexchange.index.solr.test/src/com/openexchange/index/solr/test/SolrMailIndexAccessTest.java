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

package com.openexchange.index.solr.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;

/**
 * {@link SolrMailIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailIndexAccessTest extends AbstractSolrIndexAccessTest {

    public SolrMailIndexAccessTest() {
        super();
    }

    @Before
    public void setUpTest() throws OXException {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        managementService.unlockIndex(context.getId(), user.getId(), Types.EMAIL);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.ALL_REQUEST)
                                    .build();
        indexAccess.deleteByQuery(params);
    }

    @Test
    public void testIndexLock() throws Exception {
        try {
            managementService.lockIndex(context.getId(), user.getId(), Types.EMAIL);
            assertTrue("Index was not locked.", managementService.isLocked(context.getId(), user.getId(), Types.EMAIL));

            try {
                indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
            } catch (OXException e) {
                assertTrue("Wrong exception.", IndexExceptionCodes.INDEX_LOCKED.equals(e));
            }

            managementService.unlockIndex(context.getId(), user.getId(), Types.EMAIL);
            assertFalse("Index was locked.", managementService.isLocked(context.getId(), user.getId(), Types.EMAIL));

            IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
            managementService.lockIndex(context.getId(), user.getId(), Types.EMAIL);
            assertTrue("Index was not locked.", managementService.isLocked(context.getId(), user.getId(), Types.EMAIL));
            MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
            m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
            m1.setFolder("INBOX");
            m1.setAccountId(0);
            try {
                indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));
            } catch (OXException e) {
                assertTrue("Wrong exception.", IndexExceptionCodes.INDEX_LOCKED.equals(e));
            }

            managementService.unlockIndex(context.getId(), user.getId(), Types.EMAIL);
            assertFalse("Index was locked.", managementService.isLocked(context.getId(), user.getId(), Types.EMAIL));
            indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
            indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));
        } finally {
            managementService.unlockIndex(context.getId(), user.getId(), Types.EMAIL);
            assertFalse("Index was locked.", managementService.isLocked(context.getId(), user.getId(), Types.EMAIL));
        }
    }

    @Test
    public void testSimpleQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());

        MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
        m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m1.setFolder("INBOX");
        m1.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));

        MailMessage m2 = TestMails.toMailMessage(TestMails.MAIL1);
        m2.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m2.setFolder("INBOX/Somewhere");
        m2.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m2));

        MailMessage m3 = TestMails.toMailMessage(TestMails.MAIL1);
        m3.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m3.setFolder("INBOX");
        m3.setAccountId(1);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m3));

        QueryParameters q1 = buildSimpleQuery(m1.getAccountId(), m1.getFolder(), m1.getSubject());
        IndexResult<MailMessage> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 1);
        checkResult(m1, r1.getResults().get(0).getObject());

        QueryParameters q2 = buildSimpleQuery(m2.getAccountId(), m2.getFolder(), m2.getSubject());
        IndexResult<MailMessage> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 1);
        checkResult(m2, r2.getResults().get(0).getObject());

        QueryParameters q3 = buildSimpleQuery(m3.getAccountId(), m3.getFolder(), m3.getSubject());
        IndexResult<MailMessage> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong result size.", r3.getNumFound() == 1);
        checkResult(m3, r3.getResults().get(0).getObject());

        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.SIMPLE)
                                    .setSearchTerm(m1.getSubject())
                                    .build();
        IndexResult<MailMessage> result = indexAccess.query(params, null);
        assertTrue("Wrong result size.", result.getNumFound() == 3);
    }

    @Test
    public void testGetQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());

        MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
        m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m1.setFolder("INBOX");
        m1.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));

        MailMessage m2 = TestMails.toMailMessage(TestMails.MAIL1);
        m2.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m2.setFolder("INBOX/Somewhere");
        m2.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m2));

        MailMessage m3 = TestMails.toMailMessage(TestMails.MAIL1);
        m3.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m3.setFolder("INBOX");
        m3.setAccountId(1);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m3));

        Set<String> allIds = new HashSet<String>();
        allIds.add(MailUUID.newUUID(context.getId(), user.getId(), m1).toString());
        allIds.add(MailUUID.newUUID(context.getId(), user.getId(), m2).toString());
        allIds.add(MailUUID.newUUID(context.getId(), user.getId(), m3).toString());
        QueryParameters query = buildGetQuery(allIds);
        IndexResult<MailMessage> result = indexAccess.query(query, null);
        assertTrue("Wrong result size.", result.getNumFound() == 3);

        QueryParameters q1 = buildGetQuery(Collections.singleton(MailUUID.newUUID(context.getId(), user.getId(), m1).toString()));
        IndexResult<MailMessage> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 1);
        checkResult(m1, r1.getResults().get(0).getObject());

        QueryParameters q2 = buildGetQuery(Collections.singleton(MailUUID.newUUID(context.getId(), user.getId(), m2).toString()));
        IndexResult<MailMessage> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 1);
        checkResult(m2, r2.getResults().get(0).getObject());

        QueryParameters q3 = buildGetQuery(Collections.singleton(MailUUID.newUUID(context.getId(), user.getId(), m3).toString()));
        IndexResult<MailMessage> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong result size.", r3.getNumFound() == 1);
        checkResult(m3, r3.getResults().get(0).getObject());
    }

    @Test
    public void testAllQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());

        MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
        m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m1.setFolder("INBOX");
        m1.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));

        MailMessage m2 = TestMails.toMailMessage(TestMails.MAIL1);
        m2.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m2.setFolder("Apstiprināts \"ham");
        m2.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m2));

        MailMessage m3 = TestMails.toMailMessage(TestMails.MAIL1);
        m3.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m3.setFolder("INBOX");
        m3.setAccountId(1);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m3));

        QueryParameters allQuery = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.ALL_REQUEST)
                                    .build();
        IndexResult<MailMessage> result = indexAccess.query(allQuery, null);
        assertTrue("Wrong result size.", result.getNumFound() == 3);

        QueryParameters q1 = buildAllQuery(m1.getAccountId(), m1.getFolder());
        IndexResult<MailMessage> r1 = indexAccess.query(q1, null);
        assertTrue("Wrong result size.", r1.getNumFound() == 1);
        checkResult(m1, r1.getResults().get(0).getObject());

        QueryParameters q2 = buildAllQuery(m2.getAccountId(), m2.getFolder());
        IndexResult<MailMessage> r2 = indexAccess.query(q2, null);
        assertTrue("Wrong result size.", r2.getNumFound() == 1);
        checkResult(m2, r2.getResults().get(0).getObject());

        QueryParameters q3 = buildAllQuery(m3.getAccountId(), m3.getFolder());
        IndexResult<MailMessage> r3 = indexAccess.query(q3, null);
        assertTrue("Wrong result size.", r3.getNumFound() == 1);
        checkResult(m3, r3.getResults().get(0).getObject());

        AccountFolders accountFolders = new AccountFolders(String.valueOf(m1.getAccountId()));
        QueryParameters secondAllQuery = new QueryParameters.Builder()
                                            .setHandler(SearchHandlers.ALL_REQUEST)
                                            .setAccountFolders(Collections.singleton(accountFolders))
                                            .build();
        IndexResult<MailMessage> result2 = indexAccess.query(secondAllQuery, null);
        assertTrue("Wrong result size.", result2.getNumFound() == 2);
    }

    @Test
    public void testCustomQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
        MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
        m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m1.setFolder("INBOX");
        m1.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));

        MailMessage m2 = TestMails.toMailMessage(TestMails.MAIL1);
        m2.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m2.setFolder("INBOX");
        m2.setAccountId(0);
        m2.removeFrom();
        m2.addFrom(new InternetAddress("erwgtrg@asdeafer.cd"));
        m2.removeTo();
        m2.addTo(new InternetAddress("bncvvn@yxcvyvyxcv.kh"));
        m2.setSubject(UUID.randomUUID().toString());
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m2));

        SearchTerm<?> fromTerm = new FromTerm(m1.getFrom()[0].toUnicodeString());
        SearchTerm<?> toTerm = new ToTerm(m1.getTo()[0].toUnicodeString());
        SearchTerm<?> fromAndTo = new com.openexchange.mail.search.ANDTerm(fromTerm, toTerm);
        SearchTerm<?> subjectTerm = new SubjectTerm(m1.getSubject());
        SearchTerm<?> finalTerm = new ANDTerm(fromAndTo, subjectTerm);
        QueryParameters query = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.CUSTOM)
                                    .setSearchTerm(finalTerm)
                                    .build();

        IndexResult<MailMessage> result = indexAccess.query(query, null);
        assertTrue("Wrong result size.", result.getNumFound() == 1);
        checkResult(m1, result.getResults().get(0).getObject());

        SearchTerm<?> notTerm = new NOTTerm(new SubjectTerm(m1.getSubject()));
        QueryParameters query2 = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.CUSTOM)
                                    .setSearchTerm(notTerm)
                                    .build();
        IndexResult<MailMessage> result2 = indexAccess.query(query2, null);
        assertTrue("Wrong result size.", result2.getNumFound() == 1);
        checkResult(m2, result2.getResults().get(0).getObject());
    }

    @Test
    public void testDocumentCountWithFullAndEmptyResultSets() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());

        MailMessage m1 = TestMails.toMailMessage(TestMails.MAIL1);
        m1.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m1.setFolder("INBOX");
        m1.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m1));

        MailMessage m2 = TestMails.toMailMessage(TestMails.MAIL1);
        m2.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m2.setFolder("INBOX");
        m2.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m2));

        MailMessage m3 = TestMails.toMailMessage(TestMails.MAIL1);
        m3.setMailId(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        m3.setFolder("INBOX");
        m3.setAccountId(0);
        indexAccess.addDocument(new StandardIndexDocument<MailMessage>(m3));

        final Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
        Collections.addAll(fields, MailIndexField.ID, MailIndexField.ACCOUNT, MailIndexField.FULL_NAME);
        QueryParameters.Builder builder = new QueryParameters.Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setOffset(0)
            .setLength(0)
            .setAccountFolders(Collections.singleton(new AccountFolders("0", Collections.singleton("INBOX"))));
        QueryParameters countQuery = builder.build();
        IndexResult<MailMessage> result = indexAccess.query(countQuery, fields);
        assertTrue("Wrong result size.", result.getNumFound() == 3);
        assertTrue("Wrong number of documents.", result.getResults().size() == 0);

        builder.setLength(Integer.MAX_VALUE);
        QueryParameters retreiveQuery = builder.build();
        result = indexAccess.query(retreiveQuery, fields);
        assertTrue("Wrong result size.", result.getNumFound() == 3);
        assertTrue("Wrong number of documents.", result.getResults().size() == 3);
    }

    @Test
    public void testDeleteByQuery() throws Exception {
        assertNotNull("IndexFacadeService was null.", indexFacade);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, user.getId(), context.getId());
        List<IndexDocument<MailMessage>> mails = new ArrayList<IndexDocument<MailMessage>>();
        for (int i = 0; i < 2000; i++) {
            MailMessage m = TestMails.toMailMessage(TestMails.MAIL1);
            m.setMailId(String.valueOf(i));
            m.setFolder("INBOX");
            m.setAccountId(0);
            mails.add(new StandardIndexDocument<MailMessage>(m));
        }
        indexAccess.addDocuments(mails);

        final Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
        Collections.addAll(fields, MailIndexField.ID);
        QueryParameters allQuery = buildAllQuery(0, "INBOX");
        IndexResult<MailMessage> result = indexAccess.query(allQuery, fields);
        assertEquals("Wrong number of documents", 2000, result.getResults().size());

        indexAccess.deleteByQuery(allQuery);

        result = indexAccess.query(allQuery, fields);
        assertEquals("Wrong number of documents", 0, result.getResults().size());
    }

    private void checkResult(MailMessage expected, MailMessage actual) {
        assertEquals("Wrong Account", expected.getAccountId(), actual.getAccountId());
        assertEquals("Wrong Folder", expected.getFolder(), actual.getFolder());
        assertEquals("Wrong mail id", expected.getMailId(), actual.getMailId());
    }

    private QueryParameters buildAllQuery(int account, String folder) {
        AccountFolders accountFolders = new AccountFolders(String.valueOf(account), Collections.singleton(folder));
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.ALL_REQUEST)
                                    .setAccountFolders(Collections.singleton(accountFolders))
                                    .build();
        return params;
    }

    private QueryParameters buildGetQuery(Set<String> uuids) {
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.GET_REQUEST)
                                    .setIndexIds(uuids)
                                    .build();
        return params;
    }

    private QueryParameters buildSimpleQuery(int account, String folder, String searchTerm) {
        AccountFolders accountFolders = new AccountFolders(String.valueOf(account), Collections.singleton(folder));
        QueryParameters params = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.SIMPLE)
                                    .setSearchTerm(searchTerm)
                                    .setAccountFolders(Collections.singleton(accountFolders))
                                    .build();

        return params;
    }

}
