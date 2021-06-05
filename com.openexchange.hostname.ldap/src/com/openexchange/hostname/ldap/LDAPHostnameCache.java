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

package com.openexchange.hostname.ldap;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.exception.OXException;
import com.openexchange.hostname.ldap.osgi.Services;

public class LDAPHostnameCache {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LDAPHostnameCache.class);

    /** The name for the cache region */
    public final static String REGION_NAME = "LDAPHostname";

    private static final LDAPHostnameCache INSTANCE = new LDAPHostnameCache();

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     */
    public static LDAPHostnameCache getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Singleton instantiation.
     */
    private LDAPHostnameCache() {
        super();
    }

    private Cache optCache() {
        try {
            CacheService service = Services.optService(CacheService.class);
            return null == service ? null : service.getCache(REGION_NAME);
        } catch (Exception e) {
            LOG.warn("Cache {} could not be obtained", REGION_NAME, e);
            return null;
        }
    }

    /**
     * Associates the given host names with specified context identifier in cache.
     *
     * @param contextId The context identifier
     * @param hostnames The associated host names
     * @throws OXException If operation fails
     */
    public void addHostnamesToCache(int contextId, String[] hostnames) throws OXException {
        if (hostnames != null && hostnames.length > 0) {
            Cache cache = optCache();
            if (null != cache) {
                Integer iContextId = I(contextId);
                try {
                    cache.putSafe(iContextId, hostnames);
                } catch (Exception e) {
                    // Put into cache failed
                    cache.remove(iContextId);
                    cache.put(iContextId, hostnames, false);
                }
            }
        }
    }

    /**
     * Gets the host name associated with given context identifier from cache
     *
     * @param contextId The context identifier
     * @return The associated host name or <code>null</code>
     */
    public String[] getHostnamesFromCache(int contextId) {
        Cache cache = optCache();
        return null == cache ? null : (String[]) cache.get(I(contextId));
    }

    /**
     * Outputs the cache configuration to this class' logger using <code>INFO</code> log level.
     *
     * @throws OXException If logging fails
     */
    public void outputSettings() throws OXException {
        Cache cache = optCache();
        if (cache == null) {
            throw CacheExceptionCode.MISSING_CACHE_REGION.create(REGION_NAME);
        }
        final ElementAttributes defaultElementAttributes = cache.getDefaultElementAttributes();
        final StringBuilder sb = new StringBuilder(128).append('\n');
        sb.append("Cache setting for hostname ldap bundle:\n");
        sb.append("\tCreate time: ");
        sb.append(defaultElementAttributes.getCreateTime());
        sb.append('\n');
        sb.append("\tIdle time: ");
        sb.append(defaultElementAttributes.getIdleTime());
        sb.append('\n');
        sb.append("\tMax life: ");
        sb.append(defaultElementAttributes.getMaxLifeSeconds());
        sb.append('\n');
        sb.append("\tTime To Live time: ");
        sb.append(defaultElementAttributes.getTimeToLiveSeconds());
        sb.append('\n');
        sb.append("\tSize: ");
        sb.append(defaultElementAttributes.getSize());
        sb.append('\n');
        sb.append("\tIsEternal: ");
        sb.append(defaultElementAttributes.getIsEternal());
        sb.append('\n');
        sb.append("\tIsLateral: ");
        sb.append(defaultElementAttributes.getIsLateral());
        sb.append('\n');
        sb.append("\tIsRemote: ");
        sb.append(defaultElementAttributes.getIsRemote());
        sb.append("\tIsSpool: ");
        sb.append(defaultElementAttributes.getIsSpool());
        sb.append('\n');
        LOG.info(sb.toString());
    }

}
