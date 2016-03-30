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

package com.openexchange.proxy;

import java.io.IOException;
import java.io.InputStream;


/**
 * {@link Response} - Represents a HTTP response.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Response {

    /**
     * Returns the status code associated with the latest response.
     *
     * @return The status code from the most recent execution of this method.
     *         If the method has not yet been executed, the result is undefined.
     */
    int getStatusCode();

    /**
     * Returns the status text (or "reason phrase") associated with the latest
     * response.
     *
     * @return The status text from the most recent execution of this method.
     *         If the method has not yet been executed, the result is undefined.
     */
    String getStatusText();

    /**
     * Returns the response headers from the most recent execution of this request.
     *
     * @return A newly-created array containing all of the response headers,
     *         in the order in which they appeared in the response.
     */
    Header[] getResponseHeaders();

    /**
     * Returns the specified response header. Note that header-name matching is
     * case insensitive.
     *
     * @param headerName The name of the header to be returned.
     *
     * @return The specified response header.  If the repsonse contained multiple
     *         instances of the header, its values will be combined using the ','
     *         separator as specified by RFC2616.
     */
    Header getResponseHeader(String headerName);

    /**
     * Returns the response headers with the given name. Note that header-name matching is
     * case insensitive.
     * @param headerName the name of the headers to be returned.
     * @return an array of zero or more headers
     *
     * @since 3.0
     */
    Header[] getResponseHeaders(String headerName);

    /**
     * Returns the response footers from the most recent execution of this request.
     *
     * @return an array containing the response footers in the order that they
     *         appeared in the response.  If the response had no footers,
     *         an empty array will be returned.
     */
    Header[] getResponseFooters();

    /**
     * Return the specified response footer. Note that footer-name matching is
     * case insensitive.
     *
     * @param footerName The name of the footer.
     * @return The response footer.
     */
    Header getResponseFooter(String footerName);

    /**
     * Returns the response body of the HTTP method, if any, as an array of bytes.
     * If the method has not yet been executed or the response has no body, <code>null</code>
     * is returned.  Note that this method does not propagate I/O exceptions.
     * If an error occurs while reading the body, <code>null</code> will be returned.
     *
     * @return The response body, or <code>null</code> if the
     *         body is not available.
     *
     * @throws IOException if an I/O (transport) problem occurs
     */
    byte[] getResponseBody() throws IOException;

    /**
     * Returns the response body of the HTTP method, if any, as a {@link String}.
     * If response body is not available or cannot be read, <tt>null</tt> is returned.
     * The raw bytes in the body are converted to a <code>String</code> using the
     * character encoding specified in the response's <tt>Content-Type</tt> header, or
     * ISO-8859-1 if the response did not specify a character set.
     * <p>
     * Note that this method does not propagate I/O exceptions.
     * If an error occurs while reading the body, <code>null</code> will be returned.
     *
     * @return The response body converted to a <code>String</code>, or <code>null</code>
     *         if the body is not available.
     *
     * @throws IOException if an I/O (transport) problem occurs
     */
    String getResponseBodyAsString() throws IOException;

    /**
     * Returns the response body of the HTTP method, if any, as an InputStream.
     * If the response had no body or the method has not yet been executed,
     * <code>null</code> is returned.  Additionally, <code>null</code> may be returned
     * if {@link #releaseConnection} has been called or
     * if this method was called previously and the resulting stream was closed.
     *
     * @return The response body, or <code>null</code> if it is not available
     *
     * @throws IOException if an I/O (transport) problem occurs
     */
    InputStream getResponseBodyAsStream() throws IOException;

}
