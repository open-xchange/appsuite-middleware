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

package com.openexchange.rest.client.exception;

/**
 * {@link HTTPResponseCodes}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class HTTPResponseCodes {

    /** The request was successful. This won't ever be thrown in an exception. */
    public static final int _200_OK = 200;

    /** The request was successful for a Range request. This won't ever be thrown in an exception. */
    public static final int _206_PARTIAL_CONTENT = 206;

    /** Moved to a new location temporarily. */
    public static final int _302_FOUND = 302;

    /** Contents have not changed */
    public static final int _304_NOT_MODIFIED = 304;

    /** Bad input parameter. Error message should indicate which one and why. */
    public static final int _400_BAD_REQUEST = 400;

    /** Bad or expired access token. Need to re-authenticate user. */
    public static final int _401_UNAUTHORIZED = 401;

    /** Usually from an invalid app key pair or other permanent error. */
    public static final int _403_FORBIDDEN = 403;

    /** Path not found. */
    public static final int _404_NOT_FOUND = 404;

    /** Request method not allowed. */
    public static final int _405_METHOD_NOT_ALLOWED = 405;

    public static final int _406_NOT_ACCEPTABLE = 406;

    public static final int _409_CONFLICT = 409;

    public static final int _411_LENGTH_REQUIRED = 411;

    public static final int _415_UNSUPPORTED_MEDIA = 415;

    /** Internal server error. */
    public static final int _500_INTERNAL_SERVER_ERROR = 500;

    /** Not implemented. */
    public static final int _501_NOT_IMPLEMENTED = 501;

    /** Try again later. */
    public static final int _502_BAD_GATEWAY = 502;

    /** Try again later. */
    public static final int _503_SERVICE_UNAVAILABLE = 503;

    /** User is over quota. */
    public static final int _507_INSUFFICIENT_STORAGE = 507;

}
