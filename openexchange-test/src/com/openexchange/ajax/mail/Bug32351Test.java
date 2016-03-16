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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link Bug32351Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug32351Test extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public Bug32351Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != fmid) {
            client.execute(new DeleteRequest(fmid, true).ignoreError());
        }
        super.tearDown();
    }

    public void testBug32355() throws OXException, IOException, JSONException {
        StringBuilder sb = new StringBuilder(8192);
        {
            InputStreamReader streamReader = null;
            try {
                streamReader = new InputStreamReader(new FileInputStream(new File(MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR), "mail010.eml")), "UTF-8");
                char[] buf = new char[2048];
                for (int read; (read = streamReader.read(buf, 0, 2048)) > 0;) {
                    sb.append(buf, 0, read);
                }
            } finally {
                Streams.close(streamReader);
            }
        }

        JSONArray json;
        {
            InputStream inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
            sb = null;
            final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
            final ImportMailResponse importResp = client.execute(importMailRequest);
            json = (JSONArray) importResp.getData();
            fmid = importResp.getIds();
        }

        {
            int err = 0;
            for (int i = 0; i < json.length(); i++) {
                JSONObject jo = json.getJSONObject(i);
                if (jo.has("Error")) {
                    err++;
                }
            }

            if (err != 0) {
                fail("Error importing mail");
            }
        }

        String mailID = json.getJSONObject(0).getString("id");
        String folderID = json.getJSONObject(0).getString("folder_id");

        // Concurrent delete attempts
        final String[][] fmid = this.fmid;
        final AJAXClient client = this.client;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(2);

        final AtomicReference<Exception> exc1 = new AtomicReference<Exception>();
        final Runnable deleteTask1 = new Runnable() {

            @Override
            public void run() {
                try {
                    startLatch.await();
                    client.execute(new DeleteRequest(fmid, false));

                    System.out.println("Thread #1 performed deletion");

                } catch (Exception e) {
                    exc1.set(e);
                } finally {
                    endLatch.countDown();
                }
            }
        };
        new Thread(deleteTask1).start();

        final AtomicReference<Exception> exc2 = new AtomicReference<Exception>();
        final Runnable deleteTask2 = new Runnable() {

            @Override
            public void run() {
                try {
                    startLatch.await();
                    client.execute(new DeleteRequest(fmid, false));

                    System.out.println("Thread #2 performed deletion");

                } catch (Exception e) {
                    exc2.set(e);
                } finally {
                    endLatch.countDown();
                }
            }
        };
        new Thread(deleteTask2).start();

        // Release threads
        startLatch.countDown();

        // Await termination
        try {
            endLatch.await();

            assertNull("Unexpected error.", exc1.get());
            assertNull("Unexpected error.", exc2.get());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

}
