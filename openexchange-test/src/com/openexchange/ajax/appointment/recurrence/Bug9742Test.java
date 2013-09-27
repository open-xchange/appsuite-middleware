package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class Bug9742Test extends AbstractAJAXSession {

	private static final Log LOG = LogFactory.getLog(Bug9742Test.class);

	public static final int[] APPOINTMENT_FIELDS = { DataObject.OBJECT_ID, DataObject.CREATED_BY,
			DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
			FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES,
			CalendarObject.TITLE, CalendarObject.START_DATE, CalendarObject.END_DATE,
			Appointment.LOCATION, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
			CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS,
			Appointment.FULL_TIME, Appointment.COLOR_LABEL };

	public Bug9742Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() {

	}

	public void testBug9742() throws Exception {
		final AJAXSession ajaxSession = getSession();
		final AJAXClient ajaxClient = getClient();
		TimeZone timeZone = ajaxClient.getValues().getTimeZone();
		if (!timeZone.getID().equals("Europe/Berlin")) {
			ajaxClient.getValues().setTimeZone(TimeZone.getTimeZone("America/New_York"));
		}

		timeZone = ajaxClient.getValues().getTimeZone();

		final Calendar calendar = Calendar.getInstance(timeZone);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		final Date startDate = calendar.getTime();

		calendar.add(Calendar.HOUR_OF_DAY, 2);

		final Date endDate = calendar.getTime();

		final int appointmentFolderId = ajaxClient.getValues().getPrivateAppointmentFolder();

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testBug9742");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setRecurrenceCount(5);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setTimezone("Europe/Berlin");

		final Calendar calendarRange = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarRange.add(Calendar.MILLISECOND, timeZone.getOffset(calendarRange.getTimeInMillis()));
		calendarRange.set(Calendar.HOUR_OF_DAY, 0);
		calendarRange.set(Calendar.MINUTE, 0);
		calendarRange.set(Calendar.SECOND, 0);
		calendarRange.set(Calendar.MILLISECOND, 0);

		final Date start = calendarRange.getTime();

		calendarRange.add(Calendar.DAY_OF_MONTH, 5);

		final Date end = calendarRange.getTime();

		final InsertRequest insertRequest = new InsertRequest(appointmentObj, timeZone, true);

		final CommonInsertResponse insertResponse = Executor.execute(ajaxSession,
				insertRequest);
		final int objectId = insertResponse.getId();

		final String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
		final WebConversation webCon = ajaxSession.getConversation();

		final Appointment loadAppointment = AppointmentTest.loadAppointment(webCon, objectId, appointmentFolderId, timeZone, hostname, ajaxSession.getId());
		final Date modified = loadAppointment.getLastModified();


		final Appointment[] appointmentArray = AppointmentTest.listAppointment(webCon,
				appointmentFolderId, APPOINTMENT_FIELDS, start, end, timeZone, false, hostname, ajaxSession
						.getId());

		int appointmentCounter = 0;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				appointmentCounter++;
			}
		}

		assertEquals("unexpected appointments size", 4, appointmentCounter);

		final DeleteRequest deleteRequest = new DeleteRequest(objectId, appointmentFolderId, modified);
		Executor.execute(ajaxSession, deleteRequest);
	}
}
