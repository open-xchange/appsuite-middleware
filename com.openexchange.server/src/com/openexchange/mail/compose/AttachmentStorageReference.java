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
 * {@link AttachmentStorageReference}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentStorageReference {

    private final AttachmentStorageIdentifier storageIdentifier;
    private final AttachmentStorageType storageType;

    /**
     * Initializes a new {@link AttachmentStorageReference}.
     */
    public AttachmentStorageReference(AttachmentStorageIdentifier storageIdentifier, AttachmentStorageType referenceType) {
        super();
        this.storageIdentifier = storageIdentifier;
        this.storageType = referenceType;
    }

    /**
     * Gets the identifier of the resource held in the storage.
     *
     * @return The storage identifier
     */
    public AttachmentStorageIdentifier getStorageIdentifier() {
        return storageIdentifier;
    }

    /**
     * Gets the storage type.
     *
     * @return The storage type
     */
    public AttachmentStorageType getStorageType() {
        return storageType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (storageIdentifier != null) {
            builder.append("storageIdentifier=").append(storageIdentifier).append(", ");
        }
        if (storageType != null) {
            builder.append("storageType=").append(storageType);
        }
        builder.append("]");
        return builder.toString();
    }

}
