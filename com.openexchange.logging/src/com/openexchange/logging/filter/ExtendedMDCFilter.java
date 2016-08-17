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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.MDC;
import org.slf4j.Marker;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link ExtendedMDCFilter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExtendedMDCFilter extends TurboFilter {

    private final Set<String> whitelist;

    private final Set<Tuple> tuples;

    private final Map<String, Level> levels;

    /**
     * Initializes a new {@link ExtendedMDCFilter}.
     */
    public ExtendedMDCFilter(Set<String> whitelist) {
        super();
        this.whitelist = whitelist;
        tuples = new HashSet<Tuple>();
        levels = new HashMap<String, Level>();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable th) {
        boolean check = false;
        for (String allowedLogger : whitelist) {
            if (logger.getName().startsWith(allowedLogger)) {
                check = true;
                break;
            }
        }

        boolean loggerCheck = false;
        if (check) {
            for (Entry<String, Level> levelEntry : levels.entrySet()) {
                if (logger.getName().startsWith(levelEntry.getKey()) && level.levelInt >= levelEntry.getValue().levelInt) {
                    loggerCheck = true;
                    break;
                }
            }
        }

        if (check && loggerCheck) {
            for (Tuple t : tuples) {
                String v = MDC.get(t.getKey());
                if (v == null) {
                    return FilterReply.NEUTRAL;
                } else if (!v.equals(t.getValue())) {
                    return FilterReply.NEUTRAL;
                }
            }

            return FilterReply.ACCEPT;
        }

        return FilterReply.NEUTRAL;
    }

    /**
     * Adds a tuple for this filter
     *
     * @param k The tuple's key to query MDC map
     * @param v The tuple's expected value
     */
    public void addTuple(String k, String v) {
        tuples.add(new Tuple(k, v));
    }

    /**
     * Add a logger level for this filter
     *
     * @param loggerName
     * @param level
     */
    public void addLogger(String loggerName, Level level) {
        levels.put(loggerName, level);
    }

    /**
     * Remove a logger level from this filter
     *
     * @param loggerName
     */
    public void removeLogger(String loggerName) {
        levels.remove(loggerName);
    }

    public boolean hasLoggers() {
        return (levels.size() > 0);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append(", Loggers: [");
        for (Entry<String, Level> levelEntry : levels.entrySet()) {
            builder.append("\n\t").append(levelEntry.getKey()).append(" = ").append(levelEntry.getValue());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Nested {@link Tuple} class.
     */
    private class Tuple {

        private final String key;

        private final String value;

        /**
         * Initializes a new {@link Tuple}.
         *
         * @param k
         * @param v
         */
        public Tuple(String k, String v) {
            key = k;
            value = v;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Tuple other = (Tuple) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        /**
         * Gets the key
         *
         * @return The key
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the value
         *
         * @return The value
         */
        public String getValue() {
            return value;
        }

        /**
         * Get outer type
         *
         * @return
         */
        private ExtendedMDCFilter getOuterType() {
            return ExtendedMDCFilter.this;
        }
    }
}
