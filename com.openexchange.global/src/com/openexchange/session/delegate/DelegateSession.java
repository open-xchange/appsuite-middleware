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

package com.openexchange.session.delegate;

import java.util.Set;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;
import com.openexchange.session.SetableSession;

/**
 * {@link DelegateSession} - The delegate session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
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
    public boolean isStaySignedIn() {
        return session.isStaySignedIn();
    }

    @Override
    public Set<String> getParameterNames() {
        return session.getParameterNames();
    }

    @Override
    public Origin getOrigin() {
        return session.getOrigin();
    }

}
