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

package com.openexchange.mail.compose.mailstorage.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.MessageDescription;

/**
 * {@link AttachmentMetadata} - Metadata for an attachment.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class AttachmentMetadata {

    /**
     * Creates attachment metadata listing from specified message description.
     *
     * @param messageDesc The message description
     * @return The newly created listing of attachment metadata
     */
    public static List<AttachmentMetadata> fromMessage(MessageDescription messageDesc) {
        Objects.requireNonNull(messageDesc);
        List<Attachment> attachments = messageDesc.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<AttachmentMetadata> result = new ArrayList<>(attachments.size());
        for (Attachment attachment : attachments) {
            result.add(fromAttachment(attachment));
        }

        return result;
    }

    /**
     * Creates attachment metadata from specified attachment.
     *
     * @param attachment The attachment
     * @return The newly created attachment metadata
     */
    public static AttachmentMetadata fromAttachment(Attachment attachment) {
        Objects.requireNonNull(attachment);
        return new AttachmentMetadata(attachment.getId(), attachment.getName(), attachment.getSize(), attachment.getMimeType(), attachment.getContentDisposition(), attachment.getOrigin());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final UUID id;
    private final String name;
    private final long size;
    private final String mimeType;
    private final ContentDisposition contentDisposition;
    private final AttachmentOrigin origin;

    /**
     * Initializes a new {@link AttachmentMetadata}.
     */
    private AttachmentMetadata(UUID id, String name, long size, String mimeType, ContentDisposition contentDisposition, AttachmentOrigin origin) {
        super();
        this.id = id;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.contentDisposition = contentDisposition;
        this.origin = origin;
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the file name.
     *
     * @return The file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size in bytes.
     *
     * @return The size in bytes or <code>-1</code> if unknown
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the MIME type.
     *
     * @return The MIME type or <code>null</code>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the content disposition.
     *
     * @return The content disposition
     */
    public ContentDisposition getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Gets the attachment's origin.
     *
     * @return The origin
     */
    public AttachmentOrigin getOrigin() {
        return origin;
    }

}
