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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.application.AppAuthenticatorService;
import com.openexchange.authentication.application.AppLoginRequest;
import com.openexchange.authentication.application.RestrictedAuthentication;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Uses the normal login authentication method to perform the authentication.
 * 
 * @see AuthenticationService#handleLoginInfo(com.openexchange.authentication.LoginInfo)
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 */
final class NormalLoginMethod implements LoginMethodClosure {

    private final Map<String, Object> properties;
    private final LoginRequest request;

    /**
     * Initializes a new {@link NormalLoginMethod}.
     *
     * @param request The login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     */
    NormalLoginMethod(LoginRequest request, Map<String, Object> properties) {
        super();
        this.request = request;
        this.properties = properties;
    }

    @Override
    public Authenticated doAuthentication(final LoginResultImpl retval) throws OXException {
        AppAuthenticatorService appAuthenticator = ServerServiceRegistry.getInstance().getService(AppAuthenticatorService.class);
        if (null != appAuthenticator) {
            AppLoginRequest appLoginRequest = getAppLoginRequest(request);
            if (appAuthenticator.applies(appLoginRequest)) {
                RestrictedAuthentication authentication = appAuthenticator.doAuth(appLoginRequest);
                if (null != authentication) {
                    return authentication;
                }
            }
        }
        return Authentication.login(request.getLogin(), request.getPassword(), properties);
    }

    private static AppLoginRequest getAppLoginRequest(LoginRequest request) {
        return new AppLoginRequest() {

            @Override
            public String getUserAgent() {
                return request.getUserAgent();
            }

            @Override
            public String getPassword() {
                return request.getPassword();
            }

            @Override
            public Map<String, Object> getParameters() {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(com.openexchange.login.LoginRequest.class.getName(), request);
                return parameters;
            }

            @Override
            public String getLogin() {
                return request.getLogin();
            }

            @Override
            public String getClientIP() {
                return request.getClientIP();
            }

            @Override
            public String getClient() {
                return request.getClient();
            }
        };
    }

}
