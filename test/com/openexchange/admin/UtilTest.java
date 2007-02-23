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

import com.openexchange.admin.container.Database;
import com.openexchange.admin.container.Filestore;
import com.openexchange.admin.container.MaintenanceReason;
import com.openexchange.admin.container.Server;
import com.openexchange.admin.dataSource.I_OXUtil;

import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author cutmasta
 */
public class UtilTest extends AbstractAdminTest {
        
    public void testAddMaintenanceReason() throws Exception { 
        MaintenanceReason mr = new MaintenanceReason();
        mr.setText("testcase text-"+System.currentTimeMillis());
        long id = addMaintenanceReason(mr,getRMIHost());
        assertTrue("object id > 0 expected",id>0);
    }
    
    public void testDeleteMaintenanceReason() throws Exception { 
        MaintenanceReason mr = new MaintenanceReason();
        mr.setText("testcase text-"+System.currentTimeMillis());
        long id = addMaintenanceReason(mr,getRMIHost());
        mr.setId(id);
        deleteMaintenanceReason(mr,getRMIHost());
        try{
            getMaintenanceReason(mr,getRMIHost());
            fail("maintenancereason not exists expected");
        }catch(Exception ecp){
            if(ecp.toString().toLowerCase().indexOf("does not exist")!=-1){
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }else{
                fail("error occured");
            }
        }
       
    }
    
    public void testGetAllMaintenanceReasons() throws Exception {
        MaintenanceReason mr = new MaintenanceReason();
        mr.setText("testcase text-"+System.currentTimeMillis());
        addMaintenanceReason(mr,getRMIHost());
        long[] ids = getAllMaintenanceReasons(getRMIHost());
        assertTrue("object id > 0 expected",ids.length>0);
    }
    
    public void testGetMaintenanceReason() throws Exception {
        String ident = "testcase text-"+System.currentTimeMillis();
        MaintenanceReason mr = new MaintenanceReason();
        mr.setText(ident);
        long id = addMaintenanceReason(mr,getRMIHost());
        mr.setId(id);
        try{
            mr =  getMaintenanceReason(mr,getRMIHost());
            assertEquals("expected reason: "+ident ,ident,mr.getText());
        }catch(Exception ecp){
            fail("expected to get maintenance reason: "+ident);
        }
    }
    
    public void testRegisterDatabase() throws Exception {        
        Database db = getTestDatabaseObject();
        long id = registerDatabase(getTestDatabaseObject(),getRMIHost());
        db.setDatabaseId(id);
        unregisterDatabase(db,getRMIHost());
        assertTrue("database id > 0 expected",id>0);
    }
    
    public void testUnregisterDatabase() throws Exception {        
        Database db = getTestDatabaseObject();
        long id = registerDatabase(db,getRMIHost());
        db.setDatabaseId(id);
        unregisterDatabase(db,getRMIHost());
        Database [] dbs = searchForDatabase("*",getRMIHost());
        boolean foundserver = false;
        for(int a = 0;a<dbs.length;a++){
            Database ss = dbs [a];
            if(ss.getDatabaseId()==id){
                foundserver = true;
            }
        }
        assertFalse("database found",foundserver);
    }
    
    public void testSearchForDatabase() throws Exception {
        String idhost = "dbtesthost-"+System.currentTimeMillis();
        String name = "mytesthost display name"+System.currentTimeMillis();
        Database db = getTestDatabaseObject(idhost,name);
        long id = registerDatabase(db,getRMIHost());
        Database[] dbs = searchForDatabase(name,getRMIHost());
        db.setDatabaseId(id);
        unregisterDatabase(db,getRMIHost());
        assertTrue("1 search result expected but found "+dbs.length,dbs.length==1);
    }
    
    public void testRegisterFilestore() throws Exception {
        long id = registerFilestore(getTestFilestoreObject(),getRMIHost());
        assertTrue("filestore id > 0 expected",id>0);
    }
    
    public void testUnregisterFilestore() throws Exception {
        Filestore fis = getTestFilestoreObject();
        long id = registerFilestore(fis,getRMIHost());
        fis.setFilestoreId(id);
        new java.io.File(new java.net.URI(fis.getFilestoreUrl())).delete();
        unregisterFilestore(fis,getRMIHost());
        Filestore[] fiss = searchFilestore("*",getRMIHost());
        boolean foundfilestore = false;
        for(int a = 0;a<fiss.length;a++){
            Filestore fstore = fiss[a];
            if(fstore.getFilestoreId()==id){
                foundfilestore = true;
            }
        }
        assertFalse("filestore found but expected not do find it",foundfilestore);
    }
    
