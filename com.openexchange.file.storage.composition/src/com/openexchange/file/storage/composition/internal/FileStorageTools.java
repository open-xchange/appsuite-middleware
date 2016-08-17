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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper.EventProperty;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageExtendedMetadata;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link FileStorageTools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileStorageTools {

    /**
     * Gets a value indicating whether a specific account supports a specific capability.
     *
     * @param fileAccess The file access reference to check the capability for
     * @param capability The capability to check
     * @return <code>true</code> if the capability is supported, <code>false</code>, otherwise
     */
    public static boolean supports(FileStorageFileAccess fileAccess, FileStorageCapability capability) {
        return FileStorageCapabilityTools.supports(fileAccess, capability);
    }

    /**
     * Gets a value indicating whether a specific account supports storing a specific file meta-data field.
     *
     * @param fileAccess The file access reference to check the capability for
     * @param field The field to check
     * @return <code>true</code> if extended meta-data and the field itself is supported, <code>false</code>, otherwise
     */
    public static boolean supports(FileStorageFileAccess fileAccess, File.Field field) {
        if (supports(fileAccess, FileStorageCapability.EXTENDED_METADATA)) {
            List<Field> supportedFields = ((FileStorageExtendedMetadata) fileAccess).getSupportedFields();
            return null != supportedFields && supportedFields.contains(field);
        }
        return false;
    }

    /**
     * Adds the fields {@link Field#ID}, {@link Field#FOLDER_ID} and {@link Field#LAST_MODIFIED} to the field list if not already
     * contained.
     *
     * @param columns The fields to add the ID colums to
     * @return A new list holding the fields as well as the field columns, or the list itself if all already contained
     */
    public static List<File.Field> addIDColumns(List<File.Field> columns) {
        final boolean hasID = columns.contains(File.Field.ID);
        final boolean hasFolder = columns.contains(File.Field.FOLDER_ID);
        final boolean hasLastModified = columns.contains(File.Field.LAST_MODIFIED);

        if (hasID && hasFolder && hasLastModified) {
            return columns;
        }

        List<File.Field> cols = new ArrayList<File.Field>(columns);

        if (!hasID) {
            cols.add(File.Field.ID);
        }

        if (!hasFolder) {
            cols.add(File.Field.FOLDER_ID);
        }

        if (!hasLastModified) {
            cols.add(File.Field.LAST_MODIFIED);
        }

        return cols;
    }

    /**
     * Checks that length of the supplied search pattern is allowed according to the <code>com.openexchange.MinimumSearchCharacters</code>
     * configuration property, throwing an appropriate exception if validation fails.
     *
     * @param pattern The pattern to check
     * @throws OXException If validation fails
     */
    public static void checkPatternLength(final String pattern) throws OXException {
        final ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        final int minimumSearchCharacters = null == configurationService ? 0 : configurationService.getIntProperty("com.openexchange.MinimumSearchCharacters", 0);
        if (minimumSearchCharacters <= 0) {
            return;
        }
        if (null != pattern && 0 != pattern.length() && com.openexchange.java.SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw FileStorageExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.create(I(minimumSearchCharacters));
        }
    }

    /**
     * Processes the list of supplied ID tuples to ensure that each entry has an assigned folder ID.
     *
     * @param access The file access to query if folder IDs are missing
     * @param idTuples The ID tuples to process
     * @return The ID tuples, with each entry holding its full file- and folder-ID information
     * @throws OXException
     */
    //TODO: This is weird. The client already sends fileID:folderID pairs, though they get stripped for the infostore currently
    //      when generating the corresponding com.openexchange.file.storage.composition.FileID.
    public static List<IDTuple> ensureFolderIDs(FileStorageFileAccess access, List<IDTuple> idTuples) throws OXException {
        if (null == idTuples || 0 == idTuples.size()) {
            return idTuples;
        }
        List<IDTuple> incompleteTuples = new ArrayList<FileStorageFileAccess.IDTuple>();
        for (IDTuple tuple : idTuples) {
            if (null == tuple.getFolder()) {
                incompleteTuples.add(tuple);
            }
        }
        if (0 < incompleteTuples.size()) {
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = access.getDocuments(incompleteTuples, Arrays.asList(Field.ID, Field.FOLDER_ID)).results();
                for (int i = 0; i < incompleteTuples.size() && searchIterator.hasNext(); i++) {
                    File file = searchIterator.next();
                    incompleteTuples.get(i).setFolder(file.getFolderId());
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return idTuples;
    }

    /**
     * Creates an event property holding the current request's remote address by utilizing the thread-local log properties.
     *
     * @return An <code>remoteAddress</code> event property, or <code>null</code> if not available
     */
    public static EventProperty extractRemoteAddress() {
        Object serverName = LogProperties.get(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS);
        if (null != serverName) {
            return new EventProperty("remoteAddress", serverName.toString());
        }
        return null;
    }

    /**
     * Creates an array of folder identifiers for the supplied file storage folder path array.
     *
     * @param path The folders to get the path for
     * @param serviceID The service identifier
     * @param accountID The account identifier
     * @return The path of folder identifiers
     */
    public static FolderID[] getPath(FileStorageFolder[] path, String serviceID, String accountID) {
        if (null == path) {
            return null;
        }
        FolderID[] folderIDs = new FolderID[path.length];
        for (int i = 0; i < path.length; i++) {
            folderIDs[i] = new FolderID(serviceID, accountID, path[i].getId());
        }
        return folderIDs;
    }

    /**
     * Builds a dictionary to be used with common file storage folder events.
     *
     * @param session The session
     * @param folderID The identifier of the folder to get the event properties for
     * @param path The folder path to include
     * @return The event properties
     */
    public static Dictionary<String, Object> getEventProperties(Session session, FolderID folderID, FileStorageFolder[] path) {
        return getEventProperties(session, folderID, getPath(path, folderID.getService(), folderID.getAccountId()));
    }

    /**
     * Builds a dictionary to be used with common file storage folder events.
     *
     * @param session The session
     * @param folderID The identifier of the folder to get the event properties for
     * @param path The folder path to include
     * @return The event properties
     */
    public static Dictionary<String, Object> getEventProperties(Session session, FolderID folderID, FolderID[] path) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FileStorageEventConstants.SESSION, session);
        properties.put(FileStorageEventConstants.ACCOUNT_ID, folderID.getAccountId());
        properties.put(FileStorageEventConstants.SERVICE, folderID.getService());
        properties.put(FileStorageEventConstants.FOLDER_ID, folderID.toUniqueID());
        if (null != path) {
            String[] parentFolderIDs = new String[path.length];
            for (int i = 0; i < path.length; i++) {
                parentFolderIDs[i] = path[i].toUniqueID();
            }
            properties.put(FileStorageEventConstants.FOLDER_PATH, parentFolderIDs);
        }
        return properties;
    }

    /**
     * Gets a readable path string containing all folder names separated by the path separator character <code>/</code>.
     *
     * @param path The file storage folders on the path in reverse order, i.e. the root folder is the last one
     * @param additionalFolders Additional folders to append at the end of the path
     * @return The path string
     */
    public static String getPathString(FileStorageFolder[] path, FileStorageFolder...additionalFolders) {
        if ((null == path || 0 == path.length) && (null == additionalFolders || 0 == additionalFolders.length)) {
            return "/";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (null != path) {
            for (int i = path.length - 1; i >= 0; i--) {
                stringBuilder.append('/').append(path[i].getName());
            }
        }
        if (null != additionalFolders) {
            for (int i = 0; i < additionalFolders.length; i++) {
                stringBuilder.append('/').append(additionalFolders[i].getName());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a value indicating whether the supplied folder contains permissions for entities other than the supplied current user.
     *
     * @param userID The entity identifier of the user that should be considered as "not" foreign
     * @param folder The folder to check
     * @return <code>true</code> if foreign permissions were found, <code>false</code>, otherwise
     */
    public static boolean containsForeignPermissions(int userID, FileStorageFolder folder) {
        List<FileStoragePermission> permissions = folder.getPermissions();
        if (null != permissions && 0 < permissions.size()) {
            for (FileStoragePermission permission : permissions) {
                if (permission.getEntity() != userID) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the display name for a specific file storage account.
     *
     * @param compositingAccess a reference to the compositing access
     * @param serviceID The service identifier to get the account name for
     * @param accountID The account identifier to get the account name for
     * @return The account name
     */
    public static String getAccountName(AbstractCompositingIDBasedAccess compositingAccess, String serviceID, String accountID) throws OXException {
        return compositingAccess.getAccountAccess(serviceID, accountID).getService().getAccountManager()
            .getAccount(accountID, compositingAccess.getSession()).getDisplayName();
    }

    /**
     * Gets the display name for the file storage account of a specific folder.
     *
     * @param compositingAccess A reference to the compositing access
     * @param folderID The identifier of the folder to get the account name for
     * @return The account name
     */
    public static String getAccountName(AbstractCompositingIDBasedAccess compositingAccess, FolderID folderID) throws OXException {
        return getAccountName(compositingAccess, folderID.getService(), folderID.getAccountId());
    }

    /**
     * Gets the display name for the file storage account of a specific file.
     *
     * @param compositingAccess A reference to the compositing access
     * @param fileID The identifier of the file to get the account name for
     * @return The account name
     */
    public static String getAccountName(AbstractCompositingIDBasedAccess compositingAccess, FileID fileID) throws OXException {
        return getAccountName(compositingAccess, fileID.getService(), fileID.getAccountId());
    }

    /**
     * Gets the display name for the file storage account of a specific storage service.
     *
     * @param compositingAccess A reference to the compositing access
     * @param compositingAccess A reference to the compositing access
     * @return The account name
     */
    public static String getAccountName(AbstractCompositingIDBasedAccess compositingAccess, FileStorageFileAccess fileAccess) throws OXException {
        String accountID = fileAccess.getAccountAccess().getAccountId();
        String serviceID = fileAccess.getAccountAccess().getService().getId();
        return getAccountName(compositingAccess, serviceID, accountID);
    }

}
