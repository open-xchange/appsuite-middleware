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

package com.openexchange.folderstorage.outlook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.outlook.sql.Duplicate;
import com.openexchange.java.util.Tools;


/**
 * {@link DuplicateCleaner}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DuplicateCleaner {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DuplicateCleaner.class);

    /**
     * Initializes a new {@link DuplicateCleaner}.
     */
    private DuplicateCleaner() {
        super();
    }

    /**
     * Cleans duplicates from virtual tree.
     *
     * @param treeId The virtual tree identifier
     * @param storageParameters The storage parameters
     * @throws OXException If cleaning fails
     */
    public static void cleanDuplicates(final String treeId, final StorageParameters storageParameters) throws OXException {
        cleanDuplicates(treeId, storageParameters, null);
    }

    /**
     * Cleans duplicates from virtual tree.
     *
     * @param treeId The virtual tree identifier
     * @param storageParameters The storage parameters
     * @param lookUp The optional look-up identifier possibly contained in deleted duplicates
     * @return <code>true</code> if look-up is contained in deleted IDs; otherwise <code>false</code>
     * @throws OXException If cleaning fails
     */
    public static String cleanDuplicates(final String treeId, final StorageParameters storageParameters, final String lookUp) throws OXException {
        final OutlookFolderStorage outlookFolderStorage = OutlookFolderStorage.getInstance();
        final FolderStorageDiscoverer folderStorageRegistry = outlookFolderStorage.folderStorageRegistry;
        final String realTreeId = outlookFolderStorage.realTreeId;

        final int tree = Tools.getUnsignedInteger(treeId);

        final int contextId = storageParameters.getContextId();
        final int userId = storageParameters.getUserId();
        final Map<String, List<String>> name2ids = Duplicate.lookupDuplicateNames(contextId, tree, userId);
        if (name2ids.isEmpty()) {
            return null;
        }
        boolean actionPerformed = false;
        String first = null;
        final Set<String> ids = new HashSet<String>(name2ids.size());
        for (final List<String> folderIds : name2ids.values()) {
            for (final String folderId : folderIds) {
                if (null == first && null != lookUp && lookUp.equals(folderId)) {
                    first = folderId;
                }
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (folderStorage == null) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                final boolean started = folderStorage.startTransaction(storageParameters, true);
                try {
                    folderStorage.deleteFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    actionPerformed = true;
                    ids.add(folderId);
                } catch (OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    LOG.warn("Deleting folder {} failed for tree {}", folderId, treeId, e);
                } catch (Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    LOG.warn("Deleting folder {} failed for tree {}", folderId, treeId, e);
                }
            }
        }
        if (!ids.isEmpty()) {
            Connection con = null;
            try {
                con = Database.get(contextId, true);
                con.setAutoCommit(false); // BEGIN
                for (final String folderId : ids) {
                    Delete.deleteFolder(contextId, tree, userId, folderId, false, false, con);
                }
                con.commit(); // COMMIT
                actionPerformed = true;
            } catch (OXException e) {
                Databases.rollback(con);
                throw e;
            } catch (SQLException e) {
                Databases.rollback(con);
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } catch (RuntimeException e) {
                Databases.rollback(con);
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (null != con) {
                    Databases.autocommit(con);
                    Database.back(contextId, true, con);
                }
            }
        }
        if (actionPerformed) {
            CacheFolderStorage.getInstance().clearCache(userId, contextId);
        }
        return first;
    }

}
