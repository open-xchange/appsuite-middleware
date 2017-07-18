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

package com.openexchange.monitoring.osgi;

import java.util.Arrays;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.monitoring.MonitorService;
import com.openexchange.monitoring.internal.MonitorImpl;
import com.openexchange.monitoring.internal.MonitoringInit;
import com.openexchange.monitoring.sockets.SocketMonitoringService;
import com.openexchange.monitoring.sockets.TracingSocketMonitor;
import com.openexchange.monitoring.sockets.TracingSocketMonitor.TracingSocketMonitorConfig;
import com.openexchange.monitoring.sockets.internal.SocketMonitoringSystem;
import com.openexchange.net.HostList;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link MonitoringActivator} - The {@link BundleActivator activator} for monitoring bundle.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MonitoringActivator extends HousekeepingActivator implements Reloadable {

    private MonitoringInit init;
    private TracingSocketMonitor tracingSocketMonitor;
    private ServiceRegistration<SocketMonitoringService> socketMonitoringRegistration;
    private ServiceTracker<ManagementService, ManagementService> managementServiceTracker;
    private ServiceTracker<TimerService, TimerService> timerServiceTracker;

    /**
     * Initializes a new {@link MonitoringActivator}
     */
    public MonitoringActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ManagementService.class, SessiondService.class, TimerService.class };
    }

    @Override
    public synchronized void startBundle() throws Exception {
        MonitoringInit init = MonitoringInit.newInstance(this);
        init.start();
        this.init = init;

        ConfigurationService service = getService(ConfigurationService.class);

        rememberTracker(new MailCounterServiceTracker(context));
        rememberTracker(new MailIdleCounterServiceTracker(context));
        openTrackers();

        enableAndConfigureMemoryMonitoring(service);

        enableAndConfigureSocketMonitoring(service);

        /*
         * Register monitor service
         */
        registerService(MonitorService.class, new MonitorImpl(), null);

        registerService(Reloadable.class, this, null);
    }

    private void enableAndConfigureMemoryMonitoring(ConfigurationService configService) {
        dropMemoryMonitoring();

        boolean enableMemoryMonitoring = configService.getBoolProperty("com.openexchange.monitoring.memory.enabled", true);
        if (!enableMemoryMonitoring) {
            // Not enabled per configuration
            return;
        }

        int periodMinutes = configService.getIntProperty("com.openexchange.monitoring.memory.periodMinutes", 5);
        double threshold = Double.parseDouble(configService.getProperty("com.openexchange.monitoring.memory.threshold", "10.0").trim());

        ServiceTracker<TimerService, TimerService> tracker = new ServiceTracker<TimerService, TimerService>(context, TimerService.class, new MemoryMonitoringInitializer(periodMinutes, threshold, context));
        this.timerServiceTracker = tracker;
        tracker.open();
    }

    private void dropMemoryMonitoring() {
        ServiceTracker<TimerService, TimerService> timerServiceTracker = this.timerServiceTracker;
        if (null != timerServiceTracker) {
            this.timerServiceTracker = null;
            timerServiceTracker.close();
        }
    }

    private void enableAndConfigureSocketMonitoring(ConfigurationService configService) throws Exception {
        dropSocketMonitoring();

        boolean enableSocketMonitoring = configService.getBoolProperty("com.openexchange.monitoring.sockets.enabled", false);
        if (!enableSocketMonitoring) {
            // Not enabled per configuration
            return;
        }

        SocketMonitoringSystem.initForDelegator();
        SocketMonitoringSystem socketMonitoringSystem = SocketMonitoringSystem.getInstance();

        // Configuration for socket tracing
        {
            TracingSocketMonitorConfig.Builder config = TracingSocketMonitorConfig.builder();

            // Having numberOfSamplesPerSocket less than/equal to 0 (zero) effectively disables sample recording
            int numberOfSamplesPerSocket = configService.getIntProperty("com.openexchange.monitoring.sockets.tracing.numberOfSamplesPerSocket", 1000);
            config.setNumberOfSamplesPerSocket(numberOfSamplesPerSocket);

            config.setThresholdMillis(configService.getIntProperty("com.openexchange.monitoring.sockets.tracing.thresholdMillis", 100));
            config.setWithRequestData(configService.getBoolProperty("com.openexchange.monitoring.sockets.tracing.withRequestData", false));
            {
                String filterExpression = configService.getProperty("com.openexchange.monitoring.sockets.tracing.filter.hostnames", "").trim();
                config.setFilter(Strings.isEmpty(filterExpression) ? null : HostList.valueOf(filterExpression));
            }
            {
                int[] filterPorts = null;
                {
                    String filterExpression = configService.getProperty("com.openexchange.monitoring.sockets.tracing.filter.ports", "").trim();
                    if (!Strings.isEmpty(filterExpression)) {
                        try {
                            String[] sPorts = Strings.splitByComma(filterExpression);
                            filterPorts = new int[sPorts.length];
                            for (int i = 0; i < sPorts.length; i++) {
                                filterPorts[i] = Integer.parseInt(sPorts[i]);
                            }
                            Arrays.sort(filterPorts);
                        } catch (NumberFormatException e) {
                            // Ignore invalid port filter
                            filterPorts = null;
                        }
                    }
                }
                config.setFilterPorts(filterPorts);
            }
            config.setKeepIdleThreshold(configService.getIntProperty("com.openexchange.monitoring.sockets.tracing.keepIdleThreshold", 300000));

            boolean enableDedicatedLogging = configService.getBoolProperty("com.openexchange.monitoring.sockets.tracing.logging.enabled", false);
            config.setEnableDedicatedLogging(enableDedicatedLogging);
            if (enableDedicatedLogging) {
                config.setLogLevel(configService.getProperty("com.openexchange.monitoring.sockets.tracing.logging.level", "error").trim());
                config.setLoggingFileLocation(configService.getProperty("com.openexchange.monitoring.sockets.tracing.logging.fileLocation", "").trim());
                config.setLoggingFileLimit(configService.getIntProperty("com.openexchange.monitoring.sockets.tracing.logging.fileLimit", 2097152));
                config.setLoggingFileCount(configService.getIntProperty("com.openexchange.monitoring.sockets.tracing.logging.fileCount", 99));
                config.setLoggingFileLayoutPattern(configService.getProperty("com.openexchange.monitoring.sockets.tracing.logging.fileLayoutPattern", "%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"} %-5level [%thread]%n%sanitisedMessage%n%lmdc%exception{full}").trim());
            }

            TracingSocketMonitor tracingSocketMonitor = new TracingSocketMonitor(config.build(), getService(TimerService.class));
            this.tracingSocketMonitor = tracingSocketMonitor;
            socketMonitoringSystem.add(tracingSocketMonitor);

            // Only makes sense if samples are collected
            if (numberOfSamplesPerSocket > 0) {
                ServiceTracker<ManagementService, ManagementService> tracker = new ServiceTracker<ManagementService, ManagementService>(context, ManagementService.class, new ManagementServiceTracker(tracingSocketMonitor, context));
                this.managementServiceTracker = tracker;
                tracker.open();
            }
        }

        socketMonitoringRegistration = context.registerService(SocketMonitoringService.class, socketMonitoringSystem, null);
    }

    private void dropSocketMonitoring() {
        SocketMonitoringSystem.shutDown();

        ServiceRegistration<SocketMonitoringService> socketMonitoringRegistration = this.socketMonitoringRegistration;
        if (null != socketMonitoringRegistration) {
            this.socketMonitoringRegistration = null;
            socketMonitoringRegistration.unregister();
        }

        TracingSocketMonitor tracingSocketMonitor = this.tracingSocketMonitor;
        if (null != tracingSocketMonitor) {
            this.tracingSocketMonitor = null;
            SocketMonitoringSystem.getInstance().remove(tracingSocketMonitor);
            tracingSocketMonitor.stop();
        }

        ServiceTracker<ManagementService, ManagementService> managementServiceTracker = this.managementServiceTracker;
        if (null != managementServiceTracker) {
            this.managementServiceTracker = null;
            managementServiceTracker.close();
        }
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        dropSocketMonitoring();

        dropMemoryMonitoring();

        MonitoringInit init = this.init;
        if (null != init) {
            this.init = null;
            init.stop();
        }

        super.stopBundle();
    }

    // ---------------------------------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MonitoringActivator.class);

        try {
            enableAndConfigureMemoryMonitoring(configService);
        } catch (Exception e) {
            logger.error("Failed to re-initialize memory monitoring", e);
        }

        try {
            enableAndConfigureSocketMonitoring(configService);
        } catch (Exception e) {
            logger.error("Failed to re-initialize socket monitoring", e);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.monitoring.sockets.*", "com.openexchange.monitoring.memory.*");
    }

}
