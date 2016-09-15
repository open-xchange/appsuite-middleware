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

package com.openexchange.folderstorage.outlook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.openexchange.tools.sql.DBUtils;


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
                final boolean started = folderStorage.startTransaction(storageParameters, true);
                try {
                    folderStorage.deleteFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    actionPerformed = true;
                    ids.add(folderId);
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    LOG.warn("Deleting folder {} failed for tree {}", folderId, treeId, e);
                } catch (final Exception e) {
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
            } catch (final OXException e) {
                DBUtils.rollback(con);
                throw e;
            } catch (final SQLException e) {
                DBUtils.rollback(con);
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                DBUtils.rollback(con);
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (null != con) {
                    DBUtils.autocommit(con);
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
