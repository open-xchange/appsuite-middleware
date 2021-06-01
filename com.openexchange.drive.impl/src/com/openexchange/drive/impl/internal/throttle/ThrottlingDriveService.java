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
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ThrottlingDriveService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ThrottlingDriveService implements DriveService {

    private final DriveService delegate;
    private final AtomicInteger currentSyncOperations;

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
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolder(DriveSession session, DirectoryVersion originalVersion, DirectoryVersion clientVersion) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.syncFolder(session, originalVersion, clientVersion);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions, List<DirectoryVersion> clientVersions) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.syncFolders(session, originalVersions, clientVersions);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public SyncResult<FileVersion> syncFiles(DriveSession session, String path, List<FileVersion> originalVersions, List<FileVersion> clientVersions) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.syncFiles(session, path, originalVersions, clientVersions);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public IFileHolder download(DriveSession session, String path, FileVersion fileVersion, long offset, long length) throws OXException {
        return delegate.download(session, path, fileVersion, offset, length);
    }

    @Override
    public SyncResult<FileVersion> upload(DriveSession session, String path, InputStream uploadStream, FileVersion originalVersion,
        FileVersion newVersion, String contentType, long offset, long totalLength, Date created, Date modified) throws OXException {
        return delegate.upload(
            session, path, uploadStream, originalVersion, newVersion, contentType, offset, totalLength, created, modified);
    }

    @Override
    public DriveQuota getQuota(DriveSession session) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.getQuota(session);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public DriveSettings getSettings(DriveSession session) throws OXException {
        return delegate.getSettings(session);
    }

    @Override
    public List<DriveFileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions, List<DriveFileField> fields) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.getFileMetadata(session, path, fileVersions, fields);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException {
        try {
            enterSyncOperation(session);
            return delegate.getDirectoryMetadata(session, path);
        } finally {
            leaveSyncOperation();
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

    private void enterSyncOperation(DriveSession session) throws OXException {
        ServerSession serverSession = session.getServerSession();
        int maxConcurrentSyncOperations = new DriveConfig(serverSession.getContextId(), serverSession.getUserId()).getMaxConcurrentSyncOperations();
        if (0 < maxConcurrentSyncOperations && maxConcurrentSyncOperations < currentSyncOperations.incrementAndGet()) {
            throw DriveExceptionCodes.SERVER_BUSY.create();
        }
    }

    private void leaveSyncOperation() {
        currentSyncOperations.decrementAndGet();
    }

}
