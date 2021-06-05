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

import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.caching.internal.jcs2cache.JCSStatElementDelegator;

/**
 * {@link Statistics2JCS} - The {@link Statistics} implementation backed by a {@link IStats} object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Statistics2JCS implements Statistics {

    private static final long serialVersionUID = -9146499976597779961L;

    protected final IStats stats;

    /**
     * Initializes a new {@link Statistics2JCS}
     *
     * @param stats The {@link IStats} object to delegate to
     */
    public Statistics2JCS(final IStats stats) {
        super();
        this.stats = stats;
    }

    @Override
    public StatisticElement[] getStatElements() {
        final IStatElement[] elems = stats.getStatElements();
        if (elems == null) {
            return null;
        }
        final StatisticElement[] retval = new StatisticElement[elems.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new StatisticElement2JCS(elems[i]);
        }
        return retval;
    }

    @Override
    public String getTypeName() {
        return stats.getTypeName();
    }

    @Override
    public void setStatElements(final StatisticElement[] statisticElements) {
        if (statisticElements == null) {
            stats.setStatElements(null);
            return;
        }
        final IStatElement[] elems = new IStatElement[statisticElements.length];
        for (int i = 0; i < elems.length; i++) {
            elems[i] = new JCSStatElementDelegator(statisticElements[i]);
        }
        stats.setStatElements(elems);
    }

    @Override
    public void setTypeName(final String name) {
        stats.setTypeName(name);
    }

}
