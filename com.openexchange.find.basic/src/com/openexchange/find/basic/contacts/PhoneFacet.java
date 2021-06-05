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
 * {@link PhoneFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PhoneFacet extends ContactSearchFieldFacet {

    private static final long serialVersionUID = -9131205652463933031L;

    static final ContactField[] PHONE_FIELDS = {
        ContactField.TELEPHONE_ASSISTANT, ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2,
        ContactField.TELEPHONE_CALLBACK, ContactField.TELEPHONE_CAR, ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1,
        ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP, ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_OTHER,
        ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY, ContactField.TELEPHONE_RADIO, ContactField.TELEPHONE_TELEX,
        ContactField.TELEPHONE_TTYTDD, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2
    };

    /**
     * Initializes a new {@link PhoneFacet}.
     *
     * @param query The query to insert into the display item
     * @param tokenized The tokenized query to insert into the filter
     */
    public PhoneFacet(String query, List<String> tokenized) {
        super(ContactsFacetType.PHONE, new FormattableDisplayItem(ContactsStrings.FACET_PHONE, query), tokenized);
    }

    @Override
    protected ContactField[] getFields() {
        return PHONE_FIELDS;
    }

}
