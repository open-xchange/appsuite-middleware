
package com.openexchange.groupware.container;

import static com.openexchange.groupware.container.Contact.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Date;
import org.junit.Test;

public class ContactObjectTest extends CommonObjectTest {

    public Contact getContact() {
        Contact object = new Contact();

        fillContact(object);

        return object;
    }

    public void fillContact(Contact object) {
        super.fillCommonObject(object);

        object.setURL("Bla");

        object.setAnniversary(new Date(42));

        object.setAssistantName("Bla");

        object.setBirthday(new Date(42));

        object.setBranches("Bla");

        object.setBusinessCategory("Bla");

        object.setCellularTelephone1("Bla");

        object.setCellularTelephone2("Bla");

        object.setCityBusiness("Bla");

        object.setCityHome("Bla");

        object.setCityOther("Bla");

        object.setCommercialRegister("Bla");

        object.setCompany("Bla");

        object.setContextId(-12);

        object.setCountryBusiness("Bla");

        object.setCountryHome("Bla");

        object.setCountryOther("Bla");

        object.setDefaultAddress(-12);

        object.setDepartment("Bla");

        object.setDisplayName("Bla");

        object.setEmail1("Bla");

        object.setEmail2("Bla");

        object.setEmail3("Bla");

        object.setEmployeeType("Bla");

        object.setFaxBusiness("Bla");

        object.setFaxHome("Bla");

        object.setFaxOther("Bla");

        object.setFileAs("Bla");

        object.setGivenName("Bla");

        object.setImage1(new byte[] { 1, 2, 3 });

        object.setImageContentType("Bla");

        object.setImageLastModified(new Date(42));

        object.setInfo("Bla");

        object.setInstantMessenger1("Bla");

        object.setInstantMessenger2("Bla");

        object.setInternalUserId(-12);

        object.setManagerName("Bla");

        object.setMaritalStatus("Bla");

        object.setMiddleName("Bla");

        object.setNickname("Bla");

        object.setNote("Bla");

        object.setNumberOfChildren("Bla");

        object.setNumberOfDistributionLists(-12);

        object.setNumberOfEmployee("Bla");

        object.setNumberOfImages(-12);

        object.setPosition("Bla");

        object.setPostalCodeBusiness("Bla");

        object.setPostalCodeHome("Bla");

        object.setPostalCodeOther("Bla");

        object.setProfession("Bla");

        object.setRoomNumber("Bla");

        object.setSalesVolume("Bla");

        object.setSpouseName("Bla");

        object.setStateBusiness("Bla");

        object.setStateHome("Bla");

        object.setStateOther("Bla");

        object.setStreetBusiness("Bla");

        object.setStreetHome("Bla");

        object.setStreetOther("Bla");

        object.setSuffix("Bla");

        object.setSurName("Bla");

        object.setTaxID("Bla");

        object.setTelephoneAssistant("Bla");

        object.setTelephoneBusiness1("Bla");

        object.setTelephoneBusiness2("Bla");

        object.setTelephoneCallback("Bla");

        object.setTelephoneCar("Bla");

        object.setTelephoneCompany("Bla");

        object.setTelephoneHome1("Bla");

        object.setTelephoneHome2("Bla");

        object.setTelephoneIP("Bla");

        object.setTelephoneISDN("Bla");

        object.setTelephoneOther("Bla");

        object.setTelephonePager("Bla");

        object.setTelephonePrimary("Bla");

        object.setTelephoneRadio("Bla");

        object.setTelephoneTTYTTD("Bla");

        object.setTelephoneTelex("Bla");

        object.setTitle("Bla");

        object.setUserField01("Bla");

        object.setUserField02("Bla");

        object.setUserField03("Bla");

        object.setUserField04("Bla");

        object.setUserField05("Bla");

        object.setUserField06("Bla");

        object.setUserField07("Bla");

        object.setUserField08("Bla");

        object.setUserField09("Bla");

        object.setUserField10("Bla");

        object.setUserField11("Bla");

        object.setUserField12("Bla");

        object.setUserField13("Bla");

        object.setUserField14("Bla");

        object.setUserField15("Bla");

        object.setUserField16("Bla");

        object.setUserField17("Bla");

        object.setUserField18("Bla");

        object.setUserField19("Bla");

        object.setUserField20("Bla");

    }

