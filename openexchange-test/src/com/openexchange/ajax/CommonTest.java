package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.container.CommonObject;
import java.io.ByteArrayInputStream;
import org.json.JSONObject;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class CommonTest extends AbstractAJAXTest {
	
	protected WebRequest req = null;
	
	protected WebResponse resp = null;
	
	protected int insert(byte b[]) throws Exception {
		int object_id = 0;
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=new");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + hostName + getURL() + parameter.toString(), bais, "text/javascript");
		resp = webConversation.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail("server error: " + (String)jsonobject.get("error"));
		}
		
		if (jsonobject.has(OXObject.OBJECT_ID)) {
			object_id = jsonobject.getInt(OXObject.OBJECT_ID);
			assertTrue("object_id not > 0", (object_id > 0));
		} else {
			fail("no object_id in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
		
		return object_id;
	}
	
	protected void update(byte b[]) throws Exception {
		int object_id = 0;
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=update");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + hostName + getURL() + parameter.toString(), bais, "text/javascript");
		resp = webConversation.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail("server error: " + jsonobject.getString("error"));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void delete(CommonObject commonObject) throws Exception {
		long begins = System.currentTimeMillis();
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=delete");
		parameter.append("&object_id=" + commonObject.getObjectID());
		parameter.append("&folder_id=" + commonObject.getParentFolderID());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		req = new PostMethodWebRequest(PROTOCOL + hostName + getURL() + parameter.toString(), bais, "text/javascript");
		resp = webConversation.getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail("server error: " + jsonobject.getString("error"));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void list(int folder_id, int from, int to) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=list");
		parameter.append("&folder_id=" + folder_id);
		parameter.append("&from=" + from);;
		parameter.append("&to=" + to);
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + getURL() + parameter.toString());
		resp = webConversation.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected WebResponse getObject(int object_id, int folder_id) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=get");
		parameter.append("&object_id=" + object_id);
		parameter.append("&folder_id=" + folder_id);
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + getURL() + parameter.toString());
		resp = webConversation.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		return resp;
	}
	
	protected abstract String getURL() ;
	
}
