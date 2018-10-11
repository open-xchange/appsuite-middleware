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

package com.openexchange.logging.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse filterContext(int contextId, Map<String, Level> loggers) throws RemoteException;

    /**
     * Creates a logging filter for the specified loggers for the specified user in the specified context
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse filterUser(int contextId, int userId, Map<String, Level> loggers) throws RemoteException;

    /**
     * Creates a logging filter for the specified loggers for the specified session
     * 
     * @param sessionId The session identifier
     * @param loggers The logger names and their {@link Level}s
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse filterSession(String sessionId, Map<String, Level> loggers) throws RemoteException;

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
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse removeContextFilter(int contextId, List<String> loggers) throws RemoteException;

    /**
     * Removes the logging filter for the specified user in the specified context and for the specified loggers
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loggers The logger names
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse removeUserFilter(int contextId, int userId, List<String> loggers) throws RemoteException;

    /**
     * Removes the logging filter for the specified session and for the specified loggers
     * 
     * @param sessionId The session identifier
     * @param loggers The logger names
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse removeSessionFilter(String sessionId, List<String> loggers) throws RemoteException;

    /**
     * Removes all logging filters
     * 
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse clearFilters() throws RemoteException;

    /**
     * Modifies the specified level for the specified loggers
     * 
     * @param loggers The loggers for which to modify the levels
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse modifyLogLevels(Map<String, Level> loggers) throws RemoteException;

    /**
     * Overrides {@link Exception} categories to be suppressed (comma separated)
     * 
     * @param categories The categories to suppress (TODO: Maybe use a {@link List}?)
     * @return A {@link LogbackRemoteResponse} with information about the outcome of the operation
     * @throws RemoteException if an error is occurred
     */
    LogbackRemoteResponse overrideExceptionCategories(String categories) throws RemoteException;

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
