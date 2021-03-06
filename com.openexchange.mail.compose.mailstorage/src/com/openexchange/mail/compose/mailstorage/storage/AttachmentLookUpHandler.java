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

package com.openexchange.mail.compose.mailstorage.storage;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.UUEncodedAttachmentMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link AttachmentLookUpHandler} - Handler to look-up a certain mail part by <code>"X-Part-Id"</code> header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AttachmentLookUpHandler implements MailMessageHandler {

    private final String attachmentId;
    private MailPart attachment;

    /**
     * Initializes a new {@link AttachmentLookUpHandler}
     */
    public AttachmentLookUpHandler(UUID attachmentId) {
        super();
        this.attachmentId = getUnformattedString(attachmentId);
    }

    /**
     * Gets the look-up attachment.
     *
     * @return The attachment or empty
     */
    public Optional<MailPart> getAttachment() {
        return Optional.ofNullable(attachment);
    }

    private boolean equalsId(MailPart part) {
        if (attachmentId.equals(part.getFirstHeader(MessageHeaders.HDR_X_PART_ID))) {
            attachment = part;
            return true;
        }
        return false;
    }

    @Override
    public boolean handleAttachment(MailPart part, boolean isInline, String baseContentType, String fileName, String id) throws OXException {
        return !(equalsId(part));
    }

    @Override
    public boolean handleImagePart(MailPart part, String imageCID, String baseContentType, boolean isInline, String fileName, String id) throws OXException {
        return !(equalsId(part));
    }

    @Override
    public boolean handleNestedMessage(MailPart mailPart, String id) throws OXException {
        mailPart.getContentDisposition().setDisposition(Part.ATTACHMENT);
        if (!mailPart.containsSequenceId()) {
            mailPart.setSequenceId(id);
        }
        return !(equalsId(mailPart));
    }

    @Override
    public boolean handleSpecialPart(MailPart part, String baseContentType, String fileName, String id) throws OXException {
        if (!part.containsSequenceId()) {
            part.setSequenceId(id);
        }
        return !(equalsId(part));
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException {
        MailPart mailPart = new UUEncodedAttachmentMailPart(part);
        String ct = MimeType2ExtMap.getContentType(part.getFileName());
        if (ct == null || ct.length() == 0) {
            ct = MimeTypes.MIME_APPL_OCTET;
        }
        mailPart.setContentType(ct);
        mailPart.setSize(part.getFileSize());
        mailPart.setFileName(part.getFileName());
        mailPart.setSequenceId(id);
        return !(equalsId(mailPart));
    }

    @Override
    public boolean handleInlineHtml(ContentProvider htmlContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlinePlainText(String plainTextContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipartEnd(MailPart mp, String id) throws OXException {
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
    public boolean handleDispositionNotification(InternetAddress dispositionNotificationTo, boolean acknowledged) throws OXException {
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
    public boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMsgRef(String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipart(MailPart mp, int bodyPartCount, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handlePriority(int priority) throws OXException {
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

    @Override
    public void handleMessageEnd(MailMessage mail) throws OXException {
        // Nothing to do
    }

}
