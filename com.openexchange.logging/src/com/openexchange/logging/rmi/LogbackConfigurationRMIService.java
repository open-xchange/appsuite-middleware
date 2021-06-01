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

package com.openexchange.logging.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.logging.LogResponse;
import ch.qos.logback.classic.Level;

/**
 * {@link LogbackConfigurationRMIService} - A remote interface service used to
 * setup logging filters for users, contexts, sessions and adjust the log levels
 * of different loggers on runtime.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface LogbackConfigurationRMIService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "LogbackConfigurationRMIService";

    /**
     * Creates a logging filter for the specified loggers in the specified context
     * 
     * @param contextId The context identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse filterContext(int contextId, Map<String, Level> loggers) throws RemoteException;

    /**
     * Creates a logging filter for the specified loggers for the specified user in the specified context
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse filterUser(int contextId, int userId, Map<String, Level> loggers) throws RemoteException;

    /**
     * Creates a logging filter for the specified loggers for the specified session
     * 
     * @param sessionId The session identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse filterSession(String sessionId, Map<String, Level> loggers) throws RemoteException;

    /**
     * Returns a {@link Set} with all logging filters
     * 
     * @return a {@link Set} with all logging filters
     * @throws RemoteException if an error is occurred
     */
    Set<String> listFilters() throws RemoteException;

    /**
     * Removes the logging filter for the specified context and for the specified loggers
     * 
     * @param contextId The context identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse removeContextFilter(int contextId, List<String> loggers) throws RemoteException;

    /**
     * Removes the logging filter for the specified user in the specified context and for the specified loggers
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse removeUserFilter(int contextId, int userId, List<String> loggers) throws RemoteException;

    /**
     * Removes the logging filter for the specified session and for the specified loggers
     * 
     * @param sessionId The session identifier
     * @param loggers The logger names
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse removeSessionFilter(String sessionId, List<String> loggers) throws RemoteException;

    /**
     * Removes all logging filters
     * 
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse clearFilters() throws RemoteException;

    /**
     * Modifies the specified level for the specified loggers
     * 
     * @param loggers The loggers for which to modify the levels
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse modifyLogLevels(Map<String, Level> loggers) throws RemoteException;

    /**
     * Overrides {@link Exception} categories to be suppressed (comma separated)
     * 
     * @param categories The categories to suppress (TODO: Maybe use a {@link List}?)
     * @return A {@link LogResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogResponse overrideExceptionCategories(String categories) throws RemoteException;

    /**
     * Returns a {@link Set} with all {@link Exception} categories
     * 
     * @return a {@link Set} with all {@link Exception} categories
     * @throws RemoteException if an error is occurred
     */
    Set<String> listExceptionCategories() throws RemoteException;

    /**
     * Returns a {@link Set} with the names of all known loggers in the system
     * 
     * @return a {@link Set} with the names of all known loggers in the system
     * @throws RemoteException if an error is occurred
     */
    Set<String> listLoggers() throws RemoteException;

    /**
     * Returns a {@link Set} with the names of all loggers along with their level that were dynamically modified.
     * 
     * @return a {@link Set} with the names of all loggers along with their level that were dynamically modified.
     * @throws RemoteException if an error is occurred
     */
    Set<String> listDynamicallyModifiedLoggers() throws RemoteException;

    /**
     * Returns a {@link Set} with the level of the specified loggers
     * 
     * @param loggers The logger names
     * @return a {@link Set} with the level of the specified loggers
     * @throws RemoteException if an error is occurred
     */
    Set<String> getLevelForLoggers(String[] loggers) throws RemoteException;

    /**
     * Sets whether to include stack-traces in HTTP API JSON responses for the specified user
     * in the specified context
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param enable whether or not to enable the stack-traces in the HTTP API
     * @throws RemoteException if an error is occurred
     */
    void includeStackTraceForUser(int contextId, int userId, boolean enable) throws RemoteException;

    /**
     * Returns an information string about all root appenders.
     * 
     * @return an information string about all root appenders.
     * @throws RemoteException if an error is occurred
     */
    String getRootAppenderStats() throws RemoteException;
}
