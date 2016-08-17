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

package com.openexchange.push.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
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
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
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
import com.openexchange.push.impl.mbean.PushMBeanImpl;
import com.openexchange.push.mbean.PushMBean;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link PushImplActivator} - The activator for push implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushImplActivator extends HousekeepingActivator  {

    private volatile PermanentListenerRescheduler rescheduler;

    /**
     * Initializes a new {@link PushImplActivator}.
     */
    public PushImplActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, TimerService.class, SessiondService.class, DatabaseService.class };
    }

    @Override
    public void startBundle() throws Exception {
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
            trackService(EventFactoryService.class);
            trackService(ThreadPoolService.class);
            trackService(EventAdmin.class);
            trackService(CryptoService.class);
            trackService(HazelcastInstance.class);

            // Track management service & register MBean
            {
                ServiceTrackerCustomizer<ManagementService, ManagementService> customizer = new ServiceTrackerCustomizer<ManagementService, ManagementService>() {

                    @Override
                    public void removedService(ServiceReference<ManagementService> reference, ManagementService management) {
                        try {
                            management.unregisterMBean(Managements.getObjectName(PushMBean.class.getName(), PushMBean.DOMAIN));
                        } catch (Exception e) {
                            log.warn("Could not un-register MBean {}", PushMBean.class.getName());
                        }
                        context.ungetService(reference);
                    }

                    @Override
                    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService management) {
                        // Nothing
                    }

                    @Override
                    public ManagementService addingService(ServiceReference<ManagementService> reference) {
                        ManagementService management = context.getService(reference);
                        try {
                            ObjectName objectName = Managements.getObjectName(PushMBean.class.getName(), PushMBean.DOMAIN);
                            management.registerMBean(objectName, new PushMBeanImpl());
                            return management;
                        } catch (Exception e) {
                            log.warn("Could not register MBean {}", PushMBean.class.getName());
                        }

                        context.ungetService(reference);
                        return null;
                    }
                };
                track(ManagementService.class, customizer);
            }

            // Get initialized registry instance
            PushManagerRegistry pushManagerRegistry = PushManagerRegistry.getInstance();

            // Register portables
            registerService(CustomPortableFactory.class, new PortableCheckForExtendedServiceCallableFactory());
            registerService(CustomPortableFactory.class, new PortableDropPermanentListenerCallableFactory());
            registerService(CustomPortableFactory.class, new PortablePlanRescheduleCallableFactory());
            registerService(CustomPortableFactory.class, new PortableOwnerFactory());
            registerService(CustomPortableFactory.class, new PortableDropAllPermanentListenerCallableFactory());
            registerService(CustomPortableFactory.class, new PortableStartPermanentListenerCallableFactory());

            if (pushManagerRegistry.isPermanentPushAllowed()) {

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
                    pushManagerRegistry.applyInitialListeners(pushManagerRegistry.getUsersWithPermanentListeners(), 0L);
                }
            }

            registerService(CreateTableService.class, new CreatePushTable(), null);
            registerService(DeleteListener.class, new PushDeleteListener(), null);
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PushCreateTableTask()));

            registerService(PushListenerService.class, pushManagerRegistry);

            // Register event handler to detect removed sessions
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new PushEventHandler(), serviceProperties);
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.push.impl", e);
            throw e;
        }
        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushImplActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.push.impl");

            PermanentListenerRescheduler rescheduler = this.rescheduler;
            if (null != rescheduler) {
                rescheduler.stop();
                this.rescheduler = null;
            }

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
