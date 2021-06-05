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

import java.util.List;

/**
 * {@link SimpleCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface SimpleCollectionUpdate<O> {

    /**
     * Gets the list of newly added items.
     *
     * @return The added items, or an empty list if there are none
     */
    List<O> getAddedItems();

    /**
     * Gets the list of removed items.
     *
     * @return The removed items, or an empty list if there are none
     */
    List<O> getRemovedItems();

    /**
     * Gets a value indicating whether the collection update is empty, i.e. if there were any kind of changes or not.
     *
     * @return <code>true</code> if there were no changes at all, <code>false</code>, otherwise
     */
    boolean isEmpty();

}

