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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * Stores the values of the redirect URL returned by the formLogin action of the login servlet.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLoginResponse extends AbstractAJAXResponse {

    private final String path;
    private final String sessionId;
    private final String login;
    private final int userId;
    private final String language;
    private final boolean store;

    protected FormLoginResponse(String path, String sessionId, String login, int userId, String language, boolean store) {
        super(null);
        this.path = path;
        this.sessionId = sessionId;
        this.login = login;
        this.userId = userId;
        this.language = language;
        this.store = store;
    }

    public String getPath() {
        return path;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getLogin() {
        return login;
    }

    public int getUserId() {
        return userId;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isStore() {
        return store;
    }
}
