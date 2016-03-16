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
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link Bug36333Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug36333Test extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public Bug36333Test(final String name) {
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

    public void testBug36333() throws OXException, IOException, JSONException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(
            MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR),
            "bug36333.eml")), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = streamReader.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        streamReader.close();
        InputStream inputStream = new ByteArrayInputStream(
            TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
        final ImportMailResponse importResp = client.execute(importMailRequest);
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
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] {folderID, mailID, "2"}).setFromStructure(true));
            String strBody = response.getStringBody();
            assertNotNull(strBody);
            assertTrue(strBody.startsWith("{\\rtf1"));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = client.execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

    public void testBug36333_2() throws OXException, IOException, JSONException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(
            MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR),
            "bug36333_2.eml")), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = streamReader.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        streamReader.close();
        InputStream inputStream = new ByteArrayInputStream(
            TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
        final ImportMailResponse importResp = client.execute(importMailRequest);
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
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] {folderID, mailID, "2"}).setFromStructure(true));
            String strBody = response.getStringBody();
            assertNotNull(strBody);
            assertTrue(strBody.startsWith("{\\rtf1"));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = client.execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

    public void testBug36333_3() throws OXException, IOException, JSONException {
        JSONArray json;
        {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(
                MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR),
                "bug36333_3.eml")), "UTF-8");
            char[] buf = new char[512];
            int length;
            StringBuilder sb = new StringBuilder();
            while ((length = streamReader.read(buf)) != -1) {
                sb.append(buf, 0, length);
            }
            streamReader.close();
            InputStream inputStream = new ByteArrayInputStream(
                TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
            final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream);
            final ImportMailResponse importResp = client.execute(importMailRequest);
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
            AttachmentResponse response = Executor.execute(getSession(), new AttachmentRequest(new String[] {folderID, mailID, "2.2"}).setFromStructure(true));

            byte[] binBody = response.getBinaryBody();
            assertNotNull(binBody);

            String base64 = Base64.encodeBase64String(binBody);
            binBody = null;
            assertTrue("Unexpected content: " + Strings.abbreviate(base64, 32), base64.startsWith("JVBERi0xLjUNJeLjz9MN"));
            assertTrue("Unexpected content: " + base64.substring(base64.length() - 32, base64.length()), base64.endsWith("DSUlRU9GDQ=="));
        }

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = client.execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

}
