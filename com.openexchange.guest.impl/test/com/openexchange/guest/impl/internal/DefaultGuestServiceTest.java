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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.impl.storage.GuestStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 *
 * {@link DefaultGuestServiceTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GuestStorage.class, GuestStorageServiceLookup.class, Databases.class })
public class DefaultGuestServiceTest {

    private static final String GUEST_MAIL_ADDRESS = "hotte@example.com";
    private static final String GUEST_PASSWORD = "myToppiPasswordi";
    private static final String GUEST_PASSWORD_MECH = "{BCRYPT}";
    private static final String GROUP_ID = "default";
    private static final long GUEST_ID = 77;
    private static final int CONTEXT_ID = 1;
    private static final int USER_ID = 11;

    private DefaultGuestService defaultGuestService;

    @Mock
    private UserService userService;

    @Mock
    private ConfigViewFactory configViewFactory;

    @Mock
    private ConfigView configView;

    @Mock
    private ContactUserStorage contactUserStorage;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private GuestStorage guestStorage;

    @Mock
    private ServiceLookup services;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(GuestStorage.class);
        PowerMockito.when(GuestStorage.getInstance()).thenReturn(guestStorage);
        //        Mockito.when(guestStorage.getGuestId(Matchers.anyString(), (Connection) Matchers.any())).thenReturn(GUEST_ID);
        //        Mockito.when(guestStorage.getGuestId(Matchers.anyInt(), Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(GUEST_ID);

        PowerMockito.mockStatic(GuestStorageServiceLookup.class);
        PowerMockito.when(GuestStorageServiceLookup.get()).thenReturn(services);

        Mockito.when(services.getService(DatabaseService.class)).thenReturn(databaseService);
        Mockito.when(databaseService.getWritableForGlobal(Matchers.anyInt())).thenReturn(connection);
        Mockito.when(databaseService.getReadOnlyForGlobal(Matchers.anyInt())).thenReturn(connection);
        Mockito.when(databaseService.getWritableForGlobal(Matchers.anyString())).thenReturn(connection);
        Mockito.when(databaseService.getReadOnlyForGlobal(Matchers.anyString())).thenReturn(connection);

        Mockito.when(configViewFactory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(configView);

        Mockito.when(userService.getUser(Matchers.anyInt(), Matchers.anyInt())).thenReturn(new UserImpl());

        PowerMockito.mockStatic(Databases.class);
        PowerMockito.doNothing().when(Databases.class, "startTransaction", (Connection) Matchers.any());

        this.defaultGuestService = new DefaultGuestService(userService, contactUserStorage, configViewFactory);
    }

    @Test
    public void testAddGuest_alreadyExistingGuestAndAssignment_doNotAdd() throws OXException {
        Mockito.when(guestStorage.isAssignmentExisting(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(true);

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH);

        Mockito.verify(guestStorage, Mockito.never()).addGuestAssignment((GuestAssignment) Matchers.any(), (Connection) Matchers.any());
        Mockito.verify(guestStorage, Mockito.never()).addGuest(Matchers.anyString(), Matchers.anyString(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testAddGuest_alreadyExistingGuest_addAssignment() throws OXException {
        Mockito.when(guestStorage.isAssignmentExisting(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(false);

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH);

        Mockito.verify(guestStorage, Mockito.times(1)).addGuestAssignment((GuestAssignment) Matchers.any(), (Connection) Matchers.any());
        Mockito.verify(guestStorage, Mockito.never()).addGuest(Matchers.anyString(), Matchers.anyString(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testAddGuest_alreadyExistingGuest_addCompleteNewGuest() throws OXException {
        Mockito.when(guestStorage.isAssignmentExisting(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(false);
        Mockito.when(guestStorage.getGuestId(Matchers.anyString(), Matchers.anyString(), (Connection) Matchers.any())).thenReturn(GuestStorage.NOT_FOUND);
        Mockito.when(guestStorage.addGuest(Matchers.anyString(), Matchers.anyString(), (Connection) Matchers.any())).thenReturn(GUEST_ID);
        Mockito.when(guestStorage.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection)).thenReturn(GUEST_ID);

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH);

        Mockito.verify(guestStorage, Mockito.times(1)).addGuestAssignment((GuestAssignment) Matchers.any(), (Connection) Matchers.any());
        Mockito.verify(guestStorage, Mockito.times(1)).addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection);
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testRemoveGuest_guestNotFound_doNothing() throws OXException {
        Mockito.when(guestStorage.getGuestId(Matchers.anyString(), Matchers.anyString(), (Connection) Matchers.any())).thenReturn(GuestStorage.NOT_FOUND);

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuestAssignment(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(guestStorage, Mockito.never()).removeGuest(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testRemoveGuest_assignmentStillExisting_doNotDeleteUser() throws OXException {
        Mockito.when(guestStorage.getNumberOfAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(10L);

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.times(1)).removeGuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, connection);
        Mockito.verify(guestStorage, Mockito.never()).removeGuest(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testRemoveGuest_noAssignment_deleteUser() throws OXException {
        Mockito.when(guestStorage.getNumberOfAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(0L);

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.times(1)).removeGuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, connection);
        Mockito.verify(guestStorage, Mockito.times(1)).removeGuest(GUEST_ID, connection);
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testremoveGuestAssignments_assignmentRemovedButAssignmentStillAvailable_doNotRemoveGuest() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        removedGuests.add(22L);
        removedGuests.add(44L);
        removedGuests.add(66L);
        Mockito.when(guestStorage.resolveGuestAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(removedGuests);
        Mockito.when(guestStorage.getNumberOfAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(2L);

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuest(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testremoveGuestAssignments_assignmentRemovedButAssignmentNotAvailable_RemoveGuest() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        removedGuests.add(22L);
        removedGuests.add(44L);
        removedGuests.add(66L);
        Mockito.when(guestStorage.resolveGuestAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(removedGuests);
        Mockito.when(guestStorage.getNumberOfAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(0L);

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.times(removedGuests.size())).removeGuest(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }

    @Test
    public void testremoveGuestAssignments_noGuestRemoved() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        Mockito.when(guestStorage.resolveGuestAssignments(Matchers.anyInt(), (Connection) Matchers.any())).thenReturn(removedGuests);

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuest(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(guestStorage, Mockito.never()).getNumberOfAssignments(Matchers.anyInt(), (Connection) Matchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritable(connection);
    }
}
