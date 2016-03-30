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

package com.openexchange.sessiond.impl;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.PutIfAbsent;

/**
 * {@link SessionObject} - Implements {@link com.openexchange.session.Session}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionObject implements PutIfAbsent {

    private final String sessionid;

    private String username;

    private String userlogin;

    private String loginName;

    private String password;

    private String language;

    private String localip;

    private String host;

    private long lifetime;

    private Date timestamp;

    private Date creationtime;

    private String secret;

    private String randomToken;

    private int contextId;

    private String login;

    private String authId;

    private String hash;

    private String client;

    private final ConcurrentMap<String, Object> parameters;

    /**
     * Initializes a new {@link SessionObject}.
     *
     * @param sessionId The session identifier
     */
    public SessionObject(final String sessionId) {
        this.sessionid = sessionId;
        parameters = new ConcurrentHashMap<String, Object>();
        parameters.put(PARAM_LOCK, new ReentrantLock());
        parameters.put(PARAM_COUNTER, new AtomicInteger());
        parameters.put(PARAM_ALTERNATIVE_ID, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setUserlogin(final String userlogin) {
        this.userlogin = userlogin;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    @Override
    public void setLocalIp(final String localip) {
        this.localip = localip;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setLifetime(final long lifetime) {
        this.lifetime = lifetime;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = (Date) timestamp.clone();
    }

    public void setCreationtime(final Date creationtime) {
        this.creationtime = (Date) creationtime.clone();
    }

    public void setContextId(final int contextId) {
        this.contextId = contextId;
    }

    @Override
    public String getSessionID() {
        return sessionid;
    }

    @Override
    public int getUserId() {
    	if (username == null) {
    		return 0;
    	}
        return Integer.parseInt(username);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getUserlogin() {
        return userlogin;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String getLocalIp() {
        return localip;
    }

    public String getHost() {
        return host;
    }

    public long getLifetime() {
        return lifetime;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getCreationtime() {
        return creationtime;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    public void setLoginName(final String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setRandomToken(final String randomToken) {
        this.randomToken = randomToken;
    }

    @Override
    public String getRandomToken() {
        return randomToken;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
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

    /**
     * Removes the random token
     */
    public void removeRandomToken() {
        randomToken = null;
    }

    @Override
    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    public void setAuthId(final String authId) {
        this.authId = authId;
    }

    @Override
    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void setClient(final String client) {
        this.client = client;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }
}
