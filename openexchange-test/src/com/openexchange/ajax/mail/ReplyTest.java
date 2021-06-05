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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;

/**
 * {@link ReplyTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ReplyTest extends AbstractReplyTest {

    public ReplyTest() {
        super();
    }

    @Test
    public void testShouldReplyToSenderOnly() throws OXException, IOException, JSONException {
        AJAXClient client2 = null;
        try {
            // note: doesn't work the other way around on the dev system, because only the first account is set up correctly.
            client2 = testUser2.getAjaxClient();
            String mail2 = client2.getValues().getSendAddress();

            JSONObject mySentMail = createEMail(client2, getSendAddress(), "Reply test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
            sendMail(client2, mySentMail.toString());

            JSONObject myReceivedMail = getFirstMailInFolder(getInboxFolder());
            TestMail myReplyMail = new TestMail(getReplyEMail(new TestMail(myReceivedMail)));

            assertTrue("Should contain indicator that this is a reply in the subject line", myReplyMail.getSubject().startsWith("Re:"));

            List<String> to = myReplyMail.getTo();
            assertTrue("Sender of original message should become recipient in reply", contains(to, mail2));

            String from = myReplyMail.getFrom();
            assertTrue("New sender field should be empty, because GUI offers selection there", from == null || from.isEmpty() || from.equals("[]"));
        } finally {
            if (null != client2) {
                client2.logout();
            }
        }
    }
}
