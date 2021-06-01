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
import org.junit.Test;
import com.openexchange.ajax.mail.actions.NewMailRequest;

/**
 *
 * {@link CountMailTest} - tests the CountRequest
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class CountMailTest extends AbstractMailTest {

    public CountMailTest() {
        super();
    }

    @Test
    public void testCounting() throws Exception {
        String inboxFolder = getInboxFolder();
        clearFolder(inboxFolder);
        assertEquals("Should be empty", 0, count(inboxFolder));

        final String eml = ("Message-Id: <4A002517.4650.0059.1@foobar.com>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: #ADDR#\n" + "To: #ADDR#\n" + "Subject: Invitation for launch\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.").replaceAll("#ADDR#", getSendAddress());
        for (int number = 1; number < 10; number++) {
            getClient().execute(new NewMailRequest(inboxFolder, eml, -1, true));
            assertEquals("Does not contain the expected number of elements in folder " + inboxFolder, number, count(inboxFolder));
        }

        clearFolder(inboxFolder);
        assertEquals("Should be empty again", 0, count(inboxFolder));
    }

}
