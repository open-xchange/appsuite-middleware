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
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.exception.OXException;


/**
 * {@link ArchiveMailTest}
 *
 */
public class ArchiveMailTest extends AbstractMailTest {

    private UserValues values;

    public ArchiveMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testShouldArchive() throws OXException, IOException, SAXException, JSONException{
        MailTestManager manager = new MailTestManager(client, false);

        final String eml =
            "Date: Mon, 19 Nov 2012 21:36:51 +0100 (CET)\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Message-ID: <1508703313.17483.1353357411049>\n" +
            "Subject: Copy a mail\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/alternative; \n" +
            "    boundary=\"----=_Part_17482_1388684087.1353357411002\"\n" +
            "\n" +
            "------=_Part_17482_1388684087.1353357411002\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Copy from sent to drafts\n" +
            "------=_Part_17482_1388684087.1353357411002\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/html; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">" +
            " <head>\n" +
            "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" +
            " </head><body style=\"font-family: verdana,geneva; font-size: 10pt; \">\n" +
            " \n" +
            "  <div>\n" +
            "   Copy from sent to drafts\n" +
            "  </div>\n" +
            " \n" +
            "</body></html>\n" +
            "------=_Part_17482_1388684087.1353357411002--\n";

        getClient().execute(new NewMailRequest(getInboxFolder(), eml, -1, true));

        String origin = values.getInboxFolder();

        TestMail myMail = new TestMail( getFirstMailInFolder( origin) );
        myMail.getId();

        manager.archive(myMail);
    }

}
