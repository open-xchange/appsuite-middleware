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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;

/**
 * {@link DriveMetadataFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveMetadataFactory {

    /**
     * Gets file metadata for the supplied file version.
     *
     * @param session The sync session
     * @param path The path, relative to the sync session's root folder, where the file version is located
     * @param fileVersion The file version to get the metadata for
     * @param fields The file metadata fields to fill in the result
     * @return The metadata
     * @throws OXException
     */
    public static DriveFileMetadata getFileMetadata(SyncSession session, String path, FileVersion fileVersion, List<DriveFileField> fields) throws OXException {
        return getFileMetadata(session, ServerFileVersion.valueOf(fileVersion, path, session), fields);
    }

    /**
     * Gets file metadata for the supplied file version.
     *
     * @param session The sync session
     * @param fileVersion The file version to get the metadata for
     * @param fields The file metadata fields to fill in the result
     * @return The metadata
     */
    public static DriveFileMetadata getFileMetadata(SyncSession session, ServerFileVersion fileVersion, List<DriveFileField> fields) {
        DriveFileMetadata fileMetadata = new DriveFileMetadata();
        fileMetadata.setChecksum(fileVersion.getChecksum());
        fileMetadata.setFileName(fileVersion.getName());
        File file = fileVersion.getFile();
        if (null != file) {
            if (null == fields || fields.contains(DriveFileField.CREATED)) {
                fileMetadata.setCreated(file.getCreated());
            }
            if (null == fields || fields.contains(DriveFileField.MODIFIED)) {
                fileMetadata.setModified(file.getCreated());
            }
            if (null == fields || fields.contains(DriveFileField.CONTENT_TYPE)) {
                fileMetadata.setMimeType(file.getFileMIMEType());
            }
            if (null == fields || fields.contains(DriveFileField.DIRECT_LINK)) {
                fileMetadata.setDirectLink(session.getLinkGenerator().getFileLink(file));
            }
            if (null == fields || fields.contains(DriveFileField.DIRECT_LINK_FRAGMENTS)) {
                fileMetadata.setDirectLinkFragments(session.getLinkGenerator().getFileLinkFragments(file));
            }
            if (null == fields || fields.contains(DriveFileField.PREVIEW_LINK)) {
                fileMetadata.setPreviewLink(session.getLinkGenerator().getFilePreviewLink(file));
            }
        }

        return fileMetadata;
    }

    /**
     * Gets file metadata for the supplied file versions.
     *
     * @param session The sync session
     * @param fileVersions The file version to get the metadata for
     * @param fields The file metadata fields to fill in the result
     * @return The metadata
     */
    public static List<DriveFileMetadata> getFileMetadata(SyncSession session, List<ServerFileVersion> fileVersions, List<DriveFileField> fields) {
        List<DriveFileMetadata> metadata = new ArrayList<DriveFileMetadata>(fileVersions.size());
        for (ServerFileVersion fileVersion : fileVersions) {
            metadata.add(getFileMetadata(session, fileVersion, fields));
        }
        return metadata;
    }

}
