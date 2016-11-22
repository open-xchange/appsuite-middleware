/*
 * PoolRunner.java
 *
 * Created on 8. September 2006, 21:22
 *
 */

package com.openexchange.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;

/**
 *
 * @author bishoph
 */
public class PoolRunner implements Runnable {

    private final Context c;
    private final boolean close_connections;
    private Thread run;
    private int current_run = 0;

    public static final int TEST_RUNS = 200;
    public static final int WAIT_TIME = 10;
    public static final String TEST_QUERY = "SELECT 1 as test";

    private boolean isrunning = true;
    private int modrunner = 0;

    public PoolRunner(final Context c, final boolean close_connections) {
        this.c = c;
        this.close_connections = close_connections;
        this.start();
    }

    public void start() {
        run = new Thread(this, "PoolRunner");
        run.start();
    }

    @Override
    public void run() {
        while (current_run < TEST_RUNS) {
            try {
                final Connection con = DBPool.pickup(c);
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (final java.lang.InterruptedException ie) {
                }
                if (close_connections) {
                    if (modrunner % 8 == 0) {
                        con.close();
                    } else if (modrunner % 9 == 0) {
                        con.close();
                        // con = null;
                    }
                }
                modrunner++;
                simpleAction(con);
                DBPool.push(c, con);

                current_run++;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        isrunning = false;
        run.interrupt();
    }

    private void simpleAction(final Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            final ResultSet rs = con.createStatement().executeQuery(TEST_QUERY);
            while (rs.next()) {
            }
        }
    }

    boolean isTestDone() {
        return isrunning;
    }

    Thread getRunnerThread() {
        return run;
    }

}
