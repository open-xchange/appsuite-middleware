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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.contacts.ldap.exceptions.LdapExceptionCode;
import com.openexchange.contacts.ldap.ldap.LdapGetter;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;


/**
 * This class is used to map the ldap attributes to the attributes of the OX contact object
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Mapper {

    protected static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Mapper.class));

    public interface SetterDateClosure {

        void set(Date attribute);

    }

    public interface SetterIntClosure {

        void set(final int attribute);

    }

    public interface SetterClosure {

        void set(final String attribute);

    }

    public static Contact getContact(final LdapGetter getter, final Set<Integer> cols, final FolderProperties folderprop, final UidInterface uidInterface, final int folderid, final int adminid) throws OXException {
        final Mappings mappings = folderprop.getMappings();
        final Contact retval = new Contact();
        if (cols.contains(DataObject.OBJECT_ID)) {
            final String uniqueid = mappings.getUniqueid();
            if (null != uniqueid && 0 != uniqueid.length()) {
                if (folderprop.isMemorymapping()) {
                    final String uidattribute = getter.getAttribute(uniqueid);
                    if (null == uidattribute) {
                        throw LdapExceptionCode.MISSING_ATTRIBUTE.create(uniqueid, getter.getObjectFullName());
                    }
                    retval.setObjectID(uidInterface.getUid(uidattribute));
                } else {
                    final int uidIntAttribute = getter.getIntAttribute(uniqueid);
                    if (-1 == uidIntAttribute) {
                        throw LdapExceptionCode.MISSING_ATTRIBUTE.create(uniqueid, getter.getObjectFullName());
                    }
                    retval.setObjectID(uidIntAttribute);
                }
            }
        }
        final ParameterObject parameterObject = new ParameterObject(getter, cols);
        stringSetter(parameterObject, mappings.getDisplayname(), Contact.DISPLAY_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setDisplayName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getGivenname(), Contact.GIVEN_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setGivenName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getSurname(), Contact.SUR_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setSurName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getEmail1(), Contact.EMAIL1, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setEmail1(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getDepartment(), Contact.DEPARTMENT, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setDepartment(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCompany(), Contact.COMPANY, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCompany(attribute);
            }
        });
        dateSetter(parameterObject, mappings.getBirthday(), Contact.BIRTHDAY, new SetterDateClosure() {
            @Override
            public void set(final Date attribute) {
                retval.setBirthday(attribute);
            }
        });
        dateSetter(parameterObject, mappings.getAnniversary(), Contact.ANNIVERSARY, new SetterDateClosure() {
            @Override
            public void set(final Date attribute) {
                retval.setAnniversary(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getBranches(), Contact.BRANCHES, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setBranches(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getBusiness_category(), Contact.BUSINESS_CATEGORY, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setBusinessCategory(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getPostal_code_business(), Contact.POSTAL_CODE_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setPostalCodeBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getState_business(), Contact.STATE_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStateBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getStreet_business(), Contact.STREET_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStreetBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_callback(), Contact.TELEPHONE_CALLBACK, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneCallback(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCity_home(), Contact.CITY_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCityHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCommercial_register(), Contact.COMMERCIAL_REGISTER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCommercialRegister(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCountry_home(), Contact.COUNTRY_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCountryHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getEmail2(), Contact.EMAIL2, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setEmail2(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getEmail3(), Contact.EMAIL3, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setEmail3(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getEmployeetype(), Contact.EMPLOYEE_TYPE, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setEmployeeType(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getFax_business(), Contact.FAX_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setFaxBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getFax_home(), Contact.FAX_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setFaxHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getFax_other(), Contact.FAX_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setFaxOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getInstant_messenger1(), Contact.INSTANT_MESSENGER1, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setInstantMessenger1(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getInstant_messenger2(), Contact.INSTANT_MESSENGER2, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setInstantMessenger2(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_ip(), Contact.TELEPHONE_IP, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneIP(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_isdn(), Contact.TELEPHONE_ISDN, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneISDN(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getManager_name(), Contact.MANAGER_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setManagerName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getMarital_status(), Contact.MARITAL_STATUS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setMaritalStatus(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCellular_telephone1(), Contact.CELLULAR_TELEPHONE1, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCellularTelephone1(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCellular_telephone2(), Contact.CELLULAR_TELEPHONE2, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCellularTelephone2(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getInfo(), Contact.INFO, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setInfo(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getNickname(), Contact.NICKNAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setNickname(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getNumber_of_children(), Contact.NUMBER_OF_CHILDREN, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setNumberOfChildren(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getNote(), Contact.NOTE, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setNote(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getNumber_of_employee(), Contact.NUMBER_OF_EMPLOYEE, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setNumberOfEmployee(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_pager(), Contact.TELEPHONE_PAGER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephonePager(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_assistant(), Contact.TELEPHONE_ASSISTANT, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneAssistant(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_business1(), Contact.TELEPHONE_BUSINESS1, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneBusiness1(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_business2(), Contact.TELEPHONE_BUSINESS2, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneBusiness2(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_car(), Contact.TELEPHONE_CAR, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneCar(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_company(), Contact.TELEPHONE_COMPANY, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneCompany(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_home1(), Contact.TELEPHONE_HOME1, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneHome1(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_home2(), Contact.TELEPHONE_HOME2, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneHome2(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_other(), Contact.TELEPHONE_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getPostal_code_home(), Contact.POSTAL_CODE_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setPostalCodeHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getProfession(), Contact.PROFESSION, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setProfession(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_radio(), Contact.TELEPHONE_RADIO, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneRadio(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getRoom_number(), Contact.ROOM_NUMBER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setRoomNumber(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getSales_volume(), Contact.SALES_VOLUME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setSalesVolume(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCity_other(), Contact.CITY_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCityOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCountry_other(), Contact.COUNTRY_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCountryOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getMiddle_name(), Contact.MIDDLE_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setMiddleName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getPostal_code_other(), Contact.POSTAL_CODE_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setPostalCodeOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getState_other(), Contact.STATE_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStateOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getStreet_other(), Contact.STREET_OTHER, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStreetOther(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getSpouse_name(), Contact.SPOUSE_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setSpouseName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getState_home(), Contact.STATE_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStateHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getStreet_home(), Contact.STREET_HOME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setStreetHome(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getSuffix(), Contact.SUFFIX, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setSuffix(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTax_id(), Contact.TAX_ID, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTaxID(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_telex(), Contact.TELEPHONE_TELEX, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneTelex(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_ttytdd(), Contact.TELEPHONE_TTYTDD, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephoneTTYTTD(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUrl(), Contact.URL, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setURL(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield01(), Contact.USERFIELD01, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField01(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield02(), Contact.USERFIELD02, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField02(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield03(), Contact.USERFIELD03, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField03(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield04(), Contact.USERFIELD04, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField04(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield05(), Contact.USERFIELD05, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField05(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield06(), Contact.USERFIELD06, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField06(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield07(), Contact.USERFIELD07, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField07(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield08(), Contact.USERFIELD08, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField08(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield09(), Contact.USERFIELD09, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField09(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield10(), Contact.USERFIELD10, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField10(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield11(), Contact.USERFIELD11, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField11(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield12(), Contact.USERFIELD12, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField12(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield13(), Contact.USERFIELD13, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField13(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield14(), Contact.USERFIELD14, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField14(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield15(), Contact.USERFIELD15, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField15(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield16(), Contact.USERFIELD16, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField16(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield17(), Contact.USERFIELD17, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField17(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield18(), Contact.USERFIELD18, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField18(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield19(), Contact.USERFIELD19, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField19(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getUserfield20(), Contact.USERFIELD20, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setUserField20(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCity_business(), Contact.CITY_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCityBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCountry_business(), Contact.COUNTRY_BUSINESS, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCountryBusiness(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getAssistant_name(), Contact.ASSISTANT_NAME, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setAssistantName(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTelephone_primary(), Contact.TELEPHONE_PRIMARY, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTelephonePrimary(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getCategories(), CommonObject.CATEGORIES, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setCategories(attribute);
            }
        });
        intSetter(parameterObject, mappings.getDefaultaddress(), Contact.DEFAULT_ADDRESS, new SetterIntClosure() {
            @Override
            public void set(final int attribute) {
                retval.setDefaultAddress(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getTitle(), Contact.TITLE, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setTitle(attribute);
            }
        });
        stringSetter(parameterObject, mappings.getPosition(), Contact.POSITION, new SetterClosure() {
            @Override
            public void set(final String attribute) {
                retval.setPosition(attribute);
            }
        });

        commonParts(cols, folderid, adminid, retval, mappings, getter);

        return retval;
    }

    public static final class ParameterObject {

        private final LdapGetter m_getter;

        private final Set<Integer> m_cols;

        public ParameterObject(final LdapGetter getter, final Set<Integer> cols) {
            m_getter = getter;
            m_cols = cols;
        }

        public LdapGetter getGetter() {
            return m_getter;
        }

        public Set<Integer> getCols() {
            return m_cols;
        }

        public boolean contains(final int field) {
            return m_cols.contains(Integer.valueOf(field));
        }
    }

    private static void stringSetter(final ParameterObject parameterObject, final String attributename, final int field, final SetterClosure closure) throws OXException {
        if (parameterObject.contains(field)) {
            if (null != attributename && 0 != attributename.length()) {
                final String attribute = parameterObject.getGetter().getAttribute(attributename);
                if (null != attribute) {
                    closure.set(attribute);
                }
            }
        }
    }

    private static void intSetter(final ParameterObject parameterObject, final String attributename, final int field, final SetterIntClosure closure) throws OXException {
        if (parameterObject.contains(field)) {
            if (null != attributename && 0 != attributename.length()) {
                final int attribute = parameterObject.getGetter().getIntAttribute(attributename);
                if (-1 != attribute) {
                    closure.set(attribute);
                }
            }
        }
    }

    private static void dateSetter(final ParameterObject parameterObject, final String attributename, final int field, final SetterDateClosure closure) throws OXException {
        if (parameterObject.contains(field)) {
            if (null != attributename && 0 != attributename.length()) {
                final Date attribute = parameterObject.getGetter().getDateAttribute(attributename);
                if (null != attribute) {
                    closure.set(attribute);
                }
            }
        }
    }

    public static Contact getDistriContact(final LdapGetter getter, final Set<Integer> cols, final FolderProperties folderprop, final UidInterface uidInterface, final int folderid, final int adminid, final String[] attributes) throws OXException {
        final Contact retval = new Contact();

        final Mappings mappings = folderprop.getMappings();
        retval.setDistributionList(getDistributionlist(getter, folderid, mappings, attributes));
        retval.setMarkAsDistributionlist(true);

        if (cols.contains(Contact.DISPLAY_NAME)) {
            final String displayname = mappings.getDistributionlistname();
            if (null != displayname && 0 != displayname.length()) {
                retval.setDisplayName(getter.getAttribute(displayname));
            }
        }

        if (cols.contains(Contact.SUR_NAME)) {
            final String displayname = mappings.getDistributionlistname();
            if (null != displayname && 0 != displayname.length()) {
                retval.setSurName(getter.getAttribute(displayname));
            }
        }

        if (cols.contains(DataObject.OBJECT_ID)) {
            final String uniqueid = mappings.getDistributionuid();
            if (folderprop.isMemorymapping()) {
                retval.setObjectID(uidInterface.getUid(getter.getAttribute(uniqueid)));
            } else {
                retval.setObjectID(getter.getIntAttribute(uniqueid));
            }
        }

        commonParts(cols, folderid, adminid, retval, mappings, getter);

        return retval;
    }

    /**
     * All those setting which are the same between users and distributionlists
     *
     * @param cols
     * @param folderid
     * @param adminid
     * @param retval
     * @param mappings TODO
     * @param getter TODO
     * @throws OXException
     */
    // protected to be able to test this
    protected static void commonParts(final Set<Integer> cols, final int folderid, final int adminid, final Contact retval, final Mappings mappings, final LdapGetter getter) throws OXException {
        if (cols.contains(FolderChildObject.FOLDER_ID)) {
            retval.setParentFolderID(folderid);
        }
        if (cols.contains(DataObject.CREATED_BY)) {
            retval.setCreatedBy(adminid);
        }

        // Finally we add the timestamps here
        if (cols.contains(DataObject.CREATION_DATE)) {
            // A timestamp must be provided, so if it can be fetched from LDAP (due to configuration) this must be self-generated
            final String creationdate = mappings.getCreationdate();
            if (null != creationdate && 0 != creationdate.length()) {
                final Date creationDateValue = getter.getDateAttribute(creationdate);
                if (null != creationDateValue) {
                    retval.setCreationDate(creationDateValue);
                } else {
                    retval.setCreationDate(new Date(1000));
                    LOG.warn("Object: " + getter.getObjectFullName() + " has no value for creation date. Using self-defined fallback. This may lead to problems.");
                }
            } else {
                retval.setCreationDate(new Date(1000));
                LOG.warn("No creation date found in mapping file. Using self-defined fallback for object: " + getter.getObjectFullName() + ". This may lead to problems.");
            }
        }
        if (cols.contains(DataObject.LAST_MODIFIED)) {
            final String lastmodified = mappings.getLastmodified();
            final Date creationDate = creationDateFallback(retval, mappings, getter);
            if (null != lastmodified && 0 != lastmodified.length()) {
                final Date modifiedDateValue = getter.getDateAttribute(lastmodified);
                if (null != modifiedDateValue) {
                    if (null == creationDate || creationDate.before(modifiedDateValue)) {
                        retval.setLastModified(modifiedDateValue);
                    }
                } else {
                    LOG.warn("Object: " + getter.getObjectFullName() + " has no value for last modified date. Using self-defined fallback. This may lead to problems.");
                }
            } else {
                retval.setLastModified(creationDate);
                LOG.warn("No lastmodified date found in mapping file. Using self-defined fallback for object: " + getter.getObjectFullName() + ". This may lead to problems.");
            }
        }
    }

    private static Date creationDateFallback(final Contact retval, final Mappings mappings, final LdapGetter getter) throws OXException {
        Date creationDate = retval.getCreationDate();
        // A timestamp must be provided, so if it can be fetched from LDAP (due to configuration) this must be self-generated
        if (null != creationDate) {
            retval.setLastModified(creationDate);
        } else {
            final String creationdate = mappings.getCreationdate();
            if (null != creationdate && 0 != creationdate.length()) {
                final Date creationDateValue = getter.getDateAttribute(creationdate);
                if (null != creationDateValue) {
                    creationDate = creationDateValue;
                    retval.setLastModified(creationDateValue);
                } else {
                    retval.setLastModified(new Date(1000));
                    LOG.warn("Object: " + getter.getObjectFullName() + " has no value for creation date. Using self-defined fallback. This may lead to problems.");
                }
            } else {
                retval.setCreationDate(new Date(1000));
                LOG.warn("No creation date found in mapping file. Using self-defined fallback for object: " + getter.getObjectFullName() + ". This may lead to problems.");
            }
        }
        return creationDate;
    }

    private static DistributionListEntryObject[] getDistributionlist(final LdapGetter getter, final int folderid, final Mappings mappings, final String[] attributes) throws OXException {
        // Iterate over all elements, fetch them from LDAP and create the objects...
        final List<DistributionListEntryObject> distrilist = new ArrayList<DistributionListEntryObject>();
        for (final String member : getter.getMultiValueAttribute("member")) {
            final DistributionListEntryObject distri = getDistriEntry(getter.getLdapGetterForDN(member, attributes), folderid, mappings);
            distrilist.add(distri);
        }
        return distrilist.toArray(new DistributionListEntryObject[distrilist.size()]);
    }

    // If changes are made to this method the method com.openexchange.contacts.ldap.contacts.LdapContactInterface.getDistriAttributes(FolderProperties)
    // should also be checked for changes to be made
    private static DistributionListEntryObject getDistriEntry(final LdapGetter getter, final int folderid, final Mappings mappings) throws OXException {
        final DistributionListEntryObject retval = new DistributionListEntryObject();
        final String displayname = mappings.getDisplayname();
        if (null != displayname && 0 != displayname.length()) {
            retval.setDisplayname(getter.getAttribute(displayname));
        }
        final String email1 = mappings.getEmail1();
        if (null != email1 && 0 != email1.length()) {
            final String mailAttribute = getter.getAttribute(email1);
            if (null != mailAttribute) {
                try {
                    retval.setEmailaddress(mailAttribute);
                } catch (final OXException e) {
                    throw LdapExceptionCode.MAIL_ADDRESS_DISTRI_INVALID.create(mailAttribute);
                }
            }
        }

        final String givenname = mappings.getGivenname();
        if (null != givenname && 0 != givenname.length()) {
            retval.setFirstname(getter.getAttribute(givenname));
        }

        final String surname = mappings.getSurname();
        if (null != surname && 0 != surname.length()) {
            retval.setLastname(getter.getAttribute(surname));
        }

        retval.setEmailfield(DistributionListEntryObject.INDEPENDENT);

        retval.setFolderID(folderid);
        return retval;
    }
}
