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
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug55090Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Bug55090Test extends VCardTest {

    /**
     * Initializes a new {@link Bug55090Test}.
     */
    public Bug55090Test() {
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
            "FN:test\n" +
            "PHOTO;VALUE=URL;TYPE=PNG:file:///opt/open-xchange/etc/none.png\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertNull(contact.getImageContentType());
        assertNull(contact.getImage1());

        vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "FN:test\n" +
            "PHOTO;VALUE=URL;TYPE=PNG:http://localhost:8009\n" +
            "END:VCARD"
        ;
        contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertNull(contact.getImageContentType());
        assertNull(contact.getImage1());
    }

}
