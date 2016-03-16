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

package com.openexchange.mail.text;

import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.CompressedRTFInputStream;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.TNEFBodyPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.tools.tnef.TNEF2ICal;

/**
 * {@link TextPartFinder} - Looks-up the primary text part of a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextPartFinder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TextPartFinder.class);

    private static final TextPartFinder INSTANCE = new TextPartFinder();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static TextPartFinder getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link TextPartFinder}.
     */
    private TextPartFinder() {
        super();
    }

    /**
     * Gets the primary text part of the specified part.
     *
     * @param p The part
     * @return The primary text part or <code>null</code>
     * @throws OXException If primary text part cannot be returned
     */
    public MailPart getText(final MailPart p) throws OXException {
        return getTextRecursive(p);
    }

    private MailPart getTextRecursive(final MailPart part) throws OXException {
        if (null == part) {
            return null;
        }
        try {
            final ContentType ct = part.getContentType();
            if (ct.startsWith("text/")) {
                MailPart textPart = part;
                String content = readContent(part, ct);
                if (ct.startsWith("text/plain")) {
                    // Check for possible uuencoded plain-text part
                    if (UUEncodedMultiPart.isUUEncoded(content)) {
                        final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
                        if (uuencodedMP.isUUEncoded()) {
                            content = uuencodedMP.getCleanText();
                            textPart = createPartFromPlainText(content);
                        }
                    }
                }
                return textPart;
            }
            if (ct.startsWith("multipart/alternative")) {
                /*
                 * Prefer HTML text over plain text
                 */
                final int count = part.getEnclosedCount();
                MailPart textPart = null;
                for (int i = 0; i < count; i++) {
                    final MailPart bp = part.getEnclosedMailPart(i);
                    final ContentType bct = bp.getContentType();
                    if (bct.startsWith("text/plain")) {
                        if (textPart == null) {
                            textPart = getTextRecursive(bp);
                        }
                        continue;
                    } else if (bct.startsWith("text/htm")) {
                        final MailPart p = getTextRecursive(bp);
                        if (p != null) {
                            return p;
                        }
                    } else if (bct.startsWith("multipart/")) {
                        final MailPart p = getTextRecursive(bp);
                        if (p != null) {
                            return p;
                        }
                    }
                }
                return textPart;
            } else if (ct.startsWith("multipart/")) {
                final int count = part.getEnclosedCount();
                for (int i = 0; i < count; i++) {
                    final MailPart p = getTextRecursive(part.getEnclosedMailPart(i));
                    if (p != null) {
                        return p;
                    }
                }
            } if (TNEFUtils.isTNEFMimeType(ct.getBaseType())) {
                return handleTNEFPart(part);
            }
            return null;
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private MailPart createPartFromPlainText(String content) throws OXException {
        try {
            final MimeBodyPart mimePart = new MimeBodyPart();
            final ContentType ct = new ContentType(MimeTypes.MIME_TEXT_PLAIN);
            ct.setCharsetParameter("UTF-8");
            mimePart.setDataHandler(new DataHandler(new MessageDataSource(content.getBytes(Charsets.UTF_8), ct.toString())));
            mimePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
            mimePart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            return MimeMessageConverter.convertPart(mimePart, false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private MailPart handleTNEFPart(final MailPart part) throws OXException {
        return handleTNEFStream(part.getInputStream());
    }

    private static final String TNEF_IPM_CONTACT = "IPM.Contact";

    private static final String TNEF_IPM_MS_READ_RECEIPT = "IPM.Microsoft Mail.Read Receipt";

    /**
     * Handles specified TNEF stream.
     *
     * @param inputStream The TNEF stream
     * @return The extracted plain text
     * @throws OXException If an OX error occurs
     */
    public MailPart handleTNEFStream(final InputStream inputStream) throws OXException {
        try {
            final TNEFInputStream tnefInputStream = new TNEFInputStream(inputStream);
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
                return null;
            }
            if (TNEF_IPM_MS_READ_RECEIPT.equalsIgnoreCase(messageClassName)) {
                return null;
            }
            if (TNEF2ICal.isVPart(messageClassName)) {
                return null;
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
                final MailPart p = getTextRecursive(MimeMessageConverter.convertPart(bodyPart));
                if (null != p) {
                    return p;
                }
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
                        rtfPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentTypeStr);
                        rtfPart.setSize(decompressedBytes.length);
                    }
                }
            }
            return getTextRecursive(MimeMessageConverter.convertPart(rtfPart));
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "ISO-8859-1";
            LOG.warn("Character conversion exception while reading content with charset \"{}\". Using fallback charset \"{}\" instead.", charset, fallback, e);
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                final String prev = cs;
                if (contentType.startsWith("text/")) {
                    try {
                        cs = CharsetDetector.detectCharsetFailOnError(mailPart.getInputStream());
                    } catch (final IOException e) {
                        if (mailPart instanceof MimeRawSource) {
                            cs = CharsetDetector.detectCharset(((MimeRawSource) mailPart).getRawInputStream());
                        } else {
                            cs = CharsetDetector.getFallback();
                        }
                    }
                    LOG.warn("Illegal or unsupported encoding \"{}\". Using auto-detected encoding: \"{}\"", prev, cs);
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    LOG.warn("Illegal or unsupported encoding \"{}\". Using fallback encoding: \"{}\"", prev, cs);
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith("text/")) {
                String cs;
                try {
                    cs = CharsetDetector.detectCharsetFailOnError(mailPart.getInputStream());
                } catch (final IOException e) {
                    if (mailPart instanceof MimeRawSource) {
                        cs = CharsetDetector.detectCharset(((MimeRawSource) mailPart).getRawInputStream());
                    } else {
                        cs = CharsetDetector.getFallback();
                    }
                }
                charset = cs;
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

}
