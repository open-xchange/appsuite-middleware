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

package com.openexchange.rest.services.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.NativeBuilders.MapBuilder;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.GET;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.rest.services.database.migrations.VersionChecker;
import com.openexchange.rest.services.database.transactions.Transaction;
import com.openexchange.rest.services.database.transactions.TransactionKeeper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.sql.DBUtils;
import static com.openexchange.java.util.NativeBuilders.*;
/**
 * The {@link DBRESTService} exposes database access via an HTTP API. See doc/README.md for all the details.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@ROOT("/database")
public class DBRESTService extends OXRESTService<DBRESTService.Environment> {
    
    private static final int MAX_ROWS = 1000;
    private static final int QUERY_LIMIT = 100;
    
    private Connection con = null;
    private Transaction tx = null;
    
    private ConnectionPostProcessor postProcessor = null;

    private List<Statement> statements = new LinkedList<Statement>();
    private List<ResultSet> resultSets = new LinkedList<ResultSet>();
    
    private AccessType accessType = null;
    private Integer ctxId = null;
    private boolean skipVersionNegotiation = false;
    private ConnectionPostProcessor oldPostProcessor;
    private MigrationMetadata migrationMetadata = null;
    private boolean success = false;
    private BeforeHandler beforeHandler = null;
    
    @PUT("/configdb/readOnly")
    public void queryConfigDB() throws OXException {
        returnConnectionWhenDone(AccessType.READ);
        con = dbService().getReadOnly();
        perform();
    }

    @PUT("/configdb/writable")
    public void updateConfigDB() throws OXException {
        returnConnectionWhenDone(AccessType.WRITE);
        con = dbService().getWritable();
        perform();
    }

    @PUT("/oxdb/:ctxId/readOnly")
    public void queryOXDB(int ctxId) throws OXException {
        returnConnectionWhenDone(AccessType.READ, ctxId);
        con = dbService().getReadOnly(ctxId);
        
        perform();
    }

    @PUT("/oxdb/:ctxId/writable")
    public void updateOXDB(int ctxId) throws OXException {
        returnConnectionWhenDone(AccessType.WRITE, ctxId);
        con = dbService().getWritable(ctxId);
        
        perform();
    }

    @PUT("/transaction/:transactionId")
    public void queryTransaction(String txId) throws OXException {
        tx = context.transactions.getTransaction(txId);
        if (tx == null) {
            halt(404);
        }
        postProcessor = new TransactionCloser(tx);
        
        con = tx.getConnection();
        
        perform();
    }

    @GET("/transaction/:transactionId/rollback")
    public void rollback(String txId) throws OXException {
        tx = context.transactions.getTransaction(txId);
        if (tx == null) {
            halt(404);
        }
        postProcessor = new TransactionCloser(tx);

        try {
            context.transactions.rollback(txId);
        } catch (SQLException e) {
            throw RESTDBErrorCodes.SQL_ERROR.create(e.getMessage());
        }
        unpackTransaction();
    }

    @GET("/transaction/:transactionId/commit")
    public void commit(String txId) throws OXException {
        tx = context.transactions.getTransaction(txId);
        if (tx == null) {
            halt(404);
        }
        postProcessor = new TransactionCloser(tx);
        
        try {
            context.transactions.commit(txId);
        } catch (SQLException e) {
            throw RESTDBErrorCodes.SQL_ERROR.create(e.getMessage());
        }
        unpackTransaction();
    }
    
    @PUT("/migration/for/:ctxId/to/:toVersion/forModule/:module")
    public void initialMigration(int ctxId, String toVersion, String module) throws OXException {
        migrate(ctxId, "", toVersion, module);
    }
    
    @PUT("/migration/for/:ctxId/from/:fromVersion/to/:toVersion/forModule/:module")
    public void migrate(int ctxId, final String fromVersion, final String toVersion, final String module) throws OXException {
        finishMigrationWhenDone(ctxId);
        con = services.getService(DatabaseService.class).getForUpdateTask(ctxId);
        migrationMetadata = new MigrationMetadata(ctxId, fromVersion, toVersion, module);
        skipVersionNegotiation = true;
        
        boolean successfullyLocked = context.versions.lock(con, module, System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS));
        if (!successfullyLocked) {
            services.getService(DatabaseService.class).backForUpdateTask(ctxId, con);
            postProcessor = null;
            halt(423); // LOCKED
        }
        
        beforeHandler = new BeforeHandler() {
            
            @Override
            public void before() throws OXException, SQLException {
                String conflictingVersion = context.versions.updateVersion(con, module, fromVersion, toVersion);
                if (conflictingVersion != null) {
                    if (oldPostProcessor != null) {
                        postProcessor = oldPostProcessor;
                    }
                    success = false;
                    header("X-OX-DB-VERSION", conflictingVersion);
                    halt(409);
                }
            }
        };
        
        perform();
    }
    
    @GET("/unlock/for/:ctxId/andModule/:module")
    public void unlock(int ctxId, String module) throws OXException {
        DatabaseService dbs = services.getService(DatabaseService.class);
        Connection con = dbs.getForUpdateTask(ctxId);
        try {
            context.versions.unlock(con, module);
        } finally {
            if (con != null) {
                dbs.backForUpdateTask(ctxId, con);
            }
        }
    }
    
    @Override
    public void after() throws OXException {
        for(ResultSet rs: resultSets) {
            DBUtils.closeSQLStuff(rs);
        }
        for(Statement stmt: statements) {
            DBUtils.closeSQLStuff(stmt);
        }
        if (postProcessor != null) {
            try {
                postProcessor.done(con);
            } catch (SQLException e) {
                respond(500, e.getMessage());
            }
        }
        super.after();
    }
    
    // Common logic

    private void prepare() throws OXException {
        handleTransaction();
        handleVersionNegotiation();
    }
    
    private void perform() throws OXException {
        prepare();
        try {
            beforeQueries();
        } catch (SQLException x) {
            handleSQLException();
        }
        Map<String, Query> queries = getQueries();
        if (queries == null) {
            return;
        }
        Map<String, Object> results = new HashMap<String, Object>();
        if (queries.size() > QUERY_LIMIT) {
            throw RESTDBErrorCodes.QUERY_LIMIT_EXCEEDED.create(QUERY_LIMIT, queries.size());
        }
        for(Map.Entry<String, Query> question: queries.entrySet()) {
            Query query = question.getValue();
            try {
                PreparedStatement stmt = query.prepareFor(con);
                statements.add(stmt);
                if (query.wantsResultSet()) {
                    ResultSet rs = stmt.executeQuery();
                    resultSets.add(rs);
                    
                    Map<String, Object> result = new HashMap<String, Object>();
                    
                    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
                    
                    int count = 0;
                    List<String> columns = new ArrayList<String>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    for(int i = 1, size = metaData.getColumnCount(); i <= size; i++) {
                        columns.add(metaData.getColumnName(i));
                    }
                    while(rs.next() && count < MAX_ROWS) {
                        Map<String, Object> row = new HashMap<String, Object>();
                        for(String colName: columns) {
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
                    results.put(question.getKey(), map().put("updated", rows).build());
                }
                success = true;
            } catch (SQLException x) {
                success = false;
                results.put(question.getKey(), map().put("error", x.getMessage()).put("query", query.query).build());
                respond(map().put("results", results).put("error", x.getMessage()).build());
                handleSQLException();
            }
        }
        
        MapBuilder<Object, Object> response = map().put("results", results);
        if (tx != null) {
            response.put("tx", tx.getID());
        }
        respond(response.build());
    }
    
    private void handleSQLException() {
        try {
            if (tx != null) {
                context.transactions.rollback(tx.getID());
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
            Object data = request.getData();
            if (data instanceof String) {
                queries.put("result", new Query((String)data, Collections.emptyList(), accessType == AccessType.READ));
            } else if (data instanceof JSONObject) {
                JSONObject queryMap = (JSONObject) data;
                for(String queryName: queryMap.keySet()) {
                    Object queryObj = queryMap.get(queryName);
                    if (queryObj instanceof String) {
                        queries.put(queryName, new Query((String)queryObj, Collections.emptyList(), accessType == AccessType.READ));
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
                        queries.put(queryName, new Query(q, params, wantsResultSet));
                    }
                }
            }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage());
        }
        return queries;
    }

    
    private void handleTransaction() throws OXException {
        unpackTransaction();
        if (isSet("keepOpen") && param("keepOpen", boolean.class)) {
            oldPostProcessor = postProcessor;
            postProcessor = null;
            if (tx == null) {
                try {
                    tx = context.transactions.newTransaction(con);
                    tx.put("ctxId", ctxId);
                    tx.put("accessType", accessType);
                    if (migrationMetadata != null) {
                        tx.put("migration", migrationMetadata);
                        tx.extendLifetimeForMigration();
                    }
                } catch (SQLException e) {
                    throw RESTDBErrorCodes.SQL_ERROR.create(e.getMessage());
                }
            }
        } else if (tx == null) {
            try {
                con.setAutoCommit(false);
            } catch (SQLException e) {
                throw RESTDBErrorCodes.SQL_ERROR.create(e.getMessage());
            }
        }
    }
    
    private void unpackTransaction() {
        if (tx != null) {
            tx.extendLifetime();
            skipVersionNegotiation = true;
            migrationMetadata = (MigrationMetadata) tx.getParameter("migration");
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
        String module = request.getHeader("x-ox-db-module");
        String version = request.getHeader("x-ox-db-version");
        if (version == null && module == null) {
            return;
        }
        Object id = null;
        if (ctxId != null) {
            id = "ctx:" + ctxId;
        } else {
            halt(403, "Can not modify the schema of the configdb");
        }
        String conflictingVersion = context.versions.isUpToDate(id, con, module, version);
        if (conflictingVersion != null) {
            postProcessor = oldPostProcessor;
            header("X-OX-DB-VERSION", conflictingVersion);
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
        return services.getService(DatabaseService.class);
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

    private void returnConnectionWhenDone(final AccessType accessType) {
        this.accessType = accessType;
        this.ctxId = null;
        
        this.postProcessor = new ConnectionPostProcessor() {

            @Override
            public void done(Connection con) throws SQLException {
                con.commit();
                con.setAutoCommit(true);
                DatabaseService db = services.getService(DatabaseService.class);
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
                DatabaseService db = services.getService(DatabaseService.class);
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
                context.versions.unlock(con, migrationMetadata.module);
                services.getService(DatabaseService.class).backForUpdateTask(ctxId, con);
            }
        };
    }
    
    private class TransactionCloser implements ConnectionPostProcessor {
        private Transaction tx;
        
        public TransactionCloser(Transaction tx) {
            this.tx = tx;
        }
        
        @Override
        public void done(Connection con) throws SQLException, OXException {
            if (success) {
                context.transactions.commit(tx.getID());
            }
            DatabaseService db = services.getService(DatabaseService.class);
            Integer ctxId = (Integer) tx.getParameter("ctxId");
            if (migrationMetadata != null) {
                if (success) {
                    con.commit();
                }
                con.setAutoCommit(true);
                context.versions.unlock(con, migrationMetadata.module);
                services.getService(DatabaseService.class).backForUpdateTask(ctxId, con);
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
        private String query;
        private List<Object> values;
        private boolean wantsResultSet = false;
        
        public Query(String query, List<Object> values, boolean wantsResultSet) {
            this.query = query;
            this.values = values;
            this.wantsResultSet = wantsResultSet;
        }
        
        public PreparedStatement prepareFor(Connection con) throws SQLException {
            PreparedStatement stmt = con.prepareStatement(query);
            if (values == null) {
                return stmt;
            }
            for(int i = 0, size = values.size(); i < size; i++) {
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
        
        public MigrationMetadata(int ctxId, String fromVersion, String toVersion, String module) {
            super();
            this.ctxId = ctxId;
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.module = module;
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
}
