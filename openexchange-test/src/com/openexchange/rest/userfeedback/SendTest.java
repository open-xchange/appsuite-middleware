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

package com.openexchange.rest.userfeedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.io.IOException;
import javax.mail.internet.AddressException;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.test.common.tools.RandomString;
import com.openexchange.testing.restclient.invoker.ApiException;
import com.openexchange.userfeedback.rest.services.SendUserFeedbackService;

/**
 * {@link SendTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class SendTest extends AbstractUserFeedbackTest {

    private JSONObject requestBody = null;

    private String mailId = "";

    @Override
    protected Application configure() {
        return new ResourceConfig(SendUserFeedbackService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        JSONArray recipients = new JSONArray();
        JSONObject recipient1 = new JSONObject();
        recipient1.put("address", testUser.getLogin());
        recipient1.put("displayName", testUser.getUser());
        recipients.put(recipient1);
        requestBody = new JSONObject();
        requestBody.put("recipients", recipients);
        requestBody.put("subject", "subject");
        requestBody.put("body", "body");
        requestBody.put("compress", true);
    }

    private void assertMailRetrieved(String subject, MailMessage[] mailMessages) {
        for (MailMessage mailMessage : mailMessages) {
            if (mailMessage.getSubject().equals(subject)) {
                mailId = mailMessage.getMailId();
            }
        }
        assertFalse("Unable to find feeback mail", mailId.equals(""));
    }

    private static final int[] listAttributes = new int[] { MailListField.ID.getField(), MailListField.FROM.getField(), MailListField.TO.getField(), MailListField.SUBJECT.getField() };

    @Test
    public void testSend_everythingFine_returnMessageAndVerifyMail() throws OXException, IOException, JSONException, AddressException, ApiException {
        String subject = RandomString.generateChars(35);
        requestBody.put("subject", subject);
        String send = userfeedbackApi.send("default", type, requestBody.toString(), new Long(0), new Long(0));
        assertEquals(200, getRestClient().getStatusCode());
        JSONObject resp = new JSONObject(send);
        assertFalse(resp.hasAndNotNull("fail"));

        AllRequest all = new AllRequest(getAjaxClient().getValues().getInboxFolder(), listAttributes, 0, Order.DESCENDING, true);
        AllResponse response = getAjaxClient().execute(all);

        MailMessage[] mailMessages = response.getMailMessages(listAttributes);
        assertMailRetrieved(subject, mailMessages);
    }

    @Test
    public void testSend_subjectNull_sentWithDefaultSubject() throws ApiException, OXException, IOException, JSONException, AddressException {
        requestBody.remove("subject");
        userfeedbackApi.send("default", type, requestBody.toString(), new Long(0), new Long(0));
        assertEquals(200, getRestClient().getStatusCode());

        AllRequest all = new AllRequest(getAjaxClient().getValues().getInboxFolder(), listAttributes, 0, Order.DESCENDING, true);
        AllResponse response = getAjaxClient().execute(all);

        MailMessage[] mailMessages = response.getMailMessages(listAttributes);
        assertMailRetrieved("User Feedback Report", mailMessages);
    }

    @Test
    public void testSend_bodyNull_sentWithEmptyBody() throws ApiException, OXException, IOException, JSONException, AddressException {
        String subject = RandomString.generateChars(35);
        requestBody.put("subject", subject);
        requestBody.remove("body");
        userfeedbackApi.send("default", type, requestBody.toString(), new Long(0), new Long(0));
        assertEquals(200, getRestClient().getStatusCode());

        AllRequest all = new AllRequest(getAjaxClient().getValues().getInboxFolder(), listAttributes, 0, Order.DESCENDING, true);
        AllResponse response = getAjaxClient().execute(all);

        MailMessage[] mailMessages = response.getMailMessages(listAttributes);
        assertMailRetrieved(subject, mailMessages);
    }

    @Test
    public void testSend_noDisplayName_sentWithoutDisplayName() throws JSONException, ApiException, OXException, IOException, AddressException {
        JSONArray recipients = new JSONArray();
        JSONObject recipient1 = new JSONObject();
        recipient1.put("address", testUser.getLogin());
        recipients.put(recipient1);
        JSONObject body = new JSONObject();
        body.put("recipients", recipients);

        String subject = RandomString.generateChars(35);
        body.put("subject", subject);
        body.put("compress", true);

        userfeedbackApi.send("default", type, body.toString(), new Long(0), new Long(0));

        assertEquals(200, getRestClient().getStatusCode());

        AllRequest all = new AllRequest(getAjaxClient().getValues().getInboxFolder(), listAttributes, 0, Order.DESCENDING, true);
        AllResponse response = getAjaxClient().execute(all);

        MailMessage[] mailMessages = response.getMailMessages(listAttributes);
        assertMailRetrieved(subject, mailMessages);
    }

    @Test
    public void testSend_badMailAddress_returnException() throws JSONException {
        JSONArray recipients = new JSONArray();
        JSONObject recipient1 = new JSONObject();
        recipient1.put("address", "badmailaddress");
        recipient1.put("displayName", testUser.getUser());
        recipients.put(recipient1);
        JSONObject body = new JSONObject();
        body.put("recipients", recipients);


        try {
            userfeedbackApi.send("default", type, body.toString(), new Long(0), new Long(0));
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
            JSONObject exception = new JSONObject(e.getResponseBody());
            assertEquals("Provided addresses are invalid.", exception.get("error_desc"));
        }
    }

    @Test
    public void testSend_unknownContextGroup_return404() {
        try {
            userfeedbackApi.send("unknown", type, requestBody.toString(), new Long(0), new Long(0));
            fail();
        } catch (ApiException e) {
            assertEquals(404, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_unknownFeefdbackType_return404() {
        try {
            userfeedbackApi.send("default", "schalke-rating", requestBody.toString(), new Long(0), new Long(0));
            fail();
        } catch (ApiException e) {
            assertEquals(404, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_negativeStart_return404() {
        try {
            userfeedbackApi.send("default", type, requestBody.toString(), new Long(-11111), new Long(0));
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_negativeEnd_return404() {
        try {
            userfeedbackApi.send("default", type, requestBody.toString(), new Long(0), new Long(-11111));
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }

    @Test
    public void testSend_endBeforeStart_return404() {
        try {
            userfeedbackApi.send("default", type, requestBody.toString(), new Long(222222222), new Long(11111));
            fail();
        } catch (ApiException e) {
            assertEquals(400, getRestClient().getStatusCode());
        }
    }
}
