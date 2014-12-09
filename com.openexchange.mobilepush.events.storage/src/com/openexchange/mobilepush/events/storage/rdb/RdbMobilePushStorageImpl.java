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

package com.openexchange.mobilepush.events.storage.rdb;

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
import com.openexchange.mobilepush.MobilePushExceptionCodes;
import com.openexchange.mobilepush.MobilePushProviders;
import com.openexchange.mobilepush.events.storage.ContextUsers;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;
import com.openexchange.mobilepush.events.storage.Subscription;
import com.openexchange.mobilepush.events.storage.UserToken;
import com.openexchange.mobilepush.events.storage.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbMobilePushStorageImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class RdbMobilePushStorageImpl implements MobilePushStorageService {
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbSubscriptionStore}.
     *
     * @throws OXException If the database service is missing
     */
    public RdbMobilePushStorageImpl() throws OXException {
        super();
        this.databaseService = Services.getService(DatabaseService.class);
    }

    @Override
    public Subscription createSubscription(Session session, String token, String serviceId, MobilePushProviders providerId) throws OXException {
        Subscription subscription = new Subscription(
            session.getContextId(), session.getUserId(), token, serviceId, providerId, System.currentTimeMillis());
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            if (0 == replaceSubscription(connection, session, subscription)) {
                throw MobilePushExceptionCodes.DB_ERROR.create("Subscription not added: " + subscription);
            }
        } catch (SQLException e) {
            throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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
    public boolean updateToken(int contextId, String token, String serviceId, String newToken) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < updateSubscriptions(connection, contextId, serviceId, token, newToken);
        } catch (SQLException e) {
            throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    private static int updateSubscriptions(Connection connection, int contextId, String serviceId, String token, String newToken) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.UPDATE_TOKENS);
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
    public boolean blockLoginPush(List<ContextUsers> contextUsers, long blockUntil) throws OXException {
        for(ContextUsers cu : contextUsers) {
            int contextId = cu.getContextId();
            Connection connection = databaseService.getWritable(contextId);
            try {
                touchBlockUntilTimestamp(connection, contextId, cu.getUserTokens(), blockUntil);
            } catch (SQLException e) {
                throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                databaseService.backWritable(contextId, connection);
            }
        }
        return false;
    }

    private static int touchBlockUntilTimestamp(Connection connection, int contextId, List<UserToken> userTokens, long blockUntil) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.UPDATE_LAST_PUSH_LOGIN_TIMESTAMP(userTokens.size()));
            int index = 0;
            //UPDATE ... SET:
            stmt.setLong(++index, System.currentTimeMillis() + blockUntil);
            //WHERE:
            stmt.setInt(++index, contextId);
            //IN(?, ?, ...)
            for(UserToken userToken : userTokens) {
                stmt.setInt(++index, userToken.getUserId());
            }
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<ContextUsers> getSubscriptions(MobilePushProviders provider, boolean isLoginPush) throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Set<Integer> allContextIDs = new HashSet<Integer>(contextService.getAllContextIds());
        List<ContextUsers> contextUser = new LinkedList<ContextUsers>();
        if(false == allContextIDs.isEmpty()) {
            for(Iterator<Integer> iter = allContextIDs.iterator(); iter.hasNext();) {
                int ctx = iter.next().intValue();
                Connection connection = databaseService.getReadOnly(ctx);
                try {
                    selectAllSubscription(connection, provider, ctx, contextUser, isLoginPush);
                } catch (SQLException e) {
                    if ("42S02".equals(e.getSQLState())) {
                        // "Table 'mobileEventSubscriptions' doesn't exist" => no update task for tables in this schema yet, so ignore
                    } else {
                        throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    }
                } finally {
                    databaseService.backReadOnly(ctx, connection);
                }
            }
        }
        return contextUser;
    }

    private void selectAllSubscription(Connection connection, MobilePushProviders provider, int currentCtx, List<ContextUsers> contextUser, boolean rejectLoginPush) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(Statements.SELECT_SUBSCRIPTIONS(rejectLoginPush));
            stmt.setString(1, provider.getProviderName());

            ResultSet results = stmt.executeQuery();
            List<UserToken> userIds = new LinkedList<UserToken>();
            int contextId = -1;

            while(results.next()) {
                contextId = results.getInt(1);

                int userId = results.getInt(2);
                String token = results.getString(3);
                userIds.add(new UserToken(userId, token));
            }
            if(false == userIds.isEmpty()) {
                contextUser.add(new ContextUsers(contextId, userIds));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteSubscription(int contextId, String token, String serviceId, MobilePushProviders providerId) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < deleteSubscription(connection, contextId, token, serviceId, providerId);
        } catch (SQLException e) {
            throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    private static int deleteSubscription(Connection connection, int contextId, String token, String serviceId, MobilePushProviders provider) throws SQLException, OXException {
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
    public boolean deleteSubscription(int contextId, String token, String serviceId) throws OXException {
        Connection connection = databaseService.getWritable(contextId);
        try {
            return 0 < deleteSubscriptions(connection, contextId, token, serviceId);
        } catch (SQLException e) {
            throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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
    public List<Subscription> getSubscriptions(int contextId, int userId, String serviceId, MobilePushProviders provider) throws OXException {
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            return selectSubscriptions(connection, contextId, userId, serviceId, provider.getProviderName());
        } catch (SQLException e) {
            throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, connection);
        }
    }

    private static List<Subscription> selectSubscriptions(Connection connection, int contextId, int userId, String service, String providerName) throws SQLException {
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = connection.prepareStatement(Statements.SELECT_SUBSCRIPTIONS);
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            stmt.setString(++index, service);
            stmt.setString(++index, providerName);
            ResultSet results = stmt.executeQuery();
            while(results.next()) {
                int cid = results.getInt(1);
                String resService = results.getString(2);
                String resToken = results.getString(3);
                String resProvider = results.getString(4);
                int resUserId = results.getInt(5);
                long resTimestamp = results.getLong(6);
                subscriptions.add(new Subscription(cid, resUserId, resToken, resService, MobilePushProviders.parseProvider(resProvider), resTimestamp));
            }
            return subscriptions;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<String> getTokens(List<ContextUsers> contextUser, String serviceId, MobilePushProviders provider) throws OXException {
        List<String> subscriptions = new LinkedList<String>();
        if(false == contextUser.isEmpty()) {
            for(ContextUsers cu : contextUser) {
                int contextId = cu.getContextId();
                Connection connection = databaseService.getReadOnly(contextId);
                try {
                    subscriptions.addAll(selectTokensFromContext(connection, contextId, cu.getUserTokens(), serviceId, provider));
                } catch (SQLException e) {
                    throw MobilePushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    databaseService.backReadOnly(contextId, connection);
                }
            }
        }
        return subscriptions;
    }

    private List<String> selectTokensFromContext(Connection connection, int contextId, List<UserToken> userTokens, String serviceId, MobilePushProviders provider) throws SQLException {
        PreparedStatement stmt = null;
        try {

            List<String> tokens = new LinkedList<String>();

            stmt = connection.prepareStatement(Statements.SELECT_TOKENS);

            for(UserToken userToken : userTokens) {
                int index = 0;
                stmt.setInt(++index, contextId);
                stmt.setInt(++index, userToken.getUserId());
                stmt.setString(++index, provider.getProviderName());

                ResultSet results = stmt.executeQuery();
                while(results.next()) {
                    tokens.add(results.getString(1));
                }
            }
            return tokens;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
}