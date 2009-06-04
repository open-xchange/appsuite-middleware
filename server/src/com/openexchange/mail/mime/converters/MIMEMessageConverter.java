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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.mail.mime.utils.MIMEMessageUtility.decodeMultiEncodedHeader;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.getFileName;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.hasAttachments;
import static com.openexchange.mail.mime.utils.MIMEMessageUtility.unfold;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.FullnameFolder;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.dataobjects.MIMEMailMessage;
import com.openexchange.mail.mime.dataobjects.MIMEMailPart;
import com.openexchange.mail.mime.filler.MIMEMessageFiller;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.sun.mail.pop3.POP3Folder;

/**
 * {@link MIMEMessageConverter} - Provides several methods to convert instances of {@link MimeMessage} to {@link MailMessage} in vice versa.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEMessageConverter {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEMessageConverter.class);

    private static interface MailMessageFieldFiller {

        public static final String[] NON_MATCHING_HEADERS = {
            MessageHeaders.HDR_FROM, MessageHeaders.HDR_TO, MessageHeaders.HDR_CC, MessageHeaders.HDR_BCC, MessageHeaders.HDR_DISP_NOT_TO,
            MessageHeaders.HDR_REPLY_TO, MessageHeaders.HDR_SUBJECT, MessageHeaders.HDR_DATE, MessageHeaders.HDR_X_PRIORITY,
            MessageHeaders.HDR_MESSAGE_ID, MessageHeaders.HDR_IN_REPLY_TO, MessageHeaders.HDR_REFERENCES, MessageHeaders.HDR_X_OX_VCARD,
            MessageHeaders.HDR_X_OX_NOTIFICATION };

        public static final org.apache.commons.logging.Log LOG1 = org.apache.commons.logging.LogFactory.getLog(MailMessageFieldFiller.class);

        /**
         * Fills a fields from source instance of {@link Message} in given destination instance of {@link MailMessage}.
         * 
         * @param mailMessage The mail message to fill
         * @param msg The source message
         * @throws MessagingException If a messaging error occurs
         * @throws MailException If a mail related error occurs
         */
        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException;
    }

    private static abstract class ExtendedMailMessageFieldFiller implements MailMessageFieldFiller {

        final Folder folder;

        public ExtendedMailMessageFieldFiller(final Folder folder) {
            super();
            this.folder = folder;
        }

    }

    private static final String STR_EMPTY = "";

    /**
     * Prevent instantiation.
     */
    private MIMEMessageConverter() {
        super();
    }

    /**
     * Creates a {@link Part} object from given instance of {@link MailPart}.
     * 
     * @param mailPart The instance of {@link MailPart}
     * @return Appropriate instance of {@link Part}
     */
    public static Part convertMailPart(final MailPart mailPart) throws MailException {
        try {
            if (mailPart instanceof MailMessage) {
                return convertMailMessage((MailMessage) mailPart);
            }
            final int size = (int) mailPart.getSize();
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(size <= 0 ? DEFAULT_MESSAGE_SIZE : size);
            mailPart.writeTo(out);
            return new MimeBodyPart(new UnsynchronizedByteArrayInputStream(out.toByteArray()));
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
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
     * @throws MailException If conversion fails
     */
    public static Message[] convertMailMessages(final MailMessage[] mails) throws MailException {
        if (null == mails) {
            return null;
        }
        final Message[] retval = new Message[mails.length];
        for (int i = 0; i < retval.length; i++) {
            if (null != mails[i]) {
                retval[i] = convertMailMessage(mails[i]);
            }
        }
        return retval;
    }

    /**
     * Converts given instance of {@link MailMessage} into a JavaMail-conform {@link Message} object.
     * 
     * @param mail The source instance of {@link MailMessage}
     * @return A JavaMail-conform {@link Message} object
     * @throws MailException If conversion fails
     */
    public static Message convertMailMessage(final MailMessage mail) throws MailException {
        if (mail instanceof ComposedMailMessage) {
            return convertComposedMailMessage((ComposedMailMessage) mail);
        }
        try {
            final int size = (int) mail.getSize();
            final MimeMessage mimeMessage;
            {
                final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(size <= 0 ? DEFAULT_MESSAGE_SIZE : size);
                mail.writeTo(out);
                mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession(), new UnsynchronizedByteArrayInputStream(
                    out.toByteArray()));
            }
            if (mail.containsFlags()) {
                parseMimeFlags(mail.getFlags(), mimeMessage);
            }
            if (mail.containsColorLabel()) {
                final Flags flags = new Flags();
                flags.add(MailMessage.getColorLabelStringValue(mail.getColorLabel()));
                mimeMessage.setFlags(flags, true);
            }
            if (mail.containsUserFlags()) {
                final Flags flags = new Flags();
                final String[] userFlags = mail.getUserFlags();
                for (final String userFlag : userFlags) {
                    flags.add(userFlag);
                }
                mimeMessage.setFlags(flags, true);
            }
            return mimeMessage;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        }
    }

    /**
     * Converts given instance of {@link ComposedMailMessage} into a JavaMail-conform {@link Message} object.
     * 
     * @param composedMail The source instance of {@link ComposedMailMessage}
     * @return A JavaMail-conform {@link Message} object
     * @throws MailException If conversion fails
     */
    public static Message convertComposedMailMessage(final ComposedMailMessage composedMail) throws MailException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession());
            /*
             * Fill message
             */
            final MIMEMessageFiller filler = new MIMEMessageFiller(composedMail.getSession(), composedMail.getContext());
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
            mimeMessage.saveChanges();
            return mimeMessage;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /**
     * Fills specified instance of {@link ComposedMailMessage} with {@link MIMEMessageFiller}.
     * 
     * @param composedMail The composed mail
     * @return A filled instance of {@link MailMessage} ready for further usage
     * @throws MailException If mail cannot be filled.
     */
    public static MailMessage fillComposedMailMessage(final ComposedMailMessage composedMail) throws MailException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MIMEDefaultSession.getDefaultSession());
            /*
             * Fill message
             */
            final MIMEMessageFiller filler = new MIMEMessageFiller(composedMail.getSession(), composedMail.getContext());
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
            mimeMessage.saveChanges();
            return convertMessage(mimeMessage);
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
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
     * @throws MailException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final MailField[] fields) throws MailException {
        return convertMessages(msgs, fields, false);
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
     * @param includeBody Whether to create mail messages with reference to content or not
     * @return The converted array of {@link Message} instances
     * @throws MailException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final MailField[] fields, final boolean includeBody) throws MailException {
        try {
            final MailMessageFieldFiller[] fillers = createFieldFillers(fields);
            final MailMessage[] mails = new MIMEMailMessage[msgs.length];
            for (int i = 0; i < mails.length; i++) {
                if (null != msgs[i]) {
                    /*
                     * Create with no reference to content
                     */
                    mails[i] = includeBody ? new MIMEMailMessage((MimeMessage) msgs[i]) : new MIMEMailMessage();
                    fillMessage(fillers, mails[i], msgs[i]);
                }
            }
            return mails;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
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
     * @throws MailException If conversion fails
     */
    public static MailMessage[] convertMessages(final Message[] msgs, final Folder folder, final MailField[] fields, final boolean includeBody) throws MailException {
        try {
            final MailMessageFieldFiller[] fillers = createFieldFillers(folder, fields);
            final MailMessage[] mails = new MIMEMailMessage[msgs.length];
            for (int i = 0; i < mails.length; i++) {
                if (null != msgs[i]) {
                    /*
                     * Create with no reference to content
                     */
                    mails[i] = includeBody ? new MIMEMailMessage((MimeMessage) msgs[i]) : new MIMEMailMessage();
                    fillMessage(fillers, mails[i], msgs[i]);
                }
            }
            return mails;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        }
    }

    private static void fillMessage(final MailMessageFieldFiller[] fillers, final MailMessage mailMessage, final Message msg) throws MailException, MessagingException {
        for (final MailMessageFieldFiller filler : fillers) {
            if (null != filler) {
                filler.fillField(mailMessage, msg);
            }
        }
    }

    private static final EnumMap<MailField, MailMessageFieldFiller> FILLER_MAP_EXT = new EnumMap<MailField, MailMessageFieldFiller>(
        MailField.class);

    static {
        FILLER_MAP_EXT.put(MailField.HEADERS, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                /*
                 * From
                 */
                try {
                    mailMessage.addFrom((InternetAddress[]) extMimeMessage.getFrom());
                } catch (final AddressException e) {
                    final String addrStr = extMimeMessage.getHeader(MessageHeaders.HDR_FROM, null);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable internet address" + addrStr);
                    }
                    mailMessage.addFrom(new PlainTextAddress(addrStr));
                }
                /*
                 * To, Cc, and Bcc
                 */
                try {
                    mailMessage.addTo((InternetAddress[]) extMimeMessage.getRecipients(Message.RecipientType.TO));
                } catch (final AddressException e) {
                    final String[] addrs = extMimeMessage.getHeader(MessageHeaders.HDR_TO);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable internet addresses" + Arrays.toString(addrs));
                    }
                    mailMessage.addTo(PlainTextAddress.getAddresses(addrs));
                }
                try {
                    mailMessage.addCc((InternetAddress[]) extMimeMessage.getRecipients(Message.RecipientType.CC));
                } catch (final AddressException e) {
                    final String[] addrs = extMimeMessage.getHeader(MessageHeaders.HDR_CC);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable internet addresses" + Arrays.toString(addrs));
                    }
                    mailMessage.addCc(PlainTextAddress.getAddresses(addrs));
                }
                try {
                    mailMessage.addBcc((InternetAddress[]) extMimeMessage.getRecipients(Message.RecipientType.BCC));
                } catch (final AddressException e) {
                    final String[] addrs = extMimeMessage.getHeader(MessageHeaders.HDR_BCC);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable internet addresses" + Arrays.toString(addrs));
                    }
                    mailMessage.addBcc(PlainTextAddress.getAddresses(addrs));
                }
                /*
                 * Disposition-Notification-To
                 */
                {
                    InternetAddress[] dispNotTo = null;
                    final String addrStr = extMimeMessage.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null);
                    if (null != addrStr) {
                        try {
                            dispNotTo = InternetAddress.parse(addrStr, true);
                        } catch (final AddressException e) {
                            dispNotTo = new InternetAddress[] { new PlainTextAddress(addrStr) };
                        }
                    }
                    if (null != dispNotTo && dispNotTo.length > 0) {
                        mailMessage.setDispositionNotification(dispNotTo[0]);
                    } else {
                        mailMessage.setDispositionNotification(null);
                    }
                }
                /*
                 * Reply-To
                 */
                final StringBuilder sb = new StringBuilder(128);
                {
                    InternetAddress[] replyTo = null;
                    try {
                        replyTo = (InternetAddress[]) extMimeMessage.getReplyTo();
                    } catch (final AddressException e) {
                        final String addrStr = extMimeMessage.getHeader(MessageHeaders.HDR_REPLY_TO, null);
                        if (null != addrStr) {
                            replyTo = new InternetAddress[] { new PlainTextAddress(addrStr) };
                        }
                    }
                    if (null != replyTo) {
                        sb.append(replyTo[0].toString());
                        for (int j = 1; j < replyTo.length; j++) {
                            sb.append(", ").append(replyTo[j].toString());
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REPLY_TO, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * Subject
                 */
                mailMessage.setSubject(extMimeMessage.getSubject());
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
                {
                    final String[] inReplyTo = extMimeMessage.getHeader(MessageHeaders.HDR_IN_REPLY_TO);
                    if (null != inReplyTo) {
                        sb.append(inReplyTo[0]);
                        for (int j = 1; j < inReplyTo.length; j++) {
                            sb.append(", ").append(inReplyTo[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REPLY_TO, sb.toString());
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
                 * All other
                 */
                for (final Enumeration<?> e = extMimeMessage.getNonMatchingHeaders(NON_MATCHING_HEADERS); e.hasMoreElements();) {
                    final Header h = (Header) e.nextElement();
                    mailMessage.addHeader(h.getName(), h.getValue());
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.ID, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setMailId(String.valueOf(((ExtendedMimeMessage) msg).getUid()));
            }
        });
        FILLER_MAP_EXT.put(MailField.FOLDER_ID, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setFolder(((ExtendedMimeMessage) msg).getFullname());
            }
        });

        FILLER_MAP_EXT.put(MailField.CONTENT_TYPE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MailException, MessagingException {
                try {
                    mailMessage.setContentType(((ExtendedMimeMessage) msg).getContentType());
                } catch (final MailException e) {
                    /*
                     * Cannot occur
                     */
                    LOG1.error(e.getMessage(), e);
                }
                mailMessage.setHasAttachment(((ExtendedMimeMessage) msg).hasAttachment());
            }
        });

        FILLER_MAP_EXT.put(MailField.FROM, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                try {
                    mailMessage.addFrom((InternetAddress[]) extMimeMessage.getFrom());
                } catch (final AddressException e) {
                    final String addrStr = extMimeMessage.getHeader(MessageHeaders.HDR_FROM, null);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug(new StringBuilder(128).append("Internet address could not be properly parsed, ").append(
                            "using plain address' string representation instead: ").append(addrStr).toString(), e);
                    }
                    mailMessage.addFrom(new PlainTextAddress(addrStr));
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.TO, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                try {
                    mailMessage.addTo((InternetAddress[]) extMimeMessage.getRecipients(RecipientType.TO));
                } catch (final AddressException e) {
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug(new StringBuilder(128).append("Internet addresses could not be properly parsed, ").append(
                            "using plain addresses' string representation instead.").toString(), e);
                    }
                    mailMessage.addTo(PlainTextAddress.getAddresses(extMimeMessage.getHeader(MessageHeaders.HDR_TO)));
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.CC, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                try {
                    mailMessage.addCc((InternetAddress[]) extMimeMessage.getRecipients(RecipientType.CC));
                } catch (final AddressException e) {
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug(new StringBuilder(128).append("Internet addresses could not be properly parsed, ").append(
                            "using plain addresses' string representation instead.").toString(), e);
                    }
                    mailMessage.addCc(PlainTextAddress.getAddresses(extMimeMessage.getHeader(MessageHeaders.HDR_CC)));
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.BCC, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final ExtendedMimeMessage extMimeMessage = (ExtendedMimeMessage) msg;
                try {
                    mailMessage.addBcc((InternetAddress[]) extMimeMessage.getRecipients(RecipientType.BCC));
                } catch (final AddressException e) {
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug(new StringBuilder(128).append("Internet addresses could not be properly parsed, ").append(
                            "using plain addresses' string representation instead.").toString(), e);
                    }
                    mailMessage.addCc(PlainTextAddress.getAddresses(extMimeMessage.getHeader(MessageHeaders.HDR_BCC)));
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.SUBJECT, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSubject(((ExtendedMimeMessage) msg).getSubject());
            }
        });
        FILLER_MAP_EXT.put(MailField.SIZE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSize(((ExtendedMimeMessage) msg).getSize());
            }
        });
        FILLER_MAP_EXT.put(MailField.SENT_DATE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSentDate(((ExtendedMimeMessage) msg).getSentDate());
            }
        });
        FILLER_MAP_EXT.put(MailField.RECEIVED_DATE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setReceivedDate(((ExtendedMimeMessage) msg).getReceivedDate());
            }
        });
        FILLER_MAP_EXT.put(MailField.FLAGS, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException {
                parseFlags(((ExtendedMimeMessage) msg).getFlags(), mailMessage);
            }
        });
        FILLER_MAP_EXT.put(MailField.THREAD_LEVEL, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setThreadLevel(((ExtendedMimeMessage) msg).getThreadLevel());
            }
        });
        FILLER_MAP_EXT.put(MailField.DISPOSITION_NOTIFICATION_TO, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = ((ExtendedMimeMessage) msg).getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                if ((val != null) && (val.length > 0)) {
                    mailMessage.setDispositionNotification(InternetAddress.parse(val[0], true)[0]);
                } else {
                    mailMessage.setDispositionNotification(null);
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.PRIORITY, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = ((ExtendedMimeMessage) msg).getHeader(MessageHeaders.HDR_X_PRIORITY);
                if ((val != null) && (val.length > 0)) {
                    parsePriority(val[0], mailMessage);
                } else {
                    mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                }
            }
        });
        FILLER_MAP_EXT.put(MailField.COLOR_LABEL, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException {
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
     * @throws MailException If field fillers cannot be created
     */
    private static MailMessageFieldFiller[] createFieldFillers(final MailField[] fields) throws MailException {
        final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[fields.length];
        for (int i = 0; i < fields.length; i++) {
            final MailMessageFieldFiller filler = FILLER_MAP_EXT.get(fields[i]);
            if (filler == null) {
                if (MailField.BODY.equals(fields[i]) || MailField.FULL.equals(fields[i])) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Ignoring mail field " + fields[i]);
                    }
                    fillers[i] = null;
                } else {
                    throw new MailException(MailException.Code.INVALID_FIELD, fields[i].toString());
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
        FILLER_MAP.put(MailField.HEADERS, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                /*
                 * From
                 */
                try {
                    mailMessage.addFrom((InternetAddress[]) msg.getFrom());
                } catch (final AddressException e) {
                    mailMessage.addFrom(PlainTextAddress.getAddresses(msg.getHeader(MessageHeaders.HDR_FROM)));
                }
                /*
                 * To, Cc, and Bcc
                 */
                try {
                    mailMessage.addTo((InternetAddress[]) msg.getRecipients(Message.RecipientType.TO));
                } catch (final AddressException e) {
                    mailMessage.addTo(PlainTextAddress.getAddresses(msg.getHeader(MessageHeaders.HDR_TO)));
                }
                try {
                    mailMessage.addCc((InternetAddress[]) msg.getRecipients(Message.RecipientType.CC));
                } catch (final AddressException e) {
                    mailMessage.addCc(PlainTextAddress.getAddresses(msg.getHeader(MessageHeaders.HDR_CC)));
                }
                try {
                    mailMessage.addBcc((InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC));
                } catch (final AddressException e) {
                    mailMessage.addBcc(PlainTextAddress.getAddresses(msg.getHeader(MessageHeaders.HDR_BCC)));
                }
                /*
                 * Disposition-Notification-To
                 */
                {
                    InternetAddress[] dispNotTo = null;
                    final String[] addrStr = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                    if (null != addrStr) {
                        try {
                            dispNotTo = InternetAddress.parse(addrStr[0], true);
                        } catch (final AddressException e) {
                            dispNotTo = new InternetAddress[] { new PlainTextAddress(addrStr[0]) };
                        }
                    }
                    if (null != dispNotTo && dispNotTo.length > 0) {
                        mailMessage.setDispositionNotification(dispNotTo[0]);
                    } else {
                        mailMessage.setDispositionNotification(null);
                    }
                }
                /*
                 * Reply-To
                 */
                final StringBuilder sb = new StringBuilder(128);
                {
                    InternetAddress[] replyTo = null;
                    try {
                        replyTo = (InternetAddress[]) msg.getReplyTo();
                    } catch (final AddressException e) {
                        final String[] addrStr = msg.getHeader(MessageHeaders.HDR_REPLY_TO);
                        if (null != addrStr) {
                            replyTo = new InternetAddress[] { new PlainTextAddress(addrStr[0]) };
                        }
                    }
                    if (null != replyTo) {
                        sb.append(replyTo[0].toString());
                        for (int j = 1; j < replyTo.length; j++) {
                            sb.append(", ").append(replyTo[j].toString());
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REPLY_TO, sb.toString());
                        sb.setLength(0);
                    }
                }
                /*
                 * Subject
                 */
                mailMessage.setSubject(msg.getSubject());
                /*
                 * Date
                 */
                mailMessage.setSentDate(msg.getSentDate());
                /*
                 * X-Priority
                 */
                {
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
                {
                    final String[] inReplyTo = msg.getHeader(MessageHeaders.HDR_IN_REPLY_TO);
                    if (null != inReplyTo) {
                        sb.append(inReplyTo[0]);
                        for (int j = 1; j < inReplyTo.length; j++) {
                            sb.append(", ").append(inReplyTo[j]);
                        }
                        mailMessage.addHeader(MessageHeaders.HDR_REPLY_TO, sb.toString());
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
                 * All other
                 */
                for (final Enumeration<?> e = msg.getNonMatchingHeaders(NON_MATCHING_HEADERS); e.hasMoreElements();) {
                    final Header h = (Header) e.nextElement();
                    mailMessage.addHeader(h.getName(), h.getValue());
                }
            }
        });
        FILLER_MAP.put(MailField.CONTENT_TYPE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException {
                ContentType ct = null;
                try {
                    final String[] tmp = msg.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                    if (tmp != null && tmp.length > 0) {
                        ct = new ContentType(tmp[0]);
                    } else {
                        ct = new ContentType(MIMETypes.MIME_DEFAULT);
                    }
                } catch (final MailException e) {
                    /*
                     * Cannot occur
                     */
                    LOG1.error("Invalid content type: " + msg.getContentType(), e);
                    try {
                        ct = new ContentType(MIMETypes.MIME_DEFAULT);
                    } catch (final MailException e1) {
                        /*
                         * Cannot occur
                         */
                        LOG1.error(e1.getMessage(), e1);
                        return;
                    }
                }
                mailMessage.setContentType(ct);
                if (msg instanceof ExtendedMimeMessage) {
                    mailMessage.setHasAttachment(((ExtendedMimeMessage) msg).hasAttachment());
                } else {
                    try {
                        mailMessage.setHasAttachment(ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL) && (ct.isMimeType(MIMETypes.MIME_MULTIPART_MIXED) || hasAttachments(
                            (Multipart) msg.getContent(),
                            ct.getSubType())));
                    } catch (final IOException e) {
                        throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
                    }
                }
            }
        });
        FILLER_MAP.put(MailField.FROM, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                try {
                    mailMessage.addFrom((InternetAddress[]) msg.getFrom());
                } catch (final AddressException e) {
                    final String[] fromHdr = msg.getHeader(MessageHeaders.HDR_FROM);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable addresse(s): " + Arrays.toString(fromHdr), e);
                    }
                    mailMessage.addFrom(new PlainTextAddress(fromHdr[0]));
                }
            }
        });
        FILLER_MAP.put(MailField.TO, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                try {
                    mailMessage.addTo((InternetAddress[]) msg.getRecipients(RecipientType.TO));
                } catch (final AddressException e) {
                    final String[] hdr = msg.getHeader(MessageHeaders.HDR_TO);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
                    }
                    mailMessage.addTo(new PlainTextAddress(hdr[0]));
                }
            }
        });
        FILLER_MAP.put(MailField.CC, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                try {
                    mailMessage.addCc((InternetAddress[]) msg.getRecipients(RecipientType.CC));
                } catch (final AddressException e) {
                    final String[] hdr = msg.getHeader(MessageHeaders.HDR_CC);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
                    }
                    mailMessage.addCc(new PlainTextAddress(hdr[0]));
                }
            }
        });
        FILLER_MAP.put(MailField.BCC, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                try {
                    mailMessage.addBcc((InternetAddress[]) msg.getRecipients(RecipientType.BCC));
                } catch (final AddressException e) {
                    final String[] hdr = msg.getHeader(MessageHeaders.HDR_BCC);
                    if (LOG1.isDebugEnabled()) {
                        LOG1.debug("Unparseable addresse(s): " + Arrays.toString(hdr), e);
                    }
                    mailMessage.addBcc(new PlainTextAddress(hdr[0]));
                }
            }
        });
        FILLER_MAP.put(MailField.SUBJECT, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSubject(decodeMultiEncodedHeader(msg.getSubject()));
            }
        });
        FILLER_MAP.put(MailField.SIZE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSize(msg.getSize());
            }
        });
        FILLER_MAP.put(MailField.SENT_DATE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setSentDate(msg.getSentDate());
            }
        });
        FILLER_MAP.put(MailField.RECEIVED_DATE, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                mailMessage.setReceivedDate(msg.getReceivedDate());
            }
        });
        FILLER_MAP.put(MailField.FLAGS, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException {
                parseFlags(msg.getFlags(), mailMessage);
            }
        });
        FILLER_MAP.put(MailField.THREAD_LEVEL, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                /*
                 * TODO: Thread level
                 */
                mailMessage.setThreadLevel(0);
            }
        });
        FILLER_MAP.put(MailField.DISPOSITION_NOTIFICATION_TO, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO);
                if ((val != null) && (val.length > 0)) {
                    mailMessage.setDispositionNotification(InternetAddress.parse(val[0], true)[0]);
                } else {
                    mailMessage.setDispositionNotification(null);
                }
            }
        });
        FILLER_MAP.put(MailField.PRIORITY, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                final String[] val = msg.getHeader(MessageHeaders.HDR_X_PRIORITY);
                if ((val != null) && (val.length > 0)) {
                    parsePriority(val[0], mailMessage);
                } else {
                    mailMessage.setPriority(MailMessage.PRIORITY_NORMAL);
                }
            }
        });
        FILLER_MAP.put(MailField.COLOR_LABEL, new MailMessageFieldFiller() {

            public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException, MailException {
                parseFlags(msg.getFlags(), mailMessage);
                if (!mailMessage.containsColorLabel()) {
                    mailMessage.setColorLabel(MailMessage.COLOR_LABEL_NONE);
                }
            }
        });
    }

    /**
     * Creates the field fillers and expects the messages to be common instances of {@link Message}.
     * 
     * @param folder The folder containing the messages
     * @param fields The fields to fill
     * @return An array of appropriate {@link MailMessageFieldFiller} implementations
     * @throws MailException If field fillers cannot be created
     */
    private static MailMessageFieldFiller[] createFieldFillers(final Folder folder, final MailField[] fields) throws MailException {
        final MailMessageFieldFiller[] fillers = new MailMessageFieldFiller[fields.length];
        for (int i = 0; i < fields.length; i++) {
            final MailMessageFieldFiller filler = FILLER_MAP.get(fields[i]);
            if (filler == null) {
                if (MailField.ID.equals(fields[i])) {
                    fillers[i] = new ExtendedMailMessageFieldFiller(folder) {

                        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                            if (folder instanceof UIDFolder) {
                                mailMessage.setMailId(String.valueOf(((UIDFolder) folder).getUID(msg)));
                            } else if (folder instanceof FullnameFolder) {
                                mailMessage.setMailId(((FullnameFolder) folder).getUID(msg));
                            } else if (folder instanceof POP3Folder) {
                                mailMessage.setMailId(((POP3Folder) folder).getUID(msg));
                            }
                        }
                    };
                } else if (MailField.FOLDER_ID.equals(fields[i])) {
                    fillers[i] = new ExtendedMailMessageFieldFiller(folder) {

                        public void fillField(final MailMessage mailMessage, final Message msg) throws MessagingException {
                            mailMessage.setFolder(folder.getFullName());
                        }
                    };
                } else if (MailField.BODY.equals(fields[i]) || MailField.FULL.equals(fields[i])) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Ignoring mail field " + fields[i]);
                    }
                    fillers[i] = null;
                } else {
                    throw new MailException(MailException.Code.INVALID_FIELD, fields[i].toString());
                }
            } else {
                fillers[i] = filler;
            }
        }
        return fillers;
    }

    /**
     * Creates a message data object from given message bytes conform to RFC822.
     * 
     * @param asciiBytes The message bytes conform to RFC822
     * @return An instance of <code>{@link MailMessage}</code>
     * @throws MailException If conversion fails
     */
    public static MailMessage convertMessage(final byte[] asciiBytes) throws MailException {
        try {
            return convertMessage(new MimeMessage(
                MIMEDefaultSession.getDefaultSession(),
                new UnsynchronizedByteArrayInputStream(asciiBytes)));
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    /**
     * Creates a message data object from given MIME message.
     * 
     * @param msg The MIME message
     * @return An instance of <code>{@link MailMessage}</code> containing the attributes from given MIME message
     * @throws MailException If conversion fails
     */
    public static MailMessage convertMessage(final MimeMessage msg) throws MailException {
        try {
            /*
             * Create with reference to content
             */
            final MIMEMailMessage mail = new MIMEMailMessage(msg);
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
            {
                final Folder f = msg.getFolder();
                if (f != null) {
                    /*
                     * No nested message
                     */
                    mail.setFolder(f.getFullName());
                    if (f instanceof UIDFolder) {
                        mail.setMailId(String.valueOf(((UIDFolder) f).getUID(msg)));
                    } else if (f instanceof FullnameFolder) {
                        mail.setMailId(((FullnameFolder) f).getUID(msg));
                    } else if (f instanceof POP3Folder) {
                        mail.setMailId(((POP3Folder) f).getUID(msg));
                    }
                    mail.setUnreadMessages(f.getUnreadMessageCount());
                }
            }
            setHeaders(msg, mail);
            try {
                final String addresses = mail.getHeader(MessageHeaders.HDR_FROM, ",");
                mail.addFrom(addresses == null ? null : InternetAddress.parseHeader(addresses, false));
                // Formerly: mail.addFrom((InternetAddress[]) msg.getFrom());
            } catch (final AddressException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
                mail.addFrom(getAddressesOnParseError(msg.getHeader(MessageHeaders.HDR_FROM)));
            }
            try {
                final String addresses = mail.getHeader(MessageHeaders.HDR_TO, ",");
                mail.addTo(addresses == null ? null : InternetAddress.parseHeader(addresses, false));
                // mail.addTo((InternetAddress[]) msg.getRecipients(Message.RecipientType.TO));
            } catch (final AddressException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
                mail.addTo(getAddressesOnParseError(msg.getHeader(MessageHeaders.HDR_TO)));
            }
            try {
                final String addresses = mail.getHeader(MessageHeaders.HDR_CC, ",");
                mail.addCc(addresses == null ? null : InternetAddress.parseHeader(addresses, false));
                // mail.addCc((InternetAddress[]) msg.getRecipients(Message.RecipientType.CC));
            } catch (final AddressException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
                mail.addCc(getAddressesOnParseError(msg.getHeader(MessageHeaders.HDR_CC)));
            }
            try {
                final String addresses = mail.getHeader(MessageHeaders.HDR_BCC, ",");
                mail.addBcc(addresses == null ? null : InternetAddress.parseHeader(addresses, false));
                // mail.addBcc((InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC));
            } catch (final AddressException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
                mail.addBcc(getAddressesOnParseError(msg.getHeader(MessageHeaders.HDR_BCC)));
            }
            {
                final String[] tmp = msg.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                if ((tmp != null) && (tmp.length > 0)) {
                    mail.setContentType(tmp[0]);
                } else {
                    mail.setContentType(MIMETypes.MIME_DEFAULT);
                }
            }
            {
                final ContentType ct = mail.getContentType();
                mail.setHasAttachment(ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL) && (MULTI_SUBTYPE_MIXED.equalsIgnoreCase(ct.getSubType()) || hasAttachments(
                    (Multipart) msg.getContent(),
                    ct.getSubType())));
            }
            {
                final String[] tmp = msg.getHeader(MessageHeaders.HDR_CONTENT_ID);
                if ((tmp != null) && (tmp.length > 0)) {
                    mail.setContentId(tmp[0]);
                } else {
                    mail.setContentId(null);
                }
            }
            {
                final String tmp = msg.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
                if ((tmp != null) && (tmp.length() > 0)) {
                    mail.setContentDisposition(tmp);
                } else {
                    mail.setContentDisposition((String) null);
                }
            }
            {
                final String dispNot = msg.getHeader(MessageHeaders.HDR_DISP_NOT_TO, null);
                if (dispNot == null) {
                    mail.setDispositionNotification(null);
                } else {
                    mail.setDispositionNotification(InternetAddress.parse(dispNot, true)[0]);
                }
            }
            {
                final String msgrefStr = msg.getHeader(MessageHeaders.HDR_X_OXMSGREF, null);
                if (msgrefStr == null) {
                    mail.setMsgref(null);
                } else {
                    mail.setMsgref(new MailPath(msgrefStr));
                    msg.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    mail.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                }
            }
            mail.setFileName(getFileName(mail));
            parsePriority(mail.getFirstHeader(MessageHeaders.HDR_X_PRIORITY), mail);
            if (msg.getReceivedDate() == null) {
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
                mail.setReceivedDate(msg.getReceivedDate());
            }
            if (msg.getSentDate() == null) {
                mail.setSentDate(null);
            } else {
                mail.setSentDate(msg.getSentDate());
            }
            mail.setSize(msg.getSize());
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
            mail.setSubject(decodeMultiEncodedHeader(mail.getFirstHeader(MessageHeaders.HDR_SUBJECT)));
            mail.setThreadLevel(0);
            return mail;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
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
     * @throws MailException If conversion fails
     */
    public static MailMessage convertMessage(final byte[] asciiBytes, final String uid, final String fullname, final char separator, final MailField[] fields) throws MailException {
        try {
            return convertMessage(new MimeMessage(
                MIMEDefaultSession.getDefaultSession(),
                new UnsynchronizedByteArrayInputStream(asciiBytes)), uid, fullname, separator, fields);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
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
     * @throws MailException If conversion fails
     */
    public static MailMessage convertMessage(final MimeMessage msg, final String uid, final String fullname, final char separator, final MailField[] fields) throws MailException {
        final MailFields set = new MailFields(fields);
        if (set.contains(MailField.FULL)) {
            final MailMessage mail = convertMessage(msg);
            mail.setMailId(uid);
            mail.setFolder(fullname);
            return mail;
        }
        try {
            final MailMessageFieldFiller[] fillers = createFieldFillers(new FullnameFolder(fullname, separator, uid), fields);
            final MailMessage mail = (set.contains(MailField.BODY)) ? new MIMEMailMessage(msg) : new MIMEMailMessage();
            fillMessage(fillers, mail, msg);
            return mail;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        }
    }

    /**
     * Creates a MIME mail part object from given raw bytes.
     * 
     * @param asciiBytes The raw bytes
     * @return A MIME mail part object
     * @throws MailException If creating MIME mail part object fails
     */
    public static MailPart convertPart(final byte[] asciiBytes) throws MailException {
        try {
            return convertPart(new MimeBodyPart(new UnsynchronizedByteArrayInputStream(asciiBytes)));
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
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
    public static MailPart convertPart(final Part part) throws MailException {
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
    public static MailPart convertPart(final Part part, final boolean enforeSize) throws MailException {
        try {
            /*
             * Create with reference to content
             */
            final MailPart mailPart = new MIMEMailPart(part);
            /*
             * Set all cacheable data
             */
            setHeaders(part, mailPart);
            {
                final String[] contentTypeHdr = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                if (null != contentTypeHdr && contentTypeHdr.length > 0) {
                    mailPart.setContentType(unfold(contentTypeHdr[0]));
                }
            }
            {
                final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_ID);
                if ((tmp != null) && (tmp.length > 0)) {
                    mailPart.setContentId(tmp[0]);
                }
            }
            {
                final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
                if ((tmp != null) && (tmp.length > 0)) {
                    mailPart.setContentDisposition(tmp[0]);
                }
            }
            {
                final String[] msgrefStr = part.getHeader(MessageHeaders.HDR_X_OXMSGREF);
                if (msgrefStr != null && msgrefStr.length > 0) {
                    mailPart.setMsgref(new MailPath(msgrefStr[0]));
                    part.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                    mailPart.removeHeader(MessageHeaders.HDR_X_OXMSGREF);
                } else {
                    mailPart.setMsgref(null);
                }
            }
            mailPart.setFileName(getFileName(mailPart));
            int size = part.getSize();
            if (size == -1 && enforeSize) {
                /*
                 * Estimate unknown size: The encoded form of the file is expanded by 37% for UU encoding and by 35% for base64 encoding (3
                 * bytes become 4 plus control information).
                 */
                final String tansferEnc = (((MimePart) part).getHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, null));
                try {
                    size = estimateSize(part.getInputStream(), tansferEnc);
                } catch (final IOException e) {
                    try {
                        if (part instanceof MimeBodyPart) {
                            size = estimateSize(((MimeBodyPart) part).getRawInputStream(), tansferEnc);
                        } else if (part instanceof MimeMessage) {
                            size = estimateSize(((MimeMessage) part).getRawInputStream(), tansferEnc);
                        } else {
                            LOG.warn(new StringBuilder(256).append(part.getClass().getCanonicalName()).append(
                                "'s size cannot be determined").toString(), e);
                        }
                    } catch (final IOException e1) {
                        LOG.warn(
                            new StringBuilder(256).append(part.getClass().getCanonicalName()).append("'s size cannot be determined").toString(),
                            e1);
                    } catch (final MessagingException e1) {
                        LOG.warn(
                            new StringBuilder(256).append(part.getClass().getCanonicalName()).append("'s size cannot be determined").toString(),
                            e1);
                    }
                }
            }
            mailPart.setSize(size);
            return mailPart;
        } catch (final MessagingException e) {
            throw new MailException(MailException.Code.MESSAGING_ERROR, e, e.getMessage());
        }
    }

    /**
     * Converts specified flags bit mask to an instance of {@link Flags}.
     * 
     * @param flags The flags bit mask
     * @return The corresponding instance of {@link Flags}
     */
    public static Flags convertMailFlags(final int flags) {
        final Flags flagsObj = new Flags();
        if ((flags & MailMessage.FLAG_ANSWERED) == MailMessage.FLAG_ANSWERED) {
            flagsObj.add(Flags.Flag.ANSWERED);
        }
        if ((flags & MailMessage.FLAG_DELETED) == MailMessage.FLAG_DELETED) {
            flagsObj.add(Flags.Flag.DELETED);
        }
        if ((flags & MailMessage.FLAG_DRAFT) == MailMessage.FLAG_DRAFT) {
            flagsObj.add(Flags.Flag.DRAFT);
        }
        if ((flags & MailMessage.FLAG_FLAGGED) == MailMessage.FLAG_FLAGGED) {
            flagsObj.add(Flags.Flag.FLAGGED);
        }
        if ((flags & MailMessage.FLAG_RECENT) == MailMessage.FLAG_RECENT) {
            flagsObj.add(Flags.Flag.RECENT);
        }
        if ((flags & MailMessage.FLAG_SEEN) == MailMessage.FLAG_SEEN) {
            flagsObj.add(Flags.Flag.SEEN);
        }
        if ((flags & MailMessage.FLAG_USER) == MailMessage.FLAG_USER) {
            flagsObj.add(Flags.Flag.USER);
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
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static final String[] EMPTY_STRS = new String[0];

    /**
     * Parses specified {@link Flags flags} to given {@link MailMessage mail}.
     * 
     * @param flags The flags to parse
     * @param mailMessage The mail to apply the flags to
     * @throws MailException If a mail error occurs
     */
    static void parseFlags(final Flags flags, final MailMessage mailMessage) throws MailException {
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

    private static void setHeaders(final Part part, final MailPart mailPart) {
        /*
         * HEADERS
         */
        HeaderCollection headers = null;
        try {
            headers = new HeaderCollection();
            for (final Enumeration<?> e = part.getAllHeaders(); e.hasMoreElements();) {
                final Header h = (Header) e.nextElement();
                final String value = h.getValue();
                if (value == null || isEmpty(value)) {
                    headers.addHeader(h.getName(), STR_EMPTY);
                } else {
                    headers.addHeader(h.getName(), unfold(value));
                }
            }
        } catch (final MessagingException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("JavaMail API failed to load part's headers. Using own routine.", e);
            }
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_MESSAGE_SIZE);
            try {
                part.writeTo(out);
                headers = loadHeaders(new String(out.toByteArray(), "US-ASCII"));
            } catch (final IOException e2) {
                LOG.error("Unable to parse headers", e2);
                headers = new HeaderCollection(0);
            } catch (final MessagingException e2) {
                LOG.error("Unable to parse headers", e2);
                headers = new HeaderCollection(0);
            } catch (final IllegalArgumentException e2) {
                LOG.error("Unable to parse headers", e2);
                headers = new HeaderCollection(0);
            }
        } catch (final IllegalArgumentException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("JavaMail API failed to load part's headers. Using own routine.", e);
            }
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_MESSAGE_SIZE);
            try {
                part.writeTo(out);
                headers = loadHeaders(new String(out.toByteArray(), "US-ASCII"));
            } catch (final IOException e2) {
                LOG.error("Unable to parse headers", e2);
                headers = new HeaderCollection(0);
            } catch (final MessagingException e2) {
                LOG.error("Unable to parse headers", e2);
                headers = new HeaderCollection(0);
            } catch (final IllegalArgumentException e2) {
                LOG.error("Unable to parse headers", e2);
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
        while ((read = inputStream.read(bbuf, 0, bbuf.length)) != -1) {
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
        try {
            return loadHeaders(new String(bytes, "US-ASCII"));
        } catch (final UnsupportedEncodingException e) {
            /*
             * Cannot occur
             */
            LOG.error(e.getMessage(), e);
            return new HeaderCollection(0);
        }
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
        final char[] chars = value.toCharArray();
        boolean empty = true;
        for (int i = 0; i < chars.length && empty; i++) {
            empty = ((chars[i] == ' ') || (chars[i] == '\t'));
        }
        return empty;
    }

    private static InternetAddress[] getAddressesOnParseError(final String[] addrs) {
        final InternetAddress[] retval = new InternetAddress[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            retval[i] = new PlainTextAddress(addrs[i]);
        }
        return retval;
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Assuming priority NORMAL due to strange X-Priority header: " + priorityStr, nfe);
                }
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }
}
