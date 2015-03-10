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

package com.openexchange.jaxrs.database.internal;

import static com.openexchange.java.util.NativeBuilders.map;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.NativeBuilders.MapBuilder;
import com.openexchange.jaxrs.database.DatabaseRESTErrorCodes;
import com.openexchange.jaxrs.database.migrations.VersionChecker;
import com.openexchange.jaxrs.database.osgi.Services;
import com.openexchange.jaxrs.database.sql.CreateServiceSchemaLockTable;
import com.openexchange.jaxrs.database.sql.CreateServiceSchemaVersionTable;
import com.openexchange.jaxrs.database.transactions.Transaction;
import com.openexchange.jaxrs.database.transactions.TransactionKeeper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DatabaseRESTPerformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
public class DatabaseRESTPerformer {

    private static final int MAX_ROWS = 1000;
    private static final int QUERY_LIMIT = 100;

    private DatabaseAccessType accessType;

    private Connection connection;
    private Transaction tx;

    private ConnectionPostProcessor postProcessor;
    private ConnectionPostProcessor oldPostProcessor;

    private final List<Statement> statements = new LinkedList<Statement>();
    private final List<ResultSet> resultSets = new LinkedList<ResultSet>();

    private Integer ctxId;

    private boolean skipVersionNegotiation;
    private boolean success = false;
    private final boolean transaction;

    private MigrationMetadata migrationMetadata;
    private MonitoredMetadata monitoredMetadata;

    private BeforeHandler beforeHandler;

    private AJAXRequestData ajaxData;

    /**
     * 
     * Initializes a new {@link DatabaseRESTPerformer}.
     * 
     * @param transaction
     * @throws OXException
     */
    public DatabaseRESTPerformer(AJAXRequestData data) throws OXException {
        this.ajaxData = data;
        transaction = isTransaction(data);

    }

    /**
     * Sets the connection
     *
     * @param connection The connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Executes the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @return A JSONObject with the results
     * @throws OXException
     * @throws JSONException
     */
    public Response executeTransaction(String txId) throws OXException, JSONException {
        tx = DatabaseEnvironment.getInstance().getTransactionKeeper().getTransaction(txId);
        if (tx == null) {
            halt(404);
        } else {
            postProcessor = new TransactionCloser(tx);
            connection = tx.getConnection();
        }

        return perform();
    }

    /**
     * Rolls back the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @throws OXException
     */
    public void rollbackTransaction(String txId) throws OXException {
        tx = DatabaseEnvironment.getInstance().getTransactionKeeper().getTransaction(txId);
        if (tx == null) {
            halt(404);
        } else {
            postProcessor = new TransactionCloser(tx);
            try {
                DatabaseEnvironment.getInstance().getTransactionKeeper().rollback(txId);
            } catch (SQLException e) {
                throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
            }
            unpackTransaction();
        }
    }

    /**
     * Commits the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @throws OXException
     */
    public void commitTransaction(String txId) throws OXException {
        tx = DatabaseEnvironment.getInstance().getTransactionKeeper().getTransaction(txId);
        if (tx == null) {
            halt(404);
        } else {
            postProcessor = new TransactionCloser(tx);
            try {
                DatabaseEnvironment.getInstance().getTransactionKeeper().commit(txId);
            } catch (SQLException e) {
                throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
            }
            unpackTransaction();
        }
    }

    /**
     * Initialize a new database schema with the specified name and the specified write pool identifier.
     * 
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the initialization of the new schema fails
     */
    public void initSchema(int writePoolId, String schema) throws OXException {
        DatabaseService db = dbService();
        db.initMonitoringTables(writePoolId, schema);
        db.initPartitions(writePoolId, schema, 0);

        connection = db.get(writePoolId, schema);

        new CreateServiceSchemaVersionTable().perform(connection);
        new CreateServiceSchemaLockTable().perform(connection);
    }

