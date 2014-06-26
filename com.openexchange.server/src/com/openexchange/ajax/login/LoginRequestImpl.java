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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.login;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.Cookie;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;

/**
 * {@link LoginRequestImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginRequestImpl implements LoginRequest {

    private final String login, password, clientIP, userAgent, authId, client, version, hash;
    private String clientToken;
    private final Interface iface;
    private final Map<String, List<String>> headers;
    private final Cookie[] cookies;
    private final boolean secure;
    private final String serverName;
    private final int serverPort;
    private final String httpSessionID;
    private boolean tranzient;

    public LoginRequestImpl(String login, String password, String clientIP, String userAgent, String authId, String client, String version, String hash, Interface iface, Map<String, List<String>> headers, Cookie[] cookies, boolean secure, String serverName, int serverPort, String httpSessionID) {
        super();
        this.login = login;
        this.password = password;
        this.clientIP = clientIP;
        this.userAgent = userAgent;
        this.authId = authId;
        this.client = client;
        this.version = version;
        this.hash = hash;
        this.iface = iface;
        this.headers = headers;
        this.cookies = cookies;
        this.secure = secure;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.httpSessionID = httpSessionID;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getClientIP() {
        return clientIP;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public Interface getInterface() {
        return iface;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getHttpSessionID() {
        return httpSessionID;
    }

    @Override
    public boolean isTransient() {
        return tranzient;
    }
    
    /**
     * Sets if whether the session should be created in a transient way or not, i.e. the session should not be distributed to other nodes 
     * in the cluster or put into another persistent storage.
     * 
     * @param tranzient <code>true</code> if the session should be transient, <code>false</code>, otherwise
     */
    public void setTransient(boolean tranzient) {
        this.tranzient = tranzient;
    }

}
