package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;

public class FreeBusyTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(FreeBusyTest.class);

	public FreeBusyTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testUserParticipant() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUserParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		final Appointment[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());

		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;

				appointmentObj.removeTitle();
				//appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}

		assertTrue("appointment with id " + objectId + " not found in free busy response!", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}

	public void testFullTimeUserParticipant() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final long newStartTime = c.getTimeInMillis();
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testFullTimeUserParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		final Appointment[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());

		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;

				appointmentObj.removeTitle();
				//appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}

		assertTrue("appointment with id " + objectId + " not found in free busy response!", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}

	public void testUserParticipantStatusFree() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUserParticipantStatusFree");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));
		final Appointment[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());

		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;

				appointmentObj.removeTitle();
				//appointmentObj.removeParentFolderID();
				compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
			}
		}

		assertTrue("appointment with id " + objectId + " was found in free busy response!", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}

	public void testResourceParticipantStatusFree() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testResourceParticipantStatusFree");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();

		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant(userId);
		participants[1] = new ResourceParticipant(resourceParticipantId);

		appointmentObj.setParticipants(participants);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		appointmentObj.removeParticipants();

		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*2));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*2));

		Appointment[] appointmentArray = getFreeBusy(getWebConversation(), userId, Participant.USER, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());

		boolean found = false;

		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				found = true;

				appointmentObj.removeTitle();
				//appointmentObj.removeParentFolderID();
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

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
	}
}

