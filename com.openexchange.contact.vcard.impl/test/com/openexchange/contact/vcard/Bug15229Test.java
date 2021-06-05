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

    @Test
    public void testImportVCard() {
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
        Assert.assertEquals("http://wiki.piratenpartei.de/Benutzer:Magister_Navis", contact.getURL());
    }

}
