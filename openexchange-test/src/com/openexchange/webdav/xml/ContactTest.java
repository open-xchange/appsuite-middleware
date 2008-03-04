package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class ContactTest extends AbstractWebdavXMLTest {
	
	protected int contactFolderId = -1;
	
	private static final String CONTACT_URL = "/servlet/webdav.contacts";
	
	private long dateTime = 0;
	
	public ContactTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		final FolderObject folderObj = FolderTest.getContactDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		contactFolderId = folderObj.getObjectID();
		userId = folderObj.getCreatedBy();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		dateTime = c.getTimeInMillis();
	}
	
	public static void compareObject(ContactObject contactObj1, ContactObject contactObj2) throws Exception {
		assertEquals("id is not equals", contactObj1.getObjectID(), contactObj2.getObjectID());
		assertEquals("folder id is not equals", contactObj1.getParentFolderID(), contactObj2.getParentFolderID());
		assertEquals("private flag is not equals", contactObj1.getPrivateFlag(), contactObj2.getPrivateFlag());
		assertEqualsAndNotNull("categories is not equals", contactObj1.getCategories(), contactObj2.getCategories());
		assertEqualsAndNotNull("given name is not equals", contactObj1.getGivenName(), contactObj2.getGivenName());
		assertEqualsAndNotNull("surname is not equals", contactObj1.getSurName(), contactObj2.getSurName());
		assertEqualsAndNotNull("anniversary is not equals", contactObj1.getAnniversary(), contactObj2.getAnniversary());
		assertEqualsAndNotNull("assistant name is not equals", contactObj1.getAssistantName(), contactObj2.getAssistantName());
		assertEqualsAndNotNull("birthday is not equals", contactObj1.getBirthday(), contactObj2.getBirthday());
		assertEqualsAndNotNull("branches is not equals", contactObj1.getBranches(), contactObj2.getBranches());
		assertEqualsAndNotNull("business categorie is not equals", contactObj1.getBusinessCategory(), contactObj2.getBusinessCategory());
		assertEqualsAndNotNull("cellular telephone1 is not equals", contactObj1.getCellularTelephone1(), contactObj2.getCellularTelephone1());
		assertEqualsAndNotNull("cellular telephone2 is not equals", contactObj1.getCellularTelephone2(), contactObj2.getCellularTelephone2());
		assertEqualsAndNotNull("city business is not equals", contactObj1.getCityBusiness(), contactObj2.getCityBusiness());
		assertEqualsAndNotNull("city home is not equals", contactObj1.getCityHome(), contactObj2.getCityHome());
		assertEqualsAndNotNull("city other is not equals", contactObj1.getCityOther(), contactObj2.getCityOther());
		assertEqualsAndNotNull("commercial register is not equals", contactObj1.getCommercialRegister(), contactObj2.getCommercialRegister());
		assertEqualsAndNotNull("company is not equals", contactObj1.getCompany(), contactObj2.getCompany());
		assertEqualsAndNotNull("country business is not equals", contactObj1.getCountryBusiness(), contactObj2.getCountryBusiness());
		assertEqualsAndNotNull("country home is not equals", contactObj1.getCountryHome(), contactObj2.getCountryHome());
		assertEqualsAndNotNull("country other is not equals", contactObj1.getCountryOther(), contactObj2.getCountryOther());
		assertEqualsAndNotNull("department is not equals", contactObj1.getDepartment(), contactObj2.getDepartment());
		// assertEqualsAndNotNull("display name is not equals", contactObj1.getDisplayName(), contactObj2.getDisplayName());
		assertEqualsAndNotNull("email1 is not equals", contactObj1.getEmail1(), contactObj2.getEmail1());
		assertEqualsAndNotNull("email2 is not equals", contactObj1.getEmail2(), contactObj2.getEmail2());
		assertEqualsAndNotNull("email3 is not equals", contactObj1.getEmail3(), contactObj2.getEmail3());
		assertEqualsAndNotNull("employee type is not equals", contactObj1.getEmployeeType(), contactObj2.getEmployeeType());
		assertEqualsAndNotNull("fax business is not equals", contactObj1.getFaxBusiness(), contactObj2.getFaxBusiness());
		assertEqualsAndNotNull("fax home is not equals", contactObj1.getFaxHome(), contactObj2.getFaxHome());
		assertEqualsAndNotNull("fax other is not equals", contactObj1.getFaxOther(), contactObj2.getFaxOther());
		assertEqualsAndNotNull("info is not equals", contactObj1.getInfo(), contactObj2.getInfo());
		assertEqualsAndNotNull("image1 is not equals", contactObj1.getImage1(), contactObj2.getImage1());
		assertEqualsAndNotNull("instant messenger1 is not equals", contactObj1.getInstantMessenger1(), contactObj2.getInstantMessenger1());
		assertEqualsAndNotNull("instant messenger2 is not equals", contactObj1.getInstantMessenger2(), contactObj2.getInstantMessenger2());
		assertEqualsAndNotNull("instant messenger2 is not equals", contactObj1.getInstantMessenger2(), contactObj2.getInstantMessenger2());
		assertEqualsAndNotNull("marital status is not equals", contactObj1.getMaritalStatus(), contactObj2.getMaritalStatus());
		assertEqualsAndNotNull("manager name is not equals", contactObj1.getManagerName(), contactObj2.getManagerName());
		assertEqualsAndNotNull("middle name is not equals", contactObj1.getMiddleName(), contactObj2.getMiddleName());
		assertEqualsAndNotNull("nickname is not equals", contactObj1.getNickname(), contactObj2.getNickname());
		assertEqualsAndNotNull("note is not equals", contactObj1.getNote(), contactObj2.getNote());
		assertEqualsAndNotNull("number of children is not equals", contactObj1.getNumberOfChildren(), contactObj2.getNumberOfChildren());
		assertEqualsAndNotNull("number of employee is not equals", contactObj1.getNumberOfEmployee(), contactObj2.getNumberOfEmployee());
		assertEqualsAndNotNull("position is not equals", contactObj1.getPosition(), contactObj2.getPosition());
		assertEqualsAndNotNull("postal code business is not equals", contactObj1.getPostalCodeBusiness(), contactObj2.getPostalCodeBusiness());
		assertEqualsAndNotNull("postal code home is not equals", contactObj1.getPostalCodeHome(), contactObj2.getPostalCodeHome());
		assertEqualsAndNotNull("postal code other is not equals", contactObj1.getPostalCodeOther(), contactObj2.getPostalCodeOther());
		assertEqualsAndNotNull("profession is not equals", contactObj1.getProfession(), contactObj2.getProfession());
		assertEqualsAndNotNull("room number is not equals", contactObj1.getRoomNumber(), contactObj2.getRoomNumber());
		assertEqualsAndNotNull("sales volume is not equals", contactObj1.getSalesVolume(), contactObj2.getSalesVolume());
		assertEqualsAndNotNull("spouse name is not equals", contactObj1.getSpouseName(), contactObj2.getSpouseName());
		assertEqualsAndNotNull("state business is not equals", contactObj1.getStateBusiness(), contactObj2.getStateBusiness());
		assertEqualsAndNotNull("state home is not equals", contactObj1.getStateHome(), contactObj2.getStateHome());
		assertEqualsAndNotNull("state other is not equals", contactObj1.getStateOther(), contactObj2.getStateOther());
		assertEqualsAndNotNull("street business is not equals", contactObj1.getStreetBusiness(), contactObj2.getStreetBusiness());
		assertEqualsAndNotNull("street home is not equals", contactObj1.getStreetHome(), contactObj2.getStreetHome());
		assertEqualsAndNotNull("street other is not equals", contactObj1.getStreetOther(), contactObj2.getStreetOther());
		assertEqualsAndNotNull("suffix is not equals", contactObj1.getSuffix(), contactObj2.getSuffix());
		assertEqualsAndNotNull("tax id is not equals", contactObj1.getTaxID(), contactObj2.getTaxID());
		assertEqualsAndNotNull("telephone assistant is not equals", contactObj1.getTelephoneAssistant(), contactObj2.getTelephoneAssistant());
		assertEqualsAndNotNull("telephone business1 is not equals", contactObj1.getTelephoneBusiness1(), contactObj2.getTelephoneBusiness1());
		assertEqualsAndNotNull("telephone business2 is not equals", contactObj1.getTelephoneBusiness2(), contactObj2.getTelephoneBusiness2());
		assertEqualsAndNotNull("telephone callback is not equals", contactObj1.getTelephoneCallback(), contactObj2.getTelephoneCallback());
		assertEqualsAndNotNull("telephone car is not equals", contactObj1.getTelephoneCar(), contactObj2.getTelephoneCar());
		assertEqualsAndNotNull("telehpone company is not equals", contactObj1.getTelephoneCompany(), contactObj2.getTelephoneCompany());
		assertEqualsAndNotNull("telephone home1 is not equals", contactObj1.getTelephoneHome1(), contactObj2.getTelephoneHome1());
		assertEqualsAndNotNull("telephone home2 is not equals", contactObj1.getTelephoneHome2(), contactObj2.getTelephoneHome2());
		assertEqualsAndNotNull("telehpone ip is not equals", contactObj1.getTelephoneIP(), contactObj2.getTelephoneIP());
		assertEqualsAndNotNull("telehpone isdn is not equals", contactObj1.getTelephoneISDN(), contactObj2.getTelephoneISDN());
		assertEqualsAndNotNull("telephone other is not equals", contactObj1.getTelephoneOther(), contactObj2.getTelephoneOther());
		assertEqualsAndNotNull("telephone pager is not equals", contactObj1.getTelephonePager(), contactObj2.getTelephonePager());
		assertEqualsAndNotNull("telephone primary is not equals", contactObj1.getTelephonePrimary(), contactObj2.getTelephonePrimary());
		assertEqualsAndNotNull("telephone radio is not equals", contactObj1.getTelephoneRadio(), contactObj2.getTelephoneRadio());
		assertEqualsAndNotNull("telephone telex is not equals", contactObj1.getTelephoneTelex(), contactObj2.getTelephoneTelex());
		assertEqualsAndNotNull("telephone ttytdd is not equals", contactObj1.getTelephoneTTYTTD(), contactObj2.getTelephoneTTYTTD());
		assertEqualsAndNotNull("title is not equals", contactObj1.getTitle(), contactObj2.getTitle());
		assertEqualsAndNotNull("url is not equals", contactObj1.getURL(), contactObj2.getURL());
		assertEqualsAndNotNull("userfield01 is not equals", contactObj1.getUserField01(), contactObj2.getUserField01());
		assertEqualsAndNotNull("userfield02 is not equals", contactObj1.getUserField02(), contactObj2.getUserField02());
		assertEqualsAndNotNull("userfield03 is not equals", contactObj1.getUserField03(), contactObj2.getUserField03());
		assertEqualsAndNotNull("userfield04 is not equals", contactObj1.getUserField04(), contactObj2.getUserField04());
		assertEqualsAndNotNull("userfield05 is not equals", contactObj1.getUserField05(), contactObj2.getUserField05());
		assertEqualsAndNotNull("userfield06 is not equals", contactObj1.getUserField06(), contactObj2.getUserField06());
		assertEqualsAndNotNull("userfield07 is not equals", contactObj1.getUserField07(), contactObj2.getUserField07());
		assertEqualsAndNotNull("userfield08 is not equals", contactObj1.getUserField08(), contactObj2.getUserField08());
		assertEqualsAndNotNull("userfield09 is not equals", contactObj1.getUserField09(), contactObj2.getUserField09());
		assertEqualsAndNotNull("userfield10 is not equals", contactObj1.getUserField10(), contactObj2.getUserField10());
		assertEqualsAndNotNull("userfield11 is not equals", contactObj1.getUserField11(), contactObj2.getUserField11());
		assertEqualsAndNotNull("userfield12 is not equals", contactObj1.getUserField12(), contactObj2.getUserField12());
		assertEqualsAndNotNull("userfield13 is not equals", contactObj1.getUserField13(), contactObj2.getUserField13());
		assertEqualsAndNotNull("userfield14 is not equals", contactObj1.getUserField14(), contactObj2.getUserField14());
		assertEqualsAndNotNull("userfield15 is not equals", contactObj1.getUserField15(), contactObj2.getUserField15());
		assertEqualsAndNotNull("userfield16 is not equals", contactObj1.getUserField16(), contactObj2.getUserField16());
		assertEqualsAndNotNull("userfield17 is not equals", contactObj1.getUserField17(), contactObj2.getUserField17());
		assertEqualsAndNotNull("userfield18 is not equals", contactObj1.getUserField18(), contactObj2.getUserField18());
		assertEqualsAndNotNull("userfield19 is not equals", contactObj1.getUserField19(), contactObj2.getUserField19());
		assertEqualsAndNotNull("userfield20 is not equals", contactObj1.getUserField20(), contactObj2.getUserField20());
		assertEqualsAndNotNull("number of attachments is not equals", contactObj1.getNumberOfAttachments(), contactObj2.getNumberOfAttachments());
		assertEqualsAndNotNull("default address is not equals", contactObj1.getDefaultAddress(), contactObj2.getDefaultAddress());
		
		assertEqualsAndNotNull("links are not equals", links2String(contactObj1.getLinks()), links2String(contactObj2.getLinks()));
		assertEqualsAndNotNull("distribution list is not equals", distributionlist2String(contactObj1.getDistributionList()), distributionlist2String(contactObj2.getDistributionList()));
	}
	
	protected ContactObject createContactObject(String displayname) {
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
		contactObj.setEmail1("hebert.meier@open-xchange.com");
		contactObj.setParentFolderID(contactFolderId);
		
		return contactObj;
	}
	
	protected ContactObject createCompleteContactObject() throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setPrivateFlag(true);
		contactObj.setCategories("categories");
		contactObj.setGivenName("given name");
		contactObj.setSurName("surname");
		contactObj.setAnniversary(new Date(dateTime));
		contactObj.setAssistantName("assistant name");
		contactObj.setBirthday(new Date(dateTime));
		contactObj.setBranches("branches");
		contactObj.setBusinessCategory("business categorie");
		contactObj.setCellularTelephone1("cellular telephone1");
		contactObj.setCellularTelephone2("cellular telephone2");
		contactObj.setCityBusiness("city business");
		contactObj.setCityHome("city home");
		contactObj.setCityOther("city other");
		contactObj.setCommercialRegister("commercial register");
		contactObj.setCompany("company");
		contactObj.setCountryBusiness("country business");
		contactObj.setCountryHome("country home");
		contactObj.setCountryOther("country other");
		contactObj.setDepartment("department");
		contactObj.setDisplayName("display name");
		contactObj.setEmail1("email1@test.de");
		contactObj.setEmail2("email2@test.de");
		contactObj.setEmail3("email3@test.de");
		contactObj.setEmployeeType("employee type");
		contactObj.setFaxBusiness("fax business");
		contactObj.setFaxHome("fax home");
		contactObj.setFaxOther("fax other");
		//contactObj.setImage1(com.openexchange.ajax.ContactTest.image);
		//contactObj.setImageContentType("image/jpeg");
		contactObj.setInfo("info");
		contactObj.setInstantMessenger1("instant messenger1");
		contactObj.setInstantMessenger2("instant messenger2");
		contactObj.setManagerName("manager name");
		contactObj.setMaritalStatus("marital status");
		contactObj.setMiddleName("middle name");
		contactObj.setNickname("nickname");
		contactObj.setNote("note");
		contactObj.setNumberOfChildren("number of children");
		contactObj.setNumberOfEmployee("number of employee");
		contactObj.setPosition("position");
		contactObj.setPostalCodeBusiness("postal code business");
		contactObj.setPostalCodeHome("postal code home");
		contactObj.setPostalCodeOther("postal code other");
		contactObj.setProfession("profession");
		contactObj.setRoomNumber("room number");
		contactObj.setSalesVolume("sales volume");
		contactObj.setSpouseName("spouse name");
		contactObj.setStateBusiness("state business");
		contactObj.setStateHome("state home");
		contactObj.setStateOther("state other");
		contactObj.setStreetBusiness("street business");
		contactObj.setStreetHome("street home");
		contactObj.setStreetOther("street other");
		contactObj.setSuffix("suffix");
		contactObj.setTaxID("tax id");
		contactObj.setTelephoneAssistant("telephone assistant");
		contactObj.setTelephoneBusiness1("telephone business1");
		contactObj.setTelephoneBusiness2("telephone business2");
		contactObj.setTelephoneCallback("telephone callback");
		contactObj.setTelephoneCar("telephone car");
		contactObj.setTelephoneCompany("telehpone company");
		contactObj.setTelephoneHome1("telephone home1");
		contactObj.setTelephoneHome2("telephone home2");
		contactObj.setTelephoneIP("telehpone ip");
		contactObj.setTelephoneISDN("telehpone isdn");
		contactObj.setTelephoneOther("telephone other");
		contactObj.setTelephonePager("telephone pager");
		contactObj.setTelephonePrimary("telephone primary");
		contactObj.setTelephoneRadio("telephone radio");
		contactObj.setTelephoneTelex("telephone telex");
		contactObj.setTelephoneTTYTTD("telephone ttytdd");
		contactObj.setTitle("title");
		contactObj.setURL("url");
		contactObj.setUserField01("userfield01");
		contactObj.setUserField02("userfield02");
		contactObj.setUserField03("userfield03");
		contactObj.setUserField04("userfield04");
		contactObj.setUserField05("userfield05");
		contactObj.setUserField06("userfield06");
		contactObj.setUserField07("userfield07");
		contactObj.setUserField08("userfield08");
		contactObj.setUserField09("userfield09");
		contactObj.setUserField10("userfield10");
		contactObj.setUserField11("userfield11");
		contactObj.setUserField12("userfield12");
		contactObj.setUserField13("userfield13");
		contactObj.setUserField14("userfield14");
		contactObj.setUserField15("userfield15");
		contactObj.setUserField16("userfield16");
		contactObj.setUserField17("userfield17");
		contactObj.setUserField18("userfield18");
		contactObj.setUserField19("userfield19");
		contactObj.setUserField20("userfield20");
		
		contactObj.setParentFolderID(contactFolderId);
		
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = insertContact(webCon, link1, PROTOCOL + hostName, login, password);
		link1.setObjectID(linkId1);
		int linkId2 = insertContact(webCon, link2, PROTOCOL + hostName, login, password);
		link2.setObjectID(linkId2);
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(link1.getObjectID());
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(link2.getObjectID());
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject(link1.getDisplayName(), link1.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[1].setEntryID(link1.getObjectID());
		
		contactObj.setDistributionList(entry);
		
		return contactObj;
	}
	
	public static int insertContact(WebConversation webCon, ContactObject contactObj, String host, String login, String password) throws OXException, Exception {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		contactObj.removeObjectID();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		ContactWriter contactWriter = new ContactWriter();
		contactWriter.addContent2PropElement(eProp, contactObj, false, true);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		} else {
			contactObj = (ContactObject)response[0].getDataObject();
			objectId = contactObj.getObjectID();
			
			assertNotNull("last modified is null", contactObj.getLastModified());
			assertTrue("last modified is not > 0", contactObj.getLastModified().getTime() > 0);
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		assertTrue("check objectId", objectId > 0);
		
		return objectId;
	}
	
	public static void updateContact(WebConversation webCon, ContactObject contactObj, int objectId, int inFolder, String host, String login, String password) throws OXException, Exception {
		updateContact(webCon, contactObj, objectId, inFolder, new Date(), host, login, password);
	}
	
	public static void updateContact(WebConversation webCon, ContactObject contactObj, int objectId, int inFolder, Date lastModified, String host, String login, String password) throws OXException, Exception {
		host = appendPrefix(host);
		
		contactObj.setObjectID(objectId);
		contactObj.setLastModified(lastModified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		ContactWriter appointmentWriter = new ContactWriter();
		appointmentWriter.addContent2PropElement(eProp, contactObj, false, true);
		Element eInFolder = new Element("infolder", XmlServlet.NS);
		eInFolder.addContent(String.valueOf(inFolder));
		eProp.addContent(eInFolder);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		} else {
			contactObj = (ContactObject)response[0].getDataObject();
			objectId = contactObj.getObjectID();
			
			assertNotNull("last modified is null", contactObj.getLastModified());
			assertTrue("last modified is not > 0", contactObj.getLastModified().getTime() > 0);
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static int[] deleteContact(WebConversation webCon, int[][] objectIdAndFolderId, String host, String login, String password) throws Exception {
		ArrayList failed = new ArrayList();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			deleteContact(webCon, objectIdAndFolderId[a][0], objectIdAndFolderId[a][1], host, login, password);
		}
		
		return new int[] { };
	}
	
	public static void deleteContact(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws OXException, Exception {
		deleteContact(webCon, objectId, inFolder, new Date(), host, login, password);
	}
	
	public static void deleteContact(WebConversation webCon, int objectId, int inFolder, Date lastModified, String host, String login, String password) throws OXException, Exception {
		host = appendPrefix(host);
		
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		ContactObject contactObj = new ContactObject();
		contactObj.setObjectID(objectId);
		contactObj.setParentFolderID(inFolder);
		contactObj.setLastModified(lastModified);
		
		Element eProp = new Element("prop", webdav);
		
		ContactWriter contactWriter = new ContactWriter();
		contactWriter.addContent2PropElement(eProp, contactObj, false, true);
		
		Element eMethod = new Element("method", XmlServlet.NS);
		eMethod.addContent("DELETE");
		eProp.addContent(eMethod);
		
		rootElement.addContent(addProp2PropertyUpdate(eProp));
		
		Document doc = new Document(rootElement);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());		
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static int[] listContact(WebConversation webCon, int inFolder, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eObjectmode.addContent("LIST");
		
		eProp.addContent(eFolderId);
		eProp.addContent(eObjectmode);
		
		ePropfind.addContent(eProp);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + CONTACT_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT, true);
		
		assertEquals("response length not is 1", 1, response.length);
		
		return (int[])response[0].getDataObject();
	}
	
	public static ContactObject[] listContact(WebConversation webCon, int inFolder, Date modified, boolean changed, boolean deleted, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		if (!changed && !deleted) {
			return new ContactObject[] { };
		}
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eLastSync.addContent(String.valueOf(modified.getTime()));
		
		StringBuffer objectMode = new StringBuffer();
		
		if (changed) {
			objectMode.append("NEW_AND_MODIFIED,");
		}
		
		if (deleted) {
			objectMode.append("DELETED,");
		}
		
		objectMode.delete(objectMode.length()-1, objectMode.length());
		
		eObjectmode.addContent(objectMode.toString());
		eProp.addContent(eObjectmode);
		
		ePropfind.addContent(eProp);
		eProp.addContent(eFolderId);
		eProp.addContent(eLastSync);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + CONTACT_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT);
		
		ContactObject[] contactArray = new ContactObject[response.length];
		for (int a = 0; a < contactArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			contactArray[a] = (ContactObject)response[a].getDataObject();
			assertNotNull("last modified is null", contactArray[a].getLastModified());
		}
		
		return contactArray;
	}
	
	public static ContactObject loadContact(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws OXException, Exception {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eObjectId = new Element("object_id", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eObjectId.addContent(String.valueOf(objectId));
		
		ePropfind.addContent(eProp);
		eProp.addContent(eFolderId);
		eProp.addContent(eObjectId);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + CONTACT_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.CONTACT);
		
		assertEquals("check response" , 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return (ContactObject)response[0].getDataObject();
	}
	
	private static HashSet links2String(LinkEntryObject[] linkEntryObject) throws Exception {
		if (linkEntryObject == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < linkEntryObject.length; a++) {
			hs.add(link2String(linkEntryObject[a]));
		}
		
		return hs;
	}
	
	private static String link2String(LinkEntryObject linkEntryObject) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + linkEntryObject.getLinkID());
		sb.append("DISPLAYNAME" + linkEntryObject.getLinkDisplayname());
		
		return sb.toString();
	}
	
	private static HashSet distributionlist2String(DistributionListEntryObject[] distributionListEntry) throws Exception {
		if (distributionListEntry == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < distributionListEntry.length; a++) {
			hs.add(entry2String(distributionListEntry[a]));
		}
		
		return hs;
	}
	
	private static String entry2String(DistributionListEntryObject entry) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + entry.getEntryID());
		sb.append("D" + entry.getDisplayname());
		sb.append("F" + entry.getEmailfield());
		sb.append("E" + entry.getEmailaddress());
		
		return sb.toString();
	}
}

