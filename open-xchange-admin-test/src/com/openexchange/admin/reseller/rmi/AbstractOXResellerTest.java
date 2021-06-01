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

import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 * {@link AbstractOXResellerTest}
 *
 * @author choeger
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractOXResellerTest extends AbstractRMITest {

    public static final String TESTUSER = "testuser";
    protected static final String TESTCHANGEUSER = "testchange";
    protected static final String CHANGEDNAME = "testchangedchangedagain";
    protected static final String TESTRESTRICTIONUSER = "testwithrestriction";
    protected static final String TESTRESTCHANGERICTIONUSER = "testchangewithrestriction";

    /**
     * Initialises a new {@link AbstractOXResellerTest}.
     */
    public AbstractOXResellerTest() {
        super();
    }

    protected static Credentials ResellerRandomCredentials(String user) {
        return new Credentials(user, "secret");
    }

    protected static Credentials TestUserCredentials() {
        return new Credentials(TESTUSER, "secret");
    }

    protected static Restriction MaxContextRestriction() {
        return new Restriction(Restriction.MAX_CONTEXT_PER_SUBADMIN, "2");
    }

    protected static Restriction MaxContextQuotaRestriction() {
        return new Restriction(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN, "1000");
    }

    protected static Restriction MaxUserPerContextRestriction() {
        return new Restriction(Restriction.MAX_USER_PER_CONTEXT, "3");
    }

    protected static Restriction MaxOverallUserRestriction(int count) {
        return new Restriction(Restriction.MAX_OVERALL_USER_PER_SUBADMIN, new Integer(count).toString());
    }
    
    protected static Restriction CanCreateSubAdmin() {
        return new Restriction(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS, "true");
    }

    protected static Context createContext(final Credentials auth) throws Exception {
        User oxadmin = UserFactory.createContextAdmin();
        Context ctx = new Context();
        ctx.setMaxQuota(100000L);

        Context create = getContextManager().create(ctx, oxadmin, auth);
        try {
            // wait to ensure the context is available for further operations
            // FIXME when master-slave setup for configdb is available remove the line below
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            // should not happen
        }
        return create;
    }

    protected static Context createContextNoQuota(final Credentials auth) throws Exception {
        User oxadmin = UserFactory.createContextAdmin();
        Context ctx = new Context();

        Context create = getContextManager().create(ctx, oxadmin, auth);
        try {
            // wait to ensure the context is available for further operations
            // FIXME when master-slave setup for configdb is available remove the line below
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            // should not happen
        }

        return create;
    }

    protected static User createUser(final Context ctx, final Credentials auth) throws Exception {
        return createUser(ctx, null, auth);
    }

    protected static User createUser(final Context ctx, final UserModuleAccess access, final Credentials auth) throws Exception {
        final String random = new Long(System.currentTimeMillis()).toString();
        User oxuser = new User();
        oxuser.setName("oxuser" + random);
        oxuser.setDisplay_name("oxuser" + random);
        oxuser.setGiven_name("oxuser" + random);
        oxuser.setSur_name("oxuser" + random);
        oxuser.setPrimaryEmail("oxuser" + random + "@example.com");
        oxuser.setEmail1("oxuser" + random + "@example.com");
        oxuser.setPassword("secret");
        if (access == null) {
            return getUserManager().create(ctx, oxuser, auth);
        }
        return getUserManager().create(ctx, oxuser, access, auth);
    }

    protected static void deleteContext(final Context ctx, final Credentials auth) throws Exception {
        getContextManager().delete(ctx, auth);
    }

    protected static Restriction getRestrictionByName(final String name, final Restriction[] res) {
        for (final Restriction r : res) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }
}
