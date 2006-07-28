package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import java.io.ByteArrayInputStream;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderTest extends AbstractAJAXTest {
	
	private static String url = "/ajax/reminder";
	
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
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_UPDATES);
		parameter.append("&timestamp=" + end.getTime());
		
		actionUpdates(parameter.toString());
	}
	
	public void testRange() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_RANGE);
		parameter.append("&start=" + start.getTime());
		parameter.append("&end=" + end.getTime());
		
		actionRange(parameter.toString());
	}
	
	protected void actionRange(String parameter) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + url + parameter.toString());
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
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + url + parameter.toString());
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
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=delete");
		parameter.append("&timestamp=" + new Date(0).getTime());
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < id.length; a++) {
			jsonArray.put(id[a]);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + url + parameter.toString(), bais, "text/javascript");
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

