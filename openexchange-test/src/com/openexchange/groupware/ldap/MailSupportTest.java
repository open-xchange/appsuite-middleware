package com.openexchange.groupware.ldap;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextSearch;
import com.openexchange.groupware.ldap.Factory;
import com.openexchange.groupware.ldap.MailSupport;

import junit.framework.TestCase;

public class MailSupportTest extends TestCase {

    private MailSupport ms;

    protected void setUp() throws Exception {
        super.setUp();
        if (null == LdapTests.p) {
            LdapTests.init();
            if (null == LdapTests.p) {
                throw new Exception("Problem reading properties.");
            }
        }
        Context context = new ContextSearch().getContextByName("defaultcontext");
        ms = Factory.newMailSupport(context, null);
    }

    protected void tearDown() throws Exception {
        ms.close();
        ms = null;
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.MailSupport.getGlobalMailConfig()'
     */
    public void testGetGlobalMailConfig() throws Throwable {
        String[] test = ms.getGlobalMailConfig();
        assertNotNull(test[0]);
        assertNotNull(test[1]);
        assertNotNull(test[2]);
    }
}
