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

package com.openexchange.groupware.container;

import java.util.Date;


/**
 * ContactObject
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 */

public class ContactObject extends CommonObject
{
	public static final int DISPLAY_NAME = 500;
	
	public static final int GIVEN_NAME = 501;
	
	public static final int SUR_NAME = 502;
	
	public static final int MIDDLE_NAME = 503;
	
	public static final int SUFFIX = 504;	

	public static final int TITLE = 505;	

	public static final int STREET_HOME = 506;	

	public static final int POSTAL_CODE_HOME = 507;	

	public static final int CITY_HOME = 508;	

	public static final int STATE_HOME = 509;
	
	public static final int COUNTRY_HOME = 510;
	
	public static final int BIRTHDAY = 511;
	
	public static final int MARITAL_STATUS = 512;
	
	public static final int NUMBER_OF_CHILDREN = 513;
	
	public static final int PROFESSION = 514;
	
	public static final int NICKNAME = 515;
	
	public static final int SPOUSE_NAME = 516;
	
	public static final int ANNIVERSARY = 517;
	
	public static final int NOTE = 518;
	
	public static final int DEPARTMENT = 519;

	public static final int POSITION = 520;
	
	public static final int EMPLOYEE_TYPE = 521;
	
	public static final int ROOM_NUMBER = 522;
	
	public static final int STREET_BUSINESS = 523;
	
	public static final int INTERNAL_USERID = 524;
	
	public static final int POSTAL_CODE_BUSINESS = 525;
	
	public static final int CITY_BUSINESS = 526;
	
	public static final int STATE_BUSINESS = 527;
	
	public static final int COUNTRY_BUSINESS = 528;
	
	public static final int NUMBER_OF_EMPLOYEE = 529;
	
	public static final int SALES_VOLUME = 530;
	
	public static final int TAX_ID = 531;
	
	public static final int COMMERCIAL_REGISTER = 532;
	
	public static final int BRANCHES = 533;
	
	public static final int BUSINESS_CATEGORY = 534;
	
	public static final int INFO = 535;
	
	public static final int MANAGER_NAME = 536;
	
	public static final int ASSISTANT_NAME = 537;
	
	public static final int STREET_OTHER = 538;
	
	public static final int CITY_OTHER = 539;
	
	public static final int POSTAL_CODE_OTHER = 540;
	
	public static final int COUNTRY_OTHER = 541;
	
	public static final int TELEPHONE_BUSINESS1 = 542;
	
	public static final int TELEPHONE_BUSINESS2 = 543;
	
	public static final int FAX_BUSINESS = 544;
	
	public static final int TELEPHONE_CALLBACK = 545;
	
	public static final int TELEPHONE_CAR = 546;
	
	public static final int TELEPHONE_COMPANY = 547;
	
	public static final int TELEPHONE_HOME1 = 548;
	
	public static final int TELEPHONE_HOME2 = 549;
	
	public static final int FAX_HOME = 550;
	
	public static final int CELLULAR_TELEPHONE1 = 551;
	
	public static final int CELLULAR_TELEPHONE2 = 552;
	
	public static final int TELEPHONE_OTHER = 553;
	
	public static final int FAX_OTHER = 554;
	
	public static final int EMAIL1 = 555;
	
	public static final int EMAIL2 = 556;
	
	public static final int EMAIL3 = 557;
	
	public static final int URL = 558;
	
	public static final int TELEPHONE_ISDN = 559;
	
	public static final int TELEPHONE_PAGER = 560;
	
	public static final int TELEPHONE_PRIMARY = 561;
	
	public static final int TELEPHONE_RADIO = 562;
	
	public static final int TELEPHONE_TELEX = 563;
	
	public static final int TELEPHONE_TTYTDD = 564;
	
	public static final int INSTANT_MESSENGER1 = 565;
	
	public static final int INSTANT_MESSENGER2 = 566;
	
	public static final int TELEPHONE_IP = 567;
	
	public static final int TELEPHONE_ASSISTANT = 568;
	
	public static final int COMPANY = 569;
	
	public static final int IMAGE1 = 570;

	public static final int USERFIELD01 = 571;
	
	public static final int USERFIELD02 = 572;
	
	public static final int USERFIELD03 = 573;
	
	public static final int USERFIELD04 = 574;
	
	public static final int USERFIELD05 = 575;
	
	public static final int USERFIELD06 = 576;
	
	public static final int USERFIELD07 = 577;
	
	public static final int USERFIELD08 = 578;
	
	public static final int USERFIELD09 = 579;
	
	public static final int USERFIELD10 = 580;
	
	public static final int USERFIELD11 = 581;
	
	public static final int USERFIELD12 = 582;
	
	public static final int USERFIELD13 = 583;
	
	public static final int USERFIELD14 = 584;
	
	public static final int USERFIELD15 = 585;
	
	public static final int USERFIELD16 = 586;
	
	public static final int USERFIELD17 = 587;
	
	public static final int USERFIELD18 = 588;
	
	public static final int USERFIELD19 = 589;
	
	public static final int USERFIELD20 = 590;	

	public static final int LINKS = 591;

	public static final int DISTRIBUTIONLIST = 592;	

	public static final int CONTEXTID = 593;	

	public static final int NUMBER_OF_DISTRIBUTIONLIST = 594;	

	public static final int NUMBER_OF_LINKS = 595;
	
	public static final int NUMBER_OF_IMAGES = 596;
	
	public static final int IMAGE_LAST_MODIFIED = 597;

	public static final int STATE_OTHER = 598;
	
	public static final int FILE_AS = 599;
		
	public static final int ATTACHMENT = 600;

	public static final int IMAGE1_CONTENT_TYPE = 601;
	
	public static final int MARK_AS_DISTRIBUTIONLIST = 602;
	
	public static final int DEFAULT_ADDRESS = 605;
	
