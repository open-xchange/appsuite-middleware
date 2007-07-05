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

package com.openexchange.groupware.contact.mappers;

import java.util.Collection;
import java.util.HashMap;

import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * This class maps names of fields found Outlook's CSV files to names used by OX
 * and vice versa. This class has been generated automatically from i18n files.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class EnglishOutlookMapper implements ContactFieldMapper {

    protected final HashMap<String,ContactField> english2field = new HashMap<String, ContactField>();
    protected final HashMap<ContactField, String> field2english = new HashMap<ContactField, String>();

    public EnglishOutlookMapper(){
    english2field.put("Title" , ContactField.TITLE);
    field2english.put(ContactField.TITLE , "Title");

    english2field.put("First Name" , ContactField.GIVEN_NAME);
    field2english.put(ContactField.GIVEN_NAME , "First Name");

    english2field.put("Middle Name" , ContactField.MIDDLE_NAME);
    field2english.put(ContactField.MIDDLE_NAME , "Middle Name");

    english2field.put("Last Name" , ContactField.SUR_NAME);
    field2english.put(ContactField.SUR_NAME , "Last Name");

    english2field.put("Suffix" , ContactField.SUFFIX);
    field2english.put(ContactField.SUFFIX , "Suffix");

    english2field.put("Company" , ContactField.COMPANY);
    field2english.put(ContactField.COMPANY , "Company");

    english2field.put("Department" , ContactField.DEPARTMENT);
    field2english.put(ContactField.DEPARTMENT , "Department");

    english2field.put("Title" , ContactField.TITLE); //company???
    field2english.put(ContactField.TITLE , "Title"); //company???

    english2field.put("Business Street" , ContactField.STREET_BUSINESS);
    field2english.put(ContactField.STREET_BUSINESS , "Business Street");

    //french2field.put("Business Street 2" , ContactField.);
    //field2french.put(ContactField. , "Business Street 2");

    //french2field.put("Business Street 3" , ContactField.);
    //field2french.put(ContactField. , "Business Street 3");

    english2field.put("Business City" , ContactField.CITY_BUSINESS);
    field2english.put(ContactField.CITY_BUSINESS , "Business City");

    english2field.put("Business State" , ContactField.STATE_BUSINESS);
    field2english.put(ContactField.STATE_BUSINESS , "Business State");

    english2field.put("Business Postal Code" , ContactField.POSTAL_CODE_BUSINESS);
    field2english.put(ContactField.POSTAL_CODE_BUSINESS , "Business Postal Code");

    english2field.put("Business Country" , ContactField.COUNTRY_BUSINESS);
    field2english.put(ContactField.COUNTRY_BUSINESS , "Business Country");

    english2field.put("Home Street" , ContactField.STREET_HOME);
    field2english.put(ContactField.STREET_HOME , "Home Street");

    //french2field.put("Home Street 2" , ContactField.);
    //field2french.put(ContactField. , "Home Street 2");

    //french2field.put("Home Street 3" , ContactField.);
    //field2french.put(ContactField. , "Home Street 3");

    english2field.put("Home City" , ContactField.CITY_HOME);
    field2english.put(ContactField.CITY_HOME , "Home City");

    english2field.put("Home State" , ContactField.STATE_HOME);
    field2english.put(ContactField.STATE_HOME , "Home State");

    english2field.put("Home Postal Code" , ContactField.POSTAL_CODE_HOME);
    field2english.put(ContactField.POSTAL_CODE_HOME , "Home Postal Code");

    english2field.put("Home Country" , ContactField.COUNTRY_HOME);
    field2english.put(ContactField.COUNTRY_HOME , "Home Country");

    english2field.put("Other Street" , ContactField.STREET_OTHER);
    field2english.put(ContactField.STREET_OTHER , "Other Street");

    //french2field.put("Other Street 2" , ContactField.);
    //field2french.put(ContactField. , "Other Street 2");

    //french2field.put("Other Street 3" , ContactField.);
    //field2french.put(ContactField. , "Other Street 3");

    english2field.put("Other City" , ContactField.CITY_OTHER);
    field2english.put(ContactField.CITY_OTHER , "Other City");

    english2field.put("Other State" , ContactField.STATE_OTHER);
    field2english.put(ContactField.STATE_OTHER , "Other State");

    english2field.put("Other Postal Code" , ContactField.POSTAL_CODE_OTHER);
    field2english.put(ContactField.POSTAL_CODE_OTHER , "Other Postal Code");

    english2field.put("Other Country" , ContactField.COUNTRY_OTHER);
    field2english.put(ContactField.COUNTRY_OTHER , "Other Country");

    english2field.put("Assistant's Phone" , ContactField.TELEPHONE_ASSISTANT);
    field2english.put(ContactField.TELEPHONE_ASSISTANT , "Assistant's Phone");

    english2field.put("Business Fax" , ContactField.FAX_BUSINESS);
    field2english.put(ContactField.FAX_BUSINESS , "Business Fax");

    english2field.put("Business Phone" , ContactField.TELEPHONE_BUSINESS1);
    field2english.put(ContactField.TELEPHONE_BUSINESS1 , "Business Phone");

    english2field.put("Business Phone 2" , ContactField.TELEPHONE_BUSINESS2);
    field2english.put(ContactField.TELEPHONE_BUSINESS2 , "Business Phone 2");

    english2field.put("Callback" , ContactField.TELEPHONE_CALLBACK);
    field2english.put(ContactField.TELEPHONE_CALLBACK , "Callback");

    english2field.put("Car Phone" , ContactField.TELEPHONE_CAR);
    field2english.put(ContactField.TELEPHONE_CAR , "Car Phone");

    english2field.put("Company Main Phone" , ContactField.TELEPHONE_COMPANY);
    field2english.put(ContactField.TELEPHONE_COMPANY , "Company Main Phone");

    english2field.put("Home Fax" , ContactField.FAX_HOME);
    field2english.put(ContactField.FAX_HOME , "Home Fax");

    english2field.put("Home Phone" , ContactField.TELEPHONE_HOME1);
    field2english.put(ContactField.TELEPHONE_HOME1 , "Home Phone");

    english2field.put("Home Phone 2" , ContactField.TELEPHONE_HOME2);
    field2english.put(ContactField.TELEPHONE_HOME2 , "Home Phone 2");

    english2field.put("ISDN" , ContactField.TELEPHONE_ISDN);
    field2english.put(ContactField.TELEPHONE_ISDN , "ISDN");

    english2field.put("Mobile Phone" , ContactField.CELLULAR_TELEPHONE1); 
    field2english.put(ContactField.CELLULAR_TELEPHONE1 , "Mobile Phone"); 

    english2field.put("Other Fax" , ContactField.FAX_OTHER);
    field2english.put(ContactField.FAX_OTHER , "Other Fax");

    english2field.put("Other Phone" , ContactField.TELEPHONE_OTHER);
    field2english.put(ContactField.TELEPHONE_OTHER , "Other Phone");

    english2field.put("Pager" , ContactField.TELEPHONE_PAGER);
    field2english.put(ContactField.TELEPHONE_PAGER , "Pager");

    english2field.put("Primary Phone" , ContactField.TELEPHONE_PRIMARY);
    field2english.put(ContactField.TELEPHONE_PRIMARY , "Primary Phone");

    english2field.put("Radio Phone" , ContactField.TELEPHONE_RADIO);
    field2english.put(ContactField.TELEPHONE_RADIO , "Radio Phone");

    english2field.put("TTY/TDD Phone" , ContactField.TELEPHONE_TTYTDD);
    field2english.put(ContactField.TELEPHONE_TTYTDD , "TTY/TDD Phone");

    english2field.put("Telex" , ContactField.TELEPHONE_TELEX);
    field2english.put(ContactField.TELEPHONE_TELEX , "Telex");

    english2field.put("Account" , ContactField.EMAIL1);
    field2english.put(ContactField.EMAIL1 , "Account");

    //french2field.put("E-mail Type" , ContactField.);
    //field2french.put(ContactField. , "E-mail Type");

    //french2field.put("E-mail Display Name" , ContactField.);
    //field2french.put(ContactField. , "E-mail Display Name");

    english2field.put("E-mail 2 Address" , ContactField.EMAIL2);
    field2english.put(ContactField.EMAIL2 , "E-mail 2 Address");

    //french2field.put("E-mail 2 Type" , ContactField.);
    //field2french.put(ContactField. , "E-mail 2 Type");

    //french2field.put("E-mail 2 Display Name" , ContactField.);
    //field2french.put(ContactField. , "E-mail 2 Display Name");

    english2field.put("E-mail 3 Address" , ContactField.EMAIL3);
    field2english.put(ContactField.EMAIL3 , "E-mail 3 Address");

    //french2field.put("E-mail 3 Type" , ContactField.);
    //field2french.put(ContactField. , "E-mail 3 Type");

    //french2field.put("Nom complet de l'adresse de messagerie 3" , ContactField.);
    //field2french.put(ContactField. , "Nom complet de l'adresse de messagerie 3");

    english2field.put("Birthday" , ContactField.BIRTHDAY);
    field2english.put(ContactField.BIRTHDAY , "Birthday");

    english2field.put("Anniversary" , ContactField.ANNIVERSARY);
    field2english.put(ContactField.ANNIVERSARY , "Anniversary");

    //french2field.put("Other Address PO Box" , ContactField.);
    //field2french.put(ContactField. , "Other Address PO Box");

    //french2field.put("Business Address PO Box" , ContactField.);
    //field2french.put(ContactField. , "Business Address PO Box");

    //french2field.put("Home Address PO Box" , ContactField.);
    //field2french.put(ContactField. , "Home Address PO Box");

    //french2field.put("Office Location" , ContactField.);
    //field2french.put(ContactField. , "Office Location");

    english2field.put("Categories" , ContactField.CATEGORIES);
    field2english.put(ContactField.CATEGORIES , "Categories");

    //french2field.put("Government ID Number" , ContactField.);
    //field2french.put(ContactField. , "Government ID Number");

    //french2field.put("Account" , ContactField.); //=account
    //field2french.put(ContactField. , "Account"); //=account

    english2field.put("Spouse " , ContactField.SPOUSE_NAME);
    field2english.put(ContactField.SPOUSE_NAME , "Spouse ");

    english2field.put("Sensitivity" , ContactField.PRIVATE_FLAG); //=sensitivity
    field2english.put(ContactField.PRIVATE_FLAG , "Sensitivity"); //=sensitivity

    //french2field.put("Internet Free Busy" , ContactField.); //=internet free/busy
    //field2french.put(ContactField. , "Internet Free Busy"); //=internet free/busy

    //french2field.put("Location" , ContactField.); //= location
    //field2french.put(ContactField. , "Location"); //= location

    english2field.put("Children" , ContactField.NUMBER_OF_CHILDREN); //guessed
    field2english.put(ContactField.NUMBER_OF_CHILDREN , "Children"); //guessed

    //french2field.put("Billing Information" , ContactField.); // = billing information
    //field2french.put(ContactField. , "Billing Information"); // = billing information

    //french2field.put("Initials" , ContactField.); // = initials
    //field2french.put(ContactField. , "Initials"); // = initials

    //french2field.put("Mileage" , ContactField.); //= mileage
    //field2french.put(ContactField. , "Mileage"); //= mileage

    //french2field.put("Language " , ContactField.); // = language
    //field2french.put(ContactField. , "Language "); // = language

    //french2field.put("Keywords" , ContactField.); // = keywords
    //field2french.put(ContactField. , "Keywords"); // = keywords

    english2field.put("Assistant's Name" , ContactField.ASSISTANT_NAME);
    field2english.put(ContactField.ASSISTANT_NAME , "Assistant's Name");

    english2field.put("Notes" , ContactField.NOTE);
    field2english.put(ContactField.NOTE , "Notes");

    english2field.put("Organizational ID Number " , ContactField.COMMERCIAL_REGISTER); //guessed
    field2english.put(ContactField.COMMERCIAL_REGISTER , "Organizational ID Number "); //guessed

    english2field.put("Web Page" , ContactField.URL); //guessed
    field2english.put(ContactField.URL , "Web Page"); //guessed

    //french2field.put("Hobby" , ContactField.); //= hobby
    //field2french.put(ContactField. , "Hobby"); //= hobby

    //french2field.put("Priority" , ContactField.); //= priority
    //field2french.put(ContactField. , "Priority"); //= priority

    //french2field.put("Private" , ContactField.); // = private
    //field2french.put(ContactField. , "Private"); // = private

    english2field.put("Profession" , ContactField.PROFESSION);
    field2english.put(ContactField.PROFESSION , "Profession");

    //french2field.put("Referred By" , ContactField.); // = referred by
    //field2french.put(ContactField. , "Referred By"); // = referred by

    english2field.put("Manager's Name" , ContactField.MANAGER_NAME); //guessed (by Antje)
    field2english.put(ContactField.MANAGER_NAME , "Manager's Name"); //guessed (by Antje)

    //french2field.put("Directory Server" , ContactField.); // = directory server
    //field2french.put(ContactField. , "Directory Server"); // = directory server

    //french2field.put("Gender" , ContactField.); // = gender
    //field2french.put(ContactField. , "Gender"); // = gender

    //french2field.put("User 1" , ContactField.);
    //field2french.put(ContactField. , "User 1");

    //french2field.put("User 2" , ContactField.);
    //field2french.put(ContactField. , "User 2");

    //french2field.put("User 3" , ContactField.);
    //field2french.put(ContactField. , "User 3");

    //french2field.put("Utilisateur 4" , ContactField.);
    //field2french.put(ContactField. , "Utilisateur 4");


    }

    public ContactField getFieldByName(String name){
            return english2field.get(name);
    }

    public String getNameOfField(ContactField field){
    	return field2english.get(field);
    }

    public Collection<String> getNamesOfFields(){
        return field2english.values();
    }
    public Collection<ContactField> getSupportedFields(){
    	return english2field.values(); 
    }
}
