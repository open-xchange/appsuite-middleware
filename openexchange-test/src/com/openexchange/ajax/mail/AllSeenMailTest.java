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

    public AllSeenMailTest(final String name) {
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

    public void testAllSeen() throws OXException, IOException, SAXException, JSONException{
        String eml =
            "Message-Id: <4A002517.4650.0059.212@foobar.com>\n" +
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
