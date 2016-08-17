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

package com.openexchange.logging.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Marker;
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
        final String sep = System.getProperty("line.separator");
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
            } catch (final IndexOutOfBoundsException iobe) {
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
