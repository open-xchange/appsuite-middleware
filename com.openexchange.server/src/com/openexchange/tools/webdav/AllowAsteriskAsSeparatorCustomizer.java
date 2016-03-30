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
        };
    }
}
