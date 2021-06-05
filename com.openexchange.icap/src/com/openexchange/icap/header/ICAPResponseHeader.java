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
 * {@link ICAPResponseHeader} - Defines the standard ICAP response headers.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.3">RFC-3507, Section 4.3.3</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPResponseHeader extends ICAPCommonHeader {

    public static final String SERVER = "Server";

    /**
     * An ISTag validates that previous ICAP server responses can still
     * be considered fresh by an ICAP client that may be caching them.
     * If a change on the ICAP server invalidates previous responses,
     * the ICAP server can invalidate portions of the ICAP client's cache
     * by changing its ISTag.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.7">RFC-3507, Section 4.7</a>
     */
    public static final String ISTAG = "ISTag";

    /**
     * All ICAP response headers.
     */
    public static final Set<String> RESPONSE_HEADERS;
    static {
        Set<String> set = new HashSet<>(8);
        set.addAll(COMMON_HEADERS);
        set.add(SERVER);
        set.add(ISTAG);
        RESPONSE_HEADERS = Collections.unmodifiableSet(set);
    }
}
