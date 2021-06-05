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

package com.openexchange.antivirus;

import java.util.Map;
import java.util.Optional;

/**
 * {@link AntiVirusEncapsulatedContent} - Used to encapsulate additional HTTP content, such
 * as the original HTTP request/response headers to the ICAP request.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.4">RFC-3507, Section 4.4</a>
 */
public interface AntiVirusEncapsulatedContent {

    /**
     * Returns the optional original HTTP request, e.g. <code>GET /api/files/someFile.jpg HTTP/1.1</code>
     *
     * @return the optional original HTTP request
     */
    Optional<String> getOriginalRequest();

    /**
     * Returns a map with the original request headers
     *
     * @return a map with the original request headers
     *         or an empty map
     */
    Map<String, String> getOriginalRequestHeaders();

    /**
     * Returns (if available) the original response's status line,
     * e.g. <code>HTTP/1.1 200 OK</code>
     *
     * @return The optional response's status line
     */
    Optional<String> getOriginalResponseLine();

    /**
     * Returns the optional original response's headers
     *
     * @return The optional original response's headers
     *         or an empty map
     */
    Map<String, String> getOriginalResponseHeaders();
}
