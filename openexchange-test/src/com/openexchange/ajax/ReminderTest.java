package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import java.io.ByteArrayInputStream;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderTest extends AbstractAJAXTest {
	
	private static final String REMINDER_URL = "/ajax/reminder";
	
	private static final long d7 = 604800000;
	
	private static Date end = new Date();
	
	private static Date start = new Date(end.getTime()-d7);
	
	protected WebRequest req = null;
	
	protected WebResponse resp = null;
	
	public void testDelete() throws Exception {
		int id[] = {1};
		actionDelete(id);
	}
	
	public void testUpdates() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_UPDATES);
		parameter.append("&" + AJAXServlet.PARAMETER_TIMESTAMP + "=" + end.getTime());
		
		actionUpdates(parameter.toString());
	}
	
	public void testRange() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_RANGE);
		parameter.append("&" + AJAXServlet.PARAMETER_START + "=" + start.getTime());
		parameter.append("&" + AJAXServlet.PARAMETER_END + "=" + end.getTime());
		
		actionRange(parameter.toString());
	}
	
	protected void actionRange(String parameter) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + REMINDER_URL + parameter.toString());
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionUpdates(String parameter) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + REMINDER_URL + parameter.toString());
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionDelete(int[] id) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_DELETE);
		parameter.append("&" + AJAXServlet.PARAMETER_TIMESTAMP + "=" + new Date(0).getTime());
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < id.length; a++) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, id[a]);
			jsonArray.put(jsonObj);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + REMINDER_URL + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
			assertTrue("array length is 1", data.length() == 1);
			assertEquals("first entry in array is 1", 1, data.getInt(0));
		} else {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has("error")) {
			fail("server error: " + jsonobject.getString("error"));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
}

