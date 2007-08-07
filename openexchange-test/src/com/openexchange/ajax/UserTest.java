package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.URLParameter;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserTest extends AbstractAJAXTest {
	
	protected final static int[] CONTACT_FIELDS = {
		DataObject.OBJECT_ID,
		ContactObject.EMAIL1,
	};
	
	public UserTest(String name) {
		super(name);
	}

	private static final String USER_URL = "/ajax/contacts";
	
	public void testSearch() throws Exception {
		com.openexchange.groupware.ldap.User users[] = searchUser(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("user array size > 0", users.length > 0);
	}

	public void testList() throws Exception {
		com.openexchange.groupware.ldap.User users[] = searchUser(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("user array size > 0", users.length > 0);
		
		int[] id = new int[users.length];
		for (int a = 0; a < id.length; a++) {
			id[a] = users[a].getId();
		}
		
		users = listUser(getWebConversation(), id, PROTOCOL + getHostName(), getSessionId());
		assertTrue("user array size > 0", users.length > 0);
	}
	
	public void testSearchUsers() throws Exception {
		com.openexchange.groupware.ldap.User users[] = searchUser(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("user array size > 0", users.length > 0);
	}
	
	public void testGet() throws Exception {
		com.openexchange.groupware.ldap.User users[] = searchUser(getWebConversation(), "*", PROTOCOL + getHostName(), getSessionId());
		assertTrue("user array size > 0", users.length > 0);
		com.openexchange.groupware.ldap.User user = loadUser(getWebConversation(), users[0].getId(), PROTOCOL + getHostName(), getSessionId());
	}
	
	public static UserImpl4Test[] searchUser(WebConversation webCon, String searchpattern, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		
		final StringBuffer stringBuffer = new StringBuffer();
		for (int a = 0; a < CONTACT_FIELDS.length; a++) {
			if (a > 0) {
				stringBuffer.append(',');
			} 
			stringBuffer.append(CONTACT_FIELDS[a]);
		}
		
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, stringBuffer.toString());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + USER_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		final JSONArray jsonArray = (JSONArray)response.getData();
		final UserImpl4Test[] user = new UserImpl4Test[jsonArray.length()];
		for (int a = 0; a < user.length; a++) {
			final JSONArray jsonContactArray = jsonArray.getJSONArray(a);
			user[a] = new UserImpl4Test();
			user[a].setId(jsonContactArray.getInt(0));
			user[a].setMail(jsonContactArray.getString(1));
		}
		
		return user;
	}
	
	public static UserImpl4Test[] listUser(WebConversation webCon, int[] id, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		
		final StringBuffer stringBuffer = new StringBuffer();
		for (int a = 0; a < CONTACT_FIELDS.length; a++) {
			if (a > 0) {
				stringBuffer.append(',');
			} 
			stringBuffer.append(CONTACT_FIELDS[a]);
		}
		
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, stringBuffer.toString());
		
		JSONArray requestArray = new JSONArray();
		for (int a = 0; a < id.length; a++) {
			JSONObject jData = new JSONObject();
			jData.put(DataFields.ID, id[a]);
			jData.put(AJAXServlet.PARAMETER_FOLDERID, FolderObject.SYSTEM_LDAP_FOLDER_ID);
			requestArray.put(jData);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(requestArray.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + USER_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONArray jsonArray = (JSONArray)response.getData();
		UserImpl4Test[] user = new UserImpl4Test[jsonArray.length()];
		for (int a = 0; a < user.length; a++) {
			final JSONArray jsonContactArray = jsonArray.getJSONArray(a);
			user[a] = new UserImpl4Test();
			user[a].setId(jsonContactArray.getInt(0));
			user[a].setMail(jsonContactArray.getString(1));
		}
		
		return user;
	}
	
	public static UserImpl4Test loadUser(WebConversation webCon, int userId, String host, String session) throws Exception {
		host = appendPrefix(host);
		
        final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(userId));
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, FolderObject.SYSTEM_LDAP_FOLDER_ID);
		
		WebRequest req = new GetMethodWebRequest(host + USER_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		JSONObject jsonObj = (JSONObject)response.getData();
		
		UserImpl4Test user = new UserImpl4Test();
		assertTrue("check id", jsonObj.has(ParticipantsFields.ID));
		user.setId(jsonObj.getInt(DataFields.ID));
		user.setMail(jsonObj.getString(ContactFields.EMAIL1));
		
		return user;
	}
}

