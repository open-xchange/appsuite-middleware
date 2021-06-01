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

package com.openexchange.drive.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.BodyParser;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.drive.json.action.DriveActionFactory;
import com.openexchange.drive.json.internal.ListenerRegistrar;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.internal.UploadActionBodyParser;
import com.openexchange.drive.json.listener.BlockingListenerFactory;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService;

/**
 * {@link DriveJsonActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveJsonActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveJsonActivator.class);

    /**
     * Initializes a new {@link DriveJsonActivator}.
     */
    public DriveJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DriveService.class, DriveEventService.class, LeanConfigurationService.class,
            ConfigurationService.class, DriveSubscriptionStore.class, CapabilityService.class, ModuleSupport.class,
            ShareNotificationService.class, DatabaseService.class, ShareService.class, ServerConfigService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.drive.json\"");
        Services.set(this);
        BundleContext context = this.context;
        registerModule(new DriveActionFactory(), "drive");
        ListenerRegistrar listenerRegistrar = ListenerRegistrar.getInstance();
        registerService(EventHandler.class, listenerRegistrar, singletonDictionary(EventConstants.EVENT_TOPIC,
            new String[] { SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_CONTAINER }));
        getService(DriveEventService.class).registerPublisher(listenerRegistrar);
        track(LongPollingListenerFactory.class, new ServiceTrackerCustomizer<LongPollingListenerFactory, LongPollingListenerFactory>() {

            @Override
            public LongPollingListenerFactory addingService(ServiceReference<LongPollingListenerFactory> serviceReference) {
                LongPollingListenerFactory service = context.getService(serviceReference);
                if (listenerRegistrar.addFactory(service)) {
                    return service;
                } else {
                    // already known
                    context.ungetService(serviceReference);
                    return null;
                }
            }

            @Override
            public void modifiedService(ServiceReference<LongPollingListenerFactory> serviceReference, LongPollingListenerFactory service) {
                // nothing to do
            }

            @Override
            public void removedService(ServiceReference<LongPollingListenerFactory> serviceReference, LongPollingListenerFactory service) {
                try {
                    listenerRegistrar.removeFactory(service);
                } finally {
                    context.ungetService(serviceReference);
                }
            }
        });
        trackService(DispatcherPrefixService.class);
        trackService(HostnameService.class);
        openTrackers();
        /*
         * register blocking long polling listener factory
         *
         */
        LOG.info("Registering blocking long polling listener factory...");
        registerService(LongPollingListenerFactory.class, new BlockingListenerFactory(this));

        registerService(BodyParser.class, UploadActionBodyParser.getInstance());
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive.json\"");
        DriveEventService driveEventService = getService(DriveEventService.class);
        if (null != driveEventService) {
            driveEventService.unregisterPublisher(ListenerRegistrar.getInstance());
        }
        Services.set(null);
        super.stopBundle();
    }

}
