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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.subscribe.sql;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.subscriptions;
import static com.openexchange.subscribe.SubscriptionErrorMessage.IDGiven;
import static com.openexchange.subscribe.SubscriptionErrorMessage.SQLException;
import static com.openexchange.subscribe.SubscriptionErrorMessage.SubscriptionNotFound;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.Expression;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.subscribe.EncryptedField;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.SubscriptionStorage;
import com.openexchange.tools.sql.DBUtils;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - deleteAllSubscriptionsForUser
 */
public class SubscriptionSQLStorage implements SubscriptionStorage {

    private final DBProvider dbProvider;
    private final DBTransactionPolicy txPolicy;

    private final GenericConfigurationStorageService storageService;

    private final SubscriptionSourceDiscoveryService discoveryService;

    public SubscriptionSQLStorage(final DBProvider dbProvider, final GenericConfigurationStorageService storageService, final SubscriptionSourceDiscoveryService discoveryService) {
        this(dbProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS, storageService, discoveryService);
    }
    public SubscriptionSQLStorage(final DBProvider dbProvider, final DBTransactionPolicy txPolicy, final GenericConfigurationStorageService storageService, final SubscriptionSourceDiscoveryService discoveryService) {
        this.dbProvider = dbProvider;
        this.txPolicy = txPolicy;
        this.storageService = storageService;
        this.discoveryService = discoveryService;
    }

