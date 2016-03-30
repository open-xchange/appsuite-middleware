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

import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Stack;
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
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;


public class OXResellerUserTest extends OXResellerAbstractTest {

    private static OXResellerInterface oxresell = null;

    private static OXContextInterface oxctx = null;

    @BeforeClass
    public static void startup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        oxctx = (OXContextInterface)Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
        oxresell.initDatabaseRestrictions(creds);
    }

    @AfterClass
    public static void cleanup() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchContextException, DatabaseUpdateException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        oxresell.removeDatabaseRestrictions(creds);
    }

    @Test
    public void testCreateTooManyOverallUser() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = DummyMasterCredentials();

        ResellerAdmin adm = FooAdminUser();
        adm.setRestrictions(new Restriction[]{MaxOverallUserRestriction(6)});
        oxresell.create(adm, creds);
        try {
            Stack<Context> ctxstack = new Stack<Context>();
            try {
                // create 3 contexts with 1 user -> 6 user total
                for(final Context ctx : new Context[]{createContext(ResellerFooCredentials()),
                        createContext(ResellerFooCredentials()), createContext(ResellerFooCredentials())} ){
                    ctxstack.push(ctx);
                    Credentials ctxauth = new Credentials(ContextAdmin().getName(),ContextAdmin().getPassword());
                    for(int i=1; i<2; i++) {
                        System.out.println("creating user " + i + " in Context " + ctx.getId());
                        createUser(ctx, ctxauth);
                    }
                }

                // 7th user must fail
                boolean createFailed = false;
                try {
                    createUser(ctxstack.firstElement(), new Credentials(ContextAdmin().getName(),ContextAdmin().getPassword()));
                } catch (StorageException e) {
                    createFailed = true;
                }
                assertTrue("Create user must fail",createFailed);
            } finally {
                for(final Context ctx : ctxstack ){
                    deleteContext(ctx, ResellerFooCredentials());
                }
            }
        } finally {
            oxresell.delete(FooAdminUser(), DummyMasterCredentials());
        }
    }

    @Test
    public void testCreateTooManyPerContextUser() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = ResellerFooCredentials();

        ResellerAdmin adm = FooAdminUser();
        oxresell.create(adm, DummyMasterCredentials());
        try {
            Context ctx = createContext(creds);
            try {
                try {
                    ctx.addExtension(new OXContextExtensionImpl(new Restriction[]{MaxUserPerContextRestriction()}));
                } catch (final DuplicateExtensionException e1) {
                    // Because the context is newly created this exception cannot occur
                    e1.printStackTrace();
                }
                // TODO Here we call change context to apply the restrictions if the create call is ready to handle extensions
                // this can be done directly with the create call
                oxctx.change(ctx, creds);

                User oxadmin = ContextAdmin();
                Credentials ctxadmcreds = new Credentials(oxadmin.getName(), oxadmin.getPassword());
                createUser(ctx, ctxadmcreds);
                createUser(ctx, ctxadmcreds);

                // 3rd user must fail
                boolean createFailed = false;
                try {
                    createUser(ctx, ctxadmcreds);
                } catch (StorageException e) {
                    createFailed = true;
                }
                assertTrue("Create user must fail",createFailed);
            } finally {
                deleteContext(ctx, creds);
            }
        } finally {
            oxresell.delete(FooAdminUser(), DummyMasterCredentials());
        }
    }

    /*
     * NOTE: this test must be changed, if /opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties
     * will be changed!
     */
    @Test
    public void testCreateTooManyPerContextUserByModuleAccess() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final Credentials creds = ResellerFooCredentials();

        ResellerAdmin adm = FooAdminUser();
        oxresell.create(adm, DummyMasterCredentials());
        try {
            Context ctx = createContext(creds);
            try {
                try {
                    ctx.addExtension(new OXContextExtensionImpl(new Restriction[]{
                        new Restriction(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX+"webmail_plus","2"),
                        new Restriction(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX+"premium","2")
                    }));
                } catch (DuplicateExtensionException e1) {
                    // Because the context is newly created this exception cannot occur
                    e1.printStackTrace();
                }
                // TODO Here we call change context to apply the restrictions if the create call is ready to handle extensions
                // this can be done directly with the create call
                oxctx.change(ctx, creds);

                try {
                    Thread.sleep(500);
                    // Short sleep so the context is fully ready.
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                // webmail test (default perms)
                User oxadmin = ContextAdmin();
                Credentials ctxadmcreds = new Credentials(oxadmin.getName(), oxadmin.getPassword());
                createUser(ctx, ctxadmcreds);

                // 3rd user must fail
                boolean createFailed = false;
                try {
                    createUser(ctx, ctxadmcreds);
                } catch (StorageException e) {
                    createFailed = true;
                }
                assertTrue("Create user must fail",createFailed);

                // premium test
                // premium=contacts,webmail,calendar,delegatetask,tasks,editpublicfolders,infostore,
                // readcreatesharedfolders,ical,vcard,webdav,webdavxml
                final UserModuleAccess access = new UserModuleAccess();
                access.disableAll();
                access.setContacts(true);
                access.setWebmail(true);
                access.setCalendar(true);
                access.setDelegateTask(true);
                access.setTasks(true);
                access.setEditPublicFolders(true);
                access.setInfostore(true);
                access.setReadCreateSharedFolders(true);
                access.setIcal(true);
                access.setVcard(true);
                access.setWebdav(true);
                access.setWebdavXml(true);
                access.setGlobalAddressBookDisabled(false);

                createUser(ctx, access, ctxadmcreds);
                createUser(ctx, access, ctxadmcreds);

                // 3rd user must fail
                createFailed = false;
                try {
                    createUser(ctx, access, ctxadmcreds);
                } catch (StorageException e) {
                    createFailed = true;
                }
                assertTrue("Create user must fail",createFailed);
            } finally {
                deleteContext(ctx, creds);
            }
        } finally {
            oxresell.delete(FooAdminUser(), DummyMasterCredentials());
        }
    }
}
