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

package com.openexchange.drive.events.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.internal.DriveEventServiceImpl;
import com.openexchange.drive.events.internal.DriveEventServiceLookup;
import com.openexchange.drive.events.ms.PortableDriveEventFactory;
import com.openexchange.drive.events.ms.PortableFolderContentChangeFactory;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.ms.PortableMsService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link DriveEventsActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventsActivator extends HousekeepingActivator {

    /** The logger constant */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveEventsActivator.class);

    /**
     * Initializes a new {@link DriveEventsActivator}.
     */
    public DriveEventsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, IDBasedFolderAccessFactory.class, TimerService.class,
            ConfigurationService.class, ThreadPoolService.class, DriveService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.drive.events\"");
        DriveEventServiceLookup.set(this);
        final DriveEventServiceImpl service = new DriveEventServiceImpl();
        registerService(DriveEventService.class, service);
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] {
            FileStorageEventConstants.CREATE_TOPIC,
            FileStorageEventConstants.UPDATE_TOPIC,
            FileStorageEventConstants.DELETE_TOPIC,
            FileStorageEventConstants.CREATE_FOLDER_TOPIC,
            FileStorageEventConstants.UPDATE_FOLDER_TOPIC,
            FileStorageEventConstants.DELETE_FOLDER_TOPIC
        });
        registerService(EventHandler.class, service, serviceProperties);
        registerService(CustomPortableFactory.class, new PortableDriveEventFactory());
        registerService(CustomPortableFactory.class, new PortableFolderContentChangeFactory());
        track(PortableMsService.class, new PortableMsTracker(context, service));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive.events\"");
        DriveEventServiceLookup.set(null);
        super.stopBundle();
    }

}
