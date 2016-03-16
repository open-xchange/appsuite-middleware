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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;


/**
 * {@link ReplyAllTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ReplyAllTest extends AbstractReplyTest {

    public ReplyAllTest(final String name) {
        super(name);
    }

    public List<Contact> extract(final int amount, final Contact[] source, final List<String> excludedEmail){
        final List<Contact> returnees = new LinkedList<Contact>();
        int used = 0;
        for (final Contact elem : source) {
            if (!(excludedEmail.contains(elem.getEmail1()) || excludedEmail.contains(elem.getEmail2()) || excludedEmail.contains(elem.getEmail3())) && used < amount) {
                returnees.add(elem);
                used++;
            }
        }
        return returnees;
    }

    public void testDummy() throws Exception {
        // Disabled test below because sending mails do not cover an estimateable time frame
        final String mail1 = new AJAXClient(User.User1).getValues().getSendAddress();
        assertTrue(mail1.length() > 0);
    }

    public void no_testShouldReplyToSenderAndAllRecipients() throws OXException, IOException, SAXException, JSONException {
        final AJAXClient client1 = new AJAXClient(User.User1);
        final AJAXClient client2 = new AJAXClient(User.User2);
        {
            String folder = client2.getValues().getInboxFolder();
            Executor.execute(client2.getSession(), new com.openexchange.ajax.mail.actions.ClearRequest(folder).setHardDelete(true));
            folder = client2.getValues().getSentFolder();
            Executor.execute(client2.getSession(), new com.openexchange.ajax.mail.actions.ClearRequest(folder).setHardDelete(true));
        }


        final String mail1 = client1.getValues().getSendAddress(); // note: doesn't work the other way around on the dev system, because only the
        final String mail2 = client2.getValues().getSendAddress(); // first account is set up correctly.

        List<Contact> otherContacts;
        {
            ContactSearchObject searchObject = new ContactSearchObject();
            searchObject.setEmail1("*" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME)+"*");
            searchObject.addFolder(6);
            otherContacts = extract(2, contactManager.searchAction(searchObject), Arrays.asList(mail1,mail2));
            if (otherContacts.isEmpty()) {
                searchObject = new ContactSearchObject();
                searchObject.setEmail1("*");
                searchObject.addFolder(6);
                otherContacts = extract(2, contactManager.searchAction(searchObject), Arrays.asList(mail1,mail2));
            }
        }
        assertTrue("Precondition: This test needs at least two other contacts in the global address book to work, but has "+otherContacts.size(), otherContacts.size() > 1);

        this.client = client2;
        final String anotherMail = otherContacts.get(0).getEmail1();
        final String yetAnotherMail = otherContacts.get(1).getEmail1();

        final JSONObject mySentMail = createEMail(adresses(mail1, anotherMail, yetAnotherMail), "ReplyAll test", MailContentType.ALTERNATIVE.toString(), MAIL_TEXT_BODY);
        sendMail(mySentMail.toString());

        this.client = client1;
        final JSONObject myReceivedMail = getFirstMailInFolder(getInboxFolder());
        final TestMail myReplyMail = new TestMail(getReplyAllEMail(new TestMail(myReceivedMail)));

        assertTrue("Should contain indicator that this is a reply in the subject line", myReplyMail.getSubject().startsWith("Re:"));

        final List<String> toAndCC = myReplyMail.getTo();
        toAndCC.addAll(myReplyMail.getCc()); //need to do both because depending on user settings, it might be one of these

        assertTrue("Sender of original message should become recipient in reply", contains(toAndCC, mail2));
        assertTrue("1st recipient ("+anotherMail+") of original message should still be recipient in reply, but TO/CC field only has these: " + toAndCC, contains(toAndCC, anotherMail));
        assertTrue("2nd recipient ("+yetAnotherMail+") of original message should still be recipient in reply, but TO/CC field only has these: " + toAndCC, contains(toAndCC, yetAnotherMail));
    }


    protected String adresses(final String... mails){
        final StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(final String mail: mails){
            builder.append("[null,");
            builder.append(mail);
            builder.append("],");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(']');
        return builder.toString();
    }

}
