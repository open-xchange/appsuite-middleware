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

package com.openexchange.find.basic.contacts;

import java.util.List;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.contacts.ContactsStrings;
import com.openexchange.find.facet.FormattableDisplayItem;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link AddressFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddressFacet extends ContactSearchFieldFacet {

    private static final long serialVersionUID = -9031103652462933031L;

    static final ContactField[] ADDRESS_FIELDS = {
        ContactField.STREET_BUSINESS, ContactField.STREET_HOME, ContactField.STREET_OTHER,
        ContactField.POSTAL_CODE_BUSINESS, ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_OTHER,
        ContactField.CITY_BUSINESS, ContactField.CITY_HOME, ContactField.CITY_OTHER,
        ContactField.STATE_BUSINESS, ContactField.STATE_HOME, ContactField.STATE_OTHER,
        ContactField.COUNTRY_BUSINESS, ContactField.COUNTRY_HOME, ContactField.COUNTRY_OTHER,
    };

    /**
     * Initializes a new {@link AddressFacet}.
     *
     * @param query The query to insert into the display item
     * @param tokenized The tokenized query to insert into the filter
     */
    public AddressFacet(String query, List<String> tokenized) {
        super(ContactsFacetType.ADDRESS, new FormattableDisplayItem(ContactsStrings.FACET_ADDRESS, query), tokenized);
    }

    @Override
    protected ContactField[] getFields() {
        return ADDRESS_FIELDS;
    }

}
