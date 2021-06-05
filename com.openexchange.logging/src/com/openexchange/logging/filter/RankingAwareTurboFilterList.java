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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Marker;
import com.openexchange.java.Strings;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link RankingAwareTurboFilterList} - Accepts a list of {@link ExtendedTurboFilter filters} and executes them according to
 * {@link ExtendedTurboFilter#getRanking()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RankingAwareTurboFilterList extends TurboFilter {

    private final AtomicReference<List<ExtendedTurboFilter>> filtersRef;

    /**
     * Initializes a new {@link RankingAwareTurboFilterList}.
     */
    public RankingAwareTurboFilterList() {
        super();
        filtersRef = new AtomicReference<List<ExtendedTurboFilter>>(Collections.<ExtendedTurboFilter> emptyList());
        setName(RankingAwareTurboFilterList.class.getSimpleName());
    }

    /**
     * Adds given filter to this ranking-aware turbo filter list.
     *
     * @param filter The filter to add
     * @return <code>true</code> if added; otherwise <code>false</code>
     */
    public boolean addTurboFilter(final ExtendedTurboFilter filter) {
        if (null == filter) {
            return false;
        }
        boolean added;
        List<ExtendedTurboFilter> expected;
        List<ExtendedTurboFilter> list;
        do {
            expected = filtersRef.get();
            list = new ArrayList<ExtendedTurboFilter>(expected);
            if (list.contains(filter)) {
                return false;
            }
            added = list.add(filter);
            Collections.sort(list);
        } while (!filtersRef.compareAndSet(expected, list));

        return added;
    }

    /**
     * Removes given filter from this ranking-aware turbo filter list.
     *
     * @param filter The filter to remove
     * @return <code>true</code> if removed; otherwise <code>false</code>
     */
    public boolean removeTurboFilter(final ExtendedTurboFilter filter) {
        boolean removed;
        List<ExtendedTurboFilter> expected;
        List<ExtendedTurboFilter> list;
        do {
            expected = filtersRef.get();
            list = new ArrayList<ExtendedTurboFilter>(expected);
            removed = list.remove(filter);
            // No need to re-sort on remove
        } while (!filtersRef.compareAndSet(expected, list));

        return removed;
    }

    /**
     * Clears this ranking-aware turbo filter list.
     */
    public void clear() {
        filtersRef.set(Collections.<ExtendedTurboFilter> emptyList());
    }

    /**
     * Gets the size
     *
     * @return The size
     */
    public int size() {
        return filtersRef.get().size();
    }

    @Override
    public String getName() {
        final List<ExtendedTurboFilter> list = filtersRef.get();
        final int size = list.size();

        if (size == 0) {
            return super.getName();
        }

        if (size == 1) {
            final String name = list.get(0).getName();
            return null == name ? super.getName() : name;
        }

        final StringBuilder nameBuilder = new StringBuilder(size << 4);
        boolean added = false;
        String name = list.get(0).getName();
        if (name != null) {
            nameBuilder.append(name);
            added = true;
        }
        final String sep = Strings.getLineSeparator();
        for (int i = 1; i < size; i++) {
            name = list.get(i).getName();
            if (name != null) {
                if (added) {
                    nameBuilder.append(sep);
                }
                nameBuilder.append(name);
                added = true;
            }
        }
        return added ? nameBuilder.toString() : super.getName();
    }

    @Override
    public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format, final Object[] params, final Throwable t) {
        final List<ExtendedTurboFilter> list = filtersRef.get();
        final int size = list.size();

        if (size == 0) {
            return FilterReply.NEUTRAL;
        }

        if (size == 1) {
            try {
                final TurboFilter tf = list.get(0);
                return tf.decide(marker, logger, level, format, params, t);
            } catch (IndexOutOfBoundsException iobe) {
                return FilterReply.NEUTRAL;
            }
        }

        for (int i = 0; i < size; i++) {
            final FilterReply r = list.get(i).decide(marker, logger, level, format, params, t);
            if (r != FilterReply.NEUTRAL) {
                return r;
            }
        }
        return FilterReply.NEUTRAL;
    }

}
