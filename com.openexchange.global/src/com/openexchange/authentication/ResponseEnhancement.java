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

package com.openexchange.authentication;

/**
 * An interface which can be used to add further response details to the {@link Authenticated} return object of a authentication bundle.
 * This information is used to redirect the client or add additional headers and/or cookies to the response for the client.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public interface ResponseEnhancement {

    /**
     * Indicates how the response to the client should look like. See {@link ResultCode} for possible options.
     *
     * @return how to modify the response to the client.
     */
    ResultCode getCode();

    /**
     * Headers that should be part of the response to the client.
     *
     * @return headers that should be added to the client response. Never return <code>null</code> but an empty array.
     */
    Header[] getHeaders();

    /**
     * Cookies that should be part of the response to the client.
     *
     * @return cookies that should be added to the client response. Never return <code>null</code> but an empty array.
     */
    Cookie[] getCookies();

    /**
     * Contains the URL the client should be redirected to if {@link #getCode()} indicates {@link ResultCode#REDIRECT}. If {@link #getCode()}
     * indicates another result, this value can be <code>null</code>.
     *
     * @return URL to that the client should be redirected.
     */
    String getRedirect();

}
