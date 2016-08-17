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

package com.openexchange.groupware.contact.helpers;

import java.sql.Types;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

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
 * The design pattern used has no widely acknowledged name (to the best
 * of my knowledge) but is used in Hibernate under the name Switcher.
 *
 * Note: This class was mostly generated automatically.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public enum ContactField{

    DISPLAY_NAME (500 , "field01" , "DISPLAY_NAME" , "Display name"  , ContactFields.DISPLAY_NAME, Types.VARCHAR),
    SUR_NAME (502 , "field02" , "SUR_NAME" , "Sur name" , ContactFields.LAST_NAME, Types.VARCHAR),
    GIVEN_NAME (501 , "field03" , "GIVEN_NAME" , "Given name" , ContactFields.FIRST_NAME, Types.VARCHAR),
    MIDDLE_NAME (503 , "field04" , "MIDDLE_NAME" , "Middle name" , ContactFields.SECOND_NAME, Types.VARCHAR),
    SUFFIX (504 , "field05" , "SUFFIX" , "Suffix" , ContactFields.SUFFIX, Types.VARCHAR),
    TITLE (505 , "field06" , "TITLE" , "Title" , ContactFields.TITLE, Types.VARCHAR),
    STREET_HOME (506 , "field07" , "STREET_HOME" , "Street home" , ContactFields.STREET_HOME, Types.VARCHAR),
    POSTAL_CODE_HOME (507 , "field08" , "POSTAL_CODE_HOME" , "Postal code home" , ContactFields.POSTAL_CODE_HOME, Types.VARCHAR),
    CITY_HOME (508 , "field09" , "CITY_HOME" , "City home" , ContactFields.CITY_HOME, Types.VARCHAR),
    STATE_HOME (509 , "field10" , "STATE_HOME" , "State home" , ContactFields.STATE_HOME, Types.VARCHAR),
    COUNTRY_HOME (510 , "field11" , "COUNTRY_HOME" , "Country home" , ContactFields.COUNTRY_HOME, Types.VARCHAR),
    MARITAL_STATUS (512 , "field12" , "MARITAL_STATUS" , "Marital status"  , ContactFields.MARITAL_STATUS, Types.VARCHAR),
    NUMBER_OF_CHILDREN (513 , "field13" , "NUMBER_OF_CHILDREN" , "Children" , ContactFields.NUMBER_OF_CHILDREN, Types.VARCHAR),
    PROFESSION (514 , "field14" , "PROFESSION" , "Profession" , ContactFields.PROFESSION, Types.VARCHAR),
    NICKNAME (515 , "field15" , "NICKNAME" , "Nickname"  , ContactFields.NICKNAME, Types.VARCHAR),
    SPOUSE_NAME (516 , "field16" , "SPOUSE_NAME" , "Spouse's name" , ContactFields.SPOUSE_NAME, Types.VARCHAR),
    NOTE (518 , "field17" , "NOTE" , "Note" , ContactFields.NOTE, Types.VARCHAR),
    COMPANY (569 , "field18" , "COMPANY" , "Company" , ContactFields.COMPANY, Types.VARCHAR),
    DEPARTMENT (519 , "field19" , "DEPARTMENT" , "Department" , ContactFields.DEPARTMENT, Types.VARCHAR),
    POSITION (520 , "field20" , "POSITION" , "Position"  , ContactFields.POSITION, Types.VARCHAR),
    EMPLOYEE_TYPE (521 , "field21" , "EMPLOYEE_TYPE" , "Employee type"  , ContactFields.EMPLOYEE_TYPE, Types.VARCHAR),
    ROOM_NUMBER (522 , "field22" , "ROOM_NUMBER" , "Room number"  , ContactFields.ROOM_NUMBER, Types.VARCHAR),
    STREET_BUSINESS (523 , "field23" , "STREET_BUSINESS" , "Street business" , ContactFields.STREET_BUSINESS, Types.VARCHAR),
    POSTAL_CODE_BUSINESS (525 , "field24" , "POSTAL_CODE_BUSINESS" , "Postal code business" , ContactFields.POSTAL_CODE_BUSINESS, Types.VARCHAR),
    CITY_BUSINESS (526 , "field25" , "CITY_BUSINESS" , "City business" , ContactFields.CITY_BUSINESS, Types.VARCHAR),
    STATE_BUSINESS (527 , "field26" , "STATE_BUSINESS" , "State business" , ContactFields.STATE_BUSINESS, Types.VARCHAR),
    COUNTRY_BUSINESS (528 , "field27" , "COUNTRY_BUSINESS" , "Country business" , ContactFields.COUNTRY_BUSINESS, Types.VARCHAR),
    NUMBER_OF_EMPLOYEE (529 , "field28" , "NUMBER_OF_EMPLOYEE" , "Employee ID"  , ContactFields.NUMBER_OF_EMPLOYEE, Types.VARCHAR),
    SALES_VOLUME (530 , "field29" , "SALES_VOLUME" , "Sales volume"  , ContactFields.SALES_VOLUME, Types.VARCHAR),
    TAX_ID (531 , "field30" , "TAX_ID" , "Tax id"  , ContactFields.TAX_ID, Types.VARCHAR),
    COMMERCIAL_REGISTER (532 , "field31" , "COMMERCIAL_REGISTER" , "Commercial register"  , ContactFields.COMMERCIAL_REGISTER, Types.VARCHAR),
    BRANCHES (533 , "field32" , "BRANCHES" , "Branches"  , ContactFields.BRANCHES, Types.VARCHAR),
    BUSINESS_CATEGORY (534 , "field33" , "BUSINESS_CATEGORY" , "Business category"  , ContactFields.BUSINESS_CATEGORY, Types.VARCHAR),
    INFO (535 , "field34" , "INFO" , "Info"  , ContactFields.INFO, Types.VARCHAR),
    MANAGER_NAME (536 , "field35" , "MANAGER_NAME" , "Manager" , ContactFields.MANAGER_NAME, Types.VARCHAR),
    ASSISTANT_NAME (537 , "field36" , "ASSISTANT_NAME" , "Assistant"  , ContactFields.ASSISTANT_NAME, Types.VARCHAR),
    STREET_OTHER (538 , "field37" , "STREET_OTHER" , "Street other" , ContactFields.STREET_OTHER, Types.VARCHAR),
    POSTAL_CODE_OTHER (540 , "field38" , "POSTAL_CODE_OTHER" , "Postal code other" , ContactFields.POSTAL_CODE_OTHER, Types.VARCHAR),
    CITY_OTHER (539 , "field39" , "CITY_OTHER" , "City other" , ContactFields.CITY_OTHER, Types.VARCHAR),
    STATE_OTHER (598 , "field40" , "STATE_OTHER" , "State other" , ContactFields.STATE_OTHER, Types.VARCHAR),
    COUNTRY_OTHER (541 , "field41" , "COUNTRY_OTHER" , "Country other" , ContactFields.COUNTRY_OTHER, Types.VARCHAR),
    TELEPHONE_ASSISTANT (568 , "field42" , "TELEPHONE_ASSISTANT" , "Telephone assistant" , ContactFields.TELEPHONE_ASSISTANT, Types.VARCHAR),
    TELEPHONE_BUSINESS1 (542 , "field43" , "TELEPHONE_BUSINESS1" , "Telephone business 1" , ContactFields.TELEPHONE_BUSINESS1, Types.VARCHAR),
    TELEPHONE_BUSINESS2 (543 , "field44" , "TELEPHONE_BUSINESS2" , "Telephone business 2" , ContactFields.TELEPHONE_BUSINESS2, Types.VARCHAR),
    FAX_BUSINESS (544 , "field45" , "FAX_BUSINESS" , "FAX business" , ContactFields.FAX_BUSINESS, Types.VARCHAR),
    TELEPHONE_CALLBACK (545 , "field46" , "TELEPHONE_CALLBACK" , "Telephone callback" , ContactFields.TELEPHONE_CALLBACK, Types.VARCHAR),
    TELEPHONE_CAR (546 , "field47" , "TELEPHONE_CAR" , "Phone (car)" , ContactFields.TELEPHONE_CAR, Types.VARCHAR),
    TELEPHONE_COMPANY (547 , "field48" , "TELEPHONE_COMPANY" , "Telephone company" , ContactFields.TELEPHONE_COMPANY, Types.VARCHAR),
    TELEPHONE_HOME1 (548 , "field49" , "TELEPHONE_HOME1" , "Telephone home 1" , ContactFields.TELEPHONE_HOME1, Types.VARCHAR),
    TELEPHONE_HOME2 (549 , "field50" , "TELEPHONE_HOME2" , "Telephone home 2" , ContactFields.TELEPHONE_HOME2, Types.VARCHAR),
    FAX_HOME (550 , "field51" , "FAX_HOME" , "FAX home" , ContactFields.FAX_HOME, Types.VARCHAR),
    TELEPHONE_ISDN (559 , "field52" , "TELEPHONE_ISDN" , "Telephone ISDN" , ContactFields.TELEPHONE_ISDN, Types.VARCHAR),
    CELLULAR_TELEPHONE1 (551 , "field53" , "CELLULAR_TELEPHONE1" , "Cellular telephone 1" , ContactFields.CELLULAR_TELEPHONE1, Types.VARCHAR),
    CELLULAR_TELEPHONE2 (552 , "field54" , "CELLULAR_TELEPHONE2" , "Cellular telephone 2"  , ContactFields.CELLULAR_TELEPHONE2, Types.VARCHAR),
    TELEPHONE_OTHER (553 , "field55" , "TELEPHONE_OTHER" , "Telephone other" , ContactFields.TELEPHONE_OTHER, Types.VARCHAR),
    FAX_OTHER (554 , "field56" , "FAX_OTHER" , "FAX other" , ContactFields.FAX_OTHER, Types.VARCHAR),
    TELEPHONE_PAGER (560 , "field57" , "TELEPHONE_PAGER" , "Pager" , ContactFields.TELEPHONE_PAGER, Types.VARCHAR),
    TELEPHONE_PRIMARY (561 , "field58" , "TELEPHONE_PRIMARY" , "Telephone primary" , ContactFields.TELEPHONE_PRIMARY, Types.VARCHAR),
    TELEPHONE_RADIO (562 , "field59" , "TELEPHONE_RADIO" , "Telephone radio" , ContactFields.TELEPHONE_RADIO, Types.VARCHAR),
    TELEPHONE_TELEX (563 , "field60" , "TELEPHONE_TELEX" , "Telex" , ContactFields.TELEPHONE_TELEX, Types.VARCHAR),
    TELEPHONE_TTYTDD (564 , "field61" , "TELEPHONE_TTYTDD" , "TTY/TDD" , ContactFields.TELEPHONE_TTYTDD, Types.VARCHAR),
    INSTANT_MESSENGER1 (565 , "field62" , "INSTANT_MESSENGER1" , "Instantmessenger 1"  , ContactFields.INSTANT_MESSENGER1, Types.VARCHAR),
    INSTANT_MESSENGER2 (566 , "field63" , "INSTANT_MESSENGER2" , "Instantmessenger 2"  , ContactFields.INSTANT_MESSENGER2, Types.VARCHAR),
    TELEPHONE_IP (567 , "field64" , "TELEPHONE_IP" , "IP phone"  , ContactFields.TELEPHONE_IP, Types.VARCHAR),
    EMAIL1 (555 , "field65" , "EMAIL1" , "Email 1" , ContactFields.EMAIL1, Types.VARCHAR),
    EMAIL2 (556 , "field66" , "EMAIL2" , "Email 2" , ContactFields.EMAIL2, Types.VARCHAR),
    EMAIL3 (557 , "field67" , "EMAIL3" , "Email 3" , ContactFields.EMAIL3, Types.VARCHAR),
    URL (558 , "field68" , "URL" , "URL"  , ContactFields.URL, Types.VARCHAR),
    CATEGORIES (100 , "field69" , "CATEGORIES" , "Categories"  , ContactFields.CATEGORIES, Types.VARCHAR),
    USERFIELD01 (571 , "field70" , "USERFIELD01" , "Dynamic Field 1" , ContactFields.USERFIELD01, Types.VARCHAR),
    USERFIELD02 (572 , "field71" , "USERFIELD02" , "Dynamic Field 2" , ContactFields.USERFIELD02, Types.VARCHAR),
    USERFIELD03 (573 , "field72" , "USERFIELD03" , "Dynamic Field 3" , ContactFields.USERFIELD03, Types.VARCHAR),
    USERFIELD04 (574 , "field73" , "USERFIELD04" , "Dynamic Field 4" , ContactFields.USERFIELD04, Types.VARCHAR),
    USERFIELD05 (575 , "field74" , "USERFIELD05" , "Dynamic Field 5"  , ContactFields.USERFIELD05, Types.VARCHAR),
    USERFIELD06 (576 , "field75" , "USERFIELD06" , "Dynamic Field 6"  , ContactFields.USERFIELD06, Types.VARCHAR),
    USERFIELD07 (577 , "field76" , "USERFIELD07" , "Dynamic Field 7"  , ContactFields.USERFIELD07, Types.VARCHAR),
    USERFIELD08 (578 , "field77" , "USERFIELD08" , "Dynamic Field 8"  , ContactFields.USERFIELD08, Types.VARCHAR),
    USERFIELD09 (579 , "field78" , "USERFIELD09" , "Dynamic Field 9"  , ContactFields.USERFIELD09, Types.VARCHAR),
    USERFIELD10 (580 , "field79" , "USERFIELD10" , "Dynamic Field 10"  , ContactFields.USERFIELD10, Types.VARCHAR),
    USERFIELD11 (581 , "field80" , "USERFIELD11" , "Dynamic Field 11"  , ContactFields.USERFIELD11, Types.VARCHAR),
    USERFIELD12 (582 , "field81" , "USERFIELD12" , "Dynamic Field 12"  , ContactFields.USERFIELD12, Types.VARCHAR),
    USERFIELD13 (583 , "field82" , "USERFIELD13" , "Dynamic Field 13"  , ContactFields.USERFIELD13, Types.VARCHAR),
    USERFIELD14 (584 , "field83" , "USERFIELD14" , "Dynamic Field 14"  , ContactFields.USERFIELD14, Types.VARCHAR),
    USERFIELD15 (585 , "field84" , "USERFIELD15" , "Dynamic Field 15"  , ContactFields.USERFIELD15, Types.VARCHAR),
    USERFIELD16 (586 , "field85" , "USERFIELD16" , "Dynamic Field 16"  , ContactFields.USERFIELD16, Types.VARCHAR),
    USERFIELD17 (587 , "field86" , "USERFIELD17" , "Dynamic Field 17"  , ContactFields.USERFIELD17, Types.VARCHAR),
    USERFIELD18 (588 , "field87" , "USERFIELD18" , "Dynamic Field 18"  , ContactFields.USERFIELD18, Types.VARCHAR),
    USERFIELD19 (589 , "field88" , "USERFIELD19" , "Dynamic Field 19"  , ContactFields.USERFIELD19, Types.VARCHAR),
    USERFIELD20 (590 , "field89" , "USERFIELD20" , "Dynamic Field 20"  , ContactFields.USERFIELD20, Types.VARCHAR),
    OBJECT_ID (1 , "intfield01" , "OBJECT_ID" , "Object id"  , DataFields.ID, Types.INTEGER),
    NUMBER_OF_DISTRIBUTIONLIST (594 , "intfield02" , "NUMBER_OF_DISTRIBUTIONLIST" , "Number of distributionlists"  , ContactFields.NUMBER_OF_DISTRIBUTIONLIST, Types.INTEGER),
    DISTRIBUTIONLIST (592 , "" , "DISTRIBUTIONLIST" , ""  , ContactFields.DISTRIBUTIONLIST, 0),
    FOLDER_ID (20 , "fid" , "FOLDER_ID" , "Folder id"  , ContactFields.FOLDER_ID, Types.INTEGER),
    CONTEXTID (593 , "cid" , "CONTEXTID" , "Context id"  , "", Types.INTEGER),
    PRIVATE_FLAG (101 , "pflag" , "PRIVATE_FLAG" , "private"  , ContactFields.PRIVATE_FLAG, Types.INTEGER),
    CREATED_BY (2 , "created_from" , "CREATED_BY" , "Created by"  , ContactFields.CREATED_BY, Types.INTEGER),
    MODIFIED_BY (3 , "changed_from" , "MODIFIED_BY" , "Modified by"  , ContactFields.MODIFIED_BY, Types.INTEGER),
    CREATION_DATE (4 , "creating_date" , "CREATION_DATE" , "Creation date"  , ContactFields.CREATION_DATE, Types.BIGINT),
    LAST_MODIFIED (5 , "changing_date" , "LAST_MODIFIED" , "Changing date"  , ContactFields.LAST_MODIFIED, Types.BIGINT),
    BIRTHDAY (511 , "timestampfield01" , "BIRTHDAY" , "Birthday" , ContactFields.BIRTHDAY, Types.DATE),
    ANNIVERSARY (517 , "timestampfield02" , "ANNIVERSARY" , "Anniversary" , ContactFields.ANNIVERSARY, Types.DATE),
    IMAGE1 (570 , "image1" , "IMAGE1" , ""  , ContactFields.IMAGE1, Types.VARBINARY),
    IMAGE_LAST_MODIFIED (597 , "changing_date" , "IMAGE_LAST_MODIFIED" , ""  ,"image_last_modified", Types.DATE),
    IMAGE1_CONTENT_TYPE (601 , "mime_type" , "IMAGE1_CONTENT_TYPE" , ""  , "image1_content_type", Types.VARCHAR),
    INTERNAL_USERID (524 , "userid" , "INTERNAL_USERID" , ""  , ContactFields.USER_ID, Types.INTEGER),
    COLOR_LABEL (102 , "intfield05" , "COLOR_LABEL" , ""  , CommonFields.COLORLABEL, Types.INTEGER),
    FILE_AS (599 , "field90" , "FILE_AS" , ""  , ContactFields.FILE_AS, Types.VARCHAR),
    DEFAULT_ADDRESS (605 , "intfield06" , "DEFAULT_ADDRESS" , "Default address"  , ContactFields.DEFAULT_ADDRESS, Types.INTEGER),
    MARK_AS_DISTRIBUTIONLIST (602 , "intfield07" , "MARK_AS_DISTRIBUTIONLIST" , ""  , ContactFields.MARK_AS_DISTRIBUTIONLIST, Types.INTEGER),
    NUMBER_OF_ATTACHMENTS (104 , "intfield08" , "NUMBER_OF_ATTACHMENTS" , ""  , ContactFields.NUMBER_OF_ATTACHMENTS, Types.INTEGER),
    NUMBER_OF_IMAGES(596, "intfield04", "NUMBER_OF_IMAGES", "number_of_images", ContactFields.NUMBER_OF_IMAGES, Types.INTEGER),
    LAST_MODIFIED_OF_NEWEST_ATTACHMENT(105, "", "LAST_MODIFIED_OF_NEWEST_ATTACHMENT", "lastModifiedOfNewestAttachment", ContactFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC, 0),
    USE_COUNT(608, "value", "USE_COUNT", "useCount", ContactFields.USE_COUNT, Types.INTEGER),
    IMAGE1_URL(606, "", "IMAGE1_URL", "image1_url", ContactFields.IMAGE1_URL, 0),
    LAST_MODIFIED_UTC(6, "", "LAST_MODIFIED_UTC", "last_modified_utc", ContactFields.LAST_MODIFIED_UTC, 0),
    YOMI_FIRST_NAME(Contact.YOMI_FIRST_NAME, "yomiFirstName", "YOMI_FIRST_NAME", "yomiFirstName", ContactFields.YOMI_FIRST_NAME, Types.VARCHAR),
    YOMI_LAST_NAME(Contact.YOMI_LAST_NAME, "yomiLastName", "YOMI_LAST_NAME", "yomiLastName", ContactFields.YOMI_LAST_NAME, Types.VARCHAR),
    YOMI_COMPANY(Contact.YOMI_COMPANY, "yomiCompany", "YOMI_COMPANY", "yomiCompany", ContactFields.YOMI_COMPANY, Types.VARCHAR),
    HOME_ADDRESS(Contact.ADDRESS_HOME, "homeAddress", "ADDRESS_HOME", "homeAddress", ContactFields.ADDRESS_HOME, Types.VARCHAR),
    BUSINESS_ADDRESS(Contact.ADDRESS_BUSINESS, "businessAddress", "BUSINESS_ADDRESS", "businessAddress", ContactFields.ADDRESS_BUSINESS, Types.VARCHAR),
    OTHER_ADDRESS(Contact.ADDRESS_OTHER, "otherAddress", "OTHER_ADDRESS", "otherAddress", ContactFields.ADDRESS_OTHER, Types.VARCHAR),
    UID(Contact.UID, "uid", "UID", "uid", ContactFields.UID, Types.VARCHAR),
    FILENAME(Contact.FILENAME, "filename", "FILENAME", "filename", "", Types.VARCHAR),
    SORT_NAME(Contact.SPECIAL_SORTING, "", "SORT_NAME", "sort_name", ContactFields.SORT_NAME, 0),
    VCARD_ID(Contact.VCARD_ID, "vCardId", "VCARD_ID", "vCardId", "", Types.VARCHAR),
    ;

    private int columnNumber, sqlType;
    private String fieldName, readableName, dbName, ajaxName;

    private ContactField(final int columnNumber, final String dbName, final String fieldName, final String readableName, final String ajaxString, final int sqlType){
        this.dbName = dbName;
        this.columnNumber = columnNumber;
        this.fieldName = fieldName ;
        this.readableName = readableName;
        this.ajaxName = ajaxString;
        this.sqlType = sqlType;
    }

    @Deprecated
    public int getNumber(){
        return columnNumber;
    }

    /**
     * Gets the field name
     *
     * @return the field name
     */
    @Deprecated
    public String getFieldName(){
        return fieldName;
    }

    @Deprecated
    public String getReadableName(){
        return readableName;
    }

    /**
     * Gets the name of the corresponding database columns
     *
     * @return the database name, or <code>""</code> if there's no database column associated with this field
     */
    @Deprecated
    public String getDbName(){
        return dbName;
    }

    @Deprecated
    public String getAjaxName(){
        return ajaxName;
    }

    @Deprecated
    public String getVCardElementName(){
        return readableName; //TODO get real VCard element name
    }

    @Deprecated
    public int getSQLType() {
        return sqlType;
    }

    @Deprecated
    public static ContactField getByDBFieldName(final String dbFieldName){
        if( null == dbFieldName) {
            return null;
        }
        if( "".equals(dbFieldName)) {
            return null;
        }

        for(final ContactField field: values()){
            if(dbFieldName.equals( field.getFieldName() )){
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public static ContactField getByDisplayName(final String displayName){
        if( null == displayName) {
            return null;
        }
        if( "".equals(displayName)) {
            return null;
        }

        for(final ContactField field : values()){
            if(displayName.equals( field.getReadableName() ) ){
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public static ContactField getByFieldName(final String fieldName){
           if( null == fieldName) {
            return null;
        }
            if( "".equals(fieldName)) {
                return null;
            }

        for(final ContactField field : values()){
            if(fieldName.equals( field.getDbName() ) ){
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public static ContactField getByValue(final int value){
        for(final ContactField field: values()){
            if(value == field.getNumber()){
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public static ContactField getByAjaxName(final String value){
        for(final ContactField field: values()){
            if(value.equals(field.getAjaxName())){
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public static ContactField getBySimilarity(String value){ //I call this the "d7-compatibility mode"
        String needle = value.replaceAll("[_\\. ]", "").toLowerCase();
        for(final ContactField field: values()){
            List<String> haystack = Arrays.asList(new String[]{
                field.getAjaxName().replaceAll("[_\\. ]", "").toLowerCase(),
                field.getReadableName().replaceAll("[_\\. ]", "").toLowerCase(),
                field.getFieldName().replaceAll("[_\\. ]", "").toLowerCase()
            });
            if(haystack.contains(needle)) {
                return field;
            }
        }
        return null;
    }

    @Deprecated
    public Object doSwitch(final ContactSwitcher switcher, final Object... objects) throws OXException {
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
        case DISTRIBUTIONLIST : return switcher.distributionlist(objects);
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
        case NUMBER_OF_IMAGES: return switcher.numberofimages(objects);
        case LAST_MODIFIED_OF_NEWEST_ATTACHMENT: return switcher.lastmodifiedofnewestattachment(objects);
        case MARK_AS_DISTRIBUTIONLIST: return switcher.markasdistributionlist(objects);
        case YOMI_FIRST_NAME: return switcher.yomifirstname(objects);
        case YOMI_LAST_NAME: return switcher.yomilastname(objects);
        case YOMI_COMPANY: return switcher.yomicompanyname(objects);
        case IMAGE1_CONTENT_TYPE: return switcher.image1contenttype(objects);
        case HOME_ADDRESS: return switcher.homeaddress(objects);
        case BUSINESS_ADDRESS: return switcher.businessaddress(objects);
        case OTHER_ADDRESS: return switcher.otheraddress(objects);
        case UID: return switcher.uid(objects);
        case IMAGE1: return switcher.image1(objects);
        default: return null;
        }
    }

    private static final EnumSet<ContactField> VIRTUAL_FIELDS = EnumSet.of(IMAGE1_URL, SORT_NAME);
    @Deprecated
    public boolean isVirtual() {
        return VIRTUAL_FIELDS.contains(this);
    }

    private static final EnumSet<ContactField> NON_DB_FIELDS = EnumSet.of(IMAGE1_URL, IMAGE1_CONTENT_TYPE, IMAGE_LAST_MODIFIED, IMAGE1, DISTRIBUTIONLIST, SORT_NAME);
    @Deprecated
    public boolean isDBField() {
        return !NON_DB_FIELDS.contains(this);
    }

}
