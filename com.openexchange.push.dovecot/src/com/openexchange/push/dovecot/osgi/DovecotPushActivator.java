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

package com.openexchange.push.dovecot.osgi;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.mail.service.MailService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.DovecotPushDeleteListener;
import com.openexchange.push.dovecot.DovecotPushManagerService;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.HzDovecotPushClusterLock;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link DovecotPushActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public class DovecotPushActivator extends HousekeepingActivator {

    private static final class HzConfigTracker implements ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> {

        final BundleContext context;
        final DovecotPushActivator activator;
        final DovecotPushConfiguration configuration;
        private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker;

        HzConfigTracker(BundleContext context, DovecotPushConfiguration configuration, DovecotPushActivator activator) {
            super();
            this.context = context;
            this.activator = activator;
            this.configuration = configuration;
        }

        @Override
        public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
            HazelcastConfigurationService hzConfigService = context.getService(reference);

            final Logger logger = org.slf4j.LoggerFactory.getLogger(DovecotPushActivator.class);
            try {
                boolean hzEnabled = hzConfigService.isEnabled();
                if (false == hzEnabled) {
                    String msg = "IMAP-IDLE is configured to use Hazelcast-based locking, but Hazelcast is disabled as per configuration! Start of IMAP-IDLE aborted!";
                    logger.error(msg, new Exception(msg));
                    context.ungetService(reference);
                    return null;
                }

                final BundleContext context = this.context;
                ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> stc = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    private volatile ServiceRegistration<PushManagerService> reg;

                    @Override
                    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                        HazelcastInstance hzInstance = context.getService(reference);
                        try {
                            String mapName = discoverMapName(hzInstance.getConfig(), logger);
                            ((HzDovecotPushClusterLock) configuration.getClusterLock()).setMapName(mapName);
                            activator.addService(HazelcastInstance.class, hzInstance);

                            reg = context.registerService(PushManagerService.class, DovecotPushManagerService.newInstance(configuration.getEndPoint(), configuration.getClusterLock(), activator), null);

                            return hzInstance;
                        } catch (Exception e) {
                            logger.warn("Failed start-up for {}", context.getBundle().getSymbolicName(), e);
                        }
                        context.ungetService(reference);
                        return null;
                    }

                    @Override
                    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        // Nothing
                    }

                    @Override
                    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        ServiceRegistration<PushManagerService> reg = this.reg;
                        if (null != reg) {
                            reg.unregister();
                            // stopPushManagerSafe();
                            this.reg = null;
                        }

                        activator.removeService(HazelcastInstance.class);
                        context.ungetService(reference);
                    }
                };
                ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, stc);
                this.hzInstanceTracker = hzInstanceTracker;
                hzInstanceTracker.open();

                return hzConfigService;
            } catch (Exception e) {
                logger.warn("Failed to start IMAP-IDLE!", e);
            }

            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            // Ignore
        }

        @Override
        public void removedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = this.hzInstanceTracker;
            if (null != hzInstanceTracker) {
                hzInstanceTracker.close();
                this.hzInstanceTracker = null;
            }

            context.ungetService(reference);
        }

        /**
         * Discovers the map name from the supplied Hazelcast configuration.
         *
         * @param config The config object
         * @return The sessions map name
         * @throws IllegalStateException
         */
        String discoverMapName(Config config, Logger logger) throws IllegalStateException {
            Map<String, MapConfig> mapConfigs = config.getMapConfigs();
            if (null != mapConfigs && 0 < mapConfigs.size()) {
                for (String mapName : mapConfigs.keySet()) {
                    if (mapName.startsWith("dovecotnotify-")) {
                        logger.info("Using distributed Dovecot Push map '{}'.", mapName);
                        return mapName;
                    }
                }
            }
            String msg = "No distributed Dovecot Push map found in Hazelcast configuration";
            throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DovecotPushActivator}.
     */
    public DovecotPushActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, TimerService.class, MailService.class, ConfigurationService.class, SessiondService.class,
            ThreadPoolService.class, ContextService.class, UserService.class, PushListenerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        DovecotPushConfiguration configuration = new DovecotPushConfiguration();
        configuration.init(this);

        // Check Hazelcast-based locking is enabled
        if (DovecotPushClusterLock.Type.HAZELCAST.equals(configuration.getClusterLock().getType())) {
            // Start tracking for Hazelcast
            track(HazelcastConfigurationService.class, new HzConfigTracker(context, configuration, this));
        } else {
            // Register PushManagerService instance
            registerService(PushManagerService.class, DovecotPushManagerService.newInstance(configuration.getEndPoint(), configuration.getClusterLock(), this));
        }
        openTrackers();

        registerService(DeleteListener.class, new DovecotPushDeleteListener());
    }

    @Override
    protected void stopBundle() throws Exception {
        DovecotPushManagerService.dropInstance();
        super.stopBundle();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
