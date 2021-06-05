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
