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

package com.openexchange.groupware.tools.mappings.common;

import java.util.Set;

/**
 * {@link ItemUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface ItemUpdate<O, E extends Enum<E>> {

    /**
     * Gets the original item.
     *
     * @return The original item
     */
    O getOriginal();

    /**
     * Gets the updated item.
     *
     * @return The updated item
     */
    O getUpdate();

    /**
     * Gets the fields that were modified through the update.
     *
     * @return The updated fields
     */
    Set<E> getUpdatedFields();

    /**
     * Gets a value indicating whether at least one of the specified fields has been modified through the update operation.
     *
     * @param fields The event fields to check
     * @return <code>true</code> if at least one field was updated, <code>false</code>, otherwise
     */
    boolean containsAnyChangeOf(E[] fields);

    /**
     * Gets a value indicating whether the item update is empty or not, i.e. if no fields were changed at all.
     *
     * @return <code>true</code> if the update is empty, <code>false</code>, otherwise
     */
    default boolean isEmpty() {
        Set<E> updatedFields = getUpdatedFields();
        return null == updatedFields || updatedFields.isEmpty();
    }

}
