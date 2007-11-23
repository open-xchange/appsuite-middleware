package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.GroupTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListTest extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(ListTest.class);
	
	public ListTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testList() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testList");
		appointmentObj.setIgnoreConflicts(true);
		
		int id1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		int id3 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { id1, appointmentFolderId }, { id2, appointmentFolderId }, { id3, appointmentFolderId } };
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, cols, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, appointmentArray.length);
		
		deleteAppointment(getWebConversation(), id1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id3, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testListWithNoEntries() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testList");
		appointmentObj.setIgnoreConflicts(true);
		int id1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		int id3 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { };
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, cols, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 0, appointmentArray.length);
		
		deleteAppointment(getWebConversation(), id1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id3, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testListWithAllFields() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		//appointmentObj.setPrivateFlag(true); // Currently not supported!
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
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
		
		try {
			int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
			
			final int[][] objectIdAndFolderId = { { objectId, appointmentFolderId } };
			
			AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, APPOINTMENT_FIELDS, timeZone, PROTOCOL + getHostName(), getSessionId());
			
			assertEquals("check response array", 1, appointmentArray.length);
			
			AppointmentObject loadAppointment = appointmentArray[0];
			
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			long newStartTime = c.getTimeInMillis();
			long newEndTime = newStartTime + 86400000;
			
			appointmentObj.setObjectID(objectId);
			appointmentObj.setParentFolderID(appointmentFolderId);
			compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
			
			deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		} catch (OXConflictException exc) {
			LOG.warn("Conflict Exception found. Maybe test result is wrong: " + exc);
		}
	}

	public void testListWithRecurrencePosition() throws Exception {
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS, AppointmentObject.RECURRENCE_POSITION };
		
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testListWithRecurrencePosition" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		
		OCLPermission[] permission = new OCLPermission[] {
			com.openexchange.webdav.xml.FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int publicFolderId = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithRecurrencePosition");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(publicFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithRecurrencePosition2");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setParentFolderID(appointmentFolderId);
		final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject[] appointmentList = new AppointmentObject[3];
		appointmentList[0] = new AppointmentObject();
		appointmentList[0].setObjectID(objectId1);
		appointmentList[0].setParentFolderID(publicFolderId);
		appointmentList[0].setRecurrencePosition(2);
		appointmentList[1] = new AppointmentObject();
		appointmentList[1].setObjectID(objectId1);
		appointmentList[1].setParentFolderID(publicFolderId);
		appointmentList[1].setRecurrencePosition(3);
		appointmentList[2] = new AppointmentObject();
		appointmentList[2].setObjectID(objectId2);
		appointmentList[2].setParentFolderID(appointmentFolderId);
		
		AppointmentObject[] appointmentArray = AppointmentTest.listAppointment(getWebConversation(), appointmentList, cols, timeZone, getHostName(), getSessionId());
		
		assertEquals("3 elements expected", 3, appointmentArray.length);
		
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId1 && appointmentArray[a].getRecurrencePosition() == 2) {
				found1 = true;
			} else if (appointmentArray[a].getObjectID() == objectId1 && appointmentArray[a].getRecurrencePosition() == 3) {
				found2 = true;
			} else if (appointmentArray[a].getObjectID() == objectId2) {
				found3 = true;
			}
		}
		
		assertTrue("not all objects in response", (found1 && found2 && found3));
		
		deleteAppointment(getWebConversation(), objectId1, publicFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}
