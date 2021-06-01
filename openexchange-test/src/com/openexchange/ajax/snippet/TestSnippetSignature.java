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

package com.openexchange.ajax.snippet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.managedfile.actions.GetPictureManagedFileRequest;
import com.openexchange.ajax.managedfile.actions.NewManagedFileRequest;
import com.openexchange.ajax.managedfile.actions.NewManagedFileResponse;
import com.openexchange.ajax.snippet.actions.ListSnippetRequest;
import com.openexchange.ajax.snippet.actions.ListSnippetResponse;
import com.openexchange.ajax.snippet.actions.NewSnippetRequest;
import com.openexchange.ajax.snippet.actions.NewSnippetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;

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
    public TestSnippetSignature() {
        super();
    }

    @Test
    public void testCreateSignatureWithImageWorkflow() throws Exception {
        String mfID1 = uploadFile("contact_image.png");
        String mfID2 = uploadFile("ox_logo_sml.jpg");

        final StringBuilder builder = new StringBuilder();
        builder.append("<img alt=\"\" width=\"120\" src=\"").append("/ajax/file?action=get&id=").append(mfID1).append("&session=").append(getClient().getSession().getId()).append("\" />");
        builder.append("<img alt=\"\" width=\"50\" src=\"").append("/ajax/file?action=get&id=").append(mfID2).append("&session=").append(getClient().getSession().getId()).append("\" />");

        JSONObject misc = new JSONObject();
        misc.put("insertion", "below");
        misc.put("content-type", "text/html");

        JSONObject body = new JSONObject();
        body.put("type", "signature");
        body.put("misc", misc);
        body.put("createdby", getClient().getValues().getUserId());
        body.put("content", "<h1>my test signature</h1>" + builder.toString());
        body.put("accountid", 0);
        body.put("displayname", "DisplayName of signatures");
        body.put("module", "io.ox/mail");

        // New signature
        NewSnippetRequest req = new NewSnippetRequest(body);
        NewSnippetResponse resp = getClient().execute(req);
        Object data = resp.getData();
        assertNotNull(data);
        int signId = Integer.parseInt((String) data);
        JSONArray array = new JSONArray();
        array.put(signId);
        ListSnippetRequest listReq = new ListSnippetRequest(array, true);
        ListSnippetResponse listResp = getClient().execute(listReq);
        assertNotNull(listResp);
        data = listResp.getData();
        JSONArray json = (JSONArray) data;
        assertEquals(1, json.length());

        GetPictureManagedFileRequest getPicReq = new GetPictureManagedFileRequest(mfID1);
        getClient().execute(getPicReq);
    }

    private String uploadFile(String filename) throws OXException, IOException, JSONException {
        byte[] file = readFile(filename);
        NewManagedFileRequest newMFReq = new NewManagedFileRequest("snippet", "image", file);
        NewManagedFileResponse newMFResp = getClient().execute(newMFReq);
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
            ios = new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + filename));
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
