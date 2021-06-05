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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.exception.Category;
import com.openexchange.exception.Category.EnumType;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppender;
import com.openexchange.logging.LogConfigurationService;
import com.openexchange.logging.LogResponse;
import com.openexchange.logging.MessageType;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.ExtendedMDCFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.filter.TurboFilterCache;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * {@link LogbackLogConfigurationService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
@SuppressWarnings("deprecation")
public class LogbackLogConfigurationService implements LogConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackLogConfigurationService.class);
    private static final String WHITELIST_PROPERTY = "com.openexchange.logging.filter.loggerWhitelist";
    private static final String lineSeparator = Strings.getLineSeparator();

    // -------------------------------------------------------------------------------------------- //

    private final LoggerContext loggerContext;
    private final JoranConfigurator configurator;

    private final Map<String, Level> dynamicallyModifiedLoggers;

    private final TurboFilterCache turboFilterCache;
    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final IncludeStackTraceService traceService;

    /**
     * Initializes a new {@link LogbackLogConfigurationService}.
     *
     * @param loggerContext The {@link LoggerContext}
     * @param rankingAwareTurboFilterList The {@link RankingAwareTurboFilterList}
     * @param traceService The {@link IncludeStackTraceService}
     */
    public LogbackLogConfigurationService(LoggerContext loggerContext, RankingAwareTurboFilterList rankingAwareTurboFilterList, IncludeStackTraceService traceService) {
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

    @Override
    public LogResponse createContextFilter(int contextId, Map<String, Level> loggers) {
        return createFilter(ImmutableList.of(createContextPair(contextId)), loggers);
    }

    @Override
    public LogResponse createUserFilter(int userId, int contextId, Map<String, Level> loggers) {
        return createFilter(ImmutableList.of(createContextPair(contextId), createUserPair(userId)), loggers);
    }

    @Override
    public LogResponse createSessionFilter(String sessionId, Map<String, Level> loggers) {
        return createFilter(ImmutableList.of(createSessionPair(sessionId)), loggers);
    }

    @Override
    public Set<String> listFilters() {
        StringBuilder builder = new StringBuilder();
        Set<String> filters = new HashSet<String>();
        for (String key : turboFilterCache.keySet()) {
            filters.add(builder.append(turboFilterCache.get(key).toString()).toString());
            builder.setLength(0);
        }
        return Collections.unmodifiableSet(filters);
    }

    @Override
    public boolean anyFilterExists(int contextId, int userId, String sessionId) {
        if (turboFilterCache.containsKey(createKey(ImmutableList.of(createSessionPair(sessionId))))) {
            return true;
        }
        Pair<Name, String> userKeyPair = createUserPair(userId);
        Pair<Name, String> contextKeyPair = createContextPair(contextId);
        if (turboFilterCache.containsKey(createKey(ImmutableList.of(contextKeyPair, userKeyPair)))) {
            return true;
        }
        if (turboFilterCache.containsKey(createKey(ImmutableList.of(contextKeyPair)))) {
            return true;
        }
        return false;
    }

    @Override
    public LogResponse removeContextFilter(int contextId, List<String> loggers) {
        return removeFilter(ImmutableList.of(createContextPair(contextId)), loggers);
    }

    @Override
    public LogResponse removeUserFilter(int contextId, int userId, List<String> loggers) {
        return removeFilter(ImmutableList.of(createContextPair(contextId), createUserPair(userId)), loggers);
    }

    @Override
    public LogResponse removeSessionFilter(String sessionId, List<String> loggers) {
        return removeFilter(ImmutableList.of(createSessionPair(sessionId)), loggers);
    }

    @Override
    public LogResponse clearFilters() {
        turboFilterCache.clear();
        String msg = "Removed all filters";
        LOG.info(msg);

        LogbackLogResponse.Builder responseBuilder = LogbackLogResponse.builder();
        responseBuilder.withMessage(msg, MessageType.INFO);
        return responseBuilder.build();
    }

    @Override
    public LogResponse modifyLogLevels(Map<String, Level> loggers) {
        LogbackLogResponse.Builder responseBuilder = LogbackLogResponse.builder();
        StringBuilder builder = new StringBuilder();
        for (Entry<String, Level> levelEntry : loggers.entrySet()) {
            Level l = levelEntry.getValue();
            String s = levelEntry.getKey();
            loggerContext.getLogger(s).setLevel(l);
            dynamicallyModifiedLoggers.put(s, l);
            builder.setLength(0);
            builder.append("Setting log level for '").append(s).append("' to '").append(l).append("'");
            responseBuilder.withMessage(builder.toString(), MessageType.INFO);
            LOG.info(builder.toString());
        }
        return responseBuilder.build();
    }

    @Override
    public LogResponse overrideExceptionCategories(String categories) {
        LOG.info("Setting suppressed Exception Categories to '{}'", categories);
        ExceptionCategoryFilter.setCategories(categories);

        LogbackLogResponse.Builder responseBuilder = LogbackLogResponse.builder();
        responseBuilder.withMessage("Setting suppressed Exception Categories to " + categories, MessageType.INFO);
        return responseBuilder.build();
    }

    @Override
    public Set<String> listExceptionCategories() {
        Set<EnumType> cats = ExceptionCategoryFilter.getCategories();
        Set<String> categories = new LinkedHashSet<String>(cats.size());
        for (Category.EnumType category : cats) {
            categories.add(category.getName());
        }
        return categories;
    }

    @Override
    public Set<String> listLoggers() {
        Set<String> loggers = new HashSet<String>();
        for (ch.qos.logback.classic.Logger l : loggerContext.getLoggerList()) {
            loggers.add(getLoggerNameAndLevel(l));
        }
        return loggers;
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
    public Set<String> getLevelForLoggers(String[] loggers) {
        Set<String> l = new HashSet<String>();
        for (String s : loggers) {
            l.add(getLoggerNameAndLevel(loggerContext.getLogger(s)));
        }
        return l;
    }

    @Override
    public void includeStackTraceForUser(int contextId, int userId, boolean enable) {
        ((IncludeStackTraceServiceImpl) traceService).addTuple(userId, contextId, enable);
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
                sb.append(" [capacity=").append(asyncAppender.getQueueSize()).append(", size=").append(asyncAppender.getNumberOfElementsInQueue()).append(']');
            } else if (appender instanceof LogstashSocketAppender) {
                LogstashSocketAppender socketAppender = (LogstashSocketAppender) appender;
                sb.append(" [capacity=").append(socketAppender.getQueueSize()).append(", size=").append(socketAppender.getNumberOfElementsInQueue()).append(']');
            }

            sb.append(lineSeparator);
        }
        if (sb.length() == 0) {
            sb.append("No root appenders found.").append(lineSeparator);
        }
        return sb.toString();
    }

    /////////////////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////////

    /**
     * Creates a logging filter with the specified key elements and for the specified loggers
     *
     * @param keyElements A list with the key elements
     * @param loggers the loggers
     * @return The outcome of the operation
     */
    private LogResponse createFilter(List<Pair<Name, String>> keyElements, Map<String, Level> loggers) {
        LogbackLogResponse.Builder responseBuilder = LogbackLogResponse.builder();
        String filterKey = createKey(keyElements);
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(filterKey);
        StringBuilder builder = new StringBuilder();

        if (filter == null) {
            filter = new ExtendedMDCFilter(getLoggerWhitelist());
            addTuples(filter, keyElements);
            filter.setName(filterKey);
            builder.append("Created new ");
        } else {
            builder.append("Updated ");
        }
        builder.append("filter with ");
        fillBuilderWithPairs(builder, keyElements);
        builder.append("' and policy 'ACCEPT'");
        responseBuilder.withMessage(builder.toString(), MessageType.INFO);

        addLoggersToFilter(getLoggerWhitelist(), loggers, filter, responseBuilder);

        turboFilterCache.put(filterKey, filter);
        LOG.info(builder.toString());

        return responseBuilder.build();
    }

    /**
     * Removes the logging filter with the specified key elements and for the specified loggers
     *
     * @param keyElements The key elements
     * @param loggers the loggers
     * @return The outcome of the operation
     */
    private LogResponse removeFilter(List<Pair<Name, String>> keyElements, List<String> loggers) {
        LogbackLogResponse.Builder responseBuilder = LogbackLogResponse.builder();

        String key = createKey(keyElements);
        StringBuilder builder = new StringBuilder(128);

        if (loggers.isEmpty()) {
            removeFilter(key);
            builder.setLength(0);
            builder.append("Removed filter for ");
            fillBuilderWithPairs(builder, keyElements);
            builder.append(" and policy 'ACCEPT'");

            LOG.info(builder.toString());
            responseBuilder.withMessage(builder.toString(), MessageType.INFO);
            return responseBuilder.build();
        }
        ExtendedMDCFilter filter = (ExtendedMDCFilter) turboFilterCache.get(key);
        if (filter == null) {
            builder.append("Filter for ");
            fillBuilderWithPairs(builder, keyElements);
            builder.append(" and policy 'ACCEPT' does not exist");
            LOG.info(builder.toString());
            responseBuilder.withMessage(builder.toString(), MessageType.WARNING);
            return responseBuilder.build();
        }
        for (String s : loggers) {
            filter.removeLogger(s);
            builder.setLength(0);
            builder.append("Removed logger '").append(s).append("'").append(" from ").append(" filter with ");
            fillBuilderWithPairs(builder, keyElements);
            builder.append(" and policy 'ACCEPT'");
            LOG.info(builder.toString());
            responseBuilder.withMessage(builder.toString(), MessageType.INFO);
        }
        if (!filter.hasLoggers()) {
            turboFilterCache.remove(key);
        }
        return responseBuilder.build();
    }

    /**
     * Adds the pairs of the specified key elements to the specified builder
     *
     * @param builder The builder to add the key elements
     * @param keyElements The key elements to add to the builder
     */
    private void fillBuilderWithPairs(StringBuilder builder, List<Pair<Name, String>> keyElements) {
        int size;
        if (keyElements == null || (size = keyElements.size()) <= 0) {
            return;
        }

        Pair<Name, String> firstKeyElement = keyElements.get(0);
        builder.append("key='").append(firstKeyElement.getFirst().getName()).append("' and value='").append(firstKeyElement.getSecond()).append('\'');
        for (int i = 1; i < size; i++) {
            Pair<Name, String> keyElement = keyElements.get(i);
            builder.append(", key='").append(keyElement.getFirst().getName()).append("' and value='").append(keyElement.getSecond()).append('\'');
        }
    }

    /**
     * Adds the specified key elements to the specified filter
     *
     * @param filter The filter
     * @param keyElements The key elements to add to the filter
     */
    private void addTuples(ExtendedMDCFilter filter, List<Pair<Name, String>> keyElements) {
        for (Pair<Name, String> pair : keyElements) {
            filter.addTuple(pair.getFirst().getName(), pair.getSecond());
        }
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
    private void addLoggersToFilter(Set<String> whitelist, Map<String, Level> loggers, ExtendedMDCFilter filter, LogbackLogResponse.Builder responseBuilder) {
        for (Entry<String, Level> levelEntry : loggers.entrySet()) {
            addLogger(whitelist, filter, responseBuilder, levelEntry);
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
    private void addLogger(Set<String> whitelist, ExtendedMDCFilter filter, LogbackLogResponse.Builder responseBuilder, Entry<String, Level> entry) {
        String key = entry.getKey();
        Level level = entry.getValue();
        for (String loggerName : whitelist) {
            if (false == key.startsWith(loggerName)) {
                continue;
            }
            filter.addLogger(key, level);
            String msg = "Added logger '" + key + "' with level '" + level + "'";
            responseBuilder.withMessage(msg, MessageType.INFO);
            return;
        }
        String msg = "The provided logger '" + key + "' is not in the whitelist, hence it is not added to the filter.";
        responseBuilder.withMessage(msg, MessageType.WARNING);
        LOG.warn(msg);
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

    /**
     * Removes the specified filter from the cache
     */
    private void removeFilter(String key) {
        turboFilterCache.remove(key);
    }

    /**
     * Creates a context {@link Pair}
     *
     * @param contextId The context identifier
     * @return The {@link Pair}
     */
    private Pair<Name, String> createContextPair(int contextId) {
        return new Pair<Name, String>(Name.SESSION_CONTEXT_ID, Integer.toString(contextId));
    }

    /**
     * Creates a user {@link Pair}
     *
     * @param userId The user identifier
     * @return The {@link Pair}
     */
    private Pair<Name, String> createUserPair(int userId) {
        return new Pair<Name, String>(Name.SESSION_USER_ID, Integer.toString(userId));
    }

    /**
     * Creates a session {@link Pair}
     *
     * @param sessionId The session identifier
     * @return The {@link Pair}
     */
    private Pair<Name, String> createSessionPair(String sessionId) {
        return new Pair<Name, String>(Name.SESSION_SESSION_ID, sessionId);
    }

    /**
     * Creates a stringified version ('<code>key=value</code>')of the specified key/value {@link Pair}.
     *
     * @param pair The {@link Pair}
     * @return The String version
     */
    private String createKeyValuePair(Pair<Name, String> pair) {
        StringBuilder builder = new StringBuilder();
        builder.append(pair.getFirst()).append("=").append(pair.getSecond());
        return builder.toString();
    }

    /**
     * Creates colon separated key with all the specified key elements
     *
     * @param keyElements the key elements to include in the key
     * @return The string version of the key
     */
    private String createKey(List<Pair<Name, String>> keyElements) {
        StringBuilder builder = new StringBuilder(128);
        for (Pair<Name, String> pair : keyElements) {
            builder.append(createKeyValuePair(pair)).append(":");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }
}
