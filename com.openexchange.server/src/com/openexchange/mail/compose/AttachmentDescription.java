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

package com.openexchange.mail.compose;

import java.util.UUID;
import com.openexchange.mail.compose.Attachment.ContentDisposition;

/**
 * {@link AttachmentDescription} - Represents an attachment description.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentDescription {

    private UUID uuid;
    private UUID compositionSpaceId;
    private String name;
    private long size;
    private String mimeType;
    private ContentId contentId;
    private ContentDisposition contentDisposition;
    private AttachmentOrigin origin;

    /**
     * Initializes a new {@link AttachmentDescription}.
     */
    public AttachmentDescription() {
        super();
        size = -1L;
    }

    /**
     * Gets the UUID
     *
     * @return The UUID
     */
    public UUID getId() {
        return uuid;
    }

    /**
     * Gets the identifier of the composition space, this attachment is associated with
     *
     * @return The composition space identifier
     */
    public UUID getCompositionSpaceId() {
        return compositionSpaceId;
    }

    /**
     * Gets the file name
     *
     * @return The name
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
     * Gets the MIME type
     *
     * @return The MIME type or <code>null</code>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the content identifier reference
     *
     * @return The content identifier
     */
    public ContentId getContentId() {
        return contentId;
    }

    /**
     * Gets the content disposition
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

    /**
     * Sets the UUID
     *
     * @param uuid The UUID to set
     */
    public void setId(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Sets the identifier of the composition space, this attachment is associated with
     *
     * @param compositonSpaceId The identifier of the composition space
     */
    public void setCompositionSpaceId(UUID compositonSpaceId) {
        this.compositionSpaceId = compositonSpaceId;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the size
     *
     * @param size The size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Sets the MIME type
     *
     * @param mimeType The MIME type to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets the content identifier
     *
     * @param contentId The content identifier to set
     */
    public void setContentId(String contentId) {
        this.contentId = ContentId.valueOf(contentId);
    }

    /**
     * Sets the content identifier
     *
     * @param contentId The content identifier to set
     */
    public void setContentId(ContentId contentId) {
        this.contentId = contentId;
    }

    /**
     * Sets the content disposition
     *
     * @param contentDisposition The content disposition to set
     */
    public void setContentDisposition(ContentDisposition contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * Sets the origin
     *
     * @param origin The origin to set
     */
    public void setOrigin(AttachmentOrigin origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (uuid != null) {
            builder.append("uuid=").append(uuid).append(", ");
        }
        if (compositionSpaceId != null) {
            builder.append("compositionSpaceId=").append(compositionSpaceId).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        builder.append("size=").append(size).append(", ");
        if (mimeType != null) {
            builder.append("mimeType=").append(mimeType).append(", ");
        }
        if (contentId != null) {
            builder.append("contentId=").append(contentId).append(", ");
        }
        if (contentDisposition != null) {
            builder.append("contentDisposition=").append(contentDisposition).append(", ");
        }
        if (origin != null) {
            builder.append("origin=").append(origin);
        }
        builder.append("]");
        return builder.toString();
    }

}
