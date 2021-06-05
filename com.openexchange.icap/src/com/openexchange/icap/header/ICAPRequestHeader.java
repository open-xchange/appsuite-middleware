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
 * {@link ICAPRequestHeader} - Defines the standard ICAP request headers.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPRequestHeader extends ICAPCommonHeader {

    public static final String AUTHORIZATION = "Authorization";

    public static final String FROM = "From";

    public static final String HOST = "Host";

    public static final String REFERER = "Referer";

    public static final String USER_AGENT = "User-Agent";

    public static final String PREVIEW = "Preview";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String ALLOW = "Allow";

    /**
     * All ICAP request headers.
     */
    public static final Set<String> REQUEST_HEADERS;
    static {
        Set<String> set = new HashSet<>(8);
        set.addAll(COMMON_HEADERS);
        set.add(AUTHORIZATION);
        set.add(FROM);
        set.add(HOST);
        set.add(REFERER);
        set.add(USER_AGENT);
        set.add(PREVIEW);
        set.add(CONTENT_LENGTH);
        set.add(ALLOW);
        REQUEST_HEADERS = Collections.unmodifiableSet(set);
    }
}
