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

import java.util.Optional;

/**
 * {@link LogConfiguration} - Defines the configuration for a dedicated logger,
 * i.e. a logger that logs on a separate file.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public interface LogConfiguration {

    /**
     * Returns whether the dedicated logging is enabled
     * 
     * @return <code>true</code> if dedicated logging is enabled; <code>false</code> otherwise
     */
    boolean isEnabledDedicatedLogging();

    /**
     * Returns the logger name.
     * 
     * @return the logger name
     */
    String getLoggerName();

    /**
     * Returns the optional log level of the logger
     * 
     * @return the optional log level of the logger
     */
    Optional<String> getLogLevel();

    /**
     * Returns the file location of the dedicated logger
     * 
     * @return the file location of the dedicated logger
     */
    String getLoggingFileLocation();

    /**
     * Returns the maximum file size of a dedicated log file
     * 
     * @return the maximum file size of a dedicated log file
     */
    int getLoggingFileLimit();

    /**
     * Returns the amount of maximum log files that are allowed to be created
     * from the dedicated file logger.
     * 
     * @return the amount
     */
    int getLoggingFileCount();

    /**
     * Returns the optional logging pattern
     * 
     * @return The optional logging pattern or <code>null</code> if the default one shall be used.
     */
    Optional<String> getLoggingPattern();
}
