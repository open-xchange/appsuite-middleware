package com.openexchange.ajax.appointment;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class UpdatesTest extends AppointmentTest {

	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
        DataObject.LAST_MODIFIED_UTC,
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
		CalendarObject.INTERVAL,
        CalendarObject.RECURRENCE_COUNT,
        CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
        CalendarObject.ALARM,
        CalendarObject.NOTIFICATION,
        Appointment.SHOWN_AS,
		Appointment.FULL_TIME,
		Appointment.COLOR_LABEL,
		Appointment.TIMEZONE,
		Appointment.RECURRENCE_START
	};

	private static final Log LOG = LogFactory.getLog(UpdatesTest.class);

	public UpdatesTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() throws Exception {

	}

	public void testModified() throws Exception {
		AppointmentTest.listModifiedAppointment(getWebConversation(), appointmentFolderId, new Date(), new Date(), new Date(System.currentTimeMillis()-(dayInMillis*7)), _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
	}

	public void testDeleted() throws Exception {
		AppointmentTest.listDeleteAppointment(getWebConversation(), appointmentFolderId, new Date(), new Date(), new Date(System.currentTimeMillis()-(dayInMillis*7)), timeZone, PROTOCOL + getHostName(), getSessionId());
	}

	public void testModifiedWithoutFolderId() throws Exception {
		final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));

		final Appointment appointmentObj = createAppointmentObject("testModifiedWithoutFolderId");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();

		final Appointment[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, decrementDate(modified), _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());

		assertTrue("no appointment object in response", appointmentArray.length > 0);
		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}

		assertTrue("created object not found in response", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
	}

	public void testModifiedWithoutFolderIdExtended() throws Exception {
		final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));

		final Appointment appointmentObj = createAppointmentObject("testModifiedWithoutFolderIdExtended");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId1, appointmentFolderId, timeZone, getHostName(), getSessionId());
		Date modified = loadAppointment.getLastModified();

		Appointment[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, decrementDate(modified), _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());

		assertTrue("no appointment object in response", appointmentArray.length > 0);
		boolean found1 = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId1) {
				found1 = true;
			}
		}

		assertTrue("created object not found in response", found1);

		final int objectId2 = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		loadAppointment = loadAppointment(getWebConversation(), objectId2, appointmentFolderId, timeZone, getHostName(), getSessionId());
		modified = loadAppointment.getLastModified();

		appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, decrementDate(modified), _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());

		assertTrue("no appointment object in response", appointmentArray.length > 0);
		found1 = false;
		boolean found2 = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId1) {
				found1 = true;
			} else if (appointmentArray[a].getObjectID() == objectId2) {
				found2 = true;
			}
		}

		assertFalse("invalid object id in reponse", found1);
		assertTrue("created object not found in response", found2);

		deleteAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getSessionId(), false);
		deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, getHostName(), getSessionId(), false);
	}


	public void testModifiedWithoutFolderIdWithFutureTimestamp() throws Exception {
		final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));

		final Appointment appointmentObj = createAppointmentObject("testModifiedWithoutFolderIdWithFutureTimestamp");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = new Date(loadAppointment.getLastModified().getTime() + (7 * dayInMillis));

		final Appointment[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, modified, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());

		assertEquals("unexpected data in response", 0, appointmentArray.length);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
	}

	public void testModifiedRecurrenceAppointment() throws Exception {
		final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));

		final Appointment appointmentObj = createAppointmentObject("testModifiedRecurrenceAppointment");
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(5);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		appointmentObj.setObjectID(objectId);

		final Appointment[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, new Date(0), _appointmentFields, timeZone, getHostName(), getSessionId());

		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
				break;
			}
		}

		assertTrue("object with object_id: " + objectId + " not found in response", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
	}

	private static Date decrementDate(final Date date) {
	    return new Date(date.getTime() - 1);
	}

}

