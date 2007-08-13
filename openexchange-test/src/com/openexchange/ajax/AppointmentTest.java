package com.openexchange.ajax;

import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.tools.StringCollection;
import java.io.ByteArrayInputStream;
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
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceGroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;
import java.io.StringWriter;

public class AppointmentTest extends AbstractAJAXTest {
	
	public AppointmentTest(String name) {
		super(name);
	}
	
	public static final int[] APPOINTMENT_FIELDS = { DataObject.OBJECT_ID,
	DataObject.CREATED_BY, DataObject.CREATION_DATE,
	DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
	FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG,
	CommonObject.CATEGORIES, CalendarObject.TITLE,
	CalendarObject.START_DATE, CalendarObject.END_DATE,
	AppointmentObject.LOCATION, CalendarObject.NOTE,
	CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS,
	CalendarObject.USERS, AppointmentObject.SHOWN_AS,
	AppointmentObject.FULL_TIME, AppointmentObject.COLOR_LABEL };
	
	protected static final String APPOINTMENT_URL = "/ajax/calendar";
	
	protected static int appointmentFolderId = -1;
	
	protected static long startTime = 0;
	
	protected static long endTime = 0;
	
	protected static final long dayInMillis = 86400000;
	
	protected String userParticipant2 = null;
	
	protected String userParticipant3 = null;
	
	protected String groupParticipant = null;
	
	protected String resourceParticipant = null;
	
	protected int userId = 0;
	
	protected TimeZone timeZone = null;
	
