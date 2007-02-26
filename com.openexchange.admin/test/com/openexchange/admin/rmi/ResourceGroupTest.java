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

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.ResourceGroup;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 *
 * @author cutmasta
 */
public class ResourceGroupTest extends AbstractTest{
    
    private OXResourceGroupInterface getResourceGroupClient() throws NotBoundException, MalformedURLException, RemoteException{
        return (OXResourceGroupInterface)Naming.lookup(getRMIHostUrl()+OXResourceGroupInterface.RMI_NAME);
    }
    
    public static ResourceGroup getTestResourceGroupObject(String name){
        ResourceGroup resgrp = new ResourceGroup();
        resgrp.setDisplayname("displayname-resource-"+name+"-");
        resgrp.setEmail("resource-"+name+"@"+TEST_DOMAIN);
        resgrp.setName(name);
        return resgrp;
    }
    
    private static Context getTestContext() throws Exception{
        final Context ctxset = getTestContextObject(DummyCredentials());        
        
        // create new context
        Context ctx = new Context();
        ctx.setID(addContext(ctxset,getRMIHostUrl(),DummyCredentials()));
        return ctx;
    }
    
    public void testAddMember()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       // now add a resource 
       Resource client_resource = ResourceTest.getTestResourceObject("tescase-addmeber2resource-resource-"+System.currentTimeMillis());
       OXResourceInterface oxre = (OXResourceInterface)Naming.lookup(getRMIHostUrl()+OXResourceInterface.RMI_NAME);
       client_resource.setId(oxre.create(ctx,client_resource,DummyCredentials()));
       
       // now add member to resourcegroup
       oxres.addMember(ctx,client_grp.getId(),new Resource[]{client_resource},DummyCredentials());
       
       // check if member was added
       Resource[] server_members = oxres.getMembers(ctx,client_grp.getId(),DummyCredentials());
       if(server_members==null){
         fail("response from server was null");
       }
       assertTrue("Expected min. 1 resource group member",server_members.length>0);
       
