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

package com.openexchange.groupware.infostore.database.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Tools {

    private static final Pattern IS_NUMBERED_WITH_EXTENSION = Pattern.compile("\\(\\d+\\)\\.");
    private static final Pattern IS_NUMBERED = Pattern.compile("\\(\\d+\\)$");

    /**
     * Creates a string containing a placeholder for a possible enhancement counter for each of the supplied filenames. Those strings
     * are meant to be used in SQL <code>LIKE</code> statements to detect conflicting filenames.
     *
     * @param fileNames The filenames to generate the wildcard strings for
     * @return The wildcard strings
     */
    public static Set<String> getEnhancedWildcards(Set<String> fileNames) {
        Set<String> possibleWildcards = new HashSet<String>(fileNames.size());
        for (String filename : fileNames) {
            if (false == Strings.isEmpty(filename)) {
                StringBuilder stringBuilder = new StringBuilder(filename);
                Matcher matcher = IS_NUMBERED_WITH_EXTENSION.matcher(filename);
                if (matcher.find()) {
                    stringBuilder.replace(matcher.start(), matcher.end() - 1, "(%)");
                    possibleWildcards.add(stringBuilder.toString());
                    continue;
                }
                matcher = IS_NUMBERED.matcher(filename);
                if (matcher.find()) {
                    stringBuilder.replace(matcher.start(), matcher.end(), "(%)");
                    possibleWildcards.add(stringBuilder.toString());
                    continue;
                }
                int index = 0;
                //see Bug 40142
                if (filename.endsWith(".pgp")) {
                    index = filename.substring(0, filename.length() - 4).lastIndexOf('.');
                }
                else {
                    index = filename.lastIndexOf('.');
                }
                if (0 >= index) {
                    index = filename.length();
                }
                stringBuilder.insert(index, " (%)");
                possibleWildcards.add(stringBuilder.toString());
                continue;
            }
        }
        return possibleWildcards;
    }

    /**
     * Gets a list containing the object identifiers of the supplied ID tuples.
     *
     * @param tuples The tuples to get the object identifiers for
     * @return A list of corresponding object identifiers
     * @throws OXException
     */
    public static List<Integer> getObjectIDs(List<IDTuple> tuples) throws OXException {
        if (null == tuples) {
            return null;
        }
        List<Integer> ids = new ArrayList<Integer>(tuples.size());
        try {
            for (IDTuple tuple : tuples) {
                ids.add(Integer.valueOf(tuple.getId()));
            }
        } catch (NumberFormatException e) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }
        return ids;
    }

    /**
     * Gets a list containing the object identifiers of the supplied ID tuples.
     *
     * @param tuples The tuples to get the object identifiers for
     * @return A list of corresponding object identifiers
     * @throws OXException
     */
    public static int[] getObjectIDArray(List<IDTuple> tuples) throws OXException {
        if (null == tuples) {
            return null;
        }
        int[] ids = new int[tuples.size()];
        try {
            for (int i = 0; i < ids.length; i++) {
                ids[i] = Integer.parseInt(tuples.get(i).getId());
            }
        } catch (NumberFormatException e) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }
        return ids;
    }

    /**
     * Gets a list containing the object identifiers of the supplied documents.
     *
     * @param documents The documents to get the object identifiers for
     * @return A list of corresponding object identifiers
     * @throws OXException
     */
    public static List<Integer> getIDs(List<DocumentMetadata> documents) throws OXException {
        if (null == documents) {
            return null;
        }
        List<Integer> ids = new ArrayList<Integer>(documents.size());
        for (DocumentMetadata document : documents) {
            ids.add(Integer.valueOf(document.getId()));
        }
        return ids;
    }

    /**
     * Gets a map containing the object identifiers of the supplied ID tuples, mapped to their corresponding folder ID.
     *
     * @param tuples The tuples to get the ID mapping for
     * @return A map of corresponding object identifiers
     * @throws OXException
     */
    public static Map<Integer, Long> getIDsToFolders(List<IDTuple> tuples) throws OXException {
        Map<Integer, Long> idsToFolders = new HashMap<Integer, Long>(tuples.size());
        try {
            for (IDTuple idTuple : tuples) {
                idsToFolders.put(Integer.valueOf(idTuple.getId()), Long.valueOf(idTuple.getFolder()));
            }
        } catch (NumberFormatException e) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }
        return idsToFolders;
    }

    /**
     * Collects all infostore folders visible to a user and puts their folder IDs into the supplied lists, depending on the user being
     * allowed to read all contained items or only own ones.
     *
     * @param security A reference to the infostore security service
     * @param connection A readable connection to the database
     * @param context The context
     * @param user The user
     * @param userPermissions The user's permission bits
     * @param requestedFolderIDs The folder identifiers requested from the client, or <code>null</code> to use all visible ones (excluding trash folders)
     * @param all A collection to add the IDs of folders the user is able to read "all" items from
     * @param own A collection to add the IDs of folders the user is able to read only "own" items from
     * @return The effective permissions of all resulting folders, mapped to the corresponding folder identifiers
     */
    private static Map<Integer, EffectiveInfostoreFolderPermission> gatherVisibleFolders(InfostoreSecurity security, Connection connection, Context context, User user, UserPermissionBits userPermissions, int[] requestedFolderIDs, Collection<Integer> all, Collection<Integer> own) throws OXException {
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID = new HashMap<Integer, EffectiveInfostoreFolderPermission>();
        if (null != requestedFolderIDs) {
            /*
             * check permissions of supplied folders
             */
            for (int folderID : requestedFolderIDs) {
                EffectiveInfostoreFolderPermission infostorePermission = security.getFolderPermission(folderID, context, user, userPermissions, connection);
                if (false == infostorePermission.canReadOwnObjects()) {
                    throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
                }
                trackEffectivePermission(infostorePermission, permissionsByFolderID, all, own);
            }
        } else {
            /*
             * gather all visible folders and check their permissions
             */
            SearchIterator<FolderObject> searchIterator = null;
            try {
                searchIterator = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
                    user.getId(), user.getGroups(), userPermissions.getAccessibleModules(), FolderObject.INFOSTORE, context, connection);
                while (searchIterator.hasNext()) {
                    FolderObject folder = searchIterator.next();
                    if (FolderObject.TRASH == folder.getType()) {
                        continue;
                    }
                    EffectivePermission permission = folder.getEffectiveUserPermission(user.getId(), userPermissions);
                    EffectiveInfostoreFolderPermission infostorePermission = new EffectiveInfostoreFolderPermission(permission, folder.getCreatedBy());
                    trackEffectivePermission(infostorePermission, permissionsByFolderID, all, own);
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
            /*
             * also add special "shared files" folder if readable
             */
            int sharedFilesFolderID = FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
            EffectiveInfostoreFolderPermission infostorePermission = security.getFolderPermission(sharedFilesFolderID, context, user, userPermissions, connection);
            trackEffectivePermission(infostorePermission, permissionsByFolderID, all, own);
        }
        return permissionsByFolderID;
    }

    /**
     * Collects all infostore folders visible to a user below a specific one and puts their folder IDs into the supplied lists, depending
     * on the user being allowed to read all contained items or only own ones.
     *
     * @param security A reference to the infostore security service
     * @param connection A readable connection to the database
     * @param context The context
     * @param user The user
     * @param userPermissions The user's permission bits
     * @param rootFolderID The root folder identifier
     * @param ignoreTrash <code>true</code> to ignore trash folders, <code>false</code>, otherwise
     * @param all A collection to add the IDs of folders the user is able to read "all" items from
     * @param own A collection to add the IDs of folders the user is able to read only "own" items from
     * @return The effective permissions of all resulting folders, mapped to the corresponding folder identifiers
     */
    private static Map<Integer, EffectiveInfostoreFolderPermission> gatherVisibleFolders(InfostoreSecurity security, Connection connection, Context context, User user, UserPermissionBits userPermissions, int rootFolderID, boolean ignoreTrash, Collection<Integer> all, Collection<Integer> own) throws OXException {
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID = new HashMap<Integer, EffectiveInfostoreFolderPermission>();
        /*
         * gather all visible folders below the root folder and check their permissions
         */
        SearchIterator<FolderObject> searchIterator = null;
        try {
            searchIterator = OXFolderIteratorSQL.getVisibleSubfoldersIterator(rootFolderID, user.getId(), user.getGroups(), context, userPermissions, null, connection);
            while (searchIterator.hasNext()) {
                FolderObject folder = searchIterator.next();
                if (ignoreTrash && FolderObject.TRASH == folder.getType()) {
                    continue;
                }
                EffectivePermission permission = folder.getEffectiveUserPermission(user.getId(), userPermissions);
                EffectiveInfostoreFolderPermission infostorePermission = new EffectiveInfostoreFolderPermission(permission, folder.getCreatedBy());
                trackEffectivePermission(infostorePermission, permissionsByFolderID, all, own);
                /*
                 * gather visible subfolders recursively
                 */
                permissionsByFolderID.putAll(gatherVisibleFolders(security, connection, context, user, userPermissions, folder.getObjectID(), ignoreTrash, all, own));
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            SearchIterators.close(searchIterator);
        }
        return permissionsByFolderID;
    }

    /**
     * Iterates through a search iterator of documents and collects those documents that are located in or below the users personal
     * infostore folder, i.e. removing all documents from "non-private" folders.
     *
     * @param searchIterator The search iterator to filter the documents for
     * @param session The session
     * @param dbProvider The database provider to use
     * @return The documents, or an empty list if no "private" documents where found at all
     */
    public static List<DocumentMetadata> removeNonPrivate(SearchIterator<DocumentMetadata> searchIterator, ServerSession session, DBProvider dbProvider) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(session.getContext());
            return removeNonPrivate(searchIterator, session, connection);
        } finally {
            if (null != connection) {
                dbProvider.releaseReadConnection(session.getContext(), connection);
            }
        }
    }

    /**
     * Iterates through a search iterator of documents and collects those documents that are located in or below the users personal
     * infostore folder, i.e. removing all documents from "non-private" folders.
     *
     * @param searchIterator The search iterator to filter the documents for
     * @param session The session
     * @param connection A readable connection to the database
     * @return The documents, or an empty list if no "private" documents where found at all
     */
    public static List<DocumentMetadata> removeNonPrivate(SearchIterator<DocumentMetadata> searchIterator, ServerSession session, Connection connection) throws OXException {
        List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
        Map<Integer, Boolean> knownFolders = new HashMap<Integer, Boolean>();
        OXFolderAccess folderAccess = new OXFolderAccess(connection, session.getContext());
        int defaultFolderID = folderAccess.getDefaultFolderID(session.getUserId(), FolderObject.INFOSTORE);
        knownFolders.put(I(defaultFolderID), Boolean.TRUE);
        while (searchIterator.hasNext()) {
            DocumentMetadata document = searchIterator.next();
            if (document == null) {
                continue;
            }
            Integer folderID = I((int) document.getFolderId());
            List<Integer> seenFolders = new ArrayList<Integer>();
            while (false == knownFolders.containsKey(folderID) && FolderObject.MIN_FOLDER_ID < folderID.intValue()) {
                seenFolders.add(folderID);
                folderID = I(folderAccess.getParentFolderID(folderID.intValue()));
            }
            Boolean isPrivate = knownFolders.get(folderID);
            if (null == isPrivate) {
                isPrivate = Boolean.FALSE;
            } else if (isPrivate.booleanValue()) {
                documents.add(document);
            }
            for (Integer seenFolder : seenFolders) {
                knownFolders.put(seenFolder, isPrivate);
            }
        }
        return documents;
    }

    /**
     * Collects all infostore folders visible to a user and puts their folder IDs into the supplied lists, depending on the user being
     * allowed to read all contained items or only own ones.
     *
     * @param session The session
     * @param security A reference to the infostore security service
     * @param dbProvider The database provider to use
     * @param requestedFolderIDs The folder identifiers requested from the client, or <code>null</code> to use all visible ones
     * @param all A collection to add the IDs of folders the user is able to read "all" items from
     * @param own A collection to add the IDs of folders the user is able to read only "own" items from
     * @return The effective permissions of all resulting folders, mapped to the corresponding folder identifiers
     */
    public static Map<Integer, EffectiveInfostoreFolderPermission> gatherVisibleFolders(ServerSession session, InfostoreSecurity security, DBProvider dbProvider, int[] requestedFolderIDs, Collection<Integer> all, Collection<Integer> own) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(session.getContext());
            return gatherVisibleFolders(security, connection, session.getContext(), session.getUser(), session.getUserPermissionBits(), requestedFolderIDs, all, own);
        } finally {
            if (null != connection) {
                dbProvider.releaseReadConnection(session.getContext(), connection);
            }
        }
    }

    /**
     * Collects all infostore folders visible to a user below a specific one and puts their folder IDs into the supplied lists, depending
     * on the user being allowed to read all contained items or only own ones. Trash folders are excluded (in case the root folder is not
     * a trash folder itself).
     *
     * @param session The session
     * @param security A reference to the infostore security service
     * @param dbProvider The database provider to use
     * @param rootFolderID The root folder identifier
     * @param all A collection to add the IDs of folders the user is able to read "all" items from
     * @param own A collection to add the IDs of folders the user is able to read only "own" items from
     * @return The effective permissions of all resulting folders, mapped to the corresponding folder identifiers
     */
    public static Map<Integer, EffectiveInfostoreFolderPermission> gatherVisibleFolders(ServerSession session, InfostoreSecurity security, DBProvider dbProvider, int rootFolderID, Collection<Integer> all, Collection<Integer> own) throws OXException {
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID = new HashMap<Integer, EffectiveInfostoreFolderPermission>();
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(session.getContext());
            /*
             * get & add root folder
             */
            FolderObject rootFolder = new OXFolderAccess(connection, session.getContext()).getFolderObject(rootFolderID);
            EffectivePermission permission = rootFolder.getEffectiveUserPermission(session.getUserId(), session.getUserPermissionBits());
            EffectiveInfostoreFolderPermission infostorePermission = new EffectiveInfostoreFolderPermission(permission, rootFolder.getCreatedBy());
            trackEffectivePermission(infostorePermission, permissionsByFolderID, all, own);
            boolean ignoreTrash = FolderObject.TRASH != rootFolder.getType();
            /*
             * gather permissions of subfolders recursively
             */
            permissionsByFolderID.putAll(gatherVisibleFolders(
                security, connection, session.getContext(), session.getUser(), session.getUserPermissionBits(), rootFolderID, ignoreTrash, all, own));
            return permissionsByFolderID;
        } finally {
            if (null != connection) {
                dbProvider.releaseReadConnection(session.getContext(), connection);
            }
        }
    }

    /**
     * Prepares an array of metadata fields to include in the read documents based on a client-supplied list and additional fields
     * required for additional result processing.
     *
     * @param requestedFields The fields as requested from the client, or <code>null</code> to use all fields
     * @param requiredFields Additional fields to always include in the returned array, or <code>null</code> if not needed
     * @return The metadata fields to query
     */
    public static Metadata[] getFieldsToQuery(Metadata[] requestedFields, Metadata...requiredFields) {
        if (null == requestedFields) {
            return Metadata.VALUES_ARRAY; // all fields
        }
        if (null == requiredFields || 0 == requiredFields.length) {
            return requestedFields;
        }
        Set<Metadata> fields = new HashSet<Metadata>(Arrays.asList(requestedFields));
        fields.addAll(Arrays.asList(requiredFields));
        return fields.toArray(new Metadata[fields.size()]);
    }

    /**
     * Evaluates an effective infostore permission and puts it into different data structures for later usage.
     *
     * @param infostorePermission The infostore permission to track
     * @param permissionsByFolderID The map holding the permissions mapped to their corresponding folder identifier
     * @param all A collection to add the IDs of folders the user is able to read "all" items from
     * @param own A collection to add the IDs of folders the user is able to read only "own" items from
     */
    private static void trackEffectivePermission(EffectiveInfostoreFolderPermission infostorePermission, Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID, Collection<Integer> all, Collection<Integer> own) throws OXException {
        Integer id = I(infostorePermission.getFuid());
        if (infostorePermission.canReadAllObjects()) {
            all.add(id);
        } else if (infostorePermission.canReadOwnObjects()) {
            own.add(id);
        }
        permissionsByFolderID.put(id, infostorePermission);
    }

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

}
