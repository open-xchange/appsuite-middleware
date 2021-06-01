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
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.AllSeenMailRequest;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link AllSeenMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllSeenMailTest extends AbstractMailTest {

    private UserValues values;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testAllSeen() throws OXException, IOException, JSONException {
        String eml = "Message-Id: <4A002517.4650.0059.212@foobar.com>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Invitation for launch\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";

        for (int i = 0; i < 12; i++) {
            getClient().execute(new NewMailRequest(values.getInboxFolder(), eml, -1, true));
        }
        eml = null;

        getClient().execute(new AllSeenMailRequest(values.getInboxFolder()));

        final int[] cols = new int[] { MailListField.FOLDER_ID.getField(), MailListField.ID.getField(), MailListField.FLAGS.getField() };
        AllRequest allRequest = new AllRequest(values.getInboxFolder(), cols, MailListField.RECEIVED_DATE.getField(), null, true);
        AllResponse allResponse = getClient().execute(allRequest);
        Object[][] array = allResponse.getArray();
        for (Object[] objects : array) {
            final Integer flags = (Integer) objects[2];
            assertTrue("\\Seen flag set, but shouldn't", (flags.intValue() & MailMessage.FLAG_SEEN) > 0);
        }
    }

}
