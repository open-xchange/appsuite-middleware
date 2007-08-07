package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.GroupTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FreeBusyTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(FreeBusyTest.class);
	
	public FreeBusyTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testUserParticipant() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUserParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		AppointmentObject[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				
				appointmentObj.removeTitle();
				appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}
		
		assertTrue("appointment with id " + objectId + " not found in free busy response!", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testFullTimeUserParticipant() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		long newStartTime = c.getTimeInMillis();
		long newEndTime = newStartTime + dayInMillis;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testFullTimeUserParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		AppointmentObject[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				
				appointmentObj.removeTitle();
				appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}
		
		assertTrue("appointment with id " + objectId + " not found in free busy response!", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUserParticipantStatusFree() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUserParticipantStatusFree");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		AppointmentObject[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				
				appointmentObj.removeTitle();
				appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}
		
		assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testResourceParticipantStatusFree() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testResourceParticipantStatusFree");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ResourceParticipant();
		participants[1].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		appointmentObj.removeParticipants();
		
		Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		
		AppointmentObject[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				
				appointmentObj.removeTitle();
				appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}
		
		assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
		
		appointmentArray = getFreeBusy(getWebConversation(), resourceParticipantId, Participant.RESOURCE, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;
				
				appointmentObj.removeTitle();
				appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}
		
		assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

