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

package com.openexchange.groupware.contact.helpers;

import com.openexchange.groupware.contact.ContactException;

/**
 * This class represent constants as used in the classes Contacts
 * and ContactObject. It also allows for operations to be performed
 * using one of these fields using the class ContactSwitcher.
 * 
 * Reason: Though Contacts allows for single object operations, you cannot
 * do bulk operations like "these are my field names and here is a list
 * of values for them, insert them". This class allows to find fields by
 * different names and IDs, then write a Switcher which will perform an
 * action (on one or more ContactObjects in most cases). 
 * 
 * The design pattern used has no name to my knowledge but is used in 
 * Hibernate under the name Switcher.
 * 
 * Note: This class was mostly generated automatically - don't even think
 * about trying to keep it up to date by hand.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public enum ContactField{  
	  DISPLAY_NAME (500 , "field01" , "DISPLAY_NAME" , "Display name" ),
	  SUR_NAME (502 , "field02" , "SUR_NAME" , "Sur name" ),
	  GIVEN_NAME (501 , "field03" , "GIVEN_NAME" , "Given name" ),
	  MIDDLE_NAME (503 , "field04" , "MIDDLE_NAME" , "Middle name" ),
	  SUFFIX (504 , "field05" , "SUFFIX" , "Suffix" ),
	  TITLE (505 , "field06" , "TITLE" , "Title" ),
	  STREET_HOME (506 , "field07" , "STREET_HOME" , "Street home" ),
	  POSTAL_CODE_HOME (507 , "field08" , "POSTAL_CODE_HOME" , "Postal code home" ),
	  CITY_HOME (508 , "field09" , "CITY_HOME" , "City home" ),
	  STATE_HOME (509 , "field10" , "STATE_HOME" , "State home" ),
	  COUNTRY_HOME (510 , "field11" , "COUNTRY_HOME" , "Country home" ),
	  MARITAL_STATUS (512 , "field12" , "MARITAL_STATUS" , "Martial status" ),
	  NUMBER_OF_CHILDREN (513 , "field13" , "NUMBER_OF_CHILDREN" , "Number of children" ),
	  PROFESSION (514 , "field14" , "PROFESSION" , "Profession" ),
	  NICKNAME (515 , "field15" , "NICKNAME" , "Nickname" ),
	  SPOUSE_NAME (516 , "field16" , "SPOUSE_NAME" , "Spouse name" ),
	  NOTE (518 , "field17" , "NOTE" , "Note" ),
	  COMPANY (569 , "field18" , "COMPANY" , "Company" ),
	  DEPARTMENT (519 , "field19" , "DEPARTMENT" , "Department" ),
	  POSITION (520 , "field20" , "POSITION" , "Position" ),
	  EMPLOYEE_TYPE (521 , "field21" , "EMPLOYEE_TYPE" , "Employee type" ),
	  ROOM_NUMBER (522 , "field22" , "ROOM_NUMBER" , "Room number" ),
	  STREET_BUSINESS (523 , "field23" , "STREET_BUSINESS" , "Street business" ),
	  POSTAL_CODE_BUSINESS (525 , "field24" , "POSTAL_CODE_BUSINESS" , "Postal code business" ),
	  CITY_BUSINESS (526 , "field25" , "CITY_BUSINESS" , "City business" ),
	  STATE_BUSINESS (527 , "field26" , "STATE_BUSINESS" , "State business" ),
	  COUNTRY_BUSINESS (528 , "field27" , "COUNTRY_BUSINESS" , "Country business" ),
	  NUMBER_OF_EMPLOYEE (529 , "field28" , "NUMBER_OF_EMPLOYEE" , "Number of employee" ),
	  SALES_VOLUME (530 , "field29" , "SALES_VOLUME" , "Sales volume" ),
	  TAX_ID (531 , "field30" , "TAX_ID" , "Tax id" ),
	  COMMERCIAL_REGISTER (532 , "field31" , "COMMERCIAL_REGISTER" , "Commercial register" ),
	  BRANCHES (533 , "field32" , "BRANCHES" , "Branches" ),
	  BUSINESS_CATEGORY (534 , "field33" , "BUSINESS_CATEGORY" , "Business category" ),
	  INFO (535 , "field34" , "INFO" , "Info" ),
	  MANAGER_NAME (536 , "field35" , "MANAGER_NAME" , "Manager's name" ),
	  ASSISTANT_NAME (537 , "field36" , "ASSISTANT_NAME" , "Assistant's name" ),
	  STREET_OTHER (538 , "field37" , "STREET_OTHER" , "Street other" ),
	  POSTAL_CODE_OTHER (540 , "field38" , "POSTAL_CODE_OTHER" , "Postal code other" ),
	  CITY_OTHER (539 , "field39" , "CITY_OTHER" , "City other" ),
	  STATE_OTHER (598 , "field40" , "STATE_OTHER" , "State other" ),
	  COUNTRY_OTHER (541 , "field41" , "COUNTRY_OTHER" , "Country other" ),
	  TELEPHONE_ASSISTANT (568 , "field42" , "TELEPHONE_ASSISTANT" , "Telephone assostant" ),
	  TELEPHONE_BUSINESS1 (542 , "field43" , "TELEPHONE_BUSINESS1" , "Telephone business 1" ),
	  TELEPHONE_BUSINESS2 (543 , "field44" , "TELEPHONE_BUSINESS2" , "Telephone business 2" ),
	  FAX_BUSINESS (544 , "field45" , "FAX_BUSINESS" , "FAX business" ),
	  TELEPHONE_CALLBACK (545 , "field46" , "TELEPHONE_CALLBACK" , "Telephone callback" ),
	  TELEPHONE_CAR (546 , "field47" , "TELEPHONE_CAR" , "Telephone car" ),
	  TELEPHONE_COMPANY (547 , "field48" , "TELEPHONE_COMPANY" , "Telephone company" ),
	  TELEPHONE_HOME1 (548 , "field49" , "TELEPHONE_HOME1" , "Telephone home 1" ),
	  TELEPHONE_HOME2 (549 , "field50" , "TELEPHONE_HOME2" , "Telephone home 2" ),
	  FAX_HOME (550 , "field51" , "FAX_HOME" , "FAX home" ),
	  TELEPHONE_ISDN (559 , "field52" , "TELEPHONE_ISDN" , "Telephone ISDN" ),
	  CELLULAR_TELEPHONE1 (551 , "field53" , "CELLULAR_TELEPHONE1" , "Cellular telephone 1" ),
	  CELLULAR_TELEPHONE2 (552 , "field54" , "CELLULAR_TELEPHONE2" , "Cellular telephone 2" ),
	  TELEPHONE_OTHER (553 , "field55" , "TELEPHONE_OTHER" , "Telephone other" ),
	  FAX_OTHER (554 , "field56" , "FAX_OTHER" , "FAX other" ),
	  TELEPHONE_PAGER (560 , "field57" , "TELEPHONE_PAGER" , "Telephone pager" ),
	  TELEPHONE_PRIMARY (561 , "field58" , "TELEPHONE_PRIMARY" , "Telephone primary" ),
	  TELEPHONE_RADIO (562 , "field59" , "TELEPHONE_RADIO" , "Telephone radio" ),
	  TELEPHONE_TELEX (563 , "field60" , "TELEPHONE_TELEX" , "Telephone telex" ),
	  TELEPHONE_TTYTDD (564 , "field61" , "TELEPHONE_TTYTDD" , "Telephone TTY/TDD" ),
	  INSTANT_MESSENGER1 (565 , "field62" , "INSTANT_MESSENGER1" , "Instantmessenger 1" ),
	  INSTANT_MESSENGER2 (566 , "field63" , "INSTANT_MESSENGER2" , "Instantmessenger 2" ),
	  TELEPHONE_IP (567 , "field64" , "TELEPHONE_IP" , "Telephone IP" ),
	  EMAIL1 (555 , "field65" , "EMAIL1" , "Email 1" ),
	  EMAIL2 (556 , "field66" , "EMAIL2" , "Email 2" ),
	  EMAIL3 (557 , "field67" , "EMAIL3" , "Email 3" ),
	  URL (558 , "field68" , "URL" , "URL" ),
	  CATEGORIES (100 , "field69" , "CATEGORIES" , "Categories" ),
	  USERFIELD01 (571 , "field70" , "USERFIELD01" , "Dynamic Field 1" ),
	  USERFIELD02 (572 , "field71" , "USERFIELD02" , "Dynamic Field 2" ),
	  USERFIELD03 (573 , "field72" , "USERFIELD03" , "Dynamic Field 3" ),
	  USERFIELD04 (574 , "field73" , "USERFIELD04" , "Dynamic Field 4" ),
	  USERFIELD05 (575 , "field74" , "USERFIELD05" , "Dynamic Field 5" ),
	  USERFIELD06 (576 , "field75" , "USERFIELD06" , "Dynamic Field 6" ),
	  USERFIELD07 (577 , "field76" , "USERFIELD07" , "Dynamic Field 7" ),
	  USERFIELD08 (578 , "field77" , "USERFIELD08" , "Dynamic Field 8" ),
	  USERFIELD09 (579 , "field78" , "USERFIELD09" , "Dynamic Field 9" ),
	  USERFIELD10 (580 , "field79" , "USERFIELD10" , "Dynamic Field 10" ),
	  USERFIELD11 (581 , "field80" , "USERFIELD11" , "Dynamic Field 11" ),
	  USERFIELD12 (582 , "field81" , "USERFIELD12" , "Dynamic Field 12" ),
	  USERFIELD13 (583 , "field82" , "USERFIELD13" , "Dynamic Field 13" ),
	  USERFIELD14 (584 , "field83" , "USERFIELD14" , "Dynamic Field 14" ),
	  USERFIELD15 (585 , "field84" , "USERFIELD15" , "Dynamic Field 15" ),
	  USERFIELD16 (586 , "field85" , "USERFIELD16" , "Dynamic Field 16" ),
	  USERFIELD17 (587 , "field86" , "USERFIELD17" , "Dynamic Field 17" ),
	  USERFIELD18 (588 , "field87" , "USERFIELD18" , "Dynamic Field 18" ),
	  USERFIELD19 (589 , "field88" , "USERFIELD19" , "Dynamic Field 19" ),
	  USERFIELD20 (590 , "field89" , "USERFIELD20" , "Dynamic Field 20" ),
	  OBJECT_ID (1 , "intfield01" , "OBJECT_ID" , "Object id" ),
	  NUMBER_OF_DISTRIBUTIONLIST (594 , "intfield02" , "NUMBER_OF_DISTRIBUTIONLIST" , "Number of distributionlists" ),
	  NUMBER_OF_LINKS (103 , "intfield03" , "NUMBER_OF_LINKS" , "Number of links" ),
	  DISTRIBUTIONLIST (592 , "intfield02" , "DISTRIBUTIONLIST" , "" ),
	  LINKS (591 , "intfield03" , "LINKS" , "" ),
	  FOLDER_ID (20 , "fid" , "FOLDER_ID" , "Folder id" ),
	  CONTEXTID (593 , "cid" , "CONTEXTID" , "Context id" ),
	  PRIVATE_FLAG (101 , "pflag" , "PRIVATE_FLAG" , "" ),
	  CREATED_BY (2 , "created_from" , "CREATED_BY" , "Created by" ),
	  MODIFIED_BY (3 , "changed_from" , "MODIFIED_BY" , "Modified by" ),
	  CREATION_DATE (4 , "creating_date" , "CREATION_DATE" , "Creation date" ),
	  LAST_MODIFIED (5 , "changing_date" , "LAST_MODIFIED" , "Changing date" ),
	  BIRTHDAY (511 , "timestampfield01" , "BIRTHDAY" , "Birthday" ),
	  ANNIVERSARY (517 , "timestampfield02" , "ANNIVERSARY" , "Anniversay" ),
	  IMAGE1 (570 , "intfield04" , "IMAGE1" , "" ),
	  IMAGE_LAST_MODIFIED (597 , "intfield04" , "IMAGE_LAST_MODIFIED" , "" ),
	  IMAGE1_CONTENT_TYPE (601 , "intfield04" , "IMAGE1_CONTENT_TYPE" , "" ),
	  INTERNAL_USERID (524 , "userid" , "INTERNAL_USERID" , "" ),
	  COLOR_LABEL (102 , "intfield05" , "COLOR_LABEL" , "" ),
	  FILE_AS (599 , "field90" , "FILE_AS" , "" ),
	  DEFAULT_ADDRESS (605 , "intfield06" , "DEFAULT_ADDRESS" , "Default address" ),
	  MARK_AS_DISTRIBUTIONLIST (602 , "intfield07" , "MARK_AS_DISTRIBUTIONLIST" , "" ),
	  NUMBER_OF_ATTACHMENTS (104 , "intfield08" , "NUMBER_OF_ATTACHMENTS" , "" );


  private int columnNumber;
  private String dbName, readableName, fieldName;

  private ContactField(int columnNumber, String fieldName, String dbName, String readableName){
	this.fieldName = fieldName;
    this.columnNumber = columnNumber;
    this.dbName = dbName ;
    this.readableName = readableName;
  }
  
  public int getNumber(){
	  return columnNumber;
  }
  
  public String getDBName(){
	  return dbName;
  }
  
  public String getReadableName(){
	  return readableName;
  }
  
  public String getFieldName(){
	  return fieldName;
  }
  
  public static ContactField getByDBFieldName(final String dbFieldName){
	  if ( dbFieldName.equals( "field01" ) ) {
		  return DISPLAY_NAME;
	  } else if ( dbFieldName.equals( "field02" ) ) {
		  return SUR_NAME;
	  } else if ( dbFieldName.equals( "field03" ) ) {
		  return GIVEN_NAME;
	  } else if ( dbFieldName.equals( "field04" ) ) {
		  return MIDDLE_NAME;
	  } else if ( dbFieldName.equals( "field05" ) ) {
		  return SUFFIX;
	  } else if ( dbFieldName.equals( "field06" ) ) {
		  return TITLE;
	  } else if ( dbFieldName.equals( "field07" ) ) {
		  return STREET_HOME;
	  } else if ( dbFieldName.equals( "field08" ) ) {
		  return POSTAL_CODE_HOME;
	  } else if ( dbFieldName.equals( "field09" ) ) {
		  return CITY_HOME;
	  } else if ( dbFieldName.equals( "field10" ) ) {
		  return STATE_HOME;
	  } else if ( dbFieldName.equals( "field11" ) ) {
		  return COUNTRY_HOME;
	  } else if ( dbFieldName.equals( "field12" ) ) {
		  return MARITAL_STATUS;
	  } else if ( dbFieldName.equals( "field13" ) ) {
		  return NUMBER_OF_CHILDREN;
	  } else if ( dbFieldName.equals( "field14" ) ) {
		  return PROFESSION;
	  } else if ( dbFieldName.equals( "field15" ) ) {
		  return NICKNAME;
	  } else if ( dbFieldName.equals( "field16" ) ) {
		  return SPOUSE_NAME;
	  } else if ( dbFieldName.equals( "field17" ) ) {
		  return NOTE;
	  } else if ( dbFieldName.equals( "field18" ) ) {
		  return COMPANY;
	  } else if ( dbFieldName.equals( "field19" ) ) {
		  return DEPARTMENT;
	  } else if ( dbFieldName.equals( "field20" ) ) {
		  return POSITION;
	  } else if ( dbFieldName.equals( "field21" ) ) {
		  return EMPLOYEE_TYPE;
	  } else if ( dbFieldName.equals( "field22" ) ) {
		  return ROOM_NUMBER;
	  } else if ( dbFieldName.equals( "field23" ) ) {
		  return STREET_BUSINESS;
	  } else if ( dbFieldName.equals( "field24" ) ) {
		  return POSTAL_CODE_BUSINESS;
	  } else if ( dbFieldName.equals( "field25" ) ) {
		  return CITY_BUSINESS;
	  } else if ( dbFieldName.equals( "field26" ) ) {
		  return STATE_BUSINESS;
	  } else if ( dbFieldName.equals( "field27" ) ) {
		  return COUNTRY_BUSINESS;
	  } else if ( dbFieldName.equals( "field28" ) ) {
		  return NUMBER_OF_EMPLOYEE;
	  } else if ( dbFieldName.equals( "field29" ) ) {
		  return SALES_VOLUME;
	  } else if ( dbFieldName.equals( "field30" ) ) {
		  return TAX_ID;
	  } else if ( dbFieldName.equals( "field31" ) ) {
		  return COMMERCIAL_REGISTER;
	  } else if ( dbFieldName.equals( "field32" ) ) {
		  return BRANCHES;
	  } else if ( dbFieldName.equals( "field33" ) ) {
		  return BUSINESS_CATEGORY;
	  } else if ( dbFieldName.equals( "field34" ) ) {
		  return INFO;
	  } else if ( dbFieldName.equals( "field35" ) ) {
		  return MANAGER_NAME;
	  } else if ( dbFieldName.equals( "field36" ) ) {
		  return ASSISTANT_NAME;
	  } else if ( dbFieldName.equals( "field37" ) ) {
		  return STREET_OTHER;
	  } else if ( dbFieldName.equals( "field38" ) ) {
		  return POSTAL_CODE_OTHER;
	  } else if ( dbFieldName.equals( "field39" ) ) {
		  return CITY_OTHER;
	  } else if ( dbFieldName.equals( "field40" ) ) {
		  return STATE_OTHER;
	  } else if ( dbFieldName.equals( "field41" ) ) {
		  return COUNTRY_OTHER;
	  } else if ( dbFieldName.equals( "field42" ) ) {
		  return TELEPHONE_ASSISTANT;
	  } else if ( dbFieldName.equals( "field43" ) ) {
		  return TELEPHONE_BUSINESS1;
	  } else if ( dbFieldName.equals( "field44" ) ) {
		  return TELEPHONE_BUSINESS2;
	  } else if ( dbFieldName.equals( "field45" ) ) {
		  return FAX_BUSINESS;
	  } else if ( dbFieldName.equals( "field46" ) ) {
		  return TELEPHONE_CALLBACK;
	  } else if ( dbFieldName.equals( "field47" ) ) {
		  return TELEPHONE_CAR;
	  } else if ( dbFieldName.equals( "field48" ) ) {
		  return TELEPHONE_COMPANY;
	  } else if ( dbFieldName.equals( "field49" ) ) {
		  return TELEPHONE_HOME1;
	  } else if ( dbFieldName.equals( "field50" ) ) {
		  return TELEPHONE_HOME2;
	  } else if ( dbFieldName.equals( "field51" ) ) {
		  return FAX_HOME;
	  } else if ( dbFieldName.equals( "field52" ) ) {
		  return TELEPHONE_ISDN;
	  } else if ( dbFieldName.equals( "field53" ) ) {
		  return CELLULAR_TELEPHONE1;
	  } else if ( dbFieldName.equals( "field54" ) ) {
		  return CELLULAR_TELEPHONE2;
	  } else if ( dbFieldName.equals( "field55" ) ) {
		  return TELEPHONE_OTHER;
	  } else if ( dbFieldName.equals( "field56" ) ) {
		  return FAX_OTHER;
	  } else if ( dbFieldName.equals( "field57" ) ) {
		  return TELEPHONE_PAGER;
	  } else if ( dbFieldName.equals( "field58" ) ) {
		  return TELEPHONE_PRIMARY;
	  } else if ( dbFieldName.equals( "field59" ) ) {
		  return TELEPHONE_RADIO;
	  } else if ( dbFieldName.equals( "field60" ) ) {
		  return TELEPHONE_TELEX;
	  } else if ( dbFieldName.equals( "field61" ) ) {
		  return TELEPHONE_TTYTDD;
	  } else if ( dbFieldName.equals( "field62" ) ) {
		  return INSTANT_MESSENGER1;
	  } else if ( dbFieldName.equals( "field63" ) ) {
		  return INSTANT_MESSENGER2;
	  } else if ( dbFieldName.equals( "field64" ) ) {
		  return TELEPHONE_IP;
	  } else if ( dbFieldName.equals( "field65" ) ) {
		  return EMAIL1;
	  } else if ( dbFieldName.equals( "field66" ) ) {
		  return EMAIL2;
	  } else if ( dbFieldName.equals( "field67" ) ) {
		  return EMAIL3;
	  } else if ( dbFieldName.equals( "field68" ) ) {
		  return URL;
	  } else if ( dbFieldName.equals( "field69" ) ) {
		  return CATEGORIES;
	  } else if ( dbFieldName.equals( "field70" ) ) {
		  return USERFIELD01;
	  } else if ( dbFieldName.equals( "field71" ) ) {
		  return USERFIELD02;
	  } else if ( dbFieldName.equals( "field72" ) ) {
		  return USERFIELD03;
	  } else if ( dbFieldName.equals( "field73" ) ) {
		  return USERFIELD04;
	  } else if ( dbFieldName.equals( "field74" ) ) {
		  return USERFIELD05;
	  } else if ( dbFieldName.equals( "field75" ) ) {
		  return USERFIELD06;
	  } else if ( dbFieldName.equals( "field76" ) ) {
		  return USERFIELD07;
	  } else if ( dbFieldName.equals( "field77" ) ) {
		  return USERFIELD08;
	  } else if ( dbFieldName.equals( "field78" ) ) {
		  return USERFIELD09;
	  } else if ( dbFieldName.equals( "field79" ) ) {
		  return USERFIELD10;
	  } else if ( dbFieldName.equals( "field80" ) ) {
		  return USERFIELD11;
	  } else if ( dbFieldName.equals( "field81" ) ) {
		  return USERFIELD12;
	  } else if ( dbFieldName.equals( "field82" ) ) {
		  return USERFIELD13;
	  } else if ( dbFieldName.equals( "field83" ) ) {
		  return USERFIELD14;
	  } else if ( dbFieldName.equals( "field84" ) ) {
		  return USERFIELD15;
	  } else if ( dbFieldName.equals( "field85" ) ) {
		  return USERFIELD16;
	  } else if ( dbFieldName.equals( "field86" ) ) {
		  return USERFIELD17;
	  } else if ( dbFieldName.equals( "field87" ) ) {
		  return USERFIELD18;
	  } else if ( dbFieldName.equals( "field88" ) ) {
		  return USERFIELD19;
	  } else if ( dbFieldName.equals( "field89" ) ) {
		  return USERFIELD20;
	  } else if ( dbFieldName.equals( "intfield01" ) ) {
		  return OBJECT_ID;
	  } else if ( dbFieldName.equals( "intfield02" ) ) {
		  return NUMBER_OF_DISTRIBUTIONLIST;
	  } else if ( dbFieldName.equals( "intfield03" ) ) {
		  return NUMBER_OF_LINKS;
	  } else if ( dbFieldName.equals( "intfield02" ) ) {
		  return DISTRIBUTIONLIST;
	  } else if ( dbFieldName.equals( "intfield03" ) ) {
		  return LINKS;
	  } else if ( dbFieldName.equals( "fid" ) ) {
		  return FOLDER_ID;
	  } else if ( dbFieldName.equals( "cid" ) ) {
		  return CONTEXTID;
	  } else if ( dbFieldName.equals( "pflag" ) ) {
		  return PRIVATE_FLAG;
	  } else if ( dbFieldName.equals( "created_from" ) ) {
		  return CREATED_BY;
	  } else if ( dbFieldName.equals( "changed_from" ) ) {
		  return MODIFIED_BY;
	  } else if ( dbFieldName.equals( "creating_date" ) ) {
		  return CREATION_DATE;
	  } else if ( dbFieldName.equals( "changing_date" ) ) {
		  return LAST_MODIFIED;
	  } else if ( dbFieldName.equals( "timestampfield01" ) ) {
		  return BIRTHDAY;
	  } else if ( dbFieldName.equals( "timestampfield02" ) ) {
		  return ANNIVERSARY;
	  } else if ( dbFieldName.equals( "intfield04" ) ) {
		  return IMAGE1;
	  } else if ( dbFieldName.equals( "intfield04" ) ) {
		  return IMAGE_LAST_MODIFIED;
	  } else if ( dbFieldName.equals( "intfield04" ) ) {
		  return IMAGE1_CONTENT_TYPE;
	  } else if ( dbFieldName.equals( "userid" ) ) {
		  return INTERNAL_USERID;
	  } else if ( dbFieldName.equals( "intfield05" ) ) {
		  return COLOR_LABEL;
	  } else if ( dbFieldName.equals( "field90" ) ) {
		  return FILE_AS;
	  } else if ( dbFieldName.equals( "intfield06" ) ) {
		  return DEFAULT_ADDRESS;
	  } else if ( dbFieldName.equals( "intfield07" ) ) {
		  return MARK_AS_DISTRIBUTIONLIST;
	  } else if ( dbFieldName.equals( "intfield08" ) ) {
		  return NUMBER_OF_ATTACHMENTS;
	  } else {
		  return null;
	  }
  }

public static ContactField getByDisplayName(final String displayName){
  if ( displayName.equals( "Display name" ) ) {
    return DISPLAY_NAME;
  } else
  if ( displayName.equals( "Sur name" ) ) {
    return SUR_NAME;
  } else
  if ( displayName.equals( "Given name" ) ) {
    return GIVEN_NAME;
  } else
  if ( displayName.equals( "Middle name" ) ) {
    return MIDDLE_NAME;
  } else
  if ( displayName.equals( "Suffix" ) ) {
    return SUFFIX;
  } else
  if ( displayName.equals( "Title" ) ) {
    return TITLE;
  } else
  if ( displayName.equals( "Street home" ) ) {
    return STREET_HOME;
  } else
  if ( displayName.equals( "Postal code home" ) ) {
    return POSTAL_CODE_HOME;
  } else
  if ( displayName.equals( "City home" ) ) {
    return CITY_HOME;
  } else
  if ( displayName.equals( "State home" ) ) {
    return STATE_HOME;
  } else
  if ( displayName.equals( "Country home" ) ) {
    return COUNTRY_HOME;
  } else
  if ( displayName.equals( "Martial status" ) ) {
    return MARITAL_STATUS;
  } else
  if ( displayName.equals( "Number of children" ) ) {
    return NUMBER_OF_CHILDREN;
  } else
  if ( displayName.equals( "Profession" ) ) {
    return PROFESSION;
  } else
  if ( displayName.equals( "Nickname" ) ) {
    return NICKNAME;
  } else
  if ( displayName.equals( "Spouse name" ) ) {
    return SPOUSE_NAME;
  } else
  if ( displayName.equals( "Note" ) ) {
    return NOTE;
  } else
  if ( displayName.equals( "Company" ) ) {
    return COMPANY;
  } else
  if ( displayName.equals( "Department" ) ) {
    return DEPARTMENT;
  } else
  if ( displayName.equals( "Position" ) ) {
    return POSITION;
  } else
  if ( displayName.equals( "Employee type" ) ) {
    return EMPLOYEE_TYPE;
  } else
  if ( displayName.equals( "Room number" ) ) {
    return ROOM_NUMBER;
  } else
  if ( displayName.equals( "Street business" ) ) {
    return STREET_BUSINESS;
  } else
  if ( displayName.equals( "Postal code business" ) ) {
    return POSTAL_CODE_BUSINESS;
  } else
  if ( displayName.equals( "City business" ) ) {
    return CITY_BUSINESS;
  } else
  if ( displayName.equals( "State business" ) ) {
    return STATE_BUSINESS;
  } else
  if ( displayName.equals( "Country business" ) ) {
    return COUNTRY_BUSINESS;
  } else
  if ( displayName.equals( "Number of employee" ) ) {
    return NUMBER_OF_EMPLOYEE;
  } else
  if ( displayName.equals( "Sales volume" ) ) {
    return SALES_VOLUME;
  } else
  if ( displayName.equals( "Tax id" ) ) {
    return TAX_ID;
  } else
  if ( displayName.equals( "Commercial register" ) ) {
    return COMMERCIAL_REGISTER;
  } else
  if ( displayName.equals( "Branches" ) ) {
    return BRANCHES;
  } else
  if ( displayName.equals( "Business category" ) ) {
    return BUSINESS_CATEGORY;
  } else
  if ( displayName.equals( "Info" ) ) {
    return INFO;
  } else
  if ( displayName.equals( "Manager's name" ) ) {
    return MANAGER_NAME;
  } else
  if ( displayName.equals( "Assistant's name" ) ) {
    return ASSISTANT_NAME;
  } else
  if ( displayName.equals( "Street other" ) ) {
    return STREET_OTHER;
  } else
  if ( displayName.equals( "Postal code other" ) ) {
    return POSTAL_CODE_OTHER;
  } else
  if ( displayName.equals( "City other" ) ) {
    return CITY_OTHER;
  } else
  if ( displayName.equals( "State other" ) ) {
    return STATE_OTHER;
  } else
  if ( displayName.equals( "Country other" ) ) {
    return COUNTRY_OTHER;
  } else
  if ( displayName.equals( "Telephone assostant" ) ) {
    return TELEPHONE_ASSISTANT;
  } else
  if ( displayName.equals( "Telephone business 1" ) ) {
    return TELEPHONE_BUSINESS1;
  } else
  if ( displayName.equals( "Telephone business 2" ) ) {
    return TELEPHONE_BUSINESS2;
  } else
  if ( displayName.equals( "FAX business" ) ) {
    return FAX_BUSINESS;
  } else
  if ( displayName.equals( "Telephone callback" ) ) {
    return TELEPHONE_CALLBACK;
  } else
  if ( displayName.equals( "Telephone car" ) ) {
    return TELEPHONE_CAR;
  } else
  if ( displayName.equals( "Telephone company" ) ) {
    return TELEPHONE_COMPANY;
  } else
  if ( displayName.equals( "Telephone home 1" ) ) {
    return TELEPHONE_HOME1;
  } else
  if ( displayName.equals( "Telephone home 2" ) ) {
    return TELEPHONE_HOME2;
  } else
  if ( displayName.equals( "FAX home" ) ) {
    return FAX_HOME;
  } else
  if ( displayName.equals( "Telephone ISDN" ) ) {
    return TELEPHONE_ISDN;
  } else
  if ( displayName.equals( "Cellular telephone 1" ) ) {
    return CELLULAR_TELEPHONE1;
  } else
  if ( displayName.equals( "Cellular telephone 2" ) ) {
    return CELLULAR_TELEPHONE2;
  } else
  if ( displayName.equals( "Telephone other" ) ) {
    return TELEPHONE_OTHER;
  } else
  if ( displayName.equals( "FAX other" ) ) {
    return FAX_OTHER;
  } else
  if ( displayName.equals( "Telephone pager" ) ) {
    return TELEPHONE_PAGER;
  } else
  if ( displayName.equals( "Telephone primary" ) ) {
    return TELEPHONE_PRIMARY;
  } else
  if ( displayName.equals( "Telephone radio" ) ) {
    return TELEPHONE_RADIO;
  } else
  if ( displayName.equals( "Telephone telex" ) ) {
    return TELEPHONE_TELEX;
  } else
  if ( displayName.equals( "Telephone TTY/TDD" ) ) {
    return TELEPHONE_TTYTDD;
  } else
  if ( displayName.equals( "Instantmessenger 1" ) ) {
    return INSTANT_MESSENGER1;
  } else
  if ( displayName.equals( "Instantmessenger 2" ) ) {
    return INSTANT_MESSENGER2;
  } else
  if ( displayName.equals( "Telephone IP" ) ) {
    return TELEPHONE_IP;
  } else
  if ( displayName.equals( "Email 1" ) ) {
    return EMAIL1;
  } else
  if ( displayName.equals( "Email 2" ) ) {
    return EMAIL2;
  } else
  if ( displayName.equals( "Email 3" ) ) {
    return EMAIL3;
  } else
  if ( displayName.equals( "URL" ) ) {
    return URL;
  } else
  if ( displayName.equals( "Categories" ) ) {
    return CATEGORIES;
  } else
  if ( displayName.equals( "Dynamic Field 1" ) ) {
    return USERFIELD01;
  } else
  if ( displayName.equals( "Dynamic Field 2" ) ) {
    return USERFIELD02;
  } else
  if ( displayName.equals( "Dynamic Field 3" ) ) {
    return USERFIELD03;
  } else
  if ( displayName.equals( "Dynamic Field 4" ) ) {
    return USERFIELD04;
  } else
  if ( displayName.equals( "Dynamic Field 5" ) ) {
    return USERFIELD05;
  } else
  if ( displayName.equals( "Dynamic Field 6" ) ) {
    return USERFIELD06;
  } else
  if ( displayName.equals( "Dynamic Field 7" ) ) {
    return USERFIELD07;
  } else
  if ( displayName.equals( "Dynamic Field 8" ) ) {
    return USERFIELD08;
  } else
  if ( displayName.equals( "Dynamic Field 9" ) ) {
    return USERFIELD09;
  } else
  if ( displayName.equals( "Dynamic Field 10" ) ) {
    return USERFIELD10;
  } else
  if ( displayName.equals( "Dynamic Field 11" ) ) {
    return USERFIELD11;
  } else
  if ( displayName.equals( "Dynamic Field 12" ) ) {
    return USERFIELD12;
  } else
  if ( displayName.equals( "Dynamic Field 13" ) ) {
    return USERFIELD13;
  } else
  if ( displayName.equals( "Dynamic Field 14" ) ) {
    return USERFIELD14;
  } else
  if ( displayName.equals( "Dynamic Field 15" ) ) {
    return USERFIELD15;
  } else
  if ( displayName.equals( "Dynamic Field 16" ) ) {
    return USERFIELD16;
  } else
  if ( displayName.equals( "Dynamic Field 17" ) ) {
    return USERFIELD17;
  } else
  if ( displayName.equals( "Dynamic Field 18" ) ) {
    return USERFIELD18;
  } else
  if ( displayName.equals( "Dynamic Field 19" ) ) {
    return USERFIELD19;
  } else
  if ( displayName.equals( "Dynamic Field 20" ) ) {
    return USERFIELD20;
  } else
  if ( displayName.equals( "Object id" ) ) {
    return OBJECT_ID;
  } else
  if ( displayName.equals( "Number of distributionlists" ) ) {
    return NUMBER_OF_DISTRIBUTIONLIST;
  } else
  if ( displayName.equals( "Number of links" ) ) {
    return NUMBER_OF_LINKS;
  } else
  if ( displayName.equals( "Folder id" ) ) {
    return FOLDER_ID;
  } else
  if ( displayName.equals( "Context id" ) ) {
    return CONTEXTID;
  } else
  if ( displayName.equals( "Created by" ) ) {
    return CREATED_BY;
  } else
  if ( displayName.equals( "Modified by" ) ) {
    return MODIFIED_BY;
  } else
  if ( displayName.equals( "Creation date" ) ) {
    return CREATION_DATE;
  } else
  if ( displayName.equals( "Changing date" ) ) {
    return LAST_MODIFIED;
  } else
  if ( displayName.equals( "Birthday" ) ) {
    return BIRTHDAY;
  } else
  if ( displayName.equals( "Anniversay" ) ) {
    return ANNIVERSARY;
  } else
  if ( displayName.equals( "Default address" ) ) {
    return DEFAULT_ADDRESS;
  } else
  { return null; }
}

public static ContactField getByValue(final int value){
    if (value == 500) {
      return DISPLAY_NAME;
    } else    if (value == 502) {
      return SUR_NAME;
    } else    if (value == 501) {
      return GIVEN_NAME;
    } else    if (value == 503) {
      return MIDDLE_NAME;
    } else    if (value == 504) {
      return SUFFIX;
    } else    if (value == 505) {
      return TITLE;
    } else    if (value == 506) {
      return STREET_HOME;
    } else    if (value == 507) {
      return POSTAL_CODE_HOME;
    } else    if (value == 508) {
      return CITY_HOME;
    } else    if (value == 509) {
      return STATE_HOME;
    } else    if (value == 510) {
      return COUNTRY_HOME;
    } else    if (value == 512) {
      return MARITAL_STATUS;
    } else    if (value == 513) {
      return NUMBER_OF_CHILDREN;
    } else    if (value == 514) {
      return PROFESSION;
    } else    if (value == 515) {
      return NICKNAME;
    } else    if (value == 516) {
      return SPOUSE_NAME;
    } else    if (value == 518) {
      return NOTE;
    } else    if (value == 569) {
      return COMPANY;
    } else    if (value == 519) {
      return DEPARTMENT;
    } else    if (value == 520) {
      return POSITION;
    } else    if (value == 521) {
      return EMPLOYEE_TYPE;
    } else    if (value == 522) {
      return ROOM_NUMBER;
    } else    if (value == 523) {
      return STREET_BUSINESS;
    } else    if (value == 525) {
      return POSTAL_CODE_BUSINESS;
    } else    if (value == 526) {
      return CITY_BUSINESS;
    } else    if (value == 527) {
      return STATE_BUSINESS;
    } else    if (value == 528) {
      return COUNTRY_BUSINESS;
    } else    if (value == 529) {
      return NUMBER_OF_EMPLOYEE;
    } else    if (value == 530) {
      return SALES_VOLUME;
    } else    if (value == 531) {
      return TAX_ID;
    } else    if (value == 532) {
      return COMMERCIAL_REGISTER;
    } else    if (value == 533) {
      return BRANCHES;
    } else    if (value == 534) {
      return BUSINESS_CATEGORY;
    } else    if (value == 535) {
      return INFO;
    } else    if (value == 536) {
      return MANAGER_NAME;
    } else    if (value == 537) {
      return ASSISTANT_NAME;
    } else    if (value == 538) {
      return STREET_OTHER;
    } else    if (value == 540) {
      return POSTAL_CODE_OTHER;
    } else    if (value == 539) {
      return CITY_OTHER;
    } else    if (value == 598) {
      return STATE_OTHER;
    } else    if (value == 541) {
      return COUNTRY_OTHER;
    } else    if (value == 568) {
      return TELEPHONE_ASSISTANT;
    } else    if (value == 542) {
      return TELEPHONE_BUSINESS1;
    } else    if (value == 543) {
      return TELEPHONE_BUSINESS2;
    } else    if (value == 544) {
      return FAX_BUSINESS;
    } else    if (value == 545) {
      return TELEPHONE_CALLBACK;
    } else    if (value == 546) {
      return TELEPHONE_CAR;
    } else    if (value == 547) {
      return TELEPHONE_COMPANY;
    } else    if (value == 548) {
      return TELEPHONE_HOME1;
    } else    if (value == 549) {
      return TELEPHONE_HOME2;
    } else    if (value == 550) {
      return FAX_HOME;
    } else    if (value == 559) {
      return TELEPHONE_ISDN;
    } else    if (value == 551) {
      return CELLULAR_TELEPHONE1;
    } else    if (value == 552) {
      return CELLULAR_TELEPHONE2;
    } else    if (value == 553) {
      return TELEPHONE_OTHER;
    } else    if (value == 554) {
      return FAX_OTHER;
    } else    if (value == 560) {
      return TELEPHONE_PAGER;
    } else    if (value == 561) {
      return TELEPHONE_PRIMARY;
    } else    if (value == 562) {
      return TELEPHONE_RADIO;
    } else    if (value == 563) {
      return TELEPHONE_TELEX;
    } else    if (value == 564) {
      return TELEPHONE_TTYTDD;
    } else    if (value == 565) {
      return INSTANT_MESSENGER1;
    } else    if (value == 566) {
      return INSTANT_MESSENGER2;
    } else    if (value == 567) {
      return TELEPHONE_IP;
    } else    if (value == 555) {
      return EMAIL1;
    } else    if (value == 556) {
      return EMAIL2;
    } else    if (value == 557) {
      return EMAIL3;
    } else    if (value == 558) {
      return URL;
    } else    if (value == 100) {
      return CATEGORIES;
    } else    if (value == 571) {
      return USERFIELD01;
    } else    if (value == 572) {
      return USERFIELD02;
    } else    if (value == 573) {
      return USERFIELD03;
    } else    if (value == 574) {
      return USERFIELD04;
    } else    if (value == 575) {
      return USERFIELD05;
    } else    if (value == 576) {
      return USERFIELD06;
    } else    if (value == 577) {
      return USERFIELD07;
    } else    if (value == 578) {
      return USERFIELD08;
    } else    if (value == 579) {
      return USERFIELD09;
    } else    if (value == 580) {
      return USERFIELD10;
    } else    if (value == 581) {
      return USERFIELD11;
    } else    if (value == 582) {
      return USERFIELD12;
    } else    if (value == 583) {
      return USERFIELD13;
    } else    if (value == 584) {
      return USERFIELD14;
    } else    if (value == 585) {
      return USERFIELD15;
    } else    if (value == 586) {
      return USERFIELD16;
    } else    if (value == 587) {
      return USERFIELD17;
    } else    if (value == 588) {
      return USERFIELD18;
    } else    if (value == 589) {
      return USERFIELD19;
    } else    if (value == 590) {
      return USERFIELD20;
    } else    if (value == 1) {
      return OBJECT_ID;
    } else    if (value == 594) {
      return NUMBER_OF_DISTRIBUTIONLIST;
    } else    if (value == 103) {
      return NUMBER_OF_LINKS;
    } else    if (value == 592) {
      return DISTRIBUTIONLIST;
    } else    if (value == 591) {
      return LINKS;
    } else    if (value == 20) {
      return FOLDER_ID;
    } else    if (value == 593) {
      return CONTEXTID;
    } else    if (value == 101) {
      return PRIVATE_FLAG;
    } else    if (value == 2) {
      return CREATED_BY;
    } else    if (value == 3) {
      return MODIFIED_BY;
    } else    if (value == 4) {
      return CREATION_DATE;
    } else    if (value == 5) {
      return LAST_MODIFIED;
    } else    if (value == 511) {
      return BIRTHDAY;
    } else    if (value == 517) {
      return ANNIVERSARY;
    } else    if (value == 570) {
      return IMAGE1;
    } else    if (value == 597) {
      return IMAGE_LAST_MODIFIED;
    } else    if (value == 601) {
      return IMAGE1_CONTENT_TYPE;
    } else    if (value == 524) {
      return INTERNAL_USERID;
    } else    if (value == 102) {
      return COLOR_LABEL;
    } else    if (value == 599) {
      return FILE_AS;
    } else    if (value == 605) {
      return DEFAULT_ADDRESS;
    } else    if (value == 602) {
      return MARK_AS_DISTRIBUTIONLIST;
    } else    if (value == 104) {
      return NUMBER_OF_ATTACHMENTS;
    } else    { return null; }
  }


public Object doSwitch(ContactSwitcher switcher, Object... objects) throws ContactException{
    switch(this){
      case DISPLAY_NAME : return switcher.displayname(objects);
      case SUR_NAME : return switcher.surname(objects);
      case GIVEN_NAME : return switcher.givenname(objects);
      case MIDDLE_NAME : return switcher.middlename(objects);
      case SUFFIX : return switcher.suffix(objects);
      case TITLE : return switcher.title(objects);
      case STREET_HOME : return switcher.streethome(objects);
      case POSTAL_CODE_HOME : return switcher.postalcodehome(objects);
      case CITY_HOME : return switcher.cityhome(objects);
      case STATE_HOME : return switcher.statehome(objects);
      case COUNTRY_HOME : return switcher.countryhome(objects);
      case MARITAL_STATUS : return switcher.maritalstatus(objects);
      case NUMBER_OF_CHILDREN : return switcher.numberofchildren(objects);
      case PROFESSION : return switcher.profession(objects);
      case NICKNAME : return switcher.nickname(objects);
      case SPOUSE_NAME : return switcher.spousename(objects);
      case NOTE : return switcher.note(objects);
      case COMPANY : return switcher.company(objects);
      case DEPARTMENT : return switcher.department(objects);
      case POSITION : return switcher.position(objects);
      case EMPLOYEE_TYPE : return switcher.employeetype(objects);
      case ROOM_NUMBER : return switcher.roomnumber(objects);
      case STREET_BUSINESS : return switcher.streetbusiness(objects);
      case POSTAL_CODE_BUSINESS : return switcher.postalcodebusiness(objects);
      case CITY_BUSINESS : return switcher.citybusiness(objects);
      case STATE_BUSINESS : return switcher.statebusiness(objects);
      case COUNTRY_BUSINESS : return switcher.countrybusiness(objects);
      case NUMBER_OF_EMPLOYEE : return switcher.numberofemployee(objects);
      case SALES_VOLUME : return switcher.salesvolume(objects);
      case TAX_ID : return switcher.taxid(objects);
      case COMMERCIAL_REGISTER : return switcher.commercialregister(objects);
      case BRANCHES : return switcher.branches(objects);
      case BUSINESS_CATEGORY : return switcher.businesscategory(objects);
      case INFO : return switcher.info(objects);
      case MANAGER_NAME : return switcher.managername(objects);
      case ASSISTANT_NAME : return switcher.assistantname(objects);
      case STREET_OTHER : return switcher.streetother(objects);
      case POSTAL_CODE_OTHER : return switcher.postalcodeother(objects);
      case CITY_OTHER : return switcher.cityother(objects);
      case STATE_OTHER : return switcher.stateother(objects);
      case COUNTRY_OTHER : return switcher.countryother(objects);
      case TELEPHONE_ASSISTANT : return switcher.telephoneassistant(objects);
      case TELEPHONE_BUSINESS1 : return switcher.telephonebusiness1(objects);
      case TELEPHONE_BUSINESS2 : return switcher.telephonebusiness2(objects);
      case FAX_BUSINESS : return switcher.faxbusiness(objects);
      case TELEPHONE_CALLBACK : return switcher.telephonecallback(objects);
      case TELEPHONE_CAR : return switcher.telephonecar(objects);
      case TELEPHONE_COMPANY : return switcher.telephonecompany(objects);
      case TELEPHONE_HOME1 : return switcher.telephonehome1(objects);
      case TELEPHONE_HOME2 : return switcher.telephonehome2(objects);
      case FAX_HOME : return switcher.faxhome(objects);
      case TELEPHONE_ISDN : return switcher.telephoneisdn(objects);
      case CELLULAR_TELEPHONE1 : return switcher.cellulartelephone1(objects);
      case CELLULAR_TELEPHONE2 : return switcher.cellulartelephone2(objects);
      case TELEPHONE_OTHER : return switcher.telephoneother(objects);
      case FAX_OTHER : return switcher.faxother(objects);
      case TELEPHONE_PAGER : return switcher.telephonepager(objects);
      case TELEPHONE_PRIMARY : return switcher.telephoneprimary(objects);
      case TELEPHONE_RADIO : return switcher.telephoneradio(objects);
      case TELEPHONE_TELEX : return switcher.telephonetelex(objects);
      case TELEPHONE_TTYTDD : return switcher.telephonettyttd(objects);
      case INSTANT_MESSENGER1 : return switcher.instantmessenger1(objects);
      case INSTANT_MESSENGER2 : return switcher.instantmessenger2(objects);
      case TELEPHONE_IP : return switcher.telephoneip(objects);
      case EMAIL1 : return switcher.email1(objects);
      case EMAIL2 : return switcher.email2(objects);
      case EMAIL3 : return switcher.email3(objects);
      case URL : return switcher.url(objects);
      case CATEGORIES : return switcher.categories(objects);
      case USERFIELD01 : return switcher.userfield01(objects);
      case USERFIELD02 : return switcher.userfield02(objects);
      case USERFIELD03 : return switcher.userfield03(objects);
      case USERFIELD04 : return switcher.userfield04(objects);
      case USERFIELD05 : return switcher.userfield05(objects);
      case USERFIELD06 : return switcher.userfield06(objects);
      case USERFIELD07 : return switcher.userfield07(objects);
      case USERFIELD08 : return switcher.userfield08(objects);
      case USERFIELD09 : return switcher.userfield09(objects);
      case USERFIELD10 : return switcher.userfield10(objects);
      case USERFIELD11 : return switcher.userfield11(objects);
      case USERFIELD12 : return switcher.userfield12(objects);
      case USERFIELD13 : return switcher.userfield13(objects);
      case USERFIELD14 : return switcher.userfield14(objects);
      case USERFIELD15 : return switcher.userfield15(objects);
      case USERFIELD16 : return switcher.userfield16(objects);
      case USERFIELD17 : return switcher.userfield17(objects);
      case USERFIELD18 : return switcher.userfield18(objects);
      case USERFIELD19 : return switcher.userfield19(objects);
      case USERFIELD20 : return switcher.userfield20(objects);
      case OBJECT_ID : return switcher.objectid(objects);
      case NUMBER_OF_DISTRIBUTIONLIST : return switcher.numberofdistributionlists(objects);
      case NUMBER_OF_LINKS : return switcher.numberoflinks(objects);
      case DISTRIBUTIONLIST : return switcher.distributionlist(objects);
      case LINKS : return switcher.links(objects);
      case FOLDER_ID : return switcher.parentfolderid(objects);
      case CONTEXTID : return switcher.contextid(objects);
      case PRIVATE_FLAG : return switcher.privateflag(objects);
      case CREATED_BY : return switcher.createdby(objects);
      case MODIFIED_BY : return switcher.modifiedby(objects);
      case CREATION_DATE : return switcher.creationdate(objects);
      case LAST_MODIFIED : return switcher.lastmodified(objects);
      case BIRTHDAY : return switcher.birthday(objects);
      case ANNIVERSARY : return switcher.anniversary(objects);
      case IMAGE_LAST_MODIFIED : return switcher.imagelastmodified(objects);
      case INTERNAL_USERID : return switcher.internaluserid(objects);
      case COLOR_LABEL : return switcher.label(objects);
      case FILE_AS : return switcher.fileas(objects);
      case DEFAULT_ADDRESS : return switcher.defaultaddress(objects);
      case NUMBER_OF_ATTACHMENTS : return switcher.numberofattachments(objects);
      default: return null;
    }
  }

}
