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



package com.openexchange.webdav.xml.fields;

public interface ContactFields extends CommonFields {
	
	public static final String DISPLAY_NAME = "displayname";
	public static final String LAST_NAME = "last_name";
	public static final String FIRST_NAME = "first_name";
	public static final String SECOND_NAME = "second_name";
	public static final String SUFFIX = "suffix";
	public static final String TITLE = "title";
	public static final String STREET = "street";
	public static final String POSTAL_CODE = "postal_code";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String BIRTHDAY = "birthday";
	public static final String MARTITAL_STATUS = "martital_status";
	public static final String NUMBER_OF_CHILDREN = "number_of_children";
	public static final String PROFESSION = "profession";
	public static final String NICKNAME = "nickname";
	public static final String SPOUSE_NAME = "spouse_name";
	public static final String ANNIVERSARY = "anniversary";
	public static final String NOTE = "note";
	public static final String DEPARTMENT = "department";
	public static final String POSITION = "position";
	public static final String EMPLOYEE_TYPE = "employee_type";
	public static final String ROOM_NUMBER = "room_number";
	public static final String BUSINESS_STREET = "business_street";
	public static final String BUSINESS_POSTAL_CODE = "business_postal_code";
	public static final String BUSINESS_CITY = "business_city";
	public static final String BUSINESS_STATE = "business_state";
	public static final String BUSINESS_COUNTRY = "business_country";
	public static final String NUMBER_OF_EMPLOYEE = "number_of_employee";
	public static final String SALES_VOLUME = "sales_volume";
	public static final String TAX_ID = "tax_id";
	public static final String COMMERCIAL_REGISTER = "commercial_register";
	public static final String BRANCHES = "branches";
	public static final String BUSINESS_CATEGORY = "business_category";
	public static final String MORE_INFO = "more_info";
	public static final String MANAGERS_NAME = "managers_name";
	public static final String ASSISTANTS_NAME = "assistants_name";
	
	public static final String SECOND_STREET = "second_street";
	public static final String SECOND_POSTAL_CODE = "second_postal_code";
	public static final String SECOND_CITY = "second_city";
	public static final String SECOND_STATE = "second_state";
	public static final String SECOND_COUNTRY = "second_country";
	
	public static final String PHONE_BUSINESS = "phone_business";
	public static final String PHONE_BUSINESS2 = "phone_business2";
	public static final String FAX_BUSINESS = "fax_business";
	public static final String CALLBACK = "callback";
	public static final String PHONE_CAR = "phone_car";
	public static final String PHONE_COMPANY = "phone_company";
	public static final String PHONE_HOME = "phone_home";
	public static final String PHONE_HOME2 = "phone_home2";
	public static final String FAX_HOME = "fax_home";
	public static final String MOBILE1 = "mobile1";
	public static final String MOBILE2 = "mobile2";
	public static final String PHONE_OTHER = "phone_other";
	public static final String FAX_OTHER = "fax_other";
	public static final String FILE_AS = "fileas";
	public static final String EMAIL1 = "email1";
	public static final String EMAIL2 = "email2";
	public static final String EMAIL3 = "email3";
	public static final String URL = "url";
	
	public static final String ISDN = "isdn";
	public static final String PAGER = "pager";
	public static final String PRIMARY = "primary";
	public static final String RADIO = "radio";
	public static final String TELEX = "telex";
	public static final String TTY_TDD = "tty_tdd";
	public static final String INSTANT_MESSENGER = "instant_messenger";
	public static final String INSTANT_MESSENGER2 = "instant_messenger2";
	public static final String IP_PHONE = "ip_phone";
	
	public static final String USERFIELD01 = "userfield01";
	public static final String USERFIELD02 = "userfield02";
	public static final String USERFIELD03 = "userfield03";
	public static final String USERFIELD04 = "userfield04";
	public static final String USERFIELD05 = "userfield05";
	public static final String USERFIELD06 = "userfield06";
	public static final String USERFIELD07 = "userfield07";
	public static final String USERFIELD08 = "userfield08";
	public static final String USERFIELD09 = "userfield09";
	public static final String USERFIELD10 = "userfield10";
	public static final String USERFIELD11 = "userfield11";
	public static final String USERFIELD12 = "userfield12";
	public static final String USERFIELD13 = "userfield13";
	public static final String USERFIELD14 = "userfield14";
	public static final String USERFIELD15 = "userfield15";
	public static final String USERFIELD16 = "userfield16";
	public static final String USERFIELD17 = "userfield17";
	public static final String USERFIELD18 = "userfield18";
	public static final String USERFIELD19 = "userfield19";
	public static final String USERFIELD20 = "userfield20";
	
	public static final String PHONE_ASSISTANT = "phone_assistant";
	public static final String DEFAULTADDRESS = "defaultaddress";
	
	public static final String COMPANY = "company";
	
	public static final String DISTRIBUTIONLIST = "distributionlist";
	public static final String DISTRIBUTIONLIST_FLAG = "distributionlist_flag";
	public static final String LINKS = "links";
	public static final String LINK_FLAG = "link_flag";
	
	public static final String IMAGE1 = "image1";

	public static final String IMAGE_CONTENT_TYPE = "image_content_type";
		
}
