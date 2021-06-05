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

package com.openexchange.webdav.client;

import com.openexchange.exception.OXException;

/**
 * {@link WebDAVClientException}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class WebDAVClientException extends OXException {

    public static final int NO_STATUS = 0;

    private static final long serialVersionUID = 7529643967697240920L;

    /**
     * Initializes a new {@link WebDAVClientException} for an HTTP error.
     *
     * @param statusCode The HTTP status code
     * @param reasonPhrase The reason phrase
     * @param cause The root cause
     */
    public WebDAVClientException(int statusCode, String reasonPhrase, Throwable cause) {
        super(statusCode, reasonPhrase, cause);
    }

    /**
     * Initializes a generic {@link WebDAVClientException} without a specific HTTP status code.
     *
     * @param cause The root cause
     */
    public WebDAVClientException(Throwable cause) {
        this(NO_STATUS, null, cause);
    }

    /**
     * Gets the underlying HTTP status code.
     *
     * @return The HTTP status code, or {@link #NO_STATUS} if not set
     */
    public int getStatusCode() {
        return getCode();
    }

}
