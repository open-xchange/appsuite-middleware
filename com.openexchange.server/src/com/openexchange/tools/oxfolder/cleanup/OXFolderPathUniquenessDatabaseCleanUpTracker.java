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

package com.openexchange.tools.oxfolder.cleanup;

import java.time.Duration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
import com.openexchange.exception.OXException;
import com.openexchange.tools.oxfolder.OXFolderPathCleanUp;

/**
 *
 * {@link OXFolderPathUniquenessDatabaseCleanUpTracker}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class OXFolderPathUniquenessDatabaseCleanUpTracker implements ServiceTrackerCustomizer<DatabaseCleanUpService, DatabaseCleanUpService> {

    private final BundleContext context;
    private CleanUpInfo jobInfo;

    /**
     * Initializes a new {@link OXFolderPathUniquenessDatabaseCleanUpTracker}.
     *
     * @param context The {@link BundleContext}
     */
    public OXFolderPathUniquenessDatabaseCleanUpTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized DatabaseCleanUpService addingService(ServiceReference<DatabaseCleanUpService> reference) {
        DatabaseCleanUpService databaseCleanUpService = context.getService(reference);
        try {
            jobInfo = databaseCleanUpService.scheduleCleanUpJob(DefaultCleanUpJob.builder() //@formatter:off
                .withId(OXFolderPathCleanUp.class)
                .withDelay(Duration.ofMinutes(60))
                .withInitialDelay(Duration.ofMinutes(30))
                .withRunsExclusive(false)
                .withExecution(new OXFolderPathCleanUp())
                .build()); //@formatter:on
        } catch (OXException e) {
            LoggerFactory.getLogger(OXFolderPathUniquenessDatabaseCleanUpTracker.class).error("Unable to register clean up job for \"oxfolder_reservedpaths\" table.", e);
        }
        return databaseCleanUpService;
    }

    @Override
    public void modifiedService(ServiceReference<DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
        CleanUpInfo jobInfo = this.jobInfo;
        if (null != jobInfo) {
            this.jobInfo = null;
            try {
                jobInfo.cancel(true);
            } catch (Exception e) {
                LoggerFactory.getLogger(OXFolderPathUniquenessDatabaseCleanUpTracker.class).error("Unable to stop clean up job for \"oxfolder_reservedpaths\" table.", e);
            }
        }
        context.ungetService(reference);
    }

}
