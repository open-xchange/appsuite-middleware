
package com.openexchange.server;

import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.test.AjaxInit;

public class ComplexDBPoolTest {

    private final static int contextid = 1;

    private int userid;

    private final int TEST_RUNS = 50;

    private static int checksize = 0;
    private static int poolsize = 0;
    private static int totalcount = 0;

    @Before
    public void setUp() throws Exception {
        Init.startServer();
        AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
    }

    @After
    public void tearDown() throws Exception {
        Init.stopServer();
    }

    protected Properties getAJAXProperties() {
        final Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }

    private int resolveUser(final String user) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance();
        userid = uStorage.getUserId(user, new ContextImpl(contextid));
        return userid;
    }

    @Test
    public void testThreadedPool() throws Throwable {
        final Context context = new ContextImpl(contextid);
        // poolsize = DBPool.getSize(context, true);
        checksize = poolsize;
        final PoolRunner pr[] = new PoolRunner[TEST_RUNS];
        for (int a = 0; a < TEST_RUNS; a++) {
            pr[a] = new PoolRunner(context, false);
        }

        for (int a = 0; a < TEST_RUNS; a++) {
            // assertTrue("Check pool size ", DBPool.getSize(context, true) <= poolsize);
            pr[a].getRunnerThread().join();
        }
        // assertEquals("Check pool size ", checksize, DBPool.getSize(context, true));
    }

    @Test
    public void testThreadedPoolWithClosedConnections() throws Throwable {
        final Context context = new ContextImpl(contextid);
        // int poolsize = DBPool.getSize(context, true);
        checksize = poolsize;
        final PoolRunner pr[] = new PoolRunner[TEST_RUNS];
        for (int a = 0; a < TEST_RUNS; a++) {
            pr[a] = new PoolRunner(context, false);
        }

        for (int a = 0; a < TEST_RUNS; a++) {
            // assertTrue("Check pool size ", DBPool.getSize(context, true) <= poolsize);
            pr[a].getRunnerThread().join();
        }
        // assertEquals("Check pool size ", poolsize, DBPool.getSize(context, true));

        System.err.println("Total runs : " + totalcount);
    }

}
