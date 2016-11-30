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

package com.openexchange.ajax.mail.categories;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;

/**
 * {@link ThreadedMailCategoriesTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ThreadedMailCategoriesTest extends AbstractMailCategoriesTest {
    
    
    private int[] columns = new int[] { 102, 600, 601, 602, 603, 604, 605, 606, 607, 608, 610, 611, 614, 652, 656 };
    
    @Test
    public void conversationTest() throws Exception {
        MailTestManager manager = new MailTestManager(client, false);
        AJAXClient user2Client = new AJAXClient(User.User2);
        String send2 = getSendAddress(user2Client);
        
        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 1;
        final String eml =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
        
        final String eml2 =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + send2 + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
        
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            user2Client.execute(new NewMailRequest(client.getValues().getInboxFolder(), eml2, -1, true));
        }
        
        String origin = values.getInboxFolder();

        // check general - should contain the thread
        List<ThreadSortMailMessage> messages = manager.listConversations(origin, columns, 610, Order.DESCENDING, false, "general", 0, 10);

        assertTrue("The number of messages is incorrect.", messages.size() == 1);
        assertTrue("The message does not contain any child messages.", messages.get(0).getChildMessages().size() != 0);

        // check social - should be empty
        messages = manager.listConversations(origin, columns, 610, Order.DESCENDING, false, "social", 0, 10);

        assertTrue("The number of messages is incorrect.", messages.size() == 0);

        // train categories

        manager.trainCategory("social", true, false, send2, getSendAddress());

        // check social again - should now contain the thread
        messages = manager.listConversations(origin, columns, 610, Order.DESCENDING, false, "social", 0, 10);

        assertTrue("The number of messages is incorrect.", messages.size() == 1);
        assertTrue("The message does not contain any child messages.", messages.get(0).getChildMessages().size() != 0);


    }
    
    
    

}
