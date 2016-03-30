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

package com.openexchange.ajax.fields;

/**
 * Contains names of JSON attributes for contacts.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface ContactFields extends CommonFields {

    String DISPLAY_NAME = "display_name";

    String FIRST_NAME = "first_name";

    String LAST_NAME = "last_name";

    String SECOND_NAME = "second_name";

    String SUFFIX = "suffix";

    String TITLE = "title";

    String STREET_HOME = "street_home";

    String POSTAL_CODE_HOME = "postal_code_home";

    String CITY_HOME = "city_home";

    String STATE_HOME = "state_home";

    String COUNTRY_HOME = "country_home";

    String BIRTHDAY = "birthday";

    String MARITAL_STATUS = "marital_status";

    String NUMBER_OF_CHILDREN = "number_of_children";

    String PROFESSION = "profession";

    String NICKNAME = "nickname";

    String SPOUSE_NAME = "spouse_name";

    String ANNIVERSARY = "anniversary";

    String NOTE = "note";

    String DEFAULT_ADDRESS = "default_address";

    String DEPARTMENT = "department";

    String POSITION = "position";

    String EMPLOYEE_TYPE = "employee_type";

    String ROOM_NUMBER = "room_number";

    String STREET_BUSINESS = "street_business";

    String POSTAL_CODE_BUSINESS = "postal_code_business";

    String CITY_BUSINESS = "city_business";

    String STATE_BUSINESS = "state_business";

    String COUNTRY_BUSINESS = "country_business";

    String NUMBER_OF_EMPLOYEE = "number_of_employees";

    String SALES_VOLUME = "sales_volume";

    String TAX_ID = "tax_id";

    String COMMERCIAL_REGISTER = "commercial_register";

    String BRANCHES = "branches";

    String BUSINESS_CATEGORY = "busines_categorie";

    String INFO = "info";

    String MANAGER_NAME = "manager_name";

    String ASSISTANT_NAME = "assistant_name";

    String STREET_OTHER = "street_other";

    String CITY_OTHER = "city_other";

    String STATE_OTHER = "state_other";

    String POSTAL_CODE_OTHER = "postal_code_other";

    String COUNTRY_OTHER = "country_other";

    String TELEPHONE_BUSINESS1 = "telephone_business1";

    String TELEPHONE_BUSINESS2 = "telephone_business2";

    String FAX_BUSINESS = "fax_business";

    String TELEPHONE_CALLBACK = "telephone_callback";

    String TELEPHONE_CAR = "telephone_car";

    String TELEPHONE_COMPANY = "telephone_company";

    String TELEPHONE_HOME1 = "telephone_home1";

    String TELEPHONE_HOME2 = "telephone_home2";

    String FAX_HOME = "fax_home";

    String CELLULAR_TELEPHONE1 = "cellular_telephone1";

    String CELLULAR_TELEPHONE2 = "cellular_telephone2";

    String TELEPHONE_OTHER = "telephone_other";

    String FAX_OTHER = "fax_other";

    String EMAIL1 = "email1";

    String EMAIL2 = "email2";

    String EMAIL3 = "email3";

    String URL = "url";

    String TELEPHONE_ISDN = "telephone_isdn";

    String TELEPHONE_PAGER = "telephone_pager";

    String TELEPHONE_PRIMARY = "telephone_primary";

    String TELEPHONE_RADIO = "telephone_radio";

    String TELEPHONE_TELEX = "telephone_telex";

    String TELEPHONE_TTYTDD = "telephone_ttytdd";

    String INSTANT_MESSENGER1 = "instant_messenger1";

    String INSTANT_MESSENGER2 = "instant_messenger2";

    String TELEPHONE_IP = "telephone_ip";

    String TELEPHONE_ASSISTANT = "telephone_assistant";

    String COMPANY = "company";

    String IMAGE1 = "image1";

    String USERFIELD01 = "userfield01";

    String USERFIELD02 = "userfield02";

    String USERFIELD03 = "userfield03";

    String USERFIELD04 = "userfield04";

    String USERFIELD05 = "userfield05";

    String USERFIELD06 = "userfield06";

    String USERFIELD07 = "userfield07";

    String USERFIELD08 = "userfield08";

    String USERFIELD09 = "userfield09";

    String USERFIELD10 = "userfield10";

    String USERFIELD11 = "userfield11";

    String USERFIELD12 = "userfield12";

    String USERFIELD13 = "userfield13";

    String USERFIELD14 = "userfield14";

    String USERFIELD15 = "userfield15";

    String USERFIELD16 = "userfield16";

    String USERFIELD17 = "userfield17";

    String USERFIELD18 = "userfield18";

    String USERFIELD19 = "userfield19";

    String USERFIELD20 = "userfield20";

    String USER_ID = "user_id";

    String DISTRIBUTIONLIST = "distribution_list";

    String MARK_AS_DISTRIBUTIONLIST = "mark_as_distributionlist";

    String NUMBER_OF_DISTRIBUTIONLIST = "number_of_distribution_list";

    String NUMBER_OF_IMAGES = "number_of_images";

    String IMAGE1_URL = "image1_url";

    String USE_COUNT = "useCount";

    String FILE_AS = "file_as";

    String YOMI_FIRST_NAME = "yomiFirstName";

    String YOMI_LAST_NAME = "yomiLastName";

    String YOMI_COMPANY = "yomiCompany";

    String ADDRESS_HOME = "addressHome";

    String ADDRESS_BUSINESS = "addressBusiness";

    String ADDRESS_OTHER = "addressOther";

    String SORT_NAME = "sort_name";

}
