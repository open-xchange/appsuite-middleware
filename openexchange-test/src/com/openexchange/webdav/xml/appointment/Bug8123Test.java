package com.openexchange.webdav.xml.appointment;

import java.util.Date;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.resource.Resource;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class Bug8123Test extends AppointmentTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug8123Test.class);

	public Bug8123Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBug8123() throws Exception {
		final Resource[] resource = GroupUserTest.searchResource(getWebConversation(), "*", new Date(0), getHostName(), getLogin(), getPassword(), context);

		if (resource.length == 0) {
			fail("no resource found for this test");
		}

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testBug8123");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);

		final Participant[] participant = new Participant[2];
		participant[0] = new UserParticipant();
		participant[0].setIdentifier(userId);
		participant[1] = new ResourceParticipant();
		participant[1].setIdentifier(resource[0].getIdentifier());

		appointmentObj.setParticipants(participant);

		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);

		final Date modified = new Date(loadAppointment.getCreationDate().getTime()-1000);

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword(), context);
		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
	}
}
