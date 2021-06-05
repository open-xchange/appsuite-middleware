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

package com.openexchange.tools.oxfolder;

import java.io.Serializable;

/**
 * {@link GABMode} - Defines a set of different modes for the GABs permission handling
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public enum GABMode implements Serializable {

    /**
     * The modus <i>global</i>.
     * <p>
     * If this modus is chosen for a context, the all user group will be added
     * to the global address book (GAB) permissions instead of each user.
     *
     * @see {@link com.openexchange.group.GroupStorage#GROUP_ZERO_IDENTIFIER}
     */
    GLOBAL,

    /**
     * The modus <i>individual</i>.
     * <p>
     * If this modus is chosen for a context, each user will be added to the
     * permission set of the global address book (GAB) resulting in a dedicated
     * row in the <code>oxfolder_permission</code> table
     */
    INDIVIDUAL

    ;

    /**
     * Gets a value describing whether the given string can be seen as equal to the
     * enum constant or not
     *
     * @param mode The mode to check
     * @return <code>true</code> if the mode can be seen as equal, <code>false</code> otherwise
     */
    public boolean equalsMode(String mode) {
        return null == mode ? false : name().equalsIgnoreCase(mode.trim());
    }

    /**
     * Get the {@link GABMode} fitting to the given string
     *
     * @param mode The mode
     * @return A {@link GABMode} fitting to the string or <code>null</code>
     */
    public static GABMode of(String mode) {
        for (GABMode gabMode : values()) {
            if (gabMode.equalsMode(mode)) {
                return gabMode;
            }
        }
        return null;
    }
}
