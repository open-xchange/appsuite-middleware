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
