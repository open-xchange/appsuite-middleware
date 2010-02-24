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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
 * This class maps names of fields found Outlook's CSV files to names used by OX and vice versa. This class has been generated automatically
 * from i18n files.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class GermanOutlookMapper extends AbstractOutlookMapper {

    public GermanOutlookMapper() {
        outlook2ox.put("Anrede", ContactField.TITLE);
        ox2outlook.put(ContactField.TITLE, "Anrede");

        outlook2ox.put("Vorname", ContactField.GIVEN_NAME);
        ox2outlook.put(ContactField.GIVEN_NAME, "Vorname");

        outlook2ox.put("Weitere Vornamen", ContactField.MIDDLE_NAME);
        ox2outlook.put(ContactField.MIDDLE_NAME, "Weitere Vornamen");

        outlook2ox.put("Nachname", ContactField.SUR_NAME);
        ox2outlook.put(ContactField.SUR_NAME, "Nachname");

        outlook2ox.put("Suffix", ContactField.SUFFIX);
        ox2outlook.put(ContactField.SUFFIX, "Suffix");

        outlook2ox.put("Firma", ContactField.COMPANY);
        ox2outlook.put(ContactField.COMPANY, "Firma");

        outlook2ox.put("Abteilung", ContactField.DEPARTMENT);
        ox2outlook.put(ContactField.DEPARTMENT, "Abteilung");

        outlook2ox.put("Anrede", ContactField.TITLE); // company???
        ox2outlook.put(ContactField.TITLE, "Anrede"); // company???

        outlook2ox.put("Stra\u00dfe gesch\u00e4ftlich", ContactField.STREET_BUSINESS);
        ox2outlook.put(ContactField.STREET_BUSINESS, "Stra\u00dfe gesch\u00e4ftlich");

        // french2field.put("Stra\u00dfe gesch\u00e4ftlich 2" , ContactField.);
        // field2french.put(ContactField. , "Stra\u00dfe gesch\u00e4ftlich 2");

        // french2field.put("Stra\u00dfe gesch\u00e4ftlich 3" , ContactField.);
        // field2french.put(ContactField. , "Stra\u00dfe gesch\u00e4ftlich 3");

        outlook2ox.put("Ort gesch\u00e4ftlich", ContactField.CITY_BUSINESS);
        ox2outlook.put(ContactField.CITY_BUSINESS, "Ort gesch\u00e4ftlich");

        outlook2ox.put("Region gesch\u00e4ftlich", ContactField.STATE_BUSINESS);
        ox2outlook.put(ContactField.STATE_BUSINESS, "Region gesch\u00e4ftlich");

        outlook2ox.put("Postleitzahl gesch\u00e4ftlich", ContactField.POSTAL_CODE_BUSINESS);
        ox2outlook.put(ContactField.POSTAL_CODE_BUSINESS, "Postleitzahl gesch\u00e4ftlich");

        outlook2ox.put("Land gesch\u00e4ftlich", ContactField.COUNTRY_BUSINESS);
        ox2outlook.put(ContactField.COUNTRY_BUSINESS, "Land gesch\u00e4ftlich");

        outlook2ox.put("Stra\u00dfe privat", ContactField.STREET_HOME);
        ox2outlook.put(ContactField.STREET_HOME, "Stra\u00dfe privat");

        // french2field.put("Stra\u00dfe privat 2" , ContactField.);
        // field2french.put(ContactField. , "Stra\u00dfe privat 2");

        // french2field.put("Stra\u00dfe privat 3" , ContactField.);
        // field2french.put(ContactField. , "Stra\u00dfe privat 3");

        outlook2ox.put("Ort privat", ContactField.CITY_HOME);
        ox2outlook.put(ContactField.CITY_HOME, "Ort privat");

        outlook2ox.put("Region privat", ContactField.STATE_HOME);
        ox2outlook.put(ContactField.STATE_HOME, "Region privat");

        outlook2ox.put("Postleitzahl privat", ContactField.POSTAL_CODE_HOME);
        ox2outlook.put(ContactField.POSTAL_CODE_HOME, "Postleitzahl privat");

        outlook2ox.put("Land privat", ContactField.COUNTRY_HOME);
        ox2outlook.put(ContactField.COUNTRY_HOME, "Land privat");

        outlook2ox.put("Weitere Stra\u00dfe", ContactField.STREET_OTHER);
        ox2outlook.put(ContactField.STREET_OTHER, "Weitere Stra\u00dfe");

        // french2field.put("Weitere Stra\u00dfe 2" , ContactField.);
        // field2french.put(ContactField. , "Weitere Stra\u00dfe 2");

        // french2field.put("Weitere Stra\u00dfe 3" , ContactField.);
        // field2french.put(ContactField. , "Weitere Stra\u00dfe 3");

        outlook2ox.put("Weiterer Ort", ContactField.CITY_OTHER);
        ox2outlook.put(ContactField.CITY_OTHER, "Weiterer Ort");

        outlook2ox.put("Weitere Region", ContactField.STATE_OTHER);
        ox2outlook.put(ContactField.STATE_OTHER, "Weitere Region");

        outlook2ox.put("Weitere Postleitzahl", ContactField.POSTAL_CODE_OTHER);
        ox2outlook.put(ContactField.POSTAL_CODE_OTHER, "Weitere Postleitzahl");

        outlook2ox.put("Weiteres Land", ContactField.COUNTRY_OTHER);
        ox2outlook.put(ContactField.COUNTRY_OTHER, "Weiteres Land");

        outlook2ox.put("Telefon Assistent", ContactField.TELEPHONE_ASSISTANT);
        ox2outlook.put(ContactField.TELEPHONE_ASSISTANT, "Telefon Assistent");

        outlook2ox.put("Fax gesch\u00e4ftlich", ContactField.FAX_BUSINESS);
        ox2outlook.put(ContactField.FAX_BUSINESS, "Fax gesch\u00e4ftlich");

        outlook2ox.put("Telefon gesch\u00e4ftlich", ContactField.TELEPHONE_BUSINESS1);
        ox2outlook.put(ContactField.TELEPHONE_BUSINESS1, "Telefon gesch\u00e4ftlich");

        outlook2ox.put("Telefon gesch\u00e4ftlich 2", ContactField.TELEPHONE_BUSINESS2);
        ox2outlook.put(ContactField.TELEPHONE_BUSINESS2, "Telefon gesch\u00e4ftlich 2");

        outlook2ox.put("R\u00fcckmeldung", ContactField.TELEPHONE_CALLBACK);
        ox2outlook.put(ContactField.TELEPHONE_CALLBACK, "R\u00fcckmeldung");

        outlook2ox.put("Autotelefon", ContactField.TELEPHONE_CAR);
        ox2outlook.put(ContactField.TELEPHONE_CAR, "Autotelefon");

        outlook2ox.put("Telefon Firma", ContactField.TELEPHONE_COMPANY);
        ox2outlook.put(ContactField.TELEPHONE_COMPANY, "Telefon Firma");

        outlook2ox.put("Fax privat", ContactField.FAX_HOME);
        ox2outlook.put(ContactField.FAX_HOME, "Fax privat");

        outlook2ox.put("Telefon privat", ContactField.TELEPHONE_HOME1);
        ox2outlook.put(ContactField.TELEPHONE_HOME1, "Telefon privat");

        outlook2ox.put("Telefon privat 2", ContactField.TELEPHONE_HOME2);
        ox2outlook.put(ContactField.TELEPHONE_HOME2, "Telefon privat 2");

        outlook2ox.put("ISDN", ContactField.TELEPHONE_ISDN);
        ox2outlook.put(ContactField.TELEPHONE_ISDN, "ISDN");

        outlook2ox.put("Mobiltelefon", ContactField.CELLULAR_TELEPHONE1);
        ox2outlook.put(ContactField.CELLULAR_TELEPHONE1, "Mobiltelefon");

        outlook2ox.put("Weiteres Fax", ContactField.FAX_OTHER);
        ox2outlook.put(ContactField.FAX_OTHER, "Weiteres Fax");

        outlook2ox.put("Weiteres Telefon", ContactField.TELEPHONE_OTHER);
        ox2outlook.put(ContactField.TELEPHONE_OTHER, "Weiteres Telefon");

        outlook2ox.put("Pager", ContactField.TELEPHONE_PAGER);
        ox2outlook.put(ContactField.TELEPHONE_PAGER, "Pager");

        outlook2ox.put("Haupttelefon", ContactField.TELEPHONE_PRIMARY);
        ox2outlook.put(ContactField.TELEPHONE_PRIMARY, "Haupttelefon");

        outlook2ox.put("Mobiltelefon 2", ContactField.TELEPHONE_RADIO);
        ox2outlook.put(ContactField.TELEPHONE_RADIO, "Mobiltelefon 2");

        outlook2ox.put("Telefon f\u00fcr H\u00f6rbehinderte", ContactField.TELEPHONE_TTYTDD);
        ox2outlook.put(ContactField.TELEPHONE_TTYTDD, "Telefon f\u00fcr H\u00f6rbehinderte");

        outlook2ox.put("Telex", ContactField.TELEPHONE_TELEX);
        ox2outlook.put(ContactField.TELEPHONE_TELEX, "Telex");

        outlook2ox.put("E-Mail-Adresse", ContactField.EMAIL1);
        ox2outlook.put(ContactField.EMAIL1, "E-Mail-Adresse");

        // french2field.put("E-Mail-Typ" , ContactField.);
        // field2french.put(ContactField. , "E-Mail-Typ");

        // french2field.put("E-Mail: Angezeigter Name" , ContactField.);
        // field2french.put(ContactField. , "E-Mail: Angezeigter Name");

        outlook2ox.put("E-Mail 2: Adresse", ContactField.EMAIL2);
        ox2outlook.put(ContactField.EMAIL2, "E-Mail 2: Adresse");

        // french2field.put("E-Mail 2: Typ" , ContactField.);
        // field2french.put(ContactField. , "E-Mail 2: Typ");

        // french2field.put("E-Mail 2: Angezeigter Name" , ContactField.);
        // field2french.put(ContactField. , "E-Mail 2: Angezeigter Name");

        outlook2ox.put("E-Mail 3: Adresse ", ContactField.EMAIL3);
        ox2outlook.put(ContactField.EMAIL3, "E-Mail 3: Adresse ");

        // french2field.put("E-Mail 3: Typ" , ContactField.);
        // field2french.put(ContactField. , "E-Mail 3: Typ");

        // french2field.put("Nom complet de l'adresse de messagerie 3" , ContactField.);
        // field2french.put(ContactField. , "Nom complet de l'adresse de messagerie 3");

        outlook2ox.put("Geburtstag", ContactField.BIRTHDAY);
        ox2outlook.put(ContactField.BIRTHDAY, "Geburtstag");

        outlook2ox.put("Jahrestag", ContactField.ANNIVERSARY);
        ox2outlook.put(ContactField.ANNIVERSARY, "Jahrestag");

        // french2field.put("Weiteres Postfach" , ContactField.);
        // field2french.put(ContactField. , "Weiteres Postfach");

        // french2field.put("Postfach gesch\u00e4ftlich" , ContactField.);
        // field2french.put(ContactField. , "Postfach gesch\u00e4ftlich");

        // french2field.put("Postfach privat" , ContactField.);
        // field2french.put(ContactField. , "Postfach privat");

        // french2field.put("B\u00fcro" , ContactField.);
        // field2french.put(ContactField. , "B\u00fcro");

        outlook2ox.put("Kategorien", ContactField.CATEGORIES);
        ox2outlook.put(ContactField.CATEGORIES, "Kategorien");

        // french2field.put("Regierungs-Nr. " , ContactField.);
        // field2french.put(ContactField. , "Regierungs-Nr. ");

        // french2field.put("Konto" , ContactField.); //=account
        // field2french.put(ContactField. , "Konto"); //=account

        outlook2ox.put("Partner", ContactField.SPOUSE_NAME);
        ox2outlook.put(ContactField.SPOUSE_NAME, "Partner");

        outlook2ox.put("Vertraulichkeit", ContactField.PRIVATE_FLAG);
        ox2outlook.put(ContactField.PRIVATE_FLAG, "Vertraulichkeit");

        // french2field.put("Internet-Frei/Gebucht" , ContactField.); //=internet free/busy
        // field2french.put(ContactField. , "Internet-Frei/Gebucht"); //=internet free/busy

        // french2field.put("Ort " , ContactField.); //= location
        // field2french.put(ContactField. , "Ort "); //= location

        outlook2ox.put("Kinder", ContactField.NUMBER_OF_CHILDREN); // guessed
        ox2outlook.put(ContactField.NUMBER_OF_CHILDREN, "Kinder"); // guessed

        // french2field.put("Abrechnungsinformation" , ContactField.); // = billing information
        // field2french.put(ContactField. , "Abrechnungsinformation"); // = billing information

        // french2field.put("Initialen" , ContactField.); // = initials
        // field2french.put(ContactField. , "Initialen"); // = initials

        // french2field.put("Reisekilometer" , ContactField.); //= mileage
        // field2french.put(ContactField. , "Reisekilometer"); //= mileage

        // french2field.put("Sprache" , ContactField.); // = language
        // field2french.put(ContactField. , "Sprache"); // = language

        // french2field.put("Stichw\u00f6rter" , ContactField.); // = keywords
        // field2french.put(ContactField. , "Stichw\u00f6rter"); // = keywords

        outlook2ox.put("Name Assistent", ContactField.ASSISTANT_NAME);
        ox2outlook.put(ContactField.ASSISTANT_NAME, "Name Assistent");

        outlook2ox.put("Notizen", ContactField.NOTE);
        ox2outlook.put(ContactField.NOTE, "Notizen");

        outlook2ox.put("Organisations-Nr.", ContactField.COMMERCIAL_REGISTER); // guessed
        ox2outlook.put(ContactField.COMMERCIAL_REGISTER, "Organisations-Nr."); // guessed

        outlook2ox.put("Webseite", ContactField.URL); // guessed
        ox2outlook.put(ContactField.URL, "Webseite"); // guessed

        // french2field.put("Hobby " , ContactField.); //= hobby
        // field2french.put(ContactField. , "Hobby "); //= hobby

        // french2field.put("Priorit\u00e4t " , ContactField.); //= priority
        // field2french.put(ContactField. , "Priorit\u00e4t "); //= priority

        // french2field.put("Privat" , ContactField.); // = private
        // field2french.put(ContactField. , "Privat"); // = private

        outlook2ox.put("Beruf", ContactField.PROFESSION);
        ox2outlook.put(ContactField.PROFESSION, "Beruf");

        // french2field.put("Empfohlen von " , ContactField.); // = referred by
        // field2french.put(ContactField. , "Empfohlen von "); // = referred by

        outlook2ox.put("Name des/der Vorgesetzten", ContactField.MANAGER_NAME); // guessed (by Antje)
        ox2outlook.put(ContactField.MANAGER_NAME, "Name des/der Vorgesetzten"); // guessed (by Antje)

        // french2field.put("Verzeichnisserver" , ContactField.); // = directory server
        // field2french.put(ContactField. , "Verzeichnisserver"); // = directory server

        // french2field.put("Geschlecht" , ContactField.); // = gender
        // field2french.put(ContactField. , "Geschlecht"); // = gender

        // french2field.put("Benutzer 1" , ContactField.);
        // field2french.put(ContactField. , "Benutzer 1");

        // french2field.put("Benutzer 2" , ContactField.);
        // field2french.put(ContactField. , "Benutzer 2");

        // french2field.put("Benutzer 3" , ContactField.);
        // field2french.put(ContactField. , "Benutzer 3");

        // french2field.put("Benutzer 4" , ContactField.);
        // field2french.put(ContactField. , "Utilisateur 4");

        /*
         * Changes since bug 9367: Fields that Outlook 2007 is exporting either new or with a different name than 2003. We can be pretty
         * tolerant when importing these, because only one of the fields will be set (either the old one or the new one), but it does not
         * work in the other direction. TODO: Split the mapper between Outlook 2003 and 2007 because different fields need to be set.
         */
        outlook2ox.put("Land/Region gesch\u00e4ftlich", ContactField.STATE_BUSINESS);
        outlook2ox.put("E-Mail-Adresse", ContactField.EMAIL1); // this is the second asignment for EMAIL1. One should only occur in 2003,
                                                               // the other only in 2007
        outlook2ox.put("Position", ContactField.POSITION);
        outlook2ox.put("B\u00fcro", ContactField.ROOM_NUMBER);
        outlook2ox.put("Bundesland/Kanton privat", ContactField.STATE_HOME);
        outlook2ox.put("Land/Region privat", ContactField.COUNTRY_HOME);
        outlook2ox.put("Weiteres/r Bundesland/Kanton", ContactField.STATE_OTHER);
        outlook2ox.put("Weiteres/e Land/Region", ContactField.COUNTRY_OTHER);
        outlook2ox.put("E-Mail 3: Adresse", ContactField.EMAIL3);
    }

}
