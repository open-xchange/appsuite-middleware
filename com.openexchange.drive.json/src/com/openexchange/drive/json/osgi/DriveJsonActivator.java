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

package com.openexchange.drive.json.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.drive.json.action.DriveActionFactory;
import com.openexchange.drive.json.internal.ListenerRegistrar;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.listener.BlockingListenerFactory;
import com.openexchange.groupware.notify.hostname.HostnameService;
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
        return new Class<?>[] { DriveService.class, DriveEventService.class,
            ConfigurationService.class, DriveSubscriptionStore.class, CapabilityService.class, ModuleSupport.class,
            ShareNotificationService.class, DatabaseService.class, ShareService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.drive.json\"");
        Services.set(this);
        registerModule(new DriveActionFactory(), "drive");
        getService(DriveEventService.class).registerPublisher(ListenerRegistrar.getInstance());
        track(LongPollingListenerFactory.class, new ServiceTrackerCustomizer<LongPollingListenerFactory, LongPollingListenerFactory>() {

            @Override
            public LongPollingListenerFactory addingService(ServiceReference<LongPollingListenerFactory> serviceReference) {
                LongPollingListenerFactory service = context.getService(serviceReference);
                if (ListenerRegistrar.getInstance().addFactory(service)) {
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
                    ListenerRegistrar.getInstance().removeFactory(service);
                } finally {
                    context.ungetService(serviceReference);
                }
            }
        });
        trackService(DispatcherPrefixService.class);
        trackService(HostnameService.class);
        openTrackers();
        /*
         * register blocking long polling listener factory if allowed by configuration
         *
         */
        if (getService(ConfigurationService.class).getBoolProperty("com.openexchange.drive.events.blockingLongPolling.enabled", false)) {
            LOG.info("Registering blocking long polling listener factory...");
            registerService(LongPollingListenerFactory.class, new BlockingListenerFactory());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive.json\"");
        getService(DriveEventService.class).unregisterPublisher(ListenerRegistrar.getInstance());
        Services.set(null);
        super.stopBundle();
    }

}
