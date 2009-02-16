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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * {@link AdditionalRMITests}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>

 *
 */
public class AdditionalRMITests extends AbstractRMITest {
    public String myUserName = "thorben";
    public String myDisplayName = "Thorben Betten";
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AdditionalRMITests.class);
    }
    
    /**
     * Test the #any method. 
     * This explains how it is used, too, in case you either have never seen first-order-functions
     * or seen the monstrosity that is necessary to model them in Java.
     */
    @Test public void testAnyHelper(){
        Integer[] myArray = new Integer[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), };
        Integer inThere = Integer.valueOf(1);
        Integer notInThereInteger = Integer.valueOf(0);
        
        assertFalse(any(myArray, notInThereInteger, new Verifier<Integer, Integer>(){
            public boolean verify(Integer obj1, Integer obj2) {
                return obj1.equals(obj2);
            }}));

        assertTrue(any(myArray, inThere, new Verifier<Integer, Integer>(){
            public boolean verify(Integer obj1, Integer obj2) {
                return obj1.equals(obj2);
            }}));
    }
    /**
     * Looking up users by User#name by checking whether display_name is updated
     */
    @Test public void testGetOxAccount() throws Exception{
        
        OXUserInterface userInterface = getUserInterface();

        User knownUser = new User();
        knownUser.setName(myUserName);
        User[] mailboxNames = new User[]{ knownUser}; //users with only their mailbox name (User#name) - the rest is going to be looked up
        User[] queriedUsers = userInterface.getData(testContext, mailboxNames, testCredentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf( queriedUsers.length ));
        User queriedUser = queriedUsers[0];
        assertEquals("Should have looked up display name", myDisplayName, queriedUser.getDisplay_name());
    }
 
    /**
     * Tests #listAll by comparing it with the result of #getData for all users
     */
    @Test public void testGetAllUsers() throws Exception{
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);
        
        OXUserInterface userInterface = getUserInterface();
        User[] allUsers = userInterface.listAll(context, credentials); 
        User[] queriedUsers = userInterface.getData(context, allUsers , credentials); // query by userIds
        assertIDsAreEqual( allUsers, queriedUsers );
    }

    /*
     * Gets all groups and checks whether our test user is in one ore more
     */
    @Test public void testGetOxGroups() throws Exception{
        OXContextInterface conInterface = getContextInterface();
        Context updatedContext = conInterface.getData(testContext, adminCredentials);
        
        OXUserInterface userInterface = getUserInterface();
        OXGroupInterface groupInterface = getGroupInterface();
        
        User myUser = new User();
        myUser.setName( myUserName );
        User[] returnedUsers = userInterface.getData(updatedContext, new User[]{myUser}, testCredentials);
        assertEquals(Integer.valueOf( 1 ), Integer.valueOf( returnedUsers.length ) );
        User myUpdatedUser = returnedUsers[0];
        Group[] allGroups = groupInterface.listAll(testContext, testCredentials);
        
        assertTrue("User's ID group should be found in a group", 
            any( allGroups, myUpdatedUser.getId(), new Verifier<Group,Integer>(){ 
                public boolean verify(Group group, Integer userid) {
                    return (Arrays.asList( group.getMembers() )).contains(userid); }})
               );
    }

    /**
     * Creates a resource and checks whether it is found
     */
    @Test public void testGetOxResources() throws Exception{
        Resource res = getTestResource();
        createTestResource();
        try {
            OXResourceInterface resInterface = getResourceInterface();
            List<Resource> allResources = Arrays.asList( resInterface.listAll(testContext, testCredentials) );
            assertTrue("Should contain our trusty test resource", any(allResources, res, new Verifier<Resource,Resource>(){
                public boolean verify(Resource fromCollection, Resource myResource) {
                    return myResource.getDisplayname().equals(fromCollection.getDisplayname())
                    && myResource.getEmail().equals(fromCollection.getEmail())
                    && myResource.getName().equals(fromCollection.getName());
                } }));
        } finally {
            removeTestResource();
        }
    }
    /**
     * Tests creating a context, 
     * setting the access level and 
     * creating a first user for that context.
     */
    @Test public void testCreateFirstUser() throws Exception {
        OXContextInterface conInterface = getContextInterface();
        OXUserInterface userInterface = getUserInterface();
        
        Context myNewContext = new Context();
        Filestore filestore = new Filestore();
        filestore.setSize(Long.valueOf(128l));
        myNewContext.setFilestoreId(filestore.getId());
        myNewContext.setName("newContext");
        myNewContext.setMaxQuota(filestore.getSize());
        myNewContext.setId( Integer.valueOf(666) );
        
        User myNewUser = null;
        boolean userCreated = false;
        try {
            conInterface.create(myNewContext, adminUser, adminCredentials); //
            UserModuleAccess myNewAccessRules = new UserModuleAccess();
            myNewAccessRules.setCalendar(true);
                
            userInterface.changeModuleAccess(myNewContext, adminUser, myNewAccessRules, adminCredentials); //
            myNewUser = newUser("new_user", "secret", "New User", "New", "User", "newuser@ox.invalid");

            userInterface.create(myNewContext, myNewUser, adminCredentials); //
            userCreated = true;
        } finally {
            if(userCreated){
                userInterface.delete(myNewContext, myNewUser, adminCredentials);
            }
            conInterface.delete(myNewContext, adminCredentials);
        }
    }

    @Test public void testCreateOxUser(){
        //OxUserInterface.create(Context, User, UserModuleAccess, null); 
    }

    @Test public void testCreateOxGroup(){ 
        //OxGroupInterface.create(Context,Group, null);
    }

    @Test public void testCreateOxResource(){ 
        //OxResourceInterface.create(Context, Resource, null);
    }

    @Test public void testUpdateOxAdmin_updateOxUser(){ 
        //OxUserInterface.change(Context, User, null);
    }

    @Test public void testUpdateOxGroup(){
        //OxGroupInterface.change(Context, Group, null); 
    }
    @Test public void testUpdateOxResource(){ 
        //OxResourceInterface.change(Context, Resource, null);
    }

    @Test public void testDeleteOxUsers(){
        //OxUserInterface.delete(Context, User[], null); //user identified by mailboxName (User.name)
    }
    @Test public void testDeleteOxGroups(){ 

        //OXGroupInterface.delete(Context, Group[], null);
    }
    @Test public void testDeleteOxResources(){


        //OXResourceInterface.delete(Context, Resource, null);
    }
    @Test public void testDeleteOxAccount(){


        //OXContextInterface.delete(Context, null); 
    } 
    @Test public void testGetUserAccessModules(){ 
        //OxUserInterface.getModuleAccess(Context, User, null); 
    }
    @Test public void testSetUserAccessModules(){
        //OxUserInterface.changeModuleAccess(Context, User, UserModuleAccess, null);
    }
    @Test public void testUpdateMaxCollapQuota(){ 
        //OXContextInterface.change(Context, null);
    }
    @Test public void testGetUser(){ 
        //OxUserInterface.getData(Context, User, null); //query by mailboxName (User.name) 
    }
    @Test public void testUpdateModuleAccess(){ 
        //OxUserInterface.changeModuleAccess(Context, User, UserModuleAccess , null); //query by mailboxName (User.name)
    }

    //      Exceptions 
    //      Folgende Exceptions behandeln wir explizit: 
    //      com.openexchange.admin.rmi.exceptions.ContextExistsException; 
    //      com.openexchange.admin.rmi.exceptions.NoSuchContextException; 
    //      com.openexchange.admin.rmi.exceptions.NoSuchGroupException; 
    //      com.openexchange.admin.rmi.exceptions.NoSuchResourceException; 
    //      com.openexchange.admin.rmi.exceptions.NoSuchUserException;
}
