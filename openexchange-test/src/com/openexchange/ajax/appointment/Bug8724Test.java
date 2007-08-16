package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.test.TestException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		CalendarDataObject.TIMEZONE
	};

	private static final Log LOG = LogFactory.getLog(Bug8724Test.class);
	
	public Bug8724Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * This test checks if the list action return an object not found exception 
	 * if one id is requested trhat doesn't exist
	 */
	public void testBug8724_I() throws Exception {
		final AppointmentObject appointmentObj = createAppointmentObject("testBug8724_I");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		final AppointmentObject appointmentListObject = new AppointmentObject();
		appointmentListObject.setObjectID(objectId+1000);
		appointmentListObject.setParentFolderID(appointmentFolderId);
		
		try {
			listAppointment(getWebConversation(), new AppointmentObject[] { appointmentListObject }, _appointmentFields, timeZone, getHostName(), getSessionId());
			fail("object not found exception expected");
		} catch (TestException exc) {
			assertTrue(true);
		}
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getSessionId());
	}
	
	/**
	 * This test checks if the list action return an object not found exception 
	 * if one id is requested trhat doesn't exist
	 */
	public void testBug8724_II() throws Exception {
		final AppointmentObject appointmentObj = createAppointmentObject("testBug8724_II");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		final AppointmentObject appointmentListObject1 = new AppointmentObject();
		appointmentListObject1.setObjectID(objectId+1000);
		appointmentListObject1.setParentFolderID(appointmentFolderId);

		final AppointmentObject appointmentListObject2 = new AppointmentObject();
		appointmentListObject2.setObjectID(objectId+1001);
		appointmentListObject2.setParentFolderID(appointmentFolderId);
		
		final AppointmentObject[] appointmentArray = { appointmentListObject1, appointmentListObject2 };
		
		try {
			listAppointment(getWebConversation(), appointmentArray, _appointmentFields, timeZone, getHostName(), getSessionId());
			fail("object not found exception expected");
		} catch (TestException exc) {
			assertTrue(true);
		}
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getSessionId());
	}
}
