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

package com.openexchange.find.contacts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;

/**
 * The {@link FacetType}s for the contacts module.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public enum ContactsFacetType implements FacetType {

    /**
     * The "name" field facet
     */
    NAME("name", null),
    /**
     * The "email" field facet
     */
    EMAIL("email", null),
    /**
     * The "phone" field facet
     */
    PHONE("phone", null),
    /**
     * The "address" field facet
     */
    ADDRESS("address", null),
    /**
     * The "contact type" facet
     */
    CONTACT_TYPE("contact_type", ContactsStrings.FACET_TYPE_CONTACT_TYPE),
    /**
     * The "contact" facet
     */
    CONTACT("contact", ContactsStrings.FACET_TYPE_CONTACT),
    /**
     * The "department" facet
     */
    DEPARTMENT("department", null),
    /**
     * The "user fields" facet
     */
    USER_FIELDS("user_fields", null)
    ;

    private final String id;
    private final String displayName;
    private final List<FacetType> conflictingFacets = new LinkedList<>();

    /**
     * Initializes a new {@link ContactsFacetType}.
     *
     * @param id The identifier of this facet
     * @param displayName The display name, or <code>null</code> if not relevant.
     */
    private ContactsFacetType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<FacetType> getConflictingFacets() {
        return conflictingFacets;
    }

    /**
     * Gets a {@link ContactsFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static ContactsFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        return typesById.get(id);
    }

    private static final Map<String, ContactsFacetType> typesById = new HashMap<>();
    static {
        for (ContactsFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

}
