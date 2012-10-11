package com.openexchange.webdav.xml.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends AppointmentTest {

	public DeleteTest(final String name) {
		super(name);
	}

	public void testDelete() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testDelete");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
		final int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		final int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };

		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteConcurentConflict() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentConcurentConflict");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteAppointment(webCon, objectId, appointmentFolderId, new Date(0), PROTOCOL + hostName, login, password, context);
			fail("expected concurent modification exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
		}

		deleteAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteNotFound() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentNotFound");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

		try {
			deleteAppointment(webCon, (objectId + 1000), appointmentFolderId, PROTOCOL + hostName, login, password, context);
			fail("expected object not found exception!");
		} catch (final OXException exc) {
			assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}

		deleteAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteRecurrenceWithDatePosition() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));

		final int changeExceptionPosition = 3;

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDeleteRecurrenceWithDatePosition");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
		compareObject(appointmentObj, loadAppointment);

		final Date modified = loadAppointment.getLastModified();

		deleteAppointment(webCon, objectId, appointmentFolderId, modified, new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis), PROTOCOL + getHostName(), getLogin(), getPassword(), context);

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		assertEqualsAndNotNull("delete exception is not equals", loadAppointment.getDeleteException(), new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) } );

		deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + hostName, login, password, context);
	}

	public void testDeleteRecurrenceWithDeleteExceptions() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));

		final int changeExceptionPosition = 3;

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDeleteRecurrenceWithDeleteExceptions");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
		appointmentObj.setObjectID(objectId);

		Appointment loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);
		final Date modified = loadAppointment.getLastModified();

		loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, decrementDate(modified), PROTOCOL + hostName, login, password, context);
		compareObject(appointmentObj, loadAppointment);

		appointmentObj.setDeleteExceptions(new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) } );

		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + hostName, login, password, context);
	}

}

