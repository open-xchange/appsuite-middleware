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

package com.openexchange.filestore.sproxyd.chunkstorage;

import java.util.UUID;

/**
 * {@link Chunk} - Represents a chunk for a document.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Chunk implements Comparable<Chunk> {

    private final UUID documentId;
    private final UUID scalityId;
    private final long offset;
    private final long length;
    private final int hashCode;

    /**
     * Initializes a new {@link Chunk}.
     *
     * @param documentId The document identifier
     * @param scalityId The associated identifier in Scality storage
     * @param offset This chunk's offset in document
     * @param length This chunk's length
     */
    public Chunk(final UUID documentId, final UUID scalityId, final long offset, final long length) {
        super();
        this.documentId = documentId;
        this.scalityId = scalityId;
        this.offset = offset;
        this.length = length;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((documentId == null) ? 0 : documentId.hashCode());
        result = prime * result + (int) (length ^ (length >>> 32));
        result = prime * result + (int) (offset ^ (offset >>> 32));
        result = prime * result + ((scalityId == null) ? 0 : scalityId.hashCode());
        hashCode = result;
    }

    /**
     * Gets the document identifier
     *
     * @return The document identifier
     */
    public UUID getDocumentId() {
        return documentId;
    }

    /**
     * Gets the scality identifier
     *
     * @return The scality identifier
     */
    public UUID getScalityId() {
        return scalityId;
    }

    /**
     * Gets the offset
     *
     * @return The offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Gets the length
     *
     * @return The length
     */
    public long getLength() {
        return length;
    }

    @Override
    public int compareTo(final Chunk other) {
        final long thisOffset = offset;
        final long otherOffset = other.offset;
        return (thisOffset < otherOffset ? -1 : (thisOffset == otherOffset ? 0 : 1));
    }

    @Override
    public String toString() {
        return "Chunk [documentId=" + documentId + ", scalityId=" + scalityId + ", offset=" + offset + ", length=" + length + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Chunk other = (Chunk) obj;
        if (documentId == null) {
            if (other.documentId != null) {
                return false;
            }
        } else if (!documentId.equals(other.documentId)) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        if (scalityId == null) {
            if (other.scalityId != null) {
                return false;
            }
        } else if (!scalityId.equals(other.scalityId)) {
            return false;
        }
        return true;
    }
}
