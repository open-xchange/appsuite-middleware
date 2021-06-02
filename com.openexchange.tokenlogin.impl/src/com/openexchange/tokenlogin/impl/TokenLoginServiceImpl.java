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

package com.openexchange.tokenlogin.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.lock.LockService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.DefaultAddSessionParameter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tokenlogin.DefaultTokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginExceptionCodes;
import com.openexchange.tokenlogin.TokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginService;

/**
 * {@link TokenLoginServiceImpl} - Implementation of {@code TokenLoginService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TokenLoginServiceImpl implements TokenLoginService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenLoginServiceImpl.class);

    private final Blocker blocker = new ConcurrentBlocker();

    private volatile String sessionId2tokenMapName;

    private final Cache<String, String> sessionId2token;

    private final Map<String, TokenLoginSecret> secrets;

    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;

    private volatile boolean useHzMap = false;

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
     *
     * @param maxIdleTime The max. idle time for the local cache
     * @param configService The configuration service
     */
    public TokenLoginServiceImpl(final int maxIdleTime, final ConfigurationService configService) throws OXException {
        this(maxIdleTime, configService, null);
    }

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
     *
     * @param maxIdleTime The max. idle time for the local cache
     * @param configService The configuration service
     * @param notActiveExceptionHandler The {@link HazelcastInstanceNotActiveExceptionHandler}
     */
    public TokenLoginServiceImpl(final int maxIdleTime, final ConfigurationService configService, final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler) throws OXException {
        super();
        Validate.notNull(configService);
        {
            CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(Integer.MAX_VALUE)
                .initialCapacity(1024)
                .expireAfterAccess(maxIdleTime, TimeUnit.MILLISECONDS);
            Cache<String, String> sessionId2token = cacheBuilder.build();
            this.sessionId2token = sessionId2token;
        }
        // Parse app secrets
        secrets = initSecrets(configService.getFileByName("tokenlogin-secrets"));

        this.notActiveExceptionHandler = notActiveExceptionHandler;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes web application secrets
     *
     * @param secretsFile The secrets file to use
     * @return A map of secret to {@link TokenLoginSecret}s
     * @throws OXException in case an error occured while reading the given file
     */
    protected Map<String, TokenLoginSecret> initSecrets(final File secretsFile) throws OXException {
        if (null == secretsFile) {
            return Collections.emptyMap();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(secretsFile), StandardCharsets.UTF_8));
            final Map<String, TokenLoginSecret> map = new LinkedHashMap<String, TokenLoginSecret>(16);
            String line;
            while ((line = reader.readLine()) != null) {
                if (Strings.isNotEmpty(line)) {
                    line = line.trim();
                    if (!line.startsWith("#") && !line.startsWith("!")) {
                        // Parse secret + parameters
                        int pos = line.indexOf(';');
                        if (pos < 0) {
                            map.put(line, new DefaultTokenLoginSecret().setSecret(line));
                        } else {
                            final String secret = line.substring(0, pos).trim();
                            final DefaultTokenLoginSecret tokenLoginSecret = new DefaultTokenLoginSecret().setSecret(secret);
                            final Map<String, Object> params = new LinkedHashMap<String, Object>(4);
                            // Parse parameters
                            do {
                                final int start = pos + 1;
                                pos = line.indexOf(';', start);
                                if (pos < 0) {
                                    parseParameter(line.substring(start).trim(), params);
                                } else {
                                    parseParameter(line.substring(start, pos).trim(), params);
                                }
                            } while (pos > 0);
                            tokenLoginSecret.setParameters(params);
                            map.put(secret, tokenLoginSecret);
                        }
                    }
                }
            }
            return map;
        } catch (IOException e) {
            throw TokenLoginExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * Parse secret parameter
     *
     * @param param The param to parse
     * @param params The map to add the params to
     */
    private static void parseParameter(final String param, final Map<String, Object> params) {
        if (Strings.isNotEmpty(param)) {
            final int pos = param.indexOf('=');
            if (pos < 0) {
                params.put(param, Boolean.TRUE);
            } else {
                final String value = Strings.unquote(param.substring(pos + 1).trim());
                if ("true".equalsIgnoreCase(value)) {
                    params.put(param.substring(0, pos).trim(), Boolean.TRUE);
                } else if ("false".equalsIgnoreCase(value)) {
                    params.put(param.substring(0, pos).trim(), Boolean.FALSE);
                } else {
                    try {
                        final Integer i = Integer.valueOf(value);
                        params.put(param.substring(0, pos).trim(), i);
                    } catch (@SuppressWarnings("unused") NumberFormatException nfe1) {
                        // Try parse to long
                        try {
                            final Long l = Long.valueOf(value);
                            params.put(param.substring(0, pos).trim(), l);
                        } catch (@SuppressWarnings("unused") NumberFormatException nfe2) {
                            // Assume String value
                            params.put(param.substring(0, pos).trim(), value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public TokenLoginSecret getTokenLoginSecret(final String secret) {
        return Strings.isEmpty(secret) ? null : secrets.get(secret);
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * Sets the name of the backing Hazelcast map
     *
     * @param hzMapName The map name
     */
    public void setBackingHzMapName(final String sessionId2tokenMapName) {
        this.sessionId2tokenMapName = sessionId2tokenMapName;
    }

    /**
     * Handles {@link HazelcastInstanceNotActiveException}
     *
     * @param e The {@link HazelcastInstanceNotActiveException}
     */
    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error.", HazelcastInstanceNotActiveException.class.getSimpleName());
        changeBackingMapToLocalMap();

        HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler = this.notActiveExceptionHandler;
        if (null != notActiveExceptionHandler) {
            notActiveExceptionHandler.propagateNotActive(e);
        }
    }

    /**
     * Gets the Hazelcast map or <code>null</code> if unavailable.
     *
     * @param mapIdentifier The Hazelcast map identifier
     */
    private IMap<String, String> hzMap(String mapIdentifier) {
        if (null == mapIdentifier) {
            LOG.trace("Name of Hazelcast map is missing for token login service.");
            return null;
        }
        final HazelcastInstance hazelcastInstance = Services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOG.trace("Hazelcast instance is not available.");
            return null;
        }
        try {
            return hazelcastInstance.getMap(mapIdentifier);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        }
    }

    /**
     * Removes the key/value pair from the Hazelcast map with the given identifier
     *
     * @param mapIdentifier The Hazelcast map identifier
     * @param key The key to remove
     * @return The removed value or <code>null</code>
     */
    private String removeFromHzMap(String mapIdentifier, String key) {
        IMap<String, String> hzMap = hzMap(mapIdentifier);
        String retval = null;
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            // This MUST be synchronous!
            retval = hzMap.remove(key);
        }
        return retval;
    }

    /**
     * Puts the given key/value pair into the Hazelcast map with given identifier if absent
     *
     * @param mapIdentifier The Hazelcast map identifier
     * @param key The key to add
     * @param value The value to add
     * @return The already existing value or <code>null</code>
     * @throws OXException If Hazelcast map is not available
     */
    private String putToHzMapIfAbsent(String mapIdentifier, String key, String value) throws OXException {
        IMap<String, String> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            throw OXException.general("Hazelcast map for remote token logins is not available.");
        }
        // This MUST be synchronous!
        return hzMap.putIfAbsent(key, value);
    }

    /**
     * Puts the given key/value pair into the Hazelcast map
     *
     * @param mapIdentifier The Hazelcast map identifier
     * @param key The key to add
     * @param value The value to add
     * @throws OXException If Hazelcast map is not available
     */
    private void putToHzMap(String mapIdentifier, String key, String value) throws OXException {
        IMap<String, String> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            throw OXException.general("Hazelcast map for remote token logins is not available.");
        }
        // This MUST be synchronous!
        hzMap.put(key, value);
    }

    /**
     * Gets a value from a given Hazelcast map
     *
     * @param mapIdentifier The Hazelcast map identifier
     * @param key The key to get
     * @return The value for the given key or <code>null</code>
     */
    private String getFromHzMap(String mapIdentifier, String key) {
        IMap<String, String> hzMap = hzMap(mapIdentifier);
        String retval = null;
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            retval = hzMap.get(key);
        }
        return retval;
    }

    @Override
    public String acquireToken(Session session) throws OXException {
        Validate.notNull(session);

        // Only one token per session
        String sessionId = session.getSessionID();
        String token = getToken(sessionId);

        // Check token is in correct format & matches the session identifier
        if (null != token) {
            ObfuscatorService obfuscatorService = Services.getService(ObfuscatorService.class);
            if (null == obfuscatorService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ObfuscatorService.class.getName());
            }
            String[] splitToken = Strings.splitBy(token, '-', true);
            if (null == splitToken || 2 != splitToken.length || false == Objects.equal(sessionId, obfuscatorService.unobfuscate(splitToken[0]))) {
                token = null;
                remove(sessionId);
            }
        }

        if (null == token) {
            // No such token or token is of invalid format
            String newToken = createNewToken(sessionId);
            token = putIfAbsent(sessionId, newToken);
            return token == null ? newToken : token;
        }
        return token;
    }

    /**
     * Creates a new token.
     *
     * @param sessionId The session identifier
     * @return The new token
     * @throws OXException If required service is absent
     */
    private static String createNewToken(String sessionId) throws OXException {
        ObfuscatorService obfuscatorService = Services.getService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ObfuscatorService.class.getName());
        }
        return new StringBuilder(obfuscatorService.obfuscate(sessionId)).append('-').append(UUIDs.getUnformattedString(UUID.randomUUID())).toString();
    }

    @Override
    public Session redeemToken(String token, String appSecret, String optClientIdentifier, String optAuthId, String optHash, String optClientIp, String optUserAgent) throws OXException {
        TokenLoginSecret tokenLoginSecret = Strings.isEmpty(appSecret) ? null : getTokenLoginSecret(appSecret);
        if (null == tokenLoginSecret) {
            throw TokenLoginExceptionCodes.TOKEN_REDEEM_DENIED.create();
        }

        // Check available services needed for session creation
        SessiondService sessiondService = Services.getService(SessiondService.class);
        if (null == sessiondService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        ContextService contextService = Services.getService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContextService.class.getName());
        }
        ObfuscatorService obfuscatorService = Services.getService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ObfuscatorService.class.getName());
        }

        // Look-up session identifier
        String sessionId = lookUpSessionIdByToken(token, obfuscatorService);

        // Create duplicate session
        Session session = sessiondService.peekSession(sessionId);
        if (null == session) {
            throw TokenLoginExceptionCodes.NO_SUCH_SESSION_FOR_TOKEN.create(token);
        }

        // Create parameter object
        final DefaultAddSessionParameter parameter = new DefaultAddSessionParameter().setUserId(session.getUserId());
        parameter.setClientIP(Strings.isEmpty(optClientIp) ? session.getLocalIp() : optClientIp);
        parameter.setFullLogin(session.getLogin()).setPassword(session.getPassword());
        parameter.setContext(contextService.getContext(session.getContextId()));
        parameter.setUserLoginInfo(session.getLoginName());
        // Client identifier
        parameter.setClient(Strings.isEmpty(optClientIdentifier) ? session.getClient() : optClientIdentifier);
        // Authentication identifier
        parameter.setAuthId(Strings.isEmpty(optAuthId) ? session.getAuthId() : optAuthId);
        // Hash value
        parameter.setHash(Strings.isEmpty(optHash) ? session.getHash() : optHash);
        parameter.setUserAgent(Strings.isEmpty(optUserAgent) ? (String) session.getParameter(Session.PARAM_USER_AGENT) : optUserAgent);
        // short-living session without fail-over
        parameter.setTransient(true).setStaySignedIn(false);
        // Add & return session
        return sessiondService.addSession(parameter);
    }

    private String lookUpSessionIdByToken(String token, ObfuscatorService obfuscatorService) throws OXException {
        Lock lock = getLockFor(token);
        lock.lock();
        try {
            String[] splitToken = Strings.splitBy(token, '-', true);
            if (splitToken.length != 2) {
                // Token is of invalid format; expected: <obfuscated-session-id> + "-" + <UUID>
                throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
            }

            // Get session identifier
            String sessionId = obfuscatorService.unobfuscate(splitToken[0]);

            // Fetch token for session identifier & validate it
            String storedToken = remove(sessionId);
            if (storedToken == null) {
                // No such token at all
                throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
            }

            // Check equality
            if (storedToken.equals(token)) {
                // All fine...
                return sessionId;
            }

            // Client-given and stored token are different. Restore stored token & signal error.
            put(sessionId, storedToken);
            throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
        } finally {
            lock.unlock();
        }
    }

    private static Lock getLockFor(String token) throws OXException {
        LockService lockService = Services.getService(LockService.class);
        return null == lockService ? Session.EMPTY_LOCK : lockService.getLockFor(token);
    }

    /**
     * Puts the given token into the map if not already present.
     *
     * @param sessionId The session identifier
     * @param newToken The new token
     * @return The existing value or <code>null</code>
     * @throws OXException If put operation fails
     */
    private String putIfAbsent(String sessionId, String newToken) throws OXException {
        blocker.acquire();
        try {
            if (useHzMap) {
                return putToHzMapIfAbsent(sessionId2tokenMapName, sessionId, newToken);
            }
            return sessionId2token.asMap().putIfAbsent(sessionId, newToken);
        } finally {
            blocker.release();
        }
    }

    /**
     * Puts the given token into the map.
     *
     * @param sessionId The session identifier
     * @param newToken The new token
     * @throws OXException If put operation fails
     */
    private void put(String sessionId, String newToken) throws OXException {
        blocker.acquire();
        try {
            if (useHzMap) {
                putToHzMap(sessionId2tokenMapName, sessionId, newToken);
            } else {
                sessionId2token.asMap().put(sessionId, newToken);
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * Gets the token associated with the given session identifier.
     *
     * @param sessionId The session identifier
     * @return The token or null
     */
    private String getToken(String sessionId) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return getFromHzMap(sessionId2tokenMapName, sessionId);
            }
            return sessionId2token.getIfPresent(sessionId);
        } finally {
            blocker.release();
        }
    }

    /**
     * Removes the token for the given session identifier.
     *
     * @param sessionId The session identifier
     * @return The associated token or null
     */
    private String remove(String sessionId) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return removeFromHzMap(sessionId2tokenMapName, sessionId);
            }
            return sessionId2token.asMap().remove(sessionId);
        } finally {
            blocker.release();
        }
    }

    /**
     * Removes the token for specified session.
     *
     * @param session The session
     */
    public void removeTokenFor(final Session session) {
        Validate.notNull(session);
        remove(session.getSessionID());
    }

    /**
     * Changes the backing map to a local map
     */
    public void changeBackingMapToLocalMap() {
        blocker.block();
        try {
            //This happens if hazelcast is removed in the meantime. We cannot copy any information back to the local map.
            useHzMap = false;
            LOG.info("Token-login backing map changed to local");
        } finally {
            blocker.unblock();
        }
    }

    /**
     * Changes the backing map to a Hazelcast one
     */
    public void changeBackingMapToHz() {
        blocker.block();
        try {
            Validate.notNull(sessionId2tokenMapName);
            if (useHzMap) {
                return;
            }
            if (0 < sessionId2token.size()) {
                IMap<String, String> hzMap = hzMap(sessionId2tokenMapName);
                if (null == hzMap) {
                    LOG.trace("Hazelcast map for remote token logins is not available.");
                } else {
                    hzMap.putAll(sessionId2token.asMap());
                    sessionId2token.invalidateAll();
                }
            }
            useHzMap = true;
            LOG.info("Token-login backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }
}
