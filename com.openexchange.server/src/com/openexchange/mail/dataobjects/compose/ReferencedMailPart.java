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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource.InputStreamProvider;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.io.IOUtils;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ReferencedMailPart} - A {@link MailPart} implementation that points to a referenced part in original mail.
 * <p>
 * Since a mail part causes troubles when its input stream is read multiple times, corresponding data is either stored in an internal byte
 * array or copied as a temporary file to disk (depending on {@link TransportConfig#getReferencedPartLimit()}). Therefore this part needs
 * special handling to ensure removal of temporary file when it is dispatched.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ReferencedMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = 1097727980840011436L;

    private static final transient org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ReferencedMailPart.class));

    protected static final int DEFAULT_BUF_SIZE = 0x2000;

    private static final int MB = 1048576;

    private final boolean isMail;

    private transient DataSource dataSource;

    private transient Object cachedContent;

    private byte[] data;

    private ManagedFile file;

    private String fileId;

    /**
     * Initializes a new {@link ReferencedMailPart}.
     * <p>
     * The referenced part's content is loaded dependent on its size. If size exceeds defined
     * {@link TransportConfig#getReferencedPartLimit() limit} its content is temporary written to disc; otherwise its content is kept inside
     * an array of <code>byte</code>.
     *
     * @param referencedPart The referenced part
     * @param session The session used to store a possible temporary disc file
     * @throws OXException If a mail error occurs
     */
    protected ReferencedMailPart(final MailPart referencedPart, final Session session) throws OXException {
        isMail = referencedPart.getContentType().isMimeType(MimeTypes.MIME_MESSAGE_RFC822) && !referencedPart.getContentDisposition().isAttachment();
        try {
            handleReferencedPart(referencedPart, session);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Initializes a new {@link ReferencedMailPart}.
     * <p>
     * The referenced mail's content is loaded dependent on its size. If size exceeds defined
     * {@link TransportConfig#getReferencedPartLimit() limit} its content is temporary written to disc; otherwise its content is kept inside
     * an array of <code>byte</code>.
     *
     * @param referencedMail The referenced mail
     * @param session The session used to store a possible temporary disc file
     * @throws OXException If a mail error occurs
     */
    protected ReferencedMailPart(final MailMessage referencedMail, final Session session) throws OXException {
        isMail = true;
        try {
            handleReferencedPart(referencedMail, session);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles referenced part dependent on its size. If size exceeds defined {@link TransportConfig#getReferencedPartLimit() limit} its
     * content is temporary written to disc; otherwise its content is kept inside an array of <code>byte</code>.
     *
     * @param referencedPart The referenced mail part
     * @param session The session to manage a possible temporary disc file
     * @return A file ID if content is written to disc; otherwise <code>null</code> to indicate content is held inside.
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    private void handleReferencedPart(final MailPart referencedPart, final Session session) throws OXException, IOException {
        final long size = referencedPart.getSize();
        if (size > 0 && size <= TransportProperties.getInstance().getReferencedPartLimit()) {
            if (isMail) {
                // Consume surrounding part's headers until nested message bytes appears
                data = Streams.stream2bytes(messageSource(referencedPart));
                setContentType(MimeTypes.MIME_MESSAGE_RFC822);
                setContentDisposition(Part.INLINE);
                setSize(size);
            } else {
                copy2ByteArr(referencedPart.getInputStream());
                setHeaders(referencedPart);
            }
        } else {
            if (isMail) {
                // Consume surrounding part's headers until nested message bytes appears
                copy2File(messageSource(referencedPart));
                setContentType(MimeTypes.MIME_MESSAGE_RFC822);
                setContentDisposition(Part.INLINE);
                setSize(file.getFile().length());
            } else {
                copy2File(referencedPart.getInputStream());
                setHeaders(referencedPart);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(new com.openexchange.java.StringAllocator("Referenced mail part exeeds ").append(
                    Float.valueOf(TransportProperties.getInstance().getReferencedPartLimit() / MB).floatValue()).append(
                    "MB limit. A temporary disk copy has been created: ").append(file.getFile().getName()));
            }
        }
        if (!containsFileName() && referencedPart.containsFileName()) {
            setFileName(referencedPart.getFileName());
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

    private InputStream messageSource(final MailPart referencedPart) throws OXException {
        if (referencedPart instanceof MailMessage) {
            // Copy to ByteArrayOutputStream
            return MimeMessageUtility.getStreamFromMailPart(referencedPart);
        }
        return referencedPart.getInputStream();
    }

    private void copy2ByteArr(final InputStream in) throws IOException {
        try {
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
            final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
            for (int read; (read = in.read(bbuf)) > 0;) {
                out.write(bbuf, 0, read);
            }
            out.flush();
            data = out.toByteArray();
        } finally {
            IOUtils.closeStreamStuff(in);
        }
    }

    private void setHeaders(final MailPart referencedPart) {
        if (referencedPart.containsContentId()) {
            setContentId(referencedPart.getContentId());
        }
        setContentType(referencedPart.getContentType());
        setContentDisposition(referencedPart.getContentDisposition());
        setFileName(referencedPart.getFileName());
        if (!containsSize()) {
            setSize(referencedPart.getSize());
        }
        final int count = referencedPart.getHeadersSize();
        final Iterator<Map.Entry<String, String>> iter = referencedPart.getHeadersIterator();
        for (int i = 0; i < count; i++) {
            final Map.Entry<String, String> e = iter.next();
            addHeader(e.getKey(), e.getValue());
        }
    }

    private static final String TEXT = "text/";

    private DataSource getDataSource() throws OXException {
        /*
         * Lazy creation
         */
        if (null == dataSource) {
            try {
                if (data != null) {
                    if (getContentType().startsWith(TEXT) && getContentType().getCharsetParameter() == null) {
                        /*
                         * Add default mail charset
                         */
                        getContentType().setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    }
                    return (dataSource = new MessageDataSource(data, getContentType().toString()));
                }
                if (file != null) {
                    if (getContentType().startsWith(TEXT) && getContentType().getCharsetParameter() == null) {
                        /*
                         * Add system charset
                         */
                        getContentType().setCharsetParameter(
                            System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset()));
                    }
                    final ManagedFile managedFile = file;
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
                            return null;
                        }
                    };
                    return (dataSource = new StreamDataSource(isp, getContentType().toString()));
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
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL)) {
            if (data != null) {
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

    private void applyFileContent(final String charset) throws OXException {
        InputStream fis = null;
        try {
            fis = file.getInputStream();
            cachedContent = readStream(fis, charset);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
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

    private void applyByteContent(final String charset) throws OXException {
        try {
            cachedContent = new String(data, Charsets.forName(charset));
        } catch (final UnsupportedCharsetException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return new DataHandler(getDataSource());
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
        if (data != null) {
            return new UnsynchronizedByteArrayInputStream(data);
        }
        if (file != null) {
            return file.getInputStream();
        }
        throw MailExceptionCode.NO_CONTENT.create();
    }

    @Override
    public void loadContent() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ReferencedMailPart.loadContent()");
        }
    }

    @Override
    public void prepareForCaching() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ReferencedMailPart.prepareForCaching()");
        }
    }

    /**
     * Gets this referenced part's file ID if its content has been written to disc.
     *
     * @return The file ID or <code>null</code> if content is kept inside rather than on disc.
     */
    public String getFileID() {
        return fileId;
    }

    /**
     * Checks if referenced part is a mail
     *
     * @return <code>true</code> if referenced part is a mail; otherwise <code>false</code>
     */
    public boolean isMail() {
        return isMail;
    }

    /**
     * Generates a UUID using {@link UUID#randomUUID()}; e.g.:<br>
     * <i>a5aa65cb-6c7e-4089-9ce2-b107d21b9d15</i>
     *
     * @return A UUID string
     */
    private static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.REFERENCE;
    }
}
