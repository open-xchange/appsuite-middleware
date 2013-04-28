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

/**
 *
 */

package com.openexchange.ajp13.exception;

import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link AJPv13Exception} - Indicates an AJP error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13Exception extends OXException {

    private static final long serialVersionUID = 3492183535749551904L;

    /**
     * The AJP error code enumeration.
     */
    public static enum AJPCode {

        /**
         * An internal exception: %1$s
         */
        INTERNAL_EXCEPTION("An error occurred: %1$s", Category.CATEGORY_ERROR, 1),
        /**
         * Corrupt AJP package #%1$d: First two bytes do not indicate a package from server to container: %2$s %3$s\nWrong AJP package's
         * data:\n%4$s
         * <p>
         * Invalid byte sequence
         * </p>
         */
        INVALID_BYTE_SEQUENCE("Corrupt AJP package #%1$d: First two bytes do not indicate a package from server to container: %2$s %3$s\nWrong AJP package's data:\n%4$s", Category.CATEGORY_CONNECTIVITY, 2),
        /**
         * Socket closed by web server. Wait for input data of package #%1$d took %2$dmsec.
         */
        SOCKET_CLOSED_BY_WEB_SERVER("Socket closed by web server. Wait for input data of package #%1$d took %2$dmsec.", Category.CATEGORY_CONNECTIVITY, 3),
        /**
         * No data provided from web server: input stream returned \"-1\" while reading AJP magic bytes in package #%1$d. Wait for input
         * data took %2$dmsec.
         */
        EMPTY_INPUT_STREAM("No data provided from web server: input stream returned \"-1\" while reading AJP magic bytes in package #%1$d. Wait for input data took %2$dmsec.", Category.CATEGORY_CONNECTIVITY, 4),
        /**
         * AJP connection is not set to status "ASSIGNED"
         */
        INVALID_CONNECTION_STATE("AJP connection is not set to status \"ASSIGNED\"", Category.CATEGORY_ERROR, 5),
        /**
         * Response package exceeds max package size value of 8192k: %s
         */
        MAX_PACKAGE_SIZE("Response package exceeds max package size value of 8192k: %1$s", Category.CATEGORY_ERROR, 6),
        /**
         * Unknown Request Prefix Code: %1$s
         */
        UNKNOWN_PREFIX_CODE("Unknown Request Prefix Code: %1$s", Category.CATEGORY_CONNECTIVITY, 7),
        /**
         * Missing payload data in client's body chunk package
         */
        MISSING_PAYLOAD_DATA("Missing payload data in client's body chunk package", Category.CATEGORY_CONNECTIVITY, 8),
        /**
         * Empty SEND_BODY_CHUNK package MUST NOT be sent
         */
        NO_EMPTY_SENT_BODY_CHUNK("Empty SEND_BODY_CHUNK package MUST NOT be sent", Category.CATEGORY_ERROR, 9),
        /**
         * Integer value exceeds max allowed value ([MAX_INT_VALUE]): %1$d
         */
        INTEGER_VALUE_TOO_BIG(new com.openexchange.java.StringAllocator("Integer value exceeds max allowed value (").append(AJPv13Response.MAX_INT_VALUE).append(
            "): %1$d").toString(), Category.CATEGORY_ERROR, 10),
        /**
         * Invalid content-type header value: %1$s
         */
        INVALID_CONTENT_TYPE("Invalid content-type header value: %1$s", Category.CATEGORY_ERROR, 11),
        /**
         * Unparseable header field %1$s in forward request package
         */
        UNPARSEABLE_HEADER_FIELD("Unparseable header field %1$s in forward request package", Category.CATEGORY_CONNECTIVITY, 12),
        /**
         * String parse exception: No ending 0x00 found
         */
        UNPARSEABLE_STRING("String parse exception: No ending 0x00 found", Category.CATEGORY_CONNECTIVITY, 13),
        /**
         * Unsupported encoding: %1$s
         */
        UNSUPPORTED_ENCODING("Unsupported encoding: %1$s", Category.CATEGORY_ERROR, 14),
        /**
         * No attribute name could be found for code: %1$d
         */
        NO_ATTRIBUTE_NAME("No attribute name could be found for code: %1$d", Category.CATEGORY_ERROR, 15),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_CONNECTIVITY, 16),
        /**
         * A messaging error occurred: %1$s
         */
        MESSAGING_ERROR("A messaging error occurred: %1$s", Category.CATEGORY_ERROR, 17),
        /**
         * Missing property "com.openexchange.server.backendRoute" in file "server.properties"
         */
        MISSING_JVM_ROUTE("Missing property \"com.openexchange.server.backendRoute\" in file \"server.properties\"", Category.CATEGORY_CONFIGURATION, 18),
        /**
         * Cookie JSESSIONID contains non-matching JVM route: %1$s not equal to %2$s
         */
        WRONG_JVM_ROUTE("Cookie JSESSIONID contains non-matching JVM route: %1$s not equal to %2$s", Category.CATEGORY_CONNECTIVITY, 19),
        /**
         * Unexpected empty body package received from web server. Total-Received: %1$d | Content-Length: %2$d.\nCorresponding AJP forward
         * package:\n%3$s
         */
        UNEXPECTED_EMPTY_DATA_PACKAGE("Unexpected empty body package received from web server. Total-Received: %1$d | Content-Length: %2$d.\nCorresponding AJP forward package:\n%3$s", Category.CATEGORY_CONNECTIVITY, 20),
        /**
         * AJP server socket could not be bind to port %1$d. Probably another process is already listening on this port.
         */
        STARTUP_ERROR("AJP server socket could not be bound to port %1$d. Probably another process is already listening on this port.", Category.CATEGORY_CONNECTIVITY, 21),
        /**
         * File "%1$s" could not be found
         */
        FILE_NOT_FOUND("File \"%1$s\" could not be found.", Category.CATEGORY_ERROR, 22),
        /**
         * Invalid cookie header value: %1$s
         */
        INVALID_COOKIE_HEADER("Invalid cookie header value: %1$s", Category.CATEGORY_ERROR, 23),
        /**
         * Broken AJP cycle detected.
         */
        BROKEN_AJP_CYCLE("Broken AJP cycle detected.", Category.CATEGORY_ERROR, 24);

        private final String message;

        private final int detailNumber;

        private final Category category;

        private AJPCode(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
        }

        public Category getCategory() {
            return category;
        }

        public int getDetailNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * The empty arguments array.
     */
    protected static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Determines if AJP connection shall be kept alive or closed
     */
    private final boolean keepAlive;

    /**
     * Initializes a new {@link AJPv13Exception}
     *
     * @param code The AJP error code
     * @param keepAlive Whether to keep the AJP connection alive or not
     */
    public AJPv13Exception(final AJPCode code, final boolean keepAlive) {
        this(code, keepAlive, (Exception) null, EMPTY_ARGS);
    }

    /**
     * Initializes a new {@link AJPv13Exception}
     *
     * @param code The AJP error code
     * @param keepAlive Whether to keep the AJP connection alive or not
     * @param messageArgs The error message arguments
     */
    public AJPv13Exception(final AJPCode code, final boolean keepAlive, final Object... messageArgs) {
        this(code, keepAlive, (Exception) null, messageArgs);
    }

    /**
     * Initializes a new {@link AJPv13Exception}
     *
     * @param code The AJP error code
     * @param keepAlive Whether to keep the AJP connection alive or not
     * @param cause The init cause
     * @param messageArgs The error message arguments
     */
    public AJPv13Exception(final AJPCode code, final boolean keepAlive, final Exception cause, final Object... messageArgs) {
        super(code.getDetailNumber(), OXExceptionStrings.MESSAGE, cause);
        addCategory(code.getCategory()).setPrefix("AJP").setLogMessage(code.getMessage(), messageArgs);
        this.keepAlive = keepAlive;
    }

    /**
     * Initializes a new {@link AJPv13Exception} used as wrapper for given throwable to be conform to logging format
     *
     * @param cause The throwable to wrap
     */
    public AJPv13Exception(final Throwable cause) {
        super(AJPCode.INTERNAL_EXCEPTION.getDetailNumber(), OXExceptionStrings.MESSAGE, cause);
        addCategory(AJPCode.INTERNAL_EXCEPTION.getCategory()).setPrefix("AJP").setLogMessage(AJPCode.INTERNAL_EXCEPTION.getMessage(), cause.getMessage());
        this.keepAlive = false;
    }

    /**
     * Determines if AJP connection shall be kept alive or closed
     *
     * @return <code>true</code> if AJP connection shall be kept alive; otherwise <code>false</code> for closure
     */
    public boolean keepAlive() {
        return keepAlive;
    }

}
