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

package com.openexchange.database;

import com.openexchange.exception.OXException;

/**
 * {@link AssignmentFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.1
 */
public interface AssignmentFactory {

    /**
     * Returns an {@link Assignment} based on the given context identifier
     *
     * @param contextId The context identifier to get an {@link Assignment} for
     * @return {@link Assignment} for the given parameters or <code>null</code> if no {@link Assignment} can be found
     * @throws OXException
     */
    Assignment get(int contextId) throws OXException;

    /**
     * Returns an {@link Assignment} based on the given schema name
     *
     * @param schemaName The schemaName to get an {@link Assignment} for
     * @return {@link Assignment} for the given parameters or <code>null</code> if no {@link Assignment} can be found
     * @throws OXException
     */
    Assignment get(String schemaName);

    /**
     * Reloads the previously read {@link Assignment}s
     *
     * @throws OXException
     */
    void reload() throws OXException;
}
