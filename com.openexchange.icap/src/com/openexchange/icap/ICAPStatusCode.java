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

package com.openexchange.icap;

import static com.openexchange.java.Autoboxing.I;
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
            m.put(I(code.getCode()), code);
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
        ICAPStatusCode icapStatusCode = reverseIndex.get(I(statusCode));
        if (icapStatusCode == null) {
            throw new IllegalArgumentException("Unknown status code '" + statusCode + "'");
        }
        return icapStatusCode;
    }

}
