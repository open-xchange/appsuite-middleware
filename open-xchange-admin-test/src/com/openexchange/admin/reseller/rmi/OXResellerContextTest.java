/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.reseller.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import com.openexchange.admin.rmi.factory.ResellerAdminFactory;

public class OXResellerContextTest extends AbstractOXResellerTest {

    private static Context ownedContext = null;

    private ResellerAdmin randomAdmin;

    /**
     * Initialises a new {@link OXResellerContextTest}.
     */
    public OXResellerContextTest() {
        super();
    }

    @Before
    public final void setUp() throws Exception {
        super.setUp();
        randomAdmin = ResellerAdminFactory.createRandomResellerAdmin();
        getResellerManager().create(randomAdmin);
    }

    @After
    public final void tearDown() throws Exception {
        final ResellerAdmin[] adms = getResellerManager().search(randomAdmin.getName());
        for (final ResellerAdmin adm : adms) {
            getResellerManager().delete(adm);
        }
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testListAllContextInvalidAuthNoUser() throws Exception {
        ResellerAdmin second = ResellerAdminFactory.createRandomResellerAdmin();
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
        getResellerManager().delete(randomAdmin); // Delete normally created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxContextRestriction() });
        getResellerManager().create(randomAdmin);

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

        getResellerManager().delete(randomAdmin);
    }

    @Test
    public void testCreateContextNoQuota() throws Exception {
        getResellerManager().delete(randomAdmin); // Delete normally created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        getResellerManager().create(randomAdmin);

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

        getResellerManager().delete(randomAdmin);
    }

    @Test
    public void testCreateTooManyOverallUser() throws Exception {
        getResellerManager().delete(randomAdmin); // Delete normally created FooAdminUser

        randomAdmin.setRestrictions(new Restriction[] { MaxOverallUserRestriction(2) });
        getResellerManager().create(randomAdmin);

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

        getResellerManager().delete(randomAdmin);
    }

    @Test
    public void testListContextOwnedByReseller() throws Exception {
        ResellerAdmin second = ResellerAdminFactory.createRandomResellerAdmin();

        getResellerManager().create(second);

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

        getResellerManager().delete(second);

    }

    @Test
    public void testGetDataContextOwnedByReseller() throws Exception {
        Credentials resellerRandomCredentials = ResellerRandomCredentials(randomAdmin.getName());
        ownedContext = createContext(resellerRandomCredentials);
        try {
            boolean fail = false;
            try {
                ResellerAdmin second = ResellerAdminFactory.createRandomResellerAdmin();
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
