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
