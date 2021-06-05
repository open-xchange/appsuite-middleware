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

import com.openexchange.ajax.framework.AbstractRedirectParser;

/**
 * {@link HttpAuthParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class HttpAuthParser extends AbstractRedirectParser<HttpAuthResponse> {

    /**
     * Initializes a new {@link HttpAuthParser}.
     *
     * @param cookiesNeeded <code>true</code> if cookies should be parsed and checked from the response, <code>false</code>, otherwise
     * @param locationNeeded <code>true</code> to fail if the response contains no <code>Location</code> header, <code>false</code>, otherwise
     * @param failOnNonRedirect <code>true</code> to fail if the response status code is anything else than <code>HTTP 302</code>, <code>false</code>, otherwise
     */
    public HttpAuthParser(boolean cookiesNeeded, boolean locationNeeded, boolean failOnNonRedirect) {
        super(cookiesNeeded, locationNeeded, failOnNonRedirect);
    }

    @Override
    protected HttpAuthResponse createResponse(String location) {
        return new HttpAuthResponse(getStatusCode(), getReasonPhrase(), location);
    }
}
