package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;
import java.util.Date;

public class NewTest extends AppointmentTest {
	
	public void testNewAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void _notestNewAppointmentWithAlarm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithAlarm");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarm(45);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}

	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		
		Group[] groupArray = GroupUserTest.searchGroup(getWebConversation(), groupParticipant, new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new GroupParticipant();
		participants[1].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithUsers() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithUsers");
		appointmentObj.setIgnoreConflicts(true);
		
		int userParticipantId = GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		UserParticipant[] users = new UserParticipant[2];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		users[1] = new UserParticipant();
		users[1].setIdentifier(userParticipantId);
		users[1].setConfirm(CalendarObject.DECLINE);
		
		appointmentObj.setUsers(users);
		appointmentObj.setParticipants(users);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void _notestNewAppointmentWithExternalParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithExternalParticipants");
		appointmentObj.setIgnoreConflicts(true);
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		Participant[] participant = new Participant[3];
		participant[0] = new UserParticipant();
		participant[0].setIdentifier(userId);
		participant[1] = new ExternalUserParticipant();
		participant[1].setEmailAddress("externaluser@example.org");
		participant[2] = new ExternalGroupParticipant();
		participant[2].setEmailAddress("externalgroup@example.org");
		
		appointmentObj.setParticipants(participant);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
}

