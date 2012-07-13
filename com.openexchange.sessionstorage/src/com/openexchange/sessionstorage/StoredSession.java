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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.session.Session;

/**
 * {@link StoredSession}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class StoredSession implements Session {

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
    
    private String userLogin;

    private Map<String, Object> parameters;

    /**
     * Initializes a new {@link StoredSession}.
     */
    public StoredSession(Session session) {
       this.authId = session.getAuthId();
       this.client = session.getClient();
       this.contextId = session.getContextId();
       this.hash = session.getHash();
       this.localIp = session.getLocalIp();
       this.login = session.getLogin();
       this.loginName = session.getLoginName();
       this.parameters = new HashMap<String, Object>();
       this.parameters.put(Session.PARAM_LOCK, session.getParameter(Session.PARAM_LOCK));
       this.parameters.put(Session.PARAM_COUNTER, session.getParameter(Session.PARAM_COUNTER));
       this.parameters.put(Session.PARAM_ALTERNATIVE_ID, session.getParameter(Session.PARAM_ALTERNATIVE_ID));
       this.parameters.put(Session.PARAM_CAPABILITIES, session.getParameter(Session.PARAM_CAPABILITIES));
       this.password = session.getPassword();
       this.randomToken = session.getRandomToken();
       this.secret = session.getSecret();
       this.sessionId = session.getSessionID();
       this.userId = session.getUserId();
       this.userLogin = session.getUserlogin();
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getRandomToken() {
        return randomToken;
    }

    public void setRandomToken(String randomToken) {
        this.randomToken = randomToken;
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }

    @Override
    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    @Override
    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public void setHash(String hash) {
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
    
    public String getUserLogin() {
        return userLogin;
    }
    
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean containsParameter(String name) {
        Object value = parameters.get(name);
        return value == null;
    }

    @Override
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String getSessionID() {
        return sessionId;
    }

    @Override
    public String getUserlogin() {
        return loginName;
    }

    @Override
    public void setParameter(String name, Object value) {
        if (parameters.containsKey(name)) {
            parameters.remove(name);
        }
        parameters.put(name, value);
    }

    @Override
    public void removeRandomToken() {
        randomToken = null;
    }


}
