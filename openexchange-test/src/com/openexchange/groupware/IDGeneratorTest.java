package com.openexchange.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;

import junit.framework.TestCase;

public class IDGeneratorTest extends TestCase {

    private Context context;
    
    protected void setUp() throws Exception {
        super.setUp();
        Init.initDB();
        context = ContextStorage.getInstance().getContext("defaultcontext");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.IDGenerator.getId(Context, int)'
     */
    public void testGetId() throws Throwable {
        Connection con = DBPool.pickupWriteable(context);
        try {
            Statement stmt = con.createStatement();
            try {
                stmt.execute("CREATE TABLE id_generator_test (cid INT4 UNSIGNED NOT NULL, id INT4 UNSIGNED NOT NULL, PRIMARY KEY (cid,id))");
            } catch (SQLException e) {
            }
            stmt.close();
            Inserter[] tester = new Inserter[5];
            Thread[] threads = new Thread[tester.length];
            for (int i = 0; i < tester.length; i++) {
                tester[i] = new Inserter();
                threads[i] = new Thread(tester[i]);
                threads[i].start();
            }
            Thread.sleep(20 * 1000);
            for (int i = 0; i < tester.length; i++) {
                tester[i].run = false;
            }
            for (int i = 0; i < tester.length; i++) {
                threads[i].join();
            }
            System.out.println("Inserted "
                + IDGenerator.getId(context, Types.FOLDER) + " rows.");
            stmt = con.createStatement();
            try {
                stmt.execute("DROP TABLE id_generator_test");
            } catch (SQLException e) {
            }
            stmt.close();
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    private class Inserter implements Runnable {

        boolean run = true;
        
        public void run() {
            Connection con = null;
            try {
                con = DBPool.pickupWriteable(context);
            } catch (DBPoolingException e) {
                e.printStackTrace();
                return;
            }
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO id_generator_test (cid, id) VALUES (?, ?)");
                while (run) {
                    int id = IDGenerator.getId(context, Types.FOLDER);
                    ps.setInt(1, context.getContextId());
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                ps.close();
            } catch (SQLException e) {
                fail(e.getMessage());
                e.printStackTrace();
            } finally {
                DBPool.closeWriterSilent(context, con);
            }
        }
    }
}
