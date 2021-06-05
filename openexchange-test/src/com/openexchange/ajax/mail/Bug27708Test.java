/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.mail;

import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.test.common.test.TestClassConfig;
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

    public Bug27708Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clients = new AJAXClient[NUM_THREADS];
        clients[0] = getClient();
        clients[1] = testUser2.getAjaxClient();
        clients[2] = testContext.acquireUser().getAjaxClient();
        clients[3] = testContext.acquireUser().getAjaxClient();
        recipient = getClient().getValues().getSendAddress();
        // Unique identifier for all threads, to be able to detect own and foreign content in mail body.
        identifier = new String[NUM_THREADS];
        for (int i = 0; i < identifier.length; i++) {
            identifier[i] = UUID.randomUUID().toString();
        }
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().withUserPerContext(4).createAjaxClient().build();
    }

    @Test
    public void testBug27708() throws Exception {
        Thread[] threads = new Thread[clients.length];
        MailSender[] senders = new MailSender[clients.length];
        for (int i = 0; i < clients.length; i++) {
            senders[i] = new MailSender(clients[i], recipient, identifier[i], Arrays.remove(identifier, identifier[i]));
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

        private boolean running = true;
        private Throwable throwable;

        MailSender(AJAXClient client, String recipient, String identifier, String[] others) {
            super();
            this.recipient = recipient;
            this.identifier = identifier;
            this.client = client;
            this.others = others;
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
