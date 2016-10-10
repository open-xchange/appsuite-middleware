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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.actions.MailReferenceResponse;
import com.openexchange.ajax.mail.actions.NewMailRequestWithUploads;
import com.openexchange.mail.MailJSONField;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * {@link MailPushTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class MailPushTest extends AbstractMailTest {

    private AJAXClient client2;

    /**
     * Initializes a new {@link MailPushTest}.
     * @param name
     */
    public MailPushTest(String name) {
        super(name);
    }

    @Override
    protected String getClientId() {
        return "open-xchange-appsuite";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSocketIO() throws Exception {
        String sessionId = client.getSession().getId();
        String hostname = client.getHostname();
        String protocol = client.getProtocol();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> ref = new AtomicReference<>();

        // Task for sending a new mail
        Runnable sendMail = new Runnable() {

            @Override
            public void run() {
                AJAXClient client2 = null;
                try {
                    client2 = new AJAXClient(User.User2);

                    String eml =
                        "    Date: Tue, 1 Jul 2014 17:37:28 +0200 (CEST)\n" +
                        "    From: from foo <from@example.tld>\n" +
                        "    Reply-To: rto foo <bar@example.tld>\n" +
                        "    To: to bar <to@example.tld>\n" +
                        "    Cc: cc bar <cc@example.tld>\n" +
                        "    Bcc: bcc bar <bcc@example.tld>\n" +
                        "    Message-ID: <11140339.1095068.1404229048881.JavaMail.www@wwinf8905>\n" +
                        "    Subject: plain text\n" +
                        "    MIME-Version: 1.0\n" +
                        "    Content-Type: text/plain; charset=UTF-8\n" +
                        "    Content-Transfer-Encoding: 7bit\n" +
                        "\n" +
                        "    plain text\n" +
                        "\n" +
                        "    A test signature";

                    JSONObject composedMail = createEMail(getSendAddress(), "Test mail", "text/plain", eml);
                    composedMail.put(MailJSONField.FROM.getKey(), getSendAddress(client2));
                    JSONObject mail = new JSONObject(composedMail);

                    NewMailRequestWithUploads sendRequest = new NewMailRequestWithUploads(mail);
                    MailReferenceResponse sendResponse = client2.execute(sendRequest);
                    assertNotNull(sendResponse);

                } catch (Exception e) {
                    ref.set(e);
                } finally {
                    latch.countDown();
                    if (null != client2) {
                        try {
                            client2.logout();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        // Establish Socket.IO connection and await "new mail" event
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.query = "session=" + sessionId;
        opts.transports = new String[] {"websocket"};

        int port = -1; // Set to "8009" for local testing
        String uri = protocol + "://" + hostname;
        if (port > 0) {
            uri = uri + ":" + port;
        }

        final Socket socket = IO.socket(uri, opts);
        try {
            socket.on("ox:mail:new", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    values.offer(args);
                    socket.disconnect();
                }

            });

            socket.connect();

            new Thread(sendMail, "SendNewMail").start();

            latch.await();

            Exception exception = ref.get();
            if (null != exception) {
                fail(exception.getMessage());
            }

            int timeout = 10;
            Object[] args = (Object[]) values.poll(timeout, TimeUnit.SECONDS);

            assertNotNull("Received no \"ox:mail:new\" event within " + timeout + " seconds.", args);
            assertNotNull("No data found in \"ox:mail:new\" event,", args[0]);

            JSONObject jArg = (JSONObject) args[0];
            assertTrue("Missing \"folder\" field in JSON data of \"ox:mail:new\" event", jArg.hasAndNotNull("folder"));
            assertEquals("Unexpected folder identifier", "default0/INBOX", jArg.getString("folder"));
        } finally {
            socket.close();
        }
    }

}
