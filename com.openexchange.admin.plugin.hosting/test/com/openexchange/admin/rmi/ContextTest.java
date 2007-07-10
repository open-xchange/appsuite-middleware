
package com.openexchange.admin.rmi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.admin.rmi.UserTest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * 
 * @author cutmasta
 * @author d7
 */
public class ContextTest extends AbstractTest {

    
    public static Credentials DummyMasterCredentials(){
        return new Credentials("oxadminmaster","secret");
    }
    
    public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ContextTest.class);
	}
    
    
    @Test
    public void testGetContext() throws Exception {
        fail("NOT IMPLEMENTED");
        final Credentials cred = DummyMasterCredentials();
        Context ctx = getTestContextObject(cred);
        addContext(ctx, getRMIHostUrl(), cred);

        // Context loaded_ctx = ctx_setup.getContext();
        //                
        //               
        // // now change some context values like enabled/disabled, quota max,
        // filestore username,filestore passwd
        // long changed_quota_max = loaded_ctx.getFilestoreQuotaMax()+10;
        // loaded_ctx.setFilestoreQuotaMax(changed_quota_max);
        // loaded_ctx.setFilestoreUsername(""+loaded_ctx.getFilestoreUsername()+"_changed");
        //                
        // changeStorageData(loaded_ctx,getRMIHost());
        // Context ctx_setup_loaded =
        // getContext(loaded_ctx,getRMIHost());
        //                
        // // compareContext();
        // log(ctx_setup_loaded.getContext().getFilestoreUsername()+""+ctx_setup.getContext().getFilestoreUsername());
        // //
        // log("->"+loaded_ctx.getFilestoreQuotaMax()+"->"+ctx.getFilestoreQuotaMax());
        // fail("not completely implemented in testsuite");
    }

    @Test
    public void testSearchContextByDatabase() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        Database[] dbs = oxu.listDatabases("*", cred);
        if (dbs.length > 0) {
            Context[] ids = searchContextByDatabase(dbs[0], hosturl, cred);
            assertTrue("no contexts found on database " + dbs[0].getUrl(), ids.length > 0);
        } else {
            fail("no databases found to search with");
        }
    }

    @Test
    public void testSearchContextByFilestore() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        Filestore[] fiss = oxu.listFilestores("*", cred);
        if (fiss.length > 0) {
            boolean foundctxviastore = false;
            for (int a = 0; a < fiss.length; a++) {
                Context[] ids = searchContextByFilestore(fiss[a], hosturl, cred);
                if (ids.length > 0) {
                    foundctxviastore = true;
                }
            }
            assertTrue("no contexts found on filestores", foundctxviastore);
        } else {
            fail("no databases found to search with");
        }
    }

    @Test
    public void testDisableContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        MaintenanceReason[] mrs = oxu.listMaintenanceReasons(cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.createMaintenanceReason(mr, cred).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }
        disableContext(ctx, mr, hosturl, cred);
        Context[] ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_disabled = false;

        for (final Context elem : ctxs) {            
            if (!elem.isEnabled() && elem.getMaintenanceReason().getId().intValue() == mr.getId().intValue()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);

    }

    @Test
    public void testEnableContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        MaintenanceReason[] mrs = oxu.listMaintenanceReasons(cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.createMaintenanceReason(mr, cred).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }

        disableContext(ctx, mr, hosturl, cred);
        Context[] ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_disabled = false;
        for (final Context elem : ctxs) {
            if (elem.getIdAsInt().intValue() == ctx.getIdAsInt().intValue() && !elem.isEnabled() && elem.getMaintenanceReason().getId().intValue() == mr.getId().intValue()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);

        // now enable context again
        enableContext(ctx, hosturl, cred);
        ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_ensabled = false;
        for (final Context elem : ctxs) {
            if ((elem.getIdAsInt().intValue() == ctx.getIdAsInt().intValue()) && elem.isEnabled()) {
                ctx_ensabled = true;
            }
        }
        assertTrue("context could be not enabled", ctx_ensabled);
    }

    @Test
    public void testAddContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        Context ctxset = getTestContextObject(cred);
        addContext(ctxset, getRMIHostUrl(), cred);
    }

    @Test
    public void testDeleteContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctx = getTestContextObject(ctxid, 50);
        ctxid = addContext(ctx, hosturl, cred);
        deleteContext(ctx, hosturl, cred);
    }

    @Test
    public void testSearchContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctxset = getTestContextObject(ctxid, 50);
        addContext(ctxset, hosturl, cred);
        // now search exactly for the added context
        Context[] ctxs = searchContext(String.valueOf(ctxid), hosturl, cred);
        boolean foundctx = false;
        for (final Context elem : ctxs) {
            if (elem.getIdAsInt().intValue() == ctxid) {
                foundctx = true;
            }
        }
        assertTrue("context not found", foundctx);
    }

   
    public static Context[] searchContextByDatabase(Database db, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.listByDatabase(db, cred);
    }

    public static Context[] searchContextByFilestore(Filestore fis, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.listByFilestore(fis, cred);
    }

    public static Context getContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.getData(ctx, cred);
    }

    public static Context[] searchContext(String pattern, String host, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);        
        return xres.list(pattern, cred);
    }

    public static void deleteContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.delete(ctx, cred);
    }

    public static int addContext(Context ctx, String host, Credentials cred) throws Exception {
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
        if (oxu.listFilestores("*", cred).length == 0) {
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
        // THEN we can add the contex with its data
        if (oxu.listDatabases("test-ox-db", cred).length == 0) {
            Database db = UtilTest.getTestDatabaseObject("localhost", "test-ox-db");
            oxu.registerDatabase(db, cred);
        }
        
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        
        xres.create(ctx,UserTest.getTestUserObject("admin","secret"), ctx.getFilestore().getQuota_max(), cred);
        return ctx.getIdAsInt();
    }

    public static void disableContext(Context ctx, MaintenanceReason mr, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.disable(ctx, mr, cred);
    }

    public static void enableContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.enable(ctx, cred);
    }

    private static int searchNextFreeContextID(int pos, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        Context[] ctx = searchContext(String.valueOf(pos), getRMIHostUrl(), cred);
        if (ctx.length == 0) {
            return pos;
        } else {
            return -1;
        }
    }

    public static Context getTestContextObject(Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        return getTestContextObject(createNewContextID(cred), 50);
    }

    public static Context getTestContextObject(int context_id, long quota_max_in_mb) {
        Context ctx = new Context(context_id);        
        final Filestore filestore = new Filestore();
        filestore.setQuota_max(quota_max_in_mb);
        ctx.setFilestore(filestore);
        return ctx;
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

}
