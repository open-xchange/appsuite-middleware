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
import java.rmi.Naming;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXResellerContextTest extends OXResellerAbstractTest {

    private static Context ownedContext = null;

    private static OXResellerInterface oxresell = null;

    private ResellerAdmin randomAdmin;

    @Before
    public final void setUp() throws Exception {
        super.setUp();
        oxresell = (OXResellerInterface) Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        randomAdmin = RandomAdmin();

        oxresell.create(randomAdmin, superAdminCredentials);
    }

    @After
    public final void tearDown() throws Exception {
        final Credentials creds = superAdminCredentials;

        final ResellerAdmin[] adms = oxresell.list(randomAdmin.getName(), creds);
        for (final ResellerAdmin adm : adms) {
            oxresell.delete(adm, creds);
        }
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testListAllContextInvalidAuthNoUser() throws Exception {
        ResellerAdmin second = RandomAdmin();
        getContextManager().listAll(ResellerRandomCredentials(second.getName()));
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testListAllContextInvalidAuthWrongpasswd() throws Exception {
        Credentials creds = ResellerRandomCredentials(randomAdmin.getName());
        creds.setPassword("wrongpass");
        getContextManager().listAll(creds);
    }

    @Test
    public void testListAllContextValidAuth() throws Exception {
        Credentials creds = ResellerRandomCredentials(randomAdmin.getName());
        getContextManager().listAll(creds);
    }

    @Test
    public void testCreateTooManyContexts() throws Exception {
        oxresell.delete(randomAdmin, superAdminCredentials); // Delete normaly created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxContextRestriction() });
        oxresell.create(randomAdmin, superAdminCredentials);

        Credentials creds = ResellerRandomCredentials(randomAdmin.getName());
        Context ctx1 = createContext(creds);
        Context ctx2 = createContext(creds);
        Context ctx3 = null;
        boolean failed_ctx3 = false;
        try {
            ctx3 = createContext(creds);
        } catch (StorageException e) {
            failed_ctx3 = true;
        }

        deleteContext(ctx1, creds);
        deleteContext(ctx2, creds);
        if (ctx3 != null) {
            deleteContext(ctx3, creds);
        }

        assertTrue("creation of ctx3 must fail", failed_ctx3);

        oxresell.delete(randomAdmin, superAdminCredentials);
    }

    @Test
    public void testCreateContextNoQuota() throws Exception {
        final Credentials creds = superAdminCredentials;
        oxresell.delete(randomAdmin, creds); // Delete normaly created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        oxresell.create(randomAdmin, creds);

        Credentials contextAdmin = ResellerRandomCredentials(randomAdmin.getName());

        Context ctx1 = null;
        boolean failed_ctx1 = false;
        try {
            ctx1 = createContextNoQuota(contextAdmin);
        } catch (InvalidDataException e) {
            failed_ctx1 = true;
        }

        if (ctx1 != null) {
            deleteContext(ctx1, contextAdmin);
        }
        assertTrue("creation of ctx1 must fail", failed_ctx1);

        oxresell.delete(randomAdmin, superAdminCredentials);
    }

    @Test
    public void testCreateTooManyOverallUser() throws Exception {
        oxresell.delete(randomAdmin, superAdminCredentials); // Delete normaly created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        oxresell.create(randomAdmin, superAdminCredentials);

        Credentials resellerRandomCredentials = ResellerRandomCredentials(randomAdmin.getName());
        Context ctx1 = createContext(resellerRandomCredentials);
        Context ctx2 = createContext(resellerRandomCredentials);
        Context ctx3 = null;
        boolean failed_ctx3 = false;
        try {
            ctx3 = createContext(resellerRandomCredentials);
        } catch (StorageException e) {
            failed_ctx3 = true;
        }
        deleteContext(ctx1, resellerRandomCredentials);
        deleteContext(ctx2, resellerRandomCredentials);
        if (ctx3 != null) {
            deleteContext(ctx3, resellerRandomCredentials);
        }
        assertTrue("creation of ctx3 must fail", failed_ctx3);

        oxresell.delete(randomAdmin, superAdminCredentials);
    }

    @Test
    public void testListContextOwnedByReseller() throws Exception {
        ResellerAdmin second = RandomAdmin();

        oxresell.create(second, superAdminCredentials);

        Credentials resellerRandomCredentials = ResellerRandomCredentials(randomAdmin.getName());
        ownedContext = createContext(resellerRandomCredentials);
        try {
            Context[] ret = getContextManager().listAll(resellerRandomCredentials);
            assertEquals("listAll must return one entry", 1, ret.length);

            ret = getContextManager().listAll(ResellerRandomCredentials(second.getName()));
            assertEquals("listAll must return no entries", 0, ret.length);
        } finally {
            deleteContext(ownedContext, resellerRandomCredentials);
            ownedContext = null;
        }

        oxresell.delete(second, superAdminCredentials);

    }

    @Test
    public void testGetDataContextOwnedByReseller() throws Exception {
        Credentials resellerRandomCredentials = ResellerRandomCredentials(randomAdmin.getName());
        ownedContext = createContext(resellerRandomCredentials);
        try {
            boolean fail = false;
            try {
                ResellerAdmin second = RandomAdmin();
                Credentials secondCreds = ResellerRandomCredentials(second.getName());

                getContextManager().getData(ownedContext, secondCreds);
            } catch (Exception e) {
                fail = true;
            }
            assertTrue("getData on an unowned context must fail", fail);

            Context ctx = getContextManager().getData(ownedContext, resellerRandomCredentials);
            assertEquals("getData must return context with same id as ownedContext", ownedContext.getId(), ctx.getId());
        } finally {
            deleteContext(ownedContext, resellerRandomCredentials);
            ownedContext = null;
        }
    }

}
