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

package com.openexchange.imap.entity2acl;


/**
 * {@link CachedString}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
class CachedString {

    /** The special wrapper for absence */
    static final CachedString NIL = new CachedString(null);

    /**
     * Creates a wrapper for specified string.
     *
     * @param string The string to wrap
     * @return The wrapper instance
     */
    static CachedString wrapperFor(String string) {
        return null == string ? NIL : new CachedString(string);
    }

    // ----------------------------------------------------------------------------------------------------

    /** The wrapped string */
    final String string;

    /**
     * Initializes a new {@link CachedString}.
     */
    private CachedString(String string) {
        super();
        this.string = string;
    }

    @Override
    public String toString() {
        return null == string ? "NIL" : string;
    }
}