       boolean found_member = false;       
       for(Resource element : server_members){
           if(element.getId()==client_resource.getId()){
               assertEquals(client_resource.getDescription(),element.getDescription());
               assertEquals(client_resource.getDisplayname(),element.getDisplayname());
               assertEquals(client_resource.getEmail(),element.getEmail());
               found_member = true;
           }
       }
       assertTrue("Expected to find member with correct data",found_member);
    }
    
    public void testRemoveMember()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       // now add a resource 
       Resource client_resource = ResourceTest.getTestResourceObject("tescase-addmeber2resource-resource-"+System.currentTimeMillis());
       OXResourceInterface oxre = (OXResourceInterface)Naming.lookup(getRMIHostUrl()+OXResourceInterface.RMI_NAME);
       client_resource.setId(oxre.create(ctx,client_resource,DummyCredentials()));
       
       // now add member to resourcegroup
       oxres.addMember(ctx,client_grp.getId(),new Resource[]{client_resource},DummyCredentials());
       
       // check if member was added
       Resource[] server_members = oxres.getMembers(ctx,client_grp.getId(),DummyCredentials());
       if(server_members==null){
         fail("response from server was null");
       }
       
       assertTrue("Expected min. 1 resource group member",server_members.length>0);
       
       boolean found_member = false;       
       for(Resource element : server_members){
           if(element.getId()==client_resource.getId()){
               assertEquals(client_resource.getDescription(),element.getDescription());
               assertEquals(client_resource.getDisplayname(),element.getDisplayname());
               assertEquals(client_resource.getEmail(),element.getEmail());
               found_member = true;
           }
       }
       assertTrue("Expected to find member with correct data",found_member);
       
       // now remove member from resourcegroup
       oxres.removeMember(ctx,client_grp.getId(),new Resource[]{client_resource},DummyCredentials());
       
       // now try to load members again, now the member SHOULD NOT appear
       server_members = oxres.getMembers(ctx,client_grp.getId(),DummyCredentials());
       found_member = false;       
       for(Resource element : server_members){
           if(client_resource.getId()==element.getId()){
               found_member = true;
           }
       }
       
       assertFalse("Expected to not find the resource as a member after removing",found_member);
       
    }
    
    public void testCreate()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       ResourceGroup[] srv_list = oxres.list(ctx,"*",DummyCredentials());
       if(srv_list==null){
         fail("response from server was null");
       }
       boolean found_grp = false;
       for(ResourceGroup element: srv_list){
           if(client_grp.getId()==element.getId()){
               assertEquals(client_grp.getDisplayname(),element.getDisplayname());
               assertEquals(client_grp.getEmail(),element.getEmail());
               assertEquals(client_grp.getName(),element.getName());
               found_grp = true;
           }
       }
       
       assertEquals("Expected to to find created resource group",found_grp);
       
    }
    
    
    public void testDelete()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       // load group
       ResourceGroup[] srv_list = oxres.list(ctx,"*",DummyCredentials());
       if(srv_list==null){
         fail("response from server was null");
       }
       boolean found_grp = false;
       for(ResourceGroup element: srv_list){
           if(client_grp.getId()==element.getId()){
               assertEquals(client_grp.getDisplayname(),element.getDisplayname());
               assertEquals(client_grp.getEmail(),element.getEmail());
               assertEquals(client_grp.getName(),element.getName());
               found_grp = true;
           }
       }
       
       assertEquals("Expected to to find created resource group",found_grp);
       
       // delete resourcegroup
       oxres.delete(ctx,new int[]{client_grp.getId()},DummyCredentials());
       
       // now load all resource group and check if our deleted resource group is really deleted
       srv_list = oxres.list(ctx,"*",DummyCredentials());
       found_grp = false;
       for(ResourceGroup element: srv_list){
           if(client_grp.getId()==element.getId()){               
               found_grp = true;
           }
       }
       assertFalse("Expected to not find the resource group after delete",found_grp);
       
    }
    
    public void testList()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       // list groups
       ResourceGroup[] srv_list = oxres.list(ctx,"*",DummyCredentials());
       if(srv_list==null){
           fail("response from server was null");
       }
       
       assertTrue("expected list size > 0",srv_list.length>0);
       
    }
    public void testChange ()throws Exception{
       Context ctx = getTestContext();
       OXResourceGroupInterface oxres = getResourceGroupClient();
       
       // add resource group first
       ResourceGroup client_grp = getTestResourceGroupObject("addmember-testcase-"+System.currentTimeMillis());       
       client_grp.setId(oxres.create(ctx,client_grp,DummyCredentials()));
       
       // list groups
       ResourceGroup[] srv_list = oxres.list(ctx,"*",DummyCredentials());
       if(srv_list==null){
           fail("response from server was null");
       }
       
       // get group from server     
       boolean found_grp = false;
       for(ResourceGroup grp:srv_list){
           if(grp.getId()==client_grp.getId()){
               assertEquals(client_grp.getDisplayname(),grp.getDisplayname());
               assertEquals(client_grp.getEmail(),grp.getEmail());
               assertEquals(client_grp.getName(),grp.getName());
               found_grp = true;
           }
       }
       
       assertTrue("Expected to find resouce group with correct data",found_grp);
       
       
       // change group locally
       client_grp.setDisplayname(client_grp.getDisplayname()+change_suffix);
       client_grp.setEmail(client_grp.getEmail()+change_suffix);
       client_grp.setName(client_grp.getName()+change_suffix);
       
       // now submit changes to server
       oxres.change(ctx,client_grp,DummyCredentials());
       
       // load resources from server and verify changed data
       srv_list = oxres.list(ctx,"*",DummyCredentials());
       if(srv_list==null){
           fail("response from server was null");
       }
       for(ResourceGroup grp:srv_list){
           if(grp.getId()==client_grp.getId()){
               assertEquals(client_grp.getDisplayname(),grp.getDisplayname());
               assertEquals(client_grp.getEmail(),grp.getEmail());
               assertEquals(client_grp.getName(),grp.getName());               
           }
       }
    }
}
