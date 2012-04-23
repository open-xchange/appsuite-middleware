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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Part;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource.InputStreamProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.io.IOUtils;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link DataMailPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class DataMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = -2377505617785953620L;

    private static final int DEFAULT_BUF_SIZE = 0x2000;

    private static transient final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DataMailPart.class));

    private static final int MB = 1048576;

    private byte[] bytes;

    private transient Object cachedContent;

    private transient DataSource dataSource;

    private ManagedFile file;

    private String fileId;

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
            /*
             * Check data properties
             */
            long size;
            try {
                final String sSize = dataProperties.get(DataProperties.PROPERTY_SIZE);
                size = null == sSize ? 0 : Long.parseLong(sSize.trim());
                setSize(size);
            } catch (final NumberFormatException e) {
                size = 0;
            }
            handleInputStream((InputStream) data, size);
        } else if (data instanceof byte[]) {
            bytes = (byte[]) data;
            setSize(bytes.length);
        } else {
            throw MailExceptionCode.UNSUPPORTED_DATASOURCE.create();
        }
    }

    private void applyByteContent(final String charset) throws OXException {
        try {
            cachedContent = new String(bytes, Charsets.forName(charset));
        } catch (final UnsupportedCharsetException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
    }

    private void applyFileContent(final String charset) throws OXException {
        InputStream fis = null;
        try {
            fis = file.getInputStream();
            cachedContent = readStream(fis, charset);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private void copy2ByteArr(final InputStream in) throws IOException {
        try {
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
            final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
            int len;
            while ((len = in.read(bbuf)) != -1) {
                out.write(bbuf, 0, len);
            }
            out.flush();
            bytes = out.toByteArray();
            setSize(bytes.length);
        } finally {
            IOUtils.closeStreamStuff(in);
        }
    }

    private void copy2File(final InputStream in) throws IOException {
        try {
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            if (null == mfm) {
                throw new IOException("Missing file management");
            }
            final ManagedFile mf;
            try {
                mf = mfm.createManagedFile(in);
            } catch (final OXException e) {
                final IOException ioerr = new IOException();
                ioerr.initCause(e);
                throw ioerr;
            }
            setSize(mf.getSize());
            file = mf;
            fileId = mf.getID();
        } finally {
            IOUtils.closeStreamStuff(in);
        }
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL)) {
            if (bytes != null) {
                String charset = getContentType().getCharsetParameter();
                if (null == charset) {
                    charset = MailProperties.getInstance().getDefaultMimeCharset();
                }
                applyByteContent(charset);
                return cachedContent;
            }
            if (file != null) {
                String charset = getContentType().getCharsetParameter();
                if (null == charset) {
                    charset = System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset());
                }
                applyFileContent(charset);
                return cachedContent;
            }
        }
        return null;
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
            try {
                final ContentType contentType = getContentType();
                if (bytes != null) {
                    if (contentType.startsWith(TEXT) && !contentType.containsCharsetParameter()) {
                        /*
                         * Add default mail charset
                         */
                        contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    }
                    return (dataSource = new MessageDataSource(bytes, contentType.toString()));
                }
                if (file != null) {
                    if (contentType.startsWith(TEXT) && !contentType.containsCharsetParameter()) {
                        /*
                         * Add system charset
                         */
                        contentType.setCharsetParameter(System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset()));
                    }
                    final ManagedFile managedFile = file;
                    final String fileName = getFileName();
                    final InputStreamProvider isp = new InputStreamProvider() {

                        @Override
                        public InputStream getInputStream() throws IOException {
                            try {
                                return managedFile.getInputStream();
                            } catch (final OXException e) {
                                final IOException err = new IOException();
                                err.initCause(e);
                                throw err;
                            }
                        }

                        @Override
                        public String getName() {
                            return fileName;
                        }
                    };
                    return (dataSource = new StreamDataSource(isp, contentType.toString()));
                }
                throw MailExceptionCode.NO_CONTENT.create();
            } catch (final MailConfigException e) {
                LOG.error(e.getMessage(), e);
                dataSource = new MessageDataSource(new byte[0], "application/octet-stream");
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
        try {
            if (bytes != null) {
                return new UnsynchronizedByteArrayInputStream(bytes);
            }
            if (file != null) {
                return file.getInputStream();
            }
            throw MailExceptionCode.NO_CONTENT.create();
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public ComposedPartType getType() {
        return ComposedPartType.DATA;
    }

    private void handleInputStream(final InputStream inputStream, final long size) throws OXException {
        try {
            if (size > 0 && size <= TransportProperties.getInstance().getReferencedPartLimit()) {
                copy2ByteArr(inputStream);
                return;
            }
            copy2File(inputStream);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder("Data mail part exeeds ").append(
                Float.valueOf(TransportProperties.getInstance().getReferencedPartLimit() / MB).floatValue()).append(
                "MB limit. A temporary disk copy has been created: ").append(file.getFile().getName()));
        }
    }

    @Override
    public void loadContent() throws OXException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DataSourceMailPart.loadContent()");
        }
    }

    @Override
    public void prepareForCaching() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DataSourceMailPart.prepareForCaching()");
        }
    }

    /**
     * Gets this data part's file ID if its content has been written to disc.
     *
     * @return The file ID or <code>null</code> if content is kept inside rather than on disc.
     */
    public String getFileID() {
        return fileId;
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
