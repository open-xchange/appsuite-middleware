/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.user.UserTools;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.contact.Data;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;

public class ContactTest extends AbstractAJAXTest {

    public ContactTest(final String name) {
        super(name);
    }

    protected final static int[] CONTACT_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.CATEGORIES, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.ANNIVERSARY,
        Contact.ASSISTANT_NAME, Contact.BIRTHDAY, Contact.BRANCHES, Contact.BUSINESS_CATEGORY, Contact.CELLULAR_TELEPHONE1,
        Contact.CELLULAR_TELEPHONE2, Contact.CITY_BUSINESS, Contact.CITY_HOME, Contact.CITY_OTHER, Contact.COLOR_LABEL,
        Contact.COMMERCIAL_REGISTER, Contact.COMPANY, Contact.COUNTRY_BUSINESS, Contact.COUNTRY_HOME, Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT, Contact.DISPLAY_NAME, Contact.DISTRIBUTIONLIST, Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE, Contact.FAX_BUSINESS, Contact.FAX_HOME, Contact.FAX_OTHER, Contact.INFO, Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2, Contact.IMAGE1, Contact.MANAGER_NAME, Contact.MARITAL_STATUS, Contact.MIDDLE_NAME,
        Contact.NICKNAME, Contact.NOTE, Contact.NUMBER_OF_CHILDREN, Contact.NUMBER_OF_EMPLOYEE, Contact.POSITION,
        Contact.POSTAL_CODE_BUSINESS, Contact.POSTAL_CODE_HOME, Contact.POSTAL_CODE_OTHER, Contact.PRIVATE_FLAG, Contact.PROFESSION,
        Contact.ROOM_NUMBER, Contact.SALES_VOLUME, Contact.SPOUSE_NAME, Contact.STATE_BUSINESS, Contact.STATE_HOME, Contact.STATE_OTHER,
        Contact.STREET_BUSINESS, Contact.STREET_HOME, Contact.STREET_OTHER, Contact.SUFFIX, Contact.TAX_ID, Contact.TELEPHONE_ASSISTANT,
        Contact.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS2, Contact.TELEPHONE_CALLBACK, Contact.TELEPHONE_CAR,
        Contact.TELEPHONE_COMPANY, Contact.TELEPHONE_HOME1, Contact.TELEPHONE_HOME2, Contact.TELEPHONE_IP, Contact.TELEPHONE_ISDN,
        Contact.TELEPHONE_OTHER, Contact.TELEPHONE_PAGER, Contact.TELEPHONE_PRIMARY, Contact.TELEPHONE_RADIO, Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD, Contact.TITLE, Contact.URL, Contact.USERFIELD01, Contact.USERFIELD02, Contact.USERFIELD03,
        Contact.USERFIELD04, Contact.USERFIELD05, Contact.USERFIELD06, Contact.USERFIELD07, Contact.USERFIELD08, Contact.USERFIELD09,
        Contact.USERFIELD10, Contact.USERFIELD11, Contact.USERFIELD12, Contact.USERFIELD13, Contact.USERFIELD14, Contact.USERFIELD15,
        Contact.USERFIELD16, Contact.USERFIELD17, Contact.USERFIELD18, Contact.USERFIELD19, Contact.USERFIELD20, Contact.DEFAULT_ADDRESS };

    protected static final String CONTACT_URL = "/ajax/contacts";

    protected static int contactFolderId = -1;

    protected long dateTime = 0;

    protected int userId = 0;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactTest.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final FolderObject folderObj = FolderTest.getStandardContactFolder(getWebConversation(), getHostName(), getSessionId());
        contactFolderId = folderObj.getObjectID();
        userId = folderObj.getCreatedBy();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        dateTime = c.getTimeInMillis();
    }

    protected int createContactWithDistributionList(final String title, final Contact contactEntry) throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName(title);
        contactObj.setParentFolderID(contactFolderId);

        final DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
        entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
        entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
        entry[2] = new DistributionListEntryObject(
            contactEntry.getDisplayName(),
            contactEntry.getEmail1(),
            DistributionListEntryObject.EMAILFIELD1);
        entry[2].setEntryID(contactEntry.getObjectID());

        contactObj.setDistributionList(entry);
        return insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
    }

    protected void compareObject(final Contact contactObj1, final Contact contactObj2) throws Exception {
        assertEquals("id is not equals", contactObj1.getObjectID(), contactObj2.getObjectID());
        assertEquals("folder id is not equals", contactObj1.getParentFolderID(), contactObj2.getParentFolderID());
        assertEquals("private flag is not equals", contactObj1.getPrivateFlag(), contactObj2.getPrivateFlag());
        OXTestToolkit.assertEqualsAndNotNull("categories is not equals", contactObj1.getCategories(), contactObj2.getCategories());
        OXTestToolkit.assertEqualsAndNotNull("given name is not equals", contactObj1.getGivenName(), contactObj2.getGivenName());
        OXTestToolkit.assertEqualsAndNotNull("surname is not equals", contactObj1.getSurName(), contactObj2.getSurName());
        OXTestToolkit.assertEqualsAndNotNull("anniversary is not equals", contactObj1.getAnniversary(), contactObj2.getAnniversary());
        OXTestToolkit.assertEqualsAndNotNull("assistant name is not equals", contactObj1.getAssistantName(), contactObj2.getAssistantName());
        OXTestToolkit.assertEqualsAndNotNull("birthday is not equals", contactObj1.getBirthday(), contactObj2.getBirthday());
        OXTestToolkit.assertEqualsAndNotNull("branches is not equals", contactObj1.getBranches(), contactObj2.getBranches());
        OXTestToolkit.assertEqualsAndNotNull(
            "business categorie is not equals",
            contactObj1.getBusinessCategory(),
            contactObj2.getBusinessCategory());
        OXTestToolkit.assertEqualsAndNotNull(
            "cellular telephone1 is not equals",
            contactObj1.getCellularTelephone1(),
            contactObj2.getCellularTelephone1());
        OXTestToolkit.assertEqualsAndNotNull(
            "cellular telephone2 is not equals",
            contactObj1.getCellularTelephone2(),
            contactObj2.getCellularTelephone2());
        OXTestToolkit.assertEqualsAndNotNull("city business is not equals", contactObj1.getCityBusiness(), contactObj2.getCityBusiness());
        OXTestToolkit.assertEqualsAndNotNull("city home is not equals", contactObj1.getCityHome(), contactObj2.getCityHome());
        OXTestToolkit.assertEqualsAndNotNull("city other is not equals", contactObj1.getCityOther(), contactObj2.getCityOther());
        OXTestToolkit.assertEqualsAndNotNull(
            "commercial register is not equals",
            contactObj1.getCommercialRegister(),
            contactObj2.getCommercialRegister());
        OXTestToolkit.assertEqualsAndNotNull("company is not equals", contactObj1.getCompany(), contactObj2.getCompany());
        OXTestToolkit.assertEqualsAndNotNull(
            "country business is not equals",
            contactObj1.getCountryBusiness(),
            contactObj2.getCountryBusiness());
        OXTestToolkit.assertEqualsAndNotNull("country home is not equals", contactObj1.getCountryHome(), contactObj2.getCountryHome());
        OXTestToolkit.assertEqualsAndNotNull("country other is not equals", contactObj1.getCountryOther(), contactObj2.getCountryOther());
        OXTestToolkit.assertEqualsAndNotNull("department is not equals", contactObj1.getDepartment(), contactObj2.getDepartment());
        OXTestToolkit.assertEqualsAndNotNull("display name is not equals", contactObj1.getDisplayName(), contactObj2.getDisplayName());
        OXTestToolkit.assertEqualsAndNotNull("email1 is not equals", contactObj1.getEmail1(), contactObj2.getEmail1());
        OXTestToolkit.assertEqualsAndNotNull("email2 is not equals", contactObj1.getEmail2(), contactObj2.getEmail2());
        OXTestToolkit.assertEqualsAndNotNull("email3 is not equals", contactObj1.getEmail3(), contactObj2.getEmail3());
        OXTestToolkit.assertEqualsAndNotNull("employee type is not equals", contactObj1.getEmployeeType(), contactObj2.getEmployeeType());
        OXTestToolkit.assertEqualsAndNotNull("fax business is not equals", contactObj1.getFaxBusiness(), contactObj2.getFaxBusiness());
        OXTestToolkit.assertEqualsAndNotNull("fax home is not equals", contactObj1.getFaxHome(), contactObj2.getFaxHome());
        OXTestToolkit.assertEqualsAndNotNull("fax other is not equals", contactObj1.getFaxOther(), contactObj2.getFaxOther());
        OXTestToolkit.assertEqualsAndNotNull("info is not equals", contactObj1.getInfo(), contactObj2.getInfo());
        OXTestToolkit.assertEqualsAndNotNull(
            "instant messenger1 is not equals",
            contactObj1.getInstantMessenger1(),
            contactObj2.getInstantMessenger1());
        OXTestToolkit.assertEqualsAndNotNull(
            "instant messenger2 is not equals",
            contactObj1.getInstantMessenger2(),
            contactObj2.getInstantMessenger2());
        OXTestToolkit.assertEqualsAndNotNull(
            "instant messenger2 is not equals",
            contactObj1.getInstantMessenger2(),
            contactObj2.getInstantMessenger2());
        OXTestToolkit.assertEqualsAndNotNull("marital status is not equals", contactObj1.getMaritalStatus(), contactObj2.getMaritalStatus());
        OXTestToolkit.assertEqualsAndNotNull("manager name is not equals", contactObj1.getManagerName(), contactObj2.getManagerName());
        OXTestToolkit.assertEqualsAndNotNull("middle name is not equals", contactObj1.getMiddleName(), contactObj2.getMiddleName());
        OXTestToolkit.assertEqualsAndNotNull("nickname is not equals", contactObj1.getNickname(), contactObj2.getNickname());
        OXTestToolkit.assertEqualsAndNotNull("note is not equals", contactObj1.getNote(), contactObj2.getNote());
        OXTestToolkit.assertEqualsAndNotNull(
            "number of children is not equals",
            contactObj1.getNumberOfChildren(),
            contactObj2.getNumberOfChildren());
        OXTestToolkit.assertEqualsAndNotNull(
            "number of employee is not equals",
            contactObj1.getNumberOfEmployee(),
            contactObj2.getNumberOfEmployee());
        OXTestToolkit.assertEqualsAndNotNull("position is not equals", contactObj1.getPosition(), contactObj2.getPosition());
        OXTestToolkit.assertEqualsAndNotNull(
            "postal code business is not equals",
            contactObj1.getPostalCodeBusiness(),
            contactObj2.getPostalCodeBusiness());
        OXTestToolkit.assertEqualsAndNotNull(
            "postal code home is not equals",
            contactObj1.getPostalCodeHome(),
            contactObj2.getPostalCodeHome());
        OXTestToolkit.assertEqualsAndNotNull(
            "postal code other is not equals",
            contactObj1.getPostalCodeOther(),
            contactObj2.getPostalCodeOther());
        OXTestToolkit.assertEqualsAndNotNull("profession is not equals", contactObj1.getProfession(), contactObj2.getProfession());
        OXTestToolkit.assertEqualsAndNotNull("room number is not equals", contactObj1.getRoomNumber(), contactObj2.getRoomNumber());
        OXTestToolkit.assertEqualsAndNotNull("sales volume is not equals", contactObj1.getSalesVolume(), contactObj2.getSalesVolume());
        OXTestToolkit.assertEqualsAndNotNull("spouse name is not equals", contactObj1.getSpouseName(), contactObj2.getSpouseName());
        OXTestToolkit.assertEqualsAndNotNull("state business is not equals", contactObj1.getStateBusiness(), contactObj2.getStateBusiness());
        OXTestToolkit.assertEqualsAndNotNull("state home is not equals", contactObj1.getStateHome(), contactObj2.getStateHome());
        OXTestToolkit.assertEqualsAndNotNull("state other is not equals", contactObj1.getStateOther(), contactObj2.getStateOther());
        OXTestToolkit.assertEqualsAndNotNull(
            "street business is not equals",
            contactObj1.getStreetBusiness(),
            contactObj2.getStreetBusiness());
        OXTestToolkit.assertEqualsAndNotNull("street home is not equals", contactObj1.getStreetHome(), contactObj2.getStreetHome());
        OXTestToolkit.assertEqualsAndNotNull("street other is not equals", contactObj1.getStreetOther(), contactObj2.getStreetOther());
        OXTestToolkit.assertEqualsAndNotNull("suffix is not equals", contactObj1.getSuffix(), contactObj2.getSuffix());
        OXTestToolkit.assertEqualsAndNotNull("tax id is not equals", contactObj1.getTaxID(), contactObj2.getTaxID());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone assistant is not equals",
            contactObj1.getTelephoneAssistant(),
            contactObj2.getTelephoneAssistant());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone business1 is not equals",
            contactObj1.getTelephoneBusiness1(),
            contactObj2.getTelephoneBusiness1());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone business2 is not equals",
            contactObj1.getTelephoneBusiness2(),
            contactObj2.getTelephoneBusiness2());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone callback is not equals",
            contactObj1.getTelephoneCallback(),
            contactObj2.getTelephoneCallback());
        OXTestToolkit.assertEqualsAndNotNull("telephone car is not equals", contactObj1.getTelephoneCar(), contactObj2.getTelephoneCar());
        OXTestToolkit.assertEqualsAndNotNull(
            "telehpone company is not equals",
            contactObj1.getTelephoneCompany(),
            contactObj2.getTelephoneCompany());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone home1 is not equals",
            contactObj1.getTelephoneHome1(),
            contactObj2.getTelephoneHome1());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone home2 is not equals",
            contactObj1.getTelephoneHome2(),
            contactObj2.getTelephoneHome2());
        OXTestToolkit.assertEqualsAndNotNull("telehpone ip is not equals", contactObj1.getTelephoneIP(), contactObj2.getTelephoneIP());
        OXTestToolkit.assertEqualsAndNotNull("telehpone isdn is not equals", contactObj1.getTelephoneISDN(), contactObj2.getTelephoneISDN());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone other is not equals",
            contactObj1.getTelephoneOther(),
            contactObj2.getTelephoneOther());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone pager is not equals",
            contactObj1.getTelephonePager(),
            contactObj2.getTelephonePager());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone primary is not equals",
            contactObj1.getTelephonePrimary(),
            contactObj2.getTelephonePrimary());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone radio is not equals",
            contactObj1.getTelephoneRadio(),
            contactObj2.getTelephoneRadio());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone telex is not equals",
            contactObj1.getTelephoneTelex(),
            contactObj2.getTelephoneTelex());
        OXTestToolkit.assertEqualsAndNotNull(
            "telephone ttytdd is not equals",
            contactObj1.getTelephoneTTYTTD(),
            contactObj2.getTelephoneTTYTTD());
        OXTestToolkit.assertEqualsAndNotNull("title is not equals", contactObj1.getTitle(), contactObj2.getTitle());
        OXTestToolkit.assertEqualsAndNotNull("url is not equals", contactObj1.getURL(), contactObj2.getURL());
        OXTestToolkit.assertEqualsAndNotNull("userfield01 is not equals", contactObj1.getUserField01(), contactObj2.getUserField01());
        OXTestToolkit.assertEqualsAndNotNull("userfield02 is not equals", contactObj1.getUserField02(), contactObj2.getUserField02());
        OXTestToolkit.assertEqualsAndNotNull("userfield03 is not equals", contactObj1.getUserField03(), contactObj2.getUserField03());
        OXTestToolkit.assertEqualsAndNotNull("userfield04 is not equals", contactObj1.getUserField04(), contactObj2.getUserField04());
        OXTestToolkit.assertEqualsAndNotNull("userfield05 is not equals", contactObj1.getUserField05(), contactObj2.getUserField05());
        OXTestToolkit.assertEqualsAndNotNull("userfield06 is not equals", contactObj1.getUserField06(), contactObj2.getUserField06());
        OXTestToolkit.assertEqualsAndNotNull("userfield07 is not equals", contactObj1.getUserField07(), contactObj2.getUserField07());
        OXTestToolkit.assertEqualsAndNotNull("userfield08 is not equals", contactObj1.getUserField08(), contactObj2.getUserField08());
        OXTestToolkit.assertEqualsAndNotNull("userfield09 is not equals", contactObj1.getUserField09(), contactObj2.getUserField09());
        OXTestToolkit.assertEqualsAndNotNull("userfield10 is not equals", contactObj1.getUserField10(), contactObj2.getUserField10());
        OXTestToolkit.assertEqualsAndNotNull("userfield11 is not equals", contactObj1.getUserField11(), contactObj2.getUserField11());
        OXTestToolkit.assertEqualsAndNotNull("userfield12 is not equals", contactObj1.getUserField12(), contactObj2.getUserField12());
        OXTestToolkit.assertEqualsAndNotNull("userfield13 is not equals", contactObj1.getUserField13(), contactObj2.getUserField13());
        OXTestToolkit.assertEqualsAndNotNull("userfield14 is not equals", contactObj1.getUserField14(), contactObj2.getUserField14());
        OXTestToolkit.assertEqualsAndNotNull("userfield15 is not equals", contactObj1.getUserField15(), contactObj2.getUserField15());
        OXTestToolkit.assertEqualsAndNotNull("userfield16 is not equals", contactObj1.getUserField16(), contactObj2.getUserField16());
        OXTestToolkit.assertEqualsAndNotNull("userfield17 is not equals", contactObj1.getUserField17(), contactObj2.getUserField17());
        OXTestToolkit.assertEqualsAndNotNull("userfield18 is not equals", contactObj1.getUserField18(), contactObj2.getUserField18());
        OXTestToolkit.assertEqualsAndNotNull("userfield19 is not equals", contactObj1.getUserField19(), contactObj2.getUserField19());
        OXTestToolkit.assertEqualsAndNotNull("userfield20 is not equals", contactObj1.getUserField20(), contactObj2.getUserField20());
        OXTestToolkit.assertEqualsAndNotNull(
            "number of attachments is not equals",
            contactObj1.getNumberOfAttachments(),
            contactObj2.getNumberOfAttachments());
        OXTestToolkit.assertEqualsAndNotNull(
            "default address is not equals",
            contactObj1.getDefaultAddress(),
            contactObj2.getDefaultAddress());

        OXTestToolkit.assertEqualsAndNotNull(
            "distribution list is not equals",
            distributionlist2String(contactObj1.getDistributionList()),
            distributionlist2String(contactObj2.getDistributionList()));
    }

    protected Contact createContactObject(final String displayname) {
        final Contact contactObj = new Contact();
        contactObj.setSurName("Meier");
        contactObj.setGivenName("Herbert");
        // contactObj.setDisplayName(displayname);
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

    protected Contact createCompleteContactObject() throws Exception {
        final Contact contactObj = new Contact();
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
        contactObj.setImage1(Data.image);
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

        final Contact link1 = createContactObject("link1");
        final int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
        link1.setObjectID(linkId1);

        final DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
        entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
        entry[1] = new DistributionListEntryObject(link1.getDisplayName(), link1.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
        entry[1].setEntryID(link1.getObjectID());

        contactObj.setDistributionList(entry);

        return contactObj;
    }

    public static int insertContact(final WebConversation webCon, final Contact contactObj, String host, final String session) throws Exception {
        host = appendPrefix(host);

        int objectId = 0;

        final StringWriter stringWriter = new StringWriter();
        final JSONObject jsonObj = new JSONObject();
        final ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
        contactWriter.writeContact(contactObj, jsonObj, null);

        stringWriter.write(jsonObj.toString());
        stringWriter.flush();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

        WebRequest req = null;
        WebResponse resp = null;

        JSONObject jResponse = null;

        if (contactObj.containsImage1()) {
            final PostMethodWebRequest postReq = new PostMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), true);

            postReq.setParameter("json", stringWriter.toString());

            final File f = File.createTempFile("open-xchange_image", ".jpg");
            final FileOutputStream fos = new FileOutputStream(f);
            fos.write(contactObj.getImage1());
            fos.flush();
            fos.close();

            postReq.selectFile("file", f, Data.CONTENT_TYPE);

            req = postReq;
            resp = webCon.getResource(req);
            f.delete();
            jResponse = extractFromCallback(resp.getText());
        } else {
            final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes(
                com.openexchange.java.Charsets.UTF_8));

            req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
            resp = webCon.getResponse(req);

            jResponse = new JSONObject(resp.getText());
        }

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(jResponse.toString());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        final JSONObject data = (JSONObject) response.getData();
        if (data.has(DataFields.ID)) {
            objectId = data.getInt(DataFields.ID);
        }

        return objectId;
    }

    public static void updateContact(final WebConversation webCon, final Contact contactObj, final int objectId, final int inFolder, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final StringWriter stringWriter = new StringWriter();
        final JSONObject jsonObj = new JSONObject();
        final ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
        contactWriter.writeContact(contactObj, jsonObj, null);

        stringWriter.write(jsonObj.toString());
        stringWriter.flush();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        parameter.setParameter(DataFields.ID, Integer.toString(objectId));
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, Integer.toString(inFolder));
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date(System.currentTimeMillis() + 1000000));

        WebRequest req = null;
        WebResponse resp = null;

        JSONObject jResponse = null;

        if (contactObj.containsImage1()) {
            final PostMethodWebRequest postReq = new PostMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), true);
            postReq.setParameter("json", stringWriter.toString());

            final File f = File.createTempFile("open-xchange_image", ".jpg");
            final FileOutputStream fos = new FileOutputStream(f);
            fos.write(contactObj.getImage1());
            fos.flush();
            fos.close();

            postReq.selectFile("file", f, Data.CONTENT_TYPE);

            req = postReq;
            resp = webCon.getResource(req);
            f.delete();
            jResponse = extractFromCallback(resp.getText());
        } else {
            final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes(
                com.openexchange.java.Charsets.UTF_8));

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

    public static void deleteContact(final WebConversation webCon, final int id, final int inFolder, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date(System.currentTimeMillis() + 1000000));

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(DataFields.ID, id);
        jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }
    }

    public static Contact[] listContact(final WebConversation webCon, final int inFolder, final int[] cols, final String host, final String session) throws Exception {
        return listContact(webCon, inFolder, cols, -1, -1, host, session);
    }

    public static Contact[] listContact(final WebConversation webCon, final int inFolder, final int[] cols, final int leftHandLimit, final int rightHandLimit, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        if (leftHandLimit > -1) {
            parameter.setParameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit);
        }

        if (rightHandLimit > -1) {
            parameter.setParameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit);
        }

        final WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    public static Contact[] searchContact(final WebConversation webCon, final String searchpattern, final int inFolder, final int[] cols, final String host, final String session) throws OXException, Exception {
        return searchContact(webCon, searchpattern, inFolder, cols, false, host, session);
    }

    public static Contact[] searchContact(final WebConversation webCon, final String searchpattern, final int inFolder, final int[] cols, final boolean startletter, String host, final String session) throws OXException, Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("pattern", searchpattern);
        jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        jsonObj.put("startletter", startletter);

        final WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), new ByteArrayInputStream(
            jsonObj.toString().getBytes()), "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException(response.getErrorMessage());
        }

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    public static Contact[] searchContactAdvanced(final WebConversation webCon, final ContactSearchObject cso, final int folder, final int[] cols, final String host, final String session) throws OXException, Exception {
        return searchContactAdvanced(webCon, cso, folder, 0, cols, host, session);
    }

    public static Contact[] searchContactAdvanced(final WebConversation webCon, final ContactSearchObject cso, final int folder, final int orderBy, final int[] cols, String host, final String session) throws OXException, Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        if (orderBy != 0) {
            parameter.setParameter(AJAXServlet.PARAMETER_SORT, orderBy);
            parameter.setParameter(AJAXServlet.PARAMETER_ORDER, "ASC");
        }

        final JSONObject jsonObj = new JSONObject();
        // jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, folder);
        jsonObj.put(ContactFields.LAST_NAME, cso.getSurname());
        jsonObj.put(ContactFields.FIRST_NAME, cso.getGivenName());
        jsonObj.put(ContactFields.DISPLAY_NAME, cso.getDisplayName());
        jsonObj.put(ContactFields.EMAIL1, cso.getEmail1());
        jsonObj.put(ContactFields.EMAIL2, cso.getEmail2());
        jsonObj.put(ContactFields.EMAIL3, cso.getEmail3());

        if (cso.isEmailAutoComplete()) {
            jsonObj.put("emailAutoComplete", "true");
            // parameter.setParameter("emailAutoComplete","true");
        }

        final WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), new ByteArrayInputStream(
            jsonObj.toString().getBytes()), "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            throw new TestException(response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    public static Contact[] listContact(final WebConversation webCon, final int[][] objectIdAndFolderId, final int[] cols, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final JSONArray jsonArray = new JSONArray();

        for (int a = 0; a < objectIdAndFolderId.length; a++) {
            final int i[] = objectIdAndFolderId[a];
            final JSONObject jsonObj = new JSONObject();
            jsonObj.put(DataFields.ID, i[0]);
            jsonObj.put(AJAXServlet.PARAMETER_INFOLDER, i[1]);
            jsonArray.put(jsonObj);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);
        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    public static Contact loadUser(final WebConversation webCon, final int userId, final String host, final String session) throws OXException, IOException, SAXException, JSONException {
        return UserTools.getUserContact(webCon, host, session, userId);
    }

    public static Contact loadContact(final WebConversation webCon, final int objectId, final int inFolder, final String protocol, final String host, final String session) throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(webCon, host, session), false);
        if (protocol.endsWith("://")) {
            client.setProtocol(protocol.substring(0, protocol.length() - 3));
        } else {
            client.setProtocol(protocol);
        }
        client.setHostname(host);
        final TimeZone timeZone = client.getValues().getTimeZone();
        final GetRequest request = new GetRequest(inFolder, objectId, timeZone);
        final GetResponse response = client.execute(request);
        return response.getContact();
    }

    public static Contact loadUser(final WebConversation webCon, final int userId, final int inFolder, final String host, final String session) throws Exception {
        final int[] cols = {
            DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
            FolderChildObject.FOLDER_ID, CommonObject.CATEGORIES, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.EMAIL1, Contact.EMAIL2,
            Contact.EMAIL3, Contact.INTERNAL_USERID };

        final Contact[] contactArray = listContact(webCon, inFolder, cols, host, session);

        for (int a = 0; a < contactArray.length; a++) {
            if (contactArray[a].getInternalUserId() == userId) {
                return contactArray[a];
            }
        }

        return null;
    }

    public static byte[] loadImage(final WebConversation webCon, final int objectId, final int inFolder, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_IMAGE);
        parameter.setParameter(DataFields.ID, objectId);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);

        final WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final InputStream is = resp.getInputStream();
        assertNotNull("response InputStream is null", is);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] b = new byte[1024];
        int i = 0;
        while ((i = is.read(b)) != -1) {
            baos.write(b, 0, i);
        }

        return baos.toByteArray();
    }

    public static Contact[] listModifiedAppointment(final WebConversation webCon, final int inFolder, final Date modified, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final int[] cols = new int[] { Appointment.OBJECT_ID };

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "deleted");
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    public static Contact[] listDeleteAppointment(final WebConversation webCon, final int inFolder, final Date modified, String host, final String session) throws Exception {
        host = appendPrefix(host);

        final int[] cols = new int[] { Appointment.OBJECT_ID };

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER, inFolder);
        parameter.setParameter(AJAXServlet.PARAMETER_IGNORE, "updated");
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, modified);
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, URLParameter.colsArray2String(cols));

        final WebRequest req = new GetMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters());
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = Response.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        assertEquals(200, resp.getResponseCode());

        return jsonArray2ContactArray((JSONArray) response.getData(), cols);
    }

    private static Contact[] jsonArray2AppointmentArray(final JSONArray jsonArray) throws Exception {
        final Contact[] contactArray = new Contact[jsonArray.length()];

        final ContactParser contactParser = new ContactParser();

        for (int a = 0; a < contactArray.length; a++) {
            contactArray[a] = new Contact();
            final JSONObject jObj = jsonArray.getJSONObject(a);

            contactParser.parse(contactArray[a], jObj);
        }

        return contactArray;
    }

    protected static Contact[] jsonArray2ContactArray(final JSONArray jsonArray, final int[] cols) throws Exception {
        final Contact[] contactArray = new Contact[jsonArray.length()];

        for (int a = 0; a < contactArray.length; a++) {
            contactArray[a] = new Contact();
            parseCols(cols, jsonArray.getJSONArray(a), contactArray[a]);
        }

        return contactArray;
    }

    private static void parseCols(final int[] cols, final JSONArray jsonArray, final Contact contactObj) throws Exception {
        assertEquals("compare array size with cols size", cols.length, jsonArray.length());

        for (int a = 0; a < cols.length; a++) {
            parse(a, cols[a], jsonArray, contactObj);
        }
    }

    private static void parse(final int pos, final int field, final JSONArray jsonArray, final Contact contactObj) throws Exception {
        switch (field) {
        case Contact.OBJECT_ID:
            if (!jsonArray.isNull(pos)) {
                contactObj.setObjectID(jsonArray.getInt(pos));
            }
            break;
        case Contact.CREATED_BY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCreatedBy(jsonArray.getInt(pos));
            }
            break;
        case Contact.CREATION_DATE:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCreationDate(new Date(jsonArray.getLong(pos)));
            }
            break;
        case Contact.MODIFIED_BY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setModifiedBy(jsonArray.getInt(pos));
            }
            break;
        case Contact.LAST_MODIFIED:
            if (!jsonArray.isNull(pos)) {
                contactObj.setLastModified(new Date(jsonArray.getLong(pos)));
            }
            break;
        case Contact.FOLDER_ID:
            if (!jsonArray.isNull(pos)) {
                contactObj.setParentFolderID(jsonArray.getInt(pos));
            }
            break;
        case Contact.PRIVATE_FLAG:
            if (!jsonArray.isNull(pos)) {
                contactObj.setPrivateFlag(jsonArray.getBoolean(pos));
            }
            break;
        case Contact.SUR_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setSurName(jsonArray.getString(pos));
            }
            break;
        case Contact.GIVEN_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setGivenName(jsonArray.getString(pos));
            }
            break;
        case Contact.ANNIVERSARY:
            if (!jsonArray.isNull(pos)) {
                final String lAnniversary = jsonArray.getString(pos);
                if (lAnniversary != null && !lAnniversary.equals("null")) {
                    contactObj.setAnniversary(new Date(Long.parseLong(lAnniversary)));
                } else {
                    contactObj.setAnniversary(null);
                }
            }
            break;
        case Contact.ASSISTANT_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setAssistantName(jsonArray.getString(pos));
            }
            break;
        case Contact.BIRTHDAY:
            if (!jsonArray.isNull(pos)) {
                final String lBirthday = jsonArray.getString(pos);
                if (lBirthday != null && !lBirthday.equals("null")) {
                    contactObj.setBirthday(new Date(Long.parseLong(lBirthday)));
                } else {
                    contactObj.setBirthday(null);
                }
            }
            break;
        case Contact.BRANCHES:
            if (!jsonArray.isNull(pos)) {
                contactObj.setBranches(jsonArray.getString(pos));
            }
            break;
        case Contact.BUSINESS_CATEGORY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setBusinessCategory(jsonArray.getString(pos));
            }
            break;
        case Contact.CATEGORIES:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCategories(jsonArray.getString(pos));
            }
            break;
        case Contact.CELLULAR_TELEPHONE1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCellularTelephone1(jsonArray.getString(pos));
            }
            break;
        case Contact.CELLULAR_TELEPHONE2:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCellularTelephone2(jsonArray.getString(pos));
            }
            break;
        case Contact.CITY_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCityHome(jsonArray.getString(pos));
            }
            break;
        case Contact.CITY_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCityBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.CITY_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCityOther(jsonArray.getString(pos));
            }
            break;
        case Contact.COMMERCIAL_REGISTER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCommercialRegister(jsonArray.getString(pos));
            }
            break;
        case Contact.COMPANY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCompany(jsonArray.getString(pos));
            }
            break;
        case Contact.COUNTRY_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCountryHome(jsonArray.getString(pos));
            }
            break;
        case Contact.COUNTRY_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCountryBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.COUNTRY_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setCountryOther(jsonArray.getString(pos));
            }
            break;
        case Contact.DEPARTMENT:
            if (!jsonArray.isNull(pos)) {
                contactObj.setDepartment(jsonArray.getString(pos));
            }
            break;
        case Contact.DEFAULT_ADDRESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setDefaultAddress(jsonArray.getInt(pos));
            }
            break;
        case Contact.DISPLAY_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setDisplayName(jsonArray.getString(pos));
            }
            break;
        case Contact.EMAIL1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setEmail1(jsonArray.getString(pos));
            }
            break;
        case Contact.EMAIL2:
            if (!jsonArray.isNull(pos)) {
                contactObj.setEmail2(jsonArray.getString(pos));
            }
            break;
        case Contact.EMAIL3:
            if (!jsonArray.isNull(pos)) {
                contactObj.setEmail3(jsonArray.getString(pos));
            }
            break;
        case Contact.EMPLOYEE_TYPE:
            if (!jsonArray.isNull(pos)) {
                contactObj.setEmployeeType(jsonArray.getString(pos));
            }
            break;
        case Contact.FAX_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setFaxBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.FAX_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setFaxHome(jsonArray.getString(pos));
            }
            break;
        case Contact.FAX_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setFaxOther(jsonArray.getString(pos));
            }
            break;
        case Contact.IMAGE1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setImage1(jsonArray.getString(pos).getBytes());
            }
            break;
        /*
         * NO LONGER PRESENT case ContactObject.NUMBER_OF_IMAGES: contactObj.setNumberOfImages(jsonArray.getInt(pos)); break;
         */
        case Contact.INFO:
            if (!jsonArray.isNull(pos)) {
                contactObj.setInfo(jsonArray.getString(pos));
            }
            break;
        case Contact.INSTANT_MESSENGER1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setInstantMessenger1(jsonArray.getString(pos));
            }
            break;
        case Contact.INSTANT_MESSENGER2:
            if (!jsonArray.isNull(pos)) {
                contactObj.setInstantMessenger2(jsonArray.getString(pos));
            }
            break;
        case Contact.INTERNAL_USERID:
            if (!jsonArray.isNull(pos)) {
                contactObj.setInternalUserId(jsonArray.getInt(pos));
            }
            break;
        case Contact.COLOR_LABEL:
            if (!jsonArray.isNull(pos)) {
                contactObj.setLabel(jsonArray.getInt(pos));
            }
            break;
        case Contact.MANAGER_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setManagerName(jsonArray.getString(pos));
            }
            break;
        case Contact.MARITAL_STATUS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setMaritalStatus(jsonArray.getString(pos));
            }
            break;
        case Contact.MIDDLE_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setMiddleName(jsonArray.getString(pos));
            }
            break;
        case Contact.NICKNAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setNickname(jsonArray.getString(pos));
            }
            break;
        case Contact.NOTE:
            if (!jsonArray.isNull(pos)) {
                contactObj.setNote(jsonArray.getString(pos));
            }
            break;
        case Contact.NUMBER_OF_CHILDREN:
            if (!jsonArray.isNull(pos)) {
                contactObj.setNumberOfChildren(jsonArray.getString(pos));
            }
            break;
        case Contact.NUMBER_OF_EMPLOYEE:
            if (!jsonArray.isNull(pos)) {
                contactObj.setNumberOfEmployee(jsonArray.getString(pos));
            }
            break;
        case Contact.POSITION:
            if (!jsonArray.isNull(pos)) {
                contactObj.setPosition(jsonArray.getString(pos));
            }
            break;
        case Contact.POSTAL_CODE_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setPostalCodeHome(jsonArray.getString(pos));
            }
            break;
        case Contact.POSTAL_CODE_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setPostalCodeBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.POSTAL_CODE_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setPostalCodeOther(jsonArray.getString(pos));
            }
            break;
        case Contact.PROFESSION:
            if (!jsonArray.isNull(pos)) {
                contactObj.setProfession(jsonArray.getString(pos));
            }
            break;
        case Contact.ROOM_NUMBER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setRoomNumber(jsonArray.getString(pos));
            }
            break;
        case Contact.SALES_VOLUME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setSalesVolume(jsonArray.getString(pos));
            }
            break;
        case Contact.SPOUSE_NAME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setSpouseName(jsonArray.getString(pos));
            }
            break;
        case Contact.STATE_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStateHome(jsonArray.getString(pos));
            }
            break;
        case Contact.STATE_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStateBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.STATE_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStateOther(jsonArray.getString(pos));
            }
            break;
        case Contact.STREET_HOME:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStreetHome(jsonArray.getString(pos));
            }
            break;
        case Contact.STREET_BUSINESS:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStreetBusiness(jsonArray.getString(pos));
            }
            break;
        case Contact.STREET_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setStreetOther(jsonArray.getString(pos));
            }
            break;
        case Contact.SUFFIX:
            if (!jsonArray.isNull(pos)) {
                contactObj.setSuffix(jsonArray.getString(pos));
            }
            break;
        case Contact.TAX_ID:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTaxID(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_ASSISTANT:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneAssistant(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_BUSINESS1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneBusiness1(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_BUSINESS2:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneBusiness2(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_CALLBACK:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneCallback(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_CAR:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneCar(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_COMPANY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneCompany(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_HOME1:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneHome1(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_HOME2:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneHome2(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_IP:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneIP(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_ISDN:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneISDN(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_OTHER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneOther(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_PAGER:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephonePager(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_PRIMARY:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephonePrimary(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_RADIO:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneRadio(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_TELEX:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneTelex(jsonArray.getString(pos));
            }
            break;
        case Contact.TELEPHONE_TTYTDD:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTelephoneTTYTTD(jsonArray.getString(pos));
            }
            break;
        case Contact.TITLE:
            if (!jsonArray.isNull(pos)) {
                contactObj.setTitle(jsonArray.getString(pos));
            }
            break;
        case Contact.URL:
            if (!jsonArray.isNull(pos)) {
                contactObj.setURL(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD01:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField01(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD02:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField02(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD03:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField03(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD04:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField04(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD05:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField05(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD06:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField06(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD07:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField07(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD08:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField08(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD09:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField09(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD10:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField10(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD11:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField11(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD12:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField12(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD13:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField13(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD14:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField14(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD15:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField15(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD16:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField16(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD17:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField17(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD18:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField18(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD19:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField19(jsonArray.getString(pos));
            }
            break;
        case Contact.USERFIELD20:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUserField20(jsonArray.getString(pos));
            }
            break;
        case Contact.DISTRIBUTIONLIST:
            if (!jsonArray.isNull(pos)) {
                contactObj.setDistributionList(parseDistributionList(contactObj, jsonArray.getJSONArray(pos)));
            }
            break;
        case Contact.USE_COUNT:
            if (!jsonArray.isNull(pos)) {
                contactObj.setUseCount(jsonArray.getInt(pos));
            }
            break;
        default:
            throw new Exception("missing field in mapping: " + field);

        }
    }

    private static DistributionListEntryObject[] parseDistributionList(final Contact contactObj, final JSONArray jsonArray) throws Exception {
        final DistributionListEntryObject[] distributionlist = new DistributionListEntryObject[jsonArray.length()];
        for (int a = 0; a < jsonArray.length(); a++) {
            final JSONObject entry = jsonArray.getJSONObject(a);
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

    private HashSet distributionlist2String(final DistributionListEntryObject[] distributionListEntry) throws Exception {
        if (distributionListEntry == null) {
            return null;
        }

        final HashSet hs = new HashSet();

        for (int a = 0; a < distributionListEntry.length; a++) {
            hs.add(entry2String(distributionListEntry[a]));
        }

        return hs;
    }

    private String entry2String(final DistributionListEntryObject entry) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("ID" + entry.getEntryID());
        sb.append("D" + entry.getDisplayname());
        sb.append("F" + entry.getEmailfield());
        sb.append("E" + entry.getEmailaddress());

        return sb.toString();
    }
}
