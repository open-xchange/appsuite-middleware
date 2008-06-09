package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.resource.Resource;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.GroupUserTest;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug8123Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug8123Test.class);
	
	public Bug8123Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug8123() throws Exception {
		final Resource[] resource = GroupUserTest.searchResource(getWebConversation(), "*", new Date(0), getHostName(), getLogin(), getPassword());
		
		if (resource.length == 0) {
			fail("no resource found for this test");
		}
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8123");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		Participant[] participant = new Participant[2];
		participant[0] = new UserParticipant();
		participant[0].setIdentifier(userId);
		participant[1] = new ResourceParticipant();
		participant[1].setIdentifier(resource[0].getIdentifier());
		
		appointmentObj.setParticipants(participant);

		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = new Date(loadAppointment.getCreationDate().getTime()-1000);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}