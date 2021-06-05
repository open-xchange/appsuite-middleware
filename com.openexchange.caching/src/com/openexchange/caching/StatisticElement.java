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

/**
 * {@link StatisticElement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface StatisticElement {

    /**
     * Gets the name of the statistic element, e.g. "HitCount"
     *
     * @return the statistic element name
     */
    public abstract String getName();

    /**
     * Sets the name of the statistic element, e.g. "HitCount"
     *
     * @param name
     */
    public abstract void setName(String name);

    /**
     * Get the data, e.g. for hit count you would get a {@link String} value for some number.
     *
     * @return The data as a string
     */
    public abstract String getData();

    /**
     * Set the data for this element as a string.
     *
     * @param data The data as a string
     */
    public abstract void setData(String data);
}
