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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.DeleteResponse;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;

/**
 * {@link AbortAttachmentDownloadTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AbortAttachmentDownloadTest extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public AbortAttachmentDownloadTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testAbortedAttachmentDownload() throws OXException, IOException, JSONException {
        JSONArray json;
        {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug31855.eml")), "UTF-8");
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

        final GetRequest getRequest = new GetRequest(folderID, mailID);
        final GetResponse response = Executor.execute(getSession(), getRequest);
        JSONArray array = response.getAttachments();
        assertEquals("Incorrect number of attachments", 3, array.length());
        assertEquals("Incorrect content type of attachment 2", "application/octet-stream", array.getJSONObject(1).getString("content_type"));
        assertEquals("Incorrect content type of attachment 3", "application/octet-stream", array.getJSONObject(2).getString("content_type"));

        // Initiate download
        final String protocol = AJAXConfig.getProperty(Property.PROTOCOL);
        final String hostname = AJAXConfig.getProperty(Property.HOSTNAME);

        final AJAXSession session = getClient().getSession();
        final String sessionId = session.getId();

        InputStream in = null;
        try {
            // Establish URL connection
            String urlString = protocol + "://" + hostname + getRequest.getServletPath();
            urlString = urlString + "?action=attachment";
            urlString = urlString + "&session=" + sessionId;
            urlString = urlString + "&folder=" + urlEncode(folderID);
            urlString = urlString + "&id=" + urlEncode(mailID);
            urlString = urlString + "&attachment=2";
            URL myUrl = new URL(urlString);
            URLConnection urlConn = myUrl.openConnection();
            // Add cookies
            final DefaultHttpClient httpClient = session.getHttpClient();
            final List<Cookie> cookies = httpClient.getCookieStore().getCookies();
            final StringBuilder cookieStringBuilder = new StringBuilder(512);
            boolean first = true;
            for (Cookie cookie : cookies) {
                if (first) {
                    first = false;
                } else {
                    cookieStringBuilder.append("; ");
                }

                cookieStringBuilder.append(cookie.getName()).append('=').append(cookie.getValue());
            }
            urlConn.setRequestProperty("Cookie", cookieStringBuilder.toString());
            urlConn.setRequestProperty("User-Agent", "HTTP API Testing Agent");
            //urlConn.connect();
            in = urlConn.getInputStream();

            // Read some bytes
            for (int i = 0; i < 4; i++) {
                in.read();
            }

        } finally {
            Streams.close(in);
        }

        // Delete
        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

}
