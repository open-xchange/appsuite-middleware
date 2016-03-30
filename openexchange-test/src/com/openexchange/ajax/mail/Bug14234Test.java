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

import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;

/**
 * {@link Bug14234Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug14234Test extends AbstractMailTest {

    private static final String EML_WO_REFERENCES =
        "Date: Mon, 10 Oct 2011 08:54:17 +0200 (CEST)\n" +
        "From: #ADDR#\n" +
        "Reply-To: #ADDR#\n" +
        "To: #ADDR#\n" +
        "Message-ID: <9999ABC48.316.1318233257789.JavaMail.open-xchange@localhost>\n" +
        "Subject: =?UTF-8?Q?DB_Schema_ge=C3=A4ndert=3F?=\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: multipart/alternative; \n" +
        "    boundary=\"----=_Part_315_1962735845.1318233257734\"\n" +
        "X-Priority: 3\n" +
        "Importance: Medium\n" +
        "\n" +
        "------=_Part_315_1962735845.1318233257734\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain; charset=UTF-8\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "\n" +
        "=C2=A0\n" +
        "Palim-Palim...\n" +
        "=C2=A0";

    private static String getEML_WITH_REFERENCES() {
        return ("Date: Mon, 10 Oct 2011 09:54:17 +0200 (CEST)\n" +
        "From: #ADDR#\n" +
        "Reply-To: #ADDR#\n" +
        "To: #ADDR#\n" +
        "Message-ID: <205045348.316.1318233257789.JavaMail.open-xchange@localhost>\n" +
        "In-Reply-To: <1203129835.0.1318322754199.JavaMail.thorben@localhost>\n" +
        "References: <1203129835.0.1318322754199.JavaMail.thorben@localhost>\n" +
        "Subject: =?UTF-8?Q?Re:_DB_Schema_ge=C3=A4ndert=3F?=\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: multipart/alternative; \n" +
        "    boundary=\"----=_Part_315_1962735845.1318233257734\"\n" +
        "X-Priority: 3\n" +
        "Importance: Medium\n" +
        "\n" +
        "------=_Part_315_1962735845.1318233257734\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain; charset=UTF-8\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "\n" +
        "=C2=A0\n" +
        "Meine Antwort: Palim-Palim...\n" +
        "=C2=A0");
    }

    private UserValues values;

    public Bug14234Test(final String name) {
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
