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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.UUEncodedAttachmentMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link AllAttachmentsHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class AllAttachmentsHandler implements MailMessageHandler {

    private final List<MailPart> attachmentParts;

    /**
     * Initializes a new {@link AllAttachmentsHandler}
     */
    public AllAttachmentsHandler() {
        super();
        attachmentParts = new ArrayList<MailPart>();
    }

    /**
     * @return The gathered attachment parts
     */
    public List<MailPart> getAttachmentParts() {
        return attachmentParts;
    }

    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        attachmentParts.add(part);
        return true;
    }

    @Override
    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        attachmentParts.add(part);
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {
        mailPart.getContentDisposition().setDisposition(Part.ATTACHMENT);
        if (!mailPart.containsSequenceId()) {
            mailPart.setSequenceId(id);
        }
        attachmentParts.add(mailPart);
        return true;
    }

    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
        if (!part.containsSequenceId()) {
            part.setSequenceId(id);
        }
        attachmentParts.add(part);
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        final MailPart mailPart = new UUEncodedAttachmentMailPart(part);
        String ct = MimeType2ExtMap.getContentType(part.getFileName());
        if (ct == null || ct.length() == 0) {
            ct = MimeTypes.MIME_APPL_OCTET;
        }
        mailPart.setContentType(ct);
        mailPart.setSize(part.getFileSize());
        mailPart.setFileName(part.getFileName());
        mailPart.setSequenceId(id);
        attachmentParts.add(mailPart);
        return true;
    }

    @Override
    public boolean handleInlineHtml(final ContentProvider htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlinePlainText(final String plainTextContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        return true;
    }

    @Override
    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean acknowledged) throws OXException {
        return true;
    }

    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMsgRef(final String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handlePriority(final int priority) throws OXException {
        return true;
    }

    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSubject(final String subject) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        return true;
    }

    @Override
    public void handleMessageEnd(final MailMessage mail) throws OXException {
        // Nothing to do
    }

}
