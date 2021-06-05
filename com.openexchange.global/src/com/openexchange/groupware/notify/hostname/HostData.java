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

package com.openexchange.groupware.notify.hostname;

/**
 * Encapsulates information about the current HTTP request that is needed to generate
 * links and redirect locations and to perform hostname-based configuration lookups.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface HostData {

    /**
     * Gets the HTTP session ID (including the backend "route" suffix) as set in the underlying HTTP servlet request, e.g.
     * &lt;http-session-id&gt; + <code>"." </code>+ &lt;route&gt;.
     *
     * @return The session ID, or <code>null</code> if none was assigned
     */
    String getHTTPSession();

    /**
     * Gets the request's "route" based on the assigned HTTP session ID or this server's default backend route as configured via
     * <code>com.openexchange.server.backendRoute</code>.
     *
     * @return The backend route, e.g. <code>OX1</code>
     */
    String getRoute();

    /**
     * Gets the requested host name. This could have been determined by the HTTP request
     * itself or an optional <code>HostnameService</code>.
     *
     * @return The host name
     */
    String getHost();

    /**
     * Gets the port.
     *
     * @return The port or the well-known port according to used protocol (HTTPS vs. HTTP)
     */
    int getPort();

    /**
     * Indicates if the request was made via HTTPS.
     *
     * @return <code>true</code> for a secure connection; otherwise <code>false</code>
     */
    boolean isSecure();

    /**
     * Gets the server-side dispatcher servlet prefix, e.g. <code>/appsuite/api/</code>.
     * The prefix has always leading and trailing slashes.
     *
     * @return The servlet prefix
     */
    String getDispatcherPrefix();

}
