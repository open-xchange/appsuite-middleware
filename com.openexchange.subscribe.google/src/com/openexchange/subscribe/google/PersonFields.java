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

package com.openexchange.subscribe.google;

/**
 * {@link GoogleContactsSubscribeService}
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.6
 */
public enum PersonFields {

    /**
     * The person's addresses
     */
    ADDRESSES("addresses"),
    /**
     * The person's age ranges
     */
    AGE_RAGES("ageRanges"),
    /**
     * The person's biographies
     */
    BIOGRAPHIES("biographies"),
    /**
     * The person's biographies
     */
    BIRTHDAYS("birthdays"),
    /**
     * The person's calendar urls
     */
    CALENDAR_URLS("calendarUrls"),
    /**
     * The person's client data
     */
    CLIENT_DATA("clientData"),
    /**
     * The person's cover photos
     */
    COVER_PHOTOS("coverPhotos"),
    /**
     * The person's e-mail addresses
     */
    EMAIL_ADDRESSES("emailAddresses"),
    /**
     * The person's events
     */
    EVENTS("events"),
    /**
     * The person's external ids
     */
    EXTERNAL_IDS("externalIds"),
    /**
     * The person's genders
     */
    GENDERS("genders"),
    /**
     * The person's instant messenger clients
     */
    IM_CLIENTS("imClients"),
    /**
     * The person's interests
     */
    INTERESTS("interests"),
    /**
     * The person's locales
     */
    LOCALES("locales"),
    /**
     * The person's locations
     */
    LOCATIONS("locations"),
    /**
     * The person's memberships
     */
    MEMBERSHIPS("memberships"),
    /**
     * The person's metadata
     */
    METADATA("metadata"),
    /**
     * The person's misc keywords
     */
    MISCKEYWORDS("miscKeywords"),
    /**
     * The person's names
     */
    NAMES("names"),
    /**
     * The person's nicknames
     */
    NICKNAMES("nicknames"),
    /**
     * The person's occupations
     */
    OCCUPATIONS("occupations"),
    /**
     * The person's organizations
     */
    ORGANIZATIONS("organizations"),
    /**
     * The person's phone numbers
     */
    PHONE_NUMBERS("phoneNumbers"),
    /**
     * The person's photos
     */
    PHOTOS("photos"),
    /**
     * The person's relations
     */
    RELATIONS("relations"),
    /**
     * The person's relations
     */
    SIP_ADDRESSES("sipAddresses"),
    /**
     * The person's skills
     */
    SKILLS("skills"),
    /**
     * The person's urls
     */
    URLS("urls"),
    /**
     * The person's user defined data
     */
    USER_DEFINED("userDefined");

    private String fieldName;

    /**
     * Initialises a new {@link PersonFields}
     *
     * @param fieldName The field name
     */
    private PersonFields(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the field name
     *
     * @param fields The {@link PersonFields} arguments
     * @return Comma separated list of person field names
     */
    public String getName() {
        return fieldName;
    }

    /**
     * Creates a comma separated list of person field names
     *
     * @param fields The {@link PersonFields} arguments
     * @return Comma separated list of person field names
     */
    public static String create(PersonFields... fields) {
        StringBuilder sb = new StringBuilder();
        for (PersonFields field : fields) {
            sb.append(field.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
