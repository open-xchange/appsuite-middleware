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

package com.openexchange.login;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.Cookie;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Data to process a login request.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface LoginRequest {

    String getLogin();

    String getPassword();

    String getClientIP();

    String getUserAgent();

    String getAuthId();

    /**
     * The client is used for reporting issues to known which client used the backend. This can be OX6 frontend, AppSuite frontend, some
     * OXtender identification, somebody's own client identification and so on. Additionally the client is used to separate cookies in the
     * same cookie store for different client specific sessions.
     * Especially with basic authentication methods like for some WebDAV interface this string can be <code>null</code>.
     * @return the client identification or <code>null</code>.
     */
    String getClient();

    String getVersion();

    String getHash();

    /**
     * The client token will only be present when the token login is used. This attribute does not apply to any other login mechanism.
     * @return the client token from the token login. Otherwise <code>null</code>.
     */
    String getClientToken();

    Interface getInterface();

    Map<String, List<String>> getHeaders();

    /**
     * Gets the parameters associated with the HTTP login request (if any).
     *
     * @return The parameters or an empty map
     */
    Map<String, String[]> getRequestParameter();

    Cookie[] getCookies();

    /**
     * Every login mechanism defining this value must consider the following topics: <li>
     * <ul>
     * if com.openexchange.forceHTTPS is configured to true, then this must be true but not if the client comes from the local installation
     * like USM
     * </ul>
     * <ul>
     * the value told by the servlet container if the request was retrieved through HTTPS
     * </ul>
     * </li>
     *
     * @see Tools#considerSecure(javax.servlet.http.HttpServletRequest, boolean)
     * @return <code>true</code> if URLs should be told to the client with HTTPS.
     */
    boolean isSecure();

    String getServerName();

    int getServerPort();

    String getHttpSessionID();

    /**
     * Propagates authenticated status to request-associated HTTP session.
     *
     * @return <code>true</code> if status has been successfully propagated; otherwise <code>false</code> (if no such HTTP session is available)
     */
    boolean markHttpSessionAuthenticated();

    /**
     * Gets a value indicating whether the session should be created in a transient way or not, i.e. the session should not be distributed
     * to other nodes in the cluster or put into another persistent storage.
     *
     * @return <code>true</code> if the session should be transient, <code>false</code>, otherwise
     */
    boolean isTransient();

    String getLanguage();

    boolean isStoreLanguage();

    String getLocale();

    boolean isStoreLocale();

    /**
     * Gets the value of 'staySignedIn' parameter
     *
     * @return <code>true</code> if parameter was set, <code>false</code> if not
     */
    boolean isStaySignedIn();

}
