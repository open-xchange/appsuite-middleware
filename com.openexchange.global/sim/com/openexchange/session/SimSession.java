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
        super();
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

    @Override
    public void setLocalIp(final String ip) {
        // Nothing to do

    }

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

    @Override
    public boolean isStaySignedIn() {
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

    @Override
    public Origin getOrigin() {
        return null;
    }

}
