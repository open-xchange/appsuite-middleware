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

package com.openexchange.mail.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import net.fortuna.ical4j.model.Property;
import net.freeutils.tnef.Attachment;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.CompressedRTFInputStream;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;
import net.freeutils.tnef.mime.ContactHandler;
import net.freeutils.tnef.mime.RawDataSource;
import net.freeutils.tnef.mime.ReadReceiptHandler;
import net.freeutils.tnef.mime.TNEFMime;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.CountingOutputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.TNEFBodyPart;
import com.openexchange.mail.mime.converters.FileBackedMimeMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeMailPart;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MaxBytesExceededIOException;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.tnef.TNEF2ICal;

/**
 * {@link MailMessageParser} - A callback parser to parse instances of {@link MailMessage} by invoking the <code>handleXXX()</code> methods
 * of given {@link MailMessageHandler} object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageParser.class);

    private static final String APPL_OCTET = MimeTypes.MIME_APPL_OCTET;

    private static final String HDR_CONTENT_DISPOSITION = MessageHeaders.HDR_CONTENT_DISPOSITION;

    private static final String HDR_CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;

    private static final int BUF_SIZE = 8192;

    private static final Iterator<Entry<String, String>> EMPTY_ITER = new Iterator<Entry<String, String>>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Entry<String, String> next() {
            throw new NoSuchElementException("Iterator is empty");
        }

        @Override
        public void remove() {
            // Nothing to do
        }
    };

    private static final class ContentProviderImpl implements ContentProvider {

        private final ContentType contentType;
        private final MailPart mailPart;
        private final String mailId;
        private final String folder;

        ContentProviderImpl(ContentType contentType, MailPart mailPart, String mailId, String folder) {
            super();
            this.contentType = contentType;
            this.mailPart = mailPart;
            this.mailId = mailId;
            this.folder = folder;
        }

        @Override
        public String getContent() throws OXException {
            try {
                return readContent(mailPart, contentType, mailId, folder);
            } catch (MaxBytesExceededIOException e) {
                throw MailExceptionCode.CONTENT_TOO_BIG.create(e, null == mailId ? "" : mailId, null == folder ? "" : folder);
            } catch (IOException e) {
                throw MailExceptionCode.UNREADBALE_PART_CONTENT.create(e, null == mailId ? "" : mailId, null == folder ? "" : folder);
            }
        }
    }

    private static interface InlineDetector {

        public boolean isInline(String disposition, String fileName);
    }

    /**
     * If disposition equals ignore-case <code>"INLINE"</code>, then it is treated as inline in any case.<br>
     * Only if disposition is <code>null</code> the file name is examined.
     */
    private static final InlineDetector LENIENT_DETECTOR = new InlineDetector() {

        @Override
        public boolean isInline(final String disposition, final String fileName) {
            return Part.INLINE.equalsIgnoreCase(disposition) || ((disposition == null) && (fileName == null));
        }
    };

    /**
     * Considered as inline if disposition equals ignore-case <code>"INLINE"</code> OR is <code>null</code>, but in any case the file name
     * must be <code>null</code>.
     */
    private static final InlineDetector STRICT_DETECTOR = new InlineDetector() {

        @Override
        public boolean isInline(final String disposition, final String fileName) {
            return (Part.INLINE.equalsIgnoreCase(disposition) || (disposition == null)) && (fileName == null);
        }
    };

    /*
     * +++++++++++++++++++ TNEF CONSTANTS +++++++++++++++++++
     */
    private static final String TNEF_IPM_CONTACT = "IPM.Contact";

    private static final String TNEF_IPM_MS_READ_RECEIPT = "IPM.Microsoft Mail.Read Receipt";

    // private static final String TNEF_IPM_MS_SCHEDULE_CANCELED = "IPM.Microsoft Schedule.MtgCncl";

    // private static final String TNEF_IPM_MS_SCHEDULE_REQUEST = "IPM.Microsoft Schedule.MtgReq";

    // private static final String TNEF_IPM_MS_SCHEDULE_ACCEPTED = "IPM.Microsoft Schedule.MtgRespP";

    // private static final String TNEF_IPM_MS_SCHEDULE_DECLINED = "IPM.Microsoft Schedule.MtgRespN";

    // private static final String TNEF_IPM_MS_SCHEDULE_TENTATIVE = "IPM.Microsoft Schedule.MtgRespA";

    /*
     * +++++++++++++++++++ MEMBERS +++++++++++++++++++
     */

    private boolean stop;
    private boolean multipartDetected;
    private InlineDetector inlineDetector;
    private String subject;
    private MimeFilter mimeFilter;
    private boolean handleAllAsParts;
    private final List<OXException> warnings;
    private String mailId;
    private String folder;

    /**
     * Constructor
     */
    public MailMessageParser() {
        super();
        handleAllAsParts = false;
        inlineDetector = LENIENT_DETECTOR;
        mimeFilter = null;
        warnings = new ArrayList<OXException>(2);
    }

    /**
     * Switches the INLINE detector behavior (default is {@link #LENIENT_DETECTOR}).
     *
     * @param strict <code>true</code> to perform strict INLINE detector behavior; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public MailMessageParser setInlineDetectorBehavior(final boolean strict) {
        inlineDetector = strict ? STRICT_DETECTOR : LENIENT_DETECTOR;
        return this;
    }

    /**
     * Adds specified MIME filter (default is <code>null</code>).
     *
     * @param mimeFilter The MIME filter
     * @return This parser with MIME filter applied
     */
    public MailMessageParser addMimeFilter(final MimeFilter mimeFilter) {
        this.mimeFilter = mimeFilter;
        return this;
    }

    /**
     * Sets the <code>handleAllAsParts</code> flag (default id <code>false</code>).
     *
     * @param handleAllAsParts The <code>handleAllAsParts</code> flag to set
     * @return This parser with new behavior applied
     */
    public MailMessageParser setHandleAllAsParts(boolean handleAllAsParts) {
        this.handleAllAsParts = handleAllAsParts;
        return this;
    }

    /**
     * Gets possible warnings occurred during parsing.
     *
     * @return The warnings
     */
    public List<OXException> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Resets this parser and returns itself
     *
     * @return The parser itself
     */
    public MailMessageParser reset() {
        stop = false;
        multipartDetected = false;
        subject = null;
        return this;
    }

    /**
     * Parses specified mail using given handler as call-back
     *
     * @param mail The mail to parse
     * @param handler The call-back handler
     * @throws OXException If parsing specified mail fails
     */
    public void parseMailMessage(final MailMessage mail, final MailMessageHandler handler) throws OXException {
        parseMailMessage(mail, handler, null);
    }

    /**
     * Parses specified mail using given handler as call-back and given initial prefix for mail part identifiers; e.g.
     * <code>&quot;1.1&quot;</code>.
     *
     * @param mail The mail to parse
     * @param handler The call-back handler
     * @param prefix The initial prefix for mail part identifiers; e.g. <code>&quot;1.1&quot;</code>
     * @throws OXException If parsing specified mail fails
     */
    public void parseMailMessage(final MailMessage mail, final MailMessageHandler handler, final String prefix) throws OXException {
        if (null == mail) {
            throw MailExceptionCode.MISSING_PARAMETER.create("mail");
        }
        if (null == handler) {
            throw MailExceptionCode.MISSING_PARAMETER.create("handler");
        }
        try {
            /*
             * Parse mail's envelope
             */
            parseEnvelope(mail, handler);
            mailId = mail.getMailId();
            folder = mail.getFolder();
            /*
             * Parse content
             */
            final ContentType contentType = mail.getContentType();
            if (contentType.startsWith("multipart/related") && ("application/smil".equals(Strings.asciiLowerCase(contentType.getParameter("type"))))) {
                parseMailContent(MimeSmilFixer.getInstance().process(mail), handler, prefix, 1);
            } else {
                parseMailContent(mail, handler, prefix, 1);
            }
        } catch (MaxBytesExceededIOException e) {
            final String mailId = mail.getMailId();
            final String folder = mail.getFolder();
            throw MailExceptionCode.CONTENT_TOO_BIG.create(e, null == mailId ? "" : mailId, null == folder ? "" : folder);
        } catch (final IOException e) {
            if ("No content".equals(e.getMessage())) {
                /*-
                 * Special JavaMail I/O error to indicate no content available from IMAP server.
                 * Return the empty string in this case.
                 */
                throw MailExceptionCode.NO_CONTENT.create(e, e.getMessage());
            }
            final String mailId = mail.getMailId();
            final String folder = mail.getFolder();
            throw MailExceptionCode.UNREADBALE_PART_CONTENT.create(e, null == mailId ? "" : mailId, null == folder ? "" : folder);
        } catch (final OXException e) {
            if (MailExceptionCode.INVALID_MULTIPART_CONTENT.equals(e)) {
                // Strange multipart...
                final String mailId = mail.getMailId();
                final String folder = mail.getFolder();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid multipart detected ''{}'' ({}-{}-{}):{}{}", e.getMessage(),  null == mailId ? "" : mailId, null == folder ? "" : folder, mail.getAccountId(), System.getProperty("line.separator"), mail.getSource());
                }
            }
            throw e;
        }

        handler.handleMessageEnd(mail);
    }

    private void parseMailContent(final MailPart mailPartArg, final MailMessageHandler handler, final String prefix, final int partCountArg) throws OXException, IOException {
        if (stop) {
            return;
        }
        /*
         * Part modifier
         */
        final MailPart mailPart = MailConfig.usePartModifier() ? MailConfig.getPartModifier().modifyPart(mailPartArg) : mailPartArg;
        /*
         * Set part information
         */
        int partCount = partCountArg;
        final String disposition = mailPart.containsContentDisposition() ? mailPart.getContentDisposition().getDisposition() : null;
        final long size = mailPart.getSize();
        final ContentType contentType = mailPart.containsContentType() ? mailPart.getContentType() : new ContentType(APPL_OCTET);
        final String lcct = LocaleTools.toLowerCase(contentType.getBaseType());
        if (null != mimeFilter && mimeFilter.ignorable(lcct, mailPart)) {
            return;
        }
        final String fileName = getFileName(mailPart.getFileName(), getSequenceId(prefix, partCount), lcct);
        /*
         * Parse part dependent on its MIME type
         */
        final boolean isInline = inlineDetector.isInline(disposition, mailPart.getFileName());
        /*-
         * formerly:
         * final boolean isInline = ((disposition == null
         *     || disposition.equalsIgnoreCase(Part.INLINE)) && mailPart.getFileName() == null);
         */
        if (isMultipart(lcct)) {
            if (lcct.equals("multipart/signed")) {
                MailPart handledSMIME = checkSMIME(mailPart, lcct, contentType);
                if (null != handledSMIME) {
                    parseMailContent(handledSMIME, handler, prefix, partCount);
                    return;
                }
            }
            try {
                final int count = mailPart.getEnclosedCount();
                if (count == -1) {
                    throw MailExceptionCode.INVALID_MULTIPART_CONTENT.create();
                }
                final String mpId = null == prefix && !multipartDetected ? "" : getSequenceId(prefix, partCount);
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
                if (!handler.handleMultipartEnd(mailPart, mpId)) {
                    stop = true;
                    return;
                }
            } catch (final RuntimeException rte) {
                /*
                 * Parsing of multipart mail failed fatally; treat as empty plain-text mail
                 */
                LOG.error("Multipart mail could not be parsed", rte);
                warnings.add(MailExceptionCode.UNPARSEABLE_MESSAGE.create(rte, new Object[0]));
                if (!handler.handleInlinePlainText(
                    "",
                    ContentType.DEFAULT_CONTENT_TYPE,
                    0,
                    fileName,
                    MailMessageParser.getSequenceId(prefix, partCount))) {
                    stop = true;
                    return;
                }
            }
        } else if (isText(lcct, fileName)) {
            if (handleAllAsParts) {
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } else {
                if (isInline) {
                    if (null != mailPart.getFileName()) {
                        contentType.setParameter("realfilename", mailPart.getFileName());
                    }
                    try {
                        String content = readContent(mailPart, contentType, mailId, folder);
                        UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
                        if (uuencodedMP.isUUEncoded()) {
                            /*
                             * UUEncoded content detected. Handle normal text.
                             */
                            if (!handler.handleInlineUUEncodedPlainText(
                                uuencodedMP.getCleanText(),
                                contentType,
                                uuencodedMP.getCleanText().length(),
                                fileName,
                                getSequenceId(prefix, partCount))) {
                                stop = true;
                                return;
                            }
                            /*
                             * Now handle uuencoded attachments
                             */
                            int count = uuencodedMP.getCount();
                            if (count > 0) {
                                for (int a = 0; a < count; a++) {
                                    /*
                                     * Increment part count by 1
                                     */
                                    partCount++;
                                    if (!handler.handleInlineUUEncodedAttachment(
                                        uuencodedMP.getBodyPart(a),
                                        MailMessageParser.getSequenceId(prefix, partCount))) {
                                        stop = true;
                                        return;
                                    }
                                }
                            }
                        } else {
                            /*
                             * Just non-encoded plain text
                             */
                            if (!handler.handleInlinePlainText(
                                content,
                                contentType,
                                size,
                                fileName,
                                MailMessageParser.getSequenceId(prefix, partCount))) {
                                stop = true;
                                return;
                            }
                        }
                    } finally {
                        contentType.removeParameter("realfilename");
                    }
                } else {
                    /*
                     * Non-Inline: Text attachment
                     */
                    if (!mailPart.containsSequenceId()) {
                        mailPart.setSequenceId(getSequenceId(prefix, partCount));
                    }
                    if (!handler.handleAttachment(mailPart, false, lcct, fileName, mailPart.getSequenceId())) {
                        stop = true;
                        return;
                    }
                }
            }
        } else if (isHtml(lcct)) {
            if (handleAllAsParts) {
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } else {
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (isInline) {
                    if (null == mailPart.getFileName()) {
                        if (!handler.handleInlineHtml(new ContentProviderImpl(contentType, mailPart, mailId, folder), contentType, size, fileName, mailPart.getSequenceId())) {
                            stop = true;
                            return;
                        }
                    } else {
                        contentType.setParameter("realfilename", mailPart.getFileName());
                        try {
                            if (!handler.handleInlineHtml(new ContentProviderImpl(contentType, mailPart, mailId, folder), contentType, size, fileName, mailPart.getSequenceId())) {
                                stop = true;
                                return;
                            }
                        } finally {
                            contentType.removeParameter("realfilename");
                        }
                    }

                } else {
                    if (!handler.handleAttachment(mailPart, false, lcct, fileName, mailPart.getSequenceId())) {
                        stop = true;
                        return;
                    }
                }
            }
        } else if (isImage(lcct)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleImagePart(mailPart, mailPart.getContentId(), lcct, isInline, fileName, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        } else if (isMessage(lcct, fileName)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            // Fix for bug #16461: Show every RFC822 part as a nested mail
            if (!handler.handleNestedMessage(mailPart, getSequenceId(prefix, partCount))) {
                stop = true;
                return;
            }
            
        } else if (TNEFUtils.isTNEFMimeType(lcct)) {
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
                final String messageClassName;
                if (messageClass == null) {
                    final MAPIProp prop = message.getMAPIProps().getProp(MAPIProp.PR_MESSAGE_CLASS);
                    messageClassName = null == prop ? "" : prop.getValue().toString();
                } else {
                    messageClassName = ((String) messageClass.getValue());
                }
                if (TNEF_IPM_CONTACT.equalsIgnoreCase(messageClassName)) {
                    /*
                     * Convert contact to standard vCard. Resulting Multipart object consists of only ONE BodyPart object which encapsulates
                     * converted VCard. But for consistency reasons keep the code structure to iterate over Multipart's child objects.
                     */
                    final Multipart mp;
                    try {
                        final Attr subjetcAttr = message.getAttribute(Attr.attSubject);
                        if (null == subjetcAttr) {
                            message.addAttribute(new Attr(Attr.LVL_MESSAGE, Attr.atpText, Attr.attSubject, "vcard"));
                        }
                        mp = ContactHandler.convert(message);
                    } catch (final RuntimeException e) {
                        LOG.error("Invalid TNEF contact", e);
                        return;
                    }
                    parseMailContent(new MimeMailPart(mp), handler, prefix, partCount);
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
                        LOG.warn("Invalid TNEF read receipt", e);
                        return;
                    }
                    parseMailContent(new MimeMailPart(mp), handler, prefix, partCount);
                    /*
                     * Stop to further process TNEF attachment
                     */
                    return;
                } else if (TNEF2ICal.isVPart(messageClassName)) {
                    final net.fortuna.ical4j.model.Calendar calendar = TNEF2ICal.tnef2VPart(message);
                    if (null != calendar) {
                        /*
                         * VPart successfully converted. Generate appropriate body part.
                         */
                        final TNEFBodyPart part = new TNEFBodyPart();
                        /*
                         * Determine VPart's Content-Type
                         */
                        final String contentTypeStr;
                        {
                            final net.fortuna.ical4j.model.Property method = calendar.getProperties().getProperty(net.fortuna.ical4j.model.Property.METHOD);
                            if (null == method) {
                                contentTypeStr = "text/calendar; charset=UTF-8";
                            } else {
                                contentTypeStr = new StringBuilder("text/calendar; method=").append(method.getValue()).append("; charset=UTF-8").toString();
                            }
                        }
                        /*
                         * Set part's body
                         */
                        {
                            final byte[] bytes = calendar.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
                            part.setDataHandler(new DataHandler(new MessageDataSource(bytes, contentTypeStr)));
                            part.setSize(bytes.length);
                        }
                        /*
                         * Set part's headers
                         */
                        part.setHeader(HDR_CONTENT_TYPE, contentTypeStr);
                        {
                            final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                            cd.setFilenameParameter(getFileName(null, getSequenceId(prefix, partCount), "text/calendar"));
                            part.setHeader(HDR_CONTENT_DISPOSITION, cd.toString());
                        }
                        part.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                        {
                            final net.fortuna.ical4j.model.Component vEvent = calendar.getComponents().getComponent(net.fortuna.ical4j.model.Component.VEVENT);
                            final Property summary = vEvent.getProperties().getProperty(net.fortuna.ical4j.model.Property.SUMMARY);
                            if (summary != null) {
                                part.setFileName(new StringBuilder(MimeUtility.encodeText(summary.getValue().replaceAll("\\s", "_"), MailProperties.getInstance().getDefaultMimeCharset(), "Q")).append(".ics").toString());
                            }
                        }
                        /*
                         * Parse part
                         */
                        parseMailContent(MimeMessageConverter.convertPart(part), handler, prefix, partCount++);
                        /*
                         * Stop to further process TNEF attachment
                         */
                        return;
                    }
                }
                /*
                 * Look for body. Usually the body is the RTF text.
                 */
                final Attr attrBody = Attr.findAttr(message.getAttributes(), Attr.attBody);
                if (attrBody != null) {
                    final TNEFBodyPart bodyPart = new TNEFBodyPart();
                    final String value = (String) attrBody.getValue();
                    MessageUtility.setText(value, bodyPart);
                    // bodyPart.setText(value);
                    bodyPart.setSize(value.length());
                    parseMailContent(MimeMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                }
                /*
                 * Check for possible RTF content
                 */
                TNEFBodyPart rtfPart = null;
                {
                    final MAPIProps mapiProps = message.getMAPIProps();
                    if (mapiProps != null) {
                        final RawInputStream ris = (RawInputStream) mapiProps.getPropValue(MAPIProp.PR_RTF_COMPRESSED);
                        if (ris != null) {
                            rtfPart = new TNEFBodyPart();
                            /*
                             * De-compress RTF body
                             */
                            final byte[] decompressedBytes = CompressedRTFInputStream.decompressRTF(ris.toByteArray());
                            final String contentTypeStr;
                            {
                                // final String charset = CharsetDetector.detectCharset(new
                                // UnsynchronizedByteArrayInputStream(decompressedBytes));
                                contentTypeStr = "application/rtf";
                            }
                            /*
                             * Set content through a data handler to avoid further exceptions raised by unavailable DCH (data content handler)
                             */
                            rtfPart.setDataHandler(new DataHandler(new MessageDataSource(decompressedBytes, contentTypeStr)));
                            rtfPart.setHeader(HDR_CONTENT_TYPE, contentTypeStr);
                            rtfPart.setSize(decompressedBytes.length);
                            parseMailContent(MimeMessageConverter.convertPart(rtfPart), handler, prefix, partCount++);
                            /*
                             * Further process TNEF attachment
                             */
                        }
                    }
                }
                /*
                 * Iterate TNEF attachments and nested messages
                 */
                final List<?> attachments = message.getAttachments();
                final int s = attachments.size();
                if (s > 0) {
                    final Iterator<?> iter = attachments.iterator();
                    final ByteArrayOutputStream os = new UnsynchronizedByteArrayOutputStream(BUF_SIZE);
                    Next: for (int i = 0; !stop && i < s; i++) {
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
                            if (null != contentTypeStr) {
                                final String tmp = contentTypeStr.toLowerCase(Locale.US);
                                if (tmp.startsWith("multipart/") && tmp.indexOf("boundary=") < 0) {
                                    final MimeMessage nested = new MimeMessage(MimeDefaultSession.getDefaultSession(), attachment.getRawData());
                                    parseMailContent(MimeMessageConverter.convertMessage(nested, false), handler, prefix, partCount++);
                                    // Proceed with next attachment in list
                                    continue Next;
                                }
                            }
                            if ((contentTypeStr == null) && (attachFilename != null)) {
                                contentTypeStr = MimeType2ExtMap.getContentType(attachFilename);
                            }
                            if (contentTypeStr == null) {
                                contentTypeStr = MimeTypes.MIME_APPL_OCTET;
                            }
                            final DataSource ds = new RawDataSource(attachment.getRawData(), contentTypeStr);
                            bodyPart.setDataHandler(new DataHandler(ds));
                            bodyPart.setHeader(
                                HDR_CONTENT_TYPE,
                                ContentType.prepareContentTypeString(contentTypeStr, attachFilename));
                            if (attachFilename != null) {
                                final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                                cd.setFilenameParameter(attachFilename);
                                bodyPart.setHeader(
                                    HDR_CONTENT_DISPOSITION,
                                    MimeMessageUtility.foldContentDisposition(cd.toString()));
                            }
                            CountingOutputStream counter = null;
                            try {
                                counter = new CountingOutputStream();
                                attachment.writeTo(counter);
                                bodyPart.setSize((int) counter.getCount());
                            } finally {
                                Streams.close(counter);
                            }
                            parseMailContent(MimeMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                        } else {
                            /*
                             * Nested message
                             */
                            final MimeMessage nestedMessage =
                                TNEFMime.convert(MimeDefaultSession.getDefaultSession(), attachment.getNestedMessage());
                            os.reset();
                            nestedMessage.writeTo(os);
                            bodyPart.setDataHandler(new DataHandler(new MessageDataSource(os.toByteArray(), MimeTypes.MIME_MESSAGE_RFC822)));
                            bodyPart.setHeader(HDR_CONTENT_TYPE, MimeTypes.MIME_MESSAGE_RFC822);
                            parseMailContent(MimeMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                        }
                    }
                } else {
                    // Check RTF part
                    if (null != rtfPart) {
                        final MailPart convertedPart = MimeMessageConverter.convertPart(rtfPart);
                        convertedPart.setFileName(new StringBuilder(subject.replaceAll("\\s+", "_")).append(".rtf").toString());
                        parseMailContent(convertedPart, handler, prefix, partCount++);
                    }
                    // As attachment
                    if (null == messageClass) {
                        if (!mailPart.containsSequenceId()) {
                            mailPart.setSequenceId(getSequenceId(prefix, partCount));
                        }
                        if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                            stop = true;
                            return;
                        }
                    } else {
                        final TNEFBodyPart bodyPart = new TNEFBodyPart();
                        /*
                         * Add TNEF attributes
                         */
                        bodyPart.setTNEFAttributes(message.getAttributes());
                        /*
                         * Translate TNEF attributes to MIME
                         */
                        final String attachFilename = fileName;
                        final DataSource ds = new RawDataSource(messageClass.getRawData(), MimeTypes.MIME_APPL_OCTET);
                        bodyPart.setDataHandler(new DataHandler(ds));
                        bodyPart.setHeader(
                            HDR_CONTENT_TYPE,
                            ContentType.prepareContentTypeString(MimeTypes.MIME_APPL_OCTET, attachFilename));
                        if (attachFilename != null) {
                            final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                            cd.setFilenameParameter(attachFilename);
                            bodyPart.setHeader(
                                HDR_CONTENT_DISPOSITION,
                                MimeMessageUtility.foldContentDisposition(cd.toString()));
                        }
                        bodyPart.setSize(messageClass.getLength());
                        parseMailContent(MimeMessageConverter.convertPart(bodyPart), handler, prefix, partCount++);
                    }
                }
            } catch (final IOException tnefExc) {
                LOG.warn("", tnefExc);
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } catch (final MessagingException e) {
                LOG.error("", e);
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (isSpecial(lcct)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleSpecialPart(mailPart, lcct, fileName, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        } else {
            MailPart handledSMIME = checkSMIME(mailPart, lcct, contentType);
            if (null != handledSMIME) {
                parseMailContent(handledSMIME, handler, prefix, partCount);
                return;
            }

            // As last resort
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleAttachment(mailPart, isInline, lcct, fileName, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        }
    }

    private MailPart checkSMIME(MailPart mailPart, String lcct, ContentType contentType) throws IOException, OXException {
        if (!(mailPart instanceof MimeRawSource)) {
            return null;
        }

        // Check for "application/pkcs7-mime; name=smime.p7m; smime-type=signed-data"
        SMIMESigned smimeSigned = null;
        try {
            if (isMultipartSigned(lcct, contentType)) {
                Multipart multipart = MimeMessageUtility.getMultipartContentFrom(((MimeRawSource) mailPart).getPart(), lcct);
                smimeSigned = new SMIMESigned((MimeMultipart) multipart);
            } else if (isSigned(lcct, contentType)) {
                smimeSigned = new SMIMESigned(((MimeRawSource) mailPart).getPart());
            }
        } catch (MessagingException e) {
            LOG.warn("Failed to handle S/MIME message", e);
        } catch (CMSException e) {
            LOG.warn("Failed to handle S/MIME message", e);
        } catch (SMIMEException e) {
            LOG.warn("Failed to handle S/MIME message", e);
        }

        return smimeSigned == null ? null : MimeMessageConverter.convertPart(smimeSigned.getContent());
    }

    private void parseEnvelope(final MailMessage mail, final MailMessageHandler handler) throws OXException {
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
        {
            String subj = mail.getSubject();
            if (subj == null) { // in case no subject was set
                subj = "";
            }

            subject = subj;
            handler.handleSubject(subj);
        }
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
        final int headersSize = mail.getHeadersSize();
        handler.handleHeaders(headersSize, headersSize > 0 ? mail.getHeadersIterator() : EMPTY_ITER);
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
        if ((filename == null) || com.openexchange.java.Strings.isEmpty(filename)) {
            final List<String> exts = MimeType2ExtMap.getFileExtensions(baseMimeType.toLowerCase(Locale.ENGLISH));
            final StringBuilder sb = new StringBuilder(16).append(PREFIX).append(sequenceId).append('.');
            if (exts == null) {
                sb.append("dat");
            } else {
                sb.append(exts.get(0));
            }
            filename = sb.toString();
        } else {
            filename = MimeMessageUtility.decodeMultiEncodedHeader(filename);
        }
        return filename;
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
     * @param baseMimeType The base MIME type to look up an appropriate file extension if <code>rawFileName</code> is <code>null</code>
     * @return The generated filename
     */
    public static String generateFilename(String sequenceId, String baseMimeType) {
        return getFileName(null, sequenceId, baseMimeType);
    }

    static String readContent(MailPart mailPart, ContentType contentType, String mailId, String folder) throws OXException, IOException {
        /*
         * Read content
         */
        String content = MimeMessageUtility.readContent(mailPart, contentType, true);
        if (null == content) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, folder);
        }
        return content;
    }

    private static final String PRIMARY_TEXT = "text/";

    private static final String[] SUB_TEXT = { "plain", "enriched", "richtext", "rtf" };

    /**
     * Checks if content type matches one of text content types:
     * <ul>
     * <li><code>text/plain</code></li>
     * <li><code>text/enriched</code></li>
     * <li><code>text/richtext</code></li>
     * <li><code>text/rtf</code></li>
     * </ul>
     *
     * @param contentType The content type
     * @param fileName
     * @return <code>true</code> if content type matches text; otherwise <code>false</code>
     */
    private static boolean isText(final String contentType, String name) {
        if (name != null && name.endsWith(".eml")) {
            return false;
        }
        if (contentType.startsWith(PRIMARY_TEXT, 0)) {
            final int off = PRIMARY_TEXT.length();
            for (final String subtype : SUB_TEXT) {
                if (contentType.startsWith(subtype, off)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String PRIMARY_HTML = "text/htm";

    /**
     * Checks if content type matches <code>text/htm*</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>text/htm*</code>; otherwise <code>false</code>
     */
    private static boolean isHtml(final String contentType) {
        return contentType.startsWith(PRIMARY_HTML, 0);
    }

    private static final String PRIMARY_MULTI = "multipart/";

    /**
     * Checks if content type matches <code>multipart/*</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>multipart/*</code>; otherwise <code>false</code>
     */
    private static boolean isMultipart(final String contentType) {
        return contentType.startsWith(PRIMARY_MULTI, 0);
    }

    private static final String PRIMARY_IMAGE = "image/";

    /**
     * Checks if content type matches <code>image/*</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>image/*</code>; otherwise <code>false</code>
     */
    private static boolean isImage(final String contentType) {
        return contentType.startsWith(PRIMARY_IMAGE, 0);
    }

    private static final String PRIMARY_RFC822 = "message/rfc822";

    /**
     * Checks if content type matches <code>message/rfc822</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>message/rfc822</code>; otherwise <code>false</code>
     */
    private static boolean isMessage(final String contentType, String name) {
        if (name != null && name.endsWith(".eml")) {
            return true;
        }
        return contentType.startsWith(PRIMARY_RFC822, 0);
    }

    private static final String PRIMARY_MESSAGE = "message/";

    private static final String[] SUB_SPECIAL1 = { "delivery-status", "disposition-notification" };

    private static final String[] SUB_SPECIAL2 = { "rfc822-headers", "vcard", "x-vcard", "calendar", "x-vcalendar" };

    /**
     * Checks if content type matches one of special content types:
     * <ul>
     * <li><code>message/delivery-status</code></li>
     * <li><code>message/disposition-notification</code></li>
     * <li><code>text/rfc822-headers</code></li>
     * <li><code>text/vcard</code></li>
     * <li><code>text/x-vcard</code></li>
     * <li><code>text/calendar</code></li>
     * <li><code>text/x-vcalendar</code></li>
     * </ul>
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches special; otherwise <code>false</code>
     */
    private static boolean isSpecial(final String contentType) {
        if (contentType.startsWith(PRIMARY_TEXT, 0)) {
            final int off = PRIMARY_TEXT.length();
            for (final String subtype : SUB_SPECIAL2) {
                if (contentType.startsWith(subtype, off)) {
                    return true;
                }
            }
        } else if (contentType.startsWith(PRIMARY_MESSAGE, 0)) {
            final int off = PRIMARY_MESSAGE.length();
            for (final String subtype : SUB_SPECIAL1) {
                if (contentType.startsWith(subtype, off)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSigned(String lcct, ContentType ct) {
        return "application/pkcs7-mime".equals(lcct) && "signed-data".equals(Strings.asciiLowerCase(ct.getParameter("smime-type"))) && "smime.p7m".equals(Strings.asciiLowerCase(ct.getNameParameter()));
    }

    private static boolean isMultipartSigned(String lcct, ContentType ct) {
        return "multipart/signed".equals(lcct) && "application/pkcs7-signature".equals(Strings.asciiLowerCase(ct.getParameter("protocol")));
    }

    /**
     * Gets the <code>MailMessage</code> content from given mail part.
     *
     * @param mailPart The mail part to get the <code>MailMessage</code> content from
     * @return The <code>MailMessage</code> content or <code>null</code>
     * @throws OXException If <code>MailMessage</code> content cannot be returned
     */
    public static MailMessage getMessageContentFrom(MailPart mailPart) throws OXException {
        if (null == mailPart) {
            return null;
        }

        ThresholdFileHolder backup = null;
        try {
            Object content = mailPart.getContent();
            if (content instanceof MailMessage) {
                return (MailMessage) content;
            } else if (content instanceof MimeMessage) {
                return MimeMessageConverter.convertMessage((MimeMessage) content, false);
            } else if (content instanceof InputStream) {
                try {
                    backup = new ThresholdFileHolder();
                    backup.write((InputStream) content);
                    FileBackedMimeMessage mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), backup.getSharedStream());
                    MailMessage mailMessage = MimeMessageConverter.convertMessage(mimeMessage, false);
                    backup = null; // Avoid preliminary closing
                    return mailMessage;
                } catch (IOException e) {
                    throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                } catch (MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            } else if (content instanceof String) {
                try {
                    MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), new ByteArrayInputStream(((String) content).getBytes("UTF-8")));
                    return MimeMessageConverter.convertMessage(mimeMessage, false);
                } catch (UnsupportedEncodingException e) {
                    throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
                } catch (MessagingException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            }
            return null;
        } finally {
            if (null != backup) {
                backup.close();
            }
        }
    }

}
