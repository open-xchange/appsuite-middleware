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
	
	public ResourceTest(final String name) {
		super(name);
	}

	private static final String RESOURCE_URL = "/ajax/resource";
	
	public void testSearch() throws Exception {
		final com.openexchange.resource.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
	}
	
	public void testList() throws Exception {
		com.openexchange.resource.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
		
		final int[] id = new int[resources.length];
		for (int a = 0; a < id.length; a++) {
			id[a] = resources[a].getIdentifier();
		}
		
		resources = listResource(getWebConversation(), id, PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
	}
	
	public void testGet() throws Exception {
		final com.openexchange.resource.Resource resources[] = searchResource(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("resource array size is not > 0", resources.length > 0);
		final com.openexchange.resource.Resource r = loadResource(getWebConversation(), resources[0].getIdentifier(), PROTOCOL + getHostName(), getSessionId());
	}
	
	public static com.openexchange.resource.Resource[] searchResource(final WebConversation webCon, final String searchpattern, String host, final String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		
		final JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		
		final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		final WebRequest req = new PutMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters(), bais, "text/javascript");
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp is null", response.getTimestamp());
		
		final JSONArray jsonArray = (JSONArray)response.getData();
		final com.openexchange.resource.Resource[] r = new com.openexchange.resource.Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			final JSONObject jObj = jsonArray.getJSONObject(a);
			r[a] = new com.openexchange.resource.Resource();
			r[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				r[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			}
		}
		
		return r;
	}
	
	public static com.openexchange.resource.Resource[] listResource(final WebConversation webCon, final int[] id, String host, final String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		
		final JSONArray requestArray = new JSONArray();
		for (int a = 0; a < id.length; a++) {
			final JSONObject jData = new JSONObject();
			jData.put(DataFields.ID, id[a]);
			requestArray.put(jData);
		}
		
		final ByteArrayInputStream bais = new ByteArrayInputStream(requestArray.toString().getBytes());
		final WebRequest req = new PutMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters(), bais, "text/javascript");
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		final JSONArray jsonArray = (JSONArray)response.getData();
		final com.openexchange.resource.Resource[] r = new com.openexchange.resource.Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			final JSONObject jObj = jsonArray.getJSONObject(a);
			r[a] = new com.openexchange.resource.Resource();
			r[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				r[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			}
		}
		
		return r;
	}
	
	public static com.openexchange.resource.Resource loadResource(final WebConversation webCon, final int groupId, String host, final String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(groupId));
		
		final WebRequest req = new GetMethodWebRequest(host + RESOURCE_URL + parameter.getURLParameters());
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp is null", response.getTimestamp());
		
		final JSONObject jsonObj = (JSONObject)response.getData();
		
		final com.openexchange.resource.Resource r = new com.openexchange.resource.Resource();
		assertTrue("id is not in json object", jsonObj.has(ParticipantsFields.ID));
		r.setIdentifier(jsonObj.getInt(ParticipantsFields.ID));
		
		if (jsonObj.has(ParticipantsFields.DISPLAY_NAME)) {
			r.setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return r;
	}
}

