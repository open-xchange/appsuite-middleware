/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
