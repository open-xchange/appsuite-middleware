package com.openexchange.ajax.appointment;

import static com.openexchange.ajax.framework.ListIDs.l;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.GroupTest;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

public class ListTest extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(ListTest.class);
	
	public ListTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testList() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testList");
		appointmentObj.setIgnoreConflicts(true);
		
		final int id1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		final int id2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		final int id3 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { id1, appointmentFolderId }, { id2, appointmentFolderId }, { id3, appointmentFolderId } };
		
		final int cols[] = new int[]{ Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };
		
		final Appointment[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, cols, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, appointmentArray.length);
		
		deleteAppointment(getWebConversation(), id1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id3, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testListWithNoEntries() throws Exception {
		final Appointment appointmentObj = createAppointmentObject("testList");
		appointmentObj.setIgnoreConflicts(true);
		final int id1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		final int id2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		final int id3 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { };
		
		final int cols[] = new int[]{ Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };
		
		final Appointment[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, cols, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 0, appointmentArray.length);
		
		deleteAppointment(getWebConversation(), id1, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), id3, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testListWithAllFields() throws Exception {
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(Appointment.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		//appointmentObj.setPrivateFlag(true); // Currently not supported!
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		final int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { Contact.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		final int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, getHostName(), getSessionId())[0].getIdentifier();
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
		appointmentObj.setIgnoreConflicts(true);
		
		try {
			final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
			
			final int[][] objectIdAndFolderId = { { objectId, appointmentFolderId } };
			
			final Appointment[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, APPOINTMENT_FIELDS, timeZone, PROTOCOL + getHostName(), getSessionId());
			
			assertEquals("check response array", 1, appointmentArray.length);
			
			final Appointment loadAppointment = appointmentArray[0];
			
			final Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			final long newStartTime = c.getTimeInMillis();
			final long newEndTime = newStartTime + 86400000;
			
			appointmentObj.setObjectID(objectId);
			appointmentObj.setParentFolderID(appointmentFolderId);
			compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
			
			deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		} catch (final OXConflictException exc) {
			LOG.warn("Conflict Exception found. Maybe test result is wrong: " + exc);
		}
	}

	public void testListWithRecurrencePosition() throws Exception {
		final int cols[] = new int[]{ Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS, Appointment.RECURRENCE_POSITION };
		
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testListWithRecurrencePosition" + System.currentTimeMillis());
		folderObj.setParentFolderID(FolderObject.PUBLIC);
		folderObj.setModule(FolderObject.CALENDAR);
		folderObj.setType(FolderObject.PUBLIC);
		
		final OCLPermission[] permission = new OCLPermission[] {
			com.openexchange.webdav.xml.FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int publicFolderId = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, getHostName(), getLogin(), getPassword());
		
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		final Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testListWithRecurrencePosition");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(publicFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId1 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj = new Appointment();
		appointmentObj.setTitle("testListWithRecurrencePosition2");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setParentFolderID(appointmentFolderId);
		final int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final Appointment[] appointmentList = new Appointment[3];
		appointmentList[0] = new Appointment();
		appointmentList[0].setObjectID(objectId1);
		appointmentList[0].setParentFolderID(publicFolderId);
		appointmentList[0].setRecurrencePosition(2);
		appointmentList[1] = new Appointment();
		appointmentList[1].setObjectID(objectId1);
		appointmentList[1].setParentFolderID(publicFolderId);
		appointmentList[1].setRecurrencePosition(3);
		appointmentList[2] = new Appointment();
		appointmentList[2].setObjectID(objectId2);
		appointmentList[2].setParentFolderID(appointmentFolderId);
		
		final Appointment[] appointmentArray = AppointmentTest.listAppointment(getWebConversation(), appointmentList, cols, timeZone, getHostName(), getSessionId());
		
		assertEquals("3 elements expected", 3, appointmentArray.length);
		
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId1 && appointmentArray[a].getRecurrencePosition() == 2) {
				found1 = true;
			} else if (appointmentArray[a].getObjectID() == objectId1 && appointmentArray[a].getRecurrencePosition() == 3) {
				found2 = true;
			} else if (appointmentArray[a].getObjectID() == objectId2) {
				found3 = true;
			}
		}
		
		assertTrue("not all objects in response", (found1 && found2 && found3));
		
		deleteAppointment(getWebConversation(), objectId1, publicFolderId, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), objectId2, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[]{ Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC};

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60*60*1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
        try {
            final ListRequest listRequest = new ListRequest(l(new int[] {appointmentFolderId, objectId}), cols, true);
            final CommonListResponse response = Executor.execute(client, listRequest);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);

            for(int i = 0; i < size; i++ ){
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
        }
    }
}
