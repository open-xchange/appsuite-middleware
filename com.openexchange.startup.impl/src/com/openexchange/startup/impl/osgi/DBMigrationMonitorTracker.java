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

package com.openexchange.startup.impl.osgi;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.migration.DBMigrationMonitorService;
import com.openexchange.java.Strings;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.StaticSignalStartedService;
import com.openexchange.version.VersionService;

/**
 *
 * Tracker for the {@link DBMigrationMonitorService}.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.0
 */
public class DBMigrationMonitorTracker implements ServiceTrackerCustomizer<DBMigrationMonitorService, DBMigrationMonitorService> {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("StartUp-Logger");

    private final BundleContext context;
    private final AtomicReference<ServiceRegistration<SignalStartedService>> signalStartedRegistrationRef;

    private final VersionService versionService;

    /**
     * Initializes a new {@link DBMigrationMonitorTracker}.
     * @param signalStartedRegistrationRef
     * @param context The bundle context
     * @param versionService The version service
     */
    public DBMigrationMonitorTracker(AtomicReference<ServiceRegistration<SignalStartedService>> signalStartedRegistrationRef, BundleContext context, VersionService versionService) {
        super();
        this.context = context;
        this.signalStartedRegistrationRef = signalStartedRegistrationRef;
        this.versionService = versionService;
    }

    @Override
    public DBMigrationMonitorService addingService(final ServiceReference<DBMigrationMonitorService> reference) {
        final DBMigrationMonitorService migrationMonitor = context.getService(reference);

        if (migrationMonitor != null) {
            final BundleContext context = this.context;
            final AtomicReference<ServiceRegistration<SignalStartedService>> serviceRegistrationRef = this.signalStartedRegistrationRef;
            final VersionService versionService = this.versionService;
            Executors.newSingleThreadExecutor().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        boolean dbUpdateInProgress = !migrationMonitor.getScheduledFiles().isEmpty();
                        if (dbUpdateInProgress) {
                            int countLoops = 0;
                            long waitNanos = TimeUnit.SECONDS.toNanos(1L); // 1 second
                            do {
                                LockSupport.parkNanos(waitNanos);
                                dbUpdateInProgress = !migrationMonitor.getScheduledFiles().isEmpty();
                                if (++countLoops % 10 == 0) {
                                    LOG.info("Still updating configdb/globaldb.");
                                }
                            } while (dbUpdateInProgress);
                            LOG.info("Finished configdb/globaldb update. Time elapsed: {}ms", Integer.valueOf(countLoops * 1000));
                        } else {
                            LOG.debug("No configdb/globaldb update in progress.");
                        }
                    } catch (Exception e) {
                        LOG.error("Error while waiting for configdb/globaldb changes.", e);
                    }

                    StaticSignalStartedService singleton = StaticSignalStartedService.getInstance();
                    serviceRegistrationRef.set(context.registerService(SignalStartedService.class, singleton, null));

                    String sep = Strings.getLineSeparator();
                    if (StaticSignalStartedService.State.OK == singleton.getState()) {
                        LOG.info("{}{}\tOpen-Xchange Server v{} initialized. The server should be up and running...{}", sep, sep, versionService.getVersionString(), sep);
                    } else {
                        String message = singleton.getStateInfo(StaticSignalStartedService.INFO_MESSAGE);
                        if (null == message) {
                            Throwable error = singleton.getStateInfo(StaticSignalStartedService.INFO_EXCEPTION);
                            message = null == error ? null : error.getMessage();
                        }

                        if (null == message) {
                            LOG.error("{}\tFailed to initialize Open-Xchange Server v{}!{}", sep, versionService.getVersionString(), sep);
                        } else {
                            LOG.error("{}\tFailed to initialize Open-Xchange Server v{}: '{}'{}", sep, versionService.getVersionString(), message, sep);
                        }
                    }

                }
            });

            return migrationMonitor;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<DBMigrationMonitorService> reference, final DBMigrationMonitorService service) {
        context.ungetService(reference);
    }

    @Override
    public void modifiedService(final ServiceReference<DBMigrationMonitorService> reference, final DBMigrationMonitorService service) {
        // Nope
    }

}
