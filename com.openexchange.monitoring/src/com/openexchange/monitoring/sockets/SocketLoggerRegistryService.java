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

package com.openexchange.monitoring.sockets;

import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.monitoring.sockets.exceptions.BlackListException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link SocketLoggerRegistryService} - A registry to manage registered and black-listed loggers.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
@SingletonService
public interface SocketLoggerRegistryService {

    /**
     * Unregisters a logger with the specified name
     *
     * @param name The name of the logger
     */
    void unregisterLoggerFor(String name);

    /**
     * Registers a new logger for the specified {@link Class}
     *
     * @param clazz The {@link Class} for which to register the logger
     */
    void registerLoggerFor(Class<?> clazz) throws BlackListException;

    /**
     * Registers a new logger with the specified name
     *
     * @param name The name of the new {@link Logger}
     */
    void registerLoggerFor(String name) throws BlackListException;

    /**
     * Blacklists the logger with the specified name. If a logger
     * was already registered, unregisters it and then black lists it.
     *
     * @param name The logger name to blacklist
     */
    void blacklistLogger(String name);

    /**
     * Blacklists the specified logger. If a logger
     * was already registered, unregisters it and then black lists it.
     *
     * @param logger The logger blacklist
     */
    void blacklistLogger(Logger logger);

    /**
     * Removes the blacklisted logger with the specified name
     *
     * @param name The name of the logger to remove from the blacklist
     */
    void unblacklistLoggerFor(String name);

    /**
     * Checks if this registry is currently empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    boolean isEmpty();

    /**
     * Returns a {@link Logger} instance with the specified name
     *
     * @param name The name of the {@link Logger} to return
     * @return an {@link Optional} {@link Logger} instance with the specified name,
     */
    Optional<Logger> getLoggerFor(String name);

    /**
     * Returns an unmodifiable {@link Set} with all registered logger names
     *
     * @return an unmodifiable {@link Set} with all registered logger names
     */
    Set<String> getAllLoggerNames();

    /**
     * Returns an unmodifiable {@link Set} with all currently black-listed logger names
     *
     * @return an unmodifiable {@link Set} with all black-listed logger names
     */
    Set<String> getBlackListedLoggerNames();
}
