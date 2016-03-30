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

package com.openexchange.java;

import java.text.Collator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class for storing {@link Collator} instances that avoids excessive locking when calling {@link Collator#getInstance(Locale)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Collators {

    private static final class CollatorKey {

        private final int strength;
        private final Locale locale;

        CollatorKey(Locale locale, int strength) {
            super();
            this.locale = locale;
            this.strength = strength;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            result = prime * result + strength;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CollatorKey)) {
                return false;
            }
            CollatorKey other = (CollatorKey) obj;
            if (strength != other.strength) {
                return false;
            }
            if (locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!locale.equals(other.locale)) {
                return false;
            }
            return true;
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    /**
     * The collator cache.
     */
    private static final ConcurrentMap<CollatorKey, Collator> CACHE = new ConcurrentHashMap<CollatorKey, Collator>(24, 0.9f, 1);

    /**
     * No instantiation
     */
    private Collators() {
        super();
    }

    /**
     * Gets the default {@link Collator} instance (strength set to {@link Collator#TERTIARY}) for given locale.
     *
     * @param locale The locale
     * @return The {@link Collator} instance
     */
    public static Collator getDefaultInstance(final Locale locale) {
        return getInstance(locale, Collator.TERTIARY);
    }

    /**
     * Gets the {@link Collator} instance with strength set to {@link Collator#SECONDARY} for given locale.
     *
     * @param locale The locale
     * @return The {@link Collator} instance
     */
    public static Collator getSecondaryInstance(final Locale locale) {
        return getInstance(locale, Collator.SECONDARY);
    }

    /**
     * Gets the {@link Collator} instance with specified strength for given locale.
     *
     * @param locale The locale
     * @param strength The collator strength
     * @return The {@link Collator} instance
     * @see Collator#PRIMARY
     * @see Collator#SECONDARY
     * @see Collator#TERTIARY
     */
    public static Collator getInstance(final Locale locale, final int strength) {
        CollatorKey key = new CollatorKey(locale, strength);
        Collator c = CACHE.get(key);
        if (null == c) {
            final Collator collator = Collator.getInstance(locale);
            collator.setStrength(strength);
            c = CACHE.putIfAbsent(key, collator);
            if (null == c) {
                c = collator;
            }
        }
        return (Collator) c.clone();
    }

}
