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

package com.openexchange.tools.webdav;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.Cookie;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;

/**
 * {@link AllowAsteriskAsSeparatorCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AllowAsteriskAsSeparatorCustomizer implements LoginCustomizer {

    @Override
    public LoginRequest modifyLogin(final LoginRequest loginReq) {
        return new LoginRequest() {

            @Override
            public String getAuthId() {
                return loginReq.getAuthId();
            }

            @Override
            public String getClient() {
                return loginReq.getClient();
            }

            @Override
            public String getClientIP() {
                return loginReq.getClientIP();
            }

            @Override
            public String getHash() {
                return loginReq.getHash();
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return loginReq.getHeaders();
            }

            @Override
            public Map<String, String[]> getRequestParameter() {
                return loginReq.getRequestParameter();
            }

            @Override
            public Interface getInterface() {
                return loginReq.getInterface();
            }

            @Override
            public String getLogin() {
                return loginReq.getLogin().replaceAll("\\*", "@");
            }

            @Override
            public String getPassword() {
                return loginReq.getPassword();
            }

            @Override
            public String getUserAgent() {
                return loginReq.getUserAgent();
            }

            @Override
            public String getVersion() {
                return loginReq.getVersion();
            }

            @Override
            public Cookie[] getCookies() {
                return loginReq.getCookies();
            }

            @Override
            public boolean isSecure() {
                return loginReq.isSecure();
            }

            @Override
            public String getServerName() {
                return loginReq.getServerName();
            }

            @Override
            public int getServerPort() {
                return loginReq.getServerPort();
            }

            @Override
            public String getHttpSessionID() {
                return loginReq.getHttpSessionID();
            }

            @Override
            public boolean markHttpSessionAuthenticated() {
                return loginReq.markHttpSessionAuthenticated();
            }

            @Override
            public String getClientToken() {
                return loginReq.getClientToken();
            }

            @Override
            public boolean isTransient() {
                return loginReq.isTransient();
            }

            @Override
            public String getLanguage() {
                return loginReq.getLanguage();
            }

            @Override
            public boolean isStoreLanguage() {
                return loginReq.isStoreLanguage();
            }

            @Override
            public String getLocale() {
                return loginReq.getLocale();
            }

            @Override
            public boolean isStoreLocale() {
                return loginReq.isStoreLocale();
            }

            @Override
            public boolean isStaySignedIn() {
                return loginReq.isStaySignedIn();
            }
        };
    }
}
