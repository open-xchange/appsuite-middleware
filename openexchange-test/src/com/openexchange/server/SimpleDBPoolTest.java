
package com.openexchange.server;

import com.openexchange.api.OXFolder;
import com.openexchange.groupware.*;
import com.openexchange.groupware.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.api.OXCalendar;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.OXFolderTools;
import com.openexchange.tools.oxfolder.OXFolderPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

public class SimpleDBPoolTest extends TestCase {
    
    
    protected void setUp() throws Exception {        
        super.setUp();
        Init.initDB();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testBasicPoolFunctions() throws Throwable {
        Context context = new ContextImpl(CalendarTest.contextid);

        int testsize = DBPool.getReadSize(context);
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
        
        assertEquals("Check pool size ", testsize, DBPool.getReadSize(context));      
    }
    
    public void testClosedConnectionsInPool() throws Throwable {
        Context context = new ContextImpl(CalendarTest.contextid);

        int testsize = DBPool.getReadSize(context);
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
            } catch(Exception e) {
                e.printStackTrace();
            }
        }              
        
        assertEquals("Check that pool is  not emtpy", testsize, DBPool.getReadSize(context));    

    }    
    
}