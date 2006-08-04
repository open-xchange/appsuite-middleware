package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceGroup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.URLParameter;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;


public class ParticipantTest extends AbstractAJAXTest {
	
	private static final String PARTICIPANT_URL = "/ajax/participant";
	
	public void testUserSearch() throws Exception {
		JSONObject jsonObj = searchAction(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.USER);
		
		if (!jsonObj.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonObj.has(jsonTagError)) {
			fail("json error: " + jsonObj.get(jsonTagError));
		}
	}
	
	public void testResourceSearch() throws Exception {
		JSONObject jsonObj = searchAction(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.RESOURCE);
		
		if (!jsonObj.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonObj.has(jsonTagError)) {
			fail("json error: " + jsonObj.get(jsonTagError));
		}
	}
	
	public void testGetGroupMembers() throws Exception {
		int groupId = 0;
		
		Group[] g = listGroup(getWebConversation(), PROTOCOL + getHostName(), getSessionId());
		
		groupId = g[0].getIdentifier();
		
		System.out.println("GROUPID: " + groupId);
		
		JSONObject jsonObj = listMembers(getWebConversation(), PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.GROUP, groupId);
		
		if (!jsonObj.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonObj.has(jsonTagError)) {
			fail("json error: " + jsonObj.get(jsonTagError));
		}
	}
	
	public void testGetGroups() throws Exception {
		JSONObject jsonObj = listAction(getWebConversation(), PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.GROUP);
		
		if (!jsonObj.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonObj.has(jsonTagError)) {
			fail("json error: " + jsonObj.get(jsonTagError));
		}
	}
	
	public void testGetResourceGroups() throws Exception {
		JSONObject jsonObj = listAction(getWebConversation(), PROTOCOL + getHostName(), getSessionId(), com.openexchange.groupware.container.Participant.RESOURCEGROUP);
		
		if (!jsonObj.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonObj.has(jsonTagError)) {
			fail("json error: " + jsonObj.get(jsonTagError));
		}
	}
	
	protected String getURL() {
		return PARTICIPANT_URL;
	}
	
	protected static JSONObject searchAction(WebConversation webCon, String searchpattern, String host, String session, int type) throws Exception {
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", type);
		jsonObj.put("pattern", "*");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + PARTICIPANT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		return new JSONObject(resp.getText());	
	}
	
	protected static JSONObject listAction(WebConversation webCon, String host, String session, int type) throws Exception {
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GROUPS);
		parameter.setParameter("type", String.valueOf(type));
		
		WebRequest req = new GetMethodWebRequest(host + PARTICIPANT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		return new JSONObject(resp.getText());	
	}
	
	protected static JSONObject listMembers(WebConversation webCon, String host, String session, int type, int groupId) throws Exception {
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, "members");
		parameter.setParameter("type", String.valueOf(type));
		parameter.setParameter("group_id", String.valueOf(groupId));
		
		WebRequest req = new GetMethodWebRequest(host + PARTICIPANT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		return new JSONObject(resp.getText());	
		
	}
	
	public static User[] searchUser(WebConversation webCon, String searchpattern, String host, String session, int type) throws Exception {
		JSONObject jsonObj = searchAction(webCon, searchpattern, host, session, com.openexchange.groupware.container.Participant.USER);
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		User[] u = new User[jsonArray.length()];
		for (int a = 0; a < u.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			u[a] = new User();
			u[a].setId(jObj.getInt(ParticipantsFields.ID));
			u[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return u;			
	}

	public static Group[] listGroup(WebConversation webCon, String host, String session) throws Exception {
		JSONObject jsonObj = listAction(webCon, host, session, com.openexchange.groupware.container.Participant.GROUP);
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		Group[] g = new Group[jsonArray.length()];
		for (int a = 0; a < g.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			g[a] = new Group();
			g[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			g[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return g;
	}
	
	public static Resource[] searchResource(WebConversation webCon, String searchpattern, String host, String session) throws Exception {
		JSONObject jsonObj = searchAction(webCon, searchpattern, host, session, com.openexchange.groupware.container.Participant.RESOURCE);
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		
		Resource[] r = new Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			r[a] = new Resource();
			r[a].setIdentifier(jObj.getInt(ParticipantsFields.ID));
			r[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return r;	
	}
	
	public static ResourceGroup[] listResourceGroup(WebConversation webCon, String host, String session) throws Exception {
		JSONObject jsonObj = listAction(webCon, host, session, com.openexchange.groupware.container.Participant.RESOURCEGROUP);
		JSONArray jsonArray = jsonObj.getJSONArray(jsonTagData);
		ResourceGroup[] rg = new ResourceGroup[jsonArray.length()];
		for (int a = 0; a < rg.length; a++) {
			JSONObject jObj = jsonArray.getJSONObject(a);
			rg[a] = new ResourceGroup();
			rg[a].setId(jObj.getInt(ParticipantsFields.ID));
			rg[a].setDisplayName(jObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return rg;
	}

}

