package com.openexchange.groupware.ldap;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextSearch;
import com.openexchange.groupware.ldap.AuthenticationSupport;
import com.openexchange.groupware.ldap.Credentials;
import com.openexchange.groupware.ldap.Factory;

import junit.framework.TestCase;

public class AuthenticationSupportTest extends TestCase {

    private AuthenticationSupport as;

    protected void setUp() throws Exception {
        super.setUp();
        if (null == LdapTests.p) {
            LdapTests.init();
            if (null == LdapTests.p) {
                throw new Exception("Problem reading properties.");
            }
        }
        Context context = new ContextSearch().getContextByName("defaultcontext");
        as = Factory.newAuthenticationSupport(context);
    }

    protected void tearDown() throws Exception {
        as.close();
        as = null;
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.AuthenticationSupport.findUserBaseDN(String)'
     */
    public void testFindUserBaseDN() throws Throwable {
        String username = LdapTests.p.getProperty("login");
        String foundBaseDN = as.findUserBaseDN(username);
        assertNotNull(foundBaseDN);
        username = LdapTests.p.getProperty("nonexistentlogin");
        foundBaseDN = as.findUserBaseDN(username);
        assertNull(foundBaseDN);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.AuthenticationSupport.authenticateUser(String, String)'
     */
    public void testAuthenticateUser() throws Throwable {
        String username = (String) LdapTests.p.get("login");
        String passwd = (String) LdapTests.p.get("password");
        String foundBaseDN = as.findUserBaseDN(username);
        Credentials creds = as.authenticateUser(foundBaseDN, passwd);
        assertNotNull(creds);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.AuthenticationSupport.isUserEnabled(String, String)'
     */
    public void testIsUserEnabled() throws Throwable {
        String username = (String) LdapTests.p.get("login");
        String passwd = (String) LdapTests.p.get("password");
        assertTrue(as.isUserEnabled(username, passwd));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.AuthenticationSupport.isPasswordExpired(String, String)'
     */
    public void testIsPasswordExpired() throws Throwable {
        String username = (String) LdapTests.p.get("login");
        String passwd = (String) LdapTests.p.get("password");
        assertFalse(as.isPasswordExpired(username, passwd));
    }
}
