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



package com.openexchange.ajax.writer;

import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import java.io.PrintWriter;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * ContactWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ContactWriter extends CommonWriter {
	
	public ContactWriter(final TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	public void writeArray(final ContactObject contactobject, final int cols[], final JSONArray jsonArray) throws JSONException {
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], contactobject, jsonArray);
		}
	}
	
	public void writeContact(final ContactObject contactobject, final JSONObject jsonObj) throws JSONException {
		writeCommonFields(contactobject, jsonObj);
		
		writeParameter(ContactFields.LAST_NAME, contactobject.getSurName(), jsonObj);
		writeParameter(ContactFields.FIRST_NAME, contactobject.getGivenName(), jsonObj);
		writeParameter(ContactFields.ANNIVERSARY, contactobject.getAnniversary(), jsonObj);
		writeParameter(ContactFields.ASSISTANT_NAME, contactobject.getAssistantName(), jsonObj);
		writeParameter(ContactFields.BIRTHDAY, contactobject.getBirthday(), jsonObj);
		writeParameter(ContactFields.BRANCHES, contactobject.getBranches(), jsonObj);
		writeParameter(ContactFields.BUSINESS_CATEGORY, contactobject.getBusinessCategory(), jsonObj);
		writeParameter(ContactFields.CELLULAR_TELEPHONE1, contactobject.getCellularTelephone1(), jsonObj);
		writeParameter(ContactFields.CELLULAR_TELEPHONE2, contactobject.getCellularTelephone2(), jsonObj);
		writeParameter(ContactFields.CITY_HOME, contactobject.getCityHome(), jsonObj);
		writeParameter(ContactFields.CITY_BUSINESS, contactobject.getCityBusiness(), jsonObj);
		writeParameter(ContactFields.CITY_OTHER, contactobject.getCityOther(), jsonObj);
		writeParameter(ContactFields.COMMERCIAL_REGISTER, contactobject.getCommercialRegister(), jsonObj);
		writeParameter(ContactFields.COMPANY, contactobject.getCompany(), jsonObj);
		writeParameter(ContactFields.COUNTRY_HOME, contactobject.getCountryHome(), jsonObj);
		writeParameter(ContactFields.COUNTRY_BUSINESS, contactobject.getCountryBusiness(), jsonObj);
		writeParameter(ContactFields.COUNTRY_OTHER, contactobject.getCountryOther(), jsonObj);
		writeParameter(ContactFields.DEFAULT_ADDRESS, contactobject.getDefaultAddress(), jsonObj);
		writeParameter(ContactFields.DEPARTMENT, contactobject.getDepartment(), jsonObj);
		writeParameter(ContactFields.DISPLAY_NAME, contactobject.getDisplayName(), jsonObj);
		writeParameter(ContactFields.EMAIL1, contactobject.getEmail1(), jsonObj);
		writeParameter(ContactFields.EMAIL2, contactobject.getEmail2(), jsonObj);
		writeParameter(ContactFields.EMAIL3, contactobject.getEmail3(), jsonObj);
		writeParameter(ContactFields.EMPLOYEE_TYPE, contactobject.getEmployeeType(), jsonObj);
		writeParameter(ContactFields.FAX_BUSINESS, contactobject.getFaxBusiness(), jsonObj);
		writeParameter(ContactFields.FAX_HOME, contactobject.getFaxHome(), jsonObj);
		writeParameter(ContactFields.FAX_OTHER, contactobject.getFaxOther(), jsonObj);
		if (contactobject.containsImage1()){
			writeParameter(ContactFields.NUMBER_OF_IMAGES, contactobject.getNumberOfImages(), jsonObj);
		}
		//writeParameter(ContactFields.IMAGE1, contactobject.getImage1());
		writeParameter(ContactFields.INFO, contactobject.getInfo(), jsonObj);
		writeParameter(ContactFields.NOTE, contactobject.getNote(), jsonObj);
		writeParameter(ContactFields.INSTANT_MESSENGER1, contactobject.getInstantMessenger1(), jsonObj);
		writeParameter(ContactFields.INSTANT_MESSENGER2, contactobject.getInstantMessenger2(), jsonObj);
		writeParameter(ContactFields.MARITAL_STATUS, contactobject.getMaritalStatus(), jsonObj);
		writeParameter(ContactFields.MANAGER_NAME, contactobject.getManagerName(), jsonObj);
		writeParameter(ContactFields.SECOND_NAME, contactobject.getMiddleName(), jsonObj);
		writeParameter(ContactFields.NICKNAME, contactobject.getNickname(), jsonObj);
		writeParameter(ContactFields.NUMBER_OF_CHILDREN, contactobject.getNumberOfChildren(), jsonObj);
		writeParameter(ContactFields.NUMBER_OF_EMPLOYEE, contactobject.getNumberOfEmployee(), jsonObj);
		writeParameter(ContactFields.POSITION, contactobject.getPosition(), jsonObj);
		writeParameter(ContactFields.POSTAL_CODE_HOME, contactobject.getPostalCodeHome(), jsonObj);
		writeParameter(ContactFields.POSTAL_CODE_BUSINESS, contactobject.getPostalCodeBusiness(), jsonObj);
		writeParameter(ContactFields.POSTAL_CODE_OTHER, contactobject.getPostalCodeOther(), jsonObj);
		writeParameter(ContactFields.PROFESSION, contactobject.getProfession(), jsonObj);
		writeParameter(ContactFields.ROOM_NUMBER, contactobject.getRoomNumber(), jsonObj);
		writeParameter(ContactFields.SALES_VOLUME, contactobject.getSalesVolume(), jsonObj);
		writeParameter(ContactFields.SPOUSE_NAME, contactobject.getSpouseName(), jsonObj);
		writeParameter(ContactFields.STATE_HOME, contactobject.getStateHome(), jsonObj);
		writeParameter(ContactFields.STATE_BUSINESS, contactobject.getStateBusiness(), jsonObj);
		writeParameter(ContactFields.STATE_OTHER, contactobject.getStateOther(), jsonObj);
		writeParameter(ContactFields.STREET_HOME, contactobject.getStreetHome(), jsonObj);
		writeParameter(ContactFields.STREET_BUSINESS, contactobject.getStreetBusiness(), jsonObj);
		writeParameter(ContactFields.STREET_OTHER, contactobject.getStreetOther(), jsonObj);
		writeParameter(ContactFields.SUFFIX, contactobject.getSuffix(), jsonObj);
		writeParameter(ContactFields.TAX_ID, contactobject.getTaxID(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_ASSISTANT, contactobject.getTelephoneAssistant(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_BUSINESS1, contactobject.getTelephoneBusiness1(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_BUSINESS2, contactobject.getTelephoneBusiness2(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_CALLBACK, contactobject.getTelephoneCallback(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_CAR, contactobject.getTelephoneCar(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_COMPANY, contactobject.getTelephoneCompany(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_HOME1, contactobject.getTelephoneHome1(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_HOME2, contactobject.getTelephoneHome2(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_IP, contactobject.getTelephoneIP(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_ISDN, contactobject.getTelephoneISDN(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_OTHER, contactobject.getTelephoneOther(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_PAGER, contactobject.getTelephonePager(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_PRIMARY, contactobject.getTelephonePrimary(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_RADIO, contactobject.getTelephoneRadio(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_TELEX, contactobject.getTelephoneTelex(), jsonObj);
		writeParameter(ContactFields.TELEPHONE_TTYTDD, contactobject.getTelephoneTTYTTD(), jsonObj);
		writeParameter(ContactFields.TITLE, contactobject.getTitle(), jsonObj);
		writeParameter(ContactFields.URL, contactobject.getURL(), jsonObj);
		writeParameter(ContactFields.USERFIELD01, contactobject.getUserField01(), jsonObj);
		writeParameter(ContactFields.USERFIELD02, contactobject.getUserField02(), jsonObj);
		writeParameter(ContactFields.USERFIELD03, contactobject.getUserField03(), jsonObj);
		writeParameter(ContactFields.USERFIELD04, contactobject.getUserField04(), jsonObj);
		writeParameter(ContactFields.USERFIELD05, contactobject.getUserField05(), jsonObj);
		writeParameter(ContactFields.USERFIELD06, contactobject.getUserField06(), jsonObj);
		writeParameter(ContactFields.USERFIELD07, contactobject.getUserField07(), jsonObj);
		writeParameter(ContactFields.USERFIELD08, contactobject.getUserField08(), jsonObj);
		writeParameter(ContactFields.USERFIELD09, contactobject.getUserField09(), jsonObj);
		writeParameter(ContactFields.USERFIELD10, contactobject.getUserField10(), jsonObj);
		writeParameter(ContactFields.USERFIELD11, contactobject.getUserField11(), jsonObj);
		writeParameter(ContactFields.USERFIELD12, contactobject.getUserField12(), jsonObj);
		writeParameter(ContactFields.USERFIELD13, contactobject.getUserField13(), jsonObj);
		writeParameter(ContactFields.USERFIELD14, contactobject.getUserField14(), jsonObj);
		writeParameter(ContactFields.USERFIELD15, contactobject.getUserField15(), jsonObj);
		writeParameter(ContactFields.USERFIELD16, contactobject.getUserField16(), jsonObj);
		writeParameter(ContactFields.USERFIELD17, contactobject.getUserField17(), jsonObj);
		writeParameter(ContactFields.USERFIELD18, contactobject.getUserField18(), jsonObj);
		writeParameter(ContactFields.USERFIELD19, contactobject.getUserField19(), jsonObj);
		writeParameter(ContactFields.USERFIELD20, contactobject.getUserField20(), jsonObj);
		writeParameter(ContactFields.DISTRIBUTIONLIST_FLAG, contactobject.getMarkAsDistribtuionlist(), jsonObj);
		
		final JSONArray jsonLinkArray = getLinksAsJSONArray(contactobject);
		if (jsonLinkArray != null) {
			jsonObj.put(ContactFields.LINKS, jsonLinkArray);
		}
		
		final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contactobject);
		if (jsonDistributionListArray != null) {
			jsonObj.put(ContactFields.DISTRIBUTIONLIST, jsonDistributionListArray);
		}
	}
	
	public JSONArray getLinksAsJSONArray(final ContactObject contactobject) throws JSONException {
		final LinkEntryObject[] linkentries = contactobject.getLinks();
		
		if (linkentries != null) {
			final JSONArray jsonArray = new JSONArray();
			
			for (int a = 0; a < linkentries.length; a++) {
				final JSONObject jsonLinkObject = new JSONObject();
				writeParameter(ContactFields.ID, linkentries[a].getLinkID(), jsonLinkObject);
				writeParameter(ContactFields.DISPLAY_NAME, linkentries[a].getLinkDisplayname(), jsonLinkObject);
				jsonArray.put(jsonLinkObject);
			}
			return jsonArray;
		}
		return null;
	}
	
	public JSONArray getDistributionListAsJSONArray(final ContactObject contactobject) throws JSONException {
		final DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();
		
		if (distributionlist != null) {
			final JSONArray jsonArray = new JSONArray();
			
			for (int a = 0; a < distributionlist.length; a++) {
				final JSONObject jsonDListObj = new JSONObject();
				final int emailField = distributionlist[a].getEmailfield();
				
				if (!(emailField == DistributionListEntryObject.INDEPENDENT)) {
					writeParameter(DistributionListFields.ID, distributionlist[a].getEntryID(), jsonDListObj);
				}
				
				writeParameter(DistributionListFields.MAIL, distributionlist[a].getEmailaddress(), jsonDListObj);
				writeParameter(DistributionListFields.DISPLAY_NAME, distributionlist[a].getDisplayname(), jsonDListObj);
				writeParameter(DistributionListFields.MAIL_FIELD, emailField, jsonDListObj);
				
				jsonArray.put(jsonDListObj);
			}
			return jsonArray;
		} else {
			return null;
		}
	}
	
	public void write(final int field, final ContactObject contactobject, final JSONArray jsonArray) throws JSONException {
		switch (field) {
			case ContactObject.OBJECT_ID:
				writeValue(contactobject.getObjectID(), jsonArray);
				break;
			case ContactObject.CREATED_BY:
				writeValue(contactobject.getCreatedBy(), jsonArray);
				break;
			case ContactObject.CREATION_DATE:
				writeValue(contactobject.getCreationDate().getTime(), jsonArray);
				break;
			case ContactObject.MODIFIED_BY:
				writeValue(contactobject.getModifiedBy(), jsonArray);
				break;
			case ContactObject.LAST_MODIFIED:
				writeValue(contactobject.getLastModified().getTime(), jsonArray);
				break;
			case ContactObject.FOLDER_ID:
				writeValue(contactobject.getParentFolderID(), jsonArray);
				break;
			case ContactObject.PRIVATE_FLAG:
				writeValue(contactobject.getPrivateFlag(), jsonArray);
				break;
			case ContactObject.SUR_NAME:
				writeValue(contactobject.getSurName(), jsonArray);
				break;
			case ContactObject.GIVEN_NAME:
				writeValue(contactobject.getGivenName(), jsonArray);
				break;
			case ContactObject.ANNIVERSARY:
				writeValue(contactobject.getAnniversary(), jsonArray);
				break;
			case ContactObject.ASSISTANT_NAME:
				writeValue(contactobject.getAssistantName(), jsonArray);
				break;
			case ContactObject.BIRTHDAY:
				writeValue(contactobject.getBirthday(), jsonArray);
				break;
			case ContactObject.BRANCHES:
				writeValue(contactobject.getBranches(), jsonArray);
				break;
			case ContactObject.BUSINESS_CATEGORY:
				writeValue(contactobject.getBusinessCategory(), jsonArray);
				break;
			case ContactObject.CATEGORIES:
				writeValue(contactobject.getCategories(), jsonArray);
				break;
			case ContactObject.CELLULAR_TELEPHONE1:
				writeValue(contactobject.getCellularTelephone1(), jsonArray);
				break;
			case ContactObject.CELLULAR_TELEPHONE2:
				writeValue(contactobject.getCellularTelephone2(), jsonArray);
				break;
			case ContactObject.CITY_HOME:
				writeValue(contactobject.getCityHome(), jsonArray);
				break;
			case ContactObject.CITY_BUSINESS:
				writeValue(contactobject.getCityBusiness(), jsonArray);
				break;
			case ContactObject.CITY_OTHER:
				writeValue(contactobject.getCityOther(), jsonArray);
				break;
			case ContactObject.COLOR_LABEL:
				writeValue(contactobject.getLabel(), jsonArray);
				break;
			case ContactObject.COMMERCIAL_REGISTER:
				writeValue(contactobject.getCommercialRegister(), jsonArray);
				break;
			case ContactObject.COMPANY:
				writeValue(contactobject.getCompany(), jsonArray);
				break;
			case ContactObject.COUNTRY_HOME:
				writeValue(contactobject.getCountryHome(), jsonArray);
				break;
			case ContactObject.COUNTRY_BUSINESS:
				writeValue(contactobject.getCountryBusiness(), jsonArray);
				break;
			case ContactObject.COUNTRY_OTHER:
				writeValue(contactobject.getCountryOther(), jsonArray);
				break;
			case ContactObject.DEFAULT_ADDRESS:
				writeValue(contactobject.getDefaultAddress(), jsonArray);
				break;
			case ContactObject.DEPARTMENT:
				writeValue(contactobject.getDepartment(), jsonArray);
				break;
			case ContactObject.DISPLAY_NAME:
				writeValue(contactobject.getDisplayName(), jsonArray);
				break;
			case ContactObject.MARK_AS_DISTRIBUTIONLIST:
				writeValue(contactobject.getMarkAsDistribtuionlist(), jsonArray);
				break;
			case ContactObject.EMAIL1:
				writeValue(contactobject.getEmail1(), jsonArray);
				break;
			case ContactObject.EMAIL2:
				writeValue(contactobject.getEmail2(), jsonArray);
				break;
			case ContactObject.EMAIL3:
				writeValue(contactobject.getEmail3(), jsonArray);
				break;
			case ContactObject.EMPLOYEE_TYPE:
				writeValue(contactobject.getEmployeeType(), jsonArray);
				break;
			case ContactObject.FAX_BUSINESS:
				writeValue(contactobject.getFaxBusiness(), jsonArray);
				break;
			case ContactObject.FAX_HOME:
				writeValue(contactobject.getFaxHome(), jsonArray);
				break;
			case ContactObject.FAX_OTHER:
				writeValue(contactobject.getFaxOther(), jsonArray);
				break;
			case ContactObject.IMAGE1:
				final byte[] imageData = contactobject.getImage1();
				if (imageData != null) {
					writeValue(new String(imageData), jsonArray);
				} else {
					writeValueNull(jsonArray);
				}
				break;
			case ContactObject.NUMBER_OF_IMAGES:
				writeValue(contactobject.getNumberOfImages(), jsonArray);
				break;
			case ContactObject.INFO:
				writeValue(contactobject.getInfo(), jsonArray);
				break;
			case ContactObject.INSTANT_MESSENGER1:
				writeValue(contactobject.getInstantMessenger1(), jsonArray);
				break;
			case ContactObject.INSTANT_MESSENGER2:
				writeValue(contactobject.getInstantMessenger2(), jsonArray);
				break;
			case ContactObject.INTERNAL_USERID:
				writeValue(contactobject.getInternalUserId(), jsonArray);
				break;
			case ContactObject.MANAGER_NAME:
				writeValue(contactobject.getManagerName(), jsonArray);
				break;
			case ContactObject.MARITAL_STATUS:
				writeValue(contactobject.getMaritalStatus(), jsonArray);
				break;
			case ContactObject.MIDDLE_NAME:
				writeValue(contactobject.getMiddleName(), jsonArray);
				break;
			case ContactObject.NICKNAME:
				writeValue(contactobject.getNickname(), jsonArray);
				break;
			case ContactObject.NOTE:
				writeValue(contactobject.getNote(), jsonArray);
				break;
			case ContactObject.NUMBER_OF_CHILDREN:
				writeValue(contactobject.getNumberOfChildren(), jsonArray);
				break;
			case ContactObject.NUMBER_OF_EMPLOYEE:
				writeValue(contactobject.getNumberOfEmployee(), jsonArray);
				break;
			case ContactObject.POSITION:
				writeValue(contactobject.getPosition(), jsonArray);
				break;
			case ContactObject.POSTAL_CODE_HOME:
				writeValue(contactobject.getPostalCodeHome(), jsonArray);
				break;
			case ContactObject.POSTAL_CODE_BUSINESS:
				writeValue(contactobject.getPostalCodeBusiness(), jsonArray);
				break;
			case ContactObject.POSTAL_CODE_OTHER:
				writeValue(contactobject.getPostalCodeOther(), jsonArray);
				break;
			case ContactObject.PROFESSION:
				writeValue(contactobject.getProfession(), jsonArray);
				break;
			case ContactObject.ROOM_NUMBER:
				writeValue(contactobject.getRoomNumber(), jsonArray);
				break;
			case ContactObject.SALES_VOLUME:
				writeValue(contactobject.getSalesVolume(), jsonArray);
				break;
			case ContactObject.SPOUSE_NAME:
				writeValue(contactobject.getSpouseName(), jsonArray);
				break;
			case ContactObject.STATE_HOME:
				writeValue(contactobject.getStateHome(), jsonArray);
				break;
			case ContactObject.STATE_BUSINESS:
				writeValue(contactobject.getStateBusiness(), jsonArray);
				break;
			case ContactObject.STATE_OTHER:
				writeValue(contactobject.getStateOther(), jsonArray);
				break;
			case ContactObject.STREET_HOME:
				writeValue(contactobject.getStreetHome(), jsonArray);
				break;
			case ContactObject.STREET_BUSINESS:
				writeValue(contactobject.getStreetBusiness(), jsonArray);
				break;
			case ContactObject.STREET_OTHER:
				writeValue(contactobject.getStreetOther(), jsonArray);
				break;
			case ContactObject.SUFFIX:
				writeValue(contactobject.getSuffix(), jsonArray);
				break;
			case ContactObject.TAX_ID:
				writeValue(contactobject.getTaxID(), jsonArray);
				break;
			case ContactObject.TELEPHONE_ASSISTANT:
				writeValue(contactobject.getTelephoneAssistant(), jsonArray);
				break;
			case ContactObject.TELEPHONE_BUSINESS1:
				writeValue(contactobject.getTelephoneBusiness1(), jsonArray);
				break;
			case ContactObject.TELEPHONE_BUSINESS2:
				writeValue(contactobject.getTelephoneBusiness2(), jsonArray);
				break;
			case ContactObject.TELEPHONE_CALLBACK:
				writeValue(contactobject.getTelephoneCallback(), jsonArray);
				break;
			case ContactObject.TELEPHONE_CAR:
				writeValue(contactobject.getTelephoneCar(), jsonArray);
				break;
			case ContactObject.TELEPHONE_COMPANY:
				writeValue(contactobject.getTelephoneCompany(), jsonArray);
				break;
			case ContactObject.TELEPHONE_HOME1:
				writeValue(contactobject.getTelephoneHome1(), jsonArray);
				break;
			case ContactObject.TELEPHONE_HOME2:
				writeValue(contactobject.getTelephoneHome2(), jsonArray);
				break;
			case ContactObject.TELEPHONE_IP:
				writeValue(contactobject.getTelephoneIP(), jsonArray);
				break;
			case ContactObject.TELEPHONE_ISDN:
				writeValue(contactobject.getTelephoneISDN(), jsonArray);
				break;
			case ContactObject.TELEPHONE_OTHER:
				writeValue(contactobject.getTelephoneOther(), jsonArray);
				break;
			case ContactObject.TELEPHONE_PAGER:
				writeValue(contactobject.getTelephonePager(), jsonArray);
				break;
			case ContactObject.TELEPHONE_PRIMARY:
				writeValue(contactobject.getTelephonePrimary(), jsonArray);
				break;
			case ContactObject.TELEPHONE_RADIO:
				writeValue(contactobject.getTelephoneRadio(), jsonArray);
				break;
			case ContactObject.TELEPHONE_TELEX:
				writeValue(contactobject.getTelephoneTelex(), jsonArray);
				break;
			case ContactObject.TELEPHONE_TTYTDD:
				writeValue(contactobject.getTelephoneTTYTTD(), jsonArray);
				break;
			case ContactObject.TITLE:
				writeValue(contactobject.getTitle(), jsonArray);
				break;
			case ContactObject.URL:
				writeValue(contactobject.getURL(), jsonArray);
				break;
			case ContactObject.USERFIELD01:
				writeValue(contactobject.getUserField01(), jsonArray);
				break;
			case ContactObject.USERFIELD02:
				writeValue(contactobject.getUserField02(), jsonArray);
				break;
			case ContactObject.USERFIELD03:
				writeValue(contactobject.getUserField03(), jsonArray);
				break;
			case ContactObject.USERFIELD04:
				writeValue(contactobject.getUserField04(), jsonArray);
				break;
			case ContactObject.USERFIELD05:
				writeValue(contactobject.getUserField05(), jsonArray);
				break;
			case ContactObject.USERFIELD06:
				writeValue(contactobject.getUserField06(), jsonArray);
				break;
			case ContactObject.USERFIELD07:
				writeValue(contactobject.getUserField07(), jsonArray);
				break;
			case ContactObject.USERFIELD08:
				writeValue(contactobject.getUserField08(), jsonArray);
				break;
			case ContactObject.USERFIELD09:
				writeValue(contactobject.getUserField09(), jsonArray);
				break;
			case ContactObject.USERFIELD10:
				writeValue(contactobject.getUserField10(), jsonArray);
				break;
			case ContactObject.USERFIELD11:
				writeValue(contactobject.getUserField11(), jsonArray);
				break;
			case ContactObject.USERFIELD12:
				writeValue(contactobject.getUserField12(), jsonArray);
				break;
			case ContactObject.USERFIELD13:
				writeValue(contactobject.getUserField13(), jsonArray);
				break;
			case ContactObject.USERFIELD14:
				writeValue(contactobject.getUserField14(), jsonArray);
				break;
			case ContactObject.USERFIELD15:
				writeValue(contactobject.getUserField15(), jsonArray);
				break;
			case ContactObject.USERFIELD16:
				writeValue(contactobject.getUserField16(), jsonArray);
				break;
			case ContactObject.USERFIELD17:
				writeValue(contactobject.getUserField17(), jsonArray);
				break;
			case ContactObject.USERFIELD18:
				writeValue(contactobject.getUserField18(), jsonArray);
				break;
			case ContactObject.USERFIELD19:
				writeValue(contactobject.getUserField19(), jsonArray);
				break;
			case ContactObject.USERFIELD20:
				writeValue(contactobject.getUserField20(), jsonArray);
				break;
			case ContactObject.LINKS:
				final JSONArray jsonLinksArray = getLinksAsJSONArray(contactobject);
				if (jsonLinksArray == null) {
					jsonArray.put(JSONObject.NULL);
				} else {
					jsonArray.put(jsonLinksArray);
				}
				break;
			case ContactObject.DISTRIBUTIONLIST:
				final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contactobject);
				if (jsonDistributionListArray == null) {
					jsonArray.put(JSONObject.NULL);
				} else {
					jsonArray.put(jsonDistributionListArray);
				}
				break;
			default:
				throw new JSONException("missing field in mapping: " + field);
		}
	}
}
