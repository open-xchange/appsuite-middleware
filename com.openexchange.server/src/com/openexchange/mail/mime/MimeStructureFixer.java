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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.sun.mail.util.MessageRemovedIOException;

/**
 * {@link MimeStructureFixer} - Detects badly structured multipart as composed by Apple mailer and fixes it.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public final class MimeStructureFixer {

    private static final MimeStructureFixer INSTANCE = new MimeStructureFixer();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MimeStructureFixer getInstance() {
        return INSTANCE;
    }

    private static final String CONTENT_TRANSFER_ENC = MessageHeaders.HDR_CONTENT_TRANSFER_ENC;
    private static final String MIME_VERSION = MessageHeaders.HDR_MIME_VERSION;
    private static final String CONTENT_DISPOSITION = MessageHeaders.HDR_CONTENT_DISPOSITION;
    private static final String CONTENT_TYPE = MessageHeaders.HDR_CONTENT_TYPE;
    private static final String MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;

    // ------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link MimeStructureFixer}.
     */
    private MimeStructureFixer() {
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
        // Check mailer/boundary for "Apple"
        {
            final String mailer = message.getHeader(MessageHeaders.HDR_X_MAILER, null);
            final boolean noAppleMailer;
            if (null == mailer || (noAppleMailer = (toLowerCase(mailer).indexOf("apple") < 0))) {
                // Not composed by Apple mailer
                return message;
            }
            final String boundary = contentType.getParameter("boundary");
            if (noAppleMailer && (null == boundary || toLowerCase(boundary).indexOf("apple") < 0)) {
                // Not composed by Apple mailer
                return message;
            }
        }
        // Fix it...
        final MimeMessage mimeMessage = (MimeMessage) MimeMessageConverter.convertMailMessage(message);
        final MimeMessage processed = process0(mimeMessage, contentType);
        final MailMessage processedMessage = MimeMessageConverter.convertMessage(processed, true);
        processedMessage.setMailId(message.getMailId());
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
        try {
            // Check mailer/boundary for "Apple"
            {
                final String mailer = mimeMessage.getHeader(MessageHeaders.HDR_X_MAILER, null);
                final boolean noAppleMailer;
                if (null == mailer || (noAppleMailer = (toLowerCase(mailer).indexOf("apple") < 0))) {
                    // Not composed by Apple mailer
                    return mimeMessage;
                }
                final String boundary = contentType.getParameter("boundary");
                if (noAppleMailer && (null == boundary || toLowerCase(boundary).indexOf("apple") < 0)) {
                    // Not composed by Apple mailer
                    return mimeMessage;
                }
            }
            // Start to check & fix multipart structure
            return process0(mimeMessage, contentType);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private MimeMessage process0(final MimeMessage mimeMessage, final ContentType contentType) throws OXException {
        try {
            // Start to check & fix multipart structure
            final MimeMultipart newMultipart = new MimeMultipart(contentType.getSubType());
            final String messageId = mimeMessage.getHeader(MESSAGE_ID, null);
            // Possible root multipart for unexpectedly found file attachments
            final AtomicReference<MimeMultipart> artificialRoot = new AtomicReference<MimeMultipart>();
            final LinkedList<Multipart> mpStack = new LinkedList<Multipart>();
            final Multipart content = (Multipart) mimeMessage.getContent();
            mpStack.add(content);
            handlePart(content, new AtomicReference<MimeMultipart>(newMultipart), artificialRoot, mpStack);
            // Check if a new root has been set
            final MimeMultipart artificialRootMimeMultipart = artificialRoot.get();
            if (null == artificialRootMimeMultipart) {
                MessageUtility.setContent(newMultipart, mimeMessage);
            } else {
                // Need to create a new body part for deprecated root multipart
                final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                MessageUtility.setContent(newMultipart, mimeBodyPart);
                artificialRootMimeMultipart.addBodyPart(mimeBodyPart, 0);
                MessageUtility.setContent(artificialRootMimeMultipart, mimeMessage);
            }
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

    private void handlePart(final Multipart multipart, final AtomicReference<MimeMultipart> newMultipartRef, final AtomicReference<MimeMultipart> artificialRoot, final LinkedList<Multipart> mpStack) throws MessagingException, IOException, OXException {
        final int count = multipart.getCount();
        if (toLowerCase(multipart.getContentType()).startsWith("multipart/mixed")) {
            final String prefixImage = "image/";
            final String prefixHtm = "text/htm";
            final String prefixText = "text/plain";
            int inlineCount = 0;
            boolean isHtml = true;
            /*-
             * Check for multiple inline HTML parts
             *
             * (HTML)+
             * Image
             * (HTML)*
             */
            {
                for (int i = 0; i < count; i++) {
                    final BodyPart bodyPart = multipart.getBodyPart(i);
                    final ContentType contentType = getContentType(bodyPart);
                    if ((contentType.startsWith(prefixHtm) || contentType.startsWith(prefixImage)) && isInline(bodyPart, contentType)) {
                        inlineCount++;
                    }
                }
            }
            if (inlineCount <= 1) {
                /*-
                 * Check for multiple inline TEXT parts
                 *
                 * (TEXT)+
                 * Image
                 * (TEXT)*
                 */
                isHtml = false;
                for (int i = 0; i < count; i++) {
                    final BodyPart bodyPart = multipart.getBodyPart(i);
                    final ContentType contentType = getContentType(bodyPart);
                    if ((contentType.startsWith(prefixText) || contentType.startsWith(prefixImage)) && isInline(bodyPart, contentType)) {
                        inlineCount++;
                    }
                }
            }
            if (inlineCount > 1) {
                String textContent = null;
                String firstCharset = null;
                final List<BodyPart> bodyParts = new ArrayList<BodyPart>(count);
                final List<BodyPart> others = new LinkedList<BodyPart>();
                for (int i = 0; i < count; i++) {
                    final BodyPart bodyPart = multipart.getBodyPart(i);
                    final ContentType contentType = getContentType(bodyPart);
                    if (contentType.startsWith(isHtml ? prefixHtm : prefixText)) {
                        final String charset = MessageUtility.checkCharset(bodyPart, contentType);
                        if (null == firstCharset) {
                            firstCharset = charset;
                        }
                        final String text = MessageUtility.readMimePart(bodyPart, charset);
                        if (null == textContent) {
                            textContent = text;
                        } else {
                            textContent = isHtml ? mergeInto(text, textContent) : textContent + text;
                        }
                    } else if (contentType.startsWith(prefixImage)) {
                        if (isHtml) {
                            final MimeBodyPart imageBodyPart = new MimeBodyPart();
                            // Set content
                            imageBodyPart.setDataHandler(new DataHandler(new javax.mail.internet.MimePartDataSource((MimePart) bodyPart)));
                            // Set headers
                            imageBodyPart.setHeader(CONTENT_TYPE, contentType.toString());
                            String contentId = null;
                            for (@SuppressWarnings("unchecked") final Enumeration<Header> headers = bodyPart.getAllHeaders(); headers.hasMoreElements();) {
                                final Header header = headers.nextElement();
                                final String name = toLowerCase(header.getName());
                                if (!"content-type".equals(name)) {
                                    if ("content-id".equals(name)) {
                                        contentId = header.getValue();
                                    }
                                    imageBodyPart.addHeader(header.getName(), header.getValue());
                                }
                            }
                            if (null == contentId) {
                                contentId = new StringAllocator(48).append('<').append(UUID.randomUUID().toString()).append('>').toString();
                                imageBodyPart.setContentID(contentId);
                            }
                            bodyParts.add(imageBodyPart);
                            // Append <img> tag
                            final String text = new StringAllocator(64).append("<img src=\"cid:").append(getContentId(contentId)).append("\">").toString();
                            if (null == textContent) {
                                textContent = text;
                            } else {
                                textContent = mergeInto(text, textContent);
                            }
                        } else {
                            bodyParts.add(bodyPart);
                        }
                    } else {
                        if (mpStack.size() > 1) {
                            others.add(bodyPart);
                        } else {
                            bodyParts.add(bodyPart);
                        }
                    }
                }
                // Get "multipart/mixed" (TEXT) or create "multipart/related" (HTML)
                final MimeMultipart newSubMultipart = isHtml ? new MimeMultipart("related") : newMultipartRef.get();
                final MimeBodyPart contentBodyPart;
                if (isHtml) {
                    contentBodyPart = new MimeBodyPart();
                    final String charset = null == firstCharset ? "ISO-8859-1" : firstCharset;
                    contentBodyPart.setText(textContent, charset, "html");
                    contentBodyPart.setHeader(CONTENT_TYPE, MimeMessageUtility.foldContentType("text/html; charset=\"" + charset + "\""));
                } else {
                    contentBodyPart = new MimeBodyPart();
                    final String charset = null == firstCharset ? "ISO-8859-1" : firstCharset;
                    contentBodyPart.setText(textContent, charset, "plain");
                    contentBodyPart.setHeader(CONTENT_TYPE, MimeMessageUtility.foldContentType("text/plain; charset=\"" + charset + "\""));
                }
                contentBodyPart.setHeader(CONTENT_DISPOSITION, "inline");
                contentBodyPart.setHeader(MIME_VERSION, "1.0");
                contentBodyPart.setHeader(CONTENT_TRANSFER_ENC, "quoted-printable");
                newSubMultipart.addBodyPart(contentBodyPart);
                // Add body parts
                for (final BodyPart nextBodyPart : bodyParts) {
                    newSubMultipart.addBodyPart(nextBodyPart);
                }
                // Replace new multipart
                newMultipartRef.set(newSubMultipart);
                // Check for others
                if (!others.isEmpty()) {
                    if (isHtml) {
                        MimeMultipart mixedMultipart;
                        if (toLowerCase(mpStack.getFirst().getContentType()).indexOf("multipart/mixed") < 0) {
                            // This means we processed a multipart below root-level multipart that is not of type "multipart/mixed"
                            // Need to create an artificial one
                            mixedMultipart = artificialRoot.get();
                            if (null == mixedMultipart) {
                                mixedMultipart = new MimeMultipart("mixed");
                                artificialRoot.set(mixedMultipart);
                            }
                        } else {
                            mixedMultipart = newSubMultipart;
                        }
                        for (final BodyPart otherBodyPart : others) {
                            mixedMultipart.addBodyPart(otherBodyPart);
                        }
                    } else {
                        final MimeMultipart mixedMultipart = newSubMultipart;
                        for (final BodyPart otherBodyPart : others) {
                            mixedMultipart.addBodyPart(otherBodyPart);
                        }
                    }
                }
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
                    final AtomicReference<MimeMultipart> mpReference = new AtomicReference<MimeMultipart>(new MimeMultipart(contentType.getSubType()));
                    {
                        final Multipart mpContent;
                        final Object content = bodyPart.getContent();
                        if (content instanceof Multipart) {
                            mpContent = (Multipart) content;
                        } else {
                            mpContent = new MimeMultipart(bodyPart.getDataHandler().getDataSource());
                        }
                        mpStack.add(mpContent);
                        handlePart(mpContent, mpReference, artificialRoot, mpStack);
                        mpStack.removeLast();
                    }
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(mpReference.get(), mimeBodyPart);
                    // mimeBodyPart.setContent(newSubMultipart);
                    newMimeMultipart.addBodyPart(mimeBodyPart);
                } else if (contentType.startsWith("message/rfc822")) {
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
                    new StringAllocator(Matcher.quoteReplacement(anotherHtml)).append(Matcher.quoteReplacement(mMain.group())).toString());
                mMain.appendTail(sb);
                return sb.toString();
            }
            final StringAllocator sb = new StringAllocator(mainHtml.length() + anotherHtml.length());
            sb.append(mainHtml).append(anotherHtml);
            return sb.toString();
        }

        final Matcher mMain = PATTERN_BODY_END.matcher(mainHtml);
        if (mMain.find()) {
            final StringBuffer sb = new StringBuffer(mainHtml.length() + anotherHtml.length());
            mMain.appendReplacement(
                sb,
                new StringAllocator(Matcher.quoteReplacement(anotherHtml.substring(mStart.end(), mEnd.start()))).append(
                    Matcher.quoteReplacement(mMain.group())).toString());
            mMain.appendTail(sb);
            return sb.toString();
        }
        final StringAllocator sb = new StringAllocator(mainHtml.length() + anotherHtml.length());
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

    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
