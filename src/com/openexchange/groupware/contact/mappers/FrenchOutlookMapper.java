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

        french2field.put("Prénom" , ContactField.GIVEN_NAME);
        field2french.put(ContactField.GIVEN_NAME , "Prénom");

        french2field.put("Deuxième prénom" , ContactField.MIDDLE_NAME);
        field2french.put(ContactField.MIDDLE_NAME , "Deuxième prénom");

        french2field.put("Nom" , ContactField.SUR_NAME);
        field2french.put(ContactField.SUR_NAME , "Nom");

        french2field.put("Suffixe" , ContactField.SUFFIX);
        field2french.put(ContactField.SUFFIX , "Suffixe");

        french2field.put("Société " , ContactField.COMPANY);
        field2french.put(ContactField.COMPANY , "Société ");

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

        french2field.put("Dép/Région (bureau)" , ContactField.STATE_BUSINESS);
        field2french.put(ContactField.STATE_BUSINESS , "Dép/Région (bureau)");

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

        french2field.put("Dép/Région (domicile)" , ContactField.STATE_HOME);
        field2french.put(ContactField.STATE_HOME , "Dép/Région (domicile)");

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

        french2field.put("Dép/Région (autre)" , ContactField.STATE_OTHER);
        field2french.put(ContactField.STATE_OTHER , "Dép/Région (autre)");

        french2field.put("Code postal (autre)" , ContactField.POSTAL_CODE_OTHER);
        field2french.put(ContactField.POSTAL_CODE_OTHER , "Code postal (autre)");

        french2field.put("Pays (autre)" , ContactField.COUNTRY_OTHER);
        field2french.put(ContactField.COUNTRY_OTHER , "Pays (autre)");

        french2field.put("Téléphone de l'assistant(e)" , ContactField.TELEPHONE_ASSISTANT);
        field2french.put(ContactField.TELEPHONE_ASSISTANT , "Téléphone de l'assistant(e)");

        french2field.put("Télécopie (bureau)" , ContactField.FAX_BUSINESS);
        field2french.put(ContactField.FAX_BUSINESS , "Télécopie (bureau)");

        french2field.put("Téléphone (bureau)" , ContactField.TELEPHONE_BUSINESS1);
        field2french.put(ContactField.TELEPHONE_BUSINESS1 , "Téléphone (bureau)");

        french2field.put("Téléphone 2 (bureau)" , ContactField.TELEPHONE_BUSINESS2);
        field2french.put(ContactField.TELEPHONE_BUSINESS2 , "Téléphone 2 (bureau)");

        french2field.put("Rappel" , ContactField.TELEPHONE_CALLBACK);
        field2french.put(ContactField.TELEPHONE_CALLBACK , "Rappel");

        french2field.put("Téléphone (voiture)" , ContactField.TELEPHONE_CAR);
        field2french.put(ContactField.TELEPHONE_CAR , "Téléphone (voiture)");

        french2field.put("Téléphone société" , ContactField.TELEPHONE_COMPANY);
        field2french.put(ContactField.TELEPHONE_COMPANY , "Téléphone société");

        french2field.put("Télécopie (domicile)" , ContactField.FAX_HOME);
        field2french.put(ContactField.FAX_HOME , "Télécopie (domicile)");

        french2field.put("Téléphone (domicile)" , ContactField.TELEPHONE_HOME1);
        field2french.put(ContactField.TELEPHONE_HOME1 , "Téléphone (domicile)");

        french2field.put("Téléphone 2 (domicile)" , ContactField.TELEPHONE_HOME2);
        field2french.put(ContactField.TELEPHONE_HOME2 , "Téléphone 2 (domicile)");

        french2field.put("RNIS" , ContactField.TELEPHONE_ISDN);
        field2french.put(ContactField.TELEPHONE_ISDN , "RNIS");

        french2field.put("Tél. mobile" , ContactField.CELLULAR_TELEPHONE1); 
        field2french.put(ContactField.CELLULAR_TELEPHONE1 , "Tél. mobile"); 

        french2field.put("Télécopie (autre)" , ContactField.FAX_OTHER);
        field2french.put(ContactField.FAX_OTHER , "Télécopie (autre)");

        french2field.put("Téléphone (autre)" , ContactField.TELEPHONE_OTHER);
        field2french.put(ContactField.TELEPHONE_OTHER , "Téléphone (autre)");

        french2field.put("Récepteur de radiomessagerie" , ContactField.TELEPHONE_PAGER);
        field2french.put(ContactField.TELEPHONE_PAGER , "Récepteur de radiomessagerie");

        french2field.put("Téléphone principal" , ContactField.TELEPHONE_PRIMARY);
        field2french.put(ContactField.TELEPHONE_PRIMARY , "Téléphone principal");

        french2field.put("Radio téléphone" , ContactField.TELEPHONE_RADIO);
        field2french.put(ContactField.TELEPHONE_RADIO , "Radio téléphone");

        french2field.put("Téléphone TDD/TTY" , ContactField.TELEPHONE_TTYTDD);
        field2french.put(ContactField.TELEPHONE_TTYTDD , "Téléphone TDD/TTY");

        french2field.put("Télex" , ContactField.TELEPHONE_TELEX);
        field2french.put(ContactField.TELEPHONE_TELEX , "Télex");

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

        french2field.put("Anniversaire de mariage ou fête" , ContactField.ANNIVERSARY);
        field2french.put(ContactField.ANNIVERSARY , "Anniversaire de mariage ou fête");

        //french2field.put("Autre boîte postale" , ContactField.);
        //field2french.put(ContactField. , "Autre boîte postale");

        //french2field.put("B.P. professionnelle" , ContactField.);
        //field2french.put(ContactField. , "B.P. professionnelle");

        //french2field.put("Boîte postale du domicile" , ContactField.);
        //field2french.put(ContactField. , "Boîte postale du domicile");

        //french2field.put("Bureau" , ContactField.);
        //field2french.put(ContactField. , "Bureau");

        french2field.put("Catégories" , ContactField.CATEGORIES);
        field2french.put(ContactField.CATEGORIES , "Catégories");

        //french2field.put("Code gouvernement" , ContactField.);
        //field2french.put(ContactField. , "Code gouvernement");

        //french2field.put("Compte" , ContactField.); //=account
        //field2french.put(ContactField. , "Compte"); //=account

        french2field.put("Conjoint(e)" , ContactField.SPOUSE_NAME);
        field2french.put(ContactField.SPOUSE_NAME , "Conjoint(e)");

        //french2field.put("Critère de diffusion" , ContactField.); //=sensitivity
        //field2french.put(ContactField. , "Critère de diffusion"); //=sensitivity

        //french2field.put("Disponibilité Internet" , ContactField.); //=internet free/busy
        //field2french.put(ContactField. , "Disponibilité Internet"); //=internet free/busy

        //french2field.put("Emplacement" , ContactField.); //= location
        //field2french.put(ContactField. , "Emplacement"); //= location

        french2field.put("Enfants" , ContactField.NUMBER_OF_CHILDREN); //guessed
        field2french.put(ContactField.NUMBER_OF_CHILDREN , "Enfants"); //guessed

        //french2field.put("Informations facturation" , ContactField.); // = billing information
        //field2french.put(ContactField. , "Informations facturation"); // = billing information

        //french2field.put("Initiales" , ContactField.); // = initials
        //field2french.put(ContactField. , "Initiales"); // = initials

        //french2field.put("Kilométrage" , ContactField.); //= mileage
        //field2french.put(ContactField. , "Kilométrage"); //= mileage

        //french2field.put("Langue" , ContactField.); // = language
        //field2french.put(ContactField. , "Langue"); // = language

        //french2field.put("Mots clés" , ContactField.); // = keywords
        //field2french.put(ContactField. , "Mots clés"); // = keywords

        french2field.put("Nom de l'assistant(e)" , ContactField.ASSISTANT_NAME);
        field2french.put(ContactField.ASSISTANT_NAME , "Nom de l'assistant(e)");

        french2field.put("Notes" , ContactField.NOTE);
        field2french.put(ContactField.NOTE , "Notes");

        french2field.put("Numéro d'identification de l'organisation" , ContactField.COMMERCIAL_REGISTER); //guessed
        field2french.put(ContactField.COMMERCIAL_REGISTER , "Numéro d'identification de l'organisation"); //guessed

        french2field.put("Page Web" , ContactField.URL); //guessed
        field2french.put(ContactField.URL , "Page Web"); //guessed

        //french2field.put("Passe-temps" , ContactField.); //= hobby
        //field2french.put(ContactField. , "Passe-temps"); //= hobby

        //french2field.put("Priorité" , ContactField.); //= priority
        //field2french.put(ContactField. , "Priorité"); //= priority

        //french2field.put("Privé" , ContactField.); // = private
        //field2french.put(ContactField. , "Privé"); // = private

        french2field.put("Profession" , ContactField.PROFESSION);
        field2french.put(ContactField.PROFESSION , "Profession");

        //french2field.put("Recommandé par" , ContactField.); // = referred by
        //field2french.put(ContactField. , "Recommandé par"); // = referred by

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