    @Override
    public void forgetSubscription(final Subscription subscription) throws OXException {
        if (!exist(subscription.getId(), subscription.getContext())) {
            return;
        }

        Connection writeConnection = null;
        boolean rollback = false;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            rollback = true;
            delete(subscription, writeConnection);
            txPolicy.commit(writeConnection);
            rollback = false;
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            if (writeConnection != null) {
                if (rollback) {
                    Databases.rollback(writeConnection);
                }
                Databases.autocommit(writeConnection);
                dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
            }
        }
    }

    @Override
    public Subscription getSubscription(final Context ctx, final int id) throws OXException {
        Subscription retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("id", "user_id", "configuration_id", "source_id", "folder_id", "last_update", "created", "enabled")
            .FROM(subscriptions)
            .WHERE(
                new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(id));
            values.add(I(ctx.getContextId()));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            final List<Subscription> subscriptions = parseResultSet(resultSet, ctx, readConnection);
            if (subscriptions.size() != 0) {
                retval = subscriptions.get(0);
            }
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public List<Subscription> getSubscriptions(final Context ctx, final String folderId) throws OXException {
        List<Subscription> retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new
                SELECT("id", "user_id", "configuration_id", "source_id", "folder_id", "last_update", "created", "enabled")
                .FROM(subscriptions)
                .WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("folder_id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(folderId);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public List<Subscription> getSubscriptionsOfUser(Context ctx, int userId, String sourceId) throws OXException {
        Connection con = dbProvider.getReadConnection(ctx);
        try {
            return getSubscriptionsOfUser(ctx, userId, sourceId, con);
        } finally {
            dbProvider.releaseReadConnection(ctx, con);
        }
    }

    public List<Subscription> getSubscriptionsOfUser(Context ctx, int userId, String sourceId, Connection con) throws OXException {
        if (null == con) {
            return getSubscriptionsOfUser(ctx, userId, sourceId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, folder_id, last_update, created, user_id, enabled, configuration_id, source_id FROM subscriptions WHERE cid=? AND user_id=? AND source_id = ?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setString(3, sourceId);
            rs = stmt.executeQuery();
            return parseResultSet(rs, ctx, con);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public List<Subscription> getSubscriptionsOfUser(Context ctx, int userId)  throws OXException {
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("*")
            .FROM(subscriptions)
            .WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("user_id", PLACEHOLDER)));

            final List<Object> values = new LinkedList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(I(userId));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            return parseResultSet(resultSet, ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }
    }

    @Override
    public void rememberSubscription(final Subscription subscription) throws OXException {
        if (subscription.getId() > 0) {
            throw IDGiven.create();
        }

        Connection writeConnection = null;
        boolean rollback = false;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            rollback = true;
            final int id = save(subscription, writeConnection);
            subscription.setId(id);
            txPolicy.commit(writeConnection);
            rollback = false;
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            if (writeConnection != null) {
                if (rollback) {
                    Databases.rollback(writeConnection);
                }
                Databases.autocommit(writeConnection);
                dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
            }
        }
    }

    @Override
    public void updateSubscription(final Subscription subscription) throws OXException {
        if (!exist(subscription.getId(), subscription.getContext())) {
            throw SubscriptionNotFound.create();
        }

        Connection writeConnection = null;
        boolean rollback = false;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            rollback = true;
            update(subscription, writeConnection);
            txPolicy.commit(writeConnection);
            rollback = false;
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            if (writeConnection != null) {
                if (rollback) {
                    Databases.rollback(writeConnection);
                }
                Databases.autocommit(writeConnection);
                dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
            }
        }
    }

    private void delete(final Subscription subscription, final Connection writeConnection) throws SQLException, OXException {
        final DELETE delete = new DELETE().FROM(subscriptions).WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));

        final List<Object> values = new ArrayList<Object>();
        values.add(I(subscription.getId()));
        values.add(I(subscription.getContext().getContextId()));

        new StatementBuilder().executeStatement(writeConnection, delete, values);

        storageService.delete(writeConnection, subscription.getContext(), getConfigurationId(subscription));
    }

    private int save(final Subscription subscription, final Connection writeConnection) throws OXException, SQLException {
        final int configId = storageService.save(writeConnection, subscription.getContext(), subscription.getConfiguration());

        final int id = IDGenerator.getId(subscription.getContext().getContextId(), Types.SUBSCRIPTION, writeConnection);

        final INSERT insert = new INSERT().INTO(subscriptions).SET("id", PLACEHOLDER).SET("cid", PLACEHOLDER).SET("user_id", PLACEHOLDER).SET(
            "configuration_id",
            PLACEHOLDER).SET("source_id", PLACEHOLDER).SET("folder_id", PLACEHOLDER).SET("last_update", PLACEHOLDER).SET("created", PLACEHOLDER).SET("enabled", PLACEHOLDER);

        final List<Object> values = new ArrayList<Object>();
        values.add(I(id));
        values.add(I(subscription.getContext().getContextId()));
        values.add(I(subscription.getUserId()));
        values.add(I(configId));
        values.add(subscription.getSource().getId());
        values.add(subscription.getFolderId());
        values.add(L(subscription.getLastUpdate()));
        values.add(System.currentTimeMillis());
        values.add(subscription.isEnabled());

        new StatementBuilder().executeStatement(writeConnection, insert, values);
        return id;
    }

    private void update(final Subscription subscription, final Connection writeConnection) throws OXException, SQLException {
        if (subscription.getConfiguration() != null) {
            final int configId = getConfigurationId(subscription);
            storageService.update(writeConnection, subscription.getContext(), configId, subscription.getConfiguration());
        }

        final UPDATE update = new UPDATE(subscriptions);
        final List<Object> values = new ArrayList<Object>();

        if (subscription.containsUserId()) {
            update.SET("user_id", PLACEHOLDER);
            values.add(I(subscription.getUserId()));
        }
        if (subscription.containsSource()) {
            update.SET("source_id", PLACEHOLDER);
            values.add(subscription.getSource().getId());
        }
        if (subscription.containsFolderId()) {
            update.SET("folder_id", PLACEHOLDER);
            values.add(subscription.getFolderId());
        }
        if (subscription.containsLastUpdate()) {
            update.SET("last_update", PLACEHOLDER);
            values.add(L(subscription.getLastUpdate()));
        }
        if (subscription.containsEnabled()) {
            update.SET("enabled", PLACEHOLDER);
            values.add(subscription.isEnabled());
        }

        update.WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
        values.add(I(subscription.getContext().getContextId()));
        values.add(I(subscription.getId()));

        if (values.size() > 2) {
            new StatementBuilder().executeStatement(writeConnection, update, values);
        }
    }

    @Override
    public void touch(Context ctx, int subscriptionId, long currentTimeMillis) throws OXException {
        Connection writeConnection = null;
        boolean rollback = false;
        try {
            writeConnection = dbProvider.getWriteConnection(ctx);
            txPolicy.setAutoCommit(writeConnection, false);
            rollback = true;

            new StatementBuilder().executeStatement(writeConnection,
                new UPDATE(subscriptions)
                    .SET("last_update", PLACEHOLDER)
                    .WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id",  PLACEHOLDER))), Arrays.<Object>asList(currentTimeMillis, ctx.getContextId(), subscriptionId));



            txPolicy.commit(writeConnection);
            rollback = false;
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            if (writeConnection != null) {
                if (rollback) {
                    Databases.rollback(writeConnection);
                }
                Databases.autocommit(writeConnection);
                dbProvider.releaseWriteConnection(ctx, writeConnection);
            }
        }
    }

    private int getConfigurationId(final Subscription subscription) throws OXException {
        int retval = 0;
        Connection readConection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConection = dbProvider.getReadConnection(subscription.getContext());

            final SELECT select = new
                SELECT("configuration_id")
                .FROM(subscriptions)
                .WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(subscription.getContext().getContextId()));
            values.add(I(subscription.getId()));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConection, select, values);

            if (resultSet.next()) {
                retval = resultSet.getInt("configuration_id");
            }
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(subscription.getContext(), readConection);
            }
        }
        return retval;
    }

    private List<Subscription> parseResultSet(final ResultSet resultSet, final Context ctx, final Connection readConnection) throws OXException, SQLException {
        final List<Subscription> retval = new LinkedList<Subscription>();
        while (resultSet.next()) {
            final Subscription subscription = new Subscription();
            subscription.setContext(ctx);
            subscription.setFolderId(resultSet.getString("folder_id"));
            subscription.setId(resultSet.getInt("id"));
            subscription.setLastUpdate(resultSet.getLong("last_update"));
            subscription.setCreated(resultSet.getLong("created"));
            subscription.setUserId(resultSet.getInt("user_id"));
            subscription.setEnabled(resultSet.getBoolean("enabled"));

            final Map<String, Object> content = new HashMap<String, Object>();
            storageService.fill(readConnection, ctx, resultSet.getInt("configuration_id"), content);

            subscription.setConfiguration(content);
            subscription.setSource(discoveryService.getSource(resultSet.getString("source_id")));

            retval.add(subscription);
        }
        return retval;
    }

    private boolean exist(final int id, final Context ctx) throws OXException {
        boolean retval = false;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new
                SELECT("id")
                .FROM(subscriptions)
                .WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(I(id));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = resultSet.next();
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public void deleteAllSubscriptionsForUser(final int userId, final Context ctx) throws OXException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(ctx);
            txPolicy.setAutoCommit(writeConnection, false);

            final List<Subscription> subs = getSubscriptionsOfUser(ctx, userId);
            for(final Subscription sub: subs){
                delete(sub, writeConnection);
            }
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if(writeConnection != null) {
                    txPolicy.rollback(writeConnection);
                    txPolicy.setAutoCommit(writeConnection, true);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            }
            dbProvider.releaseWriteConnection(ctx, writeConnection);
        }
    }

    @Override
    public void deleteAllSubscriptionsInContext(final int contextId, final Context ctx) throws OXException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(ctx);
            txPolicy.setAutoCommit(writeConnection, false);
            final DELETE delete = new
                DELETE()
                .FROM(subscriptions)
                .WHERE(new EQUALS("cid", PLACEHOLDER));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));

            new StatementBuilder().executeStatement(writeConnection, delete, values);
            storageService.delete(writeConnection, ctx);
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if(writeConnection != null) {
                    txPolicy.rollback(writeConnection);
                    txPolicy.setAutoCommit(writeConnection, true);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            }
            dbProvider.releaseWriteConnection(ctx, writeConnection);
        }
    }

    @Override
    public void deleteAllSubscriptionsWhereConfigMatches(final Map<String, Object> query, final String sourceId, final Context ctx) throws OXException {
        boolean modified = false;
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(ctx);
            txPolicy.setAutoCommit(writeConnection, false);
            final DELETE delete = new DELETE().FROM(subscriptions).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("source_id", PLACEHOLDER)).AND(new EQUALS("configuration_id", PLACEHOLDER)));
            final List<Object> values = new ArrayList<Object>(Arrays.asList(null, null, null));
            values.set(0, ctx.getContextId());
            values.set(1, sourceId);

            final List<Integer> configIds = storageService.search(ctx, query);
            for (final Integer configId : configIds) {
                values.set(2, configId);
                final int deleted = new StatementBuilder().executeStatement(writeConnection, delete, values);
                if(deleted == 1) {
                    // Delete the generic configuration only if the source_id matched
                    storageService.delete(writeConnection, ctx, configId);
                    modified = true;
                }
            }
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if(writeConnection != null) {
                    txPolicy.rollback(writeConnection);
                    txPolicy.setAutoCommit(writeConnection, true);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            }
            if (modified) {
                dbProvider.releaseWriteConnectionAfterReading(ctx, writeConnection);
            } else {
                dbProvider.releaseWriteConnection(ctx, writeConnection);
            }
        }
    }

    @Override
    public Map<String, Boolean> hasSubscriptions(final Context ctx, final List<String> folderIds) throws OXException {

        final Map<String, Boolean> retval = new HashMap<String, Boolean>();
        for(final String folderId : folderIds) {
            retval.put(folderId, false);
        }

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final ArrayList<Expression> placeholders = new ArrayList<Expression>(folderIds.size());
            for(int i = 0; i < folderIds.size(); i++) {
                placeholders.add(PLACEHOLDER);
            }

            final SELECT select = new SELECT("folder_id").FROM(subscriptions).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new IN("folder_id", new LIST(placeholders))));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.addAll(folderIds);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            while(resultSet.next()) {
                final String folderId = resultSet.getString(1);
                retval.put(folderId, true);
            }
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;

    }

    @Override
    public boolean hasSubscriptions(final Context ctx, final User user) throws OXException {

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("1").FROM(subscriptions).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("user_id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(I(user.getId()));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            return resultSet.next();
        } catch (final SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }
    }

    @Override
    public void update(final String recrypted, final EncryptedField customizationNote) throws OXException {
        final int configId = getConfigurationId(customizationNote.subscription);
        final Map<String,Object> update = new HashMap<String, Object>();
        update.put(customizationNote.field, recrypted);

        storageService.update(customizationNote.subscription.getContext(), configId, update);

    }
}
