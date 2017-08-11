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

package com.openexchange.folderstorage.folderproperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.groupware.ldap.User;

/**
 * {@link FolderPropertyStorage}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class FolderPropertyStorage implements FolderStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderPropertyStorage.class);

    private final static String EXIST = "SELECT EXISTS(SELECT 1 FROM oxfolder_userized WHERE cid=? AND fuid=? AND userid=? LIMIT 1);";

    @Override
    public void clearCache(int userId, int contextId) {
        // Ignore
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        // For all types
        return new ContentType[] {};
    }

    @Override
    public FolderType getFolderType() {
        return FolderType.GLOBAL;
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public ContentType getDefaultContentType() {
        // No specific content type by default
        return null;
    }

    @Override
    public boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // Check if properties for the given folder exist. If so folder is considered as not empty
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            // Acquire connection
            connection = getConnection(storageParameters);

            // Prepare statement
            stmt = connection.prepareStatement(EXIST);
            stmt.setInt(1, storageParameters.getContextId());
            stmt.setInt(2, Integer.valueOf(folderId));
            stmt.setInt(3, storageParameters.getUserId());

            // Execute & check result
            ResultSet result = stmt.executeQuery();
            if (result.next() && result.getInt(1) == 1) {
                return false;
            }
        } catch (Exception e) {
            LOG.debug("Couldn't check if the folder exists", e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
        return true;
    }

    @Override
    public boolean containsForeignObjects(User user, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // TODO - False per default? 
        return false;
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // If empty no folder exists
        return false == isEmpty(treeId, folderId, storageParameters);
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        // If empty no folder exists. Ignore StorageType (Not relevant for folder)
        return false == isEmpty(treeId, folderId, storageParameters);
    }

    @Override
    public void checkConsistency(String treeId, StorageParameters storageParameters) throws OXException {
        // TODO 
    }

    @Override
    public void restore(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // TODO  - Unsupported? No caching or moving to del_* tables
    }

    @Override
    public Folder prepareFolder(String treeId, Folder folder, StorageParameters storageParameters) throws OXException {
        return folder;
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // Get the folder from other storage, get properties for the folder, return
        
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageType storageType, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefaultFolderID(User user, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type getTypeByParent(User user, String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateLastModified(long lastModified, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public SortableId[] getVisibleFolders(String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortableId[] getVisibleFolders(String rootFolderId, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortableId[] getSubfolders(String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getModifiedFolderIDs(String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getDeletedFolderIDs(String treeId, Date timeStamp, StorageParameters storageParameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void commitTransaction(StorageParameters params) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback(StorageParameters params) {
        // TODO Auto-generated method stub

    }

    /**
     * Get a connection from the {@link TransactionManager}
     * 
     * @param storageParameters THe {@link StorageParameters}
     * @return A {@link Connection}
     * @throws OXException See {@link TransactionManager#initTransaction(StorageParameters)}
     */
    private Connection getConnection(StorageParameters storageParameters) throws OXException {
        TransactionManager.initTransaction(storageParameters);
        return TransactionManager.getTransactionManager(storageParameters).getConnection();
    }

}
