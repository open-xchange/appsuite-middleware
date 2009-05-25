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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.subscriptions;
import static com.openexchange.subscribe.SubscriptionErrorMessage.IDGiven;
import static com.openexchange.subscribe.SubscriptionErrorMessage.SQLException;
import static com.openexchange.subscribe.SubscriptionErrorMessage.SubscriptionNotFound;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.publish.PublicationException;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.SubscriptionStorage;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscriptionSQLStorage implements SubscriptionStorage {
    
    private DBProvider dbProvider;
    private GenericConfigurationStorageService storageService;
    private SubscriptionSourceDiscoveryService discoveryService;

    public SubscriptionSQLStorage(DBProvider dbProvider, GenericConfigurationStorageService storageService, SubscriptionSourceDiscoveryService discoveryService) {
        this.dbProvider = dbProvider;
        this.storageService = storageService;
        this.discoveryService = discoveryService;
    }

    public void forgetSubscription(Subscription subscription) throws SubscriptionException {
        if (!exist(subscription.getId(), subscription.getContext())) {
            return;
        }
        
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            writeConnection.setAutoCommit(false);
            delete(subscription, writeConnection);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new SubscriptionException(e);
        } finally {
            if (writeConnection != null ) {
                try {
                    writeConnection.rollback();
                    writeConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw SQLException.create(e);
                } finally {
                    dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
                }
            }
        }
    }

    public Subscription getSubscription(Context ctx, int id) throws SubscriptionException {
        Subscription retval = null;
        
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "user_id", "configuration_id", "source_id", "folder_id", "last_update").
            FROM(subscriptions).
            WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(id);
            values.add(ctx.getContextId());
            
            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            List<Subscription> subscriptions = parseResultSet(resultSet, ctx, readConnection);
            if (subscriptions.size() != 0) {
                retval = subscriptions.get(0);
            }
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new SubscriptionException(e);
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
        
        return retval;
    }

    public List<Subscription> getSubscriptions(Context ctx, int folderId) throws SubscriptionException {
        List<Subscription> retval = null;
        
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "user_id", "configuration_id", "source_id", "folder_id", "last_update").
            FROM(subscriptions).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("folder_id", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(folderId);
            
            builder = new StatementBuilder();
            retval = parseResultSet(builder.executeQuery(readConnection, select, values), ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new SubscriptionException(e);
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
        
        return retval;
    }

    public void rememberSubscription(Subscription subscription) throws SubscriptionException {
        if (subscription.getId() > 0) {
            throw IDGiven.create();
        }
        
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            writeConnection.setAutoCommit(false);
            int id = save(subscription, writeConnection);
            subscription.setId(id);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new SubscriptionException(e);
        } finally {
            if (writeConnection != null ) {
                try {
                    writeConnection.rollback();
                    writeConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw SQLException.create(e);
                } finally {
                    dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
                }
            }
        }
    }

    public void updateSubscription(Subscription subscription) throws SubscriptionException {
        if (!exist(subscription.getId(), subscription.getContext())) {
            throw SubscriptionNotFound.create();
        }
        
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(subscription.getContext());
            writeConnection.setAutoCommit(false);
            update(subscription, writeConnection);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new SubscriptionException(e);
        } finally {
            if (writeConnection != null ) {
                try {
                    writeConnection.rollback();
                    writeConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw SQLException.create(e);
                } finally {
                    dbProvider.releaseWriteConnection(subscription.getContext(), writeConnection);
                }
            }
        }
    }

    private void delete(Subscription subscription, Connection writeConnection) throws SQLException, GenericConfigStorageException, SubscriptionException {
        DELETE delete = new DELETE().FROM(subscriptions).WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));
        
        List<Object> values = new ArrayList<Object>();
        values.add(subscription.getId());
        values.add(subscription.getContext().getContextId());
        
        new StatementBuilder().executeStatement(writeConnection, delete, values);
        
        storageService.delete(writeConnection, subscription.getContext(), getConfigurationId(subscription));
    }

    private int save(Subscription subscription, Connection writeConnection) throws GenericConfigStorageException, SQLException {
        int configId = storageService.save(writeConnection, subscription.getContext(), subscription.getConfiguration());
        
        int id = IDGenerator.getId(subscription.getContext().getContextId(), Types.SUBSCRIPTION, writeConnection);

        INSERT insert = new INSERT().
        INTO(subscriptions).
        SET("id", PLACEHOLDER).
        SET("cid", PLACEHOLDER).
        SET("user_id", PLACEHOLDER).
        SET("configuration_id", PLACEHOLDER).
        SET("source_id", PLACEHOLDER).
        SET("folder_id", PLACEHOLDER).
        SET("last_update", PLACEHOLDER);

        List<Object> values = new ArrayList<Object>();
        values.add(id);
        values.add(subscription.getContext().getContextId());
        values.add(subscription.getUserId());
        values.add(configId);
        values.add(subscription.getSource().getId());
        values.add(subscription.getFolderId());
        values.add(subscription.getLastUpdate());

        new StatementBuilder().executeStatement(writeConnection, insert, values);
        return id;
    }

    private void update(Subscription subscription, Connection writeConnection) throws SubscriptionException, GenericConfigStorageException, SQLException {
        if (subscription.getConfiguration() != null) {
            int configId = getConfigurationId(subscription);
            storageService.update(writeConnection, subscription.getContext(), configId, subscription.getConfiguration());
        }
        
        UPDATE update = new UPDATE(subscriptions);
        List<Object> values = new ArrayList<Object>();
        
        if (subscription.getUserId() > 0) {
            update.SET("user_id", PLACEHOLDER);
            values.add(subscription.getUserId());
        }
        if (subscription.getSource() != null) {
            update.SET("source_id", PLACEHOLDER);
            values.add(subscription.getSource().getId());
        }
        if (subscription.getFolderId() > 0) {
            update.SET("folder_id", PLACEHOLDER);
            values.add(subscription.getFolderId());
        }
        if (subscription.getLastUpdate() > 0) {
            update.SET("last_update", PLACEHOLDER);
            values.add(subscription.getLastUpdate());
        }

        if (values.size() > 0) {
            new StatementBuilder().executeStatement(writeConnection, update, values);
        }
    }
    
    private int getConfigurationId(Subscription subscription) throws SubscriptionException {
        int retval = 0;
        Connection readConection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConection = dbProvider.getReadConnection(subscription.getContext());
            
            SELECT select = new SELECT("configuration_id").
            FROM(subscriptions).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(subscription.getContext().getContextId());
            values.add(subscription.getId());
            
            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConection, select, values);
            
            if (resultSet.next()) {
                retval = resultSet.getInt("configuration_id");
            }
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new SubscriptionException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(subscription.getContext(), readConection);
            }
        }
        return retval;
    }
    
    private List<Subscription> parseResultSet(ResultSet resultSet, Context ctx, Connection readConnection) throws GenericConfigStorageException, SQLException {
        List<Subscription> retval = new ArrayList<Subscription>();
        while (resultSet.next()) {
            Subscription subscription = new Subscription();
            subscription.setContext(ctx);
            subscription.setFolderId(resultSet.getInt("folder_id"));
            subscription.setId(resultSet.getInt("id"));
            subscription.setLastUpdate(resultSet.getLong("last_update"));
            subscription.setUserId(resultSet.getInt("user_id"));
            
            Map<String, Object> content = new HashMap<String, Object>();
            storageService.fill(readConnection, ctx, resultSet.getInt("configuration_id"), content);
            
            subscription.setConfiguration(content);
            subscription.setSource(discoveryService.getSource(resultSet.getString("source_id")));
            
            retval.add(subscription);
        }
        return retval;
    }
    
    private boolean exist(int id, Context ctx) throws SubscriptionException {
        boolean retval = false;
        
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id").
            FROM(subscriptions).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(id);
            
            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = resultSet.next();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
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
        
        return retval;
    }

}
