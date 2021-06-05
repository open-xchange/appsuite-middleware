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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

/**
 * {@link Attribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface Attribute {

    /**
     * Gets the name of the SQL field
     *
     * @return The name
     */
    String getSQLFieldName();

    /**
     * Gets the name of the table in which this attribute is stored
     * 
     * @return The table's name
     */
    String getSQLTableName();

    /**
     * Gets the original type of the attribute's value
     *
     * @return The original type
     */
    Class<?> getOriginalType();

    /**
     * Returns the attribute's name
     * 
     * @return the name of the attribute
     */
    String getName();
}
