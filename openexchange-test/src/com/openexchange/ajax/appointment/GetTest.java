package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
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
		final Appointment appointmentObj = createAppointmentObject("testGet");
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithParticipants() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testGetWithParticipants");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
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
		
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithAllFields() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testGetWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
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
		
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
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
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testGetWithAllFieldsOnUpdate");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
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
		
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
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

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60*60*1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
        try {
            final GetRequest getRequest = new GetRequest(appointmentFolderId, objectId);
            final GetResponse response = Executor.execute(client, getRequest);
            final JSONObject appointment = (JSONObject) response.getResponse().getData();

            assertNotNull(appointment);
            assertTrue(appointment.has("last_modified_utc"));

        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
        }
    }
}