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

package com.openexchange.rest.services.database.internal;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.database.DatabaseRESTErrorCodes;
import com.openexchange.rest.services.database.migrations.VersionChecker;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaLockTable;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaVersionTable;
import com.openexchange.rest.services.database.transactions.Transaction;
import com.openexchange.rest.services.database.transactions.TransactionKeeper;
import com.openexchange.server.ServiceLookup;
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

    private MigrationMetadata migrationMetadata;
    private MonitoredMetadata monitoredMetadata;

    private BeforeHandler beforeHandler;

    private final RESTRequest request;

    private final ServiceLookup services;
    private final DatabaseEnvironment environment;

    /**
     *
     * Initializes a new {@link DatabaseRESTPerformer}.
     *
     * @param request The RESTRequest
     * @param services The ServiceLookup instance
     * @param environment The DatabaseEnvironment instance containing the TransactionKeeper and the VersionChecker
     *
     * @throws OXException
     */
    public DatabaseRESTPerformer(RESTRequest request, ServiceLookup services, DatabaseEnvironment environment) throws OXException {
        this.request = request;
        this.services = services;
        this.environment = environment;
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
     * Perform an update or a query on the 'configdb'
     *
     * @param accessType The access type
     * @return The response of the query/update
     * @throws OXException
     * @throws JSONException
     */
    public Response performOnConfigDB(DatabaseAccessType accessType) throws OXException, JSONException {
        try {
            switch (accessType) {
                case READ:
                    returnConnectionWhenDone(DatabaseAccessType.READ);
                    connection = dbService().getReadOnly();
                    break;
                case WRITE:
                    returnConnectionWhenDone(DatabaseAccessType.WRITE);
                    connection = dbService().getWritable();
                    break;
            }
            return perform();
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Perform an update or a query on the 'configdb'
     *
     * @param ctxId The context identifier
     * @param accessType The access type
     * @return The response of the query/update
     * @throws OXException
     * @throws JSONException
     */
    public Response performOnOXDB(int ctxId, DatabaseAccessType accessType) throws OXException, JSONException {
        try {
            switch (accessType) {
                case READ:
                    returnConnectionWhenDone(DatabaseAccessType.READ, ctxId);
                    connection = dbService().getReadOnly(ctxId);
                    break;
                case WRITE:
                    returnConnectionWhenDone(DatabaseAccessType.WRITE, ctxId);
                    connection = dbService().getWritable(ctxId);
                    break;
            }
            return perform();
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Perform an update or a query in a monitored db.
     *
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     * @param accessType The access type
     * @return The response of the query/update
     * @throws OXException If the operation fails
     * @throws JSONException
     */
    public Response performInMonitored(int readId, int writeId, String schema, int partitionId, DatabaseAccessType accessType) throws OXException, JSONException {
        try {
            switch (accessType) {
                case READ:
                    returnMonitoredConnectionWhenDone(DatabaseAccessType.READ, readId, writeId, schema, partitionId);
                    connection = dbService().getReadOnlyMonitored(readId, writeId, schema, partitionId);
                    break;
                case WRITE:
                    returnMonitoredConnectionWhenDone(DatabaseAccessType.WRITE, readId, writeId, schema, partitionId);
                    connection = dbService().getWritableMonitored(readId, writeId, schema, partitionId);
                    break;
            }
            return perform();
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
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
        try {
            tx = environment.getTransactionKeeper().getTransaction(txId);
            if (tx == null) {
                halt(Status.NOT_FOUND);
            } else {
                postProcessor = new TransactionCloser(tx);
                connection = tx.getConnection();
            }

            return perform();
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Rolls back the transaction with the specified transaction identifier
     *
     * @param txId The transaction identifier
     * @throws OXException
     */
    public Response rollbackTransaction(String txId) throws OXException {
        try {
            tx = environment.getTransactionKeeper().getTransaction(txId);
            if (tx == null) {
                halt(Status.NOT_FOUND);
            } else {
                postProcessor = new TransactionCloser(tx);
                try {
                    environment.getTransactionKeeper().rollback(txId);
                } catch (SQLException e) {
                    throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
                }
                unpackTransaction();
            }
            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Commits the transaction with the specified transaction identifier
     *
     * @param txId The transaction identifier
     * @throws OXException
     */
    public Response commitTransaction(String txId) throws OXException {
        try {
            tx = environment.getTransactionKeeper().getTransaction(txId);
            if (tx == null) {
                halt(Status.NOT_FOUND);
            } else {
                postProcessor = new TransactionCloser(tx);
                try {
                    environment.getTransactionKeeper().commit(txId);
                } catch (SQLException e) {
                    throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
                }
                unpackTransaction();
            }
            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Initialize a new database schema with the specified name and the specified write pool identifier.
     *
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the initialization of the new schema fails
     */
    public Response initSchema(int writePoolId, String schema) throws OXException {
        try {
            DatabaseService db = dbService();
            db.initMonitoringTables(writePoolId, schema);
            db.initPartitions(writePoolId, schema, 0);

            connection = db.get(writePoolId, schema);

            new CreateServiceSchemaVersionTable().perform(connection);
            new CreateServiceSchemaLockTable().perform(connection);

            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Inserts the partition identifiers to the replication monitor table
     *
     * @param writeId The write identifier referencing the master db server
     * @param schema The name of the schema
     * @throws OXException If the operation fails
     */
    public Response insertPartitionIds(int writeId, String schema) throws OXException {
        try {
            Object data = request.getBody();
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
            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Unlocks a schema/module combination for the specified context identifier.
     *
     * @param ctxId The context identifier
     * @param module The module
     * @throws OXException If the operation fails
     */
    public Response unlock(int ctxId, String module) throws OXException {
        try {
            DatabaseService dbService = dbService();
            connection = dbService.getForUpdateTask(ctxId);
            try {
                environment.getVersionChecker().unlock(connection, module);
            } finally {
                if (connection != null) {
                    dbService.backForUpdateTask(ctxId, connection);
                }
            }
            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
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
    public Response unlockMonitored(int readPoolId, int writePoolId, String schema, int partitionId, String module) throws OXException {
        try {
            DatabaseService dbService = dbService();
            Connection connection = dbService.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId);
            try {
                environment.getVersionChecker().unlock(connection, module);
            } finally {
                if (connection != null) {
                    dbService.backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, connection);
                }
            }
            return compileResponse(Status.OK);
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
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
        try {
            finishMigrationWhenDone(ctxId);
            connection = dbService().getForUpdateTask(ctxId);
            migrationMetadata = new MigrationMetadata(ctxId, fromVersion, toVersion, module);
            skipVersionNegotiation = true;

            boolean successfullyLocked = environment.getVersionChecker().lock(connection, module, System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS));
            if (!successfullyLocked) {
                dbService().backForUpdateTask(ctxId, connection);
                postProcessor = null;
                halt(423); // LOCKED
            }

            beforeHandler = new TrySchemaVersionUpdate(module, fromVersion, toVersion);

            return perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } catch (OXException e) {
            throw e;
        } finally {
            cleanup();
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
        try {
            finishMigrationWhenDone(readId, writeId, schema, partitionId);
            connection = dbService().getWritableMonitoredForUpdateTask(readId, writeId, schema, partitionId);
            migrationMetadata = new MigrationMetadata(fromVersion, toVersion, module);
            monitoredMetadata = new MonitoredMetadata(readId, writeId, schema, partitionId);

            skipVersionNegotiation = true;

            boolean successfullyLocked = environment.getVersionChecker().lock(connection, module, System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS));
            if (!successfullyLocked) {
                dbService().backForUpdateTask(ctxId, connection);
                postProcessor = null;
                halt(423); // LOCKED
            }

            beforeHandler = new TrySchemaVersionUpdate(module, fromVersion, toVersion);

            return perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Performs the execution of the DatabaseQuery objects
     *
     * @return A JSONObject with the results.
     * @throws OXException
     * @throws JSONException
     */
    private Response perform() throws OXException, JSONException {
        prepare();
        try {
            beforeQueries();
        } catch (SQLException x) {
            handleSQLException(new JSONObject());
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
                    JSONObject res = new JSONObject();
                    res.put("updated", rows);

                    if (query.wantsGeneratedKeys()) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        this.resultSets.add(rs);
                        JSONArray keys = new JSONArray();
                        while (rs.next()) {
                            keys.put(rs.getObject(1));
                        }
                        res.put("generatedKeys", keys);
                    }
                    results.put(question.getKey(), res);
                }
                success = true;
            } catch (SQLException x) {
                success = false;

                JSONObject q = new JSONObject();
                q.put("error", x.getMessage());
                q.put("query", query.getQuery());
                results.put(question.getKey(), q);

                JSONObject response = new JSONObject();
                response.put("results", results);
                response.put("error", x.getMessage());

                handleSQLException(response);
            }
        }

        JSONObject response = new JSONObject();
        response.put("results", results);
        if (tx != null) {
            response.put("tx", tx.getID());
        }

        return compileResponse(200, response);
    }

    private void prepare() throws OXException {
        handleTransaction();
        handleVersionNegotiation();
    }

    /**
     * Handles the SQL exception by rolling back the current transaction (if any) and restoring the post processor object
     * Returns a response code of 400
     *
     * @throws OXException
     */
    private void handleSQLException(JSONObject response) throws OXException {
        try {
            if (tx != null) {
                environment.getTransactionKeeper().rollback(tx.getID());
                tx = null;
            } else {
                connection.rollback();
            }
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            success = false;
        } catch (SQLException e) {
            throw DatabaseRESTErrorCodes.SQL_ERROR.create(e.getMessage());
        }

        halt(Status.BAD_REQUEST, response);
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
        Object data = request.getBody();
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
        if (isTransaction()) {
            oldPostProcessor = postProcessor;
            postProcessor = null;
            if (tx == null) {
                try {
                    tx = environment.getTransactionKeeper().newTransaction(connection);
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
        String module = request.getHeader("x-ox-db-module");
        String version = request.getHeader("x-ox-db-version");
        if (version == null && module == null) {
            return;
        }
        Object id = null;
        if (ctxId != null) {
            id = "ctx:" + ctxId;
        } else if (monitoredMetadata != null) {
            id = monitoredMetadata.getID();
        } else {
            halt(Status.FORBIDDEN, "Can not modify the schema of the configdb");
        }
        String conflictingVersion = environment.getVersionChecker().isUpToDate(id, connection, module, version);
        if (conflictingVersion != null) {
            if (oldPostProcessor != null) {
                postProcessor = oldPostProcessor;
            }
            halt(Status.CONFLICT, "X-OX-DB-VERSION", conflictingVersion);
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
        ConnectionPostProcessor postProcessor = this.postProcessor;
        if (postProcessor != null) {
            Connection con = this.connection;
            if (null != con) {
                try {
                    postProcessor.done(con);
                } catch (SQLException e) {
                    halt(Status.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }

    // Utilities

    private DatabaseService dbService() throws OXException {
        return services.getService(DatabaseService.class);
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
    private void returnMonitoredConnectionWhenDone(final DatabaseAccessType accessType, final int readId, final int writeId, final String schema, final int partitionId) {
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
    private void returnConnectionWhenDone(final DatabaseAccessType accessType) {
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
    private void returnConnectionWhenDone(final DatabaseAccessType accessType, final int ctxId) {
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

    private void finishMigrationWhenDone(final int ctxId) {
        this.ctxId = ctxId;
        this.accessType = DatabaseAccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                environment.getVersionChecker().unlock(con, migrationMetadata.module);
                dbService().backForUpdateTask(ctxId, con);
            }
        };
    }

    private void finishMigrationWhenDone(final int readPoolId, final int writePoolId, final String schema, final int partitionId) {
        this.accessType = DatabaseAccessType.WRITE;

        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException, OXException {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                environment.getVersionChecker().unlock(con, migrationMetadata.module);
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
                environment.getTransactionKeeper().commit(tx.getID());
            }
            DatabaseService db = dbService();
            Integer ctxId = (Integer) tx.getParameter("ctxId");

            if (migrationMetadata != null) {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                environment.getVersionChecker().unlock(con, migrationMetadata.module);
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
            String conflictingVersion = environment.getVersionChecker().updateVersion(connection, module, fromVersion, toVersion);
            if (conflictingVersion != null) {
                if (oldPostProcessor != null) {
                    postProcessor = oldPostProcessor;
                }
                success = false;
                halt(Status.CONFLICT, "X-OX-DB-VERSION", conflictingVersion);
            }
        }

    }

    /**
     * Determines whether the request is considered to be part of a transaction
     *
     * @param data The AJAX request data
     * @return true if the request is part of a transaction; false otherwise
     * @throws OXException
     */
    private boolean isTransaction() throws OXException {
        return (request.isParameterSet("keepOpen") && request.getParameter("keepOpen", boolean.class));
    }

    // Error handling & Response

    /**
     * Throws a WebApplicationException with the specified status code and the specified header name and value
     *
     * @param statusCode The status code for the error
     * @param hName Header name
     * @param hValue Header value
     */
    private void halt(Status statusCode, String hName, String hValue) {
        ResponseBuilder builder = Response.status(statusCode).header(hName, hValue);
        addHeaders(builder);
        Response r = builder.build();
        throw new WebApplicationException(r);
    }

    /**
     * Throws a WebApplicationException with the specified status code
     *
     * @param statusCode The status code for the error
     */
    private void halt(Status statusCode) {
        halt(statusCode.getStatusCode());
    }

    /**
     * Throws a WebApplicationException with the specified status code
     *
     * @param statusCode The status code for the error
     */
    private void halt(int statusCode) {
        ResponseBuilder builder = Response.status(statusCode);
        addHeaders(builder);
        Response r = builder.build();
        throw new WebApplicationException(r);
    }

    /**
     * Throws a WebApplicationException with the specified status code and the specified error message
     *
     * @param statusCode The status code for the error
     * @param message The error message
     */
    private void halt(Status statusCode, String message) {
        halt(statusCode.getStatusCode(), message);
    }

    /**
     * Throws a WebApplicationException with the specified status code and the specified error message
     *
     * @param statusCode The status code for the error
     * @param message The error message
     */
    private void halt(int statusCode, String message) {
        ResponseBuilder builder = Response.status(statusCode).entity(message).type(MediaType.TEXT_PLAIN);
        addHeaders(builder);
        Response r = builder.build();
        throw new WebApplicationException(r);
    }

    /**
     * Throws a WebApplicationException with the specified status code and the specified JSONObject body
     *
     * @param statusCode The status code for the error
     * @param entity The error's bodys
     */
    private void halt(Status statusCode, JSONObject entity) {
        halt(statusCode.getStatusCode(), entity);
    }

    /**
     * Throws a WebApplicationException with the specified status code and the specified JSONObject body
     *
     * @param statusCode The status code for the error
     * @param entity The error's bodys
     */
    private void halt(int statusCode, JSONObject entity) {
        ResponseBuilder builder = Response.status(statusCode).entity(entity).type(MediaType.APPLICATION_JSON);
        addHeaders(builder);
        Response r = builder.build();
        throw new WebApplicationException(r);
    }

    /**
     * Adds the 'Pragma', 'Cache-Control' and 'X-OX-ACHTUNG' headers
     *
     * @param r The response builder
     */
    private void addHeaders(ResponseBuilder r) {
        r.header("Pragma", "no-cache")
            .header("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0")
            .header("X-OX-ACHTUNG", "This is an internal API that may change without notice");
    }

    /**
     * Compiles the response object for a successful/error-free response
     *
     * @param statusCode The status code
     * @param entity The body of the response
     * @return The response object
     */
    private Response compileResponse(int statusCode, JSONObject entity) {
        ResponseBuilder builder = Response.status(statusCode).entity(entity).type(MediaType.APPLICATION_JSON);
        addHeaders(builder);
        return builder.build();
    }

    /**
     * Compiles the response object for a successful/error-free response
     *
     * @param statusCode The status code
     * @return The response object
     */
    private Response compileResponse(Status statusCode) {
        ResponseBuilder builder = Response.status(statusCode);
        addHeaders(builder);
        return builder.build();
    }
}
