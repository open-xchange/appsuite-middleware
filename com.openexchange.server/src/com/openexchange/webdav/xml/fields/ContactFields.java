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

package com.openexchange.webdav.xml.fields;

public interface ContactFields extends CommonFields {

    String DISPLAY_NAME = "displayname";
    String LAST_NAME = "last_name";
    String FIRST_NAME = "first_name";
    String SECOND_NAME = "second_name";
    String SUFFIX = "suffix";
    String TITLE = "title";
    String STREET = "street";
    String POSTAL_CODE = "postal_code";
    String CITY = "city";
    String STATE = "state";
    String COUNTRY = "country";
    String BIRTHDAY = "birthday";
    String MARTITAL_STATUS = "martital_status";
    String NUMBER_OF_CHILDREN = "number_of_children";
    String PROFESSION = "profession";
    String NICKNAME = "nickname";
    String SPOUSE_NAME = "spouse_name";
    String ANNIVERSARY = "anniversary";
    String NOTE = "note";
    String DEPARTMENT = "department";
    String POSITION = "position";
    String EMPLOYEE_TYPE = "employee_type";
    String ROOM_NUMBER = "room_number";
    String BUSINESS_STREET = "business_street";
    String BUSINESS_POSTAL_CODE = "business_postal_code";
    String BUSINESS_CITY = "business_city";
    String BUSINESS_STATE = "business_state";
    String BUSINESS_COUNTRY = "business_country";
    String NUMBER_OF_EMPLOYEE = "number_of_employee";
    String SALES_VOLUME = "sales_volume";
    String TAX_ID = "tax_id";
    String COMMERCIAL_REGISTER = "commercial_register";
    String BRANCHES = "branches";
    String BUSINESS_CATEGORY = "business_category";
    String MORE_INFO = "more_info";
    String MANAGERS_NAME = "managers_name";
    String ASSISTANTS_NAME = "assistants_name";

    String SECOND_STREET = "second_street";
    String SECOND_POSTAL_CODE = "second_postal_code";
    String SECOND_CITY = "second_city";
    String SECOND_STATE = "second_state";
    String SECOND_COUNTRY = "second_country";

    String PHONE_BUSINESS = "phone_business";
    String PHONE_BUSINESS2 = "phone_business2";
    String FAX_BUSINESS = "fax_business";
    String CALLBACK = "callback";
    String PHONE_CAR = "phone_car";
    String PHONE_COMPANY = "phone_company";
    String PHONE_HOME = "phone_home";
    String PHONE_HOME2 = "phone_home2";
    String FAX_HOME = "fax_home";
    String MOBILE1 = "mobile1";
    String MOBILE2 = "mobile2";
    String PHONE_OTHER = "phone_other";
    String FAX_OTHER = "fax_other";
    String FILE_AS = com.openexchange.ajax.fields.ContactFields.FILE_AS;
    String EMAIL1 = "email1";
    String EMAIL2 = "email2";
    String EMAIL3 = "email3";
    String URL = "url";

    String ISDN = "isdn";
    String PAGER = "pager";
    String PRIMARY = "primary";
    String RADIO = "radio";
    String TELEX = "telex";
    String TTY_TDD = "tty_tdd";
    String INSTANT_MESSENGER = "instant_messenger";
    String INSTANT_MESSENGER2 = "instant_messenger2";
    String IP_PHONE = "ip_phone";

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

    String PHONE_ASSISTANT = "phone_assistant";
    String DEFAULTADDRESS = "defaultaddress";

    String COMPANY = "company";

    String DISTRIBUTIONLIST = "distributionlist";
    String DISTRIBUTIONLIST_FLAG = "distributionlist_flag";
    String LINKS = "links";
    String LINK_FLAG = "link_flag";

    String IMAGE1 = "image1";

    String IMAGE_CONTENT_TYPE = "image_content_type";

}
