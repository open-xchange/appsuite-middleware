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

import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;

/**
 * {@link ArchiveMailTest}
 *
 */
public class ArchiveMailTest extends AbstractMailTest {

    private UserValues values;

    public ArchiveMailTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testShouldArchive() throws OXException, IOException, JSONException {
        MailTestManager manager = new MailTestManager(getClient(), false);

        final String eml = "Date: Mon, 19 Nov 2012 21:36:51 +0100 (CET)\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Message-ID: <1508703313.17483.1353357411049>\n" + "Subject: Copy a mail\n" + "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n" + "    boundary=\"----=_Part_17482_1388684087.1353357411002\"\n" + "\n" + "------=_Part_17482_1388684087.1353357411002\n" + "MIME-Version: 1.0\n" + "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "Copy from sent to drafts\n" + "------=_Part_17482_1388684087.1353357411002\n" + "MIME-Version: 1.0\n" + "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">" + " <head>\n" + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" + " </head><body style=\"font-family: verdana,geneva; font-size: 10pt; \">\n" + " \n" + "  <div>\n" + "   Copy from sent to drafts\n" + "  </div>\n" + " \n" + "</body></html>\n" + "------=_Part_17482_1388684087.1353357411002--\n";

        getClient().execute(new NewMailRequest(getInboxFolder(), eml, -1, true));

        String origin = values.getInboxFolder();

        TestMail myMail = new TestMail(getFirstMailInFolder(origin));
        myMail.getId();

        manager.archive(myMail);
    }

}
