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

package com.openexchange.oauth.provider.impl.authcode;

import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * {@link AuthCodeInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class AuthCodeInfo {

    private String authCode;
    private String clientId;
    private String redirectURI;
    private Scope scope;
    private int userId;
    private int contextId;
    private long timestamp;

    /**
     * Initializes a new {@link AuthCodeInfo}.
     */
    public AuthCodeInfo() {
        super();
    }

    /**
     * Initializes a new {@link AuthCodeInfo}.
     *
     * @param authCode The authorization code
     * @param clientId The client identifier
     * @param redirectURI The redirect URI
     * @param scope The scope
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param nanos The nanos
     */
    public AuthCodeInfo(String authCode, String clientId, String redirectURI, Scope scope, int userId, int contextId, long timestamp) {
        super();
        this.authCode = authCode;
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.scope = scope;
        this.userId = userId;
        this.contextId = contextId;
        this.timestamp = timestamp;
    }

    /**
     * Gets the authCode
     *
     * @return The authCode
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Sets the authCode
     *
     * @param authCode The authCode to set
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client identifier
     *
     * @param clientId The client identifier to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the redirectURI
     *
     * @return The redirectURI
     */
    public String getRedirectURI() {
        return redirectURI;
    }

    /**
     * Sets the redirectURI
     *
     * @param redirectURI The redirectURI to set
     */
    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    /**
     * Gets the scope
     *
     * @return The scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Sets the scope
     *
     * @param scope The scope to set
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier
     *
     * @param userId The user identifier to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier to set
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets the timestamp
     *
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     *
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
