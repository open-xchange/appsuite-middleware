package com.openexchange.groupware.ldap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

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
			LdapTests.init();
            if (null == LdapTests.p) {
                throw new Exception("Problem reading properties.");
            }
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
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        String group1 = LdapTests.p.getProperty("group1");
        Set groups = ugh.getGroupsForUser(user1);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group1));
        groups = ugh.getGroupsForUser(user2);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group1));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getForeSureName(String)'
     */
    public void testGetForeSureName() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        String[] fsname = ugh.getForeSureName(user1);
        assertEquals(fsname[0], "Marcus");
        assertEquals(fsname[1], "Klein");
        fsname = ugh.getForeSureName(user2);
        assertEquals(fsname[0], "Martin");
        assertEquals(fsname[1], "Kauss");
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchGroups(String)'
     */
    public void testSearchGroups() {
        Set groups = ugh.searchGroups("*");
        assertNotNull(groups);
        assertTrue(groups.size() > 0);
        assertTrue(groups.contains("users"));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchFSUidUsers(String)'
     */
    public void testSearchFSUidUsers() {
        Map users = ugh.searchFSUidUsers("*");
        assertNotNull(users);
        assertTrue(users.size() > 0);
        assertTrue(users.containsKey(LdapTests.p.getProperty("user1")));
        assertTrue(users.containsKey(LdapTests.p.getProperty("user2")));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchFullName(String)'
     */
    public void testSearchFullName() {
        Map users = ugh.searchFullName("*");
        assertNotNull(users);
        assertTrue(users.size() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchUsers(String)'
     */
    public void testSearchUsersString() {
        Set users = ugh.searchUsers("*");
        assertNotNull(users);
        assertTrue(users.size() > 0);
        assertTrue(users.contains(LdapTests.p.getProperty("user1")));
        assertTrue(users.contains(LdapTests.p.getProperty("user2")));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.searchUsers(String, long)'
     */
    public void testSearchUsersStringLong() throws Throwable {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String usermodifiedsince = LdapTests.p.getProperty("usermodifiedsince");
        Date d = df.parse(usermodifiedsince);
        Set users = ugh.searchUsers("*", d.getTime());
        assertTrue(users.size() > 0);
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
           assertTrue(found.size() > 0);
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
        String group2 = LdapTests.p.getProperty("group2");
        Map users = ugh.getFSUsersInGroup(group2);
        assertNotNull(users);
        assertTrue(users.size() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getGroups()'
     */
    public void testGetGroups() {
        Set groups = ugh.getGroups();
        assertNotNull(groups);
        assertTrue(groups.size() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getUsersInGroup(String)'
     */
    public void testGetUsersInGroupString() {
        String group = LdapTests.p.getProperty("group1");
		Set s = ugh.getUsersInGroup(group);
		assertNotNull(s);
		assertTrue(s.size() > 0);
        group = LdapTests.p.getProperty("group2");
        s = ugh.getUsersInGroup(group);
        assertNotNull(s);
        assertTrue(s.size() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getUsersInGroup(String, String[])'
     */
    public void testGetUsersInGroupStringStringArray() {
        String group1 = LdapTests.p.getProperty("group1");
        String group2 = LdapTests.p.getProperty("group2");
        String[] attributes =  new String[] { UserStorage.MODIFYTIMESTAMP,
            UserStorage.DISPLAYNAME, UserStorage.ALIAS,
            UserStorage.IMAPSERVER };
        Set users = ugh.getUsersInGroup(group1, attributes);
        assertNotNull(users);
        assertTrue(users.size() > 0);
        Iterator iter = users.iterator();
        while (iter.hasNext()) {
            String[] values = (String[]) iter.next();
            assertEquals(attributes.length, values.length);
        }
        users = ugh.getUsersInGroup(group2, attributes);
        assertNotNull(users);
        assertTrue(users.size() > 0);
        iter = users.iterator();
        while (iter.hasNext()) {
            String[] values = (String[]) iter.next();
            assertEquals(attributes.length, values.length);
        }
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.existsGroup(String)'
     */
    public void testExistsGroup() {
        String group1 = "Users";
        assertTrue(ugh.existsGroup(group1));
        String nonExistentGroup = "nonExistentGroup";
        assertFalse(ugh.existsGroup(nonExistentGroup));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.existsUser(String)'
     */
    public void testExistsUser() {
        String user1 = LdapTests.p.getProperty("user1");
        String nonexistentuser = LdapTests.p.getProperty("nonexistentuser");
        assertTrue(ugh.existsUser(user1));
        assertFalse(ugh.existsUser(nonexistentuser));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getMail(String)'
     */
    public void testGetMail() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user1");
        assertNotNull(ugh.getMail(user1));
        assertNotNull(ugh.getMail(user2));
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getQualifiedMailDomain()'
     */
    public void testGetQualifiedMailDomain() {
        String domain = ugh.getQualifiedMailDomain();
        assertNotNull(domain);
        assertTrue(domain.length() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getMailServer()'
     */
    public void testGetMailServer() {
        String[] server = ugh.getMailServer();
        assertNotNull(server);
        assertTrue(server.length == 2);
        assertNotNull(server[0]);
        assertNotNull(server[1]);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getTimeZone(String)'
     */
    public void testGetTimeZone() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        String timezone = ugh.getTimeZone(user1);
        assertNotNull(timezone);
        assertTrue(timezone.length() > 0);
        timezone = ugh.getTimeZone(user2);
        assertNotNull(timezone);
        assertTrue(timezone.length() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getAppointmentDays(String)'
     */
    public void testGetAppointmentDays() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        int days = ugh.getAppointmentDays(user1);
        assertTrue(days > 0);
        days = ugh.getAppointmentDays(user2);
        assertTrue(days > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getTaskDays(String)'
     */
    public void testGetTaskDays() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        int days = ugh.getTaskDays(user1);
        assertTrue(days > 0);
        days = ugh.getTaskDays(user2);
        assertTrue(days > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getPreferedLanguage(String)'
     */
    public void testGetPreferedLanguage() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        String language = ugh.getPreferedLanguage(user1);
        assertNotNull(language);
        assertTrue(language.length() > 0);
        language = ugh.getPreferedLanguage(user2);
        assertNotNull(language);
        assertTrue(language.length() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getCountry(String)'
     */
    public void testGetCountry() {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        String country = ugh.getCountry(user1);
        assertNotNull(country);
        assertTrue(country.length() > 0);
        country = ugh.getCountry(user2);
        assertNotNull(country);
        assertTrue(country.length() > 0);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.getAliases(String)'
     */
    public void testGetAliases() throws Throwable {
        String user1 = LdapTests.p.getProperty("user1");
        String user2 = LdapTests.p.getProperty("user2");
        Set aliases = ugh.getAliases(user1);
        assertNotNull(aliases);
        assertTrue(aliases.size() > 0);
        aliases = ugh.getAliases(user2);
        assertNotNull(aliases);
        assertTrue(aliases.size() > 0);
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
    public void testGetUserAttributes() throws Throwable {
        String user1 = LdapTests.p.getProperty("user1");
        String modifyTimestamp = ugh.getAttributeName(Names
            .USER_ATTRIBUTE_MODIFYTIMESTAMP);
        Map test = ugh.getUserAttributes(user1, new String[]{ modifyTimestamp });
        assertTrue(test.containsKey(modifyTimestamp));
        assertNotNull(test.get(modifyTimestamp));
        String alias = ugh.getAttributeName(Names.USER_ATTRIBUTE_ALIAS);
        test = ugh.getUserAttributes(user1, new String[] { alias });
        assertTrue(test.containsKey(alias));
        String[] aliases = (String[]) test.get(alias);
        assertNotNull(aliases);
    }

    /*
     * Test method for 'com.openexchange.groupware.ldap.UserGroupHandle.updateUserAttributes(String, Map)'
     */
    public void testUpdateUserAttributes() {
        fail("Unimplemented");
    }

}
