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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.servlet;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.java.Charsets;

/**
 * {@link HttpStatusCode} - Encapsulates the HTTP response status and
 * reason phrases as defined by <code>RFC 2616</code>.
 * 
 * <p>Copied from {@link org.glassfish.grizzly.http.util.HttpStatus} and slightly modified.</p>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10">RFC 26,16, Section 10</a>
 */
public class HttpStatusCode {

    private static final Map<Integer, HttpStatusCode> statusMessages = new HashMap<Integer, HttpStatusCode>();

    public static final HttpStatusCode CONINTUE_100 = register(100, "Continue");
    public static final HttpStatusCode SWITCHING_PROTOCOLS_101 = register(101, "Switching Protocols");
    public static final HttpStatusCode WEB_SOCKET_PROTOCOL_HANDSHAKE_101 = register(101, "Web Socket Protocol Handshake");
    public static final HttpStatusCode OK_200 = register(200, "OK");
    public static final HttpStatusCode CREATED_201 = register(201, "Created");
    public static final HttpStatusCode ACCEPTED_202 = register(202, "Accepted");
    public static final HttpStatusCode NON_AUTHORATIVE_INFORMATION_203 = register(203, "Not-Authoritative Information");
    public static final HttpStatusCode NO_CONTENT_204 = register(204, "No Content");
    public static final HttpStatusCode RESET_CONTENT_205 = register(205, "Reset Content");
    public static final HttpStatusCode PARTIAL_CONTENT_206 = register(206, "Partial Content");
    public static final HttpStatusCode MULTIPLE_CHOICES_300 = register(300, "Multiple Choices");
    public static final HttpStatusCode MOVED_PERMANENTLY_301 = register(301, "Moved Permanently");
    public static final HttpStatusCode FOUND_302 = register(302, "Found");
    public static final HttpStatusCode SEE_OTHER_303 = register(303, "See Other");
    public static final HttpStatusCode NOT_MODIFIED_304 = register(304, "Not Modified");
    public static final HttpStatusCode USE_PROXY_305 = register(305, "Use Proxy");
    public static final HttpStatusCode TEMPORARY_REDIRECT_307 = register(307, "Temporary Redirect");
    public static final HttpStatusCode PERMANENT_REDIRECT_308 = register(308, "Permanent Redirect");
    public static final HttpStatusCode BAD_REQUEST_400 = register(400, "Bad Request");
    public static final HttpStatusCode UNAUTHORIZED_401 = register(401, "Unauthorized");
    public static final HttpStatusCode PAYMENT_REQUIRED_402 = register(402, "Payment Required");
    public static final HttpStatusCode FORBIDDEN_403 = register(403, "Forbidden");
    public static final HttpStatusCode NOT_FOUND_404 = register(404, "Not Found");
    public static final HttpStatusCode METHOD_NOT_ALLOWED_405 = register(405, "Method Not Allowed");
    public static final HttpStatusCode NOT_ACCEPTABLE_406 = register(406, "Not Acceptable");
    public static final HttpStatusCode PROXY_AUTHENTICATION_REQUIRED_407 = register(407, "Proxy Authentication Required");
    public static final HttpStatusCode REQUEST_TIMEOUT_408 = register(408, "Request Timeout");
    public static final HttpStatusCode CONFLICT_409 = register(409, "Conflict");
    public static final HttpStatusCode GONE_410 = register(410, "Gone");
    public static final HttpStatusCode LENGTH_REQUIRED_411 = register(411, "Length Required");
    public static final HttpStatusCode PRECONDITION_FAILED_412 = register(412, "Precondition Failed");
    public static final HttpStatusCode REQUEST_ENTITY_TOO_LARGE_413 = register(413, "Request Entity Too Large");
    public static final HttpStatusCode REQUEST_URI_TOO_LONG_414 = register(414, "Request-URI Too Long");
    public static final HttpStatusCode UNSUPPORTED_MEDIA_TYPE_415 = register(415, "Unsupported Media Type");
    public static final HttpStatusCode REQUEST_RANGE_NOT_SATISFIABLE_416 = register(416, "Request Range Not Satisfiable");
    public static final HttpStatusCode EXPECTATION_FAILED_417 = register(417, "Expectation Failed");
    public static final HttpStatusCode MISDIRECTED_REQUEST = register(421, "Misdirected Request");
    public static final HttpStatusCode REQUEST_HEADER_FIELDS_TOO_LARGE = register(431, "Request Header Fields Too Large");
    public static final HttpStatusCode INTERNAL_SERVER_ERROR_500 = register(500, "Internal Server Error");
    public static final HttpStatusCode NOT_IMPLEMENTED_501 = register(501, "Not Implemented");
    public static final HttpStatusCode BAD_GATEWAY_502 = register(502, "Bad Gateway");
    public static final HttpStatusCode SERVICE_UNAVAILABLE_503 = register(503, "Service Unavailable");
    public static final HttpStatusCode GATEWAY_TIMEOUT_504 = register(504, "Gateway Timeout");
    public static final HttpStatusCode HTTP_VERSION_NOT_SUPPORTED_505 = register(505, "HTTP Version Not Supported");

    private static HttpStatusCode register(int statusCode, final String reasonPhrase) {
        final HttpStatusCode httpStatus = newHttpStatus(statusCode, reasonPhrase);
        statusMessages.put(I(statusCode), httpStatus);
        return httpStatus;
    }

    public static HttpStatusCode newHttpStatus(int statusCode, final String reasonPhrase) {
        return new HttpStatusCode(statusCode, reasonPhrase);
    }

    /**
     * @param statusCode HTTP status code
     *
     * @return {@link HttpStatusCode} representation of the status.
     */
    public static HttpStatusCode getHttpStatus(int statusCode) {
        HttpStatusCode status = statusMessages.get(I(statusCode));
        if (status == null) {
            status = new HttpStatusCode(statusCode, "CUSTOM");
        }

        return status;
    }

    private final int status;
    private final String reasonPhrase;
    private final byte[] reasonPhraseBytes;
    private final byte[] statusBytes;

    private HttpStatusCode(int status, final String reasonPhrase) {
        this.status = status;
        this.reasonPhrase = reasonPhrase;
        reasonPhraseBytes = reasonPhrase.getBytes(Charsets.US_ASCII);
        statusBytes = Integer.toString(status).getBytes(Charsets.US_ASCII);
    }

    /**
     * @return <code>true</code> if the specified int status code matches
     *         the status of this <code>HttpStatus</code>.
     */
    public boolean statusMatches(final int status) {
        return (status == this.status);
    }

    /**
     * @return the <code>int</code> status code.
     */
    public int getStatusCode() {
        return status;
    }

    public byte[] getStatusBytes() {
        return statusBytes;
    }

    /**
     * @return the {@link String} representation of the reason phrase.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * @return the bytes containing the reason phrase as
     *         defined by <code>RFC 2616</code>.
     */
    public byte[] getReasonPhraseBytes() {
        return reasonPhraseBytes;
    }
}
