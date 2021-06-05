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

package com.openexchange.caching.internal.cache2jcs;

import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.engine.stats.behavior.IStats;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.jcs2cache.JCSStatsDelegator;

/**
 * {@link CacheStatistics2JCS} - The {@link CacheStatistics} implementation backed by a {@link ICacheStats} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheStatistics2JCS extends Statistics2JCS implements CacheStatistics {

    private static final long serialVersionUID = 8335990894723060160L;

    /**
     * Initializes a new {@link CacheStatistics2JCS}
     *
     * @param cacheStats The {@link ICacheStats} object to delegate to
     */
    public CacheStatistics2JCS(final ICacheStats cacheStats) {
        super(cacheStats);
    }

    @Override
    public Statistics[] getAuxiliaryCacheStats() {
        final IStats[] stats = ((ICacheStats) super.stats).getAuxiliaryCacheStats();
        if (stats == null) {
            return null;
        }
        final Statistics[] retval = new Statistics[stats.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new Statistics2JCS(stats[i]);
        }
        return retval;
    }

    @Override
    public String getRegionName() {
        return ((ICacheStats) super.stats).getRegionName();
    }

    @Override
    public void setAuxiliaryCacheStats(final Statistics[] statistics) {
        if (statistics == null) {
            ((ICacheStats) super.stats).setAuxiliaryCacheStats(null);
            return;
        }
        final Stats[] stats = new Stats[statistics.length];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new JCSStatsDelegator(statistics[i]);
        }
        ((ICacheStats) super.stats).setAuxiliaryCacheStats(stats);
    }

    @Override
    public void setRegionName(final String name) {
        ((ICacheStats) super.stats).setRegionName(name);
    }

    @Override
    public String toString() {
        return stats.toString();
    }

}
