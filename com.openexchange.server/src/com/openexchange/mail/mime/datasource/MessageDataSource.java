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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link MessageDataSource} - Allows creation of a data source by either an input stream, a string or a byte array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageDataSource implements DataSource {

    private static final int DEFAULT_BUF_SIZE = 0x1000;

    private final byte[] data;
    private String contentType;
    private String name;

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(InputStream inputStream, String contentType) throws IOException {
        this(inputStream, contentType, null);
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(InputStream inputStream, ContentType contentType) throws IOException {
        this(inputStream, contentType, null);
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(InputStream inputStream, String contentType, String name) throws IOException {
        this.contentType = contentType;
        data = Streams.stream2bytes(inputStream);
        this.name = name;
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(InputStream inputStream, ContentType contentType, String name) throws IOException {
        this(inputStream, contentType.toString(), name);
    }

    /**
     * Create a data source from a byte array
     */
    public MessageDataSource(byte[] data, String contentType) {
        this.contentType = contentType;
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    /**
     * Create a data source from a String
     */
    public MessageDataSource(String data, String contentType) throws UnsupportedEncodingException, OXException {
        final ContentType ct = new ContentType(contentType);
        if (!ct.containsCharsetParameter()) {
            ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
        }
        this.data = data.getBytes(ct.getCharsetParameter());
        this.contentType = ct.toString();
    }

    /**
     * Create a data source from a String
     */
    public MessageDataSource(String data, ContentType contentType) {
        final ContentType ct;
        if (contentType.containsCharsetParameter()) {
            ct = contentType;
        } else {
            ct = new ContentType();
            ct.setContentType(contentType);
            ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
        }
        this.data = data.getBytes(Charsets.forName(ct.getCharsetParameter()));
        this.contentType = ct.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (data == null) {
            throw new IOException("no data");
        }
        return new UnsynchronizedByteArrayInputStream(data);
    }

    /**
     * Gets the {@link ByteArrayInputStream}.
     *
     * @return The {@link ByteArrayInputStream}.
     * @throws IOException If an I/O error occurs
     */
    public ByteArrayInputStream getByteArrayInputStream() throws IOException {
        if (data == null) {
            throw new IOException("no data");
        }
        return new UnsynchronizedByteArrayInputStream(data);
    }

    /**
     * Gets the data
     *
     * @return The data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Not implemented
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

    /**
     * Sets the contentType
     *
     * @param contentType The contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

}
