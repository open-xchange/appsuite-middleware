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
    public void testIsPublication() {
        assertTrue(userPermission.isPublication());
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
