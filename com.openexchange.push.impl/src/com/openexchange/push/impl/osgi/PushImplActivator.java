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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.impl.PushEventHandler;
import com.openexchange.push.impl.PushManagerRegistry;
import com.openexchange.push.impl.credstorage.inmemory.HazelcastCredentialStorage;
import com.openexchange.push.impl.credstorage.inmemory.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.push.impl.groupware.CreatePushTable;
import com.openexchange.push.impl.groupware.PushCreateTableTask;
import com.openexchange.push.impl.groupware.PushDeleteListener;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link PushImplActivator} - The activator for push implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushImplActivator extends HousekeepingActivator implements HazelcastInstanceNotActiveExceptionHandler {

    /**
     * Initializes a new {@link PushImplActivator}.
     */
    public PushImplActivator() {
        super();
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class };
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
            track(ConfigurationService.class, new WhitelistServiceTracker(context));

            // Thread pool service tracker
            trackService(ConfigurationService.class);
            trackService(EventFactoryService.class);
            trackService(ThreadPoolService.class);
            trackService(EventAdmin.class);
            trackService(DatabaseService.class);

            final HazelcastCredentialStorage hzCredStorage = new HazelcastCredentialStorage(this, this);
            // Check Hazelcast stuff
            {
                final HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);
                if (hazelcastConfig.isEnabled()) {
                    // Track HazelcastInstance service
                    ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> customizer = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                        @Override
                        public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                            HazelcastInstance hazelcastInstance = context.getService(reference);
                            try {
                                String mapName = hazelcastConfig.discoverMapName("credentials");
                                if (null == mapName) {
                                    context.ungetService(reference);
                                    return null;
                                }
                                addService(HazelcastInstance.class, hazelcastInstance);
                                hzCredStorage.setHzMapName(mapName);
                                hzCredStorage.changeBackingMapToHz();
                                return hazelcastInstance;
                            } catch (OXException e) {
                                log.warn("Couldn't initialize remote binary sources map.", e);
                            } catch (RuntimeException e) {
                                log.warn("Couldn't initialize remote binary sources map.", e);
                            }
                            context.ungetService(reference);
                            return null;
                        }

                        @Override
                        public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                            // Ignore
                        }

                        @Override
                        public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                            removeService(HazelcastInstance.class);
                            hzCredStorage.changeBackingMapToLocalMap();
                            context.ungetService(reference);
                        }
                    };
                    track(HazelcastInstance.class, customizer);
                }
            }

            openTrackers();

            registerService(CreateTableService.class, new CreatePushTable(), null);
            registerService(DeleteListener.class, new PushDeleteListener(), null);
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PushCreateTableTask()));

            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            registerService(CredentialStorage.class, hzCredStorage);
            registerService(PushListenerService.class, PushManagerRegistry.getInstance());

            // Register event handler to detect removed sessions
            serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new PushEventHandler(), serviceProperties);
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.push.impl", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushImplActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.push.impl");
            Services.setServiceLookup(null);
            super.stopBundle();
            PushManagerRegistry.shutdown();
        } catch (Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.push.impl", e);
            throw e;
        }
    }

}
