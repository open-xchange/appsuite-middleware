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
public class GermanOutlookMapper implements ContactFieldMapper {
    protected final HashMap<String,ContactField> german2field = new HashMap<String, ContactField>();
    protected final HashMap<ContactField, String> field2german = new HashMap<ContactField, String>();

    public GermanOutlookMapper(){
        german2field.put("Anrede" , ContactField.TITLE);
        field2german.put(ContactField.TITLE , "Anrede");

        german2field.put("Vorname" , ContactField.GIVEN_NAME);
        field2german.put(ContactField.GIVEN_NAME , "Vorname");

        german2field.put("Weitere Vornamen" , ContactField.MIDDLE_NAME);
        field2german.put(ContactField.MIDDLE_NAME , "Weitere Vornamen");

        german2field.put("Nachname" , ContactField.SUR_NAME);
        field2german.put(ContactField.SUR_NAME , "Nachname");

        german2field.put("Suffix" , ContactField.SUFFIX);
        field2german.put(ContactField.SUFFIX , "Suffix");

        german2field.put("Firma" , ContactField.COMPANY);
        field2german.put(ContactField.COMPANY , "Firma");

        german2field.put("Abteilung" , ContactField.DEPARTMENT);
        field2german.put(ContactField.DEPARTMENT , "Abteilung");

        german2field.put("Anrede" , ContactField.TITLE); //company???
        field2german.put(ContactField.TITLE , "Anrede"); //company???

        german2field.put("Stra\u00dfe gesch\u00e4ftlich" , ContactField.STREET_BUSINESS);
        field2german.put(ContactField.STREET_BUSINESS , "Stra\u00dfe gesch\u00e4ftlich");

        //french2field.put("Stra\u00dfe gesch\u00e4ftlich 2" , ContactField.);
        //field2french.put(ContactField. , "Stra\u00dfe gesch\u00e4ftlich 2");

        //french2field.put("Stra\u00dfe gesch\u00e4ftlich 3" , ContactField.);
        //field2french.put(ContactField. , "Stra\u00dfe gesch\u00e4ftlich 3");

        german2field.put("Ort gesch\u00e4ftlich" , ContactField.CITY_BUSINESS);
        field2german.put(ContactField.CITY_BUSINESS , "Ort gesch\u00e4ftlich");

        german2field.put("Region gesch\u00e4ftlich" , ContactField.STATE_BUSINESS);
        field2german.put(ContactField.STATE_BUSINESS , "Region gesch\u00e4ftlich");

        german2field.put("Postleitzahl gesch\u00e4ftlich" , ContactField.POSTAL_CODE_BUSINESS);
        field2german.put(ContactField.POSTAL_CODE_BUSINESS , "Postleitzahl gesch\u00e4ftlich");

        german2field.put("Land gesch\u00e4ftlich" , ContactField.COUNTRY_BUSINESS);
        field2german.put(ContactField.COUNTRY_BUSINESS , "Land gesch\u00e4ftlich");

        german2field.put("Stra\u00dfe privat" , ContactField.STREET_HOME);
        field2german.put(ContactField.STREET_HOME , "Stra\u00dfe privat");

        //french2field.put("Stra\u00dfe privat 2" , ContactField.);
        //field2french.put(ContactField. , "Stra\u00dfe privat 2");

        //french2field.put("Stra\u00dfe privat 3" , ContactField.);
        //field2french.put(ContactField. , "Stra\u00dfe privat 3");

        german2field.put("Ort privat" , ContactField.CITY_HOME);
        field2german.put(ContactField.CITY_HOME , "Ort privat");

        german2field.put("Region privat" , ContactField.STATE_HOME);
        field2german.put(ContactField.STATE_HOME , "Region privat");

        german2field.put("Postleitzahl privat" , ContactField.POSTAL_CODE_HOME);
        field2german.put(ContactField.POSTAL_CODE_HOME , "Postleitzahl privat");

        german2field.put("Land privat" , ContactField.COUNTRY_HOME);
        field2german.put(ContactField.COUNTRY_HOME , "Land privat");

        german2field.put("Weitere Stra\u00dfe" , ContactField.STREET_OTHER);
        field2german.put(ContactField.STREET_OTHER , "Weitere Stra\u00dfe");

        //french2field.put("Weitere Stra\u00dfe 2" , ContactField.);
        //field2french.put(ContactField. , "Weitere Stra\u00dfe 2");

        //french2field.put("Weitere Stra\u00dfe 3" , ContactField.);
        //field2french.put(ContactField. , "Weitere Stra\u00dfe 3");

        german2field.put("Weiterer Ort" , ContactField.CITY_OTHER);
        field2german.put(ContactField.CITY_OTHER , "Weiterer Ort");

        german2field.put("Weitere Region" , ContactField.STATE_OTHER);
        field2german.put(ContactField.STATE_OTHER , "Weitere Region");

        german2field.put("Weitere Postleitzahl" , ContactField.POSTAL_CODE_OTHER);
        field2german.put(ContactField.POSTAL_CODE_OTHER , "Weitere Postleitzahl");

        german2field.put("Weiteres Land" , ContactField.COUNTRY_OTHER);
        field2german.put(ContactField.COUNTRY_OTHER , "Weiteres Land");

        german2field.put("Telefon Assistent" , ContactField.TELEPHONE_ASSISTANT);
        field2german.put(ContactField.TELEPHONE_ASSISTANT , "Telefon Assistent");

        german2field.put("Fax gesch\u00e4ftlich" , ContactField.FAX_BUSINESS);
        field2german.put(ContactField.FAX_BUSINESS , "Fax gesch\u00e4ftlich");

        german2field.put("Telefon gesch\u00e4ftlich" , ContactField.TELEPHONE_BUSINESS1);
        field2german.put(ContactField.TELEPHONE_BUSINESS1 , "Telefon gesch\u00e4ftlich");

        german2field.put("Telefon gesch\u00e4ftlich 2" , ContactField.TELEPHONE_BUSINESS2);
        field2german.put(ContactField.TELEPHONE_BUSINESS2 , "Telefon gesch\u00e4ftlich 2");

        german2field.put("R\u00fcckmeldung" , ContactField.TELEPHONE_CALLBACK);
        field2german.put(ContactField.TELEPHONE_CALLBACK , "R\u00fcckmeldung");

        german2field.put("Autotelefon" , ContactField.TELEPHONE_CAR);
        field2german.put(ContactField.TELEPHONE_CAR , "Autotelefon");

        german2field.put("Telefon Firma" , ContactField.TELEPHONE_COMPANY);
        field2german.put(ContactField.TELEPHONE_COMPANY , "Telefon Firma");

        german2field.put("Fax privat" , ContactField.FAX_HOME);
        field2german.put(ContactField.FAX_HOME , "Fax privat");

        german2field.put("Telefon privat" , ContactField.TELEPHONE_HOME1);
        field2german.put(ContactField.TELEPHONE_HOME1 , "Telefon privat");

        german2field.put("Telefon privat 2" , ContactField.TELEPHONE_HOME2);
        field2german.put(ContactField.TELEPHONE_HOME2 , "Telefon privat 2");

        german2field.put("ISDN" , ContactField.TELEPHONE_ISDN);
        field2german.put(ContactField.TELEPHONE_ISDN , "ISDN");

        german2field.put("Mobiltelefon" , ContactField.CELLULAR_TELEPHONE1); 
        field2german.put(ContactField.CELLULAR_TELEPHONE1 , "Mobiltelefon"); 

        german2field.put("Weiteres Fax" , ContactField.FAX_OTHER);
        field2german.put(ContactField.FAX_OTHER , "Weiteres Fax");

        german2field.put("Weiteres Telefon" , ContactField.TELEPHONE_OTHER);
        field2german.put(ContactField.TELEPHONE_OTHER , "Weiteres Telefon");

        german2field.put("Pager" , ContactField.TELEPHONE_PAGER);
        field2german.put(ContactField.TELEPHONE_PAGER , "Pager");

        german2field.put("Haupttelefon" , ContactField.TELEPHONE_PRIMARY);
        field2german.put(ContactField.TELEPHONE_PRIMARY , "Haupttelefon");

        german2field.put("Mobiltelefon 2" , ContactField.TELEPHONE_RADIO);
        field2german.put(ContactField.TELEPHONE_RADIO , "Mobiltelefon 2");

        german2field.put("Telefon f\u00fcr H\u00f6rbehinderte" , ContactField.TELEPHONE_TTYTDD);
        field2german.put(ContactField.TELEPHONE_TTYTDD , "Telefon f\u00fcr H\u00f6rbehinderte");

        german2field.put("Telex" , ContactField.TELEPHONE_TELEX);
        field2german.put(ContactField.TELEPHONE_TELEX , "Telex");

        german2field.put("Abrechnungsinformation" , ContactField.EMAIL1);
        field2german.put(ContactField.EMAIL1 , "Abrechnungsinformation");

        //french2field.put("E-Mail-Typ" , ContactField.);
        //field2french.put(ContactField. , "E-Mail-Typ");

        //french2field.put("E-Mail: Angezeigter Name" , ContactField.);
        //field2french.put(ContactField. , "E-Mail: Angezeigter Name");

        german2field.put("E-Mail 2: Adresse" , ContactField.EMAIL2);
        field2german.put(ContactField.EMAIL2 , "E-Mail 2: Adresse");

        //french2field.put("E-Mail 2: Typ" , ContactField.);
        //field2french.put(ContactField. , "E-Mail 2: Typ");

        //french2field.put("E-Mail 2: Angezeigter Name" , ContactField.);
        //field2french.put(ContactField. , "E-Mail 2: Angezeigter Name");

        german2field.put("E-Mail 3: Adresse " , ContactField.EMAIL3);
        field2german.put(ContactField.EMAIL3 , "E-Mail 3: Adresse ");

        //french2field.put("E-Mail 3: Typ" , ContactField.);
        //field2french.put(ContactField. , "E-Mail 3: Typ");

        //french2field.put("Nom complet de l'adresse de messagerie 3" , ContactField.);
        //field2french.put(ContactField. , "Nom complet de l'adresse de messagerie 3");

        german2field.put("Geburtstag" , ContactField.BIRTHDAY);
        field2german.put(ContactField.BIRTHDAY , "Geburtstag");

        german2field.put("Jahrestag" , ContactField.ANNIVERSARY);
        field2german.put(ContactField.ANNIVERSARY , "Jahrestag");

        //french2field.put("Weiteres Postfach" , ContactField.);
        //field2french.put(ContactField. , "Weiteres Postfach");

        //french2field.put("Postfach gesch\u00e4ftlich" , ContactField.);
        //field2french.put(ContactField. , "Postfach gesch\u00e4ftlich");

        //french2field.put("Postfach privat" , ContactField.);
        //field2french.put(ContactField. , "Postfach privat");

        //french2field.put("B\u00fcro" , ContactField.);
        //field2french.put(ContactField. , "B\u00fcro");

        german2field.put("Kategorien" , ContactField.CATEGORIES);
        field2german.put(ContactField.CATEGORIES , "Kategorien");

        //french2field.put("Regierungs-Nr. " , ContactField.);
        //field2french.put(ContactField. , "Regierungs-Nr. ");

        //french2field.put("Konto" , ContactField.); //=account
        //field2french.put(ContactField. , "Konto"); //=account

        german2field.put("Partner" , ContactField.SPOUSE_NAME);
        field2german.put(ContactField.SPOUSE_NAME , "Partner");

        german2field.put("Vertraulichkeit" , ContactField.PRIVATE_FLAG);
        field2german.put(ContactField.PRIVATE_FLAG , "Vertraulichkeit");

        //french2field.put("Internet-Frei/Gebucht" , ContactField.); //=internet free/busy
        //field2french.put(ContactField. , "Internet-Frei/Gebucht"); //=internet free/busy

        //french2field.put("Ort " , ContactField.); //= location
        //field2french.put(ContactField. , "Ort "); //= location

        german2field.put("Kinder" , ContactField.NUMBER_OF_CHILDREN); //guessed
        field2german.put(ContactField.NUMBER_OF_CHILDREN , "Kinder"); //guessed

        //french2field.put("Abrechnungsinformation" , ContactField.); // = billing information
        //field2french.put(ContactField. , "Abrechnungsinformation"); // = billing information

        //french2field.put("Initialen" , ContactField.); // = initials
        //field2french.put(ContactField. , "Initialen"); // = initials

        //french2field.put("Reisekilometer" , ContactField.); //= mileage
        //field2french.put(ContactField. , "Reisekilometer"); //= mileage

        //french2field.put("Sprache" , ContactField.); // = language
        //field2french.put(ContactField. , "Sprache"); // = language

        //french2field.put("Stichw\u00f6rter" , ContactField.); // = keywords
        //field2french.put(ContactField. , "Stichw\u00f6rter"); // = keywords

        german2field.put("Name Assistent" , ContactField.ASSISTANT_NAME);
        field2german.put(ContactField.ASSISTANT_NAME , "Name Assistent");

        german2field.put("Notizen" , ContactField.NOTE);
        field2german.put(ContactField.NOTE , "Notizen");

        german2field.put("Organisations-Nr." , ContactField.COMMERCIAL_REGISTER); //guessed
        field2german.put(ContactField.COMMERCIAL_REGISTER , "Organisations-Nr."); //guessed

        german2field.put("Webseite" , ContactField.URL); //guessed
        field2german.put(ContactField.URL , "Webseite"); //guessed

        //french2field.put("Hobby " , ContactField.); //= hobby
        //field2french.put(ContactField. , "Hobby "); //= hobby

        //french2field.put("Priorit\u00e4t " , ContactField.); //= priority
        //field2french.put(ContactField. , "Priorit\u00e4t "); //= priority

        //french2field.put("Privat" , ContactField.); // = private
        //field2french.put(ContactField. , "Privat"); // = private

        german2field.put("Beruf" , ContactField.PROFESSION);
        field2german.put(ContactField.PROFESSION , "Beruf");

        //french2field.put("Empfohlen von " , ContactField.); // = referred by
        //field2french.put(ContactField. , "Empfohlen von "); // = referred by

        german2field.put("Name des/der Vorgesetzten" , ContactField.MANAGER_NAME); //guessed (by Antje)
        field2german.put(ContactField.MANAGER_NAME , "Name des/der Vorgesetzten"); //guessed (by Antje)

        //french2field.put("Verzeichnisserver" , ContactField.); // = directory server
        //field2french.put(ContactField. , "Verzeichnisserver"); // = directory server

        //french2field.put("Geschlecht" , ContactField.); // = gender
        //field2french.put(ContactField. , "Geschlecht"); // = gender

        //french2field.put("Benutzer 1" , ContactField.);
        //field2french.put(ContactField. , "Benutzer 1");

        //french2field.put("Benutzer 2" , ContactField.);
        //field2french.put(ContactField. , "Benutzer 2");

        //french2field.put("Benutzer 3" , ContactField.);
        //field2french.put(ContactField. , "Benutzer 3");

        //french2field.put("Benutzer 4" , ContactField.);
        //field2french.put(ContactField. , "Utilisateur 4");


        }

        public ContactField getFieldByName(String name){
                return german2field.get(name);
        }

        public String getNameOfField(ContactField field){
            return field2german.get(field);
        }

        public Collection<String> getNamesOfFields(){
            return field2german.values();
        }

        public Collection<ContactField> getSupportedFields(){
            return german2field.values(); 
        }
    }
