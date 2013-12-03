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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    
    /**
     * Initializes a new {@link LogbackConfiguration}.
     * @throws NotCompliantMBeanException 
     */
    public LogbackConfiguration() throws NotCompliantMBeanException {
        super(LogbackConfigurationMBean.class);
        loggerContext.getTurboFilterList().get(0).setName("DEFAULT");
        configurator.setContext(loggerContext);
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
            loggerContext.getLogger(s).setLevel(Level.valueOf(level));
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
    public Set<String> getLoggers() {
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
    public Set<String> getFilters() {
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
