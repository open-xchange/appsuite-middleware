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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;

/**
 * {@link ForwardMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ForwardMailTest extends AbstractReplyTest {

    MailTestManager manager;

    public ForwardMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(getClient());
    }

    @Override
    protected void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    public void testShouldForwardWithoutNotifyingFormerRecipients() throws OXException, IOException, SAXException, JSONException, OXException {
        String mail1 = getClient().getValues().getSendAddress();

        JSONObject mySentMail = createEMail(mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
        sendMail(mySentMail.toString());

        JSONObject myReceivedMail = getFirstMailInFolder(getInboxFolder());
        TestMail myForwardMail = new TestMail(getForwardMail(new TestMail(myReceivedMail)));

        String subject = myForwardMail.getSubject();
        assertTrue("Should contain indicator that this is a forwarded mail in the subject line", subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }

    public void testShouldForwardUsingTestMailManager() throws OXException, IOException, SAXException, JSONException, OXException {

        String mail1 = getClient().getValues().getSendAddress();

        TestMail mySentMail = new TestMail(mail1, mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), "This is a forwarded mail");
        mySentMail = manager.send(mySentMail);

        TestMail myForwardMail = manager.forwardButDoNotSend(mySentMail);

        String subject = myForwardMail.getSubject();
        assertTrue(
            "Should contain indicator that this is a forwarded mail in the subject line, which is '" + subject + "'",
            subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }

    public void testShouldForwardUsingTestMailManager2() throws OXException, IOException, SAXException, JSONException, OXException {

        String mail1 = getClient().getValues().getSendAddress();

        TestMail mySentMail = new TestMail(mail1, mail1, "Forward test", MailContentType.ALTERNATIVE.toString(), "This is a forwarded mail");

        TestMail myForwardMail = manager.forwardAndSendBefore(mySentMail);

        String subject = myForwardMail.getSubject();
        assertTrue(
            "Should contain indicator that this is a forwarded mail in the subject line, which is '" + subject + "'",
            subject.startsWith("Fwd:"));

        AbstractReplyTest.assertNullOrEmpty("Recipient field should be empty", myForwardMail.getTo());

        AbstractReplyTest.assertNullOrEmpty("Carbon copy field should be empty", myForwardMail.getCc());

        AbstractReplyTest.assertNullOrEmpty("Blind carbon copy field should be empty", myForwardMail.getBcc());
    }
}
