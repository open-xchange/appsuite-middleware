/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
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
