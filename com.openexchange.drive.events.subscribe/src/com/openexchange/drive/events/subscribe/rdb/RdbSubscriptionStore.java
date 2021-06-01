/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.events.subscribe.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.Session;

/**
 * {@link RdbSubscriptionStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbSubscriptionStore implements DriveSubscriptionStore {

    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbSubscriptionStore}.
     *
     * @throws OXException If the database service is missing
     */
    public RdbSubscriptionStore() throws OXException {
        super();
        this.databaseService = SubscribeServiceLookup.getService(DatabaseService.class, true);
    }

    @Override
    public List<Subscription> subscribe(Session session, String serviceID, String token, List<String> rootFolderIDs, SubscriptionMode mode) throws OXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>(rootFolderIDs.size());
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            for (String rootFolderID : rootFolderIDs) {
                Subscription subscription = new Subscription(UUIDs.getUnformattedStringFromRandom(),
                    session.getContextId(), session.getUserId(), serviceID, token, rootFolderID, mode, System.currentTimeMillis());
                deleteSubscription(connection, session.getContextId(), serviceID, token, rootFolderID);
                if (0 == replaceSubscription(connection, subscription)) {
                    throw DriveExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
                }
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
        return subscriptions;
    }

    @Override
    public Subscription subscribe(Session session, String serviceID, String token, String rootFolderID, SubscriptionMode mode) throws OXException {
        List<Subscription> subscriptions = subscribe(session, serviceID, token, Collections.singletonList(rootFolderID), mode);
        return null != subscriptions && 0 < subscriptions.size() ? subscriptions.get(0) : null;
    }

    @Override
    public boolean unsubscribe(Session session, String serviceID, String token, List<String> rootFolderIDs) throws OXException {
        int deleted = 0;
        int contextID = session.getContextId();
        Connection connection = databaseService.getWritable(contextID);
        try {
            if (null == rootFolderIDs) {
                deleted = deleteSubscription(connection, contextID, serviceID, token);
            } else {
                for (String rootFolderID : rootFolderIDs) {
                    deleted += deleteSubscription(connection, contextID, serviceID, token, rootFolderID);
                }
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (0 < deleted) {
                databaseService.backWritable(contextID, connection);
            } else {
                databaseService.backWritableAfterReading(contextID, connection);
            }
        }
        return 0 <deleted;
    }

    @Override
    public boolean unsubscribe(Session session, String serviceID, String token) throws OXException {
        return unsubscribe(session, serviceID, token, null);
    }

    @Override
    public boolean updateToken(Session session, String serviceID, String oldToken, String newToken) throws OXException {
        return updateToken(session.getContextId(), serviceID, oldToken, newToken);
    }

    @Override
    public boolean updateToken(int contextID, String serviceID, String oldToken, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        int updated = 0;
        try {
            if (null == serviceID) {
                // workaround for bug #33652
                if (existsToken(connection, contextID, newToken)) {
                    throw DriveExceptionCodes.TOKEN_ALREADY_REGISTERED.create(newToken);
                }
                updated = updateToken(connection, contextID, oldToken, newToken);
            } else {
                if (existsToken(connection, contextID, serviceID, newToken)) {
                    throw DriveExceptionCodes.TOKEN_ALREADY_REGISTERED.create(newToken);
                }
                updated = updateToken(connection, contextID, serviceID, oldToken, newToken);
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (0 < updated) {
                databaseService.backWritable(contextID, connection);
            } else {
                databaseService.backWritableAfterReading(contextID, connection);
            }
        }
        return 0 < updated;
    }

    @Override
    public List<Subscription> getSubscriptions(int contextID, String[] serviceIDs, Collection<String> rootFolderIDs) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectSubscriptions(connection, contextID, serviceIDs, rootFolderIDs);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public int removeSubscriptions(String serviceID, String token, long timestamp) throws OXException {
        int removed = 0;
        ContextService contextService = SubscribeServiceLookup.getService(ContextService.class, true);
        for (Integer contextID : contextService.getDistinctContextsPerSchema()) {
            /*
             * Delete for whole schema using connection for representative context
             */
            int cid = contextID.intValue();
            Connection connection = databaseService.getWritable(cid);
            try {
                removed += deleteSubscriptionsForToken(connection, serviceID, token, timestamp);
            } catch (SQLException e) {
                if ("42S02".equals(e.getSQLState())) {
                    // "Table 'driveEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                } else {
                    throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
                }
            } finally {
                databaseService.backWritable(cid, connection);
            }
        }
        return removed;
    }

    @Override
    public List<Subscription> getSubscriptions(String serviceID) throws OXException {
        String[] services = new String[] { serviceID };
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        ContextService contextService = SubscribeServiceLookup.getService(ContextService.class, true);
        for (Integer contextId : contextService.getDistinctContextsPerSchema()) {
            /*
             * Select for whole schema using connection for representative context
             */
            Connection connection = databaseService.getReadOnly(contextId.intValue());
            try {
                subscriptions.addAll(selectSubscriptions(connection, services));
            } catch (SQLException e) {
                if ("42S02".equals(e.getSQLState())) {
                    // "Table 'driveEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                } else {
                    throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
                }
            } finally {
                databaseService.backWritable(contextId.intValue(), connection);
            }
        }
        return subscriptions;
    }

    @Override
    public boolean removeSubscription(Subscription subscription) throws OXException {
        return 0 < removeSubscriptions(subscription.getContextID(), subscription.getServiceID(), subscription.getToken());
    }

    @Override
    public int removeSubscriptions(int contextID, String serviceID, String token) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return deleteSubscription(connection, contextID, serviceID, token);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    private static int replaceSubscription(Connection connection, Subscription subscription) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.REPLACE_SUBSCRIPTION_STMT);
            stmt.setString(1, subscription.getUuid());
            stmt.setInt(2, subscription.getContextID());
            stmt.setString(3, subscription.getServiceID());
            stmt.setString(4, subscription.getToken());
            stmt.setInt(5, subscription.getUserID());
            stmt.setString(6, SQL.escape(subscription.getRootFolderID()));
            stmt.setString(7, null != subscription.getMode() ? String.valueOf(subscription.getMode()).toLowerCase() : null);
            stmt.setLong(8, subscription.getTimestamp());
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static int deleteSubscription(Connection connection, int cid, String service, String token) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTION_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, token);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static int deleteSubscription(Connection connection, int cid, String service, String token, String folder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTION_FOR_FOLDER_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, token);
            stmt.setString(4, SQL.escape(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean existsToken(Connection connection, int cid, String service, String token) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SQL.EXISTS_TOKEN_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, token);
            resultSet = SQL.logExecuteQuery(stmt);
            return resultSet.next();
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
    }

    private static int updateToken(Connection connection, int cid, String service, String oldToken, String newToken) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_TOKEN_STMT);
            stmt.setString(1, newToken);
            stmt.setInt(2, cid);
            stmt.setString(3, service);
            stmt.setString(4, oldToken);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean existsToken(Connection connection, int cid, String token) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SQL.EXISTS_TOKEN_WITHOUT_SERVICE_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, token);
            resultSet = SQL.logExecuteQuery(stmt);
            return resultSet.next();
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
    }

    private static int updateToken(Connection connection, int cid, String oldToken, String newToken) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_TOKEN_WITHOUT_SERVICE_STMT);
            stmt.setString(1, newToken);
            stmt.setInt(2, cid);
            stmt.setString(3, oldToken);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static List<Subscription> selectSubscriptions(Connection connection, int cid, String[] services, Collection<String> rootFolderIDs) throws SQLException, OXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SUBSCRIPTIONS_STMT(services.length, rootFolderIDs.size()));
            int parameterIndex = 0;
            stmt.setInt(++parameterIndex, cid);
            for (String service : services) {
                stmt.setString(++parameterIndex, service);
            }
            for (String rootFolderID : rootFolderIDs) {
                stmt.setString(++parameterIndex, Strings.reverse(SQL.escape(rootFolderID)));
            }
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                String uuid = resultSet.getString(1);
                String service = resultSet.getString(2);
                String token = resultSet.getString(3);
                int user = resultSet.getInt(4);
                String folder = SQL.unescape(resultSet.getString(5));
                SubscriptionMode mode = Enums.parse(SubscriptionMode.class, resultSet.getString(6), null);
                long timestamp = resultSet.getLong(7);
                subscriptions.add(new Subscription(uuid, cid, user, service, token, folder, mode, timestamp));
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
        return subscriptions;
    }

    private static List<Subscription> selectSubscriptions(Connection connection, String[] services) throws SQLException, OXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SUBSCRIPTIONS_STMT(services.length));
            int parameterIndex = 0;
            for (String service : services) {
                stmt.setString(++parameterIndex, service);
            }
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                String uuid = resultSet.getString(1);
                int cid = resultSet.getInt(2);
                String service = resultSet.getString(3);
                String token = resultSet.getString(4);
                int user = resultSet.getInt(5);
                String folder = SQL.unescape(resultSet.getString(6));
                SubscriptionMode mode = Enums.parse(SubscriptionMode.class, resultSet.getString(7), null);
                long timestamp = resultSet.getLong(8);
                subscriptions.add(new Subscription(uuid, cid, user, service, token, folder, mode, timestamp));
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
        return subscriptions;
    }

    private static int deleteSubscriptionsForToken(Connection connection, String service, String token, long timestamp) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTIONS_FOR_TOKEN_STMT);
            stmt.setString(1, service);
            stmt.setString(2, token);
            stmt.setLong(3, timestamp);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
