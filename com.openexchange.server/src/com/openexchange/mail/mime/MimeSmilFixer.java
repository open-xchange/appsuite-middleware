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

package com.openexchange.mail.mime;

import static com.openexchange.mail.mime.converters.MimeMessageConverter.multipartFor;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.utils.MessageUtility;
import com.sun.mail.util.MessageRemovedIOException;

/**
 * {@link MimeSmilFixer} - Detects possible <code>multipart/related; type=application/smil</code> parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public final class MimeSmilFixer {

    private static final MimeSmilFixer INSTANCE = new MimeSmilFixer();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MimeSmilFixer getInstance() {
        return INSTANCE;
    }

    private static final String CONTENT_TRANSFER_ENC = MessageHeaders.HDR_CONTENT_TRANSFER_ENC;
    private static final String MIME_VERSION = MessageHeaders.HDR_MIME_VERSION;
    private static final String CONTENT_DISPOSITION = MessageHeaders.HDR_CONTENT_DISPOSITION;
    private static final String CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;
    private static final String MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;

    // ------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link MimeSmilFixer}.
     */
    private MimeSmilFixer() {
        super();
    }

    /**
     * Processes specified MIME message.
     *
     * @param message The message
     * @return The message with fixed multipart structure
     * @throws OXException If fix attempt fails
     */
    public MailMessage process(final MailMessage message) throws OXException {
        if (null == message) {
            return message;
        }
        final ContentType contentType = message.getContentType();
        if (!contentType.startsWith("multipart/")) {
            // Nothing to filter
            return message;
        }
        // Fix it...
        final MimeMessage mimeMessage = (MimeMessage) MimeMessageConverter.convertMailMessage(message);
        final MimeMessage processed = process0(mimeMessage, contentType);
        final MailMessage processedMessage = MimeMessageConverter.convertMessage(processed, true);
        processedMessage.setMailId(message.getMailId());
        if (message.containsReceivedDate()) {
            processedMessage.setReceivedDate(message.getReceivedDate());
        }
        if (!processedMessage.containsSize() && message.containsSize()) {
            processedMessage.setSize(message.getSize());
        }
        if (message.containsAccountId()) {
            processedMessage.setAccountId(message.getAccountId());
        }
        if (message.containsFolder()) {
            processedMessage.setFolder(message.getFolder());
        }
        if (message.containsFlags()) {
            processedMessage.setFlags(message.getFlags());
        }
        if (message.containsColorLabel()) {
            processedMessage.setColorLabel(message.getColorLabel());
        }
        if (message.containsUserFlags()) {
            processedMessage.addUserFlags(message.getUserFlags());
        }
        return processedMessage;
    }


    // ------------------------------------------------------------------------------------------------- //

    /**
     * Processes specified MIME message.
     *
     * @param mimeMessage The MIME message
     * @return The MIME message with fixed multipart structure
     * @throws OXException If fix attempt fails
     */
    public MimeMessage process(final MimeMessage mimeMessage) throws OXException {
        if (null == mimeMessage) {
            return mimeMessage;
        }
        final ContentType contentType = getContentType(mimeMessage);
        if (!contentType.startsWith("multipart/")) {
            // Nothing to filter
            return mimeMessage;
        }
        // Start to check & fix multipart structure
        return process0(mimeMessage, contentType);
    }

    private MimeMessage process0(final MimeMessage mimeMessage, final ContentType contentType) throws OXException {
        try {
            // Start to check & fix multipart structure
            final String messageId = mimeMessage.getHeader(MESSAGE_ID, null);
            // Possible root multipart for unexpectedly found file attachments
            final AtomicReference<MimeMultipart> multipartRef = new AtomicReference<MimeMultipart>(new MimeMultipart(contentType.getSubType()));
            handlePart(multipartFor(mimeMessage, contentType), multipartRef);
            // Check if a new root has been set
            MessageUtility.setContent(multipartRef.get(), mimeMessage);
            // mimeMessage.setContent(newMultipart);
            MimeMessageConverter.saveChanges(mimeMessage);
            // Restore original Message-Id header
            if (null == messageId) {
                mimeMessage.removeHeader(MESSAGE_ID);
            } else {
                mimeMessage.setHeader(MESSAGE_ID, messageId);
            }
            return mimeMessage;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final MessageRemovedIOException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create();
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static final String[] HEADER_NAMES = new String[] {"Content-Description", "Content-Location", "Content-Disposition", "Content-ID"};

    private void handlePart(final Multipart multipart, final AtomicReference<MimeMultipart> newMultipartRef) throws MessagingException, IOException, OXException {
        final int count = multipart.getCount();
        final ContentType mpContentType = new ContentType(multipart.getContentType());
        if (mpContentType.startsWith("multipart/related")) {
            if ("application/smil".equals(mpContentType.getParameter(com.openexchange.java.Strings.toLowerCase("type")))) {
                final MimeMultipart mixedMultipart = new MimeMultipart("mixed");
                for (int i = 0; i < count; i++) {
                    final BodyPart bodyPart = multipart.getBodyPart(i);
                    final ContentType contentType = getContentType(bodyPart);
                    if (!contentType.startsWith("application/smil")) {
                        if (contentType.startsWith("text/")) {
                            final MimeBodyPart textBodyPart = new MimeBodyPart();
                            // Set content
                            textBodyPart.setDataHandler(new DataHandler(new javax.mail.internet.MimePartDataSource((MimePart) bodyPart)));
                            for (@SuppressWarnings("unchecked")  final Enumeration<Header> en = bodyPart.getNonMatchingHeaders(HEADER_NAMES); en.hasMoreElements();) {
                                final Header header = en.nextElement();
                                textBodyPart.addHeader(header.getName(), header.getValue());
                            }
                            // Remove "name" parameter
                            contentType.removeParameter("name");
                            textBodyPart.setHeader(CONTENT_TYPE, contentType.toString());
                            mixedMultipart.addBodyPart(textBodyPart);
                        } else {
                            mixedMultipart.addBodyPart(bodyPart);
                        }
                    }
                }
                newMultipartRef.set(mixedMultipart);
                return;
            }
        }
        // Process other multipart
        final MimeMultipart newMimeMultipart = newMultipartRef.get();
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            String sContentType = bodyPart.getContentType();
            if (isEmpty(sContentType)) {
                newMimeMultipart.addBodyPart(bodyPart);
            } else {
                final ContentType contentType = new ContentType(sContentType);
                if (contentType.startsWith("multipart/")) {
                    final MimeMultipart newMimeMultipart2 = new MimeMultipart(contentType.getSubType());
                    final AtomicReference<MimeMultipart> mpReference = new AtomicReference<MimeMultipart>(newMimeMultipart2);
                    {
                        final Multipart mpContent;
                        final Object content = bodyPart.getContent();
                        if (content instanceof Multipart) {
                            mpContent = (Multipart) content;
                        } else {
                            mpContent = new MimeMultipart(bodyPart.getDataHandler().getDataSource());
                        }
                        handlePart(mpContent, mpReference);
                    }
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(mpReference.get(), mimeBodyPart);
                    // mimeBodyPart.setContent(newSubMultipart);
                    newMimeMultipart.addBodyPart(mimeBodyPart);
                } else if (contentType.startsWith("message/rfc822") || (contentType.getNameParameter() != null && contentType.getNameParameter().endsWith(".eml"))) {
                    final MimeMessage filteredMessage;
                    {
                        final Object content = bodyPart.getContent();
                        if (content instanceof MimeMessage) {
                            filteredMessage = process((MimeMessage) content);
                        } else {
                            filteredMessage = process(new MimeMessage(MimeDefaultSession.getDefaultSession(), bodyPart.getInputStream()));
                        }
                    }
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(filteredMessage, mimeBodyPart);
                    // mimeBodyPart.setContent(filteredMessage, "message/rfc822");
                    newMimeMultipart.addBodyPart(mimeBodyPart);
                } else {
                    newMimeMultipart.addBodyPart(bodyPart);
                }
            }
        }
    }

    private static final Pattern PATTERN_BODY_START = Pattern.compile("<body[^>]*?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_BODY_END = Pattern.compile("</body>", Pattern.CASE_INSENSITIVE);

    private String mergeInto(String anotherHtml, String mainHtml) {
        final Matcher mStart = PATTERN_BODY_START.matcher(anotherHtml);
        final Matcher mEnd = PATTERN_BODY_END.matcher(anotherHtml);

        if (!mStart.find() || !mEnd.find()) {
            final Matcher mMain = PATTERN_BODY_END.matcher(mainHtml);
            if (mMain.find()) {
                final StringBuffer sb = new StringBuffer(mainHtml.length() + anotherHtml.length());
                mMain.appendReplacement(
                    sb,
                    new StringBuilder(Matcher.quoteReplacement(anotherHtml)).append(Matcher.quoteReplacement(mMain.group())).toString());
                mMain.appendTail(sb);
                return sb.toString();
            }
            final StringBuilder sb = new StringBuilder(mainHtml.length() + anotherHtml.length());
            sb.append(mainHtml).append(anotherHtml);
            return sb.toString();
        }

        final Matcher mMain = PATTERN_BODY_END.matcher(mainHtml);
        if (mMain.find()) {
            final StringBuffer sb = new StringBuffer(mainHtml.length() + anotherHtml.length());
            mMain.appendReplacement(
                sb,
                new StringBuilder(Matcher.quoteReplacement(anotherHtml.substring(mStart.end(), mEnd.start()))).append(
                    Matcher.quoteReplacement(mMain.group())).toString());
            mMain.appendTail(sb);
            return sb.toString();
        }
        final StringBuilder sb = new StringBuilder(mainHtml.length() + anotherHtml.length());
        sb.append(mainHtml).append(anotherHtml.substring(mStart.end(), mEnd.start()));
        return sb.toString();
    }

    private ContentType getContentType(final Part part) throws OXException {
        try {
            final String[] tmp = part.getHeader(CONTENT_TYPE);
            return (tmp != null) && (tmp.length > 0) ? new ContentType(tmp[0]) : new ContentType(MimeTypes.MIME_DEFAULT);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private String getContentId(final String sContentId) {
        if (null == sContentId) {
            return null;
        }
        if (sContentId.startsWith("<") && sContentId.endsWith(">")) {
            return sContentId.substring(1, sContentId.length() - 1);
        }
        return sContentId;
    }

    /**
     * Checks if given part's disposition is inline; meaning more likely a regular message body than an attachment.
     *
     * @param part The message's part
     * @param contentType The part's Content-Type header
     * @return <code>true</code> if given part is considered to be an inline part; otherwise <code>false</code>
     * @throws OXException If part's headers cannot be accessed or parsed
     */
    private static boolean isInline(final Part part, final ContentType contentType) throws OXException {
        try {
            final ContentDisposition cd;
            final boolean hasDisposition;
            {
                final String[] hdr = part.getHeader(CONTENT_DISPOSITION);
                if (null == hdr) {
                    cd = new ContentDisposition();
                    hasDisposition = false;
                } else {
                    cd = new ContentDisposition(hdr[0]);
                    hasDisposition = true;
                }
            }
            return (hasDisposition && Part.INLINE.equalsIgnoreCase(cd.getDisposition())) || (!hasDisposition && !cd.containsFilenameParameter() && !contentType.containsParameter("name"));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            switch (string.charAt(i)) {
            case 9: // 'unicode: 0009
            case 10: // 'unicode: 000A'
            case 11: // 'unicode: 000B'
            case 12: // 'unicode: 000C'
            case 13: // 'unicode: 000D'
            case 28: // 'unicode: 001C'
            case 29: // 'unicode: 001D'
            case 30: // 'unicode: 001E'
            case 31: // 'unicode: 001F'
            case ' ': // Space
                // case Character.SPACE_SEPARATOR:
                // case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                isWhitespace = true;
                break;
            default:
                isWhitespace = false;
                break;
            }
        }
        return isWhitespace;
    }
}
