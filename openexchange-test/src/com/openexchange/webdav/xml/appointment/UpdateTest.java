package com.openexchange.webdav.xml.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.resource.Resource;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.XmlServlet;

public class UpdateTest extends AppointmentTest {

	public UpdateTest(final String name) {
		super(name);
	}

	public void testUpdate() throws Exception {
		Appointment appointmentObj = createAppointmentObject("testUpdateAppointment");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		appointmentObj = createAppointmentObject("testUpdateAppointment2");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setIgnoreConflicts(true);

		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);

		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testUpdateAppointmentRemoveAlarm() throws Exception {
		Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentRemoveAlarm");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarm(45);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		appointmentObj = createAppointmentObject("testUpdateAppointmentRemoveAlarm");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarmFlag(false);

		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);
		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testUpdateConcurentConflict() throws Exception {
		Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentConcurentConflict");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		appointmentObj = createAppointmentObject("testUpdateAppointmentConcurentConflict2");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setIgnoreConflicts(true);

		try {
			updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, new Date(0), PROTOCOL + hostName, login, password, context);
			fail("expected concurent modification exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
		}

		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testUpdateNotFound() throws Exception {
		Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentNotFound");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		appointmentObj = createAppointmentObject("testUpdateAppointmentNotFound");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setIgnoreConflicts(true);

		try {
			updateAppointment(webCon, appointmentObj, (objectId + 1000), appointmentFolderId, new Date(0), PROTOCOL + hostName, login, password, context);
			fail("expected object not found exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}

		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testUpdateAppointmentWithParticipants() throws Exception {
		Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");

		final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant3, getPassword(), context);
		assertTrue("user participant not found", userParticipantId != -1);
		final Group[] groupArray = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password, context);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		final int groupParticipantId = groupArray[0].getIdentifier();
		final Resource[] resourceArray = GroupUserTest.searchResource(webCon, resourceParticipant, new Date(0), PROTOCOL + hostName, login, password, context);
		assertTrue("resource array size is not > 0", resourceArray.length > 0);
		final int resourceParticipantId = resourceArray[0].getIdentifier();

		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);

		appointmentObj.setParticipants(participants);
		appointmentObj.setIgnoreConflicts(true);

		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);

		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context );
	}

	public void testUpdateRecurrenceWithDatePosition() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));

		final int changeExceptionPosition = 3;

		Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpdateRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), login, password, context);
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);
		compareObject(appointmentObj, loadAppointment);

		appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpdateRecurrence - exception");
		appointmentObj.setStartDate(new Date(startTime.getTime() + 60*60*1000));
		appointmentObj.setEndDate(new Date(endTime.getTime() + 60*60*1000));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceDatePosition(new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis));
		appointmentObj.setIgnoreConflicts(true);

		final int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);
		appointmentObj.setObjectID(newObjectId);

		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);

		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);

		// Loaded exception MUST NOT contains any recurrence information except recurrence identifier and position.
        compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + getHostName(), login, password, context);
	}
	// Bug 11124
	public void testShouldExtendSeriesFromLimitedToEndless() throws OXException, Exception {
		Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpdateRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);

		appointmentObj.setOccurrence(12);

		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), login, password, context);
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);

		appointmentObj = new Appointment();
		appointmentObj.setObjectID(objectId);

		appointmentObj.setTitle("testUpdateRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);

		// omit ocurrences

		appointmentObj.setIgnoreConflicts(true);

		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password, context);

		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + getHostName(), login, password, context);

	}
}
