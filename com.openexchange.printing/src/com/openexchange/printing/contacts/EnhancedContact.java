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

package com.openexchange.printing.contacts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.openexchange.groupware.contact.helpers.ContactField;

public class EnhancedContact {
	
	private final Locale locale;

	public EnhancedContact(Locale locale) {
		this.locale = locale;
	}


	protected Map<String, Object> lists2contact(List<String> keys, List<String> values) {
		Map<String, Object> contact = new HashMap<String, Object>();
		
		for (int i = 0, length = keys.size(); i < length; i++) {
				contact.put(keys.get(i), values.get(i));
		}
		
		return contact ;
	}
	
	
	
	public String getFullname(List<String> ids, List<String> values) {
		return getFullname(lists2contact(ids, values));
	}
	
	@SuppressWarnings("deprecation")
	public String getFullname(Map<String, Object> contact) {
		//TODO: Create i18n version - frontend has a nice one in io.ox.contacts/util#getFullName
		if (contact.containsKey(ContactField.DISPLAY_NAME.getAjaxName())) {
            return (String) contact.get(ContactField.DISPLAY_NAME.getAjaxName());
        }
		
		if (	!(contact.containsKey(ContactField.GIVEN_NAME.getAjaxName()) || contact.containsKey(ContactField.SUR_NAME.getAjaxName()))
			&& contact.containsKey(ContactField.COMPANY.getAjaxName())) {
			return (String) contact.get(ContactField.COMPANY.getAjaxName());
		}
			
		StringBuilder bob = new StringBuilder();
		
		if (contact.containsKey(ContactField.TITLE.getAjaxName())){
			bob.append(contact.get(ContactField.TITLE.getAjaxName()));
			bob.append(" ");
		}
		if (contact.containsKey(ContactField.GIVEN_NAME.getAjaxName())) {
			bob.append(contact.get(ContactField.GIVEN_NAME.getAjaxName()));
			bob.append(" ");
		}
		if (contact.containsKey(ContactField.MIDDLE_NAME.getAjaxName())) {
			bob.append(contact.get(ContactField.MIDDLE_NAME.getAjaxName()));
			bob.append(" ");
		}
		if (contact.containsKey(ContactField.SUR_NAME.getAjaxName())) {
			bob.append(contact.get(ContactField.SUR_NAME.getAjaxName()));
			bob.append(" ");
		}
		if (contact.containsKey(ContactField.SUFFIX.getAjaxName())) {
			bob.append(contact.get(ContactField.SUFFIX.getAjaxName()));
			bob.append(" ");
		}
		if (bob.lastIndexOf(" ") > 0) {
            return bob.substring(0, bob.lastIndexOf(" ")).toString();
        }
		return "";
	}
	
	
	
	public List<String> getStandardPhones(List<String> ids, List<String> values) {
		return getStandardPhones(lists2contact(ids, values));
	}
	
	public List<String> getStandardPhones(Map<String, Object> contact) {
		List<ContactField> desiredFields = Arrays.asList(
			ContactField.TELEPHONE_BUSINESS1, 
			ContactField.TELEPHONE_BUSINESS2,
			ContactField.TELEPHONE_COMPANY,
			ContactField.TELEPHONE_HOME1,
			ContactField.TELEPHONE_HOME2);
		List<String> phones = new LinkedList<String>();
		
		for(ContactField field: desiredFields) {
			String key = String.valueOf(field.getAjaxName());
			if(contact.containsKey(key)) {
                phones.add((String) contact.get(key));
            }
		}
		
		return phones;
	}
	
	
	public List<String> getCellphones(List<String> ids, List<String> values) {
		return getCellphones(lists2contact(ids, values));
	}
	
	public List<String> getCellphones(Map<String, Object> contact) {
		List<ContactField> desiredFields = Arrays.asList(
			ContactField.CELLULAR_TELEPHONE1,
			ContactField.CELLULAR_TELEPHONE2);
		List<String> phones = new LinkedList<String>();
		
		for(ContactField field: desiredFields) {
			String key = String.valueOf(field.getAjaxName());
			if(contact.containsKey(key)) {
                phones.add((String) contact.get(key));
            }
		}
		
		return phones;
	}
}
