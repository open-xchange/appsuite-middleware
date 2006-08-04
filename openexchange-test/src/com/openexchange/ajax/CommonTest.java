package com.openexchange.ajax;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.CommonObject;
import java.io.ByteArrayInputStream;
import java.util.Date;
import org.json.JSONArray;
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
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			if (data.has(DataFields.ID)) {
				object_id = data.getInt(DataFields.ID);
				assertTrue("object_id not > 0", (object_id > 0));
			} else {
				fail("no object_id in JSON object!");
			}
		} else {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
		
		return object_id;
	}
	
	protected void update(byte b[], int id, int inFolder) throws Exception {
		int object_id = 0;
		
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_UPDATE);
		parameter.append("&" + DataFields.ID + "=" + id);
		parameter.append("&" + AJAXServlet.PARAMETER_INFOLDER + "=" + inFolder);
		parameter.append("&" + AJAXServlet.PARAMETER_TIMESTAMP + "=" + new Date().getTime());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail("server error: " + jsonobject.getString(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void delete(int[] id) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_DELETE);
		parameter.append("&" + AJAXServlet.PARAMETER_TIMESTAMP + "=" + new Date().getTime());
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < id.length; a++) {
			jsonArray.put(id[a]);
		} 
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("server error: " + jsonobject.getString(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
			assertTrue("array length is 1", data.length() == 1);
			assertEquals("first entry in array is 1", 1, data.getInt(0));
		} else {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void list(int[] id, int inFolder, int[] cols) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_LIST);
		parameter.append("&" + AJAXServlet.PARAMETER_COLUMNS + "=");
		
		for (int a = 0; a  < cols.length; a++) {
			if (a == 0) {
				parameter.append(cols[a]);
			} else {
				parameter.append("%2C" + cols[a]);
			}
		}
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < id.length; a++) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, id[a]);
			jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
			jsonArray.put(jsonObj);
		} 
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		req = new PutMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString(), bais, "text/javascript");
		resp = getWebConversation().getResponse(req);
		JSONObject jsonobject = new JSONObject(resp.getText());

		if (jsonobject.has(jsonTagError)) {
			fail("server error: " + jsonobject.getString(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
			assertTrue("array length is >= 3", data.length() >= 3);
		} else {
			fail("no data in JSON object!");
		}
		
		if (!jsonobject.has(jsonTagTimestamp)) {
		 	fail("no timestamp tag found!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected WebResponse getObject(int object_id, int inFolder) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?" + AJAXServlet.PARAMETER_SESSION + "=" + getSessionId());
		parameter.append("&" + AJAXServlet.PARAMETER_ACTION + "=" + AJAXServlet.ACTION_GET);
		parameter.append("&" + DataFields.ID + "=" + object_id);
		parameter.append("&" + AJAXServlet.PARAMETER_INFOLDER + "=" + inFolder);
		
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + getURL() + parameter.toString());
		resp = getWebConversation().getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		return resp;
	}
	
	protected abstract String getURL() ;
	
}
