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

import static org.junit.Assert.*;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

public class NewTest extends AppointmentTest {

    private CalendarTestManager ctm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ctm2 = new CalendarTestManager(getClient2());
        ctm2.setFailOnError(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            ctm2.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testSimple() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSimple");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
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
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
    }

    @Test
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
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
    }

    @Test
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
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());
    }

    @Test
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
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setInterval(1);
        appointmentObj.setDayInMonth(1);
        appointmentObj.setMonth(Calendar.APRIL);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());

        // load a recurrence

        loadAppointment = catm.get(appointmentFolderId, objectId, 2);

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

    @Test
    public void testUserParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testUserParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = getClient2().getValues().getUserId();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        UserParticipant userParticipant = new UserParticipant(userParticipantId);
        participants[1] = userParticipant;
        appointmentObj.setParticipants(participants);

        final StringWriter stringWriter = new StringWriter();
        final JSONObject jsonObj = new JSONObject();
        final AppointmentWriter appointmentwriter = new AppointmentWriter(getClient().getValues().getTimeZone());
        appointmentwriter.writeAppointment(appointmentObj, jsonObj);
        stringWriter.write(jsonObj.toString());
        stringWriter.flush();

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testGroupParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGroupParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int groupParticipantId = GroupTest.searchGroup(getClient(), testContext.getGroupParticipants().get(0))[0].getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new GroupParticipant(groupParticipantId);

        appointmentObj.setParticipants(participants);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testResourceParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testResourceParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int resourceParticipantId = resTm.search(testContext.getResourceParticipants().get(0)).get(0).getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        int objectId = catm.insert(appointmentObj).getObjectID();
        assertFalse(catm.getLastResponse().hasConflicts());
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        assertFalse(catm.getLastResponse().hasConflicts());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testAllParticipants() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testAllParticipants");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int groupParticipantId = GroupTest.searchGroup(getClient(), testContext.getGroupParticipants().get(0))[0].getIdentifier();
        final int resourceParticipantId = resTm.search(testContext.getResourceParticipants().get(0)).get(0).getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);

        appointmentObj.setParticipants(participants);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        assertFalse(catm.getLastResponse().hasConflicts());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testSpecialCharacters() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSpecialCharacters - \u00F6\u00E4\u00FC-:,;.#?!\u00A7$%&/()=\"<>");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testPrivateFolder() throws Exception {
        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testPrivateFolder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, 1);
        int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPrivateFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(targetFolder, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testPublicFolder() throws Exception {
        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testPublicFolder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PUBLIC, userId, 2);
        int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPrivateFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(targetFolder, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testSharedFolder() throws Exception {
        final int secondUserId = getClient2().getValues().getUserId();

        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testSharedFolder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, getClient().getValues().getPrivateAppointmentFolder());
        List<OCLPermission> permissions = folderObj.getPermissions();
        permissions.add(FolderTestManager.createPermission(secondUserId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false));
        permissions.add(FolderTestManager.createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));
        folderObj.setPermissions(permissions);

        int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testSharedFolder");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(targetFolder);
        appointmentObj.setIgnoreConflicts(true);

        int objectId = ctm2.insert(appointmentObj).getObjectID();
        final Appointment loadAppointment = ctm2.get(targetFolder, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testDailyRecurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrence");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testWeeklyRecurrence() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (10 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testWeeklyRecurrence");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.WEEKLY);
        appointmentObj.setDays(Appointment.SUNDAY + Appointment.MONDAY + Appointment.TUESDAY + Appointment.WEDNESDAY + Appointment.THURSDAY + Appointment.FRIDAY + Appointment.SATURDAY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
    }

    @Test
    public void testMonthlyRecurrenceDayInMonth() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (60 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testMonthlyRecurrenceDayInMonth");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.MONTHLY);
        appointmentObj.setDayInMonth(15);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    @Test
    public void testMonthlyRecurrenceDays() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (90 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testMonthlyRecurrenceDays");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.MONTHLY);
        appointmentObj.setDays(Appointment.WEDNESDAY);
        appointmentObj.setDayInMonth(3);
        appointmentObj.setInterval(2);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    @Test
    public void testYearlyRecurrenceDayInMonth() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (800 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testYearlyRecurrenceDayInMonth");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(Calendar.JULY);
        appointmentObj.setDayInMonth(15);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    @Test
    public void testYearlyRecurrenceDays() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (800 * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testYearlyRecurrenceDays");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(Calendar.JULY);
        appointmentObj.setDays(Appointment.WEDNESDAY);
        appointmentObj.setDayInMonth(3);
        appointmentObj.setInterval(2);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
    }

    @Test
    public void testConflict() throws Exception {
        Appointment appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict", new Date(startTime), new Date(endTime));
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setIgnoreConflicts(true);
        catm.insert(appointment);

        appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict", new Date(startTime), new Date(endTime));
        appointment.setIgnoreConflicts(false);

        int objectId = catm.insert(appointment).getObjectID();
        assertTrue(objectId == 0);
        assertTrue(catm.getLastResponse().hasConflicts());

        appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict", new Date(startTime), new Date(endTime));
        appointment.setIgnoreConflicts(true);
        int objectId2 = catm.insert(appointment).getObjectID();
        assertTrue(objectId2 != 0);
        assertFalse(catm.getLastResponse().hasConflicts());

    }

    @Test
    public void testConflictWithResource() throws Exception {
        Appointment appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflictWithResource", new Date(startTime), new Date(endTime));
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setIgnoreConflicts(true);

        final int resourceParticipantId = ResourceTools.getSomeResource(getClient());

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);

        appointment.setParticipants(participants);

        int objectId = catm.insert(appointment).getObjectID();
        assertTrue(objectId != 0);

        appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflictWithResource", new Date(startTime), new Date(endTime));
        appointment.setIgnoreConflicts(false);
        appointment.setParticipants(participants);

        int objectId2 = catm.insert(appointment).getObjectID();
        assertTrue(catm.getLastResponse().hasConflicts());
        assertEquals("conflict expected here object id should be 0", 0, objectId2);

        appointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflictWithResource", new Date(startTime), new Date(endTime));
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(participants);

        objectId2 = catm.insert(appointment).getObjectID();
        assertTrue(catm.getLastResponse().hasConflicts());
        assertEquals("conflict expected here object id should be 0", 0, objectId2);
    }
}
