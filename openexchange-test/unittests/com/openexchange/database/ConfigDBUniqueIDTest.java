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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import junit.framework.JUnit4TestAdapter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.java.ConcurrentHashSet;

/**
 * {@link ConfigDBUniqueIDTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConfigDBUniqueIDTest {

    private static final String URL = "jdbc:mysql://devel-master.netline.de/configdb";
    private static final Properties PROPS;

    private static int id;

    static {
        PROPS = new Properties();
        PROPS.put("user", "openexchange");
        PROPS.put("password", "secret");
        PROPS.put("useUnicode", "true");
        PROPS.put("characterEncoding", "UTF-8");
        PROPS.put("autoReconnect", "true");
        PROPS.put("useServerPrepStmts", "false");
        PROPS.put("useTimezone", "true");
        PROPS.put("serverTimezone", "UTC");
        PROPS.put("connectTimeout", "15000");
        PROPS.put("socketTimeout", "15000");
    }

    public ConfigDBUniqueIDTest() {
        super();
    }

    @BeforeClass
    public static void setup() throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet result = null;
        try {
            con = createConnection();
            con.setAutoCommit(false);
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT id FROM configdb_sequence FOR UPDATE");
            if (result.next()) {
                id = result.getInt(1);
            }
            con.commit();
        } finally {
            closeSQLStuff(result, stmt);
            autocommit(con);
            if (null != con) {
                con.close();
            }
        }
    }

    @AfterClass
    public static void shutdown() throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = createConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement("UPDATE configdb_sequence SET id=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            con.commit();
        } finally {
            closeSQLStuff(result, stmt);
            autocommit(con);
            if (null != con) {
                con.close();
            }
        }
    }

    @Test
    public void checkForWrongIds() throws SQLException, InterruptedException {
        int size = 2;
        Thread[] threads = new Thread[size];
        Generator[] generators = new Generator[size];
        Set<Integer> generated = new ConcurrentHashSet<Integer>(1000);
        for (int i = 0; i < size; i++) {
            generators[i] = new Generator(createConnection(), generated);
            threads[i] = new Thread(generators[i]);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        Thread.sleep(10000);
        for (Generator generator : generators) {
            generator.stop();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        for (Generator generator : generators) {
            Exception e = generator.getException();
            if (null != e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    private class Generator implements Runnable {

        private final Connection con;
        private final Set<Integer> generated;
        private boolean run;
        private Exception e;

        public Generator(Connection con, Set<Integer> generated) {
            super();
            this.con = con;
            this.generated = generated;
            run = true;
        }

        public void stop() {
            run = false;
        }

        public Exception getException() {
            return e;
        }

        @Override
        public void run() {
            try {
                while (run) {
                    int newId = -1;
                    try {
                        con.setAutoCommit(false);
                        PreparedStatement stmt = null;
                        ResultSet result = null;
                        try {
                            stmt = con.prepareStatement("UPDATE configdb_sequence SET id=last_insert_id(id+1)");
                            stmt.execute();
                            stmt.close();
                            stmt = con.prepareStatement("SELECT last_insert_id()");
                            result = stmt.executeQuery();
                            if (result.next()) {
                                newId = result.getInt(1);
                            }
                        } finally {
                            closeSQLStuff(result, stmt);
                        }
                        con.commit();
                    } finally {
                        autocommit(con);
                    }
                    if (!generated.add(Integer.valueOf(newId))) {
                        throw new Exception("Generated duplicate identifier " + newId);
                    }
                }
            } catch (Exception e1) {
                e = e1;
            }
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, PROPS);
    }

    static void closeSQLStuff(ResultSet result, Statement stmt) throws SQLException {
        if (null != result) {
            result.close();
        }
        if (null != stmt) {
            stmt.close();
        }
    }

    static void autocommit(Connection con) throws SQLException {
        if (null != con) {
            con.setAutoCommit(true);
        }
    }
    
    //workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ConfigDBUniqueIDTest.class);
	}
}
