package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class Bug4392Test extends AppointmentTest {

	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		Appointment.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		Appointment.SHOWN_AS,
		Appointment.FULL_TIME,
		Appointment.COLOR_LABEL,
		Appointment.TIMEZONE
	};

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug4392Test.class);

	public Bug4392Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * This test case check the until date of recurrence appointments
	 */
	public void testBug4392() throws Exception {
		final int occurrences = 4;

		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		calendar.add(Calendar.DAY_OF_MONTH, occurrences-1);
		final Date until = calendar.getTime();

		final Appointment appointmentObj = createAppointmentObject("testBug4392");
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		//appointmentObj.setOccurrence(4);
        appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		try {
			compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		} catch (final OXException exc) {
			fail("exception: " + exc.toString());
		}
	}
}
