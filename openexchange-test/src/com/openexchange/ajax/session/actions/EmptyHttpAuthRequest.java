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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.framework.Header;

/**
 * {@link EmptyHttpAuthRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.6.0
 */
public class EmptyHttpAuthRequest extends HttpAuthRequest {

    private final boolean cookiesNeeded;
    private final boolean locationNeeded;
    private final boolean failOnNonRedirect;

    /**
     * Initializes a new {@link EmptyHttpAuthRequest}.
     *
     * @param cookiesNeeded <code>true</code> if cookies should be parsed and checked from the response, <code>false</code>, otherwise
     * @param locationNeeded <code>true</code> to fail if the response contains no <code>Location</code> header, <code>false</code>, otherwise
     * @param failOnNonRedirect <code>true</code> to fail if the response status code is anything else than <code>HTTP 302</code>, <code>false</code>, otherwise
     */
    public EmptyHttpAuthRequest(boolean cookiesNeeded, boolean locationNeeded, boolean failOnNonRedirect) {
        super(null, null);
        this.cookiesNeeded = cookiesNeeded;
        this.locationNeeded = locationNeeded;
        this.failOnNonRedirect = failOnNonRedirect;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

    @Override
    public HttpAuthParser getParser() {
        return new HttpAuthParser(cookiesNeeded, locationNeeded, failOnNonRedirect);
    }

}
