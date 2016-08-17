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

package com.openexchange.logging.mbean;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppender;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.ExtendedMDCFilter;
import com.openexchange.logging.filter.MDCEnablerTurboFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.filter.TurboFilterCache;
import com.openexchange.logging.mbean.LogbackMBeanResponse.MessageType;
import com.openexchange.management.MBeanMethodAnnotation;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link LogbackConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LogbackConfiguration extends StandardMBean implements LogbackConfigurationMBean {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackConfiguration.class);

    private static final String WHITELIST_PROPERTY = "com.openexchange.logging.filter.loggerWhitelist";

    private static final String lineSeparator = System.getProperty("line.separator");

    // -------------------------------------------------------------------------------------------- //

    private final LoggerContext loggerContext;

    private final JoranConfigurator configurator;

    private final Map<String, Level> dynamicallyModifiedLoggers;

    private final Map<String, String> methodDescriptions;

    private final Map<String, String[]> methodParameters;

    private final Map<String, String[]> methodParameterDescriptions;

    private final TurboFilterCache turboFilterCache;

    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;

    private final IncludeStackTraceServiceImpl traceServiceImpl;

    private final MDCEnablerTurboFilter mdcEnablerTurboFilter;

    /**
     * Initialises a new {@link LogbackConfiguration}. Reads the MBean annotations and adds those to the method* maps.
     *
     * @throws NotCompliantMBeanException
     */
    public LogbackConfiguration(final LoggerContext loggerContext, final RankingAwareTurboFilterList rankingAwareTurboFilterList, final IncludeStackTraceServiceImpl traceServiceImpl) throws NotCompliantMBeanException {
        super(LogbackConfigurationMBean.class);
        this.loggerContext = loggerContext;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.traceServiceImpl = traceServiceImpl;

        // Initialise members
        configurator = new JoranConfigurator();
        dynamicallyModifiedLoggers = new HashMap<String, Level>();
        methodDescriptions = new HashMap<String, String>();
        methodParameters = new HashMap<String, String[]>();
        methodParameterDescriptions = new HashMap<String, String[]>();

        // Set the ranking aware turbo filter and initialise the turbo filter cache
        turboFilterCache = new TurboFilterCache();
        mdcEnablerTurboFilter = new MDCEnablerTurboFilter();

        // Add turbo filter cache to list
        rankingAwareTurboFilterList.addTurboFilter(turboFilterCache);
        rankingAwareTurboFilterList.addTurboFilter(mdcEnablerTurboFilter);

        configurator.setContext(loggerContext);

        Class<?>[] interfaces = this.getClass().getInterfaces();
        if (interfaces.length == 1) { // just in case, should always be equals to 1
            Method[] methods = interfaces[0].getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(MBeanMethodAnnotation.class)) {
                    MBeanMethodAnnotation a = m.getAnnotation(MBeanMethodAnnotation.class);
                    methodParameters.put(m.getName(), a.parameters());
                    methodDescriptions.put(m.getName(), a.description());
                    methodParameterDescriptions.put(m.getName(), a.parameterDescriptions());
                }
            }
        } else {
            LOG.error("Cannot initialize annotations");
        }
    }

    /**
     * Disposes this instance.
     */
    public void dispose() {
        rankingAwareTurboFilterList.removeTurboFilter(turboFilterCache);
        turboFilterCache.clear();
    }

    @Override
    public String getRootAppenderStats() {
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

    @Override
    public LogbackMBeanResponse filterContext(int contextID, Map<String, Level> loggers) {
        return createExtendedMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID), loggers, FilterReply.ACCEPT);
    }

    @Override
    public LogbackMBeanResponse filterUser(int userID, int contextID, Map<String, Level> loggers) {
        StringBuilder builder = new StringBuilder(2048).append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        String key = builder.toString();
        builder.setLength(0);
        LogbackMBeanResponse response = new LogbackMBeanResponse();

        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            filter = new ExtendedMDCFilter(getLoggerWhitelist());
            filter.addTuple(Name.SESSION_USER_ID.getName(), Integer.toString(userID));
            filter.addTuple(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID));
            filter.setName(key);
            builder.append("Created new ");
        } else {
            builder.append("Updating ");
        }
        builder.append("filter for user with ID \"").append(userID).append("\", in context with ID \"").append(contextID).append("\" and policy \"ACCEPT\"");
        response.addMessage(builder.toString(), MessageType.INFO);

        addLoggersToFilter(getLoggerWhitelist(), loggers, filter, response);

        turboFilterCache.put(key, filter);
        LOG.info(builder.toString());

        return response;
    }

    @Override
    public LogbackMBeanResponse filterSession(String sessionID, Map<String, Level> loggers) {
        return createExtendedMDCFilter(Name.SESSION_SESSION_ID.getName(), sessionID, loggers, FilterReply.ACCEPT);
    }

    @Override
    public LogbackMBeanResponse modifyLogLevels(Map<String, Level> loggers) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
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

    @Override
    public LogbackMBeanResponse overrideExceptionCategories(String categories) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
        LOG.info("Setting suppressed Exception Categories to \"{}\"", categories);
        ExceptionCategoryFilter.setCategories(categories);
        response.addMessage("Setting suppressed Exception Categories to " + categories, MessageType.INFO);
        return response;
    }

    @Override
    public Set<String> listExceptionCategories() {
        Set<String> categories = new HashSet<String>();
        for (String category : ExceptionCategoryFilter.getCategories().split(",")) {
            categories.add(category.trim());
        }
        return categories;
    }

    @Override
    public LogbackMBeanResponse removeContextFilter(int contextID, List<String> loggers) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
        String key = createKey(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID));
        StringBuilder builder = new StringBuilder();
        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed context filter with context ID \"").append(contextID).append("\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
        } else {
            ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
            if (filter != null) {
                for (String s : loggers) {
                    filter.removeLogger(s);
                    builder.setLength(0);
                    builder.append("Removed logger \"").append(s).append("\"").append(" from context filter with context ID \"").append(contextID).append("\"");
                    response.addMessage(builder.toString(), MessageType.INFO);
                    LOG.info(builder.toString());
                }
                if (!filter.hasLoggers()) {
                    turboFilterCache.remove(key);
                }
            } else {
                builder.setLength(0);
                builder.append("Context filter with contextID \"").append(contextID).append("\" does not exist.");
                LOG.info(builder.toString());
                response.addMessage(builder.toString(), MessageType.WARNING);
            }
        }
        return response;
    }

    @Override
    public LogbackMBeanResponse removeUserFilter(int userID, int contextID, List<String> loggers) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
        StringBuilder builder = new StringBuilder().append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        String key = builder.toString();

        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed user filter for user with ID \"").append(userID).append("\", context with ID \"").append(contextID).append("\" and policy \"ACCEPT\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
        } else {
            ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
            if (filter != null) {
                for (String s : loggers) {
                    filter.removeLogger(s);
                    builder.setLength(0);
                    builder.append("Removed logger \"").append(s).append("\"").append(" from ").append(" user filter for user with ID \"").append(userID).append("\", context with ID \"").append(contextID).append("\" and policy \"ACCEPT\"");
                    LOG.info(builder.toString());
                    response.addMessage(builder.toString(), MessageType.INFO);
                }
                if (!filter.hasLoggers()) {
                    turboFilterCache.remove(key);
                }
            } else {
                builder.append("User filter for user with ID \"").append(userID).append("\", context with ID \"").append(contextID).append("\" and policy \"ACCEPT\" does not exist");
                LOG.info(builder.toString());
                response.addMessage(builder.toString(), MessageType.WARNING);
            }
        }

        return response;
    }

    @Override
    public LogbackMBeanResponse removeSessionFilter(String sessionID, List<String> loggers) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
        String key = createKey(Name.SESSION_SESSION_ID.getName(), sessionID);
        StringBuilder builder = new StringBuilder();
        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed session filter with ID \"").append(sessionID).append("\"");
            LOG.info(builder.toString());
            response.addMessage(builder.toString(), MessageType.INFO);
        } else {
            ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
            if (filter != null) {
                for (String s : loggers) {
                    filter.removeLogger(s);
                    builder.setLength(0);
                    builder.append("Removed logger \"").append(s).append("\"").append(" from ").append(" session filter with ID \"").append(sessionID).append("\" and policy \"ACCEPT\"");
                    response.addMessage(builder.toString(), MessageType.INFO);
                    LOG.info(builder.toString());
                }
                if (!filter.hasLoggers()) {
                    turboFilterCache.remove(key);
                }
            } else {
                builder.setLength(0);
                builder.append("Session filter with ID \"").append(sessionID).append("\"").append(" does not exist.");
                LOG.info(builder.toString());
                response.addMessage(builder.toString(), MessageType.WARNING);
            }
        }
        return response;
    }

    @Override
    public Set<String> listAllLoggers() {
        Set<String> loggers = new HashSet<String>();
        for (ch.qos.logback.classic.Logger l : loggerContext.getLoggerList()) {
            loggers.add(getLoggerNameAndLevel(l));
        }
        return loggers;
    }

    @Override
    public Set<String> listFilters() {
        StringBuilder builder = new StringBuilder();
        Set<String> filters = new HashSet<String>();
        for (String key : turboFilterCache.keySet()) {
            filters.add(builder.append(turboFilterCache.get(key).toString()).toString());
            builder.setLength(0);
        }
        return filters;
    }

    @Override
    public synchronized LogbackMBeanResponse clearFilters() {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
        turboFilterCache.clear();
        String msg = "Removed all filters";
        LOG.info(msg);
        response.addMessage(msg, MessageType.INFO);
        return response;
    }

    @Override
    public Set<String> getLevelForLoggers(String[] loggers) {
        Set<String> l = new HashSet<String>();
        for (String s : loggers) {
            l.add(getLoggerNameAndLevel(loggerContext.getLogger(s)));
        }
        return l;
    }

    @Override
    public Set<String> listDynamicallyModifiedLoggers() {
        Set<String> loggers = new HashSet<String>();
        Iterator<String> keys = dynamicallyModifiedLoggers.keySet().iterator();
        while (keys.hasNext()) {
            loggers.add(getLoggerNameAndLevel(loggerContext.getLogger(keys.next())));
        }
        return loggers;
    }

    @Override
    public void includeStackTraceForUser(final int userID, final int contextID, final boolean enable) {
        traceServiceImpl.addTuple(userID, contextID, enable);
    }

    @Override
    protected final String getDescription(MBeanInfo info) {
        return DESCRIPTION;
    }

    @Override
    protected final String getDescription(MBeanOperationInfo info) {
        return methodDescriptions.get(info.getName());
    }

    @Override
    protected final String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return getMBeanOperationInfo(methodParameterDescriptions, op, param, sequence);
    }

    @Override
    protected final String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return getMBeanOperationInfo(methodParameters, op, param, sequence);
    }

    /**
     * Delegate method for MBeanOperationInfo
     *
     * @param map
     * @param op
     * @param param
     * @param sequence
     * @return
     */
    private final String getMBeanOperationInfo(Map<String, String[]> map, MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        String[] v = map.get(op.getName());
        if (v == null || v.length == 0 || sequence > v.length) {
            return super.getDescription(op, param, sequence);
        }
        return v[sequence];
    }

    /**
     * Create an MDCFilter based on the specified key/value/filter
     */
    private final LogbackMBeanResponse createExtendedMDCFilter(String key, String value, Map<String, Level> loggers, FilterReply onMatch) {
        LogbackMBeanResponse response = new LogbackMBeanResponse();
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
        builder.append("filter with key \"").append(key).append("\" and value \"").append(value).append("\" and policy \"ACCEPT\"");
        response.addMessage(builder.toString(), MessageType.INFO);

        addLoggersToFilter(getLoggerWhitelist(), loggers, filter, response);

        turboFilterCache.put(sKey, filter);
        LOG.info(builder.toString());

        return response;
    }

    /**
     * Remove the specified filter
     */
    private final void removeFilter(String key) {
        turboFilterCache.remove(key);
    }

    /**
     * Creates the appropriate key.
     */
    private String createKey(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(key).append("=").append(value);
        return builder.toString();
    }

    /**
     * Get a stringified version of logger name and level
     *
     * @param logger
     * @return
     */
    private String getLoggerNameAndLevel(ch.qos.logback.classic.Logger logger) {
        StringBuilder builder = new StringBuilder();
        builder.append("Logger: ").append(logger.getName()).append(", Level: ").append(logger.getLevel());
        return builder.toString();
    }

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
    private static final void addLoggersToFilter(Set<String> whitelist, Map<String, Level> loggers, ExtendedMDCFilter filter, LogbackMBeanResponse response) {
        for (Entry<String, Level> levelEntry : loggers.entrySet()) {
            boolean added = false;
            String s = levelEntry.getKey();
            Level level = levelEntry.getValue();
            for (String wl : whitelist) {
                if (s.startsWith(wl)) {
                    Level l = level;
                    filter.addLogger(s, l);
                    String msg = "Added logger \"" + s + "\" with level \"" + l + "\"";
                    response.addMessage(msg, MessageType.INFO);
                    added = true;
                    break;
                }
            }
            if (!added) {
                String msg = "The provided logger \"" + s + "\" is not in the whitelist, hence it is not added to the filter.";
                response.addMessage(msg, MessageType.WARNING);
                LOG.warn(msg);
            }
        }
    }
}
