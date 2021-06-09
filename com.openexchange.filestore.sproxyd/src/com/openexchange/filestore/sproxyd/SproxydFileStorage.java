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

package com.openexchange.filestore.sproxyd;

import static com.openexchange.java.Autoboxing.L;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DestroyAwareFileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.sproxyd.chunkstorage.Chunk;
import com.openexchange.filestore.sproxyd.chunkstorage.ChunkStorage;
import com.openexchange.filestore.sproxyd.impl.SproxydClient;
import com.openexchange.filestore.utils.DefaultChunkedUpload;
import com.openexchange.filestore.utils.TempFileHelper;
import com.openexchange.filestore.utils.UploadChunk;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SproxydFileStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorage implements DestroyAwareFileStorage {

    private final SproxydClient client;
    private final ChunkStorage chunkStorage;
    private final URI uri;

    /**
     * Initializes a new {@link SproxydFileStorage}.
     *
     * @param uri The URI that fully qualifies this file storage
     * @param services A service lookup reference
     * @param client The spoxyd client to use
     * @param chunkStorage The underlying chunk storage
     */
    public SproxydFileStorage(URI uri, SproxydClient client, ChunkStorage chunkStorage) {
        super();
        this.uri = uri;
        this.client = client;
        this.chunkStorage = chunkStorage;
    }

    @Override
    public void onDestroyed() {
        // Only if last one that uses the pool
        // client.getEndpointPool().close();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean isSpooling() {
        return true;
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        File tmpFile = null;
        try {
            /*
             * spool to file
             */
            if (!(file instanceof FileInputStream)) {
                Optional<File> optionalTempFile = TempFileHelper.getInstance().newTempFile();
                if (optionalTempFile.isPresent()) {
                    tmpFile = optionalTempFile.get();
                    file = Streams.transferToFileAndCreateStream(file, tmpFile);
                }
            }
            /*
             * proceed
             */
            UUID documentId = UUID.randomUUID();
            upload(documentId, file, 0);
            return UUIDs.getUnformattedString(documentId);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(file);
            TempFileHelper.deleteQuietly(tmpFile);
        }
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        List<Chunk> chunks = chunkStorage.getChunks(UUIDs.fromUnformattedString(name));
        if (null == chunks || chunks.isEmpty()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }

        if (1 == chunks.size()) {
            return client.get(chunks.get(0).getScalityId());
        }

        return new SproxydBufferedInputStream(chunks, client);
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        List<UUID> documentIds = chunkStorage.getDocuments();
        SortedSet<String> fileIds = new TreeSet<String>();
        for (UUID documentId : documentIds) {
            fileIds.add(UUIDs.getUnformattedString(documentId));
        }
        return fileIds;
    }

    @Override
    public long getFileSize(String name) throws OXException {
        Optional<Chunk> optionalLastChunk = chunkStorage.getLastChunk(UUIDs.fromUnformattedString(name));
        if (!optionalLastChunk.isPresent()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
        Chunk lastChunk = optionalLastChunk.get();
        return lastChunk.getOffset() + lastChunk.getLength();
    }

    @Override
    public String getMimeType(String name) throws OXException {
        return null;
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        UUID documentId = UUIDs.fromUnformattedString(identifier);
        Optional<List<Chunk>> optionalChunks = chunkStorage.optChunks(documentId);
        if (!optionalChunks.isPresent()) {
            return false;
        }

        List<Chunk> chunks = optionalChunks.get();
        int size = chunks.size();
        if (0 >= size) {
            // Apparently no such document exists; consider as deleted
            return true;
        }

        List<UUID> scalityIds = new ArrayList<UUID>(size);
        for (Chunk chunk : chunks) {
            scalityIds.add(chunk.getScalityId());
        }
        client.delete(scalityIds);
        chunkStorage.deleteDocument(documentId);
        return true;
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Set<String> notDeleted = new HashSet<String>();
        for (String identifier : identifiers) {
            if (false == deleteFile(identifier)) {
                notDeleted.add(identifier);
            }
        }
        return notDeleted;
    }

    @Override
    public void remove() throws OXException {
        SortedSet<String> fileList = getFileList();
        deleteFiles(fileList.toArray(new String[fileList.size()]));
    }

    @Override
    public void recreateStateFile() throws OXException {
        // no
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        // yes
        return true;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        File tmpFile = null;
        try {
            UUID documentId = UUIDs.fromUnformattedString(name);
            Optional<Chunk> optionalLastChunk = chunkStorage.getLastChunk(documentId);
            if (!optionalLastChunk.isPresent()) {
                throw FileStorageCodes.FILE_NOT_FOUND.create(name);
            }

            Chunk lastChunk = optionalLastChunk.get();
            long currentSize = lastChunk.getOffset() + lastChunk.getLength();
            if (offset != currentSize) {
                throw FileStorageCodes.INVALID_OFFSET.create(L(offset), name, L(currentSize));
            }

            /*
             * spool to file
             */
            if (!(file instanceof FileInputStream)) {
                Optional<File> optionalTempFile = TempFileHelper.getInstance().newTempFile();
                if (optionalTempFile.isPresent()) {
                    tmpFile = optionalTempFile.get();
                    file = Streams.transferToFileAndCreateStream(file, tmpFile);
                }
            }
            return upload(documentId, file, offset);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(file);
            TempFileHelper.deleteQuietly(tmpFile);
        }
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        UUID documentId = UUIDs.fromUnformattedString(name);
        List<Chunk> chunks = chunkStorage.getChunks(documentId);
        if (null == chunks || 0 == chunks.size()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
        for (Chunk chunk : chunks) {
            if (chunk.getOffset() >= length) {
                /*
                 * delete the whole chunk
                 */
                client.delete(chunk.getScalityId());
                chunkStorage.deleteChunk(chunk.getScalityId());
            } else if (chunk.getOffset() < length && length <= chunk.getOffset() + chunk.getLength()) {
                /*
                 * trim the last chunk
                 */
                long newChunkLength = chunk.getOffset() + chunk.getLength() - length;
                InputStream data = null;
                try {
                    data = client.get(chunk.getDocumentId(), 0, newChunkLength);
                    UUID scalityId = client.put(data, newChunkLength);
                    chunkStorage.storeChunk(new Chunk(documentId, scalityId, chunk.getOffset(), newChunkLength));
                } finally {
                    Streams.close(data);
                }
                client.delete(chunk.getScalityId());
                chunkStorage.deleteChunk(chunk.getScalityId());
                break;
            }
        }
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        if (0 >= offset && 0 >= length) {
            return getFile(name);
        }

        List<Chunk> chunks = chunkStorage.getChunks(UUIDs.fromUnformattedString(name));
        if (null == chunks || chunks.isEmpty()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }

        int size = chunks.size();
        if (1 == size) {
            /*
             * download parts of a single chunk
             */
            Chunk chunk = chunks.get(0);
            if (offset >= chunk.getLength() || length >= 0 && length > chunk.getLength() - offset) {
                throw FileStorageCodes.INVALID_RANGE.create(L(offset), L(length), name, L(chunk.getLength()));
            }
            long rangeStart = 0 < offset ? offset : 0;
            long rangeEnd = (0 < length ? rangeStart + length : chunk.getLength()) - 1;
            return client.get(chunk.getScalityId(), rangeStart, rangeEnd);
        }

        /*
         * download from multiple chunks
         */
        Chunk lastChunk = chunks.get(size - 1);
        long totalLength = lastChunk.getOffset() + lastChunk.getLength();
        if (offset >= totalLength || length >= 0 && length > totalLength - offset) {
            throw FileStorageCodes.INVALID_RANGE.create(L(offset), L(length), name, L(totalLength));
        }
        long rangeStart = 0 < offset ? offset : 0;
        long rangeEnd = (0 < length ? rangeStart + length : totalLength) - 1;
        return new SproxydBufferedInputStream(chunks, client, rangeStart, rangeEnd);
    }

    /**
     * Performs a chunk-wise upload of the supplied data.
     *
     * @param documentId The identifier of the document
     * @param data The document data
     * @param offset The current offset, or <code>0</code> for new files
     * @return The new total size of the document
     * @throws OXException
     */
    private long upload(UUID documentId, InputStream data, long offset) throws OXException {
        boolean success = false;
        DefaultChunkedUpload chunkedUpload = null;
        List<UUID> scalityIds = new LinkedList<UUID>();
        try {
            chunkedUpload = new DefaultChunkedUpload(data);
            Thread currentThread = Thread.currentThread();
            long off = offset;
            while (chunkedUpload.hasNext()) {
                if (currentThread.isInterrupted()) {
                    throw OXException.general("Upload to Sproxyd aborted");
                }
                UploadChunk chunk = chunkedUpload.next();
                try {
                    UUID scalityId = client.put(chunk.getData(), chunk.getSize());
                    scalityIds.add(scalityId);
                    chunkStorage.storeChunk(new Chunk(documentId, scalityId, off, chunk.getSize()));
                    off += chunk.getSize();
                } finally {
                    Streams.close(chunk);
                }
            }
            success = true;
            return off;
        } finally {
            Streams.close(chunkedUpload);
            if (false == success) {
                client.delete(scalityIds);
                for (UUID scalityId : scalityIds) {
                    chunkStorage.deleteChunk(scalityId);
                }
            }
        }
    }

}
