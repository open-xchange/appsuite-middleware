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
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug7249Test}
 *
 * vCard parser breaks down in case of strange EMAIL field
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug7249Test extends VCardTest {

    /**
     * Initializes a new {@link Bug7249Test}.
     */
    public Bug7249Test() {
        super();
    }

    @Test
    public void testImportVCard() {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "FN:Conference_Room_Olpe\n" +
            "EMAIL;PREF;INTERNET:Conference_Room_Olpe_EMAIL\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("Conference_Room_Olpe", contact.getDisplayName());
        assertNull(contact.getEmail1());
        assertNull(contact.getEmail2());
        assertNull(contact.getEmail3());
    }

}
