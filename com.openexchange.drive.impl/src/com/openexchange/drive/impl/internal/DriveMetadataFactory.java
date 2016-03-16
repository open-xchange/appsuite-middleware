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
