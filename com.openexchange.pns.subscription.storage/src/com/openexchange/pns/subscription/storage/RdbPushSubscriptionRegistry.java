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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.pns.subscription.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushAffiliation;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionDescription;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.TransportAssociatedSubscription;
import com.openexchange.pns.TransportAssociatedSubscriptions;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbPushSubscriptionRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RdbPushSubscriptionRegistry implements PushSubscriptionRegistry {

    private final DatabaseService databaseService;
    private final ContextService contextService;

    /**
     * Initializes a new {@link RdbPushSubscriptionRegistry}.
     *
     * @param databaseService The database service to use
     * @param contextService The context service
     */
    public RdbPushSubscriptionRegistry(DatabaseService databaseService, ContextService contextService) {
        super();
        this.databaseService = databaseService;
        this.contextService = contextService;
    }

    /*-
     * "CREATE TABLE pns_subscriptions (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "user INT4 UNSIGNED NOT NULL," +
        "token VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
        "affiliation VARCHAR(32) CHARACTER SET latin1 NOT NULL," +
        "transport VARCHAR(32) CHARACTER SET latin1 NOT NULL," +
        "last_modified BIGINT(64) NOT NULL," +
        "PRIMARY KEY (cid, user, token)," +
        "INDEX `affiliationIndex` (cid, user, affiliation)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci"
     */

    @Override
    public List<PushSubscription> getSubscriptions(int userId, int contextId, PushAffiliation affiliation, String transportId) throws OXException {
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return getSubscriptions(userId, contextId, affiliation, transportId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }

    }

    /**
     * Gets all subscriptions for specified affiliation belonging to given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param affiliation The affiliation
     * @param con The connection to use
     * @return All subscriptions for specified affiliation
     * @throws OXException If subscriptions cannot be returned
     */
    public List<PushSubscription> getSubscriptions(int userId, int contextId, PushAffiliation affiliation, String transportId, Connection con) throws OXException {
        if (null == con) {
            return getSubscriptions(userId, contextId, affiliation, transportId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT token, last_modified FROM pns_subscriptions WHERE cid=? AND user=? AND affiliation=? AND transport=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, affiliation.getAffiliationName());
            stmt.setString(4, transportId);
            rs = stmt.executeQuery();

            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<PushSubscription> subscriptions = new LinkedList<PushSubscription>();
            do {
                subscriptions.add(new RdbPushSubscription(userId, contextId, transportId, rs.getString(1), affiliation, new Date(rs.getLong(3))));
            } while (rs.next());
            return subscriptions;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public TransportAssociatedSubscriptions getSubscriptions(int userId, int contextId, PushAffiliation affiliation) throws OXException {
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return getSubscriptions(userId, contextId, affiliation, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * Gets all subscriptions for specified affiliation belonging to given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param affiliation The affiliation
     * @param con The connection to use
     * @return All subscriptions for specified affiliation
     * @throws OXException If subscriptions cannot be returned
     */
    public TransportAssociatedSubscriptions getSubscriptions(int userId, int contextId, PushAffiliation affiliation, Connection con) throws OXException {
        if (null == con) {
            return getSubscriptions(userId, contextId, affiliation);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT token, transport, last_modified FROM pns_subscriptions WHERE cid=? AND user=? AND affiliation=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, affiliation.getAffiliationName());
            rs = stmt.executeQuery();

            if (false == rs.next()) {
                return RdbTransportAssociatedSubscriptions.EMPTY;
            }

            Map<String, List<PushSubscription>> map = new LinkedHashMap<>(4);
            do {
                String transportId = rs.getString(2);
                List<PushSubscription> subscriptions = map.get(transportId);
                if (null == subscriptions) {
                    subscriptions = new LinkedList<PushSubscription>();
                    map.put(transportId, subscriptions);
                }
                subscriptions.add(new RdbPushSubscription(userId, contextId, transportId, rs.getString(1), affiliation, new Date(rs.getLong(3))));
            } while (rs.next());

            List<TransportAssociatedSubscription> associatedSubscriptions = new ArrayList<TransportAssociatedSubscription>(map.size());
            for (Map.Entry<String,List<PushSubscription>> entry : map.entrySet()) {
                associatedSubscriptions.add(new RdbTransportAssociatedSubscription(entry.getKey(), entry.getValue()));
            }
            map = null;
            return new RdbTransportAssociatedSubscriptions(associatedSubscriptions);
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void registerSubscription(PushSubscriptionDescription subscription) throws OXException {
        if (null == subscription) {
            return;
        }

        int contextId = subscription.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            registerSubscription(subscription, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Registers specified subscription.
     *
     * @param subscription The subscription to register
     * @param con The connection to use
     * @throws OXException If registration fails
     */
    public void registerSubscription(PushSubscriptionDescription subscription, Connection con) throws OXException {
        if (null == con) {
            registerSubscription(subscription);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO pns_subscriptions (cid, user, token, affiliation, transport, last_modified) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, subscription.getContextId());
            stmt.setInt(2, subscription.getUserId());
            stmt.setString(3, subscription.getToken());
            stmt.setString(4, subscription.getAffiliation().getAffiliationName());
            stmt.setString(5, subscription.getTransportId());
            stmt.setLong(6, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean unregisterSubscription(PushSubscriptionDescription subscription) throws OXException {
        if (null == subscription) {
            return false;
        }

        int contextId = subscription.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            return unregisterSubscription(subscription, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Unregisters specified subscription.
     *
     * @param subscription The subscription to unregister
     * @param con The connection to use
     * @throws OXException If unregistration fails
     */
    public boolean unregisterSubscription(PushSubscriptionDescription subscription, Connection con) throws OXException {
        if (null == con) {
            return unregisterSubscription(subscription);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pns_subscriptions WHERE cid=? AND user=? AND token=?");
            stmt.setInt(1, subscription.getContextId());
            stmt.setInt(2, subscription.getUserId());
            stmt.setString(3, subscription.getToken());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public int unregisterSubscription(String token, String transportId) throws OXException {
        if (null == token || null == transportId) {
            return 0;
        }

        int removed = 0;
        Set<Integer> allContextIDs = new HashSet<Integer>(contextService.getAllContextIds());
        while (false == allContextIDs.isEmpty()) {
            /*
             * Delete for whole schema using connection for first context
             */
            int contextId = allContextIDs.iterator().next().intValue();
            Connection connection = databaseService.getWritable(contextId);
            try {
                removed += deleteSubscription(connection, contextId, token, transportId);
            } catch (SQLException e) {
                throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                databaseService.backWritable(contextId, connection);
            }
            /*
             * Remember processed contexts
             */
            int[] contextsInSameSchema = databaseService.getContextsInSameSchema(contextId);
            for (int cid : contextsInSameSchema) {
                allContextIDs.remove(Integer.valueOf(cid));
            }
        }
        return removed;
    }

    private static int deleteSubscription(Connection connection, int contextId, String token, String transportId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("DELETE FROM pns_subscriptions WHERE cid=? AND transport=? AND token=?");
            stmt.setInt(1, contextId);
            stmt.setString(2, transportId);
            stmt.setString(3, token);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean updateToken(PushSubscriptionDescription subscription, String newToken) throws OXException {
        if (null == subscription || null == newToken) {
            return false;
        }

        int contextId = subscription.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            return updateToken(subscription, newToken, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Updates specified subscription.
     *
     * @param subscription The subscription to update
     * @param newToken The new token to set
     * @param con The connection to use
     * @return <code>true</code> if such a subscription has been updated; otherwise <code>false</code> if no such subscription existed
     * @throws OXException If update fails
     */
    public boolean updateToken(PushSubscriptionDescription subscription, String newToken, Connection con) throws OXException {
        if (null == con) {
            return updateToken(subscription, newToken);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE pns_subscriptions SET token=? WHERE cid=? AND user=? AND token=?");
            stmt.setString(1, newToken);
            stmt.setInt(2, subscription.getContextId());
            stmt.setInt(3, subscription.getUserId());
            stmt.setString(4, subscription.getToken());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
