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

package com.openexchange.jsieve.export;

/**
 * {@link SieveScript} - Provides the name of a Sieve script and whether that script is currently marked as the active one.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SieveScript {

    private final String name;
    private final boolean active;

    /**
     * Initializes a new {@link SieveScript}.
     */
    public SieveScript(String name, boolean active) {
        super();
        this.name = name;
        this.active = active;
    }

    /**
     * Gets the name of the script.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether this script is currently marked as the active one.
     *
     * @return <code>true</code> if active; otherwise <code>false</code>
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        builder.append("active=").append(active).append(']');
        return builder.toString();
    }

}
