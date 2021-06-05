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

package com.openexchange.folderstorage;

import java.io.Serializable;

/**
 * {@link ContentType} - The content type (aka the module) of a folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ContentType extends Serializable {

    /**
     * Gets this content type's module identifier.
     * <p>
     * This method is mainly for migration convenience.
     *
     * @return The module identifier
     */
    int getModule();

    /**
     * Returns a string representation of this content type.
     *
     * @return A string representation of this content type
     */
    @Override
    String toString();

    /**
     * Gets the priority.
     *
     * @return The priority.
     */
    int getPriority();

}
