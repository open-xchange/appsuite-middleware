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

package com.openexchange.logging;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.osgi.annotation.SingletonService;
import ch.qos.logback.classic.Level;

/**
 * {@link LogConfigurationService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
@SingletonService
public interface LogConfigurationService {

    /**
     * Creates a logging filter for the specified loggers in the given context.
     *
     * @param contextId The context identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse createContextFilter(int contextId, Map<String, Level> loggers);

    /**
     * Creates a logging filter for the specified loggers for the given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse createUserFilter(int userId, int contextId, Map<String, Level> loggers);

    /**
     * Creates a logging filter for the specified loggers for the given session
     *
     * @param sessionId The session identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse createSessionFilter(String sessionId, Map<String, Level> loggers);

    /**
     * Gets an unmodifiable {@link Set} with all logging filters
     *
     * @return An unmodifiable {@link Set} with all logging filters
     */
    Set<String> listFilters();

    /**
     * Checks whether any filter exists. Tries the following three combinations (in that order):
     * <ul>
     * <li>user session filter (sessionId)</li>
     * <li>user filter (contextId/userId)</li>
     * <li>context filter (contextId)</li>
     * </ul>
     *
     * @return <code>true</code> if any of the possible combinations yield to a logging filter;
     *         <code>false</code> otherwise.
     */
    boolean anyFilterExists(int contextId, int userId, String sessionId);

    /**
     * Removes the logging filter for the specified context and for the specified loggers
     *
     * @param contextId The context identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse removeContextFilter(int contextId, List<String> loggers);

    /**
     * Removes the logging filter for the specified user in the specified context and for the specified loggers
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse removeUserFilter(int contextId, int userId, List<String> loggers);

    /**
     * Removes the logging filter for the specified session and for the specified loggers
     *
     * @param sessionId The session identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse removeSessionFilter(String sessionId, List<String> loggers);

    /**
     * Removes all logging filters
     *
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse clearFilters();

    /**
     * Modifies the specified level for the specified loggers
     *
     * @param loggers The loggers for which to modify the levels
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse modifyLogLevels(Map<String, Level> loggers);

    /**
     * Overrides {@link Exception} categories to be suppressed (comma separated)
     *
     * @param categories The categories to suppress (TODO: Maybe use a {@link List}?)
     * @return A {@link LogResponse} with information about the outcome of the operation
     */
    LogResponse overrideExceptionCategories(String categories);

    /**
     * Returns a {@link Set} with all {@link Exception} categories
     *
     * @return a {@link Set} with all {@link Exception} categories
     */
    Set<String> listExceptionCategories();

    /**
     * Returns a {@link Set} with the names of all known loggers in the system
     *
     * @return a {@link Set} with the names of all known loggers in the system
     */
    Set<String> listLoggers();

    /**
     * Returns a {@link Set} with the names of all loggers along with their level that were dynamically modified.
     *
     * @return a {@link Set} with the names of all loggers along with their level that were dynamically modified.
     */
    Set<String> listDynamicallyModifiedLoggers();

    /**
     * Returns a {@link Set} with the level of the specified loggers
     *
     * @param loggers The logger names
     * @return a {@link Set} with the level of the specified loggers
     */
    Set<String> getLevelForLoggers(String[] loggers);

    /**
     * Sets whether to include stack-traces in HTTP API JSON responses for the specified user
     * in the specified context
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param enable whether or not to enable the stack-traces in the HTTP API
     */
    void includeStackTraceForUser(int contextId, int userId, boolean enable);

    /**
     * Returns an information string about all root appenders.
     *
     * @return an information string about all root appenders.
     */
    String getRootAppenderStats();
}
