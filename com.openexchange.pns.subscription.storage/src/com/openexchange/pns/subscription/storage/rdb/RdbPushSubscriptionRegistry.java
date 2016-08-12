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

package com.openexchange.pns.subscription.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.ClientAndTransport;
import com.openexchange.pns.subscription.storage.MapBackedHits;
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

    @Override
    public MapBackedHits getInterestedSubscriptions(int userId, int contextId, String topic) throws OXException {
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return getSubscriptions(userId, contextId, topic, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * Gets all subscriptions interested in specified topic belonging to given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param affiliation The affiliation
     * @param con The connection to use
     * @return All subscriptions for specified affiliation
     * @throws OXException If subscriptions cannot be returned
     */
    public MapBackedHits getSubscriptions(int userId, int contextId, String topic, Connection con) throws OXException {
        if (null == con) {
            return getInterestedSubscriptions(userId, contextId, topic);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(
                "SELECT s.token, s.client, s.transport, s.last_modified, s.all_flag, twc.topic wildcard, te.topic FROM pns_subscription s" +
                " LEFT JOIN pns_subscription_topic_wildcard twc ON s.id=twc.id" +
                " LEFT JOIN pns_subscription_topic_exact te ON s.id=te.id" +
                " WHERE s.cid=? AND s.user=? AND ((s.all_flag=1) OR (te.topic=?) OR (? LIKE CONCAT(twc.topic, '%')));");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, topic);
            stmt.setString(4, topic);
            rs = stmt.executeQuery();

            if (false == rs.next()) {
                return MapBackedHits.EMPTY;
            }

            Map<ClientAndTransport, List<PushMatch>> map = new LinkedHashMap<>(6);
            do {
                String token = rs.getString(1);
                String client = rs.getString(2);
                String transportId = rs.getString(3);
                Date lastModified = new Date(rs.getLong(4));

                // Determine matching topic
                String matchingTopic;
                {
                    boolean all = rs.getInt(5) > 0;
                    if (all) {
                        matchingTopic = "*";
                    } else {
                        matchingTopic = rs.getString(6);
                        if (rs.wasNull()) {
                            // E.g. "com/open-xchange/mail/new"
                            matchingTopic = rs.getString(7);
                        } else {
                            // E.g. "com/open-xchange/mail/*"
                            matchingTopic = new StringBuilder(matchingTopic).append('*').toString();
                        }
                    }
                }

                // Add to appropriate list
                ClientAndTransport cat = new ClientAndTransport(client, transportId);
                List<PushMatch> matches = map.get(cat);
                if (null == matches) {
                    matches = new LinkedList<PushMatch>();
                    map.put(cat, matches);
                }
                matches.add(new RdbPushMatch(userId, contextId, client, transportId, token, matchingTopic, lastModified));
            } while (rs.next());

            return new MapBackedHits(map);
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /*-
     * UUIDs.toByteArray(UUID.randomUUID())
     *
     * "CREATE TABLE pns_subscription (" +
        "id BINARY(16) NOT NULL" +
        "cid INT4 UNSIGNED NOT NULL," +
        "user INT4 UNSIGNED NOT NULL," +
        "client VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
        "transport VARCHAR(32) CHARACTER SET latin1 NOT NULL," +
        "last_modified BIGINT(64) NOT NULL," +
        "PRIMARY KEY (cid, user, token)," +
        "UNIQUE KEY `subscription_id` (`id`)," +
        "INDEX `affiliationIndex` (cid, user, affiliation)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
     */

    private byte[] getSubscriptionId(int userId, int contextId, String token, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM pns_subscription WHERE cid=? AND user=? AND token=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, token);
            rs = stmt.executeQuery();
            return false == rs.next() ? null : rs.getBytes(1);
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void registerSubscription(PushSubscription subscription) throws OXException {
        if (null == subscription) {
            return;
        }

        int contextId = subscription.getContextId();
        Connection con = databaseService.getWritable(contextId);
        boolean autocommit = false;
        boolean rollback = false;
        boolean modified = false;
        try {
            byte[] id = getSubscriptionId(subscription.getUserId(), contextId, subscription.getToken(), con);
            if (null != id) {
                // Already exists...
                updateLastModified(subscription, con);
                modified = true;
                return;
            }

            Databases.startTransaction(con);
            autocommit = true;
            rollback = true;

            registerSubscription(subscription, con);
            modified = true;

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            if (autocommit) {
                Databases.autocommit(con);
            }
            if (modified) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
        }
    }

    /**
     * Registers specified subscription.
     *
     * @param subscription The subscription to register
     * @param con The connection to use
     * @return <code>true</code> if subscription was inserted; otherwise <code>false</code>
     * @throws OXException If registration fails
     */
    public void registerSubscription(PushSubscription subscription, Connection con) throws OXException {
        if (null == con) {
            registerSubscription(subscription);
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<String> prefixes = null;
            List<String> topics = null;
            boolean isAll = false;
            for (Iterator<String> iter = subscription.getTopics().iterator(); !isAll && iter.hasNext();) {
                String topic = iter.next();
                if ("*".equals(topic)) {
                    isAll = true;
                } else {
                    try {
                        PushNotifications.validateTopicName(topic);
                    } catch (IllegalArgumentException e) {
                        throw PushExceptionCodes.INVALID_TOPIC.create(e, topic);
                    }
                    if (topic.endsWith(":*")) {
                        // Wild-card topic: we remove the *
                        if (null == prefixes) {
                            prefixes = new LinkedList<>();
                        }
                        prefixes.add(topic.substring(0, topic.length() - 1));
                    } else {
                        // Exact match
                        if (null == topics) {
                            topics = new LinkedList<>();
                        }
                        topics.add(topic);
                    }
                }
            }

            // Generate random UUID
            byte[] id = UUIDs.toByteArray(UUID.randomUUID());

            // Insert into pns_subscription
            stmt = con.prepareStatement("INSERT INTO pns_subscription (id, cid, user, token, client, transport, all_flag, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setBytes(1, id);
            stmt.setInt(2, subscription.getContextId());
            stmt.setInt(3, subscription.getUserId());
            stmt.setString(4, subscription.getToken());
            stmt.setString(5, subscription.getClient());
            stmt.setString(6, subscription.getTransportId());
            stmt.setInt(7, isAll ? 1 : 0);
            stmt.setLong(8, System.currentTimeMillis());
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            // Insert individual topics / topic wild-cards (if subscription is not interested in all)
            if (!isAll) {
                if (null != prefixes) {
                    stmt = con.prepareStatement("INSERT INTO pns_subscription_topic_wildcard (id, cid, topic) VALUES (?, ?, ?)");
                    stmt.setBytes(1, id);
                    stmt.setInt(2, subscription.getContextId());
                    for (String prefix : prefixes) {
                        stmt.setString(3, prefix);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                if (null != topics) {
                    stmt = con.prepareStatement("INSERT INTO pns_subscription_topic_exact (id, cid, topic) VALUES (?, ?, ?)");
                    stmt.setBytes(1, id);
                    stmt.setInt(2, subscription.getContextId());
                    for (String topic : topics) {
                        stmt.setString(3, topic);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean unregisterSubscription(PushSubscription subscription) throws OXException {
        if (null == subscription) {
            return false;
        }

        int contextId = subscription.getContextId();
        Connection con = databaseService.getWritable(contextId);
        boolean rollback = false;
        boolean deleted = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            deleted = unregisterSubscription(subscription, con);

            con.commit();
            rollback = false;

            return deleted;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            if (deleted) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
        }
    }

    /**
     * Unregisters specified subscription.
     *
     * @param subscription The subscription to unregister
     * @param con The connection to use
     * @throws OXException If unregistration fails
     */
    public boolean unregisterSubscription(PushSubscription subscription, Connection con) throws OXException {
        if (null == con) {
            return unregisterSubscription(subscription);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM pns_subscription WHERE cid=? AND user=? AND token=?");
            stmt.setInt(1, subscription.getContextId());
            stmt.setInt(2, subscription.getUserId());
            stmt.setString(3, subscription.getToken());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return false;
            }

            byte[] id = rs.getBytes(1);
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            return deleteById(id, con);
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Deletes the subscription identified by given ID.
     *
     * @param id The ID
     * @param con The connection to use
     * @return <code>true</code> if such a subscription was deleted; otherwise <code>false</code>
     * @throws OXException If delete attempt fails
     */
    public static boolean deleteById(byte[] id, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pns_subscription_topic_exact WHERE id=?");
            stmt.setBytes(1, id);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM pns_subscription_topic_wildcard WHERE id=?");
            stmt.setBytes(1, id);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);

            stmt = con.prepareStatement("DELETE FROM pns_subscription WHERE id=?");
            stmt.setBytes(1, id);
            int rows = stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

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
            // Delete for whole schema using connection for first context
            int contextId = allContextIDs.iterator().next().intValue();

            Connection con = databaseService.getWritable(contextId);
            boolean startedTransaction = false;
            boolean rollback = false;
            boolean modified = false;
            try {
                List<byte[]> ids = getSubscriptionIds(contextId, token, transportId, con);

                if (false == ids.isEmpty()) {
                    Databases.startTransaction(con);
                    startedTransaction = true;
                    rollback = true;

                    int numDeleted = deleteSubscription(ids, con);
                    modified = numDeleted > 0;
                    removed += numDeleted;

                    con.commit();
                    rollback = false;
                }
            } catch (SQLException e) {
                throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (rollback) {
                    Databases.rollback(con);
                }
                if (startedTransaction) {
                    Databases.autocommit(con);
                }
                if (modified) {
                    databaseService.backWritable(contextId, con);
                } else {
                    databaseService.backWritableAfterReading(contextId, con);
                }
            }

            // Remember processed contexts
            int[] contextsInSameSchema = databaseService.getContextsInSameSchema(contextId);
            for (int cid : contextsInSameSchema) {
                allContextIDs.remove(Integer.valueOf(cid));
            }
        }
        return removed;
    }

    private List<byte[]> getSubscriptionIds(int contextId, String token, String transportId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM pns_subscriptions WHERE cid=? AND transport=? AND token=?");
            stmt.setInt(1, contextId);
            stmt.setString(2, transportId);
            stmt.setString(3, token);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<byte[]> ids = new LinkedList<>();
            do {
                ids.add(rs.getBytes(1));
            } while (rs.next());
            return ids;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private int deleteSubscription(List<byte[]> ids, Connection con) throws OXException {
        int deleted = 0;
        for (byte[] id : ids) {
            if (deleteById(id, con)) {
                deleted++;
            }
        }
        return deleted;
    }

    @Override
    public boolean updateToken(PushSubscription subscription, String newToken) throws OXException {
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
     * Updates specified subscription's token (and last-modified time stamp).
     *
     * @param subscription The subscription to update
     * @param newToken The new token to set
     * @param con The connection to use
     * @return <code>true</code> if such a subscription has been updated; otherwise <code>false</code> if no such subscription existed
     * @throws OXException If update fails
     */
    public boolean updateToken(PushSubscription subscription, String newToken, Connection con) throws OXException {
        if (null == con) {
            return updateToken(subscription, newToken);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE pns_subscriptions SET token=?, last_modified=? WHERE cid=? AND user=? AND token=?");
            stmt.setString(1, newToken);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setInt(3, subscription.getContextId());
            stmt.setInt(4, subscription.getUserId());
            stmt.setString(5, subscription.getToken());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Updates specified subscription's last-modified time stamp.
     *
     * @param subscription The subscription to update
     * @param con The connection to use
     * @return <code>true</code> if such a subscription has been updated; otherwise <code>false</code> if no such subscription existed
     * @throws OXException If update fails
     */
    private boolean updateLastModified(PushSubscription subscription, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE pns_subscription SET last_modified=? WHERE cid=? AND user=? AND token=?");
            stmt.setLong(1, System.currentTimeMillis());
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
