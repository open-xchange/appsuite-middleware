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

package com.openexchange.folderstorage.calendar.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.calendar.CalendarAccountErrorField;
import com.openexchange.folderstorage.calendar.CalendarConfigField;
import com.openexchange.folderstorage.calendar.CalendarFolderStorage;
import com.openexchange.folderstorage.calendar.CalendarProviderField;
import com.openexchange.folderstorage.calendar.ExtendedPropertiesField;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link CalendarFolderStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public final class CalendarFolderStorageActivator extends HousekeepingActivator {

    private ServiceTracker<?, ?> dependentTracker;

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(CalendarFolderStorageActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * initialize folder storage
             */
            reinit();
            openTrackers();
        } catch (Exception e) {
            getLogger(CalendarFolderStorageActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    private synchronized void reinit() throws OXException {
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        /*
         * register calendar folder storage once IDBasedCalendarAccessFactory service becomes available
         */
        Dictionary<String, String> serviceProperties = new Hashtable<String, String>(1);
        serviceProperties.put("tree", FolderStorage.REAL_TREE_ID);
        DependentServiceRegisterer<FolderStorage> registerer = new DependentServiceRegisterer<FolderStorage>(context, FolderStorage.class, CalendarFolderStorage.class, serviceProperties, IDBasedCalendarAccessFactory.class);
        try {
            tracker = new ServiceTracker<>(context, registerer.getFilter(), registerer);
        } catch (InvalidSyntaxException e) {
            throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create(e);
        }
        this.dependentTracker = tracker;
        tracker.open();
        /*
         * register custom calendar folder fields
         */
        registerService(FolderField.class, ExtendedPropertiesField.getInstance());
        registerService(FolderField.class, CalendarProviderField.getInstance());
        registerService(FolderField.class, CalendarConfigField.getInstance());
        registerService(FolderField.class, CalendarAccountErrorField.getInstance());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        getLogger(CalendarFolderStorageActivator.class).info("stopping bundle {}", context.getBundle());
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        super.stopBundle();
    }

}
