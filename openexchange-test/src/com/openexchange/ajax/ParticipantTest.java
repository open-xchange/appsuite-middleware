package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceGroup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.URLParameter;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;


public class ParticipantTest extends CommonTest {
	
	private static final String PARTICIPANT_URL = "/ajax/participants";
	
	public void testUserSearch() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", com.openexchange.groupware.container.Participant.USER);
		jsonObj.put(FolderChildFields.FOLDER_ID, FolderObject.INTERNALUSERS);
		jsonObj.put("pattern", "*");
		
		actionPut(parameter.toString(), jsonObj.toString().getBytes());
	}
	
	public void testResourceSearch() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", com.openexchange.groupware.container.Participant.RESOURCE);
		jsonObj.put("pattern", "*");
		
		actionPut(parameter.toString(), jsonObj.toString().getBytes());
	}
	
	public void testGetGroupMembers() throws Exception {
		int groupId = 0;
		
		Group[] g = listGroup(getWebConversation(), getHostName(), getSessionId());
		
		groupId = g[0].getIdentifier();
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=members");
		parameter.append("&type=" + com.openexchange.groupware.container.Participant.GROUP);
		parameter.append("&group_id=" + groupId);
		
		actionGet(parameter.toString());
	}
	
	public void testGetGroups() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&type=" + com.openexchange.groupware.container.Participant.GROUP);
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_GROUPS);
		
		actionGet(parameter.toString());
	}
	
	public void testGetResourceGroups() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&type=" + com.openexchange.groupware.container.Participant.RESOURCEGROUP);
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_GROUPS);
		
		actionGet(parameter.toString());
	}
	
	protected String getURL() {
		return PARTICIPANT_URL;
	}
	
	protected void actionGet(String parameter) throws Exception {
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString());
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}

		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
	}
	
	protected void actionPut(String parameter, byte[] b) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (!jsonobject.has(jsonTagData)) {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
	}
	
	protected static JSONArray searchAction(WebConversation webCon, String searchpattern, String host, String session, int type) throws Exception {
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", type);
		jsonObj.put("pattern", "*");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + PARTICIPANT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		return new JSONArray(resp.getText());	
	}
	
	protected static JSONArray listAction(WebConversation webCon, String host, String session, int type) throws Exception {
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GROUPS);
		parameter.setParameter("type", String.valueOf(type));
		
		WebRequest req = new GetMethodWebRequest(host + PARTICIPANT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		return new JSONArray(resp.getText());	
	}
	
	public static User[] searchUser(WebConversation webCon, String searchpattern, String host, String session, int type) throws Exception {
		JSONArray jsonArray = searchAction(webCon, searchpattern, host, session, com.openexchange.groupware.container.Participant.USER);
		User[] u = new User[jsonArray.length()];
		for (int a = 0; a < u.length; a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			u[a] = new User();
			u[a].setId(jsonObj.getInt(ParticipantsFields.ID));
			u[a].setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return u;			
	}

	public static Group[] listGroup(WebConversation webCon, String host, String session) throws Exception {
		JSONArray jsonArray = listAction(webCon, host, session, com.openexchange.groupware.container.Participant.GROUP);
		Group[] g = new Group[jsonArray.length()];
		for (int a = 0; a < g.length; a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			g[a] = new Group();
			g[a].setIdentifier(jsonObj.getInt(ParticipantsFields.ID));
			g[a].setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return g;
	}
	
	public static Resource[] searchResource(WebConversation webCon, String searchpattern, String host, String session) throws Exception {
		JSONArray jsonArray = searchAction(webCon, searchpattern, host, session, com.openexchange.groupware.container.Participant.RESOURCE);
		
		Resource[] r = new Resource[jsonArray.length()];
		for (int a = 0; a < r.length; a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			r[a] = new Resource();
			r[a].setIdentifier(jsonObj.getInt(ParticipantsFields.ID));
			r[a].setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return r;	
	}
	
	public static ResourceGroup[] listResourceGroup(WebConversation webCon, String host, String session) throws Exception {
		JSONArray jsonArray = listAction(webCon, host, session, com.openexchange.groupware.container.Participant.RESOURCEGROUP);
		ResourceGroup[] rg = new ResourceGroup[jsonArray.length()];
		for (int a = 0; a < rg.length; a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			rg[a] = new ResourceGroup();
			rg[a].setId(jsonObj.getInt(ParticipantsFields.ID));
			rg[a].setDisplayName(jsonObj.getString(ParticipantsFields.DISPLAY_NAME));
		}
		
		return rg;
	}

}

