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

package com.openexchange.api.client;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;

/**
 * {@link ApiCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The class of the response
 * @since v7.10.5
 */
public interface ApiCall<T> {

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
     * Gets a value indicating whether the dispatcher prefix should be added to the path or not.
     * <p>
     * If set to <code>true</code> e.g. <code>/appsuite/api</code> will be added before the path
     * retrieved by {@link #getModule()}. The actual prefix of the path can be different on each host,
     * therefore only the client executing the call can add this dynamically
     *
     * @return <code>true</code> to append the prefix, <code>false</code> otherwise
     */
    default boolean appendDispatcherPrefix() {
        return true;
    }

    /**
     * Get the targeted module of the HTTP API, e.g. <code>group</code>.
     *
     * @return The module identifier, never <code>null</code> or empty string
     */
    @NonNull
    String getModule();

    /**
     * Get the parameters to add to the path on the outgoing request
     *
     * @return A map with parameters, can be empty
     */
    @NonNull
    Map<String, String> getPathParameters();

    /**
     * Get the Headers to add to the outgoing request
     *
     * @return A map with headers, can be empty
     */
    @NonNull
    Map<String, String> getHeaders();

    /**
     * Get the body to add.
     * <p>
     * Will only be called if the method allows bodies to be sent
     *
     * @return The body as {@link HttpEntity}
     * @throws OXException In case body can't be generated
     * @throws JSONException In case JSON parsing fails
     */
    @Nullable
    HttpEntity getBody() throws OXException, JSONException;

    /**
     * Returns a parser to parse the HTTP response
     *
     * @return The parser
     */
    HttpResponseParser<T> getParser();

}
