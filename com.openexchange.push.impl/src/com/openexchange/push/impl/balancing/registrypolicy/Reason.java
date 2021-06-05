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

package com.openexchange.push.impl.balancing.registrypolicy;


/**
 * {@link Reason}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public enum Reason {

    /**
     * The owner was assigned to the push user due to initial start-up and no other member (if at all) felt responsible for that push user.
     */
    INITIAL,
    /**
     * The owner was dedicatedly assigned to that push user.
     */
    DEDICATED,
    ;

    /**
     * Gets the reason by ordinal number (its position in its enum declaration, where the initial constant is assigned an ordinal of zero).
     *
     * @param ordinal The ordinal number
     * @return The associated reason or <code>null</code>
     */
    public static Reason byOrdinal(int ordinal) {
        if (ordinal < 0) {
            return null;
        }
        Reason[] reasons = values();
        return ordinal >= reasons.length ? null : reasons[ordinal];
    }

}
