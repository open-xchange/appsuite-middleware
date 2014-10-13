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

import static com.openexchange.share.storage.internal.SQL.MAPPER;
import static com.openexchange.share.storage.internal.SQL.logExecuteQuery;
import static com.openexchange.share.storage.internal.SQL.logExecuteUpdate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
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
 * @since v7.8.0
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
            insertShare(provider.get(), new DefaultShare(share));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void storeShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            for (Share share : shares) {
                insertShare(provider.get(), new DefaultShare(share));
            }
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateShare(Share share, StorageParameters parameters) throws OXException {
        EnumSet<ShareField> updatableFields = EnumSet.allOf(ShareField.class);
        updatableFields.remove(ShareField.CONTEXT_ID);
        updatableFields.remove(ShareField.TOKEN);
        ConnectionProvider provider = getWriteProvider(share.getContextID(), parameters);
        try {
            updateShare(provider.get(), new DefaultShare(share), updatableFields.toArray(new ShareField[updatableFields.size()]));
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
    public List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ArrayList<Share>(selectSharesCreatedBy(provider.get(), contextID, createdBy));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ArrayList<Share>(selectSharesByTokens(provider.get(), contextID, tokens.toArray(new String[tokens.size()])));
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
            return new ArrayList<Share>(selectSharesByFolder(provider.get(), contextID, folder));
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
            return new ArrayList<Share>(selectSharesByItem(provider.get(), contextID, folder, item));
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
            return new ArrayList<Share>(selectSharesForContext(provider.get(), contextID));
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
            return new ArrayList<Share>(selectSharesExpiredAfter(provider.get(), contextID, expires.getTime()));
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
            return new ArrayList<Share>(selectSharesForGuest(provider.get(), contextID, guestID));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    private static int insertShare(Connection connection, DefaultShare share) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO share (").append(MAPPER.getColumns(ShareField.values())).append(") VALUES (")
            .append(MAPPER.getParameters(ShareField.values().length)).append(");")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            MAPPER.setParameters(stmt, share, ShareField.values());
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateShare(Connection connection, DefaultShare share, ShareField[] fields) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("UPDATE share SET ").append(MAPPER.getAssignments(fields)).append(' ')
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.TOKEN).getColumnLabel()).append("=?;")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            MAPPER.setParameters(stmt, share, fields);
            stmt.setInt(1 + fields.length, share.getContextID());
            stmt.setBytes(2 + fields.length, UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken())));
            return logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static DefaultShare selectShare(Connection connection, int cid, String token) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.TOKEN).getColumnLabel()).append("=?;")
        ;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setBytes(2, UUIDs.toByteArray(UUIDs.fromUnformattedString(token)));
            resultSet = logExecuteQuery(stmt);
            return resultSet.next() ? MAPPER.fromResultSet(resultSet, ShareField.values()) : null;
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static int deleteShares(Connection connection, int cid, List<String> tokens) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM share WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.TOKEN).getColumnLabel())
        ;
        if (1 == tokens.size()) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (").append(MAPPER.getParameters(tokens.size())).append(");");
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            for (int i = 0; i < tokens.size(); i++) {
                stmt.setBytes(2 + i, UUIDs.toByteArray(UUIDs.fromUnformattedString(tokens.get(i))));
            }
            return logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<DefaultShare> selectSharesCreatedBy(Connection connection, int cid, int createdBy) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setInt(2, createdBy);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private List<DefaultShare> selectSharesForContext(Connection connection, int cid) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<DefaultShare> selectSharesByFolder(Connection connection, int cid, String folder) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.FOLDER).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setString(2, folder);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<DefaultShare> selectSharesByItem(Connection connection, int cid, String folder, String item) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.FOLDER).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.ITEM).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setString(2, folder);
            stmt.setString(3, item);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<DefaultShare> selectSharesByTokens(Connection connection, int cid, String[] tokens) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.TOKEN).getColumnLabel())
        ;
        if (1 == tokens.length) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (").append(MAPPER.getParameters(tokens.length)).append(");");
        }
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            for (int i = 0; i < tokens.length; i++) {
                stmt.setBytes(2 + i, UUIDs.toByteArray(UUIDs.fromUnformattedString(tokens[i])));
            }
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<DefaultShare> selectSharesExpiredAfter(Connection connection, int cid, long expired) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.EXPIRY_DATE).getColumnLabel()).append("<? ")
            .append("AND ").append(MAPPER.get(ShareField.ITEM).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setLong(2, expired);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<DefaultShare> selectSharesForGuest(Connection connection, int cid, int guestID) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(ShareField.values())).append(" FROM share ")
            .append("WHERE ").append(MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(MAPPER.get(ShareField.GUEST_ID).getColumnLabel()).append("=?;")
        ;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setInt(2, guestID);
            resultSet = logExecuteQuery(stmt);
            return MAPPER.listFromResultSet(resultSet, ShareField.values());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private ConnectionProvider getReadProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.READ, contextId);
    }

    private ConnectionProvider getWriteProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.WRITE, contextId);
    }

}
