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

package com.openexchange.calendar.itip.sender.datasources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * {@link StreamDataSource} - A simple {@link DataSource data source} that encapsulates an input stream provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StreamDataSource implements DataSource {

    private String contentType;

    private final InputStreamProvider provider;

    /**
     * Creates a StreamDataSource from an InputStreamProvider object. <i>Note: The stream will not actually be opened until a method is
     * called that requires the stream to be opened.</i>
     * <p>
     * Content type is initially set to "application/octet-stream".
     *
     * @param provider The input stream provider
     */
    public StreamDataSource(final InputStreamProvider provider) {
        this(provider, null);
    }

    /**
     * Creates a StreamDataSource from an InputStreamProvider object. <i>Note: The stream will not actually be opened until a method is
     * called that requires the stream to be opened.</i>
     *
     * @param provider The input stream provider
     * @param contentType The content type
     */
    public StreamDataSource(final InputStreamProvider provider, final String contentType) {
        super();
        this.provider = provider;
        this.contentType = contentType == null ? "application/octet-stream" : contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return provider.getInputStream();
    }

    /**
     * Not implemented
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return provider.getName();
    }

    /**
     * Sets the content type.
     *
     * @param contentType The content type.
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Provides a newly allocated input stream.
     */
    public interface InputStreamProvider {

        /**
         * Gets a newly allocated input stream.
         * <p>
         * This method returns an InputStream representing the data and throws the appropriate exception if it can not do so.<br>
         * <small><b>NOTE:</b></small> A new InputStream object must be returned each time this method is called, and the stream must be
         * positioned at the beginning of the data.
         *
         * @throws IOException If an I/O error occurs when allocating a new input stream.
         * @return A newly allocated input stream
         */
        InputStream getInputStream() throws IOException;

        /**
         * Gets an appropriate name for the resource providing the input stream.
         *
         * @return An appropriate name for the resource providing the input stream
         */
        String getName();
    }
}
