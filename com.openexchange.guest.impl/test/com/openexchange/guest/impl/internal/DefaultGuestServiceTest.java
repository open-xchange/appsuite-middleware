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

package com.openexchange.guest.impl.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.impl.storage.GuestStorage;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 *
 * {@link DefaultGuestServiceTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@SuppressWarnings("synthetic-access")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GuestStorage.class, GuestStorageServiceLookup.class, Databases.class })
public class DefaultGuestServiceTest {

    private static final String GUEST_MAIL_ADDRESS = "hotte@example.com";
    private static final String GUEST_PASSWORD = "myToppiPasswordi";
    private static final String GUEST_PASSWORD_MECH = "{BCRYPT}";
    private static final byte[] GUEST_PASSWORD_SALT = "theSalt".getBytes();
    private static final String GROUP_ID = "default";
    private static final long GUEST_ID = 77;
    private static final int CONTEXT_ID = 1;
    private static final int USER_ID = 11;

    private DefaultGuestService defaultGuestService;

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

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
    private PasswordMechRegistry passwordMechFactory;

    @Mock
    private ServiceLookup services;

    private UserImpl user;

    private Contact contact;

    private final List<GuestAssignment> assignments = new ArrayList<GuestAssignment>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(GuestStorage.class);
        PowerMockito.when(GuestStorage.getInstance()).thenReturn(guestStorage);
        Mockito.when(L(guestStorage.getGuestId(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any(Connection.class)))).thenReturn(L(GUEST_ID));
        Mockito.when(L(guestStorage.getGuestId(ArgumentMatchers.isNull(), ArgumentMatchers.anyString(), ArgumentMatchers.any(Connection.class)))).thenReturn(L(GUEST_ID));

        PowerMockito.mockStatic(GuestStorageServiceLookup.class);
        PowerMockito.when(GuestStorageServiceLookup.get()).thenReturn(services);

        Mockito.when(services.getService(DatabaseService.class)).thenReturn(databaseService);
        Mockito.when(databaseService.getWritableForGlobal(ArgumentMatchers.anyInt())).thenReturn(connection);
        Mockito.when(databaseService.getReadOnlyForGlobal(ArgumentMatchers.anyInt())).thenReturn(connection);
        Mockito.when(databaseService.getWritableForGlobal(ArgumentMatchers.anyString())).thenReturn(connection);
        Mockito.when(databaseService.getReadOnlyForGlobal(ArgumentMatchers.anyString())).thenReturn(connection);
        Mockito.when(databaseService.getWritable(ArgumentMatchers.anyInt())).thenReturn(connection);
        Mockito.when(databaseService.getReadOnly(ArgumentMatchers.anyInt())).thenReturn(connection);

