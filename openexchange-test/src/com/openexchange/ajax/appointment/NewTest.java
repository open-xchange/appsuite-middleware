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

package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

public class NewTest extends AppointmentTest {

    private int targetFolder;
    private int objectId;
    private int objectId2;
    private int folderId;
    private int folderId2;

    public NewTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        targetFolder = 0;
        objectId = 0;
        objectId2 = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        if (0 != objectId) {
            deleteAppointment(getWebConversation(), objectId, folderId, PROTOCOL + getHostName(), getSessionId(), false);
        }
        if (0 != objectId2) {
            deleteAppointment(getWebConversation(), objectId2, folderId2, PROTOCOL + getHostName(), getSessionId(), false);
        }
        if (0 != targetFolder) {
            com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), getLogin(), getPassword(), "");
        }
        super.tearDown();
    }

    public void testSimple() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSimple");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testFullTime() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);

        final Date end = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTime");
        appointmentObj.setStartDate(start);
        appointmentObj.setEndDate(end);
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
    }


    public void testFullTimeOverTwoDays() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 2);

        final Date end = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTime");
        appointmentObj.setStartDate(start);
        appointmentObj.setEndDate(end);
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
    }


    public void testServerShouldRoundDownFullTimeAppointments() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 13);

        final Date end = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date expectedStart = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);

        final Date expectedEnd = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTime rounds down");
        appointmentObj.setStartDate(start);
        appointmentObj.setEndDate(end);
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());
    }

    public void testShouldRoundFullTimeSeriesAsWell() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, Calendar.APRIL);

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 13);

        final Date end = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date expectedStart = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);

        final Date expectedEnd = c.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTime rounds down");
        appointmentObj.setStartDate(start);
        appointmentObj.setEndDate(end);
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setInterval(1);
        appointmentObj.setDayInMonth(1);
        appointmentObj.setMonth(Calendar.APRIL);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());

        // load a recurrence

        loadAppointment = loadAppointment(getWebConversation(), objectId, 2, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        final Calendar check = new GregorianCalendar();
        check.setTimeZone(c.getTimeZone());
        check.setTime(loadAppointment.getStartDate());

        assertEquals(0, check.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, check.get(Calendar.MINUTE));
        assertEquals(0, check.get(Calendar.SECOND));
        assertEquals(0, check.get(Calendar.MILLISECOND));

        check.setTime(loadAppointment.getEndDate());

        assertEquals(0, check.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, check.get(Calendar.MINUTE));
        assertEquals(0, check.get(Calendar.SECOND));
        assertEquals(0, check.get(Calendar.MILLISECOND));
    }

    public void testUserParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testUserParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);

        appointmentObj.setParticipants(participants);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testGroupParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGroupParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
        final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL, getHostName(), getSessionId())[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new GroupParticipant(groupParticipantId);

        appointmentObj.setParticipants(participants);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testResourceParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testResourceParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testAllParticipants() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testAllParticipants");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
        final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL, getHostName(), getSessionId())[0].getIdentifier();
        final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);

        appointmentObj.setParticipants(participants);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testSpecialCharacters() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSpecialCharacters - \u00F6\u00E4\u00FC-:,;.#?!\u00A7$%&/()=\"<>");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testPrivateFolder() throws Exception {
        final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testPrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
        targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), "");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPrivateFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = targetFolder;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testPublicFolder() throws Exception {
        final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testPublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, true);
        targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), "");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPrivateFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = targetFolder;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testSharedFolder() throws Exception {
        final FolderObject sharedFolderObject = FolderTest.getStandardCalendarFolder(getSecondWebConversation(), getHostName(), getSecondSessionId());
        final int secondUserId = sharedFolderObject.getCreatedBy();

        final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testSharedFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
        final OCLPermission[] permission = new OCLPermission[] {
            com.openexchange.webdav.xml.FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            com.openexchange.webdav.xml.FolderTest.createPermission( secondUserId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false)
        };

        folderObj.setPermissionsAsArray(permission);

        targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), "");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSharedFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        objectId = insertAppointment(getSecondWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
        folderId = targetFolder;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getSecondWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testDailyRecurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrence");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testWeeklyRecurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (10*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testWeeklyRecurrence");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.WEEKLY);
        appointmentObj.setDays(Appointment.SUNDAY + Appointment.MONDAY + Appointment.TUESDAY + Appointment.WEDNESDAY + Appointment.THURSDAY + Appointment.FRIDAY + Appointment.SATURDAY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    public void testMonthlyRecurrenceDayInMonth() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (60*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testMonthlyRecurrenceDayInMonth");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.MONTHLY);
        appointmentObj.setDayInMonth(15);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    public void testMonthlyRecurrenceDays() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (90*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testMonthlyRecurrenceDays");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.MONTHLY);
        appointmentObj.setDays(Appointment.WEDNESDAY);
        appointmentObj.setDayInMonth(3);
        appointmentObj.setInterval(2);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    public void testYearlyRecurrenceDayInMonth() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (800*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testYearlyRecurrenceDayInMonth");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(Calendar.JULY);
        appointmentObj.setDayInMonth(15);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    public void testYearlyRecurrenceDays() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (800*dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testYearlyRecurrenceDays");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(Calendar.JULY);
        appointmentObj.setDays(Appointment.WEDNESDAY);
        appointmentObj.setDayInMonth(3);
        appointmentObj.setInterval(2);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, folderId, timeZone, PROTOCOL + getHostName(), getSessionId());
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    public void testConflict() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;

        try {
            appointmentObj.setIgnoreConflicts(false);
            objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
            folderId2 = appointmentFolderId;
            deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), true);
            objectId2 = 0;
            folderId2 = 0;;
            fail("conflict exception expected here!");
        } catch (final OXException exc) {
            assertTrue(true);
        }

        appointmentObj.setIgnoreConflicts(true);
        objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId2 = appointmentFolderId;
    }

    public void testConflictWithResource() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflictWithResource");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int resourceParticipantId = ResourceTools.getSomeResource(getClient());

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        folderId = appointmentFolderId;

        appointmentObj.setIgnoreConflicts(false);
        try {
            objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
            folderId2 = appointmentFolderId;
            deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), true);
            objectId2 = 0;
            folderId2 = 0;
            assertEquals("conflict expected here object id should be 0", 0, objectId2);
        } catch (final OXException exc) {
            // Exception is expected
        }

        appointmentObj.setIgnoreConflicts(true);
        try {
            objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
            folderId2 = appointmentFolderId;
            deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), true);
            objectId2 = 0;
            folderId2 = 0;
            assertEquals("conflict expected here object id should be 0", 0, objectId2);
        } catch (final OXException exc) {
            assertTrue(true);
        }
    }
}
