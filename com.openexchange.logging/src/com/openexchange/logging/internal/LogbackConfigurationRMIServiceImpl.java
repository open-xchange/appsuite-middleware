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
