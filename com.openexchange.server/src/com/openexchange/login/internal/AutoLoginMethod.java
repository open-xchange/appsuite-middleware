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

package com.openexchange.login.internal;

import java.util.Map;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;

/**
 * Uses the auto login authentication method to perform the authentication.
 * @see AuthenticationService#handleAutoLoginInfo(com.openexchange.authentication.LoginInfo)
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class AutoLoginMethod implements LoginMethodClosure {

    private final LoginRequest request;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link AutoLoginMethod}.
     *
     * @param request The login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     */
    AutoLoginMethod(LoginRequest request, Map<String, Object> properties) {
        super();
        this.request = request;
        this.properties = properties;
    }

    @Override
    public Authenticated doAuthentication(LoginResultImpl retval) throws OXException {
        try {
            return Authentication.autologin(request.getLogin(), request.getPassword(), properties);
        } catch (OXException e) {
            if (LoginExceptionCodes.NOT_SUPPORTED.equals(e)) {
                return null;
            }
            throw e;
        }
    }
}
