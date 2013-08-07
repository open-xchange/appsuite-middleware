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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionAware;

/**
 * {@link InfostoreFacade} - Access to infostore documents.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Some JavaDoc
 */
public interface InfostoreFacade extends TransactionAware {

    /** Special Version used if you want to retrieve the latest version of an infostore document */
    public static int CURRENT_VERSION = -1;

    /** The identifier marking a new infostore document. */
    public static int NEW = -1;

    /** Ascending sort order */
    public static final int ASC = 1;

    /** Descending sort order */
    public static final int DESC = -1;

    /**
     * Checks if denoted document exists.
     *
     * @param id The identifier
     * @param version The version
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If checking for existence fails
     * @see #CURRENT_VERSION
     */
    public boolean exists(int id, int version, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the denoted document's meta data information.
     *
     * @param id The identifier
     * @param version The version
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The meta data
     * @throws OXException If operation fails
     * @see #CURRENT_VERSION
     */
    public DocumentMetadata getDocumentMetadata(int id, int version, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Saves given document meta data.
     * <p>
     * <b>Note</b>: No <tt>modifiedColumns</tt> means all columns.
     *
     * @param document The meta data of the document
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param session The session
     * @throws OXException If save operation fails
     */
    public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, ServerSession session) throws OXException;

    /**
     * Saves given document meta data
     *
     * @param document The meta data of the document
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param session The session
     * @throws OXException If save operation fails
     */
    public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException;

    /**
     * Gets the document's binary content.
     *
     * @param id The identifier
     * @param version The version
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The document's binary content
     * @throws OXException If retrieving binary content fails
     * @see #CURRENT_VERSION
     */
    public InputStream getDocument(int id, int version, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param session The session
     * @throws OXException If save operation fails
     */
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession session) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param session The session
     * @throws OXException If save operation fails
     */
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException;

    /**
     * Saves given document meta data and binary content (if not <code>null</code>).
     *
     * @param document The document meta data
     * @param data The optional binary content or <code>null</code>
     * @param sequenceNumber The sequence number; e.g. client most recent time stamp
     * @param modifiedColumns The columns to modify
     * @param ignoreVersion Whether the version shall <b>NOT</b> be updated
     * @param session The session
     * @throws OXException If save operation fails
     */
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, ServerSession session) throws OXException;

    /**
     * Removes all documents contained in specified folder.
     *
     * @param folderId The identifier of the folder to clear
     * @param date The client's time stamp
     * @param session The session
     * @throws OXException If remove operation fails
     */
    public void removeDocument(long folderId, long date, ServerSession session) throws OXException;

    /**
     * Removes denoted documents.
     *
     * @param ids The identifiers of the documents to remove
     * @param date The client's time stamp
     * @param session The session
     * @return The identifiers of those documents that could <b>not</b> be deleted successfully
     * @throws OXException If remove operation fails
     */
    public int[] removeDocument(int ids[], long date, ServerSession session) throws OXException;

    /**
     * Removes denoted versions.
     *
     * @param id The document identifier
     * @param versionIds The identifiers of the versions to remove
     * @param session The session
     * @return The identifiers of those versions that could <b>not</b> be deleted successfully
     * @throws OXException If remove operation fails
     */
    public int[] removeVersion(int id, int[] versionIds, ServerSession session) throws OXException;

    /**
     * Gets the folder's documents.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the folder's documents.
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the sorted folder's documents.
     *
     * @param folderId The folder identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The folder's documents
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The document's version
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getVersions(int id, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param columns The columns to set in returned documents
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The document's versions
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the document's versions.
     *
     * @param id The document identifier
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The document's versions
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the specified documents.
     *
     * @param ids The identifiers
     * @param columns The columns to set in returned documents
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The documents
     * @throws OXException If retrieval fails
     */
    public TimedResult<DocumentMetadata> getDocuments(int[] ids, Metadata[] columns, Context ctx, User user, UserPermissionBits userPermissons) throws IllegalAccessException, OXException;

