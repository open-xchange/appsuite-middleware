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

package com.openexchange.logging.filter;

import ch.qos.logback.classic.turbo.TurboFilter;

/**
 * {@link ExtendedTurboFilter} - extends {@link TurboFilter} by {@link #getRanking()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ExtendedTurboFilter extends TurboFilter implements Comparable<ExtendedTurboFilter> {

    /** The default ranking <code>0</code> (zero) */
    public static final int DEFAULT_RANKING = 0;

    /**
     * Initializes a new {@link ExtendedTurboFilter}.
     */
    protected ExtendedTurboFilter() {
        super();
    }

    /**
     * Gets the ranking of this filter.
     * <p>
     * The ranking is used to determine the <i>natural order</i> of filters.
     * <p>
     * A filter with a ranking of <code>Integer.MAX_VALUE</code> is very likely to be returned as the dominating filter, whereas a filter
     * with a ranking of <code>Integer.MIN_VALUE</code> is very unlikely to become effective.
     *
     * @return The ranking
     */
    public int getRanking() {
        return DEFAULT_RANKING;
    }

    @Override
    public int compareTo(final ExtendedTurboFilter o) {
        final int thisVal = this.getRanking();
        final int anotherVal = o.getRanking();
        return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1)); // Highest first
    }

}
