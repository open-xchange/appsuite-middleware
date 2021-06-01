/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.database.internal.wrapping;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;
import org.junit.Assert;
import org.junit.Test;
import com.mysql.jdbc.Connection;
import com.openexchange.database.internal.AssignmentImpl;
import com.openexchange.database.internal.ConnectionState;
import com.openexchange.database.internal.Pools;
import com.openexchange.database.internal.ReplicationMonitor;

/**
 * {@link UpdateFlagTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdateFlagTest {

    @Test
    @SuppressWarnings("static-method")
    public void testPreparedStatementExecuteUpdate() throws Exception {
        Connection mockCon = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockCon.prepareStatement(anyString())).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        PreparedStatement stmt = con.prepareStatement("");
        stmt.executeUpdate();
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertTrue(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testPreparedStatementExecute() throws Exception {
        Connection mockCon = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockCon.prepareStatement(anyString())).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        PreparedStatement stmt = con.prepareStatement("");
        stmt.execute();
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertTrue(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testPreparedStatementExecuteQuery() throws Exception {
        Connection mockCon = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockCon.prepareStatement(anyString())).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        PreparedStatement stmt = con.prepareStatement("");
        stmt.executeQuery();
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertFalse(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCreateStatementExecuteUpdate() throws Exception {
        Connection mockCon = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockCon.createStatement()).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        Statement stmt = con.createStatement();
        stmt.executeUpdate("");
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertTrue(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCreateStatementExecute() throws Exception {
        Connection mockCon = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockCon.createStatement()).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        Statement stmt = con.createStatement();
        stmt.execute("");
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertTrue(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCreateStatementExecuteQuery() throws Exception {
        Connection mockCon = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockCon.createStatement()).thenReturn(mockStmt);
        ReplicationMonitor mockMon = mock(ReplicationMonitor.class);
        VersionIndependentConnectionReturner con = new VersionIndependentConnectionReturner(null, mockMon, null, mockCon, true, true, false);
        Statement stmt = con.createStatement();
        stmt.executeQuery("");
        con.close();

        ConnectionState state = con.getState();
        verify(mockMon).backAndIncrementTransaction(null, null, mockCon, true, true, state);
        Assert.assertFalse(state.isUsedForUpdate());
        Assert.assertFalse(state.isUsedAsRead());
        Assert.assertFalse(state.isUpdateCommitted());
    }

    private static final class VersionIndependentConnectionReturner extends JDBC4ConnectionReturner {

        public VersionIndependentConnectionReturner(Pools pools, ReplicationMonitor monitor, AssignmentImpl assign, java.sql.Connection delegate, boolean noTimeout, boolean write, boolean usedAsRead) {
            super(pools, monitor, assign, delegate, noTimeout, write, usedAsRead);
        }

        @Override
        public void setSchema(String schema) {
            // TODO Auto-generated method stub
        }

        @Override
        public String getSchema() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            // TODO Auto-generated method stub
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            // TODO Auto-generated method stub
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        public ConnectionState getState() {
            return state;
        }
    }
}
