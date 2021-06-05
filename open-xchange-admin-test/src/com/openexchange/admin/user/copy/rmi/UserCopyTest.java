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

package com.openexchange.admin.user.copy.rmi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.UserExistsException;
import com.openexchange.admin.rmi.factory.ContextFactory;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 * {@link UserCopyTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UserCopyTest extends AbstractRMITest {

    private Context srcCtx;
    private Context dstCtx;

    @Before
    public void setupContexts() throws Exception {
        srcCtx = getContextManager().create(ContextFactory.createContext(1000L), contextAdminCredentials);
        dstCtx = getContextManager().create(ContextFactory.createContext(1000L), contextAdminCredentials);
    }

    @Test
    public void testMoveUser() throws Exception {
        User srcUser = createUser(srcCtx);
        User copiedUser = getUserManager().copy(srcUser, srcCtx, dstCtx);

        assertNotNull(copiedUser);
    }

    @Test
    public void testMoveUserNoUser() throws Exception {
        try {
            getUserManager().copy(null, srcCtx, dstCtx);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("The given source user object is null; exceptionId"));
        }
    }

    @Test
    public void testMoveUserNoUserId() throws Exception {
        final User user = new User();
        try {
            getUserManager().copy(user, srcCtx, dstCtx);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("One userobject has no userid or username; exceptionId"));
        }
    }

    @Test
    public void testMoveUserNoSrcContext() throws Exception {
        final User user = new User(1);
        final Context src = null;
        try {
            getUserManager().copy(user, src, dstCtx);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("Client sent invalid source context data object; exceptionId"));
        }
    }

    @Test
    public void testMoveUserNoSrcContextId() throws Exception {
        final User user = new User(1);
        final Context src = new Context();
        try {
            getUserManager().copy(user, src, dstCtx);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("Client sent invalid source context data object; exceptionId"));
        }
    }

    @Test
    public void testMoveUserNoDestContext() throws Exception {
        final User user = new User(1);
        final Context dest = null;
        try {
            getUserManager().copy(user, srcCtx, dest);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("Client sent invalid destination context data object; exceptionId"));
        }
    }

    @Test
    public void testMoveUserNoDestContextId() throws Exception {
        final User user = new User(1);
        final Context dest = new Context();
        try {
            getUserManager().copy(user, srcCtx, dest);
            Assert.fail("No error message thrown");
        } catch (InvalidDataException e) {
            Assert.assertTrue(e.getMessage().startsWith("Client sent invalid destination context data object; exceptionId"));
        }
    }

    @Test
    public void testUserExists() throws Exception {
        final User srcUser = createUser(srcCtx);
        createUser(dstCtx);
        try {
            getUserManager().copy(srcUser, srcCtx, dstCtx);
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue("No UserExistsException thrown.", e instanceof UserExistsException);
        }
    }

    private User createUser(Context ctx) throws Exception {
        User user = UserFactory.createUser("user", contextAdminCredentials.getPassword(), "Test User", "Test", "User", "oxuser@example.com");
        user.setImapServer("example.com");
        user.setImapLogin("oxuser");
        user.setSmtpServer("example.com");
        getUserManager().create(ctx, user, contextAdminCredentials);
        return user;
    }
}
