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

package com.openexchange.startup.impl.osgi;

import java.util.concurrent.Executors;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.migration.DBMigrationMonitorService;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.impl.Services;
import com.openexchange.startup.impl.SignalStartedServiceImpl;

/**
 *
 * Tracker for the {@link DBMigrationMonitorService}.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.0
 */
public class DBMigrationMonitorTracker implements ServiceTrackerCustomizer<DBMigrationMonitorService, DBMigrationMonitorService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationMonitorTracker.class);

    private final BundleContext context;

    public DBMigrationMonitorTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBMigrationMonitorService addingService(final ServiceReference<DBMigrationMonitorService> reference) {
        final DBMigrationMonitorService migrationMonitor = context.getService(reference);
        if (migrationMonitor == null) {
            return null;
        }

        final BundleContext lContext = this.context;
        Executors.newSingleThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                boolean dbUpdateInProgress = true;

                try {
                    int countLoops = 0;
                    while (dbUpdateInProgress) {
                        dbUpdateInProgress = !migrationMonitor.getScheduledFiles().isEmpty();

                        countLoops++;
                        Thread.sleep(1000);
                        if (countLoops % 10 == 0) {
                            LOG.info("Still updating configdb.");
                        }
                    }
                    LOG.info("Finished update. Time elapsed: {}ms", countLoops * 1000);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted while waiting for configdb changes.", e);
                }
                Services.addSignalStartedService(lContext.registerService(SignalStartedService.class, new SignalStartedServiceImpl(), null));
                LOG.info("Open-Xchange Server initialized. The server should be up and running...");
            }
        });

        return migrationMonitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(final ServiceReference<DBMigrationMonitorService> reference, final DBMigrationMonitorService service) {

        ServiceRegistration<SignalStartedService> removeSignalStartedService = com.openexchange.startup.impl.Services.removeSignalStartedService();
        if (removeSignalStartedService != null) {
            removeSignalStartedService.unregister();
        }
        context.ungetService(reference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(final ServiceReference<DBMigrationMonitorService> reference, final DBMigrationMonitorService service) {
        // Nope
    }

}
