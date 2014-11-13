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

package com.openexchange.mobilenotifier.events.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.MobileNotifierExceptionCodes;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.events.storage.MobileNotifierSubscriptionService;
import com.openexchange.mobilenotifier.events.storage.Subscription;
import com.openexchange.mobilenotifier.events.storage.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link MobileNotifierSubscriptionServiceImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierSubscriptionServiceImpl implements MobileNotifierSubscriptionService {
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbSubscriptionStore}.
     *
     * @throws OXException If the database service is missing
     */
    public MobileNotifierSubscriptionServiceImpl() throws OXException {
        super();
        this.databaseService = Services.getService(DatabaseService.class);
    }

    @Override
    public Subscription createSubscription(Session session, String token, String serviceId, MobileNotifierProviders providerId) throws OXException {
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, providerId, System.currentTimeMillis());
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            if (0 == replaceSubscription(connection, subscription)) {
                throw MobileNotifierExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
            }
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
        return subscription;
    }

    private static int replaceSubscription(Connection connection, Subscription subscription) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.REPLACE_OR_ADD_SUBSCRIPTION);
            stmt.setInt(1, subscription.getContextId());
            stmt.setString(2, subscription.getServiceId());
            stmt.setString(3, subscription.getToken());
            stmt.setString(4, subscription.getProviderName());
            stmt.setInt(5, subscription.getUserId());
            stmt.setLong(6, subscription.getTimestamp());
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean updateToken(Session session, String token, String serviceId, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, null, System.currentTimeMillis());
        try {
            return 0 < updateSubscription(connection, subscription, newToken);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    private static int updateSubscription(Connection connection, Subscription subscription, String newToken) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.UPDATE_TOKEN);
            stmt.setString(1, newToken);
            stmt.setLong(2, subscription.getTimestamp());
            stmt.setInt(3, subscription.getContextId());
            stmt.setInt(4, subscription.getUserId());
            stmt.setString(5, subscription.getServiceId());
            stmt.setString(6, subscription.getToken());
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteSubscription(Session session, String token, String serviceId, MobileNotifierProviders providerId) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, providerId, System.currentTimeMillis());
        try {
            return 0 < deleteSubscription(connection, subscription);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    @Override
    public boolean deleteSubscriptions(Session session, String token, String serviceId) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, null, System.currentTimeMillis());
        try {
            return 0 < deleteSubscriptions(connection, subscription);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
    }

    private static int deleteSubscriptions(Connection connection, Subscription subscription) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.DELETE_TOKEN_BY_SERVICE_ID);
            stmt.setInt(1, subscription.getContextId());
            stmt.setInt(2, subscription.getUserId());
            stmt.setString(3, subscription.getServiceId());
            stmt.setString(4, subscription.getToken());
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteSubscription(Connection connection, Subscription subscription) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.DELETE_TOKEN_BY_PROVIDER);
            stmt.setInt(1, subscription.getContextId());
            stmt.setInt(2, subscription.getUserId());
            stmt.setString(3, subscription.getServiceId());
            stmt.setString(4, subscription.getProviderName());
            stmt.setString(5, subscription.getToken());
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<Subscription> getSubscription(Session session, String serviceId, MobileNotifierProviders provider) throws OXException {
        Connection connection = databaseService.getReadOnly(session.getContextId());
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        try {
            subscriptions.addAll(selectSubscriptions(connection, session.getContextId(), session.getUserId(), serviceId, provider.getProviderName()));
        } catch (SQLException e) {
            if ("42S02".equals(e.getSQLState())) {
                // "Table 'mobileEventSubscriptions' doesn't exist" => no update task for tables in this schema yet, so ignore
            } else {
                throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
        return subscriptions;
    }

    private static List<Subscription> selectSubscriptions(Connection connection, int contextId, int user, String service, String providerName) throws SQLException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = connection.prepareStatement(Statements.SELECT_SUBSCRIPTIONS);
            stmt.setInt(++index, contextId);
            stmt.setString(++index, service);
            stmt.setString(++index, providerName);
            stmt.setInt(++index, user);
            ResultSet results = stmt.executeQuery();
            while(results.next()) {
                int cid = results.getInt(1);
                String retService = results.getString(2);
                String token = results.getString(3);
                String provider = results.getString(4);
                int userId = results.getInt(5);
                long timestamp = results.getLong(6);
                subscriptions.add(new Subscription(cid, userId, token, retService, MobileNotifierProviders.parseProviderFromParam(provider), timestamp));
            }
            return subscriptions;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
}