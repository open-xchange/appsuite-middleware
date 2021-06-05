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
 * {@link Bug14350Test}
 *
 * vcard: addresses are not imported
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug14350Test extends VCardTest {

    /**
     * Initializes a new {@link Bug14350Test}.
     */
    public Bug14350Test() {
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
            "FN:Prinz\\, Tobias\n" +
            "N:Prinz;Tobias;;;\n" +
            "NICKNAME:Tierlieb\n" +
            "BDAY:19810501\n" +
            "ADR;TYPE=work:;;Broadway 3131 / 5th Ave;T\u00fcbingen;Baden-W\u00fcrttemberg;57621;Germany\n" +
            "ADR;TYPE=home:;;Testroad 4711;Port de la V\u00e9rde;Skol-upon-sea;37542;France\n" +
            "ORG:- deactivated -\n" +
            "REV:20061204T160750.018Z\n" +
            "UID:80@ox6.netline.de\n" +
            "END:VCARD\n";
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("Broadway 3131 / 5th Ave", contact.getStreetBusiness());
        Assert.assertEquals("T\u00fcbingen", contact.getCityBusiness());
        Assert.assertEquals("Baden-W\u00fcrttemberg", contact.getStateBusiness());
        Assert.assertEquals("57621", contact.getPostalCodeBusiness());
        Assert.assertEquals("Germany", contact.getCountryBusiness());
        Assert.assertEquals("Testroad 4711", contact.getStreetHome());
        Assert.assertEquals("Port de la V\u00e9rde", contact.getCityHome());
        Assert.assertEquals("Skol-upon-sea", contact.getStateHome());
        Assert.assertEquals("37542", contact.getPostalCodeHome());
        Assert.assertEquals("France", contact.getCountryHome());

    }

}
