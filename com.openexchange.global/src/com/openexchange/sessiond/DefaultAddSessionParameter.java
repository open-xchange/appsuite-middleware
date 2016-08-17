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

package com.openexchange.sessiond;

import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link DefaultAddSessionParameter} - The default implementation of {@code AddSessionParameter}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultAddSessionParameter implements AddSessionParameter {

    private String fullLogin;
    private String password;
    private int userId;
    private String userLoginInfo;
    private Context context;
    private String clientIP;
    private String authId;
    private String hash;
    private String client;
    private String clientToken;
    private boolean tranzient;
    private SessionEnhancement enhancement;

    /**
     * Initializes a new {@link DefaultAddSessionParameter}.
     */
    public DefaultAddSessionParameter() {
        super();
    }

    @Override
    public String getFullLogin() {
        return fullLogin;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getUserLoginInfo() {
        return userLoginInfo;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public String getClientIP() {
        return clientIP;
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
    public String getClient() {
        return client;
    }

    @Override
    public String getClientToken() {
        return clientToken;
    }

    @Override
    public boolean isTransient() {
        return tranzient;
    }

    /**
     * Sets the full login
     *
     * @param fullLogin The full login to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setFullLogin(String fullLogin) {
        this.fullLogin = fullLogin;
        return this;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the user identifier
     *
     * @param userId The user identifier to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Sets the user login information
     *
     * @param userLoginInfo The user login information to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setUserLoginInfo(String userLoginInfo) {
        this.userLoginInfo = userLoginInfo;
        return this;
    }

    /**
     * Sets the context
     *
     * @param context The context to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Sets the client IP
     *
     * @param clientIP The client IP to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setClientIP(String clientIP) {
        this.clientIP = clientIP;
        return this;
    }

    /**
     * Sets the authentication identifier.
     *
     * @param authId The authentication identifier to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setAuthId(String authId) {
        this.authId = authId;
        return this;
    }

    /**
     * Sets the hash
     *
     * @param hash The hash to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setHash(String hash) {
        this.hash = hash;
        return this;
    }

    /**
     * Sets the client identifier.
     *
     * @param client The client identifier to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setClient(String client) {
        this.client = client;
        return this;
    }

    /**
     * Sets the client token
     *
     * @param clientToken The client token to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setClientToken(String clientToken) {
        this.clientToken = clientToken;
        return this;
    }

    /**
     * Sets the transient flag
     *
     * @param tranzient The transient flag to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setTransient(boolean tranzient) {
        this.tranzient = tranzient;
        return this;
    }

    @Override
    public SessionEnhancement getEnhancement() {
        return enhancement;
    }

    public void setEnhancement(SessionEnhancement enhancement) {
        this.enhancement = enhancement;
    }
}
