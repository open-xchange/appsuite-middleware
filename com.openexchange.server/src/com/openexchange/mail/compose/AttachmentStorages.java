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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import static com.openexchange.mail.MailExceptionCode.getSize;
import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.session.Session;

/**
 * {@link AttachmentStorages} - A utility class for attachment storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentStorages {

    /**
     * Initializes a new {@link AttachmentStorages}.
     */
    private AttachmentStorages() {
        super();
    }

    private static final ContentDisposition ATTACHMENT = ContentDisposition.ATTACHMENT;
    private static final ContentDisposition INLINE = ContentDisposition.INLINE;

    /**
     * Creates an attachment description for given non-inline mail part.
     *
     * @param mailPart The mail part
     * @param partNumber The part's (sequence) number
     * @param compositionSpaceId The identifier of the composition space
     * @param session The session
     * @return The newly created attachment description
     */
    public static AttachmentDescription createAttachmentDescriptionFor(MailPart mailPart, int partNumber, UUID compositionSpaceId, Session session) {
        AttachmentDescription attachment = new AttachmentDescription();
        attachment.setCompositionSpaceId(compositionSpaceId);
        attachment.setContentDisposition(ATTACHMENT);
        attachment.setMimeType(mailPart.getContentType().getBaseType());
        String fileName = mailPart.getFileName();
        attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(partNumber), mailPart.getContentType().getBaseType()) : fileName);
        attachment.setOrigin(CompositionSpaces.hasVCardMarker(mailPart, session) ? AttachmentOrigin.VCARD : AttachmentOrigin.MAIL);
        return attachment;
    }

    /**
     * Creates an attachment description for given non-inline mail message.
     *
     * @param mailMessage The mail message
     * @param partNumber The (sequence) number
     * @param size The size of the mail message
     * @param compositionSpaceId The identifier of the composition space
     * @return The newly created attachment description
     */
    public static AttachmentDescription createAttachmentDescriptionFor(MailMessage mailMessage, int partNumber, long size, UUID compositionSpaceId) {
        AttachmentDescription attachment = new AttachmentDescription();
        attachment.setCompositionSpaceId(compositionSpaceId);
        attachment.setContentDisposition(ATTACHMENT);
        attachment.setMimeType(MimeTypes.MIME_MESSAGE_RFC822);
        String subject = mailMessage.getSubject();
        attachment.setName((Strings.isEmpty(subject) ? "mail" + (partNumber > 0 ? Integer.toString(partNumber) : "") : subject.replaceAll("\\p{Blank}+", "_")) + ".eml");
        attachment.setSize(size);
        attachment.setOrigin(AttachmentOrigin.MAIL);
        return attachment;
    }

    /**
     * Creates an attachment description for given inline mail part.
     *
     * @param mailPart The mail part
     * @param contentId The value for the Content-Id header
     * @param partNumber The part's (sequence) number
     * @param compositionSpaceId The identifier of the composition space
     * @return The newly created attachment description
     */
    public static AttachmentDescription createInlineAttachmentDescriptionFor(MailPart mailPart, String contentId, int partNumber, UUID compositionSpaceId) {
        AttachmentDescription attachment = new AttachmentDescription();
        attachment.setCompositionSpaceId(compositionSpaceId);
        attachment.setContentDisposition(INLINE);
        attachment.setContentId(contentId);
        attachment.setMimeType(mailPart.getContentType().getBaseType());
        String fileName = mailPart.getFileName();
        attachment.setName(Strings.isEmpty(fileName) ? MailMessageParser.generateFilename(Integer.toString(partNumber), mailPart.getContentType().getBaseType()) : fileName);
        attachment.setOrigin(AttachmentOrigin.MAIL);
        return attachment;
    }

    /**
     * Creates an attachment description for given user vCard.
     *
     * @param userVCard The user vCard
     * @param compositionSpaceId The identifier of the composition space
     * @return The newly created attachment description
     */
    public static AttachmentDescription createVCardAttachmentDescriptionFor(VCardAndFileName userVCard, UUID compositionSpaceId) {
        byte[] vcard = userVCard.getVcard();

        // Compile attachment
        AttachmentDescription attachment = new AttachmentDescription();
        attachment.setCompositionSpaceId(compositionSpaceId);
        attachment.setContentDisposition(ContentDisposition.ATTACHMENT);
        attachment.setMimeType(MimeTypes.MIME_TEXT_VCARD + "; charset=\"UTF-8\"");
        attachment.setName(userVCard.getFileName());
        attachment.setSize(vcard.length);
        attachment.setOrigin(AttachmentOrigin.VCARD);
        return attachment;
    }

    /**
     * Creates an attachment description for given upload file.
     *
     * @param uploadFile The upload file
     * @param disposition The disposition to set
     * @param compositionSpaceId The The identifier of the composition space
     * @return The newly created attachment description
     * @throws OXException If attachment description cannot be created
     */
    public static AttachmentDescription createUploadFileAttachmentDescriptionFor(StreamedUploadFile uploadFile, String disposition, UUID compositionSpaceId) throws OXException {
        AttachmentDescription attachment = new AttachmentDescription();
        attachment.setCompositionSpaceId(compositionSpaceId);
        {
            ContentDisposition contentDisposition = ContentDisposition.dispositionFor(disposition);
            attachment.setContentDisposition(null == contentDisposition ? ATTACHMENT : contentDisposition);
        }
        {
            ContentType contentType = new ContentType(uploadFile.getContentType());
            attachment.setMimeType(contentType.getBaseType());
            if (INLINE == attachment.getContentDisposition() && contentType.startsWith("image/")) {
                // Set a Content-Id for inline image, too
                attachment.setContentId(UUIDs.getUnformattedStringFromRandom() + "@Open-Xchange");
            }
        }
        attachment.setName(uploadFile.getPreparedFileName());
        attachment.setOrigin(AttachmentOrigin.UPLOAD);
        return attachment;
    }

    /**
     * Saves the specified attachment binary data and meta data using given storage instance.
     *
     * @param input The input stream providing binary data
     * @param attachment The attachment providing meta data
     * @param session The session providing user information
     * @param attachmentStorage The storage instance to use
     * @return The resulting attachment
     * @throws OXException If saving attachment fails
     */
    public static Attachment saveAttachment(InputStream input, AttachmentDescription attachmentDesc, Session session, AttachmentStorage attachmentStorage) throws OXException {
        Attachment savedAttachment = null;
        InputStream in = input;
        try {
            // Optimistic save
            savedAttachment = attachmentStorage.saveAttachment(in, attachmentDesc, null, session);
            Streams.close(in);
            in = null;

            // Check if max. mail size might be exceeded
            long maxMailSize = MailProperties.getInstance().getMaxMailSize(session.getUserId(), session.getContextId());
            if (maxMailSize > 0) {
                SizeReturner sizeReturner = attachmentStorage.getSizeOfAttachmentsByCompositionSpace(savedAttachment.getCompositionSpaceId(), session);
                if (sizeReturner.getTotalSize() > maxMailSize) {
                    throw MailExceptionCode.MAX_MESSAGE_SIZE_EXCEEDED.create(getSize(maxMailSize, 0, false, true));
                }
            }

            // All fine. Return newly saved attachment
            Attachment retval = savedAttachment;
            savedAttachment = null; // Avoid premature deletion
            return retval;
        } finally {
            Streams.close(in);
            if (null != savedAttachment) {
                attachmentStorage.deleteAttachment(savedAttachment.getId(), session);
            }
        }
    }

}
