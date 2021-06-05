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

package com.openexchange.oauth.provider.impl.client.storage;

import java.util.List;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientData;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason;
import com.openexchange.server.ServiceLookup;


/**
 * {@link CachingOAuthClientStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CachingOAuthClientStorage extends AbstractOAuthClientStorage {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CachingOAuthClientStorage.class);

    public static final String REGION_NAME = "OAuthClientStorage";

    private final String miss;
    private final OAuthClientStorage delegate;
    private final int cachingContextId = -1;

    /**
     * Initializes a new {@link CachingOAuthClientStorage}.
     */
    public CachingOAuthClientStorage(OAuthClientStorage delegate, ServiceLookup services) {
        super(services);
        this.delegate = delegate;
        miss = "miss";
    }

    private Cache optCache() throws ClientManagementException {
        CacheService cacheService = services.getService(CacheService.class);
        try {
            return null == cacheService ? null : cacheService.getCache(REGION_NAME);
        } catch (OXException e) {
            throw new ClientManagementException(e, Reason.INTERNAL_ERROR, "Error while getting JCS cache region");
        }
    }

    @Override
    public List<Client> getClients(String groupId) throws ClientManagementException {
        List<Client> clients = delegate.getClients(groupId);
        Cache cache = optCache();
        if (cache != null) {
            for (Client client : clients) {
                String clientId = client.getId();
                CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
                Object object = cache.get(newCacheKey);
                if (object == null) {
                    try {
                        cache.put(newCacheKey, client, false);
                    } catch (OXException e) {
                        LOGGER.warn("Could not put client into cache", e);
                    }
                }
            }
        }

        return clients;
    }

    @Override
    public Client getClientById(String groupId, String clientId) throws ClientManagementException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.getClientById(groupId, clientId);
        }

        CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
        Object object = cache.get(newCacheKey);
        if (object == miss) {
            return null;
        }
        if (object instanceof Client) {
            return (Client) object;
        }

        Client client = delegate.getClientById(groupId, clientId);
        try {
            if (null == client) {
                //  No such client
                cache.put(newCacheKey, miss, false);
                return null;
            }
            cache.put(newCacheKey, client, false);
        } catch (OXException e) {
            LOGGER.warn("Could not put client into cache", e);
        }
        return client;
    }

    @Override
    public Client registerClient(String groupId, String clientId, String secret, ClientData clientData) throws ClientManagementException {
        Client newClient = delegate.registerClient(groupId, clientId, secret, clientData);

        Cache cache = optCache();
        if (null != cache) {
            CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, newClient.getId());
            try {
                cache.put(newCacheKey, newClient, false);
            } catch (OXException e) {
                LOGGER.warn("Could not put client into cache", e);
            }
        }

        return newClient;
    }

    @Override
    public boolean enableClient(String groupId, String clientId) throws ClientManagementException {
        try {
            return delegate.enableClient(groupId, clientId);
        } finally {
            invalidateClient(groupId, clientId);
        }
    }

    @Override
    public boolean disableClient(String groupId, String clientId) throws ClientManagementException {
        try {
            return delegate.disableClient(groupId, clientId);
        } finally {
            invalidateClient(groupId, clientId);
        }
    }

    @Override
    public Client updateClient(String groupId, String clientId, ClientData clientData) throws ClientManagementException {
        Client updatedClient = delegate.updateClient(groupId, clientId, clientData);

        Cache cache = optCache();
        if (null != cache) {
            CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
            try {
                cache.put(newCacheKey, updatedClient, true);
            } catch (OXException e) {
                LOGGER.warn("Could not put client into cache", e);
            }
        }

        return updatedClient;
    }

    @Override
    public boolean unregisterClient(String groupId, String clientId) throws ClientManagementException {
        boolean result = delegate.unregisterClient(groupId, clientId);
        if (result) {
            Cache cache = optCache();
            if (null != cache) {
                CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
                try {
                    cache.remove(newCacheKey);
                } catch (OXException e) {
                    LOGGER.warn("Could not remove client from cache", e);
                }
            }
        }
        return result;
    }

    @Override
    public Client revokeClientSecret(String groupId, String clientId, String secret) throws ClientManagementException {
        Client revokedClient = delegate.revokeClientSecret(groupId, clientId, secret);

        Cache cache = optCache();
        if (null != cache) {
            CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
            try {
                cache.put(newCacheKey, revokedClient, true);
            } catch (OXException e) {
                LOGGER.warn("Could not remove client from cache", e);
            }
        }

        return revokedClient;
    }

    @Override
    public void invalidateClient(String groupId, String clientId) {
        try {
            Cache cache = optCache();
            if (null != cache) {
                CacheKey newCacheKey = cache.newCacheKey(cachingContextId, groupId, clientId);
                cache.remove(newCacheKey);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to invalidate client {} in group {}", clientId, groupId, e);
        }
    }
}
