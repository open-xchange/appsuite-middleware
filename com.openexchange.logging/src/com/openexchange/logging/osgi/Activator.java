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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import javax.management.ObjectName;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.turbo.TurboFilter;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.logging.LogConfigReloadable;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;
import com.openexchange.logging.mbean.LogbackConfiguration;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;
import com.openexchange.logging.mbean.RankingAwareTurboFilterList;
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

    /** The logger */
    protected static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    // ----------------------------------------------------------------------------------- //

    private volatile ServiceTracker<ManagementService, ManagementService> managementTracker;
    private volatile ServiceTracker<ConfigurationService, ConfigurationService> configurationTracker;
    private volatile RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private volatile ServiceRegistration<IncludeStackTraceService> includeStackTraceServiceRegistration;

    private ServiceRegistration<Reloadable> reloadable;

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
        LOGGER.info("starting bundle com.openexchange.logging");

        // Obtain logger context
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // Mark first as "DEFAULT"
        if (!loggerContext.getTurboFilterList().isEmpty()) {
            final TurboFilter filter = loggerContext.getTurboFilterList().get(0);
            if (null == filter.getName()) {
                filter.setName("DEFAULT");
            }
        }

        // Initialization stuff for JUL/JCL bridges
        configureJavaUtilLogging();
        overrideLoggerLevels(loggerContext);
        installJulLevelChangePropagator(loggerContext);

        // The ranking-aware turbo filter list - itself acting as a turbo filter
        final RankingAwareTurboFilterList rankingAwareTurboFilterList = new RankingAwareTurboFilterList();
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        loggerContext.addTurboFilter(rankingAwareTurboFilterList);

        final IncludeStackTraceServiceImpl serviceImpl = new IncludeStackTraceServiceImpl();

        registerLoggingConfigurationMBean(context, loggerContext, rankingAwareTurboFilterList, serviceImpl);

        registerExceptionCategoryFilter(context, rankingAwareTurboFilterList, serviceImpl);

        registerIncludeStackTraceService(serviceImpl, context);

        reloadable = context.registerService(Reloadable.class, new LogConfigReloadable(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.info("stopping bundle com.openexchange.logging");

        final ServiceTracker<ManagementService, ManagementService> managementTracker = this.managementTracker;
        if (null != managementTracker) {
            managementTracker.close();
            this.managementTracker = null;
        }

        final ServiceTracker<ConfigurationService, ConfigurationService> configurationTracker = this.configurationTracker;
        if (null != configurationTracker) {
            configurationTracker.close();
            this.configurationTracker = null;
        }

        final RankingAwareTurboFilterList rankingAwareTurboFilterList = this.rankingAwareTurboFilterList;
        if (null != rankingAwareTurboFilterList) {
            // Obtain logger context
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getTurboFilterList().remove(rankingAwareTurboFilterList);
            rankingAwareTurboFilterList.clear();
            this.rankingAwareTurboFilterList = null;
        }

        final ServiceRegistration<IncludeStackTraceService> includeStackTraceServiceRegistration = this.includeStackTraceServiceRegistration;
        if (null != includeStackTraceServiceRegistration) {
            includeStackTraceServiceRegistration.unregister();
            this.includeStackTraceServiceRegistration = null;
        }

        if (null != reloadable) {
            reloadable.unregister();
            reloadable = null;
        }
    }

    /**
     * Installs the logback LevelChangePropagator if the descriptive configuration was removed by the admin.
     */
    protected void installJulLevelChangePropagator(final LoggerContext loggerContext) {
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
        if (collection == null) {
            throw new IllegalArgumentException("The collection is null");
        }

        for (Object o : collection) {
            if (o != null && o.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overrides the log level for LoginPerformer and SessionHandler in case that the administrator removed or changed the logging level to
     * coarser settings. If the settings are finer (TRACE, DEBUG, ALL) this level will not be overridden.
     */
    protected void overrideLoggerLevels(final LoggerContext loggerContext) {
        String disableOverrideLogLevels = loggerContext.getProperty("com.openexchange.logging.disableOverrideLogLevels");
        if ("true".equalsIgnoreCase(disableOverrideLogLevels)) {
            return;
        }

        for (final String className : new String[] { LOGIN_PERFORMER, SESSION_HANDLER }) {
            ch.qos.logback.classic.Logger lLogger = loggerContext.getLogger(className);

            if (lLogger != null) {
                Level level = lLogger.getLevel();

                if (level == null || level.isGreaterOrEqual(Level.WARN)) {
                    lLogger.setLevel(Level.INFO);
                    LOGGER.info(
                        "Configured log level {} for class {} is too coarse. It is changed to INFO!",
                        level,
                        className);
                }
            } else {
                LOGGER.warn("Not able to check (and set) the log level to INFO for class: {}", className);
            }
        }
    }

    /**
     * Register the LoggingConfigurationMBean
     */
    protected void registerLoggingConfigurationMBean(final BundleContext context, final LoggerContext loggerContext, final RankingAwareTurboFilterList turboFilterList, final IncludeStackTraceServiceImpl serviceImpl) {
        try {
            final ServiceTracker<ManagementService, ManagementService> tracker = new ServiceTracker<ManagementService, ManagementService>(context, ManagementService.class, new ServiceTrackerCustomizer<ManagementService, ManagementService>() {

                private volatile ObjectName logbackConfObjName;
                private volatile LogbackConfiguration logbackConfiguration;

                @Override
                public synchronized ManagementService addingService(ServiceReference<ManagementService> reference) {
                    ManagementService managementService = context.getService(reference);
                    try {
                        final ObjectName logbackConfObjName = new ObjectName(LogbackConfigurationMBean.DOMAIN, LogbackConfigurationMBean.KEY, LogbackConfigurationMBean.VALUE);
                        this.logbackConfObjName = logbackConfObjName;
                        // Register MBean
                        final LogbackConfiguration logbackConfiguration = new LogbackConfiguration(loggerContext, turboFilterList, serviceImpl);
                        this.logbackConfiguration = logbackConfiguration;
                        managementService.registerMBean(logbackConfObjName, logbackConfiguration);
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
            });
            this.managementTracker = tracker;
            tracker.open();
        } catch (final Exception e) {
            LOGGER.error("Could not register LogbackConfigurationMBean", e);
        }

        LOGGER.info("LoggingConfigurationMBean successfully registered.");
    }

    protected void registerExceptionCategoryFilter(final BundleContext context, final RankingAwareTurboFilterList turboFilterList, IncludeStackTraceServiceImpl serviceImpl) {
        final ServiceTracker<ConfigurationService, ConfigurationService> tracker = new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, new ExceptionCategoryFilterRegisterer(context, turboFilterList, serviceImpl));
        configurationTracker = tracker;
        tracker.open();
    }

    protected void registerIncludeStackTraceService(final IncludeStackTraceServiceImpl serviceImpl, final BundleContext context) {
        includeStackTraceServiceRegistration = context.registerService(IncludeStackTraceService.class, serviceImpl, null);
    }

}
