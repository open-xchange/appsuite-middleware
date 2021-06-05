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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.logback.extensions.appenders.AbstractRemoteAppender;
import com.openexchange.management.ManagementService;
import ch.qos.logback.classic.LoggerContext;

/**
 * {@link AbstractRemoteAppenderMBeanRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
abstract class AbstractRemoteAppenderMBeanRegisterer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    protected static Logger LOGGER = LoggerFactory.getLogger(AbstractRemoteAppenderMBeanRegisterer.class);

    private final String appenderName;
    private final BundleContext context;
    private ObjectName mbeanName;

    /**
     * Initializes a new {@link AbstractRemoteAppenderMBeanRegisterer}.
     */
    public AbstractRemoteAppenderMBeanRegisterer(BundleContext context, String appenderName) {
        super();
        this.context = context;
        this.appenderName = appenderName;
    }

    @Override
    public synchronized ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        if (!isRemoteAppenderEnabled()) {
            context.ungetService(reference);
            return null;
        }

        if (this.mbeanName != null) {
            // Already registered
            return managementService;
        }

        AbstractRemoteAppender<?> remoteAppender = getRemoteAppender();
        if (null == remoteAppender) {
            // "com.openexchange.java-commons.logback-extensions" bundle not yet started... Add a listener for it to register its MBean later on
            context.addBundleListener(new LogbackExtensionsBundleListener(this, managementService));
            return managementService;
        }
        try {
            ObjectName objectName = getObjectName();
            managementService.registerMBean(objectName, remoteAppender);
            this.mbeanName = objectName;
            LOGGER.info("Successfully registered MBean for {} appender.", getAppenderName());
        } catch (Exception e) {
            LOGGER.error("Failed to register MBean for {} appender.", getAppenderName(), e);
        }
        return managementService;
    }

    @Override
    public synchronized void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // nope
    }

    @Override
    public synchronized void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        if (null == service) {
            return;
        }
        ObjectName objectName = this.mbeanName;
        if (objectName != null) {
            this.mbeanName = null;
            try {
                service.unregisterMBean(objectName);
                LOGGER.info("Successfully unregistered MBean for {} appender.", getAppenderName());
            } catch (Exception e) {
                LOGGER.warn("Failed to unregister MBean for {} appender.", getAppenderName(), e);
            }
        }
        context.ungetService(reference);
    }

    /**
     * Sets the mbeanName
     *
     * @param mbeanName The mbeanName to set
     */
    synchronized void setMbeanName(ObjectName mbeanName) {
        this.mbeanName = mbeanName;
    }

    /**
     * Gets the appenderName
     *
     * @return The appenderName
     */
    public String getAppenderName() {
        return appenderName;
    }

    /**
     * Returns the <code>enabled</code> property name
     *
     * @return the <code>enabled</code> property name
     */
    abstract String getEnabledPropertyName();

    /**
     * Returns the mbean's object name
     *
     * @return the mbean's object name
     * @throws MalformedObjectNameException if the object name is malformed
     */
    abstract ObjectName getObjectName() throws MalformedObjectNameException;

    /**
     * Returns the instance of the remote appender
     *
     * @return the instance of the remote appender
     */
    abstract AbstractRemoteAppender<?> getRemoteAppender();

    /**
     * Returns whether the remote appender is enabled
     *
     * @return <code>true</code> if the appender is enabled; <code>false</code> otherwise
     */
    private boolean isRemoteAppenderEnabled() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return Boolean.parseBoolean(loggerContext.getProperty(getEnabledPropertyName()));
    }

    //////////////////////////////////////// NESTED /////////////////////////////////

    /**
     * {@link LogbackExtensionsBundleListener}
     */
    private static final class LogbackExtensionsBundleListener implements BundleListener {

        private final AbstractRemoteAppenderMBeanRegisterer registerer;
        private final ManagementService managementService;

        /**
         * Initializes a new {@link BundleListenerImplementation}.
         *
         * @param registerer The remote appender MBean registerer instance
         * @param managementService The {@link ManagementService} instance
         */
        LogbackExtensionsBundleListener(AbstractRemoteAppenderMBeanRegisterer registerer, ManagementService managementService) {
            super();
            this.registerer = registerer;
            this.managementService = managementService;
        }

        @Override
        public void bundleChanged(BundleEvent event) {
            if (BundleEvent.STARTED != event.getType() || !"com.openexchange.java-commons.logback-extensions".equals(event.getBundle().getSymbolicName())) {
                return;
            }
            synchronized (registerer) {
                AbstractRemoteAppender<?> remoteAppender = registerer.getRemoteAppender();
                if (null == remoteAppender) {
                    LOGGER.error("{} appender has been enabled, but not initialized. Please ensure bundle \"com.openexchange.java-commons.logback-extensions\" ('logback-extensions') is orderly started", registerer.getAppenderName());
                    return;
                }
                try {
                    ObjectName objectName = registerer.getObjectName();
                    managementService.registerMBean(objectName, remoteAppender);
                    registerer.setMbeanName(objectName);
                    LOGGER.info("Successfully registered MBean for {} appender.", registerer.getAppenderName());
                } catch (Exception e) {
                    LOGGER.error("Failed to register MBean for {} appender.", registerer.getAppenderName(), e);
                }
            }
        }
    }
}
