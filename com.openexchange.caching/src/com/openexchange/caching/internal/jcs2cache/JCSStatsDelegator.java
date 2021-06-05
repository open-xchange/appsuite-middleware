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

import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.cache2jcs.StatisticElement2JCS;

/**
 * {@link JCSStatsDelegator} - The {@link Stats} subclass backed by a {@link Statistics} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSStatsDelegator extends Stats {

    private static final long serialVersionUID = -1350875341961863476L;

    private final Statistics statistics;

    /**
     * Initializes a new {@link JCSStatsDelegator}
     *
     * @param statistics The {@link Statistics} object to delegate to
     */
    public JCSStatsDelegator(final Statistics statistics) {
        super();
        this.statistics = statistics;
    }

    @Override
    public IStatElement[] getStatElements() {
        final StatisticElement[] statisticElements = statistics.getStatElements();
        if (statisticElements == null) {
            return null;
        }
        final StatElement[] retval = new StatElement[statisticElements.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new JCSStatElementDelegator(statisticElements[i]);
        }
        return retval;
    }

    @Override
    public void setStatElements(final IStatElement[] stats) {
        if (stats == null) {
            statistics.setStatElements(null);
            return;
        }
        final StatisticElement[] statisticElements = new StatisticElement[stats.length];
        for (int i = 0; i < statisticElements.length; i++) {
            statisticElements[i] = new StatisticElement2JCS(stats[i]);
        }
        statistics.setStatElements(statisticElements);
    }

    @Override
    public String getTypeName() {
        return statistics.getTypeName();
    }

    @Override
    public void setTypeName(final String name) {
        statistics.setTypeName(name);
    }

}
