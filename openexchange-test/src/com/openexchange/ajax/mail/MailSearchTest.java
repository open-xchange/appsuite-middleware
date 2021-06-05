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
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.mail.actions.MailSearchRequest;
import com.openexchange.ajax.mail.actions.MailSearchResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailListField;

/**
 * {@link MailSearchTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MailSearchTest extends AbstractMailTest {

    private String folder;

    public MailSearchTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = getInboxFolder();
        clearFolder(folder);
    }

    @Test
    public void testSearch() throws Exception {
        final String searchText = "Your order";
        JSONArray search;

        search = searchBySubject(searchText);
        assertEquals("Should not yield results in empty folder.", 0, search.length());

        String eml = "Message-Id: <4A002517.4650.0059.1>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Your order\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";
        getClient().execute(new NewMailRequest(folder, eml, -1, true));
        search = searchBySubject(searchText);
        assertEquals("Should yield one result.", 1, search.length());

        getClient().execute(new NewMailRequest(folder, eml, -1, true));
        search = searchBySubject(searchText);
        assertEquals("Should yield two results when facing two identical mails.", 2, search.length());

        eml = "Message-Id: <4A002517.4650.0059.1>\n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "From: " + getSendAddress() + "\n" + "To: " + getSendAddress() + "\n" + "Subject: Barfoo\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";
        getClient().execute(new NewMailRequest(folder, eml, -1, true));
        search = searchBySubject(searchText);
        assertEquals("Should still yield two results after being sent a different one", 2, search.length());
    }

    public JSONArray searchBySubject(final String pattern) throws OXException, IOException, JSONException {
        final JSONArray body = new JSONArray();
        final JSONObject obj = new JSONObject();
        obj.put(Mail.PARAMETER_COL, MailListField.SUBJECT.getField());
        obj.put(Mail.PARAMETER_SEARCHPATTERN, pattern);
        body.put(obj);
        final MailSearchRequest request = new MailSearchRequest(body, folder, COLUMNS_DEFAULT_LIST, 0, "asc", true);
        final MailSearchResponse response = getClient().execute(request);
        return response.getDataAsJSONArray();
    }
}