        Mockito.when(configViewFactory.getView(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(configView);
        Mockito.when(configView.opt(ArgumentMatchers.anyString(), ArgumentMatchers.<Class<String>> any(), ArgumentMatchers.<String> any())).thenReturn("default");

        Mockito.when(userService.getUser(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(new UserImpl());
        Mockito.when(userService.getUser(ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any())).thenReturn(new UserImpl());

        PowerMockito.mockStatic(Databases.class);
        PowerMockito.doNothing().when(Databases.class, "startTransaction", (Connection) ArgumentMatchers.any());

        user = new UserImpl();
        user.setId((int) GUEST_ID);
        user.setMail(GUEST_MAIL_ADDRESS);
        user.setUserPassword(GUEST_PASSWORD);
        user.setPasswordMech(GUEST_PASSWORD_MECH);
        user.setSalt(GUEST_PASSWORD_SALT);

        contact = new Contact();
        contact.setInternalUserId((int) GUEST_ID);

        assignments.add(new GuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH, GUEST_PASSWORD_SALT));
        assignments.add(new GuestAssignment(111, 11, 1, "pwd", "pwdMech", "salt".getBytes()));

        this.defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory);
    }

    @Test
    public void testAddGuest_alreadyExistingGuestAndAssignment_doNotAdd() throws OXException {
        Mockito.when(B(guestStorage.isAssignmentExisting(ArgumentMatchers.anyLong(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any()))).thenReturn(B(true));

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH, GUEST_PASSWORD_SALT);

        Mockito.verify(guestStorage, Mockito.never()).addGuestAssignment(ArgumentMatchers.any(GuestAssignment.class), ArgumentMatchers.any(Connection.class));
        Mockito.verify(guestStorage, Mockito.never()).addGuest(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testAddGuest_alreadyExistingGuest_addAssignment() throws OXException {
        Mockito.when(B(guestStorage.isAssignmentExisting(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any()))).thenReturn(B(false));

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH, GUEST_PASSWORD_SALT);

        Mockito.verify(guestStorage, Mockito.times(1)).addGuestAssignment((GuestAssignment) ArgumentMatchers.any(), (Connection) ArgumentMatchers.any());
        Mockito.verify(guestStorage, Mockito.never()).addGuest(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testAddGuest_alreadyExistingGuest_addCompleteNewGuest() throws OXException {
        Mockito.when(B(guestStorage.isAssignmentExisting(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any()))).thenReturn(B(false));
        Mockito.when(L(guestStorage.getGuestId(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any()))).thenReturn(L(GuestStorage.NOT_FOUND));
        Mockito.when(L(guestStorage.addGuest(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any()))).thenReturn(L(GUEST_ID));
        Mockito.when(L(guestStorage.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection))).thenReturn(L(GUEST_ID));

        defaultGuestService.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH, GUEST_PASSWORD_SALT);

        Mockito.verify(guestStorage, Mockito.times(1)).addGuestAssignment((GuestAssignment) ArgumentMatchers.any(), (Connection) ArgumentMatchers.any());
        Mockito.verify(guestStorage, Mockito.times(1)).addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection);
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuest_guestNotFound_doNothing() throws OXException {
        Mockito.when(L(guestStorage.getGuestId(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any()))).thenReturn(L(GuestStorage.NOT_FOUND));

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuestAssignment(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any());
        Mockito.verify(guestStorage, Mockito.never()).removeGuest(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuest_assignmentStillExisting_doNotDeleteUser() throws OXException {
        Mockito.when(L(guestStorage.getNumberOfAssignments(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Connection.class)))).thenReturn(L(10L));

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.times(1)).removeGuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, connection);
        Mockito.verify(guestStorage, Mockito.never()).removeGuest(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Connection.class));
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuest_noAssignment_deleteUser() throws OXException {
        Mockito.when(L(guestStorage.getNumberOfAssignments(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Connection.class)))).thenReturn(L(0L));

        defaultGuestService.removeGuest(CONTEXT_ID, USER_ID);

        Mockito.verify(guestStorage, Mockito.times(1)).removeGuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, connection);
        Mockito.verify(guestStorage, Mockito.times(1)).removeGuest(GUEST_ID, connection);
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuestAssignments_assignmentRemovedButAssignmentStillAvailable_doNotRemoveGuest() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        removedGuests.add(L(22L));
        removedGuests.add(L(44L));
        removedGuests.add(L(66L));
        Mockito.when(guestStorage.resolveGuestAssignments(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any())).thenReturn(removedGuests);
        Mockito.when(L(guestStorage.getNumberOfAssignments(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any()))).thenReturn(L(2L));

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuest(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuestAssignments_assignmentRemovedButAssignmentNotAvailable_RemoveGuest() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        removedGuests.add(L(22L));
        removedGuests.add(L(44L));
        removedGuests.add(L(66L));
        Mockito.when(guestStorage.resolveGuestAssignments(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any())).thenReturn(removedGuests);
        Mockito.when(L(guestStorage.getNumberOfAssignments(ArgumentMatchers.anyLong(), (Connection) ArgumentMatchers.any()))).thenReturn(L(0L));

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.times(removedGuests.size())).removeGuest(ArgumentMatchers.anyLong(), ArgumentMatchers.any(Connection.class));
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testRemoveGuestAssignments_noGuestRemoved() throws OXException {
        List<Long> removedGuests = new ArrayList<Long>();
        Mockito.when(guestStorage.resolveGuestAssignments(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any())).thenReturn(removedGuests);

        defaultGuestService.removeGuests(CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.never()).removeGuest(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any());
        Mockito.verify(guestStorage, Mockito.never()).getNumberOfAssignments(ArgumentMatchers.anyInt(), (Connection) ArgumentMatchers.any());
        Mockito.verify(databaseService, Mockito.times(1)).backWritableForGlobal(CONTEXT_ID, connection);
    }

    @Test
    public void testCheck1() throws OXException {
        defaultGuestService.check("thisIs@valid.de");
    }

    @Test
    public void testCheck2() throws OXException {
        defaultGuestService.check("this-Is@valid.de");
    }

    @Test
    public void testCheck3() throws OXException {
        defaultGuestService.check("this-Is@va-lid.de");
    }

    @Test
    public void testCheck4() throws OXException {
        defaultGuestService.check("this-Is@va-lid");
    }

    @Test
    public void testCheck5() throws OXException {
        defaultGuestService.check("this-Is@v\u00e4-lid");
    }

    @Test(expected = OXException.class)
    public void testCheck6() throws OXException {
        defaultGuestService.check("th\u00fcs-Is@v\u00e4-lid");
    }

    @Test(expected = OXException.class)
    public void testCheck7() throws OXException {
        defaultGuestService.check("this- Is@v\u00e4-lid");
    }

