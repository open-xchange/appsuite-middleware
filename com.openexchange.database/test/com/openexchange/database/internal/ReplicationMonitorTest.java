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

package com.openexchange.database.internal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.openexchange.osgi.ServiceListings;

/**
 * Tests for class {@link ReplicationMonitor}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public class ReplicationMonitorTest {

    private ReplicationMonitor monitor;
    private AssignmentImpl assignment;

    @Before
    public void setUp() {
        monitor = new ReplicationMonitor(false, false, ServiceListings.emptyList());
        assignment = new AssignmentImpl(0, 0, 0, 0, null);
    }

    @After
    public void tearDown() {
        assignment = null;
        monitor = null;
    }

     @Test
     public void testForBug32102() throws Exception {
        final Connection con = mock(java.sql.Connection.class);
        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                fail(Connection.class.getName() + ".isClosed() should not be called if replication monitor is disabled.");
                return Boolean.FALSE;
            }
        }).when(con).isClosed();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                fail(Connection.class.getName() + ".setAutoCommit() should not be called if replication monitor is disabled.");
                return null;
            }
        }).when(con).setAutoCommit(ArgumentMatchers.anyBoolean());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                fail(Connection.class.getName() + ".commit() should not be called if replication monitor is disabled.");
                return null;
            }
        }).when(con).commit();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                fail(Connection.class.getName() + ".rollback() should not be called if replication monitor is disabled.");
                return null;
            }
        }).when(con).rollback();
        doAnswer(new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation) {
                fail(Connection.class.getName() + ".prepareStatement() should not be called if replication monitor is disabled.");
                return null;
            }
        }).when(con).prepareStatement(ArgumentMatchers.anyString());
        monitor.increaseTransactionCounter(assignment, con);
    }
}
