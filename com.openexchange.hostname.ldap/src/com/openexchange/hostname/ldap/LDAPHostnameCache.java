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

package com.openexchange.hostname.ldap;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.caching.Cache;
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
