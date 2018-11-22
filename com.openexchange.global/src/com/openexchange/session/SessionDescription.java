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
     */
    public SessionDescription(int userId, int contextId, String login, String password, String sessionId, String secret, String alternativeId) {
        super();
        this.password = password;
        this.contextId = contextId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.secret = secret;
        this.login = login;
        this.alternativeId = alternativeId;
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

}