    /**
     * Gets the folder's updated & deleted documents.
     *
     * @param folderId The folder identifier
     * @param updateSince The time stamp to consider
     * @param columns The columns to set in returned documents
     * @param ignoreDeleted Whether to ignore deleted ones
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The matching changed/deleted documents
     * @throws OXException If retrieval fails
     */
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the folder's updated & deleted documents.
     *
     * @param folderId The folder identifier
     * @param updateSince The time stamp to consider
     * @param columns The columns to set in returned documents
     * @param sort The sort-by field
     * @param order The order; see {@link #ASC} or {@link #DESC}
     * @param ignoreDeleted Whether to ignore deleted ones
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The matching changed/deleted documents
     * @throws OXException If retrieval fails
     */
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the sequence numbers for the contents of the supplied folders to quickly determine which folders contain changes. An updated
     * sequence number in a folder indicates a change, for example a new, modified or deleted file.
     *
     * @param folderIds A list of folder IDs to get the sequence numbers for
     * @param versionsOnly <code>true</code> to only take documents with at least one version into account, <code>false</code>, otherwise
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return A map holding the resulting sequence numbers to each requested folder ID
     * @throws OXException
     */
    Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Gets the number of documents in given folder.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return The number of documents
     * @throws OXException If operation fails
     */
    public int countDocuments(long folderId, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Signals if denoted folder contains documents not owned by specified user.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return <code>true</code> if folder contains documents not owned by specified user; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public boolean hasFolderForeignObjects(long folderId, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Checks if denoted folder is empty.
     *
     * @param folderId The folder identifier
     * @param ctx The context
     * @return <code>true</code> if empty; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public boolean isFolderEmpty(long folderId, Context ctx) throws OXException;

    /**
     * Performs necessary clean-up operations if specified user has been deleted.
     *
     * @param userId The user identifier
     * @param context The context
     * @param session The session
     * @throws OXException If clean-up fails
     */
    public void removeUser(int userId, Context context, ServerSession session) throws OXException;

    /**
     * Unlocks specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    public void unlock(int id, ServerSession session) throws OXException;

    /**
     * Locks specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    public void lock(int id, long diff, ServerSession session) throws OXException;

    /**
     * Touches specified document.
     *
     * @param id The document identifier
     * @param session The session
     * @throws OXException If operation fails
     */
    public void touch(int id, ServerSession session) throws OXException;

    /**
     * Sets this facade's session holder.
     *
     * @param sessionHolder The session holder
     */
    public void setSessionHolder(SessionHolder sessionHolder);

    /**
     * Gets the quota restrictions and current usage of {@link Type#FILE} for the supplied session.
     *
     * @param session The session
     * @return The quota of {@link Type#FILE}, or quota with {@link Quota#UNLIMITED} limit if not set
     * @throws OXException
     */
    Quota getFileQuota(ServerSession session) throws OXException;

    /**
     * Gets the quota restrictions and current usage of {@link Type#STORAGE} for the supplied session.
     *
     * @param session The session
     * @return The quota of {@link Type#STORAGE}, or quota with {@link Quota#UNLIMITED} limit if not set
     * @throws OXException
     */
    Quota getStorageQuota(ServerSession session) throws OXException;

    /**
     * Loads (part of) a document's content.
     *
     * @param id The ID of the document
     * @param version The version of the document. Pass {@link FileStorageFileAccess#CURRENT_VERSION} for the current version.
     * @param offset The start offset in bytes to read from the document, or <code>0</code> to start from the beginning
     * @param length The number of bytes to read from the document, or <code>-1</code> to read the stream until the end
     * @param ctx The context
     * @param user The user
     * @param userPermissions The user permissions
     * @return An input stream for the content
     * @throws OXException
     */
    InputStream getDocument(int id, int version, long offset, long length, Context ctx, User user, UserPermissionBits userPermissons) throws OXException;

    /**
     * Save file metadata and content. Since the actual version is modified, the version number is not increased.
     *
     * @param document The metadata to save
     * @param data The binary content
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass DISTANT_FUTURE to circumvent the check
     * @param modifiedColumns The fields to save. All other fields will be ignored
     * @param offset The start offset in bytes where to append the data to the document, must be equal to the actual document's length
     * @param session The session
     * @throws OXException If operation fails
     */
    void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, ServerSession session) throws OXException;
}
