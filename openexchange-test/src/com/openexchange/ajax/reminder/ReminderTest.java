package com.openexchange.ajax.reminder;


import com.openexchange.ajax.*;
import com.openexchange.ajax.fields.ReminderFields;
import java.io.ByteArrayInputStream;
import java.util.Date;

import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;
import org.json.JSONArray;

public class ReminderTest extends AbstractAJAXTest {
	
	private static final String REMINDER_URL = "/ajax/reminder";
	
	public ReminderTest(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public static ReminderObject[] listReminder(WebConversation webConversation, Date end, String host, String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_RANGE);
		parameter.setParameter(AJAXServlet.PARAMETER_END, end);
		
		WebRequest webRequest = new GetMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters());
		WebResponse webResponse = webConversation.getResponse(webRequest);
		
		assertEquals(200, webResponse.getResponseCode());
		
		JSONObject jsonObj = new JSONObject(webResponse.getText());
		
		if (jsonObj.has(jsonTagError)) {
			throw new TestException("server error: " + jsonObj.get(jsonTagError));
		}
		
		if (!jsonObj.has(jsonTagData)) {
			throw new TestException("no data in JSON object!");
		}
		
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		ReminderObject[] reminderObj = new ReminderObject[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject jsonReminder = jsonArray.getJSONObject(a);
			reminderObj[a] = new ReminderObject();
			
			reminderObj[a].setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
			reminderObj[a].setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
			reminderObj[a].setFolder(DataParser.parseString(jsonReminder, ReminderFields.FOLDER));
			reminderObj[a].setDate(DataParser.parseDate(jsonReminder, ReminderFields.ALARM));
			reminderObj[a].setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
			reminderObj[a].setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
			reminderObj[a].setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
		}
		
		return reminderObj;
	}
	
	public static ReminderObject[] listUpdates(WebConversation webConversation, Date lastModified, String host, String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified);
		
		WebRequest webRequest = new GetMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters());
		WebResponse webResponse = webConversation.getResponse(webRequest);
		
		assertEquals(200, webResponse.getResponseCode());
		
		JSONObject jsonObj = new JSONObject(webResponse.getText());
		
		if (jsonObj.has(jsonTagError)) {
			throw new TestException("server error: " + jsonObj.get(jsonTagError));
		}
		
		if (!jsonObj.has(jsonTagData)) {
			throw new TestException("no data in JSON object!");
		}
		
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		ReminderObject[] reminderObj = new ReminderObject[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject jsonReminder = jsonArray.getJSONObject(a);
			reminderObj[a] = new ReminderObject();
			
			reminderObj[a].setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
			reminderObj[a].setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
			reminderObj[a].setFolder(DataParser.parseString(jsonReminder, ReminderFields.FOLDER));
			reminderObj[a].setDate(DataParser.parseDate(jsonReminder, ReminderFields.ALARM));
			reminderObj[a].setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
			reminderObj[a].setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
			reminderObj[a].setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
		}
		
		return reminderObj;
	}
	
	public static void deleteReminder(WebConversation webConversation, int objectId, String host, String sessionId) throws Exception, TestException {
		host = appendPrefix(host);
		
		URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, objectId);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest webRequest = new PutMethodWebRequest(host + REMINDER_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse webResponse = webConversation.getResponse(webRequest);
		JSONObject jsonobject = new JSONObject(webResponse.getText());
		
		assertEquals(200, webResponse.getResponseCode());
		
		JSONObject jsonResponse = new JSONObject(webResponse.getText());
		
		if (jsonResponse.has(jsonTagError)) {
			throw new TestException("server error: " + jsonResponse.get(jsonTagError));
		}
	}
}

