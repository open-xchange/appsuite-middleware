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

package com.openexchange.mail.structure;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.CountingOutputStream;
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
import com.openexchange.mail.mime.MimeStructureFixer;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.TNEFBodyPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MIMEMultipartMailPart;
import com.openexchange.mail.mime.dataobjects.MimeMailPart;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.tnef.TNEF2ICal;

/**
 * {@link StructureMailMessageParser} - A callback parser to parse instances of {@link MailMessage} by invoking the <code>handleXXX()</code>
 * methods of given {@link MailMessageHandler} object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StructureMailMessageParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StructureMailMessageParser.class);

    private static final String HDR_CONTENT_DISPOSITION = MessageHeaders.HDR_CONTENT_DISPOSITION;

    private static final String HDR_CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;

    private static final int BUF_SIZE = 8192;

    private static Iterator<Entry<String, String>> EMPTY_ITER = new Iterator<Entry<String, String>>() {

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

    // private static final String TNEF_IPM_MS_SCHEDULE = "IPM.Microsoft Schedule.MtgCncl";

    /*
     * +++++++++++++++++++ MEMBERS +++++++++++++++++++
     */

    private boolean parseTNEFParts;
    private boolean parseUUEncodedParts;
    private boolean stop;
    private String subject;
    private boolean multipartDetected;
    private InlineDetector inlineDetector;
    private boolean neverTreatMessageAsAttachment;

    /**
     * Constructor
     */
    public StructureMailMessageParser() {
        super();
        parseTNEFParts = true;
        neverTreatMessageAsAttachment = true;
        inlineDetector = LENIENT_DETECTOR;
    }

    /**
     * Sets the behavior how to handle a message part.
     *
     * @param neverTreatMessageAsAttachment whether to treat a message part as an attachment
     * @return This parser with new behavior applied
     */
    public StructureMailMessageParser setNeverTreatMessageAsAttachment(final boolean neverTreatMessageAsAttachment) {
        this.neverTreatMessageAsAttachment = neverTreatMessageAsAttachment;
        return this;
    }

    /**
     * Switches the INLINE detector behavior.
     *
     * @param strict <code>true</code> to perform strict INLINE detector behavior; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public StructureMailMessageParser setInlineDetectorBehavior(final boolean strict) {
        inlineDetector = strict ? STRICT_DETECTOR : LENIENT_DETECTOR;
        return this;
    }

    /**
     * Sets whether TNEF parts should be parsed or not.
     *
     * @param parseTNEFParts <code>true</code> to parse TNEF parts; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public StructureMailMessageParser setParseTNEFParts(final boolean parseTNEFParts) {
        this.parseTNEFParts = parseTNEFParts;
        return this;
    }

    /**
     * Sets whether UUEncoded parts should be parsed or not.
     *
     * @param parseUUEncodedParts <code>true</code> to parse UUEncoded parts; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public StructureMailMessageParser setParseUUEncodedParts(final boolean parseUUEncodedParts) {
        this.parseUUEncodedParts = parseUUEncodedParts;
        return this;
    }

    /**
     * Resets this parser and returns itself
     *
     * @return The parser itself
     */
    public StructureMailMessageParser reset() {
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
    public void parseMailMessage(final MailMessage mail, final StructureHandler handler) throws OXException {
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
    public void parseMailMessage(MailMessage mail, StructureHandler handler, String prefix) throws OXException {
        if (null == mail) {
            throw MailExceptionCode.MISSING_PARAMETER.create("mail");
        }
        if (null == handler) {
            throw MailExceptionCode.MISSING_PARAMETER.create("handler");
        }
        MailMessage mm = MimeStructureFixer.getInstance().process(mail);
        try {
            /*
             * Parse mail's envelope
             */
            parseEnvelope(mm, handler);
            /*
             * Parse content
             */
            parseMailContent(mm, handler, prefix, 1);
            /*
             * Mark end of parsing
             */
            handler.handleEnd(mm);
        } catch (final IOException e) {
            throw MailExceptionCode.UNREADBALE_PART_CONTENT.create(e, Long.valueOf(mm.getMailId()), mm.getFolder());
        }
    }

    private void parseMailContent(final MailPart mailPartArg, final StructureHandler handler, final String prefix, final int partCountArg) throws OXException, IOException {
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
        final ContentType contentType =
            mailPart.containsContentType() ? mailPart.getContentType() : new ContentType(MimeTypes.MIME_APPL_OCTET);
        final String lcct = LocaleTools.toLowerCase(contentType.getBaseType());
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
        if (isText(lcct)) {
            final String partFileName = mailPart.getFileName();
            if (null != partFileName && "base64".equalsIgnoreCase(mailPart.getFirstHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC))) {
                // Possibly not a plain text part
                final String contentTypeByFileName = MimeType2ExtMap.getContentType(partFileName);
                if (!MimeTypes.MIME_APPL_OCTET.equals(contentTypeByFileName) && !isText(contentTypeByFileName)) {
                    contentType.setBaseType(contentTypeByFileName);
                    mailPart.setContentType(contentType);
                    mailPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString());
                    if (!mailPart.containsSequenceId()) {
                        mailPart.setSequenceId(getSequenceId(prefix, partCount));
                    }
                    if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                        stop = true;
                    }
                    return;
                }
            }
            if (isInline) {
                final String content = readContent(mailPart, contentType);
                final UUEncodedMultiPart uuencodedMP;
                if (parseUUEncodedParts && (uuencodedMP = new UUEncodedMultiPart(content)).isUUEncoded()) {
                    /*
                     * UUEncoded content detected. Handle normal text.
                     */
                    final int count = uuencodedMP.getCount();
                    if (count > 0) {
                        /*
                         * Handle as a multipart
                         */
                        if (!handler.handleMultipartStart(new ContentType("multipart/mixed"), count, prefix)) {
                            stop = true;
                            return;
                        }
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
                        for (int a = 0; a < count; a++) {
                            /*
                             * Increment part count by 1
                             */
                            partCount++;
                            if (!handler.handleInlineUUEncodedAttachment(uuencodedMP.getBodyPart(a), StructureMailMessageParser.getSequenceId(
                                prefix,
                                partCount))) {
                                stop = true;
                                return;
                            }
                        }
                        if (!handler.handleMultipartEnd()) {
                            stop = true;
                            return;
                        }
                    } else {
                        if (!handler.handleInlineUUEncodedPlainText(
                            uuencodedMP.getCleanText(),
                            contentType,
                            uuencodedMP.getCleanText().length(),
                            fileName,
                            getSequenceId(prefix, partCount))) {
                            stop = true;
                            return;
                        }
                    }
                } else {
                    /*
                     * Text attachment
                     */
                    if (!mailPart.containsSequenceId()) {
                        mailPart.setSequenceId(getSequenceId(prefix, partCount));
                    }
                    if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                        stop = true;
                        return;
                    }
                }
            } else {
                /*
                 * Text attachment
                 */
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else if (isMultipart(lcct)) {
            final int count = mailPart.getEnclosedCount();
            if (count == -1) {
                throw MailExceptionCode.INVALID_MULTIPART_CONTENT.create();
            }
            final boolean rootLevelMultipart = null == prefix && !multipartDetected;
            final String mpId = rootLevelMultipart ? "" : getSequenceId(prefix, partCount);
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(mpId);
            }
            if (/*rootLevelMultipart && */isMultipartSigned(contentType)) {
                /*
                 * Determine the part which is considered to be the message' text according to
                 */
                {
                    MailPart part = null;
                    for (int i = 0; null == part && i < count; i++) {
                        final MailPart enclosedPart = mailPart.getEnclosedMailPart(i);
                        part = extractTextFrom(enclosedPart, 0);
                    }
                    if (null != part && false == handler.handleSMIMEBodyText(part)) {
                        stop = true;
                        return;
                    }
                }
                final ByteArrayOutputStream buf = new UnsynchronizedByteArrayOutputStream(2048);
                final byte[] bytes;
                {
                    mailPart.writeTo(buf);
                    bytes = buf.toByteArray();
                    buf.reset();
                }
                {
                    final String version = mailPart.getFirstHeader("MIME-Version");
                    buf.write(Charsets.toAsciiBytes("MIME-Version: " + (null == version ? "1.0" : version) + "\r\n"));
                }
                {
                    final String ct = MimeMessageUtility.extractHeader("Content-Type", new UnsynchronizedByteArrayInputStream(bytes), false);
                    buf.write(Charsets.toAsciiBytes("Content-Type:" + ct + "\r\n"));
                }
                buf.write(extractBodyFrom(bytes));
                if (!handler.handleSMIMEBodyData(buf.toByteArray())) {
                    stop = true;
                    return;
                }
            } else {
                if (!handler.handleMultipartStart(mailPart.getContentType(), count, mpId)) {
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
                    final MailPart enclosedPart = mailPart.getEnclosedMailPart(i);
                    parseMailContent(enclosedPart, handler, mpPrefix, i + 1);
                }
                if (!handler.handleMultipartEnd()) {
                    stop = true;
                    return;
                }
            }
        } else if (isMessage(lcct)) {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (neverTreatMessageAsAttachment) {
                if (!handler.handleNestedMessage(mailPart, getSequenceId(prefix, partCount))) {
                    stop = true;
                    return;
                }
            } else {
                if (isInline) {
                    if (!handler.handleNestedMessage(mailPart, getSequenceId(prefix, partCount))) {
                        stop = true;
                        return;
                    }
                } else {
                    if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                        stop = true;
                        return;
                    }
                }
            }
        } else if (parseTNEFParts && TNEFUtils.isTNEFMimeType(lcct)) {
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
                final int s = message.getAttachments().size();
                if (s > 0) {
                    final Iterator<?> iter = message.getAttachments().iterator();
                    final ByteArrayOutputStream os = new UnsynchronizedByteArrayOutputStream(BUF_SIZE);
                    Next: for (int i = 0; i < s; i++) {
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
                            bodyPart.setHeader(HDR_CONTENT_TYPE, ContentType.prepareContentTypeString(
                                contentTypeStr,
                                attachFilename));
                            if (attachFilename != null) {
                                final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                                cd.setFilenameParameter(attachFilename);
                                bodyPart.setHeader(
                                    HDR_CONTENT_DISPOSITION,
                                    MimeMessageUtility.foldContentDisposition(cd.toString()));
                            }
                            CountingOutputStream counter = new CountingOutputStream();
                            attachment.writeTo(counter);
                            bodyPart.setSize((int) counter.getCount());
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
                            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeTypes.MIME_MESSAGE_RFC822);
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
                        if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
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
                        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ContentType.prepareContentTypeString(
                            MimeTypes.MIME_APPL_OCTET,
                            attachFilename));
                        if (attachFilename != null) {
                            final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                            cd.setFilenameParameter(attachFilename);
                            bodyPart.setHeader(
                                MessageHeaders.HDR_CONTENT_DISPOSITION,
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
                if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            } catch (final MessagingException e) {
                LOG.error("", e);
                if (!mailPart.containsSequenceId()) {
                    mailPart.setSequenceId(getSequenceId(prefix, partCount));
                }
                if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                    stop = true;
                    return;
                }
            }
        } else {
            if (!mailPart.containsSequenceId()) {
                mailPart.setSequenceId(getSequenceId(prefix, partCount));
            }
            if (!handler.handleAttachment(mailPart, mailPart.getSequenceId())) {
                stop = true;
                return;
            }
        }
    }

    private MailPart extractTextFrom(final MailPart mailPart, final int altLevel) throws OXException {
        if (!mailPart.containsContentType()) {
            return null;
        }
        final ContentType contentType = mailPart.getContentType();
        if (contentType.startsWith("text/")) {
            return (altLevel <= 0) || contentType.startsWith("text/htm") ? mailPart : null;
        }
        if (contentType.startsWith("multipart/")) {
            final boolean isAlternative = contentType.startsWith("multipart/alternative");
            int alternative = altLevel;
            if (isAlternative) {
                alternative++;
            }
            final int count = mailPart.getEnclosedCount();
            MailPart textPart = null;
            for (int i = 0; i < count; i++) {
                final MailPart enclosedPart = mailPart.getEnclosedMailPart(i);
                final MailPart ret = extractTextFrom(enclosedPart, alternative);
                if (null != ret) {
                    return ret;
                }
                if (isAlternative && null == textPart && enclosedPart.getContentType().startsWith("text/")) {
                    textPart = enclosedPart;
                }
            }
            if (isAlternative) {
                alternative--;
                if (null != textPart) {
                    return textPart;
                }
            }
        }
        return null;
    }

    private byte[] extractBodyFrom(final byte[] bytes) {
        int pos = MIMEMultipartMailPart.getHeaderEnd(bytes);
        if (pos <= 0) {
            return bytes;
        }
        pos++; // Advance last LF character
        final int len = bytes.length - pos;
        final byte[] body = new byte[len];
        System.arraycopy(bytes, pos, body, 0, len);
        return body;
    }

    private void parseEnvelope(final MailMessage mail, final StructureHandler handler) throws OXException {
        /*
         * SUBJECT
         */
        {
            String subj = mail.getSubject();
            if (subj == null) { // in case no subject was set
                subj = "";
            }
            subject = subj;
        }
        /*
         * RECEIVED DATE
         */
        handler.handleReceivedDate(mail.getReceivedDate());
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
         * HEADERS
         */
        final int headersSize = mail.getHeadersSize();
        handler.handleHeaders(headersSize > 0 ? mail.getHeadersIterator() : EMPTY_ITER);
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
    public static String generateFilename(final String sequenceId, final String baseMimeType) {
        return getFileName(null, sequenceId, baseMimeType);
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            if (contentType.startsWith("text/htm")) {
                final String html = MessageUtility.readMailPart(mailPart, charset, false, -1);
                return MessageUtility.simpleHtmlDuplicateRemoval(html);
            }
            return MessageUtility.readMailPart(mailPart, charset, false, -1);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            LOG.warn("Character conversion exception while reading content with charset \"{}\". Using fallback charset \"{}\" instead.", charset, fallback, e);
            return MessageUtility.readMailPart(mailPart, fallback, false, -1);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                if (null != cs) {
                    mailInterfaceMonitor.addUnsupportedEncodingExceptions(cs);
                }
                if (contentType.startsWith(PRIMARY_TEXT)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith(PRIMARY_TEXT)) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
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
     * @return <code>true</code> if content type matches text; otherwise <code>false</code>
     */
    private static boolean isText(final String contentType) {
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

    private static final String PRIMARY_MULTI_SIGNED = "multipart/signed";

    /**
     * Checks if content type matches <code>multipart/signed</code> content type.
     * <p>
     * See <a href="http://tools.ietf.org/html/rfc5751#section-3.9">http://tools.ietf.org/html/rfc5751#section-3.9</a>.<br>
     *
     * <pre>
     * <b>3.9.  Identifying an S/MIME Message</b>
     *
     *    Because S/MIME takes into account interoperation in non-MIME
     *    environments, several different mechanisms are employed to carry the
     *    type information, and it becomes a bit difficult to identify S/MIME
     *    messages.  The following table lists criteria for determining whether
     *    or not a message is an S/MIME message.  A message is considered an
     *    S/MIME message if it matches any of the criteria listed below.
     *
     *    The file suffix in the table below comes from the "name" parameter in
     *    the Content-Type header field, or the "filename" parameter on the
     *    Content-Disposition header field.  These parameters that give the
     *    file suffix are not listed below as part of the parameter section.
     *
     *    Media type:  application/pkcs7-mime
     *    parameters:  any
     *    file suffix: any
     *
     *    Media type:  multipart/signed
     *    parameters:  protocol="application/pkcs7-signature"
     *    file suffix: any
     *
     *    Media type:  application/octet-stream
     *    parameters:  any
     *    file suffix: p7m, p7s, p7c, p7z
     * </pre>
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>multipart/signed</code>; otherwise <code>false</code>
     */
    private static boolean isMultipartSigned(final ContentType contentType) {
        if (contentType.startsWith("application/pkcs7-mime")) {
            return true;
        }
        if (contentType.startsWith(PRIMARY_MULTI_SIGNED)) {
            final String protocol = Strings.toLowerCase(contentType.getParameter("protocol"));
            if (null != protocol && ("application/pkcs7-signature".equals(protocol) || "application/x-pkcs7-signature".equals(protocol))) {
                return true;
            }
        }
        return false;
    }

    private static final String PRIMARY_RFC822 = "message/rfc822";

    /**
     * Checks if content type matches <code>message/rfc822</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>message/rfc822</code>; otherwise <code>false</code>
     */
    private static boolean isMessage(final String contentType) {
        return contentType.startsWith(PRIMARY_RFC822, 0);
    }
}
