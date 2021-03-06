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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
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
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ClonedMimeMultipart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.EmptyStringMimeMultipart;
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
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.sun.mail.smtp.CountingOutputStream;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.QPDecoderStream;

/**
 * {@link MimeMailPart} - Represents a MIME part as per RFC 822.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeMailPart extends MailPart implements MimeRawSource, MimeCleanUp {

    private static final long serialVersionUID = -1142595512657302179L;

    /** The default max. serialization size (10MB) */
    private static final long DEFAULT_MAX_SERIALIZATION_SIZE = 10485760L;

    private static long getMaxSerializationSize() {
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == configService) {
            return DEFAULT_MAX_SERIALIZATION_SIZE;
        }

        String property = configService.getProperty("com.openexchange.mail.maxMailSize");
        if (Strings.isEmpty(property)) {
            return DEFAULT_MAX_SERIALIZATION_SIZE;
        }

        try {
            return Long.parseLong(property);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return DEFAULT_MAX_SERIALIZATION_SIZE;
        }
    }

    static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMailPart.class);

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

    private static final String CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;

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
    public MimeMailPart(Part part) throws OXException {
        super();
        applyPart(part);
    }

    /**
     * Initializes a new {@link MimeMailPart}.
     *
     * @param multipart The multipart
     * @throws OXException If setting multipart as content fails
     */
    public MimeMailPart(Multipart multipart) throws OXException {
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
        } catch (MessagingException e) {
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
    public void setHandleMissingStartBoundary(boolean handleMissingStartBoundary) {
        this.handleMissingStartBoundary = handleMissingStartBoundary;
    }

    /**
     * Sets this mail part's content.
     *
     * @param part The part
     * @throws OXException If part cannot be applied to this MIME mail part
     */
    public void setContent(Part part) throws OXException {
        applyPart(part);
    }

    private static final String MULTIPART = "multipart/";

    private void applyPart(Part part) throws OXException {
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
            } catch (OXException e) {
                LOG.error("", e);
            } catch (MessageRemovedException e) {
                // Message has been removed in the meantime
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
            } catch (MessagingException e) {
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
            } catch (Exception e) {
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
        boolean closeBackup = true;
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
                    closeBackup = false; // Avoid preliminary closing
                } else if ("base64".equalsIgnoreCase(encoding)) {
                    backup = new ThresholdFileHolder();
                    backup.write(new BASE64DecoderStream(MimeMessageUtility.getStreamFromPart(nestedMessage)));
                    FileBackedMimeMessage mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), backup.getSharedStream());
                    nestedMessage = mimeMessage;
                    mContent = MimeMessageConverter.convertMessage(nestedMessage, false);
                    closeBackup = false; // Avoid preliminary closing
                } else {
                    mContent = MimeMessageConverter.convertMessage(nestedMessage, false);
                }

                return mContent;
            } else if (obj instanceof Part) {
                return MimeMessageConverter.convertPart((Part) obj, false);
            } else {
                return obj;
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            if (closeBackup) {
                Streams.close(backup);
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
        } catch (MessagingException e) {
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
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return getInputStream0(true);
    }

    private InputStream getInputStream0(boolean handleNpe) throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return null;
        }
        try {
            InputStream partStream = null;
            try {
                // Try to read first byte and push back immediately
                partStream = part.getInputStream();
                final PushbackInputStream in = partStream instanceof PushbackInputStream ? (PushbackInputStream) partStream : new PushbackInputStream(partStream);
                final int read = in.read();
                if (read < 0) {
                    Streams.close(in);
                    partStream = null;
                    return Streams.EMPTY_INPUT_STREAM;
                }
                in.unread(read);
                partStream = null;
                return in;
            } catch (com.sun.mail.util.MessageRemovedIOException e) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            } catch (IOException e) {
                return getRawInputStream(e);
            } catch (MessageRemovedException e) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            } catch (MessagingException e) {
                return getRawInputStream(e);
            } catch (NullPointerException e) {
                // Occurs in case of non-JavaMail-parseable Content-Type header
                if (!handleNpe) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                final InputStream in = sanitizeAndGetInputStream(part);
                if (null == in) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                return in;
            } finally {
                Streams.close(partStream);
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private InputStream sanitizeAndGetInputStream(Part part) throws OXException {
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
        } catch (Exception x) {
            // Couldn't sanitize
            LOG.trace("Sanitizing part failed", x);
            return null;
        }
        return getInputStream0(false);
    }

    private InputStream getRawInputStream(Exception e) throws MessagingException, OXException {
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
        } catch (MessageRemovedException me) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(me);
        } catch (MessagingException me) {
            me.setNextException(e);
            throw me;
        }
    }



    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
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
                } catch (OXException e) {
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

    /**
     * Checks if backing multipart content was initialized from an empty string
     *
     * @return <code>true</code> if backing multipart content was initialized from an empty string; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isEmptyStringMultipart() throws OXException {
        Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        if (isMulti) {
            return getMultipartWrapper().isEmptyStringContent();
        }
        return false;
    }

    private int handleMissingStartBoundary(OXException e) throws OXException {
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
        } catch (IOException e1) {
            LOG.error("", e1);
            /*
             * Throw original mail exception
             */
            throw e;
        } catch (MessagingException e1) {
            LOG.error("", e1);
            /*
             * Throw original mail exception
             */
            throw e;
        }
    }

    @Override
    public void writeTo(OutputStream out) throws OXException {
        final Part part = this.part;
        if (null == part) {
            throw new IllegalStateException(ERR_NULL_PART);
        }
        try {
            if (part instanceof MimeMessage && !(part instanceof com.sun.mail.imap.IMAPMessage)) {
                saneContentType();
            }
            part.writeTo(out);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
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
            } catch (Exception e) {
                // Ignore
                LOG.trace("Failed to set header", e);
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
                if (contentType.startsWith(MULTIPART)) {
                    /*
                     * Compose a new body part with multipart/ data
                     */
                    Multipart ex = getMultipartContentFrom(part, contentType.toString());
                    if (ex instanceof EmptyStringMimeMultipart) {
                        MimeBodyPart mimeBodyPart = new MimeBodyPart();
                        MessageUtility.setContent(ex, mimeBodyPart);
                        this.part = part = mimeBodyPart;
                    } else {
                        MimeBodyPart mimeBodyPart = new MimeBodyPart();
                        MessageUtility.setContent(new ClonedMimeMultipart(ex), mimeBodyPart);
                        this.part = part = mimeBodyPart;
                    }
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
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored", e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /** Gets the multipart content from specified part. */
    private static Multipart getMultipartContentFrom(Part part, String contentType) throws MessagingException, IOException {
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
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
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
                if (contentType.startsWith(MULTIPART)) {
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
        } catch (OXException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (MessagingException e) {
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
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
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
            } catch (MessagingException e) {
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
    private static MimeBodyPart createBodyMultipartInMemory(InputStream data, String contentType) throws MessagingException, IOException {
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
    private static MimeBodyPart createBodyPartInMemory(InputStream data) throws MessagingException {
        return new MimeBodyPart(data);
    }

    /**
     * Compose a new MIME message directly from specified data.
     *
     * @param data The message's data
     * @return A new MIME message
     * @throws MessagingException If a messaging error occurs
     */
    private static MimeMessage createMessageInMemory(InputStream data) throws MessagingException {
        return new MimeMessage(MimeDefaultSession.getDefaultSession(), data);
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Gets the bytes of specified part's raw data.
     *
     * @param part Either a message or a body part
     * @return The bytes of specified part's raw data (with the optional empty starting line omitted)
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    private static byte[] getBytesFromPart(Part part) throws IOException, MessagingException {
        byte[] data;
        {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(4096);
            long maxSerializationSize = getMaxSerializationSize();
            part.writeTo(maxSerializationSize > 0 ? new CountingOutputStream(out, maxSerializationSize) : out);
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
    private static byte[] getBytesFromMultipart(Multipart multipart) throws IOException, MessagingException {
        byte[] data;
        {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(4096);
            long maxSerializationSize = getMaxSerializationSize();
            multipart.writeTo(maxSerializationSize > 0 ? new CountingOutputStream(out, maxSerializationSize) : out);
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
    private static byte[] stripEmptyStartingLine(byte[] data) {
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
                        /*-
                         *
                        if (false && ((size = part.getSize()) > 0) && (size <= MAX_INMEMORY_SIZE)) {
                            // If size is less than or equal to 1MB, use the in-memory implementation
                            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(size);
                            part.writeTo(out);
                            multipart = new MIMEMultipartWrapper(new MIMEMultipartMailPart(getContentType(), out.toByteArray()));
                            this.multipart = multipart;
                        } else {
                         */
                            /*
                             * If size is unknown or exceeds 1MB, use the stream-based implementation
                             */
                            final Object content = part.getContent();
                            Multipart javaMailMultipart = getValidMultipartFor(content);
                            if (null != javaMailMultipart) {
                                multipart = new JavaMailMultipartWrapper(javaMailMultipart);
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
                       // }
                    } catch (MessageRemovedException e) {
                        throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
                    } catch (MessagingException e) {
                        throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("Unsupported encoding in a message detected and monitored", e);
                        mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
                        throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
                    } catch (IOException e) {
                        if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                        }
                        throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                    } catch (ClassCastException e) {
                        // Cast to javax.mail.Multipart failed
                        LOG.debug("Message's Content-Type indicates to be multipart/* but its content is not an instance of javax.mail.Multipart but {}", e.getMessage());
                        throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        return multipart;
    }

    private Multipart getValidMultipartFor(Object content) throws OXException {
        if (!(content instanceof Multipart)) {
            return null;
        }

        try {
            Multipart mp = (Multipart) content;
            int count = mp.getCount();
            if (count == 1) {
                // Check for empty text/plain part
                BodyPart bodyPart = mp.getBodyPart(0);
                String[] contentTypeHdr = bodyPart.getHeader(CONTENT_TYPE);
                if (null == contentTypeHdr) {
                    String stringContent = MessageUtility.readMimePart(bodyPart, "US-ASCII");
                    if (Strings.isEmpty(stringContent)) {
                        return new EmptyStringMimeMultipart(new MessageDataSource(new byte[0], MimeMessageUtility.getHeader("Content-Type", null, part)));
                    }
                }
            }
            return mp;
        } catch (MessageRemovedException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
        } catch (MessagingException e) {
            LOG.error("Failed to examine multipart content.", e);
            return null;
        }
    }

    private static interface MultipartWrapper {

        public int getCount() throws OXException;

        public MailPart getMailPart(int index) throws OXException;

        public boolean isEmptyStringContent();
    }

    private static class MIMEMultipartWrapper implements MultipartWrapper {

        private final MIMEMultipartMailPart multipartMailPart;

        public MIMEMultipartWrapper(MIMEMultipartMailPart multipartMailPart) {
            super();
            this.multipartMailPart = multipartMailPart;
        }

        @Override
        public int getCount() throws OXException {
            return multipartMailPart.getEnclosedCount();
        }

        @Override
        public MailPart getMailPart(int index) throws OXException {
            return multipartMailPart.getEnclosedMailPart(index);
        }

        @Override
        public boolean isEmptyStringContent() {
            return false;
        }

    } // End of MIMEMultipartWrapper

    private static class JavaMailMultipartWrapper implements MultipartWrapper {

        private final Multipart jmMultipart;

        public JavaMailMultipartWrapper(Multipart multipart) {
            super();
            this.jmMultipart = multipart;
        }

        @Override
        public int getCount() throws OXException {
            try {
                return jmMultipart.getCount();
            } catch (MessagingException e) {
                throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }

        @Override
        public MailPart getMailPart(int index) throws OXException {
            try {
                return MimeMessageConverter.convertPart(jmMultipart.getBodyPart(index), false);
            } catch (MessagingException e) {
                throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }

        @Override
        public boolean isEmptyStringContent() {
            return jmMultipart instanceof EmptyStringMimeMultipart;
        }

    } // End of JavaMailMultipartWrapper

    private static void closeQuitely(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOG.trace("", e);
        }
    }
}
