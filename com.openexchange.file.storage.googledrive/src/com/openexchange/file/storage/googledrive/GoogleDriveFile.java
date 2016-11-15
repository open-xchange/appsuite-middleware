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

package com.openexchange.file.storage.googledrive;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Revision;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.googledrive.osgi.Services;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link GoogleDriveFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveFile extends DefaultFile {

    /**
     * Initializes a new {@link GoogleDriveFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @param rootFolderId The identifier of the root folder in the account
     */
    public GoogleDriveFile(String folderId, String id, int userId, String rootFolderId) {
        super();
        setFolderId(isRootFolder(folderId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : folderId);
        setCreatedBy(userId);
        setModifiedBy(userId);
        setId(id);
        setFileName(id);
        setVersion(FileStorageFileAccess.CURRENT_VERSION);
        setIsCurrentVersion(true);
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "root".equals(id) || rootFolderId.equals(id);
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Google Drive file.
     *
     * @param file The Google Drive file
     * @throws OXException If parsing Google Drive file fails
     * @return This Google Drive file
     */
    public GoogleDriveFile parseGoogleDriveFile(File file) throws OXException {
        return parseGoogleDriveFile(file, null);
    }

    /**
     * Parses specified Google Drive file.
     *
     * @param file The Google Drive file
     * @param fields The fields to consider
     * @throws OXException If parsing Google Drive file fails
     * @return This Google Drive file with property set applied
     */
    public GoogleDriveFile parseGoogleDriveFile(File file, List<Field> fields) throws OXException {
        if (null != file) {
            try {
                final String name = file.getTitle();
                setTitle(name);
                setFileName(name);

                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);
                if (set.contains(Field.CREATED)) {
                    DateTime createdDate = file.getCreatedDate();
                    if (null != createdDate) {
                        setCreated(new Date(createdDate.getValue()));
                    }
                }
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    DateTime modifiedDate = file.getModifiedDate();
                    if (null != modifiedDate) {
                        setLastModified(new Date(modifiedDate.getValue()));
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    String contentType = file.getMimeType();
                    if (com.openexchange.java.Strings.isEmpty(contentType)) {
                        final MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        contentType = map.getContentType(name);
                    }
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    Long fileSize = file.getFileSize();
                    if (null != fileSize) {
                        setFileSize(fileSize.longValue());
                    }
                }
                if (set.contains(Field.URL)) {
                    setURL(file.getWebContentLink());
                }
                if (set.contains(Field.COLOR_LABEL)) {
                    setColorLabel(0);
                }
                if (set.contains(Field.CATEGORIES)) {
                    setCategories(null);
                }
                if (set.contains(Field.DESCRIPTION)) {
                    setDescription(file.getDescription());
                }
                if (set.contains(Field.VERSION_COMMENT)) {
                    setVersionComment(null);
                }
                if (set.contains(Field.FILE_MD5SUM)) {
                    setFileMD5Sum(file.getMd5Checksum());
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    /**
     * Parses specified Google Drive revision.
     *
     * @param revision The Google Drive revision
     * @param name The file name/title to set
     * @param fields The fields to consider
     * @throws OXException If parsing Google Drive file fails
     * @return This Google Drive file with property set applied
     */
    public GoogleDriveFile parseRevision(Revision revision, String name, List<Field> fields) throws OXException {
        if (null != revision) {
            try {
                setVersion(revision.getId());
                String n = null == name ? revision.getOriginalFilename() : name;
                setTitle(n);
                setFileName(n);

                Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    DateTime modifiedDate = revision.getModifiedDate();
                    if (null != modifiedDate) {
                        setLastModified(new Date(modifiedDate.getValue()));
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    String contentType = revision.getMimeType();
                    if (com.openexchange.java.Strings.isEmpty(contentType)) {
                        final MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        contentType = map.getContentType(n);
                    }
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    Long fileSize = revision.getFileSize();
                    if (null != fileSize) {
                        setFileSize(fileSize.longValue());
                    }
                }
                if (set.contains(Field.URL)) {
                    setURL(revision.getDownloadUrl());
                }
                if (set.contains(Field.FILE_MD5SUM)) {
                    setFileMD5Sum(revision.getMd5Checksum());
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }
}
