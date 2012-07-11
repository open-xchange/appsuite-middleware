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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.solr.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.solr.SolrCore;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link SolrIndexMysql}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrIndexMysql {

    private static final SolrIndexMysql INSTANCE = new SolrIndexMysql();
    

    private SolrIndexMysql() {
        super();
    }

    public static SolrIndexMysql getInstance() {
        return INSTANCE;
    }

    public boolean hasActiveCore(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly(cid);
        try {
            return hasActiveCore(con, cid, uid, module);
        } finally {
            dbService.backReadOnly(cid, con);
        }
    }

    public boolean hasActiveCore(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT active FROM solrCores WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            } else {
                throw SolrExceptionCodes.CORE_ENTRY_NOT_FOUND.create(uid, module, cid);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public SolrCore getSolrCore(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly(cid);
        try {
            return getSolrCore(con, cid, uid, module);
        } finally {
            dbService.backReadOnly(cid, con);
        }
    }

    public SolrCore getSolrCore(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int storeId;
        final String server;
        final boolean active;
        try {
            stmt = con.prepareStatement("SELECT store, active, server FROM solrCores WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);

            rs = stmt.executeQuery();
            if (rs.next()) {
                storeId = rs.getInt(1);
                active = rs.getBoolean(2);
                server = rs.getString(3);
            } else {
                throw SolrExceptionCodes.CORE_ENTRY_NOT_FOUND.create(uid, module, cid);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        final SolrCore core = new SolrCore(new SolrCoreIdentifier(cid, uid, module));
        core.setStore(storeId);
        core.setActive(active);
        core.setServer(server);

        return core;
    }

    public boolean createCoreEntry(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            return createCoreEntry(con, cid, uid, module);
        } finally {
            dbService.backWritable(cid, con);
        }
    }

    public boolean createCoreEntry(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        final int storeId = getFreeCoreStore();
        try {
            stmt = con.prepareStatement("INSERT INTO solrCores (cid, uid, module, store, active, server) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE cid = cid");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);
            stmt.setInt(i++, storeId);
            stmt.setBoolean(i++, false);
            stmt.setNull(i, java.sql.Types.VARCHAR);
            final int rows = stmt.executeUpdate();
            if (rows == 1) {
                return true;
            }

            return false;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    public void removeCoreEntry(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            removeCoreEntry(con, cid, uid, module);
        } finally {
            dbService.backWritable(cid, con);
        }
    }

    public void removeCoreEntry(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement ustmt = null;
        try {
            final SolrCore solrCore = getSolrCore(con, cid, uid, module);
            ustmt = con.prepareStatement("DELETE FROM solrCores WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            ustmt.setInt(i++, cid);
            ustmt.setInt(i++, uid);
            ustmt.setInt(i, module);

            ustmt.executeUpdate();
            decrementCoreStore(solrCore.getStore());
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(ustmt);
        }
    }

    public boolean coreEntryExists(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly(cid);
        try {
            return coreEntryExists(con, cid, uid, module);
        } finally {
            dbService.backReadOnly(cid, con);
        }
    }

    public boolean coreEntryExists(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT COUNT(*) AS count FROM solrCores WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);

            rs = stmt.executeQuery();
            if (rs.next()) {
                final int count = rs.getInt(1);
                return count == 0 ? false : true;
            }

            return false;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public boolean activateCoreEntry(final int cid, final int uid, final int module, final String server) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            return activateCoreEntry(con, cid, uid, module, server);
        } finally {
            dbService.backWritable(cid, con);
        }
    }

    public boolean activateCoreEntry(final Connection con, final int cid, final int uid, final int module, final String server) throws OXException {
        return updateCoreEntry(con, cid, uid, module, true, server);
    }

    public boolean deactivateCoreEntry(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            return deactivateCoreEntry(con, cid, uid, module);
        } finally {
            dbService.backWritable(cid, con);
        }
    }

    public boolean deactivateCoreEntry(final Connection con, final int cid, final int uid, final int module) throws OXException {
        return updateCoreEntry(con, cid, uid, module, false, null);
    }

    private boolean updateCoreEntry(final Connection con, final int cid, final int uid, final int module, final boolean activate, final String server) throws OXException {
        PreparedStatement sstmt = null;
        ResultSet rs = null;
        PreparedStatement ustmt = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement("SELECT active FROM solrCores WHERE cid = ? AND uid = ? AND module = ? FOR UPDATE");
            int i = 1;
            sstmt.setInt(i++, cid);
            sstmt.setInt(i++, uid);
            sstmt.setInt(i, module);

            rs = sstmt.executeQuery();
            if (rs.next()) {
                final boolean active = rs.getBoolean(1);
                if (active && activate) {
                    con.commit();
                    return false;
                } else {
                    ustmt = con.prepareStatement("UPDATE solrCores SET active = ?, server = ? WHERE cid = ? AND uid = ? AND module = ?");
                    i = 1;
                    ustmt.setBoolean(i++, activate);
                    if (activate) {
                        ustmt.setString(i++, server);
                    } else {
                        ustmt.setNull(i++, java.sql.Types.VARCHAR);
                    }
                    ustmt.setInt(i++, cid);
                    ustmt.setInt(i++, uid);
                    ustmt.setInt(i, module);

                    ustmt.executeUpdate();
                    con.commit();

                    return true;
                }
            } else {
                throw SolrExceptionCodes.CORE_ENTRY_NOT_FOUND.create(uid, module, cid);
            }
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, sstmt);
            DBUtils.closeSQLStuff(ustmt);
            DBUtils.autocommit(con);
        }
    }

    public void deactivateCoresForServer(final String server, final int contextId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(contextId);
        try {
            deactivateCoresForServer(con, server, contextId);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    public void deactivateCoresForServer(final Connection con, final String server, final int contextId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE solrCores SET active = ?, server = ? WHERE server = ? AND cid = ?");
            int i = 1;
            stmt.setBoolean(i++, false);
            stmt.setNull(i++, java.sql.Types.VARCHAR);
            stmt.setString(i++, server);
            stmt.setInt(i, contextId);

            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    public List<SolrCoreStore> getCoreStores() throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly();
        try {
            return getCoreStores(con);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    public List<SolrCoreStore> getCoreStores(final Connection con) throws OXException {
        final List<SolrCoreStore> stores = new ArrayList<SolrCoreStore>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String uri = null;
        try {
            stmt = con.prepareStatement("SELECT id, uri, maxCores FROM solrCoreStores");
            rs = stmt.executeQuery();

            while (rs.next()) {
                final SolrCoreStore store = new SolrCoreStore();
                int i = 1;
                store.setId(rs.getInt(i++));
                uri = rs.getString(i++);
                store.setUri(new URI(uri));
                store.setMaxCores(rs.getInt(i++));

                stores.add(store);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final URISyntaxException e) {
            throw SolrExceptionCodes.URI_PARSE_ERROR.create(uri);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return stores;
    }

    public SolrCoreStore getCoreStore(final int storeId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly();
        try {
            return getCoreStore(con, storeId);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    public SolrCoreStore getCoreStore(final Connection con, final int storeId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String uri = null;
        try {
            stmt = con.prepareStatement("SELECT id, uri, maxCores FROM solrCoreStores WHERE id = ?");
            stmt.setInt(1, storeId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                final SolrCoreStore store = new SolrCoreStore();
                int i = 1;
                store.setId(rs.getInt(i++));
                uri = rs.getString(i++);
                store.setUri(new URI(uri));
                store.setMaxCores(rs.getInt(i++));

                return store;
            } else {
                throw SolrExceptionCodes.CORE_STORE_ENTRY_NOT_FOUND.create("StoreId: " + storeId);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final URISyntaxException e) {
            throw SolrExceptionCodes.URI_PARSE_ERROR.create(uri);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public int createCoreStoreEntry(final SolrCoreStore store) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            return createCoreStoreEntry(con, store);
        } finally {
            dbService.backWritable(con);
        }
    }

    public int createCoreStoreEntry(final Connection con, final SolrCoreStore store) throws OXException {
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);
            final int id = IDGenerator.getId(con);
            stmt = con.prepareStatement("INSERT INTO solrCoreStores (id, uri, maxCores) VALUES (?, ?, ?)");
            int i = 1;
            stmt.setInt(i++, id);
            stmt.setString(i++, store.getUri().toString());
            stmt.setInt(i, store.getMaxCores());

            stmt.executeUpdate();
            return id;
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(stmt);
        }
    }

    public void updateCoreStoreEntry(final SolrCoreStore store) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            updateCoreStoreEntry(con, store);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void updateCoreStoreEntry(final Connection con, final SolrCoreStore store) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE solrCoreStores SET uri = ?, maxCores = ? WHERE id = ?");
            int i = 1;
            stmt.setString(i++, store.getUri().toString());
            stmt.setInt(i++, store.getMaxCores());
            stmt.setInt(i, store.getId());

            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    public void removeCoreStoreEntry(final int storeId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            removeCoreStoreEntry(con, storeId);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void removeCoreStoreEntry(final Connection con, final int storeId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM solrCoreStores WHERE id = ?");
            stmt.setInt(1, storeId);

            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private DatabaseService getDbService() throws OXException {
        final DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

    private int getFreeCoreStore() throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement("SELECT id, maxCores - numCores AS free FROM solrCoreStores HAVING free = (SELECT MAX(maxCores - numCores) FROM solrCoreStores) AND free > 0 LIMIT 1");
            rs = sstmt.executeQuery();
            if (rs.next()) {
                final int id = rs.getInt(1);
                ustmt = con.prepareStatement("UPDATE solrCoreStores SET numCores = numCores + 1 WHERE id = ?");
                ustmt.setInt(1, id);
                
                ustmt.executeUpdate();
                con.commit();
                return id;
            }

            con.commit();
            throw SolrExceptionCodes.NO_FREE_CORE_STORE.create();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, sstmt);
            DBUtils.closeSQLStuff(ustmt);
            DBUtils.autocommit(con);
            dbService.backWritable(con);
        }
    }
    
    private void decrementCoreStore(final int storeId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE solrCoreStores SET numCores = numCores - 1 WHERE id = ?");
            stmt.setInt(1, storeId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            dbService.backWritable(con);
        }
    }
}
