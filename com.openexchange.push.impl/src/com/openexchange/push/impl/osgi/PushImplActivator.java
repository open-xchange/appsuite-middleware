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

package com.openexchange.push.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdaterEventConstants;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.credstorage.CredentialStorageProvider;
import com.openexchange.push.impl.PushEventHandler;
import com.openexchange.push.impl.PushManagerRegistry;
import com.openexchange.push.impl.balancing.registrypolicy.portable.PortableOwnerFactory;
import com.openexchange.push.impl.balancing.reschedulerpolicy.PermanentListenerRescheduler;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableCheckForExtendedServiceCallableFactory;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropAllPermanentListenerCallableFactory;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropPermanentListenerCallableFactory;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortablePlanRescheduleCallableFactory;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableStartPermanentListenerCallableFactory;
import com.openexchange.push.impl.groupware.CreatePushTable;
import com.openexchange.push.impl.groupware.PushCreateTableTask;
import com.openexchange.push.impl.groupware.PushDeleteListener;
import com.openexchange.push.impl.jobqueue.PermanentListenerJobQueue;
import com.openexchange.push.impl.rmi.PushRMIServiceImpl;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link PushImplActivator} - The activator for push implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushImplActivator extends HousekeepingActivator {

    private PermanentListenerRescheduler rescheduler;

    /**
     * Initializes a new {@link PushImplActivator}.
     */
    public PushImplActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, TimerService.class, SessiondService.class, DatabaseService.class,
            ContextService.class, UserService.class };
    }

    @Override
    public synchronized void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushImplActivator.class);
        try {
            log.info("starting bundle: com.openexchange.push.impl");

            Services.setServiceLookup(this);
            final BundleContext context = this.context;

            // Initialize and open service tracker for push manager services
            PushManagerRegistry.init(this);
            track(PushManagerService.class, new PushManagerServiceTracker(context));
            track(ConfigurationService.class, new ConfigurationServiceTracker(context));

            // Thread pool service tracker
            trackService(ConfigurationService.class);
            trackService(ConfigViewFactory.class);
            trackService(EventFactoryService.class);
            trackService(ThreadPoolService.class);
            trackService(EventAdmin.class);
            trackService(CryptoService.class);
            trackService(HazelcastInstance.class);
            trackService(ObfuscatorService.class);

            // Get initialized registry instance
            PushManagerRegistry pushManagerRegistry = PushManagerRegistry.getInstance();

            // Register portables
            registerService(CustomPortableFactory.class, new PortableCheckForExtendedServiceCallableFactory());
            registerService(CustomPortableFactory.class, new PortableDropPermanentListenerCallableFactory());
            registerService(CustomPortableFactory.class, new PortablePlanRescheduleCallableFactory());
            registerService(CustomPortableFactory.class, new PortableOwnerFactory());
            registerService(CustomPortableFactory.class, new PortableDropAllPermanentListenerCallableFactory());
            registerService(CustomPortableFactory.class, new PortableStartPermanentListenerCallableFactory());

            if (pushManagerRegistry.isPermanentPushAllowedPerConfig()) {
                // Track HazelcastInstance
                HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
                if (hazelcastConfig.isEnabled()) {
                    // Track HazelcastInstance service
                    PermanentListenerRescheduler rescheduler = new PermanentListenerRescheduler(pushManagerRegistry, context);
                    this.rescheduler = rescheduler;
                    pushManagerRegistry.setRescheduler(rescheduler);
                    track(HazelcastInstance.class, rescheduler);

                    // Event handler registration
                    {
                        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                        serviceProperties.put(EventConstants.EVENT_TOPIC, UpdaterEventConstants.getTopics());
                        registerService(EventHandler.class, rescheduler, serviceProperties);
                    }
                } else {
                    pushManagerRegistry.applyInitialListeners(pushManagerRegistry.getUsersWithPermanentListeners(), true, 0L);
                }
            }

            registerService(CreateTableService.class, new CreatePushTable(), null);
            registerService(DeleteListener.class, new PushDeleteListener(), null);
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PushCreateTableTask()));

            registerService(PushListenerService.class, pushManagerRegistry);
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", PushRMIServiceImpl.RMI_NAME);
            registerService(Remote.class, new PushRMIServiceImpl(), serviceProperties);

            // Register event handler to detect removed sessions
            serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new PushEventHandler(), serviceProperties);
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.push.impl", e);
            throw e;
        }
        openTrackers();
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushImplActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.push.impl");

            PermanentListenerRescheduler rescheduler = this.rescheduler;
            if (null != rescheduler) {
                rescheduler.stop();
                this.rescheduler = null;
            }

            PermanentListenerJobQueue.getInstance().stop();

            Services.setServiceLookup(null);
            removeService(CredentialStorageProvider.class);
            super.stopBundle();
            PushManagerRegistry.shutdown();
        } catch (Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.push.impl", e);
            throw e;
        }
    }

}
