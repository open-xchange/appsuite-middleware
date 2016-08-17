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

package com.openexchange.filestore.impl;

import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorage2EntitiesResolver;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.impl.osgi.Services;

/**
 * {@link DbFileStorage2EntitiesResolver} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class DbFileStorage2EntitiesResolver implements FileStorage2EntitiesResolver {

    /**
     * Initializes a new {@link DbFileStorage2EntitiesResolver}.
     */
    public DbFileStorage2EntitiesResolver() {
        super();
    }

    @Override
    public List<FileStorage> getFileStoragesUsedBy(int contextId, boolean quotaAware) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        // Get appropriate service
        QuotaFileStorageService qfsService = FileStorages.getQuotaFileStorageService();

        List<FileStorage> fileStorages = new LinkedList<FileStorage>();
        if (quotaAware) {
            // Add the one used by context itself
            fileStorages.add(qfsService.getQuotaFileStorage(contextId));

            // Add the ones referenced by context's users
            Set<FsInfo> infos = retrieveFileStoragesFromUsers(contextId, databaseService);
            for (FsInfo fsInfo : infos) {
                fileStorages.add(qfsService.getQuotaFileStorage(fsInfo.owner, contextId));
            }
        } else {
            // Get raw service to obtain non-quota-aware instances
            FileStorageService fsService = FileStorages.getFileStorageService();

            // Add the one used by context itself
            fileStorages.add(fsService.getFileStorage(qfsService.getQuotaFileStorage(contextId).getUri()));

            //Add the ones referenced by context's users
            Set<FsInfo> infos = retrieveFileStoragesFromUsers(contextId, databaseService);
            for (FsInfo fsInfo : infos) {
                fileStorages.add(fsService.getFileStorage(qfsService.getQuotaFileStorage(fsInfo.owner, contextId).getUri()));
            }
        }

        return fileStorages;
    }

    private Set<FsInfo> retrieveFileStoragesFromUsers(int contextId, DatabaseService databaseService) throws OXException {
        Connection schemaCon = databaseService.getReadOnly(contextId);
        try {
            return retrieveFileStoragesFromUsers(contextId, schemaCon);
        } finally {
            databaseService.backReadOnly(contextId, schemaCon);
        }
    }

    private Set<FsInfo> retrieveFileStoragesFromUsers(int contextId, Connection schemaCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = schemaCon.prepareStatement("SELECT id, filestore_id, filestore_owner FROM user WHERE cid=? AND filestore_id>0");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptySet();
            }

            // Create sorted set
            Set<FsInfo> infos = new TreeSet<FsInfo>();
            do {
                infos.add(new FsInfo(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            } while (rs.next());
            return infos;
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------

    @Override
    public FileStorage getFileStorageUsedBy(int contextId, boolean quotaAware) throws OXException {
        // Get appropriate service
        QuotaFileStorageService qfsService = FileStorages.getQuotaFileStorageService();

        if (quotaAware) {
            // Add the one used by context itself
            return qfsService.getQuotaFileStorage(contextId);
        } else {
            // Get raw service to obtain non-quota-aware instances
            FileStorageService fsService = FileStorages.getFileStorageService();

            // Add the one used by context itself
            return fsService.getFileStorage(qfsService.getQuotaFileStorage(contextId).getUri());
        }
    }

    @Override
    public int[] getIdsOfFileStoragesUsedBy(int contextId) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        TIntSet usedFileStorages = new TIntHashSet(8);

        {
            Connection configDBCon = databaseService.getReadOnly();
            try {
                addUsingContext(contextId, usedFileStorages, configDBCon);
            } finally {
                databaseService.backReadOnly(configDBCon);
            }
        }

        Connection schemaCon = databaseService.getReadOnly(contextId);
        try {
            addUsingUsers(contextId, usedFileStorages, schemaCon);
        } finally {
            databaseService.backReadOnly(contextId, schemaCon);
        }

        return usedFileStorages.toArray();
    }

    private void addUsingUsers(int contextId, TIntSet usedFileStorages, Connection schemaCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = schemaCon.prepareStatement("SELECT DISTINCT filestore_id FROM user WHERE cid=? AND filestore_id>0");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                usedFileStorages.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @Override
    public int[] getIdsOfContextsUsing(int fileStorageId) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        TIntSet usingContexts = new TIntHashSet(256);
        {
            Connection configDBCon = databaseService.getReadOnly();
            try {
                addContextsUsing(fileStorageId, usingContexts, configDBCon);
            } finally {
                databaseService.backReadOnly(configDBCon);
            }
        }

        return usingContexts.toArray();
    }

    /**
     * Fetches all context identifiers from the config database
     * 
     * @param configDBCon The config database connection
     * @return An array with all context identifiers
     * @throws OXException
     */
    protected int[] getAllContextIds(Connection configDBCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDBCon.prepareStatement("SELECT cid FROM context");
            rs = stmt.executeQuery();
            TIntList cids = new TIntLinkedList();
            while (rs.next()) {
                cids.add(rs.getInt(1));
            }
            return cids.toArray();
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Adds to the specified set all context identifiers that are using the specified file storage
     * 
     * @param fileStorageId The file storage identifier
     * @param usingContexts The set of the context identifiers
     * @param configDBCon The configDB connection
     * @throws OXException
     */
    private void addContextsUsing(int fileStorageId, TIntSet usingContexts, Connection configDBCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDBCon.prepareStatement("SELECT cid FROM context WHERE filestore_id=?");
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                usingContexts.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Adds to the specified set all file storage identifiers that are being used by the specified context
     * 
     * @param contextId The context identifier
     * @param usedFileStorages The file storages id set
     * @param configDBCon The configDB connection
     * @throws OXException
     */
    private void addUsingContext(int contextId, TIntSet usedFileStorages, Connection configDBCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDBCon.prepareStatement("SELECT filestore_id FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                usedFileStorages.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------

    static final class FsInfo implements Comparable<FsInfo> {

        final int fileStorageId;
        final int owner;
        private final int hash;

        FsInfo(int userId, int fileStorageId, int owner) {
            super();
            this.fileStorageId = fileStorageId;
            this.owner = owner > 0 ? owner : userId;
            int prime = 31;
            int result = prime * 1 + fileStorageId;
            result = prime * result + owner;
            hash = result;
        }

        @Override
        public int compareTo(FsInfo o) {
            int result = this.fileStorageId < o.fileStorageId ? -1 : (this.fileStorageId == o.fileStorageId ? 0 : 1);
            if (0 == result) {
                result = this.owner < o.owner ? -1 : (this.owner == o.owner ? 0 : 1);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FsInfo)) {
                return false;
            }
            FsInfo other = (FsInfo) obj;
            if (fileStorageId != other.fileStorageId) {
                return false;
            }
            if (owner != other.owner) {
                return false;
            }
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.filestore.FileStorage2UsersResolver#getIdsOfUsersUsing(int)
     */
    @Override
    public Map<Integer, List<Integer>> getIdsOfUsersUsing(int fileStorageId) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);
        Map<Integer, List<Integer>> users = new HashMap<Integer, List<Integer>>();
        TIntSet usingContexts = new TIntHashSet(256);

        // Get all contexts and find out which ones are using the specified file storage
        int[] contextIds;
        {
            Connection configDBConnection = databaseService.getReadOnly();
            try {
                contextIds = getAllContextIds(configDBConnection);
                addContextsUsing(fileStorageId, usingContexts, configDBConnection);
            } finally {
                databaseService.backReadOnly(configDBConnection);
            }
        }

        // Find out which users are using the specified file storage
        TIntSet processed = new TIntHashSet(256);
        for (Integer ctxId : contextIds) {
            if (processed.add(ctxId)) {
                Connection schemaConnection = databaseService.getReadOnly(ctxId);
                try {
                    addUsersUsing(fileStorageId, users, schemaConnection);
                } finally {
                    databaseService.backReadOnly(schemaConnection);
                }
                processed.addAll(databaseService.getContextsInSameSchema(ctxId));
            }
        }

        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.filestore.FileStorage2UsersResolver#getFileStoragesUsedBy(int, int, boolean)
     */
    @Override
    public FileStorage getFileStorageUsedBy(int contextId, int userId, boolean quotaAware) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);
        QuotaFileStorageService qfsService = FileStorages.getQuotaFileStorageService();
        Connection schemaCon = databaseService.getReadOnly(contextId);
        try {
            FsInfo fsInfo = retrieveFileStoragesFromUser(contextId, userId, schemaCon);

            if (quotaAware) {
                return qfsService.getQuotaFileStorage(fsInfo.owner, contextId);
            } else {
                FileStorageService fsService = FileStorages.getFileStorageService();
                return fsService.getFileStorage(qfsService.getQuotaFileStorage(fsInfo.owner, contextId).getUri());
            }

        } finally {
            databaseService.backReadOnly(schemaCon);
        }
    }

    /**
     * Adds the users using the specified file storage
     * 
     * @param fileStorageId The file storage identifier
     * @param users A map with all users using that file storage
     * @param schemaCon The schema connection
     * @throws OXException
     */
    private void addUsersUsing(int fileStorageId, Map<Integer, List<Integer>> users, Connection schemaCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = schemaCon.prepareStatement("SELECT cid, id FROM user WHERE filestore_id=? ORDER BY cid ASC");
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int ctxId = rs.getInt(1);
                List<Integer> list = users.containsKey(ctxId) ? users.get(ctxId) : new ArrayList<Integer>();
                list.add(rs.getInt(2));
                users.put(ctxId, list);
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Retrieves the file storage information for the specified user
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param schemaCon The schema connection
     * @return The file storage information for the specified user
     * @throws OXException
     */
    private FsInfo retrieveFileStoragesFromUser(int contextId, int userId, Connection schemaCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = schemaCon.prepareStatement("SELECT id, filestore_id, filestore_owner FROM user WHERE cid=? AND id=? AND filestore_id>0");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();

            return (rs.next()) ? new FsInfo(rs.getInt(1), rs.getInt(2), rs.getInt(3)) : new FsInfo(-1, -1, -1);
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }
}
