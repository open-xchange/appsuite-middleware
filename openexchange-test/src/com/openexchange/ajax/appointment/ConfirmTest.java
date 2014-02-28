package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;

public class ConfirmTest extends AppointmentTest {

	public ConfirmTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDummy() throws Exception {

	}

    public void testConfirm() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getStandardCalendarFolder(getSecondWebConversation(), getHostName(), getSecondSessionId());
		final int secondUserId = sharedFolderObject.getCreatedBy();

        final Appointment appointmentObj = createAppointmentObject("testConfirm");
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(secondUserId);

		appointmentObj.setParticipants(participants);

		appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        confirmAppointment(getSecondWebConversation(), objectId, sharedFolderObject.getObjectID(), Appointment.ACCEPT, "Yap.", PROTOCOL + getHostName(), getSecondSessionId());

		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());

		boolean found = false;

		final UserParticipant[] users = loadAppointment.getUsers();
		for (UserParticipant user : users) {
			if (user.getIdentifier() == secondUserId) {
				found = true;
				assertEquals("wrong confirm status", Appointment.ACCEPT, user.getConfirm());
			}
		}

		assertTrue("user participant with id " + secondUserId + " not found", found);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId(), false);
    }
}
