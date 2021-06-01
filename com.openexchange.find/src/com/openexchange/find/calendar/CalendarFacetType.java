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

package com.openexchange.find.calendar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;

/**
 * {@link CalendarFacetType}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public enum CalendarFacetType implements FacetType {
    /**
     * The "subject" field facet
     */
    SUBJECT("subject", null),
    /**
     * The "email" field facet
     */
    DESCRIPTION("description", null),
    /**
     * The "location" field facet
     */
    LOCATION("location", null),
    /**
     * The "attachment" field facet
     */
    ATTACHMENT_NAME("attachment", null),
    /**
     * The "participant" facet
     */
    PARTICIPANT("participant", CalendarStrings.FACET_TYPE_PARTICIPANT),
    /**
     * The "my status" facet
     */
    STATUS("status", CalendarStrings.FACET_TYPE_STATUS),
    /**
     * The "range" facet
     */
    RANGE("range", CalendarStrings.FACET_TYPE_RANGE),
    /**
     * The "recurring type" facet
     */
    RECURRING_TYPE("type", CalendarStrings.FACET_TYPE_RECURRING_TYPE),
    ;

    private final String id;
    private final String displayName;
    private final List<FacetType> conflictingFacets = new LinkedList<FacetType>();

    /**
     * Initializes a new {@link CalendarFacetType}.
     *
     * @param id The identifier of this facet
     * @param displayName The display name, or <code>null</code> if not relevant.
     */
    private CalendarFacetType(String id, String displayName) {
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

    private static final Map<String, CalendarFacetType> typesById = new HashMap<String, CalendarFacetType>();
    static {
        for (CalendarFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

    /**
     * Gets a {@link CalendarFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static CalendarFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        return typesById.get(id);
    }

}
