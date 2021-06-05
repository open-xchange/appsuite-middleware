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

package com.openexchange.ajax.fields;

/**
 * JSON attribute names definitions.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginFields {

    public static final String NAME_PARAM = "name";

    public static final String PASSWORD_PARAM = "password";

    public static final String UI_WEB_PATH_PARAM = "uiWebPath";

    public static final String LOGIN_PARAM = "login";

    public static final String AUTHID_PARAM = "authId";

    public static final String CLIENT_PARAM = "client";

    public static final String VERSION_PARAM = "version";

    public static final String RANDOM_PARAM = "random";

    public static final String AUTOLOGIN_PARAM = "autologin";

    public static final String CLIENT_IP_PARAM = "clientIP";

    public static final String USER_AGENT = "clientUserAgent";

    public static final String CLIENT_TOKEN = "clientToken";

    public static final String SERVER_TOKEN = "serverToken";

    public static final String TOKEN = "token";

    public static final String APPSECRET = "secret";

    public static final String REDIRECT_URL = "redirectUrl";

    public static final String SHARE_TOKEN = "share";

    public static final String LANGUAGE_PARAM = "language";

    public static final String STORE_LANGUAGE = "storeLanguage";
    
    public static final String LOCALE_PARAM = "locale";

    public static final String STORE_LOCALE = "storeLocale";

    public static final String TRANSIENT = "transient";

    public static final String STAY_SIGNED_IN = "staySignedIn";

    private LoginFields() {
        super();
    }
}
