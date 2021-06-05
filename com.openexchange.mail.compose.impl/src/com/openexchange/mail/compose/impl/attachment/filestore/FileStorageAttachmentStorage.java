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

package com.openexchange.mail.compose.impl.attachment.filestore;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.compose.impl.attachment.filestore.ContextAssociatedFileStorageAttachmentStorage.getContextAssociatedFileStorage;
import static com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage.getDedicatedFileStorage;
import static com.openexchange.mail.compose.impl.util.TimeLimitedFileStorageOperation.createBuilder;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageIdentifier.KnownArgument;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.SeekingDataProvider;
import com.openexchange.mail.compose.impl.attachment.AbstractNonCryptoAttachmentStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;


/**
 * {@link FileStorageAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class FileStorageAttachmentStorage extends AbstractNonCryptoAttachmentStorage {

    /**
     * Initializes a new {@link FileStorageAttachmentStorage}.
     */
    protected FileStorageAttachmentStorage(ServiceLookup services) {
        super(services);
    }

    /**
     * Gets the appropriate {@link FileStorage} to use for this <code>FileStorageAttachmentStorage</code> instance.
     *
     * @param dedicatedFileStorageId The optional identifier for dedicated file storage
     * @param session The session providing user/context data
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    protected FileStorageAndId getFileStorage(Optional<Integer> dedicatedFileStorageId, Session session) throws OXException {
        if (dedicatedFileStorageId.isPresent()) {
            int fileStorageId = dedicatedFileStorageId.get().intValue();
            if (fileStorageId > 0) {
                Pair<FileStorage, URI> fsAndUri = getDedicatedFileStorage(fileStorageId, session.getContextId());
                return new FileStorageAndId(fsAndUri.getFirst(), fileStorageId, fsAndUri.getSecond());
            }

            Pair<FileStorage, URI> fsAndUri = getContextAssociatedFileStorage(session.getContextId());
            return new FileStorageAndId(fsAndUri.getFirst(), fsAndUri.getSecond());
        }

        return getFileStorage(session);
    }

    /**
     * Gets the appropriate {@link FileStorage} to use for this <code>FileStorageAttachmentStorage</code> instance.
     *
     * @param session The session providing user/context data
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    protected abstract FileStorageAndId getFileStorage(Session session) throws OXException;

    @Override
    protected DataProvider getDataProviderFor(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException {
        return new FileStorageDataProvider(session, storageIdentifier, this);
    }

    @Override
    protected AttachmentStorageIdentifier saveData(InputStream input, long size, Session session) throws OXException {
        FileStorageAndId fileStorageRef = getFileStorage(Optional.empty(), session);

        AtomicBoolean deleteLocation = new AtomicBoolean(false);
        Task<String> saveDataTask = new AbstractTask<String>() {

            @Override
            public String call() throws Exception {
                String location = fileStorageRef.fileStorage.saveNewFile(input);
                if (deleteLocation.get()) {
                    fileStorageRef.fileStorage.deleteFile(location);
                    return null;
                }
                return location;
            }
        };
        String storageIdentifier = createBuilder(saveDataTask, fileStorageRef.fileStorage)
            .withTaskFlag(deleteLocation)
            .withWaitTimeoutSeconds(60)
            .buildAndSubmit()
            .getResult();
        if (fileStorageRef.dedicatedFileStorageId <= 0) {
            return new AttachmentStorageIdentifier(storageIdentifier, KnownArgument.FILE_STORAGE_IDENTIFIER, I(0));
        }
        return new AttachmentStorageIdentifier(storageIdentifier, KnownArgument.FILE_STORAGE_IDENTIFIER, I(fileStorageRef.dedicatedFileStorageId));
    }

    @Override
    protected boolean deleteData(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException {
        FileStorage fileStorage = getFileStorage(storageIdentifier.getArgument(KnownArgument.FILE_STORAGE_IDENTIFIER), session).fileStorage;
        Task<Boolean> deleteDataTask = new AbstractTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(fileStorage.deleteFile(storageIdentifier.getIdentifier()));
            }
        };
        Boolean deleted = createBuilder(deleteDataTask, fileStorage)
            .withWaitTimeoutSeconds(10)
            .buildAndSubmit()
            .getResult();
        return deleted.booleanValue();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** Reference to actual file storage and optional dedicated file storage identifier */
    protected static class FileStorageAndId {

        /** The  actual file storage */
        protected final FileStorage fileStorage;

        /** The optional identifier for dedicated file storage */
        protected final int dedicatedFileStorageId;

        /** The URI that fully qualifies the file storage */
        protected final URI uri;

        /**
         * Initializes a new {@link FileStorageAndId}.
         *
         * @param fileStorage The file storage
         * @param uri The URI that fully qualifies the file storage
         */
        protected FileStorageAndId(FileStorage fileStorage, URI uri) {
            this(fileStorage, 0, uri);
        }

        /**
         * Initializes a new {@link FileStorageAndId}.
         *
         * @param fileStorage The file storage
         * @param dedicatedFileStorageId The dedicated file storage identifier or <code>0</code> (zero)
         * @param uri The URI that fully qualifies the file storage
         */
        protected FileStorageAndId(FileStorage fileStorage, int dedicatedFileStorageId, URI uri) {
            super();
            this.fileStorage = fileStorage;
            this.dedicatedFileStorageId = dedicatedFileStorageId <= 0 ? 0 : dedicatedFileStorageId;
            this.uri = uri;
        }
    }

    private static class FileStorageDataProvider implements SeekingDataProvider {

        private final Session session;
        private final AttachmentStorageIdentifier storageIdentifier;
        private final FileStorageAttachmentStorage attachmentStorage;

        /**
         * Initializes a new {@link DataProviderImplementation}.
         */
        FileStorageDataProvider(Session session, AttachmentStorageIdentifier storageIdentifier, FileStorageAttachmentStorage attachmentStorage) {
            super();
            this.session = session;
            this.storageIdentifier = storageIdentifier;
            this.attachmentStorage = attachmentStorage;
        }

        private FileStorage getFileStorage() throws OXException {
            return attachmentStorage.getFileStorage(storageIdentifier.getArgument(KnownArgument.FILE_STORAGE_IDENTIFIER), session).fileStorage;
        }

        @Override
        public InputStream getData() throws OXException {
            String location = storageIdentifier.getIdentifier();
            try {
                FileStorage fileStorage = getFileStorage();
                Task<InputStream> getFileTask = new AbstractTask<InputStream>() {

                    @Override
                    public InputStream call() throws Exception {
                        return fileStorage.getFile(location);
                    }
                };
                return createBuilder(getFileTask, fileStorage)
                    .withOnTimeOutHandler(() -> CompositionSpaceErrorCode.FAILED_RETRIEVAL_ATTACHMENT_RESOURCE.create(location))
                    .withWaitTimeoutSeconds(10)
                    .buildAndSubmit()
                    .getResult();
            } catch (OXException e) {
                if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                    throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(e, location);
                }
                throw e;
            }
        }

        @Override
        public InputStream getData(long offset, long length) throws OXException {
            String location = storageIdentifier.getIdentifier();
            try {
                FileStorage fileStorage = getFileStorage();
                Task<InputStream> getFileTask = new AbstractTask<InputStream>() {

                    @Override
                    public InputStream call() throws Exception {
                        return fileStorage.getFile(location, offset, length);
                    }
                };
                return createBuilder(getFileTask, fileStorage)
                    .withOnTimeOutHandler(() -> CompositionSpaceErrorCode.FAILED_RETRIEVAL_ATTACHMENT_RESOURCE.create(location))
                    .withWaitTimeoutSeconds(10)
                    .buildAndSubmit()
                    .getResult();
            } catch (OXException e) {
                if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                    throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(e, location);
                }
                throw e;
            }
        }
    }

}
