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

package com.openexchange.oauth.scope;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link OXScope} - Defines the AppSuite's available scopes/features
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum OXScope {
    mail("Mail", false),
    calendar_ro("Calendars (Read Only)", true),
    contacts_ro("Contacts (Read Only)", true),
    calendar("Calendars", false),
    contacts("Contacts", false),
    drive("Drive", true),
    generic("", true);

    private static final String modules = Strings.concat(", ", (Object[]) OXScope.values());
    private final boolean isLegacy;
    private final String displayName;

    /**
     * Initialises a new {@link OXScope}.
     */
    private OXScope(String displayName, boolean isLegacy) {
        this.displayName = displayName;
        this.isLegacy = isLegacy;
    }

    /**
     * Resolves the specified space separated string of {@link OXScope}s to an array of {@link OXScope} values
     * 
     * @param string A space separated String containing the {@link OXScope} strings
     * @return An array with the resolved {@link OXScope} values
     * @throws OXException if the specified string cannot be resolved to a valid {@link OXScope}
     */
    public static final OXScope[] valuesOf(String string) throws OXException {
        if (Strings.isEmpty(string)) {
            return new OXScope[0];
        }
        List<OXScope> list = new ArrayList<>();
        String[] split = Strings.splitByWhitespaces(string);
        for (String s : split) {
            try {
                list.add(valueOf(s));
            } catch (IllegalArgumentException e) {
                throw OAuthScopeExceptionCodes.CANNOT_RESOLVE_MODULE.create(e, s, modules);
            }
        }

        return list.toArray(new OXScope[list.size()]);
    }

    /**
     * Gets the isLegacy
     *
     * @return The isLegacy
     */
    public boolean isLegacy() {
        return isLegacy;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }
}
