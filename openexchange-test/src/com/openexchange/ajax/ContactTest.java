package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
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
		ContactObject contactObj = createContactObject("testNew");
		int objectId = actionNew(contactObj);
	}
	
	public void testNewWithDistributionList() throws Exception {
		ContactObject contactEntry = createContactObject("testWithDistributionList");
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = actionNew(contactEntry);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testWithDistributionList");
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(contactId);
		
		contactObj.setDistributionList(entry);
		
		int objectId = actionNew(contactObj);
	}
	
	public void testNewWithLinks() throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = actionNew(link1);
		int linkId2 = actionNew(link2);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testWithLinks");
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(linkId2);
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
		int objectId = actionNew(contactObj);
	}
	
	public void testUpdate() throws Exception {
		ContactObject contactObj = createContactObject("testUpdate");
		int objectId = actionNew(contactObj);
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		
		actionUpdate(contactObj);
	}
	
	public void testUpdateWithDistributionList() throws Exception {
		ContactObject contactObj = createContactObject("testUpdateWithDistributionList");
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		
		contactObj.setDistributionList(entry);
		
		int objectId = actionNew(contactObj);
	}
	
	public void testUpdateWithLinks() throws Exception {
		ContactObject link1 = createContactObject("link1");
		int linkId1 = actionNew(link1);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithLinks");
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		
		contactObj.setLinks(links);
		
		int objectId = actionNew(contactObj);
	}
	
	public void testAll() throws Exception {
		actionAll(contactFolderId);
	}
	
	public void testList() throws Exception {
		ContactObject contactObj = createContactObject("testList");
		int id1 = actionNew(contactObj);
		int id2 = actionNew(contactObj);
		int id3 = actionNew(contactObj);
		
		actionList(new int[]{id1, id2, id3});
	}
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int id1 = actionNew(contactObj);
		int id2 = actionNew(contactObj);
		
		actionDelete(new int[]{id1, id2, 1});
	}
	
	public void testGet() throws Exception {
		ContactObject contactObj = createContactObject("testGet");
		int objectId = actionNew(contactObj);
		
		actionGet(objectId);
	}
	
	public void testGetWithDistributionList() throws Exception {
		ContactObject contactEntry = createContactObject("testGetWithDistributionList");
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = actionNew(contactEntry);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testGetWithDistributionList");
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		
		contactObj.setDistributionList(entry);
		
		int objectId = actionNew(contactObj);
		
		actionGet(objectId);
	}
	
	public void testGetWithLinks() throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = actionNew(link1);
		int linkId2 = actionNew(link2);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testGetWithLinks");
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(linkId2);
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
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
		list(id, new int[]{ ContactObject.OBJECT_ID, ContactObject.SUR_NAME } );
	}
	
	protected void actionAll(int folderId) throws Exception {
		StringBuffer parameter = new StringBuffer();
		parameter.append("?session=" + sessionId);
		parameter.append("&action=all");
		parameter.append("&folder=" + folderId);
		parameter.append("&columns=");
		parameter.append(ContactObject.OBJECT_ID + "%2C");
		parameter.append(ContactObject.SUR_NAME);
		
		req = new GetMethodWebRequest(PROTOCOL + hostName + url + parameter.toString());
		resp = webConversation.getResponse(req);
		
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + (String)jsonobject.get(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONArray data = jsonobject.getJSONArray(jsonTagData);
		} else {
			fail("no data in JSON object!");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected void actionGet(int objectId) throws Exception {
		WebResponse resp = getObject(objectId);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		if (jsonobject.has(jsonTagError)) {
			fail("json error: " + jsonobject.get(jsonTagError));
		}
		
		if (jsonobject.has(jsonTagData)) {
			JSONObject data = jsonobject.getJSONObject(jsonTagData);
			
			ContactParser contactParser = new ContactParser(null);
			ContactObject contactObj = new ContactObject();
			contactParser.parse(contactObj, data);
			
			assertEquals("same folder id:", contactFolderId, contactObj.getParentFolderID());
		} else {
			fail("missing data in json object");
		}
		
		assertEquals(200, resp.getResponseCode());
	}
	
	protected String getURL() {
		return url;
	}
	
	private ContactObject createContactObject(String displayname) {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("Meier");
		contactObj.setGivenName("Herbert");
		contactObj.setDisplayName(displayname);
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

