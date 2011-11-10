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
public class GermanOutlookMapper extends AbstractContactFieldMapper {

    public GermanOutlookMapper() {
        store(ContactField.TITLE, "Anrede");
        store(ContactField.GIVEN_NAME, "Vorname");
        store(ContactField.MIDDLE_NAME, "Weitere Vornamen");
        store(ContactField.SUR_NAME, "Nachname");
        store(ContactField.SUFFIX, "Suffix");
        store(ContactField.COMPANY, "Firma");
        store(ContactField.DEPARTMENT, "Abteilung");
        store(ContactField.TITLE, "Anrede");
        store(ContactField.STREET_BUSINESS, "Stra\u00dfe gesch\u00e4ftlich");
        store(ContactField.CITY_BUSINESS, "Ort gesch\u00e4ftlich");
        store(ContactField.STATE_BUSINESS, "Region gesch\u00e4ftlich");
        store(ContactField.POSTAL_CODE_BUSINESS, "Postleitzahl gesch\u00e4ftlich");
        store(ContactField.COUNTRY_BUSINESS, "Land gesch\u00e4ftlich");
        store(ContactField.STREET_HOME, "Stra\u00dfe privat");
        store(ContactField.CITY_HOME, "Ort privat");
        store(ContactField.STATE_HOME, "Region privat");
        store(ContactField.POSTAL_CODE_HOME, "Postleitzahl privat");
        store(ContactField.COUNTRY_HOME, "Land privat");
        store(ContactField.STREET_OTHER, "Weitere Stra\u00dfe");
        store(ContactField.CITY_OTHER, "Weiterer Ort");
        store(ContactField.STATE_OTHER, "Weitere Region");
        store(ContactField.POSTAL_CODE_OTHER, "Weitere Postleitzahl");
        store(ContactField.COUNTRY_OTHER, "Weiteres Land");
        store(ContactField.TELEPHONE_ASSISTANT, "Telefon Assistent");
        store(ContactField.FAX_BUSINESS, "Fax gesch\u00e4ftlich");
        store(ContactField.TELEPHONE_BUSINESS1, "Telefon gesch\u00e4ftlich");
        store(ContactField.TELEPHONE_BUSINESS2, "Telefon gesch\u00e4ftlich 2");
        store(ContactField.TELEPHONE_CALLBACK, "R\u00fcckmeldung");
        store(ContactField.TELEPHONE_CAR, "Autotelefon");
        store(ContactField.TELEPHONE_COMPANY, "Telefon Firma");
        store(ContactField.FAX_HOME, "Fax privat");
        store(ContactField.TELEPHONE_HOME1, "Telefon privat");
        store(ContactField.TELEPHONE_HOME2, "Telefon privat 2");
        store(ContactField.TELEPHONE_ISDN, "ISDN");
        store(ContactField.CELLULAR_TELEPHONE1, "Mobiltelefon");
        store(ContactField.FAX_OTHER, "Weiteres Fax");
        store(ContactField.TELEPHONE_OTHER, "Weiteres Telefon");
        store(ContactField.TELEPHONE_PAGER, "Pager");
        store(ContactField.TELEPHONE_PRIMARY, "Haupttelefon");
        store(ContactField.TELEPHONE_RADIO, "Mobiltelefon 2");
        store(ContactField.TELEPHONE_TTYTDD, "Telefon f\u00fcr H\u00f6rbehinderte");
        store(ContactField.TELEPHONE_TELEX, "Telex");
        store(ContactField.EMAIL1, "E-Mail-Adresse");
        store(ContactField.EMAIL2, "E-Mail 2: Adresse");
        store(ContactField.EMAIL3, "E-Mail 3: Adresse");
        store(ContactField.BIRTHDAY, "Geburtstag");
        store(ContactField.ANNIVERSARY, "Jahrestag");
        store(ContactField.CATEGORIES, "Kategorien");
        store(ContactField.SPOUSE_NAME, "Partner");
        store(ContactField.PRIVATE_FLAG, "Vertraulichkeit");
        store(ContactField.NUMBER_OF_CHILDREN, "Kinder"); // guessed
        store(ContactField.ASSISTANT_NAME, "Name Assistent");
        store(ContactField.NOTE, "Notizen");
        store(ContactField.COMMERCIAL_REGISTER, "Organisations-Nr."); // guessed
        store(ContactField.URL, "Webseite"); // guessed
        store(ContactField.PROFESSION, "Beruf");
        store(ContactField.MANAGER_NAME, "Name des/der Vorgesetzten"); // guessed (by Antje)

        /*
         * Changes since bug 9367: Fields that Outlook 2007 is exporting either new or with a different name than 2003. We can be pretty
         * tolerant when importing these, because only one of the fields will be set (either the old one or the new one), but it does not
         * work in the other direction. TODO: Split the mapper between Outlook 2003 and 2007 because different fields need to be set.
         */
        something2ox.put("Land/Region gesch\u00e4ftlich", ContactField.STATE_BUSINESS);
        something2ox.put("E-Mail-Adresse", ContactField.EMAIL1); // this is the second asignment for EMAIL1. One should only occur in 2003,                                                               // the other only in 2007
        something2ox.put("Position", ContactField.POSITION);
        something2ox.put("B\u00fcro", ContactField.ROOM_NUMBER);
        something2ox.put("Bundesland/Kanton privat", ContactField.STATE_HOME);
        something2ox.put("Land/Region privat", ContactField.COUNTRY_HOME);
        something2ox.put("Weiteres/r Bundesland/Kanton", ContactField.STATE_OTHER);
        something2ox.put("Weiteres/e Land/Region", ContactField.COUNTRY_OTHER);
    }

}
