package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;

public class UpdateTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(UpdateTest.class);
	
	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		Appointment.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.INTERVAL,
		CalendarObject.RECURRENCE_COUNT,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		Appointment.SHOWN_AS,
		Appointment.FULL_TIME,
		Appointment.COLOR_LABEL,
		Appointment.TIMEZONE,
		Appointment.RECURRENCE_START
	};
	
	public UpdateTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSimple() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testSimple");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(Appointment.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateAppointmentWithParticipant() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(Appointment.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, getHostName(), getSessionId())[0].getIdentifier();
		final int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant(userId);
		participants[1] = new UserParticipant(userParticipantId);
		participants[2] = new GroupParticipant(groupParticipantId);
		participants[3] = new ResourceParticipant(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateRecurrenceWithPosition() throws Exception {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		final int changeExceptionPosition = 3;
		
		Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpdateRecurrence");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		
		final long newStartTime = startTime + 60*60*1000;
		final long newEndTime = endTime + 60*60*1000;
		
		appointmentObj = new Appointment();
		appointmentObj.setTitle("testUpdateRecurrence - exception");
		appointmentObj.setStartDate(new Date(newStartTime));
		appointmentObj.setEndDate(new Date(newEndTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrencePosition(changeExceptionPosition);
		appointmentObj.setIgnoreConflicts(true);
		
		final int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);
		appointmentObj.setObjectID(newObjectId);
		
		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());

        // Loaded change exception MUST NOT contain any recurrence information except recurrence identifier and position.
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}

    // Node 356

    public void testShiftRecurrenceAppointment() throws Exception {
		final Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		final Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		
		final Appointment appointmentObj = createAppointmentObject("testShiftRecurrenceAppointment");
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(5);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		
		final Date startDate = appointmentObj.getStartDate();
		final Date endDate = appointmentObj.getEndDate();
		
		final Calendar calendarStart = Calendar.getInstance(timeZone);
		final Calendar calendarEnd = Calendar.getInstance(timeZone);
		
		calendarStart.setTime(startDate);
		calendarStart.add(Calendar.DAY_OF_MONTH, 2);
		
		calendarEnd.setTime(endDate);
		calendarEnd.add(Calendar.DAY_OF_MONTH, 2);
		
		appointmentObj.setStartDate(calendarStart.getTime());
		appointmentObj.setEndDate(calendarEnd.getTime());
		
		final Calendar recurrenceStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		final int startDay = calendarStart.get(Calendar.DAY_OF_MONTH);
		final int startMonth = calendarStart.get(Calendar.MONTH);
		final int startYear = calendarStart.get(Calendar.YEAR);
		recurrenceStart.set(startYear, startMonth, startDay, 0, 0, 0);
		recurrenceStart.set(Calendar.MILLISECOND, 0);
		
		appointmentObj.setRecurringStart(recurrenceStart.getTimeInMillis());
		
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		final Date modified = loadAppointment.getLastModified();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, modified, timeZone, getHostName(), getSessionId());

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());

        loadAppointment.removeUntil();   // TODO add expected until
        compareObject(appointmentObj, loadAppointment);
		
		final Appointment[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, new Date(0), _appointmentFields, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (objectId == appointmentArray[a].getObjectID()) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
				break;
			}
		}
		
		assertTrue("object with object_id: " + objectId + " not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}

    // Bug 12700    FIXME
    public void testMakeFullTime() throws Exception {
        final TimeZone utc = TimeZone.getTimeZone("urc");

        final Appointment appointmentObj = createAppointmentObject("testShiftRecurrenceAppointment");
        appointmentObj.setStartDate(D("04/01/2008 12:00"));
        appointmentObj.setEndDate(D("04/01/2008 14:00"));

        appointmentObj.setIgnoreConflicts(true);
        final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, utc, getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);        

        final Appointment update = new Appointment();
        update.setObjectID(objectId);
        update.setParentFolderID(appointmentFolderId);
        update.setFullTime(true);

        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, utc, getHostName(), getSessionId());
        final Date modified = loadAppointment.getLastModified();

        updateAppointment(getWebConversation(), update, objectId, appointmentFolderId, modified, utc, getHostName(), getSessionId());

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, utc, getHostName(), getSessionId());

        final Calendar check = new GregorianCalendar();
        check.setTimeZone(utc);
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
    }
}

