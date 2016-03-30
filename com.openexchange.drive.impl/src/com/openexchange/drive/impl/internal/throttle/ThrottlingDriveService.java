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
    public ThrottlingDriveService(DriveService delegate) throws OXException {
        super();
        this.delegate = delegate;
        currentSyncOperations = new AtomicInteger();
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions,
        List<DirectoryVersion> clientVersions) throws OXException {
        try {
            enterSyncOperation();
            return delegate.syncFolders(session, originalVersions, clientVersions);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public SyncResult<FileVersion> syncFiles(DriveSession session, String path, List<FileVersion> originalVersions,
        List<FileVersion> clientVersions) throws OXException {
        try {
            enterSyncOperation();
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
            enterSyncOperation();
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
            enterSyncOperation();
            return delegate.getFileMetadata(session, path, fileVersions, fields);
        } finally {
            leaveSyncOperation();
        }
    }

    @Override
    public DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException {
        try {
            enterSyncOperation();
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

    private void enterSyncOperation() throws OXException {
        int maxConcurrentSyncOperations = DriveConfig.getInstance().getMaxConcurrentSyncOperations();
        if (0 < maxConcurrentSyncOperations && maxConcurrentSyncOperations < currentSyncOperations.incrementAndGet()) {
            throw DriveExceptionCodes.SERVER_BUSY.create();
        }
    }

    private void leaveSyncOperation() {
        currentSyncOperations.decrementAndGet();
    }

}
