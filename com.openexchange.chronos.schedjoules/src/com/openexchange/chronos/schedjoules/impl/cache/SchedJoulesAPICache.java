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

package com.openexchange.chronos.schedjoules.impl.cache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.impl.SchedJoulesProperty;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link SchedJoulesAPICache} - Caches {@link SchedJoulesAPI} instances by using the api key
 * as the cache key.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesAPICache {

    private static final Logger LOG = LoggerFactory.getLogger(SchedJoulesAPICache.class);

    /**
     * Cache for the API clients
     */
    private final Cache<SchedJoulesCachedAPIKey, SchedJoulesAPI> apiCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
        SchedJoulesCachedAPIKey key = (SchedJoulesCachedAPIKey) notification.getKey();
        LOG.debug("Shutting down SchedJoules API for key '{}'.", key.getApiKeyHash());
    }).build();

    /**
     * Initialises a new {@link SchedJoulesAPICache}.
     */
    public SchedJoulesAPICache() {
        super();
    }

    /**
     * Retrieves a {@link SchedJoulesAPI} instance. If none in cache a new one will be initialised.
     *
     * @param contextId The context identifier
     * @return The {@link SchedJoulesAPI}
     * @throws OXException if an error is occurred
     */
    public SchedJoulesAPI getAPI(int contextId) throws OXException {
        try {
            String apiKeyHash = getHashedAPIKey(contextId);
            return apiCache.get(new SchedJoulesCachedAPIKey(apiKeyHash, contextId), () -> {
                LOG.debug("Cache miss for key '{}', initialising new SchedJoules API.", apiKeyHash);
                return new SchedJoulesAPI(getProperty(contextId, SchedJoulesProperty.scheme), getProperty(contextId, SchedJoulesProperty.host), getProperty(contextId, SchedJoulesProperty.apiKey));
            });
        } catch (ExecutionException e) {
            throw SchedJoulesAPIExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the API key for the specified context and hashes it to create
     * a unique key for the client
     *
     * @param contextId The context identifier
     * @return The hash
     * @throws OXException if the API key is not configured for the specified context
     */
    private String getHashedAPIKey(int contextId) throws OXException {
        return DigestUtils.sha256Hex(getProperty(contextId, SchedJoulesProperty.apiKey));
    }

    /**
     * Retrieves the desired {@link SchedJoulesProperty} for the specified context
     *
     * @param contextId The context identifier
     * @param property The {@link SchedJoulesProperty} to get
     * @return The value of the property
     * @throws OXException if no property is configured
     */
    private String getProperty(int contextId, SchedJoulesProperty property) throws OXException {
        LeanConfigurationService leanConfigService = Services.getService(LeanConfigurationService.class);
        String value = leanConfigService.getProperty(-1, contextId, property);
        if (Strings.isEmpty(value)) {
            throw SchedJoulesAPIExceptionCodes.CONFIGURATION_MISSING.create(property.getFQPropertyName());
        }
        return value;
    }
}
