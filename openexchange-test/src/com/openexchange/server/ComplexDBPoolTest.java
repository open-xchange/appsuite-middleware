package com.openexchange.server;


import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.tools.OXFolderTools;
import com.openexchange.tools.oxfolder.OXFolderPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

public class ComplexDBPoolTest extends TestCase {
    
    private final static int contextid = 1;

    private int userid;

    private int TEST_RUNS = 50;
    
    protected void setUp() throws Exception {        
        super.setUp();
        Init.initDB();
        String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }    
    
    private int resolveUser(String user) throws Exception {
        UserStorage uStorage = UserStorage.getInstance(new ContextImpl(contextid));
        userid = uStorage.getUserId(user);
        return userid;
    }
    
    public void testThreadedPool() throws Throwable {
        Context context = new ContextImpl(contextid);

        int testsize = DBPool.getReadSize(context);
        int min = testsize*4;
        if (TEST_RUNS < min)
            TEST_RUNS = min;
        PoolRunner pr[] = new PoolRunner[TEST_RUNS];
        for (int a = 0; a < TEST_RUNS; a++) {
            pr[a] = new PoolRunner(context);
        }
        
        for (int a = 0; a < TEST_RUNS; a++) {
            pr[a].joinThread();
        }
        
        assertEquals("Check pool size ", testsize, DBPool.getReadSize(context));
        
    }
    
        
    
    
}