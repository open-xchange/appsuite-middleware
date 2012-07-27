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

package com.openexchange.sessionstorage.nosql;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.StoredSession;
import com.openexchange.sessionstorage.nosql.osgi.NoSQLServiceRegistry;

/**
 * {@link NoSQLSessionStorageService}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class NoSQLSessionStorageService implements SessionStorageService {

    private final String HOST;

    private final int PORT;

    private final String CLUSTER;

    private final String KEYSPACE;

    private final String CF_NAME;

    private boolean CF_EXISTS;

    private final Keyspace keyspace;

    private final Cluster cluster;
    
    private final StringSerializer serializer;

    public NoSQLSessionStorageService() {
        ConfigurationService configService = NoSQLServiceRegistry.getRegistry().getService(ConfigurationService.class);
        HOST = configService.getProperty("com.openexchange.sessionstorage.nosql.host", "localhost");
        PORT = configService.getIntProperty("com.openexchange.sessionstorage.nosql.port", 9160);
        KEYSPACE = configService.getProperty("com.openexchange.sessionstorage.nosql.keyspace", "ox");
        CF_NAME = configService.getProperty("com.openexchange.sessionstorage.nosql.cfname", "sessionstorage");
        CLUSTER = HOST + ":" + PORT;
        CF_EXISTS = false;
        serializer = new StringSerializer();
        cluster = HFactory.getOrCreateCluster("oxCluster", CLUSTER);
        KeyspaceDefinition definition = cluster.describeKeyspace(KEYSPACE);
        if (definition == null) {
            CassandraHostConfigurator configurator = new CassandraHostConfigurator(CLUSTER);
            ThriftCluster thriftCluster = new ThriftCluster(CLUSTER, configurator);
            ColumnFamilyDefinition cfDefinition = HFactory.createColumnFamilyDefinition(KEYSPACE, CF_NAME);
            thriftCluster.addKeyspace(new ThriftKsDef(
                KEYSPACE,
                "org.apache.cassandra.locator.SimpleStrategy",
                1,
                Arrays.asList(cfDefinition)));
        } else {
            List<ColumnFamilyDefinition> cfDefinitions = definition.getCfDefs();
            for (ColumnFamilyDefinition def : cfDefinitions) {
                String columnFamilyName = def.getName();
                if (columnFamilyName.equals(CF_NAME)) {
                    CF_EXISTS = true;
                }
            }
        }
        if (!CF_EXISTS) {
            ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(KEYSPACE, CF_NAME);
            cluster.addColumnFamily(cfDef);
        }
        keyspace = HFactory.createKeyspace(KEYSPACE, cluster);
    }

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME).setKey(sessionId).setColumnNames("loginName", "password", "contextId", "userId", "secret", "login", "randomToken", "localIP", "authId", "hash", "client");
        QueryResult<ColumnSlice<String, String>> result = query.execute();
        ColumnSlice<String, String> slice = result.get();
        String loginName = slice.getColumnByName("loginName").getValue();
        String password = slice.getColumnByName("password").getValue();
        int contextId = Integer.parseInt(slice.getColumnByName("contextId").getValue());
        int userId = Integer.parseInt(slice.getColumnByName("userId").getValue());
        String secret = slice.getColumnByName("secret").getValue();
        String login = slice.getColumnByName("login").getValue();
        String randomToken = slice.getColumnByName("randomToken").getValue();
        String localIP = slice.getColumnByName("localIP").getValue();
        String authId = slice.getColumnByName("authId").getValue();
        String hash = slice.getColumnByName("hash").getValue();
        String client = slice.getColumnByName("client").getValue();
        Session session = new StoredSession(sessionId, loginName, password, contextId, userId, secret, login, randomToken, localIP, authId, hash, client, null);
        return session;
    }

    @Override
    public void addSession(Session session) throws OXException {
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("loginName", session.getLoginName()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("password", session.getPassword()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("contextId", String.valueOf(session.getContextId())));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("userId", String.valueOf(session.getUserId())));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("secret", session.getSecret()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("login", session.getLogin()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("randomToken", session.getRandomToken()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("localIP", session.getLocalIp()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("authId", session.getAuthId()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("hash", session.getHash()));
        mutator.insert(session.getSessionID(), CF_NAME, HFactory.createStringColumn("client", session.getClient()));
        mutator.execute();
    }

    @Override
    public void removeSession(String sessionId) throws OXException {
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        mutator.delete(sessionId, CF_NAME, null, serializer);
        mutator.execute();
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) throws OXException {
        List<Session> list = new LinkedList<Session>();
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("contextId", String.valueOf(contextId)).addEqualsExpression("userId", String.valueOf(userId));
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = slice.getColumnByName("password").getValue();
            int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIP").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(sessionId, loginName, password, contextId2, userId2, secret, login, randomToken, localIP, authId, hash, client, null);
            removeSession(sessionId);
            list.add(session);
        }
        return (Session[]) list.toArray();
    }

    @Override
    public void removeContextSessions(int contextId) throws OXException {
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("contextId", String.valueOf(contextId));
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            removeSession(sessionId);
        }
    }

    @Override
    public boolean hasForContext(int contextId) throws OXException {
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("contextId", String.valueOf(contextId));
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        return rowsList.size() > 0;
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) throws OXException {
        List<Session> list = new LinkedList<Session>();
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("contextId", String.valueOf(contextId)).addEqualsExpression("userId", String.valueOf(userId));
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = slice.getColumnByName("password").getValue();
            int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIP").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(sessionId, loginName, password, contextId2, userId2, secret, login, randomToken, localIP, authId, hash, client, null);
            list.add(session);
        }
        return (Session[]) list.toArray();
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException {
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("contextId", String.valueOf(contextId)).addEqualsExpression("userId", String.valueOf(userId));
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = slice.getColumnByName("password").getValue();
            int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIP").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(sessionId, loginName, password, contextId2, userId2, secret, login, randomToken, localIP, authId, hash, client, null);
            return session;
        }
        return null;
    }

    @Override
    public Session findFirstSessionForUser(int userId, int contextId) throws OXException {
        return getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public List<Session> getSessions() {
        List<Session> list = new LinkedList<Session>();
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = slice.getColumnByName("password").getValue();
            int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIP").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(sessionId, loginName, password, contextId2, userId2, secret, login, randomToken, localIP, authId, hash, client, null);
            list.add(session);
        }
        return list;
    }

    @Override
    public int getNumberOfActiveSessions() {
        return getSessions().size();
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) throws OXException {
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.addEqualsExpression("randomToken", randomToken);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            String sessionId = slice.getColumnByName("sessionId").getValue();
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = slice.getColumnByName("password").getValue();
            int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken2 = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIP").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(sessionId, loginName, password, contextId2, userId2, secret, login, randomToken2, localIP, authId, hash, client, null);
            return session;
        }
        return null;
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        return lookupSession(sessionId);
    }

    @Override
    public void cleanUp() throws OXException {
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        mutator.delete("*", CF_NAME, null, serializer);
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME).setKey(sessionId).setColumnNames("loginName", "password", "contextId", "userId", "secret", "login", "randomToken", "localIP", "authId", "hash", "client");
        QueryResult<ColumnSlice<String, String>> result = query.execute();
        ColumnSlice<String, String> slice = result.get();
        String loginName = slice.getColumnByName("loginName").getValue();
        int contextId = Integer.parseInt(slice.getColumnByName("contextId").getValue());
        int userId = Integer.parseInt(slice.getColumnByName("userId").getValue());
        String secret = slice.getColumnByName("secret").getValue();
        String login = slice.getColumnByName("login").getValue();
        String randomToken = slice.getColumnByName("randomToken").getValue();
        String localIP = slice.getColumnByName("localIP").getValue();
        String authId = slice.getColumnByName("authId").getValue();
        String hash = slice.getColumnByName("hash").getValue();
        String client = slice.getColumnByName("client").getValue();
        Session session = new StoredSession(sessionId, loginName, newPassword, contextId, userId, secret, login, randomToken, localIP, authId, hash, client, null);
        removeSession(sessionId);
        addSession(session);
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        // TODO Auto-generated method stub

    }

}
