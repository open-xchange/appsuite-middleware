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

package com.openexchange.monitoring.impl.sockets.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Strings;
import com.openexchange.log.DedicatedFileLoggerFactory;
import com.openexchange.log.LogConfiguration;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.monitoring.sockets.exceptions.BlackListException;

/**
 * {@link SocketLoggerRegistryServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class SocketLoggerRegistryServiceImpl implements SocketLoggerRegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketLoggerRegistryServiceImpl.class);

    private final AtomicReference<ImmutableSet<String>> blackListRegistryReference;
    private final ConcurrentMap<String, Logger> loggerRegistry;
    private final Optional<Logger> dedicatedLogger;
    private final LogConfiguration logConfiguration;

    /**
     * Initializes a new {@link SocketLoggerRegistryServiceImpl}.
     */
    public SocketLoggerRegistryServiceImpl(LogConfiguration logConfiguration) {
        super();
        this.logConfiguration = logConfiguration;
        this.dedicatedLogger = DedicatedFileLoggerFactory.createOrReinitializeLogger(logConfiguration);
        this.loggerRegistry = new ConcurrentHashMap<>();
        this.blackListRegistryReference = new AtomicReference<ImmutableSet<String>>(ImmutableSet.of());
    }

    @Override
    public boolean isEmpty() {
        return loggerRegistry.isEmpty();
    }

    @Override
    public Optional<Logger> getLoggerFor(String name) {
        // Check name validity
        if (Strings.isEmpty(name)) {
            return Optional.empty();
        }

        // Prefer an exact match
        Logger exactMatch = loggerRegistry.get(name);
        if (exactMatch != null) {
            // Prefer dedicated logger
            return logConfiguration.isEnabledDedicatedLogging() ? dedicatedLogger : Optional.of(exactMatch);
        }

        // Try to find any registered loggers that their name maybe contained in the requested logger name
        for (Logger registeredLogger : loggerRegistry.values()) {
            if (name.contains(registeredLogger.getName())) {
                return logConfiguration.isEnabledDedicatedLogging() ? dedicatedLogger : Optional.of(registeredLogger);
            }
        }

        // No suitable logger found for given name
        return Optional.empty();
    }

    @Override
    public void unregisterLoggerFor(String name) {
        Logger logger = loggerRegistry.remove(name);
        if (logger == null) {
            LOGGER.info("No logger with name '{}' was found.", name);
            return;
        }
        LOGGER.info("Unregistered socket logger with name '{}'", name);
    }

    @Override
    public void unblacklistLoggerFor(String name) {
        boolean removed;
        ImmutableSet<String> expected;
        ImmutableSet<String> set;
        do {
            expected = blackListRegistryReference.get();
            Set<String> tmp = new LinkedHashSet<String>(expected);
            removed = tmp.remove(name);
            set = ImmutableSet.copyOf(tmp);
        } while (!blackListRegistryReference.compareAndSet(expected, set));

        if (removed) {
            LOGGER.info("Removed logger '{}' from the blacklist", name);
            return;
        }
        LOGGER.info("No logger with name '{}' was found in the blacklist.", name);
    }

    @Override
    public void registerLoggerFor(Class<?> clazz) throws BlackListException {
        register(clazz.getName(), LoggerFactory.getLogger(clazz));
    }

    @Override
    public void registerLoggerFor(String name) throws BlackListException {
        register(name, LoggerFactory.getLogger(name));
    }

    @Override
    public void blacklistLogger(Logger logger) {
        blacklistLogger(logger.getName());
    }

    @Override
    public void blacklistLogger(String name) {
        Logger logger = loggerRegistry.remove(name);
        if (logger != null) {
            LOGGER.warn("Black-listing previously registered logger with name '{}'", name);
        }

        boolean added;
        ImmutableSet<String> expected;
        ImmutableSet<String> set;
        do {
            expected = blackListRegistryReference.get();
            Set<String> tmp = new LinkedHashSet<String>(expected);
            added = tmp.add(name);
            set = ImmutableSet.copyOf(tmp);
        } while (!blackListRegistryReference.compareAndSet(expected, set));

        if (added) {
            LOGGER.info("Black-listed socket logger with name '{}'", name);
            return;
        }
        LOGGER.warn("A logger with name '{}' is already black-listed.", name);
    }

    @Override
    public Set<String> getAllLoggerNames() {
        return Collections.unmodifiableSet(loggerRegistry.keySet());
    }

    @Override
    public Set<String> getBlackListedLoggerNames() {
        return blackListRegistryReference.get();
    }

    /**
     * Registers the specified logger with the specified name.
     *
     * @param name The name of the logger
     * @param logger The logger
     * @throws BlackListException If the specified logger with the specified name is blacklisted.
     */
    private void register(String name, Logger logger) throws BlackListException {
        checkBlackList(name, logger);
        Logger raced = loggerRegistry.putIfAbsent(name, logger);
        if (raced == null) {
            LOGGER.info("Registered socket logger with name '{}'", name);
            return;
        }
        LOGGER.warn("A logger with name '{}' is already registered.", name);
    }

    /**
     * Checks whether the logger with the specified name is black-listed
     *
     * @param name The name of the logger
     * @param logger The actual logger instance
     * @throws BlackListException if the logger is black-listed
     */
    private void checkBlackList(String name, Logger logger) throws BlackListException {
        // Check for an exact match
        ImmutableSet<String> blackListRegistry = blackListRegistryReference.get();
        if (blackListRegistry.contains(name) || blackListRegistry.contains(logger.getName())) {
            LOGGER.warn("Logger '{}' with name '{}' is blacklisted.", logger.getName(), name);
            throw new BlackListException(logger.getName(), name);
        }
        // Check if the black-listed logger contains portions of the specified logger's name
        // e.g. if the black-listed logger is com.openexchange.database and the logger's name is
        //      com.openexchange.database.impl, then the later logger is considered to be black-listed.
        for (String item : blackListRegistry) {
            if (item.contains(name) || item.contains(logger.getName())) {
                LOGGER.warn("Logger '{}' with name '{}' is blacklisted.", logger.getName(), name);
                throw new BlackListException(logger.getName(), name);
            }
        }
    }
}
