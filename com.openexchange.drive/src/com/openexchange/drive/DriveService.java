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

package com.openexchange.drive;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.capabilities.Capability;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Quota;


/**
 * {@link DriveService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveService {

    /**
     * The "drive" capability constant.
     */
    static final Capability CAPABILITY_DRIVE = new Capability("drive");

    /**
     * Synchronizes the folder hierarchy.
     *
     * @param session The session
     * @param originalVersions A list of directory versions previously known by the client
     * @param clientVersions The current list of client directory versions
     * @return A list of resulting actions to execute on the client afterwards
     * @throws OXException
     */
    SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions, List<DirectoryVersion> clientVersions) throws OXException;

    /**
     * Synchronizes the files in a folder.
     *
     * @param session The session
     * @param path The path to the synchronized folder, relative to the root folder
     * @param originalVersions A list of file versions previously known by the client
     * @param clientVersions The current list of client file versions
     * @return A list of resulting actions to execute on the client afterwards
     * @throws OXException
     */
    SyncResult<FileVersion> syncFiles(DriveSession session, String path, List<FileVersion> originalVersions, List<FileVersion> clientVersions) throws OXException;

    /**
     * Processes a file upload to the server.
     *
     * @param session The session
     * @param path The path to the target folder, relative to the root folder
     * @param uploadStream An input stream for the file data (not closed by the service)
     * @param originalVersion The original file version to be updated, or <code>null</code> for new file uploads
     * @param contentType The file's content type
     * @param file The target file version
     * @param offset The start offset in bytes for the upload when resuming, or <code>0</code> when initially starting an upload
     * @param totalLength The total expected length of the file (required to support resume of uploads), or <code>-1</code> if unknown
     * @param created The time the file was created, or <code>null</code> if not set
     * @param modified The time the file was modified, or <code>null</code> if not set
     * @return A list of resulting file actions to execute on the client afterwards
     * @throws OXException
     */
    SyncResult<FileVersion> upload(DriveSession session, String path, InputStream uploadStream,
        FileVersion originalVersion, FileVersion newVersion, String contentType, long offset, long totalLength, Date created, Date modified) throws OXException;

    /**
     * Processes a file download from the server.
     *
     * @param session The session
     * @param path The path to the file's parent folder, relative to the root folder
     * @param fileVersion The file version to download
     * @param offset The start offset in bytes for the download, or <code>0</code> when initially starting
     * @param length The number of bytes to include in the stream, or <code>-1</code> to read the stream until the end
     * @return The binary content wrapped into a file holder
     * @throws OXException
     */
    IFileHolder download(DriveSession session, String path, FileVersion fileVersion, long offset, long length) throws OXException;

    /**
     * Gets the quota limits and current usage for the storage the supplied root folder belongs to. This includes both restrictions on
     * the number of allowed files and the size of the files in bytes. If there's no limit, {@link Quota#UNLIMITED} is returned.
     *
     * @param session The session
     * @return The quota
     * @throws OXException
     */
    DriveQuota getQuota(DriveSession session) throws OXException;

    /**
     * Gets file metadata for the supplied file versions
     *
     * @param session The session
     * @param path The path to the file's parent folder, relative to the root folder
     * @param fileVersions A list of file versions to get the metadata for, or <code>null</code> to get metadata for all files in the
     *        denoted directory
     * @param fields The requested metadata fields, or <code>null</code> to get all available fields
     * @return The available file metadata
     * @throws OXException
     */
    List<DriveFileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions, List<DriveFileField> fields) throws OXException;

    /**
     * Gets directory metadata for the supplied directory
     *
     * @param session The session
     * @param path The path to the folder, relative to the root folder
     * @return The directory metadata
     * @throws OXException
     */
    DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException;

    /**
     * Gets the configured drive settings based on the supplied session.
     *
     * @return The drive settings
     * @throws OXException
     */
    DriveSettings getSettings(DriveSession session) throws OXException;

    /**
     * Creates an URL for the given file or directory used to jump into it directly.
     *
     * @param session The session
     * @param path The path to the folder, relative to the root folder
     * @param fileName The filename, or <code>null</code>
     * @param method The method
     * @return The URL to redirect to
     * @throws OXException
     */
    String getJumpRedirectUrl(DriveSession session, String path, String fileName, String method) throws OXException;

    /**
     * Gets a reference providing additional utility methods.
     *
     * @return The drive utilities
     * @throws OXException
     */
    DriveUtility getUtility();

}
