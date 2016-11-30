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
        assertEquals("unexpected number of addresses exported", 1, vCard.getAddresses().size());
    }

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
        assertEquals("unexpected number of addresses exported", 1, vCard.getAddresses().size());
        assertEquals("street wrong", "street", vCard.getAddresses().get(0).getStreetAddress());
        assertEquals("po box wrong", "existing po box", vCard.getAddresses().get(0).getPoBox());

    }

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
        assertEquals("unexpected number of addresses exported", 0, vCard.getAddresses().size());

    }

}
