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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link Bug32351Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug32351Test extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public Bug32351Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testBug32355() throws OXException, IOException, JSONException {
        StringBuilder sb = new StringBuilder(8192);
        {
            InputStreamReader streamReader = null;
            try {
                streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "mail010.eml")), "UTF-8");
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
            InputStream inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
            sb = null;
            final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
            final ImportMailResponse importResp = getClient().execute(importMailRequest);
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


        // Concurrent delete attempts
        final String[][] fmid = this.fmid;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(2);

        final AtomicReference<Exception> exc1 = new AtomicReference<Exception>();
        final AJAXClient client = getClient();
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
