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

package com.openexchange.find;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.java.Strings;

/**
 * A {@link Module} defines a component that contributes a search implementation.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public enum Module {

    /**
     * Mail module.
     */
    MAIL("mail"),
    /**
     * Contacts module.
     */
    CONTACTS("contacts"),
    /**
     * Calendar module.
     */
    CALENDAR("calendar"),
    /**
     * Tasks module.
     */
    TASKS("tasks"),
    /**
     * Drive module.
     */
    DRIVE("drive"),
    /**
     * Resource module.
     */
    RESOURCE("resource"),
    /**
     * Group module
     */
    GROUPS("groups");

    private static final Map<String, Module> modulesByName = new HashMap<String, Module>();
    static {
        for (Module module : values()) {
            modulesByName.put(module.getIdentifier(), module);
        }
        modulesByName.put("infostore", DRIVE);
        modulesByName.put("files", DRIVE);
    }

    private final String identifier;

    private Module(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    // ---------------------------------------------------------------------------------------- //

    /**
     * Gets the appropriate {@link Module} for specified identifier.
     *
     * @param identifier The identifier to look-up with
     * @return The appropriate module or <code>null</code>
     */
    public static Module moduleFor(final String identifier) {
        if (Strings.isEmpty(identifier)) {
            return null;
        }

        return modulesByName.get(identifier.toLowerCase().trim());
    }

}
