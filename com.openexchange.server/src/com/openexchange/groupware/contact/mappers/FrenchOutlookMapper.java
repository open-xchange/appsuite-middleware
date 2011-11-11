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
 * This class maps names of fields found Outlook's CSV files to names used by OX
 * and vice versa. This class has been generated automatically from i18n files.
 * 
 * @deprecated Use the PropertyDrivenMapper with .properties files instead.
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class FrenchOutlookMapper extends AbstractContactFieldMapper {

        public FrenchOutlookMapper(){
        something2ox.put("Titre" , ContactField.TITLE);
        ox2something.put(ContactField.TITLE , "Titre");

        something2ox.put("Pr\u00e9nom" , ContactField.GIVEN_NAME);
        ox2something.put(ContactField.GIVEN_NAME , "Pr\u00e9nom");

        something2ox.put("Deuxi\u00e8me pr\u00e9nom" , ContactField.MIDDLE_NAME);
        ox2something.put(ContactField.MIDDLE_NAME , "Deuxi\u00e8me pr\u00e9nom");

        something2ox.put("Nom" , ContactField.SUR_NAME);
        ox2something.put(ContactField.SUR_NAME , "Nom");

        something2ox.put("Suffixe" , ContactField.SUFFIX);
        ox2something.put(ContactField.SUFFIX , "Suffixe");

        something2ox.put("Soci\u00e9t\u00e9 " , ContactField.COMPANY);
        ox2something.put(ContactField.COMPANY , "Soci\u00e9t\u00e9 ");

        something2ox.put("Service " , ContactField.DEPARTMENT);
        ox2something.put(ContactField.DEPARTMENT , "Service ");

        something2ox.put("Titre" , ContactField.TITLE); //company???
        ox2something.put(ContactField.TITLE , "Titre"); //company???

        something2ox.put("Rue (bureau)" , ContactField.STREET_BUSINESS);
        ox2something.put(ContactField.STREET_BUSINESS , "Rue (bureau)");

        //outlook2ox.put("Rue (bureau) 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (bureau) 2");

        //outlook2ox.put("Rue (bureau) 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (bureau) 3");

        something2ox.put("Ville (bureau)" , ContactField.CITY_BUSINESS);
        ox2something.put(ContactField.CITY_BUSINESS , "Ville (bureau)");

        something2ox.put("D\u00e9p/R\u00e9gion (bureau)" , ContactField.STATE_BUSINESS);
        ox2something.put(ContactField.STATE_BUSINESS , "D\u00e9p/R\u00e9gion (bureau)");

        something2ox.put("Code postal (bureau)" , ContactField.POSTAL_CODE_BUSINESS);
        ox2something.put(ContactField.POSTAL_CODE_BUSINESS , "Code postal (bureau)");

        something2ox.put("Pays (bureau)" , ContactField.COUNTRY_BUSINESS);
        ox2something.put(ContactField.COUNTRY_BUSINESS , "Pays (bureau)");

        something2ox.put("Rue (domicile)" , ContactField.STREET_HOME);
        ox2something.put(ContactField.STREET_HOME , "Rue (domicile)");

        //outlook2ox.put("Rue (domicile) 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (domicile) 2");

        //outlook2ox.put("Rue (domicile) 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (domicile) 3");

        something2ox.put("Ville (domicile)" , ContactField.CITY_HOME);
        ox2something.put(ContactField.CITY_HOME , "Ville (domicile)");

        something2ox.put("D\u00e9p/R\u00e9gion (domicile)" , ContactField.STATE_HOME);
        ox2something.put(ContactField.STATE_HOME , "D\u00e9p/R\u00e9gion (domicile)");

        something2ox.put("Code postal (domicile)" , ContactField.POSTAL_CODE_HOME);
        ox2something.put(ContactField.POSTAL_CODE_HOME , "Code postal (domicile)");

        something2ox.put("Pays (domicile)" , ContactField.COUNTRY_HOME);
        ox2something.put(ContactField.COUNTRY_HOME , "Pays (domicile)");

        something2ox.put("Rue (autre)" , ContactField.STREET_OTHER);
        ox2something.put(ContactField.STREET_OTHER , "Rue (autre)");

        //outlook2ox.put("Rue (autre) 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (autre) 2");

        //outlook2ox.put("Rue (autre) 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Rue (autre) 3");

        something2ox.put("Ville (autre)" , ContactField.CITY_OTHER);
        ox2something.put(ContactField.CITY_OTHER , "Ville (autre)");

        something2ox.put("D\u00e9p/R\u00e9gion (autre)" , ContactField.STATE_OTHER);
        ox2something.put(ContactField.STATE_OTHER , "D\u00e9p/R\u00e9gion (autre)");

        something2ox.put("Code postal (autre)" , ContactField.POSTAL_CODE_OTHER);
        ox2something.put(ContactField.POSTAL_CODE_OTHER , "Code postal (autre)");

        something2ox.put("Pays (autre)" , ContactField.COUNTRY_OTHER);
        ox2something.put(ContactField.COUNTRY_OTHER , "Pays (autre)");

        something2ox.put("T\u00e9l\u00e9phone de l'assistant(e)" , ContactField.TELEPHONE_ASSISTANT);
        ox2something.put(ContactField.TELEPHONE_ASSISTANT , "T\u00e9l\u00e9phone de l'assistant(e)");

        something2ox.put("T\u00e9l\u00e9copie (bureau)" , ContactField.FAX_BUSINESS);
        ox2something.put(ContactField.FAX_BUSINESS , "T\u00e9l\u00e9copie (bureau)");

        something2ox.put("T\u00e9l\u00e9phone (bureau)" , ContactField.TELEPHONE_BUSINESS1);
        ox2something.put(ContactField.TELEPHONE_BUSINESS1 , "T\u00e9l\u00e9phone (bureau)");

        something2ox.put("T\u00e9l\u00e9phone 2 (bureau)" , ContactField.TELEPHONE_BUSINESS2);
        ox2something.put(ContactField.TELEPHONE_BUSINESS2 , "T\u00e9l\u00e9phone 2 (bureau)");

        something2ox.put("Rappel" , ContactField.TELEPHONE_CALLBACK);
        ox2something.put(ContactField.TELEPHONE_CALLBACK , "Rappel");

        something2ox.put("T\u00e9l\u00e9phone (voiture)" , ContactField.TELEPHONE_CAR);
        ox2something.put(ContactField.TELEPHONE_CAR , "T\u00e9l\u00e9phone (voiture)");

        something2ox.put("T\u00e9l\u00e9phone soci\u00e9t\u00e9" , ContactField.TELEPHONE_COMPANY);
        ox2something.put(ContactField.TELEPHONE_COMPANY , "T\u00e9l\u00e9phone soci\u00e9t\u00e9");

        something2ox.put("T\u00e9l\u00e9copie (domicile)" , ContactField.FAX_HOME);
        ox2something.put(ContactField.FAX_HOME , "T\u00e9l\u00e9copie (domicile)");

        something2ox.put("T\u00e9l\u00e9phone (domicile)" , ContactField.TELEPHONE_HOME1);
        ox2something.put(ContactField.TELEPHONE_HOME1 , "T\u00e9l\u00e9phone (domicile)");

        something2ox.put("T\u00e9l\u00e9phone 2 (domicile)" , ContactField.TELEPHONE_HOME2);
        ox2something.put(ContactField.TELEPHONE_HOME2 , "T\u00e9l\u00e9phone 2 (domicile)");

        something2ox.put("RNIS" , ContactField.TELEPHONE_ISDN);
        ox2something.put(ContactField.TELEPHONE_ISDN , "RNIS");

        something2ox.put("T\u00e9l. mobile" , ContactField.CELLULAR_TELEPHONE1); 
        ox2something.put(ContactField.CELLULAR_TELEPHONE1 , "T\u00e9l. mobile"); 

        something2ox.put("T\u00e9l\u00e9copie (autre)" , ContactField.FAX_OTHER);
        ox2something.put(ContactField.FAX_OTHER , "T\u00e9l\u00e9copie (autre)");

        something2ox.put("T\u00e9l\u00e9phone (autre)" , ContactField.TELEPHONE_OTHER);
        ox2something.put(ContactField.TELEPHONE_OTHER , "T\u00e9l\u00e9phone (autre)");

        something2ox.put("R\u00e9cepteur de radiomessagerie" , ContactField.TELEPHONE_PAGER);
        ox2something.put(ContactField.TELEPHONE_PAGER , "R\u00e9cepteur de radiomessagerie");

        something2ox.put("T\u00e9l\u00e9phone principal" , ContactField.TELEPHONE_PRIMARY);
        ox2something.put(ContactField.TELEPHONE_PRIMARY , "T\u00e9l\u00e9phone principal");

        something2ox.put("Radio t\u00e9l\u00e9phone" , ContactField.TELEPHONE_RADIO);
        ox2something.put(ContactField.TELEPHONE_RADIO , "Radio t\u00e9l\u00e9phone");

        something2ox.put("T\u00e9l\u00e9phone TDD/TTY" , ContactField.TELEPHONE_TTYTDD);
        ox2something.put(ContactField.TELEPHONE_TTYTDD , "T\u00e9l\u00e9phone TDD/TTY");

        something2ox.put("T\u00e9lex" , ContactField.TELEPHONE_TELEX);
        ox2something.put(ContactField.TELEPHONE_TELEX , "T\u00e9lex");

        something2ox.put("Adresse de messagerie" , ContactField.EMAIL1);
        ox2something.put(ContactField.EMAIL1 , "Adresse de messagerie");

        //outlook2ox.put("Type de messagerie" , ContactField.);
        //ox2outlook.put(ContactField. , "Type de messagerie");

        //outlook2ox.put("Nom complet de l'adresse de messagerie" , ContactField.);
        //ox2outlook.put(ContactField. , "Nom complet de l'adresse de messagerie");

        something2ox.put("Adresse de messagerie 2" , ContactField.EMAIL2);
        ox2something.put(ContactField.EMAIL2 , "Adresse de messagerie 2");

        //outlook2ox.put("Type de messagerie 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Type de messagerie 2");

        //outlook2ox.put("Nom complet de l'adresse de messagerie 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Nom complet de l'adresse de messagerie 2");

        something2ox.put("Adresse de messagerie 3" , ContactField.EMAIL3);
        ox2something.put(ContactField.EMAIL3 , "Adresse de messagerie 3");

        //outlook2ox.put("Type de messagerie 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Type de messagerie 3");

        //outlook2ox.put("Nom complet de l'adresse de messagerie 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Nom complet de l'adresse de messagerie 3");

        something2ox.put("Anniversaire" , ContactField.BIRTHDAY);
        ox2something.put(ContactField.BIRTHDAY , "Anniversaire");

        something2ox.put("Anniversaire de mariage ou f\u00eate" , ContactField.ANNIVERSARY);
        ox2something.put(ContactField.ANNIVERSARY , "Anniversaire de mariage ou f\u00eate");

        //outlook2ox.put("Autre bo\u00eete postale" , ContactField.);
        //ox2outlook.put(ContactField. , "Autre bo\u00eete postale");

        //outlook2ox.put("B.P. professionnelle" , ContactField.);
        //ox2outlook.put(ContactField. , "B.P. professionnelle");

        //outlook2ox.put("Bo\u00eete postale du domicile" , ContactField.);
        //ox2outlook.put(ContactField. , "Bo\u00eete postale du domicile");

        //outlook2ox.put("Bureau" , ContactField.);
        //ox2outlook.put(ContactField. , "Bureau");

        something2ox.put("Cat\u00e9gories" , ContactField.CATEGORIES);
        ox2something.put(ContactField.CATEGORIES , "Cat\u00e9gories");

        //outlook2ox.put("Code gouvernement" , ContactField.);
        //ox2outlook.put(ContactField. , "Code gouvernement");

        //outlook2ox.put("Compte" , ContactField.); //=account
        //ox2outlook.put(ContactField. , "Compte"); //=account

        something2ox.put("Conjoint(e)" , ContactField.SPOUSE_NAME);
        ox2something.put(ContactField.SPOUSE_NAME , "Conjoint(e)");

        something2ox.put("Crit\u00e8re de diffusion" , ContactField.PRIVATE_FLAG); //=sensitivity
        ox2something.put(ContactField.PRIVATE_FLAG , "Crit\u00e8re de diffusion"); //=sensitivity

        //outlook2ox.put("Disponibilit\u00e9 Internet" , ContactField.); //=internet free/busy
        //ox2outlook.put(ContactField. , "Disponibilit\u00e9 Internet"); //=internet free/busy

        //outlook2ox.put("Emplacement" , ContactField.); //= location
        //ox2outlook.put(ContactField. , "Emplacement"); //= location

        something2ox.put("Enfants" , ContactField.NUMBER_OF_CHILDREN); //guessed
        ox2something.put(ContactField.NUMBER_OF_CHILDREN , "Enfants"); //guessed

        //outlook2ox.put("Information facturation" , ContactField.); // = billing information
        //ox2outlook.put(ContactField. , "Information facturation"); // = billing information

        //outlook2ox.put("Initiales" , ContactField.); // = initials
        //ox2outlook.put(ContactField. , "Initiales"); // = initials

        //outlook2ox.put("Kilom\u00e9trage" , ContactField.); //= mileage
        //ox2outlook.put(ContactField. , "Kilom\u00e9trage"); //= mileage

        //outlook2ox.put("Langue" , ContactField.); // = language
        //ox2outlook.put(ContactField. , "Langue"); // = language

        //outlook2ox.put("Mots cl\u00e9s" , ContactField.); // = keywords
        //ox2outlook.put(ContactField. , "Mots cl\u00e9s"); // = keywords

        something2ox.put("Nom de l'assistant(e)" , ContactField.ASSISTANT_NAME);
        ox2something.put(ContactField.ASSISTANT_NAME , "Nom de l'assistant(e)");

        something2ox.put("Notes" , ContactField.NOTE);
        ox2something.put(ContactField.NOTE , "Notes");

        something2ox.put("Num\u00e9ro d'identification de l'organisation" , ContactField.COMMERCIAL_REGISTER); //guessed
        ox2something.put(ContactField.COMMERCIAL_REGISTER , "Num\u00e9ro d'identification de l'organisation"); //guessed

        something2ox.put("Page Web" , ContactField.URL); //guessed
        ox2something.put(ContactField.URL , "Page Web"); //guessed

        //outlook2ox.put("Passe-temps" , ContactField.); //= hobby
        //ox2outlook.put(ContactField. , "Passe-temps"); //= hobby

        //outlook2ox.put("Priorit\u00e9" , ContactField.); //= priority
        //ox2outlook.put(ContactField. , "Priorit\u00e9"); //= priority

        //outlook2ox.put("Priv\u00e9" , ContactField.); // = private
        //ox2outlook.put(ContactField. , "Priv\u00e9"); // = private

        something2ox.put("Profession" , ContactField.PROFESSION);
        ox2something.put(ContactField.PROFESSION , "Profession");

        //outlook2ox.put("Recommand\u00e9 par" , ContactField.); // = referred by
        //ox2outlook.put(ContactField. , "Recommand\u00e9 par"); // = referred by

        something2ox.put("Responsable" , ContactField.MANAGER_NAME); //guessed (by Antje)
        ox2something.put(ContactField.MANAGER_NAME , "Responsable"); //guessed (by Antje)

        //outlook2ox.put("Serveur d'annuaire" , ContactField.); // = directory server
        //ox2outlook.put(ContactField. , "Serveur d'annuaire"); // = directory server

        //outlook2ox.put("Sexe" , ContactField.); // = gender
        //ox2outlook.put(ContactField. , "Sexe"); // = gender

        //outlook2ox.put("Utilisateur 1" , ContactField.);
        //ox2outlook.put(ContactField. , "Utilisateur 1");

        //outlook2ox.put("Utilisateur 2" , ContactField.);
        //ox2outlook.put(ContactField. , "Utilisateur 2");

        //outlook2ox.put("Utilisateur 3" , ContactField.);
        //ox2outlook.put(ContactField. , "Utilisateur 3");

        //outlook2ox.put("Utilisateur 4" , ContactField.);
        //ox2outlook.put(ContactField. , "Utilisateur 4");

        }

}
