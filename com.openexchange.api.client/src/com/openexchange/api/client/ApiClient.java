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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client;

import java.net.URL;
import org.apache.http.client.methods.HttpRequestBase;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;

/**
 * {@link ApiClient} - A client for communicating to another Open Xchange server.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface ApiClient {

    /**
     * Get the local context identifier the client belongs to
     *
     * @return The context ID
     */
    int getContextId();

    /**
     * Get the local user identifier the access belongs to
     *
     * @return The user ID
     */
    int getUserId();

    /**
     * Get the link to the login or rather the URL for the remote App Suite instance
     *
     * @return The initial link, e.g. <code>share.appsuite.example.org/share/1123</code>
     */
    URL getLoginLink();

    /**
     * Get information that were acquired during login
     *
     * @return The information or <code>null</code> if the client is not yet logged in
     */
    @Nullable
    LoginInformation getLoginInformation();

    /**
     * Performs a login on the remote system
     *
     * @throws OXException in case of error, specifically a {@link ApiClientExceptions#NO_ACCESS} if the login fails
     */
    void login() throws OXException;

    /**
     * Performs a logout on the remote system.
     * <p>
     * Any error during the logout process will be logged
     */
    void logout();

    /**
     * Gets a value indicating whether this client is closed and or if it can serve requests.
     * <p>
     * If the client is closed, any call of {@link #execute(HttpRequestBase, HttpResponseParser)} will fail
     * with an exception.
     *
     * @return <code>true</code> if the client has been closed, <code>false</code> otherwise
     */
    boolean isClosed();

    /**
     * Executes the given request and parses it to the desired object
     *
     * @param <T> The type of the return value
     * @param request The request to execute
     * @return The parsed object
     * @throws OXException In case request can't be executed or parsing fails
     */
    <T> T execute(ApiCall<T> request) throws OXException;

    /**
     * Executes the given request and parses it to the desired object
     *
     * @param <T> The type of the return value
     * @param request The request to execute. The request will be closed by this method.
     * @param parser The parser for the response, nor null if no parsing should be done
     * @return The parsed object
     * @throws OXException In case request can't be executed or parsing fails
     */
    <T> T execute(HttpRequestBase request, HttpResponseParser<T> parser) throws OXException;
}
