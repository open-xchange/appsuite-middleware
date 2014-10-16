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

import static com.openexchange.share.storage.internal.SQL.SHARE_MAPPER;
import static com.openexchange.share.storage.internal.SQL.TARGET_MAPPER;
import static com.openexchange.share.storage.internal.SQL.logExecuteBatch;
import static com.openexchange.share.storage.internal.SQL.logExecuteQuery;
import static com.openexchange.share.storage.internal.SQL.logExecuteUpdate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.share.storage.internal.ConnectionProvider.ConnectionMode;
import com.openexchange.share.storage.mapping.ShareField;
import com.openexchange.share.storage.mapping.ShareTargetField;
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
    public void storeShare(Share share, StorageParameters parameters) throws OXException {
        storeShares(share.getContextID(), Collections.singletonList(share), parameters);
    }

    @Override
    public void storeShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        /*
         * prepare shares and share-targets
         */
        List<RdbShareTarget> targetsToInsert = new ArrayList<RdbShareTarget>();
        List<DefaultShare> sharesToInsert = new ArrayList<DefaultShare>();
        for (Share share : shares) {
            sharesToInsert.add(new DefaultShare(share));
            for (ShareTarget target : share.getTargets()) {
                RdbShareTarget rdbShareTarget = new RdbShareTarget(target);
                rdbShareTarget.setContextID(contextID);
                rdbShareTarget.setToken(share.getToken());
                rdbShareTarget.setUuid(UUIDs.toByteArray(UUID.randomUUID()));
                targetsToInsert.add(rdbShareTarget);
            }
        }
        /*
         * insert them
         */
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            insertShares(provider.get(), sharesToInsert);
            insertShareTargets(provider.get(), targetsToInsert);
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
            deleteShareTargets(provider.get(), contextID, tokens);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public Share loadShare(int contextID, String token, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            Collection<DefaultShare> shares = selectShareAndTargets(provider.get(), contextID, new String[] { token }, 0, 0);
            return null != shares && 0 < shares.size() ? shares.iterator().next() : null;
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateShare(Share share, StorageParameters parameters) throws OXException {
        updateShares(share.getContextID(), Collections.singletonList(share), parameters);
    }

    @Override
    public void updateShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        EnumSet<ShareField> updatableFields = EnumSet.allOf(ShareField.class);
        updatableFields.remove(ShareField.CONTEXT_ID);
        updatableFields.remove(ShareField.TOKEN);
        /*
         * prepare shares and share-targets
         */
        List<String> affectedTokens = new ArrayList<String>(shares.size());
        List<RdbShareTarget> targetsToInsert = new ArrayList<RdbShareTarget>();
        List<DefaultShare> sharesToUpdate = new ArrayList<DefaultShare>();
        for (Share share : shares) {
            affectedTokens.add(share.getToken());
            sharesToUpdate.add(new DefaultShare(share));
            for (ShareTarget target : share.getTargets()) {
                RdbShareTarget rdbShareTarget = new RdbShareTarget(target);
                rdbShareTarget.setContextID(contextID);
                rdbShareTarget.setToken(share.getToken());
                rdbShareTarget.setUuid(UUIDs.toByteArray(UUID.randomUUID()));
                targetsToInsert.add(rdbShareTarget);
            }
        }
        /*
         * update them
         */
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            deleteShareTargets(provider.get(), contextID, affectedTokens);
            updateShares(provider.get(), sharesToUpdate, updatableFields.toArray(new ShareField[updatableFields.size()]));
            insertShareTargets(provider.get(), targetsToInsert);
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
            return new ArrayList<Share>(selectShareAndTargets(provider.get(), contextID, null, createdBy, 0));
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
            return new ArrayList<Share>(selectShareAndTargets(provider.get(), contextID, tokens.toArray(new String[tokens.size()]), 0, 0));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesForFolder(int contextID, String folder, StorageParameters parameters) throws OXException {
        // TODO
        return null;
    }

    @Override
    public List<Share> loadSharesForItem(int contextID, String folder, String item, StorageParameters parameters) throws OXException {
        // TODO
        return null;
    }

    @Override
    public List<Share> loadSharesForContext(int contextID, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ArrayList<Share>(selectShareAndTargets(provider.get(), contextID, null, 0, 0));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException {
        // TODO
        return null;
    }

    @Override
    public List<Share> loadSharesForGuest(int contextID, int guestID, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ArrayList<Share>(selectShareAndTargets(provider.get(), contextID, null, 0, guestID));
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    private static int[] insertShares(Connection connection, List<DefaultShare> shares) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO share (").append(SHARE_MAPPER.getColumns(ShareField.values())).append(") VALUES (")
            .append(SHARE_MAPPER.getParameters(ShareField.values().length)).append(");")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            for (DefaultShare share : shares) {
                SHARE_MAPPER.setParameters(stmt, share, ShareField.values());
                stmt.addBatch();
            }
            return SQL.logExecuteBatch(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int[] insertShareTargets(Connection connection, List<RdbShareTarget> targets) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO share_target (").append(TARGET_MAPPER.getColumns(ShareTargetField.values())).append(") VALUES (")
            .append(TARGET_MAPPER.getParameters(ShareTargetField.values().length)).append(");")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            for (RdbShareTarget target : targets) {
                TARGET_MAPPER.setParameters(stmt, target, ShareTargetField.values());
                stmt.addBatch();
            }
            return SQL.logExecuteBatch(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteShares(Connection connection, int cid, List<String> tokens) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM share WHERE ").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel())
        ;
        if (1 == tokens.size()) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (").append(SHARE_MAPPER.getParameters(tokens.size())).append(");");
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

    private static int deleteShareTargets(Connection connection, int cid, List<String> tokens) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM share_target WHERE ").append(TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel())
            .append("=? ").append("AND ").append(TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel())
        ;
        if (1 == tokens.size()) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (").append(TARGET_MAPPER.getParameters(tokens.size())).append(");");
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

    private static int[] updateShares(Connection connection, List<DefaultShare> shares, ShareField[] fields) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("UPDATE share SET ").append(SHARE_MAPPER.getAssignments(fields)).append(' ')
            .append("WHERE ").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel()).append("=?;")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            for (DefaultShare share : shares) {
                SHARE_MAPPER.setParameters(stmt, share, ShareField.values());
                stmt.setInt(1 + fields.length, share.getContextID());
                stmt.setBytes(2 + fields.length, UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken())));
                stmt.addBatch();
            }
            return logExecuteBatch(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * Selects shares and their corresponding targets from the database.
     *
     * @param connection A readable db connection
     * @param cid the context ID
     * @param tokens The tokens of the shares to retrieve, or <code>null</code> to not filter by token
     * @param createdBy The ID of the user who created the shares, or <code>0</code> to not filter by the creating user
     * @param guest The ID of the guest assigned to the shares, or <code>0</code> to not filter by the guest user
     * @return The shares
     * @throws SQLException
     * @throws OXException
     */
    private static Collection<DefaultShare> selectShareAndTargets(Connection connection, int cid, String[] tokens, int createdBy, int guest) throws SQLException, OXException {
        /*
         * build statement
         */
        ShareField[] shareFields = { ShareField.TOKEN, ShareField.CREATION_DATE, ShareField.CREATED_BY, ShareField.LAST_MODIFIED,
            ShareField.MODIFIED_BY, ShareField.GUEST_ID, ShareField.AUTHENTICATION };
        ShareTargetField[] targetFields = { ShareTargetField.MODULE, ShareTargetField.FOLDER, ShareTargetField.ITEM,
            ShareTargetField.ACTIVATION_DATE, ShareTargetField.EXPIRY_DATE, ShareTargetField.META };
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(SHARE_MAPPER.getColumns(shareFields, "s")).append(',')
            .append(TARGET_MAPPER.getColumns(targetFields, "t"))
            .append(" FROM share AS s LEFT JOIN share_target AS t")
            .append(" ON s.").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel())
            .append("=t.").append(TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel())
            .append(" AND s.").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel())
            .append("=t.").append(TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel())
            .append(" WHERE s.").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=?")
        ;
        if (null != tokens && 0 < tokens.length) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel());
            if (1 == tokens.length) {
                stringBuilder.append("=?");
            } else {
                stringBuilder.append(" IN (?");
                for (int i = 1; i < tokens.length; i++) {
                    stringBuilder.append(",?");
                }
                stringBuilder.append(')');
            }
        }
        if (0 < createdBy) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?");
        }
        if (0 < guest) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.GUEST_ID).getColumnLabel()).append("=?");
        }
        Map<String, DefaultShare> sharesByToken = new HashMap<String, DefaultShare>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            if (null != tokens) {
                for (String token : tokens) {
                    stmt.setBytes(parameterIndex++, UUIDs.toByteArray(UUIDs.fromUnformattedString(token)));
                }
            }
            if (0 < createdBy) {
                stmt.setInt(parameterIndex++, createdBy);
            }
            if (0 < guest) {
                stmt.setInt(parameterIndex++, guest);
            }
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare currentShare = SHARE_MAPPER.fromResultSet(resultSet, shareFields, "s.");
                DefaultShare share = sharesByToken.get(currentShare.getToken());
                if (null == share) {
                    share = currentShare;
                    share.setTargets(new ArrayList<ShareTarget>());
                    share.setContextID(cid);
                }
                RdbShareTarget target = TARGET_MAPPER.fromResultSet(resultSet, targetFields, "t.");
                if (0 < target.getModule()) {
                    share.getTargets().add(target);
                }
            }
            return sharesByToken.values();
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
