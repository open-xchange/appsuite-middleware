package com.openexchange.ajax.reminder;


import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;

public class ReminderTest extends AbstractAJAXTest {
	
	private static final String REMINDER_URL = "/ajax/reminder";
	
	public ReminderTest(final String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public static ReminderObject[] listReminder(final WebConversation webConversation, final Date end, final TimeZone timeZone, String host, final String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_RANGE);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		
		final WebRequest webRequest = new GetMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters());
		final WebResponse webResponse = webConversation.getResponse(webRequest);
		
		assertEquals(200, webResponse.getResponseCode());
		
		final JSONObject jsonObj = new JSONObject(webResponse.getText());
		
		if (jsonObj.has(jsonTagError)) {
			throw new TestException("server error: " + jsonObj.get(jsonTagError));
		}
		
		if (!jsonObj.has(jsonTagData)) {
			throw new TestException("no data in JSON object!");
		}
		
		final JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		final ReminderObject[] reminderObj = new ReminderObject[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			final JSONObject jsonReminder = jsonArray.getJSONObject(a);
			reminderObj[a] = new ReminderObject();
			
			reminderObj[a].setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
			reminderObj[a].setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
			reminderObj[a].setFolder(DataParser.parseInt(jsonReminder, ReminderFields.FOLDER));
			reminderObj[a].setDate(DataParser.parseTime(jsonReminder, ReminderFields.ALARM, timeZone));
			reminderObj[a].setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
			reminderObj[a].setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
			reminderObj[a].setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
		}
		
		return reminderObj;
	}
	
	public static ReminderObject[] listUpdates(final WebConversation webConversation, final Date lastModified, String host, final String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified);
		
		final WebRequest webRequest = new GetMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters());
		final WebResponse webResponse = webConversation.getResponse(webRequest);
		
		assertEquals(200, webResponse.getResponseCode());
		
		final JSONObject jsonObj = new JSONObject(webResponse.getText());
		
		if (jsonObj.has(jsonTagError)) {
			throw new TestException("server error: " + jsonObj.get(jsonTagError));
		}
		
		if (!jsonObj.has(jsonTagData)) {
			throw new TestException("no data in JSON object!");
		}
		
		final JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		final ReminderObject[] reminderObj = new ReminderObject[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			final JSONObject jsonReminder = jsonArray.getJSONObject(a);
			reminderObj[a] = new ReminderObject();
			
			reminderObj[a].setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
			reminderObj[a].setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
			reminderObj[a].setFolder(DataParser.parseInt(jsonReminder, ReminderFields.FOLDER));
			reminderObj[a].setDate(DataParser.parseDate(jsonReminder, ReminderFields.ALARM));
			reminderObj[a].setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
			reminderObj[a].setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
			reminderObj[a].setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
		}
		
		return reminderObj;
	}
	
	public static int[] deleteReminder(final WebConversation webConversation, final int objectId, String host, final String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		
		final JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, objectId);
		
		final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		final WebRequest webRequest = new PutMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters(), bais, "text/javascript");
		final WebResponse webResponse = webConversation.getResponse(webRequest);
		final JSONObject jsonobject = new JSONObject(webResponse.getText());
		
		assertEquals(200, webResponse.getResponseCode());
		
		final JSONObject jsonResponse = new JSONObject(webResponse.getText());
				
		if (jsonResponse.has(jsonTagError)) {
			throw new TestException("server error: " + jsonResponse.get(jsonTagError));
		}
		
		if (jsonResponse.has("data")) {
			final JSONArray jsonArray = jsonResponse.getJSONArray("data");
			final int[] failedObjects = new int[jsonArray.length()];
			for (int a = 0; a < failedObjects.length; a++) {
				failedObjects[a] = jsonArray.getInt(a);
			}
			return failedObjects;
		}
		
		return new int[] { };
	}
	
	public void compareReminder(final ReminderObject reminderObj1, final ReminderObject reminderObj2) throws Exception {
		assertEquals("id", reminderObj1.getObjectId(), reminderObj2.getObjectId());
		OXTestToolkit.assertEqualsAndNotNull("folder", reminderObj1.getFolder(), reminderObj2.getFolder());
		OXTestToolkit.assertEqualsAndNotNull("alarm", reminderObj1.getDate(), reminderObj2.getDate());
	}
}

