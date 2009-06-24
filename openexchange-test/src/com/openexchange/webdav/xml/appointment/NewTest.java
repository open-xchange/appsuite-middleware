package com.openexchange.webdav.xml.appointment;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.openexchange.group.Group;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class NewTest extends AppointmentTest {
	
	public NewTest(final String name) {
		super(name);
	}

	public void testNewAppointment() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testNewAppointment");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);

		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithAlarm() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithAlarm");
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setAlarm(45);
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		appointmentObj.setAlarmFlag(true);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}

	public void testNewAppointmentWithParticipants() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		
		final Group[] groupArray = GroupUserTest.searchGroup(getWebConversation(), groupParticipant, new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("group array size is not > 0", groupArray.length > 0);
		final int groupParticipantId = groupArray[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant(userId);
		participants[1] = new GroupParticipant(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();		
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testNewAppointmentWithUsers() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithUsers");
		appointmentObj.setIgnoreConflicts(true);
		
		final int userParticipantId = GroupUserTest.getUserId(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		final UserParticipant[] users = new UserParticipant[2];
		users[0] = new UserParticipant(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		users[1] = new UserParticipant(userParticipantId);
		users[1].setConfirm(CalendarObject.DECLINE);
		
		appointmentObj.setUsers(users);
		appointmentObj.setParticipants(users);
		
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}

    public void testNewAppointmentWithExternalUserParticipants() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testNewAppointmentWithExternalParticipants");
		appointmentObj.setIgnoreConflicts(true);
		
		final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		final Participant[] participant = new Participant[2];
		participant[0] = new UserParticipant(userId);
		participant[1] = new ExternalUserParticipant("externaluser@example.org");
		
		appointmentObj.setParticipants(participant);
		
		final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDailyRecurrence() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyRecurrenceWithOccurrences() throws Exception {
		final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTime(startTime);
        c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final int occurrences = 5;
		
		c.add(Calendar.DAY_OF_MONTH, (occurrences-1));
		
		final Date until = c.getTime();
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyRecurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		appointmentObj.setUntil(until);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyFullTimeRecurrenceWithOccurrences() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final int occurrences = 2;
		
		final Date startDate = c.getTime();
		final Date endDate = new Date(c.getTimeInMillis() + dayInMillis);
		
		final Date until = new Date(startDate.getTime() + ((occurrences-1)*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyFullTimeRecurrenceWithOccurrences");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setFullTime(true);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		appointmentObj.setObjectID(objectId);
		appointmentObj.setUntil(until);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testAppointmentInPrivateFlagInPublicFolder() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testAppointmentInPrivateFlagInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		final OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testAppointmentInPrivateFlagInPublicFolder");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(parentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setIgnoreConflicts(true);

		try {
			final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteAppointment(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict exception expected!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), "APP-0070");
		}
		
		FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword());
	}
	
	public void testDailyRecurrenceWithDeletingFirstOccurrence() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final int occurrences = 5;
		
		final Date recurrenceDatePosition = c.getTime();
		
		c.add(Calendar.DAY_OF_MONTH, (occurrences-1));
		
		final Date until = c.getTime();
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyRecurrenceWithDeletingFirstOccurrence");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(occurrences);
		
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		Date modified = loadAppointment.getLastModified();
        appointmentObj.setObjectID(objectId);
        appointmentObj.setUntil(until);
        compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, recurrenceDatePosition, getHostName(), getLogin(), getPassword());
		appointmentObj.setDeleteExceptions(new Date[] { recurrenceDatePosition } );
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
	
	public void testAppointmentWithAttachment() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testContactWithAttachment");
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
		
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, loadAppointment.getLastModified(), true, false, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
			}
		}
		
		assertTrue("appointment not found" , found);
	}
}