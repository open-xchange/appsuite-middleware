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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Part;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.session.Session;

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

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReferencedMailPart.class);

    protected static final int DEFAULT_BUF_SIZE = 0x2000;

    private static final int MB = 1048576;

    private final boolean isMail;
    private final ThresholdFileHolder file;

    private transient DataSource dataSource;
    private transient Object cachedContent;

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
        int partLimit = TransportProperties.getInstance().getReferencedPartLimit();
        ThresholdFileHolder sink = partLimit <= 0 ? new ThresholdFileHolder() : new ThresholdFileHolder(partLimit);
        file = sink;
        handleReferencedPart(referencedPart, sink);
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
        int partLimit = TransportProperties.getInstance().getReferencedPartLimit();
        ThresholdFileHolder sink = partLimit <= 0 ? new ThresholdFileHolder() : new ThresholdFileHolder(partLimit);
        file = sink;
        handleReferencedPart(referencedMail, sink);
    }

    /**
     * Handles referenced part dependent on its size. If size exceeds defined {@link TransportConfig#getReferencedPartLimit() limit} its
     * content is temporary written to disc; otherwise its content is kept inside an array of <code>byte</code>.
     *
     * @param referencedPart The referenced mail part
     * @param file The associated file holder
     * @return A file ID if content is written to disc; otherwise <code>null</code> to indicate content is held inside.
     * @throws OXException If a mail error occurs
     */
    private void handleReferencedPart(MailPart referencedPart, ThresholdFileHolder file) throws OXException {
        if (isMail) {
            file.write(messageSource(referencedPart));
            setContentType(MimeTypes.MIME_MESSAGE_RFC822);
            setContentDisposition(Part.INLINE);
            setSize(file.getLength());
        } else {
            file.write(referencedPart.getInputStream());
            setHeaders(referencedPart);
        }

        if (!containsFileName() && referencedPart.containsFileName()) {
            setFileName(referencedPart.getFileName());
        }
    }

    private InputStream messageSource(final MailPart referencedPart) throws OXException {
        if (referencedPart instanceof MailMessage) {
            // Copy to ByteArrayOutputStream
            return MimeMessageUtility.getStreamFromMailPart(referencedPart);
        }
        return referencedPart.getInputStream();
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

    private DataSource getDataSource() {
        /*
         * Lazy creation
         */
        if (null == dataSource) {
            if (getContentType().startsWith(TEXT) && getContentType().getCharsetParameter() == null) {
                if (file.isInMemory()) {
                    getContentType().setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                } else {
                    getContentType().setCharsetParameter(System.getProperty("file.encoding", MailProperties.getInstance().getDefaultMimeCharset()));
                }
            }
            dataSource = new FileHolderDataSource(file, getContentType().toString());
        }
        return dataSource;
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }
        if (getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL)) {
            if (file.isInMemory()) {
                cachedContent = getByteContent(file.getBuffer());
            } else {
                cachedContent = getFileContent(file.getTempFile());
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

    private String getByteContent(ByteArrayOutputStream buf) throws OXException {
        String charset = getContentType().getCharsetParameter();
        if (null == charset) {
            charset = MailProperties.getInstance().getDefaultMimeCharset();
        }

        try {
            return new String(buf.toByteArray(), Charsets.forName(charset));
        } catch (UnsupportedCharsetException e) {
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
        return file.getStream();
    }

    @Override
    public void loadContent() {
        LOG.trace("ReferencedMailPart.loadContent()");
    }

    @Override
    public void prepareForCaching() {
        LOG.trace("ReferencedMailPart.prepareForCaching()");
    }

    /**
     * Closes this referenced part's file (if any).
     */
    public void close() {
        ThresholdFileHolder sink = this.file;
        if (null != sink) {
            sink.close();
        }
    }

    /**
     * Checks if referenced part is a mail
     *
     * @return <code>true</code> if referenced part is a mail; otherwise <code>false</code>
     */
    public boolean isMail() {
        return isMail;
    }

    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.REFERENCE;
    }
}
