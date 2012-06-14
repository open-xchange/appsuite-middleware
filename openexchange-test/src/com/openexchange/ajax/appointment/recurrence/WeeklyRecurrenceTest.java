package com.openexchange.ajax.appointment.recurrence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.container.Appointment;

public class WeeklyRecurrenceTest extends AbstractRecurrenceTest {

	private static final Log LOG = LogFactory.getLog(WeeklyRecurrenceTest.class);

	public WeeklyRecurrenceTest(final String name) {
		super(name);
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
	}

	public void testDummy() {

	}

	public void _notestWeeklyRecurrenceFromWinter2SummerTime() throws Exception {
		final Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");

		final Date start = simpleDateFormatUTC.parse("2007-02-01 00:00:00");
		final Date end = simpleDateFormatUTC.parse("2007-05-01 00:00:00");

		final Date startDate = simpleDateFormatUTC.parse("2007-03-05 08:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-03-05 10:00:00");

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testWeeklyRecurrenceFromWinter2SummerTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.WEEKLY);
		appointmentObj.setDays(Appointment.MONDAY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

		boolean found = false;

		final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

		final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}

		assertTrue("appointment not found in response", found);

		final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 1);
		assertOccurrence(1, simpleDateFormatUTC.parse("2007-03-05 08:00:00"), simpleDateFormatUTC.parse("2007-03-05 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 2);
		assertOccurrence(2, simpleDateFormatUTC.parse("2007-03-12 08:00:00"), simpleDateFormatUTC.parse("2007-03-12 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 3);
		assertOccurrence(3, simpleDateFormatUTC.parse("2007-03-19 08:00:00"), simpleDateFormatUTC.parse("2007-03-19 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 4);
		assertOccurrence(4, simpleDateFormatUTC.parse("2007-03-26 08:00:00"), simpleDateFormatUTC.parse("2007-03-26 10:00:00"), occurrence);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}

	public void _notestWeeklyRecurrenceFromSummer2WinterTime() throws Exception {
		final Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");

		final Date start = simpleDateFormatUTC.parse("2007-9-01 00:00:00");
		final Date end = simpleDateFormatUTC.parse("2007-12-01 00:00:00");

		final Date startDate = simpleDateFormatUTC.parse("2007-10-01 08:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-10-01 10:00:00");

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testWeeklyRecurrenceFromSummer2WinterTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.WEEKLY);
		appointmentObj.setDays(Appointment.MONDAY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());

		boolean found = false;

		final List<Occurrence> occurrenceList = new ArrayList<Occurrence>();

		final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}

		assertTrue("appointment not found in response", found);

		final Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 1);
		assertOccurrence(1, simpleDateFormatUTC.parse("2007-10-01 08:00:00"), simpleDateFormatUTC.parse("2007-10-01 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 2);
		assertOccurrence(2, simpleDateFormatUTC.parse("2007-10-08 08:00:00"), simpleDateFormatUTC.parse("2007-10-08 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 3);
		assertOccurrence(3, simpleDateFormatUTC.parse("2007-10-15 08:00:00"), simpleDateFormatUTC.parse("2007-10-15 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 4);
		assertOccurrence(4, simpleDateFormatUTC.parse("2007-10-22 08:00:00"), simpleDateFormatUTC.parse("2007-10-22 10:00:00"), occurrence);

		occurrence = getOccurrenceByPosition(occurrenceArray, 5);
		assertOccurrence(5, simpleDateFormatUTC.parse("2007-10-29 08:00:00"), simpleDateFormatUTC.parse("2007-10-29 10:00:00"), occurrence);


		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}
}

