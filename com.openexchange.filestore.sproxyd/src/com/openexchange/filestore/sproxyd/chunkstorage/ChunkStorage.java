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

import java.util.List;
import java.util.Optional;
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
     * Gets the chunks optionally available for the denoted document.
     *
     * @param documentId The document identifier
     * @return The document's chunks
     * @throws OXException If chunks cannot be returned
     */
    Optional<List<Chunk>> optChunks(UUID documentId) throws OXException;

    /**
     * Gets the chunk for the denoted document.
     *
     * @param chunkId The chunk's identifier in Scality system
     * @return The chunk
     * @throws OXException If chunk cannot be returned
     */
    Chunk getChunk(UUID chunkId) throws OXException;

    /**
     * Gets the next chunk following referenced chunk in denoted document.
     *
     * @param chunkId The chunk's identifier in Scality system
     * @param documentId The document identifier
     * @return The next chunk
     * @throws OXException If chunk cannot be returned
     */
    Chunk getNextChunk(UUID chunkId, UUID documentId) throws OXException;

    /**
     * Gets the next chunk following referenced chunk in denoted document.
     *
     * @param documentId The document identifier
     * @return The last chunk or empty if there is no next chunk
     * @throws OXException If chunk cannot be returned
     */
    Optional<Chunk> getLastChunk(UUID documentId) throws OXException;

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
