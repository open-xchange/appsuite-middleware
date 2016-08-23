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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.java.ExceptionAwarePipedInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeCleanUp;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.FileBackedMimeMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.AbortBehavior;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.QPDecoderStream;

/**
 * {@link MimeMailPart} - Represents a MIME part as per RFC 822.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeMailPart extends MailPart implements MimeRawSource, MimeCleanUp {

    private static final long serialVersionUID = -1142595512657302179L;

    static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMailPart.class);

    /**
     * The max. in-memory size in bytes.
     */
    // TODO: Make configurable
    private static final int MAX_INMEMORY_SIZE = 131072; // 128KB

    private static volatile Boolean useMimeMultipartMailPart;

    private static boolean useMimeMultipartMailPart() {
        Boolean tmp = useMimeMultipartMailPart;
        if (null == tmp) {
            synchronized (MimeMailPart.class) {
                tmp = useMimeMultipartMailPart;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    final boolean defaultValue = false;
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.mail.mime.useMimeMultipartMailPart", defaultValue));
                    useMimeMultipartMailPart = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                useMimeMultipartMailPart = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.mime.useMimeMultipartMailPart");
            }
        });
    }

    private static final String ERR_NULL_PART = "Underlying part is null";

    /**
     * If delegate {@link Part} object is an instance of {@link MimeMessage}.
     */
    private static final int STYPE_MIME_MSG = 1;

    /**
     * If delegate {@link Part} object is an instance of {@link MimeBodyPart} whose content type signals a nested message:
     * <code>message/rfc822</code>.
     */
    private static final int STYPE_MIME_BODY_MSG = 2;

    /**
     * If delegate {@link Part} object is an instance of {@link MimeBodyPart} whose content type signals a multipart content:
     * code>multipart/*</code>.
     */
    private static final int STYPE_MIME_BODY_MULTI = 3;

    /**
     * If delegate {@link Part} object is an instance of {@link MimeBodyPart} whose content type is different from
     * <code>message/rfc822</code> and <code>multipart/*</code>.
     */
    private static final int STYPE_MIME_BODY = 4;

    /**
     * The delegate {@link Part} object.
     */
    private transient volatile Part part;

    /**
     * Cached instance of multipart.
     */
    private transient volatile MultipartWrapper multipart;

    /**
     * Whether this part's content is of MIME type <code>multipart/*</code>.
     */
    private volatile boolean isMulti;

    /**
     * Indicates whether content has been loaded via {@link #loadContent()} or not.
     */
    private volatile boolean contentLoaded;

    /**
     * Remembers serialize type on serialization.
     */
    private volatile int serializeType;

    /**
     * Remembers delegate {@link Part} object's serialized content.
     */
    private volatile byte[] serializedContent;

    /**
     * Remembers delegate {@link Part} object's content type.
     */
    private volatile String serializedContentType;

    /**
     * Whether to handle "Missing start boundary" <code>javax.mail.MessagingException</code>.
     */
    private volatile boolean handleMissingStartBoundary;

    /**
     * Constructor.
     */
    public MimeMailPart() {
        super();
    }

    /**
     * Constructor - Only applies specified part, but does not set any attributes.
     *
     * @throws OXException If setting part as content fails
     */
    public MimeMailPart(final Part part) throws OXException {
        super();
        applyPart(part);
    }

    /**
     * Initializes a new {@link MimeMailPart}.
     *
     * @param multipart The multipart
     * @throws OXException If setting multipart as content fails
     */
    public MimeMailPart(final Multipart multipart) throws OXException {
        super();
        isMulti = true;
        this.multipart = new JavaMailMultipartWrapper(multipart);
        final String contentType = multipart.getContentType();
        if (null != contentType) {
            setContentType(contentType);
        }
        try {
            final MimeBodyPart part = new MimeBodyPart();
            MessageUtility.setContent(multipart, part);
            // part.setContent(multipart);
            this.part = part;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Set whether to handle <i>"Missing start boundary"</i> <code>javax.mail.MessagingException</code>.
     * <p>
     * <b>Note</b>: Set only to <code>true</code> if JavaMail property <code>"mail.mime.multipart.allowempty"</code> is set to
     * <code>"false"</code>.
     *
     * @param handleMissingStartBoundary <code>true</code> to handle <i>"Missing start boundary"</i> error; otherwise <code>false</code>
     */
    public void setHandleMissingStartBoundary(final boolean handleMissingStartBoundary) {
        this.handleMissingStartBoundary = handleMissingStartBoundary;
    }

    /**
     * Sets this mail part's content.
     *
     * @param part The part
     * @throws OXException If part cannot be applied to this MIME mail part
     */
    public void setContent(final Part part) throws OXException {
        applyPart(part);
    }

    private static final String MULTIPART = "multipart/";

    private void applyPart(final Part part) throws OXException {
        this.part = part;
        if (null == part) {
            isMulti = false;
        } else {
            boolean tmp = false;
            try {
                /*
                 * Ensure that proper content-type is set and check if content-type denotes a multipart/ message
                 */
                final String[] ct = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                if (ct != null && ct.length > 0) {
                    this.setContentType(MimeMessageUtility.decodeMultiEncodedHeader(ct[0]));
                } else {
                    this.setContentType(MimeTypes.MIME_DEFAULT);
                }
                tmp = getContentType().startsWith(MULTIPART);
            } catch (final OXException e) {
                LOG.error("", e);
            } catch (final MessageRemovedException e) {
                // Message has been removed in the meantime
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
            } catch (final MessagingException e) {
                LOG.error("", e);
            }
            isMulti = tmp;
        }
    }

    /**
     * Gets the {@link Part part}.
     *
     * @return The {@link Part part} or <code>null</code>
     */
    @Override
    public Part getPart() {
        return part;
    }

    @Override
    public void cleanUp() {
        final Part part = this.part;
        if (part instanceof ManagedMimeMessage) {
            try {
                ((ManagedMimeMessage) part).cleanUp();
            } catch (final Exception e) {
                LoggerFactory.getLogger(MimeMailPart.class).warn("Couldn't clean-up MIME resource.", e);
            }
        }
    }

    @Override
    public Object getContent() throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return null;
        }

        ThresholdFileHolder backup = null;
        try {
            final Object obj = part.getContent();
            if (obj instanceof MimeMessage) {
                MailMessage mContent;

                MimeMessage nestedMessage = (MimeMessage) obj;
                String encoding = MimeMessageUtility.getHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, null, part);
                if ("quoted-printable".equalsIgnoreCase(encoding)) {
                    backup = new ThresholdFileHolder();
                    backup.write(new QPDecoderStream(MimeMessageUtility.getStreamFromPart(nestedMessage)));
                    FileBackedMimeMessage mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), backup.getSharedStream());
                    nestedMessage = mimeMessage;
                    mContent = MimeMessageConverter.convertMessage(nestedMessage, false);
                    backup = null; // Avoid preliminary closing
                } else if ("base64".equalsIgnoreCase(encoding)) {
                    backup = new ThresholdFileHolder();
                    backup.write(new BASE64DecoderStream(MimeMessageUtility.getStreamFromPart(nestedMessage)));
                    FileBackedMimeMessage mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), backup.getSharedStream());
                    nestedMessage = mimeMessage;
                    mContent = MimeMessageConverter.convertMessage(nestedMessage, false);
                    backup = null; // Avoid preliminary closing
                } else {
                    mContent = MimeMessageConverter.convertMessage(nestedMessage, false);
                }

                return mContent;
            } else if (obj instanceof Part) {
                return MimeMessageConverter.convertPart((Part) obj, false);
            } else {
                return obj;
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            if (null != backup) {
                backup.close();
            }
        }
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return null;
        }
        try {
            return part.getDataHandler();
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    @Override
    public InputStream getRawInputStream() throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return null;
        }
        try {
            if (part instanceof MimeBodyPart) {
                return ((MimeBodyPart) part).getRawInputStream();
            } else if (part instanceof MimeMessage) {
                return ((MimeMessage) part).getRawInputStream();
            }
            throw MailExceptionCode.NO_CONTENT.create();
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return getInputStream0(true);
    }

    private InputStream getInputStream0(final boolean handleNpe) throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return null;
        }
        try {
            try {
                // Try to read first byte and push back immediately
                final PushbackInputStream in = new PushbackInputStream(part.getInputStream());
                final int read = in.read();
                if (read < 0) {
                    Streams.close(in);
                    return Streams.EMPTY_INPUT_STREAM;
                }
                in.unread(read);
                return in;
            } catch (final com.sun.mail.util.MessageRemovedIOException e) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            } catch (final IOException e) {
                return getRawInputStream(e);
            } catch (final MessageRemovedException e) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            } catch (final MessagingException e) {
                return getRawInputStream(e);
            } catch (final NullPointerException e) {
                // Occurs in case of non-JavaMail-parseable Content-Type header
                if (!handleNpe) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                final InputStream in = sanitizeAndGetInputStream(part);
                if (null == in) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                return in;
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private InputStream sanitizeAndGetInputStream(final Part part) throws OXException {
        try {
            Part p = part;
            final String cts = MimeMessageUtility.getHeader("Content-Type", null, p);
            if (null == cts) {
                return null;
            }
            if (p.getClass().getName().startsWith("com.sun.mail.imap.IMAP")) {
                loadContent();
                p = this.part;
            }
            p.setHeader("Content-Type", new ContentType(cts).toString(true));
        } catch (final Exception x) {
            // Couldn't sanitize
            return null;
        }
        return getInputStream0(false);
    }

    private InputStream getRawInputStream(final Exception e) throws MessagingException, OXException {
        try {
            LOG.debug("Part's input stream could not be obtained. Trying to read from part's raw input stream instead", e);
            final Part part = this.part;
            if (part instanceof MimeBodyPart) {
                return ((MimeBodyPart) part).getRawInputStream();
            } else if (part instanceof MimeMessage) {
                return ((MimeMessage) part).getRawInputStream();
            }
            // Not possible to obtain raw input stream
            if (e instanceof MessagingException) {
                throw MimeMailException.handleMessagingException((MessagingException) e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessageRemovedException me) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
        } catch (final MessagingException me) {
            me.setNextException(e);
            throw me;
        }
    }



    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return getMultipartWrapper().getMailPart(index);
        }
        return null;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            if (handleMissingStartBoundary) {
                final MultipartWrapper wrapper = getMultipartWrapper();
                try {
                    return wrapper.getCount();
                } catch (final OXException e) {
                    return handleMissingStartBoundary(e);
                }
            }
            /*
             * No handling
             */
            return getMultipartWrapper().getCount();
        }
        return NO_ENCLOSED_PARTS;
    }

    private int handleMissingStartBoundary(final OXException e) throws OXException {
        final Throwable cause = e.getCause();
        if (!(cause instanceof MessagingException) || !"Missing start boundary".equals(((MessagingException) cause).getMessage())) {
            throw e;
        }
        try {
            /*
             * Retry with other non-stream-based MultipartWrapper implementation
             */
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
            final Part part = this.part;
            part.writeTo(out);
            final MultipartWrapper multipart = new MIMEMultipartWrapper(new MIMEMultipartMailPart(getContentType(), part.getDataHandler().getDataSource()));
            this.multipart = multipart;
            return multipart.getCount();
        } catch (final IOException e1) {
            LOG.error("", e1);
            /*
             * Throw original mail exception
             */
            throw e;
        } catch (final MessagingException e1) {
            LOG.error("", e1);
            /*
             * Throw original mail exception
             */
            throw e;
        }
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        try {
            if (part instanceof MimeMessage && !(part instanceof com.sun.mail.imap.IMAPMessage)) {
                saneContentType();
            }
            part.writeTo(out);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            if ("No content".equals(e.getMessage())) {
                throw MailExceptionCode.NO_CONTENT.create(e, new Object[0]);
            }
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private void saneContentType() throws MessagingException {
        final Part part = this.part;
        final String[] header = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
        if (null != header && header.length > 0) {
            try {
                part.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new ContentType(header[0]).toString());
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public void prepareForCaching() {
        /*
         * Release references
         */
        if (!contentLoaded) {
            this.multipart = null;
            this.part = null;
        }
    }

    @Override
    public void loadContent() throws OXException {
        Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (contentLoaded) {
            /*
             * Already loaded...
             */
            return;
        }
        try {
            if (part instanceof MimeBodyPart) {
                final ContentType contentType;
                {
                    final String[] ct = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                    if (ct != null && ct.length > 0) {
                        contentType = new ContentType(ct[0]);
                    } else {
                        contentType = new ContentType(MimeTypes.MIME_DEFAULT);
                    }
                }
                if (contentType.startsWith("multipart/")) {
                    /*
                     * Compose a new body part with multipart/ data
                     */
                    this.part = part = createBodyMultipart(getStreamFromMultipart(getMultipartContentFrom(part, contentType.toString())), contentType.toString());
                    this.multipart = null;
                    contentLoaded = true;
                } else if (contentType.startsWith(MimeTypes.MIME_MESSAGE_RFC822)) {
                    /*
                     * Compose a new body part with message/rfc822 data
                     */
                    this.part = part = MimeMessageUtility.clonePart(part);
                    contentLoaded = true;
                } else {
                    this.part = part = MimeMessageUtility.clonePart(part);
                    contentLoaded = true;
                }
            } else if (part instanceof MimeMessage) {
                this.part = part = MimeMessageUtility.cloneMessage((MimeMessage) part, null);
                multipart = null;
                contentLoaded = true;
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /** Gets the multipart content from specified part. */
    private static Multipart getMultipartContentFrom(final Part part, final String contentType) throws MessagingException, IOException {
        return MimeMessageUtility.getMultipartContentFrom(part, contentType);
    }

    /**
     * The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding
     * readObject method can restore it. The default mechanism for saving the Object's fields can be invoked by calling
     * <code>ObjectOutputStream.defaultWriteObject()</code>. The method does not need to concern itself with the state belonging to its
     * super classes or subclasses. State is saved by writing the individual fields to the ObjectOutputStream using the writeObject method
     * or by using the methods for primitive data types supported by <code>DataOutput</code> .
     *
     * @param out The object output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        this.multipart = null;
        final Part part = this.part;
        if (part == null) {
            serializeType = 0;
            serializedContent = null;
            out.defaultWriteObject();
            return;
        }
        /*
         * Remember serialize type and content
         */
        try {
            if (part instanceof MimeBodyPart) {
                final ContentType contentType;
                {
                    final String[] ct = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                    if (ct != null && ct.length > 0) {
                        contentType = new ContentType(ct[0]);
                    } else {
                        contentType = new ContentType(MimeTypes.MIME_DEFAULT);
                    }
                }
                if (contentType.startsWith("multipart/")) {
                    serializeType = STYPE_MIME_BODY_MULTI;
                    serializedContent = getBytesFromMultipart(getMultipartContentFrom(part, contentType.toString()));
                    serializedContentType = contentType.toString();
                } else if (contentType.startsWith(MimeTypes.MIME_MESSAGE_RFC822)) {
                    serializeType = STYPE_MIME_BODY_MSG;
                    //serializedContent = getBytesFromPart((Message) part.getContent());
                    serializedContent = getBytesFromPart(part);
                } else {
                    serializeType = STYPE_MIME_BODY;
                    serializedContent = getBytesFromPart(part);
                }
            } else if (part instanceof MimeMessage) {
                serializeType = STYPE_MIME_MSG;
                serializedContent = getBytesFromPart(part);
            }
            /*
             * Write common fields
             */
            out.defaultWriteObject();
        } catch (final OXException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (final MessagingException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } finally {
            /*
             * Discard content created for serialization
             */
            serializeType = 0;
            serializedContent = null;
            serializedContentType = null;
        }
    }

    /**
     * The readObject method is responsible for reading from the stream and restoring the classes fields. It may call in.defaultReadObject
     * to invoke the default mechanism for restoring the object's non-static and non-transient fields. The
     * <code>ObjectInputStream.defaultReadObject</code> method uses information in the stream to assign the fields of the object saved in
     * the stream with the correspondingly named fields in the current object. This handles the case when the class has evolved to add new
     * fields. The method does not need to concern itself with the state belonging to its super classes or subclasses. State is saved by
     * writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for primitive data types
     * supported by <code>DataOutput</code>.
     *
     * @param in The object input stream
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a casting fails
     */
    private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        /*
         * Restore common fields
         */
        in.defaultReadObject();
        final int serializeType = this.serializeType;
        if (serializeType > 0) {
            try {
                /*
                 * Restore part
                 */
                final byte[] serializedContent = this.serializedContent;
                if (STYPE_MIME_BODY_MSG == serializeType) {
                    /*
                     * Compose a new body part with message/rfc822 data
                     */
                    this.part = createBodyPartInMemory(Streams.newByteArrayInputStream(serializedContent));
                    //this.part = createBodyMessageInMemory(Streams.newByteArrayInputStream(serializedContent));
                    contentLoaded = true;
                } else if (STYPE_MIME_BODY_MULTI == serializeType) {
                    /*
                     * Compose a new body part with multipart/ data
                     */
                    this.part = createBodyMultipartInMemory(Streams.newByteArrayInputStream(serializedContent), serializedContentType);
                    this.multipart = null;
                    contentLoaded = true;
                } else if (STYPE_MIME_BODY == serializeType) {
                    this.part = createBodyPartInMemory(Streams.newByteArrayInputStream(serializedContent));
                    contentLoaded = true;
                } else if (STYPE_MIME_MSG == serializeType) {
                    this.part = createMessageInMemory(Streams.newByteArrayInputStream(serializedContent));
                    contentLoaded = true;
                }
            } catch (final MessagingException e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                /*
                 * Discard content created for serialization
                 */
                this.serializeType = 0;
                this.serializedContent = null;
                this.serializedContentType = null;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Compose a new MIME body part with multipart/* data.
     *
     * @param data The multipart/* data
     * @param contentType The multipart's content type (containing important boundary parameter)
     * @return A new MIME body part with multipart/* data
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    private static MimeBodyPart createBodyMultipartInMemory(final InputStream data, final String contentType) throws MessagingException, IOException {
        if (null == data) {
            return null;
        }
        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
        MessageUtility.setContent(new MimeMultipart(new MessageDataSource(data, contentType)), mimeBodyPart);
        // mimeBodyPart.setContent(new MimeMultipart(new MessageDataSource(data, contentType)));
        return mimeBodyPart;
    }

    /**
     * Compose a new MIME body part directly from specified data.
     *
     * @param data The part's data
     * @return A new MIME body part
     * @throws MessagingException If a messaging error occurs
     */
    private static MimeBodyPart createBodyPartInMemory(final InputStream data) throws MessagingException {
        return new MimeBodyPart(data);
    }

    /**
     * Compose a new MIME message directly from specified data.
     *
     * @param data The message's data
     * @return A new MIME message
     * @throws MessagingException If a messaging error occurs
     */
    private static MimeMessage createMessageInMemory(final InputStream data) throws MessagingException {
        return new MimeMessage(MimeDefaultSession.getDefaultSession(), data);
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Compose a new MIME body part with multipart/* data.
     *
     * @param data The multipart/* data
     * @param contentType The multipart's content type (containing important boundary parameter)
     * @return A new MIME body part with multipart/* data
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an I/O error occurs
     */
    private static MimeBodyPart createBodyMultipart(final InputStream data, final String contentType) throws MessagingException, OXException {
        if (null == data) {
            return null;
        }
        final MimeBodyPart mimeBodyPart = new MimeBodyPart();
        MessageUtility.setContent(new MimeMultipart(MimeMessageUtility.newDataSource(data, contentType)), mimeBodyPart);
        // mimeBodyPart.setContent(new MimeMultipart(new MessageDataSource(data, contentType)));
        return mimeBodyPart;
    }

    /**
     * Gets the stream of specified multipart's raw data.
     *
     * @param multipart A multipart object
     * @return The stream of specified multipart's raw data (with the optional empty starting line omitted)
     * @throws IOException If an I/O error occurs
     */
    private static InputStream getStreamFromMultipart(final Multipart multipart) throws IOException {
        if (null == multipart) {
            return null;
        }
        final PipedOutputStream pos = new PipedOutputStream();
        final ExceptionAwarePipedInputStream pin = new ExceptionAwarePipedInputStream(pos, 65536);

        {
            final Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        multipart.writeTo(pos);
                    } catch (final Exception e) {
                        pin.setException(e);
                    } finally {
                        Streams.close(pos);
                    }
                }
            };
            final ThreadPoolService threadPool = ThreadPools.getThreadPool();
            if (null == threadPool) {
                new Thread(r, "MimeMailPart.getStreamFromMultipart").start();
            } else {
                threadPool.submit(ThreadPools.task(r), AbortBehavior.getInstance());
            }
        }

        return stripEmptyStartingLine(pin);
    }

    /**
     * Strips the possible empty starting characters from specified input stream.
     *
     * @param data The input stream
     * @return The stripped input stream
     * @throws IOException If an I/O error occurs
     */
    private static InputStream stripEmptyStartingLine(final InputStream data) throws IOException {
        if (null == data) {
            return data;
        }
        // Drop leading white-space character
        final PushbackInputStream in = new PushbackInputStream(data);
        int read = in.read();
        while (Strings.isWhitespace((char) read)) {
            read = in.read();
            if (read < 0) {
                Streams.close(in);
                throw new IOException("Unexpected end of stream");
            }
        }
        in.unread(read);
        return in;
    }

    /**
     * Gets the bytes of specified part's raw data.
     *
     * @param part Either a message or a body part
     * @return The bytes of specified part's raw data (with the optional empty starting line omitted)
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    private static byte[] getBytesFromPart(final Part part) throws IOException, MessagingException {
        byte[] data;
        {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(4096);
            part.writeTo(out);
            data = out.toByteArray();
        }
        return stripEmptyStartingLine(data);
    }

    /**
     * Gets the bytes of specified multipart's raw data.
     *
     * @param multipart A multipart object
     * @return The bytes of specified multipart's raw data (with the optional empty starting line omitted)
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    private static byte[] getBytesFromMultipart(final Multipart multipart) throws IOException, MessagingException {
        byte[] data;
        {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(4096);
            multipart.writeTo(out);
            data = out.toByteArray();
        }
        return stripEmptyStartingLine(data);
    }

    /**
     * Strips the possible empty starting line from specified byte array.
     *
     * @param data The byte array
     * @return The stripped byte array
     */
    private static byte[] stripEmptyStartingLine(final byte[] data) {
        if (null == data || data.length <= 1) {
            return data;
        }
        /*
         * Starts with an empty line?
         */
        int start = 0;
        if (data[start] == '\r') {
            start++;
        }
        if (data[start] == '\n') {
            start++;
        }
        if (start > 0) {
            final byte[] data0 = new byte[data.length - start];
            System.arraycopy(data, start, data0, 0, data0.length);
            return data0;
        }
        return data;
    }

    private MultipartWrapper getMultipartWrapper() throws OXException {
        MultipartWrapper multipart = this.multipart;
        if (null == multipart) {
            synchronized (this) {
                multipart = this.multipart;
                if (null == multipart) {
                    try {
                        Part part = this.part;
                        final int size;
                        if (useMimeMultipartMailPart() && ((size = part.getSize()) > 0) && (size <= MAX_INMEMORY_SIZE)) {
                            /*
                             * If size is less than or equal to 1MB, use the in-memory implementation
                             */
                            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(size);
                            part.writeTo(out);
                            multipart = new MIMEMultipartWrapper(new MIMEMultipartMailPart(getContentType(), out.toByteArray()));
                            this.multipart = multipart;
                        } else {
                            /*
                             * If size is unknown or exceeds 1MB, use the stream-based implementation
                             */
                            final Object content = part.getContent();
                            if (content instanceof Multipart) {
                                multipart = new JavaMailMultipartWrapper((Multipart) content);
                                this.multipart = multipart;
                            } else {
                                /*-
                                 * Content object is not a Multipart. Then data is kept in memory regardless of MultipartWrapper implementation.
                                 *
                                 * Since MIMEMultipartMailPart was introduced to provide a faster multipart/* parsing, use the MIMEMultipartWrapper
                                 * implementation to take benefit of improved parsing.
                                 */
                                if (content instanceof InputStream) {
                                    /*
                                     * Close in case of InputStream
                                     */
                                    closeQuitely((InputStream) content);
                                }
                                loadContent();
                                part = this.part;
                                final String headerName = "Content-Type";
                                final String[] header = part.getHeader(headerName);
                                if (null != header) {
                                    part.setHeader(headerName, new ContentType(header[0]).toString());
                                }
                                multipart = new JavaMailMultipartWrapper(MimeMessageConverter.multipartFor(part.getContent(), getContentType()));
                                this.multipart = multipart;
                            }
                        }
                    } catch (final MessageRemovedException e) {
                        throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
                    } catch (final MessagingException e) {
                        throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
                    } catch (final UnsupportedEncodingException e) {
                        LOG.error("Unsupported encoding in a message detected and monitored", e);
                        mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
                        throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
                    } catch (final IOException e) {
                        if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                        }
                        throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                    } catch (final ClassCastException e) {
                        // Cast to javax.mail.Multipart failed
                        LOG.debug("Message's Content-Type indicates to be multipart/* but its content is not an instance of javax.mail.Multipart but {}", e.getMessage());
                        throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        return multipart;
    }

    private static interface MultipartWrapper {

        public int getCount() throws OXException;

        public MailPart getMailPart(int index) throws OXException;
    }

    private static class MIMEMultipartWrapper implements MultipartWrapper {

        private final MIMEMultipartMailPart multipartMailPart;

        public MIMEMultipartWrapper(final MIMEMultipartMailPart multipartMailPart) {
            super();
            this.multipartMailPart = multipartMailPart;
        }

        @Override
        public int getCount() throws OXException {
            return multipartMailPart.getEnclosedCount();
        }

        @Override
        public MailPart getMailPart(final int index) throws OXException {
            return multipartMailPart.getEnclosedMailPart(index);
        }

    } // End of MIMEMultipartWrapper

    private static class JavaMailMultipartWrapper implements MultipartWrapper {

        private final Multipart jmMultipart;

        public JavaMailMultipartWrapper(final Multipart multipart) {
            super();
            this.jmMultipart = multipart;
        }

        @Override
        public int getCount() throws OXException {
            try {
                return jmMultipart.getCount();
            } catch (final MessagingException e) {
                throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }

        @Override
        public MailPart getMailPart(final int index) throws OXException {
            try {
                return MimeMessageConverter.convertPart(jmMultipart.getBodyPart(index), false);
            } catch (final MessagingException e) {
                throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }

    } // End of JavaMailMultipartWrapper

    private static void closeQuitely(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException e) {
            LOG.trace("", e);
        }
    }
}
