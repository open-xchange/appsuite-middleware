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
 * {@link Bug7719Test}
 *
 * Configuration/Import: "Telex" lost during vCard import
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug7719Test extends VCardTest {

    /**
     * Initializes a new {@link Bug7719Test}.
     */
    public Bug7719Test() {
        super();
    }

    @Test
    public void testImportVCard() {
        /*
         * import vCard
         */
        String telex = "7787987897897897897";
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "N:Schmitz;Hansi;;Dr.;\n" +
            "FN:Dr. Hansi Schmitz\n" +
            "EMAIL;PREF;INTERNET;CHARSET=Windows-1252:Hansi@Schmitz.super\n" +
            "EMAIL;TLX:" + telex + "\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals(telex, contact.getTelephoneTelex());
    }

}
