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

package com.openexchange.ajax.mail.categories;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
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
        MailTestManager manager = new MailTestManager(getAjaxClient(), false);
        AJAXClient user2Client = testUser2.getAjaxClient();
        String send2 = getSendAddress(user2Client);

        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 1;
        final String eml = "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Invitation for launch\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";

        final String eml2 = "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + send2 + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Invitation for launch\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";

        for (int i = 0; i < numOfMails; i++) {
            getAjaxClient().execute(new NewMailRequest(getAjaxClient().getValues().getInboxFolder(), eml, -1, true));
            user2Client.execute(new NewMailRequest(getAjaxClient().getValues().getInboxFolder(), eml2, -1, true));
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
