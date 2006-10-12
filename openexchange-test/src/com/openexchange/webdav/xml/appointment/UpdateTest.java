package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UpdateTest extends AppointmentTest {
	
	public void testUpdateAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		appointmentObj = createAppointmentObject("testUpdateAppointment2");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setIgnoreConflicts(true);
		
		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void _notestUpdateAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		
		ContactObject[] contactArray = GroupUserTest.searchUser(webCon, userParticipant3, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is not > 0", contactArray.length > 0);
		int userParticipantId = contactArray[0].getInternalUserId();
		Group[] groupArray = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		Resource[] resourceArray = GroupUserTest.searchResource(webCon, resourceParticipant, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("resource array size is not > 0", resourceArray.length > 0);
		int resourceParticipantId = resourceArray[0].getIdentifier();
		
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
		
		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void _notestUpdateRecurrenceWithDatePosition() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		int changeExceptionPosition = 3;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUpdateRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password);
		compareObject(appointmentObj, loadAppointment);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUpdateRecurrence - exception");
		appointmentObj.setStartDate(new Date(startTime.getTime() + 60*60*1000));
		appointmentObj.setEndDate(new Date(endTime.getTime() + 60*60*1000));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceDatePosition(new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis));
		
		int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), login, password);
		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);
		
		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, PROTOCOL + getHostName(), login, password);
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), new int[][] { { objectId }, { appointmentFolderId } }, PROTOCOL + getHostName(), login, password);
	}
}

