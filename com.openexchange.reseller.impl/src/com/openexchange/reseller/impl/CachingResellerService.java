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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.reseller.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.lock.LockService;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.reseller.data.ResellerCapability;
import com.openexchange.reseller.data.ResellerConfigProperty;
import com.openexchange.reseller.data.ResellerTaxonomy;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CachingResellerService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class CachingResellerService implements ResellerService {

    /**
     * Caches a reverse index for the context-reseller. Stores {@link ResellerValue}s
     */
    private static final String RESELLER_CONTEXT_NAME = "ResellerContext";
    private static final String CAPABILITIES_REGION_NAME = "CapabilitiesReseller";
    private static final String CONFIGURATION_REGION_NAME = "ConfigurationReseller";
    private static final String TAXONOMIES_REGION_NAME = "TaxonomiesReseller";

    private final ResellerService delegate;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CachingResellerService}.
     */
    public CachingResellerService(ServiceLookup services, ResellerService delegate) {
        super();
        this.services = services;
        this.delegate = delegate;
    }

    @Override
    public ResellerAdmin getReseller(int contextId) throws OXException {
        ResellerAdmin resellerAdmin = delegate.getReseller(contextId);
        CacheService cacheService = getCacheService();
        Cache cache = cacheService.getCache(RESELLER_CONTEXT_NAME);
        Integer key = I(contextId);
        if (null == cache.get(key)) {
            cache.put(key, new ResellerValue(resellerAdmin.getId(), resellerAdmin.getParentId()), false);
        }
        return resellerAdmin;
    }

    @Override
    public ResellerAdmin getResellerById(int resellerId) throws OXException {
        return delegate.getResellerById(resellerId);
    }

    @Override
    public ResellerAdmin getResellerByName(String resellerName) throws OXException {
        return delegate.getResellerByName(resellerName);
    }

    @Override
    public List<ResellerAdmin> getResellerAdminPath(int contextId) throws OXException {
        return delegate.getResellerAdminPath(contextId);
    }

    @Override
    public List<ResellerAdmin> getSubResellers(int parentId) throws OXException {
        return delegate.getSubResellers(parentId);
    }

    @Override
    public List<ResellerAdmin> getAll() throws OXException {
        return delegate.getAll();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ResellerCapability> getCapabilities(int resellerId) throws OXException {
        Cache cache = getCacheService().getCache(CAPABILITIES_REGION_NAME);
        Integer key = I(resellerId);
        Object object = cache.get(key);
        if (object instanceof Set) {
            return Set.class.cast(object);
        }
        LockService lockService = optLockService();
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("getResellerCapabilities-").append(resellerId).toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object instanceof Set) {
                return Set.class.cast(object);
            }
            Set<ResellerCapability> capas = delegate.getCapabilities(resellerId);
            cache.put(key, new HashSet<>(capas), false);
            return capas;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<ResellerCapability> getCapabilitiesByContext(int contextId) throws OXException {
        ResellerValue resellerValue = getResellerValue(contextId);
        Integer resellerId = resellerValue.getResellerId();
        Integer parentId = resellerValue.getParentId();

        Set<ResellerCapability> capabilities = getCapabilities(resellerId);
        if (parentId == null || parentId.intValue() == 0) {
            // Context is assigned to the root reseller, just return capas
            return capabilities;
        }

        // Traverse the admin path to get all capabilities for the context
        do {
            ResellerAdmin resellerAdmin = getResellerById(parentId);
            capabilities.addAll(getCapabilities(resellerAdmin.getId()));
            parentId = resellerAdmin.getParentId();
        } while (parentId != null && parentId.intValue() > 0);
        return capabilities;
    }

    @Override
    public ResellerConfigProperty getConfigProperty(int resellerId, String key) throws OXException {
        return getAllConfigProperties(resellerId).get(key);
    }

    @Override
    public ResellerConfigProperty getConfigPropertyByContext(int contextId, String key) throws OXException {
        return getAllConfigPropertiesByContext(contextId).get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ResellerConfigProperty> getAllConfigProperties(int resellerId) throws OXException {
        Cache cache = getCacheService().getCache(CONFIGURATION_REGION_NAME);
        Integer key = I(resellerId);
        Object object = cache.get(key);
        if (object instanceof Map) {
            return Map.class.cast(object);
        }

        LockService lockService = optLockService();
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("getAllConfigProperties-").append(resellerId).append("-").toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object instanceof Map) {
                return Map.class.cast(object);
            }
            Map<String, ResellerConfigProperty> props = delegate.getAllConfigProperties(resellerId);
            cache.put(key, new HashMap<>(props), false);
            return props;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<String, ResellerConfigProperty> getAllConfigPropertiesByContext(int contextId) throws OXException {
        ResellerValue resellerValue = getResellerValue(contextId);
        Integer resellerId = resellerValue.getResellerId();
        Integer parentId = resellerValue.getParentId();

        Map<String, ResellerConfigProperty> properties = getAllConfigProperties(resellerId);
        if (parentId == null || parentId.intValue() == 0) {
            // Context is assigned to the root reseller, just return props
            return properties;
        }

        // Traverse the admin path to get all properties for the context
        do {
            ResellerAdmin resellerAdmin = getResellerById(parentId);
            properties.putAll(getAllConfigProperties(resellerAdmin.getId()));
            parentId = resellerAdmin.getParentId();
        } while (parentId != null && parentId.intValue() > 0);
        return properties;
    }

    @Override
    public Map<String, ResellerConfigProperty> getConfigProperties(int resellerId, Set<String> keys) throws OXException {
        Map<String, ResellerConfigProperty> properties = getAllConfigProperties(resellerId);
        Map<String, ResellerConfigProperty> ret = new HashMap<>();
        for (String key : keys) {
            if (properties.containsKey(key)) {
                ret.put(key, properties.get(key));
            }
        }
        return ret;
    }

    @Override
    public Map<String, ResellerConfigProperty> getConfigPropertiesByContext(int contextId, Set<String> keys) throws OXException {
        Map<String, ResellerConfigProperty> configuration = getAllConfigPropertiesByContext(contextId);
        Map<String, ResellerConfigProperty> ret = new HashMap<>();
        for (String k : keys) {
            if (configuration.containsKey(k)) {
                ret.put(k, configuration.get(k));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ResellerTaxonomy> getTaxonomies(int resellerId) throws OXException {
        Cache cache = getCacheService().getCache(TAXONOMIES_REGION_NAME);
        Integer key = I(resellerId);
        Object object = cache.get(key);
        if (object instanceof Set) {
            return Set.class.cast(object);
        }

        LockService lockService = optLockService();
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("getTaxonomies-").append(resellerId).append("-").toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object instanceof Map) {
                return Set.class.cast(object);
            }
            Set<ResellerTaxonomy> taxonomies = delegate.getTaxonomies(resellerId);
            cache.put(key, new HashSet<>(taxonomies), false);
            return taxonomies;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<ResellerTaxonomy> getTaxonomiesByContext(int contextId) throws OXException {
        ResellerValue resellerValue = getResellerValue(contextId);
        Integer resellerId = resellerValue.getResellerId();
        Integer parentId = resellerValue.getParentId();

        Set<ResellerTaxonomy> taxonomies = getTaxonomies(resellerId);
        if (parentId == null || parentId.intValue() == 0) {
            // Context is assigned to the root reseller, just return taxonomies
            return taxonomies;
        }

        // Traverse the admin path to get all taxonomies for the context
        do {
            ResellerAdmin resellerAdmin = getResellerById(parentId);
            taxonomies.addAll(getTaxonomies(resellerAdmin.getId()));
            parentId = resellerAdmin.getParentId();
        } while (parentId != null && parentId.intValue() > 0);
        return taxonomies;
    }

    ////////////////////////////////// HELPERS ///////////////////////////

    /**
     * Retrieves the cached {@link ResellerValue} for the specified context
     *
     * @param contextId The context identifier
     * @return The {@link ResellerValue}
     * @throws OXException if an error is occurred
     */
    private ResellerValue getResellerValue(int contextId) throws OXException {
        CacheService cacheService = getCacheService();
        Cache cache = cacheService.getCache(RESELLER_CONTEXT_NAME);
        Integer key = I(contextId);
        Object object = cache.get(key);
        if (object instanceof ResellerValue) {
            return ResellerValue.class.cast(object);
        }
        ResellerAdmin ra = getReseller(contextId);
        return new ResellerValue(ra.getId(), ra.getParentId());
    }

    /**
     * Returns the {@link CacheService}
     * 
     * @return the {@link CacheService}
     * @throws OXException if the service is absent.
     */
    private CacheService getCacheService() throws OXException {
        return services.getServiceSafe(CacheService.class);
    }

    /**
     * Optionally gets the {@link LockService} or <code>null</code>
     * if the service is absent.
     * 
     * @return the {@link LockService} or <code>null</code> if the service is absent.
     */
    private LockService optLockService() {
        return services.getOptionalService(LockService.class);
    }

}
