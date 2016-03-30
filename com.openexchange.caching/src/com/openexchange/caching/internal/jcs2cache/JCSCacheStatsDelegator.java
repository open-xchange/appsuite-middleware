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

package com.openexchange.caching.internal.jcs2cache;

import org.apache.jcs.engine.stats.CacheStats;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.cache2jcs.StatisticElement2JCS;
import com.openexchange.caching.internal.cache2jcs.Statistics2JCS;

/**
 * {@link JCSCacheStatsDelegator} - The {@link CacheStats} subclass which delegates to {@link CacheStatistics} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCacheStatsDelegator extends CacheStats {

    private static final long serialVersionUID = 1343560248052716010L;

    private final CacheStatistics cacheStatistics;

    /**
     * Initializes a new {@link JCSCacheStatsDelegator}
     *
     * @param cacheStatistics The {@link CacheStatistics} object to delegate to
     */
    public JCSCacheStatsDelegator(final CacheStatistics cacheStatistics) {
        super();
        this.cacheStatistics = cacheStatistics;
    }

    @Override
    public String getRegionName() {
        return cacheStatistics.getRegionName();
    }

    @Override
    public void setRegionName(final String name) {
        cacheStatistics.setRegionName(name);
    }

    @Override
    public IStats[] getAuxiliaryCacheStats() {
        final Statistics[] statistics = cacheStatistics.getAuxiliaryCacheStats();
        if (statistics == null) {
            return null;
        }
        final Stats[] retval = new Stats[statistics.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new JCSStatsDelegator(statistics[i]);
        }
        return retval;
    }

    @Override
    public void setAuxiliaryCacheStats(final IStats[] stats) {
        if (stats == null) {
            cacheStatistics.setAuxiliaryCacheStats(null);
            return;
        }
        final Statistics[] statistics = new Statistics[stats.length];
        for (int i = 0; i < statistics.length; i++) {
            statistics[i] = new Statistics2JCS(stats[i]);
        }
        cacheStatistics.setAuxiliaryCacheStats(statistics);
    }

    @Override
    public IStatElement[] getStatElements() {
        final StatisticElement[] elements = cacheStatistics.getStatElements();
        if (elements == null) {
            return null;
        }
        final StatElement[] stats = new StatElement[elements.length];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new JCSStatElementDelegator(elements[i]);
        }
        return stats;
    }

    @Override
    public void setStatElements(final IStatElement[] stats) {
        if (stats == null) {
            cacheStatistics.setStatElements(null);
            return;
        }
        final StatisticElement[] elements = new StatisticElement[stats.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new StatisticElement2JCS(stats[i]);
        }
        cacheStatistics.setStatElements(elements);
    }
}
