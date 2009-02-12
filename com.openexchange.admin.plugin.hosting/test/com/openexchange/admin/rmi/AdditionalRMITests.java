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

import static org.junit.Assert.assertEquals;
import java.rmi.Naming;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;

/**
 * 
 * {@link AdditionalRMITests}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>

 *
 */
public class AdditionalRMITests extends AbstractTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AdditionalRMITests.class);
    }
    
    /**************** HELPERS ****************/
    
    public Integer getContextID(){
        return new Integer(1);
    }
    
    public Credentials getCredentials(){
        return new Credentials("oxadmin","secret");
    }
    
    public String getHostName(){
        return "localhost";
    }
    
    public static Credentials DummyMasterCredentials(){
        return new Credentials("oxadminmaster","secret");
    }
    
    protected static String getRMIHostUrl(){
        String host = "localhost";
        
        if(System.getProperty("host")!=null){
            host = System.getProperty("host");
        }        
        
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        return host;
    }
    

    
    /**************** TESTS ****************/
    
    /**
     * Looking up users by User#name
     */
    @Test public void testGetOxAccount() throws Exception{
        final Credentials credentials = DummyCredentials();
        Context context = getTestContextObject(credentials);
        
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup(getRMIHostUrl()+ OXUserInterface.RMI_NAME);
        //OXContextInterface contextInterface = (OXContextInterface) Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
        //contextInterface.create(context, admin_user, credentials);
        //context = contextInterface.getData(context, credentials); // query by contextId

        User knownUser = new User();
        knownUser.setName("thorben");
        User[] mailboxNames = new User[]{ knownUser}; //users with only their mailbox name (User#name) - the rest is going to be looked up
        User[] queriedUsers = userInterface.getData(context, mailboxNames, credentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf( queriedUsers.length ));
        User queriedUser = queriedUsers[0];
        assertEquals("Should have looked up first name", "Thorben Betten", queriedUser.getDisplay_name());
    }
 
    @Test public void testGetAllUsers(){  
        //OxUserInterface.listAll(Context, null); 
        //User[] users = OXUserInterface.getData(Context, User[] , null); // query by userIds
    }

    @Test public void testGetOxGroups(){
        //OXContextInterface.getData(Context, null); // query by contextId 
        //User[] users = OXUserInterface.getData(Context, User[] , null); // query by mailboxNames (User.name) 
        //OxGroupInterface.listAll(Context, null); 
    }

    @Test public void testGetOxResources(){
        //OxResourceInterface.listAll(Context, null); 
    }
    @Test public void testCreateFirstUser(){ 
        //context and admin user 
        //OXContextInterface.create(Context, User, null); 
        //OxUserInterface.changeModuleAccess(Context, User, UserModuleAccess, null); 

        //first user 
        //OxUserInterface.create(Context, User, UserModuleAccess, null);
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
