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

package com.openexchange.file.storage.json.actions.files;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InfostoreRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface InfostoreRequest {

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    String getParameter(final String name);

    /**
     * Gets the boolean value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The boolean value mapped to given parameter name or <code>false</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    boolean getBoolParameter(final String name);

    /**
     * Gets the request data if available.
     *
     * @return The request data or <code>null</code>
     */
    AJAXRequestData getRequestData();

    /**
     * Checks if specified mandatory parameters are present.
     *
     * @return This request instance
     * @throws OXException If any of the specified parameters is missing
     */
    InfostoreRequest require(AbstractFileAction.Param... params) throws OXException;

    /**
     * Checks if mandatory body is present.
     *
     * @return This request instance
     * @throws OXException If body is missing
     */
    InfostoreRequest requireBody() throws OXException;

    /**
     * Checks if mandatory file metadata is present.
     *
     * @return This request instance
     * @throws OXException If file metadata is missing
     */
    InfostoreRequest requireFileMetadata() throws OXException;

    /**
     * Requires the file access
     *
     * @return The file access
     * @throws OXException If file access cannot be returned
     */
    IDBasedFileAccess getFileAccess() throws OXException;

    /**
     * Optionally gets the file access
     *
     * @return The file access or <code>null</code>
     */
    IDBasedFileAccess optFileAccess();

    /**
     * Requires the folder access
     *
     * @return The folder access
     * @throws OXException If folder access cannot be returned
     */
    IDBasedFolderAccess getFolderAccess() throws OXException;

    /**
     * Optionally gets the folder access
     *
     * @return The folder access or <code>null</code>
     */
    IDBasedFolderAccess optFolderAccess();

    /**
     * Gets the file identifier
     *
     * @return The file identifier or <code>null</code>
     * @throws OXException If any error occurs while returning the file identifier
     */
    String getId() throws OXException;

    /**
     * Gets the version identifier or {@link FileStorageFileAccess#CURRENT_VERSION} if absent
     *
     * @return The version identifier or <code>FileStorageFileAccess.CURRENT_VERSION</code>
     * @throws OXException If any error occurs while returning the version identifier
     */
    String getVersion() throws OXException;

    /**
     * Gets the folder identifier or {@link FileStorageFileAccess#ALL_FOLDERS} if absent
     *
     * @return The folder identifier or <code>FileStorageFileAccess.ALL_FOLDERS</code>
     * @throws OXException If any error occurs while returning the folder identifier
     */
    String getFolderId() throws OXException;

    /**
     * Gets the metadata fields to load from the storage based on the requested column identifiers.
     *
     * @return The fields to load
     * @throws OXException If request specifies an unknown column
     */
    List<Field> getFieldsToLoad() throws OXException;

    /**
     * Gets the column identifiers as requested by the client.
     *
     * @return The column identifiers
     * @throws OXException If request specifies an unknown column
     */
    int[] getRequestedColumns() throws OXException;

    /**
     * Gets the field to sort by
     *
     * @return The sort field or <code>null</code>
     * @throws OXException If sort field is unknown
     */
    Field getSortingField() throws OXException;

    /**
     * Gets the sort direction or {@link SortDirection#ASC} if absent.
     *
     * @return The sort direction or <code>SortDirection.ASC</code>
     * @throws OXException If given sort direction parameter is unknown
     */
    SortDirection getSortingOrder() throws OXException;

    /**
     * Gets the time zone associated with this request.
     * <p>
     * Either parsed from <code>"timezone"</code> parameter or the time zone of the associated user
     *
     * @return The time zone associated with this request
     */
    TimeZone getTimezone();

    /**
     * Gets the associated session
     *
     * @return The session
     * @throws OXException If any error occurs while returning the session
     */
    ServerSession getSession() throws OXException;

    /**
     * Gets the specified time stamp or {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} if absent
     *
     * @return The specified time stamp or <code>FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER</code>
     * @throws OXException If any error occurs while returning the time stamp
     */
    long getTimestamp() throws OXException;

    /**
     * Gets the set of certain markers. Matching files are supposed to be ignored in the response. E.g. <code>"deleted"</code>.
     *
     * @return The set of markers or an empty set
     * @throws OXException If any error occurs while returning the set of markers
     */
    Set<String> getIgnore() throws OXException;

    /**
     * Gets the list of identifiers associated with this request.
     *
     * @return The identifiers
     * @throws OXException If this request does not specify an identifier list
     */
    List<String> getIds() throws OXException;

    /**
     * Gets the pairs of identifier and version.
     *
     * @return The pairs of identifier and version
     * @throws OXException If parse attempt fails
     */
    List<IdVersionPair> getIdVersionPairs() throws OXException;

    /**
     * Optionally gets the pairs of identifier and version.
     *
     * @return The pairs of identifier and version or <code>null</code>
     * @throws OXException If parse attempt fails
     */
    List<IdVersionPair> optIdVersionPairs() throws OXException;

    String getFolderForID(String id) throws OXException;

    String[] getVersions() throws OXException;

    long getDiff() throws OXException;

    String getSearchQuery() throws OXException;

    String getSearchFolderId() throws OXException;

    int getStart() throws OXException;

    int getEnd() throws OXException;

    File getFile() throws OXException;

    List<File.Field> getSentColumns() throws OXException;

    boolean hasUploads() throws OXException;

    InputStream getUploadedFileData() throws OXException;

    /**
     * Gets the upload stream. Retrieves the body of the request as binary data as an {@link InputStream}.
     *
     * @return The upload stream or <code>null</code> if not available
     * @throws OXException If an I/O error occurs
     */
    InputStream getUploadStream() throws OXException;

    int getAttachedId();

    int getModule();

    int getAttachment();

    AttachmentBase getAttachmentBase();

    /**
     * Gets the folder identifier at given position
     *
     * @param i The index position
     * @return The folder identifier or <code>null</code>
     */
    String getFolderAt(int i);

    List<String> getFolders();

    boolean isForSpecificVersion();

    boolean extendedResponse() throws OXException;

    public Document getCachedDocument();

    /**
     * Gets whether entities of object permissions shall be notified if they
     * were added to a new or existing file. This method is only useful for
     * create and update requests.
     *
     * @return <code>true</code> if notifications shall be sent
     * @throws OXException If parsing the request body fails
     */
    boolean notifyPermissionEntities() throws OXException;

    /**
     * Gets the transport for notification messages. This method is only useful for
     * create and update requests.
     *
     * @return The transport; <code>null</code> if {@link #notifyPermissionEntities()} returns <code>false</code>.
     * @throws OXException If parsing the request body fails
     */
    Transport getNotificationTransport() throws OXException;

    /**
     * Gets an optional user-defined notification message for permission entities.
     * This method is only useful for create and update requests.
     *
     * @return The message or <code>null</code>
     * @throws OXException If parsing the request body fails
     */
    String getNotifiactionMessage() throws OXException;

    /**
     * Gets a list of entity identifiers as supplied by the client in the <code>entities</code> request body field, as used for
     * notification purposes.
     *
     * @return The entity identifiers
     */
    List<Integer> getEntities() throws OXException;

}
