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

package com.openexchange.filestore.swift;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.swift.chunkstorage.Chunk;
import com.openexchange.filestore.swift.chunkstorage.ChunkStorage;
import com.openexchange.filestore.swift.impl.SwiftClient;
import com.openexchange.filestore.utils.DefaultChunkedUpload;
import com.openexchange.filestore.utils.UploadChunk;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SwiftFileStorage} - Represents a Swift file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SwiftFileStorage implements FileStorage {

    private final SwiftClient client;
    private final ChunkStorage chunkStorage;
    private final AtomicReference<Future<Void>> containerCreatedTask;

    /**
     * Initializes a new {@link SwiftFileStorage}.
     *
     * @param services A service lookup reference
     * @param client The spoxyd client to use
     * @param chunkStorage The underlying chunk storage
     */
    public SwiftFileStorage(SwiftClient client, ChunkStorage chunkStorage) {
        super();
        this.client = client;
        this.chunkStorage = chunkStorage;
        containerCreatedTask = new AtomicReference<Future<Void>>(null);
    }

    private void checkOrCreateContainer() throws OXException {
        Future<Void> f = containerCreatedTask.get();
        if (null == f) {
            synchronized (this) {
                f = containerCreatedTask.get();
                if (null == f) {
                    final SwiftClient client = this.client;
                    FutureTask<Void> ft = new FutureTask<Void>(new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            client.createContainerIfAbsent();
                            return null;
                        }
                    });
                    if (containerCreatedTask.compareAndSet(null, ft)) {
                        ft.run();
                        f = ft;
                    } else {
                        f = containerCreatedTask.get();
                    }
                }
            }
        }

        try {
            f.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, "Interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        try {
            UUID documentId = UUID.randomUUID();
            upload(documentId, file, 0);
            return UUIDs.getUnformattedString(documentId);
        } finally {
            Streams.close(file);
        }
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        checkOrCreateContainer();
        List<Chunk> chunks = chunkStorage.getChunks(UUIDs.fromUnformattedString(name));
        if (null == chunks || chunks.isEmpty()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }

        checkOrCreateContainer();

        if (1 == chunks.size()) {
            return client.get(chunks.get(0).getSwiftId());
        }

        return new SwiftBufferedInputStream(chunks, client);
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
        Chunk lastChunk = chunkStorage.getLastChunk(UUIDs.fromUnformattedString(name));
        if (null == lastChunk) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
        return lastChunk.getOffset() + lastChunk.getLength();
    }

    @Override
    public String getMimeType(String name) throws OXException {
        return null;
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        UUID documentId = UUIDs.fromUnformattedString(identifier);
        List<Chunk> chunks = chunkStorage.getChunks(documentId);
        if (null != chunks) {
            int size = chunks.size();
            if (0 < size) {
                List<UUID> swiftIds = new ArrayList<UUID>(size);
                for (Chunk chunk : chunks) {
                    swiftIds.add(chunk.getSwiftId());
                }
                checkOrCreateContainer();
                client.delete(swiftIds);
                chunkStorage.deleteDocument(documentId);
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        if (null == identifiers || identifiers.length <= 0) {
            return Collections.emptySet();
        }

        Set<String> notDeleted = new HashSet<String>(identifiers.length);
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
        try {
            client.deleteContainer();
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(SwiftFileStorage.class);
            logger.warn("Failed to delete objects prefixed with {}", client.getPrefix(), e);
        }
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
        try {
            UUID documentId = UUIDs.fromUnformattedString(name);
            Chunk lastChunk = chunkStorage.getLastChunk(documentId);
            if (null == lastChunk) {
                throw FileStorageCodes.FILE_NOT_FOUND.create(name);
            }
            long currentSize = lastChunk.getOffset() + lastChunk.getLength();
            if (offset != currentSize) {
                throw FileStorageCodes.INVALID_OFFSET.create(offset, name, currentSize);
            }
            return upload(documentId, file, offset);
        } finally {
            Streams.close(file);
        }
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        UUID documentId = UUIDs.fromUnformattedString(name);
        List<Chunk> chunks = chunkStorage.getChunks(documentId);
        if (null == chunks || chunks.isEmpty()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }

        checkOrCreateContainer();

        for (Chunk chunk : chunks) {
            if (chunk.getOffset() >= length) {
                /*
                 * delete the whole chunk
                 */
                client.delete(chunk.getSwiftId());
                chunkStorage.deleteChunk(chunk.getSwiftId());
            } else if (chunk.getOffset() < length && length <= chunk.getOffset() + chunk.getLength()) {
                /*
                 * trim the last chunk
                 */
                long newChunkLength = chunk.getOffset() + chunk.getLength() - length;
                InputStream data = null;
                try {
                    data = client.get(chunk.getDocumentId(), 0, newChunkLength);
                    UUID swiftId = client.put(data, newChunkLength);
                    chunkStorage.storeChunk(new Chunk(documentId, swiftId, chunk.getOffset(), newChunkLength));
                } finally {
                    Streams.close(data);
                }
                client.delete(chunk.getSwiftId());
                chunkStorage.deleteChunk(chunk.getSwiftId());
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
            if ((offset >= chunk.getLength()) || ((length >= 0) && (length > (chunk.getLength() - offset)))) {
                throw FileStorageCodes.INVALID_RANGE.create(offset, length, name, chunk.getLength());
            }

            checkOrCreateContainer();

            long rangeStart = 0 < offset ? offset : 0;
            long rangeEnd = (0 < length ? rangeStart + length : chunk.getLength()) - 1;
            return client.get(chunk.getSwiftId(), rangeStart, rangeEnd);
        }

        /*
         * download from multiple chunks
         */
        Chunk lastChunk = chunks.get(size - 1);
        long totalLength = lastChunk.getOffset() + lastChunk.getLength();
        if ((offset >= totalLength) || ((length >= 0) && (length > (totalLength - offset)))) {
            throw FileStorageCodes.INVALID_RANGE.create(offset, length, name, totalLength);
        }

        checkOrCreateContainer();

        long rangeStart = 0 < offset ? offset : 0;
        long rangeEnd = (0 < length ? rangeStart + length : totalLength) - 1;
        return new SwiftBufferedInputStream(chunks, client, rangeStart, rangeEnd);
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
        boolean deleteUploadedChunks = true;
        DefaultChunkedUpload chunkedUpload = null;
        List<UUID> swiftIds = new ArrayList<UUID>();
        try {
            chunkedUpload = new DefaultChunkedUpload(data);
            long off = offset;
            if (!chunkedUpload.hasNext()) {
                return off;
            }

            // Chunks available for upload...
            checkOrCreateContainer();
            do {
                UploadChunk chunk = chunkedUpload.next();
                try {
                    UUID swiftId = client.put(chunk.getData(), chunk.getSize());
                    swiftIds.add(swiftId);
                    chunkStorage.storeChunk(new Chunk(documentId, swiftId, off, chunk.getSize()));
                    off += chunk.getSize();
                } finally {
                    Streams.close(chunk);
                }
            } while (chunkedUpload.hasNext());
            deleteUploadedChunks = false;
            return off;
        } catch (OXException e) {
            if (SwiftExceptionCode.AUTH_FAILED.equals(e)) {
                // In case authentication failed an attempt to delete uploaded chunks would also fail
                deleteUploadedChunks = false;
            }
            throw e;
        } finally {
            Streams.close(chunkedUpload);
            if (deleteUploadedChunks) {
                client.delete(swiftIds);
                for (UUID swiftId : swiftIds) {
                    chunkStorage.deleteChunk(swiftId);
                }
            }
        }
    }

}