    /**
     * Inserts the partition identifiers to the replication monitor table
     * 
     * @param writeId The write identifier referencing the master db server
     * @param schema The name of the schema
     * @throws OXException If the operation fails
     */
    public void insertPartitionIds(int writeId, String schema) throws OXException {
        Object data = ajaxData.getData();
        if (data instanceof JSONArray) {
            try {
                JSONArray array = (JSONArray) data;
                int[] partitionIds = new int[array.length()];
                for (int i = 0; i < partitionIds.length; i++) {
                    partitionIds[i] = array.getInt(i);
                }

                dbService().initPartitions(writeId, schema, partitionIds);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
            }
        }
    }

    /**
     * Unlocks a schema/module combination for the specified context identifier.
     * 
     * @param ctxId The context identifier
     * @param module The module
     * @throws OXException If the operation fails
     */
    public void unlock(int ctxId, String module) throws OXException {
        DatabaseService dbService = dbService();
        connection = dbService.getForUpdateTask(ctxId);
        try {
            DatabaseEnvironment.getInstance().getVersionChecker().unlock(connection, module);
        } finally {
            if (connection != null) {
                dbService.backForUpdateTask(ctxId, connection);
            }
        }
    }

    /**
     * Unlocks a schema/module combination for the specified context identifier.
     * 
     * @param readPoolId The read pool identifier
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     * @param module The module name
     * @throws OXException If the operation fails
     */
    public void unlockMonitored(int readPoolId, int writePoolId, String schema, int partitionId, String module) throws OXException {
        DatabaseService dbService = dbService();
        Connection connection = dbService.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId);
        try {
            DatabaseEnvironment.getInstance().getVersionChecker().unlock(connection, module);
        } finally {
            if (connection != null) {
                dbService.backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, connection);
            }
        }
    }

    /**
     * Migrate from the specified version to the specified version
     * 
     * @param ctxId The context identifier
     * @param fromVersion Version updating from
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    public Response migrate(int ctxId, String fromVersion, String toVersion, String module) throws OXException {
        finishMigrationWhenDone(ctxId);
        connection = dbService().getForUpdateTask(ctxId);
        migrationMetadata = new MigrationMetadata(ctxId, fromVersion, toVersion, module);
        skipVersionNegotiation = true;

        boolean successfullyLocked = DatabaseEnvironment.getInstance().getVersionChecker().lock(connection, module, System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS));
        if (!successfullyLocked) {
            dbService().backForUpdateTask(ctxId, connection);
            postProcessor = null;
            halt(423); // LOCKED
        }

        beforeHandler = new TrySchemaVersionUpdate(module, fromVersion, toVersion);

        try {
            return perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Migrate from the specified version to the specified version by using a monitored connection
     * 
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The name of the schema
     * @param partitionId The partition identifier
     * @param fromVersion Version updating from
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    public Response migrateMonitored(int readId, int writeId, String schema, int partitionId, String fromVersion, String toVersion, String module) throws OXException {
        finishMigrationWhenDone(readId, writeId, schema, partitionId);
        connection = dbService().getWritableMonitoredForUpdateTask(readId, writeId, schema, partitionId);
        migrationMetadata = new MigrationMetadata(fromVersion, toVersion, module);
        monitoredMetadata = new MonitoredMetadata(readId, writeId, schema, partitionId);

        skipVersionNegotiation = true;

        boolean successfullyLocked = DatabaseEnvironment.getInstance().getVersionChecker().lock(connection, module, System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS));
        if (!successfullyLocked) {
            dbService().backForUpdateTask(ctxId, connection);
            postProcessor = null;
            halt(423); // LOCKED
        }

        beforeHandler = new TrySchemaVersionUpdate(module, fromVersion, toVersion);

        try {
            return perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Performs the execution of the DatabaseQuery objects
     * 
     * @return A JSONObject with the results.
     * @throws OXException
     * @throws JSONException
     */
    public Response perform() throws OXException, JSONException {
        prepare();
        try {
            beforeQueries();
        } catch (SQLException x) {
            handleSQLException(x, new JSONObject());
        }
        Map<String, DatabaseQuery> queries = getQueries();
        if (queries == null) {
            return Response.ok(new JSONObject(), MediaType.APPLICATION_JSON).build();
        }
        JSONObject results = new JSONObject();
        if (queries.size() > QUERY_LIMIT) {
            throw DatabaseRESTErrorCodes.QUERY_LIMIT_EXCEEDED.create(QUERY_LIMIT, queries.size());
        }
        for (Map.Entry<String, DatabaseQuery> question : queries.entrySet()) {
            DatabaseQuery query = question.getValue();
            try {
                PreparedStatement stmt = query.prepareFor(connection);
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
                results.put(question.getKey(), map().put("error", x.getMessage()).put("query", query.getQuery()).build());
                JSONObject response = new JSONObject();
                response.put("results", results);
                response.put("error", x.getMessage());
                return handleSQLException(x, response);
            }
        }

        JSONObject response = new JSONObject();
        response.put("results", results);
        if (tx != null) {
            response.put("tx", tx.getID());
        }

        cleanup();

        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    private void prepare() throws OXException {
        handleTransaction();
        handleVersionNegotiation();
    }

    /**
     * Handles the SQL exception by rolling back the current transaction (if any) and restoring the post processor object
     * Returns a response code of 400
     * TODO: include the response body
     */
    private Response handleSQLException(SQLException x, JSONObject response) {
        String message = x.getMessage();
        try {
            if (tx != null) {
                DatabaseEnvironment.getInstance().getTransactionKeeper().rollback(tx.getID());
                tx = null;
            } else {
                connection.rollback();
            }
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            success = false;
        } catch (SQLException | OXException e) {
            e.printStackTrace();
        }
        return halt(400, message, response);
    }

    /**
     * Extract the queries from the data object
     * 
     * @param ajaxData The data object (either String or JSONObject)
     * @param accessType Defines the access type for the returned queries
     * @return A map with all queries
     * @throws OXException if a JSONException occurs
     */
    @SuppressWarnings("unchecked")
    private Map<String, DatabaseQuery> getQueries() throws OXException {
        Map<String, DatabaseQuery> queries = new HashMap<String, DatabaseQuery>();
        Object data = ajaxData.getData();
        try {
            if (data instanceof String) {
                queries.put("result", new DatabaseQuery((String) data, Collections.emptyList(), accessType == DatabaseAccessType.READ, false));
            } else if (data instanceof JSONObject) {
                JSONObject queryMap = (JSONObject) data;
                for (String queryName : queryMap.keySet()) {
                    Object queryObj = queryMap.get(queryName);
                    if (queryObj instanceof String) {
                        queries.put(queryName, new DatabaseQuery((String) queryObj, Collections.emptyList(), accessType == DatabaseAccessType.READ, false));
                    } else if (queryObj instanceof JSONObject) {
                        JSONObject querySpec = (JSONObject) queryObj;
                        String q = querySpec.getString("query");
                        JSONArray paramsA = querySpec.optJSONArray("params");
                        List<Object> params = null;
                        if (paramsA != null) {
                            params = (List<Object>) JSONCoercion.coerceToNative(paramsA);
                        }
                        boolean wantsResultSet = accessType == DatabaseAccessType.READ;
                        if (querySpec.has("resultSet")) {
                            wantsResultSet = querySpec.getBoolean("resultSet");
                        }
                        boolean wantsGeneratedKeys = false;
                        if (querySpec.has("generatedKeys")) {
                            wantsGeneratedKeys = querySpec.getBoolean("generatedKeys");
                        }
                        queries.put(queryName, new DatabaseQuery(q, params, wantsResultSet, wantsGeneratedKeys));
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
        if (transaction) {
            oldPostProcessor = postProcessor;
            postProcessor = null;
            if (tx == null) {
                try {
                    tx = DatabaseEnvironment.getInstance().getTransactionKeeper().newTransaction(connection);
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
                connection.setAutoCommit(false);
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
                accessType = (DatabaseAccessType) tx.getParameter("accessType");
            }
            connection = tx.getConnection();
        }
    }

    private void handleVersionNegotiation() throws OXException {
        if (skipVersionNegotiation) {
            return;
        }
        String module = ajaxData.getHeader("x-ox-db-module");
        String version = ajaxData.getHeader("x-ox-db-version");
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
        String conflictingVersion = DatabaseEnvironment.getInstance().getVersionChecker().isUpToDate(id, connection, module, version);
        if (conflictingVersion != null) {
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            ajaxData.setHeader("X-OX-DB-VERSION", conflictingVersion);
            halt(409);
        }
    }

    private void beforeQueries() throws OXException, SQLException {
        if (beforeHandler != null) {
            beforeHandler.before();
        }
    }

    /**
     * Performs a cleanup and closes all open ResultSets and Statements,
     * as well as invokes the done() method from the PostProcessor
     * 
     * @throws OXException If the operation fails
     */
    private void cleanup() throws OXException {
        for (ResultSet rs : resultSets) {
            DBUtils.closeSQLStuff(rs);
        }
        for (Statement stmt : statements) {
            DBUtils.closeSQLStuff(stmt);
        }
        if (postProcessor != null) {
            try {
                postProcessor.done(connection);
            } catch (SQLException e) {
                //FIXME: Fill response body but don't send response yet
                halt(500, e.getMessage());
            }
        }
    }

    // Utilities

    private DatabaseService dbService() throws OXException {
        return Services.getService(DatabaseService.class);
    }

    private static interface ConnectionPostProcessor {

        public void done(Connection con) throws SQLException, OXException;
    }

    private static interface BeforeHandler {

        public void before() throws OXException, SQLException;
    }

    /**
     * Returns the monitored connection when the processing is finished.
     * 
     * @param accessType The access type of the database connection
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     */
    public void returnMonitoredConnectionWhenDone(final DatabaseAccessType accessType, final int readId, final int writeId, final String schema, final int partitionId) {
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

    /**
     * Returns the connection when the processing is finished
     * 
     * @param accessType The access type of the database connection
     */
    public void returnConnectionWhenDone(final DatabaseAccessType accessType) {
        this.accessType = accessType;
        this.ctxId = null;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
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

    /**
     * Returns the connection when the processing is finished
     * 
     * @param accessType The access type of the database connection
     * @param ctxId The context identifiers
     */
    public void returnConnectionWhenDone(final DatabaseAccessType accessType, final int ctxId) {
        this.accessType = accessType;
        this.ctxId = ctxId;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
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

    public void finishMigrationWhenDone(final int ctxId) {
        this.ctxId = ctxId;
        this.accessType = DatabaseAccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                DatabaseEnvironment.getInstance().getVersionChecker().unlock(con, migrationMetadata.module);
                dbService().backForUpdateTask(ctxId, con);
            }
        };
    }

    public void finishMigrationWhenDone(final int readPoolId, final int writePoolId, final String schema, final int partitionId) {
        this.accessType = DatabaseAccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                DatabaseEnvironment.getInstance().getVersionChecker().unlock(con, migrationMetadata.module);
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
                DatabaseEnvironment.getInstance().getTransactionKeeper().commit(tx.getID());
            }
            DatabaseService db = dbService();
            Integer ctxId = (Integer) tx.getParameter("ctxId");

            if (migrationMetadata != null) {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                DatabaseEnvironment.getInstance().getVersionChecker().unlock(con, migrationMetadata.module);
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
            } else if (tx.getParameter("accessType") == DatabaseAccessType.READ) {
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
            String conflictingVersion = DatabaseEnvironment.getInstance().getVersionChecker().updateVersion(connection, module, fromVersion, toVersion);
            if (conflictingVersion != null) {
                if (oldPostProcessor != null) {
                    postProcessor = oldPostProcessor;
                }
                success = false;
                ajaxData.setHeader("X-OX-DB-VERSION", conflictingVersion);
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

    private Response halt(int statusCode, String message, JSONObject entity) {
        //header("Transfer-Encoding", "chunked").header("Pragma", "no-cache").header("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0").
        return Response.status(statusCode).entity(entity).header("X-OX-ACHTUNG", "This is an internal API that may change without notice").type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Determines whether the request is considered to be part of a transaction
     * 
     * @param data The AJAX request data
     * @return true if the request is part of a transaction; false otherwise
     * @throws OXException
     */
    private boolean isTransaction(AJAXRequestData data) throws OXException {
        return (data.isSet("keepOpen") && data.getParameter("keepOpen", boolean.class));
    }

}
