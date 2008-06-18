package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;

public class GetTest extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(GetTest.class);
	
	public GetTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGet() throws Exception {
		final AppointmentObject appointmentObj = createAppointmentObject("testGet");
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithParticipants() throws Exception {
		final AppointmentObject appointmentObj = createAppointmentObject("testGetWithParticipants");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithAllFields() throws Exception {
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testGetWithAllFields");
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
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final long newStartTime = c.getTimeInMillis();
		final long newEndTime = newStartTime + dayInMillis;
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithAllFieldsOnUpdate() throws Exception {
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testGetWithAllFieldsOnUpdate");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final long newStartTime = c.getTimeInMillis();
		final long newEndTime = newStartTime + dayInMillis;
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setParentFolderID(appointmentFolderId);
		compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}