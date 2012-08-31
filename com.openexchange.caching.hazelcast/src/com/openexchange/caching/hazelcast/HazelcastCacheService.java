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

package com.openexchange.caching.hazelcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Instance.InstanceType;
import com.hazelcast.core.Member;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.hazelcast.util.ConfigurationParser;
import com.openexchange.caching.hazelcast.util.LocalCacheGenerator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link HazelcastCacheService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastCacheService extends DefaultCacheKeyService implements CacheService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastCacheService.class);

    private static final Boolean PRESENT = Boolean.TRUE;

    /**
     * The name prefix for a {@link IMap}.
     */
    public static final String NAME_PREFIX = "com.openexchange.caching.hazelcast.region.";

    /**
     * Gets Hazelcast's name for specified region name.
     * 
     * @param name The region name
     * @return The Hazelcast's name
     */
    public static String mapName(final String name) {
        return NAME_PREFIX + name;
    }

    private final HazelcastInstance hazelcastInstance;

    private final ConcurrentMap<String, LocalCache> localOnlyCaches;

    private final ConcurrentMap<String, Boolean> regionNames;

    /**
     * Initializes a new {@link HazelcastCacheService}.
     */
    public HazelcastCacheService(final HazelcastInstance hazelcastInstance) {
        super();
        this.hazelcastInstance = hazelcastInstance;
        regionNames = new NonBlockingHashMap<String, Boolean>(16);
        localOnlyCaches = new NonBlockingHashMap<String, LocalCache>(16);
    }

    /**
     * Performs shut-down.
     */
    public void shutdown(final boolean clusterWide) {
        for (final LocalCache localCache : localOnlyCaches.values()) {
            localCache.dispose();
        }
        localOnlyCaches.clear();
        if (clusterWide || imTheOnlyOne()) {
            // Drop associated Hazelcast resources
            final String namePrefix = NAME_PREFIX;
            for (final Instance instance : hazelcastInstance.getInstances()) {
                final InstanceType instanceType = instance.getInstanceType();
                if (InstanceType.MAP.equals(instanceType) || InstanceType.SET.equals(instanceType)) {
                    if (instance.getId().toString().indexOf(namePrefix) >= 0) {
                        instance.destroy();
                    }
                }
            }
        }
        regionNames.clear();
    }
    
    private boolean imTheOnlyOne() {
        final Set<Member> members = hazelcastInstance.getCluster().getMembers();
        if (members.size() > 1) {
            return false;
        }
        for (final Member member : members) {
            if (!member.localMember()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the map of known local caches.
     * 
     * @return The local caches
     */
    public ConcurrentMap<String, LocalCache> getLocalOnlyCaches() {
        return localOnlyCaches;
    }

    @Override
    public boolean isDistributed() {
        return true;
    }

    @Override
    public boolean isReplicated() {
        return false;
    }

    @Override
    public Cache getCache(final String name) throws OXException {
        final String mapName = mapName(name);
        final LocalCache localCache = localOnlyCaches.get(mapName);
        if (null != localCache) {
            return localCache;
        }
        if (null != regionNames.putIfAbsent(name, PRESENT)) {
            return new HazelcastCache(mapName, hazelcastInstance);
        }
        final Config cfg = hazelcastInstance.getConfig();
        MapConfig mapCfg = cfg.getMapConfig(mapName);
        if (null != mapCfg) {
            return new HazelcastCache(mapName, hazelcastInstance);
        }
        LOG.warn("Missing configuration for cache region \"" + name + "\". Using default configuration.");
        // Check for default map configuration
        mapCfg = cfg.getMapConfig(mapName("default"));
        if (null == mapCfg) {
            mapCfg = new MapConfig();
            mapCfg.setName(mapName);
            mapCfg.setBackupCount(2);
            mapCfg.getMaxSizeConfig().setSize(100000);
            mapCfg.setTimeToLiveSeconds(300);
            final NearCacheConfig nearCacheConfig = new NearCacheConfig();
            nearCacheConfig.setMaxSize(1000).setMaxIdleSeconds(120).setTimeToLiveSeconds(300);
            mapCfg.setNearCacheConfig(nearCacheConfig);
        } else {
            mapCfg = new MapConfig(mapCfg); // clone
            mapCfg.setName(mapName);
        }
        cfg.addMapConfig(mapCfg);
        return new HazelcastCache(mapName, hazelcastInstance);
    }

    @Override
    public void freeCache(final String name) throws OXException {
        final String mapName = mapName(name);
        final LocalCache localCache = localOnlyCaches.get(mapName);
        if (null != localCache) {
            localCache.dispose();
        } else if (regionNames.containsKey(name)) {
            hazelcastInstance.getMap(mapName).destroy();
        }
    }

    @Override
    public void loadConfiguration(final String cacheConfigFile) throws OXException {
        try {
            final File file = new File(cacheConfigFile);
            final Map<MapConfig, Boolean> configs = ConfigurationParser.parseConfig(new FileInputStream(file));
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final Map.Entry<MapConfig, Boolean> entry : configs.entrySet()) {
                    final MapConfig mapConfig = entry.getKey();
                    if (entry.getValue().booleanValue()) {
                        // Local only
                        localOnlyCaches.put(
                            mapConfig.getName(),
                            new LocalCache(LocalCacheGenerator.<Serializable, Serializable> createLocalCache(mapConfig), mapConfig));
                    } else {
                        config.addMapConfig(mapConfig);
                    }
                }
            }
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadConfiguration(final InputStream inputStream) throws OXException {
        try {
            final Map<MapConfig, Boolean> configs = ConfigurationParser.parseConfig(inputStream);
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final Map.Entry<MapConfig, Boolean> entry : configs.entrySet()) {
                    final MapConfig mapConfig = entry.getKey();
                    if (entry.getValue().booleanValue()) {
                        // Local only
                        localOnlyCaches.put(
                            mapConfig.getName(),
                            new LocalCache(LocalCacheGenerator.<Serializable, Serializable> createLocalCache(mapConfig), mapConfig));
                    } else {
                        config.addMapConfig(mapConfig);
                    }
                }
            }
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadDefaultConfiguration() throws OXException {
        try {
            final File file = Services.getService(ConfigurationService.class).getFileByName("cache.ccf");
            final Map<MapConfig, Boolean> configs = ConfigurationParser.parseConfig(new FileInputStream(file));
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final Map.Entry<MapConfig, Boolean> entry : configs.entrySet()) {
                    final MapConfig mapConfig = entry.getKey();
                    if (entry.getValue().booleanValue()) {
                        // Local only
                        localOnlyCaches.put(
                            mapConfig.getName(),
                            new LocalCache(LocalCacheGenerator.<Serializable, Serializable> createLocalCache(mapConfig), mapConfig));
                    } else {
                        config.addMapConfig(mapConfig);
                    }
                }
            }
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadConfiguration(Properties properties) throws OXException {
        final Map<MapConfig, Boolean> configs = ConfigurationParser.parseConfig(properties);
        if (null != configs && !configs.isEmpty()) {
            final Config config = hazelcastInstance.getConfig();
            for (final Map.Entry<MapConfig, Boolean> entry : configs.entrySet()) {
                final MapConfig mapConfig = entry.getKey();
                if (entry.getValue().booleanValue()) {
                    // Local only
                    localOnlyCaches.put(
                        mapConfig.getName(),
                        new LocalCache(LocalCacheGenerator.<Serializable, Serializable> createLocalCache(mapConfig), mapConfig));
                } else {
                    config.addMapConfig(mapConfig);
                }
            }
        }
    }

}
