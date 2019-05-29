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

package com.openexchange.gdpr.dataexport.impl.osgi;

import static com.openexchange.gdpr.dataexport.impl.storage.AbstractDataExportSql.isUseGlobalDb;
import static com.openexchange.java.Autoboxing.I;
import java.rmi.Remote;
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
import com.openexchange.filestore.FileStorageService;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportConstants;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportProviderRegistry;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStatusChecker;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.impl.DataExportProviderRegistryImpl;
import com.openexchange.gdpr.dataexport.impl.DataExportServiceImpl;
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
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
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
            CapabilityService.class, ServerConfigService.class, NotificationMailFactory.class };
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

            int numberOfConcurrentTasks = configService.getIntProperty("com.openexchange.gdpr.dataexport.numberOfConcurrentTasks", 1);
            configBuilder.withNumberOfConcurrentTasks(numberOfConcurrentTasks);

            {
                String sCheckForTasksFrequency = configService.getProperty("com.openexchange.gdpr.dataexport.checkForTasksFrequency", "5m").trim();
                long checkForTasksFrequencyMillis = ConfigTools.parseTimespan(sCheckForTasksFrequency);
                configBuilder.withCheckForTasksFrequency(checkForTasksFrequencyMillis);
            }

            {
                String sCheckForAbortedTasksFrequency = configService.getProperty("com.openexchange.gdpr.dataexport.checkForAbortedTasksFrequency", "2m").trim();
                long checkForAbortedTasksFrequency = ConfigTools.parseTimespan(sCheckForAbortedTasksFrequency);
                configBuilder.withCheckForAbortedTasksFrequency(checkForAbortedTasksFrequency);
            }

            long maxProcessingTimeMillis = configService.getIntProperty("com.openexchange.gdpr.dataexport.maxProcessingTimeMillis", -1);
            configBuilder.withMaxProcessingTimeMillis(maxProcessingTimeMillis);

            {
                String sMaxTimeToLive = configService.getProperty("com.openexchange.gdpr.dataexport.maxTimeToLive", "2W").trim();
                long maxTimeToLiveMillis = ConfigTools.parseTimespan(sMaxTimeToLive);
                if (maxTimeToLiveMillis <= 0) {
                    maxTimeToLiveMillis = 1209600000L; // Two weeks in milliseconds
                }
                configBuilder.withMaxTimeToLiveMillis(maxTimeToLiveMillis);
            }

            {
                String sExpirationTime = configService.getProperty("com.openexchange.gdpr.dataexport.expirationTime", "10m").trim();
                long expirationTimeMillis = ConfigTools.parseTimespan(sExpirationTime);
                configBuilder.withExpirationTimeMillis(expirationTimeMillis);
            }

            {
                String sDefaultMaxFileSize = configService.getProperty("com.openexchange.gdpr.dataexport.defaultMaxFileSize", "1073741824").trim();
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

            int maxFailCountForWorkItem = configService.getIntProperty("com.openexchange.gdpr.dataexport.maxFailCountForWorkItem", 4);
            configBuilder.withMaxFailCountForWorkItem(maxFailCountForWorkItem);

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
        addService(DataExportStorageService.class, storageService);

        // Signal start-up
        try {
            service.onStartUp();
        } catch (Exception e) {
            LOG.error("Failed to schedule data export tasks", e);
        }

        // Announce GDPR data export available
        {
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
                        } catch (Exception e) {
                            LOG.warn("Failed to check if GDPR data export is enabled for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
                            return FailureAwareCapabilityChecker.Result.FAILURE;
                        }
                    }

                    return FailureAwareCapabilityChecker.Result.ENABLED;
                }
            }, properties);
            getService(CapabilityService.class).declareCapability(sCapability);
        }

        registerService(DeleteListener.class, new DataExportDeleteListener());
    }

    @Override
    protected void stopBundle() throws Exception {
        DataExportServiceImpl service = this.service;
        if (service != null) {
            this.service = null;
            service.onStopped();
        }

        removeService(DataExportStorageService.class);
        super.stopBundle();
        Services.setServiceLookup(null);
        LOG.info("Stopped bundle {}", context.getBundle().getSymbolicName());
    }

}
