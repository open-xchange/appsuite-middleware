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

package com.openexchange.ajax.oauth.provider.protocol;

import com.openexchange.java.util.UUIDs;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthParams {

    private String scheme = "https";
    private String hostname = "localhost";
    private int port = 443;
    private String clientId;
    private String redirectURI;
    private String scope;
    private String state = UUIDs.getUnformattedStringFromRandom();
    private String responseType = "code";
    private String clientSecret;

    public OAuthParams setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public OAuthParams setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public OAuthParams setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public OAuthParams setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
        return this;
    }

    public OAuthParams setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public OAuthParams setState(String state) {
        this.state = state;
        return this;
    }

    public OAuthParams setResponseType(String responseType) {
        this.responseType = responseType;
        return this;
    }

    public OAuthParams setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }


    public OAuthParams setPort(int port) {
        this.port = port;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHostname() {
        return hostname;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public String getScope() {
        return scope;
    }

    public String getState() {
        return state;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public int getPort() {
        return port;
    }

}
