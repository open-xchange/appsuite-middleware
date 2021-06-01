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

package com.openexchange.logging.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppender;
import com.openexchange.logback.extensions.logstash.LogstashSocketAppenderMBean;
import com.openexchange.management.ManagementService;
import ch.qos.logback.classic.LoggerContext;

/**
 * {@link DeprecatedLogstashSocketAppenderMBeanRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @deprecated
 */
@Deprecated
public class DeprecatedLogstashSocketAppenderMBeanRegisterer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    protected static Logger LOGGER = LoggerFactory.getLogger(DeprecatedLogstashSocketAppenderMBeanRegisterer.class);

    private final BundleContext context;
    private ObjectName logstashConfName; // guarded by synchronized

    /**
     * Initialises a new {@link DeprecatedLogstashSocketAppenderMBeanRegisterer}.
     */
    public DeprecatedLogstashSocketAppenderMBeanRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Register optional LogstashSocketAppenderMBean
        boolean logstashEnabled = Boolean.parseBoolean(loggerContext.getProperty("com.openexchange.logback.extensions.logstash.enabled"));
        if (!logstashEnabled) {
            return managementService;
        }

        LogstashSocketAppender logstashSocketAppender = LogstashSocketAppender.getInstance();
        if (null == logstashSocketAppender) {
            // "com.openexchange.java-commons.logback-extensions" bundle not yet started... Add a listener for it to register its MBean later on
            context.addBundleListener(new LogbackExtensionsBundleListener(this, managementService));
        } else {
            try {
                ObjectName logstashConfName = new ObjectName(LogstashSocketAppenderMBean.DOMAIN, LogstashSocketAppenderMBean.KEY, LogstashSocketAppenderMBean.VALUE);
                managementService.registerMBean(logstashConfName, logstashSocketAppender);
                this.logstashConfName = logstashConfName;
                LOGGER.warn("DeprecatedLogstashAppenderMBean registered. The 'com.openexchange.logback.extensions.logstash.LogstashSocketAppender' is deprecated and subject to be removed. Please use 'com.openexchange.logback.extensions.appenders.logstash.LogstashAppender' instead. ");
            } catch (Exception e) {
                LOGGER.error("Could not register LogstashSocketAppenderMBean", e);
            }
        }
        return managementService;
    }

    @Override
    public synchronized void modifiedService(ServiceReference<ManagementService> reference, ManagementService managementService) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ManagementService> reference, ManagementService managementService) {
        if (null == managementService) {
            return;
        }

        ObjectName logstashConfName = this.logstashConfName;
        if (logstashConfName != null) {
            try {
                managementService.unregisterMBean(logstashConfName);
                LOGGER.info("LogstashSocketAppenderMBean successfully unregistered.");
            } catch (Exception e) {
                LOGGER.warn("Could not unregister LogstashSocketAppenderMBean", e);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    private static final class LogbackExtensionsBundleListener implements BundleListener {

        private final DeprecatedLogstashSocketAppenderMBeanRegisterer registerer;
        private final ManagementService managementService;

        /**
         * Initializes a new {@link BundleListenerImplementation}.
         */
        LogbackExtensionsBundleListener(DeprecatedLogstashSocketAppenderMBeanRegisterer registerer, ManagementService managementService) {
            super();
            this.registerer = registerer;
            this.managementService = managementService;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void bundleChanged(BundleEvent event) {
            if (BundleEvent.STARTED == event.getType() && "com.openexchange.java-commons.logback-extensions".equals(event.getBundle().getSymbolicName())) {
                synchronized (registerer) {
                    LogstashSocketAppender logstashSocketAppender = LogstashSocketAppender.getInstance();
                    if (null == logstashSocketAppender) {
                        LOGGER.warn("Logstash socket appender has been enabled, but not initialized. Please ensure bundle \"com.openexchange.java-commons.logback-extensions\" ('logback-extensions') is orderly started");
                    } else {
                        try {
                            ObjectName logstashConfName = new ObjectName(LogstashSocketAppenderMBean.DOMAIN, LogstashSocketAppenderMBean.KEY, LogstashSocketAppenderMBean.VALUE);
                            managementService.registerMBean(logstashConfName, logstashSocketAppender);
                            registerer.logstashConfName = logstashConfName;
                        } catch (Exception e) {
                            LOGGER.warn("Failed to register MBean for logstash socket appender.", e);
                        }
                    }
                }
            }
        }
    } // End of LogbackExtensionsBundleListener class

}