	protected String display_name;
	protected String given_name;
	protected String sur_name;
	protected String middle_name;
	protected String suffix;
	protected String title;
	protected String street;
	protected String postal_code;
	protected String city;
	protected String state;
	protected String country;
	protected Date birthday;
	protected String marital_status;
	protected String number_of_children;
	protected String profession;
	protected String nickname;
	protected String spouse_name;
	protected Date anniversary;
	protected String note;
	protected String department;
	protected String position;
	protected String employee_type;
	protected String room_number;
	protected String street_business;
	protected String postal_code_business;
	protected String city_business;
	protected String state_business;
	protected String country_business;
	protected String number_of_employee;
	protected String sales_volume;
	protected String tax_id;
	protected String commercial_register;
	protected String branches;
	protected String business_category;
	protected String info;
	protected String manager_name;
	protected String assistant_name;
	protected String street_other;
	protected String postal_code_other;
	protected String city_other;
	protected String state_other;
	protected String country_other;
	protected String telephone_business1;
	protected String telephone_business2;
	protected String fax_business;
	protected String telephone_callback;
	protected String telephone_car;
	protected String telephone_company;
	protected String telephone_home1;
	protected String telephone_home2;
	protected String fax_home;
	protected String cellular_telephone1;
	protected String cellular_telephone2;
	protected String telephone_other;
	protected String fax_other;
	protected String email1;
	protected String email2;
	protected String email3;
	protected String url;
	protected String telephone_isdn;
	protected String telephone_pager;
	protected String telephone_primary;
	protected String telephone_radio;
	protected String telephone_telex;
	protected String telephone_ttytdd;
	protected String instant_messenger1;
	protected String instant_messenger2;
	protected String telephone_ip;
	protected String telephone_assistant;
	protected String company;
	protected String userfield01;
	protected String userfield02;
	protected String userfield03;
	protected String userfield04;
	protected String userfield05;
	protected String userfield06;
	protected String userfield07;
	protected String userfield08;
	protected String userfield09;
	protected String userfield10;
	protected String userfield11;
	protected String userfield12;
	protected String userfield13;
	protected String userfield14;
	protected String userfield15;
	protected String userfield16;
	protected String userfield17;
	protected String userfield18;
	protected String userfield19;
	protected String userfield20;
	protected int cid;
	protected int internal_userId;
	protected int defaultaddress;
	protected byte[] image1;
	protected Date image_last_modified;
	protected int number_of_images;
	protected String file_as;
	protected String imageContentType;
	protected boolean mark_as_distributionlist;
	
	protected boolean b_display_name;
	protected boolean b_given_name;
	protected boolean b_sur_name;
	protected boolean b_middle_name;
	protected boolean b_suffix;
	protected boolean b_title;
	protected boolean b_street;
	protected boolean b_postal_code;
	protected boolean b_city;
	protected boolean b_state;
	protected boolean b_country;
	protected boolean b_birthday;
	protected boolean b_marital_status;
	protected boolean b_number_of_children;
	protected boolean b_profession;
	protected boolean b_nickname;
	protected boolean b_spouse_name;
	protected boolean b_anniversary;
	protected boolean b_note;
	protected boolean b_department;
	protected boolean b_position;
	protected boolean b_employee_type;
	protected boolean b_room_number;
	protected boolean b_street_business;
	protected boolean b_postal_code_business;
	protected boolean b_city_business;
	protected boolean b_state_business;
	protected boolean b_country_business;
	protected boolean b_number_of_employee;
	protected boolean b_sales_volume;
	protected boolean b_tax_id;
	protected boolean b_commercial_register;
	protected boolean b_branches;
	protected boolean b_business_category;
	protected boolean b_info;
	protected boolean b_manager_name;
	protected boolean b_assistant_name;
	protected boolean b_street_other;
	protected boolean b_postal_code_other;
	protected boolean b_city_other;
	protected boolean b_state_other;
	protected boolean b_country_other;
	protected boolean b_telephone_business1;
	protected boolean b_telephone_business2;
	protected boolean b_fax_business;
	protected boolean b_telephone_callback;
	protected boolean b_telephone_car;
	protected boolean b_telephone_company;
	protected boolean b_telephone_home1;
	protected boolean b_telephone_home2;
	protected boolean b_fax_home;
	protected boolean b_cellular_telephone1;
	protected boolean b_cellular_telephone2;
	protected boolean b_telephone_other;
	protected boolean b_fax_other;
	protected boolean b_email1;
	protected boolean b_email2;
	protected boolean b_email3;
	protected boolean b_url;
	protected boolean b_telephone_isdn;
	protected boolean b_telephone_pager;
	protected boolean b_telephone_primary;
	protected boolean b_telephone_radio;
	protected boolean b_telephone_telex;
	protected boolean b_telephone_ttytdd;
	protected boolean b_instant_messenger1;
	protected boolean b_instant_messenger2;
	protected boolean b_telephone_ip;
	protected boolean b_telephone_assistant;
	protected boolean b_defaultaddress;
	protected boolean b_company;
	protected boolean b_image1;
	protected boolean b_containsImage;	
	protected boolean b_userfield01;
	protected boolean b_userfield02;
	protected boolean b_userfield03;
	protected boolean b_userfield04;
	protected boolean b_userfield05;
	protected boolean b_userfield06;
	protected boolean b_userfield07;
	protected boolean b_userfield08;
	protected boolean b_userfield09;
	protected boolean b_userfield10;
	protected boolean b_userfield11;
	protected boolean b_userfield12;
	protected boolean b_userfield13;
	protected boolean b_userfield14;
	protected boolean b_userfield15;
	protected boolean b_userfield16;
	protected boolean b_userfield17;
	protected boolean b_userfield18;
	protected boolean b_userfield19;
	protected boolean b_userfield20;
	protected boolean b_links;
	protected boolean b_created_from;
	protected boolean b_changed_from;
	protected boolean b_creating_date;
	protected boolean b_changing_date;
	protected boolean b_cid;
	protected boolean b_internal_userId;
	protected boolean b_image_last_modified;
	protected boolean b_number_of_links;
	protected int number_of_links;   
	protected LinkEntryObject[] links;
	protected boolean b_file_as;
	protected boolean bImageContentType;
	protected boolean b_mark_as_distributionlist;
	
	protected boolean b_number_of_dlists;
	protected int number_of_dlists;   
	protected DistributionListEntryObject[] dlists;

	
	public ContactObject ( ) 
	{
		reset();
	}
	
	// GET METHODS
	public String getDisplayName ( ) 
	{
		return display_name;
	}
	
	public String getGivenName ( ) 
	{
		return given_name;
	}
	
	public String getSurName ( ) 
	{
		return sur_name;
	}
	
	public String getMiddleName ( ) 
	{
		return middle_name;
	}
	
	public String getSuffix ( ) 
	{
		return suffix;
	}
	
	public String getTitle ( ) 
	{
		return title;
	}
	
	public String getStreetHome ( ) 
	{
		return street;
	}
	
	public String getPostalCodeHome ( ) 
	{
		return postal_code;
	}
	
	public String getCityHome ( ) 
	{
		return city;
	}
	
	public String getStateHome ( ) 
	{
		return state;
	}
	
	public String getCountryHome ( ) 
	{
		return country;
	}
	
	public Date getBirthday ( ) 
	{
		return birthday;
	}
	
	public String getMaritalStatus ( ) 
	{
		return marital_status;
	}
	
	public String getNumberOfChildren ( ) 
	{
		return number_of_children;
	}

	public String getProfession ( ) 
	{
		return profession;
	}

	public String getNickname ( ) 
	{
		return nickname;
	}
	
	public String getSpouseName ( ) 
	{
		return spouse_name;
	}
	
	public Date getAnniversary ( ) 
	{
		return anniversary;
	}
	
	public String getNote ( ) 
	{
		return note;
	}
	
