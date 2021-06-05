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

package com.openexchange.snippet;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link DefaultAttachment} - The default attachment implementation based on {@link InputStreamProvider}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultAttachment implements Attachment {

    /**
     * The {@link InputStream stream} provider.
     */
    public static interface InputStreamProvider {

        /**
         * Gets the input stream.
         *
         * @return The input stream
         * @throws IOException If an I/O error occurs
         */
        InputStream getInputStream() throws IOException;
    }

    /** The identifier. */
    protected String id;

    /** The content type. */
    protected String contentType;

    /** The content disposition. */
    protected String contentDisposition;

    /** The size. */
    protected long size;

    /** The input stream provider */
    protected InputStreamProvider streamProvider;

    /** The <code>Content-Id</code> value */
    protected String contentId;
    
    /** The attachment's filename */
    private String filename;

    /**
     * Initializes a new {@link DefaultAttachment}.
     */
    public DefaultAttachment() {
        super();
        size = -1L;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getContentDisposition() {
        return contentDisposition;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final InputStreamProvider streamProvider = this.streamProvider;
        if (null == streamProvider) {
            throw new IOException("No input stream available.");
        }
        return streamProvider.getInputStream();
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the content type
     *
     * @param contentType The content type to set
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the content disposition
     *
     * @param contentDisposition The content disposition to set
     */
    public void setContentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * Sets the <code>Content-Id</code> value
     *
     * @param contentId The <code>Content-Id</code> value to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * Sets the size
     *
     * @param size The size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * Sets the stream provider
     *
     * @param streamProvider The stream provider to set
     */
    public void setStreamProvider(final InputStreamProvider streamProvider) {
        this.streamProvider = streamProvider;
    }

    /**
     * Gets the filename
     *
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename
     *
     * @param filename The filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DefaultAttachment [");
        final String delim = ", ";
        if (id != null) {
            builder.append("id=").append(id).append(delim);
        }
        if (contentType != null) {
            builder.append("contentType=").append(contentType).append(delim);
        }
        if (contentDisposition != null) {
            builder.append("contentDisposition=").append(contentDisposition).append(delim);
        }
        if (size > 0) {
            builder.append("size=").append(size).append(delim);
        }
        if (streamProvider != null) {
            builder.append("streamProvider=").append(streamProvider);
        }
        if (filename != null) {
            builder.append("filename=").append(filename);
        }
        builder.append(']');
        return builder.toString();
    }

}
