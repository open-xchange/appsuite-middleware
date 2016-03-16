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

import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug15229Test}
 *
 *  Illegal character in scheme name at index 4: http\://twitter.com/foobar
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug15229Test extends VCardTest {

    /**
     * Initializes a new {@link Bug15229Test}.
     */
    public Bug15229Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "\n" +
            "VERSION:3.0\n" +
            "\n" +
            "N:L\u00f6fflad;Klaus;(piraten);;\n" +
            "\n" +
            "FN:Klaus (piraten) L\u00f6fflad\n" +
            "\n" +
            "EMAIL;type=INTERNET;type=HOME;type=pref:klaus@der-kapitaen.de\n" +
            "\n" +
            "TEL;type=CELL;type=pref:+49 151 22632571\n" +
            "\n" +
            "item1.URL;type=pref:http\\://wiki.piratenpartei.de/Benutzer\\:Magister_Navis\n" +
            "\n" +
            "item1.X-ABLabel:Piraten\n" +
            "\n" +
            "END:VCARD\n" +
             "\n"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("http://wiki.piratenpartei.de/Benutzer:Magister_Navis", contact.getURL());
    }

}
