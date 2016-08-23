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

package com.openexchange.mail.mime.datasource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.message.AbstractEntity;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.storage.TempFileStorageProvider;
import org.apache.james.mime4j.storage.ThresholdStorageProvider;
import org.apache.james.mime4j.stream.RawFieldParser;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;

/**
 * {@link MimeMessageDataSource} - A MIME message data source.
 * <p>
 * Converts a {@link MimeMessage} to a <a href="http://james.apache.org/mime4j/">mime4j</a> {@link Message} and writes it using
 * {@link MessageWriter}. This bypasses the need for JAF look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeMessageDataSource implements DataSource, CleanUp {

    private static final String SUFFIX = ".tmp";
    private static final String PREFIX = "open-xchange-";

    private static volatile File directory;
    private static File directory() {
        File tmp = directory;
        if (null == tmp) {
            synchronized (MimeMessageDataSource.class) {
                tmp = directory;
                if (null == tmp) {
                    final String property = ServerConfig.getProperty(ServerConfig.Property.UploadDirectory);
                    tmp = new File(null == property ? "/tmp" : property);
                    directory = tmp;
                }
            }
        }
        return tmp;
    }

    private static volatile Field filesToDelete;
    static Field filesToDelete() {
        Field tmp = filesToDelete;
        if (null == tmp) {
            synchronized (MimeMessageDataSource.class) {
                tmp = filesToDelete;
                if (null == tmp) {
                    Class<?> innerClass = null;
                    final Class<?>[] classes = TempFileStorageProvider.class.getDeclaredClasses();
                    for (int i = 0; null == innerClass && i < classes.length; i++) {
                        final Class<?> clazz = classes[i];
                        if (clazz.getName().endsWith("TempFileStorage")) {
                            innerClass = clazz;
                        }
                    }
                    if (null != innerClass) {
                        try {
                            tmp = innerClass.getDeclaredField("filesToDelete");
                            tmp.setAccessible(true);
                            filesToDelete = tmp;
                        } catch (final Exception e) {
                            // Ignore
                        }
                    }
                }
            }
        }
        return tmp;
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                directory = null;
                filesToDelete = null;
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });
    }

    /** The mime4j message */
    private final MessageImpl message;

    /** The temp store */
    private final StorageProvider tempStore;

    /**
     * Initializes a new {@link MimeMessageDataSource}.
     * <p>
     * <b>Note</b>: {@link #cleanUp()}
     *
     * @param mimeMessage The source MIME message
     * @param optConfig The optional mail configuration (for improved error messages)
     * @param optSession The optional session (for improved error messages)
     * @throws OXException If initialization fails
     * @see #cleanUp()
     */
    public MimeMessageDataSource(final MimeMessage mimeMessage) throws OXException {
        this(mimeMessage, null, null);
    }

    /**
     * Initializes a new {@link MimeMessageDataSource}.
     * <p>
     * <b>Note</b>: {@link #cleanUp()}
     *
     * @param mimeMessage The source MIME message
     * @param optConfig The optional mail configuration (for improved error messages)
     * @param optSession The optional session (for improved error messages)
     * @throws OXException If initialization fails
     * @see #cleanUp()
     */
    public MimeMessageDataSource(final MimeMessage mimeMessage, final MailConfig optConfig, final Session optSession) throws OXException {
        super();
        message = new MessageImpl();
        final StorageProvider tempStore = new TempFileStorageProvider(PREFIX, SUFFIX, directory());
        final StorageProvider provider = new ThresholdStorageProvider(tempStore, 8192);
        final StorageBodyFactory bodyFactory = new StorageBodyFactory(provider, null);
        mime4jOf(mimeMessage, message, bodyFactory, optConfig, optSession);
        this.tempStore = tempStore;
    }

    /**
     * Cleans-up this data source.
     */
    @Override
    public void cleanUp() {
        final MessageImpl message = this.message;
        if (null != message) {
            message.dispose();
        }
        final StorageProvider tempStore = this.tempStore;
        if (null != tempStore) {
            try {
                @SuppressWarnings("unchecked") final Set<File> filesToDelete = (Set<File>) filesToDelete().get(null);
                synchronized (filesToDelete) {
                    for (final Iterator<File> iterator = filesToDelete.iterator(); iterator.hasNext();) {
                        final File file = iterator.next();
                        if (file.delete()) {
                            iterator.remove();
                        }
                    }
                }
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public String getContentType() {
        return message.getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final MessageWriter messageWriter = DEFAULT_MESSAGE_WRITER;
        final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(8192);
        messageWriter.writeMessage(message, out);
        return Streams.asInputStream(out);
    }

    @Override
    public String getName() {
        return message.getFilename();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() is not implemented");
    }

    /**
     * Output to given byte stream.
     *
     * @throws IOException If an error occurs while writing to the stream
     */
    public void writeTo(final OutputStream os) throws IOException {
        writeTo(message, os);
    }

    // ----------------------------------------------------------------------------------------------------- //

    /** Default implementation of {@link MessageWriter}. */
    private static final DefaultMessageWriter DEFAULT_MESSAGE_WRITER = new DefaultMessageWriter();

    /**
     * Output to given byte stream.
     *
     * @param message The message to write
     * @param os The output stream to wrote to
     * @throws IOException If an error occurs while writing to the stream
     */
    public static void writeTo(final Message message, final OutputStream os) throws IOException {
        if (null == message || null == os) {
            return;
        }
        DEFAULT_MESSAGE_WRITER.writeMessage(message, os);
    }

    /**
     * Maps given MIME part to specified mime4j instance.
     *
     * @param mimeMessage The MIME message (source)
     * @param optConfig The mail configuration
     * @param optSession The user session
     * @return The resulting message with {@link CleanUp} support
     * @throws OXException If mapping fails
     */
    public static Message mime4jOf(final MimeMessage mimeMessage, final MailConfig optConfig, final Session optSession) throws OXException {
        final MessageImpl message = new MessageImpl();
        final StorageProvider tempStore = new TempFileStorageProvider(PREFIX, SUFFIX, directory());
        final StorageProvider provider = new ThresholdStorageProvider(tempStore, 8192);
        final StorageBodyFactory bodyFactory = new StorageBodyFactory(provider, null);
        mime4jOf(mimeMessage, message, bodyFactory, optConfig, optSession);
        return new CleanUpMessageImpl(message, tempStore);
    }

    /**
     * Maps given MIME part to specified mime4j instance.
     *
     * @param mimePart The MIME part (source)
     * @param entity The mime4j instance (destination)
     * @param bodyFactory The body factory
     * @param mailConfig The mail configuration
     * @param session The user session
     * @throws OXException If mapping fails
     */
    private static void mime4jOf(final MimePart mimePart, final AbstractEntity entity, final StorageBodyFactory bodyFactory, final MailConfig mailConfig, final Session session) throws OXException {
        try {
            // Almighty Content-Type
            final ContentType contentType = new ContentType(mimePart.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null));
            final String name = contentType.getNameParameter();
            // Body
            if (contentType.startsWith("multipart/")) {
                final Multipart m = MimeMessageUtility.multipartFrom(mimePart);
                final String subType = parseSubType(m.getContentType());
                final org.apache.james.mime4j.dom.Multipart mime4jMultipart = new MultipartImpl(null == subType ? "mixed" : com.openexchange.java.Strings.toLowerCase(subType));
                // A multipart may have a preamble
                mime4jMultipart.setPreamble("This is a multi-part message in MIME format.");
                final int count = m.getCount();
                for (int i = 0; i < count; i++) {
                    final MimeBodyPart bodyPart = (MimeBodyPart) m.getBodyPart(i);
                    final AbstractEntity mime4jBodyPart = getEntityByContentType(bodyPart.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null));
                    mime4jOf(bodyPart, mime4jBodyPart, bodyFactory, mailConfig, session);
                    mime4jMultipart.addBodyPart(mime4jBodyPart);
                }
                entity.setMultipart(mime4jMultipart);
            } else if (contentType.startsWith("message/rfc822") || (name != null && name.endsWith(".eml"))) {
                final MimeMessage m = (MimeMessage) mimePart.getContent();
                final MessageImpl mime4jMessage = new MessageImpl();
                mime4jOf(m, mime4jMessage, bodyFactory, mailConfig, session);
                entity.setMessage(mime4jMessage);
            } else if (contentType.startsWith("text/")) {
                // A text part
                String text = tryGetStringContent(mimePart);
                if (null == text) {
                    text = MessageUtility.readMimePart(mimePart, contentType);
                }
                final TextBody textBody = bodyFactory.textBody(text, contentType.getCharsetParameter());
                entity.setBody(textBody, contentType.getBaseType());
            } else {
                // Binary
                final BinaryBody binaryBody = bodyFactory.binaryBody(mimePart.getInputStream());
                entity.setBody(binaryBody, contentType.getBaseType());
            }
            // Headers
            {
                final RawFieldParser fieldParser = RawFieldParser.DEFAULT;
                final HeaderImpl mime4jHeader = new HeaderImpl();
                @SuppressWarnings("unchecked") final Enumeration<String> enumeration = mimePart.getAllHeaderLines();
                while (enumeration.hasMoreElements()) {
                    final String headerLine = enumeration.nextElement();
                    final byte[] bytes = headerLine.getBytes(Charsets.ISO_8859_1);
                    mime4jHeader.addField(fieldParser.parseField(new ByteArrayBuffer(bytes, bytes.length, true)));
                }
                entity.setHeader(mime4jHeader);
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, mailConfig, session);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MimeException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static AbstractEntity getEntityByContentType(final String sContentType) {
        if (null == sContentType) {
            return null;
        }
        if (com.openexchange.java.Strings.toLowerCase(sContentType).startsWith("message/rfc822")) {
            return new MessageImpl();
        }
        try {
            final String name = new ContentType(sContentType).getNameParameter();
            if (name != null && name.endsWith(".eml")) {
                return new MessageImpl();
            }
        } catch (final OXException e) {
        }
        return new BodyPart();

    }

    private static String parseSubType(final String sContentType) {
        if (null == sContentType) {
            return null;
        }
        final int start = sContentType.indexOf('/') + 1;
        if (start <= 0) {
            return null;
        }
        final int pos = sContentType.indexOf(';', start);
        return pos < 0 ? sContentType.substring(start) : sContentType.substring(start, pos);
    }

    private static String tryGetStringContent(final MimePart mimePart) {
        try {
            final Object content = mimePart.getContent();
            if (content instanceof String) {
                return content.toString();
            }
            return null;
        } catch (final Exception e) {
            return null;
        }
    }

    // --------------------------------- Helper class -------------------------------------- //

    private static final class CleanUpMessageImpl extends MessageImpl implements CleanUp {

        private final MessageImpl message;
        private final StorageProvider tempStore;

        CleanUpMessageImpl(final MessageImpl message, final StorageProvider tempStore) {
            super();
            this.message = message;
            this.tempStore = tempStore;
        }

        @Override
        public void cleanUp() {
            final MessageImpl message = this.message;
            if (null != message) {
                try { message.dispose(); } catch (final Exception e) {/**/}
            }
            final StorageProvider tempStore = this.tempStore;
            if (null != tempStore) {
                try {
                    @SuppressWarnings("unchecked") final Set<File> filesToDelete = (Set<File>) filesToDelete().get(null);
                    synchronized (filesToDelete) {
                        for (final Iterator<File> iterator = filesToDelete.iterator(); iterator.hasNext();) {
                            final File file = iterator.next();
                            if (file.delete()) {
                                iterator.remove();
                            }
                        }
                    }
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }

        @Override
        public int hashCode() {
            return message.hashCode();
        }

        @Override
        public String getMessageId() {
            return message.getMessageId();
        }

        @Override
        public Entity getParent() {
            return message.getParent();
        }

        @Override
        public void setParent(final Entity parent) {
            message.setParent(parent);
        }

        @Override
        public void createMessageId(final String hostname) {
            message.createMessageId(hostname);
        }

        @Override
        public Header getHeader() {
            return message.getHeader();
        }

        @Override
        public void setHeader(final Header header) {
            message.setHeader(header);
        }

        @Override
        public Body getBody() {
            return message.getBody();
        }

        @Override
        public String getSubject() {
            return message.getSubject();
        }

        @Override
        public void setBody(final Body body) {
            message.setBody(body);
        }

        @Override
        public void setSubject(final String subject) {
            message.setSubject(subject);
        }

        @Override
        public boolean equals(final Object obj) {
            return message.equals(obj);
        }

        @Override
        public Body removeBody() {
            return message.removeBody();
        }

        @Override
        public void setMessage(final Message message) {
            ((AbstractEntity) message).setMessage(message);
        }

        @Override
        public Date getDate() {
            return message.getDate();
        }

        @Override
        public void setMultipart(final org.apache.james.mime4j.dom.Multipart multipart) {
            message.setMultipart(multipart);
        }

        @Override
        public void setDate(final Date date) {
            message.setDate(date);
        }

        @Override
        public void setDate(final Date date, final TimeZone zone) {
            message.setDate(date, zone);
        }

        @Override
        public void setMultipart(final org.apache.james.mime4j.dom.Multipart multipart, final Map<String, String> parameters) {
            message.setMultipart(multipart, parameters);
        }

        @Override
        public Mailbox getSender() {
            return message.getSender();
        }

        @Override
        public void setText(final TextBody textBody) {
            message.setText(textBody);
        }

        @Override
        public void setSender(final Mailbox sender) {
            message.setSender(sender);
        }

        @Override
        public MailboxList getFrom() {
            return message.getFrom();
        }

        @Override
        public void setText(final TextBody textBody, final String subtype) {
            message.setText(textBody, subtype);
        }

        @Override
        public void setFrom(final Mailbox from) {
            message.setFrom(from);
        }

        @Override
        public void setFrom(final Mailbox... from) {
            message.setFrom(from);
        }

        @Override
        public void setBody(final Body body, final String mimeType) {
            message.setBody(body, mimeType);
        }

        @Override
        public void setFrom(final Collection<Mailbox> from) {
            message.setFrom(from);
        }

        @Override
        public AddressList getTo() {
            return message.getTo();
        }

        @Override
        public void setBody(final Body body, final String mimeType, final Map<String, String> parameters) {
            message.setBody(body, mimeType, parameters);
        }

        @Override
        public void setTo(final Address to) {
            message.setTo(to);
        }

        @Override
        public void setTo(final Address... to) {
            message.setTo(to);
        }

        @Override
        public String getMimeType() {
            return message.getMimeType();
        }

        @Override
        public void setTo(final Collection<? extends Address> to) {
            message.setTo(to);
        }

        @Override
        public AddressList getCc() {
            return message.getCc();
        }

        @Override
        public String getCharset() {
            return message.getCharset();
        }

        @Override
        public String toString() {
            return message.toString();
        }

        @Override
        public void setCc(final Address cc) {
            message.setCc(cc);
        }

        @Override
        public String getContentTransferEncoding() {
            return message.getContentTransferEncoding();
        }

        @Override
        public void setCc(final Address... cc) {
            message.setCc(cc);
        }

        @Override
        public void setContentTransferEncoding(final String contentTransferEncoding) {
            message.setContentTransferEncoding(contentTransferEncoding);
        }

        @Override
        public void setCc(final Collection<? extends Address> cc) {
            message.setCc(cc);
        }

        @Override
        public String getDispositionType() {
            return message.getDispositionType();
        }

        @Override
        public AddressList getBcc() {
            return message.getBcc();
        }

        @Override
        public void setBcc(final Address bcc) {
            message.setBcc(bcc);
        }

        @Override
        public void setContentDisposition(final String dispositionType) {
            message.setContentDisposition(dispositionType);
        }

        @Override
        public void setBcc(final Address... bcc) {
            message.setBcc(bcc);
        }

        @Override
        public void setContentDisposition(final String dispositionType, final String filename) {
            message.setContentDisposition(dispositionType, filename);
        }

        @Override
        public void setBcc(final Collection<? extends Address> bcc) {
            message.setBcc(bcc);
        }

        @Override
        public AddressList getReplyTo() {
            return message.getReplyTo();
        }

        @Override
        public void setContentDisposition(final String dispositionType, final String filename, final long size) {
            message.setContentDisposition(dispositionType, filename, size);
        }

        @Override
        public void setReplyTo(final Address replyTo) {
            message.setReplyTo(replyTo);
        }

        @Override
        public void setReplyTo(final Address... replyTo) {
            message.setReplyTo(replyTo);
        }

        @Override
        public void setReplyTo(final Collection<? extends Address> replyTo) {
            message.setReplyTo(replyTo);
        }

        @Override
        public void setContentDisposition(final String dispositionType, final String filename, final long size, final Date creationDate, final Date modificationDate, final Date readDate) {
            message.setContentDisposition(dispositionType, filename, size, creationDate, modificationDate, readDate);
        }

        @Override
        public String getFilename() {
            return message.getFilename();
        }

        @Override
        public void setFilename(final String filename) {
            message.setFilename(filename);
        }

        @Override
        public boolean isMimeType(final String type) {
            return message.isMimeType(type);
        }

        @Override
        public boolean isMultipart() {
            return message.isMultipart();
        }

        @Override
        public void dispose() {
            message.dispose();
        }
    } // End of class CleanUpMessageImpl

}
