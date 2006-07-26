package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactTest extends CommonTest {
	
	private static String url = "/ajax/contact";
	
	private static int contactFolderId = -1;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		url = ajaxProps.getProperty("contact_url");
		contactFolderId = Integer.parseInt(ajaxProps.getProperty("contact_folder"));
	}
	
	public void testNew() throws Exception {
		ContactObject contactObj = createContactObject();
		int objectId = actionNew(contactObj);
	}
	
	public void testUpdate() throws Exception {
		ContactObject contactObj = createContactObject();
		int objectId = actionNew(contactObj);
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		
		actionUpdate(contactObj);
	}
	
	public void testAll() throws Exception {
		actionAll(contactFolderId);
	}
	
	
	public void testList() throws Exception {
		ContactObject contactObj = createContactObject();
		int id1 = actionNew(contactObj);
		int id2 = actionNew(contactObj);
		int id3 = actionNew(contactObj);
		
		actionList(new int[]{id1, id2, id3});
	}
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject();
		int id1 = actionNew(contactObj);
		int id2 = actionNew(contactObj);
		
		actionDelete(new int[]{id1, id2, 1});
	}
	
	public void testGet() throws Exception {
		ContactObject contactObj = createContactObject();
		int objectId = actionNew(contactObj);
		
		actionGet(objectId);
	}
	
	protected int actionNew(ContactObject contactObj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactWriter = new ContactWriter(pw);
		contactWriter.writeContact(contactObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		return insert(b);
	}
	
	protected void actionUpdate(ContactObject contactObj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactWriter = new ContactWriter(pw);
		contactWriter.writeContact(contactObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		update(b, contactObj.getObjectID());
	}
	
	protected void actionDelete(int[] id) throws Exception{
		delete(id);
	}
	
	protected void actionList(int[] id) throws Exception{
		list(id);
	}
	
	protected void actionAll(int folderId) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=all");
		parameter.append("&folder=" + folderId);
		parameter.append("&columns=1%2C500");
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has("error")) {
			fail("server error: " + (String)jsonobject.get("error"));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
		} else {
			fail("no data in JSON object!");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionGet(int objectId) throws Exception {
		WebResponse resp = getObject(objectId);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			
			ContactParser contactParser = new ContactParser(null);
			ContactObject contactObj = new ContactObject();
			contactParser.parse(contactObj, data);
			
			assertEquals("same folder id:", contactFolderId, contactObj.getParentFolderID());
		} else {
			fail("missing data in json object");
		}
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected String getURL() {
		return url;
	}
	
	private ContactObject createContactObject() {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("Müller");
		contactObj.setGivenName("Herbert");
		contactObj.setStreetBusiness("Franz-Meier Weg 17");
		contactObj.setCityBusiness("Test Stadt");
		contactObj.setStateBusiness("NRW");
		contactObj.setCountryBusiness("Deutschland");
		contactObj.setTelephoneBusiness1("+49112233445566");
		contactObj.setCompany("Internal Test AG");
		contactObj.setParentFolderID(contactFolderId);
		
		return contactObj;
	}
}

