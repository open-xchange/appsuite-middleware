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

package com.openexchange.passwordchange.database.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * {@link DatabasePasswordChangeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabasePasswordChangeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DatabasePasswordChangeActivator}
     */
    public DatabasePasswordChangeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        // Track BasicPasswordChangeService and re-distribute it as regular password change service
        final BundleContext context = this.context;
        track(BasicPasswordChangeService.class, new ServiceTrackerCustomizer<BasicPasswordChangeService, BasicPasswordChangeService>() {

            @Override
            public BasicPasswordChangeService addingService(ServiceReference<BasicPasswordChangeService> reference) {
                BasicPasswordChangeService basicService = context.getService(reference);

                // Re-Distribute as regular password change service
                registerService(PasswordChangeService.class, basicService);

                return basicService;
            }

            @Override
            public void modifiedService(ServiceReference<BasicPasswordChangeService> reference, BasicPasswordChangeService service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<BasicPasswordChangeService> reference, BasicPasswordChangeService service) {
                unregisterServices();
                context.ungetService(reference);
            }
        });
        openTrackers();
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

}
