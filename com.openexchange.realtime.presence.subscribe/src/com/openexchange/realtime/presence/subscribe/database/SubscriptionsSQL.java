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

package com.openexchange.realtime.presence.subscribe.database;

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.subscribe.impl.Subscription;
import com.openexchange.realtime.presence.subscribe.impl.SubscriptionParticipant;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.ISNULL;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.UPDATE;

/**
 * {@link SubscriptionsSQL}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SubscriptionsSQL {

    public static DatabaseService db;

    private static final Table table = new Table("presenceSubscriptions");

    private static final Column fromCid = new Column("fromCid");

    private static final Column fromUserId = new Column("fromUserId");

    private static final Column fromId = new Column("fromId");

    private static final Column toCid = new Column("toCid");

    private static final Column toUserId = new Column("toUserId");

    private static final Column status = new Column("status");

    private static final Column request = new Column("request");

    private static final Column timestamp = new Column("timestamp");

    private static final Column uuid = new Column("uuid");

    // @formatter:off

    private static final SELECT selectTo = new SELECT(ASTERISK).FROM(table).WHERE(
        new EQUALS(toCid, PLACEHOLDER).AND(new EQUALS(toUserId, PLACEHOLDER)));

    private static final SELECT selectFromByCidAndUserId = new SELECT(ASTERISK).FROM(table).WHERE(
        new EQUALS(fromCid, PLACEHOLDER).AND(new EQUALS(fromUserId, PLACEHOLDER)));

    private static final SELECT selectFromById = new SELECT(ASTERISK).FROM(table).WHERE(new EQUALS(fromId, PLACEHOLDER));

    private static final SELECT selectToByCidUserIdAndStatus = new SELECT(ASTERISK).FROM(table).WHERE(
        new EQUALS(toCid, PLACEHOLDER).AND(new EQUALS(toUserId, PLACEHOLDER).AND(new EQUALS(status, PLACEHOLDER))));

    // @formatter:on

    private final DatabaseService dbService;

    public SubscriptionsSQL(DatabaseService dbService) {
        super();
        this.dbService = dbService;
    }

    /**
     * Persist a Subscription
     * 
     * @param subscription the Subscrption to persist
     * @throws OXException
     */
    public void store(Subscription subscription) throws OXException {
        Predicate p = new EQUALS(toCid, PLACEHOLDER).AND(new EQUALS(toUserId, PLACEHOLDER));

        INSERT insert = new INSERT().INTO(table).SET(toCid, PLACEHOLDER).SET(toUserId, PLACEHOLDER);
        UPDATE update = new UPDATE(table);

        LinkedList<Object> values = new LinkedList<Object>();
        values.add(subscription.getTo().getCid());
        values.add(subscription.getTo().getUserId());

        if (subscription.getFrom().getCid() == -1) {
            p = p.AND(new ISNULL(fromCid));
        } else {
            p = p.AND(new EQUALS(fromCid, PLACEHOLDER));
            // update = update.SET(fromCid, PLACEHOLDER);
            insert = insert.SET(fromCid, PLACEHOLDER);
            values.add(subscription.getFrom().getCid());
        }

        if (subscription.getFrom().getUserId() == -1) {
            p = p.AND(new ISNULL(fromUserId));
        } else {
            p = p.AND(new EQUALS(fromUserId, PLACEHOLDER));
            // update = update.SET(fromUserId, PLACEHOLDER);
            insert = insert.SET(fromUserId, PLACEHOLDER);
            values.add(subscription.getFrom().getUserId());
        }

        if (subscription.getFrom().getId() == null) {
            p = p.AND(new ISNULL(fromId));
        } else {
            p = p.AND(new EQUALS(fromId, PLACEHOLDER));
            // update = update.SET(fromId, PLACEHOLDER);
            insert = insert.SET(fromId, PLACEHOLDER);
            values.add(subscription.getFrom().getId());
        }

        SELECT select = new SELECT(ASTERISK).FROM(table).WHERE(p);
        DELETE delete = new DELETE().FROM(table).WHERE(p);

        update = update.SET(status, PLACEHOLDER);
        insert = insert.SET(status, PLACEHOLDER);

        if (subscription.getRequest() != null && !subscription.getRequest().trim().equals("")) {
            insert = insert.SET(request, PLACEHOLDER);
            update = update.SET(request, PLACEHOLDER);
        }
        
        if (null != subscription.getUuid()) {
            insert = insert.SET(uuid, PLACEHOLDER);
        }

        update.WHERE(p);

        Connection connection = dbService.getWritable(subscription.getFrom().getCid());
        StatementBuilder sb = null;
        ResultSet rs = null;

        try {
            sb = new StatementBuilder();
            rs = sb.executeQuery(connection, select, values);

            if (rs.next()) {
                if (subscription.getState() == Presence.Type.UNSUBSCRIBED) {
                    new StatementBuilder().executeStatement(connection, delete, values);
                } else if (!rs.getString(status.getName()).equals(subscription.getState().name())) {
                    if (subscription.getRequest() != null && !subscription.getRequest().trim().equals("")) {
                        values.addFirst(subscription.getRequest());
                    }
                    values.addFirst(subscription.getState().name());
                    new StatementBuilder().executeStatement(connection, update, values);
                }
            } else {
                values.add(subscription.getState().name());
                if (subscription.getRequest() != null && !subscription.getRequest().trim().equals("")) {
                    values.add(subscription.getRequest());
                }
                byte[] uuidBinary = UUIDs.toByteArray(subscription.getUuid());
                values.add(uuidBinary);
                new StatementBuilder().executeStatement(connection, insert, values);
            }
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (sb != null) {
                try {
                    sb.closePreparedStatement(null, rs);
                    dbService.backWritable(subscription.getFrom().getCid(), connection);
                } catch (SQLException e1) {
                    throw new OXException(e1);
                }
            }
        }
    }

    /**
     * Get a list of Subscriptions the SubscriptionParticipant sent TO others.
     * 
     * @param recipient the SubscriptionParticipant that sent the subscriptions
     * @return a list of subscriptions the SubscriptionParticipant sent to others
     * @throws OXException
     */
    public List<Subscription> getTo(SubscriptionParticipant sender) throws OXException {
        List<Object> values = new ArrayList<Object>();
        SELECT select;
        if (sender.getId() != null && !sender.getId().trim().equals("")) {
            select = selectFromById;
            values.add(sender.getId().trim());
        } else {
            select = selectFromByCidAndUserId;
            values.add(sender.getCid());
            values.add(sender.getUserId());
        }

        Connection connection = dbService.getWritable(sender.getCid());
        StatementBuilder sb = null;
        ResultSet rs = null;

        try {
            sb = new StatementBuilder();
            rs = sb.executeQuery(connection, select, values);

            return handleResultSet(rs);
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (sb != null) {
                try {
                    sb.closePreparedStatement(null, rs);
                    dbService.backWritable(sender.getCid(), connection);
                } catch (SQLException e1) {
                    throw new OXException(e1);
                }
            }
        }
    }

    /**
     * Get a list of Subscriptions the SubscriptionParticipant received FROM others.
     * 
     * @param recipient the SubscriptionParticipant that received the subscriptions
     * @return a list of subscriptions the SubscriptionParticipant received
     * @throws OXException
     */
    public List<Subscription> getFrom(SubscriptionParticipant recipient) throws OXException {
        List<Object> values = new ArrayList<Object>();
        values.add(recipient.getCid());
        values.add(recipient.getUserId());

        Connection connection = dbService.getWritable(recipient.getCid());
        StatementBuilder sb = null;
        ResultSet rs = null;

        try {
            sb = new StatementBuilder();
            rs = sb.executeQuery(connection, selectTo, values);

            return handleResultSet(rs);
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (sb != null) {
                try {
                    sb.closePreparedStatement(null, rs);
                    dbService.backWritable(recipient.getCid(), connection);
                } catch (SQLException e1) {
                    throw new OXException(e1);
                }
            }
        }
    }

    /**
     * Get the pending Subscriptions for a SubscriptionParticipant. This will return all the subscriptions sent TO the recipient by others
     * and haven't been approved or canceled by the recipient, yet.
     * 
     * @param recipient the SubscriptionParticipant
     * @return the list of subscriptions that haven't been approved or canceled by the recipient and are in a pending state
     * @throws OXException
     */
    public List<Subscription> getPendingFor(SubscriptionParticipant recipient) throws OXException {
        List<Object> values = new ArrayList<Object>();
        values.add(recipient.getCid());
        values.add(recipient.getUserId());
        values.add(Presence.Type.PENDING.name());

        Connection connection = dbService.getWritable(recipient.getCid());
        StatementBuilder sb = null;
        ResultSet rs = null;

        try {
            sb = new StatementBuilder();
            rs = sb.executeQuery(connection, selectToByCidUserIdAndStatus, values);

            return handleResultSet(rs);
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (sb != null) {
                try {
                    sb.closePreparedStatement(null, rs);
                    dbService.backWritable(recipient.getCid(), connection);
                } catch (SQLException e1) {
                    throw new OXException(e1);
                }
            }
        }
    }


    private List<Subscription> handleResultSet(ResultSet rs) throws SQLException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();

        while (rs.next()) {
            SubscriptionParticipant from = null;
            if (rs.getString(fromId.getName()) != null) {
                from = new SubscriptionParticipant(rs.getString(fromId.getName()));
            } else {
                from = new SubscriptionParticipant(rs.getInt(fromUserId.getName()), rs.getInt(fromCid.getName()));
            }

            SubscriptionParticipant to = null;
            to = new SubscriptionParticipant(rs.getInt(toUserId.getName()), rs.getInt(toCid.getName()));

            Subscription subscription = new Subscription(from, to, Presence.Type.valueOf(rs.getString(status.getName())));

            String req = rs.getString(request.getName());
            if (req != null && !req.trim().equals("")) {
                subscription.setRequest(req);
            }

            try {
                byte[] uuidBinary = Streams.stream2bytes(rs.getBinaryStream("uuid"));
            if (!rs.wasNull()) {
                UUID uuid = UUIDs.toUUID(uuidBinary);
                subscription.setUuid(uuid);
            }
            } catch (IOException e) {
                UUID uuid = UUID.randomUUID();
                subscription.setUuid(uuid);
            }

            subscriptions.add(subscription);
        }

        return subscriptions;
    }

}
