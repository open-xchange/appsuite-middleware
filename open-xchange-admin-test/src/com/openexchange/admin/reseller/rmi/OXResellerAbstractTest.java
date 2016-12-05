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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.AbstractTest;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 *
 */
public abstract class OXResellerAbstractTest extends AbstractTest {
    protected static final String TESTUSER = "testuser";
    protected static final String TESTCHANGEUSER = "testchange";
    protected static final String CHANGEDNAME = "changedchangedagain";
    protected static final String TESTRESTRICTIONUSER = "testwithrestriction";
    protected static final String TESTRESTCHANGERICTIONUSER = "testchangewithrestriction";

    protected static Credentials ResellerFooCredentials() {
        return new Credentials("foo", "secret");
    }

    protected static Credentials TestUserCredentials() {
        return new Credentials(TESTUSER, "secret");
    }

    protected static Credentials ResellerBarCredentials() {
        return new Credentials("bar", "secret");
    }

    protected static ResellerAdmin FooAdminUser() {
        ResellerAdmin adm = TestAdminUser("foo", "Foo Admin");
        adm.setPassword("secret");
        return adm;
    }

    protected static ResellerAdmin BarAdminUser() {
        ResellerAdmin adm = TestAdminUser("bar", "Bar Admin");
        adm.setPassword("secret");
        return adm;
    }

    protected static ResellerAdmin TestAdminUser() {
        return TestAdminUser(TESTUSER, "Test Reseller Admin");
    }

    protected static ResellerAdmin TestAdminUser(final String name) {
        return TestAdminUser(name, "Test Display Name");
    }

    protected static ResellerAdmin TestAdminUser(final String name, final String displayname) {
        ResellerAdmin adm = new ResellerAdmin(name);
        adm.setDisplayname(displayname);
        adm.setPassword("secret");
        return adm;
    }

    protected static Restriction MaxContextRestriction() {
        return new Restriction(Restriction.MAX_CONTEXT_PER_SUBADMIN,"2");
    }

    protected static Restriction MaxContextQuotaRestriction() {
        return new Restriction(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN,"1000");
    }

    protected static Restriction MaxUserPerContextRestriction() {
        return new Restriction(Restriction.MAX_USER_PER_CONTEXT,"3");
    }

    protected static Restriction MaxOverallUserRestriction(int count) {
        return new Restriction(Restriction.MAX_OVERALL_USER_PER_SUBADMIN,new Integer(count).toString());
    }

    protected static User ContextAdmin() {
        User oxadmin = new User();
        oxadmin.setName("oxadmin");
        oxadmin.setDisplay_name("oxadmin");
        oxadmin.setGiven_name("oxadmin");
        oxadmin.setSur_name("oxadmin");
        oxadmin.setPrimaryEmail("oxadmin@example.com");
        oxadmin.setEmail1("oxadmin@example.com");
        oxadmin.setPassword("secret");
        return oxadmin;
    }

    protected static Context createContext(final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        final OXContextInterface oxctx = (OXContextInterface)Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);

        User oxadmin = ContextAdmin();
        Context ctx = new Context();
        ctx.setMaxQuota(100000L);

        Context create = oxctx.create(ctx, oxadmin, auth);
        try {
            // wait to ensure the context is available for further operations
            // FIXME when master-slave setup for configdb is available remove the line below
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            // should not happen
        }
        return create;
    }

    protected static Context createContextNoQuota(final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        final OXContextInterface oxctx = (OXContextInterface)Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);

        User oxadmin = ContextAdmin();
        Context ctx = new Context();

        Context create = oxctx.create(ctx, oxadmin, auth);
        try {
            // wait to ensure the context is available for further operations
            // FIXME when master-slave setup for configdb is available remove the line below
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            // should not happen
        }

        return create;
    }

    protected static User createUser(final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        return createUser(ctx, null, auth);
    }

    protected static User createUser(final Context ctx, final UserModuleAccess access, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        final OXUserInterface oxusr = (OXUserInterface)Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);

        final String random = new Long(System.currentTimeMillis()).toString();
        User oxuser = new User();
        oxuser.setName("oxuser"+random);
        oxuser.setDisplay_name("oxuser"+random);
        oxuser.setGiven_name("oxuser"+random);
        oxuser.setSur_name("oxuser"+random);
        oxuser.setPrimaryEmail("oxuser"+random+"@example.com");
        oxuser.setEmail1("oxuser"+random+"@example.com");
        oxuser.setPassword("secret");
        if( access == null ) {
            return oxusr.create(ctx, oxuser, auth);
        }
        return oxusr.create(ctx, oxuser, access, auth);
    }

    protected static void deleteContext(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, MalformedURLException, NotBoundException {
        final OXContextInterface oxctx = (OXContextInterface)Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
        oxctx.delete(ctx, auth);
    }

    protected static Restriction getRestrictionByName(final String name, final Restriction[] res) {
        for(final Restriction r : res) {
            if( r.getName().equals(name) ) {
                return r;
            }
        }
        return null;
    }
}
