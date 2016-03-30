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

package com.openexchange.mail.parser.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link DumperMessageHandler} - For testing purposes
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DumperMessageHandler implements MailMessageHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DumperMessageHandler.class);

    private final boolean bodyOnly;

    private final StringBuilder strBuilder;

    /**
	 *
	 */
    public DumperMessageHandler(final boolean bodyOnly) {
        super();
        strBuilder = new StringBuilder(8192 << 2);
        this.bodyOnly = bodyOnly;
    }

    public String getString() {
        return strBuilder.toString();
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleAttachment(com.openexchange.mail.dataobjects.MailContent, boolean,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleAttachment:\n");
        strBuilder.append("isInline=").append(isInline).append('\n');
        strBuilder.append("ContentType=").append(baseContentType).append('\n');
        strBuilder.append("fileName=").append(fileName).append('\n');
        strBuilder.append("sequenceId=").append(id).append('\n');
        try {
            strBuilder.append("Content:\n").append(MessageUtility.readMailPart(part, "US-ASCII"));
        } catch (final IOException e) {
            LOG.error("", e);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleBccRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleBccRecipient:\n");
        strBuilder.append("Bcc=").append(Arrays.toString(recipientAddrs)).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleCcRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleCcRecipient:\n");
        strBuilder.append("Cc=").append(Arrays.toString(recipientAddrs)).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleColorLabel(int)
     */
    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleColorLabel:\n");
        strBuilder.append("ColorLabel=").append(colorLabel).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleContentId(java.lang.String)
     */
    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleContentId:\n");
        strBuilder.append("Content-ID=").append(contentId).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleFrom:\n");
        strBuilder.append("From=").append(Arrays.toString(fromAddrs)).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleHeaders(int, java.util.Iterator)
     */
    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleHeaders:\n");
        for (int i = 0; i < size; i++) {
            final Map.Entry<String, String> e = iter.next();
            strBuilder.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleImagePart(com.openexchange.mail.dataobjects.MailContent, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleImagePart:\n");
        strBuilder.append("ContentType=").append(baseContentType).append('\n');
        strBuilder.append("Content-ID=").append(imageCID).append('\n');
        strBuilder.append("isInline=").append(isInline).append('\n');
        strBuilder.append("fileName=").append(fileName).append('\n');
        strBuilder.append("sequenceId=").append(id).append('\n');
        try {
            strBuilder.append("Content:\n").append(MessageUtility.readStream(part.getInputStream(), "US-ASCII"));
        } catch (final IOException e) {
            LOG.error("", e);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineHtml(java.lang.String, java.lang.String, int, java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean handleInlineHtml(final ContentProvider htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        strBuilder.append('\n').append("handleInlineHtml:\n");
        strBuilder.append("ContentType=").append(contentType).append('\n');
        strBuilder.append("Size=").append(size).append('\n');
        strBuilder.append("Filename=").append(fileName).append('\n');
        strBuilder.append("sequenceId=").append(id).append('\n');

        strBuilder.append("Content:\n").append(htmlContent.getContent());

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlinePlainText(java.lang.String, java.lang.String, int, java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean handleInlinePlainText(final String plainTextContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        strBuilder.append('\n').append("handleInlinePlainText:\n");
        strBuilder.append("ContentType=").append(contentType).append('\n');
        strBuilder.append("Size=").append(size).append('\n');
        strBuilder.append("Filename=").append(fileName).append('\n');
        strBuilder.append("sequenceId=").append(id).append('\n');

        strBuilder.append("Content:\n").append(plainTextContent);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
     * java.lang.String)
     */
    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedPlainText(java.lang.String, java.lang.String, int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMessageEnd(com.openexchange.mail.dataobjects.MailMessage)
     */
    @Override
    public void handleMessageEnd(final MailMessage msg) throws OXException {
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMultipart(com.openexchange.mail.dataobjects.MailContent, int,
     * java.lang.String)
     */
    @Override
    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleNestedMessage(com.openexchange.mail.dataobjects.MailMessage,
     * java.lang.String)
     */
    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {
        final Object content = mailPart.getContent();
        final MailMessage nestedMail;
        if (content instanceof MailMessage) {
            nestedMail = (MailMessage) content;
        } else if (content instanceof InputStream) {
            try {
                nestedMail = MimeMessageConverter.convertMessage(new MimeMessage(
                    MimeDefaultSession.getDefaultSession(),
                    (InputStream) content));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        } else {
            LOG.error("Ignoring nested message. Cannot handle part's content which should be a RFC822 message according to its content type: {}", (null == content ? "null" : content.getClass().getSimpleName()));
            return true;
        }
        final DumperMessageHandler handler = new DumperMessageHandler(bodyOnly);
        new MailMessageParser().parseMailMessage(nestedMail, handler, id);
        strBuilder.append(handler.getString());
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handlePriority(int)
     */
    @Override
    public boolean handlePriority(final int priority) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handlePriority:\n");
        strBuilder.append("Priority=").append(priority).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMsgRef(java.lang.String)
     */
    @Override
    public boolean handleMsgRef(final String msgRef) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleMsgRef:\n");
        strBuilder.append("MsgRef=").append(msgRef).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleDispositionNotification(javax.mail.internet.InternetAddress)
     */
    @Override
    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleDispositionNotification:\n");
        strBuilder.append("DispositionNotificationTo=").append(dispositionNotificationTo.toUnicodeString()).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleReceivedDate(java.util.Date)
     */
    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleReceivedDate:\n");
        strBuilder.append("ReceivedDate=").append(receivedDate).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSentDate(java.util.Date)
     */
    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleSentDate:\n");
        strBuilder.append("SentDate=").append(sentDate).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSpecialPart(com.openexchange.mail.dataobjects.MailContent,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleSpecialPart:\n");
        strBuilder.append("ContentType=").append(baseContentType).append('\n');
        strBuilder.append("filename=").append(fileName).append('\n');
        strBuilder.append("sequenceId=").append(id).append('\n');
        try {
            strBuilder.append("Content:\n").append(MessageUtility.readStream(part.getInputStream(), "US-ASCII"));
        } catch (final IOException e) {
            LOG.error("", e);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSubject(java.lang.String)
     */
    @Override
    public boolean handleSubject(final String subject) throws OXException {
        strBuilder.append('\n').append("handleSubject:\n");
        strBuilder.append("Subject=").append(subject).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSystemFlags(int)
     */
    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleSystemFlags:\n");
        strBuilder.append("Flags=").append(flags).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleToRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleToRecipient:\n");
        strBuilder.append("To=").append(Arrays.toString(recipientAddrs)).append('\n');
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleUserFlags(java.lang.String[])
     */
    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        if (bodyOnly) {
            return true;
        }
        strBuilder.append('\n').append("handleUserFlags:\n");
        strBuilder.append("UserFlags=").append(Arrays.toString(userFlags)).append('\n');
        return true;
    }

}
