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

package com.openexchange.mail.mime.processing;

import static com.openexchange.mail.mime.filler.MimeMessageFiller.setReplyHeaders;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.CharsetDetector;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.conversion.InlineImageDataSource;
import com.openexchange.mail.dataobjects.CompositeMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.mime.dataobjects.NestedMessageMailPart;
import com.openexchange.mail.mime.datasource.StreamDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MimeForward} - MIME message forward.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeForward extends AbstractMimeProcessing {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MimeForward.class);

    private static final String PREFIX_FWD = "Fwd: ";

    /**
     * No instantiation
     */
    private MimeForward() {
        super();
    }

    /**
     * Composes a forward message from specified original messages based on MIME objects from <code>JavaMail</code> API.
     * <p>
     * If multiple messages are given these messages are forwarded as attachments.
     *
     * @param originalMails The referenced original mails
     * @param session The session containing needed user data
     * @param accountID The account ID of the referenced original mails
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing an user-editable forward mail
     * @throws OXException If forward mail cannot be composed
     */
    public static MailMessage getFowardMail(final MailMessage[] originalMails, final Session session, final int accountID, boolean setFrom) throws OXException {
        return getFowardMail(originalMails, session, accountID, null, setFrom);
    }

    /**
     * Composes a forward message from specified original messages based on MIME objects from <code>JavaMail</code> API.
     * <p>
     * If multiple messages are given these messages are forwarded as attachments.
     *
     * @param originalMails The referenced original mails
     * @param session The session containing needed user data
     * @param accountID The account ID of the referenced original mails
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing an user-editable forward mail
     * @throws OXException If forward mail cannot be composed
     */
    public static MailMessage getFowardMail(MailMessage[] originalMails, Session session, int accountID, UserSettingMail usm, boolean setFrom) throws OXException {
        for (MailMessage cur : originalMails) {
            if (cur.getMailId() != null && cur.getFolder() != null && cur.getAccountId() != accountID) {
                cur.setAccountId(accountID);
            }
        }
        /*
         * Compose forward message
         */
        return getFowardMail0(originalMails, new int[] {accountID}, session, usm, setFrom);
    }

    /**
     * Composes a forward message from specified original messages taken from possibly differing accounts based on MIME objects from
     * <code>JavaMail</code> API.
     * <p>
     * If multiple messages are given these messages are forwarded as attachments.
     *
     * @param originalMails The referenced original mails
     * @param session The session containing needed user data
     * @param accountIDs The account IDs of the referenced original mails
     * @param usm The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing an user-editable forward mail
     * @throws OXException If forward mail cannot be composed
     */
    public static MailMessage getFowardMail(MailMessage[] originalMails, Session session, int[] accountIDs, UserSettingMail usm, boolean setFrom) throws OXException {
        for (int i = 0; i < originalMails.length; i++) {
            MailMessage cur = originalMails[i];
            if (cur.getMailId() != null && cur.getFolder() != null && cur.getAccountId() != accountIDs[i]) {
                cur.setAccountId(accountIDs[i]);
            }
        }
        /*
         * Compose forward message
         */
        return getFowardMail0(originalMails, accountIDs, session, usm, setFrom);
    }

    /**
     * Composes a forward message from specified original messages based on MIME objects from <code>JavaMail</code> API.
     * <p>
     * If multiple messages are given these messages are forwarded as attachments.
     *
     * @param originalMsgs The referenced original messages
     * @param accountIds The account identifiers
     * @param session The session containing needed user data
     * @param userSettingMail The user mail settings to use; leave to <code>null</code> to obtain from specified session
     * @param setFrom <code>true</code> to set 'From' header; otherwise <code>false</code> to leave it
     * @return An instance of {@link MailMessage} representing an user-editable forward mail
     * @throws OXException If forward mail cannot be composed
     */
    private static MailMessage getFowardMail0(MailMessage[] originalMsgs, int[] accountIds, Session session, UserSettingMail userSettingMail, boolean setFrom) throws OXException {
        try {
            /*
             * Clone them to ensure consistent data
             */
            MailMessage[] origMsgs = ManagedMimeMessage.clone(originalMsgs);
            /*
             * New MIME message with a dummy session
             */
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            UserSettingMail usm = userSettingMail == null ? UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx) : userSettingMail;
            final MimeMessage forwardMsg = new MimeMessage(MimeDefaultSession.getDefaultSession());
            {
                /*
                 * Set its headers. Start with subject constructed from first message.
                 */
                final String subjectPrefix = PREFIX_FWD;
                String origSubject = MimeMessageUtility.checkNonAscii(origMsgs[0].getHeader(MessageHeaders.HDR_SUBJECT, null));
                if (origSubject == null) {
                    forwardMsg.setSubject(subjectPrefix, MailProperties.getInstance().getDefaultMimeCharset());
                } else {
                    origSubject = MimeMessageUtility.unfold(origSubject);
                    final String subject =
                        MimeMessageUtility.decodeMultiEncodedHeader(origSubject.regionMatches(true, 0, subjectPrefix, 0, subjectPrefix.length()) ? origSubject : new StringBuilder(
                            subjectPrefix.length() + origSubject.length()).append(subjectPrefix).append(origSubject).toString());
                    forwardMsg.setSubject(subject, MailProperties.getInstance().getDefaultMimeCharset());
                }
            }
            /*
             * Set from
             */
            {
                boolean fromSet = false;
                if (setFrom) {
                    InternetAddress from = MimeProcessingUtility.determinePossibleFrom(true, origMsgs[0], accountIds[0], session, ctx);
                    if (null != from) {
                        forwardMsg.setFrom(from);
                        fromSet = true;
                    }
                }
                if (!fromSet) {
                    String sendAddr = usm.getSendAddr();
                    if (sendAddr != null) {
                        forwardMsg.setFrom(new QuotedInternetAddress(sendAddr, false));
                    }
                }
            }
            if (usm.isForwardAsAttachment() || origMsgs.length > 1) {
                /*
                 * Attachment-Forward
                 */
                if (1 == origMsgs.length) {
                    final MailMessage originalMsg = origMsgs[0];
                    final String owner = MimeProcessingUtility.getFolderOwnerIfShared(originalMsg.getFolder(), originalMsg.getAccountId(), session);
                    if (null != owner) {
                        final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, ctx);
                        if (null != users && users.length > 0) {
                            final InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), true);
                            forwardMsg.setFrom(onBehalfOf);
                            final QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), true);
                            forwardMsg.setSender(sender);
                        }
                    }
                }
                return asAttachmentForward(origMsgs, forwardMsg);
            }
            /*
             * Inline-Forward
             */
            final MailMessage originalMsg;
            {
                final MailMessage omm = origMsgs[0];
                final ContentType contentType = omm.getContentType();
                if (contentType.startsWith("multipart/related") && ("application/smil".equals(contentType.getParameter(com.openexchange.java.Strings.toLowerCase("type"))))) {
                    originalMsg = MimeSmilFixer.getInstance().process(omm);
                } else {
                    originalMsg = omm;
                }
            }
            final String owner = MimeProcessingUtility.getFolderOwnerIfShared(originalMsg.getFolder(), originalMsg.getAccountId(), session);
            if (null != owner) {
                final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, ctx);
                if (null != users && users.length > 0) {
                    final InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), true);
                    forwardMsg.setFrom(onBehalfOf);
                    final QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), true);
                    forwardMsg.setSender(sender);
                }
            }
            return asInlineForward(originalMsg, session, ctx, usm, forwardMsg);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static final String MULTIPART = "multipart/";

    private static final String TEXT = "text/";

    private static final String TEXT_HTM = "text/htm";

    private static final Pattern PAT_META_CT = Pattern.compile("<meta[^>]*?http-equiv=\"?content-type\"?[^>]*?>", Pattern.CASE_INSENSITIVE);

    private static String replaceMetaEquiv(final String html, final ContentType contentType) {
        final Matcher m = PAT_META_CT.matcher(html);
        final MatcherReplacer mr = new MatcherReplacer(m, html);
        final StringBuilder replaceBuffer = new StringBuilder(html.length());
        if (m.find()) {
            replaceBuffer.append("<meta http-equiv=\"Content-Type\" content=\"").append(contentType.getBaseType().toLowerCase(Locale.ENGLISH));
            replaceBuffer.append("; charset=").append(contentType.getCharsetParameter()).append("\" />");
            final String replacement = replaceBuffer.toString();
            replaceBuffer.setLength(0);
            mr.appendLiteralReplacement(replaceBuffer, replacement);
        }
        mr.appendTail(replaceBuffer);
        return replaceBuffer.toString();
    }

    private static MailMessage asInlineForward(final MailMessage originalMsg, final Session session, final Context ctx, final UserSettingMail usm, final MimeMessage forwardMsg) throws OXException, MessagingException, IOException {
        /*
         * Check for message reference
         */
        final MailPath msgref = originalMsg.getMailPath();
        final ContentType originalContentType = originalMsg.getContentType();
        final MailMessage forwardMail;
        if (originalContentType.startsWith(MULTIPART)) {
            Multipart multipart = new MimeMultipart();
            List<String> contentIds = null;
            final boolean isHtml;
            {
                /*
                 * Grab first seen text from original message
                 */
                ContentType contentType = new ContentType();
                String firstSeenText = getFirstSeenText(originalMsg, contentType, usm, originalMsg, session, false);
                {
                    final String cs = contentType.getCharsetParameter();
                    if (cs == null || "US-ASCII".equalsIgnoreCase(cs)) {
                        contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    }
                }
                /*
                 * Prepare the text to insert
                 */
                isHtml = contentType.startsWith(TEXT_HTM);
                if (null == firstSeenText) {
                    firstSeenText = "";
                    contentType.setPrimaryType("text").setSubType("plain");
                } else if (isHtml) {
                    contentIds = MimeMessageUtility.getContentIDs(firstSeenText);
                    contentType.setCharsetParameter("UTF-8");
                    firstSeenText = replaceMetaEquiv(firstSeenText, contentType);
                }
                contentType.setParameter("nature", "virtual");
                /*
                 * Add appropriate text part prefixed with forward text
                 */
                MimeBodyPart textPart = new MimeBodyPart();
                String txt = usm.isDropReplyForwardPrefix() ? firstSeenText : generateForwardText(firstSeenText, new LocaleAndTimeZone(getUser(session, ctx)), originalMsg, isHtml);
                {
                    final String cs = contentType.getCharsetParameter();
                    if (cs == null || "US-ASCII".equalsIgnoreCase(cs) || !CharsetDetector.isValid(cs) || MessageUtility.isSpecialCharset(cs)) {
                        // Select default charset
                        contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                    }
                }
                MessageUtility.setText(txt, contentType.getCharsetParameter(), contentType.getSubType(), textPart);
                // textPart.setText(txt, contentType.getCharsetParameter(), contentType.getSubType());
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType.toString()));
                multipart.addBodyPart(textPart);
                MessageUtility.setContent(multipart, forwardMsg);
                // forwardMsg.setContent(multipart);
                forwardMsg.saveChanges();
                // Remove generated Message-Id header
                forwardMsg.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
                setReplyHeaders(originalMsg, forwardMsg);
            }
            final CompositeMailMessage compositeMail = new CompositeMailMessage(MimeMessageConverter.convertMessage(forwardMsg));
            /*
             * Add all non-inline parts through a handler to keep original sequence IDs
             */
            final NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
            if (null != contentIds && !contentIds.isEmpty()) {
                handler.setImageContentIds(contentIds);
            }
            new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMsg, handler);
            for (final MailPart mailPart : handler.getNonInlineParts()) {
                mailPart.getContentDisposition().setDisposition(Part.ATTACHMENT);
                if (!isHtml) {
                    // Drop any inline information; such as "Content-Id" header
                    mailPart.removeContentId();
                    mailPart.removeHeader(MessageHeaders.HDR_CONTENT_ID);
                }
                mailPart.getFileName(); // Enforce to set file name possibly extracted from Content-Type header
                mailPart.getContentType().removeNameParameter(); // Remove name parameter
                compositeMail.addAdditionalParts(mailPart);
            }
            forwardMail = compositeMail;
        } else if (originalContentType.startsWith(TEXT) && !MimeProcessingUtility.isSpecial(originalContentType.getBaseType())) {
            /*
             * Original message is a simple text mail: Add message body prefixed with forward text
             */
            {
                final String cs = originalContentType.getCharsetParameter();
                if (null == cs) {
                    originalContentType.setCharsetParameter(MessageUtility.checkCharset(originalMsg, originalContentType));
                }
            }
            String content = MimeProcessingUtility.readContent(originalMsg, originalContentType.getCharsetParameter());
            if (originalContentType.startsWith(TEXT_HTM)) {
                originalContentType.setCharsetParameter("UTF-8");
                content = replaceMetaEquiv(content, originalContentType);
            }
            final String txt = usm.isDropReplyForwardPrefix() ? (content == null ? "" : content) : generateForwardText(
                content == null ? "" : content,
                    new LocaleAndTimeZone(getUser(session, ctx)),
                    originalMsg,
                    originalContentType.startsWith(TEXT_HTM));
            {
                final String cs = originalContentType.getCharsetParameter();
                if (cs == null || "US-ASCII".equalsIgnoreCase(cs) || !CharsetDetector.isValid(cs) || MessageUtility.isSpecialCharset(cs)) {
                    // Select default charset
                    originalContentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                }
            }
            MessageUtility.setText(txt, originalContentType.getCharsetParameter(), originalContentType.getSubType(), forwardMsg);
            // forwardMsg.setText(txt,originalContentType.getCharsetParameter(),originalContentType.getSubType());
            forwardMsg.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            forwardMsg.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(originalContentType.toString()));
            forwardMsg.saveChanges();
            // Remove generated Message-Id header
            forwardMsg.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
            setReplyHeaders(originalMsg, forwardMsg);
            forwardMail = MimeMessageConverter.convertMessage(forwardMsg);
        } else {
            /*
             * Mail only consists of one non-textual part
             */
            final Multipart multipart = new MimeMultipart();
            /*
             * Add appropriate text part prefixed with forward text
             */
            {
                ContentType contentType = new ContentType(MimeTypes.MIME_TEXT_PLAIN);
                contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
                contentType.setParameter("nature", "virtual");

                MimeBodyPart textPart = new MimeBodyPart();
                String txt = usm.isDropReplyForwardPrefix() ? "" : generateForwardText("", new LocaleAndTimeZone(getUser(session, ctx)), originalMsg, false);
                MessageUtility.setText(txt,MailProperties.getInstance().getDefaultMimeCharset(),"plain", textPart);
                // textPart.setText(txt,MailProperties.getInstance().getDefaultMimeCharset(),"plain");
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType.toString()));
                multipart.addBodyPart(textPart);
                MessageUtility.setContent(multipart, forwardMsg);
                // forwardMsg.setContent(multipart);
                forwardMsg.saveChanges();
                // Remove generated Message-Id header
                forwardMsg.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
                setReplyHeaders(originalMsg, forwardMsg);
            }
            final CompositeMailMessage compositeMail = new CompositeMailMessage(MimeMessageConverter.convertMessage(forwardMsg));
            /*
             * Add the single "attachment"
             */
            final MailPart attachmentMailPart;
            {
                final MimeBodyPart attachmentPart = new MimeBodyPart();
                {
                    final StreamDataSource.InputStreamProvider isp = new StreamDataSource.InputStreamProvider() {

                        @Override
                        public InputStream getInputStream() throws IOException {
                            try {
                                return originalMsg.getInputStream();
                            } catch (final OXException e) {
                                final IOException io = new IOException(e.getMessage());
                                io.initCause(e);
                                throw io;
                            }
                        }

                        @Override
                        public String getName() {
                            return null;
                        }
                    };
                    attachmentPart.setDataHandler(new DataHandler(new StreamDataSource(isp, originalContentType.toString())));
                }
                for (final Iterator<Map.Entry<String, String>> e = originalMsg.getHeadersIterator(); e.hasNext();) {
                    final Map.Entry<String, String> header = e.next();
                    final String name = header.getKey();
                    if (name.toLowerCase(Locale.ENGLISH).startsWith("content-")) {
                        attachmentPart.addHeader(name, header.getValue());
                    }
                }
                attachmentMailPart = MimeMessageConverter.convertPart(attachmentPart, false);
            }
            attachmentMailPart.setSequenceId(String.valueOf(1));
            compositeMail.addAdditionalParts(attachmentMailPart);
            forwardMail = compositeMail;
        }
        if (null != msgref) {
            forwardMail.setMsgref(msgref);
        }
        return forwardMail;
    }

    private static User getUser(final Session session, final Context ctx) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), ctx);
    }

    private static MailMessage asAttachmentForward(final MailMessage[] originalMsgs, final MimeMessage forwardMsg) throws MessagingException, OXException {
        final CompositeMailMessage compositeMail;
        {
            final Multipart multipart = new MimeMultipart();
            /*
             * Add empty text content as message's body
             */
            final MimeBodyPart textPart = new MimeBodyPart();
            MessageUtility.setText("", MailProperties.getInstance().getDefaultMimeCharset(), "plain", textPart);
            // textPart.setText("", MailProperties.getInstance().getDefaultMimeCharset(), "plain");
            textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            textPart.setHeader(
                MessageHeaders.HDR_CONTENT_TYPE,
                MimeTypes.MIME_TEXT_PLAIN_TEMPL.replaceFirst("#CS#", MailProperties.getInstance().getDefaultMimeCharset()));
            multipart.addBodyPart(textPart);
            MessageUtility.setContent(multipart, forwardMsg);
            // forwardMsg.setContent(multipart);
            forwardMsg.saveChanges();
            // Remove generated Message-Id header
            forwardMsg.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
            compositeMail = new CompositeMailMessage(MimeMessageConverter.convertMessage(forwardMsg));
        }
        /*
         * Attach messages
         */
        for (final MailMessage originalMsg : originalMsgs) {
            final MailMessage nested;
            if (originalMsg instanceof MimeMailMessage) {
                nested = MimeMessageConverter.convertMessage(((MimeMailMessage) originalMsg).getMimeMessage());
            } else {
                nested = MimeMessageConverter.convertMessage(new MimeMessage(MimeDefaultSession.getDefaultSession(), MimeMessageUtility.getStreamFromMailPart(originalMsg)));
            }
            nested.setMsgref(originalMsg.getMailPath());
            compositeMail.addAdditionalParts(new NestedMessageMailPart(nested));
        }
        return compositeMail;
    }

    /**
     * Determines the first seen text in given multipart content with recursive iteration over enclosed multipart contents.
     *
     * @param mp The multipart object
     * @param retvalContentType The return value's content type (gets filled during processing and should therefore be empty)
     * @return The first seen text content
     * @throws OXException
     * @throws MessagingException
     * @throws IOException
     */
    private static String getFirstSeenText(final MailPart multipartPart, final ContentType retvalContentType, final UserSettingMail usm, final MailMessage origMail, final Session session, final boolean alt) throws OXException, MessagingException, IOException {
        final ContentType contentType = multipartPart.getContentType();
        final int count = multipartPart.getEnclosedCount();
        final ContentType partContentType = new ContentType();
        if (alt || ((contentType.startsWith(MimeTypes.MIME_MULTIPART_ALTERNATIVE) || contentType.startsWith(MimeTypes.MIME_MULTIPART_RELATED)) && usm.isDisplayHtmlInlineContent() && count >= 2)) {
            /*
             * Get html content
             */
            final boolean alternative = alt || contentType.startsWith(MimeTypes.MIME_MULTIPART_ALTERNATIVE);
            for (int i = 0; i < count; i++) {
                final MailPart part = multipartPart.getEnclosedMailPart(i);
                partContentType.setContentType(part.getContentType());
                if (partContentType.startsWith(TEXT_HTM) && MimeProcessingUtility.isInline(part, partContentType)) {
                    final String charset = MessageUtility.checkCharset(part, partContentType);
                    retvalContentType.setContentType(partContentType);
                    retvalContentType.setCharsetParameter(charset);
                    if (!multipartPart.getContentType().startsWith("multipart/mixed")) {
                        return MimeProcessingUtility.readContent(part, charset);
                    }
                    final StringBuilder sb = new StringBuilder(MimeProcessingUtility.readContent(part, charset));
                    for (int j = i + 1; j < count; j++) {
                        final MailPart nextPart = multipartPart.getEnclosedMailPart(j);
                        final ContentType nextContentType = nextPart.getContentType();
                        if (nextContentType.startsWith(TEXT_HTM) && MimeProcessingUtility.isInline(nextPart, nextContentType) && !MimeProcessingUtility.isSpecial(nextContentType.getBaseType())) {
                            final String nextCharset = MessageUtility.checkCharset(nextPart, partContentType);
                            final String nextText = MimeProcessingUtility.readContent(nextPart, nextCharset);
                            sb.append(nextText);
                        } else if (nextContentType.startsWith("image/") && MimeProcessingUtility.isInline(nextPart, nextContentType)) {
                            final String imageURL;
                            String fileName = nextPart.getFileName();
                            {
                                final InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
                                if (null == fileName) {
                                    final String ext = MimeType2ExtMap.getFileExtension(nextContentType.getBaseType());
                                    fileName = new StringBuilder("image").append(j).append('.').append(ext).toString();
                                }
                                final ImageLocation imageLocation = new ImageLocation.Builder(fileName).folder(prepareFullname(origMail.getAccountId(), origMail.getFolder())).id(origMail.getMailId()).build();
                                imageURL = imgSource.generateUrl(imageLocation, session);
                            }
                            final String imgTag = "<img src=\"" + imageURL + "&scaleType=contain&width=800\" alt=\"\" style=\"display: block\" id=\"" + fileName + "\">";
                            sb.append(imgTag);
                        }
                    }
                    return sb.toString();
                } else if (partContentType.startsWith(MULTIPART)) {
                    final String text = getFirstSeenText(part, retvalContentType, usm, origMail, session, alternative);
                    if (text != null) {
                        return text;
                    }
                }
            }
        }
        /*
         * Get any text content
         */
        for (int i = 0; i < count; i++) {
            final MailPart part = multipartPart.getEnclosedMailPart(i);
            partContentType.setContentType(part.getContentType());
            if (partContentType.startsWith(TEXT) && MimeProcessingUtility.isInline(part, partContentType) && !MimeProcessingUtility.isSpecial(partContentType.getBaseType())) {
                final String charset = MessageUtility.checkCharset(part, partContentType);
                retvalContentType.setContentType(partContentType);
                retvalContentType.setCharsetParameter(charset);
                String text = MimeProcessingUtility.handleInlineTextPart(part, retvalContentType, usm.isDisplayHtmlInlineContent());
                if (com.openexchange.java.Strings.isEmpty(text)) {
                    final String htmlContent = getHtmlContent(multipartPart, count);
                    if (null != htmlContent) {
                        final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                        text = null == htmlService ? "" : htmlService.html2text(htmlContent, true);
                    }
                }
                if (!multipartPart.getContentType().startsWith("multipart/mixed")) {
                    return text;
                }
                final StringBuilder sb = new StringBuilder(text);
                for (int j = i + 1; j < count; j++) {
                    final MailPart nextPart = multipartPart.getEnclosedMailPart(j);
                    final ContentType nextContentType = nextPart.getContentType();
                    if (nextContentType.startsWith(TEXT) && MimeProcessingUtility.isInline(part, nextContentType) && !MimeProcessingUtility.isSpecial(nextContentType.getBaseType())) {
                        String nextText = MimeProcessingUtility.handleInlineTextPart(nextPart, retvalContentType, usm.isDisplayHtmlInlineContent());
                        sb.append(nextText);
                    }
                }
                return sb.toString();
            } else if (partContentType.startsWith(MULTIPART)) {
                final String text = getFirstSeenText(part, retvalContentType, usm, origMail, session, alt);
                if (text != null) {
                    return text;
                }
            }
        }
        /*
         * No text content found
         */
        return null;
    }

    private static String getHtmlContent(final MailPart multipartPart, final int count) throws OXException, IOException {
        boolean found = false;
        for (int i = 0; !found && i < count; i++) {
            final MailPart part = multipartPart.getEnclosedMailPart(i);
            final ContentType partContentType = part.getContentType();
            if (partContentType.startsWith(TEXT_HTM) && MimeProcessingUtility.isInline(part, partContentType) && !MimeProcessingUtility.isSpecial(partContentType.getBaseType())) {
                final String charset = MessageUtility.checkCharset(part, partContentType);
                return MimeProcessingUtility.readContent(part, charset);
            }
        }
        return null;
    }

    private static final Pattern PATTERN_BODY = Pattern.compile("<body[^>]*?>", Pattern.CASE_INSENSITIVE);

    /**
     * Generates the forward text on an inline-forward operation.
     *
     * @param firstSeenText The first seen text from original message
     * @param ltz The locale that determines format of date and time strings and time zone as well
     * @param msg The original message
     * @param html <code>true</code> if given text is html content; otherwise <code>false</code>
     * @return The forward text
     */
    private static String generateForwardText(final String firstSeenText, final LocaleAndTimeZone ltz, final MailMessage msg, final boolean html) {
        String forwardPrefix = generatePrefixText(MailStrings.FORWARD_PREFIX, ltz, msg);
        if (html) {
            forwardPrefix = HtmlProcessing.htmlFormat(forwardPrefix);
        }
        final String linebreak = html ? "<br>" : "\r\n";

        /*-
         * Surround with quote
         *
         * Check whether forward text shall be surrounded with quotes or not
         */
        final boolean forwardUnquoted;
        {
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            forwardUnquoted = null == service ? false : service.getBoolProperty("com.openexchange.mail.forwardUnquoted", false);
        }

        if (forwardUnquoted) {
            /*
             * Don't quote
             */
            if (html) {
                Matcher m = PATTERN_BODY.matcher(firstSeenText);
                MatcherReplacer mr = new MatcherReplacer(m, firstSeenText);
                StringBuilder replaceBuffer = new StringBuilder(firstSeenText.length() + 256);
                if (m.find()) {
                    mr.appendLiteralReplacement(replaceBuffer, new StringBuilder(forwardPrefix.length() + 16).append(linebreak).append(m.group()).append(forwardPrefix).append(linebreak).toString());
                } else {
                    replaceBuffer.append(linebreak).append(forwardPrefix).append(linebreak);
                }
                replaceBuffer.append("<div style=\"position:relative\">");
                mr.appendTail(replaceBuffer);
                replaceBuffer.append("</div>");
                return replaceBuffer.toString();
            }
            return new StringBuilder(firstSeenText.length() + 256).append(linebreak).append(forwardPrefix).append(linebreak).append(firstSeenText).toString();
        }
        /*
         * Surround with quotes
         */
        if (html) {
            Matcher m = PATTERN_BODY.matcher(firstSeenText);
            MatcherReplacer mr = new MatcherReplacer(m, firstSeenText);
            StringBuilder replaceBuffer = new StringBuilder(firstSeenText.length() + 256);
            if (m.find()) {
                mr.appendLiteralReplacement(replaceBuffer, new StringBuilder(forwardPrefix.length() + 16).append(m.group()).append(forwardPrefix).append(linebreak).append(linebreak).toString());
            } else {
                replaceBuffer.append(forwardPrefix).append(linebreak).append(linebreak);
            }
            mr.appendTail(replaceBuffer);

            String tmp = quoteHtml(replaceBuffer.toString());
            replaceBuffer.setLength(0);
            replaceBuffer.append(linebreak);
            replaceBuffer.append(tmp);
            return replaceBuffer.toString();
        }
        final StringBuilder builder = new StringBuilder(firstSeenText.length() + 256);
        final String tmp = builder.append(forwardPrefix).append(linebreak).append(linebreak).append(firstSeenText).toString();
        builder.setLength(0);
        builder.append(linebreak).append(quoteText(tmp));
        return builder.toString();
    }

    private static String quoteText(final String textContent) {
        return textContent.replaceAll("(?m)^", "> ");
    }

    private static final Pattern PATTERN_HTML = Pattern.compile("(<html[^>]*?>)" + "|" + "(</html>)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_HTML_START = Pattern.compile("<html[^>]*?>", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_HTML_END = Pattern.compile("</html>", Pattern.CASE_INSENSITIVE);

    private static final String BLOCKQUOTE_START = "<blockquote type=\"cite\" style=\"position: relative; margin-left: 0px; padding-left: 10px; border-left: solid 1px blue;\">\n";

    private static final String BLOCKQUOTE_END = "</blockquote>\n<br>&nbsp;";

    private static String quoteHtml(final String htmlContent) {
        Matcher m = PATTERN_HTML.matcher(htmlContent);
        if (m.find()) {
            final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
            final StringBuilder sb = new StringBuilder(htmlContent.length());
            do {
                if (m.group(1) != null) {
                    mr.appendLiteralReplacement(sb, BLOCKQUOTE_START);
                } else {
                    mr.appendLiteralReplacement(sb, BLOCKQUOTE_END);
                }
            } while (m.find());
            mr.appendTail(sb);
            return sb.toString();
        }
        // Regular
        m = PATTERN_HTML_START.matcher(htmlContent);
        final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
        final StringBuilder sb = new StringBuilder(htmlContent.length());
        if (m.find()) {
            mr.appendLiteralReplacement(sb, BLOCKQUOTE_START);
        } else {
            sb.append(BLOCKQUOTE_START);
        }
        mr.appendTail(sb);
        {
            final String s = sb.toString();
            m = PATTERN_HTML_END.matcher(s);
            mr.resetTo(m, s);
        }
        sb.setLength(0);
        if (m.find()) {
            mr.appendLiteralReplacement(sb, BLOCKQUOTE_END);
            mr.appendTail(sb);
        } else {
            mr.appendTail(sb);
            sb.append(BLOCKQUOTE_END);
        }
        return sb.toString();
    }
}
