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

package com.openexchange.sessiond.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.sessiond.services.SessiondServiceRegistry;

/**
 * {@link SessionImpl} - Implements interface {@link Session}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionImpl implements PutIfAbsent {

    // A random-enough key for encrypting and decrypting passwords on their way through the caching system.
    private static final String OBFUSCATION_KEY_PROPERTY = "com.openexchange.sessiond.encryptionKey";

    private final String loginName;

    private String password;

    private final int contextId;

    private final int userId;

    private final String sessionId;

    private final String secret;

    private final String login;

    private String randomToken;

    private String localIp;

    private final String authId;

    private String hash;

    private String client;

    private final ConcurrentMap<String, Object> parameters;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionImpl.class));

    /**
     * Initializes a new {@link SessionImpl}
     *
     * @param userId The user ID
     * @param loginName The login name
     * @param password The password
     * @param contextId The context ID
     * @param sessionId The session ID
     * @param secret The secret (cookie identifier)
     * @param randomToken The random token
     * @param localIp The local IP
     */
    public SessionImpl(final int userId, final String loginName, final String password, final int contextId, final String sessionId, final String secret, final String randomToken, final String localIp, final String login, final String authId, final String hash, final String client) {
        this.userId = userId;
        this.loginName = loginName;
        this.password = password;
        this.sessionId = sessionId;
        this.secret = secret;
        this.randomToken = randomToken;
        this.localIp = localIp;
        this.contextId = contextId;
        this.login = login;
        this.authId = authId;
        this.hash = hash;
        this.client = client;
        parameters = new ConcurrentHashMap<String, Object>();
        parameters.put(PARAM_LOCK, new ReentrantLock());
        parameters.put(PARAM_COUNTER, new AtomicInteger());
        parameters.put(PARAM_ALTERNATIVE_ID, UUIDSessionIdGenerator.randomUUID());
    }

    /**
     * Initializes a new {@link SessionImpl} from specified cached session.
     *
     * @param cachedSession The cached session
     */
    protected SessionImpl(final CachedSession cachedSession) {
        super();
        userId = cachedSession.getUserId();
        contextId = cachedSession.getContextId();
        loginName = cachedSession.getLoginName();
        password = unobfuscate( cachedSession.getPassword() );
        sessionId = cachedSession.getSessionId();
        secret = cachedSession.getSecret();
        randomToken = cachedSession.getRandomToken();
        login = cachedSession.getLogin();
        localIp = cachedSession.getLocalIp();
        authId = cachedSession.getAuthId();
        hash = cachedSession.getHash();
        final Map<String, Serializable> params = cachedSession.getParameters();
        parameters = new ConcurrentHashMap<String, Object>(params.size());
        for (final Entry<String, Serializable> entry : params.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
        parameters.put(PARAM_LOCK, new ReentrantLock());
        parameters.put(PARAM_COUNTER, new AtomicInteger());
        if (!parameters.containsKey(PARAM_ALTERNATIVE_ID)) {
            parameters.put(PARAM_ALTERNATIVE_ID, UUIDSessionIdGenerator.randomUUID());
        }
    }

    /**
     * Creates a new instance of {@link CachedSession} holding this session's state and information ready for being put into session cache.
     *
     * @return An appropriate instance of {@link CachedSession}
     */
    public CachedSession createCachedSession() {
        return new CachedSession(userId, loginName, obfuscate( password ), contextId, sessionId, secret, randomToken, localIp, login, authId, hash, client, parameters);
    }

    private String obfuscate(final String string) {
        try {
            final String key = getObfuscationKey();
            return isEmpty(key) ? string : SessiondServiceRegistry.getServiceRegistry().getService(CryptoService.class).encrypt(string, key);
        } catch (final OXException e) {
            LOG.error("Could not obfuscate a string before migration", e);
            return string;
        }
    }

    private static boolean isEmpty(final String str) {
        if (null == str) {
            return true;
        }
        final int length = str.length();
        boolean empty = true;
        for (int i = 0; empty && i < length; i++) {
            empty = Character.isWhitespace(str.charAt(i));
        }
        return empty;
    }

    private String getObfuscationKey() {
        return SessiondServiceRegistry.getServiceRegistry().getService(ConfigurationService.class).getProperty(OBFUSCATION_KEY_PROPERTY);
    }

    private String unobfuscate(final String string) {
        try {
            final String key = getObfuscationKey();
            return SessiondServiceRegistry.getServiceRegistry().getService(CryptoService.class).decrypt(string, key);
        } catch (final OXException e) {
            LOG.error("Could not decode string after migration", e);
            return string;
        }
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public String getRandomToken() {
        return randomToken;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public String getSessionID() {
        return sessionId;
    }

    public int getUserID() {
        return userId;
    }

    @Override
    public void setParameter(final String name, final Object value) {
        if (PARAM_LOCK.equals(name)) {
            return;
        }
        if (null == value) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

    @Override
    public Object setParameterIfAbsent(String name, Object value) {
        if (PARAM_LOCK.equals(name)) {
            return parameters.get(PARAM_LOCK);
        }
        return parameters.putIfAbsent(name, value);
    }

    @Override
    public void removeRandomToken() {
        randomToken = null;
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }


    @Override
    public void setLocalIp(final String localIp) {
        this.localIp = localIp;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getUserlogin() {
        return loginName;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void setClient(final String client) {
        this.client = client;
    }
}
