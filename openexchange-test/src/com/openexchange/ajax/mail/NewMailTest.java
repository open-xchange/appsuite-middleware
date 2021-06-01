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

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.exception.OXException;

/**
 * {@link NewMailTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NewMailTest extends AbstractMailTest {

    private static final String EML_WITHOUT_FROM = "Message-Id: <4A002517.4650.0059.1@deployfast.com>\n" + "X-Mailer: Novell GroupWise Internet Agent 8.0.0 \n" + "Date: Tue, 05 May 2009 11:37:58 -0500\n" + "To: #TOADDR#\n" + "Subject: Re: Your order for East Texas Lighthouse\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "This is a MIME message. If you are reading this text, you may want to \n" + "consider changing to a mail reader or gateway that understands how to \n" + "properly handle MIME multipart messages.";

    private UserValues values;

    public NewMailTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testTransportNewRFC822MailWithoutFrom() throws OXException, IOException, JSONException {
        // System.out.println(values.getDraftsFolder());

        final NewMailRequest newMailRequest = new NewMailRequest(null, EML_WITHOUT_FROM.replaceFirst("#TOADDR#", values.getSendAddress()), -1, true);
        final NewMailResponse newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());
    }

    @Test
    public void testAppendNewRFC822MailWithoutFrom() throws OXException, IOException, JSONException {
        // System.out.println(values.getDraftsFolder());

        final NewMailRequest newMailRequest = new NewMailRequest(values.getDraftsFolder(), EML_WITHOUT_FROM.replaceFirst("#TOADDR#", values.getSendAddress()), -1, true);
        final NewMailResponse newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());
    }
}
