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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.session.reservation;

import com.openexchange.authentication.SessionEnhancement;

/**
 * {@link ReservationInfo} - Provides the information used to redeem a reservation for a valid session instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class ReservationInfo {

    private final String token;
    private String fullLogin;
    private String password;
    private String loginInfo;
    private String clientIp;
    private String authId;
    private String hash;
    private String client;
    private boolean isTransient;
    private SessionEnhancement enhancement;

    /**
     * Initializes a new {@link ReservationInfo}.
     */
    public ReservationInfo(String token) {
        super();
        this.token = token;
    }

    /**
     * Gets the token
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

    // -----------------------------------------------------------------------------------------------------

    /**
     * Gets the full login incl. context information; e.g <code>test@foo</code>
     *
     * @return The full login
     */
    public String getFullLogin() {
        return fullLogin;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user login information.
     *
     * @return The user login information
     */
    public String getUserLoginInfo() {
        return loginInfo;
    }

    /**
     * Gets the IP address of the connected client.
     *
     * @return The IP address
     */
    public String getClientIP() {
        return clientIp;
    }

    /**
     * Gets the authentication identifier.
     *
     * @return The authentication identifier
     */
    public String getAuthId() {
        return authId;
    }

    /**
     * Gets the hash.
     *
     * @return The hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets the client identifier
     *
     * @return the identifier of the client using the session.
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets a value indicating whether the session should be created in a transient way or not, i.e. the session should not be distributed
     * to other nodes in the cluster or put into another persistent storage.
     *
     * @return <code>true</code> if the session should be transient, <code>false</code>, otherwise.
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * A callback for modifying the session after it is created. This allows to put arbitrary additional information into a newly created
     * session. Normally some parameters are added. Use this to get this arbitrary information published to the whole cluster.
     *
     * @return a callback for modifying the session after its creation or <code>null</code> if no modification should take place.
     */
    public SessionEnhancement getEnhancement() {
        return enhancement;
    }

    /**
     * Sets the full login
     *
     * @param fullLogin The full login to set
     */
    public void setFullLogin(String fullLogin) {
        this.fullLogin = fullLogin;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the login info
     *
     * @param loginInfo The login info to set
     */
    public void setLoginInfo(String loginInfo) {
        this.loginInfo = loginInfo;
    }

    /**
     * Sets the client IP address
     *
     * @param clientIp The client IP address to set
     */
    public void setClientIP(String clientIp) {
        this.clientIp = clientIp;
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
     * Sets the hash
     *
     * @param hash The hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Sets the client identifier
     *
     * @param client The client identifier to set
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * Sets the transient flag
     *
     * @param isTransient The transient flag to set
     */
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    /**
     * Sets the enhancement
     *
     * @param enhancement The enhancement to set
     */
    public void setEnhancement(SessionEnhancement enhancement) {
        this.enhancement = enhancement;
    }

}
