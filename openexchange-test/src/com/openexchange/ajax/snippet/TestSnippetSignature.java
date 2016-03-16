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

package com.openexchange.ajax.snippet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.managedfile.actions.GetPictureManagedFileRequest;
import com.openexchange.ajax.managedfile.actions.GetPictureManagedFileResponse;
import com.openexchange.ajax.managedfile.actions.NewManagedFileRequest;
import com.openexchange.ajax.managedfile.actions.NewManagedFileResponse;
import com.openexchange.ajax.snippet.actions.ListSnippetRequest;
import com.openexchange.ajax.snippet.actions.ListSnippetResponse;
import com.openexchange.ajax.snippet.actions.NewSnippetRequest;
import com.openexchange.ajax.snippet.actions.NewSnippetResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;

/**
 * {@link TestSnippetSignature}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestSnippetSignature extends AbstractAJAXSession {

    /**
     * Initializes a new {@link TestSnippetSignature}.
     * 
     * @param name
     */
    public TestSnippetSignature(String name) {
        super(name);
    }

    public void testCreateSignatureWithImageWorkflow() throws Exception {
        String mfID1 = uploadFile("contact_image.png");
        String mfID2 = uploadFile("ox_logo_sml.jpg");

        final StringBuilder builder = new StringBuilder();
        builder.append("<img alt=\"\" width=\"120\" src=\"").append("/ajax/file?action=get&id=").append(mfID1).append("&session=").append(client.getSession().getId()).append("\" />");
        builder.append("<img alt=\"\" width=\"50\" src=\"").append("/ajax/file?action=get&id=").append(mfID2).append("&session=").append(client.getSession().getId()).append("\" />");

        JSONObject misc = new JSONObject();
        misc.put("insertion", "below");
        misc.put("content-type", "text/html");

        JSONObject body = new JSONObject();
        body.put("type", "signature");
        body.put("misc", misc);
        body.put("createdby", client.getValues().getUserId());
        body.put("content", "<h1>my test signature</h1>" + builder.toString());
        body.put("accountid", 0);
        body.put("displayname", "DisplayName of signatures");
        body.put("module", "io.ox/mail");

        // New signature
        NewSnippetRequest req = new NewSnippetRequest(body);
        NewSnippetResponse resp = client.execute(req);
        Object data = resp.getData();
        assertNotNull(data);
        int signId = Integer.parseInt((String) data);
        JSONArray array = new JSONArray();
        array.put(signId);
        ListSnippetRequest listReq = new ListSnippetRequest(array, true);
        ListSnippetResponse listResp = client.execute(listReq);
        assertNotNull(listResp);
        data = listResp.getData();
        JSONArray json = (JSONArray) data;
        assertEquals(1, json.length());
        JSONObject signature = json.getJSONObject(0);
        String content = signature.getString("content");

        /*
         * final int sidx = content.indexOf("src=\\\"");
         * final int eidx = content.indexOf("\\\">");
         * final String url = content.substring(sidx, eidx);
         */

        GetPictureManagedFileRequest getPicReq = new GetPictureManagedFileRequest(mfID1);
        GetPictureManagedFileResponse getPicResp = client.execute(getPicReq);
        Object o = getPicResp.getData();

        //assertEquals("images not equal", file, o);
    }

    private String uploadFile(String filename) throws OXException, IOException, JSONException {
        byte[] file = readFile(filename);
        NewManagedFileRequest newMFReq = new NewManagedFileRequest("snippet", "image", file);
        NewManagedFileResponse newMFResp = client.execute(newMFReq);
        Object data = newMFResp.getData();
        assertTrue("Response not a JSONArray", data instanceof JSONArray);
        JSONArray array = (JSONArray) data;
        assertEquals("The response array should only contain one element", 1, array.length());
        return array.getString(0);
    }

    private byte[] readFile(String filename) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        byte[] ret;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(new File(MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR) + filename));
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
            ret = ous.toByteArray();
        } finally {
            if (ous != null) {
                ous.close();
            }
            if (ios != null) {
                ios.close();
            }
        }
        return ret;
    }
}
