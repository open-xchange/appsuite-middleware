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
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link ContactSearchFieldFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactSearchFieldFacet extends SimpleFacet {

    private static final long serialVersionUID = 2919108856076038573L;

    /**
     * Initializes a new {@link ContactSearchFieldFacet}.
     *
     * @param type The facet type
     * @param displayItem The display item representing the facet
     * @param query The query to insert into the filter
     */
    protected ContactSearchFieldFacet(ContactsFacetType type, DisplayItem displayItem, List<String> queries) {
        super(type, displayItem, Filter.of(type.getId(), queries));
    }

    /**
     * Gets the contact fields used for comparisons in the search term.
     *
     * @return The contact fields used by the facet
     */
    protected abstract ContactField[] getFields();
}
