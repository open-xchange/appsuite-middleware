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

package com.openexchange.session.delegate;

import java.util.Set;
import com.openexchange.session.Session;
import com.openexchange.session.SetableSession;

/**
 * {@link DelegateSession} - The delegate session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DelegateSession implements SetableSession {

    protected final Session session;
    protected volatile String password;
    protected volatile int contextId;
    protected volatile int userId;

    /**
     * Initializes a new {@link DelegateSession}.
     */
    public DelegateSession(final Session session) {
        super();
        this.session = session;
        contextId = -1;
        userId = -1;
    }

    @Override
    public void setContextId(final int contextId) {
        this.contextId = contextId;
    }

    @Override
    public void setUserId(final int userId) {
        this.userId = userId;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public int getContextId() {
        final int contextId = this.contextId;
        return contextId <= 0 ? session.getContextId() : contextId;
    }

    @Override
    public String getLocalIp() {
        return session.getLocalIp();
    }

    @Override
    public void setLocalIp(final String ip) {
        session.setLocalIp(ip);
    }

    @Override
    public String getLoginName() {
        return session.getLoginName();
    }

    @Override
    public boolean containsParameter(final String name) {
        return session.containsParameter(name);
    }

    @Override
    public Object getParameter(final String name) {
        return session.getParameter(name);
    }

    @Override
    public String getPassword() {
        final String password = this.password;
        return null == password ? session.getPassword() : password;
    }

    @Override
    public String getRandomToken() {
        return session.getRandomToken();
    }

    @Override
    public String getSecret() {
        return session.getSecret();
    }

    @Override
    public String getSessionID() {
        return session.getSessionID();
    }

    @Override
    public int getUserId() {
        final int userId = this.userId;
        return userId <= 0 ? session.getUserId() : userId;
    }

    @Override
    public String getUserlogin() {
        return session.getUserlogin();
    }

    @Override
    public String getLogin() {
        return session.getLogin();
    }

    @Override
    public void setParameter(final String name, final Object value) {
        session.setParameter(name, value);
    }

    @Override
    public String getAuthId() {
        return session.getAuthId();
    }

    @Override
    public String getHash() {
        return session.getHash();
    }

    @Override
    public void setHash(final String hash) {
        session.setHash(hash);
    }

    @Override
    public String getClient() {
        return session.getClient();
    }

    @Override
    public void setClient(final String client) {
        session.setClient(client);
    }

    @Override
    public boolean isTransient() {
        return session.isTransient();
    }

    @Override
    public Set<String> getParameterNames() {
        return session.getParameterNames();
    }
}
