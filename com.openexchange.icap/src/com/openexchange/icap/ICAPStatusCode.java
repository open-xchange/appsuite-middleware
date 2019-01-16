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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.icap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ICAPStatusCode} - Defines the ICAP status codes as described in
 * <a href="https://tools.ietf.org/html/rfc3507#section-4.3.3">RFC-3507, Section 4.3.3</a>
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum ICAPStatusCode {

    /**
     * 100 - Continue after ICAP Preview
     */
    CONTINUE(100, "Continue"),
    /**
     * 200 - OK
     */
    OK(200, "OK"),
    /**
     * 204 - No modifications needed
     */
    NO_CONTENT(204, "Content not modified"),
    /**
     * 400 - Bad request.
     */
    BAD_REQUEST(400, "Bad request"),
    /**
     * 403 - Forbidden.
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * 404 - ICAP Service not found.
     */
    NOT_FOUND(404, "Service not found"),
    /**
     * 405 - Method not allowed for service (e.g., RESPMOD requested for
     * service that supports only REQMOD).
     */
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    /**
     * 408 - Request timeout. ICAP server gave up waiting for a request
     * from an ICAP client.
     */
    REQUEST_TIMEOUT(408, "Request timeout"),
    /**
     * 500 - Server error. Error on the ICAP server, such as "out of disk space".
     */
    SERVER_ERROR(500, "Server error"),
    /**
     * 501 - Method not implemented. This response is illegal for an
     * OPTIONS request since implementation of OPTIONS is mandatory.
     */
    METHOD_NOT_IMPLEMENTED(501, "Method not implemented"),
    /**
     * 502 - Bad Gateway. This is an ICAP proxy and proxying produced an error.
     */
    BAD_GATEWAY(502, "Bad gateway"),
    /**
     * 503 - Service overloaded. The ICAP server has exceeded a maximum
     * connection limit associated with this service; the ICAP client
     * should not exceed this limit in the future.
     */
    SERVICE_OVERLOADED(503, "Service overloaded"),
    /**
     * 505 - ICAP version not supported by server.
     */
    ICAP_VERSION_NOT_SUPPORTED(505, "Version not supported");

    private static final Map<Integer, ICAPStatusCode> reverseIndex;
    static {
        Map<Integer, ICAPStatusCode> m = new HashMap<>();
        for (ICAPStatusCode code : ICAPStatusCode.values()) {
            m.put(code.getCode(), code);
        }
        reverseIndex = Collections.unmodifiableMap(m);
    }
    private final int code;
    private final String message;

    /**
     * Initialises a new {@link ICAPStatusCode}.
     */
    private ICAPStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Gets the code
     *
     * @return The code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Parses the specified status code into an {@link ICAPStatusCode}
     * 
     * @param statusCode The status code to parse
     * @return The {@link ICAPStatusCode}
     * @throws IllegalArgumentException if the specified status code is unknown
     */
    public static ICAPStatusCode parseStatusCode(int statusCode) {
        ICAPStatusCode icapStatusCode = reverseIndex.get(statusCode);
        if (icapStatusCode == null) {
            throw new IllegalArgumentException("Unknown status code '" + statusCode + "'");
        }
        return icapStatusCode;
    }

}
