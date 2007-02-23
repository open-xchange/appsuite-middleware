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
package com.openexchange.admin;

import com.openexchange.admin.container.Context;
import com.openexchange.admin.container.Resource;
import com.openexchange.admin.dataSource.I_OXResource;

import java.rmi.Naming;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author cutmasta
 */
public class ResourceTest extends AbstractAdminTest{

    // list of chars that must be valid
    private static String VALID_CHAR_TESTRESOURCE = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    
    public void testAddResource() throws Exception {
        int id = addResource(getTestResourceObject(),getRMIHost());
        assertTrue("object id > 0 expected",id>0);
    }
    
    public void testSearchResource() throws Exception{
        long contextid = getContextID();
        addResource(getTestResourceObject(VALID_CHAR_TESTRESOURCE,contextid),getRMIHost());
        Resource[] testsearch = searchResources(contextid,VALID_CHAR_TESTRESOURCE,getRMIHost());
        assertEquals("invalid search result",1,testsearch.length);
    }
    
    public void testDeleteResource() throws Exception{
        long contextid = getContextID();
        
        int del_id = addResource(getTestResourceObject(VALID_CHAR_TESTRESOURCE,contextid),getRMIHost());
        Resource res = new Resource();
        res.setContextId(contextid);
        res.setId(Long.parseLong(""+del_id));
        deleteResource(res,getRMIHost());
        // now try to load the resource again, this MUST fail
        try{
            res = loadResource(res,getRMIHost());
            fail("resource not exists expected");
        }catch(Exception ecp){
            if(ecp.toString().toLowerCase().indexOf("resource does not exist")!=-1){
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }
    
    public void testLoadResource() throws Exception{
        Resource resi = getTestResourceObject();
        int load_id = addResource(resi,getRMIHost());
        assertTrue("expected id > 0",load_id>0);
        resi.setId((long)load_id);
        compareResources(resi,loadResource(resi,getRMIHost()));
    }
    
    public void testChangeOXResource() throws Exception {
        Resource resi = getTestResourceObject();
        int load_id = addResource(resi,getRMIHost());
        assertTrue("expected id > 0",load_id>0);
        resi.setId((long)load_id);
        compareResources(resi,loadResource(resi,getRMIHost()));
        
        // now change the resource
        createChangeResourceData(resi);
        
        changeResource(resi,getRMIHost());
        
        Resource remote = loadResource(resi,getRMIHost());
        
        compareResources(resi,remote);
        
    }
    
    public static void changeResource(Resource res,String host) throws Exception {
        I_OXResource xres = (I_OXResource)Naming.lookup(checkHost(host)+I_OXResource.RMI_NAME);
        Vector v = xres.changeOXResource((int)res.getContextId(),(int)res.getId(),res.xform2Data());
        parseResponse(v);
    }
    
    public static Resource loadResource(Resource res, String host) throws Exception{
        // try to load the resource from server
        I_OXResource xres = (I_OXResource)Naming.lookup(checkHost(host)+I_OXResource.RMI_NAME);
        Vector v = xres.getOXResourceData((int)res.getContextId(),(int)res.getId());
        parseResponse(v);
        Resource res1 = new Resource();
        res1.xform2Object((Hashtable)v.get(1));
        return res1;
    }
    
    public static void deleteResource(Resource res,String host) throws Exception{
        I_OXResource xres = (I_OXResource)Naming.lookup(checkHost(host)+I_OXResource.RMI_NAME);
        Vector v = xres.deleteOXResource((int)res.getContextId(),(int)res.getId());
        parseResponse(v);
    }
    
    public static int addResource(Resource res,String host) throws Exception{
        I_OXResource xres = (I_OXResource)Naming.lookup(checkHost(host)+I_OXResource.RMI_NAME);
        Hashtable<String, Object> ht = new Hashtable<String, Object>();
        ht.put(I_OXResource.AVAILABLE,res.isAvailable());
        ht.put(I_OXResource.RID,res.getIdentifier());
        ht.put(I_OXResource.DISPLAYNAME,res.getDisplayName());
        ht.put(I_OXResource.PRIMARY_MAIL,res.getEmail());
        ht.put(I_OXResource.DESCRIPTION,res.getDescription());
        Vector v= xres.createOXResource((int)res.getContextId(),ht);
        parseResponse(v);
        return Integer.parseInt(v.get(1).toString());
    }
    
    public static Resource[] searchResources(long contextId,String pattern,String host) throws Exception{
        I_OXResource xres = (I_OXResource)Naming.lookup(checkHost(host)+I_OXResource.RMI_NAME);
        Vector v = xres.listOXResources((int)contextId,pattern);
        parseResponse(v);
        Vector daten = (Vector)v.get(1);
        Resource[] ret_res = new Resource[daten.size()];
        for(int a = 0;a<daten.size();a++){
            Resource res = new Resource();
            res.xform2Object((Hashtable)daten.get(a));
            ret_res[a] = res;
        }
        return ret_res;
    }
    
    private Resource getTestResourceObject()throws Exception{
        return getTestResourceObject(VALID_CHAR_TESTRESOURCE,getContextID());        
    }
    
    private Resource getTestResourceObject(String identifier,long contextid) throws Exception{
        Resource rs = new Resource();
        rs.setAvailable(true);
        rs.setContextId(contextid);
        rs.setDescription(identifier+" description");
        rs.setEmail(identifier+"@"+AbstractAdminTest.TEST_DOMAIN);
        rs.setDisplayName(identifier+" display name");
        rs.setIdentifier(identifier);
        return rs;
    }
    
    private static String getRMIHost(){
        return "localhost";
    }
    
    private static long getContextID() throws Exception{
        Context ctx = ContextTest.getTestContextObject(ContextTest.createNewContextID(),10);
        long id = ContextTest.addContext(ctx,getRMIHost());
        return id;
    }  
    
    private void compareResources(Resource a,Resource b){
        assertEquals("context id not equal",a.getContextId(),b.getContextId());
        assertEquals("displayname id not equal",a.getDisplayName(),b.getDisplayName());
        assertEquals("id not equals",a.getId(),b.getId());
        assertEquals("identifier not equal",a.getIdentifier(),b.getIdentifier());
        assertEquals("description not equal",a.getDescription(),b.getDescription());
        assertEquals("mail not equal",a.getEmail(),b.getEmail());
    }

    private void createChangeResourceData(Resource resi) {
        if(resi.isAvailable()){
            resi.setAvailable(false);
        }else{
            resi.setAvailable(true);
        }
        resi.setDescription(resi.getDescription()+change_suffix);
        resi.setDisplayName(resi.getDisplayName()+change_suffix);
        resi.setEmail(resi.getEmail()+change_suffix);
        resi.setIdentifier(resi.getIdentifier()+change_suffix);
    }
    
}





