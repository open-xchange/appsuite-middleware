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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.internal;

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
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexServer;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link ConfigIndexMysql}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexMysql {

    private static final ConfigIndexMysql INSTANCE = new ConfigIndexMysql();


    private ConfigIndexMysql() {
        super();
    }

    public static ConfigIndexMysql getInstance() {
        return INSTANCE;
    }    
    
    public IndexServer getIndexServer(final int cid, final int uid, final int module, final int serverId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly();
        try {
            return getIndexServer(con, cid, uid, module, serverId);
        } finally {
            dbService.backReadOnly(con);
        }
    }
    
    public IndexServer getIndexServer(final Connection con, final int cid, final int uid, final int module, final int serverId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT serverUrl, maxIndices, socketTimeout, connectionTimeout, maxConnections FROM solrServers WHERE id = ?");
            stmt.setInt(1, serverId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                int i = 1;
                final String serverUrl = rs.getString(i++);
                final int maxIndices = rs.getInt(i++);
                final int socketTimeout = rs.getInt(i++);
                final int connectionTimeout = rs.getInt(i++);
                final int maxConnections = rs.getInt(i++);

                final IndexServerImpl server = new IndexServerImpl();
                server.setId(serverId);
                server.setUrl(serverUrl);
                server.setMaxIndices(maxIndices);
                server.setSoTimeout(socketTimeout);
                server.setConnectionTimeout(connectionTimeout);
                server.setMaxConnectionsPerHost(maxConnections);
                
                return server;
            }
            
            throw IndexExceptionCodes.SERVER_NOT_FOUND.create(serverId);
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }
    
    public int createIndexServerEntry(final IndexServer server) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            return createIndexServerEntry(con, server);
        } finally {
            dbService.backWritable(con);
        }
    }

    public int createIndexServerEntry(final Connection con, final IndexServer server) throws OXException {
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);
            final int id = IDGenerator.getId(con);
            stmt = con.prepareStatement("INSERT INTO solrServers (id, serverUrl, maxIndices, socketTimeout, connectionTimeout, maxConnections) VALUES (?, ?, ?, ?, ?, ?)");
            int i = 1;
            stmt.setInt(i++, id);
            stmt.setString(i++, server.getUrl());
            stmt.setInt(i++, server.getMaxIndices());
            stmt.setInt(i++, server.getSoTimeout());
            stmt.setInt(i++, server.getConnectionTimeout());
            stmt.setInt(i++, server.getMaxConnectionsPerHost());

            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.REGISTER_SERVER_ERROR.create(server.getUrl());
            }

            con.commit();
            return id;
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(con);
        }
    }

    public void removeIndexServerEntry(final int serverId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            removeIndexServerEntry(con, serverId);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void removeIndexServerEntry(final Connection con, final int serverId) throws OXException {
        PreparedStatement sstmt = null;
        final PreparedStatement mstmt = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement("DELETE FROM solrServers WHERE id = ?");
            sstmt.setInt(1, serverId);

            final int rows = sstmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.UNREGISTER_SERVER_ERROR.create(Integer.valueOf(serverId));
            }

            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(mstmt);
            DBUtils.autocommit(con);
        }
    }

    public void updateIndexServerEntry(final IndexServer server) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            updateIndexServerEntry(con, server);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    public void updateIndexServerEntry(final Connection con, final IndexServer server) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(createServerUpdateSQL(server));
            int i = 1;
            if (server.hasUrl()) {
                stmt.setString(i++, server.getUrl());
            }
            if (server.hasSoTimeout()) {
                stmt.setInt(i++, server.getSoTimeout());
            }
            if (server.hasConnectionTimeout()) {
                stmt.setInt(i++, server.getConnectionTimeout());
            }
            if (server.hasMaxConnectionsPerHost()) {
                stmt.setInt(i++, server.getMaxConnectionsPerHost());
            }
            if (server.hasMaxIndices()) {
                stmt.setInt(i++, server.getMaxIndices());
            }
            stmt.setInt(i, server.getId());

            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.SERVER_NOT_FOUND.create(Integer.valueOf(server.getId()));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    
    public boolean hasActiveCore(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            return hasActiveCore(con, cid, uid, module);
        } finally {
            dbService.backWritable(cid, con);
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
                createCoreEntry(con, cid, uid, module);
                return false;
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }
    
    public String getIndexFile(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly(cid);
        try {
            return getIndexFile(con, cid, uid, module);
        } finally {
            dbService.backReadOnly(cid, con);
        }
    }
    
    public String getIndexFile(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT indexFile FROM solrIndexFiles WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);
            
            rs = stmt.executeQuery();            
            final String indexFile;
            if (rs.next()) {
                indexFile = rs.getString(1);
            } else {
                throw IndexExceptionCodes.INDEX_NOT_FOUND.create(uid, module, cid);
            }
            
            return indexFile;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }
    
    public void createIndexFileEntry(final int cid, final int uid, final int module, final String indexFile) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            createIndexFileEntry(con, cid, uid, module, indexFile);
        } finally {
            dbService.backWritable(cid, con);
        }
    }
    
    public void createIndexFileEntry(final Connection con, final int cid, final int uid, final int module, final String indexFile) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO solrIndexFiles (cid, uid, module, indexFile) VALUES (?, ?, ?, ?)");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);
            stmt.setString(i, indexFile);
            
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
        final String coreName;
        final int serverId;
        boolean hasServerId = false;
        try {
            stmt = con.prepareStatement("SELECT core, server FROM solrCores WHERE cid = ? AND uid = ? AND module = ?");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);
            
            rs = stmt.executeQuery();            
            if (rs.next()) {
                coreName = rs.getString(1);
                serverId = rs.getInt(2);
                if (!rs.wasNull()) {
                    hasServerId = true;
                }
            } else {
                throw IndexExceptionCodes.CORE_ENTRY_NOT_FOUND.create(uid, module, cid);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        
        final SolrCore core = new SolrCore();
        core.setCoreName(coreName);
        
        if (hasServerId) {
            final IndexServer indexServer = getIndexServer(con, cid, uid, module, serverId);
            core.setServer(indexServer);
        }        
        
        return core;
    }
    
    public void createCoreEntry(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            createCoreEntry(con, cid, uid, module);
        } finally {
            dbService.backWritable(cid, con);
        }
    }
    
    public void createCoreEntry(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO solrCores (cid, uid, module, active, core, server) VALUES (?, ?, ?, ?, ?, ?)");
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);
            stmt.setBoolean(i++, false);
            stmt.setNull(i++, java.sql.Types.VARCHAR);
            stmt.setNull(i, java.sql.Types.INTEGER);
            
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    
    public boolean activateCoreEntry(final int cid, final int uid, final int module, final String coreName, final int serverId) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable(cid);
        try {
            return activateCoreEntry(con, cid, uid, module, coreName, serverId);
        } finally {
            dbService.backWritable(cid, con);
        }
    }
    
    public boolean activateCoreEntry(final Connection con, final int cid, final int uid, final int module, final String coreName, final int serverId) throws OXException {
        return updateCoreEntry(con, cid, uid, module, true, coreName, serverId);
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
        return updateCoreEntry(con, cid, uid, module, false, null, 0);
    }
    
    private boolean updateCoreEntry(final Connection con, final int cid, final int uid, final int module, final boolean activate, final String coreName, final int serverId) throws OXException {
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
                    ustmt = con.prepareStatement("UPDATE solrCores SET active = ?, core = ?, server = ? WHERE cid = ? AND uid = ? AND module = ?");
                    i = 1;
                    ustmt.setBoolean(i++, activate);
                    if (activate) {
                        ustmt.setString(i++, coreName);
                        ustmt.setInt(i++, serverId);
                    } else {
                        ustmt.setNull(i++, java.sql.Types.VARCHAR);
                        ustmt.setNull(i++, java.sql.Types.INTEGER);
                    }                    
                    ustmt.setInt(i++, cid);
                    ustmt.setInt(i++, uid);
                    ustmt.setInt(i, module);
                    
                    ustmt.executeUpdate();
                    con.commit();
                    
                    return true;
                }
            } else {
                throw IndexExceptionCodes.CORE_ENTRY_NOT_FOUND.create(uid, module, cid);
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

    public List<IndexServer> getAllIndexServers() throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getReadOnly();
        try {
            return getAllIndexServers(con);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    public List<IndexServer> getAllIndexServers(final Connection con) throws OXException {
        final List<IndexServer> servers = new ArrayList<IndexServer>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, serverUrl, maxIndices, socketTimeout, connectionTimeout, maxConnections FROM solrServers");
            rs = stmt.executeQuery();

            while (rs.next()) {
                int i = 1;
                final int id = rs.getInt(i++);
                final String url = rs.getString(i++);
                final int maxIndices = rs.getInt(i++);
                final int socketTimeout = rs.getInt(i++);
                final int connectionTimeout = rs.getInt(i++);
                final int maxConnections = rs.getInt(i++);

                final IndexServerImpl server = new IndexServerImpl();
                server.setId(id);
                server.setUrl(url);
                server.setMaxIndices(maxIndices);
                server.setSoTimeout(socketTimeout);
                server.setConnectionTimeout(connectionTimeout);
                server.setMaxConnectionsPerHost(maxConnections);

                servers.add(server);
            }

            return servers;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private DatabaseService getDbService() throws OXException {
        final DatabaseService dbService = IndexServiceLookup.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

    private String createServerUpdateSQL(final IndexServer server) {
        final StringBuilder sb = new StringBuilder("UPDATE solrServers SET");
        boolean first = true;
        if (server.hasUrl()) {
            first = false;
            sb.append(" serverUrl = ?");
        }
        if (server.hasSoTimeout()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(" socketTimeout = ?");
        }
        if (server.hasConnectionTimeout()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(" connectionTimeout = ?");
        }
        if (server.hasMaxConnectionsPerHost()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(" maxConnections = ?");
        }
        if (server.hasMaxIndices()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(" maxIndices = ?");
        }
        sb.append(" WHERE id = ?");

        return sb.toString();
    }

}
