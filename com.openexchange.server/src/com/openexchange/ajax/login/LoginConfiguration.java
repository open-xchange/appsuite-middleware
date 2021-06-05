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

package com.openexchange.ajax.login;

import com.openexchange.configuration.CookieHashSource;

/**
 * Object to store the configuration parameters for the different login process mechanisms.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class LoginConfiguration {

    private final String uiWebPath;
    private final CookieHashSource hashSource;
    private final String httpAuthAutoLogin;
    private final String defaultClient;
    private final String clientVersion;
    private final String errorPageTemplate;
    private final int cookieExpiry;
    private final boolean insecure;
    private final boolean cookieForceHTTPS;
    private final boolean redirectIPChangeAllowed;
    private final boolean disableTrimLogin;
    private final boolean formLoginWithoutAuthId;
    private final boolean isRandomTokenEnabled;
    private final boolean checkPunyCodeLoginString;

    public LoginConfiguration(String uiWebPath, CookieHashSource hashSource, String httpAuthAutoLogin, String defaultClient, String clientVersion, String errorPageTemplate, int cookieExpiry, boolean cookieForceHTTPS, boolean insecure, boolean redirectIPChangeAllowed, boolean disableTrimLogin, boolean formLoginWithoutAuthId, boolean isRandomTokenEnabled, boolean checkPunyCodeLoginString) {
        super();
        this.uiWebPath = uiWebPath;
        this.hashSource = hashSource;
        this.httpAuthAutoLogin = httpAuthAutoLogin;
        this.defaultClient = defaultClient;
        this.clientVersion = clientVersion;
        this.errorPageTemplate = errorPageTemplate;
        this.cookieExpiry = cookieExpiry;
        this.cookieForceHTTPS = cookieForceHTTPS;
        this.insecure = insecure;
        this.redirectIPChangeAllowed = redirectIPChangeAllowed;
        this.disableTrimLogin = disableTrimLogin;
        this.formLoginWithoutAuthId = formLoginWithoutAuthId;
        this.isRandomTokenEnabled = isRandomTokenEnabled;
        this.checkPunyCodeLoginString = checkPunyCodeLoginString;
    }

    public String getUiWebPath() {
        return uiWebPath;
    }

    public CookieHashSource getHashSource() {
        return hashSource;
    }

    public String getHttpAuthAutoLogin() {
        return httpAuthAutoLogin;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getErrorPageTemplate() {
        return errorPageTemplate;
    }

    public int getCookieExpiry() {
        return cookieExpiry;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public boolean isCookieForceHTTPS() {
        return cookieForceHTTPS;
    }

    public boolean isRedirectIPChangeAllowed() {
        return redirectIPChangeAllowed;
    }

    public boolean isDisableTrimLogin() {
        return disableTrimLogin;
    }

    public boolean isFormLoginWithoutAuthId() {
        return formLoginWithoutAuthId;
    }

    public boolean isRandomTokenEnabled() {
        return isRandomTokenEnabled;
    }

    public boolean isCheckPunyCodeLoginString() {
        return checkPunyCodeLoginString;
    }
}