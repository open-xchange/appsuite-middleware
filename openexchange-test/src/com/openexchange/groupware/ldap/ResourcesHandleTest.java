package com.openexchange.groupware.ldap;

import com.openexchange.groupware.contexts.TestContextImpl;
import com.openexchange.groupware.ldap.Factory;
import com.openexchange.groupware.ldap.ResourcesHandle;

import junit.framework.TestCase;

public class ResourcesHandleTest extends TestCase {

    private ResourcesHandle rh;
    
    protected void setUp() throws Exception {
        super.setUp();
        if (null == LdapTests.p) {
            throw new Exception("Can only be run in a test suite.");
        }
        rh = Factory.newResourcesHandle(new TestContextImpl(), null);
    }

    protected void tearDown() throws Exception {
        rh.close();
        rh = null;
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.existsGroup(String)'
     */
    public void testExistsGroup() throws Throwable {
        String resourceGroup1 = LdapTests.p.getProperty("resourceGroup1");
        assertTrue(rh.existsGroup(resourceGroup1));
        String nonExistentGroup = LdapTests.p.getProperty("nonExistentResourceGroup");
        assertFalse(rh.existsGroup(nonExistentGroup));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.existsResource(String)'
     */
    public void testExistsResource() {
        String resource1 = LdapTests.p.getProperty("resource1");
        assertTrue(rh.existsResource(resource1));
        String nonExistentResource = LdapTests.p.getProperty("nonExistentResource");
        assertFalse(rh.existsResource(nonExistentResource));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.getGroups()'
     */
    public void testGetGroups() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.getResourcesInGroup(String)'
     */
    public void testGetResourcesInGroup() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.isResGroupNotAvailable(String)'
     */
    public void testIsResGroupNotAvailable() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.searchGroups(String)'
     */
    public void testSearchGroups() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.RdbResourcesHandle.searchResources(String)'
     */
    public void testSearchResources() {
        fail("Unimplemented");
    }
}
