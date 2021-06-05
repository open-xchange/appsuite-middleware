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

package com.openexchange.logging.internal;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.logging.LogConfigurationService;
import com.openexchange.logging.LogResponse;
import com.openexchange.logging.rmi.LogbackConfigurationRMIService;
import ch.qos.logback.classic.Level;

/**
 * {@link LogbackConfigurationRMIServiceImpl} - The default implementation of the {@link LogbackConfigurationRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class LogbackConfigurationRMIServiceImpl implements LogbackConfigurationRMIService {

    private LogConfigurationService logbackConfigService;

    /**
     * Initialises a new {@link LogbackConfigurationRMIServiceImpl}.
     */
    public LogbackConfigurationRMIServiceImpl(LogConfigurationService logbackConfigService) {
        super();
        this.logbackConfigService = logbackConfigService;
    }

    @Override
    public LogResponse filterContext(int contextId, Map<String, Level> loggers) throws RemoteException {
        return logbackConfigService.createContextFilter(contextId, loggers);
    }

    @Override
    public LogResponse filterUser(int contextId, int userId, Map<String, Level> loggers) throws RemoteException {
        return logbackConfigService.createUserFilter(userId, contextId, loggers);
    }

    @Override
    public LogResponse filterSession(String sessionId, Map<String, Level> loggers) throws RemoteException {
        return logbackConfigService.createSessionFilter(sessionId, loggers);
    }

    @Override
    public Set<String> listFilters() throws RemoteException {
        return logbackConfigService.listFilters();
    }

    @Override
    public LogResponse removeContextFilter(int contextId, List<String> loggers) throws RemoteException {
        return logbackConfigService.removeContextFilter(contextId, loggers);
    }

    @Override
    public LogResponse removeUserFilter(int contextId, int userId, List<String> loggers) throws RemoteException {
        return logbackConfigService.removeUserFilter(contextId, userId, loggers);
    }

    @Override
    public LogResponse removeSessionFilter(String sessionId, List<String> loggers) throws RemoteException {
        return logbackConfigService.removeSessionFilter(sessionId, loggers);
    }

    @Override
    public LogResponse clearFilters() throws RemoteException {
        return logbackConfigService.clearFilters();
    }

    @Override
    public LogResponse modifyLogLevels(Map<String, Level> loggers) throws RemoteException {
        return logbackConfigService.modifyLogLevels(loggers);
    }

    @Override
    public LogResponse overrideExceptionCategories(String categories) throws RemoteException {
        return logbackConfigService.overrideExceptionCategories(categories);
    }

    @Override
    public Set<String> listExceptionCategories() throws RemoteException {
        return logbackConfigService.listExceptionCategories();
    }

    @Override
    public Set<String> listLoggers() throws RemoteException {
        return logbackConfigService.listLoggers();
    }

    @Override
    public Set<String> listDynamicallyModifiedLoggers() throws RemoteException {
        return logbackConfigService.listDynamicallyModifiedLoggers();
    }

    @Override
    public Set<String> getLevelForLoggers(String[] loggers) throws RemoteException {
        return logbackConfigService.getLevelForLoggers(loggers);
    }

    @Override
    public void includeStackTraceForUser(int contextId, int userId, boolean enable) throws RemoteException {
        logbackConfigService.includeStackTraceForUser(contextId, userId, enable);
    }

    @Override
    public String getRootAppenderStats() throws RemoteException {
        return logbackConfigService.getRootAppenderStats();
    }
}
