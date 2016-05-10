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

package com.openexchange.mail.mime.filler;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message.RecipientType;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Charsets;
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.mime.utils.sourcedimage.SourcedImage;
import com.openexchange.mail.mime.utils.sourcedimage.SourcedImageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.version.Version;

/**
 * {@link MimeMessageFiller} - Provides basic methods to fills an instance of {@link MimeMessage} with headers/contents given through an
 * instance of {@link ComposedMailMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeMessageFiller {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMessageFiller.class);

    static final String HDR_ORGANIZATION = MessageHeaders.HDR_ORGANIZATION;

    private static final String HDR_X_MAILER = MessageHeaders.HDR_X_MAILER;

    static final String HDR_X_ORIGINATING_CLIENT = MessageHeaders.HDR_X_ORIGINATING_CLIENT;

    private static final String HDR_MIME_VERSION = MessageHeaders.HDR_MIME_VERSION;

    private static final String PREFIX_PART = "part";

    private static final String EXT_EML = ".eml";

    private static final String VERSION_1_0 = "1.0";

    private static final String VCARD_ERROR = "Error while appending user VCard";

    /*
     * Constants for Multipart types
     */
    private static final String MP_ALTERNATIVE = "alternative";

    private static final String MP_RELATED = "related";

    /*
     * Fields
     */
    protected int accountId;

    private Set<String> uploadFileIDs;

    private Set<String> contentIds;

    private boolean discardReferencedInlinedImages;

    private final HtmlService htmlService;

    protected final CompositionParameters compositionParameters;

    public MimeMessageFiller(final CompositionParameters compositionParameters) {
        super();
        discardReferencedInlinedImages = true;
        htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
        this.compositionParameters = compositionParameters;
    }

    /**
     * Initializes a new {@link MimeMessageFiller}
     *
     * @param session The session providing user data
     * @param ctx The context
     */
    public MimeMessageFiller(final Session session, final Context ctx) {
        this(session, ctx, UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx));
    }

    /**
     * Initializes a new {@link MimeMessageFiller}
     *
     * @param session The session providing user data
     * @param ctx The context
     * @param usm The user's mail settings
     */
    public MimeMessageFiller(final Session session, final Context ctx, final UserSettingMail usm) {
        super();
        discardReferencedInlinedImages = true;
        htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
        compositionParameters = new SessionCompositionParameters(session, ctx, usm);
    }

    /**
     * Sets whether to discard referenced inlined images.
     *
     * @param discardReferencedInlinedImages The flag to set
     * @return This filler with new behavior applied
     */
    public MimeMessageFiller setDiscardReferencedInlinedImages(boolean discardReferencedInlinedImages) {
        this.discardReferencedInlinedImages = discardReferencedInlinedImages;
        return this;
    }

    /**
     * Sets the account identifier
     *
     * @param accountId The account identifier to set
     * @return This filler with account identifier set
     */
    public MimeMessageFiller setAccountId(final int accountId) {
        this.accountId = accountId;
        if (compositionParameters instanceof SessionCompositionParameters) {
            ((SessionCompositionParameters) compositionParameters).setAccountId(accountId);
        }
        return this;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /*
     * protected final Html2TextConverter getConverter() { if (converter == null) { converter = new Html2TextConverter(); } return
     * converter; }
     */

    /**
     * Deletes referenced local uploaded files from session and disk after filled instance of <code>{@link MimeMessage}</code> is dispatched
     */
    public void deleteReferencedUploadFiles() {
        if (uploadFileIDs != null) {
            final int size = uploadFileIDs.size();
            final Iterator<String> iter = uploadFileIDs.iterator();
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            if (mfm != null) {
                for (int i = 0; i < size; i++) {
                    try {
                        mfm.removeByID(iter.next());
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                }
            }
            uploadFileIDs.clear();
        }
    }

    /**
     * Sets common headers in given MIME message: <code>X-Mailer</code> and <code>Organization</code>.
     *
     * @param mimeMessage The MIME message
     * @throws MessagingException If headers cannot be set
     */
    public void setCommonHeaders(final MimeMessage mimeMessage) throws MessagingException, OXException {
        /*
         * Set mailer
         */
        mimeMessage.setHeader(HDR_X_MAILER, "Open-Xchange Mailer v" + Version.getInstance().getVersionString());
        /*
         * Set organization to context-admin's company field setting
         */
        if (accountId <= 0) {
            try {
                final String organization = compositionParameters.getOrganization();
                if (null != organization && 0 < organization.length()) {
                    final String encoded = MimeUtility.fold(
                        14,
                        MimeUtility.encodeText(organization, MailProperties.getInstance().getDefaultMimeCharset(), null));
                    mimeMessage.setHeader(HDR_ORGANIZATION, encoded);
                }
            } catch (final Exception e) {
                LOG.warn("Header \"Organization\" could not be set", e);
            }
        }
        /*
         * Add header X-Originating-IP containing the IP address of the client
         */
        if (MailProperties.getInstance().isAddClientIPAddress()) {
            addClientIPAddress(mimeMessage);
        }
        {
            String client = compositionParameters.getClient();
            if (!Strings.isEmpty(client)) {
                try {
                    final String encoded = MimeUtility.fold(20, MimeUtility.encodeText(client, MailProperties.getInstance().getDefaultMimeCharset(), null));
                    mimeMessage.setHeader(HDR_X_ORIGINATING_CLIENT, encoded);
                } catch (final Exception e) {
                    LOG.warn("Header \"X-Originating-Client\" could not be set", e);
                }
            }
        }
    }

    /**
     * Add "X-Originating-IP" header.
     *
     * @param mimeMessage The MIME message
     * @throws MessagingException If an error occurs
     */
    private void addClientIPAddress(final MimeMessage mimeMessage) throws OXException, MessagingException {
        String originatingIP = compositionParameters.getOriginatingIP();
        if (originatingIP != null) {
            mimeMessage.setHeader("X-Originating-IP", originatingIP);
        }
    }

    /**
     * Add "X-Originating-IP" header.
     *
     * @param mimeMessage The MIME message
     * @param session The session
     * @throws MessagingException If an error occurs
     */
    public static void addClientIPAddress(final MimeMessage mimeMessage, final Session session) throws MessagingException {
        /*
         * Get IP from session
         */
        final String localIp = session.getLocalIp();
        if (isLocalhost(localIp)) {
            LOG.debug("Session provides localhost as client IP address: {}", localIp);
            // Prefer request's remote address if local IP seems to denote local host
            String clientIp = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS);
            mimeMessage.setHeader("X-Originating-IP", clientIp == null ? localIp : clientIp);
        } else {
            mimeMessage.setHeader("X-Originating-IP", localIp);
        }
    }

    private static final Set<String> LOCAL_ADDRS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("127.0.0.1", "localhost", "::1")));

    static boolean isLocalhost(final String localIp) {
        return LOCAL_ADDRS.contains(localIp);
    }

    private static final String[] SUPPRESS_HEADERS = {
        MessageHeaders.HDR_X_OX_VCARD, MessageHeaders.HDR_X_OXMSGREF, MessageHeaders.HDR_X_OX_MARKER, MessageHeaders.HDR_X_OX_NOTIFICATION,
        MessageHeaders.HDR_IMPORTANCE, MessageHeaders.HDR_X_PRIORITY, HDR_X_MAILER };

    /**
     * Sets necessary headers in specified MIME message: <code>From</code>/ <code>Sender</code>, <code>To</code>, <code>Cc</code>,
     * <code>Bcc</code>, <code>Reply-To</code>, <code>Subject</code>, etc.
     *
     * @param mail The composed mail
     * @param mimeMessage The MIME message
     * @throws MessagingException If headers cannot be set
     * @throws OXException If a mail error occurs
     */
    public void setMessageHeaders(final ComposedMailMessage mail, final MimeMessage mimeMessage) throws MessagingException, OXException {
        /*
         * Set from/sender
         */
        if (mail.containsFrom()) {
            /*
             * Get from
             */
            InternetAddress[] fromAddresses = mail.getFrom();
            if (fromAddresses.length > 0) {
                InternetAddress from = fromAddresses[0];
                mimeMessage.setFrom(from);

                /*
                 * Taken from RFC 822 section 4.4.2: In particular, the "Sender" field MUST be present if it is NOT the same as the "From"
                 * Field.
                 */
                InternetAddress sender = compositionParameters.getSenderAddress(from);
                if (sender != null) {
                    mimeMessage.setSender(sender);
                }
            }
        }
        /*
         * Set to
         */
        if (mail.containsTo()) {
            mimeMessage.setRecipients(RecipientType.TO, mail.getTo());
        }
        /*
         * Set cc
         */
        if (mail.containsCc()) {
            mimeMessage.setRecipients(RecipientType.CC, mail.getCc());
        }
        /*
         * Bcc
         */
        if (mail.containsBcc()) {
            mimeMessage.setRecipients(RecipientType.BCC, mail.getBcc());
        }
        /*
         * Reply-To
         */
        setReplyTo(mail, mimeMessage);
        /*
         * Set subject
         */
        if (mail.containsSubject()) {
            mimeMessage.setSubject(mail.getSubject(), MailProperties.getInstance().getDefaultMimeCharset());
        }
        /*
         * Set sent date
         */
        if (mail.containsSentDate()) {
            final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(compositionParameters.getTimeZoneID());
            synchronized (mdf) {
                mimeMessage.setHeader("Date", mdf.format(mail.getSentDate()));
            }
        }
        /*
         * Set flags
         */
        final Flags msgFlags = new Flags();
        if (mail.isAnswered()) {
            msgFlags.add(Flags.Flag.ANSWERED);
        }
        if (mail.isDeleted()) {
            msgFlags.add(Flags.Flag.DELETED);
        }
        if (mail.isDraft()) {
            msgFlags.add(Flags.Flag.DRAFT);
        }
        if (mail.isFlagged()) {
            msgFlags.add(Flags.Flag.FLAGGED);
        }
        if (mail.isRecent()) {
            msgFlags.add(Flags.Flag.RECENT);
        }
        if (mail.isSeen()) {
            msgFlags.add(Flags.Flag.SEEN);
        }
        if (mail.isUser()) {
            msgFlags.add(Flags.Flag.USER);
        }
        if (mail.isForwarded()) {
            msgFlags.add(MailMessage.USER_FORWARDED);
        }
        if (mail.isReadAcknowledgment()) {
            msgFlags.add(MailMessage.USER_READ_ACK);
        }
        if (mail.getColorLabel() != MailMessage.COLOR_LABEL_NONE) {
            msgFlags.add(MailMessage.getColorLabelStringValue(mail.getColorLabel()));
        }
        {
            final String[] userFlags = mail.getUserFlags();
            if (null != userFlags && userFlags.length > 0) {
                for (final String userFlag : userFlags) {
                    msgFlags.add(userFlag);
                }
            }
        }
        /*
         * Finally, apply flags to message
         */
        mimeMessage.setFlags(msgFlags, true);
        /*
         * Set disposition notification
         */
        if (mail.getDispositionNotification() != null) {
            if (mail.isDraft()) {
                mimeMessage.setHeader(MessageHeaders.HDR_X_OX_NOTIFICATION, mail.getDispositionNotification().toString());
            } else {
                mimeMessage.setHeader(MessageHeaders.HDR_DISP_TO, mail.getDispositionNotification().toString());
            }
        }
        /*
         * Set priority
         */
        final int priority = mail.getPriority();
        mimeMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(priority));
        if (MailMessage.PRIORITY_NORMAL == priority) {
            mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Medium");
        } else if (priority > MailMessage.PRIORITY_NORMAL) {
            mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
        } else {
            mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
        }
        /*
         * Headers
         */
        for (final Iterator<Map.Entry<String, String>> iter = mail.getNonMatchingHeaders(SUPPRESS_HEADERS); iter.hasNext();) {
            final Map.Entry<String, String> entry = iter.next();
            final String name = entry.getKey();
            if (isCustomOrReplyHeader(name)) {
                String value = entry.getValue();
                if(HDR_REFERENCES.equals(name)) {
                    mimeMessage.setHeader(name, MimeMessageUtility.fold(12, value));
                } else {
                    mimeMessage.setHeader(name, value);
                }
            }
        }
    }

    private void setReplyTo(final ComposedMailMessage mail, final MimeMessage mimeMessage) throws OXException, MessagingException {
        if (compositionParameters.setReplyTo()) {
            /*
             * Reply-To
             */
            final String hdrReplyTo = mail.getFirstHeader("Reply-To");
            if (!isEmpty(hdrReplyTo) && !toLowerCase(hdrReplyTo).startsWith("null")) {
                InternetAddress[] replyTo = null;
                try {
                    replyTo = QuotedInternetAddress.parse(hdrReplyTo, true);
                } catch (final AddressException e) {
                    LOG.error("Specified Reply-To address cannot be parsed", e);
                }

                if (null != replyTo) {
                    mimeMessage.setReplyTo(replyTo);
                } else if (mail.containsFrom()) {
                    mimeMessage.setReplyTo(mail.getFrom());
                }
            } else {
                String replyTo = compositionParameters.getReplyToAddress();
                if (replyTo != null) {
                    try {
                        mimeMessage.setReplyTo(QuotedInternetAddress.parse(replyTo, true));
                    } catch (final AddressException e) {
                        LOG.error("Default Reply-To address cannot be parsed", e);
                        try {
                            mimeMessage.setHeader(
                                MessageHeaders.HDR_REPLY_TO,
                                MimeUtility.encodeWord(replyTo, MailProperties.getInstance().getDefaultMimeCharset(), "Q"));
                        } catch (final UnsupportedEncodingException e1) {
                            /*
                             * Cannot occur since default mime charset is supported by JVM
                             */
                            LOG.error("", e1);
                        }
                    }
                } else if (mail.containsFrom()) {
                    mimeMessage.setReplyTo(mail.getFrom());
                }
            }
        }
    }

    /**
     * Checks if specified header name is a custom header (starts ignore-case with <code>"X-"</code>) or refers to a reply-relevant header.
     *
     * @param headerName The header name to check
     * @return <code>true</code> if specified header name is a custom OR reply-relevant header; otherwise <code>false</code>
     */
    public static boolean isCustomOrReplyHeader(final String headerName) {
        if (isEmpty(headerName)) {
            return false;
        }
        final char first = headerName.charAt(0);
        if ((('X' == first) || ('x' == first)) && ('-' == headerName.charAt(1))) {
            return true;
        }
        final String lc = toLowerCase(headerName);
        return "references".equals(lc) || "in-reply-to".equals(lc) || "message-id".equals(lc);
    }

    private static final String HDR_MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;

    private static final String HDR_REFERENCES = MessageHeaders.HDR_REFERENCES;

    private static final String HDR_IN_REPLY_TO = MessageHeaders.HDR_IN_REPLY_TO;

    /**
     * Sets the appropriate headers <code>In-Reply-To</code> and <code>References</code> in specified MIME message.
     * <p>
     * Moreover the <code>Reply-To</code> header is set.
     *
     * @param referencedMail The referenced mail
     * @param mimeMessage The MIME message
     * @throws MessagingException If setting the reply headers fails
     */
    public static void setReplyHeaders(final MailMessage referencedMail, final MimeMessage mimeMessage) throws MessagingException {
        if (null == referencedMail) {
            /*
             * Obviously referenced mail does no more exist; cancel setting reply headers Message-Id, In-Reply-To, and References.
             */
            return;
        }
        final String pMsgId = referencedMail.getFirstHeader(HDR_MESSAGE_ID);
        if (pMsgId != null) {
            mimeMessage.setHeader(HDR_IN_REPLY_TO, pMsgId);
        }
        /*
         * Set References header field
         */
        final String pReferences = referencedMail.getFirstHeader(HDR_REFERENCES);
        final String pInReplyTo = referencedMail.getFirstHeader(HDR_IN_REPLY_TO);
        final StringBuilder refBuilder = new StringBuilder();
        if (pReferences != null) {
            /*
             * The "References:" field will contain the contents of the parent's "References:" field (if any) followed by the contents of
             * the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pReferences);
        } else if (pInReplyTo != null) {
            /*
             * If the parent message does not contain a "References:" field but does have an "In-Reply-To:" field containing a single
             * message identifier, then the "References:" field will contain the contents of the parent's "In-Reply-To:" field followed by
             * the contents of the parent's "Message-ID:" field (if any).
             */
            refBuilder.append(pInReplyTo);
        }
        if (pMsgId != null) {
            if (refBuilder.length() > 0) {
                refBuilder.append(' ');
            }
            refBuilder.append(pMsgId);
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            mimeMessage.setHeader(HDR_REFERENCES, MimeMessageUtility.fold(12, refBuilder.toString()));
        } else if (refBuilder.length() > 0) {
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            mimeMessage.setHeader(HDR_REFERENCES, MimeMessageUtility.fold(12, refBuilder.toString()));
        }
    }

    /**
     * Sets the appropriate headers before message's transport: <code>Reply-To</code>, <code>Date</code>, and <code>Subject</code>
     *
     * @param mail The source mail
     * @param mimeMessage The MIME message
     * @throws OXException If a mail error occurs
     */
    public void setSendHeaders(final ComposedMailMessage mail, final MimeMessage mimeMessage) throws OXException {
        try {
            /*
             * Set the Reply-To header for future replies to this new message
             */
            setReplyTo(mail, mimeMessage);
            /*
             * Set sent date if not done, yet
             */
            {
                final Date sentDate = mimeMessage.getSentDate();
                final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(compositionParameters.getTimeZoneID());
                synchronized (mdf) {
                    mimeMessage.setHeader("Date", mdf.format(sentDate == null ? new Date() : sentDate));
                }
            }
            /*
             * Set default subject if none set
             */
            if (null == mimeMessage.getSubject()) {
                mimeMessage.setSubject(StringHelper.valueOf(compositionParameters.getLocale()).getString(MailStrings.DEFAULT_SUBJECT));
            }
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static final Pattern BODY_START = Pattern.compile("<body.*?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Fills the body of given instance of {@link MimeMessage} with the contents specified through given instance of
     * {@link ComposedMailMessage}.
     *
     * @param mail The source composed mail
     * @param mimeMessage The MIME message to fill
     * @param type The compose type
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public void fillMailBody(final ComposedMailMessage mail, final MimeMessage mimeMessage, ComposeType type) throws MessagingException, OXException, IOException {
        /*
         * Adopt Content-Type to ComposeType if necessary
         */
        if (ComposeType.NEW_SMS.equals(type)) {
            final ContentType contentType = mail.getContentType();
            contentType.setPrimaryType("text").setSubType("plain");
        }
        /*
         * Store some flags
         */
        final boolean hasNestedMessages = false;
        final boolean hasAttachments;
        final boolean isAttachmentForward;
        {
            int size = mail.getEnclosedCount();
            hasAttachments = size > 0;
            /*
             * A non-inline forward message
             */
            isAttachmentForward = ((ComposeType.FORWARD.equals(type)) && (compositionParameters.isForwardAsAttachment() || (size > 1 && hasOnlyReferencedMailAttachments(mail, size))));
        }
        /*
         * Initialize primary multipart
         */
        Multipart primaryMultipart = null;
        /*
         * Detect if primary multipart is of type multipart/mixed
         */
        if (hasNestedMessages || hasAttachments || mail.isAppendVCard() || isAttachmentForward) {
            primaryMultipart = new MimeMultipart();
        }
        /*
         * Content is expected to be multipart/alternative
         */
        final boolean sendMultipartAlternative;
        if (mail.isDraft()) {
            sendMultipartAlternative = false;
            if (mail.getContentType().startsWith(MimeTypes.MIME_MULTIPART_ALTERNATIVE)) {
                /*
                 * Allow only HTML if a draft message should be "sent"
                 */
                mail.setContentType(MimeTypes.MIME_TEXT_HTML);
            }
        } else {
            sendMultipartAlternative = mail.getContentType().startsWith(MimeTypes.MIME_MULTIPART_ALTERNATIVE);
        }
        /*
         * HTML content with embedded images
         */
        final TextBodyMailPart textBodyPart = mail.getBodyPart();
        /*
         * Check for with-source images
         */
        String content;
        final Map<String, SourcedImage> images;
        {
            final StringBuilder sb = new StringBuilder((String) textBodyPart.getContent());
            images = SourcedImageUtility.hasSourcedImages(sb);
            content = sb.toString();
        }
        /*
         * Check embedded images
         */
        final boolean embeddedImages;
        if (sendMultipartAlternative || mail.getContentType().startsWith("text/htm")) {
            /*
             * Check for referenced images (by cid oder locally available)
             */
            embeddedImages = !images.isEmpty() || MimeMessageUtility.hasEmbeddedImages(content) || MimeMessageUtility.hasReferencedLocalImages(content);
        } else {
            content = dropImages(content, false);
            embeddedImages = false;
        }
        /*
         * Compose message
         */
        final String charset = MailProperties.getInstance().getDefaultMimeCharset();
        if (hasAttachments || sendMultipartAlternative || isAttachmentForward || mail.isAppendVCard() || embeddedImages) {
            /*
             * If any condition is true, we ought to create a multipart/ message
             */
            if (sendMultipartAlternative) {
                final Multipart alternativeMultipart = createMultipartAlternative(mail, content, embeddedImages, images, textBodyPart, type);
                if (primaryMultipart == null) {
                    primaryMultipart = alternativeMultipart;
                } else {
                    final BodyPart bodyPart = new MimeBodyPart();
                    MessageUtility.setContent(alternativeMultipart, bodyPart);
                    // bodyPart.setContent(alternativeMultipart);
                    primaryMultipart.addBodyPart(bodyPart);
                }
            } else if (embeddedImages && !mail.getContentType().startsWith(MimeTypes.MIME_TEXT_PLAIN)) {
                final Multipart relatedMultipart = createMultipartRelated(mail, content, images, new String[1]);
                if (primaryMultipart == null) {
                    primaryMultipart = relatedMultipart;
                } else {
                    final BodyPart bodyPart = new MimeBodyPart();
                    MessageUtility.setContent(relatedMultipart, bodyPart);
                    // bodyPart.setContent(relatedMultipart);
                    primaryMultipart.addBodyPart(bodyPart);
                }
            } else {
                if (primaryMultipart == null) {
                    primaryMultipart = new MimeMultipart();
                }
                /*
                 * Convert html content to regular text if mail text is demanded to be text/plain
                 */
                if (mail.getContentType().startsWith(MimeTypes.MIME_TEXT_PLAIN)) {
                    /*
                     * Append text content
                     */
                    final String plainText = textBodyPart.getPlainText();
                    if (null == plainText) {
                        /*-
                         * Expect HTML content
                         *
                         * Well-formed HTML
                         */
                        // final String wellFormedHTMLContent = htmlService.getConformHTML(content, charset);
                        primaryMultipart.addBodyPart(createTextBodyPart(toArray(content, content), charset, false, true, type), 0);
                    } else {
                        primaryMultipart.addBodyPart(createTextBodyPart(toArray(plainText, plainText), charset, false, false, type), 0);
                    }
                } else {
                    /*-
                     * Append HTML content
                     *
                     * Well-formed HTML
                     */
                    final String wellFormedHTMLContent = htmlService.getConformHTML(content, charset);
                    primaryMultipart.addBodyPart(createHtmlBodyPart(wellFormedHTMLContent, charset));
                }
            }
            /*
             * Get number of enclosed parts
             */
            int size = mail.getEnclosedCount();
            if (size > 0) {
                if (isAttachmentForward) {
                    /*
                     * Add referenced mail(s)
                     */
                    StringBuilder sb = new StringBuilder(32);
                    for (int i = 0; i < size; i++) {
                        MailPart enclosedMailPart = mail.getEnclosedMailPart(i);
                        if (isMessage(enclosedMailPart)) {
                            addNestedMessage(enclosedMailPart, Boolean.FALSE, primaryMultipart, sb);
                        } else {
                            addMessageBodyPart(primaryMultipart, enclosedMailPart, false);
                        }
                    }
                } else {
                    /*
                     * Add referenced parts from ONE referenced mail
                     */
                    List<String> cidList = null;
                    for (int i = 0; i < size; i++) {
                        if (null == contentIds) {
                            if (discardReferencedInlinedImages) {
                                final MailPart mailPart = mail.getEnclosedMailPart(i);
                                boolean add = false;
                                if (embeddedImages && mailPart.getContentType().startsWith("image/")) {
                                    final String contentId = mailPart.getContentId();
                                    if (null != contentId) {
                                        // Check if image is already inlined inside HTML content
                                        if (null == cidList) {
                                            cidList = MimeMessageUtility.getContentIDs(content);
                                        }
                                        add = !MimeMessageUtility.containsContentId(contentId, cidList);
                                    } else {
                                        // A regular file-attachment image
                                        add = true;
                                    }
                                } else {
                                    add = true;
                                }
                                if (add) {
                                    addMessageBodyPart(primaryMultipart, mailPart, false);
                                }
                            } else {
                                // A regular file-attachment image
                                addMessageBodyPart(primaryMultipart, mail.getEnclosedMailPart(i), false);
                            }
                        } else {
                            final MailPart mailPart = mail.getEnclosedMailPart(i);
                            boolean add = true;
                            if (mailPart.getContentType().startsWith("image/")) {
                                final String contentId = mailPart.getContentId();
                                if (null != contentId) {
                                    // Ignore
                                    add = !MimeMessageUtility.containsContentId(contentId, contentIds);
                                }
                            }
                            if (add) {
                                addMessageBodyPart(primaryMultipart, mailPart, false);
                            }
                        }
                    }
                }
            }
            /*
             * Append VCard
             */
            primaryMultipart = appendVCard(mail, primaryMultipart, mimeMessage);
            /*
             * Finally set multipart
             */
            if (primaryMultipart != null) {
                if (1 == primaryMultipart.getCount()) {
                    final Object cto = primaryMultipart.getBodyPart(0).getContent();
                    if (cto instanceof Multipart) {
                        primaryMultipart = (Multipart) cto;
                    }
                }
                // MessageUtility.setContent(primaryMultipart, mimeMessage);
                mimeMessage.setContent(primaryMultipart);
            }
            return;
        }
        /*
         * Create a non-multipart message
         */
        final ContentType contentType = mail.getContentType();
        if (contentType.startsWith("text/")) {
            final boolean isPlainText = contentType.startsWith(MimeTypes.MIME_TEXT_PLAIN);
            if (contentType.getCharsetParameter() == null) {
                contentType.setCharsetParameter(charset);
            }
            if (primaryMultipart == null) {
                if (isPlainText) {
                    /*
                     * Convert HTML content to regular text (preserving links)
                     */
                    final boolean isHtml;
                    final String text;
                    {
                        final String plainText = textBodyPart.getPlainText();
                        if (null == plainText) {
                            /*-
                             * Check for HTML content
                             *
                             * Well-formed HTML
                             */
                            if (HTMLDetector.containsHTMLTags(content, true)) {
                                isHtml = true;
                                if (BODY_START.matcher(content).find()) {
                                    final String wellFormedHTMLContent = htmlService.getConformHTML(content, charset);
                                    text = wellFormedHTMLContent;
                                } else {
                                    final StringBuilder sb = new StringBuilder(content.length() + 512);
                                    sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
                                    sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
                                    sb.append("<head>\n");
                                    sb.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=").append(charset).append("\" />\n");
                                    sb.append("</head>\n");
                                    sb.append("<body>\n");
                                    sb.append(content);
                                    sb.append("</body>\n");
                                    sb.append("</html>");
                                    text = sb.toString();
                                }
                            } else {
                                isHtml = false;
                                text = content;
                            }
                        } else {
                            isHtml = false;
                            text = plainText;
                        }
                    }
                    /*
                     * Define text content
                     */
                    final String mailText;
                    if (text == null || text.length() == 0) {
                        mailText = "";
                    } else if (isHtml) {
                        mailText = ComposeType.NEW_SMS.equals(type) ? content : performLineFolding(htmlService.html2text(text, true), compositionParameters.getAutoLinebreak());
                    } else {
                        mailText = ComposeType.NEW_SMS.equals(type) ? content : performLineFolding(text, compositionParameters.getAutoLinebreak());
                    }
                    mimeMessage.setDataHandler(new DataHandler(new MessageDataSource(mailText, contentType)));
                    if (Streams.isAscii(mailText.getBytes(contentType.getCharsetParameter()))) {
                        mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
                    }
                } else {
                    final String wellFormedHTMLContent = htmlService.getConformHTML(content, contentType.getCharsetParameter());
                    if (wellFormedHTMLContent == null || wellFormedHTMLContent.length() == 0) {
                        mimeMessage.setDataHandler(new DataHandler(new MessageDataSource(htmlService.getConformHTML(HTML_SPACE, charset).replaceFirst(HTML_SPACE, ""), contentType)));
                    } else {
                        mimeMessage.setDataHandler(new DataHandler(new MessageDataSource(wellFormedHTMLContent, contentType)));
                    }
                }
                // mimeMessage.setContent(mailText, contentType.toString());
                mimeMessage.setHeader(HDR_MIME_VERSION, VERSION_1_0);
                mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType.toString()));
            } else {
                final MimeBodyPart msgBodyPart = new MimeBodyPart();
                String mailText = mail.getContent().toString();
                mimeMessage.setDataHandler(new DataHandler(new MessageDataSource(mailText, contentType)));
                // msgBodyPart.setContent(mail.getContent(), contentType.toString());
                msgBodyPart.setHeader(HDR_MIME_VERSION, VERSION_1_0);
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType.toString()));
                if (isPlainText && Streams.isAscii(mailText.getBytes(contentType.getCharsetParameter()))) {
                    mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
                }
                primaryMultipart.addBodyPart(msgBodyPart);
            }
        } else {
            Multipart mp = null;
            if (primaryMultipart == null) {
                primaryMultipart = mp = new MimeMultipart();
            } else {
                mp = primaryMultipart;
            }
            final MimeBodyPart msgBodyPart = new MimeBodyPart();
            MessageUtility.setText("", charset, msgBodyPart);
            // msgBodyPart.setText("", charset);
            final String disposition = msgBodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
            if (disposition == null) {
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, Part.INLINE);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.INLINE);
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(contentDisposition.toString()));
            }
            mp.addBodyPart(msgBodyPart);
            addMessageBodyPart(mp, mail, true);
        }
        /*
         * if (hasNestedMessages) { if (primaryMultipart == null) { primaryMultipart = new MimeMultipart(); } message/rfc822 final int
         * nestedMsgSize = msgObj.getNestedMsgs().size(); final Iterator<JSONMessageObject> iter = msgObj.getNestedMsgs().iterator(); for
         * (int i = 0; i < nestedMsgSize; i++) { final JSONMessageObject nestedMsgObj = iter.next(); final MimeMessage nestedMsg = new
         * MimeMessage(mailSession); fillMessage(nestedMsgObj, nestedMsg, sendType); final MimeBodyPart msgBodyPart = new MimeBodyPart();
         * msgBodyPart.setContent(nestedMsg, MIME_MESSAGE_RFC822); primaryMultipart.addBodyPart(msgBodyPart); } }
         */
        /*
         * Finally set multipart
         */
        if (primaryMultipart != null) {
            MessageUtility.setContent(primaryMultipart, mimeMessage);
            // mimeMessage.setContent(primaryMultipart);
        }
    }

    private Multipart appendVCard(ComposedMailMessage mail, Multipart primaryMultipart, MimeMessage mimeMessage) throws OXException, MessagingException, UnsupportedEncodingException {
        final String charset = MailProperties.getInstance().getDefaultMimeCharset();
        final String fileName = compositionParameters.getUserVCardFileName();
        AppendVCard: if (mail.isAppendVCard() && fileName != null) {
            final String encodedFileName = MimeUtility.encodeText(fileName, charset, "Q");
            for (int i = 0; i < mail.getEnclosedCount(); i++) {
                final MailPart part = mail.getEnclosedMailPart(i);
                if (encodedFileName.equalsIgnoreCase(part.getFileName())) {
                    /*
                     * VCard already attached in (former draft) message
                     */
                    break AppendVCard;
                }
            }
            try {
                /*
                 * Create a body part for vcard
                 */
                final MimeBodyPart vcardPart = new MimeBodyPart();
                /*
                 * Define content
                 */
                final ContentType ct = new ContentType(MimeTypes.MIME_TEXT_VCARD);
                ct.setCharsetParameter(charset);
                vcardPart.setDataHandler(new DataHandler(new MessageDataSource(compositionParameters.getUserVCard(), ct.toString())));
                if (!ct.containsNameParameter()) {
                    ct.setNameParameter(encodedFileName);
                }
                vcardPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                vcardPart.setHeader(HDR_MIME_VERSION, VERSION_1_0);
                final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                cd.setFilenameParameter(encodedFileName);
                vcardPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));
                /*
                 * Append body part
                 */
                Multipart mp = null == primaryMultipart ? new MimeMultipart() : primaryMultipart;
                mp.addBodyPart(vcardPart);
                if (mail.isDraft()) {
                    mimeMessage.setHeader(MessageHeaders.HDR_X_OX_VCARD, "true");
                }
                return mp;
            } catch (final OXException e) {
                LOG.error(VCARD_ERROR, e);
            }
        }
        return primaryMultipart;
    }

    private boolean isMessage(final MailPart enclosedMailPart) {
        ContentType contentType = enclosedMailPart.getContentType();
        if (contentType.startsWith("message/rfc822")) {
            return true;
        }
        String nameParameter = contentType.getNameParameter();
        return (nameParameter != null && nameParameter.endsWith(".eml"));
    }

    /**
     * Creates a "multipart/alternative" object.
     *
     * @param mail The source composed mail
     * @param mailBody The composed mail's HTML content
     * @param embeddedImages <code>true</code> if specified HTML content contains inline images (an appropriate "multipart/related" object
     *            is going to be created ); otherwise <code>false</code>.
     * @param images
     * @param textBodyPart The text body part
     * @return An appropriate "multipart/alternative" object.
     * @throws OXException If a mail error occurs
     * @throws MessagingException If a messaging error occurs
     */
    protected final Multipart createMultipartAlternative(final ComposedMailMessage mail, final String mailBody, final boolean embeddedImages, final Map<String, SourcedImage> images, final TextBodyMailPart textBodyPart) throws OXException, MessagingException {
        return createMultipartAlternative(mail, mailBody, embeddedImages, images, textBodyPart, null);
    }

    /**
     * Creates a "multipart/alternative" object.
     *
     * @param mail The source composed mail
     * @param mailBody The composed mail's HTML content
     * @param embeddedImages <code>true</code> if specified HTML content contains inline images (an appropriate "multipart/related" object
     *            is going to be created ); otherwise <code>false</code>.
     * @param images
     * @param textBodyPart The text body part
     * @param type The optional compose type
     * @return An appropriate "multipart/alternative" object.
     * @throws OXException If a mail error occurs
     * @throws MessagingException If a messaging error occurs
     */
    protected final Multipart createMultipartAlternative(final ComposedMailMessage mail, final String mailBody, final boolean embeddedImages, final Map<String, SourcedImage> images, final TextBodyMailPart textBodyPart, final ComposeType type) throws OXException, MessagingException {
        /*
         * Create an "alternative" multipart
         */
        final Multipart alternativeMultipart = new MimeMultipart(MP_ALTERNATIVE);
        /*
         * Define html content
         */
        final String charset = MailProperties.getInstance().getDefaultMimeCharset();
        final String htmlContent;
        if (embeddedImages) {
            /*
             * Create "related" multipart
             */
            final Multipart relatedMultipart;
            {
                final String[] arr = new String[1];
                relatedMultipart = createMultipartRelated(mail, mailBody, images, arr);
                htmlContent = arr[0];
            }
            /*
             * Add multipart/related as a body part to superior multipart
             */
            final BodyPart altBodyPart = new MimeBodyPart();
            MessageUtility.setContent(relatedMultipart, altBodyPart);
            // altBodyPart.setContent(relatedMultipart);
            alternativeMultipart.addBodyPart(altBodyPart);
        } else {
            /*
             * Well-formed HTML
             */
            final String wellFormedHTMLContent = htmlService.getConformHTML(mailBody, charset);
            htmlContent = wellFormedHTMLContent;
            final BodyPart html = createHtmlBodyPart(wellFormedHTMLContent, charset);
            /*
             * Add HTML part to superior multipart
             */
            alternativeMultipart.addBodyPart(html);
        }
        /*
         * Define & prepend text content to first index position
         */
        final String plainText = textBodyPart.getPlainText();
        if (null == plainText) {
            alternativeMultipart.addBodyPart(createTextBodyPart(toArray(htmlContent, mailBody), charset, true, true, type), 0);
        } else {
            alternativeMultipart.addBodyPart(createTextBodyPart(toArray(plainText, plainText), charset, true, false, type), 0);
        }
        return alternativeMultipart;
    }

    /**
     * Creates a "multipart/related" object. All inline images are going to be added to returned "multipart/related" object and
     * corresponding HTML content is altered to reference these images through "Content-Id".
     *
     * @param mail The source composed mail
     * @param mailBody The composed mail's HTML content
     * @param images The list of with-source images
     * @param htmlContent An array of {@link String} with length <code>1</code> serving as a container for altered HTML content
     * @return The created "multipart/related" object
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     */
    protected Multipart createMultipartRelated(final ComposedMailMessage mail, final String mailBody, final Map<String, SourcedImage> images, final String[] htmlContent) throws OXException, MessagingException {
        /*
         * Create "related" multipart
         */
        final Multipart relatedMultipart = new MimeMultipart(MP_RELATED);
        /*
         * Well-formed HTML
         */
        final String charset = MailProperties.getInstance().getDefaultMimeCharset();
        final String wellFormedHTMLContent = htmlService.getConformHTML(mailBody, charset);
        /*
         * Check for local images
         */
        htmlContent[0] = processReferencedLocalImages(wellFormedHTMLContent, relatedMultipart, this, mail);
        /*
         * Process referenced local image files and insert returned html content as a new body part to first index
         */
        relatedMultipart.addBodyPart(createHtmlBodyPart(htmlContent[0], charset), 0);
        /*
         * Traverse Content-IDs occurring in original HTML content
         */
        final List<String> cidList = MimeMessageUtility.getContentIDs(wellFormedHTMLContent);
        final StringBuilder tmp = new StringBuilder(32);
        NextImg: for (final String cid : cidList) {
            final BodyPart relatedImageBodyPart;
            final SourcedImage image = images.get(cid);
            if (null == image) {
                /*
                 * Get & remove inline image (to prevent being sent twice)
                 */
                final MailPart imgPart = getAndRemoveImageAttachment(cid, mail);
                if (imgPart == null) {
                    continue NextImg;
                }
                /*
                 * Create new body part from part's data handler
                 */
                relatedImageBodyPart = new MimeBodyPart();
                relatedImageBodyPart.setDataHandler(imgPart.getDataHandler());
                for (final Iterator<Map.Entry<String, String>> iter = imgPart.getHeadersIterator(); iter.hasNext();) {
                    final Map.Entry<String, String> e = iter.next();
                    relatedImageBodyPart.setHeader(e.getKey(), e.getValue());
                }
            } else {
                final DataSource dataSource;
                if ("base64".equalsIgnoreCase(image.getTransferEncoding())) {
                    dataSource = new MessageDataSource(Base64.decodeBase64(Charsets.toAsciiBytes(image.getData())), image.getContentType());
                } else {
                    /*
                     * Expect quoted-printable instead
                     */
                    try {
                        /*
                         * No need to specify a charset in String.getBytes(), quoted-printable is always ASCII
                         */
                        final byte[] bs = QuotedPrintableCodec.decodeQuotedPrintable(image.getData().getBytes());
                        dataSource = new MessageDataSource(bs, image.getContentType());
                    } catch (final DecoderException e) {
                        LOG.warn("Couldn't decode {} image data.", image.getTransferEncoding(), e);
                        continue NextImg;
                    }
                }
                final MimeBodyPart imgBodyPart = new MimeBodyPart();
                imgBodyPart.setDataHandler(new DataHandler(dataSource));
                tmp.setLength(0);
                imgBodyPart.setContentID(tmp.append('<').append(cid).append('>').toString());
                final ContentDisposition contentDisposition = new ContentDisposition(Part.INLINE);
                imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(contentDisposition.toString()));
                final ContentType ct = new ContentType(image.getContentType());
                imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                relatedImageBodyPart = imgBodyPart;
            }
            /*
             * Add image to "related" multipart
             */
            relatedMultipart.addBodyPart(relatedImageBodyPart);
        }
        /*
         * Remember Content-Ids
         */
        contentIds = new HashSet<String>(8);
        final int count = relatedMultipart.getCount();
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = relatedMultipart.getBodyPart(i);
            String[] header = bodyPart.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if (null != header && 0 < header.length && header[0].toLowerCase(Locale.US).startsWith("image/")) {
                header = bodyPart.getHeader(MessageHeaders.HDR_CONTENT_ID);
                if (null != header && 0 < header.length) {
                    contentIds.add(header[0]);
                }
            }
        }
        /*
         * Return multipart/related
         */
        return relatedMultipart;
    }

    private static final String MIME_MESSAGE_RFC822 = MimeTypes.MIME_MESSAGE_RFC822;
    private static final String MIME_APPL_OCTET = MimeTypes.MIME_APPL_OCTET;
    private static final String MIME_MULTIPART_OCTET = MimeTypes.MIME_MULTIPART_OCTET;

    private static volatile Set<String> octetExtensions;
    private static Set<String> octetExtensions() {
        Set<String> tmp = octetExtensions;
        if (null == tmp) {
            synchronized (MimeMessageFiller.class) {
                tmp = octetExtensions;
                if (null == tmp) {
                    String defaultValue = "pgp";
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return new HashSet<String>(Arrays.asList(defaultValue));
                    }
                    String csv = service.getProperty("com.openexchange.mail.octetExtensions", defaultValue);
                    tmp = new HashSet<String>(Arrays.asList(Strings.splitByComma(csv)));
                    octetExtensions = tmp;
                }
            }
        }
        return tmp;
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                octetExtensions = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

    private static String extensionFor(String fileName) {
        if (null == fileName) {
            return null;
        }

        int pos = fileName.lastIndexOf('.');
        return Strings.asciiLowerCase(pos > 0 ? fileName.substring(pos + 1) : fileName);
    }

    protected final void addMessageBodyPart(Multipart mp, MailPart part, boolean inline) throws MessagingException, OXException {
        if (part.getContentType().startsWith(MIME_MESSAGE_RFC822)) {
            // TODO: Works correctly?
            StringBuilder sb = new StringBuilder(32);
            addNestedMessage(part, null, mp, sb);
            return;
        }

        // A non-message attachment
        String fileName = part.getFileName();
        ContentType ct = part.getContentType();
        if (fileName != null && (ct.startsWith(MIME_APPL_OCTET) || ct.startsWith(MIME_MULTIPART_OCTET))) {
            // Only "allowed" for certain files
            if (!octetExtensions().contains(extensionFor(fileName))) {
                // Try to determine MIME type
                String ct2 = MimeType2ExtMap.getContentType(fileName);
                int pos = ct2.indexOf('/');
                ct.setPrimaryType(ct2.substring(0, pos));
                ct.setSubType(ct2.substring(pos + 1));
            }
        }
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(part.getDataHandler());
        if (fileName != null && !ct.containsNameParameter()) {
            ct.setNameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
        if (!inline) {
            // Force base64 encoding to keep data as it is
            messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        }

        // Disposition
        String disposition = messageBodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
        ContentDisposition cd;
        if (disposition == null) {
            cd = new ContentDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        } else {
            cd = new ContentDisposition(disposition);
            cd.setDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        }
        if (fileName != null && !cd.containsFilenameParameter()) {
            cd.setFilenameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));

        // Content-ID
        String contentId = part.getContentId();
        if (contentId != null) {
            if (contentId.charAt(0) == '<') {
                messageBodyPart.setContentID(contentId);
            } else {
                messageBodyPart.setContentID(new StringBuilder(contentId.length() + 2).append('<').append(contentId).append('>').toString());
            }
        }

        // Part identifier
        String partId = part.getFirstHeader(MessageHeaders.HDR_X_PART_ID);
        if (partId == null) {
            messageBodyPart.setHeader(MessageHeaders.HDR_X_PART_ID, UUIDs.getUnformattedStringFromRandom());
            // Add to parental multipart
            mp.addBodyPart(messageBodyPart);
        } else {
            messageBodyPart.setHeader(MessageHeaders.HDR_X_PART_ID, partId);
            // Check if part is already present in new mail
            if (!contains(partId, MessageHeaders.HDR_X_PART_ID, mp)) {
                // Add to parental multipart
                mp.addBodyPart(messageBodyPart);
            }
        }
    }

    private static final boolean contains(String partId, String hdrName, Multipart mp) throws MessagingException {
        int count = mp.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mp.getBodyPart(i);
            if (bodyPart instanceof MimeBodyPart) {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                String header = mimeBodyPart.getHeader(hdrName, null);
                if (header != null) {
                    if (partId.equals(header)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void addNestedMessage(final MailPart mailPart, final Boolean inline, final Multipart primaryMultipart, final StringBuilder sb) throws OXException, MessagingException {
        final ThresholdFileHolder sink = new ThresholdFileHolder(65536, 65536);
        sink.write(mailPart.getInputStream());
        final String fn;
        if (null == mailPart.getFileName()) {
            String subject = MimeMessageUtility.checkNonAscii(new InternetHeaders(sink.getStream()).getHeader(MessageHeaders.HDR_SUBJECT, null));
            if (null == subject || subject.length() == 0) {
                fn = sb.append(PREFIX_PART).append(EXT_EML).toString();
            } else {
                subject = MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.unfold(subject));
                fn = sb.append(subject.replaceAll("\\p{Blank}+", "_")).append(EXT_EML).toString();
                sb.setLength(0);
            }
        } else {
            fn = mailPart.getFileName();
        }
        final boolean bInline = null != inline ? inline.booleanValue() : (Part.INLINE.equalsIgnoreCase(mailPart.getContentDisposition().getDisposition()));
        if (sink.isInMemory()) {
            ByteArrayOutputStream buffer = sink.getBuffer();
            if (null == buffer) {
                addNestedMessage(primaryMultipart, new DataHandler(new FileHolderDataSource(sink, MIME_MESSAGE_RFC822)), fn, bInline);
            } else {
                addNestedMessage(primaryMultipart, new DataHandler(new ByteArrayDataSource(buffer.toByteArray(), MIME_MESSAGE_RFC822)), fn, bInline);
            }
        } else {
            addNestedMessage(primaryMultipart, new DataHandler(new FileHolderDataSource(sink, MIME_MESSAGE_RFC822)), fn, bInline);
        }
    }

    private final void addNestedMessage(final Multipart mp, final DataHandler dataHandler, final String filename, final boolean inline) throws MessagingException, OXException {
        /*
         * Create a body part for original message
         */
        final MimeBodyPart origMsgPart = new MimeBodyPart();
        /*
         * Set data handler
         */
        origMsgPart.setDataHandler(dataHandler);
        /*
         * Set content type
         */
        final ContentType ct = new ContentType(MIME_MESSAGE_RFC822);
        if (null != filename) {
            ct.setNameParameter(filename);
        }
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
        /*
         * Set content disposition
         */
        final String disposition = origMsgPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
        final ContentDisposition cd;
        if (disposition == null) {
            cd = new ContentDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        } else {
            cd = new ContentDisposition(disposition);
            cd.setDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        }
        if (null != filename && !cd.containsFilenameParameter()) {
            cd.setFilenameParameter(filename);
        }
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));
        mp.addBodyPart(origMsgPart);
    }

    /*
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ +++++++++++++++++++++++++ HELPER METHODS +++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    /**
     * Creates a body part of type <code>text/plain</code> from given HTML content
     *
     * @param content The content
     * @param charset The character encoding
     * @param appendHref <code>true</code> to append URLs contained in <i>href</i>s and <i>src</i>s; otherwise <code>false</code>
     * @param isHtml Whether provided content is HTML or not
     * @return A body part of type <code>text/plain</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     */
    // protected final BodyPart createTextBodyPart(final String content, final String charset, final boolean appendHref, final boolean
    // isHtml) throws MessagingException {
    // return createTextBodyPart(content, charset, appendHref, isHtml, null);
    // }

    /**
     * Creates a body part of type <code>text/plain</code> from given HTML content
     *
     * @param contents The contents array
     * @param charset The character encoding
     * @param appendHref <code>true</code> to append URLs contained in <i>href</i>s and <i>src</i>s; otherwise <code>false</code>
     * @param isHtml Whether provided content is HTML or not
     * @param type The compose type
     * @param monitor The monitor
     * @return A body part of type <code>text/plain</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a processing error occurs
     */
    protected final BodyPart createTextBodyPart(final String[] contents, final String charset, final boolean appendHref, final boolean isHtml, final ComposeType type) throws MessagingException {
        /*
         * Convert HTML content to regular text. First: Create a body part for text content
         */
        final MimeBodyPart text = new MimeBodyPart();
        /*
         * Define text content
         */
        final String textContent;
        {
            final String content = contents[0];
            if (content == null || content.length() == 0) {
                textContent = "";
            } else if (isHtml) {
                if (ComposeType.NEW_SMS.equals(type)) {
                    textContent = contents[1];
                } else {
                    textContent = performLineFolding(htmlService.html2text(content, appendHref), compositionParameters.getAutoLinebreak());
                }
            } else {
                if (ComposeType.NEW_SMS.equals(type)) {
                    textContent = content;
                } else {
                    textContent = performLineFolding(content, compositionParameters.getAutoLinebreak());
                }
            }
        }
        MessageUtility.setText(textContent, charset, text);
        // text.setText(textContent, charset);
        // text.setText(performLineFolding(getConverter().convertWithQuotes(
        // htmlContent), false, usm.getAutoLinebreak()),
        // MailConfig.getDefaultMimeCharset());
        text.setHeader(HDR_MIME_VERSION, VERSION_1_0);
        text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new StringBuilder("text/plain; charset=").append(charset).toString());
        try {
            if (Streams.isAscii(textContent.getBytes(charset))) {
                text.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "7bit");
            }
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported character encoding: " + charset, e);
        }
        return text;
    }

    private static final String HTML_SPACE = "&#160;";

    /**
     * Creates a body part of type <code>text/html</code> from given HTML content
     *
     * @param wellFormedHTMLContent The well-formed HTML content
     * @param charset The charset
     * @param monitor The monitor
     * @return A body part of type <code>text/html</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a processing error occurs
     */
    protected final BodyPart createHtmlBodyPart(final String wellFormedHTMLContent, final String charset) throws MessagingException, OXException {
        try {
            final String contentType = new StringBuilder("text/html; charset=").append(charset).toString();
            final MimeBodyPart html = new MimeBodyPart();
            if (wellFormedHTMLContent == null || wellFormedHTMLContent.length() == 0) {
                html.setDataHandler(new DataHandler(new MessageDataSource(
                    htmlService.getConformHTML(HTML_SPACE, charset).replaceFirst(HTML_SPACE, ""),
                    contentType)));
                // html.setContent(htmlService.getConformHTML(HTML_SPACE, charset).replaceFirst(HTML_SPACE, ""), contentType);
            } else {
                html.setDataHandler(new DataHandler(new MessageDataSource(wellFormedHTMLContent, contentType)));
                // html.setContent(wellFormedHTMLContent, contentType);
            }
            html.setHeader(HDR_MIME_VERSION, VERSION_1_0);
            html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);
            return html;
        } catch (final UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported encoding.", e);
        }
    }

    private static final Pattern PATTERN_IMG = Pattern.compile("<img[^>]*/?>", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_IMG_ALT = Pattern.compile("alt=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    /**
     * Drops referenced local images in passed HTML content.
     *
     * @param htmlContent The HTML content whose &lt;img&gt; tags must be dropped
     * @param considerAlt Whether the <code>"alt"</code> attribute of look-up &lt;img&gt; tag shall be maintained if present
     * @return the replaced HTML content
     */
    protected final static String dropImages(final String htmlContent, final boolean considerAlt) {
        final Matcher m = PATTERN_IMG.matcher(htmlContent);
        if (!m.find()) {
            return htmlContent;
        }
        final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
        final StringBuilder sb = new StringBuilder(htmlContent.length());
        if (considerAlt) {
            Matcher tmp = null;
            do {
                final String alternative;
                {
                    tmp = PATTERN_IMG_ALT.matcher(m.group());
                    alternative = tmp.find() ? tmp.group(1) : null;
                }
                mr.appendLiteralReplacement(sb, null == alternative ? "" : " " + alternative + " ");
            } while (m.find());
        } else {
            do {
                mr.appendLiteralReplacement(sb, "");
            } while (m.find());
        }
        mr.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_SRC = MimeMessageUtility.PATTERN_SRC;
    private static final Pattern PATTERN_AMP = Pattern.compile(Pattern.quote("&amp;"));

    private static String blankSrc(final String imageTag) {
        return MimeMessageUtility.blankSrc(imageTag);
    }

    private static final String VERSION_NAME = Version.NAME;

    /**
     * Processes referenced local images, inserts them as inlined HTML images and adds their binary data to parental instance of <code>
     * {@link Multipart}</code>.
     *
     * @param htmlContent The HTML content whose &lt;img&gt; tags must be replaced with real content IDs
     * @param mp The parental instance of <code>{@link Multipart}</code>
     * @param msgFiller The message filler
     * @param mail The associated mail
     * @return The replaced HTML content
     * @throws MessagingException If appending as body part fails
     * @throws OXException If a mail error occurs
     */
    protected final String processReferencedLocalImages(String htmlContent, Multipart mp, MimeMessageFiller msgFiller, ComposedMailMessage mail) throws MessagingException, OXException {
        if (isEmpty(htmlContent)) {
            return htmlContent;
        }
        final ImageMatcher m = ImageMatcher.matcher(htmlContent);
        final StringBuffer sb = new StringBuffer(htmlContent.length());
        if (m.find()) {
            final Set<String> uploadFileIDs = msgFiller.uploadFileIDs = new HashSet<String>(4);
            final Set<String> trackedIds = new HashSet<String>(4);
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
            do {
                final String imageTag = m.group();
                if (MimeMessageUtility.isValidImageUri(imageTag)) {
                    final String id = m.getManagedFileId();
                    ImageProvider imageProvider;
                    if (null != id) {
                        if (mfm.contains(id)) {
                            try {
                                imageProvider = new ManagedFileImageProvider(mfm.getByID(id));
                            } catch (final OXException e) {
                                LOG.warn("Image with id \"{}\" could not be loaded. Referenced image is skipped.", id, e);
                                // Anyway, replace image tag
                                m.appendLiteralReplacement(sb, blankSrc(imageTag));
                                continue;
                            }
                        } else {
                            // "ajax/file?..." but no matching file, check in referenced ones
                            int size = mail.getEnclosedCount();
                            if (size <= 0) {
                                LOG.warn("Image with id \"{}\" could not be loaded. Referenced image is skipped.", id);
                                // Anyway, replace image tag
                                m.appendLiteralReplacement(sb, blankSrc(imageTag));
                                continue;
                            }
                            ImageProvider tmp = null;
                            String prefix = "<" + UUIDs.getUnformattedString(UUID.fromString(id));
                            for (int i = size; null == tmp && i-- > 0;) {
                                MailPart part = mail.getEnclosedMailPart(i);
                                if (ComposedPartType.REFERENCE.equals(((ComposedMailPart) part).getType())) {
                                    String contentId = part.getContentId();
                                    if (null != contentId && contentId.startsWith(prefix, 0)) {
                                        tmp = new ReferencedPartImageProvider(part);
                                        mail.removeEnclosedPart(i);
                                    }
                                }
                            }
                            if (null == tmp) {
                                LOG.warn("Image with id \"{}\" could not be loaded. Referenced image is skipped.", id);
                                // Anyway, replace image tag
                                m.appendLiteralReplacement(sb, blankSrc(imageTag));
                                continue;
                            }
                            imageProvider = tmp;
                        }
                    } else {
                        final ImageLocation imageLocation;
                        String blankImageTag = null;
                        {
                            final Matcher srcMatcher = PATTERN_SRC.matcher(imageTag);
                            if (srcMatcher.find()) {
                                ImageLocation il;
                                try {
                                    il = ImageUtility.parseImageLocationFrom(PATTERN_AMP.matcher(srcMatcher.group(1)).replaceAll("&"));
                                } catch (final IllegalArgumentException e) {
                                    final StringBuffer bblankImageTag = new StringBuffer(imageTag.length());
                                    srcMatcher.appendReplacement(bblankImageTag, "");
                                    srcMatcher.appendTail(bblankImageTag);
                                    blankImageTag = bblankImageTag.toString();
                                    il = null;
                                }
                                imageLocation = il;
                            } else {
                                ImageLocation il;
                                try {
                                    il = ImageUtility.parseImageLocationFrom(imageTag);
                                } catch (final IllegalArgumentException e) {
                                    il = null;
                                }
                                imageLocation = il;
                            }
                        }
                        if (null == imageLocation) {
                            LOG.warn("Image with id \"{}\" could not be loaded. Referenced image is skipped.", m.getImageId());
                            /*
                             * Anyway, replace image tag
                             */
                            m.appendLiteralReplacement(sb, null == blankImageTag ? blankSrc(imageTag) : blankImageTag);
                            continue;
                        }
                        final ImageDataSource dataSource = (ImageDataSource) conversionService.getDataSource(imageLocation.getRegistrationName());
                        if (null == dataSource) {
                            LOG.warn("No image data source found with id \"{}\". Referenced image is skipped.", imageLocation.getRegistrationName());
                            /*
                             * Anyway, replace image tag
                             */
                            m.appendLiteralReplacement(sb, blankSrc(imageTag));
                            continue;
                        }
                        try {
                            imageProvider = compositionParameters.createImageProvider(dataSource, imageLocation);
                        } catch (final OXException e) {
                            imageProvider = null;
                            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                                // Do look-up by identifier
                                String contentId = new StringBuilder(48).append('<').append(imageLocation.getImageId()).append('>').toString();

                                MailPart imagePart = null;
                                int size = mail.getEnclosedCount();
                                for (int i = 0; null == imagePart && i < size; i++) {
                                    MailPart part = mail.getEnclosedMailPart(i);
                                    if (ComposedPartType.REFERENCE.equals(((ComposedMailPart) part).getType()) && contentId.equals(part.getContentId())) {
                                        imagePart = part;
                                    }
                                }

                                if (null != imagePart) {
                                    imageProvider = new ReferencedPartImageProvider(imagePart);
                                }
                            }
                            if (null == imageProvider) {
                                if (isIgnorableException(e)) {
                                    m.appendLiteralReplacement(sb, blankSrc(imageTag));
                                    continue;
                                }
                                throw e;
                            }
                        } catch (final RuntimeException rte) {
                            LOG.warn("Couldn't load image data", rte);
                            m.appendLiteralReplacement(sb, blankSrc(imageTag));
                            continue;
                        }
                    }
                    final String iid;
                    if (null == id) {
                        iid = urlDecode(m.getImageId());
                    } else {
                        /*
                         * Remember id to avoid duplicate attachment and for later cleanup
                         */
                        uploadFileIDs.add(id);
                        iid = id;
                    }
                    final boolean appendBodyPart = trackedIds.add(iid);
                    /*
                     * Replace "src" attribute
                     */
                    String iTag = imageTag.replaceFirst(
                        "(?i)src=\"[^\"]*\"",
                        com.openexchange.java.Strings.quoteReplacement("src=\"cid:" + processLocalImage(imageProvider, iid, appendBodyPart, mp) + "\""));
                    iTag = iTag.replaceFirst("(?i)id=\"[^\"]*@" + VERSION_NAME + "\"", "");
                    m.appendLiteralReplacement(sb, iTag);
                } else {
                    /*
                     * Re-append as-is
                     */
                    m.appendLiteralReplacement(sb, imageTag);
                }
            } while (m.find());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean isIgnorableException(OXException e) {
        if (MimeMailExceptionCode.IMAGE_ATTACHMENTS_UNSUPPORTED.equals(e) || MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND.equals(e) || DataExceptionCodes.ERROR.equals(e) || MailExceptionCode.MAIL_NOT_FOUND.equals(e) || MailExceptionCode.ATTACHMENT_NOT_FOUND.equals(e) || isFolderNotFound(e)) {
            return true;
        }
        return false;
    }

    private static String urlDecode(String s) {
        return MimeMessageUtility.urlDecode(s);
    }

    private static final Pattern PATTERN_DASHES = Pattern.compile("-+");

    /**
     * Processes a local image and returns its content id
     *
     * @param imageProvider The uploaded file
     * @param id uploaded file's ID
     * @param appendBodyPart
     * @param tmp An instance of {@link StringBuilder}
     * @param mp The parental instance of {@link Multipart}
     * @return the content id
     * @throws MessagingException If appending as body part fails
     * @throws OXException If a mail error occurs
     */
    private final static String processLocalImage(final ImageProvider imageProvider, final String id, final boolean appendBodyPart, final Multipart mp) throws MessagingException, OXException {
        /*
         * Determine filename
         */
        String fileName = imageProvider.getFileName();
        if (null == fileName) {
            /*
             * Generate dummy file name
             */
            final List<String> exts = MimeType2ExtMap.getFileExtensions(imageProvider.getContentType().toLowerCase(Locale.ENGLISH));
            final StringBuilder sb = new StringBuilder("image.");
            if (exts == null) {
                sb.append("dat");
            } else {
                sb.append(exts.get(0));
            }
            fileName = sb.toString();
        } else {
            /*
             * Encode image's file name for being mail-safe
             */
            try {
                fileName = MimeUtility.encodeText(fileName, MailProperties.getInstance().getDefaultMimeCharset(), "Q");
            } catch (final UnsupportedEncodingException e) {
                fileName = imageProvider.getFileName();
            }
        }
        /*
         * ... and cid
         */
        final StringBuilder tmp = new StringBuilder(32);
        final String cid;
        {
            if (imageProvider.isLocalFile()) {
                tmp.setLength(0);
                tmp.append(PATTERN_DASHES.matcher(id).replaceAll(""));
                tmp.append('@').append(Version.NAME);
                cid = tmp.toString();
            } else {
                cid = id;
            }
        }
        if (appendBodyPart) {
            boolean found = false;
            {
                final Set<String> set = new HashSet<String>(2);
                final int count = mp.getCount();
                for (int i = 0; !found && i < count; i++) {
                    final BodyPart bodyPart = mp.getBodyPart(i);
                    final String[] header = bodyPart.getHeader(MessageHeaders.HDR_CONTENT_ID);
                    if (null != header && 0 < header.length) {
                        set.clear();
                        set.addAll(Arrays.asList(header));
                        found = set.contains(cid);
                    }
                }
            }
            /*
             * Append body part if not found
             */
            if (!found) {
                final MimeBodyPart imgBodyPart = new MimeBodyPart();
                imgBodyPart.setDataHandler(new DataHandler(imageProvider.getDataSource()));
                tmp.setLength(0);
                imgBodyPart.setContentID(tmp.append('<').append(cid).append('>').toString());
                final ContentDisposition contentDisposition = new ContentDisposition(Part.INLINE);
                if (fileName != null) {
                    contentDisposition.setFilenameParameter(fileName);
                }
                imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(contentDisposition.toString()));
                final ContentType ct = new ContentType(imageProvider.getContentType());
                if (fileName != null && !ct.containsNameParameter()) {
                    ct.setNameParameter(fileName);
                }
                imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                mp.addBodyPart(imgBodyPart);
            }
        }
        return cid;
    }

    /**
     * Gets and removes the image attachment from specified mail whose <code>Content-Id</code> matches given <code>cid</code> argument
     *
     * @param cid The <code>Content-Id</code> of the image attachment
     * @param mail The mail containing the image attachment
     * @return The removed image attachment
     * @throws OXException If a mail error occurs
     */
    protected final static MailPart getAndRemoveImageAttachment(final String cid, final ComposedMailMessage mail) throws OXException {
        final int size = mail.getEnclosedCount();
        for (int i = 0; i < size; i++) {
            final MailPart enclosedPart = mail.getEnclosedMailPart(i);
            if (enclosedPart.containsContentId() && MimeMessageUtility.equalsCID(cid, enclosedPart.getContentId())) {
                return mail.removeEnclosedPart(i);
            }
        }
        return null;
    }

    private static final boolean hasOnlyReferencedMailAttachments(final ComposedMailMessage mail, final int size) throws OXException {
        for (int i = 0; i < size; i++) {
            final MailPart part = mail.getEnclosedMailPart(i);
            if (!ComposedPartType.REFERENCE.equals(((ComposedMailPart) part).getType()) || !((ReferencedMailPart) part).isMail()) {
                return false;
            }
        }
        return true;
    }

    private static String[] toArray(final String... contents) {
        if (null == contents) {
            return new String[0];
        }
        final int length = contents.length;
        if (0 == length) {
            return new String[0];
        }
        final String[] ret = new String[length];
        System.arraycopy(contents, 0, ret, 0, length);
        return ret;
    }

    public static interface ImageProvider {

        public boolean isLocalFile();

        public String getFileName();

        public DataSource getDataSource() throws OXException;

        public String getContentType();
    } // End of ImageProvider

    private static class ReferencedPartImageProvider implements ImageProvider {

        private final MailPart mailPart;

        public ReferencedPartImageProvider(MailPart mailPart) {
            super();
            this.mailPart = mailPart;
        }

        @Override
        public boolean isLocalFile() {
            return false;
        }

        @Override
        public String getContentType() {
            return mailPart.getContentType().toString();
        }

        @Override
        public DataSource getDataSource() throws OXException {
            return mailPart.getDataHandler().getDataSource();
        }

        @Override
        public String getFileName() {
            return mailPart.getFileName();
        }
    } // End of ReferencedPartImageProvider

    private static class ManagedFileImageProvider implements ImageProvider {

        private final ManagedFile managedFile;

        public ManagedFileImageProvider(ManagedFile managedFile) {
            super();
            this.managedFile = managedFile;
        }

        @Override
        public boolean isLocalFile() {
            return true;
        }

        @Override
        public String getContentType() {
            return managedFile.getContentType();
        }

        @Override
        public DataSource getDataSource() throws OXException {
            return new FileDataSource(managedFile.getFile());
        }

        @Override
        public String getFileName() {
            return managedFile.getFileName();
        }
    } // End of ManagedFileImageProvider

    static class ImageDataImageProvider implements ImageProvider {

        private final Data<InputStream> data;

        private final String contentType;

        private final String fileName;

        public ImageDataImageProvider(ImageDataSource imageData, ImageLocation imageLocation, Session session) throws OXException {
            super();
            this.data = imageData.getData(InputStream.class, imageData.generateDataArgumentsFrom(imageLocation), session);
            final DataProperties dataProperties = data.getDataProperties();
            fileName = dataProperties.get(DataProperties.PROPERTY_NAME);
            String contentType = dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE);
            if (null != contentType) {
                final String lcct = toLowerCase(contentType).trim();
                final String defaultContentType = "application/octet-stream";
                if (!lcct.startsWith("image/") && !lcct.startsWith(defaultContentType)) {
                    throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(imageLocation.getImageId(), imageLocation.getId(), imageLocation.getFolder());
                }
                if (lcct.startsWith(defaultContentType) && !Strings.isEmpty(fileName)) {
                    final String contentTypeByFileName = MimeType2ExtMap.getContentType(fileName);
                    if (!defaultContentType.equals(contentTypeByFileName)) {
                        contentType = contentTypeByFileName + (lcct.length() > defaultContentType.length() ? lcct.substring(defaultContentType.length()) : "");
                    }
                }
            }
            this.contentType = contentType;
        }

        @Override
        public boolean isLocalFile() {
            return false;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public DataSource getDataSource() throws OXException {
            try {
                return new MessageDataSource(data.getData(), contentType);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }

        @Override
        public String getFileName() {
            return fileName;
        }
    } // End of ImageDataImageProvider

    private static boolean isFolderNotFound(final OXException e) {
        if (null == e) {
            return false;
        }
        return ((MimeMailExceptionCode.FOLDER_NOT_FOUND.equals(e)) || (MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES.equals(e)) || ("IMAP".equals(e.getPrefix()) && (MimeMailExceptionCode.FOLDER_NOT_FOUND.getNumber() == e.getCode())));
    }

}
