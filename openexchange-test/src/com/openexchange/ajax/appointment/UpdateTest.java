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

public class UpdateTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	public UpdateTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSimple() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testSimple");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateAppointmentWithParticipant() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
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
		
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void _testPrivateFolder() throws Exception {
		fail("not implemented!");
	}
	
	public void _notestPublicFolder() throws Exception {
		fail("not implemented!");
	}
	
	public void _notestSharedFolder() throws Exception {
		fail("not implemented!");
	}
	
	public void _notestUpdateRecurrenceWithPosition() throws Exception {
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
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testUpdateRecurrence - exception");
		appointmentObj.setStartDate(new Date(startTime + 60*60*1000));
		appointmentObj.setEndDate(new Date(endTime + 60*60*1000));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrencePosition(changeExceptionPosition);
		
		int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);
		
		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

