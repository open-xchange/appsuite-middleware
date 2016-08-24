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

import com.openexchange.exception.OXException;

/**
 * {@link Mapper} - Generic mapper definition for field-wise operations on objects
 *
 * @param <O> the type of the object
 * @param <E> the enum type for the fields
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface Mapper<O, E extends Enum<E>> extends Factory<O>, ArrayFactory<E> {

	/**
	 * Gets a mapping for the supplied field.
	 *
	 * @param field the field
	 * @return the mapping
	 * @throws OXException
	 */
	Mapping<? extends Object, O> get(E field) throws OXException;

	/**
     * Gets an optional mapping for the supplied field.
	 *
	 * @param field the field
	 * @return the mapping, or <code>null</code> if no mapping is available
	 */
    Mapping<? extends Object, O> opt(final E field);

	/**
	 * Merges all differences that are set in the updated object into the
	 * original one.
	 *
	 * @param original the object to merge the differences into
	 * @param update the {@link O} containing the changes
	 * @throws OXException
	 */
	void mergeDifferences(final O original, final O update) throws OXException;

	/**
	 * Creates a new object and sets all those properties that are different
	 * in the supplied object to the values from the second one, thus,
	 * generating some kind of a 'delta' object.
	 *
	 * @param original the original object
	 * @param update the updated object
	 * @return an object containing the properties that are different
	 * @throws OXException
	 */
	O getDifferences(final O original, final O update) throws OXException;

    /**
     * Determines the differences between one object and another one. Only <i>set</i> properties in the second object are considered.
     *
     * @param original The original object
     * @param update The updated object
     * @return The different fields, or an empty array if there are no differences
     */
    E[] getDifferentFields(O original, O update) throws OXException;

	/**
	 * Gets an array of all mapped fields that are set in the supplied object.
	 *
	 * @param object the object
	 * @return the set fields
	 */
	E[] getAssignedFields(O object);

    /**
     * Copies data from on object to another. Only <i>set</i> fields are transferred.
     *
     * @param from The source object
     * @param to The destination object
     * @param fields The fields to copy
     */
    void copy(O from, O to, E... fields) throws OXException;

}
