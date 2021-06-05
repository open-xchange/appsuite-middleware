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

package com.openexchange.file.storage.json.actions.files;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.LAST_MODIFIED;
import static com.openexchange.java.Streams.bufferedInputStreamFor;
import static com.openexchange.tools.images.ImageTransformationUtility.seemsLikeThumbnailRequest;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.slf4j.Logger;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.ajax.requesthandler.LastModifiedAwareAJAXActionService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.java.Reference;
import com.openexchange.java.Streams;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAwares;

/**
 * {@link DocumentAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public class DocumentAction extends AbstractFileAction implements ETagAwareAJAXActionService, LastModifiedAwareAJAXActionService {

    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DocumentAction.class);

    public static final String DOCUMENT = "com.openexchange.file.storage.json.DocumentAction.DOCUMENT";

    /** The default in-memory threshold. */
    private static final int DEFAULT_IN_MEMORY_THRESHOLD = 65536; // 64KB

    private final boolean inlineIfPossible;

    /**
     * Initializes a new {@link DocumentAction}.
     */
    public DocumentAction() {
        super();
        inlineIfPossible = false;
    }

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.require(Param.ID);
        FileID fileID = new FileID(request.getId());
        IDBasedFileAccess fileAccess = request.getFileAccess();
        /*
         * handle request for thumbnails directly if supported by storage
         */
        if (seemsLikeThumbnailRequest(request.getRequestData()) && fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.THUMBNAIL_IMAGES)) {
            File metadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());
            InputStreamClosure isClosure = getThumbnailStream(request.getSession(), request.getId(), request.getVersion());
            IFileHolder fileHolder = new FileHolder(isClosure, -1, null, metadata.getFileName());
            boolean scanned = scan(request, fileHolder, metadata);
            if (scanned && false == fileHolder.repetitive()) {
                isClosure = getThumbnailStream(request.getSession(), request.getId(), request.getVersion());
                fileHolder = new FileHolder(isClosure, -1, null, metadata.getFileName());
            }
            AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            createAndSetETag(metadata, result);
            setLastModified(metadata, result);
            return result;
        }
        /*
         * prepare result for efficient document retrieval enabled storages if possible
         */
        if (fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.EFFICIENT_RETRIEVAL)) {
            Document document = request.getCachedDocument();
            if (null == document) {
                document = fileAccess.getDocumentAndMetadata(request.getId(), request.getVersion());
            }
            if (null != document) {
                AJAXRequestResult result;
                if (inlineIfPossible && document.getSize() > 0 && document.getSize() <= DEFAULT_IN_MEMORY_THRESHOLD) {
                    ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                    try {
                        fileHolder.write(document.getData());
                        boolean scanned = scan(request, fileHolder, document.getFile());
                        if (scanned && false == document.isRepetitive()) {
                            fileHolder.write(document.getData());
                        }
                        result = new AJAXRequestResult(fileHolder, "file");
                        fileHolder = null;
                    } finally {
                        Streams.close(fileHolder);
                    }
                } else {
                    long size = document.getSize();
                    if (false == document.getFile().isAccurateSize()) {
                        size = -1;
                    }

                    FileHolder fileHolder = document.isRepetitive() ? new FileHolder(getDocumentStream(document), size, document.getMimeType(), document.getName()) : new FileHolder(bufferedInputStreamFor(document.getData()), size, document.getMimeType(), document.getName());
                    boolean scanned = scan(request, fileHolder, document.getFile());
                    if (scanned && false == document.isRepetitive()) {
                        fileHolder = new FileHolder(bufferedInputStreamFor(document.getData()), size, document.getMimeType(), document.getName());
                    }
                    try {
                        if (fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.RANDOM_FILE_ACCESS)) {
                            fileHolder.setRandomAccessClosure(new IDBasedFileAccessRandomAccessClosure(request.getId(), request.getVersion(), size, request.getSession()));
                        }
                        result = new AJAXRequestResult(fileHolder, "file");
                        fileHolder = null;
                    } finally {
                        Streams.close(fileHolder);
                    }
                }

                if (null != document.getEtag()) {
                    setETag(document.getEtag(), 0, result);
                }
                if (0 < document.getLastModified()) {
                    setLastModified(new Date(document.getLastModified()), result);
                }
                return result;
            }
        }
        /*
         * prepare regular document result as fallback
         */
        File metadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());
        AJAXRequestResult result;
        if (inlineIfPossible && metadata.getFileSize() > 0 && metadata.getFileSize() <= DEFAULT_IN_MEMORY_THRESHOLD) {
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            try {
                fileHolder.write(fileAccess.getDocument(request.getId(), request.getVersion()));
                boolean scanned = scan(request, fileHolder, metadata);
                if (scanned && false == fileHolder.repetitive()) {
                    fileHolder.write(fileAccess.getDocument(request.getId(), request.getVersion()));
                }
                result = new AJAXRequestResult(fileHolder, "file");
                fileHolder = null;
            } finally {
                Streams.close(fileHolder);
            }
        } else {
            InputStreamClosure isClosure = getDocumentStream(request.getSession(), request.getId(), request.getVersion());
            long size = metadata.getFileSize();
            if (false == metadata.isAccurateSize()) {
                size = -1;
            }
            FileHolder fileHolder = new FileHolder(isClosure, size, metadata.getFileMIMEType(), metadata.getFileName());
            try {
                if (fileAccess.supports(fileID.getService(), fileID.getAccountId(), FileStorageCapability.RANDOM_FILE_ACCESS)) {
                    fileHolder.setRandomAccessClosure(new IDBasedFileAccessRandomAccessClosure(request.getId(), request.getVersion(), size, request.getSession()));
                }
                scan(request, fileHolder, metadata);
                result = new AJAXRequestResult(fileHolder, "file");
                fileHolder = null;
            } finally {
                Streams.close(fileHolder);
            }
        }
        createAndSetETag(metadata, result);
        setLastModified(metadata, result);
        return result;
    }

    private void createAndSetETag(File fileMetadata, AJAXRequestResult result) throws OXException {
        setETag(FileStorageUtility.getETagFor(fileMetadata), 0, result);
    }

    private void setLastModified(File fileMetadata, AJAXRequestResult result) {
        setLastModified(fileMetadata.getLastModified(), result);
    }

    private void setLastModified(Date lastModified, AJAXRequestResult result) {
        if (null != lastModified) {
            result.setHeader(LAST_MODIFIED, Tools.formatHeaderDate(lastModified));
        }
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData requestData, ServerSession session) throws OXException {
        final AJAXInfostoreRequest request = new AJAXInfostoreRequest(requestData, session);
        final IDBasedFileAccess fileAccess = request.getFileAccess();

        final String id = request.getId();
        final String version = request.getVersion();

        final Document document = fileAccess.getDocumentAndMetadata(id, version, clientETag);
        if (document != null) {
            requestData.setProperty(DOCUMENT, document);
            String etag = document.getEtag();
            return etag != null && etag.equals(clientETag);
        }

        final File fileMetadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());
        return FileStorageUtility.getETagFor(fileMetadata).equals(clientETag);
    }

    @Override
    public boolean checkLastModified(long clientLastModified, AJAXRequestData requestData, ServerSession session) throws OXException {
        AJAXInfostoreRequest request = new AJAXInfostoreRequest(requestData, session);
        IDBasedFileAccess fileAccess = request.getFileAccess();

        final String id = request.getId();
        final String version = request.getVersion();

        final Document document = fileAccess.getDocumentAndMetadata(id, version);
        if (document != null) {
            requestData.setProperty(DOCUMENT, document);
            long lastModified = document.getLastModified();
            return lastModified > 0 ? false : clientLastModified > lastModified;
        }

        File fileMetadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());

        Date lastModified = fileMetadata.getLastModified();
        return null == lastModified ? false : clientLastModified > lastModified.getTime();
    }

    @Override
    public void setETag(String eTag, long expires, AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        if (eTag != null) {
            result.setHeader(ETAG, eTag);
        }
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * Tries to get a file's thumbnail directly, falling back to the regular document stream if not available.
     *
     * @param session The session
     * @param id The identifier of the file to get the thumbnail stream for
     * @param version The file version to retrieve the thumbnail for
     * @return A file holder providing access to the thumbnail data
     */
    private IFileHolder.InputStreamClosure getThumbnailStream(final ServerSession session, final String id, final String version) {
        return new IFileHolder.InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                IDBasedFileAccess fileAccess = Services.getFileAccessFactory().createAccess(session);
                InputStream inputStream;
                try {
                    inputStream = fileAccess.optThumbnailStream(id, version);
                } catch (OXException e) {
                    LOGGER.debug("Unable to retrieve thumbnail for file: {}, falling back to regular document stream.", id, e);
                    inputStream = null;
                }
                if (null == inputStream) {
                    inputStream = fileAccess.getDocument(id, version);
                }
                return bufferedInputStreamFor(inputStream);
            }
        };
    }

    /**
     * Gets an input stream closure for a specific file.
     *
     * @param session The session
     * @param id The identifier of the file to get the input stream for
     * @param version The file version to retrieve the input stream for
     * @return A file holder providing access to the document data
     */
    private IFileHolder.InputStreamClosure getDocumentStream(final ServerSession session, final String id, final String version) {
        return new IFileHolder.InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                IDBasedFileAccess fileAccess = Services.getFileAccessFactory().createAccess(session);
                return bufferedInputStreamFor(fileAccess.getDocument(id, version));
            }
        };
    }

    /**
     * Gets an input stream closure for a document.
     *
     * @param document The document
     * @return A file holder providing access to the document data
     */
    private IFileHolder.InputStreamClosure getDocumentStream(final Document document) {
        return new IFileHolder.InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                return bufferedInputStreamFor(document.getData());
            }
        };
    }

    private static class IDBasedFileAccessRandomAccessClosure implements IFileHolder.RandomAccessClosure {

        private final String id;
        private final String version;
        private final ServerSession session;
        private final long length;

        IDBasedFileAccessRandomAccessClosure(String id, String version, long length, ServerSession session) {
            super();
            this.id = id;
            this.version = version;
            this.length = length;
            this.session = session;
        }

        @Override
        public IFileHolder.RandomAccess newRandomAccess() throws OXException, IOException {
            return new IDBasedFileAccessRandomAccess(id, version, length, session);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append("IDBasedFileAccessRandomAccessClosure [");
            if (id != null) {
                builder.append("id=").append(id).append(", ");
            }
            if (version != null) {
                builder.append("version=").append(version).append(", ");
            }
            builder.append("length=").append(length).append(']');
            return builder.toString();
        }
    }

    private static class IDBasedFileAccessRandomAccess implements IFileHolder.RandomAccess, IFileHolder.InputStreamClosure {

        private static final long EXPIRATION_DELAY = 5000L;

        private final String id;
        private final String version;
        private final ServerSession session;
        private final long length;
        private final Reference<LastAccessKnowingStream> documentStreamReference;
        private final Runnable streamCloser;
        private ScheduledTimerTask timerTask = null;
        private long pos = 0;

        IDBasedFileAccessRandomAccess(String id, String version, long length, ServerSession session) {
            super();
            this.id = id;
            this.version = version;
            this.length = length;
            this.session = session;
            this.documentStreamReference = new Reference<>(null);
            streamCloser = new Runnable() {

                @Override
                public void run() {
                    closeResources(true);
                }
            };
        }

        void closeResources(boolean onlyCloseIfExpired) {
            synchronized (documentStreamReference) {
                LastAccessKnowingStream documentStream = documentStreamReference.getValue();
                if (onlyCloseIfExpired && (documentStream == null || (System.currentTimeMillis() - documentStream.getLastAccessed() <= EXPIRATION_DELAY))) {
                    return;
                }

                // Close resources
                if (documentStream != null) {
                    // Drop reference
                    documentStreamReference.setValue(null);
                    Streams.close(documentStream);
                }

                // Cancel timer task
                ScheduledTimerTask timerTask = this.timerTask;
                if (timerTask != null) {
                    this.timerTask = null;
                    timerTask.cancel();
                }
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            long count = this.length;
            if (pos >= count) {
                return -1;
            }

            int length = len;
            long avail = count - pos;
            if (length > avail) {
                length = (int) avail;
            }
            if (length <= 0) {
                return 0;
            }

            boolean error = true; // pessimistic
            try {
                LastAccessKnowingStream documentStream;
                synchronized (documentStreamReference) {
                    documentStream = documentStreamReference.getValue();
                    if (documentStream == null) {
                        TimerService timerService = Services.getTimerService();
                        if (timerService == null) {
                            throw new IOException("No such service: " + TimerService.class.getName());
                        }

                        documentStream = initStream();
                        documentStreamReference.setValue(documentStream);
                        timerTask = timerService.scheduleWithFixedDelay(streamCloser, EXPIRATION_DELAY, EXPIRATION_DELAY >> 1);
                    }
                }

                int read = documentStream.read(b, off, length);
                pos += read;
                error = false; // All went fine
                return read;
            } finally {
                if (error) {
                    closeResources(false);
                }
            }
        }

        private LastAccessKnowingStream initStream() throws IOException {
            IDBasedFileAccess newFileAccess = null;
            try {
                newFileAccess = Services.getFileAccessFactory().createAccess(session);
                InputStream partialIn = null;
                try {
                    partialIn = newFileAccess.getDocument(id, version, pos, -1);
                    LastAccessKnowingStream documentStream = new LastAccessKnowingStream(TransactionAwares.finishingInputStream(partialIn, newFileAccess));
                    partialIn = null; // Avoid premature closing
                    newFileAccess = null; // Avoid premature closing
                    return documentStream;
                } catch (OXException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                    throw new IOException(null == cause ? e : cause);
                } finally {
                    Streams.close(partialIn);
                }
            } finally {
                TransactionAwares.finishSafe(newFileAccess);
            }
        }

        @Override
        public void close() throws IOException {
            closeResources(false);
        }

        @Override
        public void seek(long pos) throws IOException {
            this.pos = pos;
            closeResources(false);
        }

        @Override
        public long length() throws IOException {
            return length;
        }

        @Override
        public InputStream newStream() throws OXException, IOException {
            IDBasedFileAccess newFileAccess = Services.getFileAccessFactory().createAccess(session);
            return bufferedInputStreamFor(TransactionAwares.finishingInputStream(newFileAccess.getDocument(id, version), newFileAccess));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append("IDBasedFileAccessRandomAccess [");
            if (id != null) {
                builder.append("id=").append(id).append(", ");
            }
            if (version != null) {
                builder.append("version=").append(version).append(", ");
            }
            builder.append("length=").append(length).append(", pos=").append(pos).append(']');
            return builder.toString();
        }
    }

    private static class LastAccessKnowingStream extends FilterInputStream {

        private long lastAccessed;

        /**
         * Initializes a new {@link LastAccessKnowingStream}.
         *
         * @param in The input stream
         */
        LastAccessKnowingStream(InputStream in) {
            super(in);
            this.lastAccessed = System.currentTimeMillis();
        }

        @Override
        public int read() throws IOException {
            int read = super.read();
            this.lastAccessed = System.currentTimeMillis();
            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int readLen = super.read(b, off, len);
            this.lastAccessed = System.currentTimeMillis();
            return readLen;
        }

        /**
         * Gets the last-accessed time stamp, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT.
         *
         * @return The last-accessed time stamp
         */
        public long getLastAccessed() {
            return lastAccessed;
        }
    } // End of class LastAccessKnowingStream

}
