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

import static com.openexchange.index.internal.IndexDatabaseStuff.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexServer;
import com.openexchange.index.IndexUrl;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link ConfigIndexMysql}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexMysql {

    private static final ConfigIndexMysql INSTANCE = new ConfigIndexMysql();

    private final Lock LOCK = new ReentrantLock();


    private ConfigIndexMysql() {
        super();
    }

    public static ConfigIndexMysql getInstance() {
        return INSTANCE;
    }


    public IndexUrl getIndexUrl(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection readCon = dbService.getReadOnly();
        try {
            return getIndexUrl(readCon, cid, uid, module);
        } finally {
            dbService.backReadOnly(readCon);
        }
    }

    public IndexUrl getIndexUrl(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_INDEX_URL);
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i, module);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw IndexExceptionCodes.INDEX_NOT_FOUND.create(Integer.valueOf(uid), Integer.valueOf(module), Integer.valueOf(cid));
            }

            i = 1;
            final int id = rs.getInt(i++);
            final String serverUrl = rs.getString(i++);
            final int maxIndices = rs.getInt(i++);
            final int socketTimeout = rs.getInt(i++);
            final int connectionTimeout = rs.getInt(i++);
            final int maxConnections = rs.getInt(i++);
            final String index = rs.getString(i);

            final IndexServerImpl server = new IndexServerImpl();
            server.setId(id);
            server.setUrl(serverUrl);
            server.setMaxIndices(maxIndices);
            server.setSoTimeout(socketTimeout);
            server.setConnectionTimeout(connectionTimeout);
            server.setMaxConnectionsPerHost(maxConnections);

            return new IndexUrlImpl(server, index);
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public int registerIndexServer(final IndexServer server) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            return registerIndexServer(con, server);
        } finally {
            dbService.backWritable(con);
        }
    }

    public int registerIndexServer(final Connection con, final IndexServer server) throws OXException {
        PreparedStatement stmt = null;
        try {
            DBUtils.startTransaction(con);
            final int id = IDGenerator.getId(con);
            stmt = con.prepareStatement(SQL_INSERT_INDEX_SERVER);
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

    public void unregisterIndexServer(final int serverId, final boolean deleteMappings) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            unregisterIndexServer(con, serverId, deleteMappings);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void unregisterIndexServer(final Connection con, final int serverId, final boolean deleteMappings) throws OXException {
        PreparedStatement sstmt = null;
        PreparedStatement mstmt = null;
        try {
            DBUtils.startTransaction(con);
            sstmt = con.prepareStatement(SQL_DELETE_INDEX_SERVER);
            sstmt.setInt(1, serverId);

            final int rows = sstmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.UNREGISTER_SERVER_ERROR.create(Integer.valueOf(serverId));
            }

            if (deleteMappings) {
                mstmt = con.prepareStatement(SQL_DELETE_INDEX_MAPPING_BY_SERVER);
                mstmt.setInt(1, serverId);
                mstmt.executeUpdate();
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

    public void modifyIndexServer(final IndexServer server) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            modifyIndexServer(con, server);
        } finally {
            dbService.backReadOnly(con);
        }
    }

    public void modifyIndexServer(final Connection con, final IndexServer server) throws OXException {
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

    public int createIndexMapping(final int cid, final int uid, final int module, final String index) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            return createIndexMapping(con, cid, uid, module, index);
        } finally {
            dbService.backWritable(con);
        }
    }

    public int createIndexMapping(final Connection con, final int cid, final int uid, final int module, final String index) throws OXException {
        PreparedStatement stmt = null;
        try {
            LOCK.lock();
            con.createStatement().execute("SELECT * FROM user_module2index FOR UPDATE");
            final int serverId = getSuitableIndexServer(con);
            stmt = con.prepareStatement(SQL_INSERT_INDEX_MAPPING);
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);
            stmt.setInt(i++, serverId);
            stmt.setString(i, index);

            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.ADD_MAPPING_ERROR.create(Integer.valueOf(uid), Integer.valueOf(cid), Integer.valueOf(module), Integer.valueOf(serverId));
            }

            return serverId;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            LOCK.unlock();
            DBUtils.closeSQLStuff(stmt);
        }
    }

    public void removeIndexMapping(final int cid, final int uid, final int module) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            removeIndexMapping(con, cid, uid, module);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void removeIndexMapping(final Connection con, final int cid, final int uid, final int module) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_INDEX_MAPPING);
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);

            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.INDEX_NOT_FOUND.create(Integer.valueOf(uid), Integer.valueOf(module), Integer.valueOf(cid));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
            stmt = con.prepareStatement(SQL_SELECT_INDEX_SERVERS);
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

    public void modifiyIndexMapping(final int cid, final int uid, final int module, final int server, final String index) throws OXException {
        final DatabaseService dbService = getDbService();
        final Connection con = dbService.getWritable();
        try {
            modifiyIndexMapping(con, cid, uid, module, server, index);
        } finally {
            dbService.backWritable(con);
        }
    }

    public void modifiyIndexMapping(final Connection con, final int cid, final int uid, final int module, final int server, final String index) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_UPDATE_INDEX_MAPPING);
            int i = 1;
            stmt.setInt(i++, server);
            stmt.setString(i++, index);
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            stmt.setInt(i++, module);

            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw IndexExceptionCodes.INDEX_NOT_FOUND.create(Integer.valueOf(uid), Integer.valueOf(module), Integer.valueOf(cid));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private int getSuitableIndexServer(final Connection con) throws OXException {
        final String sql = SQL_SELECT_SUITABLE_INDEX_SERVER;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int serverId = 0;
        int maxIndices = 0;
        int count = 0;
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int i = 1;
                serverId = rs.getInt(i++);
                maxIndices = rs.getInt(i++);
                count = rs.getInt(i++);
            } else {
                throw IndexExceptionCodes.SERVER_FULL.create();
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        if (count >= maxIndices) {
            throw IndexExceptionCodes.SERVER_FULL.create();
        }
        return serverId;
    }

    private DatabaseService getDbService() throws OXException {
        final DatabaseService dbService = IndexServiceLookup.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

    private String createServerUpdateSQL(final IndexServer server) {
        final StringBuilder sb = new StringBuilder("UPDATE ").append(TBL_IDX_SERVER).append(" SET");
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
