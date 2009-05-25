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

package com.openexchange.mail.parser;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import net.freeutils.tnef.Attachment;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;
import net.freeutils.tnef.mime.ContactHandler;
import net.freeutils.tnef.mime.RawDataSource;
import net.freeutils.tnef.mime.ReadReceiptHandler;
import net.freeutils.tnef.mime.TNEFMime;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.TNEFBodyPart;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailMessageParser} - A callback parser to parse instances of {@link MailMessage} by invoking the <code>handleXXX()</code> methods
 * of given {@link MailMessageHandler} object
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageParser {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailMessageParser.class);

    private static final int BUF_SIZE = 8192;

    private static interface InlineDetector {

        public boolean isInline(String disposition, String fileName);
    }

    /**
     * If disposition equals ignore-case <code>"INLINE"</code>, then it is treated as inline in any case.<br>
     * Only if disposition is <code>null</code> the file name is examined.
     */
    private static final InlineDetector LENIENT_DETECTOR = new InlineDetector() {

        public boolean isInline(final String disposition, final String fileName) {
            return Part.INLINE.equalsIgnoreCase(disposition) || ((disposition == null) && (fileName == null));
        }
    };

    /**
     * Considered as inline if disposition equals ignore-case <code>"INLINE"</code> OR is <code>null</code>, but in any case the file name
     * must be <code>null</code>.
     */
    private static final InlineDetector STRICT_DETECTOR = new InlineDetector() {

        public boolean isInline(final String disposition, final String fileName) {
            return (Part.INLINE.equalsIgnoreCase(disposition) || (disposition == null)) && (fileName == null);
        }
    };

    /*
     * +++++++++++++++++++ TNEF CONSTANTS +++++++++++++++++++
     */
    private static final String TNEF_IPM_CONTACT = "IPM.Contact";

    private static final String TNEF_IPM_MS_READ_RECEIPT = "IPM.Microsoft Mail.Read Receipt";

    /*
     * +++++++++++++++++++ MEMBERS +++++++++++++++++++
     */

    private boolean stop;

    private boolean multipartDetected;

    private InlineDetector inlineDetector;

    /**
     * Constructor
     */
    public MailMessageParser() {
        super();
        inlineDetector = LENIENT_DETECTOR;
    }

    /**
     * Switches the INLINE detector behavior.
     * 
     * @param strict <code>true</code> to perform strict INLINE detector behavior; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public MailMessageParser setInlineDetectorBehavior(final boolean strict) {
        inlineDetector = strict ? STRICT_DETECTOR : LENIENT_DETECTOR;
        return this;
    }

    /**
     * Resets this parser and returns itself
     * 
     * @return The parser itself
     */
    public MailMessageParser reset() {
        stop = false;
        multipartDetected = false;
        return this;
    }

    /**
     * Parses specified mail using given handler as call-back
     * 
     * @param mail The mail to parse
     * @param handler The call-back handler
     * @throws MailException If parsing specified mail fails
     */
    public void parseMailMessage(final MailMessage mail, final MailMessageHandler handler) throws MailException {
        parseMailMessage(mail, handler, null);
    }

    /**
     * Parses specified mail using given handler as call-back and given initial prefix for mail part identifiers; e.g.
     * <code>&quot;1.1&quot;</code>.
     * 
     * @param mail The mail to parse
     * @param handler The call-back handler
     * @param prefix The initial prefix for mail part identifiers; e.g. <code>&quot;1.1&quot;</code>
     * @throws MailException If parsing specified mail fails
     */
    public void parseMailMessage(final MailMessage mail, final MailMessageHandler handler, final String prefix) throws MailException {
        if (null == mail) {
            throw new MailException(MailException.Code.MISSING_PARAMETER, "mail");
        }
        if (null == handler) {
            throw new MailException(MailException.Code.MISSING_PARAMETER, "handler");
        }
        try {
            /*
             * Parse mail's envelope
             */
            parseEnvelope(mail, handler);
            /*
             * Parse content
             */
            parseMailContent(mail, handler, prefix, 1);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.UNREADBALE_PART_CONTENT, e, Long.valueOf(mail.getMailId()), mail.getFolder());
        }
        handler.handleMessageEnd(mail);
    }

    private void parseMailContent(final MailPart mailPartArg, final MailMessageHandler handler, final String prefix, final int partCountArg) throws MailException, IOException {
        if (stop) {
            return;
        }
        /*
         * Part modifier
         */
        final MailPart mailPart = MailConfig.usePartModifier() ? MailConfig.getPartModifier().modifyPart(mailPartArg) : mailPartArg;
        /*
         * Set part infos
         */
        int partCount = partCountArg;
        final String disposition = mailPart.containsContentDisposition() ? mailPart.getContentDisposition().getDisposition() : null;
        final long size = mailPart.getSize();
        final String filename = getFileName(
            mailPart.getFileName(),
            getSequenceId(prefix, partCount),
            mailPart.getContentType().getBaseType());
        final ContentType contentType = mailPart.containsContentType() ? mailPart.getContentType() : new ContentType(
            MIMETypes.MIME_APPL_OCTET);
        /*
         * Parse part dependent on its MIME type
         */
        final boolean isInline = inlineDetector.isInline(disposition, mailPart.getFileName());
        /*-
         * formerly:
         * final boolean isInline = ((disposition == null
         *     || disposition.equalsIgnoreCase(Part.INLINE)) && mailPart.getFileName() == null);
         */
        if (contentType.isMimeType(MIMETypes.MIME_TEXT_PLAIN) || contentType.isMimeType(MIMETypes.MIME_TEXT_ENRICHED) || contentType.isMimeType(MIMETypes.MIME_TEXT_RICHTEXT) || contentType.isMimeType(MIMETypes.MIME_TEXT_RTF)) {
            if (isInline) {
                final String charset = getCharset(mailPart, contentType);
                final String content = MessageUtility.readMailPart(mailPart, charset);
                final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
                if (uuencodedMP.isUUEncoded()) {
                    /*
                     * UUEncoded content detected. Handle normal text.
                     */
                    if (!handler.handleInlineUUEncodedPlainText(
                        uuencodedMP.getCleanText(),
                        contentType,
                        uuencodedMP.getCleanText().length(),
                        filename,
                        getSequenceId(prefix, partCount))) {
                        stop = true;
                        return;
                    }
                    /*
                     * Now handle uuencoded attachments
                     */
                    final int count = uuencodedMP.getCount();
                    for (int a = 0; a < count; a++) {
                        /*
                         * Increment part count by 1
                         */
                        partCount++;
                        if (!handler.handleInlineUUEncodedAttachment(uuencodedMP.getBodyPart(a), MailMessageParser.getSequenceId(
                            prefix,
                            partCount))) {
                            stop = true;
                            return;
                        }
                    }
                } else {
                    /*
                     * Just non-encoded plain text
                     */
                    if (!handler.handleInlinePlainText(content, contentType, size, filename, MailMessageParser.getSequenceId(
                        prefix,
                        partCount))) {
                        stop = true;
                        return;
                    }
                }
            } else {
                /*
                 * Non-Inline: Text attachment
                 */
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, false, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (contentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (isInline) {
                final String charset = getCharset(mailPart, contentType);
                if (!handler.handleInlineHtml(
                    MessageUtility.readMailPart(mailPart, charset),
                    contentType,
                    size,
                    filename,
                    mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } else {
                if (!handler.handleAttachment(mailPart, false, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (contentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
            final int count = mailPart.getEnclosedCount();
            if (count == -1) {
                throw new MailException(MailException.Code.INVALID_MULTIPART_CONTENT);
            }
            final String mpId = getSequenceId(prefix, partCount);
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(mpId);
            }
            if (!handler.handleMultipart(mailPart, count, mpId)) {
                stop = true;
                return;
            }
            final String mpPrefix;
            if (multipartDetected) {
                mpPrefix = mpId;
            } else {
                mpPrefix = prefix;
                multipartDetected = true;
            }
            for (int i = 0; i < count; i++) {
                final MailPart enclosedContent = mailPart.getEnclosedMailPart(i);
                parseMailContent(enclosedContent, handler, mpPrefix, i + 1);
            }
        } else if (contentType.isMimeType(MIMETypes.MIME_IMAGE_ALL)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleImagePart(
                mailPart,
                mailPart.getContentId(),
                contentType.getBaseType(),
                isInline,
                filename,
                mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        } else if (contentType.isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (isInline) {
                if (!handler.handleNestedMessage(mailPart, getSequenceId(prefix, partCount))) {
                    stop = true;
                    return;
                }
            } else {
                if (!handler.handleAttachment(mailPart, isInline, MIMETypes.MIME_MESSAGE_RFC822, filename, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (TNEFUtils.isTNEFMimeType(mailPart.getContentType().toString())) {
            try {
                /*
                 * Here go with TNEF encoded messages. Since TNEF library is based on JavaMail API we are forced to use JavaMail-specific
                 * types regardless of the mail implementation. First, grab TNEF input stream.
                 */
                final TNEFInputStream tnefInputStream = new TNEFInputStream(mailPart.getInputStream());
                /*
                 * Wrapping TNEF message
                 */
                final net.freeutils.tnef.Message message = new net.freeutils.tnef.Message(tnefInputStream);
                /*
                 * Handle special conversion
                 */
                final Attr messageClass = message.getAttribute(Attr.attMessageClass);
                final String messageClassName = messageClass == null ? "" : ((String) messageClass.getValue());
                if (TNEF_IPM_CONTACT.equalsIgnoreCase(messageClassName)) {
                    /*
                     * Convert contact to standard vCard. Resulting Multipart object consists of only ONE BodyPart object which encapsulates
                     * converted VCard. But for consistency reasons keep the code structure to iterate over Multipart's child objects.
                     */
                    final Multipart mp;
                    try {
                        mp = ContactHandler.convert(message);
                    } catch (final RuntimeException e) {
                        LOG.error("Invalid TNEF contact", e);
                        return;
                    }
                    final int mpsize = mp.getCount();
                    for (int i = 0; i < mpsize; i++) {
                        /*
                         * Since TNEF library is based on JavaMail API we use an instance of IMAPMailContent regardless of the mail
                         * implementation
                         */
                        parseMailContent(MIMEMessageConverter.convertPart(mp.getBodyPart(i), false), handler, prefix, partCount++);
                    }
                    /*
                     * Stop to further process TNEF attachment
                     */
                    return;
                } else if (messageClassName.equalsIgnoreCase(TNEF_IPM_MS_READ_RECEIPT)) {
                    /*
                     * Convert read receipt to standard notification. Resulting Multipart object consists both the human readable text part
                     * and machine readable part.
                     */
                    final Multipart mp;
                    try {
                        mp = ReadReceiptHandler.convert(message);
                    } catch (final RuntimeException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Invalid TNEF read receipt", e);
                        }
                        return;
                    }
                    final int mpsize = mp.getCount();
                    for (int i = 0; i < mpsize; i++) {
                        /*
                         * Since TNEF library is based on JavaMail API we use an instance of IMAPMailContent regardless of the mail
                         * implementation
                         */
                        parseMailContent(MIMEMessageConverter.convertPart(mp.getBodyPart(i)), handler, prefix, partCount++);
                    }
                    /*
                     * Stop to further process TNEF attachment
                     */
                    return;
                }
                /*
                 * Look for body. Usually the body is the RTF text.
                 */
                final Attr attrBody = Attr.findAttr(message.getAttributes(), Attr.attBody);
                if (attrBody != null) {
                    final TNEFBodyPart bodyPart = new TNEFBodyPart();
                    bodyPart.setText((String) attrBody.getValue());
                    bodyPart.setSize(((String) attrBody.getValue()).length());
                    parseMailContent(MIMEMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                }
                if (message.getMAPIProps() != null) {
                    final RawInputStream ris = (RawInputStream) message.getMAPIProps().getPropValue(MAPIProp.PR_RTF_COMPRESSED);
                    if (ris != null) {
                        final TNEFBodyPart bodyPart = new TNEFBodyPart();
                        /*
                         * Decompress RTF body
                         */
                        final byte[] decompressedBytes = TNEFUtils.decompressRTF(ris.toByteArray());
                        final String contentTypeStr;
                        {
                            final String charset = CharsetDetector.detectCharset(new UnsynchronizedByteArrayInputStream(decompressedBytes));
                            contentTypeStr = new StringBuilder(MIMETypes.MIME_TEXT_RTF).append("; charset=").append(charset).toString();
                        }
                        /*
                         * Set content through a data handler to avoid further exceptions raised by unavailable DCH (data content handler)
                         * for MIME type "text/rtf" when set by setContent() method
                         */
                        bodyPart.setDataHandler(new DataHandler(new MessageDataSource(decompressedBytes, contentTypeStr)));
                        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentTypeStr);
                        bodyPart.setSize(decompressedBytes.length);
                        parseMailContent(MIMEMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                    }
                }
                /*
                 * Iterate TNEF attachments and nested messages
                 */
                final int s = message.getAttachments().size();
                if (s > 0) {
                    final Iterator<?> iter = message.getAttachments().iterator();
                    final ByteArrayOutputStream os = new UnsynchronizedByteArrayOutputStream(BUF_SIZE);
                    for (int i = 0; i < s; i++) {
                        final Attachment attachment = (Attachment) iter.next();
                        final TNEFBodyPart bodyPart = new TNEFBodyPart();
                        if (attachment.getNestedMessage() == null) {
                            /*
                             * Add TNEF attributes
                             */
                            bodyPart.setTNEFAttributes(attachment.getAttributes());
                            /*
                             * Translate TNEF attributes to MIME
                             */
                            final String attachFilename = attachment.getFilename();
                            String contentTypeStr = null;
                            if (attachment.getMAPIProps() != null) {
                                contentTypeStr = (String) attachment.getMAPIProps().getPropValue(MAPIProp.PR_ATTACH_MIME_TAG);
                            }
                            if ((contentTypeStr == null) && (attachFilename != null)) {
                                contentTypeStr = MIMEType2ExtMap.getContentType(attachFilename);
                            }
                            if (contentTypeStr == null) {
                                contentTypeStr = MIMETypes.MIME_APPL_OCTET;
                            }
                            final DataSource ds = new RawDataSource(attachment.getRawData(), contentTypeStr);
                            bodyPart.setDataHandler(new DataHandler(ds));
                            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ContentType.prepareContentTypeString(
                                contentTypeStr,
                                attachFilename));
                            if (attachFilename != null) {
                                final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                                cd.setFilenameParameter(attachFilename);
                                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MIMEMessageUtility.fold(21, cd.toString()));
                            }
                            os.reset();
                            attachment.writeTo(os);
                            bodyPart.setSize(os.size());
                            parseMailContent(MIMEMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                        } else {
                            /*
                             * Nested message
                             */
                            final MimeMessage nestedMessage = TNEFMime.convert(
                                MIMEDefaultSession.getDefaultSession(),
                                attachment.getNestedMessage());
                            os.reset();
                            nestedMessage.writeTo(os);
                            bodyPart.setDataHandler(new DataHandler(new MessageDataSource(os.toByteArray(), MIMETypes.MIME_MESSAGE_RFC822)));
                            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MIMETypes.MIME_MESSAGE_RFC822);
                            parseMailContent(MIMEMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                        }
                    }
                }
            } catch (final IOException tnefExc) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(tnefExc.getMessage(), tnefExc);
                }
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } catch (final MessagingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (contentType.isMimeType(MIMETypes.MIME_MESSAGE_DELIVERY_STATUS) || contentType.isMimeType(MIMETypes.MIME_MESSAGE_DISP_NOTIFICATION) || contentType.isMimeType(MIMETypes.MIME_TEXT_RFC822_HDRS) || contentType.isMimeType(MIMETypes.MIME_TEXT_ALL_CARD) || contentType.isMimeType(MIMETypes.MIME_TEXT_ALL_CALENDAR)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleSpecialPart(mailPart, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        } else {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleAttachment(mailPart, isInline, contentType.getBaseType(), filename, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        }
    }

    private void parseEnvelope(final MailMessage mail, final MailMessageHandler handler) throws MailException {
        /*
         * FROM
         */
        handler.handleFrom(mail.getFrom());
        /*
         * RECIPIENTS
         */
        handler.handleToRecipient(mail.getTo());
        handler.handleCcRecipient(mail.getCc());
        handler.handleBccRecipient(mail.getBcc());
        /*
         * SUBJECT
         */
        handler.handleSubject(MIMEMessageUtility.decodeMultiEncodedHeader(mail.getSubject()));
        /*
         * SENT DATE
         */
        if (mail.getSentDate() != null) {
            handler.handleSentDate(mail.getSentDate());
        }
        /*
         * RECEIVED DATE
         */
        if (mail.getReceivedDate() != null) {
            handler.handleReceivedDate(mail.getReceivedDate());
        }
        /*
         * FLAGS
         */
        handler.handleSystemFlags(mail.getFlags());
        handler.handleUserFlags(mail.getUserFlags());
        /*
         * COLOR LABEL
         */
        handler.handleColorLabel(mail.getColorLabel());
        /*
         * PRIORITY
         */
        handler.handlePriority(mail.getPriority());
        /*
         * CONTENT-ID
         */
        if (mail.containsContentId()) {
            handler.handleContentId(mail.getContentId());
        }
        /*
         * MSGREF
         */
        if (mail.getMsgref() != null) {
            handler.handleMsgRef(mail.getMsgref().toString());
        }
        /*
         * DISPOSITION-NOTIFICATION-TO
         */
        if (mail.containsDispositionNotification() && (null != mail.getDispositionNotification())) {
            handler.handleDispositionNotification(
                mail.getDispositionNotification(),
                mail.containsPrevSeen() ? mail.isPrevSeen() : mail.isSeen());
        }
        /*
         * HEADERS
         */
        handler.handleHeaders(mail.getHeadersSize(), mail.getHeadersIterator());
    }

    private static final String PREFIX = "Part_";

    /**
     * Generates an appropriate filename from either specified <code>rawFileName</code> if not <code>null</code> or generates a filename
     * composed with <code>"Part_" + sequenceId</code>
     * 
     * @param rawFileName The raw filename obtained from mail part
     * @param sequenceId The part's sequence ID
     * @param baseMimeType The base MIME type to look up an appropriate file extension, if <code>rawFileName</code> is <code>null</code>
     * @return An appropriate filename
     */
    public static String getFileName(final String rawFileName, final String sequenceId, final String baseMimeType) {
        String filename = rawFileName;
        if ((filename == null) || isEmptyString(filename)) {
            final List<String> exts = MIMEType2ExtMap.getFileExtensions(baseMimeType.toLowerCase(Locale.ENGLISH));
            final StringBuilder sb = new StringBuilder(PREFIX.length() + sequenceId.length() + 5).append(PREFIX).append(sequenceId).append(
                '.');
            if (exts == null) {
                sb.append("dat");
            } else {
                sb.append(exts.get(0));
            }
            filename = sb.toString();
        } else {
            try {
                filename = MimeUtility.decodeText(filename.replaceAll("\\?==\\?", "?= =?"));
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return filename;
    }

    private static boolean isEmptyString(final String str) {
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace(chars[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Composes part's sequence ID from given prefix and part's count
     * 
     * @param prefix The prefix (may be <code>null</code>)
     * @param partCount The part count
     * @return The sequence ID
     */
    public static String getSequenceId(final String prefix, final int partCount) {
        if (prefix == null) {
            return String.valueOf(partCount);
        }
        return new StringBuilder(prefix).append('.').append(partCount).toString();
    }

    /**
     * Generates a filename consisting of common prefix "Part_" and part's sequence ID appended
     * 
     * @param sequenceId Part's sequence ID
     * @param baseMimeType The base Mime type to look up an appropriate file extension if <code>rawFileName</code> is <code>null</code>
     * @return The generated filename
     */
    public static String generateFilename(final String sequenceId, final String baseMimeType) {
        return getFileName(null, sequenceId, baseMimeType);
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws MailException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                if (null != cs) {
                    LOG.warn(
                        "Illegal or unsupported encoding in a message detected and monitored: \"" + cs + '"',
                        new UnsupportedEncodingException(cs));
                    mailInterfaceMonitor.addUnsupportedEncodingExceptions(cs);
                }
                if (contentType.isMimeType(MIMETypes.MIME_TEXT_ALL)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                }
            }
            charset = cs;
        } else {
            if (contentType.isMimeType(MIMETypes.MIME_TEXT_ALL)) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

}
