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

package com.openexchange.mail.utils;

/**
 * {@link DisplayMode} - The display mode
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DisplayMode {

    /**
     * Generate a version as-is.
     */
    RAW,
    /**
     * Generate a version for a message forward/reply/draft-edit in front-end
     */
    MODIFYABLE,
    /**
     * Generate a version for being displayed in front-end
     */
    DISPLAY,
    /**
     * Generates the document view for associated message's body
     */
    DOCUMENT,
    ;

    /**
     * Gets the mode's integer value
     *
     * @return The mode's integer value
     */
    public int getMode() {
        return ordinal();
    }

    /**
     * Indicates if this mode is included in given mode
     *
     * @param other The other mode
     * @return <code>true</code> if this mode is included in given mode; otherwise <code>false</code>
     */
    public boolean isIncluded(DisplayMode other) {
        return ordinal() <= other.ordinal();
    }

}
