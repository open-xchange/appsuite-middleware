package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug8196Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug8196Test.class);
	
	public Bug8196Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug8196() throws Exception {
		FolderObject folderObj = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), getHostName(), getSecondLogin(), getPassword());
		int secondAppointmentFolderId = folderObj.getObjectID();
		int secondUserId = folderObj.getCreatedBy();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8196");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarm(15);
		
		final Participant[] userParticipant = new UserParticipant[2];
		userParticipant[0] = new UserParticipant();
		userParticipant[0].setIdentifier(userId);

		userParticipant[1] = new UserParticipant();
		userParticipant[1].setIdentifier(secondUserId);
		
		appointmentObj.setParticipants(userParticipant);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		
		appointmentObj.removeAlarm();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		appointmentObj.removeAlarm();
		appointmentObj.setAlarmFlag(false);
		appointmentObj.setParentFolderID(secondAppointmentFolderId);

		AppointmentObject loadAppointment = loadAppointment(getSecondWebConversation(), objectId, secondAppointmentFolderId, getHostName(), getSecondLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = new Date(loadAppointment.getLastModified().getTime()-1000);
		
		loadAppointment = loadAppointment(getSecondWebConversation(), objectId, secondAppointmentFolderId, modified, getHostName(), getSecondLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
}