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

package com.openexchange.user.json.comparator;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.json.Utility;
import com.openexchange.user.json.field.UserField;

/**
 * {@link Comparators} - TODO Short description of this class' purpose.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Comparators {

    /**
     * Initializes a new {@link Comparators}.
     */
    private Comparators() {
        super();
    }

    /**
     * The {@link Comparator} for user identifier.
     */
    public static final Comparator<User> COMPARATOR_ID = new Comparator<User>() {

        @Override
        public int compare(final User o1, final User o2) {
            final int firstVal = o1.getId();
            final int secondVal = o2.getId();
            return (firstVal < secondVal ? -1 : (firstVal == secondVal ? 0 : 1));
        }
    };

    private static final ConcurrentMap<Locale, Comparator<User>> LOGIN_INFO_COMPARATORS;

    private static final Map<UserField, Comparator<User>> COMPARATORS;

    static {
        final EnumMap<UserField, Comparator<User>> enumMap = new EnumMap<UserField, Comparator<User>>(UserField.class);
        enumMap.put(UserField.ID, COMPARATOR_ID);
        enumMap.put(UserField.ALIASES, new Comparator<User>() {

            @Override
            public int compare(final User o1, final User o2) {
                final int firstVal = o1.getId();
                final int secondVal = o2.getId();
                return (firstVal < secondVal ? -1 : (firstVal == secondVal ? 0 : 1));
            }
        });
        enumMap.put(UserField.GROUPS, new Comparator<User>() {

            @Override
            public int compare(final User o1, final User o2) {
                final int firstVal = o1.getId();
                final int secondVal = o2.getId();
                return (firstVal < secondVal ? -1 : (firstVal == secondVal ? 0 : 1));
            }
        });
        enumMap.put(UserField.TIME_ZONE, new Comparator<User>() {

            @Override
            public int compare(final User o1, final User o2) {
                final TimeZone timeZone1 = Utility.getTimeZone(o1.getTimeZone());
                final TimeZone timeZone2 = Utility.getTimeZone(o2.getTimeZone());
                if (timeZone1.equals(timeZone2)) {
                    return 0;
                }
                return timeZone1.getID().compareToIgnoreCase(timeZone2.getID());
            }
        });
        enumMap.put(UserField.LOCALE, new Comparator<User>() {

            @Override
            public int compare(final User o1, final User o2) {
                final Locale l1 = o1.getLocale();
                final Locale l2 = o2.getLocale();
                if (l1.equals(l2)) {
                    return 0;
                }
                return l1.toString().compareToIgnoreCase(l2.toString());
            }
        });
        enumMap.put(UserField.CONTACT_ID, new Comparator<User>() {

            @Override
            public int compare(final User o1, final User o2) {
                final int firstVal = o1.getContactId();
                final int secondVal = o2.getContactId();
                return (firstVal < secondVal ? -1 : (firstVal == secondVal ? 0 : 1));
            }
        });
        COMPARATORS = Collections.unmodifiableMap(enumMap);
        LOGIN_INFO_COMPARATORS = new ConcurrentHashMap<Locale, Comparator<User>>();
    }

    private static abstract class LocalizedComparator implements Comparator<User> {

        protected final Collator collator;

        /**
         * Initializes a new {@link LocalizedComparator}.
         *
         * @param locale The locale
         */
        protected LocalizedComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

    }

    private static class InvertedComparator<T> implements Comparator<T> {

        private final Comparator<T> comparator;

        public InvertedComparator(final Comparator<T> comparator) {
            super();
            this.comparator = comparator;
        }

        @Override
        public int compare(final T o1, final T o2) {
            return -1 * comparator.compare(o1, o2);
        }

    }

    /**
     * Gets the appropriate {@link Comparator} for given {@link UserField user field}.
     *
     * @param userField The user field
     * @param sessionLocale The session user's locale
     * @param descending <code>true</code> to sort in descending order; otherwise <code>false</code>
     * @return The appropriate {@link Comparator} for given {@link UserField user field} or <code>null</code>
     */
    public static Comparator<User> getComparator(final UserField userField, final Locale sessionLocale, final boolean descending) {
        final Comparator<User> comparator = COMPARATORS.get(userField);
        if (null != comparator) {
            return descending ? new InvertedComparator<User>(comparator) : comparator;
        }
        if (UserField.LOGIN_INFO.equals(userField)) {
            Comparator<User> loginInfoComp = LOGIN_INFO_COMPARATORS.get(sessionLocale);
            if (null == loginInfoComp) {
                final Comparator<User> newInst = new LocalizedComparator(sessionLocale) {

                    @Override
                    public int compare(final User o1, final User o2) {
                        return collator.compare(o1.getLoginInfo(), o2.getLoginInfo());
                    }
                };
                loginInfoComp = LOGIN_INFO_COMPARATORS.putIfAbsent(sessionLocale, newInst);
                if (null == loginInfoComp) {
                    loginInfoComp = newInst;
                }
            }
            return descending ? new InvertedComparator<User>(loginInfoComp) : loginInfoComp;
        }
        return null;
    }

}
