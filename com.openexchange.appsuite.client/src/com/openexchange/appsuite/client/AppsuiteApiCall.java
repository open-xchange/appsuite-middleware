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

package com.openexchange.appsuite.client;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;

/**
 * {@link AppsuiteApiCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The class of the response
 * @since v7.10.5
 */
public interface AppsuiteApiCall<T> {

    /**
     * The static session identifier to be added to most outgoing API calls
     */
    static final String SESSION = "session";

    /**
     * The HTTP method to use
     *
     * @return The HTTP method
     */
    @NonNull
    HttpMethods getHttpMehtod();

    /**
     * Gets a value indicating whether the session identifier as per {@link #SESSION} should be added to the
     * outgoing request within the path or not
     * <p>
     * If set to true e.g. <code>session=942bcfcfdf934e11b9e5884e05fc5fbe</code> will be added to
     * <code>exapmle.org/api/whoami</code> resulting in <code>exapmle.org/api/whoami?session=942bcfcfdf934e11b9e5884e05fc5fbe</code>
     *
     * @return <code>true</code> to append the session ID, <code>false</code> otherwise
     */
    default boolean appendSessionToPath() {
        return true;
    }

    /**
     * Gets a value indicating whether the API prefix should be added to the path or not.
     * <p>
     * If set to <code>true</code> e.g. <code>/appsuite/api</code> will be added before the path
     * retrieved by {@link #getPath()}. The actual prefix of the path can be different on each host,
     * therefore only the client executing the call can add this dynamically
     *
     * @return <code>true</code> to append the prefix, <code>false</code> otherwise
     */
    default boolean appendPathPrefix() {
        return true;
    }

    /**
     * Get the path the request shall be made to
     * <p>
     * Please note that the domain part will be added by the client. This means
     * e.g. <code>/system</code> is sufficient
     *
     * @return The path, never <code>null</code> or empty string
     */
    @NonNull
    String getPath();

    /**
     * Get the parameters to add to the path on the outgoing request
     *
     * @return A map with parameters, can be empty
     */
    @NonNull
    Map<String, String> getPathParameters();

    /**
     * Get the body to add.
     * <p>
     * Will only be called if the method allows bodies to be sent
     *
     * @return The body as {@link HttpEntity}
     * @throws OXException In case body can't be generated
     */
    @Nullable
    HttpEntity getBody() throws OXException;

    /**
     * Parses a HTTP response to the desired object
     *
     * @param response The HTTP response to parse
     * @param httpContext The HTTP context with additional information
     * @return The desired object
     * @throws OXException
     */
    T parse(HttpResponse response, HttpContext httpContext) throws OXException;

}
