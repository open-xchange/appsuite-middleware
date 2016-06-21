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

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.logging.LogLevelService;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.internal.LogLevelServiceImpl;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;
import com.openexchange.management.ManagementService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusListenerAsList;
import ch.qos.logback.core.status.StatusManager;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Activator implements BundleActivator, Reloadable {

    protected static final String LOGIN_PERFORMER = "com.openexchange.login.internal.LoginPerformer";
    protected static final String SESSION_HANDLER = "com.openexchange.sessiond.impl.SessionHandler";
    private static final String CONFIGFILE = "logback.xml";
    private static final String[] PROPERTIES = new String[] { "all properties in file" };

    /** The logger */
    protected static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    // ----------------------------------------------------------------------------------- //

    private volatile ServiceTracker<ManagementService, ManagementService> managementTracker;
    private volatile ServiceTracker<ConfigurationService, ConfigurationService> configurationTracker;
    private volatile RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private volatile ServiceRegistration<IncludeStackTraceService> includeStackTraceServiceRegistration;
    private ServiceRegistration<Reloadable> reloadable;
    private ServiceRegistration<LogLevelService> logLevelService;

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

        // Initialisation stuff for JUL/JCL bridges
        configureJavaUtilLogging();
        overrideLoggerLevels(loggerContext);
        installJulLevelChangePropagator(loggerContext);

        // The ranking-aware turbo filter list - itself acting as a turbo filter
        initialiseRankingAwareTurboFilterList(loggerContext);

        // Register services
        final IncludeStackTraceServiceImpl serviceImpl = new IncludeStackTraceServiceImpl();
        registerLoggingConfigurationMBean(context, loggerContext, rankingAwareTurboFilterList, serviceImpl);
        registerExceptionCategoryFilter(context, rankingAwareTurboFilterList, serviceImpl);
        registerIncludeStackTraceService(serviceImpl, context);
        reloadable = context.registerService(Reloadable.class, this, null);

        logLevelService = context.registerService(LogLevelService.class, new LogLevelServiceImpl(), null);
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

        if (logLevelService != null) {
            logLevelService.unregister();
            logLevelService = null;
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
                    LOGGER.info("Configured log level {} for class {} is too coarse. It is changed to INFO!", level, className);
                }
            } else {
                LOGGER.warn("Not able to check (and set) the log level to INFO for class: {}", className);
            }
        }
    }

    /**
     * Initialise the {@link RankingAwareTurboFilterList} and register itself acting as a turbo filter
     * 
     * @param loggerContext The {@link LoggerContext}
     */
    private void initialiseRankingAwareTurboFilterList(LoggerContext loggerContext) {
        final RankingAwareTurboFilterList rankingAwareTurboFilterList = new RankingAwareTurboFilterList();
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        loggerContext.addTurboFilter(rankingAwareTurboFilterList);
    }

    /**
     * Register the LoggingConfigurationMBean
     */
    protected void registerLoggingConfigurationMBean(final BundleContext context, final LoggerContext loggerContext, final RankingAwareTurboFilterList turboFilterList, final IncludeStackTraceServiceImpl serviceImpl) {
        try {
            LogbackConfigurationMBeanRegisterer logbackConfigurationMBeanRegisterer = new LogbackConfigurationMBeanRegisterer(context, turboFilterList, serviceImpl);
            ServiceTracker<ManagementService, ManagementService> tracker = new ServiceTracker<ManagementService, ManagementService>(context, ManagementService.class, logbackConfigurationMBeanRegisterer);
            this.managementTracker = tracker;
            tracker.open();
        } catch (final Exception e) {
            LOGGER.error("Could not register LogbackConfigurationMBean", e);
        }

        LOGGER.info("LoggingConfigurationMBean successfully registered.");
    }

    /**
     * Register the exception category filter
     * 
     * @param context The bundle context
     * @param turboFilterList The ranking aware turbo filter list
     * @param serviceImpl The include stack trace service
     */
    protected void registerExceptionCategoryFilter(final BundleContext context, final RankingAwareTurboFilterList turboFilterList, IncludeStackTraceServiceImpl serviceImpl) {
        ExceptionCategoryFilterRegisterer exceptionCategoryFilterRegisterer = new ExceptionCategoryFilterRegisterer(context, turboFilterList, serviceImpl);
        final ServiceTracker<ConfigurationService, ConfigurationService> tracker = new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, exceptionCategoryFilterRegisterer);
        configurationTracker = tracker;
        tracker.open();
    }

    /**
     * Register the include stacktrace service
     * 
     * @param serviceImpl The implementation
     * @param context The bundle context
     */
    protected void registerIncludeStackTraceService(final IncludeStackTraceServiceImpl serviceImpl, final BundleContext context) {
        includeStackTraceServiceRegistration = context.registerService(IncludeStackTraceService.class, serviceImpl, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#reloadConfiguration(com.openexchange.config.ConfigurationService)
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(loggerContext);
        URL url = ci.findURLOfDefaultConfigurationFile(true);
        StatusListenerAsList statusListenerAsList = new StatusListenerAsList();
        StatusManager sm = loggerContext.getStatusManager();
        loggerContext.reset();
        // after a reset the statusListenerAsList gets removed as a listener
        sm.add(statusListenerAsList);
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(url);

            // Restore the ranking aware turbo filer list to the logger context
            loggerContext.addTurboFilter(rankingAwareTurboFilterList);
        } catch (JoranException e) {
            LOGGER.error("Error reloading logback configuration: {}", e);
        } finally {
            sm.remove(statusListenerAsList);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#getConfigfileNames()
     */
    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put(CONFIGFILE, PROPERTIES);
        return map;
    }

}
