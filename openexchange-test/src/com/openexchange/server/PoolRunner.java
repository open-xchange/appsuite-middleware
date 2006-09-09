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
    private Thread run;
    private int current_run = 0;
    
    public static final int TEST_RUNS = 5;
    public static final int WAIT_TIME = 200;
    public static final String TEST_QUERY = "SELECT 1 as test";
    
    private boolean isrunning = true;
    
    public PoolRunner(Context c) {
        this.c = c;
        this.start();
    }
    
    
    public void start() {
        run = new Thread(this);
        run.start();
    }
    
    public void run() {
        while (current_run <= TEST_RUNS) {
            try {
                Connection con = DBPool.pickup(c);
                synchronized(this) { wait(WAIT_TIME); }
                simpleAction(con);
                DBPool.push(c, con);
                current_run++;                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        isrunning = false;        
    }

    private void simpleAction(Connection con) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(TEST_QUERY);
        int counter = 0;
        while (rs.next()) {
            counter++;
        }
    }

    boolean isTestDone() {
        return isrunning;
    }

    void joinThread() {
        try {
            run.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}
