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

/**
 * {@link ICAPCommunicationStrings} - Defines communication strings used by the
 * ICAP protocol.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class ICAPCommunicationStrings {

    /**
     * Carriage Return and Line Feed
     */
    public static final String CRLF = "\r\n";

    /**
     * <p>Marks the end of the ICAP request/response (including headers and body)</p>
     */
    public static final String ICAP_TERMINATOR = CRLF + CRLF;

    /**
     * <p>
     * Marks the end of the request body and thus the request itself. This way
     * the client indicates to the server that it wishes to send the content over.
     * </p>
     * <p>
     * Furthermore when the ICAP server responds with a 200 status code, it is highly
     * likely that an encapsulated HTTP message is contained with in the ICAP response.
     * The body of that HTTP message is also ended with this terminator.
     * This way the server indicates to the client that has ended the transmission.
     * </p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.5">RFC-3507, Section 4.5</a>
     */
    public static final String HTTP_TERMINATOR = "0" + CRLF + CRLF;

    /**
     * Indicates to the ICAP server the end of the last chunk.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.5">RFC-3507, Section 4.5</a>
     */
    public static final String ICAP_CHUNK_EOF = "0; ieof" + CRLF + CRLF;
}
