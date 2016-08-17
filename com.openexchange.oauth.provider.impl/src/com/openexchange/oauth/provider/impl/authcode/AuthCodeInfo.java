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
