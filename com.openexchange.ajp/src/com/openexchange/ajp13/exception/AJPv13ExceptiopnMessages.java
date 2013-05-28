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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.exception;

import com.openexchange.ajp13.AJPv13Response;

/**
 * {@link AJPv13ExceptiopnMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ExceptiopnMessages {

    /**
     * Initializes a new {@link AJPv13ExceptiopnMessages}.
     */
    private AJPv13ExceptiopnMessages() {
        super();
    }

    // An internal exception
    public static final String INTERNAL_EXCEPTION_MSG = "";


     // Corrupt AJP package #%1$d: First two bytes do not indicate a package from server to container: %2$s %3$s\nWrong AJP package's
     // data:\n%4$s
     // <p>
     // Invalid byte sequence
     // </p>
    public static final String INVALID_BYTE_SEQUENCE_MSG =
        "Corrupt AJP package #%1$d: First two bytes do not indicate a package from server to container: %2$s %3$s\nWrong AJP package's data:\n%4$s";

    // Socket closed by web server. Wait for input data of package #%1$d took %2$dmsec.
    public static final String SOCKET_CLOSED_BY_WEB_SERVER_MSG =
        "Socket closed by web server. Wait for input data of package #%1$d took %2$dmsec.";

     // No data provided from web server: input stream returned \"-1\" while reading AJP magic bytes in package #%1$d. Wait for input data
     // took %2$dmsec.
    public static final String EMPTY_INPUT_STREAM_MSG =
        "No data provided from web server: input stream returned \"-1\" while reading AJP magic bytes in package #%1$d. Wait for input data took %2$dmsec.";

    // AJP connection is not set to status "ASSIGNED"
    public static final String INVALID_CONNECTION_STATE_MSG = "AJP connection is not set to status \"ASSIGNED\"";

    // Response package exceeds max package size value of 8192k: %s
    public static final String MAX_PACKAGE_SIZE_MSG = "Response package exceeds max package size value of 8192k: %1$s";

    // Unknown Request Prefix Code: %1$s
    public static final String UNKNOWN_PREFIX_CODE_MSG = "Unknown Request Prefix Code: %1$s";

    // Missing payload data in client's body chunk package
    public static final String MISSING_PAYLOAD_DATA_MSG = "Missing payload data in client's body chunk package";

    // Empty SEND_BODY_CHUNK package MUST NOT be sent
    public static final String NO_EMPTY_SENT_BODY_CHUNK_MSG = "Empty SEND_BODY_CHUNK package MUST NOT be sent";

    // Integer value exceeds max allowed value (65535): %1$d
    public static final String INTEGER_VALUE_TOO_BIG_MSG = new com.openexchange.java.StringAllocator("Integer value exceeds max allowed value (").append(
        AJPv13Response.MAX_INT_VALUE).append("): %1$d").toString();

    // Invalid content-type header value: %1$s
    public static final String INVALID_CONTENT_TYPE_MSG = "Invalid content-type header value: %1$s";

    // Unparseable header field %1$s in forward request package
    public static final String UNPARSEABLE_HEADER_FIELD_MSG = "Unparseable header field %1$s in forward request package";

    // String parse exception: No ending 0x00 found
    public static final String UNPARSEABLE_STRING_MSG = "String parse exception: No ending 0x00 found";

    // Unsupported encoding: %1$s
    public static final String UNSUPPORTED_ENCODING_MSG = "Unsupported encoding: %1$s";

    // No attribute name could be found for code: %1$d
    public static final String NO_ATTRIBUTE_NAME_MSG = "No attribute name could be found for code: %1$d";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // A messaging error occurred: %1$s
    public static final String MESSAGING_ERROR_MSG = "A messaging error occurred: %1$s";

    // Missing property "com.openexchange.server.backendRoute" in file "server.properties"
    public static final String MISSING_JVM_ROUTE_MSG = "Missing property \"com.openexchange.server.backendRoute\" in file \"server.properties\"";

    // Cookie JSESSIONID contains non-matching JVM route: %1$s not equal to %2$s
    public static final String WRONG_JVM_ROUTE_MSG = "Cookie JSESSIONID contains non-matching JVM route: %1$s not equal to %2$s";

    /**
     * Unexpected empty body package received from web server. Total-Received: %1$d | Content-Length: %2$d.\nCorresponding AJP forward
     * package:\n%3$s
     */
    public static final String UNEXPECTED_EMPTY_DATA_PACKAGE_MSG =
        "Unexpected empty body package received from web server. Total-Received: %1$d | Content-Length: %2$d.\nCorresponding AJP forward package:\n%3$s";

    // AJP server socket could not be bind to port %1$d. Probably another process is already listening on this port.
    public static final String STARTUP_ERROR_MSG =
        "AJP server socket could not be bound to port %1$d. Probably another process is already listening on this port.";

    // File "%1$s" could not be found
    public static final String FILE_NOT_FOUND_MSG = "File \"%1$s\" could not be found.";

    // Invalid cookie header value: %1$s
    public static final String INVALID_COOKIE_HEADER_MSG = "Invalid cookie header value: %1$s";
}
