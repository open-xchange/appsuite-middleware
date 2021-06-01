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

package com.openexchange.mail;

import java.util.Objects;

/**
 * {@link FolderAndId} - An immutable tuple of folder and mail identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class FolderAndId {

    private final String folderId;
    private final String mailId;
    private int hash;

    /**
     * Initializes a new {@link FolderAndId}.
     *
     * @param folderId The folder identifier; e.g. <code>"default0/INBOX"</code>
     * @param mailId The mail identifier
     * @throws NullPointerException If either folder or mail identifier is <code>null</code>
     */
    public FolderAndId(String folderId, String mailId) {
        super();
        Objects.requireNonNull(folderId, "Folder identifier must not be null.");
        Objects.requireNonNull(mailId, "Mail identifier must not be null.");
        this.folderId = folderId;
        this.mailId = mailId;
        hash = 0;
    }

    /**
     * Gets the folder identifier; e.g. <code>"default0/INBOX"</code>.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the mail identifier.
     *
     * @return The mail identifier
     */
    public String getMailId() {
        return mailId;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = prime * result + ((mailId == null) ? 0 : mailId.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FolderAndId)) {
            return false;
        }
        FolderAndId other = (FolderAndId) obj;
        if (folderId == null) {
            if (other.folderId != null) {
                return false;
            }
        } else if (!folderId.equals(other.folderId)) {
            return false;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return false;
            }
        } else if (!mailId.equals(other.mailId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FolderAndId [");
        if (folderId != null) {
            builder.append("folderId=").append(folderId).append(", ");
        }
        if (mailId != null) {
            builder.append("mailId=").append(mailId);
        }
        builder.append("]");
        return builder.toString();
    }

}
