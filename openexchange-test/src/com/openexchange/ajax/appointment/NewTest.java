package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

public class NewTest extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(NewTest.class);
	
	public NewTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSimple() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testSimple");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testFullTime() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date start = c.getTime();
        
        c.add(Calendar.DAY_OF_MONTH, 1);
        
		final Date end = c.getTime();
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testFullTime");
		appointmentObj.setStartDate(start);
		appointmentObj.setEndDate(end);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);
        
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}


    public void testFullTimeOverTwoDays() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date start = c.getTime();
        
        c.add(Calendar.DAY_OF_MONTH, 2);
        
		final Date end = c.getTime();
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testFullTime");
		appointmentObj.setStartDate(start);
		appointmentObj.setEndDate(end);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, start.getTime(), end.getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}


    public void testServerShouldRoundDownFullTimeAppointments() throws Exception {
        final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 13);

        final Date end = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final Date expectedStart = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);

		final Date expectedEnd = c.getTime();

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testFullTime rounds down");
		appointmentObj.setStartDate(start);
		appointmentObj.setEndDate(end);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }

    public void testShouldRoundFullTimeSeriesAsWell() throws Exception {
        final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, Calendar.APRIL);

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date start = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 13);

        final Date end = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final Date expectedStart = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);

		final Date expectedEnd = c.getTime();

		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testFullTime rounds down");
		appointmentObj.setStartDate(start);
		appointmentObj.setEndDate(end);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setFullTime(true);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setInterval(1);
        appointmentObj.setDayInMonth(1);
        appointmentObj.setMonth(Calendar.APRIL);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        appointmentObj.setStartDate(expectedStart);
        appointmentObj.setEndDate(expectedEnd);

        compareObject(appointmentObj, loadAppointment, expectedStart.getTime(), expectedEnd.getTime());

        // load a recurrence

        loadAppointment = loadAppointment(getWebConversation(), objectId, 2, appointmentFolderId, c.getTimeZone(), PROTOCOL + getHostName(), getSessionId());

        Calendar check = new GregorianCalendar();
        check.setTimeZone(c.getTimeZone());
        check.setTime(loadAppointment.getStartDate());

        assertEquals(0, check.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, check.get(Calendar.MINUTE));
        assertEquals(0, check.get(Calendar.SECOND));
        assertEquals(0, check.get(Calendar.MILLISECOND));

        check.setTime(loadAppointment.getEndDate());

        assertEquals(0, check.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, check.get(Calendar.MINUTE));
        assertEquals(0, check.get(Calendar.SECOND));
        assertEquals(0, check.get(Calendar.MILLISECOND));


        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }

    public void testUserParticipant() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUserParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGroupParticipant() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testGroupParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new GroupParticipant();
		participants[1].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testResourceParticipant() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testResourceParticipant");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ResourceParticipant();
		participants[1].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testAllParticipants() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testAllParticipants");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, getHostName(), getSessionId())[0].getIdentifier();
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ResourceParticipant();
		participants[1].setIdentifier(resourceParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testSpecialCharacters() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testSpecialCharacters - \u00F6\u00E4\u00FC-:,;.#?!\u00A7$%&/()=\"<>");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testPrivateFolder() throws Exception {
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testPrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testPrivateFolder");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(targetFolder);
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testPublicFolder() throws Exception {
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testPublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, true);
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testPrivateFolder");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(targetFolder);
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSessionId());
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testSharedFolder() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getStandardCalendarFolder(getSecondWebConversation(), getHostName(), getSecondSessionId());
		final int secondUserId = sharedFolderObject.getCreatedBy();
		
		final FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testSharedFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, false);
		final OCLPermission[] permission = new OCLPermission[] {
			com.openexchange.webdav.xml.FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
			com.openexchange.webdav.xml.FolderTest.createPermission( secondUserId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false)
		};
		
		folderObj.setPermissionsAsArray(permission);
		
		final int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testSharedFolder");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(targetFolder);
		appointmentObj.setIgnoreConflicts(true);
                
		final int objectId = insertAppointment(getSecondWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getSecondWebConversation(), objectId, targetFolder, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getSecondWebConversation(), objectId, targetFolder, PROTOCOL + getHostName(), getSecondSessionId());
		com.openexchange.webdav.xml.FolderTest.deleteFolder(getWebConversation(), new int[] { targetFolder }, PROTOCOL + getHostName(), getLogin(), getPassword());
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
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testWeeklyRecurrence() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (10*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testWeeklyRecurrence");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.WEEKLY);
		appointmentObj.setDays(Appointment.SUNDAY + Appointment.MONDAY + Appointment.TUESDAY + Appointment.WEDNESDAY + Appointment.THURSDAY + Appointment.FRIDAY + Appointment.SATURDAY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testMonthlyRecurrenceDayInMonth() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (60*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testMonthlyRecurrenceDayInMonth");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.MONTHLY);
		appointmentObj.setDayInMonth(15);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testMonthlyRecurrenceDays() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (90*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testMonthlyRecurrenceDays");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.MONTHLY);
		appointmentObj.setDays(Appointment.WEDNESDAY);
		appointmentObj.setDayInMonth(3);
		appointmentObj.setInterval(2);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testYearlyRecurrenceDayInMonth() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (800*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testYearlyRecurrenceDayInMonth");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.YEARLY);
		appointmentObj.setMonth(Calendar.JULY);
		appointmentObj.setDayInMonth(15);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testYearlyRecurrenceDays() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (800*dayInMillis));
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testYearlyRecurrenceDays");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.YEARLY);
		appointmentObj.setMonth(Calendar.JULY);
		appointmentObj.setDays(Appointment.WEDNESDAY);
		appointmentObj.setDayInMonth(3);
		appointmentObj.setInterval(2);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, loadAppointment.getStartDate().getTime(), loadAppointment.getEndDate().getTime());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testConflict() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testConflict");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		try {
			appointmentObj.setIgnoreConflicts(false);
			final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
			deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
			fail("conflict exception expected here!");
		} catch (final OXConflictException exc) {
			assertTrue(true);
		}
		
		appointmentObj.setIgnoreConflicts(true);
		final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		
		deleteAppointment(getWebConversation(), objectId1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testConflictWithResource() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testConflictWithResource");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int resourceParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ResourceParticipant();
		participants[1].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		try {
			appointmentObj.setIgnoreConflicts(false);
			final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
			deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
			assertEquals("conflict expected here object id should be 0", 0, objectId2);
		} catch (final OXConflictException exc) {
			assertTrue(true);
		}
		
		appointmentObj.setIgnoreConflicts(true);
		try {
			final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
			deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
			assertEquals("conflict expected here object id should be 0", 0, objectId2);
		} catch (final OXConflictException exc) {
			assertTrue(true);
		}
		
		deleteAppointment(getWebConversation(), objectId1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

