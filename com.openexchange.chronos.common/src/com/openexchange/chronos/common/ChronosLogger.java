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

package com.openexchange.chronos.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChronosLogger}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ChronosLogger {

    public static final String LOGGER_NAME = "chronos-logger";

    public static final Logger LOGGER = LoggerFactory.getLogger(LOGGER_NAME);

    /**
     * Avoid instantiation.
     * Initializes a new {@link ChronosLogger}.
     */
    private ChronosLogger() {}

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void debug(String message, Object arg0) {
        LOGGER.debug(message, arg0);
    }

    public static void debug(String message, Object arg0, Object arg1) {
        LOGGER.debug(message, arg0, arg1);
    }

    public static void debug(String message, Object... args) {
        LOGGER.debug(message, args);
    }

    public static boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }
}
