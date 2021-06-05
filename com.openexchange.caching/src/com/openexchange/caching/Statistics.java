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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link Statistics} - The common behavior for a statistics holder
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Statistics extends Serializable {

    /**
     * Return generic statistical or historical data.
     *
     * @return The generic statistical or historical data
     */
    public abstract StatisticElement[] getStatElements();

    /**
     * Set the generic statistical or historical data.
     *
     * @param stats The generic statistical or historical data
     */
    public abstract void setStatElements(StatisticElement[] stats);

    /**
     * Get the type name, such as "LRU Memory Cache." No formal type is defined.
     *
     * @return The type name
     */
    public abstract String getTypeName();

    /**
     * Set the type name, such as "LRU Memory Cache." No formal type is defined. If we need formal types, we can use the cache-type
     * parameter.
     *
     * @param name The type name
     */
    public abstract void setTypeName(String name);

}
