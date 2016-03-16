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

package com.openexchange.drive.events.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.internal.DriveEventServiceImpl;
import com.openexchange.drive.events.internal.DriveEventServiceLookup;
import com.openexchange.drive.events.ms.MsDriveEventHandler;
import com.openexchange.drive.events.ms.PortableDriveEventFactory;
import com.openexchange.exception.OXException;
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
        track(PortableMsService.class, new ServiceTrackerCustomizer<PortableMsService, PortableMsService>() {

            private volatile MsDriveEventHandler eventHandler;

            @Override
            public PortableMsService addingService(ServiceReference<PortableMsService> reference) {
                PortableMsService messagingService = context.getService(reference);
                MsDriveEventHandler.setMsService(messagingService);
                LOG.debug("Initializing messaging service drive event handler");
                try {
                    this.eventHandler = new MsDriveEventHandler(service);
                } catch (OXException e) {
                    throw new IllegalStateException(
                        e.getMessage(), new BundleException(e.getMessage(), BundleException.ACTIVATOR_ERROR, e));
                }
                return messagingService;
            }

            @Override
            public void modifiedService(ServiceReference<PortableMsService> reference, PortableMsService service) {
                // Ignored
            }

            @Override
            public void removedService(ServiceReference<PortableMsService> reference, PortableMsService service) {
                LOG.debug("Stopping messaging service cache event handler");
                MsDriveEventHandler eventHandler = this.eventHandler;
                if (null != eventHandler) {
                    eventHandler.stop();
                    this.eventHandler = null;
                }
                MsDriveEventHandler.setMsService(null);
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive.events\"");
        DriveEventServiceLookup.set(null);
        super.stopBundle();
    }

}
