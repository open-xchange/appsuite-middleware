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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Marker;
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
        final String sep = System.getProperty("line.separator");
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
