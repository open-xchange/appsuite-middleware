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

package com.openexchange.tokenlogin.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
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

    private volatile String token2sessionIdMapName;

    private final Cache<String, String> token2sessionId;

    private final Cache<String, String> sessionId2token;

    private final Map<String, TokenLoginSecret> secrets;

    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;

    private volatile boolean useHzMap = false;

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
     */
    public TokenLoginServiceImpl(final int maxIdleTime, final ConfigurationService configService) throws OXException {
        this(maxIdleTime, configService, null);
    }

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
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
            Cache<String, String> token2sessionId = cacheBuilder.build();
            this.token2sessionId = token2sessionId;
        }
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

    protected Map<String, TokenLoginSecret> initSecrets(final File secretsFile) throws OXException {
        if (null == secretsFile) {
            return Collections.emptyMap();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(secretsFile));
            final Map<String, TokenLoginSecret> map = new LinkedHashMap<String, TokenLoginSecret>(16);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!Strings.isEmpty(line)) {
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
        } catch (final IOException e) {
            throw TokenLoginExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    private static void parseParameter(final String param, final Map<String, Object> params) {
        if (!Strings.isEmpty(param)) {
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
                    } catch (final NumberFormatException nfe1) {
                        // Try parse to long
                        try {
                            final Long l = Long.valueOf(value);
                            params.put(param.substring(0, pos).trim(), l);
                        } catch (final NumberFormatException nfe2) {
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
     * Sets the name of the Hazelcast map for sessionId2token.
     *
     * @param hzMapName The map name
     */
    public void setSessionId2tokenHzMapName(final String sessionId2tokenMapName) {
        this.sessionId2tokenMapName = sessionId2tokenMapName;
    }

    /**
     * Sets the name of the Hazelcast map for token2sessionId.
     *
     * @param hzMapName The map name
     */
    public void setToken2sessionIdMapNameHzMapName(final String token2sessionIdMapName) {
        this.token2sessionIdMapName = token2sessionIdMapName;
    }

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

    private String removeFromHzMap(String mapIdentifier, String key) {
        final IMap<String, String> hzMap = hzMap(mapIdentifier);
        String retval = null;
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            // This MUST be synchronous!
            retval = hzMap.remove(key);
        }
        return retval;
    }

    private String putToHzMapIfAbsent(String mapIdentifier, String key, String value) {
        final IMap<String, String> hzMap = hzMap(mapIdentifier);
        String retval = null;
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            // This MUST be synchronous!
            retval = hzMap.putIfAbsent(key, value);
        }
        return retval;
    }

    private void putToHzMap(String mapIdentifier, String key, String value) {
        final IMap<String, String> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            // This MUST be synchronous! Otherwise it may be possible to use a token twice, once from local map and once from remote map
            // because remote remove happens before asynchronous put.
            hzMap.put(key, value);
        }
    }

    private String getFromHzMap(String mapIdentifier, String key) {
        final IMap<String, String> hzMap = hzMap(mapIdentifier);
        String retval = null;
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote token logins is not available.");
        } else {
            retval = hzMap.get(key);
        }
        return retval;
    }

    @Override
    public String acquireToken(Session session) {
        Validate.notNull(session);

        // Only one token per session
        final String sessionId = session.getSessionID();
        String token = getToken(sessionId);
        if (null == token) {
            final String newToken = UUIDs.getUnformattedString(UUID.randomUUID());
            token = putSessionIfAbsent(sessionId, newToken);
            if (null == token) {
                token = newToken;
                putToken(token, sessionId);
            }
        }
        return token;
    }

    @Override
    public Session redeemToken(String token, String appSecret, String optClientIdentifier, String optAuthId, String optHash, String optClientIp) throws OXException {
        final TokenLoginSecret tokenLoginSecret = Strings.isEmpty(appSecret) ? null : getTokenLoginSecret(appSecret);
        if (null == tokenLoginSecret) {
            throw TokenLoginExceptionCodes.TOKEN_REDEEM_DENIED.create();
        }
        // Check available services needed for session creation
        final SessiondService sessiondService = Services.getService(SessiondService.class);
        if (null == sessiondService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        final ContextService contextService = Services.getService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContextService.class.getName());
        }
        // Look-up session identifier
        final Lock lock;
        {
            final LockService lockService = Services.getService(LockService.class);
            lock = null == lockService ? Session.EMPTY_LOCK : lockService.getLockFor(token);
        }
        // Get session identifier
        String sessionId;
        lock.lock();
        try {
            sessionId = removeToken(token);
            if (null == sessionId) {
                throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
            }
            // Remove from other mapping, too
            removeSession(sessionId);
        } finally {
            lock.unlock();
        }
        // Create duplicate session
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            throw TokenLoginExceptionCodes.NO_SUCH_SESSION_FOR_TOKEN.create(token);
        }
        // Create parameter object
        final DefaultAddSessionParameter parameter = new DefaultAddSessionParameter().setUserId(session.getUserId());
        parameter.setClientIP(Strings.isEmpty(optClientIp) ? session.getLocalIp() : optClientIp);
        parameter.setFullLogin(session.getLogin()).setPassword(session.getPassword());
        parameter.setContext(contextService.getContext(session.getContextId()));
        parameter.setUserLoginInfo(session.getLoginName()).setTransient(session.isTransient());
        // Client identifier
        parameter.setClient(Strings.isEmpty(optClientIdentifier) ? session.getClient() : optClientIdentifier);
        // Authentication identifier
        parameter.setAuthId(Strings.isEmpty(optAuthId) ? session.getAuthId() : optAuthId);
        // Hash value
        parameter.setHash(Strings.isEmpty(optHash) ? session.getHash() : optHash);
        // Add & return session
        return sessiondService.addSession(parameter);
    }

    /**
     * @param sessionId
     * @param token
     */
    private void putToken(final String token, String sessionId) {
        blocker.acquire();
        try {
            if (useHzMap) {
                putToHzMap(token2sessionIdMapName, token, sessionId);
            } else {
                token2sessionId.put(token, sessionId);
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * @param sessionId
     * @param newToken
     * @return
     */
    private String putSessionIfAbsent(final String sessionId, final String newToken) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return putToHzMapIfAbsent(sessionId2tokenMapName, sessionId, newToken);
            } else {
                return sessionId2token.asMap().putIfAbsent(sessionId, newToken);
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * @param sessionId
     * @return
     */
    private String getToken(final String sessionId) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return getFromHzMap(sessionId2tokenMapName, sessionId);
            } else {
                return sessionId2token.getIfPresent(sessionId);
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * @param sessionId
     * @return
     */
    private String removeSession(String sessionId) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return removeFromHzMap(sessionId2tokenMapName, sessionId);
            } else {
                return sessionId2token.asMap().remove(sessionId);
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * @param token
     * @return
     */
    private String removeToken(final String token) {
        blocker.acquire();
        try {
            if (useHzMap) {
                return removeFromHzMap(token2sessionIdMapName, token);
            } else {
                return token2sessionId.asMap().remove(token);
            }
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

        final String token = removeSession(session.getSessionID());
        if (null != token) {
            removeToken(token);
        }
    }

    /**
     *
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
     *
     */
    public void changeBackingMapToHz() {
        blocker.block();
        try {
            Validate.notNull(sessionId2tokenMapName);
            Validate.notNull(token2sessionIdMapName);
            if (useHzMap) {
                return;
            } else {
                if (0 < sessionId2token.size()) {
                    IMap<String, String> hzMap = hzMap(sessionId2tokenMapName);
                    if (null == hzMap) {
                        LOG.trace("Hazelcast map for remote token logins is not available.");
                    } else {
                        hzMap.putAll(sessionId2token.asMap());
                        sessionId2token.invalidateAll();
                    }
                }
                if (0 < token2sessionId.size()) {
                    IMap<String, String> hzMap = hzMap(token2sessionIdMapName);
                    if (null == hzMap) {
                        LOG.trace("Hazelcast map for remote token logins is not available.");
                    } else {
                        hzMap.putAll(token2sessionId.asMap());
                        token2sessionId.invalidateAll();
                    }
                }
                useHzMap = true;
            }
            LOG.info("Token-login backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }
}
