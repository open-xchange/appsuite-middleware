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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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
import java.util.Map;
import java.util.Set;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link LogbackConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LogbackConfiguration extends StandardMBean implements LogbackConfigurationMBean {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackConfiguration.class);

    private static final String WHITELIST_PROPERTY = "com.openexchange.logging.filter.loggerWhitelist";

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

    /**
     * Initializes a new {@link LogbackConfiguration}.
     *
     * Reads the MBean annotations and adds those to the method* maps.
     *
     * @throws NotCompliantMBeanException
     */
    public LogbackConfiguration(final LoggerContext loggerContext, final RankingAwareTurboFilterList rankingAwareTurboFilterList, final IncludeStackTraceServiceImpl traceServiceImpl) throws NotCompliantMBeanException {
        super(LogbackConfigurationMBean.class);
        this.loggerContext = loggerContext;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.traceServiceImpl = traceServiceImpl;

        // Initialize members
        configurator = new JoranConfigurator();
        dynamicallyModifiedLoggers = new HashMap<String, Level>();
        methodDescriptions = new HashMap<String, String>();
        methodParameters = new HashMap<String, String[]>();
        methodParameterDescriptions = new HashMap<String, String[]>();

        // Initialize & add turbo filter cache to list
        final TurboFilterCache turboFilterCache = new TurboFilterCache();
        this.turboFilterCache = turboFilterCache;
        rankingAwareTurboFilterList.addTurboFilter(turboFilterCache);

        configurator.setContext(loggerContext);

        Class<?> [] interfaces = this.getClass().getInterfaces();
        if (interfaces.length == 1) { //just in case, should always be equals to 1
            Method[] methods = interfaces[0].getMethods();
            for(Method m : methods) {
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
    public void filterContext(int contextID) {
        LOG.debug("New context filter created for context with ID \"{}\" and policy \"ACCEPT\"", Integer.valueOf(contextID));
        createExtendedMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID), FilterReply.ACCEPT);
    }

    @Override
    public void filterUser(int userID, int contextID) {
        LOG.debug("New user filter created for user with ID \"{}\", context with ID \"{}\" and policy \"ACCEPT\"", Integer.valueOf(userID), Integer.valueOf(contextID));
        StringBuilder builder = new StringBuilder(2048).append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        String key = builder.toString();
        builder = null;

        ExtendedMDCFilter filter = new ExtendedMDCFilter(getLoggerWhitelist());
        filter.addTuple(Name.SESSION_USER_ID.getName(), Integer.toString(userID));
        filter.addTuple(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID));
        filter.setName(key);

        if (!turboFilterCache.putIfAbsent(key, filter)) {
            LOG.debug("Duplicate user filter for user with ID \"{}\", context with ID \"{}\" and policy \"ACCEPT\"", Integer.valueOf(userID), Integer.valueOf(contextID));
        }
    }

    @Override
    public void filterSession(String sessionID) {
        LOG.debug("New session filter created for session with ID \"{}\" and policy \"ACCEPT\"", sessionID);
        createExtendedMDCFilter(Name.SESSION_SESSION_ID.getName(), sessionID, FilterReply.ACCEPT);
    }

    @Override
    public void setLogLevel(String level, String[] loggers) {
        for (String s : loggers) {
            Level l = Level.valueOf(level);
            loggerContext.getLogger(s).setLevel(l);
            dynamicallyModifiedLoggers.put(s, l);
            LOG.debug("Setting log level for \"{}\" to \"{}\"", s, level);
        }
    }

    @Override
    public void overrideExceptionCategories(String categories) {
        LOG.debug("Setting suppressed Exception Categories to \"{}\"", categories);
        ExceptionCategoryFilter.setCategories(categories);
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
    public void removeContextFilter(int contextID) {
        removeFilter(createKey(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID)));
        LOG.debug("Removed context filter with context ID \"{}\"", Integer.valueOf(contextID));
    }

    @Override
    public void removeUserFilter(int userID, int contextID) {
        StringBuilder builder = new StringBuilder(2048).append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)));
        builder.append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        removeFilter(builder.toString());
        LOG.debug("Removed user filter for user with ID \"{}\", context with ID \"{}\" and policy \"ACCEPT\"", Integer.valueOf(userID), Integer.valueOf(contextID));
    }

    @Override
    public void removeSessionFilter(String sessionID) {
        removeFilter(createKey(Name.SESSION_SESSION_ID.getName(), sessionID));
    }

    @Override
    public Set<String> listAllLoggers() {
        Set<String> loggers = new HashSet<String>();
        for(ch.qos.logback.classic.Logger l : loggerContext.getLoggerList()) {
            loggers.add(getLoggerNameAndLevel(l));
        }
        return loggers;
    }

    @Override
    public Set<String> listFilters() {
        Set<String> filters = new HashSet<String>();
        for (TurboFilter tf : loggerContext.getTurboFilterList()) {
            final String name = tf.getName();
            if (null != name) {
                filters.add(name);
            }
        }
        return filters;
    }


    @Override
    public synchronized void removeAllFilters() {
        rankingAwareTurboFilterList.clear();
        turboFilterCache.clear();
        LOG.debug("Removed all filters");
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
        while(keys.hasNext()) {
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
    private final void createExtendedMDCFilter(String key, String value, FilterReply onMatch) {
        String sKey = createKey(key, value);

        ExtendedMDCFilter filter = new ExtendedMDCFilter(getLoggerWhitelist());
        filter.addTuple(key, value);
        filter.setName(sKey);

        if (!turboFilterCache.putIfAbsent(sKey, filter)) {
            LOG.debug("Duplicate filter for \"{}\" with ID \"{}\" and policy \"ACCEPT\"", key, value);
        }
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

}
