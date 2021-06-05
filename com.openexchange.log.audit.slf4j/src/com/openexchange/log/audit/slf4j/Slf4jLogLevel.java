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

package com.openexchange.log.audit.slf4j;

import org.slf4j.spi.LocationAwareLogger;
import com.openexchange.java.Strings;

/**
 * {@link Slf4jLogLevel}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum Slf4jLogLevel {

    TRACE("trace", LocationAwareLogger.TRACE_INT),
    DEBUG("debug", LocationAwareLogger.DEBUG_INT),
    INFO("info", LocationAwareLogger.INFO_INT),
    WARN("warn", LocationAwareLogger.WARN_INT),
    ERROR("error", LocationAwareLogger.ERROR_INT),
    ;

    private final String id;
    private final int level;

    private Slf4jLogLevel(String id, int level) {
        this.id = id;
        this.level = level;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the SLF4J log level
     *
     * @return The SLF4J log level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the SLF4J log level for specified identifier.
     *
     * @param id The identifier
     * @return The SLF4J log level or <code>null</code>
     */
    public static Slf4jLogLevel valueFor(String id) {
        if (null == id) {
            return null;
        }

        String tmp = Strings.asciiLowerCase(id);
        for (Slf4jLogLevel logLevel : Slf4jLogLevel.values()) {
            if (tmp.equals(logLevel.id)) {
                return logLevel;
            }
        }
        return null;
    }

}
