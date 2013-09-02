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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.sessiond.services.Services;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessionImpl} - Implements interface {@link Session} (and {@link PutIfAbsent}).
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionImpl implements PutIfAbsent {

    private final String loginName;
    private volatile String password;
    private final int contextId;
    private final int userId;
    private final String sessionId;
    private final String secret;
    private final String login;
    private volatile String randomToken;
    private volatile String localIp;
    private final String authId;
    private volatile String hash;
    private volatile String client;
    private volatile boolean tranzient;
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
     * @param login The full user's login; e.g. <i>test@foo.bar</i>
     * @param authId The authentication identifier that is used to trace the login request across different systems
     * @param hash The hash identifier
     * @param client The client type
     * @param tranzient <code>true</code> if the session should be transient, <code>false</code>, otherwise
     */
    public SessionImpl(final int userId, final String loginName, final String password, final int contextId, final String sessionId,
        final String secret, final String randomToken, final String localIp, final String login, final String authId, final String hash,
        final String client, final boolean tranzient) {
        super();
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
        this.tranzient = tranzient;
        parameters = new ConcurrentHashMap<String, Object>();
        parameters.put(PARAM_LOCK, new ReentrantLock());
        parameters.put(PARAM_COUNTER, new AtomicInteger());
        parameters.put(PARAM_ALTERNATIVE_ID, UUIDSessionIdGenerator.randomUUID());
    }

    /**
     * Initializes a new {@link SessionImpl}
     *
     * @param session The copy session
     */
    public SessionImpl(final Session s) {
        super();
        this.userId = s.getUserId();
        this.loginName = s.getLoginName();
        this.password = s.getPassword();
        this.sessionId = s.getSessionID();
        this.secret = s.getSecret();
        this.randomToken = s.getRandomToken();
        this.localIp = s.getLocalIp();
        this.contextId = s.getContextId();
        this.login = s.getLogin();
        this.authId = s.getAuthId();
        this.hash = s.getHash();
        this.client = s.getClient();
        this.tranzient = false;
        parameters = new ConcurrentHashMap<String, Object>();
        parameters.put(PARAM_LOCK, new ReentrantLock());
        parameters.put(PARAM_COUNTER, new AtomicInteger());
        final Object altId = s.getParameter(PARAM_ALTERNATIVE_ID);
        if (null == altId) {
            parameters.put(PARAM_ALTERNATIVE_ID, UUIDSessionIdGenerator.randomUUID());
        } else {
            parameters.put(PARAM_ALTERNATIVE_ID, altId);
        }
    }

    /**
     * Logs differences between this and specified session.
     *
     * @param s The session to compare with
     * @param logger The logger
     */
    public void logDiff(final SessionImpl s, final Log logger) {
        if (null == s || null == logger) {
            return;
        }
        final StringBuilder sb = new StringBuilder(1024).append("Session Diff:\n");
        final String format = "%-15s%-45s%s";
        sb.append(String.format(format, "Name", "Session #1", "Session #1"));
        sb.append(String.format(format, "User-Id", Integer.valueOf(userId), Integer.valueOf(s.userId)));
        sb.append(String.format(format, "Context-Id", Integer.valueOf(contextId), Integer.valueOf(s.contextId)));
        sb.append(String.format(format, "Session-Id", sessionId, s.sessionId));
        sb.append(String.format(format, "Secret", secret, s.secret));
        sb.append(String.format(format, "Alternative-Id", parameters.get(PARAM_ALTERNATIVE_ID), s.parameters.get(PARAM_ALTERNATIVE_ID)));
        sb.append(String.format(format, "Auth-Id", authId, s.authId));
        sb.append(String.format(format, "Login", login, s.login));
        sb.append(String.format(format, "Login-Name", loginName, s.loginName));
        sb.append(String.format(format, "Local IP", localIp, s.localIp));
        sb.append(String.format(format, "Random-Token", randomToken, s.randomToken));
        sb.append(String.format(format, "Hash", hash, s.hash));
        logger.info(sb);
    }

    /**
     * Whether specified session is considered equal to this one.
     *
     * @param s The other session
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public boolean consideredEqual(final SessionImpl s) {
        if (this == s) {
            return true;
        }
        if (null == s) {
            return false;
        }
        if (userId != s.userId) {
            return false;
        }
        if (contextId != s.contextId) {
            return false;
        }
        if (null == loginName) {
            if (null != s.loginName) {
                return false;
            }
        } else if (loginName.equals(s.loginName)) {
            return false;
        }
        if (null == password) {
            if (null != s.password) {
                return false;
            }
        } else if (password.equals(s.password)) {
            return false;
        }
        if (null == sessionId) {
            if (null != s.sessionId) {
                return false;
            }
        } else if (sessionId.equals(s.sessionId)) {
            return false;
        }
        if (null == secret) {
            if (null != s.secret) {
                return false;
            }
        } else if (secret.equals(s.secret)) {
            return false;
        }
        if (null == randomToken) {
            if (null != s.randomToken) {
                return false;
            }
        } else if (randomToken.equals(s.randomToken)) {
            return false;
        }
        if (null == login) {
            if (null != s.login) {
                return false;
            }
        } else if (login.equals(s.login)) {
            return false;
        }
        if (null == localIp) {
            if (null != s.localIp) {
                return false;
            }
        } else if (localIp.equals(s.localIp)) {
            return false;
        }
        if (null == authId) {
            if (null != s.authId) {
                return false;
            }
        } else if (authId.equals(s.authId)) {
            return false;
        }
        if (null == hash) {
            if (null != s.hash) {
                return false;
            }
        } else if (hash.equals(s.hash)) {
            return false;
        }
        final Object object1 = parameters.get(PARAM_ALTERNATIVE_ID);
        final Object object2 = s.parameters.get(PARAM_ALTERNATIVE_ID);
        if (null == object1) {
            if (null != object2) {
                return false;
            }
        } else if (object1.equals(object2)) {
            return false;
        }
        return true;
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
    public Object setParameterIfAbsent(final String name, final Object value) {
        if (PARAM_LOCK.equals(name)) {
            return parameters.get(PARAM_LOCK);
        }
        return parameters.putIfAbsent(name, value);
    }

    /**
     * Removes the random token
     */
    public void removeRandomToken() {
        randomToken = null;
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }

    @Override
    public void setLocalIp(final String localIp) {
        try {
            setLocalIp(localIp, true);
        } catch (final OXException e) {
            LOG.error("Failed to propagate change of IP address.", e);
        }
    }

    /**
     * Sets the local IP address
     *
     * @param localIp The local IP address
     * @param propagate Whether to propagate that IP change through {@code SessiondService}
     * @throws OXException If propagating change fails
     */
    public void setLocalIp(final String localIp, final boolean propagate) throws OXException {
        this.localIp = localIp;
        if (propagate) {
            final SessionStorageService sessionStorageService = Services.getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                sessionStorageService.setLocalIp(sessionId, localIp);
            }
        }
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
        try {
            setHash(hash, true);
        } catch (final OXException e) {
            LOG.error("Failed to propagate change of hash identifier.", e);
        }
    }

    /**
     * Sets the hash identifier
     *
     * @param hash The hash identifier
     * @param propagate Whether to propagate that change through {@code SessiondService}
     * @throws OXException If propagating change fails
     */
    public void setHash(final String hash, final boolean propagate) throws OXException {
        this.hash = hash;
        if (propagate) {
            final SessionStorageService sessionStorageService = Services.getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                sessionStorageService.setHash(sessionId, hash);
            }
        }
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void setClient(final String client) {
        try {
            setClient(client, true);
        } catch (final OXException e) {
            LOG.error("Failed to propagate change of client identifier.", e);
        }
    }

    @Override
    public boolean isTransient() {
        return tranzient;
    }

    /**
     * Sets if the session is transient or not.
     *
     * @param tranzient <code>true</code> if the session is transient, <code>false</code>, otherwise
     */
    public void setTransient(boolean tranzient) {
        this.tranzient = tranzient;
    }

    /**
     * Sets the client identifier
     *
     * @param client The client identifier
     * @param propagate Whether to propagate that change through {@code SessiondService}
     * @throws OXException If propagating change fails
     */
    public void setClient(final String client, final boolean propagate) throws OXException {
        this.client = client;
        if (propagate) {
            final SessionStorageService sessionStorageService = Services.getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                sessionStorageService.setClient(sessionId, client);
            }
        }
    }

    @Override
    public String toString() {
        final StringAllocator builder = new StringAllocator(128);
        builder.append('{');
        builder.append("contextId=").append(contextId).append(", userId=").append(userId).append(", ");
        if (sessionId != null) {
            builder.append("sessionId=").append(sessionId).append(", ");
        }
        if (login != null) {
            builder.append("login=").append(login).append(", ");
        }
        if (localIp != null) {
            builder.append("localIp=").append(localIp).append(", ");
        }
        if (authId != null) {
            builder.append("authId=").append(authId).append(", ");
        }
        if (hash != null) {
            builder.append("hash=").append(hash).append(", ");
        }
        if (client != null) {
            builder.append("client=").append(client).append(", ");
        }
        builder.append("transient=").append(tranzient);
        builder.append('}');
        return builder.toString();
    }

}
