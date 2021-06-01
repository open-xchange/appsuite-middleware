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

package com.openexchange.subscribe.google.parser.consumers;
import static com.openexchange.java.Autoboxing.b;
import java.util.function.BiConsumer;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PostalAddress;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.openexchange.groupware.container.Contact;

/**
 * {@link StructuredPostalAddressConsumer} - Parses the contact's postal addresses. Note that google
 * can store an unlimited mount of postal addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a home, business and other
 * address, therefore we only fetch the first three we encounter. Furthermore, we set as home
 * address the {@link PostalAddress} that is marked as primary.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class StructuredPostalAddressConsumer implements BiConsumer<ContactEntry, Contact> {

    /**
     * Initialises a new {@link StructuredPostalAddressConsumer}.
     */
    public StructuredPostalAddressConsumer() {
        super();
    }

    @Override
    public void accept(ContactEntry t, Contact u) {
        if (!t.hasStructuredPostalAddresses()) {
            return;
        }
        int count = 0;
        for (StructuredPostalAddress spa : t.getStructuredPostalAddresses()) {
            if (b(spa.getPrimary())) {
                setHomeAddress(spa, u);
                continue;
            }
            switch (count++) {
                case 0:
                    setHomeAddress(spa, u);
                    break;
                case 1:
                    setBusinessAddress(spa, u);
                    break;
                case 2:
                    setOtherAddress(spa, u);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * Sets the home address
     * 
     * @param spa The {@link StructuredPostalAddress}
     * @param u The {@link Contact}
     */
    private void setHomeAddress(StructuredPostalAddress spa, Contact u) {
        if (spa.hasFormattedAddress()) {
            u.setAddressHome(spa.getFormattedAddress().getValue());
            return;
        }
        if (spa.hasStreet()) {
            u.setAddressHome(spa.getStreet().getValue());
        }
        if (spa.hasPostcode()) {
            u.setPostalCodeHome(spa.getPobox().getValue());
        }
        if (spa.hasCity()) {
            u.setCityHome(spa.getCity().getValue());
        }
        if (spa.hasCountry()) {
            u.setCountryHome(spa.getCountry().getValue());
        }
    }

    /**
     * Sets the business address
     * 
     * @param spa The {@link StructuredPostalAddress}
     * @param u The {@link Contact}
     */
    private void setBusinessAddress(StructuredPostalAddress spa, Contact u) {
        if (spa.hasFormattedAddress()) {
            u.setAddressBusiness(spa.getFormattedAddress().getValue());
            return;
        }

        if (spa.hasStreet()) {
            u.setAddressBusiness(spa.getStreet().getValue());
        }
        if (spa.hasPostcode()) {
            u.setPostalCodeBusiness(spa.getPobox().getValue());
        }
        if (spa.hasCity()) {
            u.setCityBusiness(spa.getCity().getValue());
        }
        if (spa.hasCountry()) {
            u.setCountryBusiness(spa.getCountry().getValue());
        }
    }

    /**
     * Sets the other address
     * 
     * @param spa The {@link StructuredPostalAddress}
     * @param u The {@link Contact}
     */
    private void setOtherAddress(StructuredPostalAddress spa, Contact u) {
        if (spa.hasFormattedAddress()) {
            u.setAddressOther(spa.getFormattedAddress().getValue());
            return;
        }
        if (spa.hasStreet()) {
            u.setAddressOther(spa.getStreet().getValue());
        }
        if (spa.hasPostcode()) {
            u.setPostalCodeOther(spa.getPobox().getValue());
        }
        if (spa.hasCity()) {
            u.setCityOther(spa.getCity().getValue());
        }
        if (spa.hasCountry()) {
            u.setCountryOther(spa.getCountry().getValue());
        }
    }
}
