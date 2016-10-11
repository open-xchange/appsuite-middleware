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

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.LinkUpdate;

/**
 * {@link DriveUtility}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveUtility {

    /**
     * Gets a value indicating whether the supplied path is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param path The path to check
     * @return <code>true</code> if the path is considered invalid, <code>false</code>, otherwise
     */
    boolean isInvalidPath(String path) throws OXException;

    /**
     * Gets a value indicating whether the supplied filename is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered invalid, <code>false</code>, otherwise
     */
    boolean isInvalidFileName(String fileName);

    /**
     * Gets a value indicating whether the supplied filename is ignored, i.e. it is excluded from synchronization by definition. Only
     * static / global exclusions are considered in this check.
     *
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered to be ignored, <code>false</code>, otherwise
     */
    boolean isIgnoredFileName(String fileName);

    /**
     * Gets a value indicating whether the supplied filename is ignored, i.e. it is excluded from synchronization by definition. Static /
     * global exclusions are considered, as well as client-defined filters based on path and filename.
     *
     * @param session The drive session
     * @param path The directory path, relative to the root directory
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered to be ignored, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean isIgnoredFileName(DriveSession session, String path, String fileName) throws OXException;

    /**
     * Gets a value indicating whether the session belongs to a known drive synchronization client or not.
     *
     * @param session The session to check
     * @return <code>true</code> if the session belongs to a known drive client, <code>false</code>, otherwise
     */
    boolean isDriveSession(Session session);

    /**
     * Gets metadata for all (direct) subfolders of the drive session's root folder. The JSON format of the metadata is the same as used
     * inside the <code>.drive-meta</code> files, yet without the metadata for contained files.
     *
     * @param session The drive session, holding the root folder identifier to get the subfolders for
     * @return The metadata of the subfolders as a list of JSON objects, or an empty list if no subfolders were found
     */
    List<JSONObject> getSubfolderMetadata(DriveSession session) throws OXException;

    /**
     * Gets metadata for all files and directories that are considered as shared by the user. The JSON format of the metadata is the same
     * as used inside the <code>.drive-meta</code> files.
     *
     * @param session The drive session
     * @return The metadata of the shared items as a JSON object holding two JSON arrays <code>files</code> and <code>directories</code>
     *         containing the metadata
     */
    JSONObject getSharesMetadata(DriveSession session) throws OXException;

    /**
     * Gets metadata for a specific file. The JSON format of the metadata is the same as used inside the <code>.drive-meta</code> files.
     *
     * @param session The drive session
     * @param path The path to the file's parent folder, relative to the root folder
     * @param fileVersion The file version of the file to get
     * @return The metadata of the requested file as a JSON object
     */
    JSONObject getFileMetadata(DriveSession session, String path, FileVersion fileVersion) throws OXException;

    /**
     * Gets metadata for a specific directory. The JSON format of the metadata is the same as used inside the <code>.drive-meta</code> files.
     *
     * @param session The drive session
     * @param directoryVersion The directory version of the directory to get
     * @return The metadata of the requested directory as a JSON object
     */
    JSONObject getDirectoryMetadata(DriveSession session, DirectoryVersion directoryVersion) throws OXException;

    /**
     * Updates metadata of a file. This currently only includes adjusting the file's object permissions.
     *
     * @param session The session
     * @param path The path to the file's parent folder, relative to the root folder
     * @param fileVersion The file version of the file to update
     * @param jsonObject The updated metadata
     * @param parameters Additional notification parameters for the update
     */
    void updateFile(DriveSession session, String path, FileVersion fileVersion, JSONObject jsonObject, NotificationParameters parameters) throws OXException;

    /**
     * Updates metadata of a directory. This currently only includes adjusting the directory's permissions.
     *
     * @param session The session
     * @param directoryVersion The directory version of the directory to update
     * @param jsonObject The updated metadata
     * @param parameters Additional notification parameters for the update
     */
    void updateDirectory(DriveSession session, DirectoryVersion directoryVersion, JSONObject jsonObject, NotificationParameters parameters) throws OXException;

    /**
     * Moves and/or renames a file version.
     *
     * @param session The session
     * @param path The path to the original file
     * @param fileVersion The original file version
     * @param newPath The new target path, or <code>null</code> to rename only
     * @param newName The new filename, or <code>null</code> to move only
     */
    void moveFile(DriveSession session, String path, FileVersion fileVersion, String newPath, String newName) throws OXException;

    /**
     * Moves and/or renames a directory version.
     *
     * @param session The session
     * @param directoryVersion The original directory version
     * @param newPath The new target path
     */
    void moveDirectory(DriveSession session, DirectoryVersion directoryVersion, String newPath) throws OXException;

    /**
     * Gets an existing or creates a new share link for a specific file or folder.
     *
     * @param session The session
     * @param target The target
     * @return The share link
     */
    DriveShareLink getLink(DriveSession session, DriveShareTarget target) throws OXException;

    /**
     * Optionally gets the share link for a specific file or folder if one exists.
     *
     * @param session The session
     * @param target The target
     * @return The share link, or <code>null</code> if no link exists
     */
    DriveShareLink optLink(DriveSession session, DriveShareTarget target) throws OXException;

    /**
     * Updates an existing share link for a specific file or folder.
     *
     * @param session The session
     * @param target The target
     * @param linkUpdate The link update
     * @return The updated share link
     */
    DriveShareLink updateLink(DriveSession session, DriveShareTarget target, LinkUpdate linkUpdate) throws OXException;

    /**
     * Deletes an existing share link for a specific file or folder.
     *
     * @param session The session
     * @param target The target
     */
    void deleteLink(DriveSession session, DriveShareTarget target) throws OXException;

    /**
     * (Re-)Sends a notification message tom one or more existing entities of a shared file or folder.
     *
     * @param session The session
     * @param target The target
     * @param entityIDs The user- or group identifiers to notify
     * @param parameters The notification parameters
     */
    void notify(DriveSession session, DriveShareTarget target, int[] entityIDs, NotificationParameters parameters) throws OXException;

    /**
     * Searches for contacts, groups and users, driving the typical "auto-completion" scenario in clients when inviting guests.
     *
     * @param session The session
     * @param query The query
     * @param parameters Additional auto-complete specific parameters
     * @return A JSON array holding the results, ready to be consumed by clients
     */
    JSONArray autocomplete(DriveSession session, String query, Map<String, Object> parameters) throws OXException;

    /**
     * Gets statistics about the trash folder contents.
     *
     * @param session The session
     * @return The statistics, or <code>null</code> if no trash folder available
     */
    FolderStats getTrashFolderStats(DriveSession session) throws OXException;

    /**
     * Empties the trash folder.
     *
     * @param session The session
     * @return Updated statistics of the trash folder after emptying, or <code>null</code> if no trash folder available
     */
    FolderStats emptyTrash(DriveSession session) throws OXException;

}
