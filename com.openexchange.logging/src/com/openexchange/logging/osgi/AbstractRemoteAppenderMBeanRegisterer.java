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
 *    trademarks of the OX Software GmbH. group of companies.
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
 * @since v7.10.4
 */
@SuppressWarnings("unused")
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
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        if (!isRemoteAppenderEnabled()) {
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
        } catch (Exception e) {
            LOGGER.error("Could not register {}", getRemoteAppender().getClass().getSimpleName(), e);
        }
        return managementService;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // nope
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        if (null == service) {
            return;
        }
        ObjectName objectName = this.mbeanName;
        if (objectName == null) {
            return;
        }
        try {
            service.unregisterMBean(objectName);
            LOGGER.info("{} successfully unregistered.", getRemoteAppender().getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.warn("Could not unregister {}", getRemoteAppender().getClass().getSimpleName(), e);
        }
    }

    /**
     * Sets the mbeanName
     *
     * @param mbeanName The mbeanName to set
     */
    void setMbeanName(ObjectName mbeanName) {
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
         * @param registerer The remote appender mbean registerer instnace
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
                } catch (Exception e) {
                    LOGGER.error("Failed to register MBean for {} appender.", registerer.getAppenderName(), e);
                }
            }
        }
    }
}
