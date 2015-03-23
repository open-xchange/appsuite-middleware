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

import static com.openexchange.database.DatabaseMocking.connection;
import static com.openexchange.database.DatabaseMocking.verifyConnection;
import static com.openexchange.database.DatabaseMocking.whenConnection;
import static com.openexchange.java.util.NativeBuilders.list;
import static com.openexchange.java.util.NativeBuilders.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.DatabaseMocking.QueryStubBuilder;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.OXRESTService.HALT;
import com.openexchange.rest.services.Response;
import com.openexchange.rest.services.database.DBRESTService.Environment;
import com.openexchange.rest.services.database.migrations.VersionChecker;
import com.openexchange.rest.services.database.transactions.Transaction;
import com.openexchange.rest.services.database.transactions.TransactionKeeper;
import com.openexchange.server.MockingServiceLookup;

/**
 * {@link DBRESTServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DBRESTServiceTest {

    private DatabaseService dbs;
    private TransactionKeeper txs;
    private VersionChecker versionChecker;

    private Connection con;

    private DBRESTService service;

    private final int ctxId = 42;

    private AJAXRequestData req;

    private final int readPoolId = 1;
    private final int writePoolId = 2;
    private final String schema = "mySchema";
    private final int partitionId = 0;


    @Before
    public void setup() {
        MockingServiceLookup services = new MockingServiceLookup();

        con = connection();

        versionChecker = mock(VersionChecker.class);
        txs = mock(TransactionKeeper.class);
        dbs = services.mock(DatabaseService.class);

        req = mock(AJAXRequestData.class);

        service = new DBRESTService();
        service.setContext(new Environment(txs, versionChecker));
        service.setServices(services);
        service.setRequest(req);
    }

    @Test
    public void singleUpdateToConfigDB() throws OXException, JSONException, SQLException {
        when(dbs.getWritable()).thenReturn(con);

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        whenConnection(con).isQueried("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenReturnModifiedRows(1);

        service.before();
        service.updateConfigDB();
        service.after();

        JSONObject result = getQueryResult("result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(con);
    }

    @Test
    public void updateInConfigDBWithSubstitutions() throws OXException, SQLException, JSONException {
        when(dbs.getWritable()).thenReturn(con);

        data(map()
            .put("update", map()
                .put("query", "UPDATE someTable SET someColumn = ? WHERE someIdentifier = ?")
                .put("params", list()
                    .add("someValue", 12)
                    .build())
                .build())
            .build()
        );

        whenConnection(con).isQueried("UPDATE someTable SET someColumn = ? WHERE someIdentifier = ?")
            .withParameter("someValue")
            .andParameter(12)
            .thenReturnModifiedRows(1);

        service.before();
        service.updateConfigDB();
        service.after();

        JSONObject result = getQueryResult("update");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(con);
    }


    @Test
    public void singleUpdateToOXDB() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        whenConnection(con).isQueried("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenReturnModifiedRows(1);

        service.before();
        service.updateOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void queryInOXDBSlave() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        data("SELECT * FROM myTable WHERE user = 12");
        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = 12").thenReturnColumns("id folder displayName").andRow(12, 13, "Charlie").andRow(13, 13, "Linus");

        service.before();
        service.queryOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("result");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());

        assertEquals(12, rows.getJSONObject(0).getInt("id"));
        assertEquals(13, rows.getJSONObject(0).getInt("folder"));
        assertEquals("Charlie", rows.getJSONObject(0).getString("displayName"));

        assertEquals(13, rows.getJSONObject(1).getInt("id"));
        assertEquals(13, rows.getJSONObject(1).getInt("folder"));
        assertEquals("Linus", rows.getJSONObject(1).getString("displayName"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backReadOnly(ctxId, con);
    }

    @Test
    public void updateInMonitoredDB() throws OXException, SQLException, JSONException {

        when(dbs.getWritableMonitored(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        whenConnection(con).isQueried("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenReturnModifiedRows(1);

        service.before();
        service.updateInMonitoredConnection(readPoolId, writePoolId, schema);
        service.after();

        JSONObject result = getQueryResult("result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritableMonitored(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void queryInMonitoredDB() throws Exception {
        when(dbs.getReadOnlyMonitored(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        data("SELECT * FROM myTable WHERE user = 12");
        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = 12").thenReturnColumns("id folder displayName").andRow(12, 13, "Charlie").andRow(13, 13, "Linus");

        service.before();
        service.queryInMonitoredConnection(readPoolId, writePoolId, schema);
        service.after();

        JSONObject result = getQueryResult("result");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());

        assertEquals(12, rows.getJSONObject(0).getInt("id"));
        assertEquals(13, rows.getJSONObject(0).getInt("folder"));
        assertEquals("Charlie", rows.getJSONObject(0).getString("displayName"));

        assertEquals(13, rows.getJSONObject(1).getInt("id"));
        assertEquals(13, rows.getJSONObject(1).getInt("folder"));
        assertEquals("Linus", rows.getJSONObject(1).getString("displayName"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backReadOnlyMonitored(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void monitoredDBConnectionIsClosedProperlyInTransaction() throws Exception {
        int readPoolId = 1;
        int writePoolId = 2;
        String schema = "mySchema";
        int partitionId = 0;

        when(dbs.getWritableMonitored(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        param("keepOpen", true, boolean.class);

        whenConnection(con).isQueried("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenReturnModifiedRows(1);

        service.before();
        service.updateInMonitoredConnection(readPoolId, writePoolId, schema);
        service.after();

        assertNotNull(tx.getParameter("monitoredMetadata"));

        newRequest();

        tx.setConnection(con);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        data("UPDATE someOtherTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        whenConnection(con).isQueried("UPDATE someOtherTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenReturnModifiedRows(1);

        service.before();
        service.queryTransaction(tx.getID());
        service.after();

        verify(dbs).backWritableMonitored(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void numberOfRowsExceedsLimit() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        data("SELECT * FROM myTable WHERE user = 12");
        QueryStubBuilder queryBuilder = whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = 12").thenReturnColumns("id folder displayName");
        for(int i = 0; i < 1100; i++) {
            queryBuilder.andRow(i, i*2, "Charlie");
        }

        service.before();
        service.queryOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("result");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(1000, rows.length());
        assertTrue(result.getBoolean("exceeded"));
    }

    @Test
    public void queryInOXDBMaster() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        data(map().put("query", map()
            .put("query", "SELECT * FROM myTable WHERE user = ?")
            .put("params", list()
                .add(12)
                .build())
            .put("resultSet", true)
            .build())
        .build());

        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(12, 13, "Charlie")
            .andRow(13, 13, "Linus");

        service.before();
        service.updateOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("query");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void batchReadInOXDB() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        data(map()
            .put("query", map()
                .put("query", "SELECT * FROM myTable WHERE user = ?")
                .put("params", list()
                    .add(12)
                    .build())
                .build())
            .put("query2", map()
                .put("query", "SELECT * FROM myOtherTable WHERE user = ?")
                .put("params", list()
                    .add(12)
                    .build())
                .build())
            .build()
        );

        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(12, 13, "Charlie")
            .andRow(13, 13, "Linus");

        whenConnection(con).isQueried("SELECT * FROM myOtherTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(24, 23, "Charlie")
            .andRow(25, 23, "Linus");

        service.before();
        service.queryOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("query");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());
        assertEquals(12, rows.getJSONObject(0).getInt("id"));
        assertEquals(13, rows.getJSONObject(1).getInt("id"));

        result = getQueryResult("query2");
        rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());
        assertEquals(24, rows.getJSONObject(0).getInt("id"));
        assertEquals(25, rows.getJSONObject(1).getInt("id"));


        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backReadOnly(ctxId, con);
    }

    @Test
    public void batchWriteInOXDB() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        data(map()
            .put("update1", map()
                .put("query", "UPDATE myTable SET displayName = ? WHERE id = ?")
                .put("params", list()
                    .add("newDisplayName1")
                    .add(12)
                    .build())
                .build())
            .put("update2", map()
                .put("query", "UPDATE myTable SET displayName = ? WHERE id = ?")
                .put("params", list()
                    .add("newDisplayName2")
                    .add(13)
                    .build())
                .build())
            .build()
        );

        whenConnection(con).isQueried("UPDATE myTable SET displayName = ? WHERE id = ?")
            .withParameter("newDisplayName1")
            .withParameter(12)
            .thenReturnModifiedRows(1);

        whenConnection(con).isQueried("UPDATE myTable SET displayName = ? WHERE id = ?")
            .withParameter("newDisplayName2")
            .withParameter(13)
            .thenReturnModifiedRows(2);

        service.before();
        service.updateOXDB(ctxId);
        service.after();

        JSONObject result = getQueryResult("update1");
        assertEquals(1, result.getInt("updated"));

        result = getQueryResult("update2");
        assertEquals(2, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void transactionInConfigDB() throws Exception {
        when(dbs.getWritable()).thenReturn(con);

        // Start the transaction with a select
        param("keepOpen", true, boolean.class);
        data(map()
            .put("query", map()
                .put("query", "SELECT FOR UPDATE value FROM table WHERE key = ?")
                .put("params", list().add(12).build())
                .put("resultSet", true)
                .build())
        .build());

        whenConnection(con).isQueried("SELECT FOR UPDATE value FROM table WHERE key = ?").withParameter(12).thenReturnColumns("value").withRow(5);

        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        service.before();
        service.updateConfigDB();
        service.after();

        verify(dbs, never()).backWritable(ctxId, con);
        verify(con, never()).commit();
        verify(con, never()).setAutoCommit(true);

        String txId = getTransactionID();
        assertEquals(tx.getID(), txId);

        // Now Update
        newRequest();

        data(map()
            .put("update", map()
                .put("query", "UPDATE table SET value = ? WHERE key = ?")
                .put("params", list().add(10, 12).build())
                .build())
        .build());

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        whenConnection(con).isQueried("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12).thenReturnModifiedRows(1);

        service.before();
        service.queryTransaction(txId);
        service.after();

        verifyConnection(con).receivedQuery("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12);
        verify(dbs).backWritable(con);
        verify(txs).commit(txId);
    }

    @Test
    public void transactionInOXDB() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        // Start the transaction with a select
        param("keepOpen", true, boolean.class);
        data(map()
            .put("query", map()
                .put("query", "SELECT FOR UPDATE value FROM table WHERE key = ?")
                .put("params", list().add(12).build())
                .put("resultSet", true)
                .build())
        .build());

        whenConnection(con).isQueried("SELECT FOR UPDATE value FROM table WHERE key = ?").withParameter(12).thenReturnColumns("value").withRow(5);

        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        service.before();
        service.updateOXDB(ctxId);
        service.after();

        verify(dbs, never()).backWritable(ctxId, con);
        verify(con, never()).commit();
        verify(con, never()).setAutoCommit(true);

        String txId = getTransactionID();
        assertEquals(tx.getID(), txId);

        // Now Update
        newRequest();

        data(map()
            .put("update", map()
                .put("query", "UPDATE table SET value = ? WHERE key = ?")
                .put("params", list().add(10, 12).build())
                .build())
        .build());

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        whenConnection(con).isQueried("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12).thenReturnModifiedRows(1);

        service.before();
        service.queryTransaction(txId);
        service.after();

        verifyConnection(con).receivedQuery("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12);
        verify(dbs).backWritable(ctxId, con);
        verify(txs).commit(txId);

    }

    @Test
    public void transactionRollBack() throws Exception {

        Transaction tx = new Transaction(con, txs);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        service.rollback(tx.getID());

        verify(txs).rollback(tx.getID());
    }

    @Test
    public void transactionCommit() throws Exception {

        Transaction tx = new Transaction(con, txs);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        service.commit(tx.getID());

        verify(txs).commit(tx.getID());
    }

    @Test
    public void testUnknownTransaction() throws Exception {
        when(txs.getTransaction("unnknownTransaction")).thenReturn(null);
        try {
            service.queryTransaction("unknownTransaction");
            fail("Not halted");
        } catch (HALT h) {
            assertEquals(404, service.getResponse().getStatus());
        }

        try {
            service.queryTransaction("unknownTransaction");
            fail("Not halted");
        } catch (HALT h) {
            assertEquals(404, service.getResponse().getStatus());
        }

        try {
            service.commit("unknownTransaction");
            fail("Not halted");
        } catch (HALT h) {
            assertEquals(404, service.getResponse().getStatus());
        }

        try {
            service.rollback("unknownTransaction");
            fail("Not halted");
        } catch (HALT h) {
            assertEquals(404, service.getResponse().getStatus());
        }
    }

    @Test
    public void testFailingStatement() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        whenConnection(con).isQueried("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12").thenFail();

        try {
            service.before();
            service.updateOXDB(ctxId);
            fail("Should have halted on SQL Error");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        JSONObject response = getResponseObject();
        assertEquals("Kabooom!", response.getString("error"));

        verify(con).rollback();
        verify(dbs).backWritable(ctxId, con);
    }


    @Test
    public void testFailingStatementRollsBackRunningTransaction() throws Exception{
        Transaction tx = new Transaction(con, txs);

        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        data(map()
            .put("update", map()
                     .put("query", "UPDATE table SET column = 'value' WHERE cid = ?")
                     .put("params", list().add(1).build())
                .build())
        .build());
        whenConnection(con).isQueried("UPDATE table SET column = 'value' WHERE cid = ?").withParameter(1).thenFail();

        try {
            service.before();
            service.queryTransaction(tx.getID());
            fail("Should have halted on SQL Error");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        verify(txs).rollback(tx.getID());
    }


    @Test
    public void versionNegotiationFails() throws OXException {
        when(dbs.getWritable(ctxId)).thenReturn(con);
        when(versionChecker.isUpToDate(anyObject(), eq(con), eq("com.openexchange.myModule"), eq("2"))).thenReturn("1");

        data("UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12");
        header("x-ox-db-module", "com.openexchange.myModule");
        header("x-ox-db-version", "2");

        try {
            service.before();
            service.updateOXDB(ctxId);
            fail("Should have halted");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        assertEquals(409, service.getResponse().getStatus());
        assertEquals("1", service.getResponse().getHeader("X-OX-DB-VERSION"));
    }

    @Test
    public void migration() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");

        service.before();
        service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
        service.after();

        verify(con).setAutoCommit(false);
        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(con).commit();
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void failingMigration() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        whenConnection(con).isQueried("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))").thenFail();

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        try {
            service.before();
            service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
            fail("Should have halted");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        assertEquals(400, service.getResponse().getStatus());

        verify(con).setAutoCommit(false);
        verify(con).rollback();
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationCanNotProceedBecauseSchemaIsLocked() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(false);

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        try {
            service.before();
            service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
            fail("Should have halted");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        assertEquals(423, service.getResponse().getStatus());

        verify(versionChecker, never()).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationCanNotProceedBecauseTheVersionWasUpdatedInTheMeantime() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        when(versionChecker.updateVersion(con, "com.openexchange.myModule", "1", "2")).thenReturn("2");

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        try {
            service.before();
            service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
            fail("Should have halted");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        assertEquals(409, service.getResponse().getStatus());
        assertEquals("2", service.getResponse().getHeader("X-OX-DB-VERSION"));

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationAcrossTransactions() throws OXException, SQLException, JSONException {
        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);

        whenConnection(con).isQueried("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))").thenReturnModifiedRows(0);

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        param("keepOpen", true, boolean.class);

        service.before();
        service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
        service.after();

        String txId = getTransactionID();
        assertEquals(tx.getID(), txId);

        verify(con, never()).commit();
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");



        newRequest();

        data("CREATE TABLE myModule_myTable2 (myAttribute TEXT)");
        whenConnection(con).isQueried("CREATE TABLE myModule_myTable2 (myAttribute TEXT)").thenReturnModifiedRows(0);

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        service.before();
        service.queryTransaction(txId);
        service.after();

        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable2 (myAttribute TEXT)");
        verify(txs).commit(txId);
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");


    }

    @Test
    public void failingMigrationAcrossTransactions() throws Exception {
        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);

        whenConnection(con).isQueried("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))").thenReturnModifiedRows(0);

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        param("keepOpen", true, boolean.class);

        service.before();
        service.migrate(ctxId, "1", "2", "com.openexchange.myModule");
        service.after();

        String txId = getTransactionID();
        assertEquals(tx.getID(), txId);

        verify(con, never()).commit();
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");



        newRequest();

        data("CREATE TABLE myModule_myTable2 (myAttribute TEXT)");
        param("keepOpen", true, boolean.class);

        whenConnection(con).isQueried("CREATE TABLE myModule_myTable2 (myAttribute TEXT)").thenFail();

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        try {
            service.before();
            service.queryTransaction(txId);
            fail("Should have halted on error");
        } catch (HALT h) {

        } finally {
            service.after();
        }

        verify(txs).rollback(txId);
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrateMonitored() throws Exception {
        when(dbs.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");

        service.before();
        service.migrateMonitored(readPoolId, writePoolId, schema, partitionId, "1", "2", "com.openexchange.myModule");
        service.after();

        verify(con).setAutoCommit(false);
        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(con).commit();
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void migrateMonitoredAcrossTransactions() throws Exception {
        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);
        when(dbs.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);

        data("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        param("keepOpen", true, boolean.class);

        service.before();
        service.migrateMonitored(readPoolId, writePoolId, schema, partitionId, "1", "2", "com.openexchange.myModule");
        service.after();

        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");

        newRequest();

        tx.setConnection(con);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        data("CREATE TABLE myModule_myTable2 (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");

        service.before();
        service.queryTransaction(tx.getID());
        service.after();

        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable2 (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void forcedUnlock() throws OXException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);

        service.before();
        service.unlock(ctxId, "com.openexchange.myModule");
        service.after();

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backForUpdateTask(ctxId, con);
    }


    @Test
    public void forceUnlockMonitoredSchema() throws OXException {
        when(dbs.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        service.before();
        service.unlockMonitored(readPoolId, writePoolId, schema, partitionId, "com.openexchange.myModule");
        service.after();

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);

    }

    @Test
    public void insertPartitionIds() throws Exception {
        data(list().add(1,2,3,4,5).build());

        service.before();
        service.insertPartitionIds(writePoolId, schema);
        service.after();

        verify(dbs).initPartitions(writePoolId, schema, 1,2,3,4,5);
    }

    private void newRequest() {
        setup();
    }

    private void data(Object data) {
        when(req.getData()).thenReturn(data);
    }

    private void data(Map map) throws JSONException {
        data(JSONCoercion.coerceToJSON(map));
    }

    private void data(List list) throws JSONException {
        data(JSONCoercion.coerceToJSON(list));
    }

    private void param(String name, String value) {
        when(req.getParameter(name)).thenReturn(value);
        when(req.isSet(name)).thenReturn(true);
    }

    private <T> void param(String name, T value, Class<T> type) throws OXException {
        when(req.getParameter(name, type)).thenReturn(value);
        when(req.isSet(name)).thenReturn(true);
    }

    private void header(String name, String value) {
        when(req.getHeader(name)).thenReturn(value);
    }

    private JSONObject getQueryResult(String qName) throws JSONException {
        Response response = service.getResponse();
        JSONObject jsonObject = new JSONObject(response.getBody().iterator().next());
        JSONObject results = jsonObject.getJSONObject("results");
        return results.getJSONObject(qName);
    }

    private JSONObject getResponseObject() throws JSONException {
        Response response = service.getResponse();
        return new JSONObject(response.getBody().iterator().next());
    }

    private String getTransactionID() throws JSONException {
        Response response = service.getResponse();
        JSONObject jsonObject = new JSONObject(response.getBody().iterator().next());
        return jsonObject.optString("tx");
    }

}
