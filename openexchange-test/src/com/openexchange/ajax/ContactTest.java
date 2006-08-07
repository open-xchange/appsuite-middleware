package com.openexchange.ajax;


import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.types.Response;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.tools.URLParameter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactTest extends AbstractAJAXTest {
	
	private static final String CONTACT_URL = "/ajax/contacts";
	
	private static int contactFolderId = -1;
	
	private static final Log LOG = LogFactory.getLog(ContactTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		FolderObject contactFolder = FolderTest.getStandardContactFolder(getWebConversation(), getHostName(), getSessionId());
		contactFolderId = contactFolder.getObjectID();
	}
	
	public void testNew() throws Exception {
		ContactObject contactObj = createContactObject("testNew");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testNewWithDistributionList() throws Exception {
		int objectId = createContactWithDistributionList("testNewWithDistributionList");
	}
	
	public void testNewWithLinks() throws Exception {
		int objectId = createContactWithLinks("testNewWithLinks");
	}
	
	public void testUpdate() throws Exception {
		ContactObject contactObj = createContactObject("testUpdate");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
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
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testUpdateWithLinks() throws Exception {
		int objectId = createContactWithLinks("testUpdateWithLinks");
		
		ContactObject link1 = createContactObject("link1");
		int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithLinks");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		LinkEntryObject[] links = new LinkEntryObject[1];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		
		contactObj.setLinks(links);
		
		updateContact(getWebConversation(), contactObj, objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testAll() throws Exception {
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		ContactObject[] contactArray = listContact(getWebConversation(), contactFolderId, cols, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testList() throws Exception {
		ContactObject contactObj = createContactObject("testList");
		int id1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int id3 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());

		final int[][] objectIdAndFolderId = { { id1, contactFolderId }, { id2, contactFolderId }, { id3, contactFolderId } };
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.TITLE, AppointmentObject.CREATED_BY, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
		
		ContactObject[] contactArray = listContact(getWebConversation(), objectIdAndFolderId, cols, PROTOCOL + getHostName(), getSessionId());
		
		assertEquals("check response array", 3, contactArray.length);
	}
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int id1 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		int id2 = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		final int[][] objectIdAndFolderId = { { id1, contactFolderId }, { id2, contactFolderId }, { 1, contactFolderId } };
		
		deleteContact(getWebConversation(), objectIdAndFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGet() throws Exception {
		ContactObject contactObj = createContactObject("testGet");
		int objectId = insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
		loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testGetWithDistributionList() throws Exception {
		int objectId = createContactWithDistributionList("testGetWithDistributionList");
		
		loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());

	}
	
	public void testGetWithLinks() throws Exception {
		int objectId = createContactWithLinks("testGetWithLinks");
		
		loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	protected String getURL() {
		return CONTACT_URL;
	}
	
	protected int createContactWithDistributionList(String title) throws Exception {
		ContactObject contactEntry = createContactObject(title);
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(contactId);
		
		contactObj.setDistributionList(entry);
		return insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
		
	}
	
	protected int createContactWithLinks(String title) throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
		
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
		
		return insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
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
	
	public static int insertContact(WebConversation webCon, ContactObject contactObj, String host, String session) throws Exception {
		int objectId = 0;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactWriter = new ContactWriter(pw);
		contactWriter.writeContact(contactObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONObject data = (JSONObject)response.getData();
		if (data.has(DataFields.ID)) {
			objectId = data.getInt(DataFields.ID);
		}
		
		return objectId;
	}
	
	public static void updateContact(WebConversation webCon, ContactObject contactObj, int objectId, int inFolder, String host, String session) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactWriter = new ContactWriter(pw);
		contactWriter.writeContact(contactObj);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		parameter.setParameter(DataFields.ID, String.valueOf(objectId));
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(inFolder));
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static int[] deleteContact(WebConversation webCon, int[][] objectIdAndFolderId, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int[] i = objectIdAndFolderId[a];
			JSONObject jObj = new JSONObject();
			jObj.put(DataFields.ID, i[0]);
			jObj.put(AJAXServlet.PARAMETER_INFOLDER, i[1]);
			jsonArray.put(jObj);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		JSONArray data = (JSONArray)response.getData();
		int i[] = new int[data.length()];
		for (int a = 0; a < i.length; a++) {
			i[a] = data.getInt(a);
		}
		
		return i;
	}
	
	public static ContactObject[] listContact(WebConversation webCon, int inFolder, int[] cols, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject[] listContact(WebConversation webCon, int[][] objectIdAndFolderId, int[] cols, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String( cols ));
		
		JSONArray jsonArray = new JSONArray();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int i[] = objectIdAndFolderId[a];
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(DataFields.ID, i[0]);
			jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, i[1]);
			jsonArray.put(jsonObj);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject loadContact(WebConversation webCon, int objectId, int inFolder, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(DataFields.ID, objectId);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		ContactObject contactObj = new ContactObject();
		
		ContactParser contactParser = new ContactParser(null);
		contactParser.parse(contactObj, (JSONObject)response.getData());
		
		return contactObj;
	}
	
	public static ContactObject[] listModifiedAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP_SINCE, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(new int[]{ AppointmentObject.OBJECT_ID }));
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData());
	}
	
	public static ContactObject[] listDeleteAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "updated");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP_SINCE, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(new int[]{ AppointmentObject.OBJECT_ID }));
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = ResponseParser.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2AppointmentArray((JSONArray)response.getData());
	}
	
	private static ContactObject[] jsonArray2AppointmentArray(JSONArray jsonArray) throws Exception {
		ContactObject[] contactArray = new ContactObject[jsonArray.length()];
		
		ContactParser contactParser = new ContactParser();
		
		for (int a = 0; a < contactArray.length; a++) {
			contactArray[a] = new ContactObject();
			JSONObject jObj = jsonArray.getJSONObject(a);
			
			contactParser.parse(contactArray[a], jObj);
		}
		
		return contactArray;
	}
	
	private static ContactObject[] jsonArray2AppointmentArray(JSONArray jsonArray, int[] cols) throws Exception {
		ContactObject[] contactArray = new ContactObject[jsonArray.length()];
		
		for (int a = 0; a < contactArray.length; a++) {
			contactArray[a] = new ContactObject();
			parseCols(cols, jsonArray.getJSONArray(a), contactArray[a]);
		}
		
		return contactArray;
	}
	
	private static void parseCols(int[] cols, JSONArray jsonArray, ContactObject contactObj) throws Exception {
		for (int a = 0; a < cols.length; a++) {
			parse(a, cols[a], jsonArray, contactObj);
		}
	}
	
	private static void parse(int pos, int field, JSONArray jsonArray, ContactObject contactObj) throws Exception {
		switch (field) {
			case ContactObject.OBJECT_ID:
				contactObj.setObjectID(jsonArray.getInt(pos));
				break;
			case ContactObject.CREATED_BY:
				contactObj.setCreatedBy(jsonArray.getInt(pos));
				break;
			case ContactObject.CREATION_DATE:
				contactObj.setCreationDate(new Date(jsonArray.getLong(pos)));
				break;
			case ContactObject.MODIFIED_BY:
				contactObj.setModifiedBy(jsonArray.getInt(pos));
				break;
			case ContactObject.LAST_MODIFIED:
				contactObj.setLastModified(new Date(jsonArray.getLong(pos)));
				break;
			case ContactObject.FOLDER_ID:
				contactObj.setParentFolderID(jsonArray.getInt(pos));
				break;
			case ContactObject.SUR_NAME:
				contactObj.setSurName(jsonArray.getString(pos));
				break;
			case ContactObject.GIVEN_NAME:
				contactObj.setGivenName(jsonArray.getString(pos));
				break;
			case ContactObject.ANNIVERSARY:
				contactObj.setAnniversary(new Date(jsonArray.getLong(pos)));
				break;
			case ContactObject.ASSISTANT_NAME:
				contactObj.setAssistantName(jsonArray.getString(pos));
				break;
			case ContactObject.BIRTHDAY:
				contactObj.setBirthday(new Date(jsonArray.getLong(pos)));
				break;
			case ContactObject.BRANCHES:
				contactObj.setBranches(jsonArray.getString(pos));
				break;
			case ContactObject.BUSINESS_CATEGORY:
				contactObj.setBusinessCategory(jsonArray.getString(pos));
				break;
			case ContactObject.CATEGORIES:
				contactObj.setCategories(jsonArray.getString(pos));
				break;
			case ContactObject.CELLULAR_TELEPHONE1:
				contactObj.setCellularTelephone1(jsonArray.getString(pos));
				break;
			case ContactObject.CELLULAR_TELEPHONE2:
				contactObj.setCellularTelephone2(jsonArray.getString(pos));
				break;
			case ContactObject.CITY_HOME:
				contactObj.setCityHome(jsonArray.getString(pos));
				break;
			case ContactObject.CITY_BUSINESS:
				contactObj.setCityBusiness(jsonArray.getString(pos));
				break;		
			case ContactObject.CITY_OTHER:
				contactObj.setCityOther(jsonArray.getString(pos));
				break;
			case ContactObject.COMMERCIAL_REGISTER:
				contactObj.setCommercialRegister(jsonArray.getString(pos));
				break;
			case ContactObject.COMPANY:
				contactObj.setCompany(jsonArray.getString(pos));
				break;
			case ContactObject.COUNTRY_HOME:
				contactObj.setCountryHome(jsonArray.getString(pos));
				break;
			case ContactObject.COUNTRY_BUSINESS:
				contactObj.setCountryBusiness(jsonArray.getString(pos));
				break;
			case ContactObject.COUNTRY_OTHER:
				contactObj.setCountryOther(jsonArray.getString(pos));
				break;
			case ContactObject.DEPARTMENT:
				contactObj.setDepartment(jsonArray.getString(pos));
				break;
			case ContactObject.DISPLAY_NAME:
				contactObj.setDisplayName(jsonArray.getString(pos));
				break;
			case ContactObject.EMAIL1:
				contactObj.setEmail1(jsonArray.getString(pos));
				break;
			case ContactObject.EMAIL2:
				contactObj.setEmail2(jsonArray.getString(pos));
				break;
			case ContactObject.EMAIL3:
				contactObj.setEmail3(jsonArray.getString(pos));
				break;
			case ContactObject.EMPLOYEE_TYPE:
				contactObj.setEmployeeType(jsonArray.getString(pos));
				break;
			case ContactObject.FAX_BUSINESS:
				contactObj.setFaxBusiness(jsonArray.getString(pos));
				break;
			case ContactObject.FAX_HOME:
				contactObj.setFaxHome(jsonArray.getString(pos));
				break;
			case ContactObject.FAX_OTHER:
				contactObj.setFaxOther(jsonArray.getString(pos));
				break;
			case ContactObject.IMAGE1:
				contactObj.setImage1(jsonArray.getString(pos));
				break;
			case ContactObject.INFO:
				contactObj.setInfo(jsonArray.getString(pos));
				break;
			case ContactObject.INSTANT_MESSENGER1:
				contactObj.setInstantMessenger1(jsonArray.getString(pos));
				break;
			case ContactObject.INSTANT_MESSENGER2:
				contactObj.setInstantMessenger2(jsonArray.getString(pos));
				break;
			case ContactObject.COLOR_LABEL:
				contactObj.setLabel(jsonArray.getInt(pos));
				break;
			case ContactObject.MANAGER_NAME:
				contactObj.setManagerName(jsonArray.getString(pos));
				break;
			case ContactObject.MARITAL_STATUS:
				contactObj.setMaritalStatus(jsonArray.getString(pos));
				break;
			case ContactObject.MIDDLE_NAME:
				contactObj.setMiddleName(jsonArray.getString(pos));
				break;
			case ContactObject.NICKNAME:
				contactObj.setNickname(jsonArray.getString(pos));
				break;
			case ContactObject.NOTE:
				contactObj.setNote(jsonArray.getString(pos));
				break;
			case ContactObject.NUMBER_OF_CHILDREN:
				contactObj.setNumberOfChildren(jsonArray.getString(pos));
				break;
			case ContactObject.NUMBER_OF_EMPLOYEE:
				contactObj.setNumberOfEmployee(jsonArray.getString(pos));
				break;
			case ContactObject.POSITION:
				contactObj.setPosition(jsonArray.getString(pos));
				break;
			case ContactObject.POSTAL_CODE_HOME:
				contactObj.setPostalCodeHome(jsonArray.getString(pos));
				break;
			case ContactObject.POSTAL_CODE_BUSINESS:
				contactObj.setPostalCodeBusiness(jsonArray.getString(pos));
				break;
			case ContactObject.POSTAL_CODE_OTHER:
				contactObj.setPostalCodeOther(jsonArray.getString(pos));
				break;
			case ContactObject.PROFESSION:
				contactObj.setProfession(jsonArray.getString(pos));
				break;
			case ContactObject.ROOM_NUMBER:
				contactObj.setRoomNumber(jsonArray.getString(pos));
				break;
			case ContactObject.SALES_VOLUME:
				contactObj.setSalesVolume(jsonArray.getString(pos));
				break;
			case ContactObject.SPOUSE_NAME:
				contactObj.setSpouseName(jsonArray.getString(pos));
				break;
			case ContactObject.STATE_HOME:
				contactObj.setStateHome(jsonArray.getString(pos));
				break;
			case ContactObject.STATE_BUSINESS:
				contactObj.setStateBusiness(jsonArray.getString(pos));
				break;
			case ContactObject.STATE_OTHER:
				contactObj.setStateOther(jsonArray.getString(pos));
				break;
			case ContactObject.STREET_HOME:
				contactObj.setStreetHome(jsonArray.getString(pos));
				break;
			case ContactObject.STREET_BUSINESS:
				contactObj.setStreetBusiness(jsonArray.getString(pos));
				break;
			case ContactObject.STREET_OTHER:
				contactObj.setStreetOther(jsonArray.getString(pos));
				break;
			case ContactObject.SUFFIX:
				contactObj.setSuffix(jsonArray.getString(pos));
				break;
			case ContactObject.TAX_ID:
				contactObj.setTaxID(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_ASSISTANT:
				contactObj.setTelephoneAssistant(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_BUSINESS1:
				contactObj.setTelephoneBusiness1(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_BUSINESS2:
				contactObj.setTelephoneBusiness2(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_CALLBACK:
				contactObj.setTelephoneCallback(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_CAR:
				contactObj.setTelephoneCar(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_COMPANY:
				contactObj.setTelephoneCompany(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_HOME1:
				contactObj.setTelephoneHome1(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_HOME2:
				contactObj.setTelephoneHome2(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_IP:
				contactObj.setTelephoneIP(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_ISDN:
				contactObj.setTelephoneISDN(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_OTHER:
				contactObj.setTelephoneOther(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_PAGER:
				contactObj.setTelephonePager(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_PRIMARY:
				contactObj.setTelephonePrimary(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_RADIO:
				contactObj.setTelephoneRadio(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_TELEX:
				contactObj.setTelephoneTelex(jsonArray.getString(pos));
				break;
			case ContactObject.TELEPHONE_TTYTDD:
				contactObj.setTelephoneTTYTTD(jsonArray.getString(pos));
				break;
			case ContactObject.TITLE:
				contactObj.setTitle(jsonArray.getString(pos));
				break;
			case ContactObject.URL:
				contactObj.setURL(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD01:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD02:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD03:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD04:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD05:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD06:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD07:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD08:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD09:
				contactObj.setUserField01(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD10:
				contactObj.setUserField10(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD11:
				contactObj.setUserField11(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD12:
				contactObj.setUserField12(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD13:
				contactObj.setUserField13(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD14:
				contactObj.setUserField14(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD15:
				contactObj.setUserField15(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD16:
				contactObj.setUserField16(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD17:
				contactObj.setUserField17(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD18:
				contactObj.setUserField18(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD19:
				contactObj.setUserField19(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD20:
				contactObj.setUserField20(jsonArray.getString(pos));
				break;

		}
	}
}

