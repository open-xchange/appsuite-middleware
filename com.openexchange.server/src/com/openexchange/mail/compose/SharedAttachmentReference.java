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

/**
 * {@link SharedAttachmentReference} - References a shared attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class SharedAttachmentReference {

    private final String attachmentId;
    private final String folderId;
    private int hash;

    /**
     * Initializes a new {@link SharedAttachmentReference}.
     *
     * @param attachmentId The attachment identifier
     * @param folderId The identifier of the folder holding the attachment
     */
    public SharedAttachmentReference(String attachmentId, String folderId) {
        super();
        this.attachmentId = attachmentId;
        this.folderId = folderId;
        hash = 0;
    }

    /**
     * Gets the attachment identifier.
     *
     * @return The attachment identifier
     */
    public String getAttachmentId() {
        return attachmentId;
    }

    /**
     * Gets the identifier of the folder holding the attachment.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((attachmentId == null) ? 0 : attachmentId.hashCode());
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SharedAttachmentReference)) {
            return false;
        }
        SharedAttachmentReference other = (SharedAttachmentReference) obj;
        if (attachmentId == null) {
            if (other.attachmentId != null) {
                return false;
            }
        } else if (!attachmentId.equals(other.attachmentId)) {
            return false;
        }
        if (folderId == null) {
            if (other.folderId != null) {
                return false;
            }
        } else if (!folderId.equals(other.folderId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (attachmentId != null) {
            builder.append("attachmentId=").append(attachmentId).append(", ");
        }
        if (folderId != null) {
            builder.append("folderId=").append(folderId);
        }
        builder.append("]");
        return builder.toString();
    }

}
