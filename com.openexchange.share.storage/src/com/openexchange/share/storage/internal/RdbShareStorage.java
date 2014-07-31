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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.storage.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link RdbShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class RdbShareStorage implements ShareStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbShareStorage.class);

    private static final String SELECT_SHARE_STMT =
        "SELECT module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE cid=? AND token=?;"
    ;

    private static final String SELECT_SHARES_CREATED_BY_STMT =
        "SELECT token,module,folder,item,created,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE cid=? AND createdBy=?;"
    ;

    private static final String INSERT_SHARE_STMT =
        "INSERT INTO share (token,cid,module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);"
    ;

    private static final String SELECT_EXPIRED_SHARES_STMT =
        "SELECT token,cid,module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE expires IS NOT NULL AND expires > ?";
    ;


    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbShareStorage}.
     *
     * @param databaseService The database service
     */
    public RdbShareStorage(DatabaseService databaseService) {
        super();
        this.databaseService = databaseService;
    }

    @Override
    public Share loadShare(int contextID, String token, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectShare(provider.get(), contextID, token);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void storeShare(Share share, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(share.getContextID(), parameters);
        try {
            storeShare(provider.get(), share, parameters);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateShare(Share share, StorageParameters parameters) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesCreatedBy(provider.get(), contextID, createdBy);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    private static void storeShare(Connection connection, Share share, StorageParameters parameters) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(INSERT_SHARE_STMT);
            int i = 1;
            stmt.setBytes(i++, UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken())));
            stmt.setInt(i++, share.getContextID());
            stmt.setInt(i++, share.getModule().getFolderConstant());
            stmt.setString(i++, share.getFolder());
            if (share.isFolder()) {
                stmt.setNull(i++, Types.VARCHAR);
            } else {
                stmt.setString(i++, share.getItem());
            }
            stmt.setLong(i++, share.getCreated().getTime());
            stmt.setInt(i++, share.getCreatedBy());
            stmt.setLong(i++, share.getLastModified().getTime());
            stmt.setInt(i++, share.getModifiedBy());
            Date expires = share.getExpires();
            if (expires == null) {
                stmt.setNull(i++, Types.BIGINT);
            } else {
                stmt.setLong(i++, expires.getTime());
            }
            stmt.setInt(i++, share.getGuest());
            stmt.setInt(i++, share.getAuthentication().getID());
            logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static DefaultShare selectShare(Connection connection, int cid, String token) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SELECT_SHARE_STMT);
            stmt.setInt(1, cid);
            stmt.setBytes(2, UUIDs.toByteArray(UUIDs.fromUnformattedString(token)));
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setToken(token);
                share.setContextID(cid);
                share.setModule(resultSet.getInt(1));
                share.setFolder(resultSet.getString(2));
                share.setItem(resultSet.getString(3));
                share.setCreated(new Date(resultSet.getLong(4)));
                share.setCreatedBy(resultSet.getInt(5));
                share.setLastModified(new Date(resultSet.getLong(6)));
                share.setModifiedBy(resultSet.getInt(7));
                long expires = resultSet.getLong(8);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(9));
                share.setAuthentication(resultSet.getInt(10));
                return share;
            } else {
                return null;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<Share> selectSharesCreatedBy(Connection connection, int cid, int createdBy) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SELECT_SHARES_CREATED_BY_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, createdBy);
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(cid);
                share.setCreatedBy(createdBy);
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setModule(resultSet.getInt(2));
                share.setFolder(resultSet.getString(3));
                share.setItem(resultSet.getString(4));
                share.setCreated(new Date(resultSet.getLong(5)));
                share.setLastModified(new Date(resultSet.getLong(6)));
                share.setModifiedBy(resultSet.getInt(7));
                long expires = resultSet.getLong(8);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(9));
                share.setAuthentication(resultSet.getInt(10));
                shares.add(share);
            } else {
                return null;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private ConnectionProvider getReadProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.READ, contextId);
    }

    private ConnectionProvider getWriteProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.WRITE, contextId);
    }

    private static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    private static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        }
    }

    private static enum ConnectionMode {
        READ, WRITE;
    }

    private static final class ConnectionProvider {

        private final Connection connection;

        private final ConnectionMode mode;

        private final boolean external;

        private final DatabaseService dbService;

        private final int contextId;

        private ConnectionProvider(DatabaseService dbService, StorageParameters parameters, ConnectionMode mode, int contextId) throws OXException {
            super();
            Connection connection = null;
            if (parameters != null) {
                connection = parameters.get(Connection.class.getName());
            }

            boolean external = true;
            if (connection == null) {
                external = false;
                if (mode == ConnectionMode.READ) {
                    connection = dbService.getReadOnly(contextId);
                } else {
                    connection = dbService.getWritable(contextId);
                }
            } else {
                try {
                    if (mode == ConnectionMode.WRITE && connection.isReadOnly()) {
                        external = false;
                        connection = dbService.getWritable(contextId);
                    }
                } catch (SQLException e) {
                    throw new OXException(e); // TODO:
                }
            }

            this.dbService = dbService;
            this.connection = connection;
            this.external = external;
            this.contextId = contextId;
            this.mode = mode;
        }

        Connection get() {
            return connection;
        }

        void close() {
            if (!external) {
                if (mode == ConnectionMode.READ) {
                    dbService.backReadOnly(contextId, connection);
                } else {
                    dbService.backWritable(contextId, connection);
                }
            }
        }

    }

}
