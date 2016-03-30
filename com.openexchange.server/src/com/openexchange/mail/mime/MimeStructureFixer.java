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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.FileBackedMimeMessage;
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

    public boolean isApplicableFor(MailMessage message) {
        if (null == message) {
            return false;
        }

        ContentType contentType = message.getContentType();
        if (!contentType.startsWith("multipart/")) {
            // Nothing to filter
            return false;
        }
        if (contentType.startsWith("multipart/signed")) {
            // Don't touch
            return false;
        }
        // Check mailer/boundary for "Apple"
        {
            final String mailer = message.getHeader(MessageHeaders.HDR_X_MAILER, null);
            final boolean noAppleMailer;
            if (null == mailer || (noAppleMailer = (com.openexchange.java.Strings.toLowerCase(mailer).indexOf("apple") < 0))) {
                // Not composed by Apple mailer
                return false;
            }
            final String boundary = contentType.getParameter("boundary");
            if (noAppleMailer && (null == boundary || com.openexchange.java.Strings.toLowerCase(boundary).indexOf("apple") < 0)) {
                // Not composed by Apple mailer
                return false;
            }
        }

        return true;
    }

    /**
     * Processes specified MIME message.
     *
     * @param message The message
     * @return The message with fixed multipart structure
     * @throws OXException If fix attempt fails
     */
    public MailMessage process(MailMessage message) throws OXException {
        if (false == isApplicableFor(message)) {
            return message;
        }

        ThresholdFileHolder sink = null;
        boolean closeSink = true;
        try {
            // Convert to a MIME message
            MimeMessage mimeMessage;
            {
                sink = new ThresholdFileHolder();
                message.writeTo(sink.asOutputStream());
                File tempFile = sink.getTempFile();
                if (null == tempFile) {
                    mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), sink.getStream());
                } else {
                    mimeMessage = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile);
                }
            }

            // Process it
            MimeMessage processed = process0(mimeMessage, message.getContentType());

            // Yield appropriate MailMessage instance
            MailMessage processedMessage = MimeMessageConverter.convertMessage(processed, false);

            // Apply fields/attributes from original message
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

            // Return result
            closeSink = false;
            return processedMessage;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (closeSink && null != sink) {
                sink.close();
            }
        }
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
        ThresholdFileHolder sink = null;
        boolean closeSink = true;
        try {
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
                    if (null == mailer || (noAppleMailer = (com.openexchange.java.Strings.toLowerCase(mailer).indexOf("apple") < 0))) {
                        // Not composed by Apple mailer
                        return mimeMessage;
                    }
                    final String boundary = contentType.getParameter("boundary");
                    if (noAppleMailer && (null == boundary || com.openexchange.java.Strings.toLowerCase(boundary).indexOf("apple") < 0)) {
                        // Not composed by Apple mailer
                        return mimeMessage;
                    }
                }

                // Start to check & fix multipart structure
                MimeMessage mime;
                if (mimeMessage instanceof com.sun.mail.util.ReadableMime) {
                    sink = new ThresholdFileHolder();
                    mimeMessage.writeTo(sink.asOutputStream());
                    File tempFile = sink.getTempFile();
                    if (null == tempFile) {
                        mime = new MimeMessage(MimeDefaultSession.getDefaultSession(), sink.getStream());
                    } else {
                        mime = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile);
                    }
                } else {
                    mime = mimeMessage;
                }
                MimeMessage retval = process0(mime, contentType);

                closeSink = false;
                return retval;
            } catch (MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } finally {
            if (closeSink && null != sink) {
                sink.close();
            }
        }
    }

    private MimeMessage process0(final MimeMessage mimeMessage, final ContentType contentType) throws OXException {
        try {
            // Remember original Message-ID
            String messageId = mimeMessage.getHeader(MESSAGE_ID, null);

            // Start to check & fix multipart structure
            handlePart(multipartFor(mimeMessage, contentType));

            // Save changes
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

    private void handlePart(Multipart multipart) throws MessagingException, IOException, OXException {
        final int count = multipart.getCount();
        if (com.openexchange.java.Strings.toLowerCase(multipart.getContentType()).startsWith("multipart/mixed")) {
            String prefixImage = "image/";
            String prefixHtm = "text/htm";
            String prefixText = "text/plain";
            int inlineCount = 0;
            int inlineImageCount = 0;
            boolean isHtml = true;

            /*-
             * Check for multiple inline HTML parts
             *
             * (HTML)+
             * (Image)+
             * (HTML)*
             */
            {
                for (int i = 0; i < count; i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    ContentType contentType = getContentType(bodyPart);
                    if (isInline(bodyPart, contentType)) {
                        if (contentType.startsWith(prefixHtm)) {
                            inlineCount++;
                        } else if (contentType.startsWith(prefixImage)) {
                            inlineImageCount++;
                        }
                    }
                }
            }

            if (inlineImageCount > 0 && inlineCount <= 1) {
                /*-
                 * Check for multiple inline TEXT parts
                 *
                 * (TEXT)+
                 * (Image)+
                 * (TEXT)*
                 */
                inlineCount = 0;
                inlineImageCount = 0;
                isHtml = false;
                for (int i = 0; i < count; i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    ContentType contentType = getContentType(bodyPart);
                    if (isInline(bodyPart, contentType)) {
                        if (contentType.startsWith(prefixText)) {
                            inlineCount++;
                        } else if (contentType.startsWith(prefixImage)) {
                            inlineImageCount++;
                        }
                    }
                }
            }

            if (inlineImageCount > 0 && inlineCount > 1) {
                String textContent = null;
                String firstCharset = null;
                List<BodyPart> bodyParts = new ArrayList<BodyPart>(count);
                List<BodyPart> others = new LinkedList<BodyPart>();
                TIntList indexes = new TIntArrayList(count);

                for (int i = 0; i < count; i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    ContentType contentType = getContentType(bodyPart);
                    if (contentType.startsWith(isHtml ? prefixHtm : prefixText)) {
                        String charset = MessageUtility.checkCharset(bodyPart, contentType);
                        if (null == firstCharset) {
                            firstCharset = charset;
                        }
                        String text = MessageUtility.readMimePart(bodyPart, charset);
                        if (null == textContent) {
                            textContent = text;
                        } else {
                            textContent = isHtml ? mergeInto(text, textContent) : textContent + text;
                        }
                        indexes.add(i);
                    } else if (contentType.startsWith(prefixImage)) {
                        if (isHtml) {
                            MimeBodyPart imageBodyPart = (MimeBodyPart) bodyPart;
                            String contentId = imageBodyPart.getHeader("Content-Id", null);
                            if (null == contentId) {
                                contentId = new StringBuilder(48).append('<').append(UUID.randomUUID().toString()).append('>').toString();
                                imageBodyPart.setContentID(contentId);
                            }

                            // Append <img> tag
                            String text = new StringBuilder(64).append("<img src=\"cid:").append(getContentId(contentId)).append("\">").toString();
                            if (null == textContent) {
                                textContent = text;
                            } else {
                                textContent = mergeInto(text, textContent);
                            }
                        }
                        bodyParts.add(bodyPart);
                        indexes.add(i);
                    } else {
                        if (isHtml) {
                            others.add(bodyPart);
                        } else {
                            bodyParts.add(bodyPart);
                        }
                        indexes.add(i);
                    }
                }

                indexes.reverse();
                for (int i : indexes.toArray()) {
                    multipart.removeBodyPart(i);
                }

                // Get "multipart/mixed" (TEXT) or create "multipart/related" (HTML)
                if (isHtml) {
                    MimeMultipart newSubMultipart = new MimeMultipart("related");

                    MimeBodyPart contentBodyPart = new MimeBodyPart();
                    String charset = null == firstCharset ? "ISO-8859-1" : firstCharset;
                    contentBodyPart.setText(textContent, charset, "html");
                    contentBodyPart.setHeader(CONTENT_TYPE, MimeMessageUtility.foldContentType("text/html; charset=\"" + charset + "\""));
                    contentBodyPart.setHeader(CONTENT_DISPOSITION, "inline");
                    contentBodyPart.setHeader(MIME_VERSION, "1.0");
                    contentBodyPart.setHeader(CONTENT_TRANSFER_ENC, "quoted-printable");
                    newSubMultipart.addBodyPart(contentBodyPart);

                    // Add body parts
                    for (final BodyPart nextBodyPart : bodyParts) {
                        newSubMultipart.addBodyPart(nextBodyPart);
                    }

                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(newSubMultipart, mimeBodyPart);
                    multipart.addBodyPart(mimeBodyPart, 0);
                } else {
                    MimeBodyPart contentBodyPart = new MimeBodyPart();
                    final String charset = null == firstCharset ? "ISO-8859-1" : firstCharset;
                    contentBodyPart.setText(textContent, charset, "plain");
                    contentBodyPart.setHeader(CONTENT_TYPE, MimeMessageUtility.foldContentType("text/plain; charset=\"" + charset + "\""));
                    multipart.addBodyPart(contentBodyPart);
                    contentBodyPart.setHeader(CONTENT_DISPOSITION, "inline");
                    contentBodyPart.setHeader(MIME_VERSION, "1.0");
                    contentBodyPart.setHeader(CONTENT_TRANSFER_ENC, "quoted-printable");

                    // Add body parts
                    for (final BodyPart nextBodyPart : bodyParts) {
                        multipart.addBodyPart(nextBodyPart);
                    }
                }

                // Check for others
                if (!others.isEmpty()) {
                    for (BodyPart otherBodyPart : others) {
                        multipart.addBodyPart(otherBodyPart);
                    }
                }
                return;
            }
        }

        // Process other multipart
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            String sContentType = bodyPart.getContentType();
            if (!isEmpty(sContentType)) {
                final ContentType contentType = new ContentType(sContentType);
                if (contentType.startsWith("multipart/")) {
                    Multipart mpContent;
                    {
                        Object content = bodyPart.getContent();
                        if (content instanceof Multipart) {
                            mpContent = (Multipart) content;
                        } else {
                            mpContent = new MimeMultipart(bodyPart.getDataHandler().getDataSource());
                        }
                    }
                    handlePart(mpContent);
                } else if (contentType.startsWith("message/rfc822") || (contentType.getNameParameter() != null && contentType.getNameParameter().endsWith(".eml"))) {
                    MimeMessage filteredMessage;
                    {
                        Object content = bodyPart.getContent();
                        if (content instanceof MimeMessage) {
                            filteredMessage = process((MimeMessage) content);
                        } else {
                            filteredMessage = process(new MimeMessage(MimeDefaultSession.getDefaultSession(), bodyPart.getInputStream()));
                        }
                    }
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(filteredMessage, mimeBodyPart);
                    // mimeBodyPart.setContent(filteredMessage, "message/rfc822");
                    multipart.removeBodyPart(i);
                    multipart.addBodyPart(mimeBodyPart, i);
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
