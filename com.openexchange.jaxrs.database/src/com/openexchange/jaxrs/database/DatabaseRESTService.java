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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.jaxrs.database;

import static com.openexchange.java.util.NativeBuilders.map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.NativeBuilders.MapBuilder;
import com.openexchange.jaxrs.JAXRSService;
import com.openexchange.jaxrs.database.migrations.DBVersionChecker;
import com.openexchange.jaxrs.database.migrations.VersionChecker;
import com.openexchange.jaxrs.database.transactions.InMemoryTransactionKeeper;
import com.openexchange.jaxrs.database.transactions.Transaction;
import com.openexchange.jaxrs.database.transactions.TransactionKeeper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DatabaseRESTService} exposes database access via an HTTP API. See doc/README.md for all the details.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
@Path("/database/v1")
public class DatabaseRESTService extends JAXRSService {

    private static final int MAX_ROWS = 1000;
    private static final int QUERY_LIMIT = 100;

    private Connection con = null;
    private Transaction tx = null;

    private ConnectionPostProcessor postProcessor = null;

    private final List<Statement> statements = new LinkedList<Statement>();
    private final List<ResultSet> resultSets = new LinkedList<ResultSet>();

    private AccessType accessType = null;
    private Integer ctxId = null;
    private boolean skipVersionNegotiation = false;
    private ConnectionPostProcessor oldPostProcessor;
    private MigrationMetadata migrationMetadata = null;
    private boolean success = false;
    private BeforeHandler beforeHandler = null;
    private MonitoredMetadata monitoredMetadata;

    private final InMemoryTransactionKeeper transactionKeeper;
    private final VersionChecker versionChecker;

