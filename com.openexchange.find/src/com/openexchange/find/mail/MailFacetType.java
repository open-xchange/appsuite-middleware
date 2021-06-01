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

package com.openexchange.find.mail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.find.facet.FacetType;
import com.openexchange.java.Strings;


/**
 * Facet types for the mail module.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public enum MailFacetType implements FacetType {

    SUBJECT,
    MAIL_TEXT,
    CONTACTS(MailStrings.FACET_PEOPLE),
    FILENAME,
    HAS_ATTACHMENT(MailStrings.FACET_ATTACHMENT),
    ;

    private static final Map<String, MailFacetType> typesById = new HashMap<String, MailFacetType>();
    static {
        for (MailFacetType type : values()) {
            typesById.put(type.getId(), type);
        }
    }

    private final String displayName;

    private final List<FacetType> conflictingFacets = new LinkedList<FacetType>();

    private MailFacetType() {
        this(null);
    }

    private MailFacetType(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return toString().toLowerCase();
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
     * Gets a {@link MailFacetType} by its id.
     * @return The type or <code>null</code>, if the id is invalid.
     */
    public static MailFacetType getById(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        return typesById.get(id);
    }

}
