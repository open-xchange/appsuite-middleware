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

package com.openexchange.filemanagement.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.internal.DistributedFileUtilsImpl;
import com.openexchange.filemanagement.internal.ManagedFileManagementImpl;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.timer.TimerService;

/**
 * {@link ManagedFileManagementActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ManagedFileManagementActivator implements BundleActivator {

    private List<ServiceTracker<?,?>> trackers = null;
    private ServiceRegistration<DistributedFileUtils> distributedFileUtilsRegistration;

    /**
     * Initializes a new {@link ManagedFileManagementActivator}.
     */
    public ManagedFileManagementActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        List<ServiceTracker<?,?>> trackers = new ArrayList<>(2);
        this.trackers = trackers;

        trackers.add(new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, new TmpFileCleaner()));

        DependentServiceRegisterer<ManagedFileManagement> registerer = new DependentServiceRegisterer<ManagedFileManagement>(context, ManagedFileManagement.class, ManagedFileManagementImpl.class, null, TimerService.class, DispatcherPrefixService.class);
        trackers.add(new ServiceTracker<Object, Object>(context, registerer.getFilter(), registerer));

        for (ServiceTracker<?,?> tracker : trackers) {
            tracker.open();
        }

        distributedFileUtilsRegistration = context.registerService(DistributedFileUtils.class, new DistributedFileUtilsImpl(), null);
    }

    @Override
    public synchronized void stop(BundleContext context) {
        ServiceRegistration<DistributedFileUtils> distributedFileUtilsRegistration = this.distributedFileUtilsRegistration;
        if (distributedFileUtilsRegistration != null) {
            this.distributedFileUtilsRegistration = null;
            distributedFileUtilsRegistration.unregister();
        }

        List<ServiceTracker<?,?>> trackers = this.trackers;
        if (trackers != null) {
            this.trackers = null;
            for (ServiceTracker<?,?> tracker : trackers) {
                tracker.close();
            }
        }
    }

}
