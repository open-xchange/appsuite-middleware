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

package com.openexchange.contact.storage.rdb.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.EnumMap;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DateMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;

/**
 * {@link ContactMapper} - Maps contact related fields to a corresponding {@link Mapping} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactMapper extends DefaultDbMapper<Contact, ContactField> {
	
	public ContactMapper() {
		super();
	}
	
	@Override
	public Contact newInstance() {
		return new Contact();
	}

	@Override
	public ContactField[] newArray(int size) {
		return new ContactField[size];
	}

	@Override
	protected EnumMap<ContactField, DbMapping<? extends Object, Contact>> createMappings() {
		final EnumMap<ContactField, DbMapping<? extends Object, Contact>> mappings = new 
				EnumMap<ContactField, DbMapping<? extends Object, Contact>>(ContactField.class);

        mappings.put(ContactField.DISPLAY_NAME, new VarCharMapping<Contact>("field01", "Display name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setDisplayName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDisplayName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getDisplayName();
            }
        });

        mappings.put(ContactField.SUR_NAME, new VarCharMapping<Contact>("field02", "Sur name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setSurName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSurName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getSurName();
            }
        });

        mappings.put(ContactField.GIVEN_NAME, new VarCharMapping<Contact>("field03", "Given name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setGivenName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsGivenName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getGivenName();
            }
        });

        mappings.put(ContactField.MIDDLE_NAME, new VarCharMapping<Contact>("field04", "Middle name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setMiddleName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMiddleName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getMiddleName();
            }
        });

        mappings.put(ContactField.SUFFIX, new VarCharMapping<Contact>("field05", "Suffix") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setSuffix(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSuffix();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getSuffix();
            }
        });

        mappings.put(ContactField.TITLE, new VarCharMapping<Contact>("field06", "Title") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTitle(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTitle();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTitle();
            }
        });

        mappings.put(ContactField.STREET_HOME, new VarCharMapping<Contact>("field07", "Street home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStreetHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStreetHome();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_HOME, new VarCharMapping<Contact>("field08", "Postal code home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setPostalCodeHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getPostalCodeHome();
            }
        });

        mappings.put(ContactField.CITY_HOME, new VarCharMapping<Contact>("field09", "City home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCityHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCityHome();
            }
        });

        mappings.put(ContactField.STATE_HOME, new VarCharMapping<Contact>("field10", "State home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStateHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStateHome();
            }
        });

        mappings.put(ContactField.COUNTRY_HOME, new VarCharMapping<Contact>("field11", "Country home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCountryHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCountryHome();
            }
        });

        mappings.put(ContactField.MARITAL_STATUS, new VarCharMapping<Contact>("field12", "Martial status") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setMaritalStatus(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMaritalStatus();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getMaritalStatus();
            }
        });

        mappings.put(ContactField.NUMBER_OF_CHILDREN, new VarCharMapping<Contact>("field13", "Number of children") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setNumberOfChildren(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfChildren();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getNumberOfChildren();
            }
        });

        mappings.put(ContactField.PROFESSION, new VarCharMapping<Contact>("field14", "Profession") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setProfession(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsProfession();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getProfession();
            }
        });

        mappings.put(ContactField.NICKNAME, new VarCharMapping<Contact>("field15", "Nickname") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setNickname(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNickname();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getNickname();
            }
        });

        mappings.put(ContactField.SPOUSE_NAME, new VarCharMapping<Contact>("field16", "Spouse name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setSpouseName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSpouseName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getSpouseName();
            }
        });

        mappings.put(ContactField.NOTE, new VarCharMapping<Contact>("field17", "Note") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setNote(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNote();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getNote();
            }
        });

        mappings.put(ContactField.COMPANY, new VarCharMapping<Contact>("field18", "Company") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCompany();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCompany();
            }
        });

        mappings.put(ContactField.DEPARTMENT, new VarCharMapping<Contact>("field19", "Department") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setDepartment(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDepartment();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getDepartment();
            }
        });

        mappings.put(ContactField.POSITION, new VarCharMapping<Contact>("field20", "Position") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setPosition(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPosition();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getPosition();
            }
        });

        mappings.put(ContactField.EMPLOYEE_TYPE, new VarCharMapping<Contact>("field21", "Employee type") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setEmployeeType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmployeeType();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getEmployeeType();
            }
        });

        mappings.put(ContactField.ROOM_NUMBER, new VarCharMapping<Contact>("field22", "Room number") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setRoomNumber(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsRoomNumber();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getRoomNumber();
            }
        });

        mappings.put(ContactField.STREET_BUSINESS, new VarCharMapping<Contact>("field23", "Street business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStreetBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStreetBusiness();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_BUSINESS, new VarCharMapping<Contact>("field24", "Postal code business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setPostalCodeBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getPostalCodeBusiness();
            }
        });

        mappings.put(ContactField.CITY_BUSINESS, new VarCharMapping<Contact>("field25", "City business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCityBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCityBusiness();
            }
        });

        mappings.put(ContactField.STATE_BUSINESS, new VarCharMapping<Contact>("field26", "State business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStateBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStateBusiness();
            }
        });

        mappings.put(ContactField.COUNTRY_BUSINESS, new VarCharMapping<Contact>("field27", "Country business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCountryBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCountryBusiness();
            }
        });

        mappings.put(ContactField.NUMBER_OF_EMPLOYEE, new VarCharMapping<Contact>("field28", "Number of employee") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setNumberOfEmployee(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfEmployee();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getNumberOfEmployee();
            }
        });

        mappings.put(ContactField.SALES_VOLUME, new VarCharMapping<Contact>("field29", "Sales volume") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setSalesVolume(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSalesVolume();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getSalesVolume();
            }
        });

        mappings.put(ContactField.TAX_ID, new VarCharMapping<Contact>("field30", "Tax id") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTaxID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTaxID();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTaxID();
            }
        });

        mappings.put(ContactField.COMMERCIAL_REGISTER, new VarCharMapping<Contact>("field31", "Commercial register") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCommercialRegister(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCommercialRegister();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCommercialRegister();
            }
        });

        mappings.put(ContactField.BRANCHES, new VarCharMapping<Contact>("field32", "Branches") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setBranches(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBranches();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getBranches();
            }
        });

        mappings.put(ContactField.BUSINESS_CATEGORY, new VarCharMapping<Contact>("field33", "Business category") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setBusinessCategory(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBusinessCategory();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getBusinessCategory();
            }
        });

        mappings.put(ContactField.INFO, new VarCharMapping<Contact>("field34", "Info") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setInfo(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInfo();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getInfo();
            }
        });

        mappings.put(ContactField.MANAGER_NAME, new VarCharMapping<Contact>("field35", "Manager's name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setManagerName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsManagerName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getManagerName();
            }
        });

        mappings.put(ContactField.ASSISTANT_NAME, new VarCharMapping<Contact>("field36", "Assistant's name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setAssistantName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAssistantName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getAssistantName();
            }
        });

        mappings.put(ContactField.STREET_OTHER, new VarCharMapping<Contact>("field37", "Street other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStreetOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStreetOther();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_OTHER, new VarCharMapping<Contact>("field38", "Postal code other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setPostalCodeOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getPostalCodeOther();
            }
        });

        mappings.put(ContactField.CITY_OTHER, new VarCharMapping<Contact>("field39", "City other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCityOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCityOther();
            }
        });

        mappings.put(ContactField.STATE_OTHER, new VarCharMapping<Contact>("field40", "State other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setStateOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getStateOther();
            }
        });

        mappings.put(ContactField.COUNTRY_OTHER, new VarCharMapping<Contact>("field41", "Country other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCountryOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCountryOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_ASSISTANT, new VarCharMapping<Contact>("field42", "Telephone assistant") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneAssistant(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneAssistant();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneAssistant();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS1, new VarCharMapping<Contact>("field43", "Telephone business 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneBusiness1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness1();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneBusiness1();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS2, new VarCharMapping<Contact>("field44", "Telephone business 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneBusiness2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness2();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneBusiness2();
            }
        });

        mappings.put(ContactField.FAX_BUSINESS, new VarCharMapping<Contact>("field45", "FAX business") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setFaxBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getFaxBusiness();
            }
        });

        mappings.put(ContactField.TELEPHONE_CALLBACK, new VarCharMapping<Contact>("field46", "Telephone callback") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneCallback(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCallback();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneCallback();
            }
        });

        mappings.put(ContactField.TELEPHONE_CAR, new VarCharMapping<Contact>("field47", "Telephone car") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneCar(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCar();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneCar();
            }
        });

        mappings.put(ContactField.TELEPHONE_COMPANY, new VarCharMapping<Contact>("field48", "Telephone company") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCompany();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneCompany();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME1, new VarCharMapping<Contact>("field49", "Telephone home 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneHome1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome1();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneHome1();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME2, new VarCharMapping<Contact>("field50", "Telephone home 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneHome2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome2();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneHome2();
            }
        });

        mappings.put(ContactField.FAX_HOME, new VarCharMapping<Contact>("field51", "FAX home") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setFaxHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getFaxHome();
            }
        });

        mappings.put(ContactField.TELEPHONE_ISDN, new VarCharMapping<Contact>("field52", "Telephone ISDN") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneISDN(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneISDN();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneISDN();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE1, new VarCharMapping<Contact>("field53", "Cellular telephone 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCellularTelephone1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone1();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCellularTelephone1();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE2, new VarCharMapping<Contact>("field54", "Cellular telephone 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCellularTelephone2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone2();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCellularTelephone2();
            }
        });

        mappings.put(ContactField.TELEPHONE_OTHER, new VarCharMapping<Contact>("field55", "Telephone other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneOther();
            }
        });

        mappings.put(ContactField.FAX_OTHER, new VarCharMapping<Contact>("field56", "FAX other") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setFaxOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxOther();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getFaxOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_PAGER, new VarCharMapping<Contact>("field57", "Telephone pager") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephonePager(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePager();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephonePager();
            }
        });

        mappings.put(ContactField.TELEPHONE_PRIMARY, new VarCharMapping<Contact>("field58", "Telephone primary") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephonePrimary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePrimary();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephonePrimary();
            }
        });

        mappings.put(ContactField.TELEPHONE_RADIO, new VarCharMapping<Contact>("field59", "Telephone radio") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneRadio(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneRadio();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneRadio();
            }
        });

        mappings.put(ContactField.TELEPHONE_TELEX, new VarCharMapping<Contact>("field60", "Telephone telex") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneTelex(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTelex();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneTelex();
            }
        });

        mappings.put(ContactField.TELEPHONE_TTYTDD, new VarCharMapping<Contact>("field61", "Telephone TTY/TDD") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneTTYTTD(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTTYTTD();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneTTYTTD();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER1, new VarCharMapping<Contact>("field62", "Instantmessenger 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setInstantMessenger1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger1();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getInstantMessenger1();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER2, new VarCharMapping<Contact>("field63", "Instantmessenger 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setInstantMessenger2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger2();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getInstantMessenger2();
            }
        });

        mappings.put(ContactField.TELEPHONE_IP, new VarCharMapping<Contact>("field64", "Telephone IP") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setTelephoneIP(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneIP();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getTelephoneIP();
            }
        });

        mappings.put(ContactField.EMAIL1, new VarCharMapping<Contact>("field65", "Email 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setEmail1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail1();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getEmail1();
            }
        });

        mappings.put(ContactField.EMAIL2, new VarCharMapping<Contact>("field66", "Email 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setEmail2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail2();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getEmail2();
            }
        });

        mappings.put(ContactField.EMAIL3, new VarCharMapping<Contact>("field67", "Email 3") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setEmail3(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail3();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getEmail3();
            }
        });

        mappings.put(ContactField.URL, new VarCharMapping<Contact>("field68", "URL") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setURL(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsURL();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getURL();
            }
        });

        mappings.put(ContactField.CATEGORIES, new VarCharMapping<Contact>("field69", "Categories") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setCategories(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCategories();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getCategories();
            }
        });

        mappings.put(ContactField.USERFIELD01, new VarCharMapping<Contact>("field70", "Dynamic Field 1") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField01(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField01();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField01();
            }
        });

        mappings.put(ContactField.USERFIELD02, new VarCharMapping<Contact>("field71", "Dynamic Field 2") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField02(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField02();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField02();
            }
        });

        mappings.put(ContactField.USERFIELD03, new VarCharMapping<Contact>("field72", "Dynamic Field 3") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField03(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField03();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField03();
            }
        });

        mappings.put(ContactField.USERFIELD04, new VarCharMapping<Contact>("field73", "Dynamic Field 4") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField04(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField04();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField04();
            }
        });

        mappings.put(ContactField.USERFIELD05, new VarCharMapping<Contact>("field74", "Dynamic Field 5") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField05(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField05();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField05();
            }
        });

        mappings.put(ContactField.USERFIELD06, new VarCharMapping<Contact>("field75", "Dynamic Field 6") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField06(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField06();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField06();
            }
        });

        mappings.put(ContactField.USERFIELD07, new VarCharMapping<Contact>("field76", "Dynamic Field 7") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField07(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField07();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField07();
            }
        });

        mappings.put(ContactField.USERFIELD08, new VarCharMapping<Contact>("field77", "Dynamic Field 8") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField08(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField08();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField08();
            }
        });

        mappings.put(ContactField.USERFIELD09, new VarCharMapping<Contact>("field78", "Dynamic Field 9") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField09(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField09();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField09();
            }
        });

        mappings.put(ContactField.USERFIELD10, new VarCharMapping<Contact>("field79", "Dynamic Field 10") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField10(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField10();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField10();
            }
        });

        mappings.put(ContactField.USERFIELD11, new VarCharMapping<Contact>("field80", "Dynamic Field 11") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField11(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField11();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField11();
            }
        });

        mappings.put(ContactField.USERFIELD12, new VarCharMapping<Contact>("field81", "Dynamic Field 12") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField12(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField12();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField12();
            }
        });

        mappings.put(ContactField.USERFIELD13, new VarCharMapping<Contact>("field82", "Dynamic Field 13") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField13(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField13();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField13();
            }
        });

        mappings.put(ContactField.USERFIELD14, new VarCharMapping<Contact>("field83", "Dynamic Field 14") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField14(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField14();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField14();
            }
        });

        mappings.put(ContactField.USERFIELD15, new VarCharMapping<Contact>("field84", "Dynamic Field 15") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField15(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField15();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField15();
            }
        });

        mappings.put(ContactField.USERFIELD16, new VarCharMapping<Contact>("field85", "Dynamic Field 16") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField16(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField16();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField16();
            }
        });

        mappings.put(ContactField.USERFIELD17, new VarCharMapping<Contact>("field86", "Dynamic Field 17") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField17(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField17();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField17();
            }
        });

        mappings.put(ContactField.USERFIELD18, new VarCharMapping<Contact>("field87", "Dynamic Field 18") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField18(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField18();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField18();
            }
        });

        mappings.put(ContactField.USERFIELD19, new VarCharMapping<Contact>("field88", "Dynamic Field 19") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField19(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField19();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField19();
            }
        });

        mappings.put(ContactField.USERFIELD20, new VarCharMapping<Contact>("field89", "Dynamic Field 20") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUserField20(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField20();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUserField20();
            }
        });

        mappings.put(ContactField.OBJECT_ID, new IntegerMapping<Contact>("intfield01", "Object id") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setObjectID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsObjectID();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getObjectID();
            }
        });

        mappings.put(ContactField.NUMBER_OF_DISTRIBUTIONLIST, new IntegerMapping<Contact>("intfield02", "Number of distributionlists") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setNumberOfDistributionLists(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfDistributionLists();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getNumberOfDistributionLists();
            }
        });

        mappings.put(ContactField.DISTRIBUTIONLIST, new DefaultDbMapping<DistributionListEntryObject[], Contact>(null, null, 0) {

			@Override
			public void set(Contact contact, DistributionListEntryObject[] value) {
                contact.setDistributionList(value);
			}

			@Override
			public boolean isSet(Contact contact) {
				return contact.containsDistributionLists();
			}

			@Override
			public DistributionListEntryObject[] get(Contact contact) {
                return contact.getDistributionList();
			}

			@Override
			public DistributionListEntryObject[] get(ResultSet resultSet) throws SQLException {
				return null;
			}
		});

        mappings.put(ContactField.NUMBER_OF_LINKS, new IntegerMapping<Contact>("intfield03", "Number of links") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setNumberOfLinks(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfLinks();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getNumberOfLinks();
            }
        });

        mappings.put(ContactField.FOLDER_ID, new IntegerMapping<Contact>("fid", "Folder id") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setParentFolderID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsParentFolderID();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getParentFolderID();
            }
        });

		mappings.put(ContactField.CONTEXTID, new IntegerMapping<Contact>("cid", "Context ID") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setContextId(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsContextId();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getContextId();
            }
        });

        mappings.put(ContactField.PRIVATE_FLAG, new IntegerMapping<Contact>("pflag", "Private Flag") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setPrivateFlag(1 == value.intValue());
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPrivateFlag();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getPrivateFlag() ? Integer.valueOf(1) : Integer.valueOf(0);
            }
            
        	@Override
        	public void set(final PreparedStatement statement, final int parameterIndex, final Contact contact) throws SQLException {
        		// special handling, since previous contact database implementation expected 'null', when the private flag is 'false'
        		if (contact.containsPrivateFlag() && contact.getPrivateFlag()) {
    				statement.setInt(parameterIndex, 1);
        		} else {
        			statement.setNull(parameterIndex, this.getSqlType());
        		}
        	}
        });

        mappings.put(ContactField.CREATED_BY, new IntegerMapping<Contact>("created_from", "Created by") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setCreatedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreatedBy();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getCreatedBy();
            }
        });

        mappings.put(ContactField.MODIFIED_BY, new IntegerMapping<Contact>("changed_from", "Modified by") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setModifiedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsModifiedBy();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getModifiedBy();
            }
        });

        mappings.put(ContactField.CREATION_DATE, new BigIntMapping<Contact>("creating_date", "Creation date") {

            @Override
            public void set(Contact contact, Long value) { 
                contact.setCreationDate(new Date(value));
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreationDate();
            }

            @Override
            public Long get(Contact contact) { 
                return contact.getCreationDate().getTime();
            }
        });

        mappings.put(ContactField.LAST_MODIFIED, new BigIntMapping<Contact>("changing_date", "Changing date") {

            @Override
            public void set(Contact contact, Long value) { 
                contact.setLastModified(new Date(value));
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLastModified();
            }

            @Override
            public Long get(Contact contact) { 
                return contact.getLastModified().getTime();
            }
        });

        mappings.put(ContactField.BIRTHDAY, new DateMapping<Contact>("timestampfield01", "Birthday") {

            @Override
            public void set(Contact contact, Date value) { 
                contact.setBirthday(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBirthday();
            }

            @Override
            public Date get(Contact contact) { 
                return contact.getBirthday();
            }
        });

        mappings.put(ContactField.ANNIVERSARY, new DateMapping<Contact>("timestampfield02", "Anniversary") {

            @Override
            public void set(Contact contact, Date value) { 
                contact.setAnniversary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAnniversary();
            }

            @Override
            public Date get(Contact contact) { 
                return contact.getAnniversary();
            }
        });

        mappings.put(ContactField.IMAGE1, new DefaultDbMapping<byte[], Contact>("image1", "Image 1", Types.VARBINARY) {

			@Override
			public void set(Contact contact, byte[] value) {
                contact.setImage1(value);
			}

			@Override
			public boolean isSet(Contact contact) {
				return contact.containsImage1();
			}

			@Override
			public byte[] get(Contact contact) {
                return contact.getImage1();
			}

			@Override
			public byte[] get(ResultSet resultSet) throws SQLException {
				return resultSet.getBytes(this.getColumnLabel());
			}
		});

        mappings.put(ContactField.IMAGE1_CONTENT_TYPE, new VarCharMapping<Contact>("mime_type", "Image 1 Content Type") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setImageContentType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageContentType();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getImageContentType();
            }
        });

        mappings.put(ContactField.IMAGE_LAST_MODIFIED, new BigIntMapping<Contact>("changing_date", "Image 1 Changing date") {

            @Override
            public void set(Contact contact, Long value) { 
                contact.setImageLastModified(new Date(value));
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageLastModified();
            }

            @Override
            public Long get(Contact contact) { 
                return contact.getImageLastModified().getTime();
            }
        });

        mappings.put(ContactField.INTERNAL_USERID, new IntegerMapping<Contact>("userid", "User ID") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setInternalUserId(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInternalUserId();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getInternalUserId();
            }
        });

        mappings.put(ContactField.COLOR_LABEL, new IntegerMapping<Contact>("intfield05", "Color Label") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setLabel(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLabel();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getLabel();
            }
        });

        mappings.put(ContactField.FILE_AS, new VarCharMapping<Contact>("field90", "File As") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setFileAs(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFileAs();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getFileAs();
            }
        });

        mappings.put(ContactField.DEFAULT_ADDRESS, new IntegerMapping<Contact>("intfield06", "Default address") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setDefaultAddress(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDefaultAddress();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getDefaultAddress();
            }
        });

        mappings.put(ContactField.MARK_AS_DISTRIBUTIONLIST, new IntegerMapping<Contact>("intfield07", "Mark as Distribution List") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setMarkAsDistributionlist(1 == value.intValue());
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMarkAsDistributionlist();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getMarkAsDistribtuionlist() ? Integer.valueOf(1) : Integer.valueOf(0);
            }
        });

        mappings.put(ContactField.NUMBER_OF_ATTACHMENTS, new IntegerMapping<Contact>("intfield08", "Number of attachments") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setNumberOfAttachments(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfAttachments();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getNumberOfAttachments();
            }
        });

        mappings.put(ContactField.YOMI_FIRST_NAME, new VarCharMapping<Contact>("yomiFirstName", "Yomi First Name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setYomiFirstName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiFirstName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getYomiFirstName();
            }
        });

        mappings.put(ContactField.YOMI_LAST_NAME, new VarCharMapping<Contact>("yomiLastName", "Yomi Last Name") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setYomiLastName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiLastName();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getYomiLastName();
            }
        });

        mappings.put(ContactField.YOMI_COMPANY, new VarCharMapping<Contact>("yomiCompany", "Yomi Company") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setYomiCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiCompany();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getYomiCompany();
            }
        });

        mappings.put(ContactField.NUMBER_OF_IMAGES, new IntegerMapping<Contact>("intfield04", "Number of images") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setNumberOfImages(value);
            }

            @Override
            public boolean isSet(Contact contact) {
            	//TODO: create Contact.containsNumberOfImages() method
                return contact.containsImage1(); 
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getNumberOfImages();
            }
        });

        mappings.put(ContactField.USE_COUNT, new IntegerMapping<Contact>("useCount", "Use Count") {

            @Override
            public void set(Contact contact, Integer value) { 
                contact.setUseCount(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUseCount();
            }

            @Override
            public Integer get(Contact contact) { 
                return contact.getUseCount();
            }
        });

        mappings.put(ContactField.HOME_ADDRESS, new VarCharMapping<Contact>("homeAddress", "Home Address") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setAddressHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressHome();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getAddressHome();
            }
        });

        mappings.put(ContactField.BUSINESS_ADDRESS, new VarCharMapping<Contact>("businessAddress", "Business Address") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setAddressBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressBusiness();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getAddressBusiness();
            }
        });

        mappings.put(ContactField.OTHER_ADDRESS, new VarCharMapping<Contact>("otherAddress", "Other Address") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setAddressOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressOther();

            }

            @Override
            public String get(Contact contact) { 
                return contact.getAddressOther();
            }
        });

        mappings.put(ContactField.UID, new VarCharMapping<Contact>("uid", "UID") {

            @Override
            public void set(Contact contact, String value) { 
                contact.setUid(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUid();
            }

            @Override
            public String get(Contact contact) { 
                return contact.getUid();
            }
        });
        
		return mappings;
	}

}
