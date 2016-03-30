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
///**
// *
// */
//package com.openexchange.admin.tools;
//
//import java.util.Date;
//import java.util.HashSet;
//import java.util.Hashtable;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import com.openexchange.admin.dataSource.I_OXUser;
//import com.openexchange.admin.rmi.dataobjects.User;
//
///**
// * @author choeger
// *
// */
//public class DataConverter {
//	public static User userHashtable2UserObject(final Hashtable<String, Comparable> ht) {
//		User u = new User();
//		for(String key : ht.keySet() ) {
//			Object value = ht.get(key);
//			if( key.equals(I_OXUser.UID) ) {
//				u.setUsername((String)value);
//			} else if( key.equals(I_OXUser.UID_NUMBER) ) {
//				u.setId((Integer)value);
//			} else if( key.equals(I_OXUser.ENABLED) ) {
//				u.setEnabled((Boolean)value);
//			} else if( key.equals(I_OXUser.PASSWORD_EXPIRED) ) {
//				u.setPassword_expired((Boolean)value);
//			} else if( key.equals(I_OXUser.PASSWORD) ) {
//				u.setPassword((String)value);
//			} else if( key.equals(I_OXUser.ALIAS) ) {
//				HashSet<String> aliases = new HashSet<String>();
//				for(String alias : ((String[])value)) {
//					aliases.add(alias);
//				}
//				u.setAliases(aliases);
//			} else if( key.equals(I_OXUser.IMAP_SERVER) ) {
//				u.setImapServer((String)value);
//			} else if( key.equals(I_OXUser.SMTP_SERVER) ) {
//				u.setSmtpServer((String)value);
//			} else if( key.equals(I_OXUser.TIMEZONE) ) {
//				u.setTimezone(TimeZone.getTimeZone((String)value));
//			} else if( key.equals(I_OXUser.LANGUAGE) ) {
//				String []loc = ((String)value).split("_");
//				Locale locale = new Locale(loc[0], loc[1]);
//				u.setLanguage(locale);
//			} else if( key.equals(I_OXUser.DEFAULT_GROUP) ) {
//				// TODO: how should that be done?!?
//			} else if( key.equals(I_OXUser.PRIMARY_MAIL) ) {
//				u.setPrimaryEmail((String)value);
//			} else if( key.equals(I_OXUser.STREET_BUSINESS) ) {
//				u.setStreet_business((String)value);
//			} else if( key.equals(I_OXUser.POSTAL_CODE_BUSINESS) ) {
//				u.setPostal_code_business((String)value);
//			} else if( key.equals(I_OXUser.CITY_BUSINESS) ) {
//				u.setCity_business((String)value);
//			} else if( key.equals(I_OXUser.STATE_BUSINESS) ) {
//				u.setState_business((String)value);
//			} else if( key.equals(I_OXUser.COUNTRY_BUSINESS) ) {
//				u.setCountry_business((String)value);
//			} else if( key.equals(I_OXUser.BUSINESS_CATEGORY) ) {
//				u.setBusiness_category((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_BUSINESS1) ) {
//				u.setTelephone_business1((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_BUSINESS2) ) {
//				u.setTelephone_business2((String)value);
//			} else if( key.equals(I_OXUser.FAX_BUSINESS) ) {
//				u.setFax_business((String)value);
//			} else if( key.equals(I_OXUser.STREET_OTHER) ) {
//				u.setState_other((String)value);
//			} else if( key.equals(I_OXUser.CITY_OTHER) ) {
//				u.setCity_other((String)value);
//			} else if( key.equals(I_OXUser.POSTAL_CODE_OTHER) ) {
//				u.setPostal_code_other((String)value);
//			} else if( key.equals(I_OXUser.COUNTRY_OTHER) ) {
//				u.setCountry_other((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_OTHER) ) {
//				u.setTelephone_other((String)value);
//			} else if( key.equals(I_OXUser.FAX_OTHER) ) {
//				u.setFax_other((String)value);
//			} else if( key.equals(I_OXUser.STATE_OTHER) ) {
//				u.setState_other((String)value);
//			} else if( key.equals(I_OXUser.GIVEN_NAME) ) {
//				u.setGiven_name((String)value);
//			} else if( key.equals(I_OXUser.SUR_NAME) ) {
//				u.setSur_name((String)value);
//			} else if( key.equals(I_OXUser.DISPLAY_NAME) ) {
//				u.setDisplay_name((String)value);
//			} else if( key.equals(I_OXUser.MIDDLE_NAME) ) {
//				u.setMiddle_name((String)value);
//			} else if( key.equals(I_OXUser.SUFFIX) ) {
//				u.setSuffix((String)value);
//			} else if( key.equals(I_OXUser.TITLE) ) {
//				u.setTitle((String)value);
//			} else if( key.equals(I_OXUser.STREET_HOME) ) {
//				u.setStreet_home((String)value);
//			} else if( key.equals(I_OXUser.POSTAL_CODE_HOME) ) {
//				u.setPostal_code_home((String)value);
//			} else if( key.equals(I_OXUser.CITY_HOME) ) {
//				u.setCity_home((String)value);
//			} else if( key.equals(I_OXUser.STATE_HOME) ) {
//				u.setState_home((String)value);
//			} else if( key.equals(I_OXUser.COUNTRY_HOME) ) {
//				u.setCountry_home((String)value);
//			} else if( key.equals(I_OXUser.BIRTHDAY) ) {
//				u.setBirthday((Date)value);
//			} else if( key.equals(I_OXUser.MARITAL_STATUS) ) {
//				u.setMarital_status((String)value);
//			} else if( key.equals(I_OXUser.NUMBER_OF_CHILDREN) ) {
//				u.setNumber_of_children((String)value);
//			} else if( key.equals(I_OXUser.PROFESSION) ) {
//				u.setProfession((String)value);
//			} else if( key.equals(I_OXUser.NICKNAME) ) {
//				u.setNickname((String)value);
//			} else if( key.equals(I_OXUser.SPOUSE_NAME) ) {
//				u.setSpouse_name((String)value);
//			} else if( key.equals(I_OXUser.ANNIVERSARY) ) {
//				u.setAnniversary((Date)value);
//			} else if( key.equals(I_OXUser.NOTE) ) {
//				u.setNote((String)value);
//			} else if( key.equals(I_OXUser.DEPARTMENT) ) {
//				u.setDepartment((String)value);
//			} else if( key.equals(I_OXUser.POSITION) ) {
//				u.setPosition((String)value);
//			} else if( key.equals(I_OXUser.EMPLOYEE_TYPE) ) {
//				u.setEmployeeType((String)value);
//			} else if( key.equals(I_OXUser.ROOM_NUMBER) ) {
//				u.setRoom_number((String)value);
//			} else if( key.equals(I_OXUser.NUMBER_OF_EMPLOYEE) ) {
//				u.setNumber_of_employee((String)value);
//			} else if( key.equals(I_OXUser.SALES_VOLUME) ) {
//				u.setSales_volume((String)value);
//			} else if( key.equals(I_OXUser.TAX_ID) ) {
//				u.setTax_id((String)value);
//			} else if( key.equals(I_OXUser.COMMERCIAL_REGISTER) ) {
//				u.setCommercial_register((String)value);
//			} else if( key.equals(I_OXUser.BRANCHES) ) {
//				u.setBranches((String)value);
//			} else if( key.equals(I_OXUser.INFO) ) {
//				u.setInfo((String)value);
//			} else if( key.equals(I_OXUser.MANAGER_NAME) ) {
//				u.setManager_name((String)value);
//			} else if( key.equals(I_OXUser.ASSISTANT_NAME) ) {
//				u.setAssistant_name((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_CALLBACK) ) {
//				u.setTelephone_callback((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_CAR) ) {
//				u.setTelephone_car((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_COMPANY) ) {
//				u.setTelephone_company((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_HOME1) ) {
//				u.setTelephone_home1((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_HOME2) ) {
//				u.setTelephone_home2((String)value);
//			} else if( key.equals(I_OXUser.FAX_HOME) ) {
//				u.setFax_home((String)value);
//			} else if( key.equals(I_OXUser.CELLULAR_TELEPHONE1) ) {
//				u.setCellular_telephone1((String)value);
//			} else if( key.equals(I_OXUser.CELLULAR_TELEPHONE2) ) {
//				u.setCellular_telephone2((String)value);
//			} else if( key.equals(I_OXUser.EMAIL1) ) {
//				u.setEmail1((String)value);
//			} else if( key.equals(I_OXUser.EMAIL2) ) {
//				u.setEmail2((String)value);
//			} else if( key.equals(I_OXUser.EMAIL3) ) {
//				u.setEmail3((String)value);
//			} else if( key.equals(I_OXUser.URL) ) {
//				u.setUrl((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_ISDN) ) {
//				u.setTelephone_isdn((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_PAGER) ) {
//				u.setTelephone_pager((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_PRIMARY) ) {
//				u.setTelephone_primary((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_RADIO) ) {
//				u.setTelephone_radio((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_TELEX) ) {
//				u.setTelephone_telex((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_TTYTDD) ) {
//				u.setTelephone_ttytdd((String)value);
//			} else if( key.equals(I_OXUser.INSTANT_MESSENGER1) ) {
//				u.setInstant_messenger1((String)value);
//			} else if( key.equals(I_OXUser.INSTANT_MESSENGER2) ) {
//				u.setInstant_messenger2((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_IP) ) {
//				u.setTelephone_ip((String)value);
//			} else if( key.equals(I_OXUser.TELEPHONE_ASSISTANT) ) {
//				u.setTelephone_assistant((String)value);
//			} else if( key.equals(I_OXUser.COMPANY) ) {
//				u.setCompany((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD01) ) {
//				u.setUserfield01((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD02) ) {
//				u.setUserfield02((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD03) ) {
//				u.setUserfield03((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD04) ) {
//				u.setUserfield04((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD05) ) {
//				u.setUserfield05((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD06) ) {
//				u.setUserfield06((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD07) ) {
//				u.setUserfield07((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD08) ) {
//				u.setUserfield08((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD09) ) {
//				u.setUserfield09((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD10) ) {
//				u.setUserfield10((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD11) ) {
//				u.setUserfield11((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD12) ) {
//				u.setUserfield12((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD13) ) {
//				u.setUserfield13((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD14) ) {
//				u.setUserfield14((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD15) ) {
//				u.setUserfield15((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD16) ) {
//				u.setUserfield16((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD17) ) {
//				u.setUserfield17((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD18) ) {
//				u.setUserfield18((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD19) ) {
//				u.setUserfield19((String)value);
//			} else if( key.equals(I_OXUser.USERFIELD20) ) {
//				u.setUserfield20((String)value);
//			}
//		}
//		return u;
//	}
//}
