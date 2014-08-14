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

import static com.openexchange.share.storage.internal.SQL.logExecuteQuery;
import static com.openexchange.share.storage.internal.SQL.logExecuteUpdate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
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
import com.openexchange.share.storage.internal.ConnectionProvider.ConnectionMode;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link RdbShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class RdbShareStorage implements ShareStorage {

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
            insertShare(provider.get(), share);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateShare(Share share, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(share.getContextID(), parameters);
        try {
            updateShare(provider.get(), share);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void deleteShare(int contextID, String token, StorageParameters parameters) throws OXException {
        deleteShares(contextID, Collections.singletonList(token), parameters);
    }

    @Override
    public void deleteShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            deleteShares(provider.get(), contextID, tokens);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }
    
    @Override
    public void deleteSharesFromContext(int contextId, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextId, parameters);
        try {
            deleteSharesFromContext(provider.get(), contextId);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }
    
    @Override
    public void deleteSharesFromUser(int contextId, int userId, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextId, parameters);
        try {
            deleteSharesFromUser(provider.get(), contextId, userId);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
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

    @Override
    public List<Share> loadSharesForFolder(int contextID, String folder, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesByFolder(provider.get(), contextID, folder);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesForItem(int contextID, String folder, String item, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesByItem(provider.get(), contextID, folder, item);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }
    
    @Override
    public List<Share> loadSharesForContext(int contextID, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesForContext(provider.get(), contextID);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesExpiredAfter(provider.get(), contextID, expires.getTime());
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesForGuest(int contextID, int guestID, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return selectSharesForGuest(provider.get(), contextID, guestID);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    private static int insertShare(Connection connection, Share share) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_SHARE_STMT);
            int i = 1;
            stmt.setBytes(i++, UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken())));
            stmt.setInt(i++, share.getContextID());
            stmt.setInt(i++, share.getModule());
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
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateShare(Connection connection, Share share) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_SHARE_STMT);
            int i = 1;
            stmt.setInt(i++, share.getModule());
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
            stmt.setBytes(i++, UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken())));
            stmt.setInt(i++, share.getContextID());
            return logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static DefaultShare selectShare(Connection connection, int cid, String token) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARE_STMT);
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

    private static int deleteShares(Connection connection, int cid, List<String> tokens) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SHARES_STMT(tokens.size()));
            stmt.setInt(1, cid);
            for (int i = 0; i < tokens.size(); i++) {
                stmt.setBytes(2 + i, UUIDs.toByteArray(UUIDs.fromUnformattedString(tokens.get(i))));
            }
            return logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    

    private void deleteSharesFromContext(Connection connection, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SHARE_CONTEXT_STMT);
            stmt.setInt(1, contextId);
            logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    

    private void deleteSharesFromUser(Connection connection, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SHARE_USER_STMT);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<Share> selectSharesCreatedBy(Connection connection, int cid, int createdBy) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_CREATED_BY_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, createdBy);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
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
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }
    
    private List<Share> selectSharesForContext(Connection connection, int contextID) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_BY_CONTEXT_STMT);
            stmt.setInt(1, contextID);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(contextID);
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setModule(resultSet.getInt(2));
                share.setFolder(resultSet.getString(3));
                share.setItem(resultSet.getString(4));
                share.setCreated(new Date(resultSet.getLong(5)));
                share.setCreatedBy(resultSet.getInt(6));
                share.setLastModified(new Date(resultSet.getLong(7)));
                share.setModifiedBy(resultSet.getInt(8));
                long expires = resultSet.getLong(9);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(10));
                share.setAuthentication(resultSet.getInt(11));
                shares.add(share);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private static List<Share> selectSharesByFolder(Connection connection, int cid, String folder) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_BY_FOLDER_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, folder);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(cid);
                share.setFolder(folder);
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setModule(resultSet.getInt(2));
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
                shares.add(share);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private static List<Share> selectSharesByItem(Connection connection, int cid, String folder, String item) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_BY_ITEM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, folder);
            stmt.setString(3, item);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(cid);
                share.setFolder(folder);
                share.setItem(item);
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setModule(resultSet.getInt(2));
                share.setCreated(new Date(resultSet.getLong(3)));
                share.setCreatedBy(resultSet.getInt(4));
                share.setLastModified(new Date(resultSet.getLong(5)));
                share.setModifiedBy(resultSet.getInt(6));
                long expires = resultSet.getLong(7);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(8));
                share.setAuthentication(resultSet.getInt(9));
                shares.add(share);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private static List<Share> selectSharesExpiredAfter(Connection connection, int cid, long expired) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_EXPIRED_AFTER_STMT);
            stmt.setInt(1, cid);
            stmt.setLong(2, expired);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setContextID(cid);
                share.setModule(resultSet.getInt(2));
                share.setFolder(resultSet.getString(3));
                share.setItem(resultSet.getString(4));
                share.setCreated(new Date(resultSet.getLong(5)));
                share.setCreatedBy(resultSet.getInt(6));
                share.setLastModified(new Date(resultSet.getLong(7)));
                share.setModifiedBy(resultSet.getInt(8));
                share.setExpires(new Date(resultSet.getLong(9)));
                share.setGuest(resultSet.getInt(10));
                share.setAuthentication(resultSet.getInt(11));
                shares.add(share);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private static List<Share> selectSharesForGuest(Connection connection, int cid, int guestID) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SHARES_FOR_GUEST_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, guestID);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(cid);
                share.setGuest(guestID);
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(resultSet.getBytes(1))));
                share.setModule(resultSet.getInt(2));
                share.setFolder(resultSet.getString(3));
                share.setItem(resultSet.getString(4));
                share.setCreated(new Date(resultSet.getLong(5)));
                share.setCreatedBy(resultSet.getInt(6));
                share.setLastModified(new Date(resultSet.getLong(7)));
                share.setModifiedBy(resultSet.getInt(8));
                long expires = resultSet.getLong(9);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setAuthentication(resultSet.getInt(10));
                shares.add(share);
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

}
