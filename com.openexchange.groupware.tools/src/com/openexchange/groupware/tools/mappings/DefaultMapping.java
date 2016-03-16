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

package com.openexchange.groupware.tools.mappings;

import java.util.Locale;
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
    @Override
    public boolean truncate(O object, int length) throws OXException {
        return false;
    }

    /**
     * Default <code>replaceAll</code> implementation that never replaces,
     * override if applicable for the mapped property.
     */
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
        T value1 = this.get(o1);
        T value2 = this.get(o2);
        if (value1 == value2) {
            return 0;
        } else if (null == value1 && null != value2) {
            return -1;
        } else if (null == value2) {
            return 1;
        } else if (null != locale && String.class.isInstance(value1)) {
            return Collators.getDefaultInstance(locale).compare((String)value1, (String)value2);
        } else if (Comparable.class.isInstance(value1)) {
            return ((Comparable)value1).compareTo(value2);
        } else {
            throw new UnsupportedOperationException("Don't know how to compare two values of class " + value1.getClass().getName());
        }
    }

}
