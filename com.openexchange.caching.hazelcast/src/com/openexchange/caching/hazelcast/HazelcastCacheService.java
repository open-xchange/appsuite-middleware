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
import java.util.Collection;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyImpl;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.hazelcast.util.ConfigurationParser;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link HazelcastCacheService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastCacheService implements CacheService {

    /**
     * The name prefix for a {@link IMap}.
     */
    public static final String NAME_PREFIX = "com.openexchange.caching.hazelcast.region.";

    private final HazelcastInstance hazelcastInstance;

    private final ISet<String> regionNames;

    /**
     * Initializes a new {@link HazelcastCacheService}.
     */
    public HazelcastCacheService(final HazelcastInstance hazelcastInstance) {
        super();
        this.hazelcastInstance = hazelcastInstance;
        regionNames = hazelcastInstance.getSet("com.openexchange.caching.hazelcast.regionNames");
    }

    @Override
    public Cache getCache(final String name) throws OXException {
        final String mapName = NAME_PREFIX + name;
        if (!regionNames.add(name)) {
            return new HazelcastCache(mapName, hazelcastInstance);
        }
        final Config cfg = hazelcastInstance.getConfig();
        MapConfig mapCfg = cfg.getMapConfig(mapName);
        if (null != mapCfg) {
            return new HazelcastCache(mapName, hazelcastInstance);
        }
        // Check for default map configuration
        mapCfg = cfg.getMapConfig(NAME_PREFIX + "default");
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
        final String mapName = NAME_PREFIX + name;
        if (regionNames.contains(mapName)) {
            hazelcastInstance.getMap(mapName).destroy();
        }
    }

    @Override
    public void loadConfiguration(final String cacheConfigFile) throws OXException {
        try {
            final File file = new File(cacheConfigFile);
            final Collection<MapConfig> configs = ConfigurationParser.parseConfig(new FileInputStream(file));
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final MapConfig mapConfig : configs) {
                    config.addMapConfig(mapConfig);
                }
            }
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadConfiguration(final InputStream inputStream) throws OXException {
        try {
            final Collection<MapConfig> configs = ConfigurationParser.parseConfig(inputStream);
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final MapConfig mapConfig : configs) {
                    config.addMapConfig(mapConfig);
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
            final Collection<MapConfig> configs = ConfigurationParser.parseConfig(new FileInputStream(file));
            if (null != configs && !configs.isEmpty()) {
                final Config config = hazelcastInstance.getConfig();
                for (final MapConfig mapConfig : configs) {
                    config.addMapConfig(mapConfig);
                }
            }
        } catch (final IOException e) {
            throw CacheExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final int objectId) {
        return new CacheKeyImpl(contextId, objectId);
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final Serializable obj) {
        return new CacheKeyImpl(contextId, obj);
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final Serializable... objs) {
        return new CacheKeyImpl(contextId, objs);
    }

}