	private static final Log LOG = LogFactory.getLog(AppointmentTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			final FolderObject folderObj = FolderTest
					.getStandardCalendarFolder(getWebConversation(),
					getHostName(), getSessionId());
			appointmentFolderId = folderObj.getObjectID();
			userId = folderObj.getCreatedBy();
			
			timeZone = ConfigTools.getTimeZone(getWebConversation(),
					getHostName(), getSessionId());
			
			LOG.debug(new StringBuilder().append("use timezone: ").append(
					timeZone).toString());
			
			Calendar c = Calendar.getInstance();
			c.setTimeZone(timeZone);
			c.set(Calendar.HOUR_OF_DAY, 8);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			
			startTime = c.getTimeInMillis();
			startTime += timeZone.getOffset(startTime);
			endTime = startTime + 3600000;
			
			userParticipant2 = AbstractConfigWrapper.parseProperty(
					getAJAXProperties(), "user_participant2", "");
			userParticipant3 = AbstractConfigWrapper.parseProperty(
					getAJAXProperties(), "user_participant3", "");
			
			groupParticipant = AbstractConfigWrapper.parseProperty(
					getAJAXProperties(), "group_participant", "");
			
			resourceParticipant = AbstractConfigWrapper.parseProperty(
					getAJAXProperties(), "resource_participant", "");
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new Exception(ex);
		}
	}

	protected void compareObject(AppointmentObject appointmentObj1,
			AppointmentObject appointmentObj2) throws Exception {
		compareObject(appointmentObj1, appointmentObj2, appointmentObj1.getStartDate().getTime(), appointmentObj1.getEndDate().getTime());
	}
	
	protected void compareObject(AppointmentObject appointmentObj1,
			AppointmentObject appointmentObj2, long newStartTime,
			long newEndTime) throws Exception {
		assertEquals("id", appointmentObj1.getObjectID(), appointmentObj2
				.getObjectID());
		assertEqualsAndNotNull("title", appointmentObj1.getTitle(),
				appointmentObj2.getTitle());
		assertEquals("start", newStartTime, appointmentObj2.getStartDate()
		.getTime());
		assertEquals("end", newEndTime, appointmentObj2.getEndDate().getTime());
		assertEqualsAndNotNull("location", appointmentObj1.getLocation(),
				appointmentObj2.getLocation());
		assertEquals("shown_as", appointmentObj1.getShownAs(), appointmentObj2
				.getShownAs());
		assertEquals("folder id", appointmentObj1.getParentFolderID(),
				appointmentObj2.getParentFolderID());
		assertEquals("private flag", appointmentObj1.getPrivateFlag(),
				appointmentObj2.getPrivateFlag());
		assertEquals("full time", appointmentObj1.getFullTime(),
				appointmentObj2.getFullTime());
		assertEquals("label", appointmentObj1.getLabel(), appointmentObj2
				.getLabel());
		assertEquals("recurrence_type", appointmentObj1.getRecurrenceType(),
				appointmentObj2.getRecurrenceType());
		assertEquals("interval", appointmentObj1.getInterval(), appointmentObj2
				.getInterval());
		assertEquals("days", appointmentObj1.getDays(), appointmentObj2
				.getDays());
		assertEquals("month", appointmentObj1.getMonth(), appointmentObj2
				.getMonth());
		assertEquals("day_in_month", appointmentObj1.getDayInMonth(),
				appointmentObj2.getDayInMonth());
		assertEquals("until", appointmentObj1.getUntil(), appointmentObj2
				.getUntil());
		assertEqualsAndNotNull("note", appointmentObj1.getNote(),
				appointmentObj2.getNote());
		assertEqualsAndNotNull("categories", appointmentObj1.getCategories(),
				appointmentObj2.getCategories());
		assertEqualsAndNotNull("delete_exceptions", appointmentObj1.getDeleteException(),
				appointmentObj2.getDeleteException());
		
		assertEqualsAndNotNull("participants are not equals",
				participants2String(appointmentObj1.getParticipants()),
				participants2String(appointmentObj2.getParticipants()));
	}
	
	protected AppointmentObject createAppointmentObject(String title) {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSENT);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
	
	public static int insertAppointment(WebConversation webCon,
			AppointmentObject appointmentObj, TimeZone userTimeZone,
			String host, String session) throws Exception, OXConflictException {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		StringWriter stringWriter = new StringWriter();
		
		final JSONObject jsonObj = new JSONObject();
		AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
		appointmentwriter.writeAppointment(appointmentObj, jsonObj);
		
		stringWriter.write(jsonObj.toString());
		stringWriter.flush();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONObject data = (JSONObject) response.getData();
		if (data.has(DataFields.ID)) {
			objectId = data.getInt(DataFields.ID);
		}
		
		if (data.has("conflicts")) {
			throw new OXConflictException("conflicts found!");
		}
		
		return objectId;
	}
	
	public static int updateAppointment(WebConversation webCon,
			AppointmentObject appointmentObj, int objectId, int inFolder,
			TimeZone userTimeZone, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final StringWriter stringWriter = new StringWriter();
		final JSONObject jsonObj = new JSONObject();
		AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
		appointmentwriter.writeAppointment(appointmentObj, jsonObj);
		
		stringWriter.write(jsonObj.toString());
		stringWriter.flush();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_UPDATE);
		parameter.setParameter(DataFields.ID, String.valueOf(objectId));
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, String
				.valueOf(inFolder));
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONObject data = (JSONObject) response.getData();
		if (data.has(DataFields.ID)) {
			objectId = data.getInt(DataFields.ID);
		}
		
		if (data.has("conflicts")) {
			throw new OXConflictException("conflicts found!");
		}
		
		return objectId;
	}
	
	public static void deleteAppointment(WebConversation webCon, int id,
			int inFolder, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		deleteAppointment(webCon, id, inFolder, 0, host, session);
	}
	
	public static void deleteAppointment(WebConversation webCon, int id,
			int inFolder, int recurrencePosition, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final AJAXSession ajaxSession = new AJAXSession(webCon, session);
		final DeleteRequest deleteRequest = new DeleteRequest(inFolder, id, recurrencePosition, new Date());
		deleteRequest.setFailOnError(false);
		final AbstractAJAXResponse response = Executor.execute(ajaxSession, deleteRequest);
		
		if (response.hasError()) {
			throw new Exception("json error: " + response.getResponse().getErrorMessage());
		}
	}
	
	public static void confirmAppointment(WebConversation webCon, int objectId,
			int confirm, String confirmMessage, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_CONFIRM);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, objectId);
		jsonObj.put(CalendarFields.CONFIRMATION, confirm);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString()
		.getBytes());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon, int inFolder, int[] cols, Date start, Date end, TimeZone userTimeZone, boolean showAll, String host, String session)
	throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		if (!showAll) {
			parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		}
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray) response.getData(), cols,
				userTimeZone);
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon,
			int[][] objectIdAndFolderId, int[] cols, TimeZone userTimeZone,
			String host, String session) throws Exception {
		AppointmentObject[] appointmentArray = new AppointmentObject[objectIdAndFolderId.length];
		for (int a = 0; a < appointmentArray.length; a++) {
			appointmentArray[a] = new AppointmentObject();
			appointmentArray[a].setObjectID(objectIdAndFolderId[a][0]);
			appointmentArray[a].setParentFolderID(objectIdAndFolderId[a][1]);
		}
		
		return listAppointment(webCon, appointmentArray, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon,
			AppointmentObject[] appointmentArray, int[] cols, TimeZone userTimeZone,
			String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_LIST);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter
				.colsArray2String(cols));
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < appointmentArray.length; a++) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, appointmentArray[a].getObjectID());
			jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, appointmentArray[a].getParentFolderID());
			
			if (appointmentArray[a].containsRecurrencePosition()) {
				jsonObj.put(CalendarFields.RECURRENCE_POSITION, appointmentArray[a].getRecurrencePosition());
			}
			
			jsonArray.put(jsonObj);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray
				.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray) response.getData(), cols,
				userTimeZone);
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, int inFolder, TimeZone userTimeZone, String host,
			String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_GET);
		parameter.setParameter(DataFields.ID, objectId);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		
		AppointmentParser appointmentParser = new AppointmentParser(
				userTimeZone);
		appointmentParser
				.parse(appointmentObj, (JSONObject) response.getData());
		
		return appointmentObj;
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, Date start, Date end, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		
		return loadAppointment(webCon, objectId, 0, start, end, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, Date start, Date end, Date modified, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		
		return loadAppointment(webCon, objectId, 0, start, end, modified, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, Date start, Date end, Date modified, int recurrencePosition, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		
		return loadAppointment(webCon, objectId, 0, start, end, modified, recurrencePosition, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, int inFolder, Date start, Date end, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		boolean showAll = (inFolder == 0);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, inFolder, cols, start, end, userTimeZone, showAll, host, session);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				return appointmentArray[a];
			}
		}
		
		throw new TestException("object not found");
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, int inFolder, Date start, Date end, Date modified, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		AppointmentObject[] appointmentArray = listModifiedAppointment(webCon, inFolder, start, end, modified, cols, userTimeZone, host, session);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				return appointmentArray[a];
			}
		}
		
		throw new TestException("object not found");
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon,
			int objectId, int inFolder, Date start, Date end, Date modified, int recurrencePosition, int[] cols, TimeZone userTimeZone, String host,
			String session) throws Exception {
		
		AppointmentObject[] appointmentArray = listModifiedAppointment(webCon, inFolder, start, end, modified, cols, userTimeZone, host, session);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId && appointmentArray[a].getRecurrencePosition() == recurrencePosition) {
				return appointmentArray[a];
			}
		}
		
		throw new TestException("object not found");
	}
	
	public static AppointmentObject[] listModifiedAppointment(
			WebConversation webCon, Date start, Date end,
			Date modified, int[] cols, TimeZone userTimeZone, String host, String session)
			throws Exception {
		return listModifiedAppointment(webCon, 0, start, end, modified, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject[] listModifiedAppointment(
			WebConversation webCon, int inFolder, Date start, Date end,
			Date modified, int[] cols, TimeZone userTimeZone, String host, String session)
			throws Exception {
			return listModifiedAppointment(webCon, inFolder, start, end, modified, cols, userTimeZone, false, host, session);
	}
	
	
	public static AppointmentObject[] listModifiedAppointment(
			WebConversation webCon, int inFolder, Date start, Date end,
			Date modified, int[] cols, TimeZone userTimeZone, boolean bRecurrenceMaster, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_UPDATES);
		
		if (inFolder != 0) {
			parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		}
		
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter
				.colsArray2String(cols));
		
		if (bRecurrenceMaster) {
			parameter.setParameter(AppointmentRequest.RECURRENCE_MASTER, true);
		}
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		assertTrue("requested timestamp bigger then timestamp in response", response.getTimestamp().getTime() >= modified.getTime());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray) response.getData(), cols,
				userTimeZone);
	}
	
	public static AppointmentObject[] listDeleteAppointment(
			WebConversation webCon, int inFolder, Date start, Date end,
			Date modified, TimeZone userTimeZone, String host, String session)
			throws OXException, Exception {
		host = appendPrefix(host);
		
		final int[] cols = new int[] { AppointmentObject.OBJECT_ID };
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_UPDATES);
		
		if (inFolder != 0) {
			parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		}
		
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "changed");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter
				.colsArray2String(cols));
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			throw new TestException(response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		JSONArray jsonArray = (JSONArray) response.getData();
		AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray
				.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			appointmentArray[a] = new AppointmentObject();
			appointmentArray[a].setObjectID(jsonArray.getInt(a));
		}
		
		return appointmentArray;
	}
	
	public static AppointmentObject[] searchAppointment(WebConversation webCon,
			String searchpattern, int inFolder, int[] cols,
			TimeZone userTimeZone, String host, String session)
			throws Exception {
		return searchAppointment(webCon, searchpattern, inFolder, null, null, cols, userTimeZone, host, session);
	}
	
	public static AppointmentObject[] searchAppointment(WebConversation webCon,
			String searchpattern, int inFolder, Date start, Date end, int[] cols,
			TimeZone userTimeZone, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_SEARCH);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter
				.colsArray2String(cols));
		
		if (start != null) {
			parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		}

		if (end != null) {
			parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		}
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters(), new ByteArrayInputStream(
				jsonObj.toString().getBytes()), "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray) response.getData(), cols,
				userTimeZone);
	}
	
	public static boolean[] hasAppointments(WebConversation webCon, Date start,
			Date end, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_HAS);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		boolean isArray = ((JSONArray) response.getData()) instanceof JSONArray;
		assertTrue("response object is not an array", isArray);
		
		JSONArray jsonArray = ((JSONArray) response.getData());
		
		boolean[] hasAppointments = new boolean[jsonArray.length()];
		for (int a = 0; a < hasAppointments.length; a++) {
			hasAppointments[a] = jsonArray.getBoolean(a);
		}
		
		return hasAppointments;
	}
	
	public static AppointmentObject[] getFreeBusy(WebConversation webCon,
			int particiantId, int type, Date start, Date end,
			TimeZone userTimeZone, String host, String session)
			throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, particiantId);
		parameter.setParameter("type", type);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_FREEBUSY);
		parameter.setParameter(AJAXServlet.PARAMETER_START, start);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		
		WebRequest req = new GetMethodWebRequest(host + APPOINTMENT_URL
				+ parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		boolean isArray = ((JSONArray) response.getData()) instanceof JSONArray;
		assertTrue("response object is not an array", isArray);
		
		JSONArray jsonArray = ((JSONArray) response.getData());
		return jsonArray2AppointmentArray(jsonArray, userTimeZone);
	}
	
	private static AppointmentObject[] jsonArray2AppointmentArray(
			JSONArray jsonArray, TimeZone userTimeZone) throws Exception {
		AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray
				.length()];
		AppointmentParser appointmentParser = new AppointmentParser(
				userTimeZone);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			appointmentArray[a] = new AppointmentObject();
			JSONObject jObj = jsonArray.getJSONObject(a);
			
			appointmentParser.parse(appointmentArray[a], jObj);
		}
		
		return appointmentArray;
	}
	
	public static AppointmentObject[] jsonArray2AppointmentArray(
			JSONArray jsonArray, int[] cols, TimeZone userTimeZone)
			throws Exception {
		AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray
				.length()];
		
		for (int a = 0; a < appointmentArray.length; a++) {
			appointmentArray[a] = new AppointmentObject();
			parseCols(cols, jsonArray.getJSONArray(a), appointmentArray[a], userTimeZone);
			
			if (!appointmentArray[a].getFullTime()) {
				Date startDate = appointmentArray[a].getStartDate();
				Date endDate = appointmentArray[a].getEndDate();
				
				if (startDate != null && endDate != null) {
					int startOffset = userTimeZone.getOffset(startDate.getTime());
					int endOffset = userTimeZone.getOffset(endDate.getTime());
					appointmentArray[a].setStartDate(new Date(startDate.getTime() - startOffset));
					appointmentArray[a].setEndDate(new Date(endDate.getTime() - endOffset));
				}
			}
		}
		
		return appointmentArray;
	}
	
	private static void parseCols(int[] cols, JSONArray jsonArray,
			AppointmentObject appointmentObj, TimeZone userTimeZone) throws Exception {
		if (cols.length != jsonArray.length()) {
			LOG.debug("expected cols: "
					+ StringCollection.convertArray2String(cols)
					+ " recieved cols: " + jsonArray.toString());
		}
		
		assertEquals("compare array size with cols size", cols.length,
				jsonArray.length());
		
		for (int a = 0; a < cols.length; a++) {
			parse(a, cols[a], jsonArray, appointmentObj, userTimeZone);
		}
	}
	
	private static void parse(int pos, int field, JSONArray jsonArray,
			AppointmentObject appointmentObj, TimeZone userTimeZone) throws Exception {
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
			case AppointmentObject.RECURRENCE_POSITION:
				appointmentObj.setRecurrencePosition(jsonArray.getInt(pos));
				break;
			case AppointmentObject.RECURRENCE_TYPE:
				appointmentObj.setRecurrenceType(jsonArray.getInt(pos));
				break;
			case AppointmentObject.INTERVAL:
				appointmentObj.setInterval(jsonArray.getInt(pos));
				break;
			case AppointmentObject.DAYS:
				appointmentObj.setDays(jsonArray.getInt(pos));
				break;
			case AppointmentObject.DAY_IN_MONTH:
				appointmentObj.setDayInMonth(jsonArray.getInt(pos));
				break;
			case AppointmentObject.MONTH:
				appointmentObj.setMonth(jsonArray.getInt(pos));
				break;
			case AppointmentObject.UNTIL:
				appointmentObj.setUntil(new Date(jsonArray.getLong(pos)));
				break;
			case AppointmentObject.RECURRING_OCCURRENCE:
				appointmentObj.setOccurrence(jsonArray.getInt(pos));
				break;
			case AppointmentObject.TIMEZONE:
				appointmentObj.setTimezone(jsonArray.getString(pos));
				break;
			case AppointmentObject.PARTICIPANTS:
				appointmentObj.setParticipants(parseParticipants(jsonArray
						.getJSONArray(pos)));
				break;
		}
	}
	
	private static Participant[] parseParticipants(JSONArray jsonArray)
	throws Exception {
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
	
	private HashSet participants2String(Participant[] participant)
	throws Exception {
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
		sb.append("E" + p.getEmailAddress());
		sb.append("D" + p.getDisplayName());
		
		return sb.toString();
	}
}
