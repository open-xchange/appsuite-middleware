/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;

/**
 * {@link AdditionalRMITests}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * */
public class AdditionalRMITests extends AbstractRMITest {

    public String myUserName = "thorben";

    public String myDisplayName = "Thorben Betten";

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AdditionalRMITests.class);
    }

    /**
     * Test the #any method. This explains how it is used, too, in case you either have never seen first-order-functions or seen the
     * monstrosity that is necessary to model them in Java.
     */
    @Test
    public void testAnyHelper() {
        Integer[] myArray = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), };
        Integer inThere = Integer.valueOf(1);
        Integer notInThereInteger = Integer.valueOf(0);

        assertFalse(any(myArray, notInThereInteger, new Verifier<Integer, Integer>() {

            @Override
            public boolean verify(Integer obj1, Integer obj2) {
                return obj1.equals(obj2);
            }
        }));

        assertTrue(any(myArray, inThere, new Verifier<Integer, Integer>() {

            @Override
            public boolean verify(Integer obj1, Integer obj2) {
                return obj1.equals(obj2);
            }
        }));
    }

    /**
     * Looking up users by User#name by checking whether display_name is updated
     */
    @Test
    public void testGetOxAccount() throws Exception {

        OXUserInterface userInterface = getUserInterface();

        User knownUser = new User();
        knownUser.setName(myUserName);
        User[] mailboxNames = new User[] { knownUser }; // users with only their mailbox name (User#name) - the rest is going to be looked
                                                        // up
        User[] queriedUsers = userInterface.getData(adminContext, mailboxNames, adminCredentials); // required line for test

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf(queriedUsers.length));
        User queriedUser = queriedUsers[0];
        assertEquals("Should have looked up display name", myDisplayName, queriedUser.getDisplay_name());
    }

    /**
     * Tests #listAll by comparing it with the result of #getData for all users
     */
    @Test
    public void testGetAllUsers() throws Exception {
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);

        OXUserInterface userInterface = getUserInterface();
        User[] allUsers = userInterface.listAll(context, credentials); // required line for test
        User[] queriedUsers = userInterface.getData(context, allUsers, credentials); // required line for test
        assertIDsAreEqual(allUsers, queriedUsers);
    }

    /*
     * Gets all groups and checks whether our test user is in one ore more
     */
    @Test
    public void testGetOxGroups() throws Exception {
        OXContextInterface conInterface = getContextInterface();
        Context updatedContext = conInterface.getData(adminContext, superAdminCredentials);

        OXUserInterface userInterface = getUserInterface();
        OXGroupInterface groupInterface = getGroupInterface();

        User myUser = new User();
        myUser.setName(myUserName);
        User[] returnedUsers = userInterface.getData(updatedContext, new User[] { myUser }, adminCredentials);
        assertEquals(Integer.valueOf(1), Integer.valueOf(returnedUsers.length));
        User myUpdatedUser = returnedUsers[0];
        Group[] allGroups = groupInterface.listAll(adminContext, adminCredentials);

        assertTrue("User's ID group should be found in a group", any(allGroups, myUpdatedUser.getId(), new Verifier<Group, Integer>() {

            @Override
            public boolean verify(Group group, Integer userid) {
                return (Arrays.asList(group.getMembers())).contains(userid);
            }
        }));
    }

    /**
     * Creates a resource and checks whether it is found
     */
    @Test
    public void testGetOxResources() throws Exception {
        Resource res = getTestResource();
        createTestResource();
        try {
            OXResourceInterface resInterface = getResourceInterface();
            List<Resource> allResources = Arrays.asList(resInterface.listAll(adminContext, adminCredentials));
            assertTrue("Should contain our trusty test resource", any(allResources, res, new Verifier<Resource, Resource>() {

                @Override
                public boolean verify(Resource fromCollection, Resource myResource) {
                    return myResource.getDisplayname().equals(fromCollection.getDisplayname()) && myResource.getEmail().equals(
                        fromCollection.getEmail()) && myResource.getName().equals(fromCollection.getName());
                }
            }));
        } finally {
            removeTestResource();
        }
    }

    /**
     * Tests creating a context, setting the access level and creating a first user for that context. The first user in a context is usually
     * the admin. Do not test creation of the first normal user, #testCreateOxUser() does that already
     */
    @Test
    public void testCreateFirstUser() throws Exception {
        OXContextInterface conInterface = getContextInterface();

        Context newContext = newContext("newContext", 666);

        User newAdmin = newUser("new_admin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        try {
            newContext = conInterface.create(newContext, newAdmin, superAdminCredentials); // required line for test
            Credentials newAdminCredentials = new Credentials();
            newAdmin.setId(Integer.valueOf(2)); // has to be hardcoded, because it cannot be looked up easily.
            newAdminCredentials.setLogin(newAdmin.getName());
            newAdminCredentials.setPassword("secret");
            assertUserWasCreatedProperly(newAdmin, newContext, newAdminCredentials);
        } finally {
            // no need to delete the admin account. Actually, it is not possible at all.
            conInterface.delete(newContext, superAdminCredentials);
        }
    }

    @Test
    public void testCreateOxUser() throws Exception {
        User myNewUser = newUser("new_user", "secret", "New User", "New", "User", "newuser@ox.invalid");
        UserModuleAccess access = new UserModuleAccess();

        boolean userCreated = false;
        OXUserInterface userInterface = getUserInterface();
        try {
            myNewUser = userInterface.create(adminContext, myNewUser, access, adminCredentials);// required line for test
            userCreated = true;
            assertUserWasCreatedProperly(myNewUser, adminContext, adminCredentials);
        } finally {
            if (userCreated) {
                userInterface.delete(adminContext, myNewUser, adminCredentials);
            }
        }
    }

    /**
     * Test the creation of a group
     */
    @Test
    public void testCreateOxGroup() throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        boolean groupCreated = false;
        Group group = newGroup("groupdisplayname", "groupname");
        try {
            group = groupInterface.create(adminContext, group, adminCredentials); // required line for test
            groupCreated = true;
            assertGroupWasCreatedProperly(group, adminContext, adminCredentials);
        } finally {
            if (groupCreated) {
                groupInterface.delete(adminContext, group, adminCredentials);
            }
        }
    }

    @Test
    public void testCreateOxResource() throws Exception {
        OXResourceInterface resInterface = getResourceInterface();
        boolean resourceCreated = false;
        Resource res = newResource("resourceName", "resourceDisplayname", "resource@email.invalid");
        try {
            res = resInterface.create(adminContext, res, adminCredentials);// required line for test
            resourceCreated = true;
            assertResourceWasCreatedProperly(res, adminContext, adminCredentials);
        } finally {
            if (resourceCreated) {
                resInterface.delete(adminContext, res, adminCredentials);
            }
        }
    }

    @Test
    public void testUpdateOxAdmin_updateOxUser() throws Exception {
        OXUserInterface userInterface = getUserInterface();
        boolean valueChanged = false;
        User admin = getAdminData();
        String originalValue = admin.getAssistant_name();
        User changesToAdmin = new User();
        changesToAdmin.setId(admin.getId());
        String newAssistantName = "Herbert Feuerstein";
        changesToAdmin.setAssistant_name(newAssistantName);
        assertFalse("Precondition: Old assistant name should differ from new assistant name", newAssistantName.equals(originalValue));
        try {
            userInterface.change(adminContext, changesToAdmin, adminCredentials);// required line for test
            valueChanged = true;
            admin = getAdminData(); // refresh data
            assertEquals(changesToAdmin.getAssistant_name(), admin.getAssistant_name());
        } finally {
            if (valueChanged) {
                changesToAdmin.setAssistant_name(originalValue);
                userInterface.change(adminContext, changesToAdmin, adminCredentials);
            }
        }
    }

    @Test
    public void testUpdateOxGroup() throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        boolean groupCreated = false;
        Group group = newGroup("groupdisplayname", "groupname");
        try {
            group = groupInterface.create(adminContext, group, adminCredentials);
            groupCreated = true;
            Group groupChange = new Group();
            groupChange.setId(group.getId());
            groupChange.setName("changed groupname");
            groupInterface.change(adminContext, groupChange, adminCredentials); // required line for test
            group = groupInterface.getData(adminContext, group, adminCredentials); // update

            assertEquals("Name should have been changed", group.getName(), groupChange.getName());
        } finally {
            if (groupCreated) {
                groupInterface.delete(adminContext, group, adminCredentials);
            }
        }
    }

    @Test
    public void testUpdateOxResource() throws Exception {
        OXResourceInterface resInterface = getResourceInterface();
        boolean resourceCreated = false;
        Resource res = newResource("resourceName", "resourceDisplayname", "resource@email.invalid");
        try {
            res = resInterface.create(adminContext, res, adminCredentials);
            resourceCreated = true;
            Resource resChange = new Resource();
            resChange.setId(res.getId());
            resChange.setDisplayname("changed display name");
            resInterface.change(adminContext, resChange, adminCredentials);// required line for test
            res = resInterface.getData(adminContext, res, adminCredentials); // update
            assertEquals("Display name should have changed", resChange.getDisplayname(), res.getDisplayname());
        } finally {
            if (resourceCreated) {
                resInterface.delete(adminContext, res, adminCredentials);
            }
        }
    }

    @Test
    public void testDeleteOxUsers() {
        // already done in the clean-up procedures for #testUpdateOxResources and #testUpdateOxResources
    }

    @Test
    public void testDeleteOxGroups() {
        // already done in the clean-up procedures for testCreateOxResources and #testUpdateOxResources
    }

    @Test
    public void testDeleteOxResources() {
        // already done in the clean-up procedures for #testCreateOxResources and #testUpdateOxResources
    }

    @Test
    public void testDeleteOxAccount() {
        // already done in the clean-up procedure for #testCreateFirstUser()
    }

    @Test
    public void testGetUserAccessModules() throws Exception {
        // OxUserInterface.getModuleAccess(Context, User, null);
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);

        OXUserInterface userInterface = (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);

        User knownUser = new User();
        knownUser.setName("thorben");
        User[] mailboxNames = new User[] { knownUser }; // users with only their mailbox name (User#name) - the rest is going to be looked
                                                        // up
        User[] queriedUsers = userInterface.getData(context, mailboxNames, credentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf(queriedUsers.length));
        User user = queriedUsers[0];

        UserModuleAccess access = userInterface.getModuleAccess(context, user, credentials);
        assertTrue("Information for module access should be available", access != null);
    }

    @Test
    public void testSetUserAccessModules() {
        // OxUserInterface.changeModuleAccess(Context, User, UserModuleAccess, null);
        // This is tested by #testUpdateModuleAccess() below
    }

    @Test
    public void testUpdateMaxCollapQuota() throws Exception {
        // OXContextInterface.change(Context, null);
        OXContextInterface contextInterface = getContextInterface();
        Context context = contextInterface.getData(adminContext, superAdminCredentials);
        Long maxQuotaBefore = context.getMaxQuota();
        Long updatedMaxQuota = new Long(1024);
        context.setMaxQuota(updatedMaxQuota);
        contextInterface.change(context, superAdminCredentials);
        Context newContext = contextInterface.getData(adminContext, superAdminCredentials);
        assertEquals("MaxCollapQuota should have the new value", newContext.getMaxQuota(), updatedMaxQuota);
    }

    @Test
    public void testGetUser() throws Exception {
        // OxUserInterface.getData(Context, User, null); //query by mailboxName (User.name)
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);

        OXUserInterface userInterface = (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);

        User knownUser = new User();
        knownUser.setName("thorben");
        User[] mailboxNames = new User[] { knownUser }; // users with only their mailbox name (User#name) - the rest is going to be looked
                                                        // up
        User[] queriedUsers = userInterface.getData(context, mailboxNames, credentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf(queriedUsers.length));
        User user = queriedUsers[0];
        User queriedUser = userInterface.getData(context, user, credentials);
        assertEquals("Should have looked up display name", myDisplayName, queriedUser.getDisplay_name());

    }

    @Test
    public void testUpdateModuleAccess() throws Exception {
        // userInterface.changeModuleAccess(context, user, moduleAccess, auth);
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);

        OXUserInterface userInterface = (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);

        User knownUser = new User();
        knownUser.setName(myUserName);
        User[] mailboxNames = new User[] { knownUser }; // users with only their mailbox name (User#name) - the rest is going to be looked
                                                        // up
        User[] queriedUsers = userInterface.getData(context, mailboxNames, credentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf(queriedUsers.length));
        User user = queriedUsers[0];

        UserModuleAccess access = userInterface.getModuleAccess(context, user, credentials);
        assertEquals("Calendar access should be granted by default", true, access.getCalendar());
        access.setCalendar(false);
        userInterface.changeModuleAccess(context, user, access, credentials);
        access = userInterface.getModuleAccess(context, user, credentials);
        assertEquals("Calendar access should be turned off now", false, access.getCalendar());
        // reset access and check again
        access.setCalendar(true);
        userInterface.changeModuleAccess(context, user, access, credentials);
        access = userInterface.getModuleAccess(context, user, credentials);
        assertEquals("Calendar access should be granted again", true, access.getCalendar());

    }

    @Test
    public void testContextExistsException() throws Exception {
        OXContextInterface conInterface = getContextInterface();
        boolean contextCreated = false;
        Context newContext = newContext("newContext", 666);
        User newAdmin = newUser("new_admin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        try {
            newContext = conInterface.create(newContext, newAdmin, superAdminCredentials);
            contextCreated = true;
            try {
                conInterface.create(newContext, newAdmin, superAdminCredentials);
                fail("Should throw ContextExistsException");
            } catch (ContextExistsException e) {
                assertTrue("Caught exception", true);
            }
        } finally {
            if (contextCreated) {
                conInterface.delete(newContext, superAdminCredentials);
            }
        }
    }

    @Test
    public void testNoSuchContextException() throws Exception {
        OXContextInterface conInterface = getContextInterface();
        Context missingContext = newContext("missing", Integer.MAX_VALUE);
        try {
            conInterface.delete(missingContext, superAdminCredentials);
            fail("Expected NoSuchContextException");
        } catch (NoSuchContextException e) {
            assertTrue("Caught exception", true);
        }
    }

    @Test
    public void testNoSuchGroupException() throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        Group missingGroup = new Group();
        missingGroup.setId(Integer.valueOf(Integer.MAX_VALUE));
        try {
            groupInterface.delete(adminContext, missingGroup, adminCredentials);
            fail("Expected NoSuchGroupException");
        } catch (NoSuchGroupException e) {
            assertTrue("Caught exception", true);
        }
    }

    @Test
    public void testNoSuchResourceException() throws Exception {
        OXResourceInterface resInterface = getResourceInterface();
        Resource missingResource = new Resource();
        missingResource.setId(Integer.valueOf(Integer.MAX_VALUE));
        try {
            resInterface.delete(adminContext, missingResource, adminCredentials);
            fail("Expected NoSuchResourceException");
        } catch (NoSuchResourceException e) {
            assertTrue("Caught exception", true);
        }
    }

    @Test
    public void testNoSuchUserException() throws Exception {
        OXUserInterface resInterface = getUserInterface();
        User missingUser = new User();
        missingUser.setId(Integer.valueOf(Integer.MAX_VALUE));
        try {
            resInterface.delete(adminContext, missingUser, adminCredentials);
            fail("Expected NoSuchUserException");
        } catch (NoSuchUserException e) {
            assertTrue("Caught exception", true);
        }
    }
}
