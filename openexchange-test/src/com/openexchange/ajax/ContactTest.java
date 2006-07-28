package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.OXFolderTools;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactTest extends CommonTest {
	
	private static String url = "/ajax/contact";
	
	private static int contactFolderId = -1;
	
	private static boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		init();
	}
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		url = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contact_url", url);
		
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(getLogin(), getPassword(), "localhost");
		
		contactFolderId = OXFolderTools.getContactStandardFolder(sessionObj.getUserObject().getId(), sessionObj.getContext());
		
		isInit = true;
	}
	
	public void testNew() throws Exception {
		ContactObject contactObj = createContactObject("testNew");
		int objectId = actionNew(contactObj);
	}
	
	public void testNewWithDistributionList() throws Exception {
		int objectId = createContactWithDistributionList("testNewWithDistributionList");
	}
	
	public void testNewWithLinks() throws Exception {
		int objectId = createContactWithLinks("testNewWithLinks");
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
		int objectId = createContactWithDistributionList("testUpdateWithDistributionList");
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithDistributionList");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		
		contactObj.setDistributionList(entry);
		
		actionUpdate(contactObj);
	}
	
	public void testUpdateWithLinks() throws Exception {
		int objectId = createContactWithLinks("testUpdateWithLinks");
		
		ContactObject link1 = createContactObject("link1");
		int linkId1 = actionNew(link1);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithLinks");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		LinkEntryObject[] links = new LinkEntryObject[1];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		
		contactObj.setLinks(links);
		
		actionUpdate(contactObj);
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
		int objectId = createContactWithDistributionList("testGetWithDistributionList");
		
		actionGet(objectId);
	}
	
	public void testGetWithLinks() throws Exception {
		int objectId = createContactWithLinks("testGetWithLinks");
		
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
		parameter.append("?session=" + getSessionId());
		parameter.append("&action=all");
		parameter.append("&folder=" + folderId);
		parameter.append("&columns=");
		parameter.append(ContactObject.OBJECT_ID + "%2C");
		parameter.append(ContactObject.SUR_NAME);
		
		req = new GetMethodWebRequest(PROTOCOL + getHostName() + url + parameter.toString());
		resp = getWebConversation().getResponse(req);
		
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
	
	protected int createContactWithDistributionList(String title) throws Exception {
		ContactObject contactEntry = createContactObject(title);
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = actionNew(contactEntry);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(contactId);
		
		contactObj.setDistributionList(entry);
		return actionNew(contactObj);
		
	}
	
	protected int createContactWithLinks(String title) throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = actionNew(link1);
		int linkId2 = actionNew(link2);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(linkId2);
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
		return actionNew(contactObj);
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

