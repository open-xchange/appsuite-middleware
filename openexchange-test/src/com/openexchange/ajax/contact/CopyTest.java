package com.openexchange.ajax.contact;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.URLParameter;

public class CopyTest extends ContactTest {
	
	private static final Log LOG = LogFactory.getLog(CopyTest.class);
	
	public CopyTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCopy() throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testCopy");
		contactObj.setParentFolderID(contactFolderId);
		int objectId1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		String login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
		String password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
		
		FolderObject folderObj = com.openexchange.webdav.xml.FolderTest.createFolderObject(userId, "testCopy" + System.currentTimeMillis(), FolderObject.CONTACT, false);
		int targetFolder = com.openexchange.webdav.xml.FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), login, password);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY);
		parameter.setParameter(AJAXServlet.PARAMETER_ID, objectId1);
		parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID, contactFolderId);
		parameter.setParameter(AppointmentFields.IGNORE_CONFLICTS, true);
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(FolderChildFields.FOLDER_ID, targetFolder);
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes("UTF-8"));
		WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		int objectId2 = 0;
		
		JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId2 = data.getInt(DataFields.ID);
		}
		
		deleteContact(getWebConversation(), objectId1, contactFolderId, PROTOCOL + getHostName(), getSessionId());
		if (objectId2 > 0) {
			deleteContact(getWebConversation(), objectId2, targetFolder, PROTOCOL + getHostName(), getSessionId());
		}
	}
}