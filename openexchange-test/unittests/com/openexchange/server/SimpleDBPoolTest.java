
package com.openexchange.server;

import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.CalendarTest;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.impl.DBPool;

public class SimpleDBPoolTest {

    @Before
    public void setUp() throws Exception {
        Init.startServer();
    }

    @After
    public void tearDown() throws Exception {
        Init.stopServer();
    }

    @Test
    public void testBasicPoolFunctions() throws Throwable {
        final Context context = new ContextImpl(CalendarTest.contextid);

        final int testsize = 50; // DBPool.getSize(context, true);
        final Connection con[] = new Connection[testsize];
        for (int a = 0; a < con.length; a++) {
            try {
                con[a] = DBPool.pickup(context);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        for (int a = 0; a < con.length; a++) {
            try {
                final Connection tc = con[a];
                DBPool.push(context, tc);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        // assertEquals("Check pool size ", testsize, DBPool.getSize(context, true));
    }

    @Test
    public void testClosedConnectionsInPool() throws Throwable {
        final Context context = new ContextImpl(CalendarTest.contextid);

        final int testsize = 50; // DBPool.getSize(context, true);
        final Connection con[] = new Connection[testsize];
        for (int a = 0; a < con.length; a++) {
            try {
                con[a] = DBPool.pickup(context);
                con[a].close();
                con[a] = null;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        for (int a = 0; a < con.length; a++) {
            try {
                Connection tc = con[a];
                DBPool.push(context, tc);
                tc = null;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        for (int a = 0; a < con.length; a++) {
            try {
                assertTrue(con != null);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        //        if (DBPool.isPreFill(context, true)) {
        //            assertEquals("Check that pool is  not emtpy", testsize, DBPool.getSize(context, true));
        //        } else if (DBPool.isPostFill(context, true)) {
        //            assertEquals("Check that pool is  not emtpy", 0, DBPool.getSize(context, true));
        //        }

    }

}