    @Test
    public void testUpdateGuestUser_noAssignmentAvailable_doNotUpdate() throws OXException {
        defaultGuestService.updateGuestUser(user, CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.never()).updateGuestAssignment((GuestAssignment) ArgumentMatchers.any(), (Connection) ArgumentMatchers.any());
    }

    @Test(expected = OXException.class)
    public void testUpdateGuestUser_guestNull_throwException() throws OXException {
        defaultGuestService.updateGuestUser(null, CONTEXT_ID);
    }

    @Test
    public void testUpdateGuestUser_foundTwoAssignments_updateBoth() throws OXException {
        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            protected List<GuestAssignment> retrieveGuestAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        defaultGuestService.updateGuestUser(user, CONTEXT_ID);

        Mockito.verify(guestStorage, Mockito.times(2)).updateGuestAssignment((GuestAssignment) ArgumentMatchers.any(), (Connection) ArgumentMatchers.any());
    }

    @Test(expected = OXException.class)
    public void testUpdateGuestContact_contacttNull_throwException() throws OXException {
        defaultGuestService.updateGuestContact(null, CONTEXT_ID);
    }

    @Test
    public void testUpdateGuestContact_noAssignmentAvailable_doNotUpdate() throws OXException {
        defaultGuestService.updateGuestContact(contact, CONTEXT_ID);

        Mockito.verify(contactUserStorage, Mockito.never()).getGuestContact(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (ContactField[]) ArgumentMatchers.any());
    }

    @Test
    public void testUpdateGuestContact_foundTwoAssignments_updateBoth() throws OXException {
        Mockito.when(contactUserStorage.getGuestContact(ArgumentMatchers.anyInt(), Mockito.anyInt(), (ContactField[]) Mockito.any())).thenReturn(Mockito.mock(Contact.class));

        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            protected List<GuestAssignment> retrieveGuestAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        defaultGuestService.updateGuestContact(contact, CONTEXT_ID);

        Mockito.verify(contactUserStorage, Mockito.times(2)).updateGuestContact(Mockito.anyInt(), Mockito.anyInt(), (Contact) Mockito.any(), (Connection) Mockito.any());
    }

