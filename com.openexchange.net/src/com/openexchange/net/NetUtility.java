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

package com.openexchange.net;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import com.openexchange.net.utils.Strings;

/**
 * {@link NetUtility} - A utility class for network-related parsing/processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class NetUtility {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NetUtility.class);

    /**
     * Initializes a new {@link NetUtility}.
     */
    private NetUtility() {
        super();
    }

    private static final class InetAddressCache {

        private final Object lock;
        private final LinkedHashMap cache;

        InetAddressCache(LinkedHashMap cache, Object lock) {
            super();
            this.cache = cache;
            this.lock = lock;
        }

        void clearCache() {
            synchronized (lock) {
                cache.clear();
            }
        }

        void clearCacheFor(String hostName) {
            synchronized (lock) {
                cache.remove(Strings.asciiLowerCase(hostName));
            }
        }
    }

    private static final class InetAddressCaches {

        private final InetAddressCache addressCache;
        private final InetAddressCache negativeCache;

        InetAddressCaches(InetAddressCache addressCache, InetAddressCache negativeCache) {
            super();
            this.addressCache = addressCache;
            this.negativeCache = negativeCache;
        }

        void clearCaches() {
            addressCache.clearCache();
            addressCache.clearCache();
        }

        void clearCachesFor(String hostName) {
            addressCache.clearCacheFor(hostName);
            addressCache.clearCacheFor(hostName);
        }
    }

    private static volatile InetAddressCaches caches;

    private static InetAddressCaches getInetAddressCaches() {
        InetAddressCaches tmp = caches;
        if (null == tmp) {
            synchronized (NetUtility.class) {
                tmp = caches;
                if (null == tmp) {
                    try {
                        // Acquire the "addressCache" cache instance for positive DNS look-ups
                        Field addressCacheField = InetAddress.class.getDeclaredField("addressCache");
                        addressCacheField.setAccessible(true);
                        Object addressCache = addressCacheField.get(null);

                        // Acquire the "negativeCache" cache instance for negative DNS look-ups
                        Field negativeCacheField = InetAddress.class.getDeclaredField("negativeCache");
                        negativeCacheField.setAccessible(true);
                        Object negativeCache = negativeCacheField.get(null);

                        // Acquire the inner LinkedHashMap instances from both caches - "addressCache" cache and "negativeCache" cache
                        Class<?> cacheClazz = Class.forName("java.net.InetAddress$Cache");
                        Field cacheField = cacheClazz.getDeclaredField("cache");
                        cacheField.setAccessible(true);
                        LinkedHashMap posCache = (LinkedHashMap) cacheField.get(addressCache);
                        LinkedHashMap negCache = (LinkedHashMap) cacheField.get(negativeCache);

                        // Both - addressCache and negativeCache -  are guarded by addressCache mutex
                        InetAddressCache positiveInetAddressCache = new InetAddressCache(posCache, addressCache);
                        InetAddressCache negativeInetAddressCache = new InetAddressCache(negCache, addressCache);

                        tmp = new InetAddressCaches(positiveInetAddressCache, negativeInetAddressCache);
                        caches = tmp;
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialze fields for InetAddress cache", e);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Flushes the JVM-internal caches for DNS look-ups.
     */
    public static void flushInetAddressCache() {
        InetAddressCaches caches = getInetAddressCaches();
        if (null == caches) {
            LOGGER.warn("Failed to flush DNS look-up caches");
            return;
        }
        caches.clearCaches();
    }

    /**
     * Flushes the specified host name from JVM-internal caches for DNS look-ups.
     *
     * @param hostName The host name to remove
     */
    public static void flushInetAddressCacheFor(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return;
        }

        InetAddressCaches caches = getInetAddressCaches();
        if (null == caches) {
            LOGGER.warn("Failed to flush DNS look-up caches for {}", hostName);
            return;
        }
        caches.clearCachesFor(hostName);
    }

}
