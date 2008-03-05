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
package com.openexchange.ajax.fields;

public interface ContactFields extends CommonFields {
	
	public static final String DISPLAY_NAME = "display_name";
	
	public static final String FIRST_NAME = "first_name";
	
	public static final String LAST_NAME = "last_name";
	
	public static final String SECOND_NAME = "second_name";
	
	public static final String SUFFIX = "suffix";	

	public static final String TITLE = "title";	

	public static final String STREET_HOME = "street_home";	

	public static final String POSTAL_CODE_HOME = "postal_code_home";	

	public static final String CITY_HOME = "city_home";	

	public static final String STATE_HOME = "state_home";
	
	public static final String COUNTRY_HOME = "country_home";
	
	public static final String BIRTHDAY = "birthday";
	
	public static final String MARITAL_STATUS = "marital_status";
	
	public static final String NUMBER_OF_CHILDREN = "number_of_children";
	
	public static final String PROFESSION = "profession";
	
	public static final String NICKNAME = "nickname";
	
	public static final String SPOUSE_NAME = "spouse_name";
	
	public static final String ANNIVERSARY = "anniversary";
	
	public static final String NOTE = "note";
	
	public static final String DEFAULT_ADDRESS = "default_address";

	public static final String DEPARTMENT = "department";
	
	public static final String POSITION = "position";
	
	public static final String EMPLOYEE_TYPE = "employee_type";
	
	public static final String ROOM_NUMBER = "room_number";
	
	public static final String STREET_BUSINESS = "street_business";
	
	public static final String POSTAL_CODE_BUSINESS = "postal_code_business";
	
	public static final String CITY_BUSINESS = "city_business";
	
	public static final String STATE_BUSINESS = "state_business";
	
	public static final String COUNTRY_BUSINESS = "country_business";
	
	public static final String NUMBER_OF_EMPLOYEE = "number_of_employees";
	
	public static final String SALES_VOLUME = "sales_volume";
	
	public static final String TAX_ID = "tax_id";
	
	public static final String COMMERCIAL_REGISTER = "commercial_register";
	
	public static final String BRANCHES = "branches";
	
	public static final String BUSINESS_CATEGORY = "busines_categorie";
	
	public static final String INFO = "info";
	
	public static final String MANAGER_NAME = "manager_name";
	
	public static final String ASSISTANT_NAME = "assistant_name";
	
	public static final String STREET_OTHER = "street_other";
	
	public static final String CITY_OTHER = "city_other";

	public static final String STATE_OTHER = "state_other";
	
	public static final String POSTAL_CODE_OTHER = "postal_code_other";
	
	public static final String COUNTRY_OTHER = "country_other";
	
	public static final String TELEPHONE_BUSINESS1 = "telephone_business1";
	
	public static final String TELEPHONE_BUSINESS2 = "telephone_business2";
	
	public static final String FAX_BUSINESS = "fax_business";
	
	public static final String TELEPHONE_CALLBACK = "telephone_callback";
	
	public static final String TELEPHONE_CAR = "telephone_car";
	
	public static final String TELEPHONE_COMPANY = "telephone_company";
	
	public static final String TELEPHONE_HOME1 = "telephone_home1";
	
	public static final String TELEPHONE_HOME2 = "telephone_home2";
	
	public static final String FAX_HOME = "fax_home";
	
	public static final String CELLULAR_TELEPHONE1 = "cellular_telephone1";
	
	public static final String CELLULAR_TELEPHONE2 = "cellular_telephone2";
	
	public static final String TELEPHONE_OTHER = "telephone_other";
	
	public static final String FAX_OTHER = "fax_other";
	
	public static final String EMAIL1 = "email1";
	
	public static final String EMAIL2 = "email2";
	
	public static final String EMAIL3 = "email3";
	
	public static final String URL = "url";
	
	public static final String TELEPHONE_ISDN = "telephone_isdn";
	
	public static final String TELEPHONE_PAGER = "telephone_pager";
	
	public static final String TELEPHONE_PRIMARY = "telephone_primary";
	
	public static final String TELEPHONE_RADIO = "telephone_radio";
	
	public static final String TELEPHONE_TELEX = "telephone_telex";
	
	public static final String TELEPHONE_TTYTDD = "telephone_ttytdd";
	
	public static final String INSTANT_MESSENGER1 = "instant_messenger1";
	
	public static final String INSTANT_MESSENGER2 = "instant_messenger2";
	
	public static final String TELEPHONE_IP = "telephone_ip";
	
	public static final String TELEPHONE_ASSISTANT = "telephone_assistant";
	
	public static final String COMPANY = "company";
	
	public static final String IMAGE1 = "image1";
	
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

	public static final String USER_ID = "user_id";	

	public static final String LINKS = "links";

	public static final String DISTRIBUTIONLIST = "distribution_list";	
	
	public static final String DISTRIBUTIONLIST_FLAG = "distribution_list_flag";
	
	public static final String NUMBER_OF_DISTRIBUTIONLIST = "number_of_distribution_list";	

	public static final String NUMBER_OF_LINKS = "number_of_links";
	
	public static final String NUMBER_OF_IMAGES = "number_of_image";
		
}
