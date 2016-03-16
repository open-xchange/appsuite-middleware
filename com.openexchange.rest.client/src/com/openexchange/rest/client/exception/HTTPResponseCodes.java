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
 *    trademarks of the OX Software GmbH group of companies.
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
