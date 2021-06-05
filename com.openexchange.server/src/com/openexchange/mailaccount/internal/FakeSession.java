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

package com.openexchange.mailaccount.internal;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;

/**
 * {@link FakeSession}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class FakeSession implements Session, Serializable {

    private static final long serialVersionUID = -4527564586038651789L;

    private final String password;
    private final int userId;
    private final int contextId;
    private final ConcurrentMap<String, Object> parameters;

    FakeSession(final String password, final int userId, final int contextId) {
        super();
        this.password = password;
        this.userId = userId;
        this.contextId = contextId;
        parameters = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String getLocalIp() {
        return null;
    }

    @Override
    public void setLocalIp(final String ip) {
        // Nothing to do
    }

    @Override
    public String getLoginName() {
        return null;
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getRandomToken() {
        return null;
    }

    @Override
    public String getSecret() {
        return null;
    }

    @Override
    public String getSessionID() {
        return null;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getUserlogin() {
        return null;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public void setParameter(final String name, final Object value) {
        if (null == value) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

    @Override
    public String getAuthId() {
        return null;
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public void setHash(final String hash) {
        // Nope
    }

    @Override
    public String getClient() {
        return null;
    }

    @Override
    public void setClient(final String client) {
        // Nothing to do
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isStaySignedIn() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    @Override
    public Origin getOrigin() {
        return Origin.SYNTHETIC;
    }

}
