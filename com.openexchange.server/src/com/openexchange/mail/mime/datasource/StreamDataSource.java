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

package com.openexchange.mail.mime.datasource;

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
    public StreamDataSource(InputStreamProvider provider) {
        this(provider, null);
    }

    /**
     * Creates a StreamDataSource from an InputStreamProvider object. <i>Note: The stream will not actually be opened until a method is
     * called that requires the stream to be opened.</i>
     *
     * @param provider The input stream provider
     * @param contentType The content type
     */
    public StreamDataSource(InputStreamProvider provider, String contentType) {
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
    public void setContentType(String contentType) {
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
