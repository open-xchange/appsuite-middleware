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
