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
 * {@link CacheStatistics} - Holds statistical information on a region for both auxiliary and core statistics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CacheStatistics extends Statistics {

    /**
     * Statistics are for a region, though auxiliary data may be for more.
     *
     * @return The region name
     */
    public abstract String getRegionName();

    /**
     * Sets the region name to which this cache statistics apply
     *
     * @param name The region name
     */
    public abstract void setRegionName(String name);

    /**
     * Gets the auxiliary cache statistics
     *
     * @return The auxiliary cache statistics
     */
    public abstract Statistics[] getAuxiliaryCacheStats();

    /**
     * Sets the auxiliary cache statistics
     *
     * @param stats The auxiliary cache statistics
     */
    public abstract void setAuxiliaryCacheStats(Statistics[] stats);

}
