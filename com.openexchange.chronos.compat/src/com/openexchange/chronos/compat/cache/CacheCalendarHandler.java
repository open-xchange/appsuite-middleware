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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.compat.cache;

import org.slf4j.LoggerFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarResult;

/**
 * {@link CacheCalendarHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CacheCalendarHandler implements CalendarHandler {

    public static final String REGION = "CalendarVolatileCache";

    private final CacheService cacheService;

    /**
     * Initializes a new {@link CacheCalendarHandler}.
     *
     * @param cacheService A reference to the cache service
     */
    public CacheCalendarHandler(CacheService cacheService) {
        super();
        this.cacheService = cacheService;
    }

    @Override
    public void handle(CalendarResult result) {
        try {
            if (needsInvalidation(result)) {
                Cache cache = cacheService.getCache(REGION);
                if (null != cache) {
                    cache.invalidateGroup(String.valueOf(result.getSession().getContext().getContextId()));
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(CacheCalendarHandler.class).error("Error invalidating legacy cache after calendar result: {} ", e.getMessage(), e);
        }
    }

    private static boolean needsInvalidation(CalendarResult result) {
        if (null != result) {
            return null != result.getCreations() && 0 < result.getCreations().size() ||
                null != result.getUpdates() && 0 < result.getUpdates().size() ||
                null != result.getDeletions() && 0 < result.getDeletions().size()
            ;
        }
        return false;
    }

}
