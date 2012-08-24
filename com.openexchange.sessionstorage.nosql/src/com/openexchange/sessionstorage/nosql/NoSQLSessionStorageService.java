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
import java.util.UUID;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.StoredSession;
import com.openexchange.sessionstorage.nosql.exceptions.OXNoSQLSessionStorageExceptionCodes;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link NoSQLSessionStorageService}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class NoSQLSessionStorageService implements SessionStorageService {

    private static final Log log = com.openexchange.log.Log.loggerFor(NoSQLSessionStorageService.class);

    private final String HOST;

    private final int PORT;

    private final String CLUSTER;

    private final String KEYSPACE;

    private final String CF_NAME;

    private boolean CF_EXISTS;

    private final int LIFETIME;

    private final String encryptionKey;

    private final String[] COLUMN_NAMES = new String[] {
        "loginName", "password", "contextId", "userId", "secret", "login", "randomToken", "localIp", "authId", "hash", "client", "altId" };

    private final Keyspace keyspace;

    private final Cluster cluster;

    private final StringSerializer serializer;

    private final CryptoService cryptoService;

    private final TimerService timerService;

    private final ScheduledTimerTask cleanupTask;

    private static NoSQLSessionStorageService instance;

    public NoSQLSessionStorageService(NoSQLSessionStorageConfiguration config) {
        HOST = config.getHost();
        PORT = config.getPort();
        KEYSPACE = config.getKeyspace();
        CF_NAME = config.getCf_name();
        LIFETIME = config.getDefaultLifeTime();
        encryptionKey = config.getEncryptionKey();
        CLUSTER = HOST + ":" + PORT;
        CF_EXISTS = false;
        cryptoService = config.getCryptoService();
        timerService = config.getTimerService();
        serializer = new StringSerializer();
        cluster = HFactory.getOrCreateCluster("oxCluster", CLUSTER);
        KeyspaceDefinition definition = cluster.describeKeyspace(KEYSPACE);
        if (definition == null) {
            CassandraHostConfigurator configurator = new CassandraHostConfigurator(CLUSTER);
            ThriftCluster thriftCluster = new ThriftCluster(CLUSTER, configurator);
            ColumnFamilyDefinition cfDefinition = HFactory.createColumnFamilyDefinition(KEYSPACE, CF_NAME);
            cfDefinition.setMaxCompactionThreshold(4); // should be as low as possible
            cfDefinition.setMinCompactionThreshold(2); // in order to perform compactions
            cfDefinition.setCompactionStrategy("SizeTieredCompactionStrategy");

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
        cleanupTask = timerService.scheduleWithFixedDelay(new NoSQLCleanupTask(), LIFETIME, LIFETIME);
        instance = this;
    }

    @Override
    public Session lookupSession(String sessionId) throws OXException {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME).setKey(sessionId).setColumnNames(COLUMN_NAMES);
        QueryResult<ColumnSlice<String, String>> result = query.execute();
        ColumnSlice<String, String> slice = result.get();
        if (slice.getColumns().size() > 0) {
            String loginName = slice.getColumnByName("loginName").getValue();
            String password = decrypt(slice.getColumnByName("password").getValue());
            int contextId = Integer.parseInt(slice.getColumnByName("contextId").getValue());
            int userId = Integer.parseInt(slice.getColumnByName("userId").getValue());
            String secret = slice.getColumnByName("secret").getValue();
            String login = slice.getColumnByName("login").getValue();
            String randomToken = slice.getColumnByName("randomToken").getValue();
            String localIP = slice.getColumnByName("localIp").getValue();
            String authId = slice.getColumnByName("authId").getValue();
            String hash = slice.getColumnByName("hash").getValue();
            String client = slice.getColumnByName("client").getValue();
            Session session = new StoredSession(
                sessionId,
                loginName,
                password,
                contextId,
                userId,
                secret,
                login,
                randomToken,
                localIP,
                authId,
                hash,
                client,
                null);
            return session;
        }
        if (log.isDebugEnabled()) {
            OXException e = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_SESSION_NOT_FOUND.create(sessionId);
            log.debug(e.getMessage(), e);
            throw e;
        }
        return null;
    }

    @Override
    public void addSession(Session session) throws OXException {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("loginName", session.getLoginName()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("password", crypt(session.getPassword())));
            mutator.addInsertion(
                session.getSessionID(),
                CF_NAME,
                HFactory.createStringColumn("contextId", String.valueOf(session.getContextId())));
            mutator.addInsertion(
                session.getSessionID(),
                CF_NAME,
                HFactory.createStringColumn("userId", String.valueOf(session.getUserId())));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("secret", session.getSecret()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("login", session.getLogin()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("randomToken", session.getRandomToken()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("localIp", session.getLocalIp()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("authId", session.getAuthId()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("hash", session.getHash()));
            mutator.addInsertion(session.getSessionID(), CF_NAME, HFactory.createStringColumn("client", session.getClient()));
            String time = String.valueOf(System.currentTimeMillis());
            String uuidCtx = new UUID(session.getContextId(), session.getUserId()).toString();
            mutator.addInsertion(uuidCtx, CF_NAME, HFactory.createStringColumn(time, session.getSessionID()));
            String uuidCtxU = new UUID(session.getContextId(), session.getContextId()).toString();
            mutator.addInsertion(uuidCtxU, CF_NAME, HFactory.createStringColumn(time, session.getSessionID()));
            mutator.addInsertion(session.getRandomToken(), CF_NAME, HFactory.createStringColumn(time, session.getSessionID()));
            mutator.addInsertion(session.getAuthId(), CF_NAME, HFactory.createStringColumn(time, session.getSessionID()));
            mutator.execute();
        } catch (Exception e) {
            OXException ox = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_SAVE_FAILED.create(session.getSessionID());
            log.error(ox.getMessage(), ox);
            throw ox;
        }
    }

    @Override
    public void removeSession(String sessionId) throws OXException {
        try {
            SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
            query.setColumnFamily(CF_NAME).setColumnNames(COLUMN_NAMES).setKey(sessionId);
            QueryResult<ColumnSlice<String, String>> result = query.execute();
            ColumnSlice<String, String> slice = result.get();
            HColumn<String, String> userIdColumn = slice.getColumnByName("userId");
            String userId = userIdColumn.getValue();
            HColumn<String, String> ctxIdColumn = slice.getColumnByName("contextId");
            String ctxId = ctxIdColumn.getValue();
            HColumn<String, String> authIdColumn = slice.getColumnByName("authId");
            String authId = authIdColumn.getValue();
            HColumn<String, String> randomTokenColumn = slice.getColumnByName("randomToken");
            String randomToken = randomTokenColumn.getValue();
            String uuidCtx = new UUID(Long.parseLong(ctxId), Long.parseLong(ctxId)).toString();
            Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
            mutator.addDeletion(sessionId, CF_NAME, null, serializer);
            removeUserSessions(Integer.parseInt(userId), Integer.parseInt(ctxId));
            SliceQuery<String, String, String> uuidCtxQuery = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
            uuidCtxQuery.setColumnFamily(CF_NAME).setKey(uuidCtx);
            ColumnSliceIterator<String, String, String> iCtx = new ColumnSliceIterator<String, String, String>(
                uuidCtxQuery,
                null,
                "\uFFFF",
                false);
            while (iCtx.hasNext()) {
                HColumn<String, String> column = iCtx.next();
                if (column.getValue().equals(sessionId)) {
                    mutator.addDeletion(uuidCtx, CF_NAME, column.getName(), serializer);
                }
            }
            mutator.addDeletion(authId, CF_NAME);
            mutator.addDeletion(randomToken, CF_NAME);
            mutator.execute();
            if (!hasForContext(Integer.parseInt(ctxId))) {
                removeContextSessions(Integer.parseInt(ctxId));
            }
        } catch (Exception e) {
            OXException ox = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_REMOVE_FAILED.create(sessionId);
            log.error(ox.getMessage(), ox);
            throw ox;
        }
    }

    @Override
    public Session[] removeUserSessions(int userId, int contextId) throws OXException {
        String uuidCtxU = new UUID(contextId, userId).toString();
        List<Session> list = new LinkedList<Session>();
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setKey(uuidCtxU);
        ColumnSliceIterator<String, String, String> i = new ColumnSliceIterator<String, String, String>(query, null, "\uFFFF", false);
        while (i.hasNext()) {
            HColumn<String, String> column = i.next();
            String sessionId = column.getValue();
            Session session = lookupSession(sessionId);
            mutator.addDeletion(sessionId, CF_NAME);
            list.add(session);
        }
        mutator.addDeletion(uuidCtxU, CF_NAME);
        mutator.execute();
        Session[] sessions = new Session[list.size()];
        int j = 0;
        for (Session s : list) {
            sessions[j++] = s;
        }
        return sessions;
    }

    @Override
    public void removeContextSessions(int contextId) throws OXException {
        String uuid = new UUID(contextId, contextId).toString();
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setKey(uuid);
        ColumnSliceIterator<String, String, String> i = new ColumnSliceIterator<String, String, String>(query, null, "\uFFFF", false);
        while (i.hasNext()) {
            HColumn<String, String> column = i.next();
            String sessionId = column.getValue();
            removeSession(sessionId);
        }
        mutator.addDeletion(uuid, CF_NAME);
        mutator.execute();
    }

    @Override
    public boolean hasForContext(int contextId) {
        String uuidCtx = new UUID(contextId, contextId).toString();
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setKey(uuidCtx);
        ColumnSliceIterator<String, String, String> i = new ColumnSliceIterator<String, String, String>(query, null, "\uFFFF", false);
        return i.hasNext();
    }

    @Override
    public Session[] getUserSessions(int userId, int contextId) throws OXException {
        List<Session> retval = new LinkedList<Session>();
        String uuid = new UUID(contextId, userId).toString();
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setKey(uuid);
        ColumnSliceIterator<String, String, String> i = new ColumnSliceIterator<String, String, String>(query, null, "\uFFFF", false);
        while (i.hasNext()) {
            HColumn<String, String> column = i.next();
            String sessionId = column.getValue();
            Session session = lookupSession(sessionId);
            retval.add(session);
        }
        if (retval.size() == 0) {
            OXException e = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_NO_USERSESSIONS.create(userId, contextId);
            log.error(e.getMessage(), e);
            throw e;
        }
        Session[] sessions = new Session[retval.size()];
        int j = 0;
        for (Session s : retval) {
            sessions[j++] = s;
        }
        return sessions;
    }

    @Override
    public Session getAnyActiveSessionForUser(int userId, int contextId) throws OXException {
        Session[] sessions = getUserSessions(userId, contextId);
        if (sessions != null && sessions.length > 0) {
            return sessions[0];
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
        RangeSlicesQuery<String, String, String> query = HFactory.createRangeSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setColumnNames(COLUMN_NAMES);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> slice = row.getColumnSlice();
            if (slice.getColumns().size() > 0) {
                String sessionId = row.getKey();
                String loginName = slice.getColumnByName("loginName").getValue();
                String password = slice.getColumnByName("password").getValue();
                int contextId2 = Integer.parseInt(slice.getColumnByName("contextId").getValue());
                int userId2 = Integer.parseInt(slice.getColumnByName("userId").getValue());
                String secret = slice.getColumnByName("secret").getValue();
                String login = slice.getColumnByName("login").getValue();
                String randomToken = slice.getColumnByName("randomToken").getValue();
                String localIP = slice.getColumnByName("localIp").getValue();
                String authId = slice.getColumnByName("authId").getValue();
                String hash = slice.getColumnByName("hash").getValue();
                String client = slice.getColumnByName("client").getValue();
                Session session = new StoredSession(
                    sessionId,
                    loginName,
                    password,
                    contextId2,
                    userId2,
                    secret,
                    login,
                    randomToken,
                    localIP,
                    authId,
                    hash,
                    client,
                    null);
                list.add(session);
            }
        }
        return list;
    }

    @Override
    public int getNumberOfActiveSessions() {
        return getSessions().size();
    }

    @Override
    public Session getSessionByRandomToken(String randomToken, String newIP) {
        RangeSlicesQuery<String, String, String> query = HFactory.createRangeSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setColumnNames(COLUMN_NAMES);
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
            Session session = new StoredSession(
                sessionId,
                loginName,
                password,
                contextId2,
                userId2,
                secret,
                login,
                randomToken2,
                localIP,
                authId,
                hash,
                client,
                null);
            return session;
        }
        return null;
    }

    @Override
    public Session getSessionByAlternativeId(String altId) throws OXException {
        List<Session> sessions = getSessions();
        for (Session s : sessions) {
            if (s.getParameter(Session.PARAM_ALTERNATIVE_ID).equals(altId)) {
                return s;
            }
        }
        OXException e = OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_ALTID_NOT_FOUND.create(altId);
        log.error(e.getMessage(), e);
        throw e;
    }

    @Override
    public Session getCachedSession(String sessionId) throws OXException {
        return lookupSession(sessionId);
    }

    @Override
    public void cleanUp() {
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        mutator.addDeletion("*", CF_NAME, null, serializer);
        mutator.execute();
    }

    @Override
    public void changePassword(String sessionId, String newPassword) throws OXException {
        SliceQuery<String, String, String> query = HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME).setKey(sessionId).setColumnNames(COLUMN_NAMES);
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
        Session session = new StoredSession(
            sessionId,
            loginName,
            newPassword,
            contextId,
            userId,
            secret,
            login,
            randomToken,
            localIP,
            authId,
            hash,
            client,
            null);
        removeSession(sessionId);
        addSession(session);
    }

    @Override
    public void checkAuthId(String login, String authId) throws OXException {
        if (null != authId) {
            for (final Session session : getSessions()) {
                if (authId.equals(session.getAuthId())) {
                    throw OXNoSQLSessionStorageExceptionCodes.NOSQL_SESSIONSTORAGE_DUPLICATE_AUTHID.create(session.getLogin(), login);
                }
            }
        }
    }

    private String crypt(String password) throws OXException {
        try {
            return cryptoService.encrypt(password, encryptionKey);
        } catch (OXException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private String decrypt(String encPassword) throws OXException {
        try {
            return cryptoService.decrypt(encPassword, encryptionKey);
        } catch (OXException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void cleanup() {
        long time = System.currentTimeMillis();
        Mutator<String> mutator = HFactory.createMutator(keyspace, serializer);
        RangeSlicesQuery<String, String, String> query = HFactory.createRangeSlicesQuery(keyspace, serializer, serializer, serializer);
        query.setColumnFamily(CF_NAME);
        query.setColumnNames(COLUMN_NAMES);
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        OrderedRows<String, String, String> rows = result.get();
        List<Row<String, String, String>> rowsList = rows.getList();
        Iterator<Row<String, String, String>> rowIterator = rowsList.iterator();
        while (rowIterator.hasNext()) {
            Row<String, String, String> row = rowIterator.next();
            ColumnSlice<String, String> columns = row.getColumnSlice();
            List<HColumn<String, String>> columnList = columns.getColumns();
            for (HColumn<String, String> column : columnList) {
                if (time > column.getClock() + LIFETIME) {
                    mutator.addDeletion(row.getKey(), CF_NAME, column.getName(), serializer);
                }
            }
        }
        mutator.execute();
    }

    public static NoSQLSessionStorageService getStorageService() {
        return instance;
    }

    public void removeCleanupTask() {
        cleanupTask.cancel();
    }

}
