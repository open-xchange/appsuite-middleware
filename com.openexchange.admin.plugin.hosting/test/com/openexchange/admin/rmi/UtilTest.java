
package com.openexchange.admin.rmi;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Vector;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 *
 * @author cutmasta
 */
public class UtilTest extends AbstractTest {
    
    private OXUtilInterface getUtilClient() throws NotBoundException, MalformedURLException, RemoteException{
        return (OXUtilInterface)Naming.lookup(getRMIHostUrl()+OXUtilInterface.RMI_NAME);
    }
    
    public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(UtilTest.class);
	}
    
    public static Filestore getTestFilestoreObject(String name,String url) throws Exception{
        Filestore client_st = new Filestore();
        
        // create dir 
        java.net.URI uri = new java.net.URI(url);
        client_st.setUrl(uri.toString());
        new java.io.File(uri.getPath()).mkdir();
        
        client_st.setSize(100L);        
        client_st.setName(name);
        client_st.setMaxContexts(100);
        
        
        return client_st;
    }
    
    public static Database getTestDatabaseObject(String hostname,String name){
        
        Database client_db = new Database();        
        client_db.setDisplayname(name);
        client_db.setDriver("com.mysql.jdbc.Driver");
        client_db.setLogin("openexchange");
        client_db.setMaster(true);
        client_db.setMaxUnits(1000);
        client_db.setPassword("secret");
        client_db.setPoolHardLimit(20);
        client_db.setPoolInitial(2);
        client_db.setPoolMax(100);
        client_db.setUrl("jdbc:mysql://"+hostname+"/?useUnicode=true&characterEncoding=UTF-8&" +
                "autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&" +
                "serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        client_db.setClusterWeight(100);
        client_db.setMasterId(0);
        return client_db;
    }
    
    
    @Test
    public void testListMaintenanceReasons() throws Exception {
        
        OXUtilInterface oxu = getUtilClient();
        
        Vector<MaintenanceReason> c_reasons = new Vector<MaintenanceReason>();
        // add some reasons
        for(int a = 0;a<10;a++){
            MaintenanceReason mr = new MaintenanceReason();
            mr.setText("testcase-get-all-reasons-"+a+"-"+System.currentTimeMillis());
            // add reason to system
            int[] srv_id = {oxu.createMaintenanceReason(mr,ContextTest.DummyMasterCredentials()).getId().intValue()};
            mr.setId(srv_id[0]);
            c_reasons.add(mr);
        }
        
        // now fetch all reasons, and look if my added reasons are within this data set        
        int resp = 0;
        MaintenanceReason[] srv_reasons = oxu.listMaintenanceReasons(ContextTest.DummyMasterCredentials());
        for(int c = 0;c<c_reasons.size();c++){
            
            MaintenanceReason tmp = c_reasons.get(c);
            
            for(int b = 0;b<srv_reasons.length;b++){
                if(srv_reasons[b].getId().intValue()==tmp.getId().intValue() && 
                   srv_reasons[b].getText().equals(tmp.getText())){
                    resp++;
                }
            }                    
        }
        
        // check if size is same, then all added reasons were found also in the data from server
        assertEquals(resp,c_reasons.size());
        
    }
    
    
    
    @Test
    public void testRegisterServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-"+System.currentTimeMillis());
        
        reg_srv.setId(oxu.registerServer(reg_srv,ContextTest.DummyMasterCredentials()).getId());
        
        Server[] srv_resp = oxu.listServer("testcase-register-server-*",ContextTest.DummyMasterCredentials());
        int resp = 0;
        for(int a = 0;a<srv_resp.length;a++){
            if(srv_resp[a].getName().equals(reg_srv.getName()) &&
               srv_resp[a].getId()==reg_srv.getId()){
               resp++;
            }
        }
        // resp muss 1 sein , ansonsten gibts 2 server mit selber id und name
        assertTrue("Expected 1 server",resp==1);
    }
    
    @Test
    public void testUnregisterServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Server reg_srv = new Server();
        reg_srv.setName("testcase-register-server-"+System.currentTimeMillis());
        
        reg_srv.setId(oxu.registerServer(reg_srv,ContextTest.DummyMasterCredentials()).getId());
        
        Server[] srv_resp = oxu.listServer("testcase-register-server-*",ContextTest.DummyMasterCredentials());
        int resp = 0;
        for(int a = 0;a<srv_resp.length;a++){
            if(srv_resp[a].getName().equals(reg_srv.getName()) &&
               srv_resp[a].getId()==reg_srv.getId()){
               resp++;
            }
        }
        // resp muss 1 sein , ansonsten gibts 2 server mit selber id und name
        assertTrue("Expected 1 server",resp==1);
        
        
        Server sv = new Server();
        sv.setId(reg_srv.getId());
        
        // here the server was added correctly to the server, now delete it
        oxu.unregisterServer(sv,ContextTest.DummyMasterCredentials());
        
        srv_resp = oxu.listServer("testcase-register-server-*",ContextTest.DummyMasterCredentials());
        resp = 0;
        for(int a = 0;a<srv_resp.length;a++){
            if(srv_resp[a].getName().equals(reg_srv.getName()) &&
               srv_resp[a].getId()==reg_srv.getId()){
               resp++;
            }
        }
        assertTrue("Expected that server is not found",resp==0);
    }
    
    @Test
    public void testRegisterDatabase() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        String db_name = "db_"+System.currentTimeMillis();
        
        Database client_db =  getTestDatabaseObject("localhost",db_name);        
        client_db.setId(oxu.registerDatabase(client_db,ContextTest.DummyMasterCredentials()).getId());
        
        Database[] srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId().equals(client_db.getId())){
                // check if data is same 
                assertEquals(client_db.getDisplayname(),tmp.getDisplayname());
                assertEquals(client_db.getDriver(),tmp.getDriver());
                assertEquals(client_db.getLogin(),tmp.getLogin());               
                assertEquals(client_db.getMaxUnits(),tmp.getMaxUnits());
                assertEquals(client_db.getPassword(),tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(),tmp.getPoolHardLimit());   
                assertEquals(client_db.getPoolInitial(),tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(),tmp.getPoolMax());
                assertEquals(client_db.getUrl(),tmp.getUrl());  
                found_db=true;                
            }
        }
        
        assertTrue("Expected to find registered db with data",found_db);
        
    }
    
    @Test
    public void testChangeDatabase() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        String db_name = "db_"+System.currentTimeMillis();
        
        Database client_db =  getTestDatabaseObject("localhost",db_name);        
        client_db.setId(oxu.registerDatabase(client_db,ContextTest.DummyMasterCredentials()).getId());
        
        Database[] srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId().equals(client_db.getId())){
                // check if data is same 
                assertEquals(client_db.getDisplayname(),tmp.getDisplayname());
                assertEquals(client_db.getDriver(),tmp.getDriver());
                assertEquals(client_db.getLogin(),tmp.getLogin());               
                assertEquals(client_db.getMaxUnits(),tmp.getMaxUnits());
                assertEquals(client_db.getPassword(),tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(),tmp.getPoolHardLimit());   
                assertEquals(client_db.getPoolInitial(),tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(),tmp.getPoolMax());
                assertEquals(client_db.getUrl(),tmp.getUrl());   
                found_db=true;
            }
        }
        
        assertTrue("Expected to find registered db with data",found_db);
        
        // now change the db data and fetch it again
        client_db.setDisplayname(client_db.getDisplayname()+change_suffix);
        client_db.setDriver(client_db.getDriver()+change_suffix);
        client_db.setLogin(client_db.getLogin()+change_suffix);        
        client_db.setMaxUnits(2000);
        client_db.setPassword(client_db.getPassword());
        client_db.setPoolHardLimit(40);
        client_db.setPoolInitial(4);
        client_db.setPoolMax(200);
        client_db.setUrl(client_db.getUrl()+change_suffix);
        
        // change db data
        oxu.changeDatabase(client_db,ContextTest.DummyMasterCredentials());
        
        srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());        
        // remove the broken _changed entries from configdb because later tests might fail
        oxu.unregisterDatabase(new Database(client_db.getId()), ContextTest.DummyMasterCredentials());
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId()==client_db.getId()){
                // check if data is same 
                assertEquals(client_db.getDisplayname(),tmp.getDisplayname());
                assertEquals(client_db.getDriver(),tmp.getDriver());
                assertEquals(client_db.getLogin(),tmp.getLogin());               
                assertEquals(client_db.getMaxUnits(),tmp.getMaxUnits());
                assertEquals(client_db.getPassword(),tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(),tmp.getPoolHardLimit());   
                assertEquals(client_db.getPoolInitial(),tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(),tmp.getPoolMax());
                assertEquals(client_db.getUrl(),tmp.getUrl());   
            }
        }        
    }   
    
    @Test
    public void testUnregisterDatabase() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        String db_name = "db_"+System.currentTimeMillis();
        Database client_db =  getTestDatabaseObject("localhost",db_name);
        
        client_db.setId(oxu.registerDatabase(client_db,ContextTest.DummyMasterCredentials()).getId());
        
        Database[] srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId().equals(client_db.getId())){
                // check if data is same 
                assertEquals(client_db.getDisplayname(),tmp.getDisplayname());
                assertEquals(client_db.getDriver(),tmp.getDriver());
                assertEquals(client_db.getLogin(),tmp.getLogin());               
                assertEquals(client_db.getMaxUnits(),tmp.getMaxUnits());
                assertEquals(client_db.getPassword(),tmp.getPassword());
                assertEquals(client_db.getPoolHardLimit(),tmp.getPoolHardLimit());   
                assertEquals(client_db.getPoolInitial(),tmp.getPoolInitial());
                assertEquals(client_db.getPoolMax(),tmp.getPoolMax());
                assertEquals(client_db.getUrl(),tmp.getUrl());  
                found_db = true;
            }
        }
        
        assertTrue("Expected to find registered db with data",found_db);
        
        
        // now unregister database
        oxu.unregisterDatabase(new Database(client_db.getId()),ContextTest.DummyMasterCredentials());
        
        srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());
        found_db = false;
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId()==client_db.getId()){
                found_db = true;
            }
        }
        
        assertTrue("Expected that the database is no more registered",!found_db);
        
        
    }
    
    @Test
    public void testListDatabase() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        String db_name = "db_"+System.currentTimeMillis();
        Database client_db =  getTestDatabaseObject("localhost",db_name);        
        if (null == client_db) {
            throw new NullPointerException("Database object is null");
        }
        client_db.setId(oxu.registerDatabase(client_db,ContextTest.DummyMasterCredentials()).getId());
        
        Database[] srv_dbs = oxu.listDatabases("db_*",ContextTest.DummyMasterCredentials());
        boolean found_db = false;
        for(int a = 0;a<srv_dbs.length;a++){
            Database tmp = srv_dbs[a];
            // we found our added db, check now the data 
            if(tmp.getId().equals(client_db.getId())){
                // check if data is same
                if(null != tmp.getDisplayname() && tmp.getDisplayname().equals(db_name) && 
                   null != tmp.getDriver() && tmp.getDriver().equals(client_db.getDriver()) && 
                   null != tmp.getLogin() && tmp.getLogin().equals(client_db.getLogin()) && 
                   null != tmp.isMaster() && tmp.isMaster().equals(client_db.isMaster()) && 
                   null != tmp.getMaxUnits() && tmp.getMaxUnits().equals(client_db.getMaxUnits()) &&
                   null != tmp.getPassword() && tmp.getPassword().equals(client_db.getPassword()) &&
                   null != tmp.getPoolHardLimit() && tmp.getPoolHardLimit().equals(client_db.getPoolHardLimit()) &&
                   null != tmp.getPoolInitial() && tmp.getPoolInitial().equals(client_db.getPoolInitial()) &&
                   null != tmp.getPoolMax() && tmp.getPoolMax().equals(client_db.getPoolMax()) && 
                   null != tmp.getUrl() && tmp.getUrl().equals(client_db.getUrl()) ) {
                    found_db=true;
                }
            }
        }
        
        assertTrue("Expected to find registered db with data",found_db);
    }
    
    @Test
    public void testListServer() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Server client_srv = new Server();
        client_srv.setName("testcase-search-server-"+System.currentTimeMillis());
        client_srv.setId(oxu.registerServer(client_srv,ContextTest.DummyMasterCredentials()).getId());
        
        Server[] srv_response = oxu.listServer("testcase-search-server-*",ContextTest.DummyMasterCredentials());
        boolean found_srv = false;
        for(int a = 0;a<srv_response.length;a++){
            Server tmp = srv_response[a];
            if(tmp.getId()==client_srv.getId() && 
                    tmp.getName().equals(client_srv.getName())){
                found_srv = true;
            }
        }
        
        assertTrue("Expected to find registered server with data",found_srv);
        
    }
    
    @Test
    public void testRegisterFilestore() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Filestore client_st = new Filestore();
        // set broken data to check server side verifying
        client_st.setUrl("file:///tmp broken");        
        try{
            client_st.setId(oxu.registerFilestore(client_st,ContextTest.DummyMasterCredentials()).getId());
            fail("Exception expected while registering broken filestore!");
        }catch(InvalidDataException ivd){
            assertTrue(true);
        }
        
        client_st = getTestFilestoreObject("testcase_registerfilestore_disc_"+System.currentTimeMillis(),"file:///tmp/disc_"+System.currentTimeMillis());
         
        client_st.setId(oxu.registerFilestore(client_st,ContextTest.DummyMasterCredentials()).getId());
        
        try{
            oxu.listFilestores(null,ContextTest.DummyMasterCredentials());
            fail("Exception expected while listing filestore with invalid pattern!");
        }catch(InvalidDataException ivd){    
            assertTrue(true);
        }
        
        Filestore[] srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        // now check if added filestore was correctly registered to the system        
        boolean found_store = false;
        for(int a = 0;a<srv_stores.length;a++){
            Filestore tmp = srv_stores[a];            
            if(tmp.getId().intValue()==client_st.getId().intValue()){               
                assertEquals(client_st.getMaxContexts(),tmp.getMaxContexts());                
                assertEquals(client_st.getSize(),tmp.getSize());                
                assertEquals(client_st.getUrl(),tmp.getUrl());
                found_store = true;                
            }
        }
        assertTrue("Expected to find registered filestore with data",found_store);
    }
    
    @Test
    public void testChangeFilestore() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Filestore client_st = getTestFilestoreObject("testcase_registerfilestore_disc_"+System.currentTimeMillis(),"file:///tmp/disc_"+System.currentTimeMillis());
         
        client_st.setId(oxu.registerFilestore(client_st,ContextTest.DummyMasterCredentials()).getId());
        
        Filestore[] srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        // now check if added filestore was correctly registered to the system        
        boolean found_store = false;
        for(int a = 0;a<srv_stores.length;a++){
            Filestore tmp = srv_stores[a];            
            if(tmp.getId().intValue()==client_st.getId().intValue()){               
                assertEquals(client_st.getMaxContexts(),tmp.getMaxContexts());                
                assertEquals(client_st.getSize(),tmp.getSize());                
                assertEquals(client_st.getUrl(),tmp.getUrl());
                found_store = true;                
            }
        }
        
        assertTrue("Expected to find registered filestore with data",found_store);
        
        
        // set change data
        client_st.setMaxContexts(1337);
        client_st.setSize(13337L);
        client_st.setUrl(client_st.getUrl()+change_suffix);
        
        // change store on server
        oxu.changeFilestore(client_st,ContextTest.DummyMasterCredentials());
        
        srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        // now check if added filestore was correctly registered to the system        
        found_store = false;
        for(int a = 0;a<srv_stores.length;a++){
            Filestore tmp = srv_stores[a];            
            if(tmp.getId().intValue()==client_st.getId().intValue()){               
                assertEquals(client_st.getMaxContexts(),tmp.getMaxContexts());                
                assertEquals(client_st.getSize(),tmp.getSize());                
                assertEquals(client_st.getUrl(),tmp.getUrl());
                found_store = true;                
            }
        }
        assertTrue("Expected to find changed filestore with data",found_store);
        
    }
    
    @Test
    public void testListFilestores() throws Exception {
       OXUtilInterface oxu = getUtilClient();
        
        Filestore client_st = getTestFilestoreObject("testcase_registerfilestore_disc_"+System.currentTimeMillis(),"file:///tmp/disc_"+System.currentTimeMillis());
         
        client_st.setId(oxu.registerFilestore(client_st,ContextTest.DummyMasterCredentials()).getId());
        
        Filestore[] srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        assertTrue("Expected list size > 0 ",srv_stores.length>0);
    }
    
    @Test
    public void testUnregisterFilestore() throws Exception {
        OXUtilInterface oxu = getUtilClient();
        
        Filestore client_st  = getTestFilestoreObject("testcase_registerfilestore_disc_"+System.currentTimeMillis(),"file:///tmp/disc_"+System.currentTimeMillis());
         
        client_st.setId(oxu.registerFilestore(client_st,ContextTest.DummyMasterCredentials()).getId());
        
        Filestore[] srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        // now check if added filestore was correctly registered to the system        
        boolean found_store = false;
        for(int a = 0;a<srv_stores.length;a++){
            Filestore tmp = srv_stores[a];            
            if(tmp.getId().intValue()==client_st.getId().intValue()){               
                assertEquals(client_st.getMaxContexts(),tmp.getMaxContexts());                
                assertEquals(client_st.getSize(),tmp.getSize());                
                assertEquals(client_st.getUrl(),tmp.getUrl());
                found_store = true;                
            }
        }
        assertTrue("Expected to find registered filestore with data",found_store);
        
        
        // now unregister and search again
        oxu.unregisterFilestore(new Filestore(client_st.getId()),ContextTest.DummyMasterCredentials());
        
        srv_stores = oxu.listFilestores("file:///tmp/disc_*",ContextTest.DummyMasterCredentials());
        
        // now check if added filestore was correctly registered to the system        
        found_store = false;
        for(int a = 0;a<srv_stores.length;a++){
            Filestore tmp = srv_stores[a];            
            if(tmp.getId()==client_st.getId()){                
                found_store = true;                
            }
        }
        
        assertFalse("Expected not to find already unregistered filestore",found_store);
        
    }
    
    
    
}
