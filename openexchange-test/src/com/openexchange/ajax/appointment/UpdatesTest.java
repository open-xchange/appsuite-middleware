package com.openexchange.ajax.appointment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
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
		CalendarObject.INTERVAL,
		CalendarObject.RECURRING_OCCURRENCE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		CalendarDataObject.TIMEZONE
	};

	private static final Log LOG = LogFactory.getLog(UpdatesTest.class);
	
	public UpdatesTest(String name) {
		super(name);
	}
	
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
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		Date since = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedWithoutFolderId");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertTrue("no appointment object in response", appointmentArray.length > 0);
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("created object not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
	
	public void testModifiedWithoutFolderIdExtended() throws Exception {
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		Date since = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedWithoutFolderIdExtended");
		appointmentObj.setIgnoreConflicts(true);
		int objectId1 = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertTrue("no appointment object in response", appointmentArray.length > 0);
		boolean found1 = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId1) {
				found1 = true;
			}
		}
		
		assertTrue("created object not found in response", found1);
		
		since = new Date();
		
		int objectId2 = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
		
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
		
		deleteAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, getHostName(), getSessionId());
	}
	
	public void testModifiedWithoutFolderIdNoResponse() throws Exception {
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedWithoutFolderIdNoResponse");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		Date since = new Date();
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertFalse("created object found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
	
	public void testModifiedWithoutFolderIdWithFutureTimestamp() throws Exception {
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedWithoutFolderIdWithFutureTimestamp");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		Date since = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, _appointmentFields, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("unexpected data in response", 0, appointmentArray.length);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
	
	public void testModifiedRecurrenceAppointment() throws Exception {
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedRecurrenceAppointment");
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(5);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, new Date(0), _appointmentFields, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
				break;
			}
		}
		
		assertTrue("object with object_id: " + objectId + " not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
}

