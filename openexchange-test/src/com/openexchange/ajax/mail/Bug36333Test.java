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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.AttachmentRequest;
import com.openexchange.ajax.mail.actions.AttachmentResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.DeleteResponse;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link Bug36333Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug36333Test extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public Bug36333Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testBug36333() throws OXException, IOException, JSONException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug36333.eml")), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = streamReader.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        streamReader.close();
        InputStream inputStream = new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
        final ImportMailResponse importResp = getClient().execute(importMailRequest);
        JSONArray json = (JSONArray) importResp.getData();
        fmid = importResp.getIds();

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

        String mailID = json.getJSONObject(0).getString("id");
        String folderID = json.getJSONObject(0).getString("folder_id");

        {
            GetResponse response = Executor.execute(getSession(), new GetRequest(folderID, mailID, true, true));
            Object data = response.getData();
            assertNotNull(data);

            JSONObject jResponse = (JSONObject) data;
            JSONArray jAttachments = jResponse.getJSONArray("body");
            assertEquals(4, jAttachments.length());

            JSONObject jAttachment = jAttachments.getJSONObject(1);
            assertEquals("application/rtf", jAttachment.getJSONObject("headers").getJSONObject("content-type").getString("type"));
        }

        {
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] { folderID, mailID, "2" }).setFromStructure(true));
            String strBody = response.getStringBody();
            assertNotNull(strBody);
            assertTrue(strBody.startsWith("{\\rtf1"));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

    @Test
    public void testBug36333_2() throws OXException, IOException, JSONException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug36333_2.eml")), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = streamReader.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        streamReader.close();
        InputStream inputStream = new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
        final ImportMailResponse importResp = getClient().execute(importMailRequest);
        JSONArray json = (JSONArray) importResp.getData();
        fmid = importResp.getIds();

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

        String mailID = json.getJSONObject(0).getString("id");
        String folderID = json.getJSONObject(0).getString("folder_id");

        {
            GetResponse response = Executor.execute(getSession(), new GetRequest(folderID, mailID, true, true));
            Object data = response.getData();
            assertNotNull(data);

            JSONObject jResponse = (JSONObject) data;
            JSONArray jAttachments = jResponse.getJSONArray("body");
            assertEquals(4, jAttachments.length());

            JSONObject jAttachment = jAttachments.getJSONObject(1);
            assertEquals("application/rtf", jAttachment.getJSONObject("headers").getJSONObject("content-type").getString("type"));
        }

        {
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] { folderID, mailID, "2" }).setFromStructure(true));
            String strBody = response.getStringBody();
            assertNotNull(strBody);
            assertTrue(strBody.startsWith("{\\rtf1"));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

    @Test
    public void testBug36333_3() throws OXException, IOException, JSONException {
        JSONArray json;
        {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug36333_3.eml")), "UTF-8");
            char[] buf = new char[512];
            int length;
            StringBuilder sb = new StringBuilder();
            while ((length = streamReader.read(buf)) != -1) {
                sb.append(buf, 0, length);
            }
            streamReader.close();
            InputStream inputStream = new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
            final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
            final ImportMailResponse importResp = getClient().execute(importMailRequest);
            json = (JSONArray) importResp.getData();
            fmid = importResp.getIds();
        }

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

        String mailID = json.getJSONObject(0).getString("id");
        String folderID = json.getJSONObject(0).getString("folder_id");

        {
            GetResponse response = Executor.execute(getSession(), new GetRequest(folderID, mailID, true, true));
            Object data = response.getData();
            assertNotNull(data);

            JSONObject jResponse = (JSONObject) data;

            JSONArray jAttachments = jResponse.getJSONArray("body");
            assertEquals(2, jAttachments.length());

            JSONObject jAttachment = jAttachments.getJSONObject(1);
            assertEquals("multipart/mixed", jAttachment.getJSONObject("headers").getJSONObject("content-type").getString("type"));
        }

        {
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] { folderID, mailID, "2.2" }).setFromStructure(true));

            byte[] binBody = response.getBinaryBody();
            assertNotNull(binBody);

            String base64 = Base64.encodeBase64String(binBody);
            binBody = null;
            assertTrue("Unexpected content: " + Strings.abbreviate(base64, 32), base64.startsWith("JVBERi0xLjUNJeLjz9MN"));
            assertTrue("Unexpected content: " + base64.substring(base64.length() - 32, base64.length()), base64.endsWith("DSUlRU9GDQ=="));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

}
