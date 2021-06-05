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
 * {@link UserFieldsFacet}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class UserFieldsFacet extends ContactSearchFieldFacet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5647640557042838626L;

    // @formatter:off
    static final ContactField[] USER_FIELDS = {
        ContactField.USERFIELD01, ContactField.USERFIELD02, ContactField.USERFIELD03, ContactField.USERFIELD04, ContactField.USERFIELD05, ContactField.USERFIELD06,
        ContactField.USERFIELD07, ContactField.USERFIELD08, ContactField.USERFIELD09, ContactField.USERFIELD10, ContactField.USERFIELD11, ContactField.USERFIELD12,
        ContactField.USERFIELD13, ContactField.USERFIELD14, ContactField.USERFIELD15, ContactField.USERFIELD16, ContactField.USERFIELD17, ContactField.USERFIELD18,
        ContactField.USERFIELD19, ContactField.USERFIELD20
    };
    // @formatter:on

    /**
     * Initializes a new {@link UserFieldsFacet}.
     */
    public UserFieldsFacet(String query, List<String> tokenized) {
        super(ContactsFacetType.USER_FIELDS, new FormattableDisplayItem(ContactsStrings.FACET_USER_FIELDS, query), tokenized);

    }

    @Override
    protected ContactField[] getFields() {
        return USER_FIELDS;
    }

}
