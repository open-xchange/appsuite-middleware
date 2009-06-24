package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.XmlServlet;

public class Bug8447Test extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(Bug8447Test.class);
	
	public Bug8447Test(final String name) {
		super(name);
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
	}
	
	public void testDummy() {
		
	}
	
	public void testBug8447() throws Exception {
		final Date modified = new Date();

		final Date startDate = simpleDateFormatUTC.parse("2007-06-01 00:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-06-02 00:00:00");

		final Date until = simpleDateFormatUTC.parse("2007-06-15 00:00:00");
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testBug8447");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Date exceptionStartDate = simpleDateFormatUTC.parse("2007-06-06 00:00:00");
		final Date exceptionEndDate = simpleDateFormatUTC.parse("2007-06-07 00:00:00");
		
		final Date recurrenceDatePosition = simpleDateFormatUTC.parse("2007-06-06 00:00:00");
		
		final Appointment exceptionAppointmentObject = new Appointment();
		exceptionAppointmentObject.setTitle("testBug8447 - change exception (2007-06-06)");
		exceptionAppointmentObject.setStartDate(exceptionStartDate);
		exceptionAppointmentObject.setEndDate(exceptionEndDate);
		exceptionAppointmentObject.setFullTime(true);
		exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
		exceptionAppointmentObject.setShownAs(Appointment.ABSENT);
		exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
		exceptionAppointmentObject.setIgnoreConflicts(true);

		final int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setDeleteExceptions(new Date[] { recurrenceDatePosition });
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());		
		
		try {
			final Appointment loadAppointment = loadAppointment(webCon, exceptionObjectId, appointmentFolderId, PROTOCOL + hostName, login, password);
			fail("object not found exception expected!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}