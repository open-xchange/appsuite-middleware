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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

public class UpdateTest extends AppointmentTest {

    private final static int[] _appointmentFields = { DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION, CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.INTERVAL, CalendarObject.RECURRENCE_COUNT, CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.RECURRENCE_START };

    @Test
    public void testSimple() throws Exception {
        Appointment appointmentObj = createAppointmentObject("testSimple");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj = catm.get(appointmentObj.getParentFolderID(), objectId);
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setFullTime(true);
        appointmentObj.setLocation(null);
        appointmentObj.setObjectID(objectId);
        appointmentObj.removeParentFolderID();

        catm.update(appointmentFolderId, appointmentObj);
    }

    @Test
    public void testUpdateAppointmentWithParticipant() throws Exception {
        Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj = catm.get(appointmentObj.getParentFolderID(), objectId);

        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setFullTime(true);
        appointmentObj.setLocation(null);
        appointmentObj.setObjectID(objectId);

        final int userParticipantId = getClient2().getValues().getUserId();
        final int groupParticipantId = GroupTest.searchGroup(getClient(), testContext.getGroupParticipants().get(0))[0].getIdentifier();
        final int resourceParticipantId = resTm.search(testContext.getResourceParticipants().get(0)).get(0).getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        appointmentObj.removeParentFolderID();

        catm.update(appointmentFolderId, appointmentObj);
    }

    @Test
    public void testUpdateRecurrenceWithPosition() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        Appointment appointmentObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testUpdateRecurrence", new Date(startTime), new Date(endTime));
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);

        final long newStartTime = startTime + 60 * 60 * 1000;
        final long newEndTime = endTime + 60 * 60 * 1000;
        final int changeExceptionPosition = 3;

        appointmentObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testUpdateRecurrence - exception", new Date(newStartTime), new Date(newEndTime));
        appointmentObj.setRecurrencePosition(changeExceptionPosition);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setLastModified(new Date(Long.MAX_VALUE));
        appointmentObj.setObjectID(objectId);

        catm.update(appointmentFolderId, appointmentObj);
        Appointment newApp = catm.get(appointmentObj);
        final int newObjectId = newApp.getObjectID();
        assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);

        loadAppointment = catm.get(appointmentFolderId, newObjectId);

        // Loaded change exception MUST NOT contain any recurrence information except recurrence identifier and position.
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
    }

    // Node 356
    @Test
    public void testShiftRecurrenceAppointment() throws Exception {
        final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
        final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));

        Appointment appointmentObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testShiftRecurrenceAppointment", new Date(startTime), new Date(endTime));
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(5);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Date startDate = appointmentObj.getStartDate();
        final Date endDate = appointmentObj.getEndDate();
        final Calendar calendarStart = Calendar.getInstance(timeZone);
        final Calendar calendarEnd = Calendar.getInstance(timeZone);
        calendarStart.setTime(startDate);
        calendarStart.add(Calendar.DAY_OF_MONTH, 2);
        calendarEnd.setTime(endDate);
        calendarEnd.add(Calendar.DAY_OF_MONTH, 2);

        appointmentObj.setStartDate(calendarStart.getTime());
        appointmentObj.setEndDate(calendarEnd.getTime());
        appointmentObj.setOrganizer(testUser.getUser());

        final Calendar recurrenceStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        final int startDay = calendarStart.get(Calendar.DAY_OF_MONTH);
        final int startMonth = calendarStart.get(Calendar.MONTH);
        final int startYear = calendarStart.get(Calendar.YEAR);
        recurrenceStart.set(startYear, startMonth, startDay, 0, 0, 0);
        recurrenceStart.set(Calendar.MILLISECOND, 0);

        appointmentObj.setRecurringStart(recurrenceStart.getTimeInMillis());

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        final Date modified = loadAppointment.getLastModified();
        appointmentObj.setLastModified(modified);

        catm.update(appointmentFolderId, appointmentObj);

        loadAppointment = catm.get(appointmentFolderId, objectId);

        loadAppointment.removeUntil();   // TODO add expected until
        compareObject(appointmentObj, loadAppointment);

        final List<Appointment> appointmentArray = catm.updates(appointmentFolderId, _appointmentFields, new Date(0), false, false, Ignore.DELETED, start, end);

        boolean found = false;

        for (int a = 0; a < appointmentArray.size(); a++) {
            if (objectId == appointmentArray.get(a).getObjectID()) {
                compareObject(appointmentObj, appointmentArray.get(a));
                found = true;
                break;
            }
        }

        assertTrue("object with object_id: " + objectId + " not found in response", found);
    }

    // Bug 12700
    @Test
    public void testMakeFullTime() throws Exception {
        final TimeZone utc = TimeZone.getTimeZone("urc");

        final Appointment appointmentObj = createAppointmentObject("testShiftRecurrenceAppointment");
        appointmentObj.setStartDate(D("04/01/2008 12:00"));
        appointmentObj.setEndDate(D("04/01/2008 14:00"));
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        final Date modified = new Date(Long.MAX_VALUE);
        loadAppointment.setLastModified(modified);
        loadAppointment.setFullTime(true);

        catm.update(appointmentFolderId, loadAppointment);

        loadAppointment = catm.get(appointmentFolderId, objectId);

        final Calendar check = new GregorianCalendar();
        check.setTimeZone(utc);
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
}
