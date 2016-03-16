/*-
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


package com.openexchange.xing.exception;

import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

/**
 * Wraps any non-200 HTTP responses from an API call. See the constants in this
 * class for the meaning of each error code. You'll typically only want to
 * handle a few specific error codes and show some kind of generic error or
 * retry for the rest.
 */
public class XingServerException extends XingException {

    public static class Error {

        /** English version of the error. */
        public String error;

        /** The error in the user's locale, if intended to be displayed to the user. */
        public String userError;

        public Map<String, Object> fields;
        @SuppressWarnings("unchecked")
        public Error(Map<String, Object> map) {
            fields = map;
            Object err = map.get("error");
            if (err instanceof String) {
                error = (String)err;
            } else if (err instanceof Map<?, ?>) {
                Map<String, Object> detail = (Map<String, Object>)err;
                for (Object val: detail.values()) {
                    if (val instanceof String) {
                        error = (String)val;
                    }
                }
            }
            Object uerr = map.get("user_error");
            if (uerr instanceof String) {
                userError = (String)uerr;
            }

        }
    }

    /** The request was successful. */
    public static final int _200_OK = 200;

    /** The request was successful. */
    public static final int _201_CREATED = 201;

    /** The request was successful, but is not returning any content. */
    public static final int _204_NO_CONTENT = 204;

    /** The request was successful for a Range request. */
    public static final int _206_PARTIAL_CONTENT = 206;

    /** Moved to a new location temporarily. */
    public static final int _302_FOUND = 302;

    /**
     * Contents have not changed (from the given hash, revision, ETag, or similar parameter).
     */
    public static final int _304_NOT_MODIFIED = 304;

    /** Bad input parameter. Error message should indicate which one and why. */
    public static final int _400_BAD_REQUEST = 400;

    /** Bad or expired access token. Need to re-authenticate user. */
    public static final int _401_UNAUTHORIZED = 401;

    /** Usually from an invalid app key pair or other permanent error. */
    public static final int _403_FORBIDDEN = 403;

    /** Path not found. */
    public static final int _404_NOT_FOUND = 404;

    /**
     * Request method not allowed. You shouldn't see this unless writing your
     * own API calls.
     */
    public static final int _405_METHOD_NOT_ALLOWED = 405;

    /** Too many metadata entries to return. */
    public static final int _406_NOT_ACCEPTABLE = 406;

    public static final int _409_CONFLICT = 409;

    /**
     * Typically from trying to upload over HTTP using chunked encoding. The
     * XING API currently does not support chunked transfer encoding.
     */
    public static final int _411_LENGTH_REQUIRED = 411;

    /** When a thumbnail cannot be created for the input file. */
    public static final int _415_UNSUPPORTED_MEDIA = 415;

    /**
     * Internal server error. Best to try again or wait until corrected by
     * Xing.
     */
    public static final int _500_INTERNAL_SERVER_ERROR = 500;

    /** Not implemented. */
    public static final int _501_NOT_IMPLEMENTED = 501;

    /** If a XING server is down - try again later. */
    public static final int _502_BAD_GATEWAY = 502;

    /** If a XING server is not working properly - try again later. */
    public static final int _503_SERVICE_UNAVAILABLE = 503;

    /** User is over quota. */
    public static final int _507_INSUFFICIENT_STORAGE = 507;

    private static final long serialVersionUID = 1L;

    /** The body, if any, of the returned error. */
    public Error body;

    /** The HTTP error code. */
    public int error;

    /** The reason string associated with the error. */
    public String reason;

    /** The server string from the headers. */
    public String server;

    /** The location string from the headers (to handle redirects). */
    public String location;

    /**
     * Creates a {@link XingServerException} from an {@link HttpResponse}.
     */
    public XingServerException(HttpResponse response) {
        this.fillInStackTrace();
        StatusLine status = response.getStatusLine();
        error = status.getStatusCode();
        reason = status.getReasonPhrase();
        server = getHeader(response, "server");
        location = getHeader(response, "location");
    }

    public Map<String, Object> parsedResponse;

    /**
     * Creates a {@link XingServerException} from an {@link HttpResponse}.
     * The rest parameter must be a Map of String to Object.
     */
    @SuppressWarnings("unchecked")
    public XingServerException(HttpResponse response, Object rest) {
        this(response);

        if (rest != null && rest instanceof Map<?, ?>) {
            parsedResponse = (Map<String, Object>)rest;
            body = new Error(parsedResponse);
        }
    }

    /**
     * When this exception comes from creating a new account, returns whether
     * the request failed because an account with the email address already
     * exists.
     */
    public boolean isDuplicateAccount() {
        return error == 400 && body != null && body.error.contains("taken");
    }

    @Override
    public String toString() {
        return "XingServerException (" + server + "): " + error + " " + reason + " (" + (body == null ? "" : body.error) + ")";
    }

    @Override
    public String getMessage() {
        return toString();
    }

    /**
     * Gets the HTTP error code
     *
     * @return The HTTP error code
     */
    public int getError() {
        return error;
    }

    /**
     * Whether the given response is valid when it has no body (only some error
     * codes are allowed without a reason, currently 302 and 304).
     */
    public static boolean isValidWithNullBody(HttpResponse response) {
        int code = response.getStatusLine().getStatusCode();
        if (code == _302_FOUND) {
            String location = getHeader(response, "location");

            if (location != null) {
                int loc = location.indexOf("://");
                if (loc > -1) {
                    location = location.substring(loc+3);
                    loc = location.indexOf("/");
                    if (loc > -1) {
                        location = location.substring(0, loc);
                        if (location.toLowerCase().contains("xing.com")) {
                            return true;
                        }
                    }
                }
            }
        } else if (code == _304_NOT_MODIFIED) {
            return true;
        }
        return false;
    }

    private static String getHeader(HttpResponse response, String name) {
        String value = null;
        Header serverheader = response.getFirstHeader(name);
        if (serverheader != null) {
            value = serverheader.getValue();
        }
        return value;
    }
}
