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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.contacts.ldap.contacts;

import java.util.Date;
import java.util.Set;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.groupware.container.ContactObject;


/**
 * This class is used to map the ldap attributes to the attributes of the OX contact object
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Mapper {

    public static ContactObject getContact(final LdapGetter getter, Set<Integer> cols, UidInterface uidInterface) throws LdapException {
        final PropertyHandler instance = PropertyHandler.getInstance();
        final ContactObject retval = new ContactObject();
        if (cols.contains(ContactObject.OBJECT_ID)) {
            final String uniqueid = instance.getUniqueid();
            if (0 != uniqueid.length()) {
                if (PropertyHandler.getInstance().isMemorymapping()) {
                    retval.setObjectID(uidInterface.getUid(getter.getAttribute(uniqueid)));
                } else {
                    retval.setObjectID(getter.getIntAttribute(uniqueid));
                }
            }
        }
        if (cols.contains(ContactObject.DISPLAY_NAME)) {
            final String displayname = instance.getDisplayname();
            if (0 != displayname.length()) {
                retval.setDisplayName(getter.getAttribute(displayname));
            }
        }
        if (cols.contains(ContactObject.GIVEN_NAME)) {
            final String givenname = instance.getGivenname();
            if (0 != givenname.length()) {
                retval.setGivenName(getter.getAttribute(givenname));
            }
        }
        if (cols.contains(ContactObject.SUR_NAME)) {
            final String surname = instance.getSurname();
            if (0 != surname.length()) {
                retval.setSurName(getter.getAttribute(surname));
            }
        }
        if (cols.contains(ContactObject.EMAIL1)) {
            final String email1 = instance.getEmail1();
            if (0 != email1.length()) {
                retval.setEmail1(getter.getAttribute(email1));
            }
        }
        if (cols.contains(ContactObject.DEPARTMENT)) {
            final String department = instance.getDepartment();
            if (0 != department.length()) {
                retval.setDepartment(getter.getAttribute(department));
            }
        }
        if (cols.contains(ContactObject.COMPANY)) {
            final String company = instance.getCompany();
            if (0 != company.length()) {
                retval.setCompany(getter.getAttribute(company));
            }
        }
        if (cols.contains(ContactObject.BIRTHDAY)) {
            final String birthday = instance.getBirthday();
            if (0 != birthday.length()) {
                retval.setBirthday(getter.getDateAttribute(birthday));
            }
        }
        if (cols.contains(ContactObject.ANNIVERSARY)) {
            final String anniversary = instance.getAnniversary();
            if (0 != anniversary.length()) {
                retval.setAnniversary(getter.getDateAttribute(anniversary));
            }
        }
        if (cols.contains(ContactObject.BRANCHES)) {
            final String branches = instance.getBranches();
            if (0 != branches.length()) {
                retval.setBranches(getter.getAttribute(branches));
            }
        }
        if (cols.contains(ContactObject.BUSINESS_CATEGORY)) {
            final String business_category = instance.getBusiness_category();
            if (0 != business_category.length()) {
                retval.setBusinessCategory(getter.getAttribute(business_category));
            }
        }
        if (cols.contains(ContactObject.POSTAL_CODE_BUSINESS)) {
            final String postal_code_business = instance.getPostal_code_business();
            if (0 != postal_code_business.length()) {
                retval.setPostalCodeBusiness(getter.getAttribute(postal_code_business));
            }
        }
        if (cols.contains(ContactObject.STATE_BUSINESS)) {
            final String state_business = instance.getState_business();
            if (0 != state_business.length()) {
                retval.setStateBusiness(getter.getAttribute(state_business));
            }
        }
        if (cols.contains(ContactObject.STREET_BUSINESS)) {
            final String street_business = instance.getStreet_business();
            if (0 != street_business.length()) {
                retval.setStreetBusiness(getter.getAttribute(street_business));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_CALLBACK)) {
            final String telephone_callback = instance.getTelephone_callback();
            if (0 != telephone_callback.length()) {
                retval.setTelephoneCallback(getter.getAttribute(telephone_callback));
            }
        }
        if (cols.contains(ContactObject.CITY_HOME)) {
            final String city_home = instance.getCity_home();
            if (0 != city_home.length()) {
                retval.setCityHome(getter.getAttribute(city_home));
            }
        }
        if (cols.contains(ContactObject.COMMERCIAL_REGISTER)) {
            final String commercial_register = instance.getCommercial_register();
            if (0 != commercial_register.length()) {
                retval.setCommercialRegister(getter.getAttribute(commercial_register));
            }
        }
        if (cols.contains(ContactObject.COUNTRY_HOME)) {
            final String country_home = instance.getCountry_home();
            if (0 != country_home.length()) {
                retval.setCountryHome(getter.getAttribute(country_home));
            }
        }
        if (cols.contains(ContactObject.EMAIL2)) {
            final String email2 = instance.getEmail2();
            if (0 != email2.length()) {
                retval.setEmail2(getter.getAttribute(email2));
            }
        }
        if (cols.contains(ContactObject.EMAIL3)) {
            final String email3 = instance.getEmail3();
            if (0 != email3.length()) {
                retval.setEmail3(getter.getAttribute(email3));
            }
        }
        if (cols.contains(ContactObject.EMPLOYEE_TYPE)) {
            final String employeetype = instance.getEmployeetype();
            if (0 != employeetype.length()) {
                retval.setEmployeeType(getter.getAttribute(employeetype));
            }
        }
        if (cols.contains(ContactObject.FAX_BUSINESS)) {
            final String fax_business = instance.getFax_business();
            if (0 != fax_business.length()) {
                retval.setFaxBusiness(getter.getAttribute(fax_business));
            }
        }
        if (cols.contains(ContactObject.FAX_HOME)) {
            final String fax_home = instance.getFax_home();
            if (0 != fax_home.length()) {
                retval.setFaxHome(getter.getAttribute(fax_home));
            }
        }
        if (cols.contains(ContactObject.FAX_OTHER)) {
            final String fax_other = instance.getFax_other();
            if (0 != fax_other.length()) {
                retval.setFaxOther(getter.getAttribute(fax_other));
            }
        }
        if (cols.contains(ContactObject.INSTANT_MESSENGER1)) {
            final String instant_messenger1 = instance.getInstant_messenger1();
            if (0 != instant_messenger1.length()) {
                retval.setInstantMessenger1(getter.getAttribute(instant_messenger1));
            }
        }
        if (cols.contains(ContactObject.INSTANT_MESSENGER2)) {
            final String instant_messenger2 = instance.getInstant_messenger2();
            if (0 != instant_messenger2.length()) {
                retval.setInstantMessenger2(getter.getAttribute(instant_messenger2));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_IP)) {
            final String telephone_ip = instance.getTelephone_ip();
            if (0 != telephone_ip.length()) {
                retval.setTelephoneIP(getter.getAttribute(telephone_ip));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_ISDN)) {
            final String telephone_isdn = instance.getTelephone_isdn();
            if (0 != telephone_isdn.length()) {
                retval.setTelephoneISDN(getter.getAttribute(telephone_isdn));
            }
        }
        if (cols.contains(ContactObject.MANAGER_NAME)) {
            final String manager_name = instance.getManager_name();
            if (0 != manager_name.length()) {
                retval.setManagerName(getter.getAttribute(manager_name));
            }
        }
        if (cols.contains(ContactObject.MARITAL_STATUS)) {
            final String marital_status = instance.getMarital_status();
            if (0 != marital_status.length()) {
                retval.setMaritalStatus(getter.getAttribute(marital_status));
            }
        }
        if (cols.contains(ContactObject.CELLULAR_TELEPHONE1)) {
            final String cellular_telephone1 = instance.getCellular_telephone1();
            if (0 != cellular_telephone1.length()) {
                retval.setCellularTelephone1(getter.getAttribute(cellular_telephone1));
            }
        }
        if (cols.contains(ContactObject.CELLULAR_TELEPHONE2)) {
            final String cellular_telephone2 = instance.getCellular_telephone2();
            if (0 != cellular_telephone2.length()) {
                retval.setCellularTelephone2(getter.getAttribute(cellular_telephone2));
            }
        }
        if (cols.contains(ContactObject.INFO)) {
            final String info = instance.getInfo();
            if (0 != info.length()) {
                retval.setInfo(getter.getAttribute(info));
            }
        }
        if (cols.contains(ContactObject.NICKNAME)) {
            final String nickname = instance.getNickname();
            if (0 != nickname.length()) {
                retval.setNickname(getter.getAttribute(nickname));
            }
        }
        if (cols.contains(ContactObject.NUMBER_OF_CHILDREN)) {
            final String number_of_children = instance.getNumber_of_children();
            if (0 != number_of_children.length()) {
                retval.setNumberOfChildren(getter.getAttribute(number_of_children));
            }
        }
        if (cols.contains(ContactObject.NOTE)) {
            final String note = instance.getNote();
            if (0 != note.length()) {
                retval.setNote(getter.getAttribute(note));
            }
        }
        if (cols.contains(ContactObject.NUMBER_OF_EMPLOYEE)) {
            final String number_of_employee = instance.getNumber_of_employee();
            if (0 != number_of_employee.length()) {
                retval.setNumberOfEmployee(getter.getAttribute(number_of_employee));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_PAGER)) {
            final String telephone_pager = instance.getTelephone_pager();
            if (0 != telephone_pager.length()) {
                retval.setTelephonePager(getter.getAttribute(telephone_pager));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_ASSISTANT)) {
            final String telephone_assistant = instance.getTelephone_assistant();
            if (0 != telephone_assistant.length()) {
                retval.setTelephoneAssistant(getter.getAttribute(telephone_assistant));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_BUSINESS1)) {
            final String telephone_business1 = instance.getTelephone_business1();
            if (0 != telephone_business1.length()) {
                retval.setTelephoneBusiness1(getter.getAttribute(telephone_business1));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_BUSINESS2)) {
            final String telephone_business2 = instance.getTelephone_business2();
            if (0 != telephone_business2.length()) {
                retval.setTelephoneBusiness2(getter.getAttribute(telephone_business2));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_CAR)) {
            final String telephone_car = instance.getTelephone_car();
            if (0 != telephone_car.length()) {
                retval.setTelephoneCar(getter.getAttribute(telephone_car));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_COMPANY)) {
            final String telephone_company = instance.getTelephone_company();
            if (0 != telephone_company.length()) {
                retval.setTelephoneCompany(getter.getAttribute(telephone_company));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_HOME1)) {
            final String telephone_home1 = instance.getTelephone_home1();
            if (0 != telephone_home1.length()) {
                retval.setTelephoneHome1(getter.getAttribute(telephone_home1));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_HOME2)) {
            final String telephone_home2 = instance.getTelephone_home2();
            if (0 != telephone_home2.length()) {
                retval.setTelephoneHome2(getter.getAttribute(telephone_home2));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_OTHER)) {
            final String telephone_other = instance.getTelephone_other();
            if (0 != telephone_other.length()) {
                retval.setTelephoneOther(getter.getAttribute(telephone_other));
            }
        }
        if (cols.contains(ContactObject.POSTAL_CODE_HOME)) {
            final String postal_code_home = instance.getPostal_code_home();
            if (0 != postal_code_home.length()) {
                retval.setPostalCodeHome(getter.getAttribute(postal_code_home));
            }
        }
        if (cols.contains(ContactObject.PROFESSION)) {
            final String profession = instance.getProfession();
            if (0 != profession.length()) {
                retval.setProfession(getter.getAttribute(profession));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_RADIO)) {
            final String telephone_radio = instance.getTelephone_radio();
            if (0 != telephone_radio.length()) {
                retval.setTelephoneRadio(getter.getAttribute(telephone_radio));
            }
        }
        if (cols.contains(ContactObject.ROOM_NUMBER)) {
            final String room_number = instance.getRoom_number();
            if (0 != room_number.length()) {
                retval.setRoomNumber(getter.getAttribute(room_number));
            }
        }
        if (cols.contains(ContactObject.SALES_VOLUME)) {
            final String sales_volume = instance.getSales_volume();
            if (0 != sales_volume.length()) {
                retval.setSalesVolume(getter.getAttribute(sales_volume));
            }
        }
        if (cols.contains(ContactObject.CITY_OTHER)) {
            final String city_other = instance.getCity_other();
            if (0 != city_other.length()) {
                retval.setCityOther(getter.getAttribute(city_other));
            }
        }
        if (cols.contains(ContactObject.COUNTRY_OTHER)) {
            final String country_other = instance.getCountry_other();
            if (0 != country_other.length()) {
                retval.setCountryOther(getter.getAttribute(country_other));
            }
        }
        if (cols.contains(ContactObject.MIDDLE_NAME)) {
            final String middle_name = instance.getMiddle_name();
            if (0 != middle_name.length()) {
                retval.setMiddleName(getter.getAttribute(middle_name));
            }
        }
        if (cols.contains(ContactObject.POSTAL_CODE_OTHER)) {
            final String postal_code_other = instance.getPostal_code_other();
            if (0 != postal_code_other.length()) {
                retval.setPostalCodeOther(getter.getAttribute(postal_code_other));
            }
        }
        if (cols.contains(ContactObject.STATE_OTHER)) {
            final String state_other = instance.getState_other();
            if (0 != state_other.length()) {
                retval.setStateOther(getter.getAttribute(state_other));
            }
        }
        if (cols.contains(ContactObject.STREET_OTHER)) {
            final String street_other = instance.getStreet_other();
            if (0 != street_other.length()) {
                retval.setStreetOther(getter.getAttribute(street_other));
            }
        }
        if (cols.contains(ContactObject.SPOUSE_NAME)) {
            final String spouse_name = instance.getSpouse_name();
            if (0 != spouse_name.length()) {
                retval.setSpouseName(getter.getAttribute(spouse_name));
            }
        }
        if (cols.contains(ContactObject.STATE_HOME)) {
            final String state_home = instance.getState_home();
            if (0 != state_home.length()) {
                retval.setStateHome(getter.getAttribute(state_home));
            }
        }
        if (cols.contains(ContactObject.STREET_HOME)) {
            final String street_home = instance.getStreet_home();
            if (0 != street_home.length()) {
                retval.setStreetHome(getter.getAttribute(street_home));
            }
        }
        if (cols.contains(ContactObject.SUFFIX)) {
            final String suffix = instance.getSuffix();
            if (0 != suffix.length()) {
                retval.setSuffix(getter.getAttribute(suffix));
            }
        }
        if (cols.contains(ContactObject.TAX_ID)) {
            final String tax_id = instance.getTax_id();
            if (0 != tax_id.length()) {
                retval.setTaxID(getter.getAttribute(tax_id));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_TELEX)) {
            final String telephone_telex = instance.getTelephone_telex();
            if (0 != telephone_telex.length()) {
                retval.setTelephoneTelex(getter.getAttribute(telephone_telex));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_TTYTDD)) {
            final String telephone_ttytdd = instance.getTelephone_ttytdd();
            if (0 != telephone_ttytdd.length()) {
                retval.setTelephoneTTYTTD(getter.getAttribute(telephone_ttytdd));
            }
        }
        if (cols.contains(ContactObject.URL)) {
            final String url = instance.getUrl();
            if (0 != url.length()) {
                retval.setURL(getter.getAttribute(url));
            }
        }
        if (cols.contains(ContactObject.USERFIELD01)) {
            final String userfield01 = instance.getUserfield01();
            if (0 != userfield01.length()) {
                retval.setUserField01(getter.getAttribute(userfield01));
            }
        }
        if (cols.contains(ContactObject.USERFIELD02)) {
            final String userfield02 = instance.getUserfield02();
            if (0 != userfield02.length()) {
                retval.setUserField02(getter.getAttribute(userfield02));
            }
        }
        if (cols.contains(ContactObject.USERFIELD03)) {
            final String userfield03 = instance.getUserfield03();
            if (0 != userfield03.length()) {
                retval.setUserField03(getter.getAttribute(userfield03));
            }
        }
        if (cols.contains(ContactObject.USERFIELD04)) {
            final String userfield04 = instance.getUserfield04();
            if (0 != userfield04.length()) {
                retval.setUserField04(getter.getAttribute(userfield04));
            }
        }
        if (cols.contains(ContactObject.USERFIELD05)) {
            final String userfield05 = instance.getUserfield05();
            if (0 != userfield05.length()) {
                retval.setUserField05(getter.getAttribute(userfield05));
            }
        }
        if (cols.contains(ContactObject.USERFIELD06)) {
            final String userfield06 = instance.getUserfield06();
            if (0 != userfield06.length()) {
                retval.setUserField06(getter.getAttribute(userfield06));
            }
        }
        if (cols.contains(ContactObject.USERFIELD07)) {
            final String userfield07 = instance.getUserfield07();
            if (0 != userfield07.length()) {
                retval.setUserField07(getter.getAttribute(userfield07));
            }
        }
        if (cols.contains(ContactObject.USERFIELD08)) {
            final String userfield08 = instance.getUserfield08();
            if (0 != userfield08.length()) {
                retval.setUserField08(getter.getAttribute(userfield08));
            }
        }
        if (cols.contains(ContactObject.USERFIELD09)) {
            final String userfield09 = instance.getUserfield09();
            if (0 != userfield09.length()) {
                retval.setUserField09(getter.getAttribute(userfield09));
            }
        }
        if (cols.contains(ContactObject.USERFIELD10)) {
            final String userfield10 = instance.getUserfield10();
            if (0 != userfield10.length()) {
                retval.setUserField10(getter.getAttribute(userfield10));
            }
        }
        if (cols.contains(ContactObject.USERFIELD11)) {
            final String userfield11 = instance.getUserfield11();
            if (0 != userfield11.length()) {
                retval.setUserField11(getter.getAttribute(userfield11));
            }
        }
        if (cols.contains(ContactObject.USERFIELD12)) {
            final String userfield12 = instance.getUserfield12();
            if (0 != userfield12.length()) {
                retval.setUserField12(getter.getAttribute(userfield12));
            }
        }
        if (cols.contains(ContactObject.USERFIELD13)) {
            final String userfield13 = instance.getUserfield13();
            if (0 != userfield13.length()) {
                retval.setUserField13(getter.getAttribute(userfield13));
            }
        }
        if (cols.contains(ContactObject.USERFIELD14)) {
            final String userfield14 = instance.getUserfield14();
            if (0 != userfield14.length()) {
                retval.setUserField14(getter.getAttribute(userfield14));
            }
        }
        if (cols.contains(ContactObject.USERFIELD15)) {
            final String userfield15 = instance.getUserfield15();
            if (0 != userfield15.length()) {
                retval.setUserField15(getter.getAttribute(userfield15));
            }
        }
        if (cols.contains(ContactObject.USERFIELD16)) {
            final String userfield16 = instance.getUserfield16();
            if (0 != userfield16.length()) {
                retval.setUserField16(getter.getAttribute(userfield16));
            }
        }
        if (cols.contains(ContactObject.USERFIELD17)) {
            final String userfield17 = instance.getUserfield17();
            if (0 != userfield17.length()) {
                retval.setUserField17(getter.getAttribute(userfield17));
            }
        }
        if (cols.contains(ContactObject.USERFIELD18)) {
            final String userfield18 = instance.getUserfield18();
            if (0 != userfield18.length()) {
                retval.setUserField18(getter.getAttribute(userfield18));
            }
        }
        if (cols.contains(ContactObject.USERFIELD19)) {
            final String userfield19 = instance.getUserfield19();
            if (0 != userfield19.length()) {
                retval.setUserField19(getter.getAttribute(userfield19));
            }
        }
        if (cols.contains(ContactObject.USERFIELD20)) {
            final String userfield20 = instance.getUserfield20();
            if (0 != userfield20.length()) {
                retval.setUserField20(getter.getAttribute(userfield20));
            }
        }
        if (cols.contains(ContactObject.CITY_BUSINESS)) {
            final String city_business = instance.getCity_business();
            if (0 != city_business.length()) {
                retval.setCityBusiness(getter.getAttribute(city_business));
            }
        }
        if (cols.contains(ContactObject.COUNTRY_BUSINESS)) {
            final String country_business = instance.getCountry_business();
            if (0 != country_business.length()) {
                retval.setCountryBusiness(getter.getAttribute(country_business));
            }
        }
        if (cols.contains(ContactObject.ASSISTANT_NAME)) {
            final String assistant_name = instance.getAssistant_name();
            if (0 != assistant_name.length()) {
                retval.setAssistantName(getter.getAttribute(assistant_name));
            }
        }
        if (cols.contains(ContactObject.TELEPHONE_PRIMARY)) {
            final String telephone_primary = instance.getTelephone_primary();
            if (0 != telephone_primary.length()) {
                retval.setTelephonePrimary(getter.getAttribute(telephone_primary));
            }
        }
        if (cols.contains(ContactObject.CATEGORIES)) {
            final String categories = instance.getCategories();
            if (0 != categories.length()) {
                retval.setCategories(getter.getAttribute(categories));
            }
        }
        if (cols.contains(ContactObject.DEFAULT_ADDRESS)) {
            final String defaultaddress = instance.getDefaultaddress();
            if (0 != defaultaddress.length()) {
                retval.setDefaultAddress(getter.getIntAttribute(defaultaddress));
            }
        }
        if (cols.contains(ContactObject.TITLE)) {
            final String title = instance.getTitle();
            if (0 != title.length()) {
                retval.setTitle(getter.getAttribute(title));
            }
        }
        if (cols.contains(ContactObject.POSITION)) {
            final String position = instance.getPosition();
            if (0 != position.length()) {
                retval.setPosition(getter.getAttribute(position));
            }
        }
        // Finally we add the timestamps here
        if (cols.contains(ContactObject.LAST_MODIFIED)) {
            // TODO Fetch it through operational attributes
            retval.setLastModified(new Date());
        }
        if (cols.contains(ContactObject.CREATION_DATE)) {
            // TODO Fetch it through operational attributes
            retval.setCreationDate(new Date());
        }
        return retval;
    }
}
