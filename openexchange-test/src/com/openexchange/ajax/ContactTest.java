package com.openexchange.ajax;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;

public class ContactTest extends AbstractAJAXTest {
	
	public ContactTest(String name) {
		super(name);
	}
	
	public static final String CONTENT_TYPE = "image/png";
	
	public static final byte[] image = { -119, 80, 78, 71, 13, 10, 26, 10, 0,
	0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0,
	37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1,
	-1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26,
	-40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0,
	9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126,
	-4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0,
	1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
	
	protected final static int[] CONTACT_FIELDS = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.CATEGORIES,
		ContactObject.GIVEN_NAME,
		ContactObject.SUR_NAME,
		ContactObject.ANNIVERSARY,
		ContactObject.ASSISTANT_NAME,
		ContactObject.BIRTHDAY,
		ContactObject.BRANCHES,
		ContactObject.BUSINESS_CATEGORY,
		ContactObject.CELLULAR_TELEPHONE1,
		ContactObject.CELLULAR_TELEPHONE2,
		ContactObject.CITY_BUSINESS,
		ContactObject.CITY_HOME,
		ContactObject.CITY_OTHER,
		ContactObject.COLOR_LABEL,
		ContactObject.COMMERCIAL_REGISTER,
		ContactObject.COMPANY,
		ContactObject.COUNTRY_BUSINESS,
		ContactObject.COUNTRY_HOME,
		ContactObject.COUNTRY_OTHER,
		ContactObject.DEPARTMENT,
		ContactObject.DISPLAY_NAME,
		ContactObject.DISTRIBUTIONLIST,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
		ContactObject.IMAGE1,
		ContactObject.LINKS,
		ContactObject.MANAGER_NAME,
		ContactObject.MARITAL_STATUS,
		ContactObject.MIDDLE_NAME,
		ContactObject.NICKNAME,
		ContactObject.NOTE,
		ContactObject.NUMBER_OF_CHILDREN,
		ContactObject.NUMBER_OF_EMPLOYEE,
		ContactObject.POSITION,
		ContactObject.POSTAL_CODE_BUSINESS,
		ContactObject.POSTAL_CODE_HOME,
		ContactObject.POSTAL_CODE_OTHER,
		ContactObject.PRIVATE_FLAG,
		ContactObject.PROFESSION,
		ContactObject.ROOM_NUMBER,
		ContactObject.SALES_VOLUME,
		ContactObject.SPOUSE_NAME,
		ContactObject.STATE_BUSINESS,
		ContactObject.STATE_HOME,
		ContactObject.STATE_OTHER,
		ContactObject.STREET_BUSINESS,
		ContactObject.STREET_HOME,
		ContactObject.STREET_OTHER,
		ContactObject.SUFFIX,
		ContactObject.TAX_ID,
		ContactObject.TELEPHONE_ASSISTANT,
		ContactObject.TELEPHONE_BUSINESS1,
		ContactObject.TELEPHONE_BUSINESS2,
		ContactObject.TELEPHONE_CALLBACK,
		ContactObject.TELEPHONE_CAR,
		ContactObject.TELEPHONE_COMPANY,
		ContactObject.TELEPHONE_HOME1,
		ContactObject.TELEPHONE_HOME2,
		ContactObject.TELEPHONE_IP,
		ContactObject.TELEPHONE_ISDN,
		ContactObject.TELEPHONE_OTHER,
		ContactObject.TELEPHONE_PAGER,
		ContactObject.TELEPHONE_PRIMARY,
		ContactObject.TELEPHONE_RADIO,
		ContactObject.TELEPHONE_TELEX,
		ContactObject.TELEPHONE_TTYTDD,
		ContactObject.TITLE,
		ContactObject.URL,
		ContactObject.USERFIELD01,
		ContactObject.USERFIELD02,
		ContactObject.USERFIELD03,
		ContactObject.USERFIELD04,
		ContactObject.USERFIELD05,
		ContactObject.USERFIELD06,
		ContactObject.USERFIELD07,
		ContactObject.USERFIELD08,
		ContactObject.USERFIELD09,
		ContactObject.USERFIELD10,
		ContactObject.USERFIELD11,
		ContactObject.USERFIELD12,
		ContactObject.USERFIELD13,
		ContactObject.USERFIELD14,
		ContactObject.USERFIELD15,
		ContactObject.USERFIELD16,
		ContactObject.USERFIELD17,
		ContactObject.USERFIELD18,
		ContactObject.USERFIELD19,
		ContactObject.USERFIELD20,
		ContactObject.DEFAULT_ADDRESS
	};
	
