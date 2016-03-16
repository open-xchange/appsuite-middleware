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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
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

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ContextTest.class);
    }
    @Test
    public void testGetAdminId() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final String hosturl = getRMIHostUrl();
        Context ctx = addSystemContext(getTestContextObject(cred), getRMIHostUrl(), cred);

        OXContextInterface xctx = (OXContextInterface) Naming.lookup(hosturl + OXContextInterface.RMI_NAME);

        assertEquals(2, xctx.getAdminId(ctx, cred));
    }

    @Test
    public void testGetAndChangeContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final String hosturl = getRMIHostUrl();
        Context ctx = addSystemContext(getTestContextObject(cred), getRMIHostUrl(), cred);

        OXContextInterface xctx = (OXContextInterface) Naming.lookup(hosturl + OXContextInterface.RMI_NAME);

        Context srv_loaded = xctx.getData(ctx, cred);

        assertEquals("lemon", srv_loaded.getUserAttribute("com.openexchange.test", "flavor"));
        assertEquals("squishy", srv_loaded.getUserAttribute("com.openexchange.test", "texture"));


        assertTrue("Expected same context ids", ctx.getId().intValue() == srv_loaded.getId().intValue());

        String add_mapping = srv_loaded.getId().intValue() + "_" + System.currentTimeMillis();
        srv_loaded.addLoginMapping(add_mapping);

        String changed_context_name = srv_loaded.getName() + "_" + System.currentTimeMillis();
        srv_loaded.setName(changed_context_name);

        // And for good measure some dynamic attributes

        srv_loaded.setUserAttribute("com.openexchange.test", "flavor", "pistaccio");
        srv_loaded.setUserAttribute("com.openexchange.test", "color", "green");
        srv_loaded.setUserAttribute("com.openexchange.test", "texture", null);

        // change context and load again
        xctx.change(srv_loaded, cred);

        Context edited_ctx = xctx.getData(ctx, cred);

        assertEquals("pistaccio", srv_loaded.getUserAttribute("com.openexchange.test", "flavor"));
        assertEquals("green", srv_loaded.getUserAttribute("com.openexchange.test", "color"));
        assertEquals(null, srv_loaded.getUserAttribute("com.openexchange.test", "texture"));


        // ids must be correct again and the mapping should now exist
        assertTrue("Expected same context ids", edited_ctx.getId().intValue() == srv_loaded.getId().intValue());

        // new mapping must exists
        assertTrue("Expected changed login mapping in loaded context", edited_ctx.getLoginMappings().contains(add_mapping));

        // changed conmtext name must exists
        assertTrue("Expected changed context name to be same as loaded ctx", edited_ctx.getName().equals(changed_context_name));
    }

    @Test
    public void testGetByName() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final String hosturl = getRMIHostUrl();
        final Context ctx = addSystemContext(getTestContextObject(cred), getRMIHostUrl(), cred);

        OXContextInterface xctx = (OXContextInterface) Naming.lookup(hosturl + OXContextInterface.RMI_NAME);

        final Integer idAsInt = ctx.getId();
        ctx.setId(null);
        final Context srv_loaded = xctx.getData(ctx, cred);

        // Reset for compare
        ctx.setId(idAsInt);
        // FIXME: Add equals for context here. Same at nearly all other occurrences
        assertTrue("Expected same context ids", idAsInt.intValue() == srv_loaded.getId().intValue());
    }

    @Test
    public void testListContextByDatabase() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        Database[] dbs = oxu.listDatabase("*", cred);
        if (dbs.length > 0) {
            Context[] ids = searchContextByDatabase(dbs[0], hosturl, cred);
            assertTrue("no contexts found on database " + dbs[0].getUrl(), ids.length > 0);
        } else {
            fail("no databases found to search with");
        }
    }

    @Test
    public void testListContextByFilestore() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        final Context ctx = getTestContextObject(cred);
        final String hosturl = getRMIHostUrl();
        addContext(ctx, hosturl, cred);
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(hosturl + OXUtilInterface.RMI_NAME);
        Filestore[] fiss = oxu.listFilestore("*", cred);
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
        MaintenanceReason[] mrs = oxu.listMaintenanceReason("*",cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.createMaintenanceReason(mr, cred).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }
        disableContext(ctx, hosturl, cred);
        Context[] ctxs = searchContext(String.valueOf(ctx.getId()), hosturl, cred);
        boolean ctx_disabled = false;

        for (final Context elem : ctxs) {
            if (!elem.isEnabled()) {
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
        MaintenanceReason[] mrs = oxu.listMaintenanceReason("*",cred);
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = oxu.createMaintenanceReason(mr, cred).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }

        disableContext(ctx, hosturl, cred);
        Context[] ctxs = searchContext(ctx.getIdAsString(), hosturl, cred);
        boolean ctx_disabled = false;
        for (final Context elem : ctxs) {
            if (elem.getId().intValue() == ctx.getId().intValue() && !elem.isEnabled()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);

        // now enable context again
        enableContext(ctx, hosturl, cred);
        ctxs = searchContext(String.valueOf(ctx.getId()), hosturl, cred);
        boolean ctx_ensabled = false;
        for (final Context elem : ctxs) {
            if ((elem.getId().intValue() == ctx.getId().intValue()) && elem.isEnabled()) {
                ctx_ensabled = true;
            }
        }
        assertTrue("context could be not enabled", ctx_ensabled);
    }

    @Test
    public void testCreateContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        Context ctxset = getTestContextObject(cred);
        addContext(ctxset, getRMIHostUrl(), cred);
    }

    @Test(expected=InvalidDataException.class)
    public void testCreateContextNoQuota() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        Context ctxset = getTestContextObjectNoQuota(cred);
        addContext(ctxset, getRMIHostUrl(), cred);
        deleteContext(ctxset, getRMIHostUrl(), cred);
    }

    @Test
    public void testCreateDeleteCreateContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctx = getTestContextObject(ctxid, 50);
        ctxid = addSystemContext(ctx, hosturl, cred).getId().intValue();
        deleteContext(ctx, hosturl, cred);
        addSystemContext(ctx, hosturl, cred).getId().intValue();
    }

    @Test
    public void testDuplicateLoginMappingsThrowReadableError() throws Exception {
        final Credentials cred = DummyMasterCredentials();

        List<Context> clean = new ArrayList<Context>();

        Context ctx1 = getTestContextObject(cred);
        ctx1.setLoginMappings(new HashSet<String>(Arrays.asList("foo")));
        addContext(ctx1, getRMIHostUrl(), cred);
        clean.add(ctx1);
        try {
            Context ctx2 = getTestContextObject(cred);
            ctx2.setLoginMappings(new HashSet<String>(Arrays.asList("foo")));
            addContext(ctx2, getRMIHostUrl(), cred);
            clean.add( ctx2 );
            fail("Could add Context");
        } catch (Exception x) {
            assertEquals("Cannot map 'foo' to the newly created context. This mapping is already in use.", x.getMessage());
        } finally {
            for (Context context : clean) {
                deleteContext(context, getRMIHostUrl(), cred);
            }
        }

    }

    @Test
    public void testDeleteContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctx = getTestContextObject(ctxid, 50);
        ctxid = addSystemContext(ctx, hosturl, cred).getId().intValue();
        deleteContext(ctx, hosturl, cred);
    }

    @Test
    public void testListContext() throws Exception {
        final Credentials cred = DummyMasterCredentials();
        int ctxid = createNewContextID(cred);
        final String hosturl = getRMIHostUrl();
        final Context ctxset = getTestContextObject(ctxid, 50);
        addContext(ctxset, hosturl, cred);
        // now search exactly for the added context
        Context[] ctxs = searchContext(String.valueOf(ctxid), hosturl, cred);
        boolean foundctx = false;
        for (final Context elem : ctxs) {
            if (elem.getId().intValue() == ctxid) {
                foundctx = true;
            }
        }
        assertTrue("context not found", foundctx);
    }


    private Context[] searchContextByDatabase(Database db, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.listByDatabase(db, cred);
    }

    private Context[] searchContextByFilestore(Filestore fis, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.listByFilestore(fis, cred);
    }

    public static Context[] searchContext(String pattern, String host, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        return xres.list(pattern, cred);
    }

    private void deleteContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.delete(ctx, cred);
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
            java.net.URI uri = new java.net.URI("file:/tmp/disc_" + System.currentTimeMillis());
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

        oxcontext.create(ctx,UserTest.getTestUserObject("admin","secret", ctx), cred);
        return ctx;
    }

    private int addContext(Context ctx, String host, Credentials cred) throws Exception {
        return addSystemContext(ctx,host,cred).getId().intValue();
    }


    private void disableContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        //xres.disable(ctx, mr, cred);
        xres.disable(ctx, cred);
    }

    private void enableContext(Context ctx, String host, Credentials cred) throws Exception {
        OXContextInterface xres = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        xres.enable(ctx, cred);
    }

    public static int searchNextFreeContextID(int pos, Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        Context[] ctx = searchContext(String.valueOf(pos), getRMIHostUrl(), cred);
        if (ctx.length == 0) {
            return pos;
        } else {
            return -1;
        }
    }

    public static Context getTestContextObject(Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        return getTestContextObject(createNewContextID(cred), 5000);
    }

    public static Context getTestContextObjectNoQuota(Credentials cred) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        return getTestContextObject(createNewContextID(cred));
    }

    public static Context getTestContextObject(int context_id, long quota_max_in_mb) {
        Context ctx = getTestContextObject(context_id);
        ctx.setMaxQuota(quota_max_in_mb);
        ctx.setUserAttribute("com.openexchange.test", "flavor", "lemon");
        ctx.setUserAttribute("com.openexchange.test", "texture", "squishy");
        return ctx;
    }

    // Must be public static to override method
    public static Context getTestContextObject(int context_id) {
        Context ctx = new Context(context_id);
        ctx.setName("Name-"+ctx.getId());
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

    @Test
    public void testExistsContext() throws Exception {
        OXContextInterface ctxstub = (OXContextInterface) Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);

        final Credentials cred = DummyMasterCredentials();
        Context ctxexists = getTestContextObject(cred);
        addContext(ctxexists, getRMIHostUrl(), cred);
        Context ctxnotexists = new Context();
        ctxnotexists.setName("notexists.com");

        assertFalse("context must not exist", ctxstub.exists(ctxnotexists, cred));
        assertTrue("context must exist", ctxstub.exists(ctxexists, cred));
    }

}
