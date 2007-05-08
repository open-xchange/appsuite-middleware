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
 * Note: This class was mostly generated automatically.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public enum ContactField{  
	  DISPLAY_NAME (500 , "field01" , "DISPLAY_NAME" , "Display name" , ""),
	  SUR_NAME (502 , "field02" , "SUR_NAME" , "Sur name" , "Last Name"),
	  GIVEN_NAME (501 , "field03" , "GIVEN_NAME" , "Given name" , "First Name"),
	  MIDDLE_NAME (503 , "field04" , "MIDDLE_NAME" , "Middle name" , "Middle Name"),
	  SUFFIX (504 , "field05" , "SUFFIX" , "Suffix" , "Suffix"),
	  TITLE (505 , "field06" , "TITLE" , "Title" , "Title"),
	  STREET_HOME (506 , "field07" , "STREET_HOME" , "Street home" , "Home Street"),
	  POSTAL_CODE_HOME (507 , "field08" , "POSTAL_CODE_HOME" , "Postal code home" , "Home Postal Code"),
	  CITY_HOME (508 , "field09" , "CITY_HOME" , "City home" , "Home City"),
	  STATE_HOME (509 , "field10" , "STATE_HOME" , "State home" , "Home State"),
	  COUNTRY_HOME (510 , "field11" , "COUNTRY_HOME" , "Country home" , "Home Country"),
	  MARITAL_STATUS (512 , "field12" , "MARITAL_STATUS" , "Martial status" , ""),
	  NUMBER_OF_CHILDREN (513 , "field13" , "NUMBER_OF_CHILDREN" , "Number of children" , "Children"),
	  PROFESSION (514 , "field14" , "PROFESSION" , "Profession" , "Profession"),
	  NICKNAME (515 , "field15" , "NICKNAME" , "Nickname" , ""),
	  SPOUSE_NAME (516 , "field16" , "SPOUSE_NAME" , "Spouse name" , "Spouse"),
	  NOTE (518 , "field17" , "NOTE" , "Note" , "Notes"),
	  COMPANY (569 , "field18" , "COMPANY" , "Company" , "Company"),
	  DEPARTMENT (519 , "field19" , "DEPARTMENT" , "Department" , "Department"),
	  POSITION (520 , "field20" , "POSITION" , "Position" , ""),
	  EMPLOYEE_TYPE (521 , "field21" , "EMPLOYEE_TYPE" , "Employee type" , ""),
	  ROOM_NUMBER (522 , "field22" , "ROOM_NUMBER" , "Room number" , ""),
	  STREET_BUSINESS (523 , "field23" , "STREET_BUSINESS" , "Street business" , "Business Street"),
	  POSTAL_CODE_BUSINESS (525 , "field24" , "POSTAL_CODE_BUSINESS" , "Postal code business" , "Business Postal Code"),
	  CITY_BUSINESS (526 , "field25" , "CITY_BUSINESS" , "City business" , "Business City"),
	  STATE_BUSINESS (527 , "field26" , "STATE_BUSINESS" , "State business" , "Business State"),
	  COUNTRY_BUSINESS (528 , "field27" , "COUNTRY_BUSINESS" , "Country business" , "Business Country"),
	  NUMBER_OF_EMPLOYEE (529 , "field28" , "NUMBER_OF_EMPLOYEE" , "Number of employee" , ""),
	  SALES_VOLUME (530 , "field29" , "SALES_VOLUME" , "Sales volume" , ""),
	  TAX_ID (531 , "field30" , "TAX_ID" , "Tax id" , ""),
	  COMMERCIAL_REGISTER (532 , "field31" , "COMMERCIAL_REGISTER" , "Commercial register" , ""),
	  BRANCHES (533 , "field32" , "BRANCHES" , "Branches" , ""),
	  BUSINESS_CATEGORY (534 , "field33" , "BUSINESS_CATEGORY" , "Business category" , ""),
	  INFO (535 , "field34" , "INFO" , "Info" , ""),
	  MANAGER_NAME (536 , "field35" , "MANAGER_NAME" , "Manager's name" , "Manager's Name"),
	  ASSISTANT_NAME (537 , "field36" , "ASSISTANT_NAME" , "Assistant's name" , "Assistant's Name"),
	  STREET_OTHER (538 , "field37" , "STREET_OTHER" , "Street other" , "Other Street"),
	  POSTAL_CODE_OTHER (540 , "field38" , "POSTAL_CODE_OTHER" , "Postal code other" , "Other Postal Code"),
	  CITY_OTHER (539 , "field39" , "CITY_OTHER" , "City other" , "Other City"),
	  STATE_OTHER (598 , "field40" , "STATE_OTHER" , "State other" , "Other State"),
	  COUNTRY_OTHER (541 , "field41" , "COUNTRY_OTHER" , "Country other" , "Other Country"),
	  TELEPHONE_ASSISTANT (568 , "field42" , "TELEPHONE_ASSISTANT" , "Telephone assostant" , "Assistant's Phone"),
	  TELEPHONE_BUSINESS1 (542 , "field43" , "TELEPHONE_BUSINESS1" , "Telephone business 1" , "Business Phone"),
	  TELEPHONE_BUSINESS2 (543 , "field44" , "TELEPHONE_BUSINESS2" , "Telephone business 2" , "Business Phone 2"),
	  FAX_BUSINESS (544 , "field45" , "FAX_BUSINESS" , "FAX business" , "Business Fax"),
	  TELEPHONE_CALLBACK (545 , "field46" , "TELEPHONE_CALLBACK" , "Telephone callback" , "Callback"),
	  TELEPHONE_CAR (546 , "field47" , "TELEPHONE_CAR" , "Telephone car" , "Car Phone"),
	  TELEPHONE_COMPANY (547 , "field48" , "TELEPHONE_COMPANY" , "Telephone company" , "Company Main Phone"),
	  TELEPHONE_HOME1 (548 , "field49" , "TELEPHONE_HOME1" , "Telephone home 1" , "Home Phone"),
	  TELEPHONE_HOME2 (549 , "field50" , "TELEPHONE_HOME2" , "Telephone home 2" , "Home Phone 2"),
	  FAX_HOME (550 , "field51" , "FAX_HOME" , "FAX home" , "Home Fax"),
	  TELEPHONE_ISDN (559 , "field52" , "TELEPHONE_ISDN" , "Telephone ISDN" , "ISDN"),
	  CELLULAR_TELEPHONE1 (551 , "field53" , "CELLULAR_TELEPHONE1" , "Cellular telephone 1" , "Mobile Phone"),
	  CELLULAR_TELEPHONE2 (552 , "field54" , "CELLULAR_TELEPHONE2" , "Cellular telephone 2" , ""),
	  TELEPHONE_OTHER (553 , "field55" , "TELEPHONE_OTHER" , "Telephone other" , "Other Phone"),
	  FAX_OTHER (554 , "field56" , "FAX_OTHER" , "FAX other" , "Other Fax"),
	  TELEPHONE_PAGER (560 , "field57" , "TELEPHONE_PAGER" , "Telephone pager" , "Pager"),
	  TELEPHONE_PRIMARY (561 , "field58" , "TELEPHONE_PRIMARY" , "Telephone primary" , "Primary Phone"),
	  TELEPHONE_RADIO (562 , "field59" , "TELEPHONE_RADIO" , "Telephone radio" , "Radio Phone"),
	  TELEPHONE_TELEX (563 , "field60" , "TELEPHONE_TELEX" , "Telephone telex" , "Telex"),
	  TELEPHONE_TTYTDD (564 , "field61" , "TELEPHONE_TTYTDD" , "Telephone TTY/TDD" , "TTY/TDD Phone"),
	  INSTANT_MESSENGER1 (565 , "field62" , "INSTANT_MESSENGER1" , "Instantmessenger 1" , ""),
	  INSTANT_MESSENGER2 (566 , "field63" , "INSTANT_MESSENGER2" , "Instantmessenger 2" , ""),
	  TELEPHONE_IP (567 , "field64" , "TELEPHONE_IP" , "Telephone IP" , ""),
	  EMAIL1 (555 , "field65" , "EMAIL1" , "Email 1" , "E-Mail Address"),
	  EMAIL2 (556 , "field66" , "EMAIL2" , "Email 2" , "E-Mail 2 Address"),
	  EMAIL3 (557 , "field67" , "EMAIL3" , "Email 3" , "E-Mail 3 Address"),
	  URL (558 , "field68" , "URL" , "URL" , ""),
	  CATEGORIES (100 , "field69" , "CATEGORIES" , "Categories" , ""),
	  USERFIELD01 (571 , "field70" , "USERFIELD01" , "Dynamic Field 1" , "User 1"),
	  USERFIELD02 (572 , "field71" , "USERFIELD02" , "Dynamic Field 2" , "User 2"),
	  USERFIELD03 (573 , "field72" , "USERFIELD03" , "Dynamic Field 3" , "User 3"),
	  USERFIELD04 (574 , "field73" , "USERFIELD04" , "Dynamic Field 4" , "User 4"),
	  USERFIELD05 (575 , "field74" , "USERFIELD05" , "Dynamic Field 5" , ""),
	  USERFIELD06 (576 , "field75" , "USERFIELD06" , "Dynamic Field 6" , ""),
	  USERFIELD07 (577 , "field76" , "USERFIELD07" , "Dynamic Field 7" , ""),
	  USERFIELD08 (578 , "field77" , "USERFIELD08" , "Dynamic Field 8" , ""),
	  USERFIELD09 (579 , "field78" , "USERFIELD09" , "Dynamic Field 9" , ""),
	  USERFIELD10 (580 , "field79" , "USERFIELD10" , "Dynamic Field 10" , ""),
	  USERFIELD11 (581 , "field80" , "USERFIELD11" , "Dynamic Field 11" , ""),
	  USERFIELD12 (582 , "field81" , "USERFIELD12" , "Dynamic Field 12" , ""),
	  USERFIELD13 (583 , "field82" , "USERFIELD13" , "Dynamic Field 13" , ""),
	  USERFIELD14 (584 , "field83" , "USERFIELD14" , "Dynamic Field 14" , ""),
	  USERFIELD15 (585 , "field84" , "USERFIELD15" , "Dynamic Field 15" , ""),
	  USERFIELD16 (586 , "field85" , "USERFIELD16" , "Dynamic Field 16" , ""),
	  USERFIELD17 (587 , "field86" , "USERFIELD17" , "Dynamic Field 17" , ""),
	  USERFIELD18 (588 , "field87" , "USERFIELD18" , "Dynamic Field 18" , ""),
	  USERFIELD19 (589 , "field88" , "USERFIELD19" , "Dynamic Field 19" , ""),
	  USERFIELD20 (590 , "field89" , "USERFIELD20" , "Dynamic Field 20" , ""),
	  OBJECT_ID (1 , "intfield01" , "OBJECT_ID" , "Object id" , ""),
	  NUMBER_OF_DISTRIBUTIONLIST (594 , "intfield02" , "NUMBER_OF_DISTRIBUTIONLIST" , "Number of distributionlists" , ""),
	  NUMBER_OF_LINKS (103 , "intfield03" , "NUMBER_OF_LINKS" , "Number of links" , ""),
	  DISTRIBUTIONLIST (592 , "intfield02" , "DISTRIBUTIONLIST" , "" , ""),
	  LINKS (591 , "intfield03" , "LINKS" , "" , ""),
	  FOLDER_ID (20 , "fid" , "FOLDER_ID" , "Folder id" , ""),
	  CONTEXTID (593 , "cid" , "CONTEXTID" , "Context id" , ""),
	  PRIVATE_FLAG (101 , "pflag" , "PRIVATE_FLAG" , "" , ""),
	  CREATED_BY (2 , "created_from" , "CREATED_BY" , "Created by" , ""),
	  MODIFIED_BY (3 , "changed_from" , "MODIFIED_BY" , "Modified by" , ""),
	  CREATION_DATE (4 , "creating_date" , "CREATION_DATE" , "Creation date" , ""),
	  LAST_MODIFIED (5 , "changing_date" , "LAST_MODIFIED" , "Changing date" , ""),
	  BIRTHDAY (511 , "timestampfield01" , "BIRTHDAY" , "Birthday" , "Birthday"),
	  ANNIVERSARY (517 , "timestampfield02" , "ANNIVERSARY" , "Anniversay" , "Anniversary"),
	  IMAGE1 (570 , "intfield04" , "IMAGE1" , "" , ""),
	  IMAGE_LAST_MODIFIED (597 , "intfield04" , "IMAGE_LAST_MODIFIED" , "" , ""),
	  IMAGE1_CONTENT_TYPE (601 , "intfield04" , "IMAGE1_CONTENT_TYPE" , "" , ""),
	  INTERNAL_USERID (524 , "userid" , "INTERNAL_USERID" , "" , ""),
	  COLOR_LABEL (102 , "intfield05" , "COLOR_LABEL" , "" , ""),
	  FILE_AS (599 , "field90" , "FILE_AS" , "" , ""),
	  DEFAULT_ADDRESS (605 , "intfield06" , "DEFAULT_ADDRESS" , "Default address" , ""),
	  MARK_AS_DISTRIBUTIONLIST (602 , "intfield07" , "MARK_AS_DISTRIBUTIONLIST" , "" , ""),
	  NUMBER_OF_ATTACHMENTS (104 , "intfield08" , "NUMBER_OF_ATTACHMENTS" , "" , "");


  private int columnNumber;
  private String dbName, readableName, fieldName, outlookENName;
  private String outlookDEName, outlookFRName;

  private ContactField(final int columnNumber, final String fieldName, final String dbName, final String readableName, final String outlookENName){
	this.fieldName = fieldName;
    this.columnNumber = columnNumber;
    this.dbName = dbName ;
    this.readableName = readableName;
    this.outlookENName = outlookENName;
//    this.outlookDEName = outlookFRName;
//    this.outlookFRName = outlookFRName;
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
  
  public String getOutlookENName(){
	  return outlookENName;
  }
  
  public static ContactField getByDBFieldName(final String dbFieldName){
	for(final ContactField field: values()){
		  if(dbFieldName.equals( field.getDBName() )){
			  return field;
		  }
	}
	return null;
  }

public static ContactField getByDisplayName(final String displayName){
  for(final ContactField field : values()){
	if(displayName.equals( field.getReadableName() ) ){
		return field;
	}
  }
  return null;
}

public static ContactField getByValue(final int value){
	for(final ContactField field: values()){
		if(value == field.getNumber()){
			return field;
		}
	}
	return null;
}

public static ContactField getByOutlookName(final String outlook){
	for(final ContactField field: values()){
		if(outlook.equals( field.getOutlookENName() ) ){
			return field;
		}
	}
	return null;
}


public Object doSwitch(final ContactSwitcher switcher, final Object... objects) throws ContactException{
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
