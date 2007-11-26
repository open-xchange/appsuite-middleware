package com.openexchange.ajax.appointment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.tools.URLParameter;

public class ConflictTest extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(ConflictTest.class);
	
	public ConflictTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(timeZone);
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		startTime = c.getTimeInMillis();
		startTime += timeZone.getOffset(startTime);
		endTime = startTime + 7200000;
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 8:00
	 * Conflict End: 10:00
	 */
	public void testConflict1() throws Exception {
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		final Date rangeStart = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		final Date rangeEnd = calendar.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict1 - insert");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		appointmentObj.setIgnoreConflicts(true);
		int secondObjectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), secondObjectId, appointmentFolderId, rangeStart, rangeEnd, APPOINTMENT_FIELDS, timeZone, getHostName(), getSessionId());
		Date modified = loadAppointment.getCreationDate();

		appointmentObj.setObjectID(secondObjectId);
		appointmentObj.setIgnoreConflicts(true);
		appointmentObj.setShownAs(AppointmentObject.FREE);
		updateAppointment(getWebConversation(), appointmentObj, secondObjectId, appointmentFolderId, modified, timeZone, PROTOCOL + getHostName(), getSessionId());

		loadAppointment = loadAppointment(getWebConversation(), secondObjectId, appointmentFolderId, rangeStart, rangeEnd, APPOINTMENT_FIELDS, timeZone, getHostName(), getSessionId());
		modified = loadAppointment.getLastModified();

		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setTitle("testConflict1 - update");
		appointmentConflicts = updateAppointmentReturnConflicts(getWebConversation(), appointmentObj, secondObjectId, appointmentFolderId, modified, timeZone, getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);		

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, PROTOCOL + getHostName(), getSessionId());
		deleteAppointment(getWebConversation(), secondObjectId, appointmentFolderId, modified, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 8:00
	 * Conflict End: 9:00
	 */
	public void testConflict2() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict2");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setEndDate(new Date(endTime-3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 8:00
	 * Conflict End: 11:00
	 */
	public void testConflict3() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict3");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setEndDate(new Date(endTime+3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 7:00
	 * Conflict End: 10:00
	 */
	public void testConflict4() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict4");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime-3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 9:00
	 * Conflict End: 10:00
	 */
	public void testConflict5() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict5");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime+3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 9:00
	 * Conflict End: 9:30
	 */
	public void testConflict6() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict6");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime+3600000));
		appointmentObj.setEndDate(new Date(endTime-1800000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 7:00
	 * Conflict End: 11:00
	 */
	public void testConflict7() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testConflict7");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime-3600000));
		appointmentObj.setEndDate(new Date(endTime+3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 7:00
	 * Conflict End: 8:00
	 */
	public void testNonConflict1() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testNonConflict1");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime-3600000));
		appointmentObj.setEndDate(new Date(endTime-7200000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		if (appointmentConflicts != null) {
			for (int a = 0; a < appointmentConflicts.length; a++) {
				if (appointmentConflicts[a].getObjectID() == objectId) {
					found = true;
				}
			}
		}
		
		assertFalse("appointment id " + objectId + " found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: 8:00
	 * Appointment End: 10:00
	 *
	 * Conflict Start: 10:00
	 * Conflict End: 11:00
	 */
	public void testNonConflict2() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testNonConflict2");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime+7200000));
		appointmentObj.setEndDate(new Date(endTime+3600000));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		if (appointmentConflicts != null) {
			for (int a = 0; a < appointmentConflicts.length; a++) {
				if (appointmentConflicts[a].getObjectID() == objectId) {
					found = true;
				}
			}
		}
		
		assertFalse("appointment id " + objectId + " found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: Today FullTime
	 * Appointment End: +24 Std
	 *
	 * Conflict Start: 8:00
	 * Conflict End: 10:00
	 */
	public void testFullTimeConflict1() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testFullTimeConflict1");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	/**
	 * Test case for conflict
	 * Appointment Start: Today FullTime
	 * Appointment End: +24 Std
	 *
	 * Conflict Start: Today FullTime
	 * Conflict End: +24 Std
	 */
	public void testFullTimeConflict2() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testFullTimeConflict2");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		appointmentObj.setIgnoreConflicts(false);
		
		AppointmentObject[] appointmentConflicts = insertAppointmentReturnConflicts(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertNotNull("conflicts expected!", appointmentConflicts);
		
		boolean found = false;
		
		for (int a = 0; a < appointmentConflicts.length; a++) {
			if (appointmentConflicts[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("appointment id " + objectId + " not found in conflicts", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public static AppointmentObject[] insertAppointmentReturnConflicts(WebConversation webCon,
			AppointmentObject appointmentObj, TimeZone userTimeZone,
			String host, String session) throws Exception, OXConflictException {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos, true);

		final JSONObject jsonObj = new JSONObject();
		AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
		appointmentwriter.writeAppointment(appointmentObj, jsonObj);
		
		pw.print(jsonObj.toString());		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
				AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
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
			AppointmentParser appointmentParser = new AppointmentParser(userTimeZone);
			
			JSONArray jsonArray = data.getJSONArray("conflicts");
			AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray.length()];
			for (int a = 0; a < jsonArray.length(); a++) {
				appointmentArray[a] = new AppointmentObject();
				appointmentParser.parse(appointmentArray[a], jsonArray.getJSONObject(a));
			}
			
			return appointmentArray;
		}
		
		return null;
	}
	
	public static AppointmentObject[] updateAppointmentReturnConflicts(WebConversation webCon,
			AppointmentObject appointmentObj, int objectId, int inFolder, Date modified, TimeZone userTimeZone,
			String host, String session) throws Exception, OXConflictException {
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
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);

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
			AppointmentParser appointmentParser = new AppointmentParser(userTimeZone);
			
			JSONArray jsonArray = data.getJSONArray("conflicts");
			AppointmentObject[] appointmentArray = new AppointmentObject[jsonArray.length()];
			for (int a = 0; a < jsonArray.length(); a++) {
				appointmentArray[a] = new AppointmentObject();
				appointmentParser.parse(appointmentArray[a], jsonArray.getJSONObject(a));
			}
			
			return appointmentArray;
		}
		
		return null;
	}
}

