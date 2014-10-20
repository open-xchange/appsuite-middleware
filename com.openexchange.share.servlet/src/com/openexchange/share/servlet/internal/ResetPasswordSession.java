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

package com.openexchange.share.servlet.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.session.Session;

/**
 * {@link ResetPasswordSession}
 *
 * Simulated session used to read internal free/busy data.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResetPasswordSession implements Session {

    private final int contextId;
    private final int userId;
    private final HttpServletRequest request;
    private final ConcurrentMap<String, Object> parameters;

    /**
     * Initializes a new {@link ResetPasswordSession}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param request The associated HTTP request
     */
    public ResetPasswordSession(int userId, int contextId, String password, HttpServletRequest request) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.request = request;
        parameters = new ConcurrentHashMap<String, Object>(4);
    }

    @Override
    public int getContextId() {
        return this.contextId;
    }

    @Override
    public int getUserId() {
        return this.userId;
    }

    @Override
    public String getLocalIp() {
        return request.getRemoteAddr();
    }

    @Override
    public void setLocalIp(String ip) {
        // Nothing to do

    }

    @Override
    public String getLoginName() {
        // Nothing to do
        return null;
    }

    @Override
    public boolean containsParameter(String name) {
        return null == name ? false : parameters.containsKey(name);
    }

    @Override
    public Object getParameter(String name) {
        return null == name ? null : parameters.get(name);
    }

    @Override
    public String getPassword() {
        // Nothing to do
        return null;
    }

    @Override
    public String getRandomToken() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSecret() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSessionID() {
        // Nothing to do
        return null;
    }

    @Override
    public String getUserlogin() {
        // Nothing to do
        return null;
    }

    @Override
    public String getLogin() {
        // Nothing to do
        return null;
    }

    @Override
    public void setParameter(String name, Object value) {
        if (null != name) {
            if (null == value) {
                parameters.remove(name);
            } else {
                parameters.put(name, value);
            }
        }
    }

    @Override
    public Set<String> getParameterNames() {
        return Collections.unmodifiableSet(parameters.keySet());
    }

    @Override
    public String getAuthId() {
        // Nothing to do
        return null;
    }

    @Override
    public String getHash() {
        // Nothing to do
        return null;
    }

    @Override
    public void setHash(String hash) {
        // Nothing to do
    }

    @Override
    public String getClient() {
        // Nothing to do
        return null;
    }

    @Override
    public void setClient(String client) {
        // Nothing to do
    }

    @Override
    public boolean isTransient() {
        return true;
    }

}
