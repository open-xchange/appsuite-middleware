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

package com.openexchange.realtime.hazelcast.osgi;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.hazelcast.Services;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.impl.GlobalMessageDispatcherImpl;
import com.openexchange.realtime.hazelcast.impl.HazelcastStanzaStorage;

/**
 * {@link HazelcastRealtimeActivator}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastRealtimeActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(HazelcastRealtimeActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastInstance.class, LocalMessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + getClass().getCanonicalName());
        Services.setServiceLookup(this);

        HazelcastInstance hazelcastInstance = getService(HazelcastInstance.class);
        HazelcastAccess.setHazelcastInstance(hazelcastInstance);
        
        // either track Hazelcast for HazelcasAccess or get it via Services each time
        track(HazelcastInstance.class, new SimpleRegistryListener<HazelcastInstance>() {

            @Override
            public void added(final ServiceReference<HazelcastInstance> ref, final HazelcastInstance hazelcastInstance) {
                HazelcastAccess.setHazelcastInstance(hazelcastInstance);
            }

            @Override
            public void removed(final ServiceReference<HazelcastInstance> ref, final HazelcastInstance hazelcastInstance) {
                HazelcastAccess.setHazelcastInstance(null);
            }
        });
        
        Config config = hazelcastInstance.getConfig();
        String id_map = discoverMapName(config, "rtIDMapping-");
        String resource_map = discoverMapName(config, "rtResourceDirectory-");
        if(Strings.isEmpty(id_map) || Strings.isEmpty(resource_map)) {
            String msg = "Distributed directory maps couldn't be found in hazelcast configuration";
            throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
        }
        final HazelcastResourceDirectory directory = new HazelcastResourceDirectory(id_map, resource_map);
        GlobalMessageDispatcherImpl globalDispatcher = new GlobalMessageDispatcherImpl(directory);
        
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
        
        openTrackers();
        registerService(ResourceDirectory.class, directory, null);
        registerService(MessageDispatcher.class, globalDispatcher);
        registerService(StanzaStorage.class, new HazelcastStanzaStorage());
        registerService(Channel.class, globalDispatcher.getChannel());
        
        directory.addChannel(globalDispatcher.getChannel());
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("Stopping bundle: " + getClass().getCanonicalName());
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * Discovers map names in the supplied hazelcast configuration based on the map prefix.
     * 
     * @param config The config object
     * @return The prefix of the map name
     */
    private String discoverMapName(Config config, String mapPrefix){
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith(mapPrefix)) {
                    return mapName;
                }
            }
        }
        return null;
    }

}
