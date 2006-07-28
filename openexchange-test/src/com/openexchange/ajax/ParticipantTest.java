package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.openexchange.groupware.container.FolderObject;
import java.io.ByteArrayInputStream;
import org.json.JSONObject;


public class ParticipantTest extends CommonTest {
	
	private static String url = "/ajax/participant";
	
	private static int groupId = -1;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		url = getAJAXProperties().getProperty("participant_url");
		groupId = Integer.parseInt(getAJAXProperties().getProperty("group_id"));
	}
	
	public void testSearchUser() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCHUSER);
		parameter.append("&searchpattern=*");
		parameter.append("&folder=" + FolderObject.INTERNALUSERS);
		
		action(parameter.toString());
	}
	
	public void testGetMembers() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_GETMEMBERS);
		parameter.append("&group=" + groupId);
		
		action(parameter.toString());
	}
	
	public void testSearchResource() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_SEARCHRESOURCE);
		parameter.append("&searchpattern=*");
		
		action(parameter.toString());
	}
	
	public void testGetGroups() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_GETGROUPS);
		
		action(parameter.toString());
	}
	
	public void testGetResourceGroups() throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=" + AJAXServlet.ACTION_GETRESOURCEGROUPS);
		
		action(parameter.toString());
	}
	
	protected String getURL() {
		return url;
	}
	
	protected void action(String parameter) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
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
}

