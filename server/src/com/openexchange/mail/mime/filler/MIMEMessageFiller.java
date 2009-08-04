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

package com.openexchange.mail.mime.filler;

import static com.openexchange.mail.text.HTMLProcessing.getConformHTML;
import static com.openexchange.mail.text.TextProcessing.performLineFolding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import com.openexchange.api2.OXException;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataProperties;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.image.ImageService;
import com.openexchange.image.internal.ImageData;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.text.parser.HTMLParser;
import com.openexchange.mail.text.parser.handler.HTML2TextHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.Version;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link MIMEMessageFiller} - Provides basic methods to fills an instance of {@link MimeMessage} with headers/contents given through an
 * instance of {@link ComposedMailMessage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MIMEMessageFiller {

    private static final String PREFIX_PART = "part";

    private static final String EXT_EML = ".eml";

    private static final int BUF_SIZE = 0x2000;

    private static final String VERSION_1_0 = "1.0";

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEMessageFiller.class);

    private static final String VCARD_ERROR = "Error while appending user VCard";

    /*
     * Constants for Multipart types
     */
    private static final String MP_ALTERNATIVE = "alternative";

    private static final String MP_RELATED = "related";

    /*
     * Patterns for common MIME text types
     */
    private static final String REPLACE_CS = "#CS#";

    private static final String PAT_TEXT_CT = "text/plain; charset=#CS#";

    private static final String PAT_HTML_CT = "text/html; charset=#CS#";

    /*
     * Fields
     */
    protected final Session session;

    protected final Context ctx;

    protected final UserSettingMail usm;

    private Set<String> uploadFileIDs;

    // private Html2TextConverter converter;

    private HTML2TextHandler html2textHandler;

    /**
     * Initializes a new {@link MIMEMessageFiller}
     * 
     * @param session The session providing user data
     * @param ctx The context
     */
    public MIMEMessageFiller(final Session session, final Context ctx) {
        this(session, ctx, null);
    }

    /**
     * Initializes a new {@link MIMEMessageFiller}
     * 
     * @param session The session providing user data
     * @param ctx The context
     * @param usm The user's mail settings
     */
    public MIMEMessageFiller(final Session session, final Context ctx, final UserSettingMail usm) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.usm = usm == null ? UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx) : usm;
    }

    /*
     * protected final Html2TextConverter getConverter() { if (converter == null) { converter = new Html2TextConverter(); } return
     * converter; }
     */

    protected final HTML2TextHandler getHTML2TextHandler() {
        if (html2textHandler == null) {
            html2textHandler = new HTML2TextHandler(4096, true);
            html2textHandler.setContextId(session.getContextId());
            html2textHandler.setUserId(session.getUserId());
        }
        return html2textHandler;
    }

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
                    } catch (final ManagedFileException e) {
                        LOG.error(e.getMessage(), e);
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
    public void setCommonHeaders(final MimeMessage mimeMessage) throws MessagingException {
        /*
         * Set mailer
         */
        mimeMessage.setHeader(MessageHeaders.HDR_X_MAILER, "Open-Xchange Mailer v" + Version.getVersionString());
        /*
         * Set organization to context-admin's company field setting
         */
        final Object org = session.getParameter(MailSessionParameterNames.PARAM_ORGANIZATION_HDR);
        if (null == org) {
            /*
             * Get context's admin contact object
             */
            try {
                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                    ContactInterfaceDiscoveryService.class).newContactInterface(FolderObject.SYSTEM_LDAP_FOLDER_ID, session);

                final Contact c = contactInterface.getUserById(ctx.getMailadmin());
                if (null != c && c.getCompany() != null && c.getCompany().length() > 0) {
                    final String encoded = MimeUtility.fold(14, MimeUtility.encodeText(
                        c.getCompany(),
                        MailProperties.getInstance().getDefaultMimeCharset(),
                        null));
                    session.setParameter(MailSessionParameterNames.PARAM_ORGANIZATION_HDR, encoded);
                    mimeMessage.setHeader(MessageHeaders.HDR_ORGANIZATION, encoded);
                } else {
                    session.setParameter(MessageHeaders.HDR_ORGANIZATION, "=?null?=");
                }
            } catch (final Exception e) {
                LOG.error("Header \"Organization\" could not be set", e);
                session.setParameter(MessageHeaders.HDR_ORGANIZATION, "=?null?=");
            }
        } else if (!"=?null?=".equals(org.toString())) {
            /*
             * Apply value from session parameter
             */
            mimeMessage.setHeader(MessageHeaders.HDR_ORGANIZATION, org.toString());
        }
    }

    private static final String[] SUPPRESS_HEADERS = {
        MessageHeaders.HDR_X_OX_VCARD, MessageHeaders.HDR_X_OXMSGREF, MessageHeaders.HDR_X_OX_MARKER, MessageHeaders.HDR_X_OX_NOTIFICATION };

    /**
     * Sets necessary headers in specified MIME message: <code>From</code>/ <code>Sender</code>, <code>To</code>, <code>Cc</code>,
     * <code>Bcc</code>, <code>Reply-To</code>, <code>Subject</code>, etc.
     * 
     * @param mail The composed mail
     * @param mimeMessage The MIME message
     * @throws MessagingException If headers cannot be set
     * @throws MailException If a mail error occurs
     */
    public void setMessageHeaders(final ComposedMailMessage mail, final MimeMessage mimeMessage) throws MessagingException, MailException {
        /*
         * Set from/sender
         */
        if (mail.containsFrom()) {
            InternetAddress sender = null;
            if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                try {
                    sender = new QuotedInternetAddress(usm.getSendAddr(), true);
                } catch (final AddressException e) {
                    LOG.error("Default send address cannot be parsed", e);
                }
            }
            final InternetAddress from = mail.getFrom()[0];
            mimeMessage.setFrom(from);
            /*
             * Taken from RFC 822 section 4.4.2: In particular, the "Sender" field MUST be present if it is NOT the same as the "From"
             * Field.
             */
            if (sender != null && !from.equals(sender)) {
                mimeMessage.setSender(sender);
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
        if (usm.getReplyToAddr() == null || usm.getReplyToAddr().length() == 0) {
            if (mail.containsFrom()) {
                mimeMessage.setReplyTo(mail.getFrom());
            }
        } else {
            try {
                mimeMessage.setReplyTo(QuotedInternetAddress.parse(usm.getReplyToAddr(), true));
            } catch (final AddressException e) {
                LOG.error("Default Reply-To address cannot be parsed", e);
                try {
                    mimeMessage.setHeader(MessageHeaders.HDR_REPLY_TO, MimeUtility.encodeWord(
                        usm.getReplyToAddr(),
                        MailProperties.getInstance().getDefaultMimeCharset(),
                        "Q"));
                } catch (final UnsupportedEncodingException e1) {
                    /*
                     * Cannot occur since default mime charset is supported by JVM
                     */
                    LOG.error(e1.getMessage(), e1);
                }
            }
        }
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
            final MailDateFormat mdf = MIMEMessageUtility.getMailDateFormat(session);
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
        mimeMessage.setHeader(MessageHeaders.HDR_X_PRIORITY, String.valueOf(mail.getPriority()));
        /*
         * Headers
         */
        for (final Iterator<Map.Entry<String, String>> iter = mail.getNonMatchingHeaders(SUPPRESS_HEADERS); iter.hasNext();) {
            final Map.Entry<String, String> entry = iter.next();
            mimeMessage.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the appropriate headers <code>In-Reply-To</code> and <code>References</code> in specified MIME message.
     * <p>
     * Moreover the <code>Reply-To</code> header is set.
     * 
     * @param referencedMail The referenced mail
     * @param mimeMessage The MIME message
     * @throws MessagingException If setting the reply headers fails
     */
    public void setReplyHeaders(final MailMessage referencedMail, final MimeMessage mimeMessage) throws MessagingException {
        if (null == referencedMail) {
            /*
             * Obviously referenced mail does no more exist; cancel setting reply headers Message-Id, In-Reply-To, and References.
             */
            return;
        }
        final String pMsgId = referencedMail.getFirstHeader(MessageHeaders.HDR_MESSAGE_ID);
        if (pMsgId != null) {
            mimeMessage.setHeader(MessageHeaders.HDR_IN_REPLY_TO, pMsgId);
        }
        /*
         * Set References header field
         */
        final String pReferences = referencedMail.getFirstHeader(MessageHeaders.HDR_REFERENCES);
        final String pInReplyTo = referencedMail.getFirstHeader(MessageHeaders.HDR_IN_REPLY_TO);
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
        }
        if (refBuilder.length() > 0) {
            /*
             * If the parent has none of the "References:", "In-Reply-To:", or "Message-ID:" fields, then the new message will have no
             * "References:" field.
             */
            mimeMessage.setHeader(MessageHeaders.HDR_REFERENCES, refBuilder.toString());
        }
    }

    /**
     * Sets the appropriate headers before message's transport: <code>Reply-To</code>, <code>Date</code>, and <code>Subject</code>
     * 
     * @param mail The source mail
     * @param mimeMessage The MIME message
     * @throws MailException If a mail error occurs
     */
    public void setSendHeaders(final ComposedMailMessage mail, final MimeMessage mimeMessage) throws MailException {
        try {
            /*
             * Set the Reply-To header for future replies to this new message
             */
            final InternetAddress[] ia;
            if (usm.getReplyToAddr() == null) {
                ia = mail.getFrom();
            } else {
                ia = MIMEMessageUtility.parseAddressList(usm.getReplyToAddr(), false);
            }
            mimeMessage.setReplyTo(ia);
            /*
             * Set sent date if not done, yet
             */
            final Date sentDate = mimeMessage.getSentDate();
            if (sentDate == null) {
                final MailDateFormat mdf = MIMEMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    mimeMessage.setHeader("Date", mdf.format(new Date()));
                }
            } else {
                // Ensure proper time zone
                final MailDateFormat mdf = MIMEMessageUtility.getMailDateFormat(session);
                synchronized (mdf) {
                    mimeMessage.setHeader("Date", mdf.format(sentDate));
                }
            }
            /*
             * Set default subject if none set
             */
            final String subject;
            if ((subject = mimeMessage.getSubject()) == null || subject.length() == 0) {
                mimeMessage.setSubject(new StringHelper(UserStorage.getStorageUser(session.getUserId(), ctx).getLocale()).getString(MailStrings.DEFAULT_SUBJECT));
            }
        } catch (final AddressException e) {
            throw MIMEMailException.handleMessagingException(e);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Fills the body of given instance of {@link MimeMessage} with the contents specified through given instance of
     * {@link ComposedMailMessage}.
     * 
     * @param mail The source composed mail
     * @param mimeMessage The MIME message to fill
     * @param type The compose type
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public void fillMailBody(final ComposedMailMessage mail, final MimeMessage mimeMessage, final ComposeType type) throws MessagingException, MailException, IOException {
        /*
         * Store some flags
         */
        final boolean hasNestedMessages = false;
        final boolean hasAttachments;
        final boolean isAttachmentForward;
        {
            final int size = mail.getEnclosedCount();
            hasAttachments = size > 0;
            /*
             * A non-inline forward message
             */
            isAttachmentForward = ((ComposeType.FORWARD.equals(type)) && (usm.isForwardAsAttachment() || (size > 1 && hasOnlyReferencedMailAttachments(
                mail,
                size))));
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
            if (mail.getContentType().isMimeType(MIMETypes.MIME_MULTIPART_ALTERNATIVE)) {
                /*
                 * Allow only HTML if a draft message should be "sent"
                 */
                mail.setContentType(MIMETypes.MIME_TEXT_HTML);
            }
        } else {
            sendMultipartAlternative = mail.getContentType().isMimeType(MIMETypes.MIME_MULTIPART_ALTERNATIVE);
        }
        /*
         * HTML content with embedded images
         */
        final String content = (String) mail.getContent();
        final boolean embeddedImages;
        if (sendMultipartAlternative || mail.getContentType().isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
            embeddedImages = MIMEMessageUtility.hasEmbeddedImages(content) || MIMEMessageUtility.hasReferencedLocalImages(content, session);
        } else {
            embeddedImages = false;
        }
        /*
         * Compose message
         */
        if (hasAttachments || sendMultipartAlternative || isAttachmentForward || mail.isAppendVCard() || embeddedImages) {
            /*
             * If any condition is true, we ought to create a multipart/ message
             */
            if (sendMultipartAlternative) {
                final Multipart alternativeMultipart = createMultipartAlternative(mail, content, embeddedImages);
                if (primaryMultipart == null) {
                    primaryMultipart = alternativeMultipart;
                } else {
                    final BodyPart bodyPart = new MimeBodyPart();
                    bodyPart.setContent(alternativeMultipart);
                    primaryMultipart.addBodyPart(bodyPart);
                }
            } else if (embeddedImages) {
                final Multipart relatedMultipart = createMultipartRelated(mail, content, new String[1]);
                if (primaryMultipart == null) {
                    primaryMultipart = relatedMultipart;
                } else {
                    final BodyPart bodyPart = new MimeBodyPart();
                    bodyPart.setContent(relatedMultipart);
                    primaryMultipart.addBodyPart(bodyPart);
                }
            } else {
                if (primaryMultipart == null) {
                    primaryMultipart = new MimeMultipart();
                }
                /*
                 * Convert html content to regular text if mail text is demanded to be text/plain
                 */
                if (mail.getContentType().isMimeType(MIMETypes.MIME_TEXT_PLAIN)) {
                    /*
                     * Append text content
                     */
                    primaryMultipart.addBodyPart(createTextBodyPart(content));
                } else {
                    /*
                     * Append html content
                     */
                    primaryMultipart.addBodyPart(createHtmlBodyPart(content));
                }
            }
            /*
             * Get number of enclosed parts
             */
            final int size = mail.getEnclosedCount();
            if (size > 0) {
                if (isAttachmentForward) {
                    /*
                     * Add referenced mail(s)
                     */
                    final StringBuilder sb = new StringBuilder(32);
                    final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(BUF_SIZE);
                    final byte[] bbuf = new byte[BUF_SIZE];
                    for (int i = 0; i < size; i++) {
                        addNestedMessage(mail.getEnclosedMailPart(i), primaryMultipart, sb, out, bbuf);
                    }
                } else {
                    /*
                     * Add referenced parts from ONE referenced mail
                     */
                    for (int i = 0; i < size; i++) {
                        addMessageBodyPart(primaryMultipart, mail.getEnclosedMailPart(i), false);
                    }
                }
            }
            /*
             * Append VCard
             */
            AppendVCard: if (mail.isAppendVCard()) {
                final String fileName = MimeUtility.encodeText(
                    new StringBuilder(UserStorage.getStorageUser(session.getUserId(), ctx).getDisplayName().replaceAll(" +", "")).append(
                        ".vcf").toString(),
                    MailProperties.getInstance().getDefaultMimeCharset(),
                    "Q");
                for (int i = 0; i < size; i++) {
                    final MailPart part = mail.getEnclosedMailPart(i);
                    if (fileName.equalsIgnoreCase(part.getFileName())) {
                        /*
                         * VCard already attached in (former draft) message
                         */
                        break AppendVCard;
                    }
                }
                if (primaryMultipart == null) {
                    primaryMultipart = new MimeMultipart();
                }
                try {
                    final String userVCard = getUserVCard(MailProperties.getInstance().getDefaultMimeCharset());
                    /*
                     * Create a body part for vcard
                     */
                    final MimeBodyPart vcardPart = new MimeBodyPart();
                    /*
                     * Define content
                     */
                    final ContentType ct = new ContentType(MIMETypes.MIME_TEXT_X_VCARD);
                    ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    vcardPart.setDataHandler(new DataHandler(new MessageDataSource(userVCard, ct)));
                    if (fileName != null && !ct.containsNameParameter()) {
                        ct.setNameParameter(fileName);
                    }
                    vcardPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, ct.toString()));
                    vcardPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
                    if (fileName != null) {
                        final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                        cd.setFilenameParameter(fileName);
                        vcardPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, cd.toString()));
                    }
                    /*
                     * Append body part
                     */
                    primaryMultipart.addBodyPart(vcardPart);
                    if (mail.isDraft()) {
                        mimeMessage.setHeader(MessageHeaders.HDR_X_OX_VCARD, "true");
                    }
                } catch (final MailException e) {
                    LOG.error(VCARD_ERROR, e);
                }
            }
            /*
             * Attach forwarded messages
             */
            // if (isAttachmentForward) {
            // if (primaryMultipart == null) {
            // primaryMultipart = new MimeMultipart();
            // }
            // final int count = mail.getEnclosedCount();
            // final MailMessage[] refMails = mail.getReferencedMails();
            // final StringBuilder sb = new StringBuilder(32);
            // final ByteArrayOutputStream out = new
            // UnsynchronizedByteArrayOutputStream();
            // for (final MailMessage refMail : refMails) {
            // out.reset();
            // sb.setLength(0);
            // refMail.writeTo(out);
            // addNestedMessage(primaryMultipart, new DataHandler(new
            // MessageDataSource(out.toByteArray(),
            // MIMETypes.MIME_MESSAGE_RFC822)), sb.append(
            // refMail.getSubject().replaceAll("\\p{Blank}+",
            // "_")).append(".eml").toString());
            // }
            // }
            /*
             * Finally set multipart
             */
            if (primaryMultipart != null) {
                mimeMessage.setContent(primaryMultipart);
            }
            return;
        }
        /*
         * Create a non-multipart message
         */
        if (mail.getContentType().isMimeType(MIMETypes.MIME_TEXT_ALL)) {
            final boolean isPlainText = mail.getContentType().isMimeType(MIMETypes.MIME_TEXT_PLAIN);
            if (mail.getContentType().getCharsetParameter() == null) {
                mail.getContentType().setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
            }
            if (primaryMultipart == null) {
                final String mailText;
                if (isPlainText) {
                    /*
                     * Convert html content to regular text
                     */
                    HTMLParser.parse(
                        getConformHTML(content, MailProperties.getInstance().getDefaultMimeCharset()),
                        getHTML2TextHandler().reset());
                    mailText = performLineFolding(getHTML2TextHandler().getText(), usm.getAutoLinebreak());
                    // mailText =
                    // performLineFolding(getConverter().convertWithQuotes
                    // ((String) mail.getContent()), false,
                    // usm.getAutoLinebreak());
                } else {
                    mailText = getConformHTML(content, mail.getContentType());
                }
                mimeMessage.setContent(mailText, mail.getContentType().toString());
                mimeMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
                mimeMessage.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, mail.getContentType().toString()));
            } else {
                final MimeBodyPart msgBodyPart = new MimeBodyPart();
                msgBodyPart.setContent(mail.getContent(), mail.getContentType().toString());
                msgBodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, mail.getContentType().toString()));
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
            msgBodyPart.setText("", MailProperties.getInstance().getDefaultMimeCharset());
            final String disposition = msgBodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
            if (disposition == null) {
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, Part.INLINE);
            } else {
                final ContentDisposition contentDisposition = new ContentDisposition(disposition);
                contentDisposition.setDisposition(Part.INLINE);
                msgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, contentDisposition.toString()));
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
            mimeMessage.setContent(primaryMultipart);
        }
    }

    /**
     * Gets session user's VCard as a string.
     * 
     * @param charset The charset to use for returned string
     * @return The session user's VCard as a string
     * @throws MailException If a mail error occurs
     */
    protected final String getUserVCard(final String charset) throws MailException {
        final User userObj = UserStorage.getStorageUser(session.getUserId(), ctx);
        Connection readCon = null;
        try {
            final OXContainerConverter converter = new OXContainerConverter(session, ctx);
            try {
                readCon = DBPool.pickup(ctx);
                Contact contactObj = null;
                try {
                    contactObj = Contacts.getContactById(
                        userObj.getContactId(),
                        userObj.getId(),
                        userObj.getGroups(),
                        ctx,
                        UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx),
                        readCon);
                } catch (final OXException oxExc) {
                    throw new MailException(oxExc);
                } catch (final Exception e) {
                    throw new MailException(MailException.Code.VERSIT_ERROR, e, e.getMessage());
                }
                final VersitObject versitObj = converter.convertContact(contactObj, "2.1");
                final ByteArrayOutputStream os = new UnsynchronizedByteArrayOutputStream();
                final VersitDefinition def = Versit.getDefinition(MIMETypes.MIME_TEXT_X_VCARD);
                final VersitDefinition.Writer w = def.getWriter(os, MailProperties.getInstance().getDefaultMimeCharset());
                def.write(w, versitObj);
                w.flush();
                os.flush();
                return new String(os.toByteArray(), charset);
            } finally {
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                    readCon = null;
                }
                converter.close();
            }
        } catch (final ConverterException e) {
            throw new MailException(MailException.Code.VERSIT_ERROR, e, e.getMessage());
        } catch (final AbstractOXException e) {
            throw new MailException(e);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /**
     * Creates a "multipart/alternative" object.
     * 
     * @param mail The source composed mail
     * @param mailBody The composed mail's HTML content
     * @param embeddedImages <code>true</code> if specified HTML content contains inline images (an appropriate "multipart/related" object
     *            is going to be created ); otherwise <code>false</code>.
     * @return An appropriate "multipart/alternative" object.
     * @throws MailException If a mail error occurs
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    protected final Multipart createMultipartAlternative(final ComposedMailMessage mail, final String mailBody, final boolean embeddedImages) throws MailException, MessagingException, IOException {
        /*
         * Create an "alternative" multipart
         */
        final Multipart alternativeMultipart = new MimeMultipart(MP_ALTERNATIVE);
        /*
         * Define html content
         */
        final String htmlContent;
        if (embeddedImages) {
            /*
             * Create "related" multipart
             */
            final Multipart relatedMultipart;
            {
                final String[] arr = new String[1];
                relatedMultipart = createMultipartRelated(mail, mailBody, arr);
                htmlContent = arr[0];
            }
            /*
             * Add multipart/related as a body part to superior multipart
             */
            final BodyPart altBodyPart = new MimeBodyPart();
            altBodyPart.setContent(relatedMultipart);
            alternativeMultipart.addBodyPart(altBodyPart);
        } else {
            htmlContent = mailBody;
            final BodyPart html = createHtmlBodyPart(mailBody);
            /*
             * Add html part to superior multipart
             */
            alternativeMultipart.addBodyPart(html);
        }
        /*
         * Define & add text content to first index position
         */
        alternativeMultipart.addBodyPart(createTextBodyPart(htmlContent), 0);
        return alternativeMultipart;
    }

    /**
     * Creates a "multipart/related" object. All inline images are going to be added to returned "multipart/related" object and
     * corresponding HTML content is altered to reference these images through "Content-Id".
     * 
     * @param mail The source composed mail
     * @param mailBody The composed mail's HTML content
     * @param htmlContent An array of {@link String} with length <code>1</code> serving as a container for altered HTML content
     * @return The created "multipart/related" object
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If a mail error occurs
     */
    protected Multipart createMultipartRelated(final ComposedMailMessage mail, final String mailBody, final String[] htmlContent) throws MessagingException, MailException {
        /*
         * Create "related" multipart
         */
        final Multipart relatedMultipart = new MimeMultipart(MP_RELATED);
        /*
         * Check for local images
         */
        htmlContent[0] = processReferencedLocalImages(mailBody, relatedMultipart, this);
        /*
         * Process referenced local image files and insert returned html content as a new body part to first index
         */
        relatedMultipart.addBodyPart(createHtmlBodyPart(htmlContent[0]), 0);
        /*
         * Traverse Content-IDs occurring in original HTML content
         */
        final List<String> cidList = MIMEMessageUtility.getContentIDs(mailBody);
        NextImg: for (final String cid : cidList) {
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
            final BodyPart relatedImageBodyPart = new MimeBodyPart();
            relatedImageBodyPart.setDataHandler(imgPart.getDataHandler());
            for (final Iterator<Map.Entry<String, String>> iter = imgPart.getHeadersIterator(); iter.hasNext();) {
                final Map.Entry<String, String> e = iter.next();
                relatedImageBodyPart.setHeader(e.getKey(), e.getValue());
            }
            /*
             * Add image to "related" multipart
             */
            relatedMultipart.addBodyPart(relatedImageBodyPart);
        }
        return relatedMultipart;
    }

    protected final void addMessageBodyPart(final Multipart mp, final MailPart part, final boolean inline) throws MessagingException, MailException, IOException {
        if (part.getContentType().isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
            // TODO: Works correctly?
            final StringBuilder sb = new StringBuilder(32);
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(BUF_SIZE);
            final byte[] bbuf = new byte[BUF_SIZE];
            addNestedMessage(part, mp, sb, out, bbuf);
            return;
        }
        /*
         * A non-message attachment
         */
        final String fileName = part.getFileName();
        final ContentType ct = part.getContentType();
        if (ct.isMimeType(MIMETypes.MIME_APPL_OCTET) && fileName != null) {
            /*
             * Try to determine MIME type
             */
            final String ct2 = MIMEType2ExtMap.getContentType(fileName);
            final int pos = ct2.indexOf('/');
            ct.setPrimaryType(ct2.substring(0, pos));
            ct.setSubType(ct2.substring(pos + 1));
        }
        final MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(part.getDataHandler());
        if (fileName != null && !ct.containsNameParameter()) {
            ct.setNameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, ct.toString()));
        if (!inline) {
            /*
             * Force base64 encoding to keep data as it is
             */
            messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        }
        /*
         * Disposition
         */
        final String disposition = messageBodyPart.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
        final ContentDisposition cd;
        if (disposition == null) {
            cd = new ContentDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        } else {
            cd = new ContentDisposition(disposition);
            cd.setDisposition(inline ? Part.INLINE : Part.ATTACHMENT);
        }
        if (fileName != null && !cd.containsFilenameParameter()) {
            cd.setFilenameParameter(fileName);
        }
        messageBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, cd.toString()));
        /*
         * Content-ID
         */
        if (part.getContentId() != null) {
            final String cid = part.getContentId().charAt(0) == '<' ? part.getContentId() : new StringBuilder(
                part.getContentId().length() + 2).append('<').append(part.getContentId()).append('>').toString();
            messageBodyPart.setContentID(cid);
        }
        /*
         * Add to parental multipart
         */
        mp.addBodyPart(messageBodyPart);
    }

    protected void addNestedMessage(final MailPart mailPart, final Multipart primaryMultipart, final StringBuilder sb, final ByteArrayOutputStream out, final byte[] bbuf) throws MailException, IOException, MessagingException {
        final byte[] rfcBytes;
        {
            final InputStream in = mailPart.getInputStream();
            try {
                int len;
                while ((len = in.read(bbuf)) != -1) {
                    out.write(bbuf, 0, len);
                }
            } finally {
                in.close();
            }
            rfcBytes = out.toByteArray();
        }
        out.reset();
        final String fn;
        if (null == mailPart.getFileName()) {
            String subject = new InternetHeaders(new UnsynchronizedByteArrayInputStream(rfcBytes)).getHeader(
                MessageHeaders.HDR_SUBJECT,
                null);
            if (null == subject || subject.length() == 0) {
                fn = sb.append(PREFIX_PART).append(EXT_EML).toString();
            } else {
                subject = MIMEMessageUtility.decodeMultiEncodedHeader(MIMEMessageUtility.unfold(subject));
                fn = sb.append(subject.replaceAll("\\p{Blank}+", "_")).append(EXT_EML).toString();
                sb.setLength(0);
            }
        } else {
            fn = mailPart.getFileName();
        }
        addNestedMessage(
            primaryMultipart,
            new DataHandler(new MessageDataSource(rfcBytes, MIMETypes.MIME_MESSAGE_RFC822)),
            fn,
            Part.INLINE.equalsIgnoreCase(mailPart.getContentDisposition().getDisposition()));
    }

    private final void addNestedMessage(final Multipart mp, final DataHandler dataHandler, final String filename, final boolean inline) throws MessagingException, MailException {
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
        final ContentType ct = new ContentType(MIMETypes.MIME_MESSAGE_RFC822);
        if (null != filename) {
            ct.setNameParameter(filename);
        }
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, ct.toString()));
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
        origMsgPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, cd.toString()));
        mp.addBodyPart(origMsgPart);
    }

    /*
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ +++++++++++++++++++++++++ HELPER METHODS +++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    /**
     * Creates a body part of type <code>text/plain</code> from given HTML content
     * 
     * @param htmlContent The HTML content
     * @return A body part of type <code>text/plain</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    protected final BodyPart createTextBodyPart(final String htmlContent) throws MessagingException, IOException {
        /*
         * Convert html content to regular text. First: Create a body part for text content
         */
        final MimeBodyPart text = new MimeBodyPart();
        /*
         * Define text content
         */
        final String textContent;
        if (htmlContent == null || htmlContent.length() == 0) {
            textContent = "";
        } else {
            HTMLParser.parse(
                getConformHTML(htmlContent, MailProperties.getInstance().getDefaultMimeCharset()),
                getHTML2TextHandler().reset());
            textContent = performLineFolding(getHTML2TextHandler().getText(), usm.getAutoLinebreak());
        }
        text.setText(textContent, MailProperties.getInstance().getDefaultMimeCharset());
        // text.setText(performLineFolding(getConverter().convertWithQuotes(
        // htmlContent), false, usm.getAutoLinebreak()),
        // MailConfig.getDefaultMimeCharset());
        text.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
        text.setHeader(MessageHeaders.HDR_CONTENT_TYPE, PAT_TEXT_CT.replaceFirst(
            REPLACE_CS,
            MailProperties.getInstance().getDefaultMimeCharset()));
        return text;
    }

    private static final String HTML_SPACE = "&#160;";

    /**
     * Creates a body part of type <code>text/html</code> from given HTML content
     * 
     * @param htmlContent The HTML content
     * @return A body part of type <code>text/html</code> from given HTML content
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If an I/O error occurs
     */
    protected final static BodyPart createHtmlBodyPart(final String htmlContent) throws MessagingException, MailException {
        final ContentType htmlCT = new ContentType(PAT_HTML_CT.replaceFirst(
            REPLACE_CS,
            MailProperties.getInstance().getDefaultMimeCharset()));
        final MimeBodyPart html = new MimeBodyPart();
        if (htmlContent == null || htmlContent.length() == 0) {
            html.setContent(getConformHTML(HTML_SPACE, htmlCT).replaceFirst(HTML_SPACE, ""), htmlCT.toString());
        } else {
            html.setContent(getConformHTML(htmlContent, htmlCT), htmlCT.toString());
        }
        html.setHeader(MessageHeaders.HDR_MIME_VERSION, VERSION_1_0);
        html.setHeader(MessageHeaders.HDR_CONTENT_TYPE, htmlCT.toString());
        return html;
    }

    private static final String IMG_PAT = "<img src=\"cid:#1#\">";

    /**
     * Processes referenced local images, inserts them as inlined html images and adds their binary data to parental instance of <code>
	 * {@link Multipart}</code>
     * 
     * @param htmlContent The html content whose &lt;img&gt; tags must be replaced with real content ids
     * @param mp The parental instance of <code>{@link Multipart}</code>
     * @param msgFiller The message filler
     * @return the replaced html content
     * @throws MessagingException If appending as body part fails
     * @throws MailException If a mail error occurs
     */
    protected final static String processReferencedLocalImages(final String htmlContent, final Multipart mp, final MIMEMessageFiller msgFiller) throws MessagingException, MailException {
        final Matcher m = MIMEMessageUtility.PATTERN_REF_IMG.matcher(htmlContent);
        final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
        final StringBuilder sb = new StringBuilder(htmlContent.length());
        if (m.find()) {
            msgFiller.uploadFileIDs = new HashSet<String>();
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            final ImageService imageService = ServerServiceRegistry.getInstance().getService(ImageService.class);
            final Session session = msgFiller.session;
            final StringBuilder tmp = new StringBuilder(128);
            do {
                final String id = m.group(5);
                final ImageProvider imageProvider;
                if (mfm.contains(id)) {
                    try {
                        imageProvider = new ManagedFileImageProvider(mfm.getByID(id));
                    } catch (final ManagedFileException e) {
                        if (LOG.isWarnEnabled()) {
                            tmp.setLength(0);
                            LOG.warn(tmp.append("Image with id \"").append(id).append(
                                "\" could not be loaded. Referenced image is skipped.").toString(), e);
                        }
                        /*
                         * Anyway, replace image tag
                         */
                        tmp.setLength(0);
                        mr.appendLiteralReplacement(sb, IMG_PAT.replaceFirst(
                            "#1#",
                            tmp.append(id).append('@').append("notfound").toString()));
                        continue;
                    }
                } else {
                    ImageData imageData = imageService.getImageData(session, id);
                    if (imageData == null) {
                        imageData = imageService.getImageData(session.getContextId(), id);
                    }
                    if (imageData == null) {
                        if (LOG.isWarnEnabled()) {
                            tmp.setLength(0);
                            LOG.warn(tmp.append("No image found with id \"").append(id).append("\". Referenced image is skipped.").toString());
                        }
                        /*
                         * Anyway, replace image tag
                         */
                        tmp.setLength(0);
                        mr.appendLiteralReplacement(sb, IMG_PAT.replaceFirst(
                            "#1#",
                            tmp.append(id).append('@').append("notfound").toString()));
                        continue;
                    }
                    try {
                        imageProvider = new ImageDataImageProvider(imageData, session);
                    } catch (final MailException e) {
                        if (MailException.Code.IMAGE_ATTACHMENT_NOT_FOUND.getNumber() == e.getDetailNumber()) {
                            tmp.setLength(0);
                            mr.appendLiteralReplacement(sb, IMG_PAT.replaceFirst(
                                "#1#",
                                tmp.append(id).append('@').append("notfound").toString()));
                            continue;
                        }
                        throw e;
                    }
                }
                final boolean appendBodyPart;
                if (msgFiller.uploadFileIDs.contains(id)) {
                    appendBodyPart = false;
                } else {
                    /*
                     * Remember id to avoid duplicate attachment and for later cleanup
                     */
                    msgFiller.uploadFileIDs.add(id);
                    appendBodyPart = true;
                }
                /*
                 * Replace image tag
                 */
                mr.appendLiteralReplacement(sb, IMG_PAT.replaceFirst("#1#", processLocalImage(imageProvider, id, appendBodyPart, tmp, mp)));
            } while (m.find());
        }
        mr.appendTail(sb);
        return sb.toString();
    }

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
     * @throws MailException If a mail error occurs
     */
    private final static String processLocalImage(final ImageProvider imageProvider, final String id, final boolean appendBodyPart, final StringBuilder tmp, final Multipart mp) throws MessagingException, MailException {
        /*
         * Determine filename
         */
        String fileName;
        try {
            fileName = MimeUtility.encodeText(imageProvider.getFileName(), MailProperties.getInstance().getDefaultMimeCharset(), "Q");
        } catch (final UnsupportedEncodingException e) {
            fileName = imageProvider.getFileName();
        }
        /*
         * ... and cid
         */
        tmp.setLength(0);
        tmp.append(id).append('@').append(Version.NAME);
        final String cid = tmp.toString();
        if (appendBodyPart) {
            /*
             * Append body part
             */
            final MimeBodyPart imgBodyPart = new MimeBodyPart();
            imgBodyPart.setDataHandler(new DataHandler(imageProvider.getDataSource()));
            tmp.setLength(0);
            imgBodyPart.setContentID(tmp.append('<').append(cid).append('>').toString());
            final ContentDisposition contentDisposition = new ContentDisposition(Part.INLINE);
            if (fileName != null) {
                contentDisposition.setFilenameParameter(fileName);
            }
            imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, contentDisposition.toString()));
            final ContentType ct = new ContentType(imageProvider.getContentType());
            if (fileName != null && !ct.containsNameParameter()) {
                ct.setNameParameter(fileName);
            }
            imgBodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMEMessageUtility.fold(14, ct.toString()));
            mp.addBodyPart(imgBodyPart);
        }
        return cid;
    }

    /**
     * Gets and removes the image attachment from specified mail whose <code>Content-Id</code> matches given <code>cid</code> argument
     * 
     * @param cid The <code>Content-Id</code> of the image attachment
     * @param mail The mail containing the image attachment
     * @return The removed image attachment
     * @throws MailException If a mail error occurs
     */
    protected final static MailPart getAndRemoveImageAttachment(final String cid, final ComposedMailMessage mail) throws MailException {
        final int size = mail.getEnclosedCount();
        for (int i = 0; i < size; i++) {
            final MailPart enclosedPart = mail.getEnclosedMailPart(i);
            if (enclosedPart.containsContentId() && MIMEMessageUtility.equalsCID(cid, enclosedPart.getContentId())) {
                return mail.removeEnclosedPart(i);
            }
        }
        return null;
    }

    private static final boolean hasOnlyReferencedMailAttachments(final ComposedMailMessage mail, final int size) throws MailException {
        for (int i = 0; i < size; i++) {
            final MailPart part = mail.getEnclosedMailPart(i);
            if (!ComposedPartType.REFERENCE.equals(((ComposedMailPart) part).getType()) || !((ReferencedMailPart) part).isMail()) {
                return false;
            }
        }
        return true;
    }

    private static interface ImageProvider {

        public String getFileName();

        public DataSource getDataSource() throws MailException;

        public String getContentType();
    } // End of ImageProvider

    private static class ManagedFileImageProvider implements ImageProvider {

        private final ManagedFile managedFile;

        public ManagedFileImageProvider(final ManagedFile managedFile) {
            super();
            this.managedFile = managedFile;
        }

        public String getContentType() {
            return managedFile.getContentType();
        }

        public DataSource getDataSource() throws MailException {
            return new FileDataSource(managedFile.getFile());
        }

        public String getFileName() {
            return managedFile.getFileName();
        }
    } // End of ManagedFileImageProvider

    private static class ImageDataImageProvider implements ImageProvider {

        private final Data<InputStream> data;

        private final String contentType;

        private final String fileName;

        public ImageDataImageProvider(final ImageData imageData, final Session session) throws MailException {
            super();
            try {
                this.data = imageData.getImageData(session);
            } catch (final DataException e) {
                throw new MailException(e);
            }
            final DataProperties dataProperties = data.getDataProperties();
            contentType = dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE);
            fileName = dataProperties.get(DataProperties.PROPERTY_NAME);
        }

        public String getContentType() {
            return contentType;
        }

        public DataSource getDataSource() throws MailException {
            try {
                return new MessageDataSource(data.getData(), contentType);
            } catch (final IOException e) {
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }
        }

        public String getFileName() {
            return fileName;
        }
    } // End of ImageDataImageProvider

}
