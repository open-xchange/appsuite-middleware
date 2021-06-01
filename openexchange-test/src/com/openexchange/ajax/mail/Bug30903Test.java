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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailJSONField;

/**
 * {@link Bug30903Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug30903Test extends AbstractMailTest {

    private static String mail = "Message-Id: <blah@non-existent.com>\n" + "X-Mailer: AppSuite 7.6.0 \n" + "Date: Tue, 08 Apr 2014 13:37:00 +0100\n" + "From: #ADDR#\n" + "To: #ADDR#\n" + "Subject: Bug30903\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "\n" + "Testing";

    private final String fmids[][] = new String[2][2];

    /**
     * Initializes a new {@link Bug30903Test}.
     *
     * @param name
     */
    public Bug30903Test() {
        super();
    }

    /**
     * Store a draft mail in 'Drafts' folder and send afterwards
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testDeleteDraft() throws OXException, IOException, JSONException {
        UserValues values = getClient().getValues();
        //Save draft
        NewMailRequest newMailReq = new NewMailRequest(values.getDraftsFolder(), mail.replaceAll("#ADDR#", values.getSendAddress()), MailFlag.DRAFT.getValue());
        NewMailResponse newMailResp = getClient().execute(newMailReq);
        assertNotNull(newMailResp);
        String draftID = newMailResp.getId();
        fmids[0][0] = values.getDraftsFolder();
        fmids[0][1] = draftID;

        //Get draft
        GetRequest getReq = new GetRequest(values.getDraftsFolder(), draftID);
        GetResponse getResp = getClient().execute(getReq);
        assertNotNull(getResp);

        //Edit draft
        JSONObject data = (JSONObject) getResp.getData();
        String msgref = data.getString("msgref");
        JSONObject jsonMail = new JSONObject();
        jsonMail.put(MailJSONField.FROM.getKey(), values.getSendAddress());
        jsonMail.put(MailJSONField.RECIPIENT_TO.getKey(), values.getSendAddress());
        jsonMail.put(MailJSONField.RECIPIENT_CC.getKey(), "");
        jsonMail.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
        jsonMail.put(MailJSONField.SUBJECT.getKey(), "Bug30903");
        jsonMail.put(MailJSONField.PRIORITY.getKey(), "3");
        jsonMail.put("sendtype", 6);
        //jsonMail.put("deleteDraftOnTransport", true);
        jsonMail.put(MailJSONField.MSGREF.getKey(), msgref);
        final JSONObject jAttachment = new JSONObject(2);
        jAttachment.put(MailJSONField.CONTENT_TYPE.getKey(), "text/plain; charset=\"UTF-8\"");
        jAttachment.put(MailJSONField.CONTENT.getKey(), "Testing\nAdded a few lines\nFoo bar\nand lorem ipsum\n.");
        final JSONArray jAttachments = new JSONArray(1);
        jAttachments.put(jAttachment);
        jsonMail.put(MailJSONField.ATTACHMENTS.getKey(), jAttachments);

        //Send mail
        SendRequest sendRequest = new SendRequest(jsonMail.toString());
        SendResponse sendResponse = getClient().execute(sendRequest);
        assertNotNull(sendResponse);
        fmids[1] = sendResponse.getFolderAndID();

        //Verify 'Drafts' folder
        AllRequest allReq = new AllRequest(values.getDraftsFolder(), COLUMNS_FOLDER_ID, 0, Order.ASCENDING, true);
        AllResponse allResp = getClient().execute(allReq);
        assertNotNull(allResp);
        Object[][] objArray = allResp.getArray();
        for (Object o[] : objArray) {
            String s = (String) o[1];
            if (Integer.parseInt(s) == Integer.parseInt(draftID)) {
                fail("Draft mail still in 'Drafts' folder");
            }
        }
    }
}
