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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.MobileNotifierExceptionCodes;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.events.storage.ContextUsers;
import com.openexchange.mobilenotifier.events.storage.MobileNotifierStorageService;
import com.openexchange.mobilenotifier.events.storage.Subscription;
import com.openexchange.mobilenotifier.events.storage.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbMobileNotifierStorageImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class RdbMobileNotifierStorageImpl implements MobileNotifierStorageService {
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbSubscriptionStore}.
     *
     * @throws OXException If the database service is missing
     */
    public RdbMobileNotifierStorageImpl() throws OXException {
        super();
        this.databaseService = Services.getService(DatabaseService.class);
    }

    @Override
    public Subscription createSubscription(Session session, String token, String serviceId, MobileNotifierProviders providerId) throws OXException {
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, providerId, System.currentTimeMillis());
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            if (0 == replaceSubscription(connection, session, subscription)) {
                throw MobileNotifierExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
            }
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(session.getContextId(), connection);
        }
        return subscription;
    }

    private static int replaceSubscription(Connection connection, Session session, Subscription subscription) throws SQLException, OXException {
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
        return updateToken(session.getContextId(), token, serviceId, newToken);
    }

    @Override
    public boolean updateToken(int contextId, String token, String serviceId, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < updateSubscription(connection, contextId, serviceId, token, newToken);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    private static int updateSubscription(Connection connection, int contextId, String serviceId, String token, String newToken) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.UPDATE_TOKEN);
            //UPDATE ... SET:
            stmt.setString(1, newToken);
            stmt.setLong(2, System.currentTimeMillis());
            //WHERE:
            stmt.setInt(3, contextId);
            stmt.setString(4, serviceId);
            stmt.setString(5, token);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<ContextUsers> getAllSubscriptions(MobileNotifierProviders provider) throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Set<Integer> allContextIDs = new HashSet<Integer>(contextService.getAllContextIds());
        List<ContextUsers> contextUser = new LinkedList<ContextUsers>();
        if(false == allContextIDs.isEmpty()) {
            for(Iterator<Integer> iter = allContextIDs.iterator(); iter.hasNext();) {
                int ctx = iter.next().intValue();
                Connection connection = databaseService.getReadOnly(ctx);
                try {
                    selectAllSubscription(connection, provider, ctx, contextUser);
                } catch (SQLException e) {
                    if ("42S02".equals(e.getSQLState())) {
                        // "Table 'mobileEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                    } else {
                        throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    }
                } finally {
                    databaseService.backReadOnly(ctx, connection);
                }
            }
        }
        return contextUser;
    }

    private void selectAllSubscription(Connection connection, MobileNotifierProviders provider, int currentCtx, List<ContextUsers> contextUser) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.SELECT_ALL_SUBSCRIPTIONS);
            stmt.setString(1, provider.getProviderName());

            ResultSet results = stmt.executeQuery();
            List<Integer> userIds = new LinkedList<Integer>();
            while(results.next()) {
                int userId = results.getInt(1);
                userIds.add(userId);
            }
            if(false == userIds.isEmpty()) {
                contextUser.add(new ContextUsers(currentCtx, userIds));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteSubscription(Session session, String token, String serviceId, MobileNotifierProviders provider) throws OXException {
        return deleteSubscription(session.getContextId(), token, serviceId, provider);
    }

    @Override
    public boolean deleteSubscription(int contextId, String token, String serviceId, MobileNotifierProviders providerId) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < deleteSubscription(connection, contextId, token, serviceId, providerId);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    private static int deleteSubscription(Connection connection, int contextId, String token, String serviceId, MobileNotifierProviders provider) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.DELETE_TOKEN_BY_PROVIDER);
            stmt.setInt(1, contextId);
            stmt.setString(2, serviceId);
            stmt.setString(3, provider.getProviderName());
            stmt.setString(4, token);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteSubscriptions(Session session, String token, String serviceId) throws OXException {
        return deleteSubscriptions(session.getContextId(), token, serviceId);
    }

    @Override
    public boolean deleteSubscriptions(int contextId, String token, String serviceId) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < deleteSubscriptions(connection, contextId, token, serviceId);
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    private static int deleteSubscriptions(Connection connection, int contextId, String token, String serviceId) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.DELETE_TOKEN_BY_SERVICE_ID);
            stmt.setInt(1, contextId);
            stmt.setString(2, serviceId);
            stmt.setString(3, token);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<Subscription> getSubscription(Session session, String serviceId, MobileNotifierProviders provider) throws OXException {
        int contextId = session.getContextId();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            return selectSubscriptions(connection, session, serviceId, provider.getProviderName());
        } catch (SQLException e) {
            throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, connection);
        }
    }

    private static List<Subscription> selectSubscriptions(Connection connection, Session session, String service, String providerName) throws SQLException {
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = connection.prepareStatement(Statements.SELECT_SUBSCRIPTIONS);
            stmt.setInt(++index, session.getContextId());
            stmt.setInt(++index, session.getUserId());
            stmt.setString(++index, service);
            stmt.setString(++index, providerName);
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

    @Override
    public List<Subscription> getTokensFromSubscriptions(List<ContextUsers> contextUser, String serviceId, MobileNotifierProviders provider) throws OXException {
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        if(false == contextUser.isEmpty()) {
            for(ContextUsers cu : contextUser) {
                int contextId = cu.getContextId();
                Connection connection = databaseService.getReadOnly(contextId);
                try {
                    subscriptions.addAll(selectTokensFromContext(connection, contextId, cu.getUserIds(), serviceId, provider));
                } catch (SQLException e) {
                    throw MobileNotifierExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    databaseService.backReadOnly(contextId, connection);
                }
            }
        }
        return subscriptions;
    }

    private List<Subscription> selectTokensFromContext(Connection connection, int contextId, List<Integer> userIds, String serviceId, MobileNotifierProviders provider) throws SQLException {
        PreparedStatement stmt = null;
        try {

            List<Subscription> subscriptions = new LinkedList<Subscription>();

            stmt = connection.prepareStatement(Statements.SELECT_TOKENS);

            for(Integer userId : userIds) {
                int index = 0;
                stmt.setInt(++index, contextId);
                stmt.setInt(++index, userId);
                stmt.setString(++index, provider.getProviderName());

                ResultSet results = stmt.executeQuery();
                while(results.next()) {
                    String token = results.getString(1);
                    long timestamp = results.getLong(2);
                    subscriptions.add(new Subscription(contextId, userId, token, serviceId, provider, timestamp));
                }
            }
            return subscriptions;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
}