	public String getDepartment ( ) 
	{
		return department;
	}
	
	public String getPosition ( ) 
	{
		return position;
	}
	
	public String getEmployeeType ( ) 
	{
		return employee_type;
	}
	
	public String getRoomNumber ( ) 
	{
		return room_number;
	}
	
	public String getStreetBusiness ( ) 
	{
		return street_business;
	}

	public String getPostalCodeBusiness ( ) 
	{
		return postal_code_business;
	}
	
	public String getCityBusiness ( ) 
	{
		return city_business;
	}
	
	public String getStateBusiness ( ) 
	{
		return state_business;
	}
	
	public String getCountryBusiness ( ) 
	{
		return country_business;
	}
	
	public String getNumberOfEmployee ( ) 
	{
		return number_of_employee;
	}

	public String getSalesVolume ( ) 
	{
		return sales_volume;
	}
	
	public String getTaxID ( ) 
	{
		return tax_id;
	}
	
	public String getCommercialRegister ( ) 
	{
		return commercial_register;
	}
	
	public String getBranches ( ) 
	{
		return branches;
	}
	
	public String getBusinessCategory ( ) 
	{
		return business_category;
	}
	
	public String getInfo ( ) 
	{
		return info;
	}
	
	public String getManagerName ( ) 
	{
		return manager_name;
	}
	
	public String getAssistantName ( ) 
	{
		return assistant_name;
	}
	
	public String getStreetOther ( ) 
	{
		return street_other;
	}

	public String getPostalCodeOther ( ) 
	{
		return postal_code_other;
	}
	
	public String getCityOther ( ) 
	{
		return city_other;
	}
	
	public String getStateOther ( ) 
	{
		return state_other;
	}
	
	public String getCountryOther( ) 
	{
		return country_other;
	}
	
	public String getTelephoneBusiness1( ) 
	{
		return telephone_business1;
	}
	
	public String getTelephoneBusiness2( ) 
	{
		return telephone_business2;
	}
	
	public String getFaxBusiness( ) 
	{
		return fax_business;
	}
	
	public String getTelephoneCallback( ) 
	{
		return telephone_callback;
	}
	
	public String getTelephoneCar( ) 
	{
		return telephone_car;
	}
	
	public String getTelephoneCompany( ) 
	{
		return telephone_company;
	}
	
	public String getTelephoneHome1( ) 
	{
		return telephone_home1;
	}
	
	public String getTelephoneHome2( ) 
	{
		return telephone_home2;
	}
	
	public String getFaxHome( ) 
	{
		return fax_home;
	}
	
	public String getCellularTelephone1( ) 
	{
		return cellular_telephone1;
	}
	
	public String getCellularTelephone2( ) 
	{
		return cellular_telephone2;
	}
	
	public String getTelephoneOther( ) 
	{
		return telephone_other;
	}
	
	public String getFaxOther( ) 
	{
		return fax_other;
	}
	
	public String getEmail1( ) 
	{
		return email1;
	}

	public String getEmail2( ) 
	{
		return email2;
	}

	public String getEmail3( ) 
	{
		return email3;
	}

	public String getURL( ) 
	{
		return url;
	}
	
	public String getTelephoneISDN( ) 
	{
		return telephone_isdn;
	}

	public String getTelephonePager( ) 
	{
		return telephone_pager;
	}

	public String getTelephonePrimary( ) 
	{
		return telephone_primary;
	}

	public String getTelephoneRadio( ) 
	{
		return telephone_radio;
	}

	public String getTelephoneTelex( ) 
	{
		return telephone_telex;
	}

	public String getTelephoneTTYTTD( ) 
	{
		return telephone_ttytdd;
	}

	public String getInstantMessenger1( ) 
	{
		return instant_messenger1;
	}

	public String getInstantMessenger2( ) 
	{
		return instant_messenger2;
	}
	
	public String getTelephoneIP( ) 
	{
		return telephone_ip;
	}

	public String getTelephoneAssistant( ) 
	{
		return telephone_assistant;
	}

	public int getDefaultAddress( ) 
	{
		return defaultaddress;
	}

	public String getCompany( ) 
	{
		return company;
	}
	
	public byte[] getImage1( ) 
	{
		return image1;
	}
	
	public String getImageContentType ( ) 
	{
		return imageContentType;
	}

	public int getNumberOfImages(){
		return number_of_images;
	}
	
	public String getUserField01( ) 
	{
		return userfield01;
	}

	public String getUserField02( ) 
	{
		return userfield02;
	}

	public String getUserField03( ) 
	{
		return userfield03;
	}

	public String getUserField04( ) 
	{
		return userfield04;
	}

	public String getUserField05( ) 
	{
		return userfield05;
	}

	public String getUserField06( ) 
	{
		return userfield06;
	}

	public String getUserField07( ) 
	{
		return userfield07;
	}

	public String getUserField08( ) 
	{
		return userfield08;
	}

	public String getUserField09( ) 
	{
		return userfield09;
	}

	public String getUserField10( ) 
	{
		return userfield10;
	}

	public String getUserField11( ) 
	{
		return userfield11;
	}

	public String getUserField12( ) 
	{
		return userfield12;
	}

	public String getUserField13( ) 
	{
		return userfield13;
	}

	public String getUserField14( ) 
	{
		return userfield14;
	}

	public String getUserField15( ) 
	{
		return userfield15;
	}

	public String getUserField16( ) 
	{
		return userfield16;
	}

	public String getUserField17( ) 
	{
		return userfield17;
	}

	public String getUserField18( ) 
	{
		return userfield18;
	}

	public String getUserField19( ) 
	{
		return userfield19;
	}

	public String getUserField20( ) 
	{
		return userfield20;
	}

	public int getNumberOfLinks( ) 
	{
		return number_of_links;
	}

	public int getNumberOfDistributionLists( ) 
	{
		return number_of_dlists;
	}

	public DistributionListEntryObject[] getDistributionList() {
		return dlists;
	}

	public LinkEntryObject[] getLinks() {
		return links;
	}

	public int getContextId() {
		return cid;
	}

	public int getInternalUserId() {
		return internal_userId;
	}

	public Date getImageLastModified() {
		return image_last_modified;
	}
	
	public String getFileAs( ) 
	{
		return file_as;
	}
	
	public boolean getMarkAsDistribtuionlist(){
		return mark_as_distributionlist;
	}
		
	// SET METHODS
	public void setDisplayName ( final String display_name ) 
	{
		this.display_name = display_name;
		b_display_name = true;
	}
	
	public void setGivenName ( final String given_name) 
	{
		this.given_name = given_name;
		b_given_name = true;
	}
	
	public void setSurName ( final String sur_name ) 
	{
		this.sur_name = sur_name;
		b_sur_name = true;
	}
	
	public void setMiddleName ( final String middle_name ) 
	{
		this.middle_name = middle_name;
		b_middle_name = true;
	}
	
