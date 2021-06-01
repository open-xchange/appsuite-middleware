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

package com.openexchange.drive.impl.internal;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;

/**
 * {@link DownloadHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadHelper {

    private final SyncSession session;

    /**
     * Initializes a new {@link DownloadHelper}.
     *
     * @param session The sync session
     */
    public DownloadHelper(SyncSession session) {
        super();
        this.session = session;
    }

    /**
     * Performs a file download.
     *
     * @param path The directory path the file is located in
     * @param fileVersion The file version to download
     * @param offset The offset in bytes where to begin
     * @param length The length of the data in bytes to download
     * @return A file holder hosting the stream
     * @throws OXException
     */
    public IFileHolder perform(final String path, final FileVersion fileVersion, final long offset, final long length) throws OXException {

        return session.getStorage().wrapInTransaction(() -> {
            /*
             * get & verify the file version to download
             */
            File file = session.getStorage().getFileByName(path, fileVersion.getName(), true);
            if (null == file) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
            }
            FileChecksum checksum = ChecksumProvider.getChecksum(session, file);
            if (false == checksum.getChecksum().equals(fileVersion.getChecksum())) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
            }
            /*
             * re-validate checksum of .drive-meta files prior download
             */
            if (session.getDriveSession().useDriveMeta() && DriveMetadata.class.isInstance(file)) {
                String fileMD5Sum = ((DriveMetadata) file).getFileMD5Sum();
                if (false == fileVersion.getChecksum().equals(fileMD5Sum)) {
                    // stored checksum no longer valid, invalidate any affected checksums
                    session.trace("Checksum " + fileVersion.getChecksum() + " different from actual checksum " + fileMD5Sum +
                        " of .drive-meta file, invalidating stored checksums.");
                    session.getChecksumStore().removeFileChecksums(DriveUtils.getFileID(file));
                    session.getChecksumStore().removeDirectoryChecksum(new FolderID(file.getFolderId()));
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
                }
            }
            /*
             * get the file's input stream, wrap stream into file holder and return
             */
            InputStream inputStream = getInputStream(file, offset, length);
            if (null == inputStream) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(fileVersion.getName(), path);
            }
            long contentLength = 0 < offset || 0 < length && length != file.getFileSize() ? -1L : file.getFileSize();
            boolean throttled = 0 < session.getConfig().getMaxBandwidth() || 0 < session.getConfig().getMaxBandwidthPerClient();
            return new DriveFileHolder(session, inputStream, fileVersion.getName(), file.getFileMIMEType(), contentLength, throttled);
        });
    }

    private InputStream getInputStream(File file, long offset, long length) throws OXException {
        if (session.getDriveSession().useDriveMeta() && DriveMetadata.class.isInstance(file)) {
            /*
             * get .drive-meta data
             */
            return ((DriveMetadata) file).getDocument(offset, length);
        }
        IDBasedFileAccess fileAccess = session.getStorage().getFileAccess();
        InputStream inputStream = null;
        if (0 < offset || 0 < length) {
            /*
             * offset or maximum length is requested, get partial stream
             */
            if (session.getStorage().supports(new FileID(file.getId()), FileStorageCapability.RANDOM_FILE_ACCESS)) {
                inputStream = fileAccess.getDocument(file.getId(), file.getVersion(), offset, length);
            } else {
                try {
                    inputStream = new PartialInputStream(fileAccess.getDocument(file.getId(), file.getVersion()), offset, length);
                } catch (IOException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        } else {
            /*
             * get complete stream by default
             */
            inputStream = fileAccess.getDocument(file.getId(), file.getVersion());
        }
        return inputStream;
    }

}
