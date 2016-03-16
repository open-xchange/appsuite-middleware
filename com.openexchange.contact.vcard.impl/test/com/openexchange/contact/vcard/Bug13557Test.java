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

package com.openexchange.contact.vcard;

import java.util.List;
import java.util.Locale;
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
