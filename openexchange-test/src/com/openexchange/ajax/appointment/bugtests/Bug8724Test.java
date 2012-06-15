package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class Bug8724Test extends AppointmentTest {

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

	private static final Log LOG = LogFactory.getLog(Bug8724Test.class);

	public Bug8724Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * This test checks if the list action return an object not found exception
	 * if one id is requested trhat doesn't exist
	 */
	public void testBug8724_I() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testBug8724_I");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		final Appointment appointmentListObject = new Appointment();
		appointmentListObject.setObjectID(objectId+1000);
		appointmentListObject.setParentFolderID(appointmentFolderId);

		try {
			listAppointment(getWebConversation(), new Appointment[] { appointmentListObject }, _appointmentFields, timeZone, getHostName(), getSessionId());
		} catch (final OXException exc) {
			assertTrue(true);
		}

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getSessionId(), false);
	}

	/**
	 * This test checks if the list action return an object not found exception
	 * if one id is requested trhat doesn't exist
	 */
	public void testBug8724_II() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testBug8724_II");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

		final Appointment appointmentListObject1 = new Appointment();
		appointmentListObject1.setObjectID(objectId+1000);
		appointmentListObject1.setParentFolderID(appointmentFolderId);

		final Appointment appointmentListObject2 = new Appointment();
		appointmentListObject2.setObjectID(objectId+1001);
		appointmentListObject2.setParentFolderID(appointmentFolderId);

		final Appointment[] appointmentArray = { appointmentListObject1, appointmentListObject2 };

		try {
			listAppointment(getWebConversation(), appointmentArray, _appointmentFields, timeZone, getHostName(), getSessionId());
		} catch (final OXException exc) {
			assertTrue(true);
		}

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getSessionId(), false);
	}
}
