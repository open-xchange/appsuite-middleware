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

package com.openexchange.caching.internal;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.jcs.admin.CountingOnlyOutputStream;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;
import com.google.common.collect.ImmutableSet;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link JCSCacheInformation} - The {@link CacheInformationMBean} implementation of <a href="http://jakarta.apache.org/jcs/">JCS</a>
 * caching system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JCSCacheInformation extends StandardMBean implements CacheInformationMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JCSCacheInformation.class);

    private final CompositeCacheManager cacheHub;
    private final JCSCacheService cacheService;

    /**
     * Initializes a new {@link JCSCacheInformation}.
     *
     * @param cacheService The JCS cache service instance
     * @throws NotCompliantMBeanException
     */
    public JCSCacheInformation(JCSCacheService cacheService) throws NotCompliantMBeanException {
        super(CacheInformationMBean.class);
        this.cacheService = cacheService;
        cacheHub = CompositeCacheManager.getInstance();
    }

    @Override
    public void clear(String name, boolean localOnly) throws MBeanException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JCSCacheInformation.class);

        if ("*".equals(name)) {
            List<String> failees = new LinkedList<>();
            for (String cacheName : cacheHub.getCacheNames()) {
                try {
                    Cache cache = cacheService.getCache(cacheName);
                    if (localOnly) {
                        cache.localClear();
                    } else {
                        cache.clear();
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    failees.add(cacheName);
                }
            }
            if (false == failees.isEmpty()) {
                StringBuilder sb = new StringBuilder("Failed to clear the following cache regions: ");
                boolean first = true;
                for (String cacheName : failees) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(cacheName);
                }
                String message = sb.toString();
                sb = null;
                throw new MBeanException(new Exception(message), message);
            }
        } else {
            if (Strings.isEmpty(name)) {
                String message = "Invalid or missing cache name";
                throw new MBeanException(new Exception(message), message);
            }
            if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
                String message = "No suche cache: " + name;
                throw new MBeanException(new Exception(message), message);
            }

            try {
                Cache cache = cacheService.getCache(name);
                if (localOnly) {
                    cache.localClear();
                } else {
                    cache.clear();
                }
            } catch (Exception e) {
                logger.error("", e);
                String message = e.getMessage();
                throw new MBeanException(new Exception(message), message);
            }
        }
    }

    @Override
    public long getMemoryCacheCount(final String name) throws MBeanException {
        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No such cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }

        return cacheHub.getCache(name).getMemoryCache().getKeyArray().length;
    }

    @Override
    public String getCacheStatistics(final String name) throws MBeanException {
        if ("*".equals(name)) {
            final String[] cacheNames = cacheHub.getCacheNames();
            final StringBuilder sb = new StringBuilder(512 * cacheNames.length);
            for (final String cacheName : cacheNames) {
                sb.append(cacheHub.getCache(cacheName).getStats()).append("\r\n\r\n");
            }
            return sb.toString();
        }

        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No suche cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }
        return cacheHub.getCache(name).getStats();
    }

    @Override
    public long getMemoryCacheDataSize(final String name) throws MBeanException {
        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No suche cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }

        final IMemoryCache memCache = cacheHub.getCache(name).getMemoryCache();

        final Iterator<?> iter = memCache.getIterator();

        final CountingOnlyOutputStream counter = new CountingOnlyOutputStream();
       ObjectOutputStream out= null;
        try {
            out = new ObjectOutputStream(counter);
        } catch (IOException e) {
            LOG.error("", e);
            Streams.close(out);
            return 0;
        }
        try {
            while (iter.hasNext()) {
                final ICacheElement ce = (ICacheElement) ((Map.Entry<?, ?>) iter.next()).getValue();
                out.writeObject(ce.getVal());
            }
            out.flush();
        } catch (Exception e) {
            LOG.info("Problem getting byte count. Likely cause is a non serializable object.{}", e.getMessage());
        } finally {
            Streams.close(out);
        }
        // 4 bytes lost for the serialization header
        return counter.getCount() - 4;
    }

    @Override
    public String[] listRegionNames() {
        return cacheHub.getCacheNames();
    }

}
