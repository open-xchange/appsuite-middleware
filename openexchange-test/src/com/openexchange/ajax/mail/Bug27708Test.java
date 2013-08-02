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

package com.openexchange.ajax.mail;

import java.util.concurrent.CountDownLatch;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;


/**
 * {@link Bug27708Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Bug27708Test extends AbstractMailTest {

    private AJAXClient client2;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Bug27708Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    protected void tearDown() throws Exception {
        AJAXClient thisClient2 = client2;
        if (null != thisClient2) {
            thisClient2.logout();
        }
        super.tearDown();
    }

    public void testBug27708() throws Exception {

        for (int i = 0; i < 100; i++) {
            final CountDownLatch startUpLatch = new CountDownLatch(1);
            final CountDownLatch finishedLatch = new CountDownLatch(2);
            {
                final AJAXClient client = this.client;
                final Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final JSONObject jMail =
                                new JSONObject(
                                    "{\"from\":\"Thorben Betten <thorben.betten@premium>\"," + "\"to\":\"Thorben Betten <thorben.betten@premium>\"," + "\"cc\":\"\",\"bcc\":\"\"," + "\"subject\":\"AAAA\"," + "\"priority\":\"3\"," + "\"attachments\":" + "[" + "{\"content_type\":\"ALTERNATIVE\",\"content\":\"<html><body style=\\u0022\\u0022><div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAAAAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div>\\u000a<div>AAAA AAAA AAAA AAAA AAAA</div></body></html>\"}" + "]," + "\"datasources\":[]}");

                            final String mailObject_25kb = jMail.toString();
                            final SendRequest sendRequest = new SendRequest(mailObject_25kb);
                            AJAXSession session = client.getSession();

                            startUpLatch.await();

                            final SendResponse response = Executor.execute(session, sendRequest);
                            String[] folderAndID = response.getFolderAndID();

                            final GetResponse getResponse =
                                Executor.execute(session, new GetRequest(folderAndID[0], folderAndID[1], true, true));
                            final JSONObject obj = (JSONObject) getResponse.getData();

                            assertFalse("Contains unexpected content", obj.hasAndNotNull("error"));

                            String retrievedContent = obj.getJSONArray("attachments").getJSONObject(0).getString("content");

                            assertTrue("Contains unexpected content", retrievedContent.indexOf("BBBB") < 0);

                        } catch (Exception e) {
                            e.printStackTrace();
                            fail(e.getMessage());
                        } finally {
                            finishedLatch.countDown();
                        }
                    }
                };
                new Thread(r1).start();
            }
            {
                final AJAXClient client = this.client2;
                final Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final JSONObject jMail =
                                new JSONObject(
                                    "{\"from\":\"Thorben Betten <thorben.betten@premium>\"," + "\"to\":\"Thorben Betten <thorben.betten@premium>\"," + "\"cc\":\"\",\"bcc\":\"\"," + "\"subject\":\"BBBB\"," + "\"priority\":\"3\"," + "\"attachments\":" + "[" + "{\"content_type\":\"ALTERNATIVE\",\"content\":\"<html><body style=\\u0022\\u0022><div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBBBBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div>\\u000a<div>BBBB BBBB BBBB BBBB BBBB</div></body></html>\"}" + "]," + "\"datasources\":[]}");

                            jMail.put(MailJSONField.FLAGS.getKey(), MailMessage.FLAG_DRAFT);

                            final String mailObject_25kb = jMail.toString();
                            final SendRequest sendRequest = new SendRequest(mailObject_25kb);
                            AJAXSession session = client.getSession();

                            startUpLatch.await();

                            final SendResponse response = Executor.execute(session, sendRequest);
                            String[] folderAndID = response.getFolderAndID();

                            final GetResponse getResponse =
                                Executor.execute(session, new GetRequest(folderAndID[0], folderAndID[1], true, true));
                            final JSONObject obj = (JSONObject) getResponse.getData();

                            assertFalse("Contains unexpected content", obj.hasAndNotNull("error"));

                            String retrievedContent = obj.getJSONArray("attachments").getJSONObject(0).getString("content");

                            assertTrue("Contains unexpected content", retrievedContent.indexOf("AAAA") < 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail(e.getMessage());
                        } finally {
                            finishedLatch.countDown();
                        }
                    }
                };
                new Thread(r2).start();
            }
            // Await initialization
            Thread.sleep(2000);
            // Start threads
            startUpLatch.countDown();
            // Await completion
            finishedLatch.await();
        }
    }

}
