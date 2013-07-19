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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.Validate;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
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

    private volatile String hzMapName;
    private final ConcurrentMap<String, String> token2sessionId;
    private final ConcurrentMap<String, String> sessionId2token;
    private final Map<String, TokenLoginSecret> secrets;

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
     */
    public TokenLoginServiceImpl(final int maxIdleTime, final ConfigurationService configService) throws OXException {
        super();
        Validate.notNull(configService);

        final IdleExpirationPolicy evictionPolicy = new IdleExpirationPolicy(maxIdleTime);
        token2sessionId = new ConcurrentLinkedHashMap<String, String>(1024, 0.75f, 16, Integer.MAX_VALUE, evictionPolicy);
        sessionId2token = new ConcurrentLinkedHashMap<String, String>(1024, 0.75f, 16, Integer.MAX_VALUE, evictionPolicy);
        // Parse app secrets
        secrets = initSecrets(configService.getFileByName("tokenlogin-secrets"));
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

    private void parseParameter(final String param, final Map<String, Object> params) {
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
     * Sets the name of the Hazelcast map.
     *
     * @param hzMapName The map name
     */
    public void setHzMapName(final String hzMapName) {
        this.hzMapName = hzMapName;
    }

    /**
     * Gets the Hazelcast 'token2sessionId' map or <code>null</code> if unavailable.
     */
    private IMap<String, String> hzMap() {
        final String hzMapName = this.hzMapName;
        if (null == hzMapName) {
            return null;
        }
        final HazelcastInstance hazelcastInstance = Services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null || !hazelcastInstance.getLifecycleService().isRunning()) {
            return null;
        }
        return hazelcastInstance.getMap(hzMapName);
    }

    private void removeFromHzMap(final String token) {
        final IMap<String, String> hzMap = hzMap();
        if (null != hzMap) {
            hzMap.removeAsync(token);
        }
    }

    private void putToHzMap(final String token, final String sessionId) {
        final IMap<String, String> hzMap = hzMap();
        if (null != hzMap) {
            hzMap.putAsync(token, sessionId);
        }
    }

    @Override
    public String acquireToken(final Session session) throws OXException {
        Validate.notNull(session);

        // Only one token per session
        final String sessionId = session.getSessionID();
        String token = sessionId2token.get(sessionId);
        if (null == token) {
            final String newToken = UUIDs.getUnformattedString(UUID.randomUUID());
            token = sessionId2token.putIfAbsent(sessionId, newToken);
            if (null == token) {
                token = newToken;
                token2sessionId.put(token, sessionId);
                putToHzMap(token, sessionId);
            }
        }
        return token;
    }

    @Override
    public Session redeemToken(final String token, final String appSecret, final String optClientId, final String optAuthId, final String optHash) throws OXException {
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
        String sessionId = token2sessionId.remove(token);
        if (null == sessionId) {
            // Local MISS, look up in Hazelcast map
            final IMap<String, String> hzMap = hzMap();
            if (null != hzMap) {
                sessionId = hzMap.remove(token);
            }
            if (null == sessionId) {
                throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
            }
        } else {
            // Local HIT, remove from Hazelcast map
            removeFromHzMap(token);
        }
        // Remove from other mapping, too
        sessionId2token.remove(sessionId);
        // Create duplicate session
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            throw TokenLoginExceptionCodes.NO_SUCH_SESSION_FOR_TOKEN.create(token);
        }
        // Create parameter object
        final DefaultAddSessionParameter parameter = new DefaultAddSessionParameter().setUserId(session.getUserId());
        parameter.setClientIP(session.getLocalIp()).setFullLogin(session.getLogin()).setPassword(session.getPassword());
        parameter.setContext(contextService.getContext(session.getContextId()));
        parameter.setUserLoginInfo(session.getLoginName()).setTransient(session.isTransient());
        // Client identifier
        parameter.setClient(Strings.isEmpty(optClientId) ? session.getClient() : optClientId);
        // Authentication identifier
        parameter.setAuthId(Strings.isEmpty(optAuthId) ? session.getAuthId() : optAuthId);
        // Hash value
        parameter.setHash(Strings.isEmpty(optHash) ? session.getHash() : optHash);
        // Add & return session
        return sessiondService.addSession(parameter);
    }

    /**
     * Removes the token for specified session.
     *
     * @param session The session
     */
    public void removeTokenFor(final Session session) {
        Validate.notNull(session);

        final String token = sessionId2token.remove(session.getSessionID());
        if (null != token) {
            token2sessionId.remove(token);
            removeFromHzMap(token);
        }
    }
}
