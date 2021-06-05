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

package com.openexchange.charset;

import java.nio.charset.Charset;
import com.openexchange.charset.internal.CharsetServiceUtility;

/**
 * {@link CharsetServices} - Utility class for charset service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CharsetServices {

    /**
     * Initializes a new {@link CharsetServices}.
     */
    private CharsetServices() {
        super();
    }

    /**
     * Gets the ISO-8859-1 character set.
     *
     * @return The ISO-8859-1 character set
     */
    public static Charset getIso8859Charset() {
        Charset iso8859cs = CharsetServiceUtility.getIso8859CS();
        return null == iso8859cs ? Charset.forName("ISO-8859-1") : iso8859cs;
    }

}
