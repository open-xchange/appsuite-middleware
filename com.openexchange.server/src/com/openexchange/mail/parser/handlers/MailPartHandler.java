/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.parser.handlers;

import static com.openexchange.mail.parser.MailMessageParser.generateFilename;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.UUEncodedAttachmentMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link MailPartHandler} - Looks for a certain mail part by sequence ID
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPartHandler implements MailMessageHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailPartHandler.class);

    private static final class TextMailPart extends MailPart {

        private static final long serialVersionUID = 5622318721711740585L;

        private final String text;

        private transient DataSource dataSource;

        /**
         * @param text The text content
         */
        public TextMailPart(String text, ContentType contentType) {
            super();
            this.text = text;
            setSize(text.length());
            if (contentType.getCharsetParameter() == null) {
                contentType.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());
            }
            setContentType(contentType);
        }

        private DataSource getDataSource() {
            if (null == dataSource) {
                dataSource = new MessageDataSource(text, getContentType());
            }
            return dataSource;
        }

        @Override
        public Object getContent() throws OXException {
            return text;
        }

        @Override
        public DataHandler getDataHandler() throws OXException {
            return new DataHandler(getDataSource());
        }

        @Override
        public int getEnclosedCount() throws OXException {
            return NO_ENCLOSED_PARTS;
        }

        @Override
        public MailPart getEnclosedMailPart(int index) throws OXException {
            return null;
        }

        @Override
        public InputStream getInputStream() throws OXException {
            try {
                return getDataSource().getInputStream();
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }

        @Override
        public void loadContent() {
            // Nothing to do
        }

        @Override
        public void prepareForCaching() {
            // Nothing to do
        }

    }

    private String id;

    private MailPart mailPart;

    /**
     * Constructor
     */
    public MailPartHandler(String id) {
        super();
        this.id = id;
    }

    /**
     * Sets sequence ID.
     * <p>
     * Remaining mail part is set to <code>null</code>
     *
     * @param id The sequence ID
     */
    public void setSequenceId(String id) {
        this.id = id;
        mailPart = null;
    }

    @Override
    public boolean handleMultipartEnd(MailPart mp, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleAttachment(MailPart part, boolean isInline, String baseContentType, String fileName, String id) throws OXException {
        if (this.id.equals(id)) {
            mailPart = part;
            if (!isInline) {
                checkFilename(mailPart, id, baseContentType);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean handleBccRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleCcRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleColorLabel(int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleContentId(String contentId) throws OXException {
        return true;
    }

    @Override
    public boolean handleFrom(InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(int size, Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    @Override
    public boolean handleImagePart(MailPart part, String imageCID, String baseContentType, boolean isInline, String fileName, String id) throws OXException {
        if (this.id.equals(id)) {
            mailPart = part;
            checkFilename(mailPart, id, baseContentType);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleInlineHtml(ContentProvider htmlContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        if (this.id.equals(id)) {
            mailPart = new TextMailPart(htmlContent.getContent(), contentType);
            mailPart.setContentType(contentType);
            mailPart.setSize(size);
            mailPart.setFileName(fileName);
            mailPart.setSequenceId(id);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleInlinePlainText(String plainTextContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        if (this.id.equals(id)) {
            mailPart = new TextMailPart(plainTextContent, contentType);
            mailPart.setContentType(contentType);
            mailPart.setSize(size);
            mailPart.setFileName(fileName);
            mailPart.setSequenceId(id);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException {
        if (this.id.equals(id)) {
            mailPart = new UUEncodedAttachmentMailPart(part);
            String ct = MimeType2ExtMap.getContentType(part.getFileName());
            if (ct == null || ct.length() == 0) {
                ct = MimeTypes.MIME_APPL_OCTET;
            }
            mailPart.setContentType(ct);
            mailPart.setSize(part.getFileSize());
            mailPart.setFileName(part.getFileName());
            mailPart.setSequenceId(id);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException {
        return handleInlinePlainText(decodedTextContent, contentType, size, fileName, id);
    }

    @Override
    public void handleMessageEnd(MailMessage msg) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean handleMultipart(MailPart mp, int bodyPartCount, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleNestedMessage(MailPart mailPart, String id) throws OXException {
        if (this.id.equals(id)) {
            this.mailPart = mailPart;
            return false;
        }
        final Object content = mailPart.getContent();
        final MailMessage nestedMail;
        if (content instanceof MailMessage) {
            nestedMail = (MailMessage) content;
        } else if (content instanceof InputStream) {
            try {
                nestedMail = MimeMessageConverter.convertMessage(new MimeMessage(
                    MimeDefaultSession.getDefaultSession(),
                    (InputStream) content));
            } catch (MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        } else {
            LOG.error("Ignoring nested message. Cannot handle part's content which should be a RFC822 message according to its content type: {}", (null == content ? "null" : content.getClass().getSimpleName()));
            return true;
        }
        final MailPartHandler nestedHandler = new MailPartHandler(this.id);
        new MailMessageParser().parseMailMessage(nestedMail, nestedHandler, id);
        if (null != nestedHandler.getMailPart()) {
            this.mailPart = nestedHandler.getMailPart();
            return false;
        }
        return true;
    }

    @Override
    public boolean handlePriority(int priority) throws OXException {
        return true;
    }

    @Override
    public boolean handleMsgRef(String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleDispositionNotification(InternetAddress dispositionNotificationTo, boolean acknowledged) throws OXException {
        return true;
    }

    @Override
    public boolean handleReceivedDate(Date receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSentDate(Date sentDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSpecialPart(MailPart part, String baseContentType, String fileName, String id) throws OXException {
        return handleAttachment(
            part,
            (!Part.ATTACHMENT.equalsIgnoreCase(part.getContentDisposition().getDisposition()) && part.getFileName() == null),
            baseContentType,
            fileName,
            id);
    }

    @Override
    public boolean handleSubject(String subject) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleToRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(String[] userFlags) throws OXException {
        return true;
    }

    /**
     * Gets the identified mail part or <code>null</code> if none found matching given sequence ID
     *
     * @return The identified mail part or <code>null</code> if none found matching given sequence ID
     */
    public MailPart getMailPart() {
        return mailPart;
    }

    private static void checkFilename(MailPart mailPart, String id, String baseMimeType) {
        if (mailPart.getFileName() == null) {
            mailPart.setFileName(generateFilename(id, baseMimeType));
        }
    }

}
