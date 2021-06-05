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

package com.openexchange.pop3.util;

import com.planetj.math.rabinhash.RabinHashFunction64;

/**
 * {@link UIDUtil} - UID utility class for POP3 bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UIDUtil {

    /**
     * Initializes a new {@link UIDUtil}.
     */
    private UIDUtil() {
        super();
    }

    /**
     * Computes the Rabin hash value of specified UID string.
     *
     * @param uidl The UID string as received from POP3 UIDL command
     * @return The Rabin hash value
     */
    public static long uid2long(final String uidl) {
        return RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(uidl);
    }

    /**
     * Computes the Rabin hash value of specified UID strings.
     *
     * @param uidls The UID strings as received from POP3 UIDL command
     * @return The Rabin hash values
     */
    public static long[] uid2long(final String[] uidls) {
        if (null == uidls) {
            return null;
        }
        final long[] longs = new long[uidls.length];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = uid2long(uidls[i]);
        }
        return longs;
    }

}
