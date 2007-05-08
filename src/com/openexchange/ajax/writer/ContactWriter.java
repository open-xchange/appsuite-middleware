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
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * ContactWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ContactWriter extends CommonWriter {
	
	public ContactWriter(final PrintWriter w, final TimeZone timeZone) {
		jsonwriter = new JSONWriter(w);
		this.timeZone = timeZone;
	}
	
	public ContactWriter(final JSONWriter jsonwriter, final TimeZone timeZone) {
		this.jsonwriter = jsonwriter;
		this.timeZone = timeZone;
	}
	
	public void writeArray(final ContactObject contactobject, final int cols[]) throws JSONException {
		jsonwriter.array();
		
		for (int a = 0; a < cols.length; a++) {
			write(cols[a], contactobject);
		}
		
		jsonwriter.endArray();
	}
	
	public void writeContact(final ContactObject contactobject) throws JSONException {
		jsonwriter.object();
		writeCommonFields(contactobject);
		
		writeParameter(ContactFields.LAST_NAME, contactobject.getSurName());
		writeParameter(ContactFields.FIRST_NAME, contactobject.getGivenName());
		writeParameter(ContactFields.ANNIVERSARY, contactobject.getAnniversary());
		writeParameter(ContactFields.ASSISTANT_NAME, contactobject.getAssistantName());
		writeParameter(ContactFields.BIRTHDAY, contactobject.getBirthday());
		writeParameter(ContactFields.BRANCHES, contactobject.getBranches());
		writeParameter(ContactFields.BUSINESS_CATEGORY, contactobject.getBusinessCategory());
		writeParameter(ContactFields.CELLULAR_TELEPHONE1, contactobject.getCellularTelephone1());
		writeParameter(ContactFields.CELLULAR_TELEPHONE2, contactobject.getCellularTelephone2());
		writeParameter(ContactFields.CITY_HOME, contactobject.getCityHome());
		writeParameter(ContactFields.CITY_BUSINESS, contactobject.getCityBusiness());
		writeParameter(ContactFields.CITY_OTHER, contactobject.getCityOther());
		writeParameter(ContactFields.COMMERCIAL_REGISTER, contactobject.getCommercialRegister());
		writeParameter(ContactFields.COMPANY, contactobject.getCompany());
		writeParameter(ContactFields.COUNTRY_HOME, contactobject.getCountryHome());
		writeParameter(ContactFields.COUNTRY_BUSINESS, contactobject.getCountryBusiness());
		writeParameter(ContactFields.COUNTRY_OTHER, contactobject.getCountryOther());
		writeParameter(ContactFields.DEFAULT_ADDRESS, contactobject.getDefaultAddress());
		writeParameter(ContactFields.DEPARTMENT, contactobject.getDepartment());
		writeParameter(ContactFields.DISPLAY_NAME, contactobject.getDisplayName());
		writeParameter(ContactFields.EMAIL1, contactobject.getEmail1());
		writeParameter(ContactFields.EMAIL2, contactobject.getEmail2());
		writeParameter(ContactFields.EMAIL3, contactobject.getEmail3());
		writeParameter(ContactFields.EMPLOYEE_TYPE, contactobject.getEmployeeType());
		writeParameter(ContactFields.FAX_BUSINESS, contactobject.getFaxBusiness());
		writeParameter(ContactFields.FAX_HOME, contactobject.getFaxHome());
		writeParameter(ContactFields.FAX_OTHER, contactobject.getFaxOther());
		if (contactobject.containsImage1()){
			writeParameter(ContactFields.NUMBER_OF_IMAGES, 1);
		}
		//writeParameter(ContactFields.IMAGE1, contactobject.getImage1());
		writeParameter(ContactFields.INFO, contactobject.getInfo());
		writeParameter(ContactFields.NOTE, contactobject.getNote());
		writeParameter(ContactFields.INSTANT_MESSENGER1, contactobject.getInstantMessenger1());
		writeParameter(ContactFields.INSTANT_MESSENGER2, contactobject.getInstantMessenger2());
		writeParameter(ContactFields.MARITAL_STATUS, contactobject.getMaritalStatus());
		writeParameter(ContactFields.MANAGER_NAME, contactobject.getManagerName());
		writeParameter(ContactFields.SECOND_NAME, contactobject.getMiddleName());
		writeParameter(ContactFields.NICKNAME, contactobject.getNickname());
		writeParameter(ContactFields.NUMBER_OF_CHILDREN, contactobject.getNumberOfChildren());
		writeParameter(ContactFields.NUMBER_OF_EMPLOYEE, contactobject.getNumberOfEmployee());
		writeParameter(ContactFields.POSITION, contactobject.getPosition());
		writeParameter(ContactFields.POSTAL_CODE_HOME, contactobject.getPostalCodeHome());
		writeParameter(ContactFields.POSTAL_CODE_BUSINESS, contactobject.getPostalCodeBusiness());
		writeParameter(ContactFields.POSTAL_CODE_OTHER, contactobject.getPostalCodeOther());
		writeParameter(ContactFields.PROFESSION, contactobject.getProfession());
		writeParameter(ContactFields.ROOM_NUMBER, contactobject.getRoomNumber());
		writeParameter(ContactFields.SALES_VOLUME, contactobject.getSalesVolume());
		writeParameter(ContactFields.SPOUSE_NAME, contactobject.getSpouseName());
		writeParameter(ContactFields.STATE_HOME, contactobject.getStateHome());
		writeParameter(ContactFields.STATE_BUSINESS, contactobject.getStateBusiness());
		writeParameter(ContactFields.STATE_OTHER, contactobject.getStateOther());
		writeParameter(ContactFields.STREET_HOME, contactobject.getStreetHome());
		writeParameter(ContactFields.STREET_BUSINESS, contactobject.getStreetBusiness());
		writeParameter(ContactFields.STREET_OTHER, contactobject.getStreetOther());
		writeParameter(ContactFields.SUFFIX, contactobject.getSuffix());
		writeParameter(ContactFields.TAX_ID, contactobject.getTaxID());
		writeParameter(ContactFields.TELEPHONE_ASSISTANT, contactobject.getTelephoneAssistant());
		writeParameter(ContactFields.TELEPHONE_BUSINESS1, contactobject.getTelephoneBusiness1());
		writeParameter(ContactFields.TELEPHONE_BUSINESS2, contactobject.getTelephoneBusiness2());
		writeParameter(ContactFields.TELEPHONE_CALLBACK, contactobject.getTelephoneCallback());
		writeParameter(ContactFields.TELEPHONE_CAR, contactobject.getTelephoneCar());
		writeParameter(ContactFields.TELEPHONE_COMPANY, contactobject.getTelephoneCompany());
		writeParameter(ContactFields.TELEPHONE_HOME1, contactobject.getTelephoneHome1());
		writeParameter(ContactFields.TELEPHONE_HOME2, contactobject.getTelephoneHome2());
		writeParameter(ContactFields.TELEPHONE_IP, contactobject.getTelephoneIP());
		writeParameter(ContactFields.TELEPHONE_ISDN, contactobject.getTelephoneISDN());
		writeParameter(ContactFields.TELEPHONE_OTHER, contactobject.getTelephoneOther());
		writeParameter(ContactFields.TELEPHONE_PAGER, contactobject.getTelephonePager());
		writeParameter(ContactFields.TELEPHONE_PRIMARY, contactobject.getTelephonePrimary());
		writeParameter(ContactFields.TELEPHONE_RADIO, contactobject.getTelephoneRadio());
		writeParameter(ContactFields.TELEPHONE_TELEX, contactobject.getTelephoneTelex());
		writeParameter(ContactFields.TELEPHONE_TTYTDD, contactobject.getTelephoneTTYTTD());
		writeParameter(ContactFields.TITLE, contactobject.getTitle());
		writeParameter(ContactFields.URL, contactobject.getURL());
		writeParameter(ContactFields.USERFIELD01, contactobject.getUserField01());
		writeParameter(ContactFields.USERFIELD02, contactobject.getUserField02());
		writeParameter(ContactFields.USERFIELD03, contactobject.getUserField03());
		writeParameter(ContactFields.USERFIELD04, contactobject.getUserField04());
		writeParameter(ContactFields.USERFIELD05, contactobject.getUserField05());
		writeParameter(ContactFields.USERFIELD06, contactobject.getUserField06());
		writeParameter(ContactFields.USERFIELD07, contactobject.getUserField07());
		writeParameter(ContactFields.USERFIELD08, contactobject.getUserField08());
		writeParameter(ContactFields.USERFIELD09, contactobject.getUserField09());
		writeParameter(ContactFields.USERFIELD10, contactobject.getUserField10());
		writeParameter(ContactFields.USERFIELD11, contactobject.getUserField11());
		writeParameter(ContactFields.USERFIELD12, contactobject.getUserField12());
		writeParameter(ContactFields.USERFIELD13, contactobject.getUserField13());
		writeParameter(ContactFields.USERFIELD14, contactobject.getUserField14());
		writeParameter(ContactFields.USERFIELD15, contactobject.getUserField15());
		writeParameter(ContactFields.USERFIELD16, contactobject.getUserField16());
		writeParameter(ContactFields.USERFIELD17, contactobject.getUserField17());
		writeParameter(ContactFields.USERFIELD18, contactobject.getUserField18());
		writeParameter(ContactFields.USERFIELD19, contactobject.getUserField19());
		writeParameter(ContactFields.USERFIELD20, contactobject.getUserField20());
		writeParameter(ContactFields.DISTRIBUTIONLIST_FLAG, contactobject.getMarkAsDistribtuionlist());
		writeLinks(contactobject, true);
		writeDistributionList(contactobject, true);
		jsonwriter.endObject();
	}
	
	public void writeLinks(final ContactObject contactobject, final boolean isObject) throws JSONException {
		final LinkEntryObject[] linkentries = contactobject.getLinks();
		
		if (linkentries != null) {
			if (isObject) {
				jsonwriter.key(ContactFields.LINKS);
			}
			
			jsonwriter.array();
			
			for (int a = 0; a < linkentries.length; a++) {
				jsonwriter.object();
				writeParameter(ContactFields.ID, linkentries[a].getLinkID());
				writeParameter(ContactFields.DISPLAY_NAME, linkentries[a].getLinkDisplayname());
				jsonwriter.endObject();
			}
			
			jsonwriter.endArray();
		} else {
			if (!isObject) {
				jsonwriter.array();
				jsonwriter.endArray();
			}
		}
	}
	
	public void writeDistributionList(final ContactObject contactobject, final boolean isObject) throws JSONException {
		final DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();
		
		if (distributionlist != null) {
			if (isObject) {
				jsonwriter.key(ContactFields.DISTRIBUTIONLIST);
			}
			
			jsonwriter.array();
			
			for (int a = 0; a < distributionlist.length; a++) {
				jsonwriter.object();
				
				final int emailField = distributionlist[a].getEmailfield();
				
				if (!(emailField == DistributionListEntryObject.INDEPENDENT)) {
					writeParameter(DistributionListFields.ID, distributionlist[a].getEntryID());
				}
				
				writeParameter(DistributionListFields.MAIL, distributionlist[a].getEmailaddress());
				writeParameter(DistributionListFields.DISPLAY_NAME, distributionlist[a].getDisplayname());
				writeParameter(DistributionListFields.MAIL_FIELD, emailField);
				jsonwriter.endObject();
			}
			
			jsonwriter.endArray();
		} else {
			if (!isObject) {
				jsonwriter.array();
				jsonwriter.endArray();
			}
		}
	}
	
	public void write(final int field, final ContactObject contactobject) throws JSONException {
		switch (field) {
			case ContactObject.OBJECT_ID:
				writeValue(contactobject.getObjectID());
				break;
			case ContactObject.CREATED_BY:
				writeValue(contactobject.getCreatedBy());
				break;
			case ContactObject.CREATION_DATE:
				writeValue(contactobject.getCreationDate().getTime());
				break;
			case ContactObject.MODIFIED_BY:
				writeValue(contactobject.getModifiedBy());
				break;
			case ContactObject.LAST_MODIFIED:
				writeValue(contactobject.getLastModified().getTime());
				break;
			case ContactObject.FOLDER_ID:
				writeValue(contactobject.getParentFolderID());
				break;
			case ContactObject.PRIVATE_FLAG:
				writeValue(contactobject.getPrivateFlag());
				break;
			case ContactObject.SUR_NAME:
				writeValue(contactobject.getSurName());
				break;
			case ContactObject.GIVEN_NAME:
				writeValue(contactobject.getGivenName());
				break;
			case ContactObject.ANNIVERSARY:
				writeValue(contactobject.getAnniversary());
				break;
			case ContactObject.ASSISTANT_NAME:
				writeValue(contactobject.getAssistantName());
				break;
			case ContactObject.BIRTHDAY:
				writeValue(contactobject.getBirthday());
				break;
			case ContactObject.BRANCHES:
				writeValue(contactobject.getBranches());
				break;
			case ContactObject.BUSINESS_CATEGORY:
				writeValue(contactobject.getBusinessCategory());
				break;
			case ContactObject.CATEGORIES:
				writeValue(contactobject.getCategories());
				break;
			case ContactObject.CELLULAR_TELEPHONE1:
				writeValue(contactobject.getCellularTelephone1());
				break;
			case ContactObject.CELLULAR_TELEPHONE2:
				writeValue(contactobject.getCellularTelephone2());
				break;
			case ContactObject.CITY_HOME:
				writeValue(contactobject.getCityHome());
				break;
			case ContactObject.CITY_BUSINESS:
				writeValue(contactobject.getCityBusiness());
				break;
			case ContactObject.CITY_OTHER:
				writeValue(contactobject.getCityOther());
				break;
			case ContactObject.COLOR_LABEL:
				writeValue(contactobject.getLabel());
				break;
			case ContactObject.COMMERCIAL_REGISTER:
				writeValue(contactobject.getCommercialRegister());
				break;
			case ContactObject.COMPANY:
				writeValue(contactobject.getCompany());
				break;
			case ContactObject.COUNTRY_HOME:
				writeValue(contactobject.getCountryHome());
				break;
			case ContactObject.COUNTRY_BUSINESS:
				writeValue(contactobject.getCountryBusiness());
				break;
			case ContactObject.COUNTRY_OTHER:
				writeValue(contactobject.getCountryOther());
				break;
			case ContactObject.DEFAULT_ADDRESS:
				writeValue(contactobject.getDefaultAddress());
				break;
			case ContactObject.DEPARTMENT:
				writeValue(contactobject.getDepartment());
				break;
			case ContactObject.DISPLAY_NAME:
				writeValue(contactobject.getDisplayName());
				break;
			case ContactObject.MARK_AS_DISTRIBUTIONLIST:
				writeValue(contactobject.getMarkAsDistribtuionlist());
				break;
			case ContactObject.EMAIL1:
				writeValue(contactobject.getEmail1());
				break;
			case ContactObject.EMAIL2:
				writeValue(contactobject.getEmail2());
				break;
			case ContactObject.EMAIL3:
				writeValue(contactobject.getEmail3());
				break;
			case ContactObject.EMPLOYEE_TYPE:
				writeValue(contactobject.getEmployeeType());
				break;
			case ContactObject.FAX_BUSINESS:
				writeValue(contactobject.getFaxBusiness());
				break;
			case ContactObject.FAX_HOME:
				writeValue(contactobject.getFaxHome());
				break;
			case ContactObject.FAX_OTHER:
				writeValue(contactobject.getFaxOther());
				break;
			case ContactObject.IMAGE1:
				writeValue(new String(contactobject.getImage1()));
				break;
				/*
			case ContactObject.NUMBER_OF_IMAGES:
				writeValue(contactobject.getNumberOfImages());
				break;
				*/
			case ContactObject.INFO:
				writeValue(contactobject.getInfo());
				break;
			case ContactObject.INSTANT_MESSENGER1:
				writeValue(contactobject.getInstantMessenger1());
				break;
			case ContactObject.INSTANT_MESSENGER2:
				writeValue(contactobject.getInstantMessenger2());
				break;
			case ContactObject.INTERNAL_USERID:
				writeValue(contactobject.getInternalUserId());
				break;
			case ContactObject.MANAGER_NAME:
				writeValue(contactobject.getManagerName());
				break;
			case ContactObject.MARITAL_STATUS:
				writeValue(contactobject.getMaritalStatus());
				break;
			case ContactObject.MIDDLE_NAME:
				writeValue(contactobject.getMiddleName());
				break;
			case ContactObject.NICKNAME:
				writeValue(contactobject.getNickname());
				break;
			case ContactObject.NOTE:
				writeValue(contactobject.getNote());
				break;
			case ContactObject.NUMBER_OF_CHILDREN:
				writeValue(contactobject.getNumberOfChildren());
				break;
			case ContactObject.NUMBER_OF_EMPLOYEE:
				writeValue(contactobject.getNumberOfEmployee());
				break;
			case ContactObject.POSITION:
				writeValue(contactobject.getPosition());
				break;
			case ContactObject.POSTAL_CODE_HOME:
				writeValue(contactobject.getPostalCodeHome());
				break;
			case ContactObject.POSTAL_CODE_BUSINESS:
				writeValue(contactobject.getPostalCodeBusiness());
				break;
			case ContactObject.POSTAL_CODE_OTHER:
				writeValue(contactobject.getPostalCodeOther());
				break;
			case ContactObject.PROFESSION:
				writeValue(contactobject.getProfession());
				break;
			case ContactObject.ROOM_NUMBER:
				writeValue(contactobject.getRoomNumber());
				break;
			case ContactObject.SALES_VOLUME:
				writeValue(contactobject.getSalesVolume());
				break;
			case ContactObject.SPOUSE_NAME:
				writeValue(contactobject.getSpouseName());
				break;
			case ContactObject.STATE_HOME:
				writeValue(contactobject.getStateHome());
				break;
			case ContactObject.STATE_BUSINESS:
				writeValue(contactobject.getStateBusiness());
				break;
			case ContactObject.STATE_OTHER:
				writeValue(contactobject.getStateOther());
				break;
			case ContactObject.STREET_HOME:
				writeValue(contactobject.getStreetHome());
				break;
			case ContactObject.STREET_BUSINESS:
				writeValue(contactobject.getStreetBusiness());
				break;
			case ContactObject.STREET_OTHER:
				writeValue(contactobject.getStreetOther());
				break;
			case ContactObject.SUFFIX:
				writeValue(contactobject.getSuffix());
				break;
			case ContactObject.TAX_ID:
				writeValue(contactobject.getTaxID());
				break;
			case ContactObject.TELEPHONE_ASSISTANT:
				writeValue(contactobject.getTelephoneAssistant());
				break;
			case ContactObject.TELEPHONE_BUSINESS1:
				writeValue(contactobject.getTelephoneBusiness1());
				break;
			case ContactObject.TELEPHONE_BUSINESS2:
				writeValue(contactobject.getTelephoneBusiness2());
				break;
			case ContactObject.TELEPHONE_CALLBACK:
				writeValue(contactobject.getTelephoneCallback());
				break;
			case ContactObject.TELEPHONE_CAR:
				writeValue(contactobject.getTelephoneCar());
				break;
			case ContactObject.TELEPHONE_COMPANY:
				writeValue(contactobject.getTelephoneCompany());
				break;
			case ContactObject.TELEPHONE_HOME1:
				writeValue(contactobject.getTelephoneHome1());
				break;
			case ContactObject.TELEPHONE_HOME2:
				writeValue(contactobject.getTelephoneHome2());
				break;
			case ContactObject.TELEPHONE_IP:
				writeValue(contactobject.getTelephoneIP());
				break;
			case ContactObject.TELEPHONE_ISDN:
				writeValue(contactobject.getTelephoneISDN());
				break;
			case ContactObject.TELEPHONE_OTHER:
				writeValue(contactobject.getTelephoneOther());
				break;
			case ContactObject.TELEPHONE_PAGER:
				writeValue(contactobject.getTelephonePager());
				break;
			case ContactObject.TELEPHONE_PRIMARY:
				writeValue(contactobject.getTelephonePrimary());
				break;
			case ContactObject.TELEPHONE_RADIO:
				writeValue(contactobject.getTelephoneRadio());
				break;
			case ContactObject.TELEPHONE_TELEX:
				writeValue(contactobject.getTelephoneTelex());
				break;
			case ContactObject.TELEPHONE_TTYTDD:
				writeValue(contactobject.getTelephoneTTYTTD());
				break;
			case ContactObject.TITLE:
				writeValue(contactobject.getTitle());
				break;
			case ContactObject.URL:
				writeValue(contactobject.getURL());
				break;
			case ContactObject.USERFIELD01:
				writeValue(contactobject.getUserField01());
				break;
			case ContactObject.USERFIELD02:
				writeValue(contactobject.getUserField02());
				break;
			case ContactObject.USERFIELD03:
				writeValue(contactobject.getUserField03());
				break;
			case ContactObject.USERFIELD04:
				writeValue(contactobject.getUserField04());
				break;
			case ContactObject.USERFIELD05:
				writeValue(contactobject.getUserField05());
				break;
			case ContactObject.USERFIELD06:
				writeValue(contactobject.getUserField06());
				break;
			case ContactObject.USERFIELD07:
				writeValue(contactobject.getUserField07());
				break;
			case ContactObject.USERFIELD08:
				writeValue(contactobject.getUserField08());
				break;
			case ContactObject.USERFIELD09:
				writeValue(contactobject.getUserField09());
				break;
			case ContactObject.USERFIELD10:
				writeValue(contactobject.getUserField10());
				break;
			case ContactObject.USERFIELD11:
				writeValue(contactobject.getUserField11());
				break;
			case ContactObject.USERFIELD12:
				writeValue(contactobject.getUserField12());
				break;
			case ContactObject.USERFIELD13:
				writeValue(contactobject.getUserField13());
				break;
			case ContactObject.USERFIELD14:
				writeValue(contactobject.getUserField14());
				break;
			case ContactObject.USERFIELD15:
				writeValue(contactobject.getUserField15());
				break;
			case ContactObject.USERFIELD16:
				writeValue(contactobject.getUserField16());
				break;
			case ContactObject.USERFIELD17:
				writeValue(contactobject.getUserField17());
				break;
			case ContactObject.USERFIELD18:
				writeValue(contactobject.getUserField18());
				break;
			case ContactObject.USERFIELD19:
				writeValue(contactobject.getUserField19());
				break;
			case ContactObject.USERFIELD20:
				writeValue(contactobject.getUserField20());
				break;
			case ContactObject.LINKS:
				writeLinks(contactobject, false);
				break;
			case ContactObject.DISTRIBUTIONLIST:
				writeDistributionList(contactobject, false);
				break;
			default:
				throw new JSONException("missing field in mapping: " + field);
		}
	}
}
