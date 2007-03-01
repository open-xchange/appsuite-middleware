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
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;

import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author cutmasta
 * @author d7
 */
public class ContextTest extends AbstractTest {

    public void testChangeStorageData() throws Exception {
        final Credentials cred = DummyCredentials();
        final int ctsid = createNewContextID(cred);
        final Context ctxset = getTestContextObject(ctsid, 50);
        final String hosturl = getRMIHostUrl();
        final int id = addContext(ctxset, hosturl, cred);
        ctxset.setID(id);
        final Filestore filestore = ctxset.getFilestore();        
        filestore.setQuota_max(1337L);
        filestore.setId(111111337);
        changeStorageData(ctxset, hosturl, cred);
    }

    public void testGetContext() throws Exception {
        fail("NOT IMPLEMENTED");
        final Credentials cred = DummyCredentials();
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

    public void testSearchContextByDatabase() throws Exception {
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        Database[] dbs = oxu.searchForDatabase("*", cred);
        if (dbs.length > 0) {
            Context[] ids = searchContextByDatabase(dbs[0], hosturl, cred);
            assertTrue("no contexts found on database " + dbs[0].getUrl(), ids.length > 0);
        } else {
            fail("no databases found to search with");
        }
    }

    public void testSearchContextByFilestore() throws Exception {
        final Credentials cred = DummyCredentials();
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

    public void testDisableContext() throws Exception {
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        MaintenanceReason[] mrs = oxu.getAllMaintenanceReasons(cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.addMaintenanceReason(mr, cred);
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }
        disableContext(ctx, mr, hosturl, DummyCredentials());
        Context[] ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_disabled = false;

        for (final Context elem : ctxs) {            
            if (!elem.isEnabled() && elem.getMaintenanceReason().getId() == mr.getId()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);

    }

    public void testEnableContext() throws Exception {
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        MaintenanceReason[] mrs = oxu.getAllMaintenanceReasons(cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.addMaintenanceReason(mr, cred);
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }

        disableContext(ctx, mr, hosturl, cred);
        Context[] ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_disabled = false;
        for (final Context elem : ctxs) {
            if (elem.getIdAsInt() == ctx.getIdAsInt() && !elem.isEnabled() && elem.getMaintenanceReason().getId() == mr.getId()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);

        // now enable context again
        enableContext(ctx, hosturl, cred);
        ctxs = searchContext(String.valueOf(ctx.getIdAsInt()), hosturl, cred);
        boolean ctx_ensabled = false;
        for (final Context elem : ctxs) {
            if ((elem.getIdAsInt() == ctx.getIdAsInt()) && elem.isEnabled()) {
                ctx_ensabled = true;
            }
        }
        assertTrue("context could be not enabled", ctx_ensabled);
    }

    public void testAddContext() throws Exception {
        final Credentials cred = DummyCredentials();
        Context ctxset = getTestContextObject(cred);
        addContext(ctxset, getRMIHostUrl(), cred);
    }

    public void testDeleteContext() throws Exception {
        final Credentials cred = DummyCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctx = getTestContextObject(ctxid, 50);
        ctxid = addContext(ctx, hosturl, cred);
        deleteContext(ctx, hosturl, cred);
    }

    public void testSearchContext() throws Exception {
        final Credentials cred = DummyCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctxset = getTestContextObject(ctxid, 50);
        addContext(ctxset, hosturl, cred);
        // now search exactly for the added context
        Context[] ctxs = searchContext(String.valueOf(ctxid), hosturl, cred);
        boolean foundctx = false;
        for (final Context elem : ctxs) {
            if (elem.getIdAsInt() == ctxid) {
                foundctx = true;
            }
        }
        assertTrue("context not found", foundctx);
    }

    public static void changeStorageData(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.changeStorageData(ctx, ctx.getFilestore(), cred);
    }

    public static Context[] searchContextByDatabase(Database db, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.searchByDatabase(db, cred);
    }

    public static Context[] searchContextByFilestore(Filestore fis, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.searchByFilestore(fis, cred);
    }

    public static Context getContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.getSetup(ctx, cred);
    }

    public static Context[] searchContext(String pattern, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);        
        return xres.search(pattern, cred);
    }

    public static void deleteContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.delete(ctx, cred);
    }

    public static int addContext(Context ctx, String host, Credentials cred) throws Exception {
        final String hosturl = getRMIHostUrl();
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(host + OXUtilInterface.RMI_NAME);
        // first check if the needed server entry is in db, if not, add server
        // first,
        if (oxu.searchForServer("local", cred).length != 1) {
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
        if (oxu.searchForDatabase("test-ox-db", cred).length == 0) {
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

    private static int searchNextFreeContextID(int pos, Credentials cred) throws Exception {
        Context[] ctx = searchContext(String.valueOf(pos), getRMIHostUrl(), cred);
        if (ctx.length == 0) {
            return pos;
        } else {
            return -1;
        }
    }

    public static Context getTestContextObject(Credentials cred) throws Exception {
        return getTestContextObject(createNewContextID(cred), 50);
    }

    public static Context getTestContextObject(int context_id, long quota_max_in_mb) {
        Context ctx = new Context(context_id);        
        final Filestore filestore = new Filestore();
        filestore.setQuota_max(quota_max_in_mb);
        ctx.setFilestore(filestore);
        return ctx;
    }

    public static int createNewContextID(Credentials cred) throws Exception {
        int pos = 5;
        int ret = -1;
        while (ret == -1) {
            ret = searchNextFreeContextID(pos, cred);
            pos = pos + 3;
        }
        return ret;
    }

}
