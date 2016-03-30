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

package com.openexchange.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link SimSession}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SimSession implements Session {

    private String loginName;
    private String randomToken;
    private String sessionId;
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    private int contextId;
    private int userId;
    private String password;
    private String secret;
    private String hash;
    private String authId;
    private String client;

    public SimSession() {

    }

    public SimSession(int user, int context) {
    	this.userId = user;
    	this.contextId = context;
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String getLocalIp() {
        return null;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(final String loginName) {
        this.loginName = loginName;
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getRandomToken() {
        return randomToken;
    }

    public void setRandomToken(final String randomToken) {
        this.randomToken = randomToken;
    }

    public void removeRandomToken() {
        randomToken = null;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public String getSessionID() {
        return sessionId;
    }

    public void setSessionID(final String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getUserlogin() {
        return null;
    }

    @Override
    public void setParameter(final String name, final Object value) {
        parameters.put(name, value);
    }

    @Override
    public String getHash() {
        return hash;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.session.Session#setLocalIp(java.lang.String)
     */
    @Override
    public void setLocalIp(final String ip) {
        // Nothing to do

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.session.Session#setHash(java.lang.String)
     */
    @Override
    public void setHash(final String hash) {
        this.hash = hash;
    }

    /**
     * Sets the contextId
     *
     * @param contextId The contextId to set
     */
    public void setContextId(final int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the userId
     *
     * @param userId The userId to set
     */
    public void setUserId(final int userId) {
        this.userId = userId;
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

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

}
