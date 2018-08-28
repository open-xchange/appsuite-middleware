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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactListElement;
import com.openexchange.testing.httpclient.models.ContactResponse;
import com.openexchange.testing.httpclient.models.ContactUpdateResponse;
import com.openexchange.testing.httpclient.models.DistributionListMember;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 *
 * {@link AbstractApiClientContactTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AbstractApiClientContactTest extends AbstractConfigAwareAPIClientSession {

    public static final String CONTENT_TYPE = "image/png";

    public static final byte[] image = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0, 37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1, -1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26, -40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126, -4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0, 1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };

    protected String contactFolderId = null;
    protected ContactsApi contactsApi = null;

    protected long dateTime = 0;

    protected int userId = 0;

    protected TimeZone tz = null;

    private Long lastTimestamp;

    private final List<String> createdContacts = new ArrayList<>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contactsApi = new ContactsApi(apiClient);
        contactFolderId = getDefaultFolder(apiClient.getSession(), new FoldersApi(apiClient));
        userId = apiClient.getUserId();

        UserResponse resp = new UserApi(apiClient).getUser(apiClient.getSession(), String.valueOf(userId));
        assertNull(resp.getErrorDesc(), resp.getError());
        assertNotNull(resp.getData());
        tz = TimeZone.getTimeZone(resp.getData().getTimezone());

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        dateTime = c.getTimeInMillis();
    }

    protected String createContactWithDistributionList(final String title, final ContactData contactEntry) throws Exception {
        ContactData contact = new ContactData();
        contact.setLastName(title);
        contact.setFolderId(contactFolderId);
        contact.setDisplayName(UUID.randomUUID().toString());

        List<DistributionListMember> distributionList = new ArrayList<>(3);
        distributionList.add(getMember("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT));
        distributionList.add(getMember("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT));
        DistributionListMember member = getMember(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
        member.setId(contactEntry.getId());
        distributionList.add(member);
        contact.setDistributionList(distributionList);

        return createContact(contact);
    }

    private DistributionListMember getMember(String displayName, String mail, int mailField) {
        DistributionListMember result = new DistributionListMember();
        result.setDisplayName(displayName);
        result.setMail(mail);
        result.setMailField(new BigDecimal(mailField));
        return result;
    }

    /**
     * Retrieves the default contact folder of the user with the specified session
     *
     * @param session The session of the user
     * @param client The {@link ApiClient}
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    protected String getDefaultFolder(String session, ApiClient client) throws Exception {
        return getDefaultFolder(session, new FoldersApi(client));
    }

    /**
     * Retrieves the default contact folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default contact folder of the user
     * @throws Exception if the default contact folder cannot be found
     */
    @SuppressWarnings("unchecked")
    private String getDefaultFolder(String session, FoldersApi foldersApi) throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, "contacts", "1,308", "0");
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        } else {
            for (ArrayList<?> folder : privateList) {
                if ((Boolean) folder.get(1)) {
                    return (String) folder.get(0);
                }
            }
        }
        throw new Exception("Unable to find default contact folder!");
    }

    protected void compareObject(final ContactData contactObj1, final ContactData contactObj2) throws Exception {
        compareObject(contactObj1, contactObj2, true);
    }

    protected void compareObject(final ContactData contactObj1, final ContactData contactObj2, boolean compareUID) throws Exception {
        assertEquals("id is not equals", contactObj1.getId(), contactObj2.getId());
        assertEquals("folder id is not equals", contactObj1.getFolderId(), contactObj2.getFolderId());
        assertEquals("private flag is not equals", contactObj1.getPrivateFlag(), contactObj2.getPrivateFlag());
        OXTestToolkit.assertEqualsAndNotNull("categories is not equals", contactObj1.getCategories(), contactObj2.getCategories());
        OXTestToolkit.assertEqualsAndNotNull("given name is not equals", contactObj1.getFirstName(), contactObj2.getFirstName());
        OXTestToolkit.assertEqualsAndNotNull("surname is not equals", contactObj1.getLastName(), contactObj2.getLastName());
        OXTestToolkit.assertEqualsAndNotNull("anniversary is not equals", contactObj1.getAnniversary(), contactObj2.getAnniversary());
        OXTestToolkit.assertEqualsAndNotNull("assistant name is not equals", contactObj1.getAssistantName(), contactObj2.getAssistantName());
        OXTestToolkit.assertEqualsAndNotNull("birthday is not equals", contactObj1.getBirthday(), contactObj2.getBirthday());
        OXTestToolkit.assertEqualsAndNotNull("branches is not equals", contactObj1.getBranches(), contactObj2.getBranches());
        OXTestToolkit.assertEqualsAndNotNull("business categorie is not equals", contactObj1.getBusinessCategory(), contactObj2.getBusinessCategory());
        OXTestToolkit.assertEqualsAndNotNull("cellular telephone1 is not equals", contactObj1.getCellularTelephone1(), contactObj2.getCellularTelephone1());
        OXTestToolkit.assertEqualsAndNotNull("cellular telephone2 is not equals", contactObj1.getCellularTelephone2(), contactObj2.getCellularTelephone2());
        OXTestToolkit.assertEqualsAndNotNull("city business is not equals", contactObj1.getCityBusiness(), contactObj2.getCityBusiness());
        OXTestToolkit.assertEqualsAndNotNull("city home is not equals", contactObj1.getCityHome(), contactObj2.getCityHome());
        OXTestToolkit.assertEqualsAndNotNull("city other is not equals", contactObj1.getCityOther(), contactObj2.getCityOther());
        OXTestToolkit.assertEqualsAndNotNull("commercial register is not equals", contactObj1.getCommercialRegister(), contactObj2.getCommercialRegister());
        OXTestToolkit.assertEqualsAndNotNull("company is not equals", contactObj1.getCompany(), contactObj2.getCompany());
        OXTestToolkit.assertEqualsAndNotNull("country business is not equals", contactObj1.getCountryBusiness(), contactObj2.getCountryBusiness());
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
        OXTestToolkit.assertEqualsAndNotNull("instant messenger1 is not equals", contactObj1.getInstantMessenger1(), contactObj2.getInstantMessenger1());
        OXTestToolkit.assertEqualsAndNotNull("instant messenger2 is not equals", contactObj1.getInstantMessenger2(), contactObj2.getInstantMessenger2());
        OXTestToolkit.assertEqualsAndNotNull("instant messenger2 is not equals", contactObj1.getInstantMessenger2(), contactObj2.getInstantMessenger2());
        OXTestToolkit.assertEqualsAndNotNull("marital status is not equals", contactObj1.getMaritalStatus(), contactObj2.getMaritalStatus());
        OXTestToolkit.assertEqualsAndNotNull("cotm name is not equals", contactObj1.getManagerName(), contactObj2.getManagerName());
        OXTestToolkit.assertEqualsAndNotNull("middle name is not equals", contactObj1.getSecondName(), contactObj2.getSecondName());
        OXTestToolkit.assertEqualsAndNotNull("nickname is not equals", contactObj1.getNickname(), contactObj2.getNickname());
        OXTestToolkit.assertEqualsAndNotNull("note is not equals", contactObj1.getNote(), contactObj2.getNote());
        OXTestToolkit.assertEqualsAndNotNull("number of children is not equals", contactObj1.getNumberOfChildren(), contactObj2.getNumberOfChildren());
        OXTestToolkit.assertEqualsAndNotNull("number of employee is not equals", contactObj1.getNumberOfEmployees(), contactObj2.getNumberOfEmployees());
        OXTestToolkit.assertEqualsAndNotNull("position is not equals", contactObj1.getPosition(), contactObj2.getPosition());
        OXTestToolkit.assertEqualsAndNotNull("postal code business is not equals", contactObj1.getPostalCodeBusiness(), contactObj2.getPostalCodeBusiness());
        OXTestToolkit.assertEqualsAndNotNull("postal code home is not equals", contactObj1.getPostalCodeHome(), contactObj2.getPostalCodeHome());
        OXTestToolkit.assertEqualsAndNotNull("postal code other is not equals", contactObj1.getPostalCodeOther(), contactObj2.getPostalCodeOther());
        OXTestToolkit.assertEqualsAndNotNull("profession is not equals", contactObj1.getProfession(), contactObj2.getProfession());
        OXTestToolkit.assertEqualsAndNotNull("room number is not equals", contactObj1.getRoomNumber(), contactObj2.getRoomNumber());
        OXTestToolkit.assertEqualsAndNotNull("sales volume is not equals", contactObj1.getSalesVolume(), contactObj2.getSalesVolume());
        OXTestToolkit.assertEqualsAndNotNull("spouse name is not equals", contactObj1.getSpouseName(), contactObj2.getSpouseName());
        OXTestToolkit.assertEqualsAndNotNull("state business is not equals", contactObj1.getStateBusiness(), contactObj2.getStateBusiness());
        OXTestToolkit.assertEqualsAndNotNull("state home is not equals", contactObj1.getStateHome(), contactObj2.getStateHome());
        OXTestToolkit.assertEqualsAndNotNull("state other is not equals", contactObj1.getStateOther(), contactObj2.getStateOther());
        OXTestToolkit.assertEqualsAndNotNull("street business is not equals", contactObj1.getStreetBusiness(), contactObj2.getStreetBusiness());
        OXTestToolkit.assertEqualsAndNotNull("street home is not equals", contactObj1.getStreetHome(), contactObj2.getStreetHome());
        OXTestToolkit.assertEqualsAndNotNull("street other is not equals", contactObj1.getStreetOther(), contactObj2.getStreetOther());
        OXTestToolkit.assertEqualsAndNotNull("suffix is not equals", contactObj1.getSuffix(), contactObj2.getSuffix());
        OXTestToolkit.assertEqualsAndNotNull("tax id is not equals", contactObj1.getTaxId(), contactObj2.getTaxId());
        OXTestToolkit.assertEqualsAndNotNull("telephone assistant is not equals", contactObj1.getTelephoneAssistant(), contactObj2.getTelephoneAssistant());
        OXTestToolkit.assertEqualsAndNotNull("telephone business1 is not equals", contactObj1.getTelephoneBusiness1(), contactObj2.getTelephoneBusiness1());
        OXTestToolkit.assertEqualsAndNotNull("telephone business2 is not equals", contactObj1.getTelephoneBusiness2(), contactObj2.getTelephoneBusiness2());
        OXTestToolkit.assertEqualsAndNotNull("telephone callback is not equals", contactObj1.getTelephoneCallback(), contactObj2.getTelephoneCallback());
        OXTestToolkit.assertEqualsAndNotNull("telephone car is not equals", contactObj1.getTelephoneCar(), contactObj2.getTelephoneCar());
        OXTestToolkit.assertEqualsAndNotNull("telehpone company is not equals", contactObj1.getTelephoneCompany(), contactObj2.getTelephoneCompany());
        OXTestToolkit.assertEqualsAndNotNull("telephone home1 is not equals", contactObj1.getTelephoneHome1(), contactObj2.getTelephoneHome1());
        OXTestToolkit.assertEqualsAndNotNull("telephone home2 is not equals", contactObj1.getTelephoneHome2(), contactObj2.getTelephoneHome2());
        OXTestToolkit.assertEqualsAndNotNull("telehpone ip is not equals", contactObj1.getTelephoneIp(), contactObj2.getTelephoneIp());
        OXTestToolkit.assertEqualsAndNotNull("telehpone isdn is not equals", contactObj1.getTelephoneIsdn(), contactObj2.getTelephoneIsdn());
        OXTestToolkit.assertEqualsAndNotNull("telephone other is not equals", contactObj1.getTelephoneOther(), contactObj2.getTelephoneOther());
        OXTestToolkit.assertEqualsAndNotNull("telephone pager is not equals", contactObj1.getTelephonePager(), contactObj2.getTelephonePager());
        OXTestToolkit.assertEqualsAndNotNull("telephone primary is not equals", contactObj1.getTelephonePrimary(), contactObj2.getTelephonePrimary());
        OXTestToolkit.assertEqualsAndNotNull("telephone radio is not equals", contactObj1.getTelephoneRadio(), contactObj2.getTelephoneRadio());
        OXTestToolkit.assertEqualsAndNotNull("telephone telex is not equals", contactObj1.getTelephoneTelex(), contactObj2.getTelephoneTelex());
        OXTestToolkit.assertEqualsAndNotNull("telephone ttytdd is not equals", contactObj1.getTelephoneTtytdd(), contactObj2.getTelephoneTtytdd());
        OXTestToolkit.assertEqualsAndNotNull("title is not equals", contactObj1.getTitle(), contactObj2.getTitle());
        OXTestToolkit.assertEqualsAndNotNull("url is not equals", contactObj1.getUrl(), contactObj2.getUrl());
        OXTestToolkit.assertEqualsAndNotNull("Userfield01 is not equals", contactObj1.getUserfield01(), contactObj2.getUserfield01());
        OXTestToolkit.assertEqualsAndNotNull("Userfield02 is not equals", contactObj1.getUserfield02(), contactObj2.getUserfield02());
        OXTestToolkit.assertEqualsAndNotNull("Userfield03 is not equals", contactObj1.getUserfield03(), contactObj2.getUserfield03());
        OXTestToolkit.assertEqualsAndNotNull("Userfield04 is not equals", contactObj1.getUserfield04(), contactObj2.getUserfield04());
        OXTestToolkit.assertEqualsAndNotNull("Userfield05 is not equals", contactObj1.getUserfield05(), contactObj2.getUserfield05());
        OXTestToolkit.assertEqualsAndNotNull("Userfield06 is not equals", contactObj1.getUserfield06(), contactObj2.getUserfield06());
        OXTestToolkit.assertEqualsAndNotNull("Userfield07 is not equals", contactObj1.getUserfield07(), contactObj2.getUserfield07());
        OXTestToolkit.assertEqualsAndNotNull("Userfield08 is not equals", contactObj1.getUserfield08(), contactObj2.getUserfield08());
        OXTestToolkit.assertEqualsAndNotNull("Userfield09 is not equals", contactObj1.getUserfield09(), contactObj2.getUserfield09());
        OXTestToolkit.assertEqualsAndNotNull("Userfield10 is not equals", contactObj1.getUserfield10(), contactObj2.getUserfield10());
        OXTestToolkit.assertEqualsAndNotNull("Userfield11 is not equals", contactObj1.getUserfield11(), contactObj2.getUserfield11());
        OXTestToolkit.assertEqualsAndNotNull("Userfield12 is not equals", contactObj1.getUserfield12(), contactObj2.getUserfield12());
        OXTestToolkit.assertEqualsAndNotNull("Userfield13 is not equals", contactObj1.getUserfield13(), contactObj2.getUserfield13());
        OXTestToolkit.assertEqualsAndNotNull("Userfield14 is not equals", contactObj1.getUserfield14(), contactObj2.getUserfield14());
        OXTestToolkit.assertEqualsAndNotNull("Userfield15 is not equals", contactObj1.getUserfield15(), contactObj2.getUserfield15());
        OXTestToolkit.assertEqualsAndNotNull("Userfield16 is not equals", contactObj1.getUserfield16(), contactObj2.getUserfield16());
        OXTestToolkit.assertEqualsAndNotNull("Userfield17 is not equals", contactObj1.getUserfield17(), contactObj2.getUserfield17());
        OXTestToolkit.assertEqualsAndNotNull("Userfield18 is not equals", contactObj1.getUserfield18(), contactObj2.getUserfield18());
        OXTestToolkit.assertEqualsAndNotNull("Userfield19 is not equals", contactObj1.getUserfield19(), contactObj2.getUserfield19());
        OXTestToolkit.assertEqualsAndNotNull("Userfield20 is not equals", contactObj1.getUserfield20(), contactObj2.getUserfield20());
        OXTestToolkit.assertEqualsAndNotNull("number of attachments is not equals", contactObj1.getNumberOfAttachments(), contactObj2.getNumberOfAttachments());
        OXTestToolkit.assertEqualsAndNotNull("default address is not equals", contactObj1.getDefaultAddress(), contactObj2.getDefaultAddress());
        if (compareUID) {
            OXTestToolkit.assertEqualsAndNotNull("uid is not equals", contactObj1.getUid(), contactObj2.getUid());
        }

        OXTestToolkit.assertEqualsAndNotNull("distribution list is not equals", distributionlist2String(contactObj1.getDistributionList()), distributionlist2String(contactObj2.getDistributionList()));
    }

    protected ContactData createContactObject(final String displayname) {
        final ContactData contactObj = new ContactData();
        contactObj.setLastName("Meier");
        contactObj.setFirstName("Herbert");
        contactObj.setDisplayName(displayname);
        contactObj.setStreetBusiness("Franz-Meier Weg 17");
        contactObj.setCityBusiness("Test Stadt");
        contactObj.setStateBusiness("NRW");
        contactObj.setCountryBusiness("Deutschland");
        contactObj.setTelephoneBusiness1("+49112233445566");
        contactObj.setCompany("Internal Test AG");
        contactObj.setEmail1("hebert.meier@open-xchange.com");
        contactObj.setFolderId(contactFolderId);
        contactObj.setMarkAsDistributionlist(false);
        contactObj.setDistributionList(null);
        return contactObj;
    }

    protected ContactData createCompleteContactObject() throws Exception {
        final ContactData contactObj = new ContactData();
        contactObj.setPrivateFlag(true);
        contactObj.setCategories("categories");
        contactObj.setFirstName("given name");
        contactObj.setLastName("surname");
        contactObj.setAnniversary(dateTime);
        contactObj.setAssistantName("assistant name");
        contactObj.setBirthday(dateTime);
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
        contactObj.setDisplayName("display name" + UUID.randomUUID().toString());
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
        contactObj.setImage1(image.toString());
        contactObj.setImage1ContentType("image/png");
        contactObj.setManagerName("cotm name");
        contactObj.setMaritalStatus("marital status");
        contactObj.setSecondName("middle name");
        contactObj.setNickname("nickname");
        contactObj.setNote("note");
        contactObj.setNumberOfChildren("number of children");
        contactObj.setNumberOfEmployees("number of employee");
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
        contactObj.setTaxId("tax id");
        contactObj.setTelephoneAssistant("telephone assistant");
        contactObj.setTelephoneBusiness1("telephone business1");
        contactObj.setTelephoneBusiness2("telephone business2");
        contactObj.setTelephoneCallback("telephone callback");
        contactObj.setTelephoneCar("telephone car");
        contactObj.setTelephoneCompany("telehpone company");
        contactObj.setTelephoneHome1("telephone home1");
        contactObj.setTelephoneHome2("telephone home2");
        contactObj.setTelephoneIp("telehpone ip");
        contactObj.setTelephoneIsdn("telehpone isdn");
        contactObj.setTelephoneOther("telephone other");
        contactObj.setTelephonePager("telephone pager");
        contactObj.setTelephonePrimary("telephone primary");
        contactObj.setTelephoneRadio("telephone radio");
        contactObj.setTelephoneTelex("telephone telex");
        contactObj.setTelephoneTtytdd("telephone ttytdd");
        contactObj.setTitle("title");
        contactObj.setUrl("url");
        contactObj.setUserfield01("Userfield01");
        contactObj.setUserfield02("Userfield02");
        contactObj.setUserfield03("Userfield03");
        contactObj.setUserfield04("Userfield04");
        contactObj.setUserfield05("Userfield05");
        contactObj.setUserfield06("Userfield06");
        contactObj.setUserfield07("Userfield07");
        contactObj.setUserfield08("Userfield08");
        contactObj.setUserfield09("Userfield09");
        contactObj.setUserfield10("Userfield10");
        contactObj.setUserfield11("Userfield11");
        contactObj.setUserfield12("Userfield12");
        contactObj.setUserfield13("Userfield13");
        contactObj.setUserfield14("Userfield14");
        contactObj.setUserfield15("Userfield15");
        contactObj.setUserfield16("Userfield16");
        contactObj.setUserfield17("Userfield17");
        contactObj.setUserfield18("Userfield18");
        contactObj.setUserfield19("Userfield19");
        contactObj.setUserfield20("Userfield20");
        contactObj.setDefaultAddress(1);
        contactObj.setUid("uid");

        contactObj.setFolderId(contactFolderId);

        final ContactData link1 = createContactObject("link1");
        final String linkId1 = createContact(link1);
        link1.setId(linkId1);

        List<DistributionListMember> distList = new ArrayList<>(2);
        distList.add(getMember("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT));
        DistributionListMember member = getMember(link1.getDisplayName(), link1.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
        member.setId(link1.getId());
        distList.add(member);

        contactObj.setDistributionList(distList);

        return contactObj;
    }

    public String createContact(final ContactData contactObj) throws Exception {
        ContactUpdateResponse response = contactsApi.createContact(apiClient.getSession(), contactObj);
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        lastTimestamp = response.getTimestamp();
        return rememberContact(response.getData().getId());
    }

    private String rememberContact(String id) {
        createdContacts.add(id);
        return id;
    }

    @Override
    public void tearDown() throws Exception {
        List<ContactListElement> body = new ArrayList<>();
        for(String id: createdContacts) {
            ContactListElement element = new ContactListElement();
            element.setFolder(contactFolderId);
            element.setId(id);
            body.add(element);
        }
        if(!body.isEmpty()) {
            try {
                contactsApi.deleteContacts(getSessionId(), Long.MAX_VALUE, body);
            } catch(Exception e) {
                // ignore
            }
        }
        super.tearDown();
    }

    public void updateContact(final ContactData contactObj, String folder) throws Exception {
        ContactUpdateResponse updateContact = contactsApi.updateContact(apiClient.getSession(), folder, contactObj.getId(), lastTimestamp, contactObj);
        assertNull(updateContact.getErrorDesc(), updateContact.getError());
    }

    public ContactData loadContact(final String objectId, final String folder) throws Exception {
        ContactResponse response = contactsApi.getContact(apiClient.getSession(), objectId, folder);
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        lastTimestamp = response.getTimestamp();
        return response.getData();
    }

    public static byte[] loadImageByURL(AJAXClient client, String imageUrl) throws Exception {
        InputStream inputStream = null;
        try {
            HttpGet httpRequest = new HttpGet(client.getProtocol() + "://" + client.getHostname() + imageUrl);
            final HttpResponse httpResponse = client.getSession().getHttpClient().execute(httpRequest);
            inputStream = httpResponse.getEntity().getContent();
            final int len = 8192;
            final byte[] buf = new byte[len];
            @SuppressWarnings("resource") //Closing a ByteArrayOutputStream has no effect
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(len << 2);
            for (int read; (read = inputStream.read(buf, 0, len)) > 0;) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

    public byte[] loadImageByURL(final String protocol, final String hostname, final String imageUrl) throws Exception {
        InputStream inputStream = null;
        try {
            final AJAXSession ajaxSession = getSession();

            final HttpGet httpRequest = new HttpGet((null == protocol ? "http" : protocol) + "://" + (hostname == null ? "localhost" : hostname) + imageUrl);
            final HttpResponse httpResponse = ajaxSession.getHttpClient().execute(httpRequest);
            inputStream = httpResponse.getEntity().getContent();
            final int len = 8192;
            final byte[] buf = new byte[len];
            @SuppressWarnings("resource") //Closing a ByteArrayOutputStream has no effect
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(len << 2);
            for (int read; (read = inputStream.read(buf, 0, len)) > 0;) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

    private HashSet<String> distributionlist2String(final List<DistributionListMember> distributionListEntry) throws Exception {
        if (distributionListEntry == null) {
            return null;
        }

        final HashSet<String> hs = new HashSet<>();

        for (DistributionListMember element : distributionListEntry) {
            hs.add(entry2String(element));
        }

        return hs;
    }

    private String entry2String(final DistributionListMember entry) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("ID" + entry.getId());
        sb.append("D" + entry.getDisplayName());
        sb.append("F" + entry.getMailField());
        sb.append("E" + entry.getMail());

        return sb.toString();
    }

    public ContactData[] createSeveralContacts(String givenName, String surName, int numberOfContacts) {
        ContactData[] contacts = new ContactData[numberOfContacts];
        for (int i = 0; i < numberOfContacts; i++) {
            contacts[i] = createOneMinimalContact(givenName, surName, givenName + "_" + surName + "_" + i);
        }
        return contacts;
    }

    public ContactData createOneMinimalContact(String givenName, String surName, String displayName) {
        ContactData newContact = new ContactData();
        newContact.setFirstName(givenName);
        newContact.setLastName(surName);
        newContact.setDisplayName(displayName);
        newContact.setFolderId(contactFolderId);
        return newContact;
    }

}
