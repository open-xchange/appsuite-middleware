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
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.MDCFilter;
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
        configurator.setContext(loggerContext);
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterContext(int)
     */
    @Override
    public void filterContext(int contextID) {
        if (LOG.isDebugEnabled())
            LOG.debug("new context filter for context " + contextID);
        loggerContext.addTurboFilter(createMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID), FilterReply.ACCEPT));
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterUser(int, int)
     */
    @Override
    public void filterUser(int userID, int contextID) {
        LOG.info("new user filter for user " + userID + "and context " + contextID);
        //loggerContext.addTurboFilter(createMDCFilter(Name.SESSION_USER_ID.getName(), Integer.toString(userID), FilterReply.ACCEPT));
        //loggerContext.addTurboFilter(createMDCFilter(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID), FilterReply.ACCEPT));
        ExtendedMDCFilter filter = new ExtendedMDCFilter(userID + ":" + contextID);
        filter.addTuple(Name.SESSION_USER_ID.getName(), Integer.toString(userID));
        filter.addTuple(Name.SESSION_CONTEXT_ID.getName(), Integer.toString(contextID));
        loggerContext.addTurboFilter(filter);
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackLoggingConfigurationMBean#filterSession(java.lang.String)
     */
    @Override
    public void filterSession(String sessionID) {
        LOG.info("new session filter for session " + sessionID);
        loggerContext.addTurboFilter(createMDCFilter(Name.SESSION_SESSION_ID.getName(), sessionID, FilterReply.ACCEPT));        
    }
    
    /*
     * (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#setLogLevel(java.lang.String, java.lang.String)
     */
    @Override
    public void setLogLevel(String level, String logger) {
        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();
        for (ch.qos.logback.classic.Logger l : loggers) {
            if (l.getName().equals(logger)) {
                l.setLevel(Level.valueOf(level));
                LOG.info("Setting log level to " + level);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#resetLogger(java.lang.String)
     */
    @Override
    public void resetLogger(String logger) {
        //TODO
    }


    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeContextFilter(int)
     */
    @Override
    public void removeContextFilter(int contextID) {
        removeFilter(Name.SESSION_CONTEXT_ID, Integer.toString(contextID));
    }


    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeUserFilter(int, int)
     */
    @Override
    public void removeUserFilter(int userID, int contextID) {
        //removeFilter(new ExtendedMDCFilter(userID + ":" + contextID));
        //removeFilter(Name.SESSION_CONTEXT_ID, Integer.toString(contextID));
        //removeFilter(Name.SESSION_USER_ID, Integer.toString(userID));
    }


    /* (non-Javadoc)
     * @see com.openexchange.logging.mbean.LogbackConfigurationMBean#removeSessionFilter(java.lang.String)
     */
    @Override
    public void removeSessionFilter(String sessionID) {
        removeFilter(Name.SESSION_SESSION_ID, sessionID);        
    }
    
    /**
     * Create an MDCFilter based on the specified key/value/filter
     * @param key
     * @param value
     * @param onMatch
     * @return
     */
    private final MDCFilter createMDCFilter(String key, String value, FilterReply onMatch) {
        MDCFilter filter = new MDCFilter();
        filter.setName(key.toString());
        filter.setValue(value);
        filter.setOnMatch(onMatch.toString());
        
        StringBuilder builder = new StringBuilder();
        builder.append(key.toString()).append("=").append(value);
        turboFilterCache.put(builder.toString(), filter);
        
        return filter;
    }
    
    /**
     * Remove the specified filter
     * @param key
     * @param value
     */
    private final void removeFilter(Name key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(key).append("=").append(value);
        
        TurboFilterList list = loggerContext.getTurboFilterList();
        if (list.remove(turboFilterCache.get(builder.toString()))) {
            turboFilterCache.remove(builder.toString());
        }
    }

}
