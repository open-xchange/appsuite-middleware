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

package com.openexchange.sessiond;

import java.util.ArrayList;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Origin;

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
    private boolean staySignedIn;
    private ArrayList<SessionEnhancement> enhancements;
    private String userAgent;
    private Origin origin;

    /**
     * Initializes a new {@link DefaultAddSessionParameter}.
     */
    public DefaultAddSessionParameter() {
        super();
        enhancements = null;
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

    @Override
    public boolean isStaySignedIn() {
        return staySignedIn;
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

    /**
     * Sets the "stay signed in" flag.
     *
     * @param staySignedIn The flag to set
     * @return This {@code DefaultAddSessionParameter} with new attribute applied
     */
    public DefaultAddSessionParameter setStaySignedIn(boolean staySignedIn) {
        this.staySignedIn = staySignedIn;
        return this;
    }

    @Override
    public ArrayList<SessionEnhancement> getEnhancements() {
        return enhancements;
    }

    /**
     * Adds given <code>SessionEnhancement</code> instance to these parameters.
     *
     * @param enhancement The enhancement to add
     */
    public void addEnhancement(SessionEnhancement enhancement) {
        if (null != enhancement) {
            ArrayList<SessionEnhancement> enhancements = this.enhancements;
            if (null == enhancements) {
                enhancements = new ArrayList<>(4);
                this.enhancements = enhancements;
            }
            enhancements.add(enhancement);
        }
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the User-Agent string.
     *
     * @param userAgent The User-Agent string to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Origin getOrigin() {
        return origin;
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
