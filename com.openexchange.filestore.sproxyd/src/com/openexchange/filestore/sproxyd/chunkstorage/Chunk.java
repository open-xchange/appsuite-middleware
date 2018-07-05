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
 *    trademarks of the OX Software GmbH group of companies.
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