	public void setSuffix ( final String suffix ) 
	{
		this.suffix = suffix;
		b_suffix = true;
	}
	
	public void setTitle ( final String title ) 
	{
		this.title = title;
		b_title = true;
	}
	
	public void setStreetHome ( final String street ) 
	{
		this.street = street;
		b_street = true;
	}
	
	public void setPostalCodeHome ( final String postal_code ) 
	{
		this.postal_code = postal_code;
		b_postal_code = true;
	}
	
	public void setCityHome ( final String city) 
	{
		this.city = city;
		b_city = true;
	}
	
	public void setStateHome ( final String state ) 
	{
		this.state = state;
		b_state = true;
	}
	
	public void setCountryHome ( final String country ) 
	{
		this.country = country; 
		b_country = true;
	}
	
	public void setBirthday ( final Date birthday ) 
	{
		this.birthday = birthday;
		b_birthday = true;
	}
	
	public void setMaritalStatus ( final String marital_status ) 
	{
		this.marital_status = marital_status;
		b_marital_status = true;
	}
	
	public void setNumberOfChildren ( final String number_of_children ) 
	{
		this.number_of_children = number_of_children;
		b_number_of_children = true;
	}

	public void setProfession ( final String profession ) 
	{
		this.profession = profession;
		b_profession = true;
	}

	public void setNickname ( final String nickname ) 
	{
		this.nickname = nickname;
		b_nickname = true;
	}
	
	public void setSpouseName ( final String spouse_name ) 
	{
		this.spouse_name = spouse_name;
		b_spouse_name = true;
	}
	
	public void setAnniversary ( final Date anniversary) 
	{
		this.anniversary = anniversary;
		b_anniversary = true;
	}
	
	public void setNote ( final String note ) 
	{
		this.note = note;
		b_note = true;
	}
	
	public void setDepartment ( final String department ) 
	{
		this.department = department;
		b_department = true;
	}
	
	public void setPosition ( final String position ) 
	{
		this.position = position;
		b_position = true;
	}
	
	public void setEmployeeType ( final String employee_type) 
	{
		this.employee_type = employee_type;
		b_employee_type = true;
	}
	
	public void setRoomNumber ( final String room_number) 
	{
		this.room_number = room_number;
		b_room_number = true;
	}
	
	public void setStreetBusiness ( final String street_business ) 
	{
		this.street_business = street_business;
		b_street_business = true;
	}

	public void setPostalCodeBusiness ( final String postal_code_business) 
	{
		this.postal_code_business = postal_code_business;
		b_postal_code_business = true;
	}
	
	public void setCityBusiness ( final String city_business ) 
	{
		this.city_business = city_business;
		b_city_business = true;
	}
	
	public void setStateBusiness ( final String state_business ) 
	{
		this.state_business = state_business;
		b_state_business = true;
	}
	
	public void setCountryBusiness ( final String country_business ) 
	{
		this.country_business = country_business;
		b_country_business = true;
	}
	
	public void setNumberOfEmployee ( final String number_of_employee ) 
	{
		this.number_of_employee = number_of_employee;
		b_number_of_employee = true;
	}

	public void setSalesVolume ( final String sales_volume ) 
	{
		this.sales_volume = sales_volume;
		b_sales_volume = true;
	}
	
	public void setTaxID ( final String tax_id ) 
	{
		this.tax_id = tax_id;
		b_tax_id = true;
	}
	
	public void setCommercialRegister ( final String commercial_register ) 
	{
		this.commercial_register = commercial_register;
		b_commercial_register = true;
	}
	
	public void setBranches ( final String branches ) 
	{
		this.branches = branches;
		b_branches = true;
	}
	
	public void setBusinessCategory ( final String business_category ) 
	{
		this.business_category = business_category;
		b_business_category = true;
	}
	
	public void setInfo ( final String info ) 
	{
		this.info = info;
		b_info = true;
	}
	
	public void setManagerName ( final String manager_name ) 
	{
		this.manager_name = manager_name;
		b_manager_name = true;
	}
	
	public void setAssistantName ( final String assistant_name ) 
	{
		this.assistant_name = assistant_name;
		b_assistant_name = true;
	}
	
	public void setStreetOther ( final String street_other ) 
	{
		this.street_other = street_other;
		b_street_other = true;
	}

	public void setPostalCodeOther ( final String postal_code_other ) 
	{
		this.postal_code_other = postal_code_other;
		b_postal_code_other = true;
	}
	
	public void setCityOther ( final String city_other ) 
	{
		this.city_other = city_other;
		b_city_other = true;
	}
	
	public void setStateOther ( final String state_other ) 
	{
		this.state_other = state_other;
		b_state_other = true;
	}
	
	public void setCountryOther ( final String country_other ) 
	{
		this.country_other = country_other;
		b_country_other = true;
	}
	
	public void setTelephoneBusiness1 ( final String telephone_business1 ) 
	{
		this.telephone_business1 = telephone_business1;
		b_telephone_business1 = true;
	}
	
	public void setTelephoneBusiness2 ( final String telephone_business2 ) 
	{
		this.telephone_business2 = telephone_business2;
		b_telephone_business2 = true;
	}
	
	public void setFaxBusiness ( final String fax_business ) 
	{
		this.fax_business = fax_business;
		b_fax_business = true;
	}
	
	public void setTelephoneCallback ( final String telephone_callback ) 
	{
		this.telephone_callback = telephone_callback;
		b_telephone_callback = true;
	}
	
	public void setTelephoneCar ( final String telephone_car ) 
	{
		this.telephone_car = telephone_car;
		b_telephone_car = true;
	}
	
	public void setTelephoneCompany ( final String telephone_company ) 
	{
		this.telephone_company = telephone_company;
		b_telephone_company = true;
	}
	
	public void setTelephoneHome1 ( final String telephone_home1 ) 
	{
		this.telephone_home1 = telephone_home1;
		b_telephone_home1 = true;
	}
	
	public void setTelephoneHome2 ( final String telephone_home2 ) 
	{
		this.telephone_home2 = telephone_home2;
		b_telephone_home2 = true;
	}
	
	public void setFaxHome ( final String fax_home ) 
	{
		this.fax_home = fax_home;
		b_fax_home = true;
	}
	
	public void setCellularTelephone1 ( final String cellular_telephone1 ) 
	{
		this.cellular_telephone1 = cellular_telephone1;
		b_cellular_telephone1 = true;
	}
	
	public void setCellularTelephone2 ( final String cellular_telephone2 ) 
	{
		this.cellular_telephone2 = cellular_telephone2;
		b_cellular_telephone2 = true;
	}
	
	public void setTelephoneOther ( final String telephone_other ) 
	{
		this.telephone_other = telephone_other;
		b_telephone_other = true;
	}
	
