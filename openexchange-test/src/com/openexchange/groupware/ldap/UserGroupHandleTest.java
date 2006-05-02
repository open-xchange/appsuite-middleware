package com.openexchange.groupware.ldap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.openexchange.groupware.contexts.TestContextImpl;
import com.openexchange.groupware.ldap.Factory;
import com.openexchange.groupware.ldap.Names;
import com.openexchange.groupware.ldap.UserGroupHandle;

import junit.framework.TestCase;

public class UserGroupHandleTest extends TestCase {

    private UserGroupHandle ugh;

    protected void setUp() throws Exception {
        super.setUp();
        if (null == LdapTests.p) {
            throw new Exception("Can only be run in a test suite.");
        }
        ugh = Factory.newUserGroupHandle(new TestContextImpl(), null);
    }

    protected void tearDown() throws Exception {
        ugh.close();
        ugh = null;
        super.tearDown();
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getGroupsForUser(String)'
     */
    public void testGetGroupsForUser() {
        String username = LdapTests.p.getProperty("username");
        String otheruser = LdapTests.p.getProperty("otheruser");
        String group1 = LdapTests.p.getProperty("group1");
        Set groups = ugh.getGroupsForUser(username);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group1));
        groups = ugh.getGroupsForUser(otheruser);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group1));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getForeSureName(String)'
     */
    public void testGetForeSureName() {
        String username = LdapTests.p.getProperty("username");
        String otheruser = LdapTests.p.getProperty("otheruser");
        String[] fsname = ugh.getForeSureName(username);
        assertEquals(fsname[0], "Marcus");
        assertEquals(fsname[1], "Klein");
        fsname = ugh.getForeSureName(otheruser);
        assertEquals(fsname[0], "Martin");
        assertEquals(fsname[1], "Kauss");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchGroups(String)'
     */
    public void testSearchGroups() {
        Set groups = ugh.searchGroups("*");
        assertEquals(1, groups.size());
        assertTrue(groups.contains("users"));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchFSUidUsers(String)'
     */
    public void testSearchFSUidUsers() {
        Map users = ugh.searchFSUidUsers("*");
        assertEquals(3, users.size());
        assertTrue(users.containsKey(LdapTests.p.getProperty("username")));
        assertTrue(users.containsKey(LdapTests.p.getProperty("otheruser")));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchFullName(String)'
     */
    public void testSearchFullName() {
        Map users = ugh.searchFullName("*");
        assertEquals(3, users.size());
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchUsers(String)'
     */
    public void testSearchUsersString() {
        Set users = ugh.searchUsers("*");
        assertEquals(3, users.size());
        assertTrue(users.contains(LdapTests.p.getProperty("username")));
        assertTrue(users.contains(LdapTests.p.getProperty("otheruser")));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchUsers(String, long)'
     */
    public void testSearchUsersStringLong() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchUser(String, String[], String[])'
     */
    public void testSearchUser() throws Throwable {
        StringTokenizer searchemail = new StringTokenizer(LdapTests.p
            .getProperty("searchemails"), ",");
        while (searchemail.hasMoreTokens()) {
           String email = searchemail.nextToken();
           String toFindUser = searchemail.nextToken();
           Set found = ugh.searchUser(email,
              new String[] {
                 ugh.getAttributeName(Names.USER_ATTRIBUTE_ALIAS),
                 ugh.getAttributeName(Names.USER_ATTRIBUTE_MAIL)},
              new String[] {
                 ugh.getAttributeName(Names.USER_ATTRIBUTE_UID)});
           Iterator iter = found.iterator();
           while (iter.hasNext()) {
              String[] attributes = (String[]) iter.next();
              assertEquals(toFindUser, attributes[0]);
           }
        }
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getFSUsersInGroup(String)'
     */
    public void testGetFSUsersInGroup() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getGroups()'
     */
    public void testGetGroups() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getUsersInGroup(String)'
     */
    public void testGetUsersInGroupString() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getUsersInGroup(String, String[])'
     */
    public void testGetUsersInGroupStringStringArray() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.existsGroup(String)'
     */
    public void testExistsGroup() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.existsUser(String)'
     */
    public void testExistsUser() {
        String username = LdapTests.p.getProperty("username");
        String nonexistentuser = LdapTests.p.getProperty("nonexistentuser");
        assertTrue(ugh.existsUser(username));
        assertFalse(ugh.existsUser(nonexistentuser));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getMail(String)'
     */
    public void testGetMail() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getQualifiedMailDomain()'
     */
    public void testGetQualifiedMailDomain() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getMailServer()'
     */
    public void testGetMailServer() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getTimeZone(String)'
     */
    public void testGetTimeZone() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getAppointmentDays(String)'
     */
    public void testGetAppointmentDays() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getTaskDays(String)'
     */
    public void testGetTaskDays() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getPreferedLanguage(String)'
     */
    public void testGetPreferedLanguage() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getCountry(String)'
     */
    public void testGetCountry() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getAliases(String)'
     */
    public void testGetAliases() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getVAddresses(String)'
     */
    public void testGetVAddresses() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchForeSureNameUsers(String)'
     */
    public void testSearchForeSureNameUsers() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getUserAttributes(String, String[])'
     */
    public void testGetUserAttributes() {
        fail("Unimplemented");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.updateUserAttributes(String, Map)'
     */
    public void testUpdateUserAttributes() {
        fail("Unimplemented");
    }

}
