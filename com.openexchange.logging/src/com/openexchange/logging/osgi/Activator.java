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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Collection;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.lang.Validate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.logging.mbean.LogbackConfiguration;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;
import com.openexchange.management.ManagementService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Activator implements BundleActivator {

    protected static final String LOGIN_PERFORMER = "com.openexchange.login.internal.LoginPerformer";

    protected static final String SESSION_HANDLER = "com.openexchange.sessiond.impl.SessionHandler";

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    private final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    private ManagementService managementService;

    private ObjectName logbackConfObjName;

    private LogbackConfigurationMBean logbackConfMBean;

    /*
     * Do not implement HousekeepingActivator, track services if you need them!
     * This bundle must start as early as possible to configure the java.util.logging bridge.
     */
    public Activator() {
        super();
    }

    protected void configureJavaUtilLogging() {
        // We configure a special j.u.l handler that routes logging to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("starting bundle com.openexchange.logging");
        configureJavaUtilLogging();
        overrideLoggerLevels();
        installJulLevelChangePropagator();
        registerLoggingConfigurationMBean(context);
        ServiceTracker<ConfigurationService, ConfigurationService> tracker = new ServiceTracker<ConfigurationService, ConfigurationService>(
            context,
            ConfigurationService.class,
            new ExceptionCategoryFilterRegisterer(context));
        tracker.open();
    }

    /**
     * Installs the logback LevelChangePropagator if the descriptive configuration was removed by the admin.
     */
    protected void installJulLevelChangePropagator() {
        List<LoggerContextListener> loggerContextListener = loggerContext.getCopyOfListenerList();

        if (!hasInstanceOf(loggerContextListener, LevelChangePropagator.class)) {
            LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
            levelChangePropagator.setContext(loggerContext);
            levelChangePropagator.start();
            loggerContext.addListener(levelChangePropagator);
        }
    }

    /**
     * Checks, if the given class is in the given collection.
     * 
     * @param collection - the collection to verify
     * @param clazz - the class to search for
     * @return true, if an object of that class is available within the collection. Otherwise false.
     */
    protected <T> boolean hasInstanceOf(Collection<?> collection, Class<T> clazz) {
        Validate.notNull(collection);

        for (Object o : collection) {
            if (o != null && o.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overrides the log level for LoginPerformer and SessionHandler in case that the administrator removed or changed the logging level.
     */
    protected void overrideLoggerLevels() {
        for (final String className : new String[] { LOGIN_PERFORMER, SESSION_HANDLER }) {
            ch.qos.logback.classic.Logger lLogger = loggerContext.getLogger(className);
            if (lLogger != null) {
                lLogger.setLevel(Level.INFO);
            } else {
                logger.warn("Not able to override the log level to INFO for class: {}", className);
            }
        }
    }

    /**
     * Register the LoggingConfigurationMBean
     */
    protected void registerLoggingConfigurationMBean(final BundleContext context) {
        try {
            logbackConfObjName = new ObjectName(
                LogbackConfigurationMBean.DOMAIN,
                LogbackConfigurationMBean.KEY,
                LogbackConfigurationMBean.VALUE);
            logbackConfMBean = new LogbackConfiguration();

            ServiceTracker<ManagementService, ManagementService> tracker = new ServiceTracker<ManagementService, ManagementService>(
                context,
                ManagementService.class,
                new ServiceTrackerCustomizer<ManagementService, ManagementService>() {
                    @Override
                    public synchronized ManagementService addingService(ServiceReference<ManagementService> reference) {
                        managementService = context.getService(reference);
                        try {
                            managementService.registerMBean(logbackConfObjName, logbackConfMBean);
                        } catch (OXException e) {
                            logger.error("Could not register LogbackConfigurationMBean", e);
                        }
                        return managementService;
                    }

                    @Override
                    public synchronized void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
                    }

                    @Override
                    public synchronized void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
                        if (managementService != null) {
                            try {
                                managementService.unregisterMBean(logbackConfObjName);
                                managementService = null;
                            } catch (OXException e) {
                                logger.warn("Could not unregister LogbackConfigurationMBean", e);
                            }
                        }
                    }
                });
            tracker.open();
        } catch (MalformedObjectNameException e) {
            logger.error("Could not register LogbackConfigurationMBean", e);
        } catch (NullPointerException e) {
            logger.error("Could not register LogbackConfigurationMBean", e);
        } catch (NotCompliantMBeanException e) {
            logger.error("Could not register LogbackConfigurationMBean", e);
        }

        logger.info("LoggingConfigurationMBean successfully registered.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("stopping bundle com.openexchange.logging");
        if (managementService != null && logbackConfObjName != null) {
            managementService.unregisterMBean(logbackConfObjName);
            logbackConfMBean = null;
            logger.info("LoggingConfigurationMBean successfully unregistered.");
        }
    }
}
