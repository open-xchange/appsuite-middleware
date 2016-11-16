/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
