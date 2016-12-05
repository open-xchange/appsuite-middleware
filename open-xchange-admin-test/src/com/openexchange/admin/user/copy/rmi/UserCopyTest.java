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

package com.openexchange.admin.user.copy.rmi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.rmi.RemoteException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.UserExistsException;
import com.openexchange.configuration.AJAXConfig;

public class UserCopyTest extends AbstractRMITest {

    private OXContextInterface ci;

    private OXUserInterface ui;

    private Context srcCtx;

    private Context dstCtx;

    private User admin;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        ci = getContextInterface();
        ui = getUserInterface();
    }

    @Before
    public final void setupContexts() throws Exception {
        admin = newUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@example.com");
        srcCtx = TestTool.createContext(ci, "UserCopySourceCtx_", admin, "all", superAdminCredentials);
        dstCtx = TestTool.createContext(ci, "UserCopyDestinationCtx_", admin, "all", superAdminCredentials);
    }

    @After
    public final void tearDownContexts() throws Exception {
        if (srcCtx != null) {
            ci.delete(srcCtx, superAdminCredentials);
        }
        if (dstCtx != null) {
            ci.delete(dstCtx, superAdminCredentials);
        }
    }

    @Test
    public final void testMoveUser() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        User srcUser = createUser(srcCtx);
        oxu.copyUser(srcUser, srcCtx, dstCtx, superAdminCredentials);
    }

    @Test
    public final void testMoveUserNoUser() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        try {
            oxu.copyUser(null, srcCtx, dstCtx, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("The given source user object is null", e.getMessage());
        }
    }

    @Test
    public final void testMoveUserNoUserId() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User user = new User();
        try {
            oxu.copyUser(user, srcCtx, dstCtx, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("One userobject has no userid or username", e.getMessage());
        }
    }

    @Test
    public final void testMoveUserNoSrcContext() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User user = new User(1);
        final Context src = null;
        try {
            oxu.copyUser(user, src, dstCtx, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid source context data object", e.getMessage());
        }
    }

    @Test
    public final void testMoveUserNoSrcContextId() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User user = new User(1);
        final Context src = new Context();
        try {
            oxu.copyUser(user, src, dstCtx, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid source context data object", e.getMessage());
        }
    }

    @Test
    public final void testMoveUserNoDestContext() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User user = new User(1);
        final Context dest = null;
        try {
            oxu.copyUser(user, srcCtx, dest, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid destination context data object", e.getMessage());
        }
    }

    @Test
    public final void testMoveUserNoDestContextId() throws Throwable {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User user = new User(1);
        final Context dest = new Context();
        try {
            oxu.copyUser(user, srcCtx, dest, superAdminCredentials);
            Assert.fail("No error message thrown");
        } catch (final InvalidDataException e) {
            Assert.assertEquals("Client sent invalid destination context data object", e.getMessage());
        }
    }

    @Test
    public final void testUserExists() throws Exception {
        final OXUserCopyInterface oxu = getUserCopyClient();
        final User srcUser = createUser(srcCtx);
        final User dstUser = createUser(dstCtx);
        try {
            oxu.copyUser(srcUser, srcCtx, dstCtx, superAdminCredentials);
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue("No UserExistsException thrown.", e instanceof UserExistsException);

        }
    }

    private User createUser(Context ctx) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        User user = newUser("user", "secret", "Test User", "Test", "User", "oxuser@example.com");
        user.setImapServer("example.com");
        user.setImapLogin("oxuser");
        user.setSmtpServer("example.com");
        ui.create(ctx, user, getCredentials());
        return user;
    }
}
