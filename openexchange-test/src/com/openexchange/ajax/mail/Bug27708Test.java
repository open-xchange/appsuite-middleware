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

package com.openexchange.ajax.mail;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.tools.arrays.Arrays;

/**
 * Verifies that mixing mail content when sending an email did not occur anymore.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug27708Test extends AbstractMailTest {

    private static final int NUM_THREADS = 4;

    private AJAXClient[] clients;
    private String recipient;
    private String[] identifier;
    private List<String[]>[] sentMails;

    public Bug27708Test(final String name) {
        super(name);
    }

    @BeforeClass
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clients = new AJAXClient[NUM_THREADS];
        clients[0] = getClient();
        clients[1] = new AJAXClient(User.User2);
        clients[2] = new AJAXClient(User.User3);
        clients[3] = new AJAXClient(User.User4);
        recipient = getClient().getValues().getSendAddress();
        // Unique identifier for all threads, to be able to detect own and foreign content in mail body.
        identifier = new String[NUM_THREADS];
        for (int i = 0; i < identifier.length; i++) {
            identifier[i] = UUID.randomUUID().toString();
        }
        sentMails = new List[NUM_THREADS];
        for (int i = 0; i < sentMails.length; i++) {
            sentMails[i] = new LinkedList<String[]>();
        }
    }

    @AfterClass
    @Override
    protected void tearDown() throws Exception {
        // Delete sent mails.
        for (int i = 0; i < clients.length; i++) {
            clients[i].execute(new DeleteRequest(sentMails[i].toArray(new String[sentMails[i].size()][]), true));
        }
        // Delete received mails.
        String inboxFolder = getClient().getValues().getInboxFolder();
        AllRequest request = new AllRequest(inboxFolder, new int[] { 600 }, -1, null, true);
        AllResponse response = getClient().execute(request);
        final String[][] folderAndIDs = new String[response.size()][2];
        for (int i = 0; i < response.size(); i++) {
            folderAndIDs[i] = new String[] { inboxFolder, (String) response.getValue(i, 600) };
        }
        getClient().execute(new DeleteRequest(folderAndIDs, true));
        // Logout clients.
        if (null != clients) {
            for (AJAXClient client : clients) {
                if (null != client) client.logout();
            }
            clients = null;
        }
        super.tearDown();
    }

    @Test
    public void testBug27708() throws Exception {
        Thread[] threads = new Thread[clients.length];
        MailSender[] senders = new MailSender[clients.length];
        for (int i = 0; i < clients.length; i++) {
            senders[i] = new MailSender(clients[i], recipient, identifier[i], Arrays.remove(identifier, identifier[i]), sentMails[i]);
            threads[i] = new Thread(senders[i]);
            threads[i].start();
        }
        // Wait until at least 3 died. The fourth will not die because no other can interfere its mail bodies.
        int running = 0;
        do {
            running = 0;
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].isAlive()) {
                    running++;
                }
            }
            Thread.sleep(100);
        } while (running > 1);
        // If 3 died, stop the fourth.
        for (MailSender sender : senders) {
            sender.stop();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        // Write full output of mails of all failed threads, if some failed.
        StringBuilder sb = new StringBuilder();
        for (MailSender sender : senders) {
            Throwable t = sender.getThrowable();
            if (null != t) {
                sb.append(t.getMessage());
                sb.append('\n');
            }
        }
        Assert.assertTrue(sb.toString(), 0 == sb.length());
    }

    private static final class MailSender implements Runnable {

        private static final boolean DRAFT = false;

        private final AJAXClient client;
        private final String recipient;
        private final String identifier;
        private final String[] others;
        private final List<String[]> mails;

        private boolean running = true;
        private Throwable throwable;


        MailSender(AJAXClient client, String recipient, String identifier, String[] others, List<String[]> mails) {
            super();
            this.recipient = recipient;
            this.identifier = identifier;
            this.client = client;
            this.others = others;
            this.mails = mails;
        }

        public void stop() {
            running = false;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void run() {
            try {
                for (int j = 0; j < 1000 && running; j++) {
                    JSONObject json = createEMail(client, recipient, "Test Bug 27708", "TEXT/HTML", "<html><body style=\\u0022\\u0022><div>" + identifier + "</div></body></html>");
                    if (DRAFT) {
                        json.put(MailJSONField.FLAGS.getKey(), MailMessage.FLAG_DRAFT);
                    }
                    SendRequest sendRequest = new SendRequest(json.toString());
                    SendResponse response = client.execute(sendRequest);
                    String[] folderAndID = response.getFolderAndID();
                    mails.add(folderAndID);
                    GetResponse getResponse = client.execute(new GetRequest(folderAndID[0], folderAndID[1]));
                    JSONArray attachments = getResponse.getAttachments();
                    JSONObject tmp = attachments.getJSONObject(0);
                    String body = tmp.getString(MailJSONField.CONTENT.getKey());
                    Assert.assertTrue("Sent mail does not contain senders mail body.\n" + body, body.contains(identifier));
                    for (String other : others) {
                        Assert.assertFalse("Sent mail does contain others mail body.\n" + body, body.contains(other));
                    }
                }
            } catch (Exception e) {
                throwable = e;
            } catch (AssertionError e) {
                throwable = e;
            }
        }
    }
}
