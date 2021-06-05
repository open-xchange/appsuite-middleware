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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.factory.ContextFactory;
import com.openexchange.admin.rmi.factory.UserFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.PermissionCheckerCodes;

/**
 * {@link PermissionCapabilityTest} - tests that capabilities which are actually permissions cannot be provisioned
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class PermissionCapabilityTest extends AbstractRMITest {

    protected Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = getContextManager().create(ContextFactory.createContext(5000L), contextAdminCredentials);
    }

    /**
     * Tests that provisioning of illegal capabilities is not possible
     */
    @Test
    public void testProvisioningAnIllegalCapability() {
        // provisioning via user attributes
        User user = UserFactory.createUser(PermissionCapabilityTest.class.getSimpleName() +"_"+ System.currentTimeMillis(), "secret", TEST_DOMAIN, context);
        user.setUserAttribute("config", "com.openexchange.capability.infostore", Boolean.FALSE.toString());
        user.setUserAttribute("config", "com.openexchange.capability.contacts", Boolean.FALSE.toString());
        try {
            getUserManager().create(context, user, contextAdminCredentials);
            fail("Expecting an exception.");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidDataException);
            Throwable cause = e.getCause();
            assertTrue(cause instanceof OXException);
            OXException oxe = (OXException) cause;
            assertTrue(PermissionCheckerCodes.ILLEGAL_USER_ATTRIBUTE.equals(oxe));
            assertTrue(Arrays.asList(oxe.getDisplayArgs()).stream().filter((arg) -> arg instanceof String && ((String) arg).contains("infostore") && ((String) arg).contains("contacts")).findFirst().isPresent());
        }

        // provisioning via capabilities
        user = UserFactory.createUser(PermissionCapabilityTest.class.getSimpleName() +"_"+ System.currentTimeMillis(), "secret", TEST_DOMAIN, context);
        try {
            user = getUserManager().create(context, user, contextAdminCredentials);
            HashSet<String> set = new HashSet<>();
            set.add("infostore");
            set.add("contacts");
            getUserManager().change(context, user, set, Collections.emptySet(), Collections.emptySet(), contextAdminCredentials);
            fail("Expecting an exception.");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidDataException);
            Throwable cause = e.getCause();
            assertTrue(cause instanceof OXException);
            OXException oxe = (OXException) cause;
            assertTrue(PermissionCheckerCodes.ILLEGAL_CAPABILITY.equals(oxe));
            assertTrue(Arrays.asList(oxe.getDisplayArgs()).stream().filter((arg) -> arg instanceof String && ((String) arg).contains("infostore") && ((String) arg).contains("contacts")).findFirst().isPresent());
        }

        // removal of existing illegal capabilities must be possible
        user = UserFactory.createUser(PermissionCapabilityTest.class.getSimpleName() +"_"+ System.currentTimeMillis(), "secret", TEST_DOMAIN, context);
        try {
            user = getUserManager().create(context, user, contextAdminCredentials);
            HashSet<String> set = new HashSet<>();
            set.add("infostore");
            set.add("contacts");
            getUserManager().change(context, user, Collections.emptySet(), Collections.emptySet(), set, contextAdminCredentials);
        } catch (@SuppressWarnings("unused") Exception e) {
            fail("Expecting no exception.");
        }
    }

}
