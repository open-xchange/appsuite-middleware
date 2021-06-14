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

package com.openexchange.drive.impl.internal.throttle;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.AtomicLongMap;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.drive.DirectoryMetadata;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveSettings;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.session.UserAndContext;

/**
 * {@link ThrottlingDriveService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ThrottlingDriveService implements DriveService {

    private final DriveService delegate;
    private final AtomicInteger currentSyncOperations;
    private final AtomicLongMap<UserAndContext> concurrentSyncFiles;
    private final AtomicLongMap<UserAndContext> concurrentSyncFolders;

    /**
     * Initializes a new {@link ThrottlingDriveService}.
     *
     * @param delegate The drive service delegate
     * @throws OXException
     */
    public ThrottlingDriveService(DriveService delegate) {
        super();
        this.delegate = delegate;
        currentSyncOperations = new AtomicInteger();
        concurrentSyncFiles = AtomicLongMap.create();
        concurrentSyncFolders = AtomicLongMap.create();
    }

    @SuppressWarnings("synthetic-access")
    private class SyncMonitor {

        private final DriveSession session;
        private final UserAndContext userAndContext;

        boolean syncOperationsIncremented;
        boolean syncFoldersOperationsIncremented;
        boolean syncFilesOperationsIncremented;

        SyncMonitor(DriveSession session) {
            this.session = session;
            this.userAndContext = UserAndContext.newInstance(session.getServerSession());
        }

        void enterSyncOperation() throws OXException {
            int maxConcurrentSyncOperations = new DriveConfig(session.getServerSession().getContextId(), session.getServerSession().getUserId()).getMaxConcurrentSyncOperations();
            if (0 < maxConcurrentSyncOperations) {
                int currentSyncOps = currentSyncOperations.incrementAndGet();
                syncOperationsIncremented = true;
                if (maxConcurrentSyncOperations < currentSyncOps) {
                    throw DriveExceptionCodes.SERVER_BUSY.create();
                }
            }
        }

        void leaveSyncOperation() {
            if (syncOperationsIncremented) {
                currentSyncOperations.decrementAndGet();
            }
        }

        void enterSyncFoldersOperation() throws OXException {
            int maxConcurrentSyncFolders = new DriveConfig(session.getServerSession().getContextId(), session.getServerSession().getUserId()).getMaxConcurrentSyncFolders();
            if (0 < maxConcurrentSyncFolders) {
                long currentSyncFoldersOps = concurrentSyncFolders.incrementAndGet(userAndContext);
                syncFoldersOperationsIncremented = true;
                if (maxConcurrentSyncFolders < currentSyncFoldersOps) {
                    throw DriveExceptionCodes.SERVER_BUSY.create();
                }
            }
        }

        void leaveSyncFoldersOperation() {
            if (syncFoldersOperationsIncremented) {
                concurrentSyncFolders.decrementAndGet(userAndContext);
                concurrentSyncFolders.removeIfZero(userAndContext);
            }
        }

        void enterSyncFilesOperation() throws OXException {
            int maxConcurrentSyncFiles = new DriveConfig(session.getServerSession().getContextId(), session.getServerSession().getUserId()).getMaxConcurrentSyncFiles();
            if (0 < maxConcurrentSyncFiles) {
                long currentSyncFilesOps = concurrentSyncFiles.incrementAndGet(userAndContext);
                syncFilesOperationsIncremented = true;
                if (maxConcurrentSyncFiles < currentSyncFilesOps) {
                    throw DriveExceptionCodes.SERVER_BUSY.create();
                }
            }
        }

        void leaveSyncFilesOperation() {
            if (syncFilesOperationsIncremented) {
                concurrentSyncFiles.decrementAndGet(userAndContext);
                concurrentSyncFiles.removeIfZero(userAndContext);
            }
        }
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolder(DriveSession session, DirectoryVersion originalVersion, DirectoryVersion clientVersion) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            monitor.enterSyncFoldersOperation();
            return delegate.syncFolder(session, originalVersion, clientVersion);
        } finally {
            monitor.leaveSyncFoldersOperation();
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions, List<DirectoryVersion> clientVersions) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            monitor.enterSyncFoldersOperation();
            return delegate.syncFolders(session, originalVersions, clientVersions);
        } finally {
            monitor.leaveSyncFoldersOperation();
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public SyncResult<FileVersion> syncFiles(DriveSession session, String path, List<FileVersion> originalVersions, List<FileVersion> clientVersions) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            monitor.enterSyncFilesOperation();
            return delegate.syncFiles(session, path, originalVersions, clientVersions);
        } finally {
            monitor.leaveSyncFilesOperation();
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public IFileHolder download(DriveSession session, String path, FileVersion fileVersion, long offset, long length) throws OXException {
        return delegate.download(session, path, fileVersion, offset, length);
    }

    @Override
    //@formatter:off
    public SyncResult<FileVersion> upload(DriveSession session, String path, InputStream uploadStream, FileVersion originalVersion,
        FileVersion newVersion, String contentType, long offset, long totalLength, Date created, Date modified) throws OXException {
        return delegate.upload(
            session, path, uploadStream, originalVersion, newVersion, contentType, offset, totalLength, created, modified);
    }
    //@formatter:on

    @Override
    public DriveQuota getQuota(DriveSession session) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            return delegate.getQuota(session);
        } finally {
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public DriveSettings getSettings(DriveSession session) throws OXException {
        return delegate.getSettings(session);
    }

    @Override
    public List<DriveFileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions, List<DriveFileField> fields) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            return delegate.getFileMetadata(session, path, fileVersions, fields);
        } finally {
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException {
        SyncMonitor monitor = new SyncMonitor(session);
        try {
            monitor.enterSyncOperation();
            return delegate.getDirectoryMetadata(session, path);
        } finally {
            monitor.leaveSyncOperation();
        }
    }

    @Override
    public String getJumpRedirectUrl(DriveSession session, String path, String fileName, String method) throws OXException {
        return delegate.getJumpRedirectUrl(session, path, fileName, method);
    }

    @Override
    public DriveUtility getUtility() {
        return delegate.getUtility();
    }

}