    /**
     * Initializes a new {@link DatabaseRESTService}.
     */
    public DatabaseRESTService(ServiceLookup services) {
        super(services);
        transactionKeeper = new InMemoryTransactionKeeper();
        versionChecker = new DBVersionChecker();

        getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                transactionKeeper.tick(System.currentTimeMillis());
            }
        }, 2, 1, TimeUnit.MINUTES);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/readOnly")
    public JSONObject queryConfigDB() throws OXException {
        returnConnectionWhenDone(AccessType.READ);
        con = dbService().getReadOnly();
        try {
            return perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    // Common logic

    private void prepare() throws OXException {
        handleTransaction();
        handleVersionNegotiation();
    }

    private JSONObject perform() throws OXException, JSONException {
        prepare();
        try {
            beforeQueries();
        } catch (SQLException x) {
            handleSQLException();
        }
        Map<String, Query> queries = getQueries();
        if (queries == null) {
            return new JSONObject();
        }
        JSONObject results = new JSONObject();
        if (queries.size() > QUERY_LIMIT) {
            throw DatabaseRESTErrorCodes.QUERY_LIMIT_EXCEEDED.create(QUERY_LIMIT, queries.size());
        }
        for (Map.Entry<String, Query> question : queries.entrySet()) {
            Query query = question.getValue();
            try {
                PreparedStatement stmt = query.prepareFor(con);
                statements.add(stmt);
                if (query.wantsResultSet()) {
                    ResultSet rs = stmt.executeQuery();
                    resultSets.add(rs);

                    JSONObject result = new JSONObject();

                    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

                    int count = 0;
                    List<String> columns = new ArrayList<String>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (int i = 1, size = metaData.getColumnCount(); i <= size; i++) {
                        columns.add(metaData.getColumnName(i));
                    }

                    while (rs.next() && count < MAX_ROWS) {
                        Map<String, Object> row = new HashMap<String, Object>();
                        for (String colName : columns) {
                            row.put(colName, rs.getObject(colName));
                        }
                        rows.add(row);
                        count++;
                    }
                    if (count == MAX_ROWS && rs.next()) {
                        result.put("exceeded", true);
                    }
                    result.put("rows", rows);

                    results.put(question.getKey(), result);
                } else {
                    int rows = stmt.executeUpdate();
                    MapBuilder<Object, Object> res = map().put("updated", rows);

                    if (query.wantsGeneratedKeys()) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        this.resultSets.add(rs);
                        JSONArray keys = new JSONArray();
                        while (rs.next()) {
                            keys.put(rs.getObject(1));
                        }
                        res.put("generatedKeys", keys);
                    }
                    results.put(question.getKey(), res.build());
                }
                success = true;
            } catch (SQLException x) {
                success = false;
                results.put(question.getKey(), map().put("error", x.getMessage()).put("query", query.query).build());
                JSONObject response = new JSONObject();
                response.put("results", results);
                response.put("error", x.getMessage());
                handleSQLException();
                return response;
            }
        }

        JSONObject response = new JSONObject();
        response.put("results", results);
        if (tx != null) {
            response.put("tx", tx.getID());
        }
        return response;
    }

    private void handleSQLException() {
        try {
            if (tx != null) {
                transactionKeeper.rollback(tx.getID());
                tx = null;
            } else {
                con.rollback();
            }
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            success = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        halt(400);
    }

    private Map<String, Query> getQueries() throws OXException {
        Map<String, Query> queries = new HashMap<String, Query>();
        try {
            Object data = getAJAXRequestData().getData();
            if (data instanceof String) {
                queries.put("result", new Query((String) data, Collections.emptyList(), accessType == AccessType.READ, false));
            } else if (data instanceof JSONObject) {
                JSONObject queryMap = (JSONObject) data;
                for (String queryName : queryMap.keySet()) {
                    Object queryObj = queryMap.get(queryName);
                    if (queryObj instanceof String) {
                        queries.put(queryName, new Query((String) queryObj, Collections.emptyList(), accessType == AccessType.READ, false));
                    } else if (queryObj instanceof JSONObject) {
                        JSONObject querySpec = (JSONObject) queryObj;
                        String q = querySpec.getString("query");
                        JSONArray paramsA = querySpec.optJSONArray("params");
                        List<Object> params = null;
                        if (paramsA != null) {
                            params = (List<Object>) JSONCoercion.coerceToNative(paramsA);
                        }
                        boolean wantsResultSet = accessType == AccessType.READ;
                        if (querySpec.has("resultSet")) {
                            wantsResultSet = querySpec.getBoolean("resultSet");
                        }
                        boolean wantsGeneratedKeys = false;
                        if (querySpec.has("generatedKeys")) {
                            wantsGeneratedKeys = querySpec.getBoolean("generatedKeys");
                        }
                        queries.put(queryName, new Query(q, params, wantsResultSet, wantsGeneratedKeys));
                    }
                }
            }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x, x.getMessage());
        }
        return queries;
    }

    private void handleTransaction() throws OXException {
        unpackTransaction();
        if (getAJAXRequestData().isSet("keepOpen") && getAJAXRequestData().getParameter("keepOpen", boolean.class)) {
            oldPostProcessor = postProcessor;
            postProcessor = null;
            if (tx == null) {
                try {
                    tx = transactionKeeper.newTransaction(con);
                    tx.put("ctxId", ctxId);
                    tx.put("accessType", accessType);
                    if (migrationMetadata != null) {
                        tx.put("migration", migrationMetadata);
                        tx.extendLifetimeForMigration();
                    }
                    if (monitoredMetadata != null) {
                        tx.put("monitoredMetadata", monitoredMetadata);
                    }
                } catch (SQLException e) {
                    throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
                }
            }
        } else if (tx == null) {
            try {
                con.setAutoCommit(false);
            } catch (SQLException e) {
                throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
            }
        }
    }

    private void unpackTransaction() {
        if (tx != null) {
            tx.extendLifetime();
            skipVersionNegotiation = true;
            migrationMetadata = (MigrationMetadata) tx.getParameter("migration");
            monitoredMetadata = (MonitoredMetadata) tx.getParameter("monitoredMetadata");
            if (tx.getParameter("ctxId") != null) {
                ctxId = (Integer) tx.getParameter("ctxId");
            }
            if (tx.getParameter("accessType") != null) {
                accessType = (AccessType) tx.getParameter("accessType");
            }
            con = tx.getConnection();
        }
    }

    private void handleVersionNegotiation() throws OXException {
        if (skipVersionNegotiation) {
            return;
        }
        String module = getAJAXRequestData().getHeader("x-ox-db-module");
        String version = getAJAXRequestData().getHeader("x-ox-db-version");
        if (version == null && module == null) {
            return;
        }
        Object id = null;
        if (ctxId != null) {
            id = "ctx:" + ctxId;
        } else if (monitoredMetadata != null) {
            id = monitoredMetadata.getID();
        } else {
            halt(403, "Can not modify the schema of the configdb");
        }
        String conflictingVersion = versionChecker.isUpToDate(id, con, module, version);
        if (conflictingVersion != null) {
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            getAJAXRequestData().setHeader("X-OX-DB-VERSION", conflictingVersion);
            halt(409);
        }
    }

    private void beforeQueries() throws OXException, SQLException {
        if (beforeHandler != null) {
            beforeHandler.before();
        }
    }

    // Utilities

    private DatabaseService dbService() {
        return getService(DatabaseService.class);
    }

    private static interface ConnectionPostProcessor {

        public void done(Connection con) throws SQLException, OXException;

    }

    private static interface BeforeHandler {

        public void before() throws OXException, SQLException;
    }

    private static enum AccessType {
        READ, WRITE
    }

    private void returnMonitoredConnectionWhenDone(final AccessType accessType, final int readId, final int writeId, final String schema, final int partitionId) {
        this.accessType = accessType;
        this.monitoredMetadata = new MonitoredMetadata(readId, writeId, schema, partitionId);

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                con.commit();
                con.setAutoCommit(true);
                DatabaseService db = dbService();
                switch (accessType) {
                    case READ:
                        db.backReadOnlyMonitored(readId, writeId, schema, partitionId, con);
                        break;
                    case WRITE:
                        db.backWritableMonitored(readId, writeId, schema, partitionId, con);
                        break;
                }
            }
        };
    }

    private void returnConnectionWhenDone(final AccessType accessType) {
        this.accessType = accessType;
        this.ctxId = null;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException {
                con.commit();
                con.setAutoCommit(true);
                DatabaseService db = dbService();
                switch (accessType) {
                    case READ:
                        db.backReadOnly(con);
                        break;
                    case WRITE:
                        db.backWritable(con);
                        break;
                }
            }
        };
    }

    private void returnConnectionWhenDone(final AccessType accessType, final int ctxId) {
        this.accessType = accessType;
        this.ctxId = ctxId;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException {
                con.commit();
                con.setAutoCommit(true);
                DatabaseService db = dbService();
                switch (accessType) {
                    case READ:
                        db.backReadOnly(ctxId, con);
                        break;
                    case WRITE:
                        db.backWritable(ctxId, con);
                        break;
                }
            }
        };
    }

    private void finishMigrationWhenDone(final int ctxId) {
        this.ctxId = ctxId;
        this.accessType = AccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                versionChecker.unlock(con, migrationMetadata.module);
                dbService().backForUpdateTask(ctxId, con);
            }
        };
    }

    private void finishMigrationWhenDone(final int readPoolId, final int writePoolId, final String schema, final int partitionId) {
        this.accessType = AccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                versionChecker.unlock(con, migrationMetadata.module);
                dbService().backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);
            }
        };
    }

    private class TransactionCloser implements ConnectionPostProcessor {

        private final Transaction tx;

        public TransactionCloser(Transaction tx) {
            this.tx = tx;
        }

        @Override
        public void done(Connection con) throws SQLException, OXException {
            if (success) {
                transactionKeeper.commit(tx.getID());
            }
            DatabaseService db = dbService();
            Integer ctxId = (Integer) tx.getParameter("ctxId");

            if (migrationMetadata != null) {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                versionChecker.unlock(con, migrationMetadata.module);
            }

            if (monitoredMetadata != null) {
                switch (accessType) {
                    case READ:
                        db.backReadOnlyMonitored(monitoredMetadata.readId, monitoredMetadata.writeId, monitoredMetadata.schema, monitoredMetadata.partitionId, con);
                        break;
                    case WRITE:
                        if (migrationMetadata != null) {
                            db.backWritableMonitoredForUpdateTask(monitoredMetadata.readId, monitoredMetadata.writeId, monitoredMetadata.schema, monitoredMetadata.partitionId, con);

                        } else {
                            db.backWritableMonitored(monitoredMetadata.readId, monitoredMetadata.writeId, monitoredMetadata.schema, monitoredMetadata.partitionId, con);
                        }
                        break;
                }
            } else if (migrationMetadata != null) {
                dbService().backForUpdateTask(ctxId, con);
            } else if (tx.getParameter("accessType") == AccessType.READ) {
                con.commit();
                con.setAutoCommit(true);
                if (ctxId == null) {
                    db.backReadOnly(con);
                } else {
                    db.backReadOnly(ctxId, con);
                }
            } else {
                con.commit();
                con.setAutoCommit(true);
                if (ctxId == null) {
                    db.backWritable(con);
                } else {
                    db.backWritable(ctxId, con);
                }
            }
        }

    }

    private static class Query {

        private final String query;
        private final List<Object> values;
        private boolean wantsResultSet = false;
        private boolean wantsGeneratedKeys = false;

        public Query(String query, List<Object> values, boolean wantsResultSet, boolean wantsGeneratedKeys) {
            this.query = query;
            this.values = values;
            this.wantsResultSet = wantsResultSet;
            this.wantsGeneratedKeys = wantsGeneratedKeys;
        }

        public boolean wantsGeneratedKeys() {
            return wantsGeneratedKeys;
        }

        public PreparedStatement prepareFor(Connection con) throws SQLException {
            PreparedStatement stmt = wantsGeneratedKeys ? con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : con.prepareStatement(query);
            if (values == null) {
                return stmt;
            }
            for (int i = 0, size = values.size(); i < size; i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            return stmt;
        }

        public boolean wantsResultSet() {
            return wantsResultSet;
        }
    }

    private static class MigrationMetadata {

        public int ctxId;
        public String fromVersion, toVersion, module;

        public MigrationMetadata(String fromVersion, String toVersion, String module) {
            super();
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.module = module;
        }

        public MigrationMetadata(int ctxId, String fromVersion, String toVersion, String module) {
            super();
            this.ctxId = ctxId;
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.module = module;
        }
    }

    private static class MonitoredMetadata {

        public int readId;
        public int writeId;
        public String schema;
        public int partitionId;

        public MonitoredMetadata(int readId, int writeId, String schema, int partitionId) {
            this.readId = readId;
            this.writeId = writeId;
            this.schema = schema;
            this.partitionId = partitionId;
        }

        public String getID() {
            return readId + ":" + writeId + ":" + schema;
        }

    }

    public static final class Environment {

        public TransactionKeeper transactions;
        public VersionChecker versions;

        public Environment(TransactionKeeper transactions, VersionChecker versions) {
            this.transactions = transactions;
            this.versions = versions;
        }

    }

    private class TrySchemaVersionUpdate implements BeforeHandler {

        private final String module;
        private final String fromVersion;
        private final String toVersion;

        public TrySchemaVersionUpdate(String module, String fromVersion, String toVersion) {
            super();
            this.module = module;
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }

        @Override
        public void before() throws OXException, SQLException {
            String conflictingVersion = versionChecker.updateVersion(con, module, fromVersion, toVersion);
            if (conflictingVersion != null) {
                if (oldPostProcessor != null) {
                    postProcessor = oldPostProcessor;
                }
                success = false;
                getAJAXRequestData().setHeader("X-OX-DB-VERSION", conflictingVersion);
                halt(409);
            }
        }

    }

    private void halt(int statusCode) {
        throw new WebApplicationException(statusCode);
    }

    private void halt(int statusCode, String message) {
        throw new WebApplicationException(message, statusCode);
    }
    
    private void halt(int statusCode, String message, JSONObject entity) {
        Response r = Response.status(statusCode).entity(entity).type("application/json").build();
        throw new WebApplicationException(message, r);
    }

}
