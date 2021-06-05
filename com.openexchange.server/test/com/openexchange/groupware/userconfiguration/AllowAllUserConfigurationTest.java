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

package com.openexchange.groupware.userconfiguration;

import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.contexts.impl.ContextImpl;

/**
 * The {@link AllowAllUserConfiguration} should give the user every possible permission. This tests verifies that.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@SuppressWarnings("deprecation")
public class AllowAllUserConfigurationTest {

    private UserPermissionBits userPermission;

    public AllowAllUserConfigurationTest() {
        super();
    }

    @Before
    public void setUp() {
        userPermission = new AllowAllUserConfiguration(1, new int[0], new ContextImpl(1)).getUserPermissionBits();
    }

    @After
    public void tearDown() {
        userPermission = null;
    }

    @Test
    public void testHasWebMail() {
        assertTrue(userPermission.hasWebMail());
    }

    @Test
    public void testHasCalendar() {
        assertTrue(userPermission.hasCalendar());
    }

    @Test
    public void testHasContact() {
        assertTrue(userPermission.hasContact());
    }

    @Test
    public void testHasTask() {
        assertTrue(userPermission.hasTask());
    }

    @Test
    public void testHasInfostore() {
        assertTrue(userPermission.hasInfostore());
    }

    @Test
    public void testHasWebDAVXML() {
        assertTrue(userPermission.hasWebDAVXML());
    }

    @Test
    public void testHasWebDAV() {
        assertTrue(userPermission.hasWebDAV());
    }

    @Test
    public void testHasICal() {
        assertTrue(userPermission.hasICal());
    }

    @Test
    public void testHasVCard() {
        assertTrue(userPermission.hasVCard());
    }

    @Test
    public void testHasSyncML() {
        assertTrue(userPermission.hasSyncML());
    }

    @Test
    public void testHasPIM() {
        assertTrue(userPermission.hasPIM());
    }

    @Test
    public void testHasTeamView() {
        assertTrue(userPermission.hasTeamView());
    }

    @Test
    public void testHasFreeBusy() {
        assertTrue(userPermission.hasFreeBusy());
    }

    @Test
    public void testHasConflictHandling() {
        assertTrue(userPermission.hasConflictHandling());
    }

    @Test
    public void testHasParticipantsDialog() {
        assertTrue(userPermission.hasParticipantsDialog());
    }

    @Test
    public void testHasGroupware() {
        assertTrue(userPermission.hasGroupware());
    }

    @Test
    public void testHasPortal() {
        assertTrue(userPermission.hasPortal());
    }

    @Test
    public void testHasFullPublicFolderAccess() {
        assertTrue(userPermission.hasFullPublicFolderAccess());
    }

    @Test
    public void testHasFullSharedFolderAccess() {
        assertTrue(userPermission.hasFullSharedFolderAccess());
    }

    @Test
    public void testCanDelegateTasks() {
        assertTrue(userPermission.canDelegateTasks());
    }

    @Test
    public void testIsCollectEmailAddresses() {
        assertTrue(userPermission.isCollectEmailAddresses());
    }

    @Test
    public void testIsMultipleMailAccounts() {
        assertTrue(userPermission.isMultipleMailAccounts());
    }

    @Test
    public void testIsSubscription() {
        assertTrue(userPermission.isSubscription());
    }

    @Test
    public void testHasActiveSync() {
        assertTrue(userPermission.hasActiveSync());
    }

    @Test
    public void testHasUSM() {
        assertTrue(userPermission.hasUSM());
    }

    @Test
    public void testHasOLOX20() {
        assertTrue(userPermission.hasOLOX20());
    }

    @Test
    public void testIsEditGroup() {
        assertTrue(userPermission.isEditGroup());
    }

    @Test
    public void testIsEditResource() {
        assertTrue(userPermission.isEditResource());
    }

    @Test
    public void testIsEditPassword() {
        assertTrue(userPermission.isEditPassword());
    }
}
