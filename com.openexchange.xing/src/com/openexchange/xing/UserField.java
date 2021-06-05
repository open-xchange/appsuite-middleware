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
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.xing;

import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.xing.util.InverseComparator;

/**
 * {@link UserField} - The supported user fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum UserField {

    ID(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getId(), user2.getId());
        }}),
    FIRST_NAME(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getFirstName(),user2.getFirstName());
        }}),
    LAST_NAME(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getLastName(),user2.getLastName());
        }}),
    DISPLAY_NAME(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getDisplayName(),user2.getDisplayName());
        }}),
    PAGE_NAME(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getPageName(),user2.getPageName());
        }}),
    PERMALINK(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getPermalink(),user2.getPermalink());
        }}),
    GENDER(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getGender(),user2.getGender());
        }}),
    BIRTH_DATE(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : 0;
        }}),
    ACTIVE_EMAIL(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getActiveMail(),user2.getActiveMail());
        }}),
    TIME_ZONE(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            if (null == user2) {
                return 1;
            }
            final TimeZone tz1 = user1.getTimeZone();
            final TimeZone tz2 = user2.getTimeZone();
            if (null == tz1) {
                return null == tz2 ? 0 : -1;
            }
            return null == tz2 ? 1 : compareThem(tz1.getDisplayName(),tz2.getDisplayName());
        }}),
    PREMIUM_SERVICES(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    BADGES(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    WANTS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getWants(),user2.getWants());
        }}),
    HAVES(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getHaves(),user2.getHaves());
        }}),
    INTERESTS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getInterests(),user2.getInterests());
        }}),
    ORGANISATION_MEMBER(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            if (null == user1) {
                return null == user2 ? 0 : -1;
            }
            return null == user2 ? 1 : compareThem(user1.getOrganisationMember(),user2.getOrganisationMember());
        }}),
    LANGUAGES(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    PRIVATE_ADDRESS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    BUSINESS_ADDRESS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    WEB_PROFILES(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    INSTANT_MESSAGING_ACCOUNTS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    PROFESSIONAL_EXPERIENCE(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    EDUCATIONAL_BACKGROUND(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}),
    PHOTO_URLS(new Comparator<User>() {

        @Override
        public int compare(final User user1, final User user2) {
            return 0;
        }}), ;

    static <C extends Comparable<C>> int compareThem(final C c1, final C c2) {
        if (null == c1) {
            return null == c2 ? 0 : -1;
        }
        return null == c2 ? 1 : c1.compareTo(c2);
    }

    private final String fieldName;
    private final Comparator<User> comparator;

    private UserField(final Comparator<User> comparator) {
        this.fieldName = name().toLowerCase(Locale.US);
        this.comparator = comparator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Comparator<User> getComparator(final boolean descending) {
        return descending ? new InverseComparator<User>(comparator) : comparator;
    }
}
