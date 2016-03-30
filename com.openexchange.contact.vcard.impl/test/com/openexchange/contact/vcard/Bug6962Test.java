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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug6962Test}
 *
 * Can't parse vCards though VCardImporter
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug6962Test extends VCardTest {

    /**
     * Initializes a new {@link Bug6962Test}.
     */
    public Bug6962Test() {
        super();
    }

    public void testImportVCard_1() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Prinz\\, Tobias\n" +
            "N:Prinz;Tobias;;;\n" +
            "NICKNAME:Tierlieb\n" +
            "BDAY:19810501\n" +
            "ADR;TYPE=work:;;;Somewhere;NRW;58641;DE\n" +
            "TEL;TYPE=home,voice:+49 2538 7921\n" +
            "EMAIL:tobias.prinz@open-xchange.com\n" +
            "ORG:- deactivated -\n" +
            "REV:20061204T160750Z\n" +
            "URL:www.tobias-prinz.de\n" +
            "UID:80@ox6.netline.de\n" +
            "END:VCARD\n"
        ;
        VCardImport vCardImport = importVCard(vCard);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warnings
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("no warning", null != warnings && 0 < warnings.size());
    }

    public void testImportVCard_2() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "N:;Svetlana;;;\n" +
            "FN:Svetlana\n" +
            "TEL;type=CELL;type=pref:6670373\n" +
            "CATEGORIES:Nicht abgelegt\n" +
            "X-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\n" +
            "END:VCARD";
        ;
        VCardImport vCardImport = importVCard(vCard);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warnings
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("no warning", null != warnings && 0 < warnings.size());
    }

    public void testImportVCard_3() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Prinz\\, Tobias\n" +
            "N:Prinz;Tobias;;;\n" +
            "NICKNAME:Tierlieb\n" +
            "BDAY:19810501\n" +
            "ADR;TYPE=work:;;;Somewhere;NRW;58641;DE\n" +
            "TEL;TYPE=home,voice:+49 2538 7921\n" +
            "EMAIL:tobias.prinz@open-xchange.com\n" +
            "ORG:- deactivated -\n" +
            "REV:20061204T160750Z\n" +
            "URL:www.tobias-prinz.de\n" +
            "UID:80@ox6.netline.de\n" +
            "END:VCARD\n"
        ;
        VCardImport vCardImport = importVCard(vCard);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warnings
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("warnings", null == warnings || 0 == warnings.size());
    }

    public void testImportVCard_4() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "N:;Svetlana;;;\n" +
            "FN:Svetlana\n" +
            "TEL;type=CELL;type=pref:6670373\n" +
            "CATEGORIES:Nicht abgelegt\n" +
            "X-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\n" +
            "END:VCARD";
        ;
        VCardImport vCardImport = importVCard(vCard);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warnings
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("warnings", null == warnings || 0 == warnings.size());
    }

    public void testImportVCard_5() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:666\n" +
            "N:;Svetlana;;;\n" +
            "FN:Svetlana\n" +
            "TEL;type=CELL;type=pref:6670373\n" +
            "END:VCARD";
        ;
        VCardImport vCardImport = importVCard(vCard);
        Contact contact = vCardImport.getContact();
        /*
         * verify imported contact & warnings
         */
        assertNotNull(contact);
        List<OXException> warnings = vCardImport.getWarnings();
        assertTrue("warnings", null != warnings && 0 < warnings.size());
    }

}
