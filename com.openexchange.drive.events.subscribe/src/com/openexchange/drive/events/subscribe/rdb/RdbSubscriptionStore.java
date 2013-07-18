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
import java.util.List;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
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
            newUid(), session.getContextId(), session.getUserId(), serviceID, token, rootFolderID);
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            if (0 == insertSubscription(connection, subscription)) {
                // already exists?
                String uuid = selectSubscriptionUuid(connection, session.getContextId(), session.getUserId(), serviceID, token, rootFolderID);
                if (null != uuid) {
                    subscription.setUuid(uuid);
                } else {
                    throw DriveExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
                }
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
        return subscription;
    }

    @Override
    public boolean unsubscribe(Session session, String serviceID, String token, String rootFolderID) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            return 0 < deleteSubscription(connection, session.getContextId(), session.getUserId(), serviceID, token, rootFolderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    @Override
    public int updateToken(Session session, String serviceID, String oldToken, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            return updateToken(connection, session.getContextId(), session.getUserId(), serviceID, oldToken, newToken);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    @Override
    public List<Subscription> getSubscriptions(int contextID, String serviceID, Collection<String> rootFolderIDs) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectSubscriptions(connection, contextID, serviceID, rootFolderIDs);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private static String newUid() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    private static int insertSubscription(Connection connection, Subscription subscription) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_SUBSCRIPTION_STMT);
            stmt.setString(1, subscription.getUuid());
            stmt.setInt(2, subscription.getContextID());
            stmt.setInt(3, subscription.getUserID());
            stmt.setString(4, subscription.getServiceID());
            stmt.setString(5, subscription.getToken());
            stmt.setString(6, SQL.escape(subscription.getRootFolderID()));
            stmt.setInt(7, subscription.getContextID());
            stmt.setInt(8, subscription.getUserID());
            stmt.setString(9, subscription.getServiceID());
            stmt.setString(10, subscription.getToken());
            stmt.setString(11, SQL.escape(subscription.getRootFolderID()));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateToken(Connection connection, int cid, int user, String service, String oldToken, String newToken) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_TOKEN_STMT);
            stmt.setString(1, newToken);
            stmt.setInt(2, cid);
            stmt.setInt(3, cid);
            stmt.setString(4, service);
            stmt.setString(5, oldToken);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteSubscription(Connection connection, int cid, int user, String service, String token, String folder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTION_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setString(3, service);
            stmt.setString(4, token);
            stmt.setString(5, SQL.escape(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<Subscription> selectSubscriptions(Connection connection, int cid, String service, Collection<String> rootFolderIDs) throws SQLException, OXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SUBSCRIPTIONS_STMT(rootFolderIDs));
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                subscriptions.add(new Subscription(resultSet.getString(1), cid, resultSet.getInt(2), service, resultSet.getString(3),
                    SQL.unescape(resultSet.getString(4))));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return subscriptions;
    }

    private static String selectSubscriptionUuid(Connection connection, int cid, int user, String service, String token, String rootFolderID) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_SUBSCRIPTION_UUID_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setString(3, service);
            stmt.setString(4, token);
            stmt.setString(5, rootFolderID);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            return resultSet.next() ? resultSet.getString(1) : null;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
