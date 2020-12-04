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

package com.openexchange.api.client.impl;

import static com.openexchange.api.client.impl.ApiClientBlacklist.isBlacklisted;
import static com.openexchange.api.client.impl.ApiClientBlacklist.isPortAllowed;
import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.Credentials;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.share.core.tools.ShareTool;

/**
 * {@link ApiClientServiceImpl} - Caching layer that manages the creation, too
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiClientServiceImpl implements ApiClientService, EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientServiceImpl.class);

    private final Cache<String, ApiClient> cachedClients;

    private final ServiceLookup services;

    private static final String PARAM_API_CLIENT_SESSIONS = "__session.api_client";

    /**
     * Initializes a new {@link ApiClientServiceImpl}.
     *
     * @param services The service lookup
     */
    public ApiClientServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
        // @formatter:off
        this.cachedClients = CacheBuilder.newBuilder()
            .initialCapacity(30)
            .maximumSize(512)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalListener<String, ApiClient>) notification -> {
                notification.getValue().logout();
             })
            .build();
        // @formatter:on
    }

    @Override
    public ApiClient getApiClient(Session session, String loginLink, Credentials creds) throws OXException {
        URL url = generateURL(loginLink);
        checkBlackList(url, session);

        Credentials credentials = null == creds ? Credentials.EMPTY : creds;
        String cacheKey = generateCacheKey(session, url);
        try {
            ApiClient client = cachedClients.get(cacheKey, () -> loginClient(chooseClient(session, url, credentials)));
            if (client.isClosed() || false == Objects.equals(client.getCredentials(), credentials)) {
                /*
                 * Client is closed or credentials are outdated. Remove from cache and create a new instance.
                 */
                close(client);
                return getApiClient(session, loginLink, credentials);
            }

            /*
             * Decorate the session so API client is closed along the user session
             */
            session.setParameter(PARAM_API_CLIENT_SESSIONS, Boolean.TRUE);

            return client;
        } catch (ExecutionException e) {
            /*
             * Throw OXException as-is if possible
             */
            if (null != e.getCause() && e.getCause() instanceof OXException) {
                throw (OXException) e.getCause();
            }
            throw ApiClientExceptions.NO_ACCESS.create(e, loginLink);
        }
    }

    @Override
    public void close(ApiClient client) {
        if (null == client) {
            return;
        }
        /*
         * Access will be closed via RemoveListener
         */
        cachedClients.invalidate(generateCacheKey(client));
    }

    @Override
    public void close(Session session, String loginLink) {

        if (Strings.isNotEmpty(loginLink)) {
            /*
             * Revoke single access
             */
            try {
                URL host = generateURL(loginLink);
                cachedClients.invalidate(generateCacheKey(session, host));
            } catch (OXException e) {
                LOGGER.warn("Unable to revoke access for user {} in context {}.", I(session.getUserId()), I(session.getContextId()), e);
            }
            return;
        }

        /*
         * Revoke all session associated accesses
         */
        List<String> removeable = new ArrayList<>();
        for (ApiClient clientAccess : cachedClients.asMap().values()) {
            if (clientAccess.getSession().equals(session.getSessionID())) {
                removeable.add(generateCacheKey(session, clientAccess.getLoginLink()));
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
        if (Strings.isEmpty(shareLink)) {
            throw ApiClientExceptions.INVALID_TARGET.create(shareLink);
        }
        try {
            if (false == shareLink.startsWith("http")) { // includes 'https'
                return new URL("https://" + shareLink);
            }
            return new URL(shareLink);
        } catch (MalformedURLException e) {
            throw ApiClientExceptions.INVALID_TARGET.create(e, shareLink);
        }
    }

    /**
     * Generates the cache key
     *
     * @param access The access
     * @return The cache key
     */
    private static String generateCacheKey(ApiClient client) {
        return generateCacheKey(client.getContextId(), client.getUserId(), client.getSession(), client.getLoginLink());
    }

    /**
     * Generates the cache key
     *
     * @param session The session
     * @param host The host to connect to
     * @return The cache key
     */
    private static String generateCacheKey(Session session, URL host) {
        return generateCacheKey(session.getContextId(), session.getUserId(), session.getSessionID(), host);
    }

    /**
     * Generates the cache key
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param sessionId The session identifier
     * @param host The host to connect to
     * @return The cache key
     */
    private static String generateCacheKey(int contextId, int userId, String sessionId, URL host) {
        String baseToken = ShareTool.getBaseToken(host.getPath());
        if (Strings.isEmpty(baseToken)) {
            return Strings.concat("-", sessionId, I(contextId), I(userId), host.getHost());
        }
        return Strings.concat("-", sessionId, I(contextId), I(userId), host.getHost(), baseToken);
    }

    /**
     * Chooses the most likely and best fitting client for the given login link
     *
     * @param session The Session
     * @param url The login link to choose the client from
     * @param credentials The optional credentials to pass to the client
     * @return The client
     */
    protected ApiClient chooseClient(Session session, URL url, Credentials credentials) {
        LOGGER.debug("Creating API client for remote system {} in context {} for user {}.", url.getHost(), I(session.getContextId()), I(session.getUserId()));

        ApiClient client = null;
        if (ShareTool.isShare(url.getPath())) {
            client = new ApiShareClient(services, session.getContextId(), session.getUserId(), session.getSessionID(), url, credentials);
        } else {
            client = new NoSessionClient(services, session.getContextId(), session.getUserId(), session.getSessionID(), url);
        }
        return client;
    }

    /**
     * Performs a login for the given client
     *
     * @param client The client to login
     * @return The logged in client
     * @throws OXException
     */
    protected ApiClient loginClient(ApiClient client) throws OXException {
        client.login();
        return client;
    }

    /**
     * Check that the given URL is not blacklisted and the port is allowed
     * <p>
     * Removes any blacklisted client from the cache.
     *
     * @param url The URL to check
     * @param session The session
     * @param userId the user ID
     * @throws OXException In case the URL is blacklisted
     */
    private void checkBlackList(URL url, Session session) throws OXException {
        LeanConfigurationService configurationService = services.getService(LeanConfigurationService.class);
        if (isBlacklisted(configurationService, url, session.getContextId(), session.getUserId())) {
            if (Strings.isNotEmpty(url.getHost())) {
                cachedClients.invalidate(generateCacheKey(session.getContextId(), session.getUserId(), session.getSessionID(), url));
            }
            throw ApiClientExceptions.NO_ACCESS.create(url.toString());
        }
        if (false == isPortAllowed(configurationService, url, session.getContextId(), session.getUserId())) {
            throw ApiClientExceptions.NO_ACCESS.create(url.toString());
        }
    }

    /*
     * -------------------------------------- EventHandler --------------------------------------
     */

    @Override
    public void handleEvent(Event event) {
        if (event == null || !SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(event.getTopic())) {
            return;
        }
        try {
            close((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
        } catch (Exception e) {
            LOGGER.warn("Unexpected error invalidating API client after local session was removed", e);
        }
    }

    /**
     * Closes all cached clients for the given session
     *
     * @param session The session
     */
    private void close(Session session) {
        if (null == session || false == session.containsParameter(PARAM_API_CLIENT_SESSIONS)) {
            return;
        }
        /*
         * Get the keys of all API clients for the given session
         */
        String sessionID = session.getSessionID();
        List<String> toInvalidate = cachedClients.asMap().keySet() //
            .stream().filter(k -> k.startsWith(sessionID)).collect(Collectors.toList());

        /*
         * Invalidate
         */
        if (toInvalidate.isEmpty() == false) {
            cachedClients.invalidateAll(toInvalidate);
            LOGGER.debug("Successfully invalidated {} API client(s) for session {}.", I(toInvalidate.size()), sessionID);
        }
    }
}
