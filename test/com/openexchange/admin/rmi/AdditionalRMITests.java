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
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

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
    
    private Context addSystemContext(Context ctx, String host, Credentials cred) throws Exception {
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(host + OXUtilInterface.RMI_NAME);
        // first check if the needed server entry is in db, if not, add server
        // first,
        if (oxu.listServer("local", cred).length != 1) {
            Server srv = new Server();
            srv.setName("local");
            oxu.registerServer(srv, cred);
        }
        // then check if filestore is in db, if not, create register filestore
        // first
        if (oxu.listFilestore("*", cred).length == 0) {
            Filestore fis = new Filestore();
            fis.setMaxContexts(10000);
            fis.setSize(8796093022208L);
            java.net.URI uri = new java.net.URI("file:///tmp/disc_" + System.currentTimeMillis());
            fis.setUrl(uri.toString());
            new java.io.File(uri.getPath()).mkdir();
            oxu.registerFilestore(fis, cred);
        }
        // then check if a database is in db for the new ctx, if not register
        // database first,
        // THEN we can add the context with its data
        if (oxu.listDatabase("test-ox-db", cred).length == 0) {
            Database db = UtilTest.getTestDatabaseObject("localhost", "test-ox-db");
            oxu.registerDatabase(db, cred);
        }
        
        OXContextInterface oxcontext = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        
        oxcontext.create(ctx,UserTest.getTestUserObject("admin","secret"), cred);
        return ctx;
    }
    
    public static Context getTestContextObject(Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        return getTestContextObject(createNewContextID(cred), 5000);
    }
    

    public static int createNewContextID(Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        int pos = 5;
        int ret = -1;
        while (ret == -1) {
            ret = searchNextFreeContextID(pos, cred);
            pos = pos + 3;
        }
        return ret;
    }

    public static Context[] searchContext(String pattern, String host, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);        
        return xres.list(pattern, cred);
    }
    
    public static int searchNextFreeContextID(int pos, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        Context[] ctx = searchContext(String.valueOf(pos), getRMIHostUrl(), cred);
        if (ctx.length == 0) {
            return pos;
        } else {
            return -1;
        }
    }
    
    /**************** TESTS ****************/
    
    /**
     * Looking up users by User#name
     */
    @Test public void testGetOxAccount() throws Exception{
        final Credentials credentials = DummyMasterCredentials();
        final String hosturl = getRMIHostUrl();
        Context context = addSystemContext(getTestContextObject(credentials), hosturl, credentials);
        OXContextInterface contextInterface = (OXContextInterface) Naming.lookup(getHostName() + OXContextInterface.RMI_NAME);

        context = contextInterface.getData(context, credentials); // query by contextId
        OXUserInterface userInterface = (OXUserInterface) Naming.lookup("localhost" + OXUserInterface.RMI_NAME);
        
        User knownUser = new User();
        knownUser.setName("thorben");
        User[] mailboxNames = new User[]{ knownUser}; //users with only their mailbox name (User#name) - the rest is going to be looked up
        User[] queriedUsers = userInterface.getData(context, mailboxNames, credentials); // query by mailboxNames (User.name)

        assertEquals("Query should return only one user", new Integer(1), Integer.valueOf( queriedUsers.length ));
        User queriedUser = queriedUsers[0];
        assertEquals("Should have looked up first name", "Thorben2", queriedUser.getGiven_name());
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