    @Test
    public void testCreateContactCopy_noAssignmentsAvailable_returnNullAsCopy() throws OXException {
        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return null;
            }
        };

        Contact contactCopy = defaultGuestService.createContactCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID);

        Assert.assertNull(contactCopy);
    }

    @Test
    public void testCreateContactCopy_storageContactNotAvailable_returnNullAsCopy() throws OXException {
        Mockito.when(contactUserStorage.getGuestContact(ArgumentMatchers.anyInt(), Mockito.anyInt(), (ContactField[]) Mockito.any())).thenReturn(null);

        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        Contact contactCopy = defaultGuestService.createContactCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID);

        Assert.assertNull(contactCopy);
    }

    @Test
    public void testCreateContactCopy_assignmentsAvailable_returnCopy() throws OXException {
        Contact storageContact = new Contact();
        String homeAddress = "this is my home";
        storageContact.setAddressHome(homeAddress);
        java.util.Date birthday = Calendar.getInstance().getTime();
        storageContact.setBirthday(birthday);

        Mockito.when(contactUserStorage.getGuestContact(ArgumentMatchers.anyInt(), Mockito.anyInt(), (ContactField[]) Mockito.any())).thenReturn(storageContact);

        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        Contact contactCopy = defaultGuestService.createContactCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID, USER_ID);

        Assert.assertNotNull(contactCopy);
        // asserts for updated fields
        Assert.assertEquals(GUEST_MAIL_ADDRESS, contactCopy.getEmail1());
        Assert.assertEquals(USER_ID, contactCopy.getCreatedBy());
        Assert.assertEquals(CONTEXT_ID, contactCopy.getContextId());
        Assert.assertEquals(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID, contactCopy.getParentFolderID());
        // asserts for existing fields
        Assert.assertEquals(homeAddress, contactCopy.getAddressHome());
        Assert.assertEquals(GUEST_MAIL_ADDRESS, contactCopy.getEmail1());
        Assert.assertEquals(birthday, contactCopy.getBirthday());
    }

    @Test
    public void testCreateUserCopy_noAssignmentsAvailable_returnNullAsCopy() throws OXException {
        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return null;
            }
        };

        UserImpl userCopy = defaultGuestService.createUserCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID);

        Assert.assertNull(userCopy);
    }

    @Test
    public void testCreateUserCopy_storageContactNotAvailable_returnNullAsCopy() throws OXException {
        Mockito.when(userService.getUser(ArgumentMatchers.anyInt(), Mockito.anyInt())).thenReturn(null);

        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        UserImpl userCopy = defaultGuestService.createUserCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID);

        Assert.assertNull(userCopy);
    }

    @Test
    public void testCreateUserCopy_assignmentsAvailable_returnCopy() throws OXException {
        user = new UserImpl();
        String displayName = "that's my display name";
        user.setDisplayName(displayName);
        user.setMail(GUEST_MAIL_ADDRESS);
        user.setLoginInfo(GUEST_MAIL_ADDRESS);
        user.setPasswordMech(GUEST_PASSWORD_MECH);
        user.setUserPassword(GUEST_PASSWORD);
        user.setSalt(GUEST_PASSWORD_SALT);
        String timeZone = "Europe/Belgrade";
        user.setTimeZone(timeZone);
        String language = "de_DE";
        user.setPreferredLanguage(language);

        String givenName = "Horsti";
        user.setGivenName(givenName);
        String smtpServer = "aValueForTheServer";
        user.setSmtpServer(smtpServer);

        Mockito.when(userService.getUser(ArgumentMatchers.anyInt(), Mockito.anyInt())).thenReturn(user);

        defaultGuestService = new DefaultGuestService(userService, contextService, contactUserStorage, configViewFactory, passwordMechFactory) {

            @Override
            public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) {
                return assignments;
            }
        };

        UserImpl userCopy = defaultGuestService.createUserCopy(GUEST_MAIL_ADDRESS, GROUP_ID, CONTEXT_ID);

        Assert.assertNotNull(userCopy);
        // asserts for updated fields
        Assert.assertEquals(GUEST_MAIL_ADDRESS, userCopy.getMail());
        Assert.assertEquals(displayName, userCopy.getDisplayName());
        Assert.assertEquals(GUEST_MAIL_ADDRESS, userCopy.getLoginInfo());
        Assert.assertEquals(GUEST_PASSWORD_MECH, userCopy.getPasswordMech());
        Assert.assertEquals(GUEST_PASSWORD, userCopy.getUserPassword());
        Assert.assertEquals(timeZone, userCopy.getTimeZone());
        Assert.assertEquals(language, userCopy.getPreferredLanguage());
        // asserts for existing fields
        Assert.assertEquals(givenName, userCopy.getGivenName());
        Assert.assertEquals(smtpServer, userCopy.getSmtpServer());
    }

    @Test
    public void testGetExistingAssignments_mailAddressNull_returnEmptyCollection() throws OXException {
        List<GuestAssignment> existingAssignments = defaultGuestService.getExistingAssignments(null, GROUP_ID);

        Assert.assertTrue(existingAssignments.isEmpty());
    }

    @Test
    public void testGetExistingAssignments_mailAddressEmpty_returnEmptyCollection() throws OXException {
        List<GuestAssignment> existingAssignments = defaultGuestService.getExistingAssignments("", GROUP_ID);

        Assert.assertTrue(existingAssignments.isEmpty());
    }

    @Test
    public void testGetExistingAssignments_guestIdNotFound_returnEmptyCollection() throws OXException {
        Mockito.when(L(guestStorage.getGuestId(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), (Connection) ArgumentMatchers.any()))).thenReturn(L(GuestStorage.NOT_FOUND));

        List<GuestAssignment> existingAssignments = defaultGuestService.getExistingAssignments(GUEST_MAIL_ADDRESS, GROUP_ID);

        Assert.assertTrue(existingAssignments.isEmpty());
    }

    @Test(expected = OXException.class)
    public void testGetExistingAssignments_noGuestAssignmentFound_throwException() throws OXException {
        Mockito.when(guestStorage.getGuestAssignments(Mockito.anyLong(), (Connection) ArgumentMatchers.any())).thenReturn(Collections.<GuestAssignment> emptyList());

        defaultGuestService.getExistingAssignments(GUEST_MAIL_ADDRESS, GROUP_ID);
    }

    @Test
    public void testGetExistingAssignmentstodo() throws OXException {
        Mockito.when(guestStorage.getGuestAssignments(Mockito.anyLong(), (Connection) ArgumentMatchers.any())).thenReturn(assignments);

        List<GuestAssignment> existingAssignments = defaultGuestService.getExistingAssignments(GUEST_MAIL_ADDRESS, GROUP_ID);

        Assert.assertEquals(assignments.size(), existingAssignments.size());
        Assert.assertEquals(assignments.get(0).getGuestId(), existingAssignments.get(0).getGuestId());
        Assert.assertEquals(assignments.get(0).getPassword(), existingAssignments.get(0).getPassword());
        Assert.assertEquals(assignments.get(0).getPasswordMech(), existingAssignments.get(0).getPasswordMech());
        Assert.assertEquals(assignments.get(0).getUserId(), existingAssignments.get(0).getUserId());
    }
}
