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

package com.openexchange.database.internal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
        monitor = new ReplicationMonitor(false, false);
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
        }).when(con).setAutoCommit(Matchers.anyBoolean());
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
        }).when(con).prepareStatement(Matchers.anyString());
        monitor.increaseTransactionCounter(assignment, con);
    }
}
