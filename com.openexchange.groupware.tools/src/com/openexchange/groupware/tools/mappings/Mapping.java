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

import java.util.Comparator;
import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link Mapping} - Generic operations for mapped object properties.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface Mapping<T, O> extends Comparator<O> {

	/**
	 * Gets a value indicating whether the mapped property is set in the
	 * supplied object or not. This is usually done by passing the result
	 * of the object's <code>containsXXX</code>-method.
	 *
	 * @param object the object
	 * @return <code>true</code>, if the property is set, <code>false</code>,
	 * otherwise.
	 */
	boolean isSet(O object);

	/**
	 * Sets the mapped property in the object to the given value.
	 *
	 * @param object the object to set the property for
	 * @param value the value to set
	 * @throws OXException
	 */
	void set(O object, T value) throws OXException;

	/**
	 * Gets the mapped property's value from a object.
	 *
	 * @param object the object to get the property value from
	 * @return the value
	 */
	T get(O object);

	/**
	 * Removes the property's value from an object.
	 *
	 * @param object the object to remove the property for
	 */
	void remove(O object);

    /**
     * Truncates the current property value to the supplied length if it is
     * longer.
     *
     * @param object the object to truncate the property's value for
     * @param length the maximum length the property's value should be
     * @return <code>true</code>, if the value was actually truncated,
     * <code>false</code>, otherwise
     */
    boolean truncate(O object, int length) throws OXException;

    /**
     * If this mapping denotes a textual property, replaces each substring of this property's value that matches the given regular
     * expression with the given replacement.
     *
     * @param object The object to replace substrings in the property's value for
     * @param regex The regular expression to which this property's value is to be matched
     * @param replacement The string to be substituted for each match
     * @return <code>true</code>, if the replacements were actually performed, <code>false</code>, otherwise
     */
    boolean replaceAll(O object, String regex, String replacement) throws OXException;

	/**
	 * Gets a value indicating whether a property's value is equal in two
	 * objects or not.
	 *
	 * @param object1 the first object for comparison
	 * @param object2 the second object for comparison
	 * @return
	 */
	boolean equals(O object1, O object2);

	/**
	 * Copies the value of a property in one object to another one.
	 *
	 * @param from the object to read the property value from
	 * @param to the object to set the property
	 * @throws OXException
	 */
	void copy(O from, O to) throws OXException;

	/**
     * Compares the supplied objects for order, respecting the given locale
     * when defined. Returns a negative integer, zero, or a positive integer
     * as the first argument is less than, equal to, or greater than the
     * second.
	 *
	 * @param o1 the first object for comparison
	 * @param o2 the second object for comparison
	 * @param locale the Java locale, or <code>null</code> if not defined
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
	 */
    int compare(O o1, O o2, Locale locale);
}
