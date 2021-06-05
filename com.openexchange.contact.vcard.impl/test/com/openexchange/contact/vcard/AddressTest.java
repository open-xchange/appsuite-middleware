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
import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.property.Address;

/**
 * {@link AddressTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddressTest extends VCardTest {

    /**
     * Initializes a new {@link AddressTest}.
     */
    public AddressTest() {
        super();
    }

         @Test
     public void testExportToBlank() {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        contact.setStreetBusiness("street");
        contact.setCityBusiness("city");
        contact.setStateBusiness("state");
        contact.setPostalCodeBusiness("postal code");
        contact.setCountryBusiness("country");
        /*
         * export to new vCard
         */
        VCard vCard = getMapper().exportContact(contact, null, null, null);
        /*
         * verify vCard
         */
        assertNotNull("no vCard exported", vCard);
        assertNotNull("no addresses exported", vCard.getAddresses());
        Assert.assertEquals("unexpected number of addresses exported", 1, vCard.getAddresses().size());
    }

         @Test
     public void testMergeIntoExisting() {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        contact.setStreetBusiness("street");
        contact.setCityBusiness("city");
        contact.setStateBusiness("state");
        contact.setPostalCodeBusiness("postal code");
        contact.setCountryBusiness("country");
        /*
         * create test vCard
         */
        VCard vCard = new VCard();
        Address address = new Address();
        address.getTypes().add(AddressType.WORK);
        address.getTypes().add(AddressType.PREF);
        address.setStreetAddress("existing street");
        address.setPoBox("existing po box");
        vCard.addAddress(address);
        /*
         * export to existing vCard
         */
        vCard = getMapper().exportContact(contact, vCard, null, null);
        /*
         * verify vCard
         */
        assertNotNull("no vCard exported", vCard);
        assertNotNull("no addresses exported", vCard.getAddresses());
        Assert.assertEquals("unexpected number of addresses exported", 1, vCard.getAddresses().size());
        Assert.assertEquals("street wrong", "street", vCard.getAddresses().get(0).getStreetAddress());
        Assert.assertEquals("po box wrong", "existing po box", vCard.getAddresses().get(0).getPoBox());

    }

         @Test
     public void testRemoveFromExisting() {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        /*
         * create test vCard
         */
        VCard vCard = new VCard();
        Address address = new Address();
        address.getTypes().add(AddressType.WORK);
        address.getTypes().add(AddressType.PREF);
        address.setStreetAddress("existing street");
        address.setPoBox("existing po box");
        vCard.addAddress(address);
        /*
         * export to existing vCard
         */
        vCard = getMapper().exportContact(contact, vCard, null, null);
        /*
         * verify vCard
         */
        assertNotNull("no vCard exported", vCard);
        assertNotNull("no addresses exported", vCard.getAddresses());
        Assert.assertEquals("unexpected number of addresses exported", 0, vCard.getAddresses().size());

    }

}
