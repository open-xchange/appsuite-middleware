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
