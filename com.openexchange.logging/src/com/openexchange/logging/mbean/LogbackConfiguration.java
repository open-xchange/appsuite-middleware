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
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.openexchange.log.LogProperties.Name;

/**
 * {@link LogbackConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogbackConfiguration extends StandardMBean implements LogbackConfigurationMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(LogbackConfiguration.class);
    
    private final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    private final JoranConfigurator configurator = new JoranConfigurator();
    
    private final Map<String, TurboFilter> turboFilterCache = new HashMap<String, TurboFilter>();
    
    private final Map<String, Level> dynamicallyModifiedLoggers = new HashMap<String, Level>();
    
    private final Map<String, String> methodDescriptions = new HashMap<String, String>();
    
    private final Map<String, String[]> methodParameters = new HashMap<String, String[]>();
    
    private final Map<String, String[]> methodParameterDescriptions = new HashMap<String, String[]>();
    
    /**
     * Initializes a new {@link LogbackConfiguration}.
     * @throws NotCompliantMBeanException 
     */
    public LogbackConfiguration() throws NotCompliantMBeanException {
        super(LogbackConfigurationMBean.class);
        loggerContext.getTurboFilterList().get(0).setName("DEFAULT");
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

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterContext(int)
     */
    @Override
    public void filterContext(int contextID) {
        if (LOG.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("New context filter created for context with ID \"")
                   .append(contextID)
                   .append("\" and policy \"")
                   .append("ACCEPT")
                   .append("\"");
            LOG.debug(builder.toString());
        }
        
        createExtendedMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID), FilterReply.ACCEPT);
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterUser(int, int)
     */
    @Override
    public void filterUser(int userID, int contextID) {
        if (LOG.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("New user filter created for user with ID \"")
                   .append(userID)
                   .append("\", context with ID \"")
                   .append(contextID)
                   .append("\" and policy \"")
                   .append("ACCEPT")
                   .append("\"");
            LOG.debug(builder.toString());
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)))
               .append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        String key = builder.toString();

        if (!turboFilterCache.containsKey(key)) {
            ExtendedMDCFilter filter = new ExtendedMDCFilter();
            filter.addTuple(Name.SESSION_USER_ID.getName(), Integer.toString(userID));
            filter.addTuple(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID));
            filter.setOnMatch(FilterReply.ACCEPT);
            filter.setName(key);
            
            turboFilterCache.put(builder.toString(), filter);
            loggerContext.addTurboFilter(filter);
        } else {
            if (LOG.isDebugEnabled()) {
                builder.setLength(0);
                builder.append("Duplicate user filter for user with ID \"")
                        .append(userID)
                       .append("\", context with ID \"")
                       .append(contextID)
                       .append("\" and policy \"")
                       .append("ACCEPT")
                       .append("\"");
                LOG.debug(builder.toString());
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterSession(java.lang.String)
     */
    @Override
    public void filterSession(String sessionID) {
        if (LOG.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("New session filter created for session with ID \"")
                   .append(sessionID)
                   .append("\" and policy \"")
                   .append("ACCEPT")
                   .append("\"");
            LOG.debug(builder.toString());
        }
        createExtendedMDCFilter(Name.SESSION_SESSION_ID.getName(), sessionID, FilterReply.ACCEPT);        
    }
    
    /*
     * (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#setLogLevel(java.lang.String, java.lang.String[])
     */
    @Override
    public void setLogLevel(String level, String[] loggers) {
        for (String s : loggers) {
            Level l = Level.valueOf(level);
            loggerContext.getLogger(s).setLevel(l);
            dynamicallyModifiedLoggers.put(s, l);
            if (LOG.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Setting log level for \"")
                        .append(s)
                        .append("\" to \"").append(level).append("\"");
                LOG.debug(builder.toString());
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeContextFilter(int)
     */
    @Override
    public void removeContextFilter(int contextID) {
        removeFilter(createKey(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID)));
        if (LOG.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Removed context filter with context ID \"")
                   .append(contextID)
                   .append("\"");
            LOG.debug(builder.toString());
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeUserFilter(int, int)
     */
    @Override
    public void removeUserFilter(int userID, int contextID) {
        StringBuilder builder = new StringBuilder();
        builder.append(createKey(Name.SESSION_USER_ID.getName(), Integer.toString(userID)))
               .append(":").append(createKey(Name.SESSION_CONTEXT_ID.getName(), (Integer.toString(contextID))));
        removeFilter(builder.toString());
        if (LOG.isDebugEnabled()) {
            builder.setLength(0);
            builder.append("Removed user filter for user with ID \"")
                   .append(userID)
                   .append("\", context with ID \"")
                   .append(contextID)
                   .append("\" and policy \"")
                   .append("ACCEPT")
                   .append("\"");
            LOG.debug(builder.toString());
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeSessionFilter(java.lang.String)
     */
    @Override
    public void removeSessionFilter(String sessionID) {
        removeFilter(createKey(Name.SESSION_SESSION_ID.getName(), sessionID));        
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#getLoggers()
     */
    @Override
    public Set<String> listAllLoggers() {
        Set<String> loggers = new HashSet<String>();
        System.out.println(loggerContext.getLoggerList());
        StringBuilder builder = new StringBuilder();
        for(ch.qos.logback.classic.Logger l : loggerContext.getLoggerList()) {
            builder.setLength(0);
            builder.append("Logger: ").append(l.getName()).append(", Level: ").append(l.getLevel());
            loggers.add(builder.toString());
        }
        return loggers;
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#getFilters()
     */
    @Override
    public Set<String> listFilters() {
        Set<String> filters = new HashSet<String>();
        for(TurboFilter tf  : loggerContext.getTurboFilterList()) {
            filters.add(tf.getName());
        }
        return filters;
    }
    

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeAllFilters()
     */
    @Override
    public synchronized void removeAllFilters() {
        TurboFilterList list = loggerContext.getTurboFilterList();
        StringBuilder builder = new StringBuilder();
        for(String key : turboFilterCache.keySet()) {
            list.remove(turboFilterCache.get(key));
            if (LOG.isDebugEnabled()) {
                builder.setLength(0);
                builder.append("Removing filter ").append(key);
                LOG.debug(builder.toString());
            }
        }
        turboFilterCache.clear();
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#getLevelForLogger(java.lang.String)
     */
    @Override
    public Set<String> getLevelForLoggers(String[] loggers) {
        Set<String> l = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        for (String s : loggers) {
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(s);
            builder.setLength(0);
            builder.append("Logger: ").append(logger.getName()).append(", Level: ").append(logger.getLevel());
            l.add(builder.toString());
        }
        return l;
    }
    

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#listDynamicallyModifiedLoggers()
     */
    @Override
    public Set<String> listDynamicallyModifiedLoggers() {
        Set<String> loggers = new HashSet<String>();
        Iterator<String> keys = dynamicallyModifiedLoggers.keySet().iterator();
        StringBuilder builder = new StringBuilder();
        while(keys.hasNext()) {
            String k = keys.next();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(k);
            builder.setLength(0);
            builder.append("Logger: ").append(logger.getName()).append(", Level: ").append(logger.getLevel());
            loggers.add(builder.toString());
        }
        return loggers;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo)
     */
    @Override
    protected final String getDescription(MBeanInfo info) {
        return DESCRIPTION;
    }
    
    
    /*
     * (non-Javadoc)
     * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo)
     */
    @Override
    protected final String getDescription(MBeanOperationInfo info) {
        return methodDescriptions.get(info.getName());
    }
    
    /*
     * (non-Javadoc)
     * @see javax.management.StandardMBean#getDescription(javax.management.MBeanOperationInfo, javax.management.MBeanParameterInfo, int)
     */
    @Override
    protected final String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        return getMBeanOperationInfo(methodParameterDescriptions, op, param, sequence);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.management.StandardMBean#getParameterName(javax.management.MBeanOperationInfo, javax.management.MBeanParameterInfo, int)
     */
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
        if (v == null || v.length == 0 || sequence > v.length)
            return super.getDescription(op, param, sequence);
        return v[sequence];
    }
    
    /**
     * Create an MDCFilter based on the specified key/value/filter
     * @param key
     * @param value
     * @param onMatch
     * @return
     */
    private final void createExtendedMDCFilter(String key, String value, FilterReply onMatch) {
        String k = createKey(key, value);
        if (!turboFilterCache.containsKey(k)) {
            ExtendedMDCFilter filter = new ExtendedMDCFilter();
            filter.addTuple(key, value);
            filter.setOnMatch(onMatch);
            filter.setName(k);
        
            turboFilterCache.put(createKey(key, value), filter);
            loggerContext.addTurboFilter(filter);
        } else {
            if (LOG.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Duplicate filter for \"")
                        .append(key)
                        .append("\" with ID \"")
                        .append(value)
                       .append("\" and policy \"")
                       .append("ACCEPT")
                       .append("\"");
                LOG.debug(builder.toString());
            }
        }
    }
    
    /**
     * Remove the specified filter
     * @param key
     * @param value
     */
    private final void removeFilter(String key) {
        TurboFilterList list = loggerContext.getTurboFilterList();
        if (list.remove(turboFilterCache.get(key))) {
            turboFilterCache.remove(key);
        }
    }
    
    /**
     * Create key
     * @param key
     * @param value
     * @return
     */
    private String createKey(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(key).append("=").append(value);
        return builder.toString();
    }
}
