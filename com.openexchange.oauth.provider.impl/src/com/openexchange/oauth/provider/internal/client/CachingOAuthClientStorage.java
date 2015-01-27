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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.client;

import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.ClientData;
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

    private final OAuthClientStorage delegate;

    /**
     * Initializes a new {@link CachingOAuthClientStorage}.
     */
    public CachingOAuthClientStorage(OAuthClientStorage delegate, ServiceLookup services) {
        super(services);
        this.delegate = delegate;
    }

    private Cache optCache() throws OXException {
        CacheService cacheService = services.getOptionalService(CacheService.class);
        return null == cacheService ? null : cacheService.getCache(REGION_NAME);
    }

    @Override
    public Client getClientById(String clientId) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.getClientById(clientId);
        }

        Object object = cache.get(clientId);
        if (object instanceof Client) {
            return (Client) object;
        }

        Client client = delegate.getClientById(clientId);
        if (null == client) {
            //  No such client
            return null;
        }
        cache.put(clientId, client, false);
        return client;
    }

    @Override
    public Client registerClient(ClientData clientData) throws OXException {
        Client newClient = delegate.registerClient(clientData);

        Cache cache = optCache();
        if (null != cache) {
            cache.put(newClient.getId(), newClient, false);
        }

        return newClient;
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws OXException {
        Client updatedClient = delegate.updateClient(clientId, clientData);

        Cache cache = optCache();
        if (null != cache) {
            cache.put(clientId, updatedClient, true);
        }

        return updatedClient;
    }

    @Override
    public boolean unregisterClient(String clientId) throws OXException {
        boolean result = delegate.unregisterClient(clientId);
        if (result) {
            Cache cache = optCache();
            if (null != cache) {
                cache.remove(clientId);
            }
        }
        return result;
    }

    @Override
    public Client revokeClientSecret(String clientId) throws OXException {
        Client revokedClient = delegate.revokeClientSecret(clientId);

        Cache cache = optCache();
        if (null != cache) {
            cache.put(clientId, revokedClient, true);
        }

        return revokedClient;
    }

    @Override
    public void invalidateClient(String clientId) {
        try {
            Cache cache = optCache();
            if (null != cache) {
                cache.remove(clientId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to invalidate client {}", clientId, e);
        }
    }

}
