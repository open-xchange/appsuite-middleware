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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;

/**
 * {@link Bug14234Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug14234Test extends AbstractMailTest {

    private static String getEML_WITH_REFERENCES() {
        return ("Date: Mon, 10 Oct 2011 09:54:17 +0200 (CEST)\n" + "From: #ADDR#\n" + "Reply-To: #ADDR#\n" + "To: #ADDR#\n" + "Message-ID: <205045348.316.1318233257789.JavaMail.open-xchange@localhost>\n" + "In-Reply-To: <1203129835.0.1318322754199.JavaMail.thorben@localhost>\n" + "References: <1203129835.0.1318322754199.JavaMail.thorben@localhost>\n" + "Subject: =?UTF-8?Q?Re:_DB_Schema_ge=C3=A4ndert=3F?=\n" + "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n" + "    boundary=\"----=_Part_315_1962735845.1318233257734\"\n" + "X-Priority: 3\n" + "Importance: Medium\n" + "\n" + "------=_Part_315_1962735845.1318233257734\n" + "MIME-Version: 1.0\n" + "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: quoted-printable\n" + "\n" + "=C2=A0\n" + "Meine Antwort: Palim-Palim...\n" + "=C2=A0");
    }

    private UserValues values;

    public Bug14234Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Test
    public void testTransportNewRFC822MailWithoutFrom() throws Exception {
        NewMailRequest newMailRequest = null;
        NewMailResponse newMailResponse = null;

        //newMailRequest = new NewMailRequest(null, EML_WO_REFERENCES.replaceAll("#ADDR#", values.getSendAddress()), -1, true);
        //newMailResponse = getClient().execute(newMailRequest);

        newMailRequest = new NewMailRequest(null, getEML_WITH_REFERENCES().replaceAll("#ADDR#", values.getSendAddress()), -1, true);
        newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());
    }

}
