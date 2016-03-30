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

package com.openexchange.groupware;

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import junit.framework.TestCase;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.server.impl.DBPool;

/**
 * Checks if {@link IDGenerator} works as expected and how fast it is.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class IDGeneratorTest extends TestCase {

    private static final String TEST_TABLE = "CREATE TABLE idGeneratorTest (cid INT4 UNSIGNED NOT NULL, id INT4 UNSIGNED NOT NULL, PRIMARY KEY (cid,id))";

    private static final int TYPE = Types.TASK;

    private static final int MAX_IN_COMMIT = 10;

    private static final int THREADS = 10;

    private static final int TIME = 20;

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IDGeneratorTest.class);

    static final Random rand = new Random(System.currentTimeMillis());

    Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
        final ContextStorage cs = ContextStorage.getInstance();
        context = cs.getContext(cs.getContextId("defaultcontext"));
    }

    @Override
    protected void tearDown() throws Exception {
        Init.stopServer();
        super.tearDown();
    }

    /**
     * Test method for {@link IDGenerator#getId(Context, int)}
     */
    public void testGetId() throws Throwable {
        Connection con = DBPool.pickupWriteable(context);
        try {
            Statement stmt = con.createStatement();
            try {
                stmt.execute(TEST_TABLE);
            } catch (SQLException e) {
                LOG.error("Error while creating test table.", e);
                fail("Error while creating test table.");
            }
            stmt.close();
        } finally {
            DBPool.closeWriterSilent(context, con);
            con = null;
        }

        Inserter[] tester = new Inserter[THREADS];
        Thread[] threads = new Thread[tester.length];
        for (int i = 0; i < tester.length; i++) {
            tester[i] = new Inserter();
            threads[i] = new Thread(tester[i]);
            threads[i].start();
        }
        Thread.sleep(TIME * 1000);
        for (int i = 0; i < tester.length; i++) {
            tester[i].run = false;
        }
        for (int i = 0; i < tester.length; i++) {
            threads[i].join();
        }

        con = DBPool.pickup(context);
        try {
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("SELECT count(*) FROM idGeneratorTest");
            int rows = 0;
            if (result.next()) {
                rows = result.getInt(1);
            }
            result.close();
            LOG.info("Inserted " + ((float) rows / TIME / THREADS) + " rows.");
            stmt.close();
        } finally {
            DBPool.closeReaderSilent(context, con);
            con = null;
        }

        con = DBPool.pickupWriteable(context);
        try {
            Statement stmt = con.createStatement();
            try {
                stmt.execute("DROP TABLE idGeneratorTest");
            } catch (SQLException e) {
                LOG.error("Error while dropping table.", e);
            }
            stmt.close();
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    class Inserter implements Runnable {

        boolean run = true;

        @Override
        public void run() {
            while (run) {
                Connection con = null;
                try {
                    con = DBPool.pickupWriteable(context);
                } catch (OXException e) {
                    LOG.error("Can't get writable database connection.", e);
                    return;
                }
                try {
                    con.setAutoCommit(false);
                    PreparedStatement insert = con.prepareStatement("INSERT INTO idGeneratorTest (cid, id) VALUES (?, ?)");
                    int countInCommit = rand.nextInt(MAX_IN_COMMIT) + 1;
                    for (int i = 0; i < countInCommit; i++) {
                        int ident = IDGenerator.getId(context, TYPE, con);
                        insert.setInt(1, context.getContextId());
                        insert.setInt(2, ident);
                        insert.executeUpdate();
                    }
                    con.commit();
                    insert.close();
                } catch (SQLException e) {
                    try {
                        con.rollback();
                    } catch (SQLException e1) {
                        LOG.error("Error while rollback.", e);
                    }
                    LOG.error("Error while getting ID and inserting.", e);
                    fail(e.getMessage());
                    return;
                } finally {
                    try {
                        con.setAutoCommit(true);
                    } catch (SQLException e) {
                        LOG.error("Error while setting autocommit true.", e);
                    }
                    DBPool.closeWriterSilent(context, con);
                }
            }
        }
    }
}
