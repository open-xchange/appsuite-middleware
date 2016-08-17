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

package com.openexchange.logging.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppender;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppenderMBean;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;
import com.openexchange.logging.mbean.LogbackConfiguration;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;
import com.openexchange.management.ManagementService;
import ch.qos.logback.classic.LoggerContext;

/**
 * {@link LogbackConfigurationMBeanRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogbackConfigurationMBeanRegisterer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    protected static Logger LOGGER = LoggerFactory.getLogger(LogbackConfigurationMBeanRegisterer.class);

    private BundleContext context;
    private volatile ObjectName logbackConfObjName;
    private volatile LogbackConfiguration logbackConfiguration;

    private RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final IncludeStackTraceServiceImpl stackTraceService;

    /**
     * Initialises a new {@link LogbackConfigurationMBeanRegisterer}.
     */
    public LogbackConfigurationMBeanRegisterer(BundleContext context, RankingAwareTurboFilterList rankingAwareTurboFilterList, IncludeStackTraceServiceImpl stackTraceService) {
        super();
        this.context = context;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.stackTraceService = stackTraceService;
    }

    @Override
    public synchronized ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            final ObjectName logbackConfObjName = new ObjectName(LogbackConfigurationMBean.DOMAIN, LogbackConfigurationMBean.KEY, LogbackConfigurationMBean.VALUE);
            this.logbackConfObjName = logbackConfObjName;

            // Register MBean
            final LogbackConfiguration logbackConfiguration = new LogbackConfiguration(loggerContext, rankingAwareTurboFilterList, stackTraceService);
            this.logbackConfiguration = logbackConfiguration;
            managementService.registerMBean(logbackConfObjName, logbackConfiguration);

            // Register Logstash Appender MBean
            {
                boolean logstash = Boolean.parseBoolean(loggerContext.getProperty("com.openexchange.logback.extensions.logstash.enabled"));
                if (logstash) {
                    final ObjectName logstashConfName = new ObjectName(LogstashSocketAppenderMBean.DOMAIN, LogstashSocketAppenderMBean.KEY, LogstashSocketAppenderMBean.VALUE);
                    managementService.registerMBean(logstashConfName, LogstashSocketAppender.getInstance());
                }
            }
            return managementService;
        } catch (final Exception e) {
            LOGGER.error("Could not register LogbackConfigurationMBean", e);
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public synchronized void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        if (service != null) {
            try {
                final ObjectName logbackConfObjName = this.logbackConfObjName;
                if (logbackConfObjName != null) {
                    service.unregisterMBean(logbackConfObjName);
                    LOGGER.info("LoggingConfigurationMBean successfully unregistered.");
                }
                final LogbackConfiguration logbackConfiguration = this.logbackConfiguration;
                if (null != logbackConfiguration) {
                    logbackConfiguration.dispose();
                    this.logbackConfiguration = null;
                }
            } catch (OXException e) {
                LOGGER.warn("Could not unregister LogbackConfigurationMBean", e);
            }
        }
    }
}
