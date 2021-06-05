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

package com.openexchange.sessionstorage.hazelcast.osgi;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageService;
import com.openexchange.sessionstorage.hazelcast.Unregisterer;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link HazelcastSessionStorageActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageActivator extends HousekeepingActivator implements Unregisterer {

    /** The logger */
    static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastSessionStorageActivator.class);

    private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzSessionStorageRegistrationTracker;

    /**
     * Initializes a new {@link HazelcastSessionStorageActivator}.
     */
    public HazelcastSessionStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, TimerService.class, HazelcastConfigurationService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.sessionstorage.hazelcast");
        Services.setServiceLookup(this);
        final HazelcastConfigurationService hzConfigService = getService(HazelcastConfigurationService.class);
        final boolean enabled = hzConfigService.isEnabled();
        if (false == enabled) {
            LOG.warn("com.openexchange.sessionstorage.hazelcast will be disabled due to disabled Hazelcast services");
        } else {
            /*
             * start session storage life-cycle to Hazelcast instance, in case com.openexchange.sessionstorage.hazelcast is enabled
             */
            final boolean storageEnabled;
            {
                final ConfigurationService configService = getService(ConfigurationService.class);
                final boolean defaultValue = true;
                storageEnabled = null == configService ? defaultValue : configService.getBoolProperty("com.openexchange.sessionstorage.hazelcast.enabled", defaultValue);
            }
            if (storageEnabled) {
                final BundleContext context = this.context;
                final Unregisterer unregisterer = this;
                trackService(SessiondService.class);
                ServiceTracker<HazelcastInstance, HazelcastInstance> hzSessionStorageRegistrationTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    private ServiceRegistration<SessionStorageService> sessionStorageRegistration;
                    private ScheduledTimerTask sessionToucherTask;

                    @Override
                    public synchronized HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                        final HazelcastInstance hazelcastInstance = context.getService(reference);
                        HazelcastSessionStorageService.setHazelcastInstance(hazelcastInstance);
                        /*
                         * create & register session storage service
                         */
                        String sessionsMapName = discoverSessionsMapName(hazelcastInstance.getConfig());
                        final HazelcastSessionStorageService sessionStorage = new HazelcastSessionStorageService(sessionsMapName, unregisterer);
                        sessionStorageRegistration = context.registerService(SessionStorageService.class, sessionStorage, null);
                        /*
                         * schedule timer task to touch active sessions regularly
                         */
                        long period = SessionToucher.getTouchPeriod(getService(ConfigurationService.class));
                        sessionToucherTask = getService(TimerService.class).scheduleAtFixedRate(new SessionToucher(sessionStorage), period, period);
                        return hazelcastInstance;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        // Ignore
                    }

                    @Override
                    public synchronized void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                        /*
                         * cancel session toucher timer task
                         */
                        ScheduledTimerTask sessionToucherTask = this.sessionToucherTask;
                        if (null != sessionToucherTask) {
                            sessionToucherTask.cancel();
                            this.sessionToucherTask = null;
                        }
                        /*
                         * remove session storage registration
                         */
                        ServiceRegistration<SessionStorageService> sessionStorageRegistration = this.sessionStorageRegistration;
                        if (null != sessionStorageRegistration) {
                            sessionStorageRegistration.unregister();
                            this.sessionStorageRegistration = null;
                        }
                        context.ungetService(reference);
                        HazelcastSessionStorageService.setHazelcastInstance(null);
                    }
                });
                // Open tracker and thus register service once HazelcastInstance is available
                hzSessionStorageRegistrationTracker.open();
                this.hzSessionStorageRegistrationTracker = hzSessionStorageRegistrationTracker;
                // Open others
                openTrackers();
            }
        }
    }

    @Override
    public void unregisterSessionStorage() {
        ServiceTracker<HazelcastInstance, HazelcastInstance> hzSessionStorageRegistrationTracker = this.hzSessionStorageRegistrationTracker;
        if (null != hzSessionStorageRegistrationTracker) {
            hzSessionStorageRegistrationTracker.close();
            this.hzSessionStorageRegistrationTracker = null;
        }
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        final BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

    @Override
    public <S> boolean removeService(final Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> boolean addService(final Class<S> clazz, final S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.sessionstorage.hazelcast");

        // Unregister service through closing service tracker
        unregisterSessionStorage();

        // Stop rest
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * Discovers the sessions map name from the supplied hazelcast configuration.
     *
     * @param config The config object
     * @return The sessions map name
     * @throws IllegalStateException
     */
    static String discoverSessionsMapName(Config config) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("sessions-")) {
                    LOG.info("Using distributed map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        String msg = "No distributed sessions map found in hazelcast configuration";
        throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
    }

}
