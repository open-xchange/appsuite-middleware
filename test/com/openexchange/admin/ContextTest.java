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
import com.openexchange.admin.container.ContextSetup;
import com.openexchange.admin.container.Database;
import com.openexchange.admin.container.Filestore;
import com.openexchange.admin.container.MaintenanceReason;
import com.openexchange.admin.container.Server;
import com.openexchange.admin.dataSource.I_OXContext;

import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author cutmasta
 */
public class ContextTest extends AbstractAdminTest {
    
    public void testChangeStorageData() throws Exception {
        long ctsid = createNewContextID();
        Context ctx = getTestContextObject(ctsid,50);
        long id = addContext(ctx,getRMIHost());
        ctx.setContextId(id);
        ctx.setFilestorePassword("changedpasswd");
        ctx.setFilestoreUsername("changedusername");
        ctx.setFilestoreQuotaMax(1337);
        ctx.setFilestoreId(111111337);
        changeStorageData(ctx,getRMIHost());        
    }
    
    public void testGetContextSetup() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
//        Context loaded_ctx = ctx_setup.getContext();
//        
//       
//        // now change some context values like enabled/disabled, quota max, filestore username,filestore passwd
//        long changed_quota_max = loaded_ctx.getFilestoreQuotaMax()+10;
//        loaded_ctx.setFilestoreQuotaMax(changed_quota_max);
//        loaded_ctx.setFilestoreUsername(""+loaded_ctx.getFilestoreUsername()+"_changed");
//        
//        changeStorageData(loaded_ctx,getRMIHost());
//        ContextSetup ctx_setup_loaded = getContextSetup(loaded_ctx,getRMIHost());
//        
//        //compareContextSetup();
//        log(ctx_setup_loaded.getContext().getFilestoreUsername()+" "+ctx_setup.getContext().getFilestoreUsername());
////        log("->"+loaded_ctx.getFilestoreQuotaMax()+" ->"+ctx.getFilestoreQuotaMax());
//        fail("not completely implemented in testsuite");
    }
    
    public void testSearchContextByDatabase() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
        Database[] dbs = UtilTest.searchForDatabase("*",getRMIHost());
        if(dbs.length>0){
            long[] ids = searchContextByDatabase(dbs[0],getRMIHost());
            assertTrue("no contexts found on database "+dbs[0].getDatabaseUrl(),ids.length>0);
        }else{
            fail("no databases found to search with");
        }
    }
    
    public void testSearchContextByFilestore() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
        Filestore [] fiss = UtilTest.searchFilestore("*",getRMIHost());
        if(fiss.length>0){
            boolean foundctxviastore = false;
            for(int a = 0;a<fiss.length;a++){
                long[] ids = searchContextByFilestore(fiss[a],getRMIHost());
                if(ids.length>0){
                    foundctxviastore = true;
                }
            }
            assertTrue("no contexts found on filestores",foundctxviastore);
        }else{
            fail("no databases found to search with");
        }
    }
    
    public void testDisableContext() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
        long[] mr_ids = UtilTest.getAllMaintenanceReasons(getRMIHost());
        MaintenanceReason mr = new MaintenanceReason();
        if(mr_ids.length==0){
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled "+System.currentTimeMillis());
            long mr_id = UtilTest.addMaintenanceReason(mr,getRMIHost());
            mr.setId(mr_id);
        }else{
            mr.setId(mr_ids[0]);
        }
        disableContext(ctx,mr,getRMIHost());
        Context [] ctxs = searchContext(""+ctx.getContextId(),getRMIHost());
        boolean ctx_disabled = false;
        for(int a = 0;a<ctxs.length;a++){
            Context ct = ctxs[a];
            if(ct.isContextLocked() && ct.getContextLockId()==mr.getId()){
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled",ctx_disabled);
        
    }
    
    public void testEnableContext() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
        long[] mr_ids = UtilTest.getAllMaintenanceReasons(getRMIHost());
        MaintenanceReason mr = new MaintenanceReason();
        if(mr_ids.length==0){
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled "+System.currentTimeMillis());
            long mr_id = UtilTest.addMaintenanceReason(mr,getRMIHost());
            mr.setId(mr_id);
        }else{
            mr.setId(mr_ids[0]);
        }
        
        disableContext(ctx,mr,getRMIHost());
        Context [] ctxs = searchContext(""+ctx.getContextId(),getRMIHost());
        boolean ctx_disabled = false;
        for(int a = 0;a<ctxs.length;a++){
            Context ct = ctxs[a];
            if(ct.getContextId()==ctx.getContextId() && ct.isContextLocked() && ct.getContextLockId()==mr.getId()){
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled",ctx_disabled);
        
        // now enable context again
        enableContext(ctx,getRMIHost());
        ctxs = searchContext(""+ctx.getContextId(),getRMIHost());
        boolean ctx_ensabled = false;
        for(int a = 0;a<ctxs.length;a++){
            Context ct = ctxs[a];
            if(ct.getContextId()==ctx.getContextId() && !ct.isContextLocked()){
                ctx_ensabled = true;
            }
        }
        assertTrue("context could be not enabled",ctx_ensabled);
    }
    
    public void testAddContext() throws Exception {
        Context ctx = getTestContextObject();
        addContext(ctx,getRMIHost());
    }
    
    public void testDeleteContext() throws Exception {
        long ctxid = createNewContextID();
        Context ctx = getTestContextObject(ctxid,50);
        ctxid = addContext(ctx,getRMIHost());
        deleteContext(ctx,getRMIHost());
    }
    
    public void testSearchContext() throws Exception {
        long ctxid = createNewContextID();
        Context ctx = getTestContextObject(ctxid,50);
        addContext(ctx,getRMIHost());
        // now search exactly for the added context
        Context [] ctxs = searchContext(""+ctxid,getRMIHost());
        boolean foundctx = false;
        for(int a = 0;a<ctxs.length;a++){
            Context cc = ctxs[a];            
            if(cc.getContextId()==ctxid){
                foundctx = true;
            }
        }
        assertTrue("context not found",foundctx);
    }
    
    public static void changeStorageData(Context ctx,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.changeStorageData((int)ctx.getContextId(),ctx.xform2Data());
        parseResponse(v);
    }
    
    public static long[] searchContextByDatabase(Database db,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.searchContextByDatabase(db.getDatabaseUrl());
        parseResponse(v);
        Vector tmpids = (Vector)v.get(1);
        long[] ids = new long[tmpids.size()];
        for(int a = 0;a<tmpids.size();a++){
            ids[a] = Long.parseLong(tmpids.get(a).toString());
        }
        return ids;
    }
    
    public static long[] searchContextByFilestore(Filestore fis,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.searchContextByFilestore(fis.getFilestoreUrl());
        parseResponse(v);
        Vector tmpids = (Vector)v.get(1);
        long[] ids = new long[tmpids.size()];
        for(int a = 0;a<tmpids.size();a++){
            ids[a] = Long.parseLong(tmpids.get(a).toString());
        }
        return ids;
    }
    
    public static ContextSetup getContextSetup(Context ctx,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.getContextSetup((int)ctx.getContextId());
        parseResponse(v);
        Hashtable ht = (Hashtable)v.get(1);
        ContextSetup ctxsetup = new ContextSetup();
        Context ct = new Context();
        ct.setContextId(Long.parseLong(""+ht.get(I_OXContext.CONTEXT_ID)));
        ct.setContextName(""+ht.get(I_OXContext.CONTEXT_NAME));
        
        // filestore handle
        ct.setFilestoreQuotaMax(Long.parseLong(""+ht.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX)));
        if(ht.containsKey(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED)){
            ct.setFilestoreQuotaUsed(Long.parseLong(""+ht.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED)));
        }
        ct.setFilestoreName(""+ht.get(I_OXContext.CONTEXT_FILESTORE_NAME));
        if(ht.containsKey(I_OXContext.CONTEXT_FILESTORE_USERNAME)){
            ct.setFilestoreUsername(""+ht.get(I_OXContext.CONTEXT_FILESTORE_USERNAME));
        }
        if(ht.containsKey(I_OXContext.CONTEXT_FILESTORE_PASSWORD)){
            ct.setFilestorePassword(""+ht.get(I_OXContext.CONTEXT_FILESTORE_PASSWORD));
        }        
        ct.setFilestoreId(Long.parseLong(""+ht.get(I_OXContext.CONTEXT_FILESTORE_ID)));
        
        // context locked?
        ct.setContextLocked(Boolean.parseBoolean(""+ht.get(I_OXContext.CONTEXT_LOCKED)));
        if(ht.containsKey(I_OXContext.CONTEXT_LOCKED_TXT_ID)){
            ct.setContextLockId(Long.parseLong(""+ht.get(I_OXContext.CONTEXT_LOCKED_TXT_ID)));
        }
        
        // database handle
        Hashtable dbhandle = (Hashtable)ht.get(I_OXContext.CONTEXT_DATABASE_HANDLE);
        ct.setContextDBReadPoolId(Long.parseLong(""+dbhandle.get(I_OXContext.CONTEXT_READ_POOL_ID)));
        ct.setContextDBWritePoolId(Long.parseLong(""+dbhandle.get(I_OXContext.CONTEXT_WRITE_POOL_ID)));
        ct.setContextDBSchemaName(""+dbhandle.get(I_OXContext.CONTEXT_DB_SCHEMA_NAME));
        
        ctxsetup.setContext(ct);
        
        return ctxsetup;
    }
    
    public static Context [] searchContext(String pattern,String host) throws Exception{
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.searchContext(pattern);
        parseResponse(v);
        Hashtable ctxdata = (Hashtable)v.get(1);
        Context [] ret = new Context[ctxdata.size()];
        Enumeration enumi = ctxdata.keys();
        int ids = 0;
        while(enumi.hasMoreElements()){
            String id = (String)enumi.nextElement();
            Hashtable data = (Hashtable)ctxdata.get(id);
            Context ct = new Context();
            ct.setContextId(Long.parseLong(id));
            if(data.containsKey(I_OXContext.CONTEXT_LOCKED_TXT_ID)){
                ct.setContextLockId(Long.parseLong(data.get(I_OXContext.CONTEXT_LOCKED_TXT_ID).toString()));
            }
            if(data.containsKey(I_OXContext.CONTEXT_LOCKED)){
                ct.setContextLocked(Boolean.parseBoolean(data.get(I_OXContext.CONTEXT_LOCKED).toString()));
            }
            if(data.containsKey(I_OXContext.CONTEXT_NAME)){
                ct.setContextName(data.get(I_OXContext.CONTEXT_NAME).toString());
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_ID)){
                ct.setFilestoreId(Long.parseLong(data.get(I_OXContext.CONTEXT_FILESTORE_ID).toString()));
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_NAME)){
                ct.setFilestoreName(data.get(I_OXContext.CONTEXT_FILESTORE_NAME).toString());
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_PASSWORD)){
                ct.setFilestorePassword(data.get(I_OXContext.CONTEXT_FILESTORE_PASSWORD).toString());
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_USERNAME)){
                ct.setFilestoreUsername(data.get(I_OXContext.CONTEXT_FILESTORE_USERNAME).toString());
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX)){
                ct.setFilestoreQuotaMax(Long.parseLong(data.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX).toString()));
            }
            if(data.containsKey(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED)){
                ct.setFilestoreQuotaUsed(Long.parseLong(data.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED).toString()));
            }
            ret[ids] = ct;
            ids++;
        }
        
        return ret;
    }
    
    public static void deleteContext(Context ctx,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.deleteContext((int)ctx.getContextId());
        parseResponse(v);
    }
    
    public static long addContext(Context ctx,String host) throws Exception {
        // first check if the needed server entry is in db, if not, add server first,
        if(UtilTest.searchForServer("local",getRMIHost()).length!=1){
            Server srv = new Server();
            srv.setName("local");
            UtilTest.registerServer(srv,getRMIHost());
        }
        // then check if filestore is in db, if not, create register filestore first
        if(UtilTest.searchFilestore("*",getRMIHost()).length==0){
            Filestore fis = new Filestore();
            fis.setFilestoreMaxContext(10000);
            fis.setFilestoreSize(8796093022208L);
            java.net.URI uri = new java.net.URI("file:///tmp/disc_"+System.currentTimeMillis());
            fis.setFilestoreUrl(uri.toString());
            new java.io.File(uri.getPath()).mkdir();
            UtilTest.registerFilestore(fis,getRMIHost());
        }
        // then check if a database is in db for the new ctx, if not register database first,
        // THEN we can add the contex with its data
        if(UtilTest.searchForDatabase("test-ox-db",getRMIHost()).length==0){
            Database db = UtilTest.getTestDatabaseObject("localhost","test-ox-db");
            UtilTest.registerDatabase(db,getRMIHost());
        }
        
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.createContext((int)ctx.getContextId(),ctx.getFilestoreQuotaMax(),ctx.xForm2AdminUserData());
        parseResponse(v);
        return ctx.getContextId();
    }
    
    public static void disableContext(Context ctx,MaintenanceReason mr,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.disableContext((int)ctx.getContextId(),(int)mr.getId());
        parseResponse(v);
    }
    
    public static void enableContext(Context ctx,String host) throws Exception {
        I_OXContext xres = (I_OXContext)Naming.lookup(checkHost(host)+I_OXContext.RMI_NAME);
        Vector v = xres.enableContext((int)ctx.getContextId());
        parseResponse(v);
    }
    
    public static Context getTestContextObject()throws Exception{
        return getTestContextObject(createNewContextID(),50);
    }
    
    public static Context getTestContextObject(long context_id,long quota_max_in_mb){
        Context ctx = new Context(UserTest.getTestUserObject("adminuser_context_"+context_id,"netline",context_id));
        ctx.setContextId(context_id);
        ctx.setFilestoreQuotaMax(quota_max_in_mb);
        return ctx;
    }
    private static String getRMIHost(){
        return "localhost";
    }
    
    private static int searchNextFreeContextID(int pos) throws Exception{
        Context [] ctx = searchContext(""+pos,getRMIHost());
        if(ctx.length==0){
            return pos;
        }else{
            return -1;
        }
    }
    
    public static int createNewContextID() throws Exception{
        int pos = 5;
        int ret = -1;
        while(ret==-1){
            ret = searchNextFreeContextID(pos);
            pos = pos+3;
        }
        return ret;
    }
    
}
