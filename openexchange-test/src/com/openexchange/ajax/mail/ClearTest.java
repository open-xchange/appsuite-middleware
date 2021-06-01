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
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import javax.mail.internet.AddressException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;

/**
 *
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class ClearTest extends AbstractMailTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ClearTest.class);

    public ClearTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

    @Test
    public void testClearingOneFolder() throws OXException, IOException, JSONException, AddressException {
        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 5;
        LOG.info("Appending " + numOfMails + " mails to fill emptied INBOX");
        final String eml = "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Invitation for launch\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"US-ASCII\"\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "Blah blah blah blah blah blah";
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(getInboxFolder(), eml, -1, true));
            LOG.info("Sent " + (i + 1) + ". mail of " + numOfMails);
        }

        // Assert that there are 5 Mails in the folder
        AllResponse allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));

        assertFalse("Didn't excpect exception in request.", allR.hasError());
        assertEquals("There should be 5 messages in the folder", allR.getMailMessages(COLUMNS_DEFAULT_LIST).length, 5);

        // Send the clear request
        clearFolder(getInboxFolder());

        // Assert there are no messages in the folder
        allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        assertFalse("Didn't excpect exception in request.", allR.hasError());
        assertEquals("There should be no messages in the folder", 0, allR.getMailMessages(COLUMNS_DEFAULT_LIST).length);
    }

}
