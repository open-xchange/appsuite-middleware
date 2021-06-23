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
 *
 */

package com.openexchange.subscribe.google.parser.consumers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.Address;
import com.google.api.services.people.v1.model.Person;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.java.Strings;
import com.openexchange.subscribe.google.parser.ContactNoteStrings;

/**
 * {@link AddressConsumer} - Parses the contact's addresses. Note that google
 * can store an unlimited mount of postal addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a home, business and other
 * address. Addresses with other types are stored inside the contact's note field.
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.6
 */
public class AddressConsumer extends AbstractNoteConsumer implements BiConsumer<Person, Contact> {

    private static final Comparator<Address> AddressComparator = new Comparator<Address>() {

        @Override
        public int compare(Address address1, Address address2) {
            String a = Strings.concat("-", new String[] { address1.getCity(), address1.getCountry(), address1.getPostalCode(), address1.getRegion(), address1.getStreetAddress() });
            String b = Strings.concat("-", new String[] { address2.getCity(), address2.getCountry(), address2.getPostalCode(), address2.getRegion(), address2.getStreetAddress() });
            return a.compareTo(b);
        }
    };

    /**
     * Initialises a new {@link AddressConsumer}.
     */
    public AddressConsumer(I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<Address> adressList = person.getAddresses();
        if (adressList == null || adressList.isEmpty()) {
            return;
        }
        boolean firstAddressInNotes = true;
        Collections.sort(adressList, AddressComparator);
        for (Address address : adressList) {
            switch (address.getType()) {
                case "home":
                    firstAddressInNotes = setHomeAddress(address, contact, firstAddressInNotes);
                    break;
                case "work":
                    firstAddressInNotes = setBusinessAddress(address, contact, firstAddressInNotes);
                    break;
                case "other":
                    firstAddressInNotes = setOtherAddress(address, contact, firstAddressInNotes);
                    break;
                default:
                    firstAddressInNotes = addAddressToNote(address, contact, firstAddressInNotes);
                    break;
            }
        }
    }

    /**
     * Sets the home address
     *
     * @param address The {@link Address}
     * @param contact The {@link Contact}
     */
    private boolean setHomeAddress(Address address, Contact contact, boolean firstPhoneNumberInNote) {
        if (contact.containsAddressHome()) {
            return addAddressToNote(address, contact, firstPhoneNumberInNote);
        }
        if (address.getFormattedValue() != null) {
            contact.setAddressHome(address.getFormattedValue());
        }
        contact.setCityHome(address.getCity());
        contact.setCountryHome(address.getCountry());
        contact.setPostalCodeHome(address.getPostalCode());
        contact.setStateHome(address.getRegion());
        contact.setStreetHome(address.getStreetAddress());
        return firstPhoneNumberInNote;
    }

    /**
     * Sets the business address.
     *
     * @param address The {@link Address}
     * @param contact The {@link Contact}
     */
    private boolean setBusinessAddress(Address address, Contact contact, boolean firstPhoneNumberInNote) {
        if (contact.containsAddressBusiness()) {
            return addAddressToNote(address, contact, firstPhoneNumberInNote);
        }
        if (address.getFormattedValue() != null) {
            contact.setAddressBusiness(address.getFormattedValue());
        }
        contact.setCityBusiness(address.getCity());
        contact.setCountryBusiness(address.getCountry());
        contact.setPostalCodeBusiness(address.getPostalCode());
        contact.setStateBusiness(address.getRegion());
        contact.setStreetBusiness(address.getStreetAddress());
        return firstPhoneNumberInNote;
    }

    /**
     * Sets the other address
     *
     * @param address The {@link Address}
     * @param contact The {@link Contact}
     */
    private boolean setOtherAddress(Address address, Contact contact, boolean firstPhoneNumberInNote) {
        if (contact.containsAddressBusiness()) {
            return addAddressToNote(address, contact, firstPhoneNumberInNote);
        }
        if (address.getFormattedValue() != null) {
            contact.setAddressOther(address.getFormattedValue());
        }
        contact.setCityOther(address.getCity());
        contact.setCountryOther(address.getCountry());
        contact.setPostalCodeOther(address.getPostalCode());
        contact.setStateOther(address.getRegion());
        contact.setStreetOther(address.getStreetAddress());
        return firstPhoneNumberInNote;
    }

    /**
     * Adds the address to the contact's note
     *
     * @param address The {@link Address}
     * @param contact The {@link Contact}
     */
    private boolean addAddressToNote(Address address, Contact contact, boolean firstPhoneNumberInNotes) {
        String formattedAddress = address.getFormattedValue();
        if (address.getFormattedValue() == null) {
            return firstPhoneNumberInNotes;
        }
        return addValueToNote(contact, ContactNoteStrings.OTHER_ADDRESSES, formattedAddress, firstPhoneNumberInNotes);
    }
}
