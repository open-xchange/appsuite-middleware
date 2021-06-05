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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Marker;
import com.openexchange.java.Strings;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link TurboFilterCache} - Manages {@link TurboFilter} instances that perform filtering of log events based on user/context/session.
 * <p>
 * Moreover it signals a higher ranking than default to let that filter be executed in first place.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TurboFilterCache extends ExtendedTurboFilter {

    private final ConcurrentMap<String, TurboFilter> map;

    /**
     * Initializes a new {@link TurboFilterCache}.
     */
    public TurboFilterCache() {
        super();
        map = new ConcurrentHashMap<String, TurboFilter>();
    }

    public void clear() {
        map.clear();
    }

    public void put(final String key, final TurboFilter filter) {
        map.put(key, filter);
    }

    public boolean putIfAbsent(final String key, final TurboFilter filter) {
        return (null == map.putIfAbsent(key, filter));
    }

    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

    public void remove(final String key) {
        map.remove(key);
    }

    public TurboFilter get(final String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public int getRanking() {
        // Return a higher ranking than default (0)
        return 10;
    }

    @Override
    public String getName() {
        if (map.isEmpty()) {
            return super.getName();
        }

        final List<TurboFilter> filters = new ArrayList<TurboFilter>(map.values());
        final int size = filters.size();

        if (size == 1) {
            final String name = filters.get(0).getName();
            return null == name ? super.getName() : name;
        }

        final StringBuilder nameBuilder = new StringBuilder(size << 4);
        boolean added = false;
        String name = filters.get(0).getName();
        if (null != name) {
            nameBuilder.append(name);
            added = true;
        }
        final String sep = Strings.getLineSeparator();
        for (int i = 1; i < size; i++) {
            name = filters.get(i).getName();
            if (null != name) {
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
    public FilterReply decide(final Marker marker, final ch.qos.logback.classic.Logger logger, final Level level, final String format, final Object[] params, final Throwable t) {
        for (final TurboFilter tf : map.values()) {
            final FilterReply r = tf.decide(marker, logger, level, format, params, t);
            if (r != FilterReply.NEUTRAL) {
                return r;
            }
        }
        return FilterReply.NEUTRAL;
    }
}
