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

import java.util.List;
import java.util.Set;
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
    E[] getDifferentFields(O original, O update);

    /**
     * Determines the differences between one object and another one. Only <i>set</i> properties in the second object are considered.
     *
     * @param original The original object
     * @param update The updated object
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     * @return The different fields, or an empty set if there are no differences
     */
    Set<E> getDifferentFields(O original, O update, boolean considerUnset, E... ignoredFields);

	/**
     * Gets an array of all mapped fields that are set in the supplied object.
     *
     * @param object The object
     * @return The set fields
     */
	E[] getAssignedFields(O object);

    /**
     * Copies data from on object to another. Only <i>set</i> fields are transferred.
     *
     * @param from The source object
     * @param to The destination object, or <code>null</code> to copy into a newly created instance
     * @param fields The fields to copy, or <code>null</code> to copy all known field mappings
     * @return The copied object
     */
    O copy(O from, O to, E... fields) throws OXException;

    /**
     * Copies the data from a list of objects into a list of new objects. Only <i>set</i> fields are transferred.
     *
     * @param objects The source objects to copy
     * @param fields The fields to copy, or <code>null</code> to copy all known field mappings
     * @return The copied list of objects
     */
    List<O> copy(List<O> objects, E... fields) throws OXException;

}
