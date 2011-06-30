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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

/**
 * {@link SimSession}
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SimSession implements Session {

    private String loginName;
    private String randomToken;
    private String sessionId;
    private Map<String, Object> parameters = new HashMap<String, Object>();
    private int contextId;
    private int userId;
    private String password;

    public SimSession() {
        super();
    }

    public boolean containsParameter(String name) {
        return parameters.containsKey(name);
    }

    public String getAuthId() {
        return null;
    }

    public int getContextId() {
        return contextId;
    }

    public String getLocalIp() {
        return null;
    }

    public String getLogin() {
        return null;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Object getParameter(String name) {
        return parameters.get(name);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRandomToken() {
        return randomToken;
    }

    public void setRandomToken(String randomToken) {
        this.randomToken = randomToken;
    }

    public void removeRandomToken() {
        randomToken = null;
    }

    public String getSecret() {
        return null;
    }

    public String getSessionID() {
        return sessionId;
    }

    public void setSessionID(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserlogin() {
        return null;
    }

    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public String getHash() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.session.Session#setLocalIp(java.lang.String)
     */
    public void setLocalIp(String ip) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.session.Session#setHash(java.lang.String)
     */
    public void setHash(String hash) {
        // TODO Auto-generated method stub

    }

    /**
     * Sets the contextId
     * 
     * @param contextId The contextId to set
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the userId
     * 
     * @param userId The userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getClient() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setClient(String client) {
        // TODO Auto-generated method stub
    }
}
