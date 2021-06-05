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
 * {@link SessionDescription} - Holds all parameters/attributes that describe a session, which is about being created.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SessionDescription implements PutIfAbsent {

    private String loginName;
    private String password;
    private int contextId;
    private int userId;
    private String sessionId;
    private String secret;
    private String login;
    private String randomToken;
    private String localIp;
    private String authId;
    private String hash;
    private String client;
    private boolean tranzient;
    private boolean staySignedIn;
    private Origin origin;
    private final Map<String, Object> parameters;
    private String alternativeId;

    /**
     * Initializes a new {@link SessionDescription}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param login The full user's login; e.g. <i>test@foo.bar</i>
     * @param password The password
     * @param sessionId The session identifier
     * @param secret The session's secret string
     * @param alternativeId The alternative session identifier
     * @param origin The session's origin
     */
    public SessionDescription(int userId, int contextId, String login, String password, String sessionId, String secret, String alternativeId, Origin origin) {
        super();
        this.password = password;
        this.contextId = contextId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.secret = secret;
        this.login = login;
        this.alternativeId = alternativeId;
        this.origin = origin;
        parameters = new HashMap<String, Object>(8);
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Gets the alternative session identifier.
     *
     * @return The alternative identifier
     */
    public String getAlternativeId() {
        return alternativeId;
    }

    /**
     * Sets the alternative session identifier.
     *
     * @param alternativeId The alternative identifier
     */
    public void setAlternativeId(String alternativeId) {
        this.alternativeId = alternativeId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public boolean containsParameter(String name) {
        return parameters.containsKey(name);
    }

    @Override
    public Object getParameter(String name) {
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
    public void setParameter(String name, Object value) {
        if (null == value) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

    @Override
    public Object setParameterIfAbsent(String name, Object value) {
        return parameters.putIfAbsent(name, value);
    }

    @Override
    public Origin getOrigin() {
        return origin;
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

    /**
     * Sets the local IP address
     *
     * @param localIp The local IP address
     */
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
    public void setPassword(String password) {
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

    /**
     * Sets the hash identifier
     *
     * @param hash The hash identifier
     */
    @Override
    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void setClient(String client) {
        this.client = client;
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
     * Sets whether session should be annotated with "stay signed in".
     *
     * @param staySignedIn <code>true</code> to annotate with "stay signed in"; otherwise <code>false</code>
     */
    public void setStaySignedIn(boolean staySignedIn) {
        this.staySignedIn = staySignedIn;
    }

    /**
     * Checks whether session should be annotated with "stay signed in".
     *
     * @return <code>true</code> to annotate with "stay signed in"; otherwise <code>false</code>
     */
    @Override
    public boolean isStaySignedIn() {
        return staySignedIn;
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    /**
     * Sets the login name.
     *
     * @param loginName The login name to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * Sets the random token.
     *
     * @param randomToken The random token to set
     */
    public void setRandomToken(String randomToken) {
        this.randomToken = randomToken;
    }

    /**
     * Sets the auth identifier
     *
     * @param authId The auth identifier to set
     */
    public void setAuthId(String authId) {
        this.authId = authId;
    }

    /**
     * Sets the session identifier
     *
     * @param sessionId The session identifier
     */
    public void setSessionID(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Sets the secret string
     *
     * @param secret The secret string
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Sets the full login string.
     *
     * @param login The full login string
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Sets the user identifier
     *
     * @param userId The user identifier
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the origin
     *
     * @param origin The origin to set
     */
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

}
