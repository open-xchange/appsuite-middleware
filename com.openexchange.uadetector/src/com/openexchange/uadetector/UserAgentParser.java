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

package com.openexchange.uadetector;

import com.openexchange.osgi.annotation.SingletonService;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;


/**
 * {@link UserAgentParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
@SingletonService
public interface UserAgentParser {

    /**
     * Parses and analyzes a <code>User-Agent</code> string sent along with an <code>HttpServletRequest</code>, wrapping the result into
     * a readable user agent structure.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @return The readable user agent, or a placeholder instance if unknown
     * @see UserAgentStringParser#parse(String)
     */
    ReadableUserAgent parse(String userAgent);

    /**
     * Gets a value indicating whether a user agent string matches a specific user agent family.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @param family The user agent family to match
     * @return <code>true</code> if the user agent family matches, <code>false</code>, otherwise
     */
    boolean matches(String userAgent, UserAgentFamily family);

    /**
     * Gets a value indicating whether a user agent string matches the supplied user agent family in a specific major version.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @param family The user agent family to match
     * @param majorVersion The major (first) version number to match
     * @return <code>true</code> if the user agent family and major version matches, <code>false</code>, otherwise
     */
    boolean matches(String userAgent, UserAgentFamily family, int majorVersion);

}
