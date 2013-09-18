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

package com.openexchange.drive.events.subscribe.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

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
    public Subscription subscribe(Session session, String serviceID, String token, String rootFolderID) throws OXException {
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), serviceID, token, rootFolderID, System.currentTimeMillis());
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            if (0 == replaceSubscription(connection, subscription)) {
                throw DriveExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
        return subscription;
    }

    @Override
    public boolean unsubscribe(Session session, String serviceID, String token) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            return 0 < deleteSubscription(connection, session.getContextId(), serviceID, token);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    @Override
    public boolean updateToken(Session session, String serviceID, String oldToken, String newToken) throws OXException {
        return updateToken(session.getContextId(), serviceID, oldToken, newToken);
    }

    @Override
    public boolean updateToken(int contextID, String serviceID, String oldToken, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < updateToken(connection, contextID, serviceID, oldToken, newToken);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
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
        Set<Integer> allContextIDs = new HashSet<Integer>(contextService.getAllContextIds());
        while (false == allContextIDs.isEmpty()) {
            /*
             * Delete for whole schema using connection for first context
             */
            int contextID = allContextIDs.iterator().next().intValue();
            Connection connection = databaseService.getWritable(contextID);
            try {
                removed += deleteSubscriptionsForToken(connection, serviceID, token, timestamp);
            } catch (SQLException e) {
                if ("42S02".equals(e.getSQLState())) {
                    // "Table 'driveEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                } else {
                    throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
                }
            } finally {
                databaseService.backWritable(contextID, connection);
            }
            /*
             * Remember processed contexts
             */
            int[] contextsInSameSchema = databaseService.getContextsInSameSchema(contextID);
            for (int cid : contextsInSameSchema) {
                allContextIDs.remove(Integer.valueOf(cid));
            }
        }
        return removed;
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
            stmt.setInt(1, subscription.getContextID());
            stmt.setString(2, subscription.getServiceID());
            stmt.setString(3, subscription.getToken());
            stmt.setInt(4, subscription.getUserID());
            stmt.setString(5, SQL.escape(subscription.getRootFolderID()));
            stmt.setLong(6, subscription.getTimestamp());
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
            DBUtils.closeSQLStuff(stmt);
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
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<Subscription> selectSubscriptions(Connection connection, int cid, String[] services, Collection<String> rootFolderIDs) throws SQLException, OXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SUBSCRIPTIONS_STMT(services, rootFolderIDs));
            stmt.setInt(1, cid);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                String service = resultSet.getString(1);
                String token = resultSet.getString(2);
                int user = resultSet.getInt(3);
                String folder = SQL.unescape(resultSet.getString(4));
                long timestamp = resultSet.getLong(5);
                subscriptions.add(new Subscription(cid, user, service, token, folder, timestamp));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
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
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