	public void setFaxOther ( final String fax_other ) 
	{
		this.fax_other = fax_other;
		b_fax_other = true;
	}
	
	public void setEmail1 ( final String email1 ) 
	{
		this.email1 = email1;
		b_email1 = true;
	}

	public void setEmail2 ( final String email2 ) 
	{
		this.email2 = email2;
		b_email2 = true;
	}

	public void setEmail3 ( final String email3 ) 
	{
		this.email3 = email3;
		b_email3 = true;
	}

	public void setURL ( final String url )
	{
		this.url = url;
		b_url = true;
	}
	
	public void setTelephoneISDN ( final String telephone_isdn ) 
	{
		this.telephone_isdn = telephone_isdn;
		b_telephone_isdn = true;
	}

	public void setTelephonePager ( final String telephone_pager ) 
	{
		this.telephone_pager = telephone_pager;
		b_telephone_pager = true;
	}

	public void setTelephonePrimary ( final String telephone_primary ) 
	{
		this.telephone_primary = telephone_primary;
		b_telephone_primary = true;
	}

	public void setTelephoneRadio ( final String telephone_radio ) 
	{
		this.telephone_radio = telephone_radio;
		b_telephone_radio = true;
	}

	public void setTelephoneTelex ( final String telephone_telex ) 
	{
		this.telephone_telex = telephone_telex;
		b_telephone_telex = true;
	}

	public void setTelephoneTTYTTD ( final String telephone_ttyttd ) 
	{
		this.telephone_ttytdd = telephone_ttyttd;
		b_telephone_ttytdd = true;
	}

	public void setInstantMessenger1 ( final String instant_messenger1 ) 
	{
		this.instant_messenger1 = instant_messenger1;
		b_instant_messenger1 = true;
	}

	public void setInstantMessenger2 ( final String instant_messenger2 ) 
	{
		this.instant_messenger2 = instant_messenger2;
		b_instant_messenger2 = true;
	}
	
	public void setTelephoneIP ( final String phone_ip ) 
	{
		this.telephone_ip = phone_ip;
		b_telephone_ip = true;
	}

	public void setTelephoneAssistant ( final String telephone_assistant ) 
	{
		this.telephone_assistant = telephone_assistant;
		b_telephone_assistant = true;
	}

	public void setDefaultAddress( final int defaultaddress ) 
	{
		this.defaultaddress = defaultaddress;
		b_defaultaddress = true;
	}

	public void setCompany ( final String company ) 
	{
		this.company = company;
		b_company = true;
	}
	
	public void setUserField01 ( final String userfield01 ) 
	{
		this.userfield01 = userfield01;
		b_userfield01 = true;
	}

	public void setUserField02 ( final String userfield02 ) 
	{
		this.userfield02 = userfield02;
		b_userfield02 = true;
	}

	public void setUserField03 ( final String userfield03 ) 
	{
		this.userfield03 = userfield03;
		b_userfield03 = true;
	}

	public void setUserField04 ( final String userfield04 ) 
	{
		this.userfield04 = userfield04;
		b_userfield04 = true;
	}

	public void setUserField05 ( final String userfield05 ) 
	{
		this.userfield05 = userfield05;
		b_userfield05 = true;
	}

	public void setUserField06 ( final String userfield06 ) 
	{
		this.userfield06 = userfield06;
		b_userfield06 = true;
	}

	public void setUserField07 ( final String userfield07 ) 
	{
		this.userfield07 = userfield07;
		b_userfield07 = true;
	}

	public void setUserField08 ( final String userfield08 ) 
	{
		this.userfield08 = userfield08;
		b_userfield08 = true;
	}

	public void setUserField09 ( final String userfield09 ) 
	{
		this.userfield09 = userfield09;
		b_userfield09 = true;
	}

	public void setUserField10 ( final String userfield10 ) 
	{
		this.userfield10 = userfield10;
		b_userfield10 = true;
	}

	public void setUserField11 ( final String userfield11 ) 
	{
		this.userfield11 = userfield11;
		b_userfield11 = true;
	}

	public void setUserField12 ( final String userfield12 ) 
	{
		this.userfield12 = userfield12;
		b_userfield12 = true;
	}

	public void setUserField13 ( final String userfield13 ) 
	{
		this.userfield13 = userfield13;
		b_userfield13 = true;
	}

	public void setUserField14 ( final String userfield14 ) 
	{
		this.userfield14 = userfield14;
		b_userfield14 = true;
	}

	public void setUserField15 ( final String userfield15 ) 
	{
		this.userfield15 = userfield15;
		b_userfield15 = true;
	}

	public void setUserField16 ( final String userfield16 ) 
	{
		this.userfield16 = userfield16;
		b_userfield16 = true;
	}

	public void setUserField17 ( final String userfield17 ) 
	{
		this.userfield17 = userfield17;
		b_userfield17 = true;
	} 

	public void setUserField18 ( final String userfield18 ) 
	{
		this.userfield18 = userfield18;
		b_userfield18 = true;
	}

	public void setUserField19 ( final String userfield19 ) 
	{
		this.userfield19 = userfield19;
		b_userfield19 = true;
	}

	public void setUserField20 ( final String userfield20 ) 
	{
		this.userfield20 = userfield20;
		b_userfield20 = true;
	}
	
	public void setImage1( final byte[]  image1 ) 
	{
		this.image1 = image1;
		b_containsImage = true;
		b_image1 = true;
		number_of_images++;
	}

	public void setImageContentType( final String imageContentType ) 
	{
		this.imageContentType = imageContentType;
		bImageContentType = true;
	}
	
	public void setNumberOfLinks( final int number_of_links ) 
	{
		this.number_of_links = number_of_links;
		b_number_of_links = true;
	}
	
	public void setNumberOfDistributionLists(final int listsize){
		this.number_of_dlists = listsize;
		this.b_number_of_dlists = true;
		markAsDistributionlist();
	}

	public void setNumberOfDistributionLists(final DistributionListEntryObject[] dleos){
		this.dlists = dleos;
		this.number_of_dlists = dleos.length;
		this.b_number_of_dlists = true;
		markAsDistributionlist();
	}
	
	public void setDistributionList(final DistributionListEntryObject[] dleo) 
	{	
		this.dlists = dleo;
		this.number_of_dlists = dleo.length;
		b_number_of_dlists = true;
		markAsDistributionlist();
	}

	public void setLinks(final LinkEntryObject[] links) {
		this.links = links;
		this.number_of_links = links.length;
		b_number_of_links = true;
	}

	public void setContextId( final int cid) 
	{
		this.cid = cid;
		b_cid = true;
	}

	public void setInternalUserId( final int internal_userId ) 
	{
		this.internal_userId = internal_userId;
		b_internal_userId = true;
	}
	
	public void setImageLastModified( final Date image_last_modified ) 
	{
		this.image_last_modified = image_last_modified;
		b_image_last_modified = true;
	}

