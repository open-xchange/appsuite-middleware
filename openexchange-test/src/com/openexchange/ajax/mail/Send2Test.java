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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;

/**
 * {@link Send2Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - tests with manager
 */
public final class Send2Test extends AbstractMailTest {

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Send2Test() {
        super();
    }
    /**
     * Tests the <code>action=new</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testSend() throws Throwable {
        // Create JSON mail object
        final JSONObject jMail = new JSONObject(16);
        jMail.put(MailJSONField.FROM.getKey(), getSendAddress(getClient()));
        jMail.put(MailJSONField.RECIPIENT_TO.getKey(), getSendAddress());
        jMail.put(MailJSONField.RECIPIENT_CC.getKey(), "");
        jMail.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
        jMail.put(MailJSONField.SUBJECT.getKey(), "OX Guard Test");
        jMail.put(MailJSONField.PRIORITY.getKey(), "3");

        jMail.put("sendtype", "0");
        jMail.put("copy2Sent", "false");

        {
            final JSONObject jHeaders = new JSONObject(4);
            jHeaders.put("X-OxGuard", true).put("X-OxGuard-ID", "45fe1029-edd1-4866-8a1a-7889b4a98bca");
            jMail.putOpt("headers", jHeaders);
        }

        {
            final JSONObject jAttachment = new JSONObject(4);
            jAttachment.put(MailJSONField.CONTENT_TYPE.getKey(), "text/html");
            jAttachment.put(MailJSONField.CONTENT.getKey(), "<table><tr>\n<td style=\"background-color:#FFFFFF; height:52px; width:100px;\">\n<span style = \"font-size:48px; font-family: Veranda; font-weight: bold; color: #6666FF;\">OX</span>\n</td><td align=\"center\" style=\"width:300px;\"><h1>Secure Email</h1></td>\n</tr>\n</table>");

            final JSONArray jAttachments = new JSONArray(1);
            jAttachments.put(jAttachment);
            jMail.put(MailJSONField.ATTACHMENTS.getKey(), jAttachments);
        }

        // Perform send request
        final SendResponse response = Executor.execute(getSession(), new SendRequest(jMail.toString()));
        final String[] folderAndID = response.getFolderAndID();

        assertNull("Send request failed", folderAndID);

    }

}
