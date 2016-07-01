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

package com.openexchange.mail.mime.converters;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.checkNonAscii;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.getFileName;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.hasAttachments;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedFileInputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeConfig;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.mime.dataobjects.MimeMailPart;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.filler.CompositionParameters;
import com.openexchange.mail.mime.filler.ContextCompositionParameters;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.filler.SessionCompositionParameters;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.util.MessageRemovedIOException;

/**
 * {@link MimeMessageConverter} - Provides several methods to convert instances of {@link MimeMessage} to {@link MailMessage} in vice versa.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeMessageConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMessageConverter.class);

    private static final EnumSet<MailField> ENUM_SET_FULL = EnumSet.complementOf(EnumSet.of(
        MailField.BODY,
        MailField.FULL,
        MailField.ACCOUNT_NAME));

    private static final String CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;

    /**
     * {@link ExistenceChecker} - A checker to ensure existence of a certain field.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since Open-Xchange v6.16
     */
    private interface ExistenceChecker {

        /**
         * Checks existence of a certain field in given mails.
         *
         * @param mailMessages The mails to check
         */
        void check(MailMessage... mailMessages);

        /**
         * Checks existence of a certain field in given mails.
         *
         * @param mailMessages The mail to check
         */
        void check(Collection<MailMessage> mailMessages);
    }

    private static interface MailMessageFieldFiller {

        public static final String[] NON_MATCHING_HEADERS = {
            MessageHeaders.HDR_FROM, MessageHeaders.HDR_TO, MessageHeaders.HDR_CC, MessageHeaders.HDR_BCC, MessageHeaders.HDR_DISP_NOT_TO,
            MessageHeaders.HDR_REPLY_TO, MessageHeaders.HDR_SUBJECT, MessageHeaders.HDR_DATE, MessageHeaders.HDR_IMPORTANCE, MessageHeaders.HDR_X_PRIORITY,
            MessageHeaders.HDR_MESSAGE_ID, MessageHeaders.HDR_IN_REPLY_TO, MessageHeaders.HDR_REFERENCES, MessageHeaders.HDR_X_OX_VCARD,
            MessageHeaders.HDR_X_OX_NOTIFICATION };

        public static final String[] ALREADY_INSERTED_HEADERS = {
            MessageHeaders.HDR_MESSAGE_ID, MessageHeaders.HDR_IN_REPLY_TO, MessageHeaders.HDR_REFERENCES };

        public static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(MailMessageFieldFiller.class);

        /**
         * Fills a fields from source instance of {@link Message} in given destination instance of {@link MailMessage}.
         *
         * @param mailMessage The mail message to fill
         * @param msg The source message
         * @throws MessagingException If a messaging error occurs
         * @throws OXException If a mail related error occurs
         */
        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException;
    }

    private static final class HeaderFieldFiller implements MailMessageFieldFiller {

        private final String headerName;

        HeaderFieldFiller(final String headerName) {
            super();
            this.headerName = headerName;
        }

        @Override
        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
            final String[] header = msg.getHeader(headerName);
            if (null == header || 0 == header.length) {
                return;
            }
            for (final String value : header) {
                mailMessage.addHeader(headerName, checkNonAscii(value));
            }
        }

    }

    private static final String STR_EMPTY = "";

    /**
     * Prevent instantiation.
     */
    private MimeMessageConverter() {
        super();
    }

    /**
     * Creates a {@link Part} object from given instance of {@link MailPart}.
     *
     * @param mailPart The instance of {@link MailPart}
     * @return Appropriate instance of {@link Part}
     */
    public static Part convertMailPart(final MailPart mailPart) throws OXException {
        try {
            if (mailPart instanceof MailMessage) {
                return convertMailMessage((MailMessage) mailPart);
            }
            @SuppressWarnings("resource") ThresholdFileHolder sink = new ThresholdFileHolder();
            mailPart.writeTo(sink.asOutputStream());
            File tempFile = sink.getTempFile();
            if (null == tempFile) {
                return new MimeBodyPart(Streams.newByteArrayInputStream(sink.toByteArray()));
            }
            return new MimeBodyPart(new SharedFileInputStream(tempFile));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts given instances of {@link MailMessage} into JavaMail-conform {@link Message} objects.
     * <p>
     * <b>Note</b>: This is just a convenience method that invokes {@link #convertMailMessage(MailMessage)} for each instance of
     * {@link MailMessage}
     *
     * @param mails The source instances of {@link MailMessage}
     * @return JavaMail-conform {@link Message} objects.
     * @throws OXException If conversion fails
     */
    public static Message[] convertMailMessages(final MailMessage[] mails) throws OXException {
        return convertMailMessages(mails, true);
    }

    /**
     * Converts given instances of {@link MailMessage} into JavaMail-conform {@link Message} objects.
     * <p>
     * <b>Note</b>: This is just a convenience method that invokes {@link #convertMailMessage(MailMessage)} for each instance of
     * {@link MailMessage}
     *
     * @param mails The source instances of {@link MailMessage}
     * @param clone <code>true</code> to clone message source; otherwise <code>false</code> to return a reference if possible
     * @return JavaMail-conform {@link Message} objects.
     * @throws OXException If conversion fails
     */
    public static Message[] convertMailMessages(final MailMessage[] mails, final boolean clone) throws OXException {
        if (null == mails) {
            return null;
        }
        final Message[] retval = new Message[mails.length];
        for (int i = 0; i < retval.length; i++) {
            if (null != mails[i]) {
                retval[i] = convertMailMessage(mails[i], clone);
            }
        }
        return retval;
    }

    /**
     * Converts given instances of {@link MailMessage} into JavaMail-conform {@link Message} objects.
     * <p>
     * <b>Note</b>: This is just a convenience method that invokes {@link #convertMailMessage(MailMessage)} for each instance of
     * {@link MailMessage}
     *
     * @param mails The source instances of {@link MailMessage}
     * @param behavior Provides the behavior bits
     * @return JavaMail-conform {@link Message} objects.
     * @throws OXException If conversion fails
     * @see #BEHAVIOR_CLONE
     * @see #BEHAVIOR_STREAM2FILE
     */
    public static Message[] convertMailMessages(final MailMessage[] mails, final int behavior) throws OXException {
        if (null == mails) {
            return null;
        }
        final Message[] retval = new Message[mails.length];
        for (int i = 0; i < retval.length; i++) {
            if (null != mails[i]) {
                retval[i] = convertMailMessage(mails[i], behavior);
            }
        }
        return retval;
    }

    /**
     * Converts given instance of {@link MailMessage} into a JavaMail-conform {@link Message} object.
     *
     * @param mail The source instance of {@link MailMessage}
     * @return A JavaMail-conform {@link Message} object
     * @throws OXException If conversion fails
     */
    public static Message convertMailMessage(final MailMessage mail) throws OXException {
        return convertMailMessage(mail, true);
    }

    /**
     * Converts given instance of {@link MailMessage} into a JavaMail-conform {@link Message} object.
     *
     * @param mail The source instance of {@link MailMessage}
     * @param clone <code>true</code> to clone message source; otherwise <code>false</code> to return a reference if possible
     * @return A JavaMail-conform {@link Message} object
     * @throws OXException If conversion fails
     */
    public static Message convertMailMessage(final MailMessage mail, final boolean clone) throws OXException {
        return convertMailMessage(mail, clone ? BEHAVIOR_CLONE : 0);
    }

    /**
     * Indicates to clone passed mail.
     */
    public static final int BEHAVIOR_CLONE = 1;

    /**
     * Indicates to stream content to a (managed) file.
     */
    public static final int BEHAVIOR_STREAM2FILE = 1 << 1;

    private static final String X_ORIGINAL_HEADERS = "x-original-headers";

    /**
     * Converts given instance of {@link MailMessage} into a JavaMail-conform {@link Message} object.
     *
     * @param mail The source instance of {@link MailMessage}
     * @param behavior Provides the behavior bits
     * @return A JavaMail-conform {@link Message} object
     * @throws OXException If conversion fails
     * @see #BEHAVIOR_CLONE
     * @see #BEHAVIOR_STREAM2FILE
     */
    public static Message convertMailMessage(MailMessage mail, int behavior) throws OXException {
        if (mail instanceof ComposedMailMessage) {
            return convertComposedMailMessage((ComposedMailMessage) mail);
        }
        try {
            boolean clone = ((behavior & BEHAVIOR_CLONE) > 0);
            boolean stream2file = ((behavior & BEHAVIOR_STREAM2FILE) > 0);

            MimeMessage mimeMessage;
            if (!clone && (mail instanceof MimeMailMessage)) {
                mimeMessage = ((MimeMailMessage) mail).getMimeMessage();
            } else {
                ManagedFileManagement fileManagement;
                if (!stream2file || (null == (fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class)))) {
                    ThresholdFileHolder sink = null;
                    boolean closeSink = true;
                    try {
                        sink = new ThresholdFileHolder();
                        mail.writeTo(sink.asOutputStream());
                        File tempFile = sink.getTempFile();
                        if (null == tempFile) {
                            mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), sink.getStream());
                        } else {
                            mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile);
                        }
                        mimeMessage.removeHeader(X_ORIGINAL_HEADERS);
                        closeSink = false;
                    } finally {
                        if (closeSink && null != sink) {
                            sink.close();
                        }
                    }
                } else {
                    File file = checkForFile(mail);
                    boolean deleteOnError = false;
                    try {
                        if (null == file) {
                            deleteOnError = true;
                            File newTempFile = fileManagement.newTempFile();
                            writeToFile(mail, newTempFile);
                            file = newTempFile;
                        }
                        mimeMessage = new ManagedMimeMessage(MimeDefaultSession.getDefaultSession(), file, mail.getReceivedDateDirect());
                        mimeMessage.removeHeader(X_ORIGINAL_HEADERS);
                        deleteOnError = false;
                    } finally {
                        if (deleteOnError && null != file) {
                            file.delete();
                        }
                    }
                }
            }
            if (mail.containsFlags()) {
                parseMimeFlags(mail.getFlags(), mimeMessage);
            }
            Flags flags = null;
            if (mail.containsColorLabel()) {
                flags = new Flags();
                flags.add(MailMessage.getColorLabelStringValue(mail.getColorLabel()));
            }
            if (mail.containsUserFlags()) {
                if (null == flags) {
                    flags = new Flags();
                }
                final String[] userFlags = mail.getUserFlags();
                for (final String userFlag : userFlags) {
                    flags.add(userFlag);
                }
            }
            if (null != flags) {
                mimeMessage.setFlags(flags, true);
            }
            return mimeMessage;
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    private static void writeToFile(MailMessage mail, File tempFile) throws IOException, OXException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            mail.writeTo(out);
            out.flush();
        } finally {
            Streams.close(out);
        }

    }

    private static File checkForFile(final MailMessage mail) {
        if (mail instanceof MimeMailMessage) {
            final MimeMessage mimeMessage = ((MimeMailMessage) mail).getMimeMessage();
            if (mimeMessage instanceof ManagedMimeMessage) {
                return ((ManagedMimeMessage) mimeMessage).getFile();
            }
        }
        return null;
    }

    /**
     * Converts given instance of {@link ComposedMailMessage} into a JavaMail-conform {@link Message} object.
     *
     * @param composedMail The source instance of {@link ComposedMailMessage}
     * @return A JavaMail-conform {@link Message} object
     * @throws OXException If conversion fails
     */
    public static Message convertComposedMailMessage(final ComposedMailMessage composedMail) throws OXException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            /*
             * Fill message
             */
            CompositionParameters compositionParameters;
            if (composedMail.getSession() == null) {
                compositionParameters = new ContextCompositionParameters(composedMail.getContext());
            } else {
                UserSettingMail usm = null == composedMail.getMailSettings() ? UserSettingMailStorage.getInstance().getUserSettingMail(composedMail.getSession()) : composedMail.getMailSettings();
                compositionParameters = new SessionCompositionParameters(composedMail.getSession(), composedMail.getContext(), usm);
            }
            final MimeMessageFiller filler = new MimeMessageFiller(compositionParameters);
            filler.setAccountId(composedMail.getAccountId());
            composedMail.setFiller(filler);
            /*
             * Set headers
             */
            filler.setMessageHeaders(composedMail, mimeMessage);
            /*
             * Set common headers
             */
            filler.setCommonHeaders(mimeMessage);
            /*
             * Fill body
             */
            filler.fillMailBody(composedMail, mimeMessage, ComposeType.NEW);
            saveChanges(mimeMessage);
            return mimeMessage;
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Fills specified instance of {@link ComposedMailMessage} with {@link MimeMessageFiller}.
     *
     * @param composedMail The composed mail
     * @return A filled instance of {@link MailMessage} ready for further usage
     * @throws OXException If mail cannot be filled.
     */
    public static MailMessage fillComposedMailMessage(final ComposedMailMessage composedMail) throws OXException {
        try {
            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            /*
             * Fill message
             */
            MimeMessageFiller filler;
            {
                CompositionParameters compositionParameters;
                if (composedMail.getSession() == null) {
                    compositionParameters = new ContextCompositionParameters(composedMail.getContext());
                } else {
                    UserSettingMail usm = null == composedMail.getMailSettings() ? UserSettingMailStorage.getInstance().getUserSettingMail(composedMail.getSession()) : composedMail.getMailSettings();
                    compositionParameters = new SessionCompositionParameters(composedMail.getSession(), composedMail.getContext(), usm);
                }
                filler = new MimeMessageFiller(compositionParameters);
            }

            filler.setAccountId(composedMail.getAccountId());
            composedMail.setFiller(filler);
            /*
             * Set headers
             */
            filler.setMessageHeaders(composedMail, mimeMessage);
            /*
             * Set common headers
             */
            filler.setCommonHeaders(mimeMessage);
            /*
             * Fill body
             */
            filler.fillMailBody(composedMail, mimeMessage, ComposeType.NEW);
            saveChanges(mimeMessage);
            return convertMessage(mimeMessage);
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the multipart from passed message.
     *
     * @param message The message
     * @return The appropriate multipart
     * @throws OXException If content cannot be presented as a multipart
     */
    public static Multipart multipartFor(final MimeMessage message) throws OXException {
        return multipartFor(message, getContentType(message));
    }

    /**
     * Gets the multipart from passed message.
     *
     * @param message The message
     * @param contentType The message's Content-Type
     * @return The appropriate multipart
     * @throws OXException If content cannot be presented as a multipart
     */
    public static Multipart multipartFor(final MimeMessage message, final ContentType contentType) throws OXException {
        return multipartFor(message, contentType, true);
    }

    private static Multipart multipartFor(final MimeMessage message, final ContentType contentType, final boolean reparse) throws OXException {
        try {
            return multipartFor(message.getContent(), contentType);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final javax.mail.internet.ParseException e) {
            if (!reparse) {
                throw MimeMailException.handleMessagingException(e);
            }
            // Sanitize parameterized headers
            try {
                final String sContentType = message.getHeader(CONTENT_TYPE, null);
                message.setHeader(CONTENT_TYPE, new ContentType(sContentType).toString(true));
                MimeMessageConverter.saveChanges(message);
            } catch (final Exception x) {
                // Content-Type cannot be sanitized
                org.slf4j.LoggerFactory.getLogger(MimeFilter.class).debug("Content-Type cannot be sanitized.", x);
                throw MimeMailException.handleMessagingException(e);
            }
            return multipartFor(message, contentType, false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the Content-Type for given part.
     *
     * @param part The part
     * @return The parsed Content-Type
     * @throws OXException If parsing fails
     */
    public static ContentType getContentType(final Part part) throws OXException {
        try {
            final String[] tmp = part.getHeader(CONTENT_TYPE);
            return (tmp != null) && (tmp.length > 0) ? new ContentType(tmp[0]) : new ContentType(MimeTypes.MIME_DEFAULT);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the multipart from passed content object.
     *
     * @param content The content object
     * @param contentType The content type
     * @return The appropriate multipart
     * @throws OXException If content cannot be presented as a multipart
     */
    public static Multipart multipartFor(final Object content, final ContentType contentType) throws OXException {
        if (null == content) {
            return null;
        }
        if (content instanceof Multipart) {
            return (Multipart) content;
        }
        if (content instanceof InputStream) {
            try {
                return new MimeMultipart(new MessageDataSource((InputStream) content, contentType));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        if (content instanceof String) {
            try {
                return new MimeMultipart(new MessageDataSource(Streams.newByteArrayInputStream(((String) content).getBytes(Charsets.ISO_8859_1)), contentType));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        throw MailExceptionCode.MESSAGING_ERROR.create("Content is not of type javax.mail.Multipart, but " + content.getClass().getName());
    }

    /**
     * Performs {@link MimeMessage#saveChanges() saveChanges()} on specified message with sanitizing for a possibly corrupt/wrong Content-Type header.
     * <p>
     * Aligns <i>Message-Id</i> header to given host name.
     *
     * @param mimeMessage The MIME message
     * @param hostName The host name
     * @param keepMessageIdIfPresent Whether to keep a possibly available <i>Message-ID</i> header or to generate a new (unique) one
     * @throws OXException If operation fails
     */
    public static void saveChanges(MimeMessage mimeMessage, String hostName, boolean keepMessageIdIfPresent) throws OXException {
        MimeMessageUtility.saveChanges(mimeMessage, hostName, keepMessageIdIfPresent);
    }

    /**
     * Performs {@link MimeMessage#saveChanges() saveChanges()} on specified message with sanitizing for a possibly corrupt/wrong
     * Content-Type header.
     *
     * @param mimeMessage The message
     * @throws OXException If an error occurs
     */
    public static void saveChanges(MimeMessage mimeMessage) throws OXException {
        MimeMessageUtility.saveChanges(mimeMessage);
    }

    /**
     * Parses specified flags to given message.
     *
     * @param flags The flags to parse
     * @param msg The message to fill
     * @throws MessagingException If a messaging error occurs
     */
    public static void parseMimeFlags(final int flags, final Message msg) throws MessagingException {
        final Flags flagsObj = new Flags();
        if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
            flagsObj.add(Flags.Flag.ANSWERED);
        }
        if ((flags & MailMessage.FLAG_DELETED) > 0) {
            flagsObj.add(Flags.Flag.DELETED);
        }
        if ((flags & MailMessage.FLAG_DRAFT) > 0) {
            flagsObj.add(Flags.Flag.DRAFT);
        }
        if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
            flagsObj.add(Flags.Flag.FLAGGED);
        }
        if ((flags & MailMessage.FLAG_RECENT) > 0) {
            flagsObj.add(Flags.Flag.RECENT);
        }
        if ((flags & MailMessage.FLAG_SEEN) > 0) {
            flagsObj.add(Flags.Flag.SEEN);
        }
        if ((flags & MailMessage.FLAG_USER) > 0) {
            flagsObj.add(Flags.Flag.USER);
        }
        if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
            flagsObj.add(MailMessage.USER_FORWARDED);
        }
        if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
            flagsObj.add(MailMessage.USER_READ_ACK);
        }
        msg.setFlags(flagsObj, true);
    }

    /**
     * Converts given array of {@link Message} instances to an array of {@link MailMessage} instances. The single elements of the array are
     * expected to be instances of {@link ExtendedMimeMessage}; meaning the messages were created through a manual fetch.
     * <p>
     * Only the fields specified through parameter <code>fields</code> are going to be set
     *
     * @see #convertMessages(Message[], Folder, MailField[]) to convert common instances of {@link Message}
     * @param msgs The source messages
     * @param fields The fields to fill
     * @return The converted array of {@link Message} instances
     * @throws OXException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final MailField[] fields) throws OXException {
        return convertMessages(msgs, fields, null, false);
    }

    /**
     * Converts given array of {@link Message} instances to an array of {@link MailMessage} instances. The single elements of the array are
     * expected to be instances of {@link ExtendedMimeMessage}; meaning the messages were created through a manual fetch.
     * <p>
     * Only the fields specified through parameter <code>fields</code> are going to be set
     *
     * @see #convertMessages(Message[], Folder, MailField[])
     * @param msgs The source messages
     * @param fields The fields to fill
     * @param headerNames The header names
     * @param includeBody Whether to create mail messages with reference to content or not
     * @return The converted array of {@link Message} instances
     * @throws OXException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final MailField[] fields, final String[] headerNames, final boolean includeBody) throws OXException {
        try {
            final MailMessageFieldFiller[] fillers;
            {
                final List<MailMessageFieldFiller> tmp = new ArrayList<MailMessageFieldFiller>(Arrays.asList(createFieldFillers(fields)));
                if (null != headerNames) {
                    for (final String headerName : headerNames) {
                        tmp.add(new HeaderFieldFiller(headerName));
                    }
                }
                fillers = tmp.toArray(new MailMessageFieldFiller[tmp.size()]);
            }
            final MailMessage[] mails = new MimeMailMessage[msgs.length];
            for (int i = 0; i < mails.length; i++) {
                if (null != msgs[i]) {
                    /*
                     * Create with no reference to content
                     */
                    mails[i] = includeBody ? new MimeMailMessage((MimeMessage) msgs[i]) : new MimeMailMessage();
                    fillMessage(fillers, mails[i], msgs[i]);
                }
            }
            return mails;
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts given array of {@link Message} instances to an array of {@link MailMessage} instances.
     * <p>
     * Only the fields specified through parameter <code>fields</code> are going to be set
     *
     * @param msgs The source messages
     * @param folder The folder containing source messages
     * @param fields The fields to fill
     * @param includeBody <code>true</code> to include body; otherwise <code>false</code>
     * @return The converted array of {@link Message} instances
     * @throws OXException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final Folder folder, final MailField[] fields, final boolean includeBody) throws OXException {
        try {
            final MailMessageFieldFiller[] fillers = createFieldFillers(new DefaultFolderInfo(folder), fields);
            final MailMessage[] mails = new MimeMailMessage[msgs.length];
            for (int i = 0; i < mails.length; i++) {
                if (null != msgs[i]) {
                    /*
                     * Create with no reference to content
                     */
                    mails[i] = includeBody ? new MimeMailMessage((MimeMessage) msgs[i]) : new MimeMailMessage();
                    fillMessage(fillers, mails[i], msgs[i]);
                }
            }
            return mails;
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    private static void fillMessage(final MailMessageFieldFiller[] fillers, final MailMessage mailMessage, final Message msg) throws OXException, MessagingException {
        for (final MailMessageFieldFiller filler : fillers) {
            if (null != filler) {
                filler.fillField(mailMessage, msg);
            }
        }
    }

    private static final EnumMap<MailField, MailMessageFieldFiller> FILLER_MAP_EXT = new EnumMap<MailField, MailMessageFieldFiller>(
        MailField.class);

    private static final EnumMap<MailField, ExistenceChecker> CHECKER_MAP;

    static {
        final org.slf4j.Logger logger = LOG;

        CHECKER_MAP = new EnumMap<MailField, ExistenceChecker>(MailField.class);
        final InternetAddress empty = null;
        CHECKER_MAP.put(MailField.FROM, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsFrom()) {
                        mailMessage.addFrom(empty);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsFrom()) {
                        mailMessage.addFrom(empty);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.TO, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsTo()) {
                        mailMessage.addTo(empty);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsTo()) {
                        mailMessage.addTo(empty);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.CC, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsCc()) {
                        mailMessage.addCc(empty);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsCc()) {
                        mailMessage.addCc(empty);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.BCC, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsBcc()) {
                        mailMessage.addBcc(empty);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsBcc()) {
                        mailMessage.addBcc(empty);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.SUBJECT, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsSubject()) {
                        mailMessage.setSubject(null);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsSubject()) {
                        mailMessage.setSubject(null);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.SENT_DATE, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsSentDate()) {
                        mailMessage.setSentDate(null);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsSentDate()) {
                        mailMessage.setSentDate(null);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.DISPOSITION_NOTIFICATION_TO, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsDispositionNotification()) {
                        mailMessage.setDispositionNotification(null);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsDispositionNotification()) {
                        mailMessage.setDispositionNotification(null);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.PRIORITY, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsPriority()) {
                        mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsPriority()) {
                        mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                    }
                }
            }
        });
        CHECKER_MAP.put(MailField.THREAD_LEVEL, new ExistenceChecker() {

            @Override
            public void check(final MailMessage... mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsThreadLevel()) {
                        mailMessage.setThreadLevel(0);
                    }
                }
            }

            @Override
            public void check(final Collection<MailMessage> mailMessages) {
                for (final MailMessage mailMessage : mailMessages) {
                    if (null != mailMessage && !mailMessage.containsThreadLevel()) {
                        mailMessage.setThreadLevel(0);
                    }
                }
            }
        });

        FILLER_MAP_EXT.put(MailField.HEADERS, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                /*
                 * From
                 */
                mailMessage.addFrom(getAddressHeader(MessageHeaders.HDR_FROM, extMimeMessage));
                /*
                 * To, Cc, and Bcc
                 */
                mailMessage.addTo(getAddressHeader(MessageHeaders.HDR_TO, extMimeMessage));
                mailMessage.addCc(getAddressHeader(MessageHeaders.HDR_CC, extMimeMessage));
                mailMessage.addBcc(getAddressHeader(MessageHeaders.HDR_BCC, extMimeMessage));
                mailMessage.addReplyTo(getAddressHeader(MessageHeaders.HDR_REPLY_TO, extMimeMessage));
                /*
                 * Disposition-Notification-To
                 */
                {
                    final String dispNot = extMimeMessage.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null);
                    if (dispNot == null) {
                        mailMessage.setDispositionNotification(null);
                    } else {
                        final InternetAddress[] addresses = getAddressHeader(dispNot);
                        mailMessage.setDispositionNotification(null == addresses || 0 == addresses.length ? null : addresses[0]);
                    }
                }
                /*
                 * Subject
                 */
                mailMessage.setSubject(getSubject(msg));
                /*
                 * Date
                 */
                mailMessage.setSentDate(extMimeMessage.getSentDate());
                /*
                 * X-Priority
                 */
                mailMessage.setPriority(extMimeMessage.getPriority());
                /*
                 * Message-Id
                 */
                {
                    final String messageId = extMimeMessage.getHeader(MessageHeaders.HDR_MESSAGE_ID, null);
                    if (null != messageId) {
                        mailMessage.addHeader(MessageHeaders.HDR_MESSAGE_ID, messageId);
                    }
                }
                /*
                 * In-Reply-To
                 */
                final StringBuilder sb = new StringBuilder(128);
                {
                    final String[] inReplyTo = extMimeMessage.getHeader(MessageHeaders.HDR_IN_REPLY_TO);
                    if (null != inReplyTo) {
                        sb.append(inReplyTo[0]);
                        for (int j = 1; j < inReplyTo.length; j++) {
                            sb.append(", ").append(inReplyTo[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_IN_REPLY_TO, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * References
                 */
                {
                    final String[] references = extMimeMessage.getHeader(MessageHeaders.HDR_REFERENCES);
                    if (null != references) {
                        sb.append(references[0]);
                        for (int j = 1; j < references.length; j++) {
                            sb.append(", ").append(references[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REFERENCES, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * Add all as regular headers; except previously inserted headers
                 */
                for (final Enumeration<?> e = msg.getNonMatchingHeaders(ALREADY_INSERTED_HEADERS); e.hasMoreElements();) {
                    final Header h = (Header) e.nextElement();
                    try {
                        mailMessage.addHeader(h.getName(), h.getValue());
                    } catch (final Exception exc) {
                        logger.warn("", exc);
                    }
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.ID, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setMailId(Long.toString(((ExtendedMimeMessage) msg).getUid()));
            }
        });
        FILLER_MAP_EXT.put(MailField.FOLDER_ID, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setFolder(((ExtendedMimeMessage) msg).getFullname());
            }
        });

        {
            MailMessageFieldFiller filler = new MailMessageFieldFiller() {

                @Override
                public void fillField(final MailMessage mailMessage, final Message msg) throws OXException, MessagingException {
                    try {
                        mailMessage.setContentType(((ExtendedMimeMessage) msg).getContentType());
                    } catch (final OXException e) {
                        /*
                         * Cannot occur
                         */
                        LOG1.error("", e);
                    }
                    mailMessage.setHasAttachment(((ExtendedMimeMessage) msg).hasAttachment());
                }
            };
            FILLER_MAP_EXT.put(MailField.CONTENT_TYPE, filler);
            FILLER_MAP_EXT.put(MailField.MIME_TYPE, filler);
        }

        FILLER_MAP_EXT.put(MailField.FROM, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addFrom(getAddressHeader(MessageHeaders.HDR_FROM, msg));
            }
        });
        FILLER_MAP_EXT.put(MailField.TO, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addTo(getAddressHeader(MessageHeaders.HDR_TO, msg));
            }
        });
        FILLER_MAP_EXT.put(MailField.CC, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addCc(getAddressHeader(MessageHeaders.HDR_CC, msg));
            }
        });
        FILLER_MAP_EXT.put(MailField.BCC, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addBcc(getAddressHeader(MessageHeaders.HDR_BCC, msg));
            }
        });
        FILLER_MAP_EXT.put(MailField.SUBJECT, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSubject(getSubject(msg));
            }
        });
        FILLER_MAP_EXT.put(MailField.SIZE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSize(((ExtendedMimeMessage) msg).getSize());
            }
        });
        FILLER_MAP_EXT.put(MailField.SENT_DATE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSentDate(((ExtendedMimeMessage) msg).getSentDate());
            }
        });
        FILLER_MAP_EXT.put(MailField.RECEIVED_DATE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setReceivedDate(((ExtendedMimeMessage) msg).getReceivedDate());
            }
        });
        FILLER_MAP_EXT.put(MailField.FLAGS, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
                parseFlags(((ExtendedMimeMessage) msg).getFlags(), mailMessage);
            }
        });
        FILLER_MAP_EXT.put(MailField.THREAD_LEVEL, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setThreadLevel(((ExtendedMimeMessage) msg).getThreadLevel());
            }
        });
        FILLER_MAP_EXT.put(MailField.DISPOSITION_NOTIFICATION_TO, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = ((ExtendedMimeMessage) msg).getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                if ((val != null) && (val.length > 0)) {
                    final String dispNot = val[0];
                    if (dispNot == null) {
                        mailMessage.setDispositionNotification(null);
                    } else {
                        final InternetAddress[] addresses = getAddressHeader(dispNot);
                        mailMessage.setDispositionNotification(null == addresses || 0 == addresses.length ? null : addresses[0]);
                    }
                } else {
                    mailMessage.setDispositionNotification(null);
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.PRIORITY, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                String[] val = ((ExtendedMimeMessage) msg).getHeader(MessageHeaders.HDR_IMPORTANCE);
                if (val != null && (val.length > 0)) {
                    parseImportance(val[0], mailMessage);
                } else {
                    val = ((ExtendedMimeMessage) msg).getHeader(MessageHeaders.HDR_X_PRIORITY);
                    if ((val != null) && (val.length > 0)) {
                        parsePriority(val[0], mailMessage);
                    } else {
                        mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                    }
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.COLOR_LABEL, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
                parseFlags(((ExtendedMimeMessage) msg).getFlags(), mailMessage);
                if (!mailMessage.containsColorLabel()) {
                    mailMessage.setColorLabel(MailMessage.COLOR_LABEL_NONE);
                }
            }
        });
    }

    /**
     * Creates the field fillers and expects the messages to be instances of {@link ExtendedMimeMessage}.
     *
     * @param fields The fields to fill
     * @return An array of appropriate {@link MailMessageFieldFiller} implementations
     * @throws OXException If field fillers cannot be created
     */
    private static MailMessageFieldFiller[] createFieldFillers(final MailField[] fields) throws OXException {
        final MailField[] arr;
        {
            final List<MailField> list = Arrays.asList(fields);
            final EnumSet<MailField> fieldSet = list.isEmpty() ? EnumSet.noneOf(MailField.class) : EnumSet.copyOf(list);
            if (fieldSet.contains(MailField.FULL)) {
                arr = ENUM_SET_FULL.toArray(new MailField[ENUM_SET_FULL.size()]);
            } else {
                arr = fields;
            }
        }
        final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final MailField field = arr[i];
            final MailMessageFieldFiller filler = FILLER_MAP_EXT.get(field);
            if (filler == null) {
                if (MailField.BODY.equals(field) || MailField.FULL.equals(field) || MailField.ACCOUNT_NAME.equals(field)) {
                    LOG.debug("Ignoring mail field {}", field);
                    fillers[i] = null;
                } else {
                    throw MailExceptionCode.INVALID_FIELD.create(field.toString());
                }
            } else {
                fillers[i] = filler;
            }
        }
        return fillers;
    }

    private static final EnumMap<MailField, MailMessageFieldFiller> FILLER_MAP = new EnumMap<MailField, MailMessageFieldFiller>(
        MailField.class);

    static {
        final org.slf4j.Logger logger = LOG;
        FILLER_MAP.put(MailField.HEADERS, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                /*
                 * From
                 */
                mailMessage.addFrom(getAddressHeader(MessageHeaders.HDR_FROM, msg));
                /*
                 * To, Cc, and Bcc
                 */
                mailMessage.addTo(getAddressHeader(MessageHeaders.HDR_TO, msg));
                mailMessage.addCc(getAddressHeader(MessageHeaders.HDR_CC, msg));
                mailMessage.addBcc(getAddressHeader(MessageHeaders.HDR_BCC, msg));
                mailMessage.addReplyTo(getAddressHeader(MessageHeaders.HDR_REPLY_TO, msg));
                /*
                 * Disposition-Notification-To
                 */
                {
                    final String[] addrStr = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                    if (null != addrStr && addrStr.length > 0) {
                        final String dispNot = addrStr[0];
                        if (dispNot == null) {
                            mailMessage.setDispositionNotification(null);
                        } else {
                            final InternetAddress[] addresses = getAddressHeader(dispNot);
                            mailMessage.setDispositionNotification(null == addresses || 0 == addresses.length ? null : addresses[0]);
                        }
                    } else {
                        mailMessage.setDispositionNotification(null);
                    }
                }
                /*
                 * Subject
                 */
                mailMessage.setSubject(getSubject(msg));
                /*
                 * Date
                 */
                mailMessage.setSentDate(msg.getSentDate());
                /*
                 * Importance
                 */
                {
                    final String[] importance = msg.getHeader(MessageHeaders.HDR_IMPORTANCE);
                    if (null != importance) {
                        parseImportance(importance[0], mailMessage);
                    }
                }
                /*
                 * X-Priority
                 */
                if (!mailMessage.containsPriority()) {
                    final String[] xPriority = msg.getHeader(MessageHeaders.HDR_X_PRIORITY);
                    if (null == xPriority) {
                        mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                    } else {
                        parsePriority(xPriority[0], mailMessage);
                    }
                }
                /*
                 * Message-Id
                 */
                {
                    final String[] messageId = msg.getHeader(MessageHeaders.HDR_MESSAGE_ID);
                    if (null != messageId) {
                        mailMessage.addHeader(MessageHeaders.HDR_MESSAGE_ID, messageId[0]);
                    }
                }
                /*
                 * In-Reply-To
                 */
                final StringBuilder sb = new StringBuilder(128);
                {
                    final String[] inReplyTo = msg.getHeader(MessageHeaders.HDR_IN_REPLY_TO);
                    if (null != inReplyTo) {
                        sb.append(inReplyTo[0]);
                        for (int j = 1; j < inReplyTo.length; j++) {
                            sb.append(", ").append(inReplyTo[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_IN_REPLY_TO, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * References
                 */
                {
                    final String[] references = msg.getHeader(MessageHeaders.HDR_REFERENCES);
                    if (null != references) {
                        sb.append(references[0]);
                        for (int j = 1; j < references.length; j++) {
                            sb.append(", ").append(references[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REFERENCES, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * Add all as regular headers; except previously inserted headers
                 */
                for (final Enumeration<?> e = msg.getNonMatchingHeaders(ALREADY_INSERTED_HEADERS); e.hasMoreElements();) {
                    final Header h = (Header) e.nextElement();
                    try {
                        mailMessage.addHeader(h.getName(), h.getValue());
                    } catch (final Exception exc) {
                        logger.warn("", exc);
                    }
                }
            }
        });

        {
            MailMessageFieldFiller filler = new MailMessageFieldFiller() {

                private final String multipart = "multipart";

                private final String mixed = "mixed";

                @Override
                public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
                    ContentType ct = null;
                    try {
                        final String[] tmp = msg.getHeader(CONTENT_TYPE);
                        if (tmp != null && tmp.length > 0) {
                            ct = new ContentType(tmp[0]);
                        } else {
                            ct = new ContentType(MimeTypes.MIME_DEFAULT);
                        }
                    } catch (final OXException e) {
                        /*
                         * Cannot occur
                         */
                        LOG1.error(MessageFormat.format("Invalid content type: {0}", msg.getContentType()), e);
                        try {
                            ct = new ContentType(MimeTypes.MIME_DEFAULT);
                        } catch (final OXException e1) {
                            /*
                             * Cannot occur
                             */
                            LOG1.error("", e1);
                            return;
                        }
                    }
                    mailMessage.setContentType(ct);
                    if (msg instanceof ExtendedMimeMessage) {
                        mailMessage.setHasAttachment(((ExtendedMimeMessage) msg).hasAttachment());
                    } else {
                        try {
                            mailMessage.setHasAttachment(ct.startsWith(multipart) && (mixed.equalsIgnoreCase(ct.getSubType()) || hasAttachments(
                                (Multipart) msg.getContent(),
                                ct.getSubType())));
                        } catch (final ClassCastException e) {
                            // Cast to javax.mail.Multipart failed
                            LOG1.debug(new StringBuilder(256).append(
                                "Message's Content-Type indicates to be multipart/* but its content is not an instance of javax.mail.Multipart but ").append(
                                e.getMessage()).append(
                                ".\nIn case if IMAP it is due to a wrong BODYSTRUCTURE returned by IMAP server.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.").toString());
                            mailMessage.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
                        } catch (final MessagingException e) {
                            // A messaging error occurred
                            LOG1.debug(new StringBuilder(256).append(
                                "Parsing message's multipart/* content to check for file attachments caused a messaging error: ").append(
                                e.getMessage()).append(
                                ".\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.").toString());
                            mailMessage.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
                        } catch (final IOException e) {
                            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                            }
                            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                        }
                    }
                }
            };
            FILLER_MAP.put(MailField.CONTENT_TYPE, filler);
            FILLER_MAP.put(MailField.MIME_TYPE, filler);
        }


        FILLER_MAP.put(MailField.FROM, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addFrom(getAddressHeader(MessageHeaders.HDR_FROM, msg));
            }
        });
        FILLER_MAP.put(MailField.TO, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addTo(getAddressHeader(MessageHeaders.HDR_TO, msg));
            }
        });
        FILLER_MAP.put(MailField.CC, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addCc(getAddressHeader(MessageHeaders.HDR_CC, msg));
            }
        });
        FILLER_MAP.put(MailField.BCC, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.addBcc(getAddressHeader(MessageHeaders.HDR_BCC, msg));
            }
        });
        FILLER_MAP.put(MailField.SUBJECT, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSubject(getSubject(msg));
            }
        });
        FILLER_MAP.put(MailField.SIZE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSize(msg.getSize());
            }
        });
        FILLER_MAP.put(MailField.SENT_DATE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSentDate(msg.getSentDate());
            }
        });
        FILLER_MAP.put(MailField.RECEIVED_DATE, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setReceivedDate(msg.getReceivedDate());
            }
        });
        FILLER_MAP.put(MailField.FLAGS, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
                parseFlags(msg.getFlags(), mailMessage);
            }
        });
        FILLER_MAP.put(MailField.THREAD_LEVEL, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                /*
                 * TODO: Thread level
                 */
                mailMessage.setThreadLevel(0);
            }
        });
        FILLER_MAP.put(MailField.DISPOSITION_NOTIFICATION_TO, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                if ((val != null) && (val.length > 0)) {
                    final String dispNot = val[0];
                    if (dispNot == null) {
                        mailMessage.setDispositionNotification(null);
                    } else {
                        final InternetAddress[] addresses = getAddressHeader(dispNot);
                        mailMessage.setDispositionNotification(null == addresses || 0 == addresses.length ? null : addresses[0]);
                    }
                } else {
                    mailMessage.setDispositionNotification(null);
                }
            }
        });
        FILLER_MAP.put(MailField.PRIORITY, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                if (msg instanceof ExtendedMimeMessage) {
                    ExtendedMimeMessage extended = (ExtendedMimeMessage) msg;
                    String[] val = extended.getHeader(MessageHeaders.HDR_IMPORTANCE);
                    if (val != null && (val.length > 0)) {
                        parseImportance(val[0], mailMessage);
                    } else {
                        val = extended.getHeader(MessageHeaders.HDR_X_PRIORITY);
                        if ((val != null) && (val.length > 0)) {
                            parsePriority(val[0], mailMessage);
                        } else {
                            mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                        }
                    }
                }
            }
        });
        FILLER_MAP.put(MailField.COLOR_LABEL, new MailMessageFieldFiller() {

            @Override
            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, OXException {
                parseFlags(msg.getFlags(), mailMessage);
                if (!mailMessage.containsColorLabel()) {
                    mailMessage.setColorLabel(MailMessage.COLOR_LABEL_NONE);
                }
            }
        });
    }

    /**
     * Checks field existence.
     *
     * @param mails The mails to checks
     * @param fields The field to check for
     */
    public static void checkFieldExistence(final MailMessage[] mails, final MailField[] fields) {
        if (null == mails) {
            return;
        }
        for (final MailField field : fields) {
            final ExistenceChecker checker = CHECKER_MAP.get(field);
            if (null != checker) {
                checker.check(mails);
            }
        }
    }

    private static interface FolderInfo {

        /**
         * Gets the full name of the mail folder.
         * @return The full name
         */
        String getFullName();

        /**
         * Gets the UID of the given message within the
         * according folder.
         *
         * @param msg The message
         * @return The UID
         * @throws MessagingException
         */
        String getUid(Message msg) throws MessagingException;

    }

    private static final class DefaultFolderInfo implements FolderInfo {

        private final Folder folder;

        public DefaultFolderInfo(Folder folder) {
            super();
            this.folder = folder;
        }

        @Override
        public String getFullName() {
            return folder.getFullName();
        }

        @Override
        public String getUid(Message msg) throws MessagingException {
            if (folder instanceof UIDFolder) {
                return Long.toString(((UIDFolder) folder).getUID(msg));
            } else if (folder instanceof POP3Folder) {
              return ((POP3Folder) folder).getUID(msg);
            }

            return null;
        }

    }

    private static final class StaticFolderInfo implements FolderInfo {

        private final String fullName;

        private final String uid;

        /**
         * @param fullName Full name of the mails folder
         * @param uid UID of the mail within the given folder
         */
        public StaticFolderInfo(String fullName, String uid) {
            super();
            this.fullName = fullName;
            this.uid = uid;
        }

        @Override
        public String getFullName() {
            return fullName;
        }

        @Override
        public String getUid(Message msg) {
            return uid;
        }

    }

    /**
     * Creates the field fillers and expects the messages to be common instances of {@link Message}.
     *
     * @param folderInfo The folder info
     * @param fields The fields to fill
     * @return An array of appropriate {@link MailMessageFieldFiller} implementations
     * @throws OXException If field fillers cannot be created
     */
    private static MailMessageFieldFiller[] createFieldFillers(final FolderInfo folderInfo, final MailField[] fields) throws OXException {
        final MailField[] arr;
        {
            final List<MailField> list = Arrays.asList(fields);
            final EnumSet<MailField> fieldSet = list.isEmpty() ? EnumSet.noneOf(MailField.class) : EnumSet.copyOf(list);
            if (fieldSet.contains(MailField.FULL)) {
                arr = ENUM_SET_FULL.toArray(new MailField[ENUM_SET_FULL.size()]);
            } else {
                arr = fields;
            }
        }
        final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final MailField field = arr[i];
            final MailMessageFieldFiller filler = FILLER_MAP.get(field);
            if (filler == null) {
                if (MailField.ID.equals(field)) {
                    fillers[i] = new MailMessageFieldFiller() {
                        @Override
                        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                            mailMessage.setMailId(folderInfo.getUid(msg));
                        }
                    };
                } else if (MailField.FOLDER_ID.equals(field)) {
                    fillers[i] = new MailMessageFieldFiller() {
                        @Override
                        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                            mailMessage.setFolder(folderInfo.getFullName());
                        }
                    };
                } else if (MailField.BODY.equals(field) || MailField.FULL.equals(field) || MailField.ACCOUNT_NAME.equals(field)) {
                    LOG.debug("Ignoring mail field {}", field);
                    fillers[i] = null;
                } else {
                    throw MailExceptionCode.INVALID_FIELD.create(field.toString());
                }
            } else {
                fillers[i] = filler;
            }
        }
        return fillers;
    }

    /**
     * Returns a new instance of {@link MailMessage} ready to get filled with header and/or flag information, but not capable to reference
     * to body content.
     *
     * @return A new instance of {@link MailMessage}
     */
    public static MailMessage newMailMessage() {
        return new MimeMailMessage();
    }

    /**
     * Creates a message data object from given message bytes conform to RFC822.
     *
     * @param in The message input stream conform to RFC822
     * @return An instance of <code>{@link MailMessage}</code>
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final InputStream in) throws OXException {
        try {
            return convertMessage(new MimeMessage(MimeDefaultSession.getDefaultSession(), in));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Creates a message data object from given message bytes conform to RFC822.
     *
     * @param asciiBytes The message bytes conform to RFC822
     * @return An instance of <code>{@link MailMessage}</code>
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final byte[] asciiBytes) throws OXException {
        try {
            return convertMessage(new MimeMessage(
                MimeDefaultSession.getDefaultSession(),
                new UnsynchronizedByteArrayInputStream(asciiBytes)));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static final String MULTI_PRIMTYPE = "multipart/";

    private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    private static final String MULTI_SUBTYPE_ALTERNATIVE = "ALTERNATIVE";

    /**
     * Creates a message data object from given MIME message.
     *
     * @param msg The MIME message
     * @return An instance of <code>{@link MailMessage}</code> containing the attributes from given MIME message
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final MimeMessage msg) throws OXException {
        return convertMessage(msg, true);
    }

    /**
     * Creates a message data object from given MIME message.
     *
     * @param msg The MIME message
     * @param considerFolder <code>true</code> to consider MIME message's folder (see {@link Message#getFolder()}); otherwise <code>false</code>
     * @return An instance of <code>{@link MailMessage}</code> containing the attributes from given MIME message
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final MimeMessage msg, final boolean considerFolder) throws OXException {
        /*
         * Create with reference to content
         */
        final MimeMailMessage mail = new MimeMailMessage(msg);
        try {
            /*
             * Parse flags
             */
            parseFlags(msg.getFlags(), mail);
            if (!mail.containsColorLabel()) {
                mail.setColorLabel(MailMessage.COLOR_LABEL_NONE);
            }
            /*
             * Set folder data
             */
            if (considerFolder) {
                final Folder f = msg.getFolder();
                if (f != null) {
                    /*
                     * No nested message
                     */
                    mail.setFolder(f.getFullName());
                    final boolean openFolder = !f.isOpen();
                    if (openFolder) {
                        f.open(Folder.READ_ONLY);
                    }
                    try {
                        if (f instanceof UIDFolder) {
                            mail.setMailId(Long.toString(((UIDFolder) f).getUID(msg)));
                        } else if (f instanceof POP3Folder) {
                            mail.setMailId(((POP3Folder) f).getUID(msg));
                        }
                    } finally {
                        if (openFolder) {
                            f.close(false);
                        }
                    }
                    mail.setUnreadMessages(f.getUnreadMessageCount());
                    // mail.setRecentCount(f.getNewMessageCount());
                }
            }
            /*
             * Check for special items
             */
            if (msg instanceof IMAPMessage) {
                IMAPMessage imapMessage = (IMAPMessage) msg;
                Long origUid = (Long) imapMessage.getItem("X-REAL-UID");
                if (null != origUid) {
                    mail.setOriginalId(origUid.toString());
                }
                String origFolder = (String) imapMessage.getItem("X-MAILBOX");
                if (null != origFolder) {
                    mail.setOriginalFolder(origFolder);
                }
            }
            /*
             * Set headers
             */
            setHeaders(msg, mail);
            /*
             * From
             */
            mail.addFrom(getAddressHeader(MessageHeaders.HDR_FROM, mail));
            /*
             * To, Cc, and Bcc
             */
            mail.addTo(getAddressHeader(MessageHeaders.HDR_TO, mail));
            mail.addCc(getAddressHeader(MessageHeaders.HDR_CC, mail));
            mail.addBcc(getAddressHeader(MessageHeaders.HDR_BCC, mail));
            mail.addReplyTo(getAddressHeader(MessageHeaders.HDR_REPLY_TO, mail));
            {
                final String[] tmp = mail.getHeader(CONTENT_TYPE);
                if ((tmp != null) && (tmp.length > 0)) {
                    mail.setContentType(MimeMessageUtility.decodeMultiEncodedHeader(tmp[0]));
                } else {
                    mail.setContentType(MimeTypes.MIME_DEFAULT);
                }
            }
           {
                ContentType ct = mail.getContentType();
                if (ct.startsWith(MULTI_PRIMTYPE)) {
                    if (MULTI_SUBTYPE_MIXED.equalsIgnoreCase(ct.getSubType())) {
                        // For convenience consider multipart/mixed to hold file attachments
                        mail.setHasAttachment(true);
                    } else if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(ct.getSubType())) {
                        // For convenience consider multipart/alternative to hold file attachments if it has more than 2 sub-parts
                        if (mail.getEnclosedCount() > 2) {
                            mail.setHasAttachment(true);
                        } else {
                            examineAttachmentPresence(mail, ct);
                        }
                    } else {
                        // Examine...
                        examineAttachmentPresence(mail, ct);
                    }
                } else {
                    // No multipart/* at all
                    mail.setHasAttachment(false);
                }
            }
            {
                final String[] tmp = mail.getHeader(MessageHeaders.HDR_CONTENT_ID);
                if ((tmp != null) && (tmp.length > 0)) {
                    mail.setContentId(tmp[0]);
                } else {
                    mail.setContentId(null);
                }
            }
            {
                final String tmp = mail.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
                if ((tmp != null) && (tmp.length() > 0)) {
                    mail.setContentDisposition(MimeMessageUtility.decodeMultiEncodedHeader(tmp));
                } else {
                    mail.setContentDisposition((String) null);
                }
            }
            {
                final String dispNot = mail.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null);
                if (dispNot == null) {
                    mail.setDispositionNotification(null);
                } else {
                    final InternetAddress[] addresses = getAddressHeader(dispNot);
                    mail.setDispositionNotification(null == addresses || 0 == addresses.length ? null : addresses[0]);
                }
            }
            {
                final String msgrefStr = mail.getHeader(MessageHeaders.HDR_X_OXMSGREF, null);
                if (msgrefStr == null) {
                    mail.setMsgref(null);
                } else {
                    mail.setMsgref(new MailPath(msgrefStr));
                    mail.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    try {
                        msg.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    } catch (final Exception e) {
                        // Ignore...
                        LOG.debug("", e);
                    }
                }
            }
            mail.setFileName(getFileName(mail));
            final String importance = mail.getFirstHeader(MessageHeaders.HDR_IMPORTANCE);
            if (null != importance) {
                parseImportance(importance, mail);
            } else {
                parsePriority(mail.getFirstHeader(MessageHeaders.HDR_X_PRIORITY), mail);
            }
            /*
             * Received date aka INTERNALDATE
             */
            {
                final Date receivedDate = msg.getReceivedDate();
                if (receivedDate == null) {
                    /*
                     * Check for "Received" header
                     */
                    /*
                     * TODO: Grab first or determine latest available date through iterating headers?
                     */
                    /*-
                     * final String[] receivedHdrs = msg.getHeader(MessageHeaders.HDR_RECEIVED);
                     * if (null != receivedHdrs) {
                     *     long lastReceived = Long.MIN_VALUE;
                     *     for (int i = 0; i &lt; receivedHdrs.length; i++) {
                     *         final String hdr = unfold(receivedHdrs[i]);
                     *         int pos;
                     *         if (hdr != null &amp;&amp; (pos = hdr.lastIndexOf(';')) != -1) {
                     *             try {
                     *                 lastReceived = Math.max(lastReceived, com.openexchange.mail.utils.DateUtils.getDateRFC822(
                     *                         hdr.substring(pos + 1).trim()).getTime());
                     *             } catch (final IllegalArgumentException e) {
                     *                 continue;
                     *             }
                     *         }
                     *     }
                     *     mail.setReceivedDate(new java.util.Date(lastReceived));
                     * } else {
                     *     mail.setReceivedDate(null);
                     * }
                     */
                    mail.setReceivedDate(null);
                } else {
                    mail.setReceivedDate(receivedDate);
                }
            }
            mail.setSentDate(getSentDate(mail));
            try {
                mail.setSize(msg.getSize());
            } catch (final Exception e) {
                // Size unavailable
                LOG.debug("Message's size could not be obtained.", e);
                mail.setSize(-1);
            }
            /*-
             * Fetch subject from mail headers since JavaMail fails to return a
             * possibly empty subject and then returns the next header line as
             * subject:<br>
             *
             * <pre>
             * To: someone@somewhere.com
             * Subject:
             * Date: Thu, 18 Sep 1997 10:49:08 +0200
             * </pre>
             */
            mail.setSubject(getSubject(mail));
            mail.setThreadLevel(0);
            return mail;
        } catch (final MessageRemovedException e) {
            final String[] sa = getFolderAndIdSafe(msg);
            final String folder = null == sa ? null : sa[0];
            final String mailId = null == sa ? null : sa[1];
            if (null != folder && null != mailId) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(e, mailId, folder);
            }
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static void examineAttachmentPresence(MimeMailMessage mail, ContentType ct) throws IOException, OXException {
        try {
            mail.setHasAttachment(hasAttachments(mail, ct.getSubType()));
        } catch (OXException e) {
            if (!MailExceptionCode.MESSAGING_ERROR.equals(e)) {
                throw e;
            }
            // A messaging error occurred
            LOG.debug("Parsing message's multipart/* content to check for file attachments caused a messaging error.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e);
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        } catch (ClassCastException e) {
            // Cast to javax.mail.Multipart failed
            LOG.debug("Message's Content-Type indicates to be multipart/* but its content is not an instance of javax.mail.Multipart but is not.\nIn case if IMAP it is due to a wrong BODYSTRUCTURE returned by IMAP server.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e);
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        } catch (MessagingException e) {
            // A messaging error occurred
            LOG.debug("Parsing message's multipart/* content to check for file attachments caused a messaging error: {}.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e.getMessage());
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        }
    }

    private static String[] getFolderAndIdSafe(final MimeMessage msg) {
        try {
            final Folder f = msg.getFolder();
            if (f != null) {
                final String[] ret = new String[2];
                ret[0] = f.getFullName();
                final boolean openFolder = !f.isOpen();
                if (openFolder) {
                    f.open(Folder.READ_ONLY);
                }
                try {
                    if (f instanceof UIDFolder) {
                        ret[1] = Long.toString(((UIDFolder) f).getUID(msg));
                    } else if (f instanceof POP3Folder) {
                        ret[1] = ((POP3Folder) f).getUID(msg);
                    }
                } finally {
                    if (openFolder) {
                        f.close(false);
                    }
                }
            }
        } catch (final Exception e) {
            // Ignore
        }
        return null;
    }

    /**
     * Creates a message data object from given <code>message/rfc822</code> content filled with desired fields.
     *
     * @param asciiBytes The <code>message/rfc822</code> content
     * @param uid The UID or <code>-1</code>
     * @param fullname The folder's fullname
     * @param separator The folder's separator
     * @param fields The desired fields to fill
     * @return An instance of {@link MailMessage} filled with desired fields
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final byte[] asciiBytes, final String uid, final String fullname, final char separator, final MailField[] fields) throws OXException {
        try {
            return convertMessage(new MimeMessage(
                MimeDefaultSession.getDefaultSession(),
                new UnsynchronizedByteArrayInputStream(asciiBytes)), uid, fullname, separator, fields);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Creates a message data object from given MIME message filled with desired fields.
     *
     * @param msg The MIME message
     * @param uid The UID or <code>null</code>
     * @param fullname The folder fullname
     * @param separator The folder separator character
     * @param fields The desired fields to fill
     * @return An instance of {@link MailMessage} filled with desired fields
     * @throws OXException If conversion fails
     */
    public static MailMessage convertMessage(final MimeMessage msg, final String uid, final String fullname, final char separator, final MailField[] fields) throws OXException {
        final MailFields set = new MailFields(fields);
        if (set.contains(MailField.FULL)) {
            final MailMessage mail = convertMessage(msg);
            mail.setMailId(uid);
            mail.setFolder(fullname);
            return mail;
        }
        try {
            final MailMessageFieldFiller[] fillers = createFieldFillers(new StaticFolderInfo(fullname, uid), fields);
            final MailMessage mail = (set.contains(MailField.BODY)) ? new MimeMailMessage(msg) : new MimeMailMessage();
            fillMessage(fillers, mail, msg);
            return mail;
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Creates a MIME mail part object from given raw bytes.
     *
     * @param asciiBytes The raw bytes
     * @return A MIME mail part object
     * @throws OXException If creating MIME mail part object fails
     */
    public static MailPart convertPart(final byte[] asciiBytes) throws OXException {
        try {
            return convertPart(new MimeBodyPart(new UnsynchronizedByteArrayInputStream(asciiBytes)), false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * "base64"
     */
    private static final String ENC_BASE64 = "base64";

    /**
     * Creates a MIME mail part object from given MIME part.
     *
     * @param part The part
     * @return an instance of <code>{@link MailPart}</code> containing the attributes from given part
     */
    public static MailPart convertPart(final Part part) throws OXException {
        return convertPart(part, true);
    }

    /**
     * Creates a MIME mail part object from given MIME part.
     *
     * @param part The part
     * @param enforeSize <code>true</code> to ensure size is set in returned mail part; otherwise <code>false</code>. If set given part's
     *            input stream is examined which might unnecessarily load data from backend.
     * @return an instance of <code>{@link MailPart}</code> containing the attributes from given part
     */
    public static MailPart convertPart(final Part part, final boolean enforeSize) throws OXException {
        try {
            /*
             * Create with reference to content
             */
            final MailPart mailPart = new MimeMailPart(part);
            /*
             * Set all cacheable data
             */
            setHeaders(part, mailPart);
            {
                final String[] contentTypeHdr = mailPart.getHeader(CONTENT_TYPE);
                if (null != contentTypeHdr && contentTypeHdr.length > 0) {
                    mailPart.setContentType(MimeMessageUtility.decodeMultiEncodedHeader(contentTypeHdr[0]));
                } else {
                    String sct = part.getContentType();
                    if (!Strings.isEmpty(sct)) {
                        mailPart.setContentType(MimeMessageUtility.decodeMultiEncodedHeader(sct));
                    }
                }
            }
            {
                final String[] tmp = mailPart.getHeader(MessageHeaders.HDR_CONTENT_ID);
                if ((tmp != null) && (tmp.length > 0)) {
                    mailPart.setContentId(tmp[0]);
                }
            }
            {
                final String[] tmp = mailPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
                if ((tmp != null) && (tmp.length > 0)) {
                    mailPart.setContentDisposition(MimeMessageUtility.decodeMultiEncodedHeader(tmp[0]));
                }
            }
            {
                final String[] msgrefStr = mailPart.getHeader(MessageHeaders.HDR_X_OXMSGREF);
                if (msgrefStr != null && msgrefStr.length > 0) {
                    mailPart.setMsgref(new MailPath(msgrefStr[0]));
                    mailPart.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    try {
                        part.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    } catch (final Exception e) {
                        // Ignore...
                        LOG.debug("", e);
                    }
                } else {
                    mailPart.setMsgref(null);
                }
            }
            mailPart.setFileName(getFileName(mailPart));
            int size;
            try {
                size = part.getSize();
            } catch (final Exception e) {
                // Ignore
                size = -1;
            }
            if (size == -1 && enforeSize) {
                /*
                 * Estimate unknown size: The encoded form of the file is expanded by 37% for UU encoding and by 35% for base64 encoding (3
                 * bytes become 4 plus control information).
                 */
                final String tansferEnc = mailPart.getHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, null);
                try {
                    size = estimateSize(part.getInputStream(), tansferEnc);
                } catch (final IOException e) {
                    try {
                        if (part instanceof MimeBodyPart) {
                            size = estimateSize(((MimeBodyPart) part).getRawInputStream(), tansferEnc);
                        } else if (part instanceof MimeMessage) {
                            size = estimateSize(((MimeMessage) part).getRawInputStream(), tansferEnc);
                        } else {
                            LOG.warn("{}'s size cannot be determined", part.getClass().getCanonicalName(),
                                e);
                        }
                    } catch (final IOException e1) {
                        LOG.warn("{}'s size cannot be determined", part.getClass().getCanonicalName(),
                            e1);
                    } catch (final MessagingException e1) {
                        LOG.warn("{}'s size cannot be determined", part.getClass().getCanonicalName(),
                            e1);
                    }
                }
            }
            mailPart.setSize(size);
            return mailPart;
        } catch (final MessageRemovedException e) {
            final String[] sa = part instanceof MimeMessage ? getFolderAndIdSafe((MimeMessage) part) : null;
            final String folder = null == sa ? null : sa[0];
            final String mailId = null == sa ? null : sa[1];
            if (null != folder && null != mailId) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(e, mailId, folder);
            }
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
        } catch (final MessagingException e) {
            throw MailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts specified flags bit mask to an instance of {@link Flags}.
     *
     * @param flags The flags bit mask
     * @return The corresponding instance of {@link Flags}
     */
    public static Flags convertMailFlags(int flags) {
        final Flags flagsObj = new Flags();
        if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
            flagsObj.add(Flags.Flag.ANSWERED);
        }
        if ((flags & MailMessage.FLAG_DELETED) > 0) {
            flagsObj.add(Flags.Flag.DELETED);
        }
        if ((flags & MailMessage.FLAG_DRAFT) > 0) {
            flagsObj.add(Flags.Flag.DRAFT);
        }
        if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
            flagsObj.add(Flags.Flag.FLAGGED);
        }
        if ((flags & MailMessage.FLAG_RECENT) > 0) {
            flagsObj.add(Flags.Flag.RECENT);
        }
        if ((flags & MailMessage.FLAG_SEEN) > 0) {
            flagsObj.add(Flags.Flag.SEEN);
        }
        if ((flags & MailMessage.FLAG_USER) > 0) {
            flagsObj.add(Flags.Flag.USER);
        }
        if ((flags & MailMessage.FLAG_SPAM) > 0) {
            flagsObj.add(MailMessage.USER_SPAM);
        }
        if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
            flagsObj.add(MailMessage.USER_FORWARDED);
        }
        if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
            flagsObj.add(MailMessage.USER_READ_ACK);
        }
        return flagsObj;
    }

    private static int estimateSize(final InputStream in, final String tansferEnc) throws IOException {
        try {
            if (ENC_BASE64.equalsIgnoreCase(tansferEnc)) {
                return (int) (in.available() * 0.65);
            }
            return in.available();
        } finally {
            Streams.close(in);
        }
    }

    private static final String[] EMPTY_STRS = new String[0];

    /**
     * Parses specified {@link Flags flags} to given {@link MailMessage mail}.
     *
     * @param flags The flags to parse
     * @param mailMessage The mail to apply the flags to
     * @throws OXException If a mail error occurs
     */
    public static void parseFlags(final Flags flags, final MailMessage mailMessage) throws OXException {
        int retval = 0;
        if (flags.contains(Flags.Flag.ANSWERED)) {
            retval |= MailMessage.FLAG_ANSWERED;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            retval |= MailMessage.FLAG_DELETED;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            retval |= MailMessage.FLAG_DRAFT;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            retval |= MailMessage.FLAG_FLAGGED;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            retval |= MailMessage.FLAG_RECENT;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            retval |= MailMessage.FLAG_SEEN;
        }
        if (flags.contains(Flags.Flag.USER)) {
            retval |= MailMessage.FLAG_USER;
        }
        final String[] userFlags = flags.getUserFlags();
        if (userFlags != null) {
            /*
             * Mark message to contain user flags
             */
            mailMessage.addUserFlags(EMPTY_STRS);
            for (final String userFlag : userFlags) {
                if (MailMessage.isColorLabel(userFlag)) {
                    mailMessage.setColorLabel(MailMessage.getColorLabelIntValue(userFlag));
                } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    retval |= MailMessage.FLAG_FORWARDED;
                } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    retval |= MailMessage.FLAG_READ_ACK;
                } else {
                    mailMessage.addUserFlag(userFlag);
                }
            }
        }
        /*
         * Set system flags
         */
        mailMessage.setFlags(retval);
    }

    private static final int DEFAULT_MESSAGE_SIZE = 8192;

    private static volatile Boolean enableMime4j;

    private static boolean useMime4j() {
        Boolean tmp = enableMime4j;
        if (null == tmp) {
            synchronized (MimeMessageConverter.class) {
                final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                tmp = Boolean.valueOf(null == service ? false : service.getBoolProperty("com.openexchange.mail.mime.enableMime4j", false));
                enableMime4j = tmp;
            }
        }
        return tmp.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                enableMime4j = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

    private static void setHeaders(final Part part, final MailPart mailPart) throws OXException {
        /*
         * HEADERS
         */
        HeaderCollection headers = null;
        try {
            headers = new HeaderCollection(128);
            if (useMime4j() && (part instanceof IMAPMessage)) {
                final ContentHandler handler = new HeaderContentHandler(headers);
                final MimeConfig config = new MimeConfig();
                config.setMaxLineLen(-1);
                config.setMaxHeaderLen(-1);
                config.setMaxHeaderCount(-1);
                final MimeStreamParser parser = new MimeStreamParser(config);
                parser.setContentHandler(handler);
                try {
                    ByteArrayOutputStream out = new HeaderOutputStream();
                    part.writeTo(out);
                    final ByteArrayInputStream in = new UnsynchronizedByteArrayInputStream(out.toByteArray());
                    out = null;
                    parser.parse(in);
                } catch (final IOException e1) {
                    LOG.warn("Unable to parse headers. Assuming no headers...", e1);
                    headers = new HeaderCollection(0);
                } catch (final MimeException e1) {
                    if (!HeaderContentHandler.END_HEADER_EXCEPTION.equals(e1)) {
                        throw new MessagingException(e1.getMessage(), e1);
                    }
                }
            } else {
                for (final Enumeration<?> e = part.getAllHeaders(); e.hasMoreElements();) {
                    final Header h = (Header) e.nextElement();
                    final String value = h.getValue();
                    if (value == null || isEmpty(value)) {
                        headers.addHeader(h.getName(), STR_EMPTY);
                    } else {
                        headers.addHeader(h.getName(), unfold(value));
                    }
                }
            }
        } catch (final MessageRemovedException e) {
            final String[] sa = part instanceof MimeMessage ? getFolderAndIdSafe((MimeMessage) part) : null;
            final String folder = null == sa ? null : sa[0];
            final String mailId = null == sa ? null : sa[1];
            if (null != folder && null != mailId) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(e, mailId, folder);
            }
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
        } catch (final MessagingException e) {
            LOG.debug("JavaMail API failed to load part's headers. Using own routine.", e);
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_MESSAGE_SIZE);
            try {
                part.writeTo(out);
                headers = loadHeaders(new String(out.toByteArray(), Charsets.ISO_8859_1));
            } catch (final MessageRemovedIOException e2) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e2, new Object[0]);
            } catch (final IOException e2) {
                LOG.warn("Unable to parse headers. Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            } catch (final MessageRemovedException e2) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e2, new Object[0]);
            } catch (final MessagingException e2) {
                LOG.warn("Unable to parse headers Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            } catch (final RuntimeException e2) {
                LOG.warn("Unable to parse headers Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            }
        } catch (final RuntimeException e) {
            LOG.debug("JavaMail API failed to load part's headers. Using own routine.", e);
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_MESSAGE_SIZE);
            try {
                part.writeTo(out);
                headers = loadHeaders(new String(out.toByteArray(), Charsets.ISO_8859_1));
            } catch (final MessageRemovedIOException e2) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e2, new Object[0]);
            } catch (final IOException e2) {
                LOG.warn("Unable to parse headers Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            } catch (final MessageRemovedException e2) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e2, new Object[0]);
            } catch (final MessagingException e2) {
                LOG.warn("Unable to parse headers Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            } catch (final RuntimeException e2) {
                LOG.warn("Unable to parse headers Assuming no headers...", e2);
                headers = new HeaderCollection(0);
            }
        }
        mailPart.addHeaders(headers);
    }

    /**
     * Parses given headers' {@link InputStream input stream} into a {@link HeaderCollection collection} until EOF or 2 subsequent CRLFs
     * occur.
     * <p>
     * This is a convenience method that delegates to {@link #loadHeaders(byte[])}.
     *
     * @param inputStream The headers' {@link InputStream input stream}
     * @return The parsed headers as a {@link HeaderCollection collection}.
     * @throws IOException If an I/O error occurs.
     */
    public static HeaderCollection loadHeaders(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_MESSAGE_SIZE);
        final byte[] bbuf = new byte[DEFAULT_MESSAGE_SIZE];
        int read = -1;
        while ((read = inputStream.read(bbuf, 0, bbuf.length)) > 0) {
            out.write(bbuf, 0, read);
        }
        return loadHeaders(out.toByteArray());
    }

    /**
     * Parses given headers' <code>byte</code> array into a {@link HeaderCollection collection} until EOF or 2 subsequent CRLFs occur.
     * <p>
     * This is a convenience method that delegates to {@link #loadHeaders(String)}.
     *
     * @param bytes The headers' <code>byte</code> array
     * @return The parsed headers as a {@link HeaderCollection collection}.
     */
    public static HeaderCollection loadHeaders(final byte[] bytes) {
        return loadHeaders(new String(bytes, Charsets.ISO_8859_1));
    }

    private static final Pattern PATTERN_PARSE_HEADER = Pattern.compile("(\\S+):\\p{Blank}?(.*)(?:(?:\r?\n)|(?:$))");

    /**
     * Parses given message source's headers into a {@link HeaderCollection collection} until EOF or 2 subsequent CRLFs occur.
     *
     * @param messageSrc The message source
     * @return The parsed headers as a {@link HeaderCollection collection}.
     */
    public static HeaderCollection loadHeaders(final String messageSrc) {
        /*
         * Determine position of double line break
         */
        final int len = messageSrc.length();
        int i;
        NextRead: for (i = 0; i < len; ++i) {
            char c = messageSrc.charAt(i);
            final int prevPos = i;
            int count = 0;
            while ((c == '\r') || (c == '\n')) {
                if ((c == '\n') && (++count >= 2)) {
                    i = prevPos;
                    break NextRead;
                }
                if (++i >= len) {
                    i = prevPos;
                    break NextRead;
                }
                c = messageSrc.charAt(i);
            }
        }
        /*
         * Parse single headers
         */
        final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(messageSrc.substring(0, i)));
        final HeaderCollection headers = new HeaderCollection();
        while (m.find()) {
            final String value = m.group(2);
            if (value == null || isEmpty(value)) {
                headers.addHeader(m.group(1), STR_EMPTY);
            } else {
                headers.addHeader(m.group(1), value);
            }
        }
        return headers;
    }

    private static boolean isEmpty(final String value) {
        final int length = value.length();
        boolean empty = true;
        for (int i = 0; empty && i < length; i++) {
            final char c = value.charAt(i);
            empty = ((c == ' ') || (c == '\t'));
        }
        return empty;
    }

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     * @throws MessagingException If a messaging error occurs
     */
    public static String getSubject(final Message message) throws MessagingException {
        final String[] valueArr = message.getHeader(MessageHeaders.HDR_SUBJECT);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        return MimeMessageUtility.decodeEnvelopeSubject(valueArr[0]);
    }

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     */
    public static String getSubject(final MailMessage message) {
        final String[] valueArr = message.getHeader(MessageHeaders.HDR_SUBJECT);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        return MimeMessageUtility.decodeEnvelopeSubject(valueArr[0]);
    }

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     * @throws MessagingException If a messaging error occurs
     */
    public static String getStringHeader(final String name, final Message message) throws MessagingException {
        return getStringHeader(name, message, '\0');
    }

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     */
    public static String getStringHeader(final String name, final MailMessage message) {
        return getStringHeader(name, message, '\0');
    }

    /**
     * Gets the headers denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&#252;ber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @param delimiter The delimiter character if message contains multiple header values; set to <code>'\0'</code> to only consider first
     *            one
     * @return The decoded header
     * @throws MessagingException If a messaging error occurs
     */
    public static String getStringHeader(final String name, final Message message, final char delimiter) throws MessagingException {
        final String[] valueArr = message.getHeader(name);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        final String values;
        if ('\0' != delimiter && valueArr.length > 1) {
            final StringBuilder sb = new StringBuilder(valueArr[0]);
            for (int i = 1; i < valueArr.length; i++) {
                sb.append(delimiter).append(valueArr[i]);
            }
            values = sb.toString();
        } else {
            values = valueArr[0];
        }
        return decodeMultiEncodedHeader(values);
    }

    /**
     * Gets the headers denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot;    is decoded to    &quot;&#252;ber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @param delimiter The delimiter character if message contains multiple header values; set to <code>'\0'</code> to only consider first
     *            one
     * @return The decoded header
     */
    public static String getStringHeader(final String name, final MailMessage message, final char delimiter) {
        final String[] valueArr = message.getHeader(name);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        final String values;
        if ('\0' != delimiter && valueArr.length > 1) {
            final StringBuilder sb = new StringBuilder(valueArr[0]);
            for (int i = 1; i < valueArr.length; i++) {
                sb.append(delimiter).append(valueArr[i]);
            }
            values = sb.toString();
        } else {
            values = valueArr[0];
        }
        return decodeMultiEncodedHeader(values);
    }

    /**
     * Gets the address headers denoted by specified header name in a safe manner.
     * <p>
     * If strict parsing of address headers yields a {@link AddressException}, then a plain-text version is generated to display broken
     * address header as it is.
     *
     * @param name The address header name
     * @param message The message providing the address header
     * @return The parsed address headers as an array of {@link InternetAddress} instances
     * @throws MessagingException If a messaging error occurs
     */
    public static InternetAddress[] getAddressHeader(final String name, final Message message) throws MessagingException {
        final String[] addressArray = message.getHeader(name);
        if (null == addressArray || addressArray.length == 0) {
            return null;
        }
        final String addresses;
        if (addressArray.length > 1) {
            final StringBuilder sb = new StringBuilder(addressArray[0]);
            for (int i = 1; i < addressArray.length; i++) {
                sb.append(',').append(addressArray[i]);
            }
            addresses = sb.toString();
        } else {
            addresses = addressArray[0];
        }
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            return getAddressHeaderNonStrict(addresses, addressArray);
        }
    }

    /**
     * Gets the address headers denoted by specified header name in a safe manner.
     * <p>
     * If strict parsing of address headers yields a {@link AddressException}, then a plain-text version is generated to display broken
     * address header as it is.
     *
     * @param name The address header name
     * @param message The message providing the address header
     * @return The parsed address headers as an array of {@link InternetAddress} instances
     */
    public static InternetAddress[] getAddressHeader(final String name, final MailMessage message) {
        final String[] addressArray = message.getHeader(name);
        if (null == addressArray || addressArray.length == 0) {
            return null;
        }
        final String addresses;
        if (addressArray.length > 1) {
            final StringBuilder sb = new StringBuilder(addressArray[0]);
            for (int i = 1; i < addressArray.length; i++) {
                sb.append(',').append(addressArray[i]);
            }
            addresses = sb.toString();
        } else {
            addresses = addressArray[0];
        }
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            return getAddressHeaderNonStrict(addresses, addressArray);
        }
    }

    private static InternetAddress[] getAddressHeaderNonStrict(final String addressStrings, final String[] addressArray) {
        try {
            final InternetAddress[] addresses = QuotedInternetAddress.parseHeader(addressStrings, false);
            final List<InternetAddress> addressList = new ArrayList<InternetAddress>(addresses.length);
            for (final InternetAddress internetAddress : addresses) {
                try {
                    addressList.add(new QuotedInternetAddress(internetAddress.toString()));
                } catch (final AddressException e) {
                    addressList.add(internetAddress);
                }
            }
            return addressList.toArray(new InternetAddress[addressList.size()]);
        } catch (final AddressException e) {
            LOG.debug("Internet addresses could not be properly parsed. Using plain addresses' string representation instead.", e);
            return getAddressesOnParseError(addressArray);
        }
    }

    /**
     * Gets the address header from given address header value.
     *
     * @param addresses The address header value
     * @return The parsed addresses
     */
    public static InternetAddress[] getAddressHeader(final String addresses) {
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            LOG.debug("Internet addresses could not be properly parsed. Using plain addresses' string representation instead.", e);
            return PlainTextAddress.parseAddresses(addresses);
        }
    }

    private static InternetAddress[] getAddressesOnParseError(final String[] addrs) {
        List<InternetAddress> list = new LinkedList<InternetAddress>();
        for (int i = 0; i < addrs.length; i++) {
            InternetAddress[] plainAddresses = PlainTextAddress.parseAddresses(addrs[i]);
            if (null != plainAddresses && plainAddresses.length > 0) {
                list.addAll(Arrays.asList(plainAddresses));
            }
        }
        return list.toArray(new InternetAddress[list.size()]);
    }

    /**
     * Returns the value of the RFC 822 "Date" field. This is the date on which this message was sent. Returns <code>null</code> if this
     * field is unavailable or its value is absent.
     *
     * @param part The mail part
     * @return The sent Date
     */
    public static Date getSentDate(final MailPart part) {
        final String s = part.getHeader("Date", null);
        if (s != null) {
            try {
                final MailDateFormat mailDateFormat = MimeMessageUtility.getDefaultMailDateFormat();
                synchronized (mailDateFormat) {
                    return mailDateFormat.parse(s);
                }
            } catch (final ParseException pex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Returns the value of the RFC 822 "Date" field. This is the date on which this message was sent. Returns <code>null</code> if this
     * field is unavailable or its value is absent.
     *
     * @param mimeMessage The MIME message
     * @return The sent Date
     * @throws MessagingException If sent date cannot be returned
     */
    public static Date getSentDate(MimeMessage mimeMessage) throws MessagingException {
        String s = mimeMessage.getHeader("Date", null);
        if (s != null) {
            try {
                MailDateFormat mailDateFormat = MimeMessageUtility.getDefaultMailDateFormat();
                synchronized (mailDateFormat) {
                    return mailDateFormat.parse(s);
                }
            } catch (ParseException pex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Parses the value of header <code>X-Priority</code>.
     *
     * @param priorityStr The header value
     * @param mailMessage The mail message to fill
     */
    public static void parsePriority(final String priorityStr, final MailMessage mailMessage) {
        mailMessage.setPriority(parsePriority(priorityStr));
    }

    /**
     * Parses the value of header <code>Importance</code>.
     *
     * @param importance The header value
     * @param mailMessage The mail message to fill
     */
    public static void parseImportance(final String importance, final MailMessage mailMessage) {
        mailMessage.setPriority(parseImportance(importance));
    }

    /**
     * Parses the value of header <code>X-Priority</code>.
     *
     * @param priorityStr The header value
     */
    public static int parsePriority(final String priorityStr) {
        int priority = MailMessage.PRIORITY_NORMAL;
        if (null != priorityStr) {
            final String[] tmp = priorityStr.split(" +");
            try {
                priority = Integer.parseInt(tmp[0]);
            } catch (final NumberFormatException nfe) {
                LOG.debug("Assuming priority NORMAL due to strange X-Priority header: {}", priorityStr);
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }

    /**
     * Parses the value of header <code>Importance</code>.
     *
     * @param importance The header value
     */
    public static int parseImportance(final String importance) {
        int priority = MailMessage.PRIORITY_NORMAL;
        if (null != importance) {
            final String imp = importance.trim();
            if ("Low".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_LOWEST;
            } else if ("Medium".equalsIgnoreCase(imp) || "Normal".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_NORMAL;
            } else if ("High".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_HIGHEST;
            } else {
                LOG.debug("Assuming priority NORMAL due to strange Importance header: {}", importance);
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }
}
