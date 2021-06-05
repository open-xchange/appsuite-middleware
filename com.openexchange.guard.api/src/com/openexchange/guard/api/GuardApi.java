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

package com.openexchange.guard.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link GuardApi} - Provides access to the OX Guard end-point.
 * <p>
 * Code sample:
 * <pre style="background-color:#FFDDDD;">
 * HttpServletRequest request = ...;
 * Session session = ...;
 * GuardApi guardApi = ...;
 *
 * // Initialize cookies
 * List&lt;Cookie&gt; userAuthCookies = GuardApis.extractCookiesFrom(request, session);
 *
 * // Initialize headers
 * String userAgent = request.getHeader("User-Agent");
 * List&lt;Header&gt; headers = null == userAgent ? Collections.&lt;Header&gt; emptyList() : Collections.singletonList(new Header("User-Agent", userAgent));
 *
 * // Initialize parameters
 * Map&lt;String, String&gt; parameters = GuardApis.mapFor("arg01", "val01", "arg02", "val02");
 *
 * // Request the decrypted resource
 * InputStream requestedResource = guardApi.requestSessionSensitiveResource(parameters, session, userAuthCookies, headers);
 * ...
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
@SingletonService
public interface GuardApi {

    /**
     * Performs the GET using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If GET fails
     * @see GuardApis#mapFor(String...)
     */
    <R> R doCallGet(Map<String, String> parameters, Class<? extends R> clazz, Session session) throws OXException;

    /**
     * Performs a POST using given parameters.
     *
     * @param parameters
     * @param bodyParameters
     * @param clazz The return type
     * @return The data
     * @throws OXException
     * @see GuardApis#mapFor(String...)
     */
    public <R> R doCallPost(Map<String, String> parameters, Map<String, String> bodyParameters, Class<? extends R> clazz, Session session) throws OXException;

    /**
     * Performs the PUT using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If PUT fails
     * @see GuardApis#mapFor(String...)
     */
    <R> R doCallPut(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz, Session session) throws OXException;

    /**
     * Requests a resource from Guard end-point.
     *
     * @param parameters The request parameters
     * @return The resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     */
    InputStream requestResource(Map<String, String> parameters, Session session) throws OXException;

    /**
     * Requests Guard end-point to process specified resource.
     *
     * @param parameters The request parameters
     * @param resource The resource
     * @param contentType The resource's content type (optional)
     * @param name The resource's name (optional)
     * @return The processed resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     */
    InputStream processResource(Map<String, String> parameters, InputStream resource, String contentType, String name, Session session) throws OXException;

    /**
     * Requests Guard end-point to process multiply resources.
     *
     * @param parameters The request parameters
     * @param resources The resources
     * @param contentType The resources' content type (optional)
     * @return The processed resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     */
    InputStream processResources(Map<String, String> parameters, Map<String, InputStream> resources, String contentType, Session session) throws OXException;

    /**
     * Performs the GET using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @param session The associated session
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @param headers The needed headers to set; typically <code>"User-Agent"</code> header
     * @throws OXException If GET fails
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    <R> R doCallSessionSensitiveGet(Map<String, String> parameters, Class<? extends R> clazz, Session session, List<Cookie> cookies, List<Header> headers) throws OXException;


    /**
     * Performs the POST using given parameters
     *
     * @param parameters
     * @param jsonBody
     * @param clazz The return type
     * @param session The associated session
     * @param cookies The needed cookies for user authentication, typically the secret and public cookie
     * @param headers The needed headers to set, typically <code>"User-Agent"</code> header
     * @throws OXException If POST fails
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    <R> R doCallSessionSensitivePost(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz, Session session, List<Cookie> cookies, List<com.openexchange.guard.api.Header> headers) throws OXException;

    /**
     * Performs the PUT using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @param session The associated session
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @param headers The needed headers to set; typically <code>"User-Agent"</code> header
     * @throws OXException If PUT fails
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    <R> R doCallSessionSensitivePut(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz, Session session, List<Cookie> cookies, List<Header> headers) throws OXException;

    /**
     * Requests a resource from Guard end-point.
     *
     * @param parameters The request parameters
     * @param session The associated session
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @param headers The needed headers to set; typically <code>"User-Agent"</code> header
     * @return The resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    InputStream requestSessionSensitiveResource(Map<String, String> parameters, Session session, List<Cookie> cookies, List<Header> headers) throws OXException;

    /**
     * Requests a resource from Guard end-point.
     *
     * @param parameters The request parameters
     * @param resource The resource
     * @param contentType The resource's content type (optional)
     * @param name The resource's name (optional)
     * @param session The associated session
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @param headers The needed headers to set; typically <code>"User-Agent"</code> header
     * @return The resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    InputStream processSessionSensitiveResource(Map<String, String> parameters, InputStream resource, String contentType, String name, Session session, List<Cookie> cookies, List<Header> headers) throws OXException;

}
