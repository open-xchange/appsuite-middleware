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
 * @deprecated Use the PropertyDrivenMapper with .properties files instead.
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
@Deprecated
public class DutchOutlookMapper extends AbstractContactFieldMapper {

    public DutchOutlookMapper() {
        super();
        store(ContactField.TITLE, "Title");
        store(ContactField.GIVEN_NAME, "Voornaam");
        store(ContactField.MIDDLE_NAME, "Middle Name");
        store(ContactField.DISPLAY_NAME, "Weergavenaam");
        store(ContactField.SUR_NAME, "Achternaam");
        store(ContactField.SUFFIX, "");
        store(ContactField.COMPANY, "Organisatie");
        store(ContactField.DEPARTMENT, "Afdeling");
        store(ContactField.TITLE, "");
        store(ContactField.STREET_BUSINESS, "Werkadres");
        store(ContactField.CITY_BUSINESS, "Werkplaats");
        store(ContactField.STATE_BUSINESS, "Werkprovincie");
        store(ContactField.POSTAL_CODE_BUSINESS, "Werkpostcode");
        store(ContactField.COUNTRY_BUSINESS, "Werkland");
        store(ContactField.STREET_HOME, "Adres");
        store(ContactField.CITY_HOME, "Woonplaats");
        store(ContactField.STATE_HOME, "Provincie");
        store(ContactField.POSTAL_CODE_HOME, "Postcode");
        store(ContactField.COUNTRY_HOME, "Land");
        store(ContactField.STREET_OTHER, "");
        store(ContactField.CITY_OTHER, "");
        store(ContactField.STATE_OTHER, "");
        store(ContactField.POSTAL_CODE_OTHER, "");
        store(ContactField.COUNTRY_OTHER, "");
        store(ContactField.TELEPHONE_ASSISTANT, "");
        store(ContactField.FAX_BUSINESS, "");
        store(ContactField.TELEPHONE_BUSINESS1, "Telefoon werk");
        store(ContactField.TELEPHONE_BUSINESS2, "");
        store(ContactField.TELEPHONE_CALLBACK, "");
        store(ContactField.TELEPHONE_CAR, "");
        store(ContactField.TELEPHONE_COMPANY, "");
        store(ContactField.FAX_HOME, "Faxnummer");
        store(ContactField.TELEPHONE_HOME1, "Telefoon thuis");
        store(ContactField.TELEPHONE_HOME2, "");
        store(ContactField.TELEPHONE_ISDN, "");
        store(ContactField.CELLULAR_TELEPHONE1, "Mobiel nummer");
        store(ContactField.FAX_OTHER, "");
        store(ContactField.TELEPHONE_OTHER, "");
        store(ContactField.TELEPHONE_PAGER, "Pieper nummer");
        store(ContactField.TELEPHONE_PRIMARY, "");
        store(ContactField.TELEPHONE_RADIO, "");
        store(ContactField.TELEPHONE_TTYTDD, "");
        store(ContactField.TELEPHONE_TELEX, "");
        store(ContactField.EMAIL1, "Eerste e-mail");
        store(ContactField.EMAIL2, "Tweede e-mail");
        store(ContactField.EMAIL3, "");
        store(ContactField.BIRTHDAY, "");
        store(ContactField.ANNIVERSARY, "");
        store(ContactField.CATEGORIES, "");
        store(ContactField.SPOUSE_NAME, "");
        store(ContactField.PRIVATE_FLAG, "");
        store(ContactField.NUMBER_OF_CHILDREN, "");
        store(ContactField.ASSISTANT_NAME, "");
        store(ContactField.NOTE, "Aantekeningen");
        store(ContactField.COMMERCIAL_REGISTER, "");
        store(ContactField.URL, "Webpagina 1");
        store(ContactField.PROFESSION, "Werktitel");
        store(ContactField.MANAGER_NAME, "");
    }

}
