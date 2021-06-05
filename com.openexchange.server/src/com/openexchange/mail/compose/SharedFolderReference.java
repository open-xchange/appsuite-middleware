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
 * {@link SharedFolderReference} - References a shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class SharedFolderReference {

    /**
     * Gets the appropriate <code>SharedFolderReference</code> instance for given folder identifier.
     *
     * @param folderId The folder identifier
     * @return The <code>SharedFolderReference</code> instance
     */
    public static SharedFolderReference valueOf(String folderId) {
        return folderId == null ? null : new SharedFolderReference(folderId);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String folderId;

    /**
     * Initializes a new {@link SharedFolderReference}.
     *
     * @param folderId The identifier of the folder holding the attachment
     */
    private SharedFolderReference(String folderId) {
        super();
        this.folderId = folderId;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SharedFolderReference)) {
            return false;
        }
        SharedFolderReference other = (SharedFolderReference) obj;
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
        if (folderId != null) {
            builder.append("folderId=").append(folderId);
        }
        builder.append("]");
        return builder.toString();
    }

}
