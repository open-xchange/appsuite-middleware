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

    ;

    private final String id;
    private final String displayName;
    private final List<FacetType> conflictingFacets = new LinkedList<FacetType>();

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

    private static final Map<String, ContactsFacetType> typesById = new HashMap<String, ContactsFacetType>();
    static {
        for (ContactsFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

}
