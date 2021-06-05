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

package com.openexchange.oauth.yahoo.osgi;

import java.util.Arrays;
import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.oauth.yahoo.internal.groupware.OAuthYahooDropTokensTask;

/**
 * {@link DatabaseUpdateTaskServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DatabaseUpdateTaskServiceTracker implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final BundleContext bundleContext;
    private ServiceRegistration<UpdateTaskProviderService> registration;

    /**
     * Initialises a new {@link DatabaseUpdateTaskServiceTracker}.
     */
    public DatabaseUpdateTaskServiceTracker(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    @Override
    public DatabaseService addingService(ServiceReference<DatabaseService> reference) {
        DatabaseService databaseService = bundleContext.getService(reference);
        registration = bundleContext.registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(new OAuthYahooDropTokensTask());
            }

        }, null);
        return databaseService;
    }

    @Override
    public void modifiedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        // Nothing to do

    }

    @Override
    public void removedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        registration.unregister();
        bundleContext.ungetService(reference);
    }
}
