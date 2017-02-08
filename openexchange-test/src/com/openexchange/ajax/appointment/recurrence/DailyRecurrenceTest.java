
package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

public class DailyRecurrenceTest extends AbstractRecurrenceTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        simpleDateFormatUTC.setTimeZone(timeZoneUTC);
        catm.setTimezone(timeZone);
    }

    @Test
    public void testDailyRecurrenceFromWinter2SummerTime() throws Exception {
        final Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");

        final Date start = simpleDateFormatUTC.parse("2007-02-01 00:00:00");
        final Date end = simpleDateFormatUTC.parse("2007-05-01 00:00:00");

        final Date startDate = simpleDateFormat.parse("2007-03-01 08:00:00");
        final Date endDate = simpleDateFormat.parse("2007-03-01 10:00:00");

        final Appointment appointmentObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testDailyRecurrenceFromWinter2SummerTime", startDate, endDate);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

        boolean found = false;

        final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

        final Appointment[] appointmentArray = catm.all(appointmentFolderId, start, end, _fields, false);
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
                found = true;
            }
        }

        assertTrue("appointment not found in response", found);

        final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

        for (int a = 1; a <= 31; a++) {
            final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
            final int day = a;
            if (day < 10) {
                assertOccurrence(day, simpleDateFormat.parse("2007-03-0" + day + " 08:00:00"), simpleDateFormat.parse("2007-03-0" + day + " 10:00:00"), occurrence, timeZone);
            } else {
                assertOccurrence(day, simpleDateFormat.parse("2007-03-" + day + " 08:00:00"), simpleDateFormat.parse("2007-03-" + day + " 10:00:00"), occurrence, timeZone);
            }
        }

        final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
        assertOccurrence(32, simpleDateFormat.parse("2007-04-01 08:00:00"), simpleDateFormat.parse("2007-04-01 10:00:00"), occurrence, timeZone);
    }

    @Test
    public void testFullTimeDailyRecurrenceFromWinter2SummerTime() throws Exception {
        final Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");

        final Date start = simpleDateFormatUTC.parse("2007-02-01 00:00:00");
        final Date end = simpleDateFormatUTC.parse("2007-05-01 00:00:00");

        final Date startDate = simpleDateFormatUTC.parse("2007-03-01 00:00:00");
        final Date endDate = simpleDateFormatUTC.parse("2007-03-02 00:00:00");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTimeDailyRecurrenceFromWinter2SummerTime");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

        boolean found = false;

        final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

        final Appointment[] appointmentArray = catm.all(appointmentFolderId, start, end, _fields, false);
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
                found = true;
            }
        }

        assertTrue("appointment not found in response", found);

        final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

        for (int a = 1; a <= 31; a++) {
            final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
            final int day = a;
            if (day < 10) {
                assertOccurrence(day, simpleDateFormatUTC.parse("2007-03-0" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-03-0" + (day + 1) + " 00:00:00"), occurrence);
            } else {
                assertOccurrence(day, simpleDateFormatUTC.parse("2007-03-" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-03-" + (day + 1) + " 00:00:00"), occurrence);
            }
        }

        final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
        assertOccurrence(32, simpleDateFormatUTC.parse("2007-04-01 00:00:00"), simpleDateFormatUTC.parse("2007-04-02 00:00:00"), occurrence, timeZone);
    }

    @Test
    public void testDailyRecurrenceFromSummer2WinterTime() throws Exception {
        final Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");

        final Date start = simpleDateFormatUTC.parse("2007-09-01 00:00:00");
        final Date end = simpleDateFormatUTC.parse("2007-12-01 00:00:00");

        final Date startDate = simpleDateFormat.parse("2007-10-01 08:00:00");
        final Date endDate = simpleDateFormat.parse("2007-10-01 10:00:00");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDailyRecurrenceFromSummer2WinterTime");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

        boolean found = false;

        final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

        final Appointment[] appointmentArray = catm.all(appointmentFolderId, start, end, _fields, false);
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
                found = true;
            }
        }

        assertTrue("appointment not found in response", found);

        final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

        for (int a = 1; a <= 31; a++) {
            final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
            final int day = a;
            if (day < 10) {
                assertOccurrence(day, simpleDateFormat.parse("2007-10-0" + day + " 08:00:00"), simpleDateFormat.parse("2007-10-0" + day + " 10:00:00"), occurrence);
            } else {
                assertOccurrence(day, simpleDateFormat.parse("2007-10-" + day + " 08:00:00"), simpleDateFormat.parse("2007-10-" + day + " 10:00:00"), occurrence);
            }
        }

        final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
        assertOccurrence(32, simpleDateFormat.parse("2007-11-01 08:00:00"), simpleDateFormat.parse("2007-11-01 10:00:00"), occurrence);
    }

    @Test
    public void testFullTimeDailyRecurrenceFromSummer2WinterTime() throws Exception {
        final Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");

        final Date start = simpleDateFormatUTC.parse("2007-09-01 00:00:00");
        final Date end = simpleDateFormatUTC.parse("2007-12-01 00:00:00");

        final Date startDate = simpleDateFormatUTC.parse("2007-10-01 00:00:00");
        final Date endDate = simpleDateFormatUTC.parse("2007-10-02 00:00:00");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTimeDailyRecurrenceFromSummer2WinterTime");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setUntil(until);
        appointmentObj.setFullTime(true);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

        boolean found = false;

        final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

        final Appointment[] appointmentArray = catm.all(appointmentFolderId, start, end, _fields, false);
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
                found = true;
            }
        }

        assertTrue("appointment not found in response", found);

        final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

        for (int a = 1; a <= 31; a++) {
            final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
            final int day = a;
            if (day < 10) {
                assertOccurrence(day, simpleDateFormatUTC.parse("2007-10-0" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-10-0" + (day + 1) + " 00:00:00"), occurrence);
            } else {
                assertOccurrence(day, simpleDateFormatUTC.parse("2007-10-" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-10-" + (day + 1) + " 00:00:00"), occurrence);
            }
        }

        final Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
        assertOccurrence(32, simpleDateFormatUTC.parse("2007-11-01 00:00:00"), simpleDateFormatUTC.parse("2007-11-02 00:00:00"), occurrence);
    }
}
