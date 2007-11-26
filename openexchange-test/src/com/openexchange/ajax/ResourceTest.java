package com.openexchange.ajax;


import java.io.ByteArrayInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.tools.URLParameter;

public class ResourceTest extends AbstractAJAXTest {
	
	public ResourceTest(String name) {
		super(name);
	}

	private static final String RESOURCE_URL = "/ajax/resource";
	
	public void testSearch() throws Exception {
		com.openexchange.groupware.ldap.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
	}
	
	public void testList() throws Exception {
		com.openexchange.groupware.ldap.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
		
		int[] id = new int[resources.length];
		for (int a = 0; a < id.length; a++) {
			id[a] = resources[a].getIdentifier();
		}
		
		resources = listResource(getWebConversation(), id, PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
	}
	
	public void testGet() throws Exception {
		com.openexchange.groupware.ldap.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
		com.openexchange.groupware.ldap.Resource r = loadResource(getWebConversation(), resources[0].getIdentifier(), PROTOCOL + getHostName(), getSessionId());
	}
	
	public static com.openexchange.groupware.ldap.Resource[] searchResource(WebConversation webCon, String searchpattern, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp is null", response.getTimestamp());
		
		JSONArray jsonArray = (JSONArray)response.getData();
		com.openexchange.groupware.ldap.Resource[] r = new com.openexchange.groupware.ldap.Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			r[a] = new com.openexchange.groupware.ldap.Resource();
			r[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				r[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			}
		}
		
		return r;
	}
	
	public static com.openexchange.groupware.ldap.Resource[] listResource(WebConversation webCon, int[] id, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		
		JSONArray requestArray = new JSONArray();
		for (int a = 0; a < id.length; a++) {
			JSONObject jData = new JSONObject();
			jData.put(DataFields.ID, id[a]);
			requestArray.put(jData);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(requestArray.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONArray jsonArray = (JSONArray)response.getData();
		com.openexchange.groupware.ldap.Resource[] r = new com.openexchange.groupware.ldap.Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			r[a] = new com.openexchange.groupware.ldap.Resource();
			r[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				r[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			}
		}
		
		return r;
	}
	
	public static com.openexchange.groupware.ldap.Resource loadResource(WebConversation webCon, int groupId, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(groupId));
		
		WebRequest req = new GetMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp is null", response.getTimestamp());
		
		JSONObject jsonObj = (JSONObject)response.getData();
		
		com.openexchange.groupware.ldap.Resource r = new com.openexchange.groupware.ldap.Resource();
		assertTrue("id is not in json object", jsonObj.has(ParticipantsFields.ID));
		r.setIdentifier(jsonObj.getInt(ParticipantsFields.ID));
		
		if (jsonObj.has(ParticipantsFields.DISPLAY_NAME)) {
			r.setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return r;
	}
}

