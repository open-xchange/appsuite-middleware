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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    public MailSearchTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folder = getInboxFolder();
        clearFolder(folder);
    }

    @Override
    public void tearDown() throws Exception {
        clearFolder(folder);
        super.tearDown();
    }

    public void testSearch() throws Exception {
        final String searchText = "Your order";
        JSONArray search;

        search = searchBySubject(searchText);
        assertEquals("Should not yield results in empty folder.", 0, search.length());

        String eml =
            "Message-Id: <4A002517.4650.0059.1>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Your order\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
        getClient().execute(new NewMailRequest(folder, eml, -1, true));
        search = searchBySubject(searchText);
        assertEquals("Should yield one result.", 1, search.length());

        getClient().execute(new NewMailRequest(folder, eml, -1, true));
        search = searchBySubject(searchText);
        assertEquals("Should yield two results when facing two identical mails.", 2, search.length());

        eml =
            "Message-Id: <4A002517.4650.0059.1>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Barfoo\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
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
