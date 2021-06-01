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

package com.openexchange.contact.vcard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.contact.vcard.impl.internal.VCardExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug13557Test}
 *
 * Helpless error message while importing vCard
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug13557Test extends VCardTest {

    /**
     * Initializes a new {@link Bug13557Test}.
     */
    public Bug13557Test() {
        super();
    }

         @Test
     public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n"
            +"VERSION:2.1\n"
            +"SOURCE:Yahoo! AddressBook (http://address.yahoo.com)\n"
            +"PRODID:-//Yahoo!/AddressBook/YAB3/2009\n"
            +"FN;CHARSET=utf-8:Christine Wei\u221a\u00fcenbr\u221a\u00banner-Doppelname\n"
            +"N;CHARSET=utf-8:Wei\u221a\u00fcenbr\u221a\u00banner-Doppelname;Christine;;;\n"
            +"BDAY;CHARSET=utf-8:198041\n"
            +"ADR;HOME;CHARSET=utf-8:;;An der Luisenburg 2a;Leverkusen;NRW;51379;Germany\n"
            +"ADR;WORK;CHARSET=utf-8:;;Bonner Str 207;K\u221a\u2202ln;NRW;90768;Germany\n"
            +"LABEL;HOME;CHARSET=utf-8;ENCODING=QUOTED-PRINTABLE: =\n"
            +";;An der Luisenburg 2a;Leverkusen;NRW;51379;Germany=0A=0A51379 Leverkusen=\n"
            +"=0AGermany\n"
            +"LABEL;WORK;CHARSET=utf-8;ENCODING=QUOTED-PRINTABLE: =\n"
            +";;Bonner Str 207;K=C3=B6ln;NRW;90768;Germany=0A=0A90768 K=C3=B6ln=0AGermany\n"
            +"TEL;HOME;CHARSET=utf-8:02171 123456\n"
            +"TEL;WORK;PREF;CHARSET=utf-8:0221 987654\n"
            +"TEL;CELL;CHARSET=utf-8:0171 456987\n"
            +"EMAIL;INTERNET;PREF;CHARSET=utf-8:christine@example.com\n"
            +"TITLE;CHARSET=utf-8:Gesch\u221a\u00a7ftsf\u221a\u00bahrerin\n"
            +"ORG;CHARSET=utf-8:Christines L\u221a\u00a7dchen\n"
            +"NOTE;CHARSET=utf-8:My private note on Christine\n"
            +"UID;CHARSET=utf-8:2310c7412cc08237f3b57dfd7fbcf90c\n"
            +"X-SKYPE-ID;CHARSET=utf-8:christine.weissenbruenner\n"
            +"X-IM;SKYPE;CHARSET=utf-8:christine.weissenbruenner\n"
            +"X-CID;CHARSET=utf-8:1\n"
            +"X-CREATED;CHARSET=utf-8:1252943361\n"
            +"X-MODIFIED;CHARSET=utf-8:1252943452\n"
            +"REV;CHARSET=utf-8:2\n"
            +"X-PRIMARY-PHONE;CHARSET=utf-8:1\n"
            +"END:VCARD\n";
        ;
        VCardImport vCardImport = importVCard(vCard, null);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warning
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("no warning", null != warnings && 0 < warnings.size());
        for (OXException warning : warnings) {
            if (VCardExceptionCodes.PARSER_ERROR.equals(warning)) {
                String message = warning.getDisplayMessage(Locale.ENGLISH);
                assertTrue("no line in message: " + message, null != message && message.contains("Line"));
            }
        }
    }

}