    public void testListFilestore() throws Exception {        
        String url = "file:///tmp/disc_"+System.currentTimeMillis();
        Filestore fis = getTestFilestoreObject(url);
        registerFilestore(fis,getRMIHost());
        Filestore[] fiss = searchFilestore(url,getRMIHost());
        assertTrue("1 search result expected but found "+fiss.length,fiss.length==1);
       
    }
    
    public void testChangeFilestore() throws Exception {
        String url = "file:///tmp/disc_"+System.currentTimeMillis();
        new java.io.File(new java.net.URI(url)).mkdir();
        Filestore fis = getTestFilestoreObject(url);
        long id = registerFilestore(fis,getRMIHost());
        fis.setFilestoreId(id);
        Filestore [] fiss = searchFilestore(url,getRMIHost());
        if(fiss.length==1){
            Filestore remote_loaded = fiss[0];
            compareFilestore(fis,remote_loaded);
            // ok, data is correct on server, 
            // change the filestore and compare again
            createFilestoreChangeData(fis);
            changeFilestore(fis,getRMIHost());
            Filestore [] fis2 = searchFilestore(fis.getFilestoreUrl(),getRMIHost());
            if(fis2.length==1){
                compareFilestore(fis,fis2[0]);
            }else{
                fail("could not load filestore from server to compare");
            }
        }else{
            fail("could not load filestore from server to change");
        }        
    }
    
    public void testChangeDatabase() throws Exception {
    	String dbname = "db"+System.currentTimeMillis();
    	Database local = getTestDatabaseObject("localhost", dbname);
    	long id = registerDatabase(local, getRMIHost());
    	local.setDatabaseId(id);
    	Database [] diss = searchForDatabase(dbname, getRMIHost());
    	if( diss.length==1) {
    		Database reloaded = diss[0];
    		compareDatabase(local,reloaded);
    		createDatabaseChangeData(local);
    		changeDatabase(local, getRMIHost());
    		Database [] dis2 = searchForDatabase(local.getDatabaseDisplayName(), getRMIHost());
    		if(dis2.length==1) {
    			compareDatabase(local, dis2[0]);
    		} else {
    			fail("could not load database from server to change");
    		}
    	} else {
    		fail("could not load database from server to change");
    	}
    }

    public void testRegisterServer() throws Exception {        
        long srv_id = registerServer(getTestServerObject(),getRMIHost());
        assertTrue("server id > 0 expected",srv_id>0);
    }
    
    public void testUnregisterServer() throws Exception {
        Server src = getTestServerObject();
        long srv_id = registerServer(src,getRMIHost());
        src.setServerId(srv_id);
        unregisterServer(src,getRMIHost());
        Server[] srvs = searchForServer("*",getRMIHost());
        boolean foundserver = false;
        for(int a = 0;a<srvs.length;a++){
            Server ss = srvs [a];
            if(ss.getServerId()==srv_id){
                foundserver = true;
            }
        }
        assertFalse("server found",foundserver);
    }
    
    public void testSearchForServer() throws Exception {
        String sid = "server_"+System.currentTimeMillis();
        Server src = getTestServerObject(sid);
        registerServer(src,getRMIHost());
        Server[] srvs = searchForServer(sid,getRMIHost());
        boolean foundserver = false;
        for(int a = 0;a<srvs.length;a++){
            Server ss = srvs [a];
            if(ss.getName().equals(sid)){
                foundserver = true;
            }
        }
        assertTrue("server not found",foundserver);
    }
    
