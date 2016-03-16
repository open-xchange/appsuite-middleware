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

package com.openexchange.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.test.TestInit;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link ReplicationMonitorPerformanceTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ReplicationMonitorPerformanceTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReplicationMonitorPerformanceTest.class);

    private static final int RUNS = 5;

    private static final int THREADS = 2;

    private static final int ITERATIONS = 100;

    private static AtomicInteger NEXT_ID = new AtomicInteger();

    private DBPoolProvider db;

    private Context context;

    public ReplicationMonitorPerformanceTest() {
        super();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestInit.loadTestProperties();
        Init.startServer();
    }

    @Before
    public void setUp() throws Exception {
        ContextStorage contextStorage = ContextStorage.getInstance();
        int contextId = contextStorage.getContextId("defaultcontext");
        context = contextStorage.getContext(contextId);
        db = new DBPoolProvider();

        Connection writeCon = db.getWriteConnection(context);
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS replicationMonitorPerformanceTest;");
            DBUtils.closeSQLStuff(stmt);
            stmt = writeCon.createStatement();
            stmt.executeUpdate("CREATE TABLE `replicationMonitorPerformanceTest` ("
                + "`id` int(10) unsigned NOT NULL, "
                + "`key` varchar(128) COLLATE utf8_unicode_ci NOT NULL, "
                + "`value` varchar(128) COLLATE utf8_unicode_ci NOT NULL, "
                + "PRIMARY KEY (`id`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        } catch (SQLException e) {
            LOG.error("", e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
            db.releaseWriteConnection(context, writeCon);
        }
    }

    @After
    public void tearDown() throws Exception {
        Connection writeCon = db.getWriteConnection(context);
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DROP TABLE replicationMonitorPerformanceTest;");
        } finally {
            DBUtils.closeSQLStuff(stmt);
            db.releaseWriteConnection(context, writeCon);
        }
    }

    @Test
    public void testInLoop() throws Exception {
        Connection writeCon = db.getWriteConnection(context);
        PreparedStatement stmt = writeCon.prepareStatement("UPDATE replicationMonitor SET `transaction` = ? WHERE `cid` = ?");
        stmt.setLong(1, 0L);
        stmt.setInt(2, context.getContextId());
        stmt.executeUpdate();
        DBUtils.closeSQLStuff(stmt);
        db.releaseWriteConnection(context, writeCon);

        Method[] methods = getClass().getDeclaredMethods();
        Map<Method, List<Long>> timeMap = new HashMap<Method, List<Long>>();
        for (int i = 0; i < RUNS; i++) {
            long timestamp = System.currentTimeMillis();
            for (Method m : methods) {
                if (m.getName().startsWith("run")) {
                    try {
                        List<Long> times = executeTest(m, ITERATIONS, timestamp);
                        List<Long> allTimes = timeMap.get(m);
                        if (allTimes == null) {
                            allTimes = new ArrayList<Long>(times);
                            timeMap.put(m, allTimes);
                        } else {
                            allTimes.addAll(times);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Connection readCon = db.getReadConnection(context);
        writeGlobalStatus(readCon, "slave_status");
        db.releaseReadConnection(context, readCon);

        writeCon = db.getWriteConnection(context);
        writeGlobalStatus(writeCon, "master_status");
        db.releaseWriteConnection(context, writeCon);

        for (Entry<Method, List<Long>> entry : timeMap.entrySet()) {
            PrintWriter w = new PrintWriter(System.getProperty("user.dir") + File.separatorChar + "rm_benchmarks" + File.separatorChar + entry.getKey().getName() + ".csv");
            w.println("time");
            for (Long time : entry.getValue()) {
                w.println(time);
            }
            w.flush();
            w.close();
        }
    }

    private void writeGlobalStatus(Connection con, String filename) throws SQLException, FileNotFoundException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW GLOBAL STATUS;");
        PrintWriter w = new PrintWriter(System.getProperty("user.dir") + File.separatorChar + "rm_benchmarks" + File.separatorChar + filename + ".csv");
        w.println("VARIABLE,VALUE");
        while (rs.next()) {
            w.println(rs.getObject(1).toString() + "," + rs.getObject(2).toString());
        }
        DBUtils.closeSQLStuff(rs, stmt);

        PreparedStatement pstmt = con.prepareStatement("SELECT `transaction` FROM replicationMonitor WHERE `cid` = ?");
        pstmt.setInt(1, context.getContextId());
        rs = pstmt.executeQuery();
        if (rs.next()) {
            w.println("ReplicationMonitor," + rs.getLong(1));
        }
        DBUtils.closeSQLStuff(rs, pstmt);

        w.flush();
        w.close();
    }

    private List<Long> executeTest(Method m, int iterations, long timestamp) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, FileNotFoundException {
        long avg = 0L;
        long min = Long.MAX_VALUE;
        long max = 0L;
        long start = System.currentTimeMillis();
        TestCaseRunner[] runners = new TestCaseRunner[THREADS];
        List<Long> times = new ArrayList<Long>();

        printHeader(m.getName());
        for (int i = 0; i < THREADS; i++) {
            runners[i] = new TestCaseRunner(this, m, iterations);
            new Thread(runners[i]).start();
        }

        for (TestCaseRunner runner : runners) {
            try {
                runner.getThread().join();
                if (runner.getMax() > max) {
                    max = runner.getMax();
                }
                if (runner.getMin() < min) {
                    min = runner.getMin();
                }
                avg = avg + runner.getAvg();

                List<Long> threadTimes = runner.getTimes();
                times.addAll(threadTimes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        avg = avg / runners.length;
        printFooter(avg, min, max, System.currentTimeMillis() - start);
        return times;
    }

    private static final class TestCaseRunner implements Runnable {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final List<Long> times;
        private final Method method;
        private final Object object;
        private final int iterations;
        private Thread thread;
        private long max;
        private long min;
        private long avg;

        public TestCaseRunner(Object object, Method method, int iterations) {
            super();
            this.object = object;
            this.method = method;
            this.iterations = iterations;
            times = new ArrayList<Long>();
            max = 0L;
            min = Long.MAX_VALUE;
            avg = 0L;
        }

        @Override
        public void run() {
            thread = Thread.currentThread();
            latch.countDown();
            try {
                for (int i = 0; i < iterations; i++) {
                    long dur = (Long) method.invoke(object);
                    times.add(dur);

                    if (dur > max) {
                        max = dur;
                    }
                    if (dur < min) {
                        min = dur;
                    }
                    avg = avg + dur;
                }
                avg = avg / 100;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public long getAvg() {
            return avg;
        }

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }

        public List<Long> getTimes() {
            return times;
        }

        public Thread getThread() throws InterruptedException {
            latch.await();
            return thread;
        }

    }

    private void printHeader(String title) {
        System.out.println("=========================");
        System.out.println(title);
        System.out.println("=========================");
    }

    private void printFooter(long avg, long min, long max, long dur) {
        System.out.println(" Min:      " + min + "ms");
        System.out.println(" Max:      " + max + "ms");
        System.out.println(" Average:  " + avg + "ms");
        System.out.println(" Duration: " + dur + "ms");
        System.out.println();
    }

    public long runSingleInTransaction() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection con = db.getWriteConnection(context);
        PreparedStatement wstmt = null;
        PreparedStatement rstmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            int id = NEXT_ID.getAndIncrement();
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            wstmt = con.prepareStatement("INSERT INTO replicationMonitorPerformanceTest (`id`, `key`, `value`) VALUES (?, ?, ?)");
            wstmt.setInt(1, id);
            wstmt.setString(2, key);
            wstmt.setString(3, value);
            wstmt.executeUpdate();

            rstmt = con.prepareStatement("SELECT `key`, `value` FROM replicationMonitorPerformanceTest WHERE `id` = ?");
            rstmt.setInt(1, id);
            rs = rstmt.executeQuery();

            Assert.assertTrue("ResultSet is empty", rs.next());
            Assert.assertEquals("Wrong key", key, rs.getString(1));
            Assert.assertEquals("Wrong value", value, rs.getString(2));
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            con.rollback();
        } finally {
            DBUtils.closeSQLStuff(wstmt);
            DBUtils.closeSQLStuff(rs, rstmt);
            DBUtils.autocommit(con);
            db.releaseWriteConnection(context, con);
        }

        return System.currentTimeMillis() - startTime;
    }

    public long runBatchInTransaction() throws Exception {
        int times = 25;
        long startTime = System.currentTimeMillis();
        Connection con = db.getWriteConnection(context);
        PreparedStatement wstmt = null;
        PreparedStatement rstmt = null;
        ResultSet rs = null;
        try {
            DBUtils.startTransaction(con);
            wstmt = con.prepareStatement("INSERT INTO replicationMonitorPerformanceTest (`id`, `key`, `value`) VALUES (?, ?, ?)");
            Set<Integer> ids = new HashSet<Integer>(times);
            for (int i = 0; i < times; i++) {
                int id = NEXT_ID.getAndIncrement();
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();
                wstmt.setInt(1, id);
                wstmt.setString(2, key);
                wstmt.setString(3, value);
                wstmt.addBatch();
                ids.add(id);
            }
            wstmt.executeBatch();

            StringBuilder sb = new StringBuilder("SELECT `id`, `key`, `value` FROM replicationMonitorPerformanceTest WHERE `id` IN (?");
            for (int i = 0; i < times - 1; i++) {
                sb.append(", ?");
            }
            sb.append(")");
            rstmt = con.prepareStatement(sb.toString());
            int i = 1;
            for (int id : ids) {
                rstmt.setInt(i, id);
                i++;
            }

            rs = rstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                ids.remove(id);
            }

            Assert.assertEquals("Did not find all entries", 0, ids.size());
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            con.rollback();
        } finally {
            DBUtils.closeSQLStuff(wstmt);
            DBUtils.closeSQLStuff(rs, rstmt);
            DBUtils.autocommit(con);
            db.releaseWriteConnection(context, con);
        }

        return System.currentTimeMillis() - startTime;
    }

    public long runSingleNoTransaction() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection con = db.getWriteConnection(context);
        PreparedStatement wstmt = null;
        PreparedStatement rstmt = null;
        ResultSet rs = null;
        try {
            int id = NEXT_ID.getAndIncrement();
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            wstmt = con.prepareStatement("INSERT INTO replicationMonitorPerformanceTest (`id`, `key`, `value`) VALUES (?, ?, ?)");
            wstmt.setInt(1, id);
            wstmt.setString(2, key);
            wstmt.setString(3, value);
            wstmt.executeUpdate();

            rstmt = con.prepareStatement("SELECT `key`, `value` FROM replicationMonitorPerformanceTest WHERE `id` = ?");
            rstmt.setInt(1, id);
            rs = rstmt.executeQuery();

            Assert.assertTrue("ResultSet is empty", rs.next());
            Assert.assertEquals("Wrong key", key, rs.getString(1));
            Assert.assertEquals("Wrong value", value, rs.getString(2));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeSQLStuff(wstmt);
            DBUtils.closeSQLStuff(rs, rstmt);
            db.releaseWriteConnection(context, con);
        }

        return System.currentTimeMillis() - startTime;
    }

    public long runReadUsedAsWrite() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection con = db.getWriteConnection(context);
        PreparedStatement rstmt = null;
        ResultSet rs = null;
        try {
            rstmt = con.prepareStatement("SELECT `id`, `key`, `value` FROM replicationMonitorPerformanceTest");
            rs = rstmt.executeQuery();

            Assert.assertTrue("ResultSet is empty", rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeSQLStuff(rs, rstmt);
            db.releaseWriteConnection(context, con);
        }

        return System.currentTimeMillis() - startTime;
    }

    public long runReadUsedAsRead() throws Exception {
        long startTime = System.currentTimeMillis();
        Connection con = db.getWriteConnection(context);
        PreparedStatement rstmt = null;
        ResultSet rs = null;
        try {
            rstmt = con.prepareStatement("SELECT `id`, `key`, `value` FROM replicationMonitorPerformanceTest");
            rs = rstmt.executeQuery();

            Assert.assertTrue("ResultSet is empty", rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeSQLStuff(rs, rstmt);
            db.releaseWriteConnectionAfterReading(context, con);
        }

        return System.currentTimeMillis() - startTime;
    }

}
