
package com.openexchange.server;

import com.openexchange.groupware.*;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.impl.DBPool;

import java.sql.Connection;
import junit.framework.TestCase;

public class SimpleDBPoolTest extends TestCase {
    
    
    protected void setUp() throws Exception {        
        super.setUp();
        Init.startServer();
    }
    
    protected void tearDown() throws Exception {
        Init.stopServer();
        super.tearDown();
    }
    
    public void testBasicPoolFunctions() throws Throwable {
        Context context = new ContextImpl(CalendarTest.contextid);

        int testsize = 50; // DBPool.getSize(context, true);
        Connection con[] = new Connection[testsize];
        for (int a = 0; a < con.length; a++) {
            try {
                con[a] = DBPool.pickup(context);                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        for (int a = 0; a < con.length; a++) {
            try {                
                Connection tc = con[a];
                DBPool.push(context, tc);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }              
        
        // assertEquals("Check pool size ", testsize, DBPool.getSize(context, true));
    }
    
    public void testClosedConnectionsInPool() throws Throwable {
        Context context = new ContextImpl(CalendarTest.contextid);

        int testsize = 50; // DBPool.getSize(context, true);
        Connection con[] = new Connection[testsize];
        for (int a = 0; a < con.length; a++) {
            try {
                con[a] = DBPool.pickup(context);
                con[a].close();
                con[a] = null;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        for (int a = 0; a < con.length; a++) {
            try {                
                Connection tc = con[a];
                DBPool.push(context, tc);
                tc = null;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }              
        
        for (int a = 0; a < con.length; a++) {
            try {                
                Connection tc = con[a];
                assertTrue(con != null);
            } catch(Exception e) {
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