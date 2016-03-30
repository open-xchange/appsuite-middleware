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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MessageDataSource} - Allows creation of a data source by either an input stream, a string or a byte array.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageDataSource implements DataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageDataSource.class);

    private static final int DEFAULT_BUF_SIZE = 0x1000;

    private final byte[] data;

    private final String contentType;

    private String name;

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(final InputStream inputStream, final String contentType) throws IOException {
        this(inputStream, contentType, null);
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(final InputStream inputStream, final ContentType contentType) throws IOException {
        this(inputStream, contentType, null);
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(final InputStream inputStream, final String contentType, final String name) throws IOException {
        this.contentType = contentType;
        data = copyStream(inputStream);
        this.name = name;
    }

    /**
     * Create a data source from an input stream
     */
    public MessageDataSource(final InputStream inputStream, final ContentType contentType, final String name) throws IOException {
        this(inputStream, contentType.toString(), name);
    }

    /**
     * Create a data source from a byte array
     */
    public MessageDataSource(final byte[] data, final String contentType) {
        this.contentType = contentType;
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    /**
     * Create a data source from a String
     * @throws OXException
     */
    public MessageDataSource(final String data, final String contentType) throws UnsupportedEncodingException, OXException {
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
    public MessageDataSource(final String data, final ContentType contentType) throws UnsupportedEncodingException {
        final ContentType ct;
        if (contentType.containsCharsetParameter()) {
            ct = contentType;
        } else {
            ct = new ContentType();
            ct.setContentType(contentType);
            ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
        }
        this.data = data.getBytes(ct.getCharsetParameter());
        this.contentType = ct.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(data);
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
        return name;
    }

    protected static byte[] copyStream(final InputStream inputStream) throws IOException {
        try {
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
            final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
            int len;
            while ((len = inputStream.read(bbuf, 0, bbuf.length)) > 0) {
                baos.write(bbuf, 0, len);
            }
            return baos.toByteArray();
        } finally {
            Streams.close(inputStream);
        }
    }
}
