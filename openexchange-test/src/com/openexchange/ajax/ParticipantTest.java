package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import java.io.ByteArrayInputStream;
import org.json.JSONObject;


public class ParticipantTest extends CommonTest {
	
	private static String url = "/ajax/participants";
	
	private static int groupId = 78;
	
	private static boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(getLogin(), getPassword(), "localhost");
		
		GroupStorage groupStorage = GroupStorage.getInstance(sessionObj.getContext());
		
		url = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "participant_url", url);
		
		String groupName = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "group_participant", "");
		
		Group group = groupStorage.searchGroups(groupName, new String[] { GroupStorage.DISPLAYNAME } )[0];
		
		groupId = group.getIdentifier();

		isInit = true;
	}
	
	public void testUserSearch() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", com.openexchange.groupware.container.Participant.USER);
		jsonObj.put(FolderChildFields.FOLDER_ID, FolderObject.INTERNALUSERS);
		jsonObj.put("searchpattern", "*");
		
		actionPut(parameter.toString(), jsonObj.toString().getBytes());
	}
	
	public void testResourceSearch() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCH);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("type", com.openexchange.groupware.container.Participant.RESOURCE);
		jsonObj.put("searchpattern", "*");
		
		actionPut(parameter.toString(), jsonObj.toString().getBytes());
	}
	
	public void testGetGroupMembers() throws Exception {
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
		parameter.append("&action=groups");
		
		actionGet(parameter.toString());
	}
	
	public void testGetResourceGroups() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&type=" + com.openexchange.groupware.container.Participant.RESOURCEGROUP);
		parameter.append("&action=groups");
		
		actionGet(parameter.toString());
	}
	
	protected String getURL() {
		return url;
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

}

