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
package com.openexchange.subscribe.microformats.transformers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.openexchange.groupware.container.ContactObject;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MapToContactObjectTransformer implements MapToObjectTransformer{
	
    private static final DateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
    
	public List<ContactObject> transform (List<Map<String, String>> inlist){
		ArrayList<ContactObject> outlist = new ArrayList<ContactObject>();
		
		for (Map<String,String> map : inlist){
		ContactObject contact = new ContactObject();	
			if (map.containsKey("ox_display_name")){
				contact.setDisplayName((String)map.get("ox_display_name"));
			}  
			if (map.containsKey("ox_first_name")){
				contact.setGivenName((String)map.get("ox_first_name"));
			}  
			if (map.containsKey("ox_last_name")){
				contact.setSurName((String)map.get("ox_last_name"));
			}  
			if (map.containsKey("ox_second_name")){
				contact.setMiddleName((String)map.get("ox_second_name"));
			}  
			if (map.containsKey("ox_suffix")){
				contact.setSuffix((String)map.get("ox_suffix"));
			}  
			if (map.containsKey("ox_title")){
				contact.setTitle((String)map.get("ox_title"));
			}  
			if (map.containsKey("ox_street_home")){
				contact.setStreetHome((String)map.get("ox_street_home"));
			}  
			if (map.containsKey("ox_postal_code_home")){
				contact.setPostalCodeHome((String)map.get("ox_postal_code_home"));
			}  
			if (map.containsKey("ox_city_home")){
				contact.setCityHome((String)map.get("ox_city_home"));
			}  
			if (map.containsKey("ox_state_home")){
				contact.setStateHome((String)map.get("ox_state_home"));
			}  
			if (map.containsKey("ox_country_home")){
				contact.setCountryHome((String)map.get("ox_country_home"));
			} 
			if (map.containsKey("ox_street_business")){
				contact.setStreetBusiness((String)map.get("ox_street_business"));
			} 
			if (map.containsKey("ox_postal_code_business")){
				contact.setPostalCodeBusiness((String)map.get("ox_postal_code_business"));
			} 
			if (map.containsKey("ox_city_business")){
				contact.setCityBusiness((String)map.get("ox_city_business"));
			} 
			if (map.containsKey("ox_state_business")){
				contact.setStateBusiness((String)map.get("ox_state_business"));
			} 
			if (map.containsKey("ox_country_business")){
				contact.setCountryBusiness((String)map.get("ox_country_business"));
			} 
			if (map.containsKey("ox_street_other")){
				contact.setStreetOther((String)map.get("ox_street_other"));
			} 
			if (map.containsKey("ox_postal_code_other")){
				contact.setPostalCodeOther((String)map.get("ox_postal_code_other"));
			} 
			if (map.containsKey("ox_city_other")){
				contact.setCityOther((String)map.get("ox_city_other"));
			} 
			if (map.containsKey("ox_state_other")){
				contact.setStateOther((String)map.get("ox_state_other"));
			} 
			if (map.containsKey("ox_country_other")){
				contact.setCountryOther((String)map.get("ox_country_other"));
			} 
			if (map.containsKey("ox_birthday")){
				try {
					contact.setBirthday(DATE.parse((String)map.get("ox_birthday")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} 
			if (map.containsKey("ox_marital_status")){
				contact.setMaritalStatus((String)map.get("ox_marital_status"));
			}  
			if (map.containsKey("ox_number_of_children")){
				contact.setNumberOfChildren((String)map.get("ox_number_of_children"));
			} 
			if (map.containsKey("ox_profession")){
				contact.setProfession((String)map.get("ox_profession"));
			}  
			if (map.containsKey("ox_nickname")){
				contact.setNickname((String)map.get("ox_nickname"));
			}  
			if (map.containsKey("ox_spouse_name")){
				contact.setSpouseName((String)map.get("ox_first_name"));
			}  
			if (map.containsKey("ox_anniversary")){
				try {
					contact.setAnniversary(DATE.parse((String)map.get("ox_anniversary")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}  
			if (map.containsKey("ox_note")){
				contact.setNote((String)map.get("ox_note"));
			}  
			if (map.containsKey("ox_department")){
				contact.setDepartment((String)map.get("ox_department"));
			}  
			if (map.containsKey("ox_position")){
				contact.setPosition((String)map.get("ox_position"));
			}  
			if (map.containsKey("ox_employee_type")){
				contact.setEmployeeType((String)map.get("ox_employee_type"));
			}  
			if (map.containsKey("ox_room_number")){
				contact.setRoomNumber((String)map.get("ox_room_number"));
			}  
			if (map.containsKey("ox_number_of_employees")){
				contact.setNumberOfEmployee((String)map.get("ox_number_of_employees"));
			}  
			if (map.containsKey("ox_sales_volume")){
				contact.setSalesVolume((String)map.get("ox_sales_volume"));
			}  
			if (map.containsKey("ox_tax_id")){
				contact.setTaxID((String)map.get("ox_tax_id"));
			}  
			if (map.containsKey("ox_commercial_register")){
				contact.setCommercialRegister((String)map.get("ox_commercial_register"));
			}  
			if (map.containsKey("ox_branches")){
				contact.setBranches((String)map.get("ox_branches"));
			}  
			if (map.containsKey("ox_business_category")){
				contact.setBusinessCategory((String)map.get("ox_business_category"));
			}  
			if (map.containsKey("ox_info")){
				contact.setInfo((String)map.get("ox_info"));
			}  
			if (map.containsKey("ox_manager_name")){
				contact.setManagerName((String)map.get("ox_manager_name"));
			}  
			if (map.containsKey("ox_assistant_name")){
				contact.setAssistantName((String)map.get("ox_assistant_name"));
			}  
			if (map.containsKey("ox_telephone_home1")){
				contact.setTelephoneHome1((String)map.get("ox_telephone_home1"));
			}  
			if (map.containsKey("ox_telephone_home2")){
				contact.setTelephoneHome2((String)map.get("ox_telephone_home2"));
			}  
			if (map.containsKey("ox_telephone_business1")){
				contact.setTelephoneBusiness1((String)map.get("ox_telephone_business1"));
			}  
			if (map.containsKey("ox_telephone_business2")){
				contact.setTelephoneBusiness2((String)map.get("ox_telephone_business2"));
			}  
			if (map.containsKey("ox_telephone_other")){
				contact.setTelephoneOther((String)map.get("ox_telephone_other"));
			}  
			if (map.containsKey("ox_fax_business")){
				contact.setFaxBusiness((String)map.get("ox_fax_business"));
			}  
			if (map.containsKey("ox_telephone_callback")){
				contact.setTelephoneCallback((String)map.get("telephone_callback"));
			}  
			if (map.containsKey("ox_telephone_car")){
				contact.setTelephoneCar((String)map.get("ox_telephone_car"));
			}  
			if (map.containsKey("ox_telephone_company")){
				contact.setTelephoneCompany((String)map.get("ox_telephone_company"));
			}  
			if (map.containsKey("ox_fax_home")){
				contact.setFaxHome((String)map.get("ox_fax_home"));
			}  
			if (map.containsKey("ox_cellular_telephone1")){
				contact.setCellularTelephone1((String)map.get("ox_cellular_telephone1"));
			}  
			if (map.containsKey("ox_cellular_telephone2")){
				contact.setCellularTelephone2((String)map.get("ox_cellular_telephone2"));
			}  
			if (map.containsKey("ox_fax_other")){
				contact.setFaxOther((String)map.get("ox_fax_other"));
			}  
			if (map.containsKey("ox_email1")){
				contact.setEmail1((String)map.get("ox_email1"));
			}  
			if (map.containsKey("ox_email2")){
				contact.setEmail2((String)map.get("ox_email2"));
			}  
			if (map.containsKey("ox_email3")){
				contact.setEmail3((String)map.get("ox_email3"));
			}  
			if (map.containsKey("ox_url")){
				contact.setURL((String)map.get("ox_url"));
			}  
			if (map.containsKey("ox_telephone_isdn")){
				contact.setTelephoneISDN((String)map.get("ox_telephone_isdn"));
			}  
			if (map.containsKey("ox_telephone_pager")){
				contact.setTelephonePager((String)map.get("ox_telephone_pager"));
			}  
			if (map.containsKey("ox_telephone_primary")){
				contact.setTelephonePrimary((String)map.get("ox_telephone_primary"));
			}  
			if (map.containsKey("ox_telephone_radio")){
				contact.setTelephoneRadio((String)map.get("ox_telephone_radio"));
			}  
			if (map.containsKey("ox_telephone_telex")){
				contact.setTelephoneTelex((String)map.get("ox_telephone_telex"));
			}  
			if (map.containsKey("ox_telephone_ttytdd")){
				contact.setTelephoneTTYTTD((String)map.get("ox_telephone_ttytdd"));
			}  
			if (map.containsKey("ox_instant_messenger1")){
				contact.setInstantMessenger1((String)map.get("ox_instant_messenger1"));
			}  
			if (map.containsKey("ox_instant_messenger2")){
				contact.setInstantMessenger2((String)map.get("ox_instant_messenger2"));
			}  
			if (map.containsKey("ox_telephone_ip")){
				contact.setTelephoneIP((String)map.get("ox_telephone_ip"));
			}  
			if (map.containsKey("ox_telephone_assistant")){
				contact.setTelephoneAssistant((String)map.get("telephone_assistant"));
			}  
			if (map.containsKey("ox_company")){
				contact.setCompany((String)map.get("ox_company"));
			}  
			if (map.containsKey("ox_image")){
				contact.setImage1(((String)map.get("ox_image")).getBytes());
			}  
			if (map.containsKey("ox_userfield01")){
				contact.setGivenName((String)map.get("ox_userfield01"));
			}  
			if (map.containsKey("ox_userfield02")){
				contact.setGivenName((String)map.get("ox_userfield02"));
			}  
			if (map.containsKey("ox_userfield03")){
				contact.setGivenName((String)map.get("ox_userfield03"));
			}  
			if (map.containsKey("ox_userfield04")){
				contact.setGivenName((String)map.get("ox_userfield04"));
			}  
			if (map.containsKey("ox_userfield05")){
				contact.setGivenName((String)map.get("ox_userfield05"));
			}  
			if (map.containsKey("ox_userfield06")){
				contact.setGivenName((String)map.get("ox_userfield06"));
			}  
			if (map.containsKey("ox_userfield07")){
				contact.setGivenName((String)map.get("ox_userfield07"));
			}  
			if (map.containsKey("ox_userfield08")){
				contact.setGivenName((String)map.get("ox_userfield08"));
			}  
			if (map.containsKey("ox_userfield09")){
				contact.setGivenName((String)map.get("ox_userfield09"));
			}  
			if (map.containsKey("ox_userfield10")){
				contact.setGivenName((String)map.get("ox_userfield10"));
			}  
			if (map.containsKey("ox_userfield11")){
				contact.setGivenName((String)map.get("ox_userfield11"));
			}  
			if (map.containsKey("ox_userfield12")){
				contact.setGivenName((String)map.get("ox_userfield12"));
			}  
			if (map.containsKey("ox_userfield13")){
				contact.setGivenName((String)map.get("ox_userfield13"));
			}  
			if (map.containsKey("ox_userfield14")){
				contact.setGivenName((String)map.get("ox_userfield14"));
			}  
			if (map.containsKey("ox_userfield15")){
				contact.setGivenName((String)map.get("ox_userfield15"));
			}  
			if (map.containsKey("ox_userfield16")){
				contact.setGivenName((String)map.get("ox_userfield16"));
			}  
			if (map.containsKey("ox_userfield17")){
				contact.setGivenName((String)map.get("ox_userfield17"));
			}  
			if (map.containsKey("ox_userfield18")){
				contact.setGivenName((String)map.get("ox_userfield18"));
			}  
			if (map.containsKey("ox_userfield19")){
				contact.setGivenName((String)map.get("ox_userfield19"));
			}  
			if (map.containsKey("ox_userfield20")){
				contact.setGivenName((String)map.get("ox_userfield20"));
			}  
			
			outlist.add(contact);
		}
		
		return outlist;
	}

}
