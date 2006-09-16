/*
 * PoolRunner.java
 *
 * Created on 8. September 2006, 21:22
 *
 */

package com.openexchange.server;

import com.openexchange.groupware.contexts.Context;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author bishoph
 */
public class PoolRunner implements Runnable {
    
    private Context c;
    private boolean close_connections;
    private Thread run;
    private int current_run = 0;
    
    public static final int TEST_RUNS = 20;
    public static final int WAIT_TIME = 200;
    public static final String TEST_QUERY = "SELECT 1 as test";
    
    private boolean isrunning = true;
    private boolean open = true;
    private int modrunner = 0;

    
    public PoolRunner(Context c, boolean close_connections) {
        this.c = c;
        this.close_connections = close_connections;
        this.start();
    }
    
    
    public void start() {
        run = new Thread(this);
        run.start();
    }
    
    public void run() {
        while (current_run < TEST_RUNS) {
            try {
                Connection con = DBPool.pickup(c);
                ComplexDBPoolTest.countSize(false);
                open = true;
                synchronized(this) { wait(WAIT_TIME); }
                if (close_connections) {
                    if (modrunner % 3 == 0) {
                        con.close();
                        open = false;
                    } else if (modrunner % 5 == 0) {
                        con.close();
                        con = null;
                        open = false;
                    }
                }
                modrunner++;
                simpleAction(con);
                if (open)
                    ComplexDBPoolTest.countSize(true);
                DBPool.push(c, con);
                current_run++;                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        isrunning = false;
        run.interrupt();
    }

    private void simpleAction(Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            ResultSet rs = con.createStatement().executeQuery(TEST_QUERY);
            int counter = 0;
            while (rs.next()) {
                counter++;
            }
        }
    }

    boolean isTestDone() {
        return isrunning;
    }
    
    Thread getRunnerThread() {
        return run;
    }
    
    int getRunnerCount() {    
        return modrunner;
    }
    
}