	public void setFileAs( final String file_as ) 
	{
		this.file_as = file_as;
		b_file_as = true;
	}
	public void setMarkAsDistributionlist(final boolean mark_as_disitributionlist){
		this.mark_as_distributionlist = mark_as_disitributionlist;
		b_mark_as_distributionlist = true;
	}
	public void markAsDistributionlist(){
		setMarkAsDistributionlist(true);
	}
	
	// REMOVE METHODS
	public void removeDisplayName ( ) 
	{
		display_name = null;
		b_display_name = false;
	}
	
	public void removeGivenName ( ) 
	{
		given_name = null;
		b_given_name = false;
	}
	
	public void removeSurName ( ) 
	{
		sur_name = null;
		b_sur_name = false;
	}
	
	public void removeMiddleName ( ) 
	{
		middle_name = null;
		b_middle_name = false;
	}
	
	public void removeSuffix ( ) 
	{
		suffix = null;
		b_suffix = false;
	}
	
	public void removeTitle ( ) 
	{
		title = null;
		b_title = false;
	}
	
	public void removeStreetHome ( ) 
	{
		street = null;
		b_street = false;
	}
	
	public void removePostalCodeHome ( ) 
	{
		postal_code = null;
		b_postal_code = false;
	}
	
	public void removeCityHome ( ) 
	{
		city = null;
		b_city = false;
	}
	
	public void removeStateHome ( ) 
	{
		state = null;
		b_state = false;
	}
	
	public void removeCountryHome ( ) 
	{
		country = null; 
		b_country = false;
	}
	
	public void removeBirthday ( ) 
	{
		birthday = null;
		b_birthday = false;
	}
	
	public void removeMaritalStatus ( ) 
	{
		marital_status = null;
		b_marital_status = false;
	}
	
	public void removeNumberOfChildren ( ) 
	{
		number_of_children = null;
		b_number_of_children = false;
	}

	public void removeProfession ( ) 
	{
		profession = null;
		b_profession = false;
	}

	public void removeNickname ( ) 
	{
		nickname = null;
		b_nickname = false;
	}
	
	public void removeSpouseName ( ) 
	{
		spouse_name = null;
		b_spouse_name = false;
	}
	
	public void removeAnniversary ( ) 
	{
		anniversary = null;
		b_anniversary = false;
	}
	
	public void removeNote ( ) 
	{
		note = null;
		b_note = false;
	}
	
	public void removeDepartment ( ) 
	{
		department = null;
		b_department = false;
	}
	
	public void removePosition ( ) 
	{
		position = null;
		b_position = false;
	}
	
	public void removeEmployeeType ( ) 
	{
		employee_type = null;
		b_employee_type = false;
	}
	
	public void removeRoomNumber ( ) 
	{
		room_number = null;
		b_room_number = false;
	}
	
	public void removeStreetBusiness ( ) 
	{
		street_business = null;
		b_street_business = false;
	}

	public void removePostalCodeBusiness ( ) 
	{
		postal_code_business = null;
		b_postal_code_business = false;
	}
	
	public void removeCityBusiness ( ) 
	{
		city_business = null;
		b_city_business = false;
	}
	
	public void removeStateBusiness ( ) 
	{
		state_business = null;
		b_state_business = false;
	}
	
	public void removeCountryBusiness ( ) 
	{
		country_business = null;
		b_country_business = false;
	}
	
	public void removeNumberOfEmployee ( ) 
	{
		number_of_employee = null;
		b_number_of_employee = false;
	}

	public void removeSalesVolume ( ) 
	{
		sales_volume = null;
		b_sales_volume = false;
	}
	
	public void removeTaxID ( ) 
	{
		tax_id = null;
		b_tax_id = false;
	}
	
	public void removeCommercialRegister ( ) 
	{
		commercial_register = null;
		b_commercial_register = false;
	}
	
	public void removeBranches ( ) 
	{
		branches = null;
		b_branches = false;
	}
	
	public void removeBusinessCategory ( ) 
	{
		business_category = null;
		b_business_category = false;
	}
	
	public void removeInfo ( ) 
	{
		info = null;
		b_info = false;
	}
	
	public void removeManagerName ( ) 
	{
		manager_name = null;
		b_manager_name = false;
	}
	
	public void removeAssistantName ( ) 
	{
		assistant_name = null;
		b_assistant_name = false;
	}
	
	public void removeStreetOther ( ) 
	{
		street_other = null;
		b_street_other = false;
	}

	public void removePostalCodeOther ( ) 
	{
		postal_code_other = null;
		b_postal_code_other = false;
	}
	
	public void removeCityOther ( ) 
	{
		city_other = null;
		b_city_other = false;
	}
	
	public void removeStateOther ( ) 
	{
		state_other = null;
		b_state_other = false;
	}
	
	public void removeCountryOther( ) 
	{
		country_other = null;
		b_country_other = false;
	}
	
	public void removeTelephoneBusiness1( ) 
	{
		telephone_business1 = null;
		b_telephone_business1 = false;
	}
	
	public void removeTelephoneBusiness2( ) 
	{
		telephone_business2 = null;
		b_telephone_business2 = false;
	}
	
	public void removeFaxBusiness( ) 
	{
		fax_business = null;
		b_fax_business = false;
	}
	
	public void removeTelephoneCallback( ) 
	{
		telephone_callback = null;
		b_telephone_callback = false;
	}
	
	public void removeTelephoneCar( ) 
	{
		telephone_car = null;
		b_telephone_car = false;
	}
	
	public void removeTelephoneCompany( ) 
	{
		telephone_company = null;
		b_telephone_company = false;
	}
	
	public void removeTelephoneHome1( ) 
	{
		telephone_home1 = null;
		b_telephone_home1 = false;
	}
	
	public void removeTelephoneHome2( ) 
	{
		telephone_home2 = null;
		b_telephone_home2 = false;
	}
	
	public void removeFaxHome( ) 
	{
		fax_home = null;
		b_fax_home = false;
	}
	
	public void removeCellularTelephone1( ) 
	{
		cellular_telephone1 = null;
		b_cellular_telephone1 = false;
	}
	
	public void removeCellularTelephone2( ) 
	{
		cellular_telephone2 = null;
		b_cellular_telephone2 = false;
	}
	
	public void removeTelephoneOther( ) 
	{
		telephone_other = null;
		b_telephone_other = false;
	}
	
	public void removeFaxOther( ) 
	{
		fax_other = null;
		b_fax_other = false;
	}
	
	public void removeEmail1( ) 
	{
		email1 = null;
		b_email1 = false;
	}

	public void removeEmail2( ) 
	{
		email2 = null;
		b_email2 = false;
	}

	public void removeEmail3( ) 
	{
		email3 = null;
		b_email3 = false;
	}

	public void removeURL( )
	{
		url = null;
		b_url = false;
	}
	
