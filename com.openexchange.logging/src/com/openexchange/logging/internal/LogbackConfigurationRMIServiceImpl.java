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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppender;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.ExtendedMDCFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.filter.TurboFilterCache;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;
import com.openexchange.logging.rmi.LogbackConfigurationRMIService;
import com.openexchange.logging.rmi.LogbackRemoteResponse;
import com.openexchange.logging.rmi.LogbackRemoteResponse.MessageType;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link LogbackConfigurationRMIServiceImpl} - The default implementation of the {@link LogbackConfigurationRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class LogbackConfigurationRMIServiceImpl implements LogbackConfigurationRMIService {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackConfigurationRMIServiceImpl.class);
    private static final String WHITELIST_PROPERTY = "com.openexchange.logging.filter.loggerWhitelist";
    private static final String lineSeparator = System.getProperty("line.separator");

    // -------------------------------------------------------------------------------------------- //

    private final LoggerContext loggerContext;
    private final JoranConfigurator configurator;

    private final Map<String, Level> dynamicallyModifiedLoggers;

    private final TurboFilterCache turboFilterCache;
    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final IncludeStackTraceService traceService;

    /**
     * Initialises a new {@link LogbackConfigurationRMIServiceImpl}.
     * 
     * @param loggerContext The {@link LoggerContext}
     * @param rankingAwareTurboFilterList The {@link RankingAwareTurboFilterList}
     * @param traceService The {@link IncludeStackTraceService}
     */
    public LogbackConfigurationRMIServiceImpl(LoggerContext loggerContext, RankingAwareTurboFilterList rankingAwareTurboFilterList, IncludeStackTraceService traceService) {
        super();
        this.loggerContext = loggerContext;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.traceService = traceService;

        //Initialise members
        configurator = new JoranConfigurator();
        dynamicallyModifiedLoggers = new HashMap<String, Level>();

        // Set the ranking aware turbo filter and initialise the turbo filter cache
        turboFilterCache = new TurboFilterCache();

        // Add turbo filter cache to list
        rankingAwareTurboFilterList.addTurboFilter(turboFilterCache);

        configurator.setContext(loggerContext);
    }

    /**
     * Disposes this instance.
     */
    public void dispose() {
        rankingAwareTurboFilterList.removeTurboFilter(turboFilterCache);
        turboFilterCache.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#filterContext(int, java.util.Map)
     */
    @Override
    public LogbackRemoteResponse filterContext(int contextId, Map<String, Level> loggers) throws RemoteException {
        return createExtendedMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextId), loggers, FilterReply.ACCEPT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#filterUser(int, int, java.util.Map)
     */
    @Override
    public LogbackRemoteResponse filterUser(int contextId, int userId, Map<String, Level> loggers) throws RemoteException {
        StringBuilder builder = new StringBuilder(2048).append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userId)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextId))));
        String key = builder.toString();
        builder.setLength(0);
        LogbackRemoteResponse response = new LogbackRemoteResponse();

        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            filter = new ExtendedMDCFilter(getLoggerWhitelist());
            filter.addTuple(Name.SESSION_USER_ID.getName(), Integer.toString(userId));
            filter.addTuple(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextId));
            filter.setName(key);
            builder.append("Created new ");
        } else {
            builder.append("Updating ");
        }
        builder.append("filter for user with ID \"").append(userId).append("\", in context with ID \"").append(contextId).append("\" and policy \"ACCEPT\"");
        response.addMessage(builder.toString(), MessageType.INFO);

        addLoggersToFilter(getLoggerWhitelist(), loggers, filter, response);

        turboFilterCache.put(key, filter);
        LOG.info(builder.toString());

        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#filterSession(java.lang.String, java.util.Map)
     */
    @Override
    public LogbackRemoteResponse filterSession(String sessionId, Map<String, Level> loggers) throws RemoteException {
        return createExtendedMDCFilter(Name.SESSION_SESSION_ID.getName(), sessionId, loggers, FilterReply.ACCEPT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#listFilters()
     */
    @Override
    public Set<String> listFilters() throws RemoteException {
        StringBuilder builder = new StringBuilder();
        Set<String> filters = new HashSet<String>();
        for (String key : turboFilterCache.keySet()) {
            filters.add(builder.append(turboFilterCache.get(key).toString()).toString());
            builder.setLength(0);
        }
        return filters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#removeContextFilter(int, java.util.List)
     */
    @Override
    public LogbackRemoteResponse removeContextFilter(int contextId, List<String> loggers) throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        String key = createKey(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextId));
        StringBuilder builder = new StringBuilder();
        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed context filter with context ID \"").append(contextId).append("\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
            return response;
        }
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            builder.setLength(0);
            builder.append("Context filter with contextID \"").append(contextId).append("\" does not exist.");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.WARNING);
            return response;
        }

        for (String s : loggers) {
            filter.removeLogger(s);
            builder.setLength(0);
            builder.append("Removed logger \"").append(s).append("\"").append(" from context filter with context ID \"").append(contextId).append("\"");
            response.addMessage(builder.toString(), MessageType.INFO);
            LOG.info(builder.toString());
        }
        if (!filter.hasLoggers()) {
            turboFilterCache.remove(key);
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#removeUserFilter(int, int, java.util.List)
     */
    @Override
    public LogbackRemoteResponse removeUserFilter(int contextId, int userId, List<String> loggers) throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        StringBuilder builder = new StringBuilder().append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userId)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextId))));
        String key = builder.toString();

        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed user filter for user with ID \"").append(userId).append("\", context with ID \"").append(contextId).append("\" and policy \"ACCEPT\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
            return response;
        }
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            builder.append("User filter for user with ID \"").append(userId).append("\", context with ID \"").append(contextId).append("\" and policy \"ACCEPT\" does not exist");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.WARNING);
            return response;
        }
        for (String s : loggers) {
            filter.removeLogger(s);
            builder.setLength(0);
            builder.append("Removed logger \"").append(s).append("\"").append(" from ").append(" user filter for user with ID \"").append(userId).append("\", context with ID \"").append(contextId).append("\" and policy \"ACCEPT\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
        }
        if (!filter.hasLoggers()) {
            turboFilterCache.remove(key);
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#removeSessionFilter(java.lang.String, java.util.List)
     */
    @Override
    public LogbackRemoteResponse removeSessionFilter(String sessionId, List<String> loggers) throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        String key = createKey(Name.SESSION_SESSION_ID.getName(), sessionId);
        StringBuilder builder = new StringBuilder();
        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed session filter with ID \"").append(sessionId).append("\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
            return response;
        }
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            builder.setLength(0);
            builder.append("Session filter with ID \"").append(sessionId).append("\"").append(" does not exist.");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.WARNING);
            return response;
        }
        for (String s : loggers) {
            filter.removeLogger(s);
            builder.setLength(0);
            builder.append("Removed logger \"").append(s).append("\"").append(" from ").append(" session filter with ID \"").append(sessionId).append("\" and policy \"ACCEPT\"");
            response.addMessage(builder.toString(), MessageType.INFO);
            LOG.info(builder.toString());
        }
        if (!filter.hasLoggers()) {
            turboFilterCache.remove(key);
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#clearFilters()
     */
    @Override
    public LogbackRemoteResponse clearFilters() throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        turboFilterCache.clear();
        String msg = "Removed all filters";
        LOG.info(msg);
        response.addMessage(msg, MessageType.INFO);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#modifyLogLevels(java.util.Map)
     */
    @Override
    public LogbackRemoteResponse modifyLogLevels(Map<String, Level> loggers) throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        StringBuilder builder = new StringBuilder();
        for (Entry<String, Level> levelEntry : loggers.entrySet()) {
            Level l = levelEntry.getValue();
            String s = levelEntry.getKey();
            loggerContext.getLogger(s).setLevel(l);
            dynamicallyModifiedLoggers.put(s, l);
            builder.setLength(0);
            builder.append("Setting log level for \"").append(s).append("\" to \"").append(l).append("\"");
            response.addMessage(builder.toString(), MessageType.INFO);
            LOG.info(builder.toString());
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#overrideExceptionCategories(java.lang.String)
     */
    @Override
    public LogbackRemoteResponse overrideExceptionCategories(String categories) throws RemoteException {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        LOG.info("Setting suppressed Exception Categories to \"{}\"", categories);
        ExceptionCategoryFilter.setCategories(categories);
        response.addMessage("Setting suppressed Exception Categories to " + categories, MessageType.INFO);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#listExceptionCategories()
     */
    @Override
    public Set<String> listExceptionCategories() throws RemoteException {
        Set<String> categories = new HashSet<String>();
        for (String category : ExceptionCategoryFilter.getCategories().split(",")) {
            categories.add(category.trim());
        }
        return categories;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#listLoggers()
     */
    @Override
    public Set<String> listLoggers() throws RemoteException {
        Set<String> loggers = new HashSet<String>();
        for (ch.qos.logback.classic.Logger l : loggerContext.getLoggerList()) {
            loggers.add(getLoggerNameAndLevel(l));
        }
        return loggers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#listDynamicallyModifiedLoggers()
     */
    @Override
    public Set<String> listDynamicallyModifiedLoggers() throws RemoteException {
        Set<String> loggers = new HashSet<String>();
        Iterator<String> keys = dynamicallyModifiedLoggers.keySet().iterator();
        while (keys.hasNext()) {
            loggers.add(getLoggerNameAndLevel(loggerContext.getLogger(keys.next())));
        }
        return loggers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#getLevelForLoggers(java.lang.String[])
     */
    @Override
    public Set<String> getLevelForLoggers(String[] loggers) throws RemoteException {
        Set<String> l = new HashSet<String>();
        for (String s : loggers) {
            l.add(getLoggerNameAndLevel(loggerContext.getLogger(s)));
        }
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#includeStackTraceForUser(int, int, boolean)
     */
    @Override
    public void includeStackTraceForUser(int contextId, int userId, boolean enable) throws RemoteException {
        ((IncludeStackTraceServiceImpl) traceService).addTuple(userId, contextId, enable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.logging.rmi.LogbackConfigurationRMIService#getRootAppenderStats()
     */
    @Override
    public String getRootAppenderStats() throws RemoteException {
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> appenders = rootLogger.iteratorForAppenders();
        StringBuilder sb = new StringBuilder();
        while (appenders.hasNext()) {
            Appender<ILoggingEvent> appender = appenders.next();
            sb.append(appender.getClass().getSimpleName()).append(": ").append(appender.getName());
            if (appender instanceof AsyncAppender) {
                AsyncAppender asyncAppender = (AsyncAppender) appender;
                sb.append(" [capacity=").append(asyncAppender.getQueueSize()).append(",size=").append(asyncAppender.getNumberOfElementsInQueue()).append(']');
            } else if (appender instanceof LogstashSocketAppender) {
                LogstashSocketAppender socketAppender = (LogstashSocketAppender) appender;
                sb.append(" [capacity=").append(socketAppender.getQueueSize()).append(",size=").append(socketAppender.getNumberOfElementsInQueue()).append(']');
            }

            sb.append(lineSeparator);
        }
        if (sb.length() == 0) {
            sb.append("No root appenders found.").append(lineSeparator);
        }
        return sb.toString();
    }

    ////////////////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Create an MDCFilter based on the specified key/value/filter
     * 
     * @param key The key of the filter
     * @param value The value of the filter
     * @param loggers The logger names along with their level
     * @param onMatch The {@link FilterReply}
     */
    private final LogbackRemoteResponse createExtendedMDCFilter(String key, String value, Map<String, Level> loggers, FilterReply onMatch) {
        LogbackRemoteResponse response = new LogbackRemoteResponse();
        String sKey = createKey(key, value);
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(sKey);
        StringBuilder builder = new StringBuilder();

        if (filter == null) {
            filter = new ExtendedMDCFilter(getLoggerWhitelist());
            filter.addTuple(key, value);
            filter.setName(sKey);
            builder.append("Created new ");
        } else {
            builder.append("Updated ");
        }
        builder.append("filter with key '").append(key).append("'and value '").append(value).append("' and policy 'ACCEPT'");
        response.addMessage(builder.toString(), MessageType.INFO);

        addLoggersToFilter(getLoggerWhitelist(), loggers, filter, response);

        turboFilterCache.put(sKey, filter);
        LOG.info(builder.toString());

        return response;
    }

    /**
     * Creates the appropriate key.
     * 
     * @param key The key
     * @param value The value
     * @return The key/value tuple
     */
    private String createKey(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(key).append("=").append(value);
        return builder.toString();
    }

    /**
     * Returns a {@link Set} with all white-listed loggers
     * 
     * @return a {@link Set} with all white-listed loggers
     */
    private Set<String> getLoggerWhitelist() {
        String whitelist = loggerContext.getProperty(WHITELIST_PROPERTY);
        if (whitelist == null) {
            return Collections.emptySet();
        }

        Set<String> resultSet = new HashSet<String>();
        String[] split = whitelist.split("\\s*,\\s*");
        for (String str : split) {
            resultSet.add(str.trim());
        }

        return resultSet;
    }

    /**
     * Add the specified loggers to the filter in regard to the whitelist
     *
     * @param whitelist of loggers
     * @param loggers to add to the filter
     * @param filter the filter
     * @param response the response object
     */
    private void addLoggersToFilter(Set<String> whitelist, Map<String, Level> loggers, ExtendedMDCFilter filter, LogbackRemoteResponse response) {
        for (Entry<String, Level> levelEntry : loggers.entrySet()) {
            addLogger(whitelist, filter, response, levelEntry);
        }
    }

    /**
     * Adds the specified logger to the filter if it is contained within the specified white-list
     * 
     * @param whitelist The white-list to check
     * @param filter The {@link ExtendedMDCFilter}
     * @param response The {@link LogbackRemoteResponse}
     * @param entry The logger entry
     */
    private void addLogger(Set<String> whitelist, ExtendedMDCFilter filter, LogbackRemoteResponse response, Entry<String, Level> entry) {
        String key = entry.getKey();
        Level level = entry.getValue();
        for (String loggerName : whitelist) {
            if (!key.startsWith(loggerName)) {
                continue;
            }
            filter.addLogger(key, level);
            String msg = "Added logger '" + key + "' with level '" + level + "'";
            response.addMessage(msg, MessageType.INFO);
            return;
        }
        String msg = "The provided logger '" + key + "' is not in the whitelist, hence it is not added to the filter.";
        response.addMessage(msg, MessageType.WARNING);
        LOG.warn(msg);
    }

    /**
     * Removes the specified filter from the cache
     */
    private void removeFilter(String key) {
        turboFilterCache.remove(key);
    }

    /**
     * Get a stringified version of logger name and level
     *
     * @param logger The {@link ch.qos.logback.classic.Logger}
     * @return The stringified version
     */
    private String getLoggerNameAndLevel(ch.qos.logback.classic.Logger logger) {
        StringBuilder builder = new StringBuilder();
        builder.append("Logger: ").append(logger.getName()).append(", Level: ").append(logger.getLevel());
        return builder.toString();
    }
}
