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

package com.openexchange.file.storage.mail;

import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link FullName}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class FullName {

    /** A full name's type */
    public static enum Type {
        /** The virtual attachment folder containing all attachments */
        ALL(FileStorageFolder.ROOT_FULLNAME),
        /** The virtual attachment folder containing received attachments */
        RECEIVED("received"),
        /** The virtual attachment folder containing sent attachments */
        SENT("sent");

        private final String folderId;

        private Type(String folderId) {
            this.folderId = folderId;
        }

        /**
         * Gets the virtual folder identifier
         *
         * @return The folder identifier
         */
        public String getFolderId() {
            return folderId;
        }

        /**
         * Gets the type for specified folder identifier
         *
         * @param folderId The folder identifier to look-up by
         * @return The associated type
         */
        public static Type typeByFolderId(String folderId) {
            for (Type type : Type.values()) {
                if (type.folderId.equals(folderId)) {
                    return type;
                }
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------------------

    private final String fullName;
    private final Type type;
    private final String folderId;
    private final int hash;

    /**
     * Initializes a new {@link FullName}.
     *
     * @param fullName The full name
     * @param type The type
     */
    public FullName(String fullName, Type type) {
        super();
        this.fullName = fullName;
        this.type = type;
        this.folderId = type.getFolderId();
        hash = 31 * 1 + ((fullName == null) ? 0 : fullName.hashCode());
    }

    /**
     * Checks if this full name denotes the default folder.
     *
     * @return <code>true</code> if full name denotes the default folder; otherwise <code>false</code>
     */
    public boolean isDefaultFolder() {
        return Type.ALL.equals(type);
    }

    /**
     * Checks if this full name does <b>not</b> denote the default folder.
     *
     * @return <code>true</code> if full name does <b>not</b> denote the default folder; otherwise <code>false</code>
     */
    public boolean isNotDefaultFolder() {
        return !isDefaultFolder();
    }

    /**
     * Checks if this full name denotes the virtual "all attachments" folder.
     *
     * @return <code>true</code> if this full name denotes the virtual "all attachments" folder; otherwise <code>false</code>
     */
    public boolean isAllFolder() {
        return Type.ALL == type;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the original folder identifier
     *
     * @return The original folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FullName)) {
            return false;
        }
        FullName other = (FullName) obj;
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        return true;
    }

}