	public void removeTelephoneISDN( ) 
	{
		telephone_isdn = null;
		b_telephone_isdn = false;
	}

	public void removeTelephonePager( ) 
	{
		telephone_pager = null;
		b_telephone_pager = false;
	}

	public void removeTelephonePrimary( ) 
	{
		telephone_primary = null;
		b_telephone_primary = false;
	}

	public void removeTelephoneRadio( ) 
	{
		telephone_radio = null;
		b_telephone_radio = false;
	}

	public void removeTelephoneTelex( ) 
	{
		telephone_telex = null;
		b_telephone_telex = false;
	}

	public void removeTelephoneTTYTTD( ) 
	{
		telephone_ttytdd = null;
		b_telephone_ttytdd = false;
	}

	public void removeInstantMessenger1( ) 
	{
		instant_messenger1 = null;
		b_instant_messenger1 = false;
	}

	public void removeInstantMessenger2( ) 
	{
		instant_messenger2 = null;
		b_instant_messenger2 = false;
	}
	
	public void removeTelephoneIP( ) 
	{
		telephone_ip = null;
		b_telephone_ip = false;
	}

	public void removeTelephoneAssistant( ) 
	{
		telephone_assistant = null;
		b_telephone_assistant = false;
	}

	public void removeDefaultAddress( ) 
	{
		defaultaddress = 0;
		b_defaultaddress = false;
	}

	public void removeCompany( ) 
	{
		company = null;
		b_company = false;
	}
	
	public void removeImage1( ) 
	{
		image1 =null;
		b_containsImage = false;
		b_image1 = false;
		number_of_images = 0;
	}
	
	public void removeImageContentType( ) 
	{
		imageContentType = null;
		bImageContentType = false;
	}
	
	public void removeUserField01( ) 
	{
		userfield01 = null;
		b_userfield01 = false;
	}

	public void removeUserField02( ) 
	{
		userfield02 = null;
		b_userfield02 = false;
	}

	public void removeUserField03( ) 
	{
		userfield03 = null;
		b_userfield03 = false;
	}

	public void removeUserField04( ) 
	{
		userfield04 = null;
		b_userfield04 = false;
	}

	public void removeUserField05( ) 
	{
		userfield05 = null;
		b_userfield05 = false;
	}

	public void removeUserField06( ) 
	{
		userfield06 = null;
		b_userfield06 = false;
	}

	public void removeUserField07( ) 
	{
		userfield07 = null;
		b_userfield07 = false;
	}

	public void removeUserField08( ) 
	{
		userfield08 = null;
		b_userfield08 = false;
	}

	public void removeUserField09( ) 
	{
		userfield09 = null;
		b_userfield09 = false;
	}

	public void removeUserField10( ) 
	{
		userfield10 = null;
		b_userfield10 = false;
	}

	public void removeUserField11( ) 
	{
		userfield11 = null;
		b_userfield11 = false;
	}

	public void removeUserField12( ) 
	{
		userfield12 = null;
		b_userfield12 = false;
	}

	public void removeUserField13( ) 
	{
		userfield13 = null;
		b_userfield13 = false;
	}

	public void removeUserField14( ) 
	{
		userfield14 = null;
		b_userfield14 = false;
	}

	public void removeUserField15( ) 
	{
		userfield15 = null;
		b_userfield15 = false;
	}

	public void removeUserField16( ) 
	{
		userfield16 = null;
		b_userfield16 = false;
	}

	public void removeUserField17( ) 
	{
		userfield17 = null;
		b_userfield17 = false;
	} 

	public void removeUserField18( ) 
	{
		userfield18 = null;
		b_userfield18 = false;
	}

	public void removeUserField19( ) 
	{
		userfield19 = null;
		b_userfield19 = false;
	}

	public void removeUserField20( ) 
	{
		userfield20 = null;
		b_userfield20 = false;
	}

	public void removeNumberOfLinks( ) 
	{
		links = null;
		number_of_links = 0;
		b_number_of_links = false;
	}
	
	public void removeNumberOfDistributionLists() {
		dlists = null;
		number_of_dlists = 0;
		b_number_of_dlists = false;
	}

	public void removeDistributionLists() {
		dlists = null;
		number_of_dlists = 0;
		b_number_of_dlists = false;
	}
	
	public void removeLinks() {
		links = null;
		b_number_of_links = false;
		number_of_links = 0;
	}
	public void removeMarkAsDistributionlist(){
		mark_as_distributionlist = false;
		b_mark_as_distributionlist = false;
	}
	public void removeContextID(){
		cid = 0;
		b_cid = false;
	}
	
	public void removeInternalUserId() {
		internal_userId = 0;
		b_internal_userId = false;
	}
	
	// CONTAINS METHODS
	public boolean containsDisplayName ( ) 
	{
		return b_display_name;
	}
	
	public boolean containsGivenName ( ) 
	{
		return b_given_name;
	}
	
	public boolean containsSurName ( ) 
	{
		return b_sur_name;
	}
	
	public boolean containsMiddleName ( ) 
	{
		return b_middle_name;
	}
	
	public boolean containsSuffix ( ) 
	{
		return b_suffix;
	}
	
	public boolean containsTitle ( ) 
	{
		return b_title;
	}
	
	public boolean containsStreetHome ( ) 
	{
		return b_street;
	}
	
	public boolean containsPostalCodeHome ( ) 
	{
		return b_postal_code;
	}
	
	public boolean containsCityHome ( ) 
	{
		return b_city;
	}
	
	public boolean containsStateHome ( ) 
	{
		return b_state;
	}
	
	public boolean containsCountryHome ( ) 
	{
		return b_country;
	}
	
	public boolean containsBirthday ( ) 
	{
		return b_birthday;
	}
	
	public boolean containsMaritalStatus ( ) 
	{
		return b_marital_status;
	}
	
	public boolean containsNumberOfChildren ( ) 
	{
		return b_number_of_children;
	}

	public boolean containsProfession ( ) 
	{
		return b_profession;
	}

	public boolean containsNickname ( ) 
	{
		return b_nickname;
	}
	
	public boolean containsSpouseName ( ) 
	{
		return b_spouse_name;
	}
	
	public boolean containsAnniversary ( ) 
	{
		return b_anniversary;
	}
	
	public boolean containsNote ( ) 
	{
		return b_note;
	}
	
	public boolean containsDepartment ( ) 
	{
		return b_department;
	}
	
	public boolean containsPosition ( ) 
	{
		return b_position;
	}
	
	public boolean containsEmployeeType ( ) 
	{
		return b_employee_type;
	}
	
	public boolean containsRoomNumber ( ) 
	{
		return b_room_number;
	}
	
	public boolean containsStreetBusiness ( ) 
	{
		return b_street_business;
	}

	public boolean containsPostalCodeBusiness ( ) 
	{
		return b_postal_code_business;
	}
	
