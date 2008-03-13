package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.ExternalGroupParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.XmlServlet;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NewTest extends AppointmentTest {
	
	public NewTest(String name) {
		super(name);
	}

	public void testNewAppointment() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithAlarm() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithAlarm");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarm(45);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		appointmentObj.setAlarmFlag(true);
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}

	public void testNewAppointmentWithParticipants() throws Exception {
		Date modified = new Date();
		
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
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithUsers() throws Exception {
		Date modified = new Date();
		
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
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	/**
     * FIXME the mail addresses are somehow not parsed in server and cause exceptions
	 */
    public void notestNewAppointmentWithExternalParticipants() throws Exception {
		Date modified = new Date();
		
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
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDailyRecurrence() throws Exception {
		Date modified = new Date();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyRecurrenceWithOccurrences() throws Exception {
		Date modified = new Date();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		int occurrences = 5;
		
		c.add(Calendar.DAY_OF_MONTH, (occurrences-1));
		
		Date until = c.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		appointmentObj.setUntil(until);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyFullTimeRecurrenceWithOccurrences() throws Exception {
		Date modified = new Date();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		int occurrences = 2;
		
		Date startDate = c.getTime();
		Date endDate = new Date(c.getTimeInMillis() + dayInMillis);
		
		Date until = new Date(c.getTimeInMillis() + ((occurrences-1)*dayInMillis));
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyFullTimeRecurrenceWithOccurrences");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setFullTime(true);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		appointmentObj.setUntil(until);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testAppointmentInPrivateFlagInPublicFolder() throws Exception {
		Date modified = new Date();
		
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testAppointmentInPrivateFlagInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testAppointmentInPrivateFlagInPublicFolder");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setIgnoreConflicts(true);

		try {
			int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteAppointment(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.USER_INPUT_STATUS);
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyRecurrenceWithDeletingFirstOccurrence() throws Exception {
		Date modified = new Date();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		int occurrences = 5;
		
		Date recurrenceDatePosition = c.getTime();
		
		c.add(Calendar.DAY_OF_MONTH, (occurrences-1));
		
		Date until = c.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyRecurrenceWithDeletingFirstOccurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, new Date(), recurrenceDatePosition, getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setUntil(until);
		appointmentObj.setDeleteExceptions(new Date[] { recurrenceDatePosition } );
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
	
	public void testAppointmentWithAttachment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testContactWithAttachment");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		// contactObj.setNumberOfAttachments(1);
		
		final AttachmentMetadata attachmentMeta = new AttachmentImpl();
		attachmentMeta.setAttachedId(objectId);
		attachmentMeta.setFolderId(appointmentFolderId);
		attachmentMeta.setFileMIMEType("text/plain");
		attachmentMeta.setModuleId(Types.APPOINTMENT);
		attachmentMeta.setFilename("test.txt");
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		AttachmentTest.insertAttachment(webCon, attachmentMeta, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, loadAppointment.getLastModified(), true, false, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
			}
		}
		
		assertTrue("task not found" , found);
	}
}