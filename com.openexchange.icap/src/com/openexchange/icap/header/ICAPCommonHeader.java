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

package com.openexchange.icap.header;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ICAPCommonHeader} - Defines the standard ICAP headers that are common for both requests
 * and responses.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.1">RFC-3507, Section 4.3.1</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPCommonHeader {

    public static final String CACHE_CONTROL = "Cache-Control";

    public static final String CONNECTION = "Connection";

    public static final String DATE = "Date";

    public static final String EXPIRES = "Expires";

    public static final String PRAGMA = "Pragma";

    public static final String TRAILER = "Trailer";

    public static final String UPGRADE = "Upgrade";

    /**
     * The offset of each encapsulated section's start relative to the start
     * of the encapsulating message's body is noted using this header.
     * For example, the header
     * 
     * <pre>Encapsulated: req-hdr=0, res-hdr=13, res-body=37</pre>
     * 
     * indicates a message that encapsulates a group of request headers, a
     * group of response headers, and then a response body. Each of these
     * is included at the byte-offsets listed.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.4.1">RFC-3507, Section 4.4.1</a>
     */
    public static final String ENCAPSULATED = "Encapsulated";

    /**
     * All ICAP headers that are common for requests and responses
     */
    public static final Set<String> COMMON_HEADERS;
    static {
        Set<String> set = new HashSet<>(8);
        set.add(CACHE_CONTROL);
        set.add(CONNECTION);
        set.add(DATE);
        set.add(EXPIRES);
        set.add(PRAGMA);
        set.add(TRAILER);
        set.add(UPGRADE);
        set.add(ENCAPSULATED);
        COMMON_HEADERS = Collections.unmodifiableSet(set);
    }
}
