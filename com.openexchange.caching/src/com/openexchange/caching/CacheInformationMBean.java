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

import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link CacheInformationMBean} - The MBean for cache information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CacheInformationMBean {

    /** The domain name for cache MBean */
    public static final String CACHE_DOMAIN = "com.openexchange.caching";

    /**
     * Gets the names of all available cache regions as an array of {@link String}
     *
     * @return The names of all available cache regions as an array of {@link String}
     */
    @MBeanMethodAnnotation (description="Gets the names of all available cache regions", parameters={}, parameterDescriptions={})
    String[] listRegionNames();

    /**
     * Gets the number of elements contained in specified region's memory cache
     *
     * @param name The region name
     * @return The number of elements contained in specified region's memory cache
     * @throws MBeanException If region name is invalid or unknown
     */
    @MBeanMethodAnnotation (description="Gets the number of elements contained in specified region's memory cache", parameters={"name"}, parameterDescriptions={"The region name"})
    long getMemoryCacheCount(String name) throws MBeanException;

    /**
     * Gets the data gathered for specified region and all the auxiliaries it currently uses.
     *
     * @param name The region name or "*" to return statistics for all regions
     * @return The data gathered for this region and all the auxiliaries it currently uses.
     * @throws MBeanException If region name is invalid or unknown
     */
    @MBeanMethodAnnotation (description="Gets the data gathered for specified region and all the auxiliaries it currently uses.", parameters={"name"}, parameterDescriptions={"The region name or \"*\" to return statistics for all regions"})
    String getCacheStatistics(String name) throws MBeanException;

    /**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non serializable objects in the region, the count
     * will stop when it encounters the first one.
     *
     * @param name The region name
     * @return The estimated data size in bytes
     * @throws MBeanException If region name is invalid or unknown
     */
    @MBeanMethodAnnotation (description="Tries to estimate how much data is in a region.", parameters={"name"}, parameterDescriptions={"The region name"})
    long getMemoryCacheDataSize(String name) throws MBeanException;

    /**
     * Clears the specified cache region
     *
     * @param name The region name
     * @param localOnly <code>true</code> to only clear local cache (on connected node); otherwise to flush cache in whole cluster
     * @return The estimated data size in bytes
     * @throws MBeanException If clear attempt fails or region name is invalid/unknown
     */
    @MBeanMethodAnnotation (description="Clears the specified cache region.", parameters={"name"}, parameterDescriptions={"The region name or \"*\" to clear all regions"})
    void clear(String name, boolean localOnly) throws MBeanException;
}
