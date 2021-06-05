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