    @Test
    public void testAttrAccessors() {
        Contact object = new Contact();
        // POSTAL_CODE_HOME
        assertFalse(object.contains(POSTAL_CODE_HOME));
        assertFalse(object.containsPostalCodeHome());

        object.setPostalCodeHome("Bla");
        assertTrue(object.contains(POSTAL_CODE_HOME));
        assertTrue(object.containsPostalCodeHome());
        assertEquals("Bla", object.get(POSTAL_CODE_HOME));

        object.set(POSTAL_CODE_HOME, "Blupp");
        assertEquals("Blupp", object.getPostalCodeHome());

        object.remove(POSTAL_CODE_HOME);
        assertFalse(object.contains(POSTAL_CODE_HOME));
        assertFalse(object.containsPostalCodeHome());

        // USERFIELD08
        assertFalse(object.contains(USERFIELD08));
        assertFalse(object.containsUserField08());

        object.setUserField08("Bla");
        assertTrue(object.contains(USERFIELD08));
        assertTrue(object.containsUserField08());
        assertEquals("Bla", object.get(USERFIELD08));

        object.set(USERFIELD08, "Blupp");
        assertEquals("Blupp", object.getUserField08());

        object.remove(USERFIELD08);
        assertFalse(object.contains(USERFIELD08));
        assertFalse(object.containsUserField08());

        // CITY_OTHER
        assertFalse(object.contains(CITY_OTHER));
        assertFalse(object.containsCityOther());

        object.setCityOther("Bla");
        assertTrue(object.contains(CITY_OTHER));
        assertTrue(object.containsCityOther());
        assertEquals("Bla", object.get(CITY_OTHER));

        object.set(CITY_OTHER, "Blupp");
        assertEquals("Blupp", object.getCityOther());

        object.remove(CITY_OTHER);
        assertFalse(object.contains(CITY_OTHER));
        assertFalse(object.containsCityOther());

        // USERFIELD09
        assertFalse(object.contains(USERFIELD09));
        assertFalse(object.containsUserField09());

        object.setUserField09("Bla");
        assertTrue(object.contains(USERFIELD09));
        assertTrue(object.containsUserField09());
        assertEquals("Bla", object.get(USERFIELD09));

        object.set(USERFIELD09, "Blupp");
        assertEquals("Blupp", object.getUserField09());

        object.remove(USERFIELD09);
        assertFalse(object.contains(USERFIELD09));
        assertFalse(object.containsUserField09());

        // USERFIELD06
        assertFalse(object.contains(USERFIELD06));
        assertFalse(object.containsUserField06());

        object.setUserField06("Bla");
        assertTrue(object.contains(USERFIELD06));
        assertTrue(object.containsUserField06());
        assertEquals("Bla", object.get(USERFIELD06));

        object.set(USERFIELD06, "Blupp");
        assertEquals("Blupp", object.getUserField06());

        object.remove(USERFIELD06);
        assertFalse(object.contains(USERFIELD06));
        assertFalse(object.containsUserField06());

        // STATE_BUSINESS
        assertFalse(object.contains(STATE_BUSINESS));
        assertFalse(object.containsStateBusiness());

        object.setStateBusiness("Bla");
        assertTrue(object.contains(STATE_BUSINESS));
        assertTrue(object.containsStateBusiness());
        assertEquals("Bla", object.get(STATE_BUSINESS));

        object.set(STATE_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getStateBusiness());

        object.remove(STATE_BUSINESS);
        assertFalse(object.contains(STATE_BUSINESS));
        assertFalse(object.containsStateBusiness());

        // NUMBER_OF_IMAGES
        object.setNumberOfImages(-12);
        assertEquals(-12, object.get(NUMBER_OF_IMAGES));

        object.set(NUMBER_OF_IMAGES, 12);
        assertEquals(12, object.getNumberOfImages());

        // IMAGE1_CONTENT_TYPE
        assertFalse(object.contains(IMAGE1_CONTENT_TYPE));
        assertFalse(object.containsImageContentType());

        object.setImageContentType("Bla");
        assertTrue(object.contains(IMAGE1_CONTENT_TYPE));
        assertTrue(object.containsImageContentType());
        assertEquals("Bla", object.get(IMAGE1_CONTENT_TYPE));

        object.set(IMAGE1_CONTENT_TYPE, "Blupp");
        assertEquals("Blupp", object.getImageContentType());

        object.remove(IMAGE1_CONTENT_TYPE);
        assertFalse(object.contains(IMAGE1_CONTENT_TYPE));
        assertFalse(object.containsImageContentType());

        // GIVEN_NAME
        assertFalse(object.contains(GIVEN_NAME));
        assertFalse(object.containsGivenName());

        object.setGivenName("Bla");
        assertTrue(object.contains(GIVEN_NAME));
        assertTrue(object.containsGivenName());
        assertEquals("Bla", object.get(GIVEN_NAME));

        object.set(GIVEN_NAME, "Blupp");
        assertEquals("Blupp", object.getGivenName());

        object.remove(GIVEN_NAME);
        assertFalse(object.contains(GIVEN_NAME));
        assertFalse(object.containsGivenName());

        // ANNIVERSARY
        assertFalse(object.contains(ANNIVERSARY));
        assertFalse(object.containsAnniversary());

        object.setAnniversary(new Date(42));
        assertTrue(object.contains(ANNIVERSARY));
        assertTrue(object.containsAnniversary());
        assertEquals(new Date(42), object.get(ANNIVERSARY));

        object.set(ANNIVERSARY, new Date(23));
        assertEquals(new Date(23), object.getAnniversary());

        object.remove(ANNIVERSARY);
        assertFalse(object.contains(ANNIVERSARY));
        assertFalse(object.containsAnniversary());

        // USERFIELD18
        assertFalse(object.contains(USERFIELD18));
        assertFalse(object.containsUserField18());

        object.setUserField18("Bla");
        assertTrue(object.contains(USERFIELD18));
        assertTrue(object.containsUserField18());
        assertEquals("Bla", object.get(USERFIELD18));

        object.set(USERFIELD18, "Blupp");
        assertEquals("Blupp", object.getUserField18());

        object.remove(USERFIELD18);
        assertFalse(object.contains(USERFIELD18));
        assertFalse(object.containsUserField18());

        // SALES_VOLUME
        assertFalse(object.contains(SALES_VOLUME));
        assertFalse(object.containsSalesVolume());

        object.setSalesVolume("Bla");
        assertTrue(object.contains(SALES_VOLUME));
        assertTrue(object.containsSalesVolume());
        assertEquals("Bla", object.get(SALES_VOLUME));

        object.set(SALES_VOLUME, "Blupp");
        assertEquals("Blupp", object.getSalesVolume());

        object.remove(SALES_VOLUME);
        assertFalse(object.contains(SALES_VOLUME));
        assertFalse(object.containsSalesVolume());

        // STREET_OTHER
        assertFalse(object.contains(STREET_OTHER));
        assertFalse(object.containsStreetOther());

        object.setStreetOther("Bla");
        assertTrue(object.contains(STREET_OTHER));
        assertTrue(object.containsStreetOther());
        assertEquals("Bla", object.get(STREET_OTHER));

        object.set(STREET_OTHER, "Blupp");
        assertEquals("Blupp", object.getStreetOther());

        object.remove(STREET_OTHER);
        assertFalse(object.contains(STREET_OTHER));
        assertFalse(object.containsStreetOther());

        // USERFIELD04
        assertFalse(object.contains(USERFIELD04));
        assertFalse(object.containsUserField04());

        object.setUserField04("Bla");
        assertTrue(object.contains(USERFIELD04));
        assertTrue(object.containsUserField04());
        assertEquals("Bla", object.get(USERFIELD04));

        object.set(USERFIELD04, "Blupp");
        assertEquals("Blupp", object.getUserField04());

        object.remove(USERFIELD04);
        assertFalse(object.contains(USERFIELD04));
        assertFalse(object.containsUserField04());

        // POSTAL_CODE_BUSINESS
        assertFalse(object.contains(POSTAL_CODE_BUSINESS));
        assertFalse(object.containsPostalCodeBusiness());

        object.setPostalCodeBusiness("Bla");
        assertTrue(object.contains(POSTAL_CODE_BUSINESS));
        assertTrue(object.containsPostalCodeBusiness());
        assertEquals("Bla", object.get(POSTAL_CODE_BUSINESS));

        object.set(POSTAL_CODE_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getPostalCodeBusiness());

        object.remove(POSTAL_CODE_BUSINESS);
        assertFalse(object.contains(POSTAL_CODE_BUSINESS));
        assertFalse(object.containsPostalCodeBusiness());

        // TELEPHONE_HOME1
        assertFalse(object.contains(TELEPHONE_HOME1));
        assertFalse(object.containsTelephoneHome1());

        object.setTelephoneHome1("Bla");
        assertTrue(object.contains(TELEPHONE_HOME1));
        assertTrue(object.containsTelephoneHome1());
        assertEquals("Bla", object.get(TELEPHONE_HOME1));

        object.set(TELEPHONE_HOME1, "Blupp");
        assertEquals("Blupp", object.getTelephoneHome1());

        object.remove(TELEPHONE_HOME1);
        assertFalse(object.contains(TELEPHONE_HOME1));
        assertFalse(object.containsTelephoneHome1());

        // USERFIELD19
        assertFalse(object.contains(USERFIELD19));
        assertFalse(object.containsUserField19());

        object.setUserField19("Bla");
        assertTrue(object.contains(USERFIELD19));
        assertTrue(object.containsUserField19());
        assertEquals("Bla", object.get(USERFIELD19));

        object.set(USERFIELD19, "Blupp");
        assertEquals("Blupp", object.getUserField19());

        object.remove(USERFIELD19);
        assertFalse(object.contains(USERFIELD19));
        assertFalse(object.containsUserField19());

        // FAX_OTHER
        assertFalse(object.contains(FAX_OTHER));
        assertFalse(object.containsFaxOther());

        object.setFaxOther("Bla");
        assertTrue(object.contains(FAX_OTHER));
        assertTrue(object.containsFaxOther());
        assertEquals("Bla", object.get(FAX_OTHER));

        object.set(FAX_OTHER, "Blupp");
        assertEquals("Blupp", object.getFaxOther());

        object.remove(FAX_OTHER);
        assertFalse(object.contains(FAX_OTHER));
        assertFalse(object.containsFaxOther());

        // USERFIELD14
        assertFalse(object.contains(USERFIELD14));
        assertFalse(object.containsUserField14());

        object.setUserField14("Bla");
        assertTrue(object.contains(USERFIELD14));
        assertTrue(object.containsUserField14());
        assertEquals("Bla", object.get(USERFIELD14));

        object.set(USERFIELD14, "Blupp");
        assertEquals("Blupp", object.getUserField14());

        object.remove(USERFIELD14);
        assertFalse(object.contains(USERFIELD14));
        assertFalse(object.containsUserField14());

        // CITY_HOME
        assertFalse(object.contains(CITY_HOME));
        assertFalse(object.containsCityHome());

        object.setCityHome("Bla");
        assertTrue(object.contains(CITY_HOME));
        assertTrue(object.containsCityHome());
        assertEquals("Bla", object.get(CITY_HOME));

        object.set(CITY_HOME, "Blupp");
        assertEquals("Blupp", object.getCityHome());

        object.remove(CITY_HOME);
        assertFalse(object.contains(CITY_HOME));
        assertFalse(object.containsCityHome());

        // USERFIELD07
        assertFalse(object.contains(USERFIELD07));
        assertFalse(object.containsUserField07());

        object.setUserField07("Bla");
        assertTrue(object.contains(USERFIELD07));
        assertTrue(object.containsUserField07());
        assertEquals("Bla", object.get(USERFIELD07));

        object.set(USERFIELD07, "Blupp");
        assertEquals("Blupp", object.getUserField07());

        object.remove(USERFIELD07);
        assertFalse(object.contains(USERFIELD07));
        assertFalse(object.containsUserField07());

        // TITLE
        assertFalse(object.contains(TITLE));
        assertFalse(object.containsTitle());

        object.setTitle("Bla");
        assertTrue(object.contains(TITLE));
        assertTrue(object.containsTitle());
        assertEquals("Bla", object.get(TITLE));

        object.set(TITLE, "Blupp");
        assertEquals("Blupp", object.getTitle());

        object.remove(TITLE);
        assertFalse(object.contains(TITLE));
        assertFalse(object.containsTitle());

        // TELEPHONE_ASSISTANT
        assertFalse(object.contains(TELEPHONE_ASSISTANT));
        assertFalse(object.containsTelephoneAssistant());

        object.setTelephoneAssistant("Bla");
        assertTrue(object.contains(TELEPHONE_ASSISTANT));
        assertTrue(object.containsTelephoneAssistant());
        assertEquals("Bla", object.get(TELEPHONE_ASSISTANT));

        object.set(TELEPHONE_ASSISTANT, "Blupp");
        assertEquals("Blupp", object.getTelephoneAssistant());

        object.remove(TELEPHONE_ASSISTANT);
        assertFalse(object.contains(TELEPHONE_ASSISTANT));
        assertFalse(object.containsTelephoneAssistant());

        // FAX_BUSINESS
        assertFalse(object.contains(FAX_BUSINESS));
        assertFalse(object.containsFaxBusiness());

        object.setFaxBusiness("Bla");
        assertTrue(object.contains(FAX_BUSINESS));
        assertTrue(object.containsFaxBusiness());
        assertEquals("Bla", object.get(FAX_BUSINESS));

        object.set(FAX_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getFaxBusiness());

        object.remove(FAX_BUSINESS);
        assertFalse(object.contains(FAX_BUSINESS));
        assertFalse(object.containsFaxBusiness());

        // PROFESSION
        assertFalse(object.contains(PROFESSION));
        assertFalse(object.containsProfession());

        object.setProfession("Bla");
        assertTrue(object.contains(PROFESSION));
        assertTrue(object.containsProfession());
        assertEquals("Bla", object.get(PROFESSION));

        object.set(PROFESSION, "Blupp");
        assertEquals("Blupp", object.getProfession());

        object.remove(PROFESSION);
        assertFalse(object.contains(PROFESSION));
        assertFalse(object.containsProfession());

        // DEPARTMENT
        assertFalse(object.contains(DEPARTMENT));
        assertFalse(object.containsDepartment());

        object.setDepartment("Bla");
        assertTrue(object.contains(DEPARTMENT));
        assertTrue(object.containsDepartment());
        assertEquals("Bla", object.get(DEPARTMENT));

        object.set(DEPARTMENT, "Blupp");
        assertEquals("Blupp", object.getDepartment());

        object.remove(DEPARTMENT);
        assertFalse(object.contains(DEPARTMENT));
        assertFalse(object.containsDepartment());

        // USERFIELD01
        assertFalse(object.contains(USERFIELD01));
        assertFalse(object.containsUserField01());

        object.setUserField01("Bla");
        assertTrue(object.contains(USERFIELD01));
        assertTrue(object.containsUserField01());
        assertEquals("Bla", object.get(USERFIELD01));

        object.set(USERFIELD01, "Blupp");
        assertEquals("Blupp", object.getUserField01());

        object.remove(USERFIELD01);
        assertFalse(object.contains(USERFIELD01));
        assertFalse(object.containsUserField01());

        // USERFIELD12
        assertFalse(object.contains(USERFIELD12));
        assertFalse(object.containsUserField12());

        object.setUserField12("Bla");
        assertTrue(object.contains(USERFIELD12));
        assertTrue(object.containsUserField12());
        assertEquals("Bla", object.get(USERFIELD12));

        object.set(USERFIELD12, "Blupp");
        assertEquals("Blupp", object.getUserField12());

        object.remove(USERFIELD12);
        assertFalse(object.contains(USERFIELD12));
        assertFalse(object.containsUserField12());

        // TELEPHONE_IP
        assertFalse(object.contains(TELEPHONE_IP));
        assertFalse(object.containsTelephoneIP());

        object.setTelephoneIP("Bla");
        assertTrue(object.contains(TELEPHONE_IP));
        assertTrue(object.containsTelephoneIP());
        assertEquals("Bla", object.get(TELEPHONE_IP));

        object.set(TELEPHONE_IP, "Blupp");
        assertEquals("Blupp", object.getTelephoneIP());

        object.remove(TELEPHONE_IP);
        assertFalse(object.contains(TELEPHONE_IP));
        assertFalse(object.containsTelephoneIP());

        // URL
        assertFalse(object.contains(URL));
        assertFalse(object.containsURL());

        object.setURL("Bla");
        assertTrue(object.contains(URL));
        assertTrue(object.containsURL());
        assertEquals("Bla", object.get(URL));

        object.set(URL, "Blupp");
        assertEquals("Blupp", object.getURL());

        object.remove(URL);
        assertFalse(object.contains(URL));
        assertFalse(object.containsURL());

        // NUMBER_OF_EMPLOYEE
        assertFalse(object.contains(NUMBER_OF_EMPLOYEE));
        assertFalse(object.containsNumberOfEmployee());

        object.setNumberOfEmployee("Bla");
        assertTrue(object.contains(NUMBER_OF_EMPLOYEE));
        assertTrue(object.containsNumberOfEmployee());
        assertEquals("Bla", object.get(NUMBER_OF_EMPLOYEE));

        object.set(NUMBER_OF_EMPLOYEE, "Blupp");
        assertEquals("Blupp", object.getNumberOfEmployee());

        object.remove(NUMBER_OF_EMPLOYEE);
        assertFalse(object.contains(NUMBER_OF_EMPLOYEE));
        assertFalse(object.containsNumberOfEmployee());

        // POSTAL_CODE_OTHER
        assertFalse(object.contains(POSTAL_CODE_OTHER));
        assertFalse(object.containsPostalCodeOther());

        object.setPostalCodeOther("Bla");
        assertTrue(object.contains(POSTAL_CODE_OTHER));
        assertTrue(object.containsPostalCodeOther());
        assertEquals("Bla", object.get(POSTAL_CODE_OTHER));

        object.set(POSTAL_CODE_OTHER, "Blupp");
        assertEquals("Blupp", object.getPostalCodeOther());

        object.remove(POSTAL_CODE_OTHER);
        assertFalse(object.contains(POSTAL_CODE_OTHER));
        assertFalse(object.containsPostalCodeOther());

        // USERFIELD10
        assertFalse(object.contains(USERFIELD10));
        assertFalse(object.containsUserField10());

        object.setUserField10("Bla");
        assertTrue(object.contains(USERFIELD10));
        assertTrue(object.containsUserField10());
        assertEquals("Bla", object.get(USERFIELD10));

        object.set(USERFIELD10, "Blupp");
        assertEquals("Blupp", object.getUserField10());

        object.remove(USERFIELD10);
        assertFalse(object.contains(USERFIELD10));
        assertFalse(object.containsUserField10());

        // BIRTHDAY
        assertFalse(object.contains(BIRTHDAY));
        assertFalse(object.containsBirthday());

        object.setBirthday(new Date(42));
        assertTrue(object.contains(BIRTHDAY));
        assertTrue(object.containsBirthday());
        assertEquals(new Date(42), object.get(BIRTHDAY));

        object.set(BIRTHDAY, new Date(23));
        assertEquals(new Date(23), object.getBirthday());

        object.remove(BIRTHDAY);
        assertFalse(object.contains(BIRTHDAY));
        assertFalse(object.containsBirthday());

        // EMAIL1
        assertFalse(object.contains(EMAIL1));
        assertFalse(object.containsEmail1());

        object.setEmail1("Bla");
        assertTrue(object.contains(EMAIL1));
        assertTrue(object.containsEmail1());
        assertEquals("Bla", object.get(EMAIL1));

        object.set(EMAIL1, "Blupp");
        assertEquals("Blupp", object.getEmail1());

        object.remove(EMAIL1);
        assertFalse(object.contains(EMAIL1));
        assertFalse(object.containsEmail1());

        // STATE_HOME
        assertFalse(object.contains(STATE_HOME));
        assertFalse(object.containsStateHome());

        object.setStateHome("Bla");
        assertTrue(object.contains(STATE_HOME));
        assertTrue(object.containsStateHome());
        assertEquals("Bla", object.get(STATE_HOME));

        object.set(STATE_HOME, "Blupp");
        assertEquals("Blupp", object.getStateHome());

        object.remove(STATE_HOME);
        assertFalse(object.contains(STATE_HOME));
        assertFalse(object.containsStateHome());

        // TELEPHONE_HOME2
        assertFalse(object.contains(TELEPHONE_HOME2));
        assertFalse(object.containsTelephoneHome2());

        object.setTelephoneHome2("Bla");
        assertTrue(object.contains(TELEPHONE_HOME2));
        assertTrue(object.containsTelephoneHome2());
        assertEquals("Bla", object.get(TELEPHONE_HOME2));

        object.set(TELEPHONE_HOME2, "Blupp");
        assertEquals("Blupp", object.getTelephoneHome2());

        object.remove(TELEPHONE_HOME2);
        assertFalse(object.contains(TELEPHONE_HOME2));
        assertFalse(object.containsTelephoneHome2());

        // TELEPHONE_TTYTDD
        assertFalse(object.contains(TELEPHONE_TTYTDD));
        assertFalse(object.containsTelephoneTTYTTD());

        object.setTelephoneTTYTTD("Bla");
        assertTrue(object.contains(TELEPHONE_TTYTDD));
        assertTrue(object.containsTelephoneTTYTTD());
        assertEquals("Bla", object.get(TELEPHONE_TTYTDD));

        object.set(TELEPHONE_TTYTDD, "Blupp");
        assertEquals("Blupp", object.getTelephoneTTYTTD());

        object.remove(TELEPHONE_TTYTDD);
        assertFalse(object.contains(TELEPHONE_TTYTDD));
        assertFalse(object.containsTelephoneTTYTTD());

        // TELEPHONE_OTHER
        assertFalse(object.contains(TELEPHONE_OTHER));
        assertFalse(object.containsTelephoneOther());

        object.setTelephoneOther("Bla");
        assertTrue(object.contains(TELEPHONE_OTHER));
        assertTrue(object.containsTelephoneOther());
        assertEquals("Bla", object.get(TELEPHONE_OTHER));

        object.set(TELEPHONE_OTHER, "Blupp");
        assertEquals("Blupp", object.getTelephoneOther());

        object.remove(TELEPHONE_OTHER);
        assertFalse(object.contains(TELEPHONE_OTHER));
        assertFalse(object.containsTelephoneOther());

        // COMMERCIAL_REGISTER
        assertFalse(object.contains(COMMERCIAL_REGISTER));
        assertFalse(object.containsCommercialRegister());

        object.setCommercialRegister("Bla");
        assertTrue(object.contains(COMMERCIAL_REGISTER));
        assertTrue(object.containsCommercialRegister());
        assertEquals("Bla", object.get(COMMERCIAL_REGISTER));

        object.set(COMMERCIAL_REGISTER, "Blupp");
        assertEquals("Blupp", object.getCommercialRegister());

        object.remove(COMMERCIAL_REGISTER);
        assertFalse(object.contains(COMMERCIAL_REGISTER));
        assertFalse(object.containsCommercialRegister());

        // COUNTRY_BUSINESS
        assertFalse(object.contains(COUNTRY_BUSINESS));
        assertFalse(object.containsCountryBusiness());

        object.setCountryBusiness("Bla");
        assertTrue(object.contains(COUNTRY_BUSINESS));
        assertTrue(object.containsCountryBusiness());
        assertEquals("Bla", object.get(COUNTRY_BUSINESS));

        object.set(COUNTRY_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getCountryBusiness());

        object.remove(COUNTRY_BUSINESS);
        assertFalse(object.contains(COUNTRY_BUSINESS));
        assertFalse(object.containsCountryBusiness());

        // USERFIELD11
        assertFalse(object.contains(USERFIELD11));
        assertFalse(object.containsUserField11());

        object.setUserField11("Bla");
        assertTrue(object.contains(USERFIELD11));
        assertTrue(object.containsUserField11());
        assertEquals("Bla", object.get(USERFIELD11));

        object.set(USERFIELD11, "Blupp");
        assertEquals("Blupp", object.getUserField11());

        object.remove(USERFIELD11);
        assertFalse(object.contains(USERFIELD11));
        assertFalse(object.containsUserField11());

        // BUSINESS_CATEGORY
        assertFalse(object.contains(BUSINESS_CATEGORY));
        assertFalse(object.containsBusinessCategory());

        object.setBusinessCategory("Bla");
        assertTrue(object.contains(BUSINESS_CATEGORY));
        assertTrue(object.containsBusinessCategory());
        assertEquals("Bla", object.get(BUSINESS_CATEGORY));

        object.set(BUSINESS_CATEGORY, "Blupp");
        assertEquals("Blupp", object.getBusinessCategory());

        object.remove(BUSINESS_CATEGORY);
        assertFalse(object.contains(BUSINESS_CATEGORY));
        assertFalse(object.containsBusinessCategory());

        // CONTEXT_ID
        assertFalse(object.contains(CONTEXTID));
        assertFalse(object.containsContextId());

        object.setContextId(-12);
        assertTrue(object.contains(CONTEXTID));
        assertTrue(object.containsContextId());
        assertEquals(-12, object.get(CONTEXTID));

        object.set(CONTEXTID, 12);
        assertEquals(12, object.getContextId());

        object.remove(CONTEXTID);
        assertFalse(object.contains(CONTEXTID));
        assertFalse(object.containsContextId());

        // STATE_OTHER
        assertFalse(object.contains(STATE_OTHER));
        assertFalse(object.containsStateOther());

        object.setStateOther("Bla");
        assertTrue(object.contains(STATE_OTHER));
        assertTrue(object.containsStateOther());
        assertEquals("Bla", object.get(STATE_OTHER));

        object.set(STATE_OTHER, "Blupp");
        assertEquals("Blupp", object.getStateOther());

        object.remove(STATE_OTHER);
        assertFalse(object.contains(STATE_OTHER));
        assertFalse(object.containsStateOther());

        // INTERNAL_USER_ID
        assertFalse(object.contains(INTERNAL_USERID));
        assertFalse(object.containsInternalUserId());

        object.setInternalUserId(-12);
        assertTrue(object.contains(INTERNAL_USERID));
        assertTrue(object.containsInternalUserId());
        assertEquals(-12, object.get(INTERNAL_USERID));

        object.set(INTERNAL_USERID, 12);
        assertEquals(12, object.getInternalUserId());

        object.remove(INTERNAL_USERID);
        assertFalse(object.contains(INTERNAL_USERID));
        assertFalse(object.containsInternalUserId());

        // CELLULAR_TELEPHONE1
        assertFalse(object.contains(CELLULAR_TELEPHONE1));
        assertFalse(object.containsCellularTelephone1());

        object.setCellularTelephone1("Bla");
        assertTrue(object.contains(CELLULAR_TELEPHONE1));
        assertTrue(object.containsCellularTelephone1());
        assertEquals("Bla", object.get(CELLULAR_TELEPHONE1));

        object.set(CELLULAR_TELEPHONE1, "Blupp");
        assertEquals("Blupp", object.getCellularTelephone1());

        object.remove(CELLULAR_TELEPHONE1);
        assertFalse(object.contains(CELLULAR_TELEPHONE1));
        assertFalse(object.containsCellularTelephone1());

        // BRANCHES
        assertFalse(object.contains(BRANCHES));
        assertFalse(object.containsBranches());

        object.setBranches("Bla");
        assertTrue(object.contains(BRANCHES));
        assertTrue(object.containsBranches());
        assertEquals("Bla", object.get(BRANCHES));

        object.set(BRANCHES, "Blupp");
        assertEquals("Blupp", object.getBranches());

        object.remove(BRANCHES);
        assertFalse(object.contains(BRANCHES));
        assertFalse(object.containsBranches());

        // NOTE
        assertFalse(object.contains(NOTE));
        assertFalse(object.containsNote());

        object.setNote("Bla");
        assertTrue(object.contains(NOTE));
        assertTrue(object.containsNote());
        assertEquals("Bla", object.get(NOTE));

        object.set(NOTE, "Blupp");
        assertEquals("Blupp", object.getNote());

        object.remove(NOTE);
        assertFalse(object.contains(NOTE));
        assertFalse(object.containsNote());

        // EMAIL3
        assertFalse(object.contains(EMAIL3));
        assertFalse(object.containsEmail3());

        object.setEmail3("Bla");
        assertTrue(object.contains(EMAIL3));
        assertTrue(object.containsEmail3());
        assertEquals("Bla", object.get(EMAIL3));

        object.set(EMAIL3, "Blupp");
        assertEquals("Blupp", object.getEmail3());

        object.remove(EMAIL3);
        assertFalse(object.contains(EMAIL3));
        assertFalse(object.containsEmail3());

        // CELLULAR_TELEPHONE2
        assertFalse(object.contains(CELLULAR_TELEPHONE2));
        assertFalse(object.containsCellularTelephone2());

        object.setCellularTelephone2("Bla");
        assertTrue(object.contains(CELLULAR_TELEPHONE2));
        assertTrue(object.containsCellularTelephone2());
        assertEquals("Bla", object.get(CELLULAR_TELEPHONE2));

        object.set(CELLULAR_TELEPHONE2, "Blupp");
        assertEquals("Blupp", object.getCellularTelephone2());

        object.remove(CELLULAR_TELEPHONE2);
        assertFalse(object.contains(CELLULAR_TELEPHONE2));
        assertFalse(object.containsCellularTelephone2());

        // INSTANT_MESSENGER1
        assertFalse(object.contains(INSTANT_MESSENGER1));
        assertFalse(object.containsInstantMessenger1());

        object.setInstantMessenger1("Bla");
        assertTrue(object.contains(INSTANT_MESSENGER1));
        assertTrue(object.containsInstantMessenger1());
        assertEquals("Bla", object.get(INSTANT_MESSENGER1));

        object.set(INSTANT_MESSENGER1, "Blupp");
        assertEquals("Blupp", object.getInstantMessenger1());

        object.remove(INSTANT_MESSENGER1);
        assertFalse(object.contains(INSTANT_MESSENGER1));
        assertFalse(object.containsInstantMessenger1());

        // MANAGER_NAME
        assertFalse(object.contains(MANAGER_NAME));
        assertFalse(object.containsManagerName());

        object.setManagerName("Bla");
        assertTrue(object.contains(MANAGER_NAME));
        assertTrue(object.containsManagerName());
        assertEquals("Bla", object.get(MANAGER_NAME));

        object.set(MANAGER_NAME, "Blupp");
        assertEquals("Blupp", object.getManagerName());

        object.remove(MANAGER_NAME);
        assertFalse(object.contains(MANAGER_NAME));
        assertFalse(object.containsManagerName());

        // TELEPHONE_TELEX
        assertFalse(object.contains(TELEPHONE_TELEX));
        assertFalse(object.containsTelephoneTelex());

        object.setTelephoneTelex("Bla");
        assertTrue(object.contains(TELEPHONE_TELEX));
        assertTrue(object.containsTelephoneTelex());
        assertEquals("Bla", object.get(TELEPHONE_TELEX));

        object.set(TELEPHONE_TELEX, "Blupp");
        assertEquals("Blupp", object.getTelephoneTelex());

        object.remove(TELEPHONE_TELEX);
        assertFalse(object.contains(TELEPHONE_TELEX));
        assertFalse(object.containsTelephoneTelex());

        // EMAIL2
        assertFalse(object.contains(EMAIL2));
        assertFalse(object.containsEmail2());

        object.setEmail2("Bla");
        assertTrue(object.contains(EMAIL2));
        assertTrue(object.containsEmail2());
        assertEquals("Bla", object.get(EMAIL2));

        object.set(EMAIL2, "Blupp");
        assertEquals("Blupp", object.getEmail2());

        object.remove(EMAIL2);
        assertFalse(object.contains(EMAIL2));
        assertFalse(object.containsEmail2());

        // EMPLOYEE_TYPE
        assertFalse(object.contains(EMPLOYEE_TYPE));
        assertFalse(object.containsEmployeeType());

        object.setEmployeeType("Bla");
        assertTrue(object.contains(EMPLOYEE_TYPE));
        assertTrue(object.containsEmployeeType());
        assertEquals("Bla", object.get(EMPLOYEE_TYPE));

        object.set(EMPLOYEE_TYPE, "Blupp");
        assertEquals("Blupp", object.getEmployeeType());

        object.remove(EMPLOYEE_TYPE);
        assertFalse(object.contains(EMPLOYEE_TYPE));
        assertFalse(object.containsEmployeeType());

        // TELEPHONE_RADIO
        assertFalse(object.contains(TELEPHONE_RADIO));
        assertFalse(object.containsTelephoneRadio());

        object.setTelephoneRadio("Bla");
        assertTrue(object.contains(TELEPHONE_RADIO));
        assertTrue(object.containsTelephoneRadio());
        assertEquals("Bla", object.get(TELEPHONE_RADIO));

        object.set(TELEPHONE_RADIO, "Blupp");
        assertEquals("Blupp", object.getTelephoneRadio());

        object.remove(TELEPHONE_RADIO);
        assertFalse(object.contains(TELEPHONE_RADIO));
        assertFalse(object.containsTelephoneRadio());

        // NUMBER_OF_CHILDREN
        assertFalse(object.contains(NUMBER_OF_CHILDREN));
        assertFalse(object.containsNumberOfChildren());

        object.setNumberOfChildren("Bla");
        assertTrue(object.contains(NUMBER_OF_CHILDREN));
        assertTrue(object.containsNumberOfChildren());
        assertEquals("Bla", object.get(NUMBER_OF_CHILDREN));

        object.set(NUMBER_OF_CHILDREN, "Blupp");
        assertEquals("Blupp", object.getNumberOfChildren());

        object.remove(NUMBER_OF_CHILDREN);
        assertFalse(object.contains(NUMBER_OF_CHILDREN));
        assertFalse(object.containsNumberOfChildren());

        // STREET_BUSINESS
        assertFalse(object.contains(STREET_BUSINESS));
        assertFalse(object.containsStreetBusiness());

        object.setStreetBusiness("Bla");
        assertTrue(object.contains(STREET_BUSINESS));
        assertTrue(object.containsStreetBusiness());
        assertEquals("Bla", object.get(STREET_BUSINESS));

        object.set(STREET_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getStreetBusiness());

        object.remove(STREET_BUSINESS);
        assertFalse(object.contains(STREET_BUSINESS));
        assertFalse(object.containsStreetBusiness());

        // DEFAULT_ADDRESS
        assertFalse(object.contains(DEFAULT_ADDRESS));
        assertFalse(object.containsDefaultAddress());

        object.setDefaultAddress(-12);
        assertTrue(object.contains(DEFAULT_ADDRESS));
        assertTrue(object.containsDefaultAddress());
        assertEquals(-12, object.get(DEFAULT_ADDRESS));

        object.set(DEFAULT_ADDRESS, 12);
        assertEquals(12, object.getDefaultAddress());

        object.remove(DEFAULT_ADDRESS);
        assertFalse(object.contains(DEFAULT_ADDRESS));
        assertFalse(object.containsDefaultAddress());

        // TELEPHONE_ISDN
        assertFalse(object.contains(TELEPHONE_ISDN));
        assertFalse(object.containsTelephoneISDN());

        object.setTelephoneISDN("Bla");
        assertTrue(object.contains(TELEPHONE_ISDN));
        assertTrue(object.containsTelephoneISDN());
        assertEquals("Bla", object.get(TELEPHONE_ISDN));

        object.set(TELEPHONE_ISDN, "Blupp");
        assertEquals("Blupp", object.getTelephoneISDN());

        object.remove(TELEPHONE_ISDN);
        assertFalse(object.contains(TELEPHONE_ISDN));
        assertFalse(object.containsTelephoneISDN());

        // FAX_HOME
        assertFalse(object.contains(FAX_HOME));
        assertFalse(object.containsFaxHome());

        object.setFaxHome("Bla");
        assertTrue(object.contains(FAX_HOME));
        assertTrue(object.containsFaxHome());
        assertEquals("Bla", object.get(FAX_HOME));

        object.set(FAX_HOME, "Blupp");
        assertEquals("Blupp", object.getFaxHome());

        object.remove(FAX_HOME);
        assertFalse(object.contains(FAX_HOME));
        assertFalse(object.containsFaxHome());

        // MIDDLE_NAME
        assertFalse(object.contains(MIDDLE_NAME));
        assertFalse(object.containsMiddleName());

        object.setMiddleName("Bla");
        assertTrue(object.contains(MIDDLE_NAME));
        assertTrue(object.containsMiddleName());
        assertEquals("Bla", object.get(MIDDLE_NAME));

        object.set(MIDDLE_NAME, "Blupp");
        assertEquals("Blupp", object.getMiddleName());

        object.remove(MIDDLE_NAME);
        assertFalse(object.contains(MIDDLE_NAME));
        assertFalse(object.containsMiddleName());

        // USERFIELD13
        assertFalse(object.contains(USERFIELD13));
        assertFalse(object.containsUserField13());

        object.setUserField13("Bla");
        assertTrue(object.contains(USERFIELD13));
        assertTrue(object.containsUserField13());
        assertEquals("Bla", object.get(USERFIELD13));

        object.set(USERFIELD13, "Blupp");
        assertEquals("Blupp", object.getUserField13());

        object.remove(USERFIELD13);
        assertFalse(object.contains(USERFIELD13));
        assertFalse(object.containsUserField13());

        // ROOM_NUMBER
        assertFalse(object.contains(ROOM_NUMBER));
        assertFalse(object.containsRoomNumber());

        object.setRoomNumber("Bla");
        assertTrue(object.contains(ROOM_NUMBER));
        assertTrue(object.containsRoomNumber());
        assertEquals("Bla", object.get(ROOM_NUMBER));

        object.set(ROOM_NUMBER, "Blupp");
        assertEquals("Blupp", object.getRoomNumber());

        object.remove(ROOM_NUMBER);
        assertFalse(object.contains(ROOM_NUMBER));
        assertFalse(object.containsRoomNumber());

        // MARITAL_STATUS
        assertFalse(object.contains(MARITAL_STATUS));
        assertFalse(object.containsMaritalStatus());

        object.setMaritalStatus("Bla");
        assertTrue(object.contains(MARITAL_STATUS));
        assertTrue(object.containsMaritalStatus());
        assertEquals("Bla", object.get(MARITAL_STATUS));

        object.set(MARITAL_STATUS, "Blupp");
        assertEquals("Blupp", object.getMaritalStatus());

        object.remove(MARITAL_STATUS);
        assertFalse(object.contains(MARITAL_STATUS));
        assertFalse(object.containsMaritalStatus());

        // USERFIELD15
        assertFalse(object.contains(USERFIELD15));
        assertFalse(object.containsUserField15());

        object.setUserField15("Bla");
        assertTrue(object.contains(USERFIELD15));
        assertTrue(object.containsUserField15());
        assertEquals("Bla", object.get(USERFIELD15));

        object.set(USERFIELD15, "Blupp");
        assertEquals("Blupp", object.getUserField15());

        object.remove(USERFIELD15);
        assertFalse(object.contains(USERFIELD15));
        assertFalse(object.containsUserField15());

        // COUNTRY_HOME
        assertFalse(object.contains(COUNTRY_HOME));
        assertFalse(object.containsCountryHome());

        object.setCountryHome("Bla");
        assertTrue(object.contains(COUNTRY_HOME));
        assertTrue(object.containsCountryHome());
        assertEquals("Bla", object.get(COUNTRY_HOME));

        object.set(COUNTRY_HOME, "Blupp");
        assertEquals("Blupp", object.getCountryHome());

        object.remove(COUNTRY_HOME);
        assertFalse(object.contains(COUNTRY_HOME));
        assertFalse(object.containsCountryHome());

        // NICKNAME
        assertFalse(object.contains(NICKNAME));
        assertFalse(object.containsNickname());

        object.setNickname("Bla");
        assertTrue(object.contains(NICKNAME));
        assertTrue(object.containsNickname());
        assertEquals("Bla", object.get(NICKNAME));

        object.set(NICKNAME, "Blupp");
        assertEquals("Blupp", object.getNickname());

        object.remove(NICKNAME);
        assertFalse(object.contains(NICKNAME));
        assertFalse(object.containsNickname());

        // SUR_NAME
        assertFalse(object.contains(SUR_NAME));
        assertFalse(object.containsSurName());

        object.setSurName("Bla");
        assertTrue(object.contains(SUR_NAME));
        assertTrue(object.containsSurName());
        assertEquals("Bla", object.get(SUR_NAME));

        object.set(SUR_NAME, "Blupp");
        assertEquals("Blupp", object.getSurName());

        object.remove(SUR_NAME);
        assertFalse(object.contains(SUR_NAME));
        assertFalse(object.containsSurName());

        // CITY_BUSINESS
        assertFalse(object.contains(CITY_BUSINESS));
        assertFalse(object.containsCityBusiness());

        object.setCityBusiness("Bla");
        assertTrue(object.contains(CITY_BUSINESS));
        assertTrue(object.containsCityBusiness());
        assertEquals("Bla", object.get(CITY_BUSINESS));

        object.set(CITY_BUSINESS, "Blupp");
        assertEquals("Blupp", object.getCityBusiness());

        object.remove(CITY_BUSINESS);
        assertFalse(object.contains(CITY_BUSINESS));
        assertFalse(object.containsCityBusiness());

        // USERFIELD20
        assertFalse(object.contains(USERFIELD20));
        assertFalse(object.containsUserField20());

        object.setUserField20("Bla");
        assertTrue(object.contains(USERFIELD20));
        assertTrue(object.containsUserField20());
        assertEquals("Bla", object.get(USERFIELD20));

        object.set(USERFIELD20, "Blupp");
        assertEquals("Blupp", object.getUserField20());

        object.remove(USERFIELD20);
        assertFalse(object.contains(USERFIELD20));
        assertFalse(object.containsUserField20());

        // TELEPHONE_CALLBACK
        assertFalse(object.contains(TELEPHONE_CALLBACK));
        assertFalse(object.containsTelephoneCallback());

        object.setTelephoneCallback("Bla");
        assertTrue(object.contains(TELEPHONE_CALLBACK));
        assertTrue(object.containsTelephoneCallback());
        assertEquals("Bla", object.get(TELEPHONE_CALLBACK));

        object.set(TELEPHONE_CALLBACK, "Blupp");
        assertEquals("Blupp", object.getTelephoneCallback());

        object.remove(TELEPHONE_CALLBACK);
        assertFalse(object.contains(TELEPHONE_CALLBACK));
        assertFalse(object.containsTelephoneCallback());

        // USERFIELD17
        assertFalse(object.contains(USERFIELD17));
        assertFalse(object.containsUserField17());

        object.setUserField17("Bla");
        assertTrue(object.contains(USERFIELD17));
        assertTrue(object.containsUserField17());
        assertEquals("Bla", object.get(USERFIELD17));

        object.set(USERFIELD17, "Blupp");
        assertEquals("Blupp", object.getUserField17());

        object.remove(USERFIELD17);
        assertFalse(object.contains(USERFIELD17));
        assertFalse(object.containsUserField17());

        // TELEPHONE_PAGER
        assertFalse(object.contains(TELEPHONE_PAGER));
        assertFalse(object.containsTelephonePager());

        object.setTelephonePager("Bla");
        assertTrue(object.contains(TELEPHONE_PAGER));
        assertTrue(object.containsTelephonePager());
        assertEquals("Bla", object.get(TELEPHONE_PAGER));

        object.set(TELEPHONE_PAGER, "Blupp");
        assertEquals("Blupp", object.getTelephonePager());

        object.remove(TELEPHONE_PAGER);
        assertFalse(object.contains(TELEPHONE_PAGER));
        assertFalse(object.containsTelephonePager());

        // COUNTRY_OTHER
        assertFalse(object.contains(COUNTRY_OTHER));
        assertFalse(object.containsCountryOther());

        object.setCountryOther("Bla");
        assertTrue(object.contains(COUNTRY_OTHER));
        assertTrue(object.containsCountryOther());
        assertEquals("Bla", object.get(COUNTRY_OTHER));

        object.set(COUNTRY_OTHER, "Blupp");
        assertEquals("Blupp", object.getCountryOther());

        object.remove(COUNTRY_OTHER);
        assertFalse(object.contains(COUNTRY_OTHER));
        assertFalse(object.containsCountryOther());

        // TAX_ID
        assertFalse(object.contains(TAX_ID));
        assertFalse(object.containsTaxID());

        object.setTaxID("Bla");
        assertTrue(object.contains(TAX_ID));
        assertTrue(object.containsTaxID());
        assertEquals("Bla", object.get(TAX_ID));

        object.set(TAX_ID, "Blupp");
        assertEquals("Blupp", object.getTaxID());

        object.remove(TAX_ID);
        assertFalse(object.contains(TAX_ID));
        assertFalse(object.containsTaxID());

        // USERFIELD03
        assertFalse(object.contains(USERFIELD03));
        assertFalse(object.containsUserField03());

        object.setUserField03("Bla");
        assertTrue(object.contains(USERFIELD03));
        assertTrue(object.containsUserField03());
        assertEquals("Bla", object.get(USERFIELD03));

        object.set(USERFIELD03, "Blupp");
        assertEquals("Blupp", object.getUserField03());

        object.remove(USERFIELD03);
        assertFalse(object.contains(USERFIELD03));
        assertFalse(object.containsUserField03());

        // TELEPHONE_COMPANY
        assertFalse(object.contains(TELEPHONE_COMPANY));
        assertFalse(object.containsTelephoneCompany());

        object.setTelephoneCompany("Bla");
        assertTrue(object.contains(TELEPHONE_COMPANY));
        assertTrue(object.containsTelephoneCompany());
        assertEquals("Bla", object.get(TELEPHONE_COMPANY));

        object.set(TELEPHONE_COMPANY, "Blupp");
        assertEquals("Blupp", object.getTelephoneCompany());

        object.remove(TELEPHONE_COMPANY);
        assertFalse(object.contains(TELEPHONE_COMPANY));
        assertFalse(object.containsTelephoneCompany());

        // SUFFIX
        assertFalse(object.contains(SUFFIX));
        assertFalse(object.containsSuffix());

        object.setSuffix("Bla");
        assertTrue(object.contains(SUFFIX));
        assertTrue(object.containsSuffix());
        assertEquals("Bla", object.get(SUFFIX));

        object.set(SUFFIX, "Blupp");
        assertEquals("Blupp", object.getSuffix());

        object.remove(SUFFIX);
        assertFalse(object.contains(SUFFIX));
        assertFalse(object.containsSuffix());

        // FILE_AS
        assertFalse(object.contains(FILE_AS));
        assertFalse(object.containsFileAs());

        object.setFileAs("Bla");
        assertTrue(object.contains(FILE_AS));
        assertTrue(object.containsFileAs());
        assertEquals("Bla", object.get(FILE_AS));

        object.set(FILE_AS, "Blupp");
        assertEquals("Blupp", object.getFileAs());

        object.remove(FILE_AS);
        assertFalse(object.contains(FILE_AS));
        assertFalse(object.containsFileAs());

        // USERFIELD02
        assertFalse(object.contains(USERFIELD02));
        assertFalse(object.containsUserField02());

        object.setUserField02("Bla");
        assertTrue(object.contains(USERFIELD02));
        assertTrue(object.containsUserField02());
        assertEquals("Bla", object.get(USERFIELD02));

        object.set(USERFIELD02, "Blupp");
        assertEquals("Blupp", object.getUserField02());

        object.remove(USERFIELD02);
        assertFalse(object.contains(USERFIELD02));
        assertFalse(object.containsUserField02());

        // TELEPHONE_BUSINESS2
        assertFalse(object.contains(TELEPHONE_BUSINESS2));
        assertFalse(object.containsTelephoneBusiness2());

        object.setTelephoneBusiness2("Bla");
        assertTrue(object.contains(TELEPHONE_BUSINESS2));
        assertTrue(object.containsTelephoneBusiness2());
        assertEquals("Bla", object.get(TELEPHONE_BUSINESS2));

        object.set(TELEPHONE_BUSINESS2, "Blupp");
        assertEquals("Blupp", object.getTelephoneBusiness2());

        object.remove(TELEPHONE_BUSINESS2);
        assertFalse(object.contains(TELEPHONE_BUSINESS2));
        assertFalse(object.containsTelephoneBusiness2());

        // USERFIELD05
        assertFalse(object.contains(USERFIELD05));
        assertFalse(object.containsUserField05());

        object.setUserField05("Bla");
        assertTrue(object.contains(USERFIELD05));
        assertTrue(object.containsUserField05());
        assertEquals("Bla", object.get(USERFIELD05));

        object.set(USERFIELD05, "Blupp");
        assertEquals("Blupp", object.getUserField05());

        object.remove(USERFIELD05);
        assertFalse(object.contains(USERFIELD05));
        assertFalse(object.containsUserField05());

        // USERFIELD16
        assertFalse(object.contains(USERFIELD16));
        assertFalse(object.containsUserField16());

        object.setUserField16("Bla");
        assertTrue(object.contains(USERFIELD16));
        assertTrue(object.containsUserField16());
        assertEquals("Bla", object.get(USERFIELD16));

        object.set(USERFIELD16, "Blupp");
        assertEquals("Blupp", object.getUserField16());

        object.remove(USERFIELD16);
        assertFalse(object.contains(USERFIELD16));
        assertFalse(object.containsUserField16());

        // INFO
        assertFalse(object.contains(INFO));
        assertFalse(object.containsInfo());

        object.setInfo("Bla");
        assertTrue(object.contains(INFO));
        assertTrue(object.containsInfo());
        assertEquals("Bla", object.get(INFO));

        object.set(INFO, "Blupp");
        assertEquals("Blupp", object.getInfo());

        object.remove(INFO);
        assertFalse(object.contains(INFO));
        assertFalse(object.containsInfo());

        // COMPANY
        assertFalse(object.contains(COMPANY));
        assertFalse(object.containsCompany());

        object.setCompany("Bla");
        assertTrue(object.contains(COMPANY));
        assertTrue(object.containsCompany());
        assertEquals("Bla", object.get(COMPANY));

        object.set(COMPANY, "Blupp");
        assertEquals("Blupp", object.getCompany());

        object.remove(COMPANY);
        assertFalse(object.contains(COMPANY));
        assertFalse(object.containsCompany());

        // DISPLAY_NAME
        assertFalse(object.contains(DISPLAY_NAME));
        assertFalse(object.containsDisplayName());

        object.setDisplayName("Bla");
        assertTrue(object.contains(DISPLAY_NAME));
        assertTrue(object.containsDisplayName());
        assertEquals("Bla", object.get(DISPLAY_NAME));

        object.set(DISPLAY_NAME, "Blupp");
        assertEquals("Blupp", object.getDisplayName());

        object.remove(DISPLAY_NAME);
        assertFalse(object.contains(DISPLAY_NAME));
        assertFalse(object.containsDisplayName());

        // STREET_HOME
        assertFalse(object.contains(STREET_HOME));
        assertFalse(object.containsStreetHome());

        object.setStreetHome("Bla");
        assertTrue(object.contains(STREET_HOME));
        assertTrue(object.containsStreetHome());
        assertEquals("Bla", object.get(STREET_HOME));

        object.set(STREET_HOME, "Blupp");
        assertEquals("Blupp", object.getStreetHome());

        object.remove(STREET_HOME);
        assertFalse(object.contains(STREET_HOME));
        assertFalse(object.containsStreetHome());

        // ASSISTANT_NAME
        assertFalse(object.contains(ASSISTANT_NAME));
        assertFalse(object.containsAssistantName());

        object.setAssistantName("Bla");
        assertTrue(object.contains(ASSISTANT_NAME));
        assertTrue(object.containsAssistantName());
        assertEquals("Bla", object.get(ASSISTANT_NAME));

        object.set(ASSISTANT_NAME, "Blupp");
        assertEquals("Blupp", object.getAssistantName());

        object.remove(ASSISTANT_NAME);
        assertFalse(object.contains(ASSISTANT_NAME));
        assertFalse(object.containsAssistantName());

        // TELEPHONE_CAR
        assertFalse(object.contains(TELEPHONE_CAR));
        assertFalse(object.containsTelephoneCar());

        object.setTelephoneCar("Bla");
        assertTrue(object.contains(TELEPHONE_CAR));
        assertTrue(object.containsTelephoneCar());
        assertEquals("Bla", object.get(TELEPHONE_CAR));

        object.set(TELEPHONE_CAR, "Blupp");
        assertEquals("Blupp", object.getTelephoneCar());

        object.remove(TELEPHONE_CAR);
        assertFalse(object.contains(TELEPHONE_CAR));
        assertFalse(object.containsTelephoneCar());

        // POSITION
        assertFalse(object.contains(POSITION));
        assertFalse(object.containsPosition());

        object.setPosition("Bla");
        assertTrue(object.contains(POSITION));
        assertTrue(object.containsPosition());
        assertEquals("Bla", object.get(POSITION));

        object.set(POSITION, "Blupp");
        assertEquals("Blupp", object.getPosition());

        object.remove(POSITION);
        assertFalse(object.contains(POSITION));
        assertFalse(object.containsPosition());

        // TELEPHONE_PRIMARY
        assertFalse(object.contains(TELEPHONE_PRIMARY));
        assertFalse(object.containsTelephonePrimary());

        object.setTelephonePrimary("Bla");
        assertTrue(object.contains(TELEPHONE_PRIMARY));
        assertTrue(object.containsTelephonePrimary());
        assertEquals("Bla", object.get(TELEPHONE_PRIMARY));

        object.set(TELEPHONE_PRIMARY, "Blupp");
        assertEquals("Blupp", object.getTelephonePrimary());

        object.remove(TELEPHONE_PRIMARY);
        assertFalse(object.contains(TELEPHONE_PRIMARY));
        assertFalse(object.containsTelephonePrimary());

        // SPOUSE_NAME
        assertFalse(object.contains(SPOUSE_NAME));
        assertFalse(object.containsSpouseName());

        object.setSpouseName("Bla");
        assertTrue(object.contains(SPOUSE_NAME));
        assertTrue(object.containsSpouseName());
        assertEquals("Bla", object.get(SPOUSE_NAME));

        object.set(SPOUSE_NAME, "Blupp");
        assertEquals("Blupp", object.getSpouseName());

        object.remove(SPOUSE_NAME);
        assertFalse(object.contains(SPOUSE_NAME));
        assertFalse(object.containsSpouseName());

        // IMAGE_LAST_MODIFIED
        assertFalse(object.contains(IMAGE_LAST_MODIFIED));
        assertFalse(object.containsImageLastModified());

        object.setImageLastModified(new Date(42));
        assertTrue(object.contains(IMAGE_LAST_MODIFIED));
        assertTrue(object.containsImageLastModified());
        assertEquals(new Date(42), object.get(IMAGE_LAST_MODIFIED));

        object.set(IMAGE_LAST_MODIFIED, new Date(23));
        assertEquals(new Date(23), object.getImageLastModified());

        object.remove(IMAGE_LAST_MODIFIED);
        assertFalse(object.contains(IMAGE_LAST_MODIFIED));
        assertFalse(object.containsImageLastModified());

        // INSTANT_MESSENGER2
        assertFalse(object.contains(INSTANT_MESSENGER2));
        assertFalse(object.containsInstantMessenger2());

        object.setInstantMessenger2("Bla");
        assertTrue(object.contains(INSTANT_MESSENGER2));
        assertTrue(object.containsInstantMessenger2());
        assertEquals("Bla", object.get(INSTANT_MESSENGER2));

        object.set(INSTANT_MESSENGER2, "Blupp");
        assertEquals("Blupp", object.getInstantMessenger2());

        object.remove(INSTANT_MESSENGER2);
        assertFalse(object.contains(INSTANT_MESSENGER2));
        assertFalse(object.containsInstantMessenger2());

        // IMAGE1
        assertFalse(object.contains(IMAGE1));
        assertFalse(object.containsImage1());

        object.setImage1(new byte[] { 1, 2, 3 });
        assertTrue(object.contains(IMAGE1));
        assertTrue(object.containsImage1());
        assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, (byte[]) object.get(IMAGE1)));

        object.set(IMAGE1, new byte[] { 3, 2, 1 });
        assertTrue(Arrays.equals(new byte[] { 3, 2, 1 }, object.getImage1()));

        object.remove(IMAGE1);
        assertFalse(object.contains(IMAGE1));
        assertFalse(object.containsImage1());

        // TELEPHONE_BUSINESS1
        assertFalse(object.contains(TELEPHONE_BUSINESS1));
        assertFalse(object.containsTelephoneBusiness1());

        object.setTelephoneBusiness1("Bla");
        assertTrue(object.contains(TELEPHONE_BUSINESS1));
        assertTrue(object.containsTelephoneBusiness1());
        assertEquals("Bla", object.get(TELEPHONE_BUSINESS1));

        object.set(TELEPHONE_BUSINESS1, "Blupp");
        assertEquals("Blupp", object.getTelephoneBusiness1());

        object.remove(TELEPHONE_BUSINESS1);
        assertFalse(object.contains(TELEPHONE_BUSINESS1));
        assertFalse(object.containsTelephoneBusiness1());

        // MARK_AS_DISTRIBUTIONLIST
        assertFalse(object.contains(MARK_AS_DISTRIBUTIONLIST));
        assertFalse(object.containsMarkAsDistributionlist());

        object.setMarkAsDistributionlist(true);
        assertTrue(object.contains(MARK_AS_DISTRIBUTIONLIST));
        assertTrue(object.containsMarkAsDistributionlist());
        assertEquals(true, object.get(MARK_AS_DISTRIBUTIONLIST));

        object.set(MARK_AS_DISTRIBUTIONLIST, false);
        assertEquals(false, object.getMarkAsDistribtuionlist());

        object.remove(MARK_AS_DISTRIBUTIONLIST);
        assertFalse(object.contains(MARK_AS_DISTRIBUTIONLIST));
        assertFalse(object.containsMarkAsDistributionlist());

    }
}
