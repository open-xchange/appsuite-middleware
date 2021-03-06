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

package com.openexchange.gdpr.dataexport.impl.osgi;

import static com.openexchange.gdpr.dataexport.impl.storage.AbstractDataExportSql.isUseGlobalDb;
import static com.openexchange.java.Autoboxing.I;
import java.rmi.Remote;
import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccessProvider;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportConstants;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportProviderRegistry;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStatusChecker;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.impl.DataExportDatabaseAccessProvider;
import com.openexchange.gdpr.dataexport.impl.DataExportProviderRegistryImpl;
import com.openexchange.gdpr.dataexport.impl.DataExportServiceImpl;
import com.openexchange.gdpr.dataexport.impl.cleanup.DataExportCleanUpTask;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportAddFailCountTask;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportAddNotificationSentColumnTask;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportAddReportTable;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportCreateTableService;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportCreateTableTask;
import com.openexchange.gdpr.dataexport.impl.groupware.DataExportDeleteListener;
import com.openexchange.gdpr.dataexport.impl.rmi.DataExportRMIServiceImpl;
import com.openexchange.gdpr.dataexport.impl.storage.DataExportStorageServiceImpl;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.reseller.ResellerService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link DataExportActivator} - The activator for GDPR data export.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportActivator extends HousekeepingActivator {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportActivator.class);

    private DataExportServiceImpl service;

    private CleanUpInfo jobInfo;

    /**
     * Initializes a new {@link DataExportActivator}.
     */
    public DataExportActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ThreadPoolService.class, TimerService.class, FileStorageService.class,
            ConfigurationService.class, ContextService.class, ConfigViewFactory.class, UserService.class, TranslatorFactory.class,
            CapabilityService.class, ServerConfigService.class, NotificationMailFactory.class, DatabaseCleanUpService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        Services.setServiceLookup(this);

        ConfigurationService configService = getService(ConfigurationService.class);

        // Configuration for data export
        DataExportConfig config;
        {
            DataExportConfig.Builder configBuilder = DataExportConfig.builder();

            boolean active = configService.getBoolProperty("com.openexchange.gdpr.dataexport.active", true);
            configBuilder.withActive(active);

            String schedule = configService.getProperty("com.openexchange.gdpr.dataexport.schedule");
            if (Strings.isEmpty(schedule)) {
                if (active) {
                    throw DataExportExceptionCode.NO_SCHEDULE_SPECIFIED.create();
                }
            } else {
                configBuilder.parse(schedule);
            }

            int numberOfConcurrentTasks = configService.getIntProperty("com.openexchange.gdpr.dataexport.numberOfConcurrentTasks", DataExportConstants.DEFAULT_NUMBER_OF_CONCURRENT_TASKS);
            configBuilder.withNumberOfConcurrentTasks(numberOfConcurrentTasks);

            {
                String sCheckForTasksFrequency = configService.getProperty("com.openexchange.gdpr.dataexport.checkForTasksFrequency", "5m").trim();
                long checkForTasksFrequencyMillis = ConfigTools.parseTimespan(sCheckForTasksFrequency);
                if (checkForTasksFrequencyMillis < 0) {
                    checkForTasksFrequencyMillis = DataExportConstants.DEFAULT_CHECK_FOR_TASKS_FREQUENCY;
                }
                configBuilder.withCheckForTasksFrequency(checkForTasksFrequencyMillis);
            }

            {
                String sCheckForAbortedTasksFrequency = configService.getProperty("com.openexchange.gdpr.dataexport.checkForAbortedTasksFrequency", "2m").trim();
                long checkForAbortedTasksFrequency = ConfigTools.parseTimespan(sCheckForAbortedTasksFrequency);
                if (checkForAbortedTasksFrequency < 0) {
                    checkForAbortedTasksFrequency = DataExportConstants.DEFAULT_CHECK_FOR_ABORTED_TASKS_FREQUENCY;
                }
                configBuilder.withCheckForAbortedTasksFrequency(checkForAbortedTasksFrequency);
            }

            long maxProcessingTimeMillis = configService.getIntProperty("com.openexchange.gdpr.dataexport.maxProcessingTimeMillis", -1);
            configBuilder.withMaxProcessingTimeMillis(maxProcessingTimeMillis);

            {
                String sMaxTimeToLive = configService.getProperty("com.openexchange.gdpr.dataexport.maxTimeToLive", "2W").trim();
                long maxTimeToLiveMillis = ConfigTools.parseTimespan(sMaxTimeToLive);
                if (maxTimeToLiveMillis <= 0) {
                    maxTimeToLiveMillis = DataExportConstants.DEFAULT_MAX_TIME_TO_LIVE; // Two weeks in milliseconds
                }
                configBuilder.withMaxTimeToLiveMillis(maxTimeToLiveMillis);
            }

            {
                String sExpirationTime = configService.getProperty("com.openexchange.gdpr.dataexport.expirationTime", "10m").trim();
                long expirationTimeMillis = ConfigTools.parseTimespan(sExpirationTime);
                if (expirationTimeMillis < 0) {
                    expirationTimeMillis = DataExportConstants.DEFAULT_EXPIRATION_TIME;
                }
                configBuilder.withExpirationTimeMillis(expirationTimeMillis);
            }

            {
                String sDefaultMaxFileSize = configService.getProperty("com.openexchange.gdpr.dataexport.defaultMaxFileSize", Long.toString(DataExportConstants.DFAULT_MAX_FILE_SIZE)).trim();
                long defaultMaxFileSize;
                try {
                    defaultMaxFileSize = Long.parseLong(sDefaultMaxFileSize);
                    if (defaultMaxFileSize < DataExportConstants.MINIMUM_FILE_SIZE) {
                        defaultMaxFileSize = DataExportConstants.MINIMUM_FILE_SIZE;
                    }
                } catch (NumberFormatException e) {
                    defaultMaxFileSize = 1073741824L;
                }
                configBuilder.withDefaultMaxFileSize(defaultMaxFileSize);
            }

            int maxFailCountForWorkItem = configService.getIntProperty("com.openexchange.gdpr.dataexport.maxFailCountForWorkItem", DataExportConstants.DEFAULT_MAX_FAIL_COUNT_FOR_WORK_ITEM);
            configBuilder.withMaxFailCountForWorkItem(maxFailCountForWorkItem);

            boolean replaceUnicodeWithAscii = configService.getBoolProperty("com.openexchange.gdpr.dataexport.replaceUnicodeWithAscii", false);
            configBuilder.withReplaceUnicodeWithAscii(replaceUnicodeWithAscii);

            config = configBuilder.build();
        }

        boolean useGlobalDb = isUseGlobalDb();

        // Track providers
        DataExportProviderRegistryImpl providerRegistry = new DataExportProviderRegistryImpl(context);
        rememberTracker(providerRegistry);

        DataExportStorageService storageService = new DataExportStorageServiceImpl(useGlobalDb, config, this);

        DataExportServiceImpl service = new DataExportServiceImpl(config, storageService, providerRegistry, this);
        this.service = service;

        trackService(HostnameService.class);
        trackService(ResellerService.class);

        openTrackers();

        if (false == useGlobalDb) {
            registerService(CreateTableService.class, new DataExportCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DataExportCreateTableTask(), new DataExportAddNotificationSentColumnTask(), new DataExportAddReportTable(), new DataExportAddFailCountTask()));
        }
        registerService(DataExportStorageService.class, storageService);
        registerService(DataExportStatusChecker.class, storageService);
        registerService(DataExportService.class, service);
        registerService(DataExportProviderRegistry.class, providerRegistry);
        registerService(Remote.class, new DataExportRMIServiceImpl(service));
        registerService(DatabaseAccessProvider.class, new DataExportDatabaseAccessProvider(this));
        addService(DataExportStorageService.class, storageService);

        // Signal start-up
        try {
            service.onStartUp();
        } catch (Exception e) {
            LOG.error("Failed to schedule data export tasks", e);
        }

        // Announce GDPR data export available
        {
            final ServiceLookup services = this;
            final String sCapability = "dataexport";
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {

                @Override
                public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session session) {
                    if (sCapability.equals(capability)) {
                        if (session == null) {
                            return FailureAwareCapabilityChecker.Result.DISABLED;
                        }

                        try {
                            ConfigViewFactory viewFactory = getService(ConfigViewFactory.class);
                            ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
                            boolean enabled = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.gdpr.dataexport.enabled", true, view);
                            if (!enabled) {
                                return FailureAwareCapabilityChecker.Result.DISABLED;
                            }

                            User user = getUserFor(session, services);
                            if (user.isAnonymousGuest() || user.isGuest()) {
                                return FailureAwareCapabilityChecker.Result.DISABLED;
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to check if GDPR data export is enabled for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
                            return FailureAwareCapabilityChecker.Result.FAILURE;
                        }
                    }

                    return FailureAwareCapabilityChecker.Result.ENABLED;
                }

                private User getUserFor(Session session, ServiceLookup services) throws OXException {
                    return session instanceof ServerSession ? ((ServerSession) session).getUser() : services.getServiceSafe(UserService.class).getUser(session.getUserId(), session.getContextId());
                }

            }, properties);
            getService(CapabilityService.class).declareCapability(sCapability);
        }

        registerService(DeleteListener.class, new DataExportDeleteListener());

        jobInfo = getServiceSafe(DatabaseCleanUpService.class).scheduleCleanUpJob(DefaultCleanUpJob.builder() //@formatter:off
            .withId(DataExportCleanUpTask.class)
            .withDelay(Duration.ofHours(6))
            .withInitialDelay(Duration.ofMinutes(5))
            .withRunsExclusive(true)
            .withExecution(new DataExportCleanUpTask(service, storageService, this))
            .build()); //@formatter:on
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            DataExportServiceImpl service = this.service;
            if (service != null) {
                this.service = null;
                service.onStopped();
            }
            removeService(DataExportStorageService.class);
            CleanUpInfo jobInfo = this.jobInfo;
            if (null != jobInfo) {
                this.jobInfo = null;
                jobInfo.cancel(true);
            }
        } finally {
            super.stopBundle();
            Services.setServiceLookup(null);
            LOG.info("Stopped bundle {}", context.getBundle().getSymbolicName());
        }
    }

}
