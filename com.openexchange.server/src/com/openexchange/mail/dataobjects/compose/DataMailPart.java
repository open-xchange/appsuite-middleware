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

package com.openexchange.mail.dataobjects.compose;

import static com.openexchange.mail.utils.MessageUtility.readStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Part;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.session.Session;

/**
 * {@link DataMailPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class DataMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = -2377505617785953620L;

    private static final int DEFAULT_BUF_SIZE = 0x2000;

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataMailPart.class);

    private static final int MB = 1048576;

    private final ThresholdFileHolder file;
    private final byte[] bytes;
    private transient Object cachedContent;
    private transient DataSource dataSource;

    /**
     * Initializes a new {@link DataMailPart}
     *
     * @param data The data (from a data source)
     * @param dataProperties The data properties
     * @param session The session
     * @throws OXException If data's content is not supported
     */
    protected DataMailPart(final Object data, final Map<String, String> dataProperties, final Session session) throws OXException {
        super();
        setHeaders(dataProperties);

        if (data instanceof InputStream) {
            bytes = null;
            int partLimit = TransportProperties.getInstance().getReferencedPartLimit();
            ThresholdFileHolder sink = partLimit <= 0 ? new ThresholdFileHolder() : new ThresholdFileHolder(partLimit);
            file = sink;
            sink.write((InputStream) data);
            setSize(sink.getLength());
        } else if (data instanceof byte[]) {
            file = null;
            bytes = (byte[]) data;
            setSize(bytes.length);
        } else {
            throw MailExceptionCode.UNSUPPORTED_DATASOURCE.create();
        }
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL)) {
            if (bytes != null) {
                cachedContent = getByteContent(bytes);
            } else {
                if (file.isInMemory()) {
                    cachedContent = getByteContent(file.getBuffer().toByteArray());
                } else {
                    cachedContent = getFileContent(file.getTempFile());
                }
            }
            return cachedContent;
        }
        return null;
    }

    private String getFileContent(File file) throws OXException {
        String charset = getContentType().getCharsetParameter();
        if (null == charset) {
            charset = System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset());
        }

        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readStream(fis, charset);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fis);
        }
    }

    private String getByteContent(byte[] bytes) throws OXException {
        String charset = getContentType().getCharsetParameter();
        if (null == charset) {
            charset = MailProperties.getInstance().getDefaultMimeCharset();
        }

        try {
            return new String(bytes, Charsets.forName(charset));
        } catch (UnsupportedCharsetException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return new DataHandler(getDataSource());
    }

    private static final String TEXT = "text/";

    private DataSource getDataSource() throws OXException {
        /*
         * Lazy creation
         */
        if (null == dataSource) {
            ContentType contentType = getContentType();
            if (bytes != null) {
                if (contentType.startsWith(TEXT) && !contentType.containsCharsetParameter()) {
                    /*
                     * Add default mail charset
                     */
                    contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                }
                return (dataSource = new MessageDataSource(bytes, contentType.toString()));
            } else if (file != null) {
                if (getContentType().startsWith(TEXT) && getContentType().getCharsetParameter() == null) {
                    if (file.isInMemory()) {
                        getContentType().setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    } else {
                        getContentType().setCharsetParameter(System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset()));
                    }
                }
                dataSource = new FileHolderDataSource(file, getContentType().toString());
            } else {
                throw MailExceptionCode.NO_CONTENT.create();
            }
        }
        return dataSource;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        if (bytes != null) {
            return Streams.newByteArrayInputStream(bytes);
        }
        if (file != null) {
            return file.getStream();
        }
        throw MailExceptionCode.NO_CONTENT.create();
    }

    @Override
    public ComposedPartType getType() {
        return ComposedPartType.DATA;
    }

    @Override
    public void loadContent() throws OXException {
        LOG.trace("DataSourceMailPart.loadContent()");
    }

    @Override
    public void prepareForCaching() {
        LOG.trace("DataSourceMailPart.prepareForCaching()");
    }

    /**
     * Closes this data part's file (if any).
     */
    public void close() {
        ThresholdFileHolder sink = this.file;
        if (null != sink) {
            sink.close();
        }
    }

    private void setHeaders(final Map<String, String> dataProperties) throws OXException {
        final String cts = dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE);
        if (null != cts) {
            setContentType(cts);
        }
        final String charset = dataProperties.get(DataProperties.PROPERTY_CHARSET);
        if (null != charset) {
            final ContentType contentType = getContentType();
            if (contentType.startsWith(TEXT)) {
                /*
                 * Charset only relevant for textual content
                 */
                contentType.setCharsetParameter(charset);
            }
        }
        final String disp = dataProperties.get(DataProperties.PROPERTY_DISPOSITION);
        if (null == disp) {
            getContentDisposition().setDisposition(Part.ATTACHMENT);
        } else {
            getContentDisposition().setDisposition(disp);
        }
        final String fileName = dataProperties.get(DataProperties.PROPERTY_NAME);
        if (null != fileName) {
            getContentType().setNameParameter(fileName);
            getContentDisposition().setFilenameParameter(fileName);
        }
    }

}
