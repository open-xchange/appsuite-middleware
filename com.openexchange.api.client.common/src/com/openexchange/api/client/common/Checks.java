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

package com.openexchange.api.client.common;

import static com.openexchange.api.client.common.OXExceptionParser.parseException;
import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.exception.OXException;

/**
 * {@link Checks} - Util class with checks for HTTP response content
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class Checks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Checks.class);

    /**
     * Initializes a new {@link Checks}.
     */
    private Checks() {}

    /**
     * Handles a status error. Either will throw the transmitted exception
     * or will generate an appropriated exception if the status code implies
     * an client or server error.
     *
     * @param response The response
     * @throws OXException The OXException to throw for the status code
     */
    public static void checkStatusError(HttpResponse response) throws OXException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < HttpStatus.SC_BAD_REQUEST) {
            // Nothing to do, pass response to caller
            return;
        }
        OXException parsedException = null;
        try {
            JSONValue json = JSONUtils.getJSON(response);
            if (null != json && json instanceof JSONObject) {
                parsedException = parseException((JSONObject) json);
            }
        } catch (Exception e) {
            LOGGER.debug("Error while getting error stack", e);
        }

        if (null != parsedException) {
            throw parsedException;
        }

        if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw ApiClientExceptions.CLIENT_ERROR.create(I(statusCode));
        }
        throw ApiClientExceptions.REMOTE_SERVER_ERROR.create(I(statusCode));
    }

    /**
     * Check that the request targets the same origin as the given URL
     *
     * @param request The HTTP request
     * @param target The desired target
     * @throws OXException In case the domain doesn't match
     */
    public static void checkSameOrigin(HttpRequestBase request, URL target) throws OXException {
        if (null == request.getURI() || false == request.getURI().getHost().equals(target.getHost())) {
            throw ApiClientExceptions.NOT_SAME_ORIGIN.create(request.getURI(), target.getHost());
        }
    }

    /**
     * Checks that the redirect target is still on the same server
     *
     * @param originHost The original targeted host
     * @param redirectTarget The redirect to follow
     * @throws OXException In case the new target is invalid or redirects to another host
     */
    public static void checkSameOrigin(URL originHost, String redirectTarget) throws OXException {
        try {
            //relative or absolute redirect?
            URL redirect = redirectTarget.startsWith("/") ? new URL(originHost, redirectTarget) : new URL(redirectTarget);
            if (null != redirect.getHost()) {
                if (false == originHost.getHost().equals(redirect.getHost())) {
                    throw ApiClientExceptions.NOT_SAME_ORIGIN.create(redirect.getHost(), originHost.getHost());
                }
            }
        } catch (MalformedURLException e) {
            throw ApiClientExceptions.INVALID_TARGET.create(e, redirectTarget);
        }
    }

}
