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
public class FrenchOutlookMapper implements ContactFieldMapper {

        protected final HashMap<String,ContactField> french2field = new HashMap<String, ContactField>();
        protected final HashMap<ContactField, String> field2french = new HashMap<ContactField, String>();

        public FrenchOutlookMapper(){
        french2field.put("Titre" , ContactField.TITLE);
        field2french.put(ContactField.TITLE , "Titre");

        french2field.put("Pr\u00e9nom" , ContactField.GIVEN_NAME);
        field2french.put(ContactField.GIVEN_NAME , "Pr\u00e9nom");

        french2field.put("Deuxi\u00e8me pr\u00e9nom" , ContactField.MIDDLE_NAME);
        field2french.put(ContactField.MIDDLE_NAME , "Deuxi\u00e8me pr\u00e9nom");

        french2field.put("Nom" , ContactField.SUR_NAME);
        field2french.put(ContactField.SUR_NAME , "Nom");

        french2field.put("Suffixe" , ContactField.SUFFIX);
        field2french.put(ContactField.SUFFIX , "Suffixe");

        french2field.put("Soci\u00e9t\u00e9 " , ContactField.COMPANY);
        field2french.put(ContactField.COMPANY , "Soci\u00e9t\u00e9 ");

        french2field.put("Service " , ContactField.DEPARTMENT);
        field2french.put(ContactField.DEPARTMENT , "Service ");

        french2field.put("Titre" , ContactField.TITLE); //company???
        field2french.put(ContactField.TITLE , "Titre"); //company???

        french2field.put("Rue (bureau)" , ContactField.STREET_BUSINESS);
        field2french.put(ContactField.STREET_BUSINESS , "Rue (bureau)");

        //french2field.put("Rue (bureau) 2" , ContactField.);
        //field2french.put(ContactField. , "Rue (bureau) 2");

        //french2field.put("Rue (bureau) 3" , ContactField.);
        //field2french.put(ContactField. , "Rue (bureau) 3");

        french2field.put("Ville (bureau)" , ContactField.CITY_BUSINESS);
        field2french.put(ContactField.CITY_BUSINESS , "Ville (bureau)");

        french2field.put("D\u00e9p/R\u00e9gion (bureau)" , ContactField.STATE_BUSINESS);
        field2french.put(ContactField.STATE_BUSINESS , "D\u00e9p/R\u00e9gion (bureau)");

        french2field.put("Code postal (bureau)" , ContactField.POSTAL_CODE_BUSINESS);
        field2french.put(ContactField.POSTAL_CODE_BUSINESS , "Code postal (bureau)");

        french2field.put("Pays (bureau)" , ContactField.COUNTRY_BUSINESS);
        field2french.put(ContactField.COUNTRY_BUSINESS , "Pays (bureau)");

        french2field.put("Rue (domicile)" , ContactField.STREET_HOME);
        field2french.put(ContactField.STREET_HOME , "Rue (domicile)");

        //french2field.put("Rue (domicile) 2" , ContactField.);
        //field2french.put(ContactField. , "Rue (domicile) 2");

        //french2field.put("Rue (domicile) 3" , ContactField.);
        //field2french.put(ContactField. , "Rue (domicile) 3");

        french2field.put("Ville (domicile)" , ContactField.CITY_HOME);
        field2french.put(ContactField.CITY_HOME , "Ville (domicile)");

        french2field.put("D\u00e9p/R\u00e9gion (domicile)" , ContactField.STATE_HOME);
        field2french.put(ContactField.STATE_HOME , "D\u00e9p/R\u00e9gion (domicile)");

        french2field.put("Code postal (domicile)" , ContactField.POSTAL_CODE_HOME);
        field2french.put(ContactField.POSTAL_CODE_HOME , "Code postal (domicile)");

        french2field.put("Pays (domicile)" , ContactField.COUNTRY_HOME);
        field2french.put(ContactField.COUNTRY_HOME , "Pays (domicile)");

        french2field.put("Rue (autre)" , ContactField.STREET_OTHER);
        field2french.put(ContactField.STREET_OTHER , "Rue (autre)");

        //french2field.put("Rue (autre) 2" , ContactField.);
        //field2french.put(ContactField. , "Rue (autre) 2");

        //french2field.put("Rue (autre) 3" , ContactField.);
        //field2french.put(ContactField. , "Rue (autre) 3");

        french2field.put("Ville (autre)" , ContactField.CITY_OTHER);
        field2french.put(ContactField.CITY_OTHER , "Ville (autre)");

        french2field.put("D\u00e9p/R\u00e9gion (autre)" , ContactField.STATE_OTHER);
        field2french.put(ContactField.STATE_OTHER , "D\u00e9p/R\u00e9gion (autre)");

        french2field.put("Code postal (autre)" , ContactField.POSTAL_CODE_OTHER);
        field2french.put(ContactField.POSTAL_CODE_OTHER , "Code postal (autre)");

        french2field.put("Pays (autre)" , ContactField.COUNTRY_OTHER);
        field2french.put(ContactField.COUNTRY_OTHER , "Pays (autre)");

        french2field.put("T\u00e9l\u00e9phone de l'assistant(e)" , ContactField.TELEPHONE_ASSISTANT);
        field2french.put(ContactField.TELEPHONE_ASSISTANT , "T\u00e9l\u00e9phone de l'assistant(e)");

        french2field.put("T\u00e9l\u00e9copie (bureau)" , ContactField.FAX_BUSINESS);
        field2french.put(ContactField.FAX_BUSINESS , "T\u00e9l\u00e9copie (bureau)");

        french2field.put("T\u00e9l\u00e9phone (bureau)" , ContactField.TELEPHONE_BUSINESS1);
        field2french.put(ContactField.TELEPHONE_BUSINESS1 , "T\u00e9l\u00e9phone (bureau)");

        french2field.put("T\u00e9l\u00e9phone 2 (bureau)" , ContactField.TELEPHONE_BUSINESS2);
        field2french.put(ContactField.TELEPHONE_BUSINESS2 , "T\u00e9l\u00e9phone 2 (bureau)");

        french2field.put("Rappel" , ContactField.TELEPHONE_CALLBACK);
        field2french.put(ContactField.TELEPHONE_CALLBACK , "Rappel");

        french2field.put("T\u00e9l\u00e9phone (voiture)" , ContactField.TELEPHONE_CAR);
        field2french.put(ContactField.TELEPHONE_CAR , "T\u00e9l\u00e9phone (voiture)");

        french2field.put("T\u00e9l\u00e9phone soci\u00e9t\u00e9" , ContactField.TELEPHONE_COMPANY);
        field2french.put(ContactField.TELEPHONE_COMPANY , "T\u00e9l\u00e9phone soci\u00e9t\u00e9");

        french2field.put("T\u00e9l\u00e9copie (domicile)" , ContactField.FAX_HOME);
        field2french.put(ContactField.FAX_HOME , "T\u00e9l\u00e9copie (domicile)");

        french2field.put("T\u00e9l\u00e9phone (domicile)" , ContactField.TELEPHONE_HOME1);
        field2french.put(ContactField.TELEPHONE_HOME1 , "T\u00e9l\u00e9phone (domicile)");

        french2field.put("T\u00e9l\u00e9phone 2 (domicile)" , ContactField.TELEPHONE_HOME2);
        field2french.put(ContactField.TELEPHONE_HOME2 , "T\u00e9l\u00e9phone 2 (domicile)");

        french2field.put("RNIS" , ContactField.TELEPHONE_ISDN);
        field2french.put(ContactField.TELEPHONE_ISDN , "RNIS");

        french2field.put("T\u00e9l. mobile" , ContactField.CELLULAR_TELEPHONE1); 
        field2french.put(ContactField.CELLULAR_TELEPHONE1 , "T\u00e9l. mobile"); 

        french2field.put("T\u00e9l\u00e9copie (autre)" , ContactField.FAX_OTHER);
        field2french.put(ContactField.FAX_OTHER , "T\u00e9l\u00e9copie (autre)");

        french2field.put("T\u00e9l\u00e9phone (autre)" , ContactField.TELEPHONE_OTHER);
        field2french.put(ContactField.TELEPHONE_OTHER , "T\u00e9l\u00e9phone (autre)");

        french2field.put("R\u00e9cepteur de radiomessagerie" , ContactField.TELEPHONE_PAGER);
        field2french.put(ContactField.TELEPHONE_PAGER , "R\u00e9cepteur de radiomessagerie");

        french2field.put("T\u00e9l\u00e9phone principal" , ContactField.TELEPHONE_PRIMARY);
        field2french.put(ContactField.TELEPHONE_PRIMARY , "T\u00e9l\u00e9phone principal");

        french2field.put("Radio t\u00e9l\u00e9phone" , ContactField.TELEPHONE_RADIO);
        field2french.put(ContactField.TELEPHONE_RADIO , "Radio t\u00e9l\u00e9phone");

        french2field.put("T\u00e9l\u00e9phone TDD/TTY" , ContactField.TELEPHONE_TTYTDD);
        field2french.put(ContactField.TELEPHONE_TTYTDD , "T\u00e9l\u00e9phone TDD/TTY");

        french2field.put("T\u00e9lex" , ContactField.TELEPHONE_TELEX);
        field2french.put(ContactField.TELEPHONE_TELEX , "T\u00e9lex");

        french2field.put("Adresse de messagerie" , ContactField.EMAIL1);
        field2french.put(ContactField.EMAIL1 , "Adresse de messagerie");

        //french2field.put("Type de messagerie" , ContactField.);
        //field2french.put(ContactField. , "Type de messagerie");

        //french2field.put("Nom complet de l'adresse de messagerie" , ContactField.);
        //field2french.put(ContactField. , "Nom complet de l'adresse de messagerie");

        french2field.put("Adresse de messagerie 2" , ContactField.EMAIL2);
        field2french.put(ContactField.EMAIL2 , "Adresse de messagerie 2");

        //french2field.put("Type de messagerie 2" , ContactField.);
        //field2french.put(ContactField. , "Type de messagerie 2");

        //french2field.put("Nom complet de l'adresse de messagerie 2" , ContactField.);
        //field2french.put(ContactField. , "Nom complet de l'adresse de messagerie 2");

        french2field.put("Adresse de messagerie 3" , ContactField.EMAIL3);
        field2french.put(ContactField.EMAIL3 , "Adresse de messagerie 3");

        //french2field.put("Type de messagerie 3" , ContactField.);
        //field2french.put(ContactField. , "Type de messagerie 3");

        //french2field.put("Nom complet de l'adresse de messagerie 3" , ContactField.);
        //field2french.put(ContactField. , "Nom complet de l'adresse de messagerie 3");

        french2field.put("Anniversaire" , ContactField.BIRTHDAY);
        field2french.put(ContactField.BIRTHDAY , "Anniversaire");

        french2field.put("Anniversaire de mariage ou f\u00eate" , ContactField.ANNIVERSARY);
        field2french.put(ContactField.ANNIVERSARY , "Anniversaire de mariage ou f\u00eate");

        //french2field.put("Autre bo\u00eete postale" , ContactField.);
        //field2french.put(ContactField. , "Autre bo\u00eete postale");

        //french2field.put("B.P. professionnelle" , ContactField.);
        //field2french.put(ContactField. , "B.P. professionnelle");

        //french2field.put("Bo\u00eete postale du domicile" , ContactField.);
        //field2french.put(ContactField. , "Bo\u00eete postale du domicile");

        //french2field.put("Bureau" , ContactField.);
        //field2french.put(ContactField. , "Bureau");

        french2field.put("Cat\u00e9gories" , ContactField.CATEGORIES);
        field2french.put(ContactField.CATEGORIES , "Cat\u00e9gories");

        //french2field.put("Code gouvernement" , ContactField.);
        //field2french.put(ContactField. , "Code gouvernement");

        //french2field.put("Compte" , ContactField.); //=account
        //field2french.put(ContactField. , "Compte"); //=account

        french2field.put("Conjoint(e)" , ContactField.SPOUSE_NAME);
        field2french.put(ContactField.SPOUSE_NAME , "Conjoint(e)");

        french2field.put("Crit\u00e8re de diffusion" , ContactField.PRIVATE_FLAG); //=sensitivity
        field2french.put(ContactField.PRIVATE_FLAG , "Crit\u00e8re de diffusion"); //=sensitivity

        //french2field.put("Disponibilit\u00e9 Internet" , ContactField.); //=internet free/busy
        //field2french.put(ContactField. , "Disponibilit\u00e9 Internet"); //=internet free/busy

        //french2field.put("Emplacement" , ContactField.); //= location
        //field2french.put(ContactField. , "Emplacement"); //= location

        french2field.put("Enfants" , ContactField.NUMBER_OF_CHILDREN); //guessed
        field2french.put(ContactField.NUMBER_OF_CHILDREN , "Enfants"); //guessed

        //french2field.put("Informations facturation" , ContactField.); // = billing information
        //field2french.put(ContactField. , "Informations facturation"); // = billing information

        //french2field.put("Initiales" , ContactField.); // = initials
        //field2french.put(ContactField. , "Initiales"); // = initials

        //french2field.put("Kilom\u00e9trage" , ContactField.); //= mileage
        //field2french.put(ContactField. , "Kilom\u00e9trage"); //= mileage

        //french2field.put("Langue" , ContactField.); // = language
        //field2french.put(ContactField. , "Langue"); // = language

        //french2field.put("Mots cl\u00e9s" , ContactField.); // = keywords
        //field2french.put(ContactField. , "Mots cl\u00e9s"); // = keywords

        french2field.put("Nom de l'assistant(e)" , ContactField.ASSISTANT_NAME);
        field2french.put(ContactField.ASSISTANT_NAME , "Nom de l'assistant(e)");

        french2field.put("Notes" , ContactField.NOTE);
        field2french.put(ContactField.NOTE , "Notes");

        french2field.put("Num\u00e9ro d'identification de l'organisation" , ContactField.COMMERCIAL_REGISTER); //guessed
        field2french.put(ContactField.COMMERCIAL_REGISTER , "Num\u00e9ro d'identification de l'organisation"); //guessed

        french2field.put("Page Web" , ContactField.URL); //guessed
        field2french.put(ContactField.URL , "Page Web"); //guessed

        //french2field.put("Passe-temps" , ContactField.); //= hobby
        //field2french.put(ContactField. , "Passe-temps"); //= hobby

        //french2field.put("Priorit\u00e9" , ContactField.); //= priority
        //field2french.put(ContactField. , "Priorit\u00e9"); //= priority

        //french2field.put("Priv\u00e9" , ContactField.); // = private
        //field2french.put(ContactField. , "Priv\u00e9"); // = private

        french2field.put("Profession" , ContactField.PROFESSION);
        field2french.put(ContactField.PROFESSION , "Profession");

        //french2field.put("Recommand\u00e9 par" , ContactField.); // = referred by
        //field2french.put(ContactField. , "Recommand\u00e9 par"); // = referred by

        french2field.put("Responsable" , ContactField.MANAGER_NAME); //guessed (by Antje)
        field2french.put(ContactField.MANAGER_NAME , "Responsable"); //guessed (by Antje)

        //french2field.put("Serveur d'annuaire" , ContactField.); // = directory server
        //field2french.put(ContactField. , "Serveur d'annuaire"); // = directory server

        //french2field.put("Sexe" , ContactField.); // = gender
        //field2french.put(ContactField. , "Sexe"); // = gender

        //french2field.put("Utilisateur 1" , ContactField.);
        //field2french.put(ContactField. , "Utilisateur 1");

        //french2field.put("Utilisateur 2" , ContactField.);
        //field2french.put(ContactField. , "Utilisateur 2");

        //french2field.put("Utilisateur 3" , ContactField.);
        //field2french.put(ContactField. , "Utilisateur 3");

        //french2field.put("Utilisateur 4" , ContactField.);
        //field2french.put(ContactField. , "Utilisateur 4");


        }

        public ContactField getFieldByName(String name){
                return french2field.get(name);
        }

        public String getNameOfField(ContactField field){
                return field2french.get(field);
        }

        public Collection<String> getNamesOfFields(){
                return field2french.values();
        }

    public Collection<ContactField> getSupportedFields(){
        return french2field.values(); 
    }
}
