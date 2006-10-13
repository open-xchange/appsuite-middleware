package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.GroupParticipant;
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

	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		Group[] groupArray = GroupUserTest.searchGroup(getWebConversation(), groupParticipant, new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithUsers() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithUsers");
		appointmentObj.setIgnoreConflicts(true);
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		UserParticipant[] users = new UserParticipant[2];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		users[1] = new UserParticipant();
		users[1].setIdentifier(userParticipantId);
		users[1].setConfirm(CalendarObject.DECLINE);
		
		appointmentObj.setUsers(users);
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );

	}
}

