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



package com.openexchange.ajax.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.session.Session;

/**
 * ContactParser
 *
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ContactParser extends CommonParser {
	
	public ContactParser() {

	}

	public ContactParser(Session sessionObj) {
		this.sessionObj = sessionObj;
	}
	
	public void parse(final ContactObject contactobject, final JSONObject jsonobject) throws OXException {
		try {
			parseElementContact(contactobject, jsonobject);
		} catch (Exception exc) {
			throw new OXException(exc);
		}
	}
	
	protected void parseElementContact(final ContactObject contactobject, final JSONObject jsonobject) throws Exception {
		for (int i = 0; i < mapping.length; i++) {
			if (mapping[i].jsonObjectContains(jsonobject)) {
				mapping[i].setObject(contactobject, jsonobject);
			}
		}
		
		if (jsonobject.has(ContactFields.DISTRIBUTIONLIST)) {
			parseDistributionList(contactobject, jsonobject);
		}
		
		if (jsonobject.has(ContactFields.LINKS)) {
			parseLinks(contactobject, jsonobject);
		}
		
		parseElementCommon(contactobject, jsonobject);
	}
	
	protected void parseDistributionList(final ContactObject oxobject, final JSONObject jsonobject) throws Exception {
		final JSONArray jdistributionlist = jsonobject.getJSONArray(ContactFields.DISTRIBUTIONLIST);
		DistributionListEntryObject[] distributionlist = new DistributionListEntryObject[jdistributionlist.length()];
		for (int a = 0; a < jdistributionlist.length(); a++) {
			final JSONObject entry = jdistributionlist.getJSONObject(a);
			distributionlist[a] = new DistributionListEntryObject();
			if (entry.has(DistributionListFields.ID)) {
				distributionlist[a].setEntryID(parseInt(entry, DistributionListFields.ID));
			} 
			
			if (entry.has(DistributionListFields.FIRST_NAME)) {
				distributionlist[a].setFirstname(parseString(entry, DistributionListFields.FIRST_NAME));
			} 
			
			if (entry.has(DistributionListFields.LAST_NAME)) {
				distributionlist[a].setLastname(parseString(entry, DistributionListFields.LAST_NAME));
			} 
			
			distributionlist[a].setDisplayname(parseString(entry, DistributionListFields.DISPLAY_NAME));
			distributionlist[a].setEmailaddress(parseString(entry, DistributionListFields.MAIL));
			distributionlist[a].setEmailfield(parseInt(entry, DistributionListFields.MAIL_FIELD));
		}
		oxobject.setDistributionList(distributionlist);
	}
	
	protected void parseLinks(final ContactObject oxobject, final JSONObject jsonobject) throws Exception {
		final JSONArray jlinks = jsonobject.getJSONArray(ContactFields.LINKS);
		LinkEntryObject[] links = new LinkEntryObject[jlinks.length()];
		for (int a = 0; a < links.length; a++) {
			links[a] = new LinkEntryObject();
			final JSONObject entry = jlinks.getJSONObject(a);
			if (entry.has(ContactFields.ID)) {
				links[a].setLinkID(parseInt(entry, ContactFields.ID));
			} 
			
			links[a].setLinkDisplayname(parseString(entry, DistributionListFields.DISPLAY_NAME));
		}
		oxobject.setLinks(links);
	}
	
	private interface JSONAttributeMapper {
		boolean jsonObjectContains(JSONObject jsonobject);
		void setObject(ContactObject contactobject, JSONObject jsonobject)
		throws Exception;
	}
	
	private JSONAttributeMapper[] mapping = new JSONAttributeMapper[] {
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.LAST_NAME);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setSurName(parseString(jsonobject, ContactFields.LAST_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TITLE);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setTitle(parseString(jsonobject, ContactFields.TITLE));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.FIRST_NAME);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setGivenName (parseString(jsonobject, ContactFields.FIRST_NAME));
			}
		},		
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.MARITAL_STATUS);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setMaritalStatus(parseString(jsonobject, ContactFields.MARITAL_STATUS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.ANNIVERSARY);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setAnniversary(parseDate(jsonobject, ContactFields.ANNIVERSARY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.ASSISTANT_NAME);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setAssistantName(parseString(jsonobject, ContactFields.ASSISTANT_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.BIRTHDAY);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setBirthday(parseDate(jsonobject, ContactFields.BIRTHDAY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.BRANCHES);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setBranches(parseString(jsonobject, ContactFields.BRANCHES));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.BUSINESS_CATEGORY);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setBusinessCategory(parseString(jsonobject, ContactFields.BUSINESS_CATEGORY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.CELLULAR_TELEPHONE1);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCellularTelephone1(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE1));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.CELLULAR_TELEPHONE2);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCellularTelephone2(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE2));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.CITY_HOME);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCityHome(parseString(jsonobject, ContactFields.CITY_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.CITY_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCityBusiness(parseString(jsonobject, ContactFields.CITY_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.CITY_OTHER);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCityOther(parseString(jsonobject, ContactFields.CITY_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.COMMERCIAL_REGISTER);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCommercialRegister(parseString(jsonobject, ContactFields.COMMERCIAL_REGISTER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.COMPANY);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCompany(parseString(jsonobject, ContactFields.COMPANY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.COUNTRY_HOME);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCountryHome(parseString(jsonobject, ContactFields.COUNTRY_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.COUNTRY_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				contactobject.setCountryBusiness(parseString(jsonobject, ContactFields.COUNTRY_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.COUNTRY_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setCountryOther(parseString(jsonobject, ContactFields.COUNTRY_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.DEFAULT_ADDRESS);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setDefaultAddress(parseInt(jsonobject, ContactFields.DEFAULT_ADDRESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.DEPARTMENT);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setDepartment(parseString(jsonobject, ContactFields.DEPARTMENT));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.DISPLAY_NAME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setDisplayName(parseString(jsonobject, ContactFields.DISPLAY_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.DISTRIBUTIONLIST_FLAG);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setMarkAsDistributionlist(parseBoolean(jsonobject, ContactFields.DISTRIBUTIONLIST_FLAG));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.EMAIL1);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setEmail1(parseString(jsonobject, ContactFields.EMAIL1));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.EMAIL2);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setEmail2(parseString(jsonobject, ContactFields.EMAIL2));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.EMAIL3);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setEmail3(parseString(jsonobject, ContactFields.EMAIL3));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.EMPLOYEE_TYPE);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setEmployeeType(parseString(jsonobject, ContactFields.EMPLOYEE_TYPE));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.FAX_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setFaxBusiness(parseString(jsonobject, ContactFields.FAX_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.FAX_HOME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setFaxHome(parseString(jsonobject, ContactFields.FAX_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.FAX_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setFaxOther(parseString(jsonobject, ContactFields.FAX_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.IMAGE1);
			}
			public void setObject(final ContactObject contactobject,
					final JSONObject jsonobject) throws Exception {
				final String image = parseString(jsonobject, ContactFields.IMAGE1);
				if (image != null) {					
					contactobject.setImage1(image.getBytes());
				} else {
					contactobject.setImage1(null);
				}
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.NOTE);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setNote(parseString(jsonobject, ContactFields.NOTE));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.INFO);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setInfo(parseString(jsonobject, ContactFields.INFO));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.INSTANT_MESSENGER1);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setInstantMessenger1(parseString(jsonobject, ContactFields.INSTANT_MESSENGER1));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.INSTANT_MESSENGER2);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setInstantMessenger2(parseString(jsonobject, ContactFields.INSTANT_MESSENGER2));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.MANAGER_NAME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setManagerName(parseString(jsonobject, ContactFields.MANAGER_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.SECOND_NAME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setMiddleName(parseString(jsonobject, ContactFields.SECOND_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.NICKNAME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setNickname(parseString(jsonobject, ContactFields.NICKNAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.NUMBER_OF_CHILDREN);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setNumberOfChildren(parseString(jsonobject, ContactFields.NUMBER_OF_CHILDREN));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.NUMBER_OF_EMPLOYEE);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setNumberOfEmployee(parseString(jsonobject, ContactFields.NUMBER_OF_EMPLOYEE));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.POSITION);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setPosition(parseString(jsonobject, ContactFields.POSITION));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.POSTAL_CODE_HOME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setPostalCodeHome(parseString(jsonobject, ContactFields.POSTAL_CODE_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.POSTAL_CODE_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setPostalCodeBusiness(parseString(jsonobject, ContactFields.POSTAL_CODE_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.POSTAL_CODE_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setPostalCodeOther(parseString(jsonobject, ContactFields.POSTAL_CODE_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.PROFESSION);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setProfession(parseString(jsonobject, ContactFields.PROFESSION));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.ROOM_NUMBER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setRoomNumber(parseString(jsonobject, ContactFields.ROOM_NUMBER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.SALES_VOLUME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setSalesVolume(parseString(jsonobject, ContactFields.SALES_VOLUME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.SPOUSE_NAME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setSpouseName(parseString(jsonobject, ContactFields.SPOUSE_NAME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STATE_HOME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStateHome(parseString(jsonobject, ContactFields.STATE_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STATE_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStateBusiness(parseString(jsonobject, ContactFields.STATE_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STATE_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStateOther(parseString(jsonobject, ContactFields.STATE_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STREET_HOME);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStreetHome(parseString(jsonobject, ContactFields.STREET_HOME));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STREET_BUSINESS);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStreetBusiness(parseString(jsonobject, ContactFields.STREET_BUSINESS));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.STREET_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setStreetOther(parseString(jsonobject, ContactFields.STREET_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.SUFFIX);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setSuffix(parseString(jsonobject, ContactFields.SUFFIX));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TAX_ID);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTaxID(parseString(jsonobject, ContactFields.TAX_ID));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_ASSISTANT);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneAssistant(parseString(jsonobject, ContactFields.TELEPHONE_ASSISTANT));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_BUSINESS1);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneBusiness1(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS1));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_BUSINESS2);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneBusiness2(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS2));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_CALLBACK);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneCallback(parseString(jsonobject, ContactFields.TELEPHONE_CALLBACK));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_CAR);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneCar(parseString(jsonobject, ContactFields.TELEPHONE_CAR));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_COMPANY);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneCompany(parseString(jsonobject, ContactFields.TELEPHONE_COMPANY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_HOME1);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneHome1(parseString(jsonobject, ContactFields.TELEPHONE_HOME1));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_HOME2);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneHome2(parseString(jsonobject, ContactFields.TELEPHONE_HOME2));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_IP);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneIP(parseString(jsonobject, ContactFields.TELEPHONE_IP));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_ISDN);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneISDN(parseString(jsonobject, ContactFields.TELEPHONE_ISDN));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_OTHER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneOther(parseString(jsonobject, ContactFields.TELEPHONE_OTHER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_PAGER);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephonePager(parseString(jsonobject, ContactFields.TELEPHONE_PAGER));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_PRIMARY);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephonePrimary(parseString(jsonobject, ContactFields.TELEPHONE_PRIMARY));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_RADIO);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneRadio(parseString(jsonobject, ContactFields.TELEPHONE_RADIO));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_TELEX);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneTelex(parseString(jsonobject, ContactFields.TELEPHONE_TELEX));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.TELEPHONE_TTYTDD);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setTelephoneTTYTTD(parseString(jsonobject, ContactFields.TELEPHONE_TTYTDD));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.URL);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setURL(parseString(jsonobject, ContactFields.URL));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD01);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField01(parseString(jsonobject, ContactFields.USERFIELD01));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD02);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField02(parseString(jsonobject, ContactFields.USERFIELD02));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD03);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField03(parseString(jsonobject, ContactFields.USERFIELD03));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD04);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField04(parseString(jsonobject, ContactFields.USERFIELD04));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD05);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField05(parseString(jsonobject, ContactFields.USERFIELD05));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD06);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField06(parseString(jsonobject, ContactFields.USERFIELD06));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD07);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField07(parseString(jsonobject, ContactFields.USERFIELD07));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD08);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField08(parseString(jsonobject, ContactFields.USERFIELD08));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD09);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField09(parseString(jsonobject, ContactFields.USERFIELD09));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD10);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField10(parseString(jsonobject, ContactFields.USERFIELD10));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD11);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField11(parseString(jsonobject, ContactFields.USERFIELD11));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD12);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField12(parseString(jsonobject, ContactFields.USERFIELD12));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD13);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField13(parseString(jsonobject, ContactFields.USERFIELD13));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD14);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField14(parseString(jsonobject, ContactFields.USERFIELD14));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD15);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField15(parseString(jsonobject, ContactFields.USERFIELD15));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD16);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField16(parseString(jsonobject, ContactFields.USERFIELD16));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD17);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField17(parseString(jsonobject, ContactFields.USERFIELD17));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD18);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField18(parseString(jsonobject, ContactFields.USERFIELD18));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD19);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField19(parseString(jsonobject, ContactFields.USERFIELD19));
			}
		},
		new JSONAttributeMapper() {
			public boolean jsonObjectContains(final JSONObject jsonobject) {
				return jsonobject.has(ContactFields.USERFIELD20);
			}
			public void setObject(final ContactObject contactobject,
				final JSONObject jsonobject) throws Exception {
				contactobject.setUserField20(parseString(jsonobject, ContactFields.USERFIELD20));
			}
		}
	};
}




