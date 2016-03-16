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
     * Gets a value indicating whether the session should be created in a transient way or not, i.e. the session should not be distributed
     * to other nodes in the cluster or put into another persistent storage.
     *
     * @return <code>true</code> if the session should be transient, <code>false</code>, otherwise
     */
    boolean isTransient();

    String getLanguage();

    boolean isStoreLanguage();

}
