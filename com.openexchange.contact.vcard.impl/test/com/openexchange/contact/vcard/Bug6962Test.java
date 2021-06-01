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
import org.junit.Test;
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

         @Test
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

         @Test
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

         @Test
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

         @Test
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

         @Test
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
