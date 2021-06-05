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
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;

/**
 * {@link ForwardMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ForwardMailTest extends AbstractReplyTest {

    MailTestManager manager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(getClient());
    }

    @Test
    public void testShouldForwardWithoutNotifyingFormerRecipients() throws OXException, IOException, JSONException {
        String mail1 = getClient().getValues().getSendAddress();

        JSONObject mySentMail = createEMail(mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
        String[] folderAndId = sendMail(mySentMail.toString());
        assertNotNull(folderAndId);

        JSONObject myReceivedMail = getFirstMailInFolder(getInboxFolder());
        TestMail myForwardMail = new TestMail(getForwardMail(new TestMail(myReceivedMail)));

        String subject = myForwardMail.getSubject();
        assertTrue("Should contain indicator that this is a forwarded mail in the subject line", subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }

    @Test
    public void testShouldForwardUsingTestMailManager() throws OXException, IOException, JSONException {

        String mail1 = getClient().getValues().getSendAddress();

        TestMail mySentMail = new TestMail(mail1, mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), "This is a forwarded mail");
        mySentMail = manager.send(mySentMail);

        TestMail myForwardMail = manager.forwardButDoNotSend(mySentMail);

        String subject = myForwardMail.getSubject();
        assertTrue("Should contain indicator that this is a forwarded mail in the subject line, which is '" + subject + "'", subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }

    @Test
    public void testShouldForwardUsingTestMailManager2() throws OXException, IOException, JSONException {

        String mail1 = getClient().getValues().getSendAddress();

        TestMail mySentMail = new TestMail(mail1, mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), "This is a forwarded mail");

        TestMail myForwardMail = manager.forwardAndSendBefore(mySentMail);

        String subject = myForwardMail.getSubject();
        assertTrue("Should contain indicator that this is a forwarded mail in the subject line, which is '" + subject + "'", subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }
}
