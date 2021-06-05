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
 * {@link MWB768Test}
 *
 * Imported vcard shows mail address twice in contact
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB768Test extends VCardTest {

    /**
     * Initializes a new {@link MWB768Test}.
     */
    public MWB768Test() {
        super();
    }

    @Test
    public void testImportVCard1() {
        /*
         * import vCard
         */
        String vCard = // @formatter:off
            "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "EMAIL;PREF=1:marie.linan19876@example.com\r\n" +
            "FN:Marie LINAN\r\n" +
            "N:LINAN;Marie;;;\r\n" +
            "TEL;TYPE=work;VALUE=TEXT:0254786523\r\n" +
            "TEL;TYPE=cell;VALUE=TEXT:0656379123\r\n" +
            "UID:ef1c4d70-0c2f-434a-b3aa-ba782926236b\r\n" +
            "END:VCARD\r\n"
        ; // @formatter:on
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("marie.linan19876@example.com", contact.getEmail1());
        assertNull(contact.getEmail2());
        assertNull(contact.getEmail3());
    }

    @Test
    public void testImportVCard2() {
        /*
         * import vCard
         */
        String vCard = // @formatter:off
            "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "EMAIL;PREF=1:paul.bosin1987@example.com\r\n" +
            "FN:Paul BOSIN\r\n" +
            "N:BOSIN;Paul;;;\r\n" +
            "TEL;TYPE=work;VALUE=TEXT:0356967534\r\n" +
            "TEL;TYPE=cell;VALUE=TEXT:0693693572\r\n" +
            "UID:e27f4994-2109-4b20-ba38-6212dcf20e61\r\n" +
            "END:VCARD\r\n"
        ; // @formatter:on
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("paul.bosin1987@example.com", contact.getEmail1());
        assertNull(contact.getEmail2());
        assertNull(contact.getEmail3());
    }

}
