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

/**
 * {@link CacheInformationMBean} - The MBean for cache information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CacheInformationMBean {

    public static final String CACHE_DOMAIN = "com.openexchange.caching";

    /**
     * Gets the names of all available cache regions as an array of {@link String}
     *
     * @return The names of all available cache regions as an array of {@link String}
     */
    public String[] listRegionNames();

    /**
     * Gets the number of elements contained in specified region's memory cache
     *
     * @param name The region name
     * @return The number of elements contained in specified region's memory cache
     */
    public long getMemoryCacheCount(String name);

    /**
     * Gets the data gathered for this region and all the auxiliaries it currently uses.
     *
     * @param name The region name or "*" to return statistics for all regions
     * @return The data gathered for this region and all the auxiliaries it currently uses.
     */
    public String getCacheStatistics(String name);

    /**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non serializable objects in the region, the count
     * will stop when it encounters the first one.
     *
     * @param name The region name
     * @return The estimated data size in bytes
     */
    public long getMemoryCacheDataSize(String name);
}
