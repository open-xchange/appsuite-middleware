package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.GroupTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
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

	public void testListWithAllFields() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
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
	}
}