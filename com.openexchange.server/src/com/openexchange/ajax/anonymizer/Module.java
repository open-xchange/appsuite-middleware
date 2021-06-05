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

package com.openexchange.ajax.anonymizer;

import com.openexchange.java.Strings;

/**
 * {@link Module} - A module to anonymize.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Module {

    /**
     * The user contact module
     */
    CONTACT("contact"),
    /**
     * The user module
     */
    USER("user"),
    /**
     * The group module
     */
    GROUP("group"),
    /**
     * The resource module
     */
    RESOURCE("resource"),
    ;

    private static final Module[] VALUES = Module.values();

    // --------------------------------------------------------------------------------------------------------------------------- //

    private final String name;

    Module(String name) {
        this.name = name;
    }

    /**
     * Gets the module string.
     *
     * @return The module string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the module for given module name
     *
     * @param name The module name to look-up by
     * @return The associated module or <code>null</code>
     */
    public static Module moduleFor(String name) {
        if (null == name) {
            return null;
        }
        String id = Strings.asciiLowerCase(name);
        for (Module m : VALUES) {
            if (id.equals(m.name)) {
                return m;
            }
        }
        return null;
    }

}
