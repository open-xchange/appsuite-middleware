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
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.impl.SignalStartedServiceImpl;

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

    /**
     * Initializes a new {@link DBMigrationMonitorTracker}.
     * @param signalStartedRegistrationRef
     *
     * @param context The bundle context
     */
    public DBMigrationMonitorTracker(AtomicReference<ServiceRegistration<SignalStartedService>> signalStartedRegistrationRef, BundleContext context) {
        super();
        this.context = context;
        this.signalStartedRegistrationRef = signalStartedRegistrationRef;
    }

    @Override
    public DBMigrationMonitorService addingService(final ServiceReference<DBMigrationMonitorService> reference) {
        final DBMigrationMonitorService migrationMonitor = context.getService(reference);

        if (migrationMonitor != null) {
            final BundleContext context = this.context;
            final AtomicReference<ServiceRegistration<SignalStartedService>> serviceRegistrationRef = this.signalStartedRegistrationRef;
            Executors.newSingleThreadExecutor().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        boolean dbUpdateInProgress = !migrationMonitor.getScheduledFiles().isEmpty();;
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
                    serviceRegistrationRef.set(context.registerService(SignalStartedService.class, new SignalStartedServiceImpl(), null));
                    LOG.info("Open-Xchange Server initialized. The server should be up and running...");
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