	protected static final String CONTACT_URL = "/ajax/contacts";
	
	protected static int contactFolderId = -1;
	
	protected long dateTime = 0;
	
	protected int userId = 0;
	
	private static final Log LOG = LogFactory.getLog(ContactTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		final FolderObject folderObj = FolderTest.getStandardContactFolder(getWebConversation(), getHostName(), getSessionId());
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
	
	protected int createContactWithDistributionList(String title, ContactObject contactEntry) throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(contactEntry.getObjectID());
		
		contactObj.setDistributionList(entry);
		return insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	protected int createContactWithLinks(String title, ContactObject link1, ContactObject link2) throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(link1.getObjectID());
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(link2.getObjectID());
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
		return insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
	}
	
	protected void compareObject(ContactObject contactObj1, ContactObject contactObj2) throws Exception {
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
		assertEqualsAndNotNull("display name is not equals", contactObj1.getDisplayName(), contactObj2.getDisplayName());
		assertEqualsAndNotNull("email1 is not equals", contactObj1.getEmail1(), contactObj2.getEmail1());
		assertEqualsAndNotNull("email2 is not equals", contactObj1.getEmail2(), contactObj2.getEmail2());
		assertEqualsAndNotNull("email3 is not equals", contactObj1.getEmail3(), contactObj2.getEmail3());
		assertEqualsAndNotNull("employee type is not equals", contactObj1.getEmployeeType(), contactObj2.getEmployeeType());
		assertEqualsAndNotNull("fax business is not equals", contactObj1.getFaxBusiness(), contactObj2.getFaxBusiness());
		assertEqualsAndNotNull("fax home is not equals", contactObj1.getFaxHome(), contactObj2.getFaxHome());
		assertEqualsAndNotNull("fax other is not equals", contactObj1.getFaxOther(), contactObj2.getFaxOther());
		assertEqualsAndNotNull("info is not equals", contactObj1.getInfo(), contactObj2.getInfo());
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
		//contactObj.setDisplayName(displayname);
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
		contactObj.setInfo("info");
		contactObj.setInstantMessenger1("instant messenger1");
		contactObj.setInstantMessenger2("instant messenger2");
		contactObj.setImage1(image);
		contactObj.setImageContentType("image/png");
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
		contactObj.setDefaultAddress(1);
		
		contactObj.setParentFolderID(contactFolderId);
		
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
		link1.setObjectID(linkId1);
		int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
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
	
	public static int insertContact(WebConversation webCon, ContactObject contactObj, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		final StringWriter stringWriter = new StringWriter();		
		final JSONObject jsonObj = new JSONObject();
		ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
		contactWriter.writeContact(contactObj, jsonObj);
		
		stringWriter.write(jsonObj.toString());
		stringWriter.flush();

		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		
		WebRequest req = null;
		WebResponse resp = null;
		
		JSONObject jResponse = null;
		
		if (contactObj.containsImage1()) {
			PostMethodWebRequest postReq = new PostMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
			postReq.setMimeEncoded(true);
			
			postReq.setParameter("json", stringWriter.toString());
			
			File f = File.createTempFile("open-xchange_image", ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(contactObj.getImage1());
			fos.flush();
			fos.close();
			
			postReq.selectFile("file", f, CONTENT_TYPE);
			
			req = postReq;
			resp = webCon.getResource(req);
			jResponse = extractFromCallback(resp.getText());
		} else {
			final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
			
			req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
			resp = webCon.getResponse(req);
			
			jResponse = new JSONObject(resp.getText());
		}
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(jResponse.toString());
		
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
		host = appendPrefix(host);
		
		final StringWriter stringWriter = new StringWriter();
		final JSONObject jsonObj = new JSONObject();
		ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
		contactWriter.writeContact(contactObj, jsonObj);
		
		stringWriter.write(jsonObj.toString());
		stringWriter.flush();
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		parameter.setParameter(DataFields.ID, String.valueOf(objectId));
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(inFolder));
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		WebRequest req = null;
		WebResponse resp = null;
		
		JSONObject jResponse = null;
		
		if (contactObj.containsImage1()) {
			PostMethodWebRequest postReq = new PostMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
			postReq.setMimeEncoded(true);
			postReq.setParameter("json", stringWriter.toString());
			
			File f = File.createTempFile("open-xchange_image", ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(contactObj.getImage1());
			fos.flush();
			fos.close();
			
			postReq.selectFile("file", f, CONTENT_TYPE);
			
			req = postReq;
			resp = webCon.getResource(req);
			jResponse = extractFromCallback(resp.getText());
		} else {
			ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
			
			req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
			resp = webCon.getResponse(req);
			
			jResponse = new JSONObject(resp.getText());
		}
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(jResponse.toString());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static void deleteContact(WebConversation webCon, int id, int inFolder, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(DataFields.ID, id);
		jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
	}
	
	public static ContactObject[] listContact(WebConversation webCon, int inFolder, int[] cols, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		//System.out.println(host + CONTACT_URL + parameter.getURLParameters());
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject[] searchContact(WebConversation webCon, String searchpattern, int inFolder, int[] cols, String host, String session) throws OXException, Exception {
		return searchContact(webCon, searchpattern, inFolder, cols, false, host, session);
	}
	
	public static ContactObject[] searchContact(WebConversation webCon, String searchpattern, int inFolder, int[] cols, boolean startletter, String host, String session) throws OXException, Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pattern", searchpattern);
		jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		jsonObj.put("startletter", startletter);
		
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), new ByteArrayInputStream(jsonObj.toString().getBytes()), "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			throw new TestException(response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject[] searchContactAdvanced(WebConversation webCon, ContactSearchObject cso,int folder, int[] cols, String host, String session) throws OXException, Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
	
		JSONObject jsonObj = new JSONObject();
		//jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, folder);
		jsonObj.put(ContactFields.LAST_NAME,cso.getSurname());
		jsonObj.put(ContactFields.FIRST_NAME ,cso.getGivenName());
		jsonObj.put(ContactFields.DISPLAY_NAME ,cso.getDisplayName());
		jsonObj.put(ContactFields.EMAIL1 ,cso.getEmail1());
		jsonObj.put(ContactFields.EMAIL2 ,cso.getEmail2());
		jsonObj.put(ContactFields.EMAIL3 ,cso.getEmail3());
		
		if (cso.getEmailAutoComplete()){
			jsonObj.put("emailAutoComplete","true");
			//parameter.setParameter("emailAutoComplete","true");
		}
		
		WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), new ByteArrayInputStream(jsonObj.toString().getBytes()), "text/javascript");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			throw new TestException(response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject[] listContact(WebConversation webCon, int[][] objectIdAndFolderId, int[] cols, String host, String session) throws Exception {
		host = appendPrefix(host);
		
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
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject loadContact(WebConversation webCon, int objectId, int inFolder, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		parameter.setParameter(DataFields.ID, objectId);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		ContactObject contactObj = new ContactObject();
		
		ContactParser contactParser = new ContactParser(null);
		contactParser.parse(contactObj, (JSONObject)response.getData());
		
		return contactObj;
	}
	
	public static ContactObject loadUser(WebConversation webCon, int userId, int inFolder, String host, String session) throws Exception {
		int[] cols = {
			DataObject.OBJECT_ID,
			DataObject.CREATED_BY,
			DataObject.CREATION_DATE,
			DataObject.LAST_MODIFIED,
			DataObject.MODIFIED_BY,
			FolderChildObject.FOLDER_ID,
			CommonObject.CATEGORIES,
			ContactObject.GIVEN_NAME,
			ContactObject.SUR_NAME,
			ContactObject.EMAIL1,
			ContactObject.EMAIL2,
			ContactObject.EMAIL3,
			ContactObject.INTERNAL_USERID
		};
		
		ContactObject[] contactArray = listContact(webCon, inFolder, cols, host, session);
		
		for (int a = 0; a < contactArray.length; a++) {
			if (contactArray[a].getInternalUserId() == userId) {
				return contactArray[a];
			}
		}
		
		return null;
	}
	
	public static byte[] loadImage(WebConversation webCon, int objectId, int inFolder, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_IMAGE);
		parameter.setParameter(DataFields.ID, objectId);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		InputStream is = resp.getInputStream();
		assertNotNull("response InputStream is null", is);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int i = 0;
		while ((i = is.read(b)) != -1) {
			baos.write(b, 0, i);
		}
		
		return baos.toByteArray();
	}
	
	
	public static ContactObject[] listModifiedAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		int[] cols = new int[]{ AppointmentObject.OBJECT_ID };
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
	}
	
	public static ContactObject[] listDeleteAppointment(WebConversation webCon, int inFolder, Date modified, String host, String session) throws Exception {
		host = appendPrefix(host);
		
		int[] cols = new int[]{ AppointmentObject.OBJECT_ID };
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
		parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
		parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "updated");
		parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
		parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));
		
		WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		if (response.hasError()) {
			fail("json error: " + response.getErrorMessage());
		}
		
		assertNotNull("timestamp", response.getTimestamp());
		
		assertEquals(200, resp.getResponseCode());
		
		return jsonArray2ContactArray((JSONArray)response.getData(), cols);
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
	
	private static ContactObject[] jsonArray2ContactArray(JSONArray jsonArray, int[] cols) throws Exception {
		ContactObject[] contactArray = new ContactObject[jsonArray.length()];
		
		for (int a = 0; a < contactArray.length; a++) {
			contactArray[a] = new ContactObject();
			parseCols(cols, jsonArray.getJSONArray(a), contactArray[a]);
		}
		
		return contactArray;
	}
	
	private static void parseCols(int[] cols, JSONArray jsonArray, ContactObject contactObj) throws Exception {
		assertEquals("compare array size with cols size", cols.length, jsonArray.length());
		
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
			case ContactObject.PRIVATE_FLAG:
				contactObj.setPrivateFlag(jsonArray.getBoolean(pos));
				break;
			case ContactObject.SUR_NAME:
				contactObj.setSurName(jsonArray.getString(pos));
				break;
			case ContactObject.GIVEN_NAME:
				contactObj.setGivenName(jsonArray.getString(pos));
				break;
			case ContactObject.ANNIVERSARY:
				String lAnniversary = jsonArray.getString(pos);
				if (lAnniversary != null && !lAnniversary.equals("null")) {
					contactObj.setAnniversary(new Date(Long.parseLong(lAnniversary)));
				} else {
					contactObj.setAnniversary(null);
				}
				break;
			case ContactObject.ASSISTANT_NAME:
				contactObj.setAssistantName(jsonArray.getString(pos));
				break;
			case ContactObject.BIRTHDAY:
				String lBirthday = jsonArray.getString(pos);
				if (lBirthday != null && !lBirthday.equals("null")) {
					contactObj.setBirthday(new Date(Long.parseLong(lBirthday)));
				} else {
					contactObj.setBirthday(null);
				}
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
			case ContactObject.DEFAULT_ADDRESS:
				contactObj.setDefaultAddress(jsonArray.getInt(pos));
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
				contactObj.setImage1(jsonArray.getString(pos).getBytes());
				break;
				/* NO LONGER PRESENT
			case ContactObject.NUMBER_OF_IMAGES:
				contactObj.setNumberOfImages(jsonArray.getInt(pos));
				break;
				 */
			case ContactObject.INFO:
				contactObj.setInfo(jsonArray.getString(pos));
				break;
			case ContactObject.INSTANT_MESSENGER1:
				contactObj.setInstantMessenger1(jsonArray.getString(pos));
				break;
			case ContactObject.INSTANT_MESSENGER2:
				contactObj.setInstantMessenger2(jsonArray.getString(pos));
				break;
			case ContactObject.INTERNAL_USERID:
				contactObj.setInternalUserId(jsonArray.getInt(pos));
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
				contactObj.setUserField02(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD03:
				contactObj.setUserField03(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD04:
				contactObj.setUserField04(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD05:
				contactObj.setUserField05(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD06:
				contactObj.setUserField06(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD07:
				contactObj.setUserField07(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD08:
				contactObj.setUserField08(jsonArray.getString(pos));
				break;
			case ContactObject.USERFIELD09:
				contactObj.setUserField09(jsonArray.getString(pos));
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
			case ContactObject.LINKS:
				contactObj.setLinks(parseLinks(contactObj, jsonArray.getJSONArray(pos)));
				break;
			case ContactObject.DISTRIBUTIONLIST:
				contactObj.setDistributionList(parseDistributionList(contactObj, jsonArray.getJSONArray(pos)));
				break;
			default:
				throw new Exception("missing field in mapping: " + field);
				
		}
	}
	
	private static LinkEntryObject[] parseLinks(ContactObject contactObj, JSONArray jsonArray) throws Exception {
		LinkEntryObject[] links = new LinkEntryObject[jsonArray.length()];
		for (int a = 0; a < links.length; a++) {
			links[a] = new LinkEntryObject();
			JSONObject entry = jsonArray.getJSONObject(a);
			if (entry.has(ContactFields.ID)) {
				links[a].setLinkID(DataParser.parseInt(entry, ContactFields.ID));
			}
			
			links[a].setLinkDisplayname(DataParser.parseString(entry, DistributionListFields.DISPLAY_NAME));
		}
		
		return links;
	}
	
	private static DistributionListEntryObject[] parseDistributionList(ContactObject contactObj, JSONArray jsonArray) throws Exception {
		DistributionListEntryObject[] distributionlist = new DistributionListEntryObject[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject entry = jsonArray.getJSONObject(a);
			distributionlist[a] = new DistributionListEntryObject();
			if (entry.has(DistributionListFields.ID)) {
				distributionlist[a].setEntryID(DataParser.parseInt(entry, DistributionListFields.ID));
			}
			
			if (entry.has(DistributionListFields.FIRST_NAME)) {
				distributionlist[a].setFirstname(DataParser.parseString(entry, DistributionListFields.FIRST_NAME));
			}
			
			if (entry.has(DistributionListFields.LAST_NAME)) {
				distributionlist[a].setLastname(DataParser.parseString(entry, DistributionListFields.LAST_NAME));
			}
			
			distributionlist[a].setDisplayname(DataParser.parseString(entry, DistributionListFields.DISPLAY_NAME));
			distributionlist[a].setEmailaddress(DataParser.parseString(entry, DistributionListFields.MAIL));
			distributionlist[a].setEmailfield(DataParser.parseInt(entry, DistributionListFields.MAIL_FIELD));
		}
		
		return distributionlist;
	}
	
	private HashSet links2String(LinkEntryObject[] linkEntryObject) throws Exception {
		if (linkEntryObject == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < linkEntryObject.length; a++) {
			hs.add(link2String(linkEntryObject[a]));
		}
		
		return hs;
	}
	
	private String link2String(LinkEntryObject linkEntryObject) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + linkEntryObject.getLinkID());
		sb.append("DISPLAYNAME" + linkEntryObject.getLinkDisplayname());
		
		return sb.toString();
	}
	
	private HashSet distributionlist2String(DistributionListEntryObject[] distributionListEntry) throws Exception {
		if (distributionListEntry == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < distributionListEntry.length; a++) {
			hs.add(entry2String(distributionListEntry[a]));
		}
		
		return hs;
	}
	
	private String entry2String(DistributionListEntryObject entry) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + entry.getEntryID());
		sb.append("D" + entry.getDisplayname());
		sb.append("F" + entry.getEmailfield());
		sb.append("E" + entry.getEmailaddress());
		
		return sb.toString();
	}
}

