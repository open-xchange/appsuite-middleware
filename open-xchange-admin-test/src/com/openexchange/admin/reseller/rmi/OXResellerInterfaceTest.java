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

package com.openexchange.admin.reseller.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Stack;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXResellerInterfaceTest extends OXResellerAbstractTest {

    private static Stack<Context> restrictionContexts = null;

    private static OXResellerInterface oxresell = null;

    private static OXContextInterface oxctx = null;

    @BeforeClass
    public static void startup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, OXResellerException {
        oxresell = (OXResellerInterface) Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        oxctx = (OXContextInterface) Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
        final Credentials creds = DummyMasterCredentials();
        oxresell.initDatabaseRestrictions(creds);
    }

    @AfterClass
    public static void cleanup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchContextException, DatabaseUpdateException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        final Context[] ctxs = oxctx.list("*", creds);
        for (final Context ctx : ctxs) {
            oxctx.delete(ctx, creds);
        }

        oxresell.removeDatabaseRestrictions(creds);
    }

    @After
    public final void deleteAdmin() throws Exception {
        final Credentials creds = DummyMasterCredentials();

        final ResellerAdmin[] adms = oxresell.list("*", creds);
        for (final ResellerAdmin adm : adms) {
            oxresell.delete(adm, creds);
        }
    }

    @Test
    public void testUpdateModuleAccessRestrictions() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.updateDatabaseModuleAccessRestrictions(creds);
    }

    @Test
    public void testCreate() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = oxresell.create(TestAdminUser(), creds);
        ResellerAdmin admch = oxresell.create(TestAdminUser(TESTCHANGEUSER, "Test Change User"), creds);

        assertNotNull("creation of ResellerAdmin failed", adm);
        assertNotNull("creation of ResellerAdmin failed", admch);
        assertTrue("creation of ResellerAdmin failed", adm.getId() > 0);
        assertTrue("creation of ResellerAdmin failed", admch.getId() > 0);
    }

    @Test(expected = InvalidDataException.class)
    public void testCreateMissingMandatoryFields() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = new ResellerAdmin();
        // no displayname
        adm.setName("incomplete");
        adm.setPassword("secret");
        oxresell.create(adm, creds);

        // no password
        adm.setPassword(null);
        adm.setDisplayname("Test incomplete");
        adm.setName("incomplete");
        oxresell.create(adm, creds);

        // no name
        adm.setPassword("secret");
        adm.setDisplayname("Test incomplete");
        adm.setName(null);
        oxresell.create(adm, creds);
    }

    @Test
    public void testCreateWithRestrictions() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        for (final String user : new String[] { TESTRESTRICTIONUSER, TESTRESTCHANGERICTIONUSER }) {
            ResellerAdmin adm = TestAdminUser(user, "Test Restriction User");
            adm.setRestrictions(new Restriction[] { MaxContextRestriction(), MaxContextQuotaRestriction() });
            adm = oxresell.create(adm, creds);

            System.out.println(adm);

            assertNotNull("creation of ResellerAdmin failed", adm);
            assertTrue("creation of ResellerAdmin failed", adm.getId() > 0);
        }
    }

    @Test
    public void testChangeWithRestrictions() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = TestAdminUser(TESTRESTCHANGERICTIONUSER);
        adm.setRestrictions(new Restriction[] { MaxContextRestriction(), MaxContextQuotaRestriction() });
        adm = oxresell.create(adm, creds);

        adm = oxresell.getData(TestAdminUser(TESTRESTCHANGERICTIONUSER), creds);
        Restriction r = getRestrictionByName(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN, adm.getRestrictions());
        assertNotNull("Restriction Restriction.MAX_CONTEXT_QUOTA not found", r);
        r.setValue("2000");
        oxresell.change(adm, creds);

        adm = oxresell.getData(TestAdminUser(TESTRESTCHANGERICTIONUSER), creds);
        r = getRestrictionByName(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN, adm.getRestrictions());

        assertNotNull("Restriction Restriction.MAX_CONTEXT_QUOTA not found", r);
        assertEquals("Change Restriction value failed", "2000", r.getValue());
    }

    @Test
    public void testChange() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.create(TestAdminUser(TESTCHANGEUSER, "Test Change User"), creds);

        ResellerAdmin adm = new ResellerAdmin(TESTCHANGEUSER);
        final String newdisp = "New Display name";
        adm.setDisplayname(newdisp);

        oxresell.change(adm, creds);

        ResellerAdmin chadm = oxresell.getData(new ResellerAdmin(TESTCHANGEUSER), creds);

        assertEquals("getData must return changed Displayname", adm.getDisplayname(), chadm.getDisplayname());
    }

    @Test
    public void testChangeName() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.create(TestAdminUser(TESTCHANGEUSER, "Test Change User"), creds);

        ResellerAdmin adm = oxresell.getData(new ResellerAdmin(TESTCHANGEUSER), creds);
        adm.setName(CHANGEDNAME);
        oxresell.change(adm, creds);
        ResellerAdmin newadm = new ResellerAdmin();
        newadm.setId(adm.getId());
        ResellerAdmin chadm = oxresell.getData(newadm, creds);
        assertEquals("getData must return changed name", adm.getName(), chadm.getName());
    }

    @Test(expected = StorageException.class)
    public void testChangeNameWithoutID() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = new ResellerAdmin();
        adm.setName(CHANGEDNAME + "new");
        oxresell.change(adm, creds);
    }

    @Test
    public void testGetData() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, PoolException, SQLException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        final ResellerAdmin adm = TestAdminUser();

        oxresell.create(adm, creds);

        final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin(TESTUSER), creds);

        assertEquals("getData returned wrong data", adm.getName(), dbadm.getName());
        assertEquals("getData returned wrong data", adm.getDisplayname(), dbadm.getDisplayname());
    }

    @Test
    public void testGetDataBug19102() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, PoolException, SQLException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        final ResellerAdmin adm = TestAdminUser();

        oxresell.create(adm, creds);

        // add some restrictions to adm
        adm.setRestrictions(new Restriction[] { new Restriction(Restriction.MAX_OVERALL_USER_PER_SUBADMIN, "2") });
        final ResellerAdmin dbadm = oxresell.getData(adm, creds);
        // and check whether they are still there after getData call
        assertNull("there must be no restrictions set", dbadm.getRestrictions());
    }

    @Test
    public void testGetDataWithRestrictions() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, PoolException, SQLException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        final ResellerAdmin adm = TestAdminUser(TESTRESTRICTIONUSER, "Test Restriction User");
        adm.setRestrictions(new Restriction[] { MaxContextRestriction(), MaxContextQuotaRestriction() });
        oxresell.create(adm, creds);

        final ResellerAdmin dbadm = oxresell.getData(adm, creds);

        Restriction[] res = dbadm.getRestrictions();
        assertNotNull("ResellerAdmin must contain Restrictions", res);

        boolean foundmaxctx = getRestrictionByName(Restriction.MAX_CONTEXT_PER_SUBADMIN, res) == null ? false : true;
        boolean foundmaxctxquota = getRestrictionByName(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN, res) == null ? false : true;

        assertTrue(MaxContextQuotaRestriction().getName() + " must be contained in ResellerAdmin", foundmaxctx);
        assertTrue(MaxContextRestriction().getName() + " must be contained in ResellerAdmin", foundmaxctxquota);
        assertEquals("getData returned wrong data", adm.getName(), dbadm.getName());
        assertEquals("getData returned wrong data", adm.getDisplayname(), dbadm.getDisplayname());
    }

    // @Test
    // public void testList() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException,
    // InvalidCredentialsException {
    // final Credentials creds = DummyMasterCredentials();
    // ResellerAdmin[] res = oxresell.list("*", creds);
    // for(final ResellerAdmin adm : res) {
    // System.out.println(adm);
    // }
    // assertEquals("list must return three entries",4, res.length);
    // }

    @Test
    public void testRestrictionsToContext() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException, OXResellerException {
        final Credentials masterCreds = DummyMasterCredentials();
        oxresell.create(TestAdminUser(), masterCreds);
        restrictionContexts = new Stack<Context>();
        for (final Credentials creds : new Credentials[] { DummyMasterCredentials(), TestUserCredentials() }) {

            User oxadmin = ContextAdmin();
            Context ctx1 = new Context();
            ctx1.setMaxQuota(100000L);

            try {
                ctx1.addExtension(new OXContextExtensionImpl(new Restriction[] { MaxUserPerContextRestriction() }));
            } catch (final DuplicateExtensionException e) {
                // cannot occur on a newly created context
                e.printStackTrace();
            }
            final Context ctx = oxctx.create(ctx1, oxadmin, creds);
            restrictionContexts.push(ctx);
        }

        for (final Credentials creds : new Credentials[] { TestUserCredentials(), DummyMasterCredentials() }) {
            final Context ctx = restrictionContexts.pop();

            Restriction[] res = oxresell.getRestrictionsFromContext(ctx, creds);
            assertNotNull("Context restrictions must not be null", res);
            assertEquals("Context restrictions must contain one restriction", 1, res.length);
            assertEquals("Restriction value does not match expected value", MaxUserPerContextRestriction().getValue(), res[0].getValue());
            deleteContext(ctx, creds);
        }
    }

    @Test
    public void testDeleteContextOwningSubadmin() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.create(TestAdminUser("owned"), creds);
        final Context ctx = createContext(new Credentials("owned", "secret"));

        boolean deleteFailed = false;
        try {
            oxresell.delete(TestAdminUser("owned"), creds);
        } catch (OXResellerException e) {
            deleteFailed = true;
        }
        assertTrue("deletion of ResellerAdmin must fail", deleteFailed);

        deleteContext(ctx, new Credentials("owned", "secret"));
        oxresell.delete(TestAdminUser("owned"), creds);
    }

    @Test
    public void testDeleteByID() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = oxresell.create(TestAdminUser(), creds);
        adm = oxresell.getData(adm, creds);
        adm.setName(null);

        oxresell.delete(adm, creds);
    }

}
