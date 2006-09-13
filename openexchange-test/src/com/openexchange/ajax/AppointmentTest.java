package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.URLParameter;

public class AppointmentTest extends AbstractAJAXTest {
	
	public AppointmentTest(String name) {
		super(name);
	}

	public static final int[] APPOINTMENT_FIELDS = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		AppointmentObject.LOCATION,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL
	};
	
	private static final String APPOINTMENT_URL = "/ajax/calendar";
	
	private static int appointmentFolderId = -1;
	
	private static long startTime = 0;
	
	private static long endTime = 0;
	
	private static final long d7 = 604800000;
	
	private String userParticipant2 = null;
	
	private String userParticipant3 = null;
	
	private String groupParticipant = null;
	
	private String resourceParticipant = null;
	
	private static TimeZone timeZone = TimeZone.getDefault();
	
	private static final Log LOG = LogFactory.getLog(AppointmentTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			String values[] = LoginTest.getSessionIdWithUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			sessionId = values[0];
			userId = Integer.parseInt(values[1]);
			
			Calendar c = Calendar.getInstance();
			c.setTimeZone(timeZone);
			c.set(Calendar.HOUR_OF_DAY, 8);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			startTime = c.getTimeInMillis();
			startTime += timeZone.getOffset(startTime);
			endTime = startTime + 3600000;
			
			userParticipant2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
			userParticipant3 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
			
			groupParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "group_participant", "");
			
			resourceParticipant = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "resource_participant", "");
			
			final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
			appointmentFolderId = folderObj.getObjectID();
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new Exception(ex);
		}
	}
	
	public void testNewAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant2, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateAppointmentWithParticipant() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentWithParticipants");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setFullTime(true);
		appointmentObj.setLocation(null);
		appointmentObj.setObjectID(objectId);
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testAll() throws Exception {
		Date start = new Date(System.currentTimeMillis()-d7);
		Date end = new Date(System.currentTimeMillis()+d7);
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testList() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testList");
		int id1 = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		int id3 = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { id1, appointmentFolderId }, { id2, appointmentFolderId }, { id3, appointmentFolderId } };
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, cols, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, appointmentArray.length);
	}
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		confirmAppointment(getWebConversation(), objectId, AppointmentObject.ACCEPT, null, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		int id = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGet() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testGet");
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
	}
	
	public void testGetWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testGetWithParticipants");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
	}
	
	public void testGetWithAllFields() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testGetWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		long newStartTime = c.getTimeInMillis();
		long newEndTime = newStartTime + 86400000;
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
	}
	
	public void testGetWithAllFieldsOnUpdate() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testGetWithAllFieldsOnUpdate");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		appointmentObj.removeParentFolderID();
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		long newStartTime = c.getTimeInMillis();
		long newEndTime = newStartTime + 86400000;
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setParentFolderID(appointmentFolderId);
		compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
	}
	
	public void testListWithAllFields() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setFullTime(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		int userParticipantId = ContactTest.searchContact(getWebConversation(), userParticipant3, FolderObject.SYSTEM_LDAP_FOLDER_ID, new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + getHostName(), getSessionId())[0].getInternalUserId();
		int groupParticipantId = GroupTest.searchGroup(getWebConversation(), groupParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		int resourceParticipantId = ResourceTest.searchResource(getWebConversation(), resourceParticipant, PROTOCOL + getHostName(), getSessionId())[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { objectId, appointmentFolderId } };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), objectIdAndFolderId, APPOINTMENT_FIELDS, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 1, appointmentArray.length);
		
		AppointmentObject loadAppointment = appointmentArray[0];
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		long newStartTime = c.getTimeInMillis();
		long newEndTime = newStartTime + 86400000;
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setParentFolderID(appointmentFolderId);
		compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
	}
	
	public void testHasAppointment() throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, "has");
		parameter.setParameter(AJAXServlet.PARAMETER_START, new Date(System.currentTimeMillis()-d7));
		parameter.setParameter(AJAXServlet.PARAMETER_END, new Date(System.currentTimeMillis()+d7));
		
		WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		boolean isArray = ((JSONArray)response.getData()) instanceof JSONArray;
		assertTrue("response object is not an array", isArray);
	}
	
	public void testFreeBusy() throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ID, userId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_FREEBUSY);
		parameter.setParameter(AJAXServlet.PARAMETER_START, new Date(System.currentTimeMillis()-d7));
		parameter.setParameter(AJAXServlet.PARAMETER_END, new Date(System.currentTimeMillis()+d7));
		
		WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		boolean isArray = ((JSONArray)response.getData()) instanceof JSONArray;
		assertTrue("response object is not an array", isArray);
	}
	
	public void testSimpleSearch() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testSimpleSearch");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject[] appointmentArray = searchAppointment(getWebConversation(), getLogin(), appointmentFolderId, APPOINTMENT_FIELDS, PROTOCOL + getHostName(), getSessionId());
		assertTrue("appointment array size is 0", appointmentArray.length > 0);
	}
	
	private void compareObject(AppointmentObject appointmentObj1, AppointmentObject appointmentObj2, long newStartTime, long newEndTime) throws Exception {
		assertEquals("id", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
		assertEqualsAndNotNull("title", appointmentObj1.getTitle(), appointmentObj2.getTitle());
		assertEquals("start", newStartTime, appointmentObj2.getStartDate().getTime());
		assertEquals("end", newEndTime, appointmentObj2.getEndDate().getTime());
		assertEqualsAndNotNull("location", appointmentObj1.getLocation(), appointmentObj2.getLocation());
		assertEquals("shown_as", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
		assertEquals("folder id", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
		assertEquals("private flag", appointmentObj1.getPrivateFlag(), appointmentObj2.getPrivateFlag());
		assertEquals("full time", appointmentObj1.getFullTime(), appointmentObj2.getFullTime());
		assertEquals("label", appointmentObj1.getLabel(), appointmentObj2.getLabel());
		assertEqualsAndNotNull("note", appointmentObj1.getNote(), appointmentObj2.getNote());
		assertEqualsAndNotNull("categories", appointmentObj1.getCategories(), appointmentObj2.getCategories());
		
		assertEqualsAndNotNull("participants are not equals" , participants2String(appointmentObj1.getParticipants()), participants2String(appointmentObj2.getParticipants()));
	}
	
	private AppointmentObject createAppointmentObject(String title) {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSENT);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
	
	public static int insertAppointment(WebConversation webCon, AppointmentObject appointmentObj, String host, String session) throws Exception {
		int objectId = 0;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw, timeZone);
		appointmentwriter.writeAppointment(appointmentObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId = data.getInt(DataFields.ID);
		}
		
		return objectId;
	}
	
	public static void updateAppointment(WebConversation webCon, AppointmentObject appointmentObj, int objectId, int inFolder, String host, String session) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		AppointmentWriter appointmentwriter = new AppointmentWriter(pw, timeZone);
		appointmentwriter.writeAppointment(appointmentObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		parameter.setParameter(DataFields.ID, String.valueOf(objectId));
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(inFolder));
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static void deleteAppointment(WebConversation webCon, int id, int inFolder, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, id);
		jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static void confirmAppointment(WebConversation webCon, int objectId, int confirm, String confirmMessage, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CONFIRM);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, objectId);
		jsonObj.put(AJAXServlet.PARAMETER_CONFIRM, confirm);

		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon, int inFolder, int[] cols, Date start, Date end, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon, int[][] objectIdAndFolderId, int[] cols, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String( cols ));
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int i[] = objectIdAndFolderId[a];
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, i[0]);
			jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, i[1]);
			jsonArray.put(jsonObj);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon, int objectId, int inFolder, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(DataFields.ID, objectId);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		
		AppointmentParser appointmentParser = new AppointmentParser(timeZone);
		appointmentParser.parse(appointmentObj, (JSONObject)response.getData());
		
		return appointmentObj;
	}
	
	public static AppointmentObject[] listModifiedAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP_SINCE, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(new int[]{ AppointmentObject.OBJECT_ID }));
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData());
	}
	
	public static AppointmentObject[] listDeleteAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "changed");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP_SINCE, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(new int[]{ AppointmentObject.OBJECT_ID }));
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData());
	}
	
	public static AppointmentObject[] searchAppointment(WebConversation webCon, String searchpattern, int inFolder, int[] cols, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL + parameter.getURLParameters(), new ByteArrayInputStream(jsonObj.toString().getBytes()), "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
	private static AppointmentObject[] jsonArray2AppointmentArray(JSONArray jsonArray) throws Exception {
		AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray.length()];
		
		AppointmentParser appointmentParser = new AppointmentParser(timeZone);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			appointmentArray[a] = new AppointmentObject();
			JSONObject jObj = jsonArray.getJSONObject(a);
			
			appointmentParser.parse(appointmentArray[a], jObj);
		}
		
		return appointmentArray;
	}
	
	private static AppointmentObject[] jsonArray2AppointmentArray(JSONArray jsonArray, int[] cols) throws Exception {
		AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray.length()];
		
		for (int a = 0; a < appointmentArray.length; a++) {
			appointmentArray[a] = new AppointmentObject();
			parseCols(cols, jsonArray.getJSONArray(a), appointmentArray[a]);
		}
		
		return appointmentArray;
	}
	
	private static void parseCols(int[] cols, JSONArray jsonArray, AppointmentObject appointmentObj) throws Exception {
		assertEquals("compare array size with cols size", cols.length, jsonArray.length());
		
		for (int a = 0; a < cols.length; a++) {
			parse(a, cols[a], jsonArray, appointmentObj);
		}
	}
	
	private static void parse(int pos, int field, JSONArray jsonArray, AppointmentObject appointmentObj) throws Exception {
		switch (field) {
			case AppointmentObject.OBJECT_ID:
				appointmentObj.setObjectID(jsonArray.getInt(pos));
				break;
			case AppointmentObject.FOLDER_ID:
				appointmentObj.setParentFolderID(jsonArray.getInt(pos));
				break;
			case AppointmentObject.TITLE:
				appointmentObj.setTitle(jsonArray.getString(pos));
				break;
			case AppointmentObject.START_DATE:
				appointmentObj.setStartDate(new Date(jsonArray.getLong(pos)));
				break;
			case AppointmentObject.END_DATE:
				appointmentObj.setEndDate(new Date(jsonArray.getLong(pos)));
				break;
			case AppointmentObject.SHOWN_AS:
				appointmentObj.setShownAs(jsonArray.getInt(pos));
				break;
			case AppointmentObject.LOCATION:
				appointmentObj.setLocation(jsonArray.getString(pos));
				break;
			case AppointmentObject.FULL_TIME:
				appointmentObj.setFullTime(jsonArray.getBoolean(pos));
				break;
			case AppointmentObject.PRIVATE_FLAG:
				appointmentObj.setPrivateFlag(jsonArray.getBoolean(pos));
				break;
			case AppointmentObject.CATEGORIES:
				appointmentObj.setCategories(jsonArray.getString(pos));
				break;
			case AppointmentObject.COLOR_LABEL:
				appointmentObj.setLabel(jsonArray.getInt(pos));
				break;
			case AppointmentObject.NOTE:
				appointmentObj.setNote(jsonArray.getString(pos));
				break;
			case AppointmentObject.PARTICIPANTS:
				appointmentObj.setParticipants(parseParticipants(jsonArray.getJSONArray(pos)));
				break;
		}
	}
	
	private static Participant[] parseParticipants(JSONArray jsonArray) throws Exception {
			Participant[] participant = new Participant[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jparticipant = jsonArray.getJSONObject(i);
				int type = jparticipant.getInt("type");
				int id = jparticipant.getInt("id");
				Participant p = null;
				switch (type) {
					case Participant.USER:
						UserParticipant user = new UserParticipant();
						user.setIdentifier(id);

						p = user;
						break;
					case Participant.GROUP:
						p = new GroupParticipant();
						p.setIdentifier(id);
						break;
					case Participant.RESOURCE:
						p = new ResourceParticipant();
						p.setIdentifier(id);
						break;
					case Participant.RESOURCEGROUP:
						p = new ResourceGroupParticipant();
						p.setIdentifier(id);
						break;
					default:
						throw new OXConflictException("invalid type");
				}
				participant[i] = p;
			}
			
			return participant;
	}

	private HashSet participants2String(Participant[] participant) throws Exception {
		if (participant == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < participant.length; a++) {
			hs.add(participant2String(participant[a]));
		}
		
		return hs;
	}
	
	private String participant2String(Participant p) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("T" + p.getType());
		sb.append("ID" + p.getIdentifier());
		
		return sb.toString();
	}
}

