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
import com.openexchange.groupware.ldap.Group;
import com.openexchange.tools.URLParameter;

public class GroupTest extends AbstractAJAXTest {
	
	public GroupTest(String name) {
		super(name);
	}

	private static final String GROUP_URL = "/ajax/group";
	
	public void testSearch() throws Exception {
		com.openexchange.groupware.ldap.Group groups[] = searchGroup(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("group array size > 0", groups.length > 0);
	}

	public void testRealSearch() throws Throwable {
		Group[] groups = searchGroup(getWebConversation(), "*l*", PROTOCOL
				+ getHostName(), getSessionId());
		assertNotNull(groups);
	}
	
	public void testList() throws Exception {
		com.openexchange.groupware.ldap.Group groups[] = searchGroup(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("group array size > 0", groups.length > 0);
		
		int[] id = new int[groups.length];
		for (int a = 0; a < id.length; a++) {
			id[a] = groups[a].getIdentifier();
		}
		
		groups = listGroup(getWebConversation(), id, PROTOCOL + getHostName(), getSessionId());
		assertTrue("group array size > 0", groups.length > 0);
	}
	
	public void testSearchGroupUsers() throws Exception {
		com.openexchange.groupware.ldap.Group groups[] = searchGroup(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("group array size > 0", groups.length > 0);
	}
	
	public void testGet() throws Exception {
		com.openexchange.groupware.ldap.Group groups[] = searchGroup(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("group array size > 0", groups.length > 0);
		com.openexchange.groupware.ldap.Group g = loadGroup(getWebConversation(), groups[0].getIdentifier(), PROTOCOL + getHostName(), getSessionId());
	}
	
	public static com.openexchange.groupware.ldap.Group[] searchGroup(WebConversation webCon, String searchpattern, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + GROUP_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONArray jsonArray = (JSONArray)response.getData();
		com.openexchange.groupware.ldap.Group[] g = new com.openexchange.groupware.ldap.Group[jsonArray.length()];
		for (int a = 0; a < g.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			g[a] = new com.openexchange.groupware.ldap.Group();
			g[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				g[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			} 
		}
		
		return g;
	}
	
	public static com.openexchange.groupware.ldap.Group[] listGroup(WebConversation webCon, int[] id, String host, String session) throws Exception {
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
		WebRequest req = new PutMethodWebRequest(host + GROUP_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONArray jsonArray = (JSONArray)response.getData();
		com.openexchange.groupware.ldap.Group[] g = new com.openexchange.groupware.ldap.Group[jsonArray.length()];
		for (int a = 0; a < g.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			g[a] = new com.openexchange.groupware.ldap.Group();
			g[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			if (jObj.has(ParticipantsFields.DISPLAY_NAME)) {
				g[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
			} 
		}
		
		return g;
	}
	
	public static com.openexchange.groupware.ldap.Group loadGroup(WebConversation webCon, int groupId, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(groupId));
		
		WebRequest req = new GetMethodWebRequest(host + GROUP_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONObject jsonObj = (JSONObject)response.getData();
		
		com.openexchange.groupware.ldap.Group g = new com.openexchange.groupware.ldap.Group();
		assertTrue("check id", jsonObj.has(ParticipantsFields.ID));
		g.setIdentifier(jsonObj.getInt(ParticipantsFields.ID));

		if (jsonObj.has(ParticipantsFields.DISPLAY_NAME)) {
			g.setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		if (jsonObj.has("members")) {
			JSONArray jsonArray = jsonObj.getJSONArray("members");
			int[] members = new int[jsonArray.length()];
			
			for (int a = 0; a < members.length; a++) {
				members[a] = jsonArray.getInt(a);
			}
			
			g.setMember(members);
		} 
		
		return g;
	}
}

