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
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug14349Test}
 *
 * vcard: Nickname is imported with brackets around it
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug14349Test extends VCardTest {

    /**
     * Initializes a new {@link Bug14349Test}.
     */
    public Bug14349Test() {
        super();
    }

    @Test
    public void testImportVCard() {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Nachname, Vorname\n" +
            "N:Nachname;Vorname;;;\n" +
            "NICKNAME:Spitzname\n" +
            "ADR;TYPE=work:;;;;;;\n" +
            "ADR;TYPE=home:;;;;;;\n" +
            "REV:20090902T125118.045Z\n" +
            "UID:39614@192.168.33.100\n" +
            "END:VCARD\n"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("Spitzname", contact.getNickname());
    }

}
