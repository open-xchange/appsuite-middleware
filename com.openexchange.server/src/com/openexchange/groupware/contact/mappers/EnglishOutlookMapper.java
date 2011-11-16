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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * This class maps names of fields found Outlook's CSV files to names used by OX and vice versa. This class has been generated automatically
 * from i18n files.
 * 
 * @deprecated Use the PropertyDrivenMapper with .properties files instead.
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */

public class EnglishOutlookMapper extends AbstractContactFieldMapper {

    public EnglishOutlookMapper() {
        store(ContactField.TITLE, "Title");
        store(ContactField.GIVEN_NAME, "First Name");
        store(ContactField.MIDDLE_NAME, "Middle Name");
        store(ContactField.SUR_NAME, "Last Name");
        store(ContactField.SUFFIX, "Suffix");
        store(ContactField.COMPANY, "Company");
        store(ContactField.DEPARTMENT, "Department");
        store(ContactField.TITLE, "Title"); // company???
        store(ContactField.STREET_BUSINESS, "Business Street");
        // store(ContactField. , "Business Street 2");
        // store(ContactField. , "Business Street 3");
        store(ContactField.CITY_BUSINESS, "Business City");
        store(ContactField.STATE_BUSINESS, "Business State");
        store(ContactField.POSTAL_CODE_BUSINESS, "Business Postal Code");
        store(ContactField.COUNTRY_BUSINESS, "Business Country");
        store(ContactField.STREET_HOME, "Home Street");
        // store(ContactField. , "Home Street 2");
        // store(ContactField. , "Home Street 3");
        store(ContactField.CITY_HOME, "Home City");
        store(ContactField.STATE_HOME, "Home State");
        store(ContactField.POSTAL_CODE_HOME, "Home Postal Code");
        store(ContactField.COUNTRY_HOME, "Home Country");
        store(ContactField.STREET_OTHER, "Other Street");
        // store(ContactField. , "Other Street 2");
        // store(ContactField. , "Other Street 3");
        store(ContactField.CITY_OTHER, "Other City");
        store(ContactField.STATE_OTHER, "Other State");
        store(ContactField.POSTAL_CODE_OTHER, "Other Postal Code");
        store(ContactField.COUNTRY_OTHER, "Other Country");
        store(ContactField.TELEPHONE_ASSISTANT, "Assistant's Phone");
        store(ContactField.FAX_BUSINESS, "Business Fax");
        store(ContactField.TELEPHONE_BUSINESS1, "Business Phone");
        store(ContactField.TELEPHONE_BUSINESS2, "Business Phone 2");
        store(ContactField.TELEPHONE_CALLBACK, "Callback");
        store(ContactField.TELEPHONE_CAR, "Car Phone");
        store(ContactField.TELEPHONE_COMPANY, "Company Main Phone");
        store(ContactField.FAX_HOME, "Home Fax");
        store(ContactField.TELEPHONE_HOME1, "Home Phone");
        store(ContactField.TELEPHONE_HOME2, "Home Phone 2");
        store(ContactField.TELEPHONE_ISDN, "ISDN");
        store(ContactField.CELLULAR_TELEPHONE1, "Mobile Phone");
        store(ContactField.FAX_OTHER, "Other Fax");
        store(ContactField.TELEPHONE_OTHER, "Other Phone");
        store(ContactField.TELEPHONE_PAGER, "Pager");
        store(ContactField.TELEPHONE_PRIMARY, "Primary Phone");
        store(ContactField.TELEPHONE_RADIO, "Radio Phone");
        store(ContactField.TELEPHONE_TTYTDD, "TTY/TDD Phone");
        store(ContactField.TELEPHONE_TELEX, "Telex");
        store(ContactField.EMAIL1, "E-mail Address");
        // store(ContactField. , "E-mail Type");
        // store(ContactField. , "E-mail Display Name");
        store(ContactField.EMAIL2, "E-mail 2 Address");
        // store(ContactField. , "E-mail 2 Type");
        // store(ContactField. , "E-mail 2 Display Name");
        store(ContactField.EMAIL3, "E-mail 3 Address");
        // store(ContactField. , "E-mail 3 Type");
        // store(ContactField. , "Nom complet de l'adresse de messagerie 3");
        store(ContactField.BIRTHDAY, "Birthday");
        store(ContactField.ANNIVERSARY, "Anniversary");
        // store(ContactField. , "Other Address PO Box");
        // store(ContactField. , "Business Address PO Box");
        // store(ContactField. , "Home Address PO Box");
        // store(ContactField. , "Office Location");
        store(ContactField.CATEGORIES, "Categories");
        // store(ContactField. , "Government ID Number");
        // store(ContactField. , "Account"); //=account
        store(ContactField.SPOUSE_NAME, "Spouse");
        store(ContactField.PRIVATE_FLAG, "Sensitivity"); // =sensitivity
        // store(ContactField. , "Internet Free Busy"); //=internet free/busy
        // store(ContactField. , "Location"); //= location
        store(ContactField.NUMBER_OF_CHILDREN, "Children"); // guessed
        // store(ContactField. , "Billing Information"); // = billing information
        // store(ContactField. , "Initials"); // = initials
        // store(ContactField. , "Mileage"); //= mileage
        // store(ContactField. , "Language "); // = language
        // store(ContactField. , "Keywords"); // = keywords
        store(ContactField.ASSISTANT_NAME, "Assistant's Name");
        store(ContactField.NOTE, "Notes");
        store(ContactField.COMMERCIAL_REGISTER, "Organizational ID Number "); // guessed
        store(ContactField.URL, "Web Page"); // guessed
        // store(ContactField. , "Hobby"); //= hobby
        // store(ContactField. , "Priority"); //= priority
        // store(ContactField. , "Private"); // = private
        store(ContactField.PROFESSION, "Profession");
        // store(ContactField. , "Referred By"); // = referred by
        store(ContactField.MANAGER_NAME, "Manager's Name"); // guessed (by Antje)
        // store(ContactField. , "Directory Server"); // = directory server
        // store(ContactField. , "Gender"); // = gender
        // store(ContactField. , "User 1");
        // store(ContactField. , "User 2");
        // store(ContactField. , "User 3");
        // store(ContactField. , "Utilisateur 4");
    }
}
