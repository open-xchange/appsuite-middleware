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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.DeleteResponse;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link Bug31855Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug31855Test extends AbstractMailTest {

    private UserValues values;

    String[][] fmid;

    public Bug31855Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testBug31855() throws OXException, IOException, JSONException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug31855.eml")), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = streamReader.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        streamReader.close();
        InputStream inputStream = new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        final ImportMailRequest importMailRequest = new ImportMailRequest(values.getInboxFolder(), MailFlag.SEEN.getValue(), inputStream).setStrictParsing(false);
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

        final GetResponse response = Executor.execute(getSession(), new GetRequest(folderID, mailID));
        JSONArray array = response.getAttachments();
        assertEquals("Incorrect number of attachments", 3, array.length());
        assertEquals("Incorrect content type of attachment 2", "application/octet-stream", array.getJSONObject(1).getString("content_type"));
        assertEquals("Incorrect content type of attachment 3", "application/octet-stream", array.getJSONObject(2).getString("content_type"));

        if ((folderID != null) && (mailID != null)) {
            DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(folderID, mailID, true));
            assertNull("Error deleting mail. Artifacts remain", deleteResponse.getErrorMessage());
        }
    }
}
