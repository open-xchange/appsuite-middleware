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
import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXResellerContextTest extends OXResellerAbstractTest {

    private static Context ownedContext = null;

    private static OXResellerInterface oxresell = null;

    private static OXContextInterface oxctx = null;

    @BeforeClass
    public static void startup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        oxresell = (OXResellerInterface) Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        oxctx = (OXContextInterface) Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
        oxresell.initDatabaseRestrictions(creds);
    }

    @AfterClass
    public static void cleanup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchContextException, DatabaseUpdateException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        oxresell.removeDatabaseRestrictions(creds);
    }

    @Before
    public final void setupAdmin() throws Exception {
        oxresell.create(FooAdminUser(), DummyMasterCredentials());
    }

    @After
    public final void deleteAdmin() throws Exception {
        final Credentials creds = DummyMasterCredentials();

        final ResellerAdmin[] adms = oxresell.list("*", creds);
        for (final ResellerAdmin adm : adms) {
            oxresell.delete(adm, creds);
        }
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testListAllContextInvalidAuthNoUser() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException {
        oxctx.listAll(ResellerBarCredentials());
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testListAllContextInvalidAuthWrongpasswd() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException {
        Credentials creds = ResellerFooCredentials();
        creds.setPassword("wrongpass");
        oxctx.listAll(creds);
    }

    @Test
    public void testListAllContextValidAuth() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException {
        final Credentials creds = ResellerFooCredentials();
        oxctx.listAll(creds);
    }

    @Test
    public void testCreateTooManyContexts() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.delete(FooAdminUser(), DummyMasterCredentials()); // Delete normaly created FooAdminUser

        ResellerAdmin adm = FooAdminUser();
        adm.setRestrictions(new Restriction[] { MaxContextRestriction() });
        oxresell.create(adm, creds);

        Context ctx1 = createContext(ResellerFooCredentials());
        Context ctx2 = createContext(ResellerFooCredentials());
        Context ctx3 = null;
        boolean failed_ctx3 = false;
        try {
            ctx3 = createContext(ResellerFooCredentials());
        } catch (StorageException e) {
            failed_ctx3 = true;
        }

        deleteContext(ctx1, ResellerFooCredentials());
        deleteContext(ctx2, ResellerFooCredentials());
        if (ctx3 != null) {
            deleteContext(ctx3, ResellerFooCredentials());
        }

        assertTrue("creation of ctx3 must fail", failed_ctx3);
    }

    @Test
    public void testCreateContextNoQuota() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.delete(FooAdminUser(), DummyMasterCredentials()); // Delete normaly created FooAdminUser

        ResellerAdmin adm = FooAdminUser();
        adm.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        oxresell.create(adm, creds);

        Context ctx1 = null;
        boolean failed_ctx1 = false;
        try {
            ctx1 = createContextNoQuota(ResellerFooCredentials());
        } catch (InvalidDataException e) {
            failed_ctx1 = true;
        }

        if (ctx1 != null) {
            deleteContext(ctx1, ResellerFooCredentials());
        }
        assertTrue("creation of ctx1 must fail", failed_ctx1);
    }

    @Test
    public void testCreateTooManyOverallUser() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.delete(FooAdminUser(), DummyMasterCredentials()); // Delete normaly created FooAdminUser

        ResellerAdmin adm = FooAdminUser();
        adm.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        oxresell.create(adm, creds);

        Context ctx1 = createContext(ResellerFooCredentials());
        Context ctx2 = createContext(ResellerFooCredentials());
        Context ctx3 = null;
        boolean failed_ctx3 = false;
        try {
            ctx3 = createContext(ResellerFooCredentials());
        } catch (StorageException e) {
            failed_ctx3 = true;
        }
        deleteContext(ctx1, ResellerFooCredentials());
        deleteContext(ctx2, ResellerFooCredentials());
        if (ctx3 != null) {
            deleteContext(ctx3, ResellerFooCredentials());
        }
        assertTrue("creation of ctx3 must fail", failed_ctx3);
    }

    @Test
    public void testListContextOwnedByReseller() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = DummyMasterCredentials();

        oxresell.create(BarAdminUser(), creds);

        ownedContext = createContext(ResellerFooCredentials());
        try {
            Context[] ret = oxctx.listAll(ResellerFooCredentials());
            assertEquals("listAll must return one entry", 1, ret.length);

            ret = oxctx.listAll(ResellerBarCredentials());
            assertEquals("listAll must return no entries", 0, ret.length);
        } finally {
            deleteContext(ownedContext, ResellerFooCredentials());
            ownedContext = null;
        }
    }

    @Test
    public void testGetDataContextOwnedByReseller() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        ownedContext = createContext(ResellerFooCredentials());
        try {
            boolean fail = false;
            try {
                oxctx.getData(ownedContext, ResellerBarCredentials());
            } catch (Exception e) {
                fail = true;
            }
            assertTrue("getData on an unowned context must fail", fail);

            Context ctx = oxctx.getData(ownedContext, ResellerFooCredentials());
            assertEquals("getData must return context with same id as ownedContext", ownedContext.getId(), ctx.getId());
        } finally {
            deleteContext(ownedContext, ResellerFooCredentials());
            ownedContext = null;
        }
    }

}
