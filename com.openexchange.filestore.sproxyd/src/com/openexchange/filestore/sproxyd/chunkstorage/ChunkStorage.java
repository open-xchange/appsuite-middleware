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

import java.util.List;
import java.util.UUID;
import com.openexchange.exception.OXException;


/**
 * {@link ChunkStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChunkStorage {

    /**
     * Gets the stored documents for specified user.
     *
     * @return The documents
     * @throws OXException If documents cannot be returned
     */
    List<UUID> getDocuments() throws OXException;

    /**
     * Gets the chunks for the denoted document.
     *
     * @param documentId The document identifier
     * @return The document's chunks
     * @throws OXException If chunks cannot be returned
     */
    List<Chunk> getChunks(UUID documentId) throws OXException;

    /**
     * Gets the chunk for the denoted document.
     *
     * @param chunkId The chunk's identifier in Scality system
     * @return The chunk or <code>null</code> if there is no such chunk
     * @throws OXException If chunk cannot be returned
     */
    Chunk getChunk(UUID chunkId) throws OXException;

    /**
     * Gets the next chunk following referenced chunk in denoted document.
     *
     * @param chunkId The chunk's identifier in Scality system
     * @param documentId The document identifier
     * @return The next chunk or <code>null</code> if there is no next chunk
     * @throws OXException If chunk cannot be returned
     */
    Chunk getNextChunk(UUID chunkId, UUID documentId) throws OXException;

    /**
     * Gets the next chunk following referenced chunk in denoted document.
     *
     * @param documentId The document identifier
     * @return The last chunk or <code>null</code> if there is no next chunk
     * @throws OXException If chunk cannot be returned
     */
    Chunk getLastChunk(UUID documentId) throws OXException;

    /**
     * Stores a new chunk
     *
     * @param chunkData The chunk data
     * @return The newly stored chunk
     * @throws OXException If chunk cannot be stored
     */
    Chunk storeChunk(Chunk chunk) throws OXException;

    /**
     * Deletes a document and all associated chunks.
     *
     * @param documentId The document identifier
     * @return <code>true</code> upon successful deletion; otherwise <code>false</code> if there was no such document
     * @throws OXException If document cannot be deleted
     */
    boolean deleteDocument(UUID documentId) throws OXException;

    /**
     * Deletes a chunk
     *
     * @param chunkId The chunk's identifier in Scality system
     * @return <code>true</code> upon successful deletion; otherwise <code>false</code> if there was no such chunk
     * @throws OXException If chunk cannot be deleted
     */
    boolean deleteChunk(UUID chunkId) throws OXException;

}
