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

package com.openexchange.rest.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.session.Session;

/**
 * {@link API}. Defines an Abstract REST API class, which encapsulates a {@link Session} and a {@link RequestAndResponse} object
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @deprecated
 */
public abstract class API<S extends Session> {

    protected final S session;

    /**
     * Initializes a new {@link API}.
     * 
     * @throws OXException if the specified session is null.
     */
    public API(final S session) throws OXException {
        if (session == null) {
            throw RESTExceptionCodes.SESSION_NULL.create();
        }
        this.session = session;
    }

    /**
     * Returns the {@link Session} that this API is using.
     * 
     * @return The {@link Session} that this API is using.
     */
    public S getSession() {
        return session;
    }

    /**
     * Holds an {@link HttpUriRequest} and the associated {@link HttpResponse}.
     */
    public static final class RequestAndResponse {

        /** The request */
        public final HttpUriRequest request;

        /** The response */
        public final HttpResponse response;

        protected RequestAndResponse(HttpUriRequest request, HttpResponse response) {
            this.request = request;
            this.response = response;
        }
    }

    /**
     * Get the domain name of the API server
     * 
     * @return The domain name of the API server
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static String getServer() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }

    /**
     * Get the API version
     * 
     * @return The API version
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static int getVersion() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }

    /**
     * Get the user agent
     * 
     * @return The user agent
     * @throws IllegalStateException if the subclass of <b>this</b> class does not implement and hence shadow/hide this method
     */
    public static String getUserAgent() {
        throw new IllegalStateException("Subclass must implement and hence shadow this method.");
    }
}