    public static long[] getAllMaintenanceReasons(String host) throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.getAllMaintenanceReasons();
        parseResponse(v);
        v = (Vector)v.get(1);
        long [] ids = new long[v.size()];
        for(int a = 0;a<v.size();a++){
            ids[a] = Long.parseLong(v.get(a).toString());
        }
        return ids;
    }
    
    public static MaintenanceReason getMaintenanceReason(MaintenanceReason mr,String host) throws Exception {
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.getMaintenanceReason((int)mr.getId());
        parseResponse(v);        
        mr.setText((String)v.get(1));
        return mr;
    }
    
    public static void deleteMaintenanceReason(MaintenanceReason mr, String host) throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.deleteMaintenanceReason((int)mr.getId());
        parseResponse(v);
    }
    
    public static long addMaintenanceReason(MaintenanceReason mr,String host) throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.addMaintenanceReason(mr.getText());
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }
    
    public static long registerDatabase(Database db,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.registerDatabase(db.xform2Data(),db.isMaster(),0);
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }
    
    public static long changeDatabase(Database db,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.changeDatabase((int)db.getDatabaseId(),db.xform2Data());
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }

    public static void unregisterDatabase(Database db,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.unregisterDatabase((int)db.getDatabaseId());
        parseResponse(v);
    }
    
    public static Database[] searchForDatabase(String pattern,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v  = oxu.searchForDatabase(pattern);
        parseResponse(v);
        Hashtable daten = (Hashtable)v.get(1);
        Database [] dbs = new Database[daten.size()];
        Enumeration enumi = daten.keys();
        int ids = 0;
        while(enumi.hasMoreElements()){
            String id = (String)enumi.nextElement();
            Hashtable data = (Hashtable)daten.get(id);
            Database db = new Database();
            db.setDatabaseAuthenticationId(data.get(I_OXUtil.DB_AUTHENTICATION_ID).toString());
            db.setDatabaseAuthenticationPassword(data.get(I_OXUtil.DB_AUTHENTICATION_PASSWORD).toString());
            db.setDatabaseClusterWeight(Integer.parseInt(data.get(I_OXUtil.DB_CLUSTER_WEIGHT).toString()));
            db.setDatabaseDisplayName(data.get(I_OXUtil.DB_DISPLAY_NAME).toString());
            db.setDatabaseDriver(data.get(I_OXUtil.DB_DRIVER).toString());
            db.setDatabaseId(Long.parseLong(id));
            db.setDatabaseMaxUnits(Integer.parseInt(data.get(I_OXUtil.DB_MAX_UNITS).toString()));
            db.setDatabasePoolHardlimit(Integer.parseInt(data.get(I_OXUtil.DB_POOL_HARDLIMIT).toString()));
            db.setDatabasePoolInitial(Integer.parseInt(data.get(I_OXUtil.DB_POOL_INIT).toString()));
            db.setDatabasePoolMax(Integer.parseInt(data.get(I_OXUtil.DB_POOL_MAX).toString()));
            db.setDatabaseUrl(data.get(I_OXUtil.DB_URL).toString());
            dbs[ids] = db;
            ids++;
        }
        return dbs;
    }
    
    
    public static long registerFilestore(Filestore fis,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.registerFilestore(fis.getFilestoreUrl(),fis.getFilestoreSize(),(int)fis.getFilestoreMaxContext());
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }
    
    public static void unregisterFilestore(Filestore fis,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.unregisterFilestore((int)fis.getFilestoreId());
        parseResponse(v);
    }
    
    public static void changeFilestore(Filestore fis, String host) throws Exception {
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);        
        Hashtable<String, Object> ht = new Hashtable<String, Object>();
        ht.put(I_OXUtil.STORE_URL,fis.getFilestoreUrl());
        ht.put(I_OXUtil.STORE_MAX_CONTEXT,(int)fis.getFilestoreMaxContext());
        ht.put(I_OXUtil.STORE_SIZE,fis.getFilestoreSize());
        Vector v = oxu.changeFilestore((int)fis.getFilestoreId(),ht);
        parseResponse(v);
    }
    
    public static Filestore[] searchFilestore(String pattern,String host)throws Exception{
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.listFilestores(pattern);
        parseResponse(v);
        Hashtable daten = (Hashtable)v.get(1);
        Filestore[] fiss = new Filestore[daten.size()];
        Enumeration enumi = daten.keys();
        int ids = 0;
        while(enumi.hasMoreElements()){
            String id = (String)enumi.nextElement();
            Hashtable data = (Hashtable)daten.get(id);
            Filestore fis = new Filestore();
            fis.setFilestoreId(Long.parseLong(id));
            fis.setFilestoreMaxContext(Long.parseLong(data.get(I_OXUtil.STORE_MAX_CONTEXT).toString()));
            fis.setFilestoreSize(Long.parseLong(data.get(I_OXUtil.STORE_SIZE).toString()));
            fis.setFilestoreUrl(data.get(I_OXUtil.STORE_URL).toString());            
            fiss[ids] = fis;
            ids++;
        }
        return fiss;
    }
    
    public static void unregisterServer(Server srv,String host) throws Exception {
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.unregisterServer((int)srv.getServerId());
        parseResponse(v);
    }
    
    public static long registerServer(Server srv,String host) throws Exception {
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.registerServer(srv.getName());
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }
    
    public static Server[] searchForServer(String pattern,String host) throws Exception {
        I_OXUtil oxu = (I_OXUtil)Naming.lookup(checkHost(host)+I_OXUtil.RMI_NAME);
        Vector v = oxu.searchForServer(pattern);
        parseResponse(v);
        Hashtable data = (Hashtable)v.get(1);
        Server[] servers = new Server[data.size()];
        Enumeration enumi = data.keys();
        int ids = 0;
        while(enumi.hasMoreElements()){
            Integer id = (Integer)enumi.nextElement();
            Hashtable tmp = (Hashtable)data.get(id);
            Server srv = new Server();
            srv.setName((String)tmp.get(I_OXUtil.SERVER_NAME));
            srv.setServerId(Long.parseLong(""+id));
            servers[ids] = srv;
            ids++;
        }        
        return servers;
    }
    
    public static Server getTestServerObject(){
        return getTestServerObject("server_"+System.currentTimeMillis());
    }
    
    public static Server getTestServerObject(String name){
        Server srv = new Server();
        srv.setName(name);
        return srv;
    }
    
    public static Filestore getTestFilestoreObject() throws URISyntaxException{
        return getTestFilestoreObject("file:///tmp/disc_"+System.currentTimeMillis());
    }
    
    public static Filestore getTestFilestoreObject(String url) throws URISyntaxException{
        new java.io.File(new java.net.URI(url)).mkdir();
        Filestore fis = new Filestore();
        fis.setFilestoreMaxContext(1000);
        fis.setFilestoreSize(818181818);
        fis.setFilestoreUrl(url);
        return fis;
    }
    
    public static Database getTestDatabaseObject(){
        return getTestDatabaseObject("localhost","testcase-displayname-host_"+System.currentTimeMillis());
    }
    
    public static Database getTestDatabaseObject(String hostname,String name){
        Database db = new Database();
        db.setDatabaseAuthenticationId("openexchange");
        db.setDatabaseAuthenticationPassword("secret");
        db.setDatabaseDisplayName(name);
        db.setDatabaseDriver("com.mysql.jdbc.Driver");
        db.setMaster(true);
        db.setDatabaseUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        return db;
    }
    
    private static String getRMIHost(){
        return "localhost";
    }
    
    private static void compareFilestore(Filestore a,Filestore b){
        assertEquals("filestore id not equal",a.getFilestoreId(),b.getFilestoreId());
        assertEquals("filestore max. contexts not equal",a.getFilestoreMaxContext(),b.getFilestoreMaxContext());
        assertEquals("filestore size not equal",a.getFilestoreSize(),b.getFilestoreSize());
        assertEquals("filestore url not equal",a.getFilestoreUrl(),b.getFilestoreUrl());
        
    }

    private void createFilestoreChangeData(Filestore fis) {
        fis.setFilestoreMaxContext(fis.getFilestoreMaxContext()+100);        
        fis.setFilestoreSize(fis.getFilestoreSize()+10000);        
        fis.setFilestoreUrl(fis.getFilestoreUrl()+change_suffix);
    }

    private void createDatabaseChangeData(Database dis) {
    	dis.setDatabaseDisplayName(dis.getDatabaseDisplayName()+"CH");
    	dis.setDatabaseMaxUnits(dis.getDatabaseMaxUnits()+100);
    	dis.setDatabasePoolMax(dis.getDatabasePoolMax()+50);
    }

    private static void compareDatabase(Database a, Database b){
        assertEquals("database authid not equal",a.getDatabaseAuthenticationId(),b.getDatabaseAuthenticationId());
        assertEquals("database passwd not equal",a.getDatabaseAuthenticationPassword(),b.getDatabaseAuthenticationPassword());
        assertEquals("database weight not equal",a.getDatabaseClusterWeight(),b.getDatabaseClusterWeight());
        assertEquals("database name not equal",a.getDatabaseDisplayName(),b.getDatabaseDisplayName());
        assertEquals("database driver not equal",a.getDatabaseDriver(),b.getDatabaseDriver());
        assertEquals("database maxunit not equal",a.getDatabaseMaxUnits(),b.getDatabaseMaxUnits());
        assertEquals("database hardlimit not equal",a.getDatabasePoolHardlimit(),b.getDatabasePoolHardlimit());
        assertEquals("database initial not equal",a.getDatabasePoolInitial(),b.getDatabasePoolInitial());
        assertEquals("database poolmax not equal",a.getDatabasePoolMax(),b.getDatabasePoolMax());
        assertEquals("database url not equal",a.getDatabaseUrl(),b.getDatabaseUrl());
        
    }
}
