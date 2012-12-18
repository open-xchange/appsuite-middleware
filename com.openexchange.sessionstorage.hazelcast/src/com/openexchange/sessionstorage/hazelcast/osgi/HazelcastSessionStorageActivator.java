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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage.hazelcast.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageConfiguration;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageService;
import com.openexchange.sessionstorage.hazelcast.Services;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link HazelcastSessionStorageActivator}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastSessionStorageActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(HazelcastSessionStorageActivator.class);
    
    /**
     * Name of the distributed sessions map. Should be changed when the serialized session object changes to avoid serialization issues.
     */
    private static final String MAP_NAME = "sessions-1";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.sessionstorage.hazelcast");
        Services.setServiceLookup(this);
        final ConfigurationService configService = getService(ConfigurationService.class);
        final boolean enabled = configService.getBoolProperty("com.openexchange.sessionstorage.hazelcast.enabled", false);
        if (enabled) {
            final MapConfig mapConfig = new MapConfig();
            {
                final int backupCount = configService.getIntProperty("com.openexchange.sessionstorage.hazelcast.map.backupcount", 1);
                final int asyncBackup = configService.getIntProperty("com.openexchange.sessionstorage.hazelcast.map.asyncbackup", 0);
                boolean readBackupData = configService.getBoolProperty("com.openexchange.sessionstorage.hazelcast.readbackupdata", true);
                mapConfig.setName(MAP_NAME);
                mapConfig.setBackupCount(backupCount);
                mapConfig.setAsyncBackupCount(asyncBackup);
                mapConfig.setTimeToLiveSeconds(0);
                mapConfig.setMaxIdleSeconds(0);
                mapConfig.setEvictionPolicy("NONE");
                mapConfig.setEvictionPercentage(25);
                mapConfig.setReadBackupData(readBackupData);
                final MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
                maxSizeConfig.setSize(0);
                mapConfig.setMaxSizeConfig(maxSizeConfig);
                mapConfig.setMergePolicy("hz.LATEST_UPDATE");
                // add indices for context- and user ID
                mapConfig.addMapIndexConfig(new MapIndexConfig("userId", false));
                mapConfig.addMapIndexConfig(new MapIndexConfig("contextId", false));
            }
            final HazelcastSessionStorageConfiguration config = new HazelcastSessionStorageConfiguration(mapConfig);
            // Track HazelcastInstance
            final BundleContext context = this.context;
            track(HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                private volatile ServiceRegistration<SessionStorageService> sessionStorageRegistration;

                @Override
                public HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                    final HazelcastInstance hazelcastInstance = context.getService(reference);
                    HazelcastSessionStorageService.setHazelcastInstance(hazelcastInstance);
                    sessionStorageRegistration = context.registerService(SessionStorageService.class, new HazelcastSessionStorageService(
                        config,
                        hazelcastInstance), null);
                    return hazelcastInstance;
                }

                @Override
                public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    // Ignore
                }

                @Override
                public void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance service) {
                    final ServiceRegistration<SessionStorageService> sessionStorageRegistration = this.sessionStorageRegistration;
                    if (null != sessionStorageRegistration) {
                        sessionStorageRegistration.unregister();
                        this.sessionStorageRegistration = null;
                    }
                    context.ungetService(reference);
                    HazelcastSessionStorageService.setHazelcastInstance(null);
                }
            });
            openTrackers();
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
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
