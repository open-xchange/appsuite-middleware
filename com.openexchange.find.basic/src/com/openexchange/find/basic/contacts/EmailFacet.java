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
 * {@link EmailFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EmailFacet extends ContactSearchFieldFacet {

    private static final long serialVersionUID = -9131103652463933031L;

    static final ContactField[] EMAIL_FIELDS = {
        ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.DISTRIBUTIONLIST
    };

    /**
     * Initializes a new {@link EmailFacet}.
     *
     * @param query The query to insert into the display item
     * @param tokenized The tokenized query to insert into the filter
     */
    public EmailFacet(String query, List<String> tokenized) {
        super(ContactsFacetType.EMAIL, new FormattableDisplayItem(ContactsStrings.FACET_EMAIL, query), tokenized);
    }

    @Override
    protected ContactField[] getFields() {
        return EMAIL_FIELDS;
    }

}
