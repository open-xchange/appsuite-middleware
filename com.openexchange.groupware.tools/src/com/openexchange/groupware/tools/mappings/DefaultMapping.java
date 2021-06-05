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

package com.openexchange.groupware.tools.mappings;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.java.Collators;

/**
 * {@link DefaultMapping} - Abstract {@link Mapping} implementation.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultMapping<T, O> implements Mapping<T, O> {

	@Override
	public boolean equals(final O object1, final O object2) {
		T value1 = this.get(object1);
		T value2 = this.get(object2);
		return null == value1 ? null == value2 : value1.equals(value2);
	}

	@Override
	public void copy(O from, O to) throws OXException {
		this.set(to, this.get(from));
	}

    /**
     * Default <code>truncate</code> implementation that never truncates,
     * override if applicable for the mapped property.
     */
    @SuppressWarnings("unused")
    @Override
    public boolean truncate(O object, int length) throws OXException {
        return false;
    }

    /**
     * Default <code>replaceAll</code> implementation that never replaces,
     * override if applicable for the mapped property.
     */
    @SuppressWarnings("unused")
    @Override
    public boolean replaceAll(O object, String regex, String replacement) throws OXException {
        return false;
    }

    /**
     * Default <code>compare</code> implementation, override if applicable for
     * the mapped property.
     */
	@Override
    public int compare(O o1, O o2) {
	    return this.compare(o1, o2, null);
    }

    /**
     * Default <code>compare</code> implementation, that uses locale-aware
     * comparison for {@link String}s properties. Override if applicable for
     * the mapped property.
     */
    @Override
    public int compare(O o1, O o2, Locale locale) {
        return compare(o1, o2, locale, null);
    }

    /**
     * Default <code>compare</code> implementation, that uses locale-aware
     * comparison for {@link String}s properties and ignores the timezone.
     * Override if applicable for the mapped property.
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "null" })
    @Override
    public int compare(O o1, O o2, Locale locale, TimeZone timeZone) {
        T value1 = this.get(o1);
        T value2 = this.get(o2);
        if (value1 == value2) {
            return 0;
        } else if (null == value1 && null != value2) {
            return -1;
        } else if (null == value2) {
            return 1;
        } else if (null != locale && String.class.isInstance(value1)) {
            return Collators.getDefaultInstance(locale).compare((String) value1, (String) value2);
        } else if (Comparable.class.isInstance(value1)) {
            return ((Comparable) value1).compareTo(value2);
        } else {
            throw new UnsupportedOperationException("Don't know how to compare two values of class " + value1.getClass().getName());
        }
    }

}
