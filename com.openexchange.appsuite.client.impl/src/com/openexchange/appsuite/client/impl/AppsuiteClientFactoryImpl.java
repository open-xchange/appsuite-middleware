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

package com.openexchange.appsuite.client.impl;

import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.getContextUserFrom;
import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.isShare;
import static com.openexchange.appsuite.client.impl.AppsuiteClientBlacklist.isBlacklisted;
import static com.openexchange.appsuite.client.impl.AppsuiteClientBlacklist.isPortAllowed;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.openexchange.annotation.NonNull;
import com.openexchange.appsuite.client.AppsuiteClient;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.AppsuiteClientFactory;
import com.openexchange.appsuite.client.AutoLoginClient;
import com.openexchange.appsuite.client.Credentials;
import com.openexchange.appsuite.client.impl.share.AppsuiteShareClient;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AppsuiteClientFactoryImpl} - Caching layer that manages the creation, too
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AppsuiteClientFactoryImpl implements AppsuiteClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppsuiteClientFactoryImpl.class);

    private final Cache<Integer, AppsuiteClient> cachedClients;

    private ServiceLookup services;

    /**
     * Initializes a new {@link AppsuiteClientFactoryImpl}.
     * 
     * @param services The service lookup
     */
    public AppsuiteClientFactoryImpl(ServiceLookup services) {
        super();
        this.services = services;
        // @formatter:off
        this.cachedClients = CacheBuilder.newBuilder()
            .initialCapacity(30)
            .maximumSize(512)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalListener<Integer, AppsuiteClient>) notification -> {
                notification.getValue().logout();
             })
            .build();
        // @formatter:on
    }

    @Override
    public AppsuiteClient generate(Session session, String loginLink, Optional<Credentials> credentials) throws OXException {
        return generate(session.getContextId(), session.getUserId(), loginLink, credentials);
    }

    @Override
    public AppsuiteClient generate(int contextId, int userId, String loginLink, Optional<Credentials> credentials) throws OXException {
        URL url = generateURL(loginLink);
        checkBlackList(url, contextId, userId);

        Integer cacheKey = generateCacheKey(contextId, userId, url);
        try {
            AppsuiteClient client = cachedClients.get(cacheKey, () -> {
                LOGGER.debug("Creating share access for remote system {} in context {} for user {}.", url.getHost(), I(contextId), I(userId));
                return chooseClient(contextId, userId, url, credentials);
            });

            if (client.isClosed()) {
                /*
                 * Remove from cache and create a new instance
                 */
                close(client);
                return generate(contextId, userId, loginLink, credentials);
            }
            return client;
        } catch (ExecutionException e) {
            throw AppsuiteClientExceptions.NO_ACCESS.create(e, loginLink);
        }
    }

    @Override
    public void close(AppsuiteClient client) {
        /*
         * Access will be closed via RemoveListener
         */
        cachedClients.invalidate(generateCacheKey(client));
    }

    @Override
    public void close(int contextId, int userId, Optional<String> targetHost) {
        if (targetHost.isPresent()) {
            /*
             * Revoke single access
             */
            try {
                URL host = generateURL(targetHost.get());
                cachedClients.invalidate(generateCacheKey(contextId, userId, host));
            } catch (OXException e) {
                LOGGER.warn("Unable to revoke access for user {} in context {}.", I(userId), I(contextId), e);
            }
            return;
        }
        /*
         * Revoke all user associated accesses
         */
        List<Integer> removeable = new ArrayList<>();
        for (AppsuiteClient clientAccess : cachedClients.asMap().values()) {
            if (clientAccess.getContextId() == contextId && clientAccess.getUserId() == userId) {
                removeable.add(generateCacheKey(clientAccess));
            }
        }
        cachedClients.invalidateAll(removeable);
    }

    /*
     * -------------------------------------- HELPERS --------------------------------------
     */

    /**
     * Check that the sharing link is syntactically correct
     *
     * @param shareLink The share link
     * @return The share link
     * @throws OXException in case link is unusable
     */
    private static @NonNull URL generateURL(String shareLink) throws OXException {
        if (null == shareLink) {
            throw AppsuiteClientExceptions.EMPTY_LINK.create();
        }
        try {
            if (false == shareLink.startsWith("http")) { // includes 'https'
                return new URL("https://" + shareLink);
            }
            return new URL(shareLink);
        } catch (MalformedURLException e) {
            throw AppsuiteClientExceptions.INVALIDE_TARGET.create(e, shareLink);
        }
    }

    /**
     * Generates the cache key
     *
     * @param access The access
     * @return The cache key
     */
    private static Integer generateCacheKey(AppsuiteClient client) {
        return generateCacheKey(client.getContextId(), client.getUserId(), client.getLoginLink());
    }

    /**
     * Generates the cache key
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param host The host to connect to
     * @return The cache key
     */
    private static Integer generateCacheKey(int contextId, int userId, URL host) {
        int hash = 2027;
        hash = hash * contextId;
        hash = hash + userId;
        hash = hash * host.getHost().hashCode();
        if (isShare(host.getPath())) {
            /*
             * Parse the share link and get the remote context and user ID.
             * Thus one client with one session can handle multiple share links
             */
            Pair<Integer, Integer> remoteContextUser = getContextUserFrom(host.getPath());
            hash = hash * i(remoteContextUser.getFirst()); // Save without null check, checked through isShare()
            hash = hash * i(remoteContextUser.getSecond());
        } else if (null != host.getPath()) {
            /*
             * Use specific path thus client
             */
            hash = hash * host.getPath().hashCode();
        } else {
            /*
             * Use full link as fallback
             */
            hash = hash * host.hashCode();
        }
        return I(hash);
    }

    /**
     * Chooses the most likely and best fitting client for the given login link
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @param url The login link to choose the client from
     * @param credentials The optional credentials to pass to the client
     * @return The client
     * @throws OXException If no fitting client can be chosen
     */
    protected AppsuiteClient chooseClient(int contextId, int userId, URL url, Optional<Credentials> credentials) throws OXException {
        AppsuiteClient client = null;
        if (isShare(url.getPath())) {
            client = new AppsuiteShareClient(services, contextId, userId, url, credentials);
        }
        if (null == client) {
            throw AppsuiteClientExceptions.UNKOWN_API.create();
        }
        if (client instanceof AutoLoginClient) {
            client.login();
        }
        return client;
    }

    /**
     * Check that the given URL is not blacklisted and the port is allowed
     * <p>
     * Removes any blacklisted client from the cache.
     *
     * @param url The URL to check
     * @param contextId The context ID
     * @param userId the user ID
     * @throws OXException In case the URL is blacklisted
     */
    private void checkBlackList(URL url, int contextId, int userId) throws OXException {
        LeanConfigurationService configurationService = services.getService(LeanConfigurationService.class);
        if (isBlacklisted(configurationService, url, contextId, userId)) {
            if (Strings.isNotEmpty(url.getHost())) {
                cachedClients.invalidate(generateCacheKey(contextId, userId, url));
            }
            throw AppsuiteClientExceptions.NO_ACCESS.create(url.toString());
        }
        if (false == isPortAllowed(configurationService, url, contextId, userId)) {
            throw AppsuiteClientExceptions.NO_ACCESS.create(url.toString());
        }
    }
}
