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

package com.openexchange.realtime.hazelcast.osgi;

import static com.openexchange.realtime.hazelcast.channel.HazelcastAccess.discoverMapName;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.cleanup.CleanupMemberShipListener;
import com.openexchange.realtime.hazelcast.cleanup.GlobalRealtimeCleanupImpl;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl;
import com.openexchange.realtime.hazelcast.impl.GlobalMessageDispatcherImpl;
import com.openexchange.realtime.hazelcast.impl.HazelcastStanzaStorage;
import com.openexchange.realtime.hazelcast.management.ManagementHouseKeeper;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link HazelcastRealtimeActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastRealtimeActivator extends HousekeepingActivator {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastRealtimeActivator.class);

    private final AtomicBoolean isStopped = new AtomicBoolean(true);
    private volatile HazelcastResourceDirectory directory;
    private volatile String cleanerRegistrationId;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]
            {
                HazelcastInstance.class,
                LocalMessageDispatcher.class,
                ManagementService.class,
                TimerService.class,
                LocalRealtimeCleanup.class,
                ThreadPoolService.class
            };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: {}", getClass().getCanonicalName());
        Services.setServiceLookup(this);

        ManagementHouseKeeper managementHouseKeeper = ManagementHouseKeeper.getInstance();
        managementHouseKeeper.initialize(this);

        HazelcastInstance hazelcastInstance = getService(HazelcastInstance.class);
        HazelcastAccess.setHazelcastInstance(hazelcastInstance);

        Config config = hazelcastInstance.getConfig();
        String id_map = discoverMapName(config, "rtIDMapping-");
        String resource_map = discoverMapName(config, "rtResourceDirectory-");
        if(Strings.isEmpty(id_map) || Strings.isEmpty(resource_map)) {
            String msg = "Distributed directory maps couldn't be found in hazelcast configuration";
            throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
        }
        final HazelcastResourceDirectory directory = new HazelcastResourceDirectory(id_map, resource_map);
        this.directory = directory;
        managementHouseKeeper.addManagementObject(directory.getManagementObject());

        GlobalMessageDispatcherImpl globalDispatcher = new GlobalMessageDispatcherImpl(directory);

        GlobalRealtimeCleanupImpl globalCleanup = new GlobalRealtimeCleanupImpl(directory);
        managementHouseKeeper.addManagementObject(globalCleanup.getManagementObject());

        String lock_map = discoverMapName(config, "rtCleanupLock-");
        CleanupMemberShipListener cleanupListener = new CleanupMemberShipListener(lock_map, directory, globalCleanup);
        hazelcastInstance.getCluster().addMembershipListener(cleanupListener);

        track(Channel.class, new SimpleRegistryListener<Channel>() {

            @Override
            public void added(ServiceReference<Channel> ref, Channel service) {
                directory.addChannel(service);
            }

            @Override
            public void removed(ServiceReference<Channel> ref, Channel service) {
                directory.removeChannel(service);
            }
        });

        registerService(ResourceDirectory.class, directory, null);
        registerService(MessageDispatcher.class, globalDispatcher);
        addService(MessageDispatcher.class, globalDispatcher);
        registerService(RealtimeJanitor.class, globalDispatcher);
        registerService(StanzaStorage.class, new HazelcastStanzaStorage());
        registerService(Channel.class, globalDispatcher.getChannel());
        registerService(GlobalRealtimeCleanup.class, globalCleanup);
        addService(GlobalRealtimeCleanup.class, globalCleanup);

        String client_map = discoverMapName(config, "rtClientMapping-");
        String group_map = discoverMapName(config, "rtGroupMapping-");
        DistributedGroupManagerImpl distributedGroupManager = new DistributedGroupManagerImpl(globalDispatcher, client_map, group_map);
        cleanerRegistrationId = directory.applyDistributedGroupManager(distributedGroupManager);

        registerService(DistributedGroupManager.class, distributedGroupManager);
        registerService(RealtimeJanitor.class, distributedGroupManager);
        managementHouseKeeper.addManagementObject(distributedGroupManager.getManagementObject());

        directory.addChannel(globalDispatcher.getChannel());
        try {
            managementHouseKeeper.exposeManagementObjects();
        } catch (OXException oxe) {
            LOG.error("Failed to expose ManagementObjects", oxe);
        }
        openTrackers();
        isStopped.set(false);
    }

    @Override
    protected void stopBundle() throws Exception {
        if (isStopped.compareAndSet(false, true)) {
            LOG.info("Stopping bundle: {}", getClass().getCanonicalName());

            HazelcastResourceDirectory directory = this.directory;
            if (null != directory) {
                this.directory = null;

                String cleanerRegistrationId = this.cleanerRegistrationId;
                if (null != cleanerRegistrationId) {
                    this.cleanerRegistrationId = null;
                    try {
                        directory.removeResourceMappingEntryListener(cleanerRegistrationId);
                    } catch (Exception oxe) {
                        LOG.debug("Unable to remove ResourceMappingEntryListener.");
                    }
                }
            }

            ManagementHouseKeeper.getInstance().cleanup();
            Services.setServiceLookup(null);
            super.stopBundle();
        }
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (allAvailable()) {
            LOG.info("{} regained all needed services {}. Going to restart bundle.", this.getClass().getSimpleName(), clazz.getSimpleName());
            try {
                startBundle();
            } catch (Exception e) {
                LOG.error("Error while starting bundle.", e);
            }
        }
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        LOG.warn("{} is handling unavailibility of needed service {}. Going to stop bundle.", this.getClass().getSimpleName(), clazz.getSimpleName());
        try {
            this.stopBundle();
        } catch (Exception e) {
            LOG.error("Error while stopping bundle.", e);
        }
    }

}
