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

package com.openexchange.log;

import java.util.Comparator;

/**
 * {@link LogPropertyName} - A log property name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LogPropertyName implements Comparable<LogPropertyName> {

    /**
     * A property name's log level.
     */
    public static enum LogLevel {
        /**
         * The ALL log level.
         */
        ALL,
        /**
         * The TRACE log level
         */
        TRACE,
        /**
         * The DEBUG log level.
         */
        DEBUG,
        /**
         * The INFO log level.
         */
        INFO,
        /**
         * The WARNING log level.
         */
        WARNING,
        /**
         * The ERROR log level.
         */
        ERROR,
        /**
         * The FATAL log level.
         */
        FATAL,

        /*
         * Don't log this
         */
        OFF, ;


        /**
         * Gets the appropriate log level for specified naming.
         *
         * @param logLevel The log level naming
         * @return The appropriate log level
         */
        public static LogLevel logLevelFor(final String logLevel) {
            if (null == logLevel) {
                return ALL;
            }
            final LogLevel[] values = LogLevel.values();
            for (final LogLevel ll : values) {
                if (ll.name().equalsIgnoreCase(logLevel)) {
                    return ll;
                }
            }
            return ALL;
        }

		public boolean includes(LogLevel other) {
			if (this == OFF) {
				return false;
			}
			return other.ordinal() <= ordinal();
		}

		public static Comparator<LogLevel> getComparator() {
			return new Comparator<LogLevel>() {

				@Override
				public int compare(LogLevel o1, LogLevel o2) {
					return o1.ordinal() - o2.ordinal();
				}

			};
		}

    }

    private final LogProperties.Name propertyName;

    private final LogLevel logLevel;

    private final int hash;

    /**
     * Initializes a new {@link LogPropertyName}.
     *
     * @param propertyName The name
     * @param logLevel The log level when property shall be logged ("ALL","FINE","INFO","WARNING","ERROR")
     */
    public LogPropertyName(final LogProperties.Name propertyName, final LogLevel logLevel) {
        super();
        this.propertyName = propertyName;
        this.logLevel = null == logLevel ? LogLevel.ALL : logLevel;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LogPropertyName)) {
            return false;
        }
        final LogPropertyName other = (LogPropertyName) obj;
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        return true;
    }

    /**
     * Whether property name is applicable for any logging.
     *
     * @return <code>true</code> if property name is applicable for any logging, otherwise <code>false</code>
     */
    public boolean isAll() {
        return LogLevel.ALL.equals(logLevel);
    }

    /**
     * Whether property name is only applicable for ERROR logging.
     *
     * @return <code>true</code> if property name is only applicable for ERROR logging, otherwise <code>false</code>
     */
    public boolean isError() {
        return isAll() || LogLevel.ERROR.equals(logLevel);
    }

    /**
     * Whether property name is only applicable for WARNING logging.
     *
     * @return <code>true</code> if property name is only applicable for WARNING logging, otherwise <code>false</code>
     */
    public boolean isWarning() {
        return isAll() || LogLevel.WARNING.equals(logLevel);
    }

    /**
     * Whether property name is only applicable for INFO logging.
     *
     * @return <code>true</code> if property name is only applicable for INFO logging, otherwise <code>false</code>
     */
    public boolean isInfo() {
        return isAll() || LogLevel.INFO.equals(logLevel);
    }

    /**
     * Whether property name is only applicable for DEBUG logging.
     *
     * @return <code>true</code> if property name is only applicable for DEBUG logging, otherwise <code>false</code>
     */
    public boolean isDebug() {
        return isAll() || LogLevel.DEBUG.equals(logLevel);
    }

    /**
     * Checks if this name's log level implies specified log level
     *
     * @param logLevel The log level to check against
     * @return <code>true</code> if log level is implied; otherwise <code>false</code>
     */
    public boolean implies(final LogLevel logLevel) {
        return isAll() || this.logLevel.ordinal() <= logLevel.ordinal();
    }

    /**
     * Gets the log level.
     *
     * @return The log level
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Gets the property name.
     *
     * @return The property name
     */
    public LogProperties.Name getPropertyName() {
        return propertyName;
    }

    @Override
    public int compareTo(final LogPropertyName o) {
        return propertyName.getName().compareToIgnoreCase(o.propertyName.getName());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("LogPropertyName [");
        if (propertyName != null) {
            builder.append("propertyName=").append(propertyName).append(", ");
        }
        if (logLevel != null) {
            builder.append("logLevel=").append(logLevel);
        }
        builder.append(']');
        return builder.toString();
    }

}