	public boolean containsCityBusiness ( ) 
	{
		return b_city_business;
	}
	
	public boolean containsStateBusiness ( ) 
	{
		return b_state_business;
	}
	
	public boolean containsCountryBusiness ( ) 
	{
		return b_country_business;
	}
	
	public boolean containsNumberOfEmployee ( ) 
	{
		return b_number_of_employee;
	}

	public boolean containsSalesVolume ( ) 
	{
		return b_sales_volume;
	}
	
	public boolean containsTaxID ( ) 
	{
		return b_tax_id;
	}
	
	public boolean containsCommercialRegister ( ) 
	{
		return b_commercial_register;
	}
	
	public boolean containsBranches ( ) 
	{
		return b_branches;
	}
	
	public boolean containsBusinessCategory ( ) 
	{
		return b_business_category;
	}
	
	public boolean containsInfo ( ) 
	{
		return b_info;
	}
	
	public boolean containsManagerName ( ) 
	{
		return b_manager_name;
	}
	
	public boolean containsAssistantName ( ) 
	{
		return b_assistant_name;
	}
	
	public boolean containsStreetOther ( ) 
	{
		return b_street_other;
	}

	public boolean containsPostalCodeOther ( ) 
	{
		return b_postal_code_other;
	}
	
	public boolean containsCityOther ( ) 
	{
		return b_city_other;
	}
	
	public boolean containsStateOther ( ) 
	{
		return b_state_other;
	}
	
	public boolean containsCountryOther( ) 
	{
		return b_country_other;
	}
	
	public boolean containsTelephoneBusiness1( ) 
	{
		return b_telephone_business1;
	}
	
	public boolean containsTelephoneBusiness2( ) 
	{
		return b_telephone_business2;
	}
	
	public boolean containsFaxBusiness( ) 
	{
		return b_fax_business;
	}
	
	public boolean containsTelephoneCallback( ) 
	{
		return b_telephone_callback;
	}
	
	public boolean containsTelephoneCar( ) 
	{
		return b_telephone_car;
	}
	
	public boolean containsTelephoneCompany( ) 
	{
		return b_telephone_company;
	}
	
	public boolean containsTelephoneHome1( ) 
	{
		return b_telephone_home1;
	}
	
	public boolean containsTelephoneHome2( ) 
	{
		return b_telephone_home2;
	}
	
	public boolean containsFaxHome( ) 
	{
		return b_fax_home;
	}
	
	public boolean containsCellularTelephone1( ) 
	{
		return b_cellular_telephone1;
	}
	
	public boolean containsCellularTelephone2( ) 
	{
		return b_cellular_telephone2;
	}
	
	public boolean containsTelephoneOther( ) 
	{
		return b_telephone_other;
	}
	
	public boolean containsFaxOther( ) 
	{
		return b_fax_other;
	}
	
	public boolean containsEmail1( ) 
	{
		return b_email1;
	}

	public boolean containsEmail2( ) 
	{
		return b_email2;
	}

	public boolean containsEmail3( ) 
	{
		return b_email3;
	}

	public boolean containsURL( ) 
	{
		return b_url;
	}
	
	public boolean containsTelephoneISDN( ) 
	{
		return b_telephone_isdn;
	}

	public boolean containsTelephonePager( ) 
	{
		return b_telephone_pager;
	}

	public boolean containsTelephonePrimary( ) 
	{
		return b_telephone_primary;
	}

	public boolean containsTelephoneRadio( ) 
	{
		return b_telephone_radio;
	}

	public boolean containsTelephoneTelex( ) 
	{
		return b_telephone_telex;
	}

	public boolean containsTelephoneTTYTTD( ) 
	{
		return b_telephone_ttytdd;
	}

	public boolean containsInstantMessenger1( ) 
	{
		return b_instant_messenger1;
	}

	public boolean containsInstantMessenger2( ) 
	{
		return b_instant_messenger2;
	}
	
	public boolean containsTelephoneIP( ) 
	{
		return b_telephone_ip;
	}

	public boolean containsTelephoneAssistant( ) 
	{
		return b_telephone_assistant;
	}

	public boolean containsDefaultAddress( ) 
	{
		return b_defaultaddress;
	}

	public boolean containsCompany( ) 
	{
		return b_company;
	}
	
	public boolean containsUserField01( ) 
	{
		return b_userfield01;
	}

	public boolean containsUserField02( ) 
	{
		return b_userfield02;
	}

	public boolean containsUserField03( ) 
	{
		return b_userfield03;
	}

	public boolean containsUserField04( ) 
	{
		return b_userfield04;
	}

	public boolean containsUserField05( ) 
	{
		return b_userfield05;
	}

	public boolean containsUserField06( ) 
	{
		return b_userfield06;
	}

	public boolean containsUserField07( ) 
	{
		return b_userfield07;
	}

	public boolean containsUserField08( ) 
	{
		return b_userfield08;
	}

	public boolean containsUserField09( ) 
	{
		return b_userfield09;
	}

	public boolean containsUserField10( ) 
	{
		return b_userfield10;
	}

	public boolean containsUserField11( ) 
	{
		return b_userfield11;
	}

	public boolean containsUserField12( ) 
	{
		return b_userfield12;
	}

	public boolean containsUserField13( ) 
	{
		return b_userfield13;
	}

	public boolean containsUserField14( ) 
	{
		return b_userfield14;
	}

	public boolean containsUserField15( ) 
	{
		return b_userfield15;
	}

	public boolean containsUserField16( ) 
	{
		return b_userfield16;
	}

	public boolean containsUserField17( ) 
	{
		return b_userfield17;
	}

	public boolean containsUserField18( ) 
	{
		return b_userfield18;
	}

	public boolean containsUserField19( ) 
	{
		return b_userfield19;
	}

	public boolean containsUserField20( ) 
	{
		return b_userfield20;
	}

	public boolean containsImage1( ) 
	{
		return b_containsImage;
	}
	
	public boolean containsImageContentType( ) 
	{
		return bImageContentType;
	}
	
	public boolean containsLinks() {
		return (links != null);
	}

	public boolean containsNumberOfLinks() {
		return b_number_of_links;
	}

	public int getSizeOfLinks() {
		return number_of_links;
	}

	public boolean containsNumberOfDistributionLists() {
		return b_number_of_dlists;
	}

	public boolean containsDistributionLists() {
		return (dlists != null);
	}
	
	public int getSizeOfDistributionListArray() {
		return number_of_dlists;
	}

	public boolean containsInternalUserId() {
		return b_internal_userId;
	}

	public boolean containsContextId() {
		return b_cid;
	}

	public boolean containsImageLastModified() {
		return b_image_last_modified;
	}
	
	public boolean containsFileAs() {
		return b_file_as;
	}
	
	public boolean containsMarkAsDistributionlist(){
		return b_mark_as_distributionlist;
	}
}
