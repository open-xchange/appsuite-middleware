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

package com.openexchange.rest.services.database;

import static com.openexchange.database.DatabaseMocking.connection;
import static com.openexchange.database.DatabaseMocking.verifyConnection;
import static com.openexchange.database.DatabaseMocking.whenConnection;
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
import java.util.Collections;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.database.DatabaseMocking.QueryStubBuilder;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.database.DatabaseRESTService;
import com.openexchange.rest.services.database.internal.DatabaseEnvironment;
import com.openexchange.rest.services.database.migrations.VersionChecker;
import com.openexchange.rest.services.database.transactions.InMemoryTransactionKeeper;
import com.openexchange.rest.services.database.transactions.Transaction;
import com.openexchange.server.MockingServiceLookup;

/**
 * {@link DBRESTServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DBRESTServiceTest {

    private DatabaseService dbs;
    private InMemoryTransactionKeeper txs;
    private VersionChecker versionChecker;
    private DatabaseEnvironment environment;

    private Connection con;

    private DatabaseRESTService service;

    private final int ctxId = 42;

    private final int readPoolId = 1;
    private final int writePoolId = 2;
    private final String schema = "mySchema";
    private final int partitionId = 0;

    private UriInfo uriInfo;
    private HttpHeaders httpHeaders;
    MultivaluedMap<String, String> mmap;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        MockingServiceLookup services = new MockingServiceLookup();

        con = connection();

        versionChecker = mock(VersionChecker.class);
        txs = mock(InMemoryTransactionKeeper.class);
        dbs = services.mock(DatabaseService.class);
        
        environment = mock(DatabaseEnvironment.class);
        
        service = new DatabaseRESTService(services, new DatabaseEnvironment(txs, versionChecker));

        mmap = mock(MultivaluedMap.class);

        uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(mmap);

        httpHeaders = mock(HttpHeaders.class);
    }

    @Test
    public void singleUpdateToConfigDB() throws OXException, JSONException, SQLException {
        when(dbs.getWritable()).thenReturn(con);

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(1);

        Response response = service.updateConfigDB(httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(con);
    }

    @Test
    public void updateInConfigDBWithSubstitutions() throws OXException, SQLException, JSONException {
        when(dbs.getWritable()).thenReturn(con);

        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "UPDATE someTable SET someColumn = ? WHERE someIdentifier = ?");
            q.put("params", new JSONArray("['someValue', 12]"));

            body.put("update", q);
        }

        whenConnection(con).isQueried("UPDATE someTable SET someColumn = ? WHERE someIdentifier = ?")
            .withParameter("someValue")
            .andParameter(12)
            .thenReturnModifiedRows(1);

        Response response = service.updateConfigDB(httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "update");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(con);
    }

    @Test
    public void singleUpdateToOXDB() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(1);

        Response response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void queryInOXDBSlave() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        String body = "SELECT * FROM myTable WHERE user = 12";
        whenConnection(con).isQueried(body).thenReturnColumns("id folder displayName").andRow(12, 13, "Charlie").andRow(13, 13, "Linus");

        Response response = service.queryOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");
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

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(1);

        Response response = service.updateInMonitoredConnection(readPoolId, writePoolId, schema, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");

        assertEquals(1, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritableMonitored(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void queryInMonitoredDB() throws Exception {
        when(dbs.getReadOnlyMonitored(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        String body = "SELECT * FROM myTable WHERE user = 12";
        whenConnection(con).isQueried(body).thenReturnColumns("id folder displayName").andRow(12, 13, "Charlie").andRow(13, 13, "Linus");

        Response response = service.queryInMonitoredConnection(readPoolId, writePoolId, schema, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");
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

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        param("keepOpen", "true");

        whenConnection(con).isQueried(body).thenReturnModifiedRows(1);

        Response response = service.updateInMonitoredConnection(readPoolId, writePoolId, schema, httpHeaders, uriInfo, body);

        assertNotNull(tx.getParameter("monitoredMetadata"));

        newRequest();

        tx.setConnection(con);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        body = "UPDATE someOtherTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(1);

        service.queryTransaction(tx.getID(), httpHeaders, uriInfo, body);

        verify(dbs).backWritableMonitored(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void numberOfRowsExceedsLimit() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        String body = "SELECT * FROM myTable WHERE user = 12";
        QueryStubBuilder queryBuilder = whenConnection(con).isQueried(body).thenReturnColumns("id folder displayName");
        for (int i = 0; i < 1100; i++) {
            queryBuilder.andRow(i, i * 2, "Charlie");
        }

        Response response = service.queryOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "result");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(1000, rows.length());
        assertTrue(result.getBoolean("exceeded"));
    }

    @Test
    public void queryInOXDBMaster() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "SELECT * FROM myTable WHERE user = ?");
            q.put("params", new JSONArray("[12]"));
            q.put("resultSet", true);
            body.put("query", q);
        }

        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(12, 13, "Charlie")
            .andRow(13, 13, "Linus");

        Response response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "query");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void batchReadInOXDB() throws Exception {
        when(dbs.getReadOnly(ctxId)).thenReturn(con);

        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "SELECT * FROM myTable WHERE user = ?");
            q.put("params", new JSONArray("[12]"));
            body.put("q1", q);
            q = new JSONObject();
            q.put("query", "SELECT * FROM myOtherTable WHERE user = ?");
            q.put("params", new JSONArray("[12]"));
            body.put("q2", q);
        }

        whenConnection(con).isQueried("SELECT * FROM myTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(12, 13, "Charlie")
            .andRow(13, 13, "Linus");

        whenConnection(con).isQueried("SELECT * FROM myOtherTable WHERE user = ?").withParameter(12)
            .thenReturnColumns("id folder displayName")
            .andRow(24, 23, "Charlie")
            .andRow(25, 23, "Linus");

        Response response = service.queryOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "q1");
        JSONArray rows = result.getJSONArray("rows");

        assertEquals(2, rows.length());
        assertEquals(12, rows.getJSONObject(0).getInt("id"));
        assertEquals(13, rows.getJSONObject(1).getInt("id"));

        result = getQueryResult(response, "q2");
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

        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "UPDATE myTable SET displayName = ? WHERE id = ?");
            q.put("params", new JSONArray("['newDisplayName1', 12]"));
            body.put("u1", q);
            q = new JSONObject();
            q.put("query", "UPDATE myTable SET displayName = ? WHERE id = ?");
            q.put("params", new JSONArray("['newDisplayName2', 13]"));
            body.put("u2", q);
        }

        whenConnection(con).isQueried("UPDATE myTable SET displayName = ? WHERE id = ?")
            .withParameter("newDisplayName1")
            .withParameter(12)
            .thenReturnModifiedRows(1);

        whenConnection(con).isQueried("UPDATE myTable SET displayName = ? WHERE id = ?")
            .withParameter("newDisplayName2")
            .withParameter(13)
            .thenReturnModifiedRows(2);

        Response response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);

        JSONObject result = getQueryResult(response, "u1");
        assertEquals(1, result.getInt("updated"));

        result = getQueryResult(response, "u2");
        assertEquals(2, result.getInt("updated"));

        verify(con).setAutoCommit(true);
        verify(con).commit();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void transactionInConfigDB() throws Exception {
        when(dbs.getWritable()).thenReturn(con);

        // Start the transaction with a select
        param("keepOpen", "true");

        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "SELECT FOR UPDATE value FROM table WHERE key = ?");
            q.put("params", new JSONArray("[12]"));
            q.put("resultSet", true);
            body.put("query", q);
        }

        whenConnection(con).isQueried("SELECT FOR UPDATE value FROM table WHERE key = ?").withParameter(12).thenReturnColumns("value").withRow(5);

        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        Response response = service.updateConfigDB(httpHeaders, uriInfo, body);

        verify(dbs, never()).backWritable(ctxId, con);
        verify(con, never()).commit();
        verify(con, never()).setAutoCommit(true);

        String txId = getTransactionID(response);
        assertEquals(tx.getID(), txId);

        // Now Update
        newRequest();

        body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "UPDATE table SET value = ? WHERE key = ?");
            q.put("params", new JSONArray("[10, 12]"));
            body.put("query", q);
        }

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        whenConnection(con).isQueried("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12).thenReturnModifiedRows(1);

        service.queryTransaction(txId, httpHeaders, uriInfo, body);

        verifyConnection(con).receivedQuery("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12);
        verify(dbs).backWritable(con);
        verify(txs).commit(txId);
    }

    @Test
    public void transactionInOXDB() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        // Start the transaction with a select
        param("keepOpen", "true");
        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "SELECT FOR UPDATE value FROM table WHERE key = ?");
            q.put("params", new JSONArray("[12]"));
            q.put("resultSet", true);
            body.put("query", q);
        }

        whenConnection(con).isQueried("SELECT FOR UPDATE value FROM table WHERE key = ?").withParameter(12).thenReturnColumns("value").withRow(5);

        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        Response response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);

        verify(dbs, never()).backWritable(ctxId, con);
        verify(con, never()).commit();
        verify(con, never()).setAutoCommit(true);

        String txId = getTransactionID(response);
        assertEquals(tx.getID(), txId);

        // Now Update
        newRequest();

        body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "UPDATE table SET value = ? WHERE key = ?");
            q.put("params", new JSONArray("[10, 12]"));
            body.put("query", q);
        }

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        whenConnection(con).isQueried("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12).thenReturnModifiedRows(1);

        service.queryTransaction(txId, httpHeaders, uriInfo, body);

        verifyConnection(con).receivedQuery("UPDATE table SET value = ? WHERE key = ?").withParameter(10).andParameter(12);
        verify(dbs).backWritable(ctxId, con);
        verify(txs).commit(txId);

    }

    @Test
    public void transactionRollBack() throws Exception {

        Transaction tx = new Transaction(con, txs);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        service.rollbackTransaction(tx.getID(), httpHeaders, uriInfo);

        verify(txs).rollback(tx.getID());
    }

    @Test
    public void transactionCommit() throws Exception {

        Transaction tx = new Transaction(con, txs);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        service.commitTransaction(tx.getID(), httpHeaders, uriInfo);

        verify(txs).commit(tx.getID());
    }

    @Test
    public void testUnknownTransaction() throws Exception {
        when(txs.getTransaction("unnknownTransaction")).thenReturn(null);
        try {
            service.queryTransaction("unknownTransaction", httpHeaders, uriInfo, new JSONObject());
            fail("Not halted");
        } catch (WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }

        try {
            service.commitTransaction("unknownTransaction", httpHeaders, uriInfo);
            fail("Not halted");
        } catch (WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }

        try {
            service.rollbackTransaction("unknownTransaction", httpHeaders, uriInfo);
            fail("Not halted");
        } catch (WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }

    @Test
    public void testFailingStatement() throws Exception {
        when(dbs.getWritable(ctxId)).thenReturn(con);

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        whenConnection(con).isQueried(body).thenFail();

        Response response = null;
        try {
            response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);
            fail("Should have halted on SQL Error");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        JSONObject j = getResponseObject(response);
        assertEquals("Kabooom!", j.getString("error"));

        verify(con).rollback();
        verify(dbs).backWritable(ctxId, con);
    }

    @Test
    public void testFailingStatementRollsBackRunningTransaction() throws Exception {
        Transaction tx = new Transaction(con, txs);

        when(txs.getTransaction(tx.getID())).thenReturn(tx);
        JSONObject body = new JSONObject();
        {
            JSONObject q = new JSONObject();
            q.put("query", "UPDATE table SET column = 'value' WHERE cid = ?");
            q.put("params", new JSONArray("[1]"));
            body.put("query", q);
        }
        whenConnection(con).isQueried("UPDATE table SET column = 'value' WHERE cid = ?").withParameter(1).thenFail();

        try {
            service.queryTransaction(tx.getID(), httpHeaders, uriInfo, body);
            fail("Should have halted on SQL Error");
        } catch (WebApplicationException e) {
        }

        verify(txs).rollback(tx.getID());
    }

    @Test
    public void versionNegotiationFails() throws OXException {
        when(dbs.getWritable(ctxId)).thenReturn(con);
        when(versionChecker.isUpToDate(anyObject(), eq(con), eq("com.openexchange.myModule"), eq("2"))).thenReturn("1");

        String body = "UPDATE someTable SET someColumn = 'someValue' WHERE someIdentifier = 12";
        header("x-ox-db-module", "com.openexchange.myModule");
        header("x-ox-db-version", "2");

        Response response;
        try {
            response = service.updateOXDB(ctxId, httpHeaders, uriInfo, body);
            fail("Should have halted");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        assertEquals(409, response.getStatus());
        assertEquals("1", response.getHeaderString("X-OX-DB-VERSION"));
    }

    @Test
    public void migration() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";

        Response response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);

        verify(con).setAutoCommit(false);
        verifyConnection(con).receivedQuery(body);
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

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        Response response;
        try {
            response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);
            fail("Should have halted");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        assertEquals(400, response.getStatus());

        verify(con).setAutoCommit(false);
        verify(con).rollback();
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationCanNotProceedBecauseSchemaIsLocked() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(false);

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        Response response;
        try {
            response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);
            fail("Should have halted");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        assertEquals(423, response.getStatus());

        verify(versionChecker, never()).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationCanNotProceedBecauseTheVersionWasUpdatedInTheMeantime() throws OXException, SQLException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        when(versionChecker.updateVersion(con, "com.openexchange.myModule", "1", "2")).thenReturn("2");

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        Response response;
        try {
            response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);
            fail("Should have halted");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        assertEquals(409, response.getStatus());
        assertEquals("2", response.getHeaderString("X-OX-DB-VERSION"));

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrationAcrossTransactions() throws OXException, SQLException, JSONException {
        Transaction tx = new Transaction(con, txs);
        when(txs.newTransaction(con)).thenReturn(tx);

        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(0);

        param("keepOpen", "true");

        Response response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);

        String txId = getTransactionID(response);
        assertEquals(tx.getID(), txId);

        verify(con, never()).commit();
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");

        newRequest();

        body = "CREATE TABLE myModule_myTable2 (myAttribute TEXT)";
        whenConnection(con).isQueried(body).thenReturnModifiedRows(0);

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        service.queryTransaction(txId, httpHeaders, uriInfo, body);

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

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        param("keepOpen", "true");

        Response response = service.migrate(ctxId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);

        String txId = getTransactionID(response);
        assertEquals(tx.getID(), txId);

        verify(con, never()).commit();
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");
        verify(versionChecker, never()).unlock(con, "com.openexchange.myModule");

        newRequest();

        body = "CREATE TABLE myModule_myTable2 (myAttribute TEXT)";
        param("keepOpen", "true");

        whenConnection(con).isQueried("CREATE TABLE myModule_myTable2 (myAttribute TEXT)").thenFail();

        tx.setConnection(con);
        when(txs.getTransaction(txId)).thenReturn(tx);

        try {
            service.queryTransaction(txId, httpHeaders, uriInfo, body);
            fail("Should have halted on error");
        } catch (WebApplicationException e) {
            response = e.getResponse();
        }

        verify(txs).rollback(txId);
        verify(con).setAutoCommit(true);
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
    }

    @Test
    public void migrateMonitored() throws Exception {
        when(dbs.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);
        when(versionChecker.lock(eq(con), eq("com.openexchange.myModule"), anyLong(), anyLong())).thenReturn(true);
        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";

        service.migrateMonitored(readPoolId, writePoolId, schema, partitionId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);

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

        String body = "CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";
        param("keepOpen", "true");

        service.migrateMonitored(readPoolId, writePoolId, schema, partitionId, "1", "2", "com.openexchange.myModule", httpHeaders, uriInfo, body);

        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).updateVersion(con, "com.openexchange.myModule", "1", "2");

        newRequest();

        tx.setConnection(con);
        when(txs.getTransaction(tx.getID())).thenReturn(tx);

        body = "CREATE TABLE myModule_myTable2 (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))";

        service.queryTransaction(tx.getID(), httpHeaders, uriInfo, body);

        verifyConnection(con).receivedQuery("CREATE TABLE myModule_myTable2 (greeting varchar(128), cid int(10), uid int(10), PRIMARY KEY (cid, uid))");
        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);
    }

    @Test
    public void forcedUnlock() throws OXException {
        when(dbs.getForUpdateTask(ctxId)).thenReturn(con);

        service.unlock(ctxId, "com.openexchange.myModule", httpHeaders, uriInfo);

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backForUpdateTask(ctxId, con);
    }

    @Test
    public void forceUnlockMonitoredSchema() throws OXException {
        when(dbs.getWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId)).thenReturn(con);

        service.unlockMonitored(readPoolId, writePoolId, schema, partitionId, "com.openexchange.myModule", httpHeaders, uriInfo);

        verify(versionChecker).unlock(con, "com.openexchange.myModule");
        verify(dbs).backWritableMonitoredForUpdateTask(readPoolId, writePoolId, schema, partitionId, con);

    }

    @Test
    public void insertPartitionIds() throws Exception {
        JSONArray body = new JSONArray("[1, 2, 3, 4, 5]");

        service.insertPartitionIds(writePoolId, schema, httpHeaders, uriInfo, body);

        verify(dbs).initPartitions(writePoolId, schema, 1, 2, 3, 4, 5);
    }

    private void newRequest() {
        setup();
    }

    private void param(String name, String value) {
        when(mmap.getFirst(name)).thenReturn(value);
        when(mmap.get(name)).thenReturn(Collections.singletonList(value));
    }

    private void header(String name, String value) {
        when(httpHeaders.getHeaderString(name)).thenReturn(value);
    }

    private JSONObject getQueryResult(Response response, String qName) throws JSONException {
        JSONObject jsonObject = new JSONObject((JSONObject) response.getEntity());
        JSONObject results = jsonObject.getJSONObject("results");
        return results.getJSONObject(qName);
    }

    private JSONObject getResponseObject(Response response) throws JSONException {
        return new JSONObject((JSONObject) response.getEntity());
    }

    private String getTransactionID(Response response) throws JSONException {
        JSONObject jsonObject = new JSONObject((JSONObject) response.getEntity());
        return jsonObject.optString("tx");
    }

}